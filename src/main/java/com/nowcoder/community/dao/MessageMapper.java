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
}
