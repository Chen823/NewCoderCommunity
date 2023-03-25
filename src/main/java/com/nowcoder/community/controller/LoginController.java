package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;

import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    UserService userService;
    @Autowired
    Producer kaptchaProducer;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(path = "/forget" , method = RequestMethod.GET)
    public String getForgetPage(){
        return "/site/forget";
    }

    @RequestMapping(path = "/register" , method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path = "/login" , method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @RequestMapping(path = "/register" , method = RequestMethod.POST)
    public String register(Model model, User user){
        //参数中的user可以用于注册出错时直接访问上一次输入的用户名密码等信息 不用重新输入
        Map<String, Object> map = userService.register(user);
        //如果map中没有内容 则说明注册成功！
        if(map == null || map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请您尽快激活账号！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            //否则就是注册失败了
            model.addAttribute("UsernameMsg",map.get("UsernameMsg"));
            model.addAttribute("PasswordMsg",map.get("PasswordMsg"));
            model.addAttribute("EmailMsg",map.get("EmailMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(path = "/activation/{userId}/{code}" , method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code){
        // http://localhost:8080/community/activation/101/code
        int activation = userService.activation(userId, code);
        if(activation == ACTIVATION_SUCCESS){
            model.addAttribute("msg","您的账号已经激活成功,可以正常使用了!");
            model.addAttribute("target","/login");
        } else if (activation == ACTIVATION_REPEAT) {
            model.addAttribute("msg","无效操作，该账号已激活！");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","激活失败，您的激活码有误！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/kaptcha" , method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse httpServletResponse/*, HttpSession session*/){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        //将验证字符串存入session
        //session.setAttribute("kaptcha",text);
        //获取ownerKey
        String ownerKey = CommunityUtil.generateUUID();
        //将ownerKey存入Cookie中
        Cookie cookie = new Cookie("ownerKey",ownerKey);
        cookie.setPath(contextPath);
        cookie.setMaxAge(60);
        httpServletResponse.addCookie(cookie);
        //生成验证码的RedisKey
        String verifyCodeRedisKey = RedisKeyUtil.getVerifyCodeRedisKey(ownerKey);
        //将验证码存入Redis中
        redisTemplate.opsForValue().set(verifyCodeRedisKey,text,60, TimeUnit.SECONDS);
        //向浏览器发送验证码
        httpServletResponse.setContentType("image/png");
        try {
            ServletOutputStream os = httpServletResponse.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("验证码出现异常！"+ e.getMessage());
        }
    }

    @RequestMapping(path = "/login" , method = RequestMethod.POST)
    public String login(String username, String password, String code,boolean rememberme,Model model,
                        HttpServletResponse httpServletResponse,@CookieValue(value = "ownerKey",required = false) String ownerKey/*,HttpSession session*/) {
        //验证验证码
        String kaptcha = null;
        if(StringUtils.isNotBlank(ownerKey)){
            String verifyCodeRedisKey = RedisKeyUtil.getVerifyCodeRedisKey(ownerKey);
            kaptcha = (String) redisTemplate.opsForValue().get(verifyCodeRedisKey);
        }
        //String kaptcha = (String)session.getAttribute("kaptcha");
        if(StringUtils.isBlank(code) || StringUtils.isBlank(kaptcha) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码错误！");
            return "/site/login";
        }
        //设置生存时间
        int expiredSeconds =  rememberme ? REMEMBERME_EXPIRED_TIME : DEFAULT_EXPIRED_TIME;
        //登陆
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("loginTicket")){
            Cookie cookie = new Cookie("loginTicket",(String) map.get("loginTicket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            httpServletResponse.addCookie(cookie);
            return "redirect:/index";
        }else{
            String usernameMsg = (String) map.get("usernameMsg");
            String passwordMsg = (String) map.get("passwordMsg");
            model.addAttribute("usernameMsg",usernameMsg);
            model.addAttribute("passwordMsg",passwordMsg);
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout" , method = RequestMethod.GET)
    public String login(@CookieValue("loginTicket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    // 获取验证码并验证邮箱是否存在
    @RequestMapping(path = "/forget/code", method = RequestMethod.GET)
    @ResponseBody
    public String getForgetCode(String email, HttpSession session, Model model) {
        if(StringUtils.isBlank(email)){
            return CommunityUtil.getJSONString(1, "邮箱不能为空！");
        }
        Map<String, Object> map = userService.forget(email);
        if(!map.containsKey("user")){
            return CommunityUtil.getJSONString(1, "查询不到该邮箱注册信息");
        }else{
            session.setAttribute("code",map.get("code"));
            return CommunityUtil.getJSONString(0);
        }
    }

    // 重置密码
    @RequestMapping(path = "/forget/password", method = RequestMethod.POST)
    public String resetPassword(String email, String code, String password, Model model, HttpSession session) {
        String myCode  = (String) session.getAttribute("code");
        if (StringUtils.isBlank(code) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(myCode)) {
            model.addAttribute("codeMsg", "验证码错误!");
            return "/site/forget";
        }
        Map<String, Object> map = userService.resetPassword(email, password);
        if(map.containsKey("user")){
            model.addAttribute("msg","修改密码成功，请重新登录");
            model.addAttribute("target","/login");
            return "/site/operate-result";
        }else{
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }


    }
}
