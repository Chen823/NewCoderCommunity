package com.nowcoder.community.util;

public class RedisKeyUtil {
    private static final String connector = ":";//连接符
    private static final String PREFIX_LIKE = "like:entity";//点赞

    private static final String PREFIX_FOLLOWEE = "followee";//关注
    private static final String PREFIX_FOLLOWER = "follower";//被关注
    private static final String PREFIX_VERIFYCODE = "verifyCode";//验证码

    private static final String PREFIX_TICKET = "ticket";

    private static final String PREFIX_USER = "user";

    public static String getEntityLikeRedisKey(int entityType,int entityId){
        return PREFIX_LIKE + connector + entityType + connector + entityId;
    }
    public static String getUserLikeRedisKey(int userId){
        return PREFIX_LIKE + connector + userId;
    }
    //某个用户关注的实体
    //followee:userId:entityType -> zset(entityId,time)
    public static String getFolloweeRedisKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + connector + userId + connector + entityType;
    }

    //某个实体拥有的粉丝
    //follower:entityType:entityId -> zset(userId,time)
    public static String getFollowerRedisKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + connector + entityType + connector + entityId;
    }

    //验证码的key
    public static String getVerifyCodeRedisKey(String ownerKey){
        return PREFIX_VERIFYCODE + connector + ownerKey;
    }

    public static String getTicketRedisKey(String ticket){
        return PREFIX_TICKET + connector + ticket;
    }
    public static String getUserRedisKey(int userId){
        return PREFIX_TICKET + connector + userId;
    }

}
