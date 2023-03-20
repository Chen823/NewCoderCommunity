package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.service.CommentService;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectComment(int entityType, int entityId, int offset, int limit);
    int selectCommentRow(int entityType, int entityId);

    int insertComment(Comment comment);

    int selectCommentRowById(int entityType, int userId);

    List<Comment> selectCommentById(int entityType, int userId, int offset, int limit);


}
