package com.nowcoder.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
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

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    MessageService messageService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @LoginRequired
    @RequestMapping(path = "/letter/list" , method = RequestMethod.GET)
    public String getConversationList(Model model, Page page) {
        User user = hostHolder.getUser();
        //设置分页
        page.setPath("/letter/list");
        page.setLimit(10);
        page.setTotal(messageService.findConversationCount(user.getId()));
        //获取对话列表
        List<Message> conversations = messageService.findConversation(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> list = new ArrayList<>();
        if(conversations != null){
            for(Message m : conversations){
                Map<String,Object> map = new HashMap<>();
                int messageCount = messageService.findMessageCount(m.getConversationId());
                int unCheckedMessage = messageService.findUncheckedCount(user.getId(),m.getConversationId());
                User fromUser = userService.findUserById(m.getFromId() == user.getId() ? m.getToId() : m.getFromId());
                map.put("conversation",m);
                map.put("fromUser",fromUser);
                map.put("messageCount",messageCount);
                map.put("unCheckedMessage",unCheckedMessage);
                list.add(map);
            }
        }
        model.addAttribute("conversations",list);
        int uncheckedConversation = messageService.findUncheckedCount(user.getId(), null);
        model.addAttribute("uncheckedConversation",uncheckedConversation);
        //查询所有未读的系统消息数量
        int unreadNoticeCount = messageService.findUnreadMessageCount(user.getId(), TOPIC_TYPE_COMMENT) +
                                messageService.findUnreadMessageCount(user.getId(), TOPIC_TYPE_FOLLOW) +
                                messageService.findUnreadMessageCount(user.getId(), TOPIC_TYPE_LIKE);
        model.addAttribute("unreadNoticeCount",unreadNoticeCount);
        return "/site/letter";
    }

    public List<Integer> getUncheckedMessageId(List<Message> messages){
        List<Integer> ids = new ArrayList<>();
        if(messages != null){
            for(Message m : messages){
                if(m.getToId() == hostHolder.getUser().getId() && m.getStatus() == 0){
                    ids.add(m.getId());
                }
            }
        }
        return ids;
    }
    @LoginRequired
    @RequestMapping(path = "/letter/detail/{conversationId}" , method = RequestMethod.GET)
    public String getMessageList(@PathVariable("conversationId") String conversationId, Model model, Page page) {

        //设置分页
        page.setTotal(messageService.findMessageCount(conversationId));
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        //获取私信
        List<Message> message = messageService.findMessage(conversationId, page.getOffset(), page.getLimit());
        Message m0 = message.get(0);
        User user = hostHolder.getUser();
        User fromUser = userService.findUserById(m0.getFromId() == user.getId() ? m0.getToId() : m0.getFromId());

        List<Map<String,Object>> list = new ArrayList<>();
        for(Message m : message){
            Map<String,Object> map = new HashMap<>();
            User from = userService.findUserById(m.getFromId());
            map.put("message",m);
            map.put("user",from);
            list.add(map);
        }

        model.addAttribute("fromUser",fromUser);
        model.addAttribute("messages",list);
        //已读标记
        List<Integer> ids = getUncheckedMessageId(message);
        if(ids.size() != 0){
            messageService.updateStatus(ids,1);
        }
        return "/site/letter-detail";
    }

    @RequestMapping(path = "/letter/send" , method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(String toName, String content) {

        if(content == null){
            return CommunityUtil.getJSONString(1,"发送内容不能为空！");
        }
        if(toName == null){
            return CommunityUtil.getJSONString(1,"发送对象不能为空！");
        }
        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);
        int fromId = hostHolder.getUser().getId();
        message.setFromId(fromId);
        if(userService.findByName(toName) == null){
            return CommunityUtil.getJSONString(1,"发送用户不存在！");
        }
        int toId = userService.findByName(toName).getId();
        message.setToId(toId);
        String conversationId = fromId > toId ? toId + "_" + fromId : fromId + "_" + toId;
        message.setConversationId(conversationId);
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0,"发送成功！");
    }

    @RequestMapping(path = "/letter/delete" , method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(int deleteMessageId) {
       messageService.deleteMessage(deleteMessageId);
        return CommunityUtil.getJSONString(0,"删除成功！");
    }

    @LoginRequired
    @RequestMapping(path = "/letter/notice" , method = RequestMethod.GET)
    public String getNotice(Model model) {
        //获取当前登录用户
        User user = hostHolder.getUser();
        int userId = user.getId();
        //系统未读通知总数
        int unreadNoticeCount = 0;
        //评论系统通知
        Message newComment = messageService.findNewMessage(userId, TOPIC_TYPE_COMMENT);
        Map<String,Object> commentMap = new HashMap();
        boolean commentIsEmpty = true;
        if(newComment != null) {
            Map<String, Integer> commentContentMap = JSONObject.parseObject(newComment.getContent(), HashMap.class);
            int commentId = commentContentMap.get("userId");
            int entityType = commentContentMap.get("entityType");
            int commentCount = messageService.findMessageCount(userId, TOPIC_TYPE_COMMENT);
            int unreadCommentCount = messageService.findUnreadMessageCount(userId, TOPIC_TYPE_COMMENT);
            unreadNoticeCount += unreadCommentCount;
            Date commentCreateTime = newComment.getCreateTime();
            commentIsEmpty = false;
            commentMap.put("name", userService.findUserById(commentId).getUsername());
            commentMap.put("Count", commentCount);
            commentMap.put("unreadCount", unreadCommentCount);
            commentMap.put("createTime", commentCreateTime);
            commentMap.put("entityType", entityType);
        }
        commentMap.put("isEmpty",commentIsEmpty);
        model.addAttribute("commentMap",commentMap);
        //点赞系统通知
        Message newLike = messageService.findNewMessage(userId, TOPIC_TYPE_LIKE);
        Map<String,Object> likeMap = new HashMap();
        boolean likeIsEmpty = true;
        if(newLike != null) {
        Map<String,Integer> likeContentMap = JSONObject.parseObject(newLike.getContent(), HashMap.class);
        int likeId = likeContentMap.get("userId");
        int entityType = likeContentMap.get("entityType");
        int likeCount = messageService.findMessageCount(userId, TOPIC_TYPE_LIKE);
        int unreadLikeCount = messageService.findUnreadMessageCount(userId, TOPIC_TYPE_LIKE);
        unreadNoticeCount += unreadLikeCount;
        Date likeCreateTime = newLike.getCreateTime();
        likeIsEmpty = false;
            likeMap.put("name",userService.findUserById(likeId).getUsername());
            likeMap.put("Count",likeCount);
            likeMap.put("unreadCount",unreadLikeCount);
            likeMap.put("createTime",likeCreateTime);
            likeMap.put("entityType", entityType);
        }
        likeMap.put("isEmpty",likeIsEmpty);
        model.addAttribute("likeMap",likeMap);


        //关注系统通知
        Message newFollow = messageService.findNewMessage(userId, TOPIC_TYPE_FOLLOW);
        Map<String, Object> followMap = new HashMap();
        boolean followIsEmpty = true;
        if(newFollow != null) {
            Map<String, Integer> followContentMap = JSONObject.parseObject(newFollow.getContent(), HashMap.class);
            int followId = followContentMap.get("userId");
            int followCount = messageService.findMessageCount(userId, TOPIC_TYPE_FOLLOW);
            int unreadFollowCount = messageService.findUnreadMessageCount(userId, TOPIC_TYPE_FOLLOW);
            unreadNoticeCount += unreadFollowCount;
            Date followCreateTime = newFollow.getCreateTime();
            followIsEmpty = false;
            followMap.put("name", userService.findUserById(followId).getUsername());
            followMap.put("Count", followCount);
            followMap.put("unreadCount", unreadFollowCount);
            followMap.put("createTime", followCreateTime);
        }
        followMap.put("isEmpty",followIsEmpty);
        model.addAttribute("followMap",followMap);
        model.addAttribute("unreadNoticeCount",unreadNoticeCount);
        int uncheckedConversation = messageService.findUncheckedCount(userId, null);
        model.addAttribute("uncheckedConversation",uncheckedConversation);
        return "/site/notice";
    }

    @LoginRequired
    @RequestMapping(path = "/letter/notice/{topic}" , method = RequestMethod.GET)
    public String getNoticeList(@PathVariable("topic") String topic, Model model, Page page) {
        //获取当前登录用户
        User user = hostHolder.getUser();
        int userId = user.getId();
        //分页设置
        page.setPath("/letter/notice/" + topic);
        page.setLimit(5);
        page.setTotal(messageService.findMessageCount(userId,topic));
        List<Map<String,Object>> list = new ArrayList<>();
        List<Message> messageList = messageService.findNoticeListById(userId, topic, page.getOffset(), page.getLimit());
        if(messageList != null){
            for(Message m : messageList){
                Map map = new HashMap<>();
                String content = m.getContent();
                HashMap<String,Object> hashMap = JSONObject.parseObject(content, HashMap.class);
                int fromId = (int) hashMap.get("userId");
                int entityType = (int) hashMap.get("entityType");
                if(!topic.equals(TOPIC_TYPE_FOLLOW)) {
                    int postId = (int) hashMap.get("postId");
                    map.put("postId", postId);
                }
                map.put("fromUser",userService.findUserById(fromId));
                map.put("entityType",entityType);
                map.put("message",m);
                list.add(map);
            }
        }
        model.addAttribute("topic",topic);
        model.addAttribute("list",list);
        //已读设置
        List<Integer> ids = getUncheckedMessageId(messageList);
        if(ids.size() != 0){
            messageService.updateStatus(ids,1);
        }
        return "/site/notice-detail";
    }



}

