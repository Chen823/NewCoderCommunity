package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello Spring Boot.";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    //静态页面
    @RequestMapping(path = "/student" , method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age){
        System.out.println(name);
        System.out.println(age);
        return "success";
    }
    //向浏览器响应html数据
    //动态页面
    @RequestMapping(path = "/school" , method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name","燕山大学");
        model.addAttribute("age",50);
        return "/demo/school";
    }

    @RequestMapping(path = "/emp" , method = RequestMethod.GET)
    @ResponseBody
    public Map getEmp(){
        Map map = new HashMap<>();
        map.put("name","ss");
        map.put("age",22);
        return map;

    }

    @RequestMapping(path = "/cookie/set" , method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(HttpServletResponse httpServletResponse){
        //创建Cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置生效范围
        cookie.setPath("/community/alpha");
        //设置生存时间
        cookie.setMaxAge(10 * 60);
        //发送Cookie
        httpServletResponse.addCookie(cookie);
        return "add cookie";

    }


}
