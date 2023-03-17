package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class LikeService {

    @Autowired
    private RedisTemplate<String,Object> template;

    //实现点赞功能
    public void setLike(int userId,int entityType,int entityId){
        String redisKey = RedisKeyUtil.getRedisKey(entityType, entityId);
        if(template.opsForSet().isMember(redisKey,userId)){
            template.opsForSet().remove(redisKey,userId);
        }else{
            template.opsForSet().add(redisKey,userId);
        }
    }

    //根据实体查询点赞总数

    public long getLikeCount(int entityType,int entityId){
        String redisKey = RedisKeyUtil.getRedisKey(entityType, entityId);
        return template.opsForSet().size(redisKey);
    }

    //查询某一用户对某实体的点赞状态
    public int getLikeStatus(int userId,int entityType,int entityId){
        String redisKey = RedisKeyUtil.getRedisKey(entityType, entityId);
        return template.opsForSet().isMember(redisKey,userId) ? 1 : 0;
    }
}
