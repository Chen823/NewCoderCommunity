package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/search" , method = RequestMethod.GET)
    public String getSearch(Model model, Page page, String keyword){
        page.setLimit(10);
        //查询结果
        org.springframework.data.domain.Page<DiscussPost> searchResult = elasticsearchService.searchPost(keyword, page.getCurPage() - 1, page.getLimit());
        //整合数据
        List<Map<String,Object>> list = new ArrayList<>();
        if(searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                long likeCount = likeService.getLikeCount(ENTITY_TYPE_COMMENT, post.getId());
                map.put("likeCount", likeCount);
                list.add(map);
            }
        }
        model.addAttribute("searchResult",list);
        //分页信息
        page.setPath("/search/?keyword=" + keyword);
        page.setTotal(searchResult == null ? 0 : (int) searchResult.getTotalElements());
        return "/site/search";
    }
}
