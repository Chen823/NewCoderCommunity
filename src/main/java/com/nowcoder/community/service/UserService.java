package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    public User findUserById(int id){
        User user = getCache(id);
        if(user == null){
            user = initCache(id);
        }
        return user;
    }
    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    public Map<String,Object> register(User user){
        Map<String, Object> map = new HashMap<>();
        // 空值处理(用户/用户名/密码/邮箱)
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        if(StringUtils.isBlank(user.getUsername())){
            map.put("UsernameMsg","用户名不能为空");
            return map;
        }

        if(StringUtils.isBlank(user.getPassword())){
            map.put("PasswordMsg","密码不能为空");
            return map;
        }

        if(StringUtils.isBlank(user.getEmail())){
            map.put("EmailMsg","邮箱不能为空");
            return map;
        }
        // 验证信息重复性(账号/邮箱)
        if(userMapper.selectByName(user.getUsername()) != null){
            map.put("UsernameMsg","用户名已存在");
            return map;
        }

        if(userMapper.selectByEmail(user.getEmail()) != null){
            map.put("EmailMsg","邮箱已存在");
            return map;
        }
        // 注册用户
        // 盐值为长度是5的随机字符串
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        // 激活邮件
        // http://localhost:8080/community/activation/101/code
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        context.setVariable("url", domain + contextPath + "/activation/"  + user.getId() + "/" + user.getActivationCode());
        String content = templateEngine.process("mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }

    public int activation(int UserId, String code){
        User user = userMapper.selectById(UserId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(UserId,1);
            deleteCache(UserId);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username, String password, int expired) {
        Map<String,Object> map = new HashMap<>();
        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        //账号验证
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        //激活验证
        if(user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }
        //密码验证
        if(!user.getPassword().equals(CommunityUtil.md5(password + user.getSalt()))){
            map.put("passwordMsg","密码错误");
            return map;
        }
        //生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        String ticket = CommunityUtil.generateUUID();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(ticket);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expired * 1000));
        //将ticket存入Redis
        String ticketRedisKey = RedisKeyUtil.getTicketRedisKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketRedisKey,loginTicket);
        //loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("loginTicket",loginTicket.getTicket());
        return map;
    }

        public void logout(String ticket){
          String ticketRedisKey = RedisKeyUtil.getTicketRedisKey(ticket);
          LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketRedisKey);
          loginTicket.setStatus(1);
          redisTemplate.opsForValue().set(ticketRedisKey,loginTicket);
          //loginTicketMapper.updateStatus(ticket,1);
        }

        public Map<String,Object> forget(String email){
            //空值处理
            Map<String,Object> map = new HashMap<>();
            if(StringUtils.isBlank(email)){
                map.put("emailMsg","请输入邮箱！");
                return map;
            }
            User user = userMapper.selectByEmail(email);
            if(user == null){
                map.put("emailMsg","该用户不存在！");
                return map;
            }
            //生成验证码
                String sources = "0123456789ABCDEFGHIJKMLNOPQRSTUVWSYZ"; // 加上一些字母，就可以生成pc站的验证码了
                Random rand = new Random();
                StringBuffer flag = new StringBuffer();
                for (int j = 0; j < 6; j++)
                {
                    flag.append(sources.charAt(rand.nextInt(9)) + "");
                }
                String code = flag.toString();
                map.put("code",code);

            //发送邮件
            Context context = new Context();
            context.setVariable("email",user.getEmail());
            context.setVariable("code",code);
            String content = templateEngine.process("mail/forget", context);
            mailClient.sendMail(user.getEmail(),"重置密码",content);
            map.put("user",user);
            return map;
        }
        public Map<String, Object> resetPassword(String email, String password) {
            Map<String,Object> map = new HashMap<>();
            //空值验证
            if(StringUtils.isBlank(email)){
                map.put("emailMsg","请输入邮箱！");
                return map;
            }
            if(StringUtils.isBlank(email)){
                map.put("passwordMsg","请输入密码！");
                return map;
            }
            //更改密码
            User user = userMapper.selectByEmail(email);
            if(user == null){
                map.put("emailMsg","该用户不存在！");
                return map;
            }
            userMapper.updatePassword(user.getId(),CommunityUtil.md5(password + user.getSalt()));
            deleteCache(user.getId());
            map.put("user",user);
            return map;
        }

        public LoginTicket findByTicket(String ticket){
            String ticketRedisKey = RedisKeyUtil.getTicketRedisKey(ticket);
            LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketRedisKey);
            return loginTicket;
        }

        public int updateHeader(int userId, String headerUrl){
            int rows = userMapper.updateHeader(userId, headerUrl);
            if(rows == 1){
                deleteCache(userId);
            }
            return rows;
        }

        public int updatePassword(int userId, String password){
            int rows = userMapper.updatePassword(userId, password);
            if(rows == 1){
                deleteCache(userId);
            }
            return rows;
        }
        public User findByName(String name){
            return userMapper.selectByName(name);
        }

        //1.优先从缓存中取值
        public User getCache(int userId){
            String userRedisKey = RedisKeyUtil.getUserRedisKey(userId);
            User user = (User)redisTemplate.opsForValue().get(userRedisKey);
            return user;
        }
        //2.如果取不到数据则初始化缓存数据
        public User initCache(int userId){
            String userRedisKey = RedisKeyUtil.getUserRedisKey(userId);
            User user = userMapper.selectById(userId);
            redisTemplate.opsForValue().set(userRedisKey, user);
            return user;
        }
        //3.如果数据发生变更则删除缓存数据
        public void deleteCache(int userId){
            String userRedisKey = RedisKeyUtil.getUserRedisKey(userId);
            redisTemplate.delete(userRedisKey);
        }

}
