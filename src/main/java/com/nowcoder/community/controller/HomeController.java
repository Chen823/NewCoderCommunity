package com.nowcoder.community.controller;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostMapperService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;

@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostMapperService discussPostMapperService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @RequestMapping(path = "/index" , method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        page.setTotal(discussPostMapperService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> list = discussPostMapperService.findDiscussPost(0,page.getOffset(),page.getLimit());
        List<Map<String, User>> discussPost = new ArrayList<>();
        if(list != null){
            for(DiscussPost key : list){
                Map map = new HashMap<>();
                User user = userService.findUserById(key.getUserId());
                long likeCount = likeService.getLikeCount(ENTITY_TYPE_COMMENT, key.getId());
                map.put("likeCount",likeCount);
                map.put("post",key);
                map.put("user",user);
                discussPost.add(map);
            }
        model.addAttribute("discussPost",discussPost);
        }
        return "/index";
    }

    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }

}
