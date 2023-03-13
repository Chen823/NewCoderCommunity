package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
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

import java.util.*;

@Controller
public class MessageController {
    @Autowired
    MessageService messageService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;


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
        return CommunityUtil.getJSONString(0,"发送成功");
    }


}

