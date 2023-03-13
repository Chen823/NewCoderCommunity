package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    MessageMapper messageMapper;

    public List<Message> findConversation(int userId, int offset, int limit){
        return messageMapper.selectConversationById(userId,offset,limit);
    }
    //查询当前用户的会话数量
    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }
    //查询当前用户某一会话的消息
    public List<Message> findMessage(String conversationId, int offset, int limit){
        return messageMapper.selectMessageById(conversationId,offset,limit);
    }
    //查询当前用户某一会话消息的数量
    public int findMessageCount(String conversationId){
        return messageMapper.selectMessageCount(conversationId);
    }
    //查询未读会话/消息的数量
    public int findUncheckedCount(int userId, String conversationId){
        return messageMapper.selectUncheckedCount(userId,conversationId);
    }

    //新增消息
    public int addMessage(Message message){
        //去html标签
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        //敏感词过滤
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    //更新消息状态
    public int updateStatus(List<Integer> ids, int status){
        if(ids != null)  return messageMapper.updateMessageStatusById(ids,status);
        return 0;
    }


}
