package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostMapperService;
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
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //帖子
        DiscussPost discussPost = discussPostMapperService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);
        //发帖用户
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);
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
    

}
