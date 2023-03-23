package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void testInsert(){
        //添加数据
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void deleteTest(){
        discussPostRepository.deleteAll();
    }

    @Test
    public void testInsertMuti(){
        //添加多条数据
        discussPostRepository.saveAll(discussPostMapper.selectPostMapper(101,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectPostMapper(102,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectPostMapper(103,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectPostMapper(111,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectPostMapper(112,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectPostMapper(131,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectPostMapper(132,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectPostMapper(133,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectPostMapper(134,0,100));
    }

    @Test
    public void testSearchByRepository(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                //根据关键词多条件检索(从title和content中检索互联网寒冬)
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title", "content"))
                //按是否置顶_分数从高到低_创建时间从最近到最远排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //分页查询 当前第0条开始 查询10条
                .withPageable(PageRequest.of(0, 10))
                //对检索结果中的关键词高亮显示
                .withHighlightFields(
                    new HighlightBuilder.Field("title").preTags("<em>").postTags("<em>"),
                    new HighlightBuilder.Field("content").preTags("<em>").postTags("<em>")
                ).build();
        //调用查询 返回一个Page集合
        //底层调用:elasticsearchTemplate.queryForPage(query, class, mapper)
        Page<DiscussPost> page = discussPostRepository.search(searchQuery);
        //查询总条数
        System.out.println(page.getTotalElements());
        //查询总页数
        System.out.println(page.getTotalPages());
        //查询当前页数
        System.out.println(page.getNumber());
        //查询每页显示多少条数据
        System.out.println(page.getSize());
        //遍历查询结果
        for(DiscussPost post : page){
            System.out.println(post);
        }
    }

    @Test
    public void testSearchByTemplate(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                //根据关键词多条件检索(从title和content中检索互联网寒冬)
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title", "content"))
                //按是否置顶_分数从高到低_创建时间从最近到最远排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //分页查询 当前第0条开始 查询10条
                .withPageable(PageRequest.of(0, 10))
                //对检索结果中的关键词高亮显示
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("<em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("<em>")
                ).build();
        Page<DiscussPost> page = elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                //获取检索到的结果
                SearchHits hits = response.getHits();
                //如果没有检索到则返回空
                if(hits.getTotalHits() <= 0){
                    return null;
                }
                //遍历检索结果
                List<DiscussPost> list = new ArrayList<>();
                for(SearchHit hit : hits){
                    DiscussPost post = new DiscussPost();

                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));

                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.valueOf(createTime)));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setStatus(Integer.valueOf(commentCount));
                    //处理高亮显示
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if(titleField != null){
                        post.setTitle(titleField.fragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if(titleField != null){
                        post.setContent(contentField.fragments()[0].toString());
                    }

                    list.add(post);
                }
                return new AggregatedPageImpl(list, pageable,
                        hits.getTotalHits(), response.getAggregations(), response.getScrollId(), hits.getMaxScore());
            }
        });

        System.out.println(page.getTotalElements());
        //查询总页数
        System.out.println(page.getTotalPages());
        //查询当前页数
        System.out.println(page.getNumber());
        //查询每页显示多少条数据
        System.out.println(page.getSize());
        //遍历查询结果
        for(DiscussPost post : page){
            System.out.println(post);
        }
    }
}
