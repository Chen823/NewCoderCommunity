package com.nowcoder.community.util;

public class RedisKeyUtil {
    private static final String connector = ":";
    private static final String PREFIX_LIKE = "like:entity";

    public static String getRedisKey(int entityType,int entityId){
        return PREFIX_LIKE + connector + entityType + connector + entityId;
    }
}
