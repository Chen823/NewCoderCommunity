package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostMapperService;
import com.nowcoder.community.util.CommunityConstant;
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


    @RequestMapping(path = "/add/{discussPostId}" , method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        commentService.addComment(comment);
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
        model.addAttribute("comment",list);
        return "/site/my-reply";
    }
}
