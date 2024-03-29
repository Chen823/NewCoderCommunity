package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.*;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostMapperService discussPostMapperService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

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
        discussPostMapperService.addDiscussPost(discussPost);
        //将帖子数据提交到es服务器
        Event event = new Event();
        event.setTopic(TOPIC_TYPE_POST)
                .setUserId(discussPost.getUserId())
                .setEntityType(ENTITY_TYPE_COMMENT)
                .setEntityId(discussPost.getId());
        eventProducer.sendEvent(event);
        return CommunityUtil.getJSONString(0,"发布成功");
    }

    @RequestMapping(path = "/detail/{discussPostId}" , method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //帖子
        DiscussPost discussPost = discussPostMapperService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);
        //发帖用户
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);
        //帖子点赞数量
        long likeCount = likeService.getLikeCount(ENTITY_TYPE_COMMENT, discussPost.getId());
        model.addAttribute("likeCount",likeCount);
        //帖子点赞状态
        User loginUser = hostHolder.getUser();
        int likeStatus = loginUser == null ? 0 : likeService.getLikeStatus(loginUser.getId(),ENTITY_TYPE_COMMENT,discussPost.getId());
        model.addAttribute("likeStatus",likeStatus);
        //设置分页
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setTotal(discussPost.getCommentCount());
        //获取评论添加到map中(评论,作者)
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        List<Comment> commentList = commentService.findComment(ENTITY_TYPE_COMMENT, discussPost.getId(), page.getOffset(), page.getLimit());
        //添加评论
        if(commentList != null){
            for(Comment comment : commentList){
                Map<String,Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment",comment);
                //评论作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                //评论的点赞数目
                long commentLikeCount = likeService.getLikeCount(ENTITY_TYPE_REPLY, comment.getId());
                commentVo.put("commentLikeCount",commentLikeCount);
                //获取点赞的状态
                int commentLikeStatus = loginUser == null ? 0 : likeService.getLikeStatus(loginUser.getId(),ENTITY_TYPE_REPLY,comment.getId());
                commentVo.put("commentLikeStatus",commentLikeStatus);
                //获取评论的回复
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                List<Comment> replyList = commentService.findComment(ENTITY_TYPE_REPLY, comment.getId(), 0, Integer.MAX_VALUE);
                    if(replyList != null){
                        for(Comment reply : replyList){
                            Map<String,Object> replyVo = new HashMap<>();
                            //评论的回复
                            replyVo.put("reply",reply);
                            //回复者
                            replyVo.put("user",userService.findUserById(reply.getUserId()));
                            //添加target(如果有)
                                User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                                replyVo.put("target",target);
                            //获取评论的回复的点赞
                            long replyLikeCount = likeService.getLikeCount(ENTITY_TYPE_REPLY, reply.getId());
                            replyVo.put("replyLikeCount",replyLikeCount);
                            //获取评论的回复的点赞的状态
                            int replyLikeStatus = loginUser == null ? 0 : likeService.getLikeStatus(loginUser.getId(),ENTITY_TYPE_REPLY,reply.getId());
                            replyVo.put("replyLikeStatus",replyLikeStatus);
                            //添加到replyVoList中
                            replyVoList.add(replyVo);
                        }
                    }
                    commentVo.put("replys",replyVoList);
                //获取某条评论的回复数目
                    int commentNum = commentService.findCommentRow(ENTITY_TYPE_REPLY, comment.getId());
                    commentVo.put("replyNum",commentNum);
                //添加到commentVoList中
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);
        model.addAttribute("offset",page.getOffset());
        model.addAttribute("total",page.getTotal());
        return "site/discuss-detail";
    }
    //获取某一用户的帖子列表
    @RequestMapping(path = "/{userId}" , method = RequestMethod.GET)
    public String getUserDiscussPost(@PathVariable("userId") int userId, Model model, Page page){
        //设置分页
        page.setLimit(5);
        page.setPath("/discuss/" + userId);
        int postCount = discussPostMapperService.findDiscussPostRows(userId);
        page.setTotal(postCount);
        //某一用户的帖子总数
        model.addAttribute("postCount",postCount);
        //获取帖子的赞
        List<DiscussPost> discussPost = discussPostMapperService.findDiscussPost(userId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> list = new ArrayList<>();
        if(discussPost != null){
            for(DiscussPost post : discussPost){
                Map<String,Object> map = new HashMap<>();
                long likeCount = likeService.getLikeCount(ENTITY_TYPE_COMMENT, post.getId());
                map.put("likeCount",likeCount);
                map.put("post",post);
                list.add(map);
            }
        }
        User user = userService.findUserById(userId);
        if(user == null){
            return CommunityUtil.getJSONString(1,"该用户不存在！");
        }
        model.addAttribute("postList",list);
        model.addAttribute("user",user);
        return "/site/my-post";
    }

    //将帖子设置为置顶/取消置顶
    @RequestMapping(path = "/top" , method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int postId, int type){
        DiscussPost post = discussPostMapperService.findDiscussPostById(postId);
        type = type == 0 ? 1 : 0;
        discussPostMapperService.updateType(postId,type);
        //将帖子数据提交到es服务器
        Event event = new Event();
        event.setTopic(TOPIC_TYPE_TOP)
                .setUserId(post.getUserId())
                .setEntityType(ENTITY_TYPE_COMMENT)
                .setEntityId(post.getId());
        eventProducer.sendEvent(event);
        return CommunityUtil.getJSONString(0,"设置成功！");
    }

    //将帖子设置为加精/取消加精
    @RequestMapping(path = "/wonderful" , method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int postId, int status){
        DiscussPost post = discussPostMapperService.findDiscussPostById(postId);
        status = status == 0 ? 1 : 0;
        discussPostMapperService.updateStatus(postId,status);
        //将帖子数据提交到es服务器
        Event event = new Event();
        event.setTopic(TOPIC_TYPE_WONDERFUL)
                .setUserId(post.getUserId())
                .setEntityType(ENTITY_TYPE_COMMENT)
                .setEntityId(post.getId());
        eventProducer.sendEvent(event);
        return CommunityUtil.getJSONString(0,"设置成功！");
    }

    //将帖子删除
    @RequestMapping(path = "/delete" , method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int postId){
        DiscussPost post = discussPostMapperService.findDiscussPostById(postId);
        discussPostMapperService.updateStatus(postId,2);
        //将帖子数据提交到es服务器
        Event event = new Event();
        event.setTopic(TOPIC_TYPE_DELETE)
                .setUserId(post.getUserId())
                .setEntityType(ENTITY_TYPE_COMMENT)
                .setEntityId(post.getId());
        eventProducer.sendEvent(event);
        return CommunityUtil.getJSONString(0,"删除成功！");
    }

}
