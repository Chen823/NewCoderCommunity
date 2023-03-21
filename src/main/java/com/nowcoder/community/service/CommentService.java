package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensetiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Autowired
    CommentMapper commentMapper;

    @Autowired
    DiscussPostMapperService discussPostMapperService;

    @Autowired
    SensetiveFilter sensetiveFilter;

    public List<Comment> findComment(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectComment(entityType,entityId,offset,limit);
    }

    public List<Comment> findCommentById(int entityType, int userId, int offset, int limit){
        return commentMapper.selectCommentById(entityType,userId,offset,limit);
    }

    public int findCommentRow(int entityType, int entityId){
        return commentMapper.selectCommentRow(entityType,entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //敏感词过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensetiveFilter.filter(comment.getContent()));
        //新增评论
        int rows = commentMapper.insertComment(comment);
        //增加评论数
        if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
            int count = commentMapper.selectCommentRow(ENTITY_TYPE_COMMENT,comment.getEntityId());
            discussPostMapperService.updateCommentCount(count, comment.getEntityId());
        }
        return rows;
    }

    public int findCommentRowById(int entityType, int userId){
        return commentMapper.selectCommentRowById(entityType,userId);
    }

    public Comment findSingleCommentById(int id){
        return commentMapper.selectSingleCommentById(id);
    }

}
