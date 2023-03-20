package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/follow" , method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String setFollow(int entityType,int entityId){
        User user = hostHolder.getUser();
//        if(user == null){
//            return CommunityUtil.getJSONString(1,"用户未登录!");
//        }
        followService.follow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"关注成功！");
    }

    @RequestMapping(path = "/unfollow" , method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String unFollow(int entityType,int entityId){
        User user = hostHolder.getUser();
//        if(user == null){
//            return CommunityUtil.getJSONString(1,"用户未登录!");
//        }
        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"取消关注成功！");
    }

    @RequestMapping(path = "/followeeList/{userId}" , method = RequestMethod.GET)
    @LoginRequired
    public String getFolloweeList(@PathVariable("userId") int userId, Page page, Model model){
        //获取当前页面的用户
        User u = userService.findUserById(userId);
        if(u == null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",u);
        //设置分页
        page.setPath("/followeeList/" + userId);
        page.setLimit(6);
        page.setTotal((int)followService.getFolloweeCount(ENTITY_TYPE_USER,userId));
        //获取分页列表
        List<Map<String, Object>> followeeList = followService.getFolloweeList(userId, page.getOffset(), page.getLimit());
        if(followeeList != null) {
            for (Map<String, Object> map : followeeList) {
                User curUser = (User)map.get("user");
                boolean isFollow = loginUserIsFollow(curUser.getId());
                map.put("isFollow", isFollow);
            }
        }
        model.addAttribute("followeeList",followeeList);
        return "/site/followee";
    }

    @RequestMapping(path = "/followerList/{userId}" , method = RequestMethod.GET)
    @LoginRequired
    public String getFollowerList(@PathVariable("userId") int userId, Page page, Model model){
        //获取当前页面的用户
        User u = userService.findUserById(userId);
        if(u == null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",u);
        //设置分页
        page.setPath("/followerList/" + userId);
        page.setLimit(6);
        page.setTotal((int)followService.getFollowerCount(ENTITY_TYPE_USER,userId));
        //获取分页列表
        List<Map<String, Object>> followerList = followService.getFollowerList(userId, page.getOffset(), page.getLimit());
        if(followerList != null) {
            for (Map<String, Object> map : followerList) {
                User curUser = (User)map.get("user");
                boolean isFollow = loginUserIsFollow(curUser.getId());
                map.put("isFollow", isFollow);
            }
        }
        model.addAttribute("followerList",followerList);
        return "/site/follower";
    }

    public boolean loginUserIsFollow(int userId){
        User user = hostHolder.getUser();
        if(user == null){
            return false;
        }else{
            return followService.isFollow(user.getId(), ENTITY_TYPE_USER, userId);
        }
    }
}
