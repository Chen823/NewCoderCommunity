package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensetiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Date;
import java.util.List;

@Service
public class DiscussPostMapperService {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensetiveFilter filter;

     public List<DiscussPost> findDiscussPost(int userId, int offset, int limit){
        return discussPostMapper.selectPostMapper(userId,offset,limit);
    }

    public int findDiscussPostRows (int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost discussPost){
         //空值处理
         if(discussPost == null){
             throw  new IllegalArgumentException("帖子对象为空");
         }
        //html标签内容转义(HtmlUtils.htmlEscape)
         discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
         discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        //过滤敏感词
        discussPost.setTitle(filter.filter(discussPost.getTitle()));
        discussPost.setContent(filter.filter(discussPost.getContent()));
        //插入数据
        return discussPostMapper.insertDiscussPost(discussPost);
    }
}
