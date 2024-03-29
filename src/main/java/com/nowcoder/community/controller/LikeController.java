package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/like" , method = RequestMethod.POST)
    @ResponseBody
    public String setLike(int entityType,int entityId, int entityUserId, int postId){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(1,"用户未登录!");
        }
        likeService.setLike(user.getId(),entityType,entityId, entityUserId);
        long likeCount = likeService.getLikeCount(entityType, entityId);
        int likeStatus = likeService.getLikeStatus(user.getId(), entityType, entityId);
        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);
        //发送消息
        if(likeCount == 1) {
            Event event = new Event();
            event.setTopic(TOPIC_TYPE_LIKE)
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setUserId(user.getId())
                    .setData("postId", postId);
            eventProducer.sendEvent(event);
        }
        return CommunityUtil.getJSONString(0,null,map);
    }
}
