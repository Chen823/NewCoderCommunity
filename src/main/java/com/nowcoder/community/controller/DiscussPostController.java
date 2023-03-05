package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostMapperService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostMapperService discussPostMapperService;

    @Autowired
    private UserService userService;


    @RequestMapping(path = "/add" , method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        //判断是否登录(403)
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403, "用户未登录！");
        }
        //添加数据
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPost.setUserId(user.getId());
        //报错未来统一处理
        discussPostMapperService.addDiscussPost(discussPost);
        return CommunityUtil.getJSONString(0,"发布成功");
    }

    @RequestMapping(path = "/detail/{discussPostId}" , method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model){
        DiscussPost discussPost = discussPostMapperService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);
        return "/site/discuss-detail";
    }
    

}
