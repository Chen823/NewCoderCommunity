package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    //查询当前用户的会话列表(只显示每个会话的最新一条消息)
    List<Message> selectConversationById(int userId, int offset, int limit);
    //查询当前用户的会话数量
    int selectConversationCount(int userId);
    //查询当前用户某一会话的消息
    List<Message> selectMessageById(String conversationId, int offset, int limit);
    //查询当前用户某一会话消息的数量
    int selectMessageCount(String conversationId);
    //查询未读会话/消息的数量
    int selectUncheckedCount(int userId, String conversationId);
    //插入信息
    int insertMessage(Message message);
    //更新Message状态
    int updateMessageStatusById(List<Integer> ids,int status);
    //删除某一条信息
    int deleteMessageById(int id);
    //查询某一主题的最新消息
    Message selectNewMessageById(int userId, String topic);
    //查询某一主题的消息数量
    int selectMessageCountById(int userId, String topic);
    //查询某一主题的未读数量
    int selectUnreadMessageCountById(int userId, String topic);
    //查询某一主题下的系统通知消息列表
    List<Message> selectNoticeById(int userId,String topic, int offset, int limit);

    int selectUnreadNoticeCount(int userId);


}
