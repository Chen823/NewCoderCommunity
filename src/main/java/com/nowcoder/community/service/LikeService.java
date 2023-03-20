package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Controller;

@Controller
public class LikeService {

    @Autowired
    private RedisTemplate<String,Object> template;

    //实现点赞功能
    public void setLike(int userId,int entityType,int entityId, int entityUserId){
        template.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeRedisKey = RedisKeyUtil.getEntityLikeRedisKey(entityType, entityId);
                String userLikeRedisKey = RedisKeyUtil.getUserLikeRedisKey(entityUserId);
                Boolean isMember = template.opsForSet().isMember(entityLikeRedisKey, userId);
                //开启事务
                template.multi();
                if(isMember){
                    template.opsForSet().remove(entityLikeRedisKey,userId);
                    template.opsForValue().decrement(userLikeRedisKey);
                }else{
                    template.opsForSet().add(entityLikeRedisKey,userId);
                    template.opsForValue().increment(userLikeRedisKey);
                }
                return redisOperations.exec();
            }
        });
//        String redisKey = RedisKeyUtil.getRedisKey(entityType, entityId);
//        if(template.opsForSet().isMember(redisKey,userId)){
//            template.opsForSet().remove(redisKey,userId);
//        }else{
//            template.opsForSet().add(redisKey,userId);
//        }
    }

    //根据实体查询点赞总数

    public long getLikeCount(int entityType,int entityId){
        String redisKey = RedisKeyUtil.getEntityLikeRedisKey(entityType, entityId);
        return template.opsForSet().size(redisKey);
    }

    //查询某一用户对某实体的点赞状态
    public int getLikeStatus(int userId,int entityType,int entityId){
        String redisKey = RedisKeyUtil.getEntityLikeRedisKey(entityType, entityId);
        return template.opsForSet().isMember(redisKey,userId) ? 1 : 0;
    }

    //查询某一用户获取赞的数量
    public int getUserLikeCount(int userId){
        String userLikeRedisKey = RedisKeyUtil.getUserLikeRedisKey(userId);
        Integer userLikeCount = (Integer) template.opsForValue().get(userLikeRedisKey);
        return userLikeCount == null ? 0 : userLikeCount.intValue();
    }
}
