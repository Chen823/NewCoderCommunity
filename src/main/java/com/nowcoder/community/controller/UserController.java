package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Value("${community.path.upload}")
    private String upload;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/setting" , method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload" , method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headFigure, Model model){
        //判别空值
        if(headFigure == null){
            model.addAttribute("figureMsg","上传图片格式有误！");
            return "/site/setting";
        }
        //生成随机文件名
        String originalFilename = headFigure.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("." ) + 1);
        String headerName = CommunityUtil.generateUUID() + "." + suffix;
        //设置文件存放路径
        File dest = new File(upload + "/" + headerName);
        try {
            //使用transferTo(dest)方法将上传文件写到服务器上指定的文件;
            headFigure.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传图片失败!" + e.getMessage());
            throw new RuntimeException(e);
        }
        //更新当前用户的头像的路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.png

        int userId = hostHolder.getUser().getId();
        String headerUrl = domain  + contextPath + "/user/header/" + headerName;
        userService.updateHeader(userId,headerUrl);
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{headerName}" , method = RequestMethod.GET)
    public void getHeader(Model model, HttpServletResponse httpServletResponse,@PathVariable("headerName") String headerName){
        //定义文件的服务器存放路径
        headerName = upload + "/" + headerName;
        //获取文件后缀
        String suffix = headerName.substring(headerName.lastIndexOf("." ) + 1);
        //响应图片
        httpServletResponse.setContentType("image" + suffix);
        //边读边写入(放在try中可以实现自动关闭fis(java7后特性))
        try(ServletOutputStream os = httpServletResponse.getOutputStream();
            FileInputStream fis = new FileInputStream(headerName)) {
            byte[] buffer = new byte[1024];
            int b;
            while((b = fis.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败!" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(path = "/updatePassword" , method = RequestMethod.POST)
    public String updatePassword(String originPassword,String newPassword, Model model){
        //判别空值
        if(StringUtils.isBlank(originPassword)){
            model.addAttribute("originPasswordMsg","原密码不能为空！");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("newPasswordMsg","新密码不能为空！");
            return "/site/setting";
        }
        //验证原密码
        User user = hostHolder.getUser();
        String password = user.getPassword();
        originPassword = CommunityUtil.md5( originPassword + user.getSalt());
        if(!originPassword.equals(password)){
            model.addAttribute("originPasswordMsg","原密码输入错误！");
            return "/site/setting";
        }
        //更新密码
        newPassword = CommunityUtil.md5( newPassword + user.getSalt());
        userService.updatePassword(user.getId(),newPassword);
        return "redirect:/logout";
    }
}


