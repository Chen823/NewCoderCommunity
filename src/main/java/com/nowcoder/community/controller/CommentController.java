package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostMapperService;
import com.nowcoder.community.service.ElasticsearchService;
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

import java.util.*;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant{

    @Autowired
    CommentService commentService;
    @Autowired
    DiscussPostMapperService discussPostMapperService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private UserService userService;




    @RequestMapping(path = "/add/{discussPostId}" , method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        commentService.addComment(comment);
        //发送消息
        Event event = new Event();
        event.setTopic(TOPIC_TYPE_COMMENT)
                .setEntityType(comment.getEntityType())
                        .setEntityId(comment.getEntityId())
                                .setUserId(hostHolder.getUser().getId())
                                        .setData("postId",discussPostId);
        int entityUserId = 0;
        if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
            //说明是给帖子的回复
            entityUserId = discussPostMapperService.findDiscussPostById(discussPostId).getUserId();
        }else if(comment.getEntityType() == ENTITY_TYPE_REPLY){
            //说明是给回复的评论
            entityUserId = commentService.findSingleCommentById(comment.getEntityId()).getUserId();
        }
        event.setEntityUserId(entityUserId);
        eventProducer.sendEvent(event);

        //将帖子数据提交到es服务器
        if(comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            event = new Event();
            event.setTopic(TOPIC_TYPE_POST)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_COMMENT)
                    .setEntityId(discussPostId);
            eventProducer.sendEvent(event);
        }
        return "redirect:/discuss/detail/" + discussPostId;
    }

    //获取某一用户的回复列表
    @RequestMapping(path = "/{userId}" , method = RequestMethod.GET)
    public String getUserReplyList(@PathVariable("userId") int userId, Model model, Page page){
        //设置分页
        page.setLimit(5);
        page.setPath("/discuss/" + userId);
        commentService.findCommentRow(ENTITY_TYPE_REPLY,userId);
        int replyCount = commentService.findCommentRowById(ENTITY_TYPE_COMMENT,userId);
        page.setTotal(replyCount);
        //某一用户的帖子总数
        model.addAttribute("replyCount",replyCount);
        //获取用户的回复列表
        List<Comment> comment = commentService.findCommentById(CommunityConstant.ENTITY_TYPE_COMMENT, userId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> list = new ArrayList<>();
        if(comment != null){
            for(Comment c : comment){
                Map<String,Object> map = new HashMap<>();
                int entityId = c.getEntityId();
                DiscussPost post = discussPostMapperService.findDiscussPostById(entityId);
                map.put("post",post);
                map.put("comment",c);
                list.add(map);
            }
        }
        User user = userService.findUserById(userId);
        if(user == null){
            return CommunityUtil.getJSONString(1,"该用户不存在！");
        }
        model.addAttribute("user",user);
        model.addAttribute("comment",list);
        return "/site/my-reply";
    }
}
