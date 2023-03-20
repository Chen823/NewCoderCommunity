package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    //关注
    public void follow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeRedisKey = RedisKeyUtil.getFolloweeRedisKey(userId, entityType);
                String followerRedisKey = RedisKeyUtil.getFollowerRedisKey(entityType, entityId);
                //开启事务
                redisTemplate.multi();
                redisTemplate.opsForZSet().add(followeeRedisKey,entityId,System.currentTimeMillis());
                redisTemplate.opsForZSet().add(followerRedisKey,userId,System.currentTimeMillis());
                return redisTemplate.exec();
            }
        });
    }

    //取消关注
    public void unfollow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeRedisKey = RedisKeyUtil.getFolloweeRedisKey(userId, entityType);
                String followerRedisKey = RedisKeyUtil.getFollowerRedisKey(entityType, entityId);
                //开启事务
                redisTemplate.multi();
                redisTemplate.opsForZSet().remove(followeeRedisKey,entityId);
                redisTemplate.opsForZSet().remove(followerRedisKey,userId);
                return redisTemplate.exec();
            }
        });
    }

    //查询某一实体的关注数量
    public long getFolloweeCount(int entityType, int entityId){
        String followeeRedisKey = RedisKeyUtil.getFolloweeRedisKey(entityId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeRedisKey);
    }
    //查询某一实体的粉丝数量
    public long getFollowerCount(int entityType, int userId){
        String followerRedisKey = RedisKeyUtil.getFollowerRedisKey(entityType,userId) ;
        return redisTemplate.opsForZSet().zCard(followerRedisKey);

    }

    //查询当前实体是否已关注
    public boolean isFollow(int userId, int entityType, int entityId){
        String followeeRedisKey = RedisKeyUtil.getFolloweeRedisKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeRedisKey,entityId) != null;
    }

    //查询某一用户已经关注的人
    public List<Map<String,Object>> getFolloweeList(int userId, int offset, int limit){
        String followeeRedisKey = RedisKeyUtil.getFolloweeRedisKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeRedisKey, offset, limit + offset - 1);
        if(targetIds == null){
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for(Integer targetId : targetIds){
            Map<String,Object> map = new HashMap<>();
            User u = userService.findUserById(targetId);
            map.put("user",u);
            Double score = redisTemplate.opsForZSet().score(followeeRedisKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    //查询某一用户的粉丝
    public List<Map<String,Object>> getFollowerList(int userId, int offset, int limit){
        String followerRedisKey = RedisKeyUtil.getFollowerRedisKey(ENTITY_TYPE_USER,userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerRedisKey, offset, limit + offset - 1);
        if(targetIds == null){
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for(Integer targetId : targetIds){
            Map<String,Object> map = new HashMap<>();
            User u = userService.findUserById(targetId);
            map.put("user",u);
            Double score = redisTemplate.opsForZSet().score(followerRedisKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }


}
