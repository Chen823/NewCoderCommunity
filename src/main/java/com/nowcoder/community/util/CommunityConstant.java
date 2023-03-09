package com.nowcoder.community.util;

public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;
    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;
    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;
    /**
     * 默认生存时间
     */
    int DEFAULT_EXPIRED_TIME = 12 * 3600;
    /**
     * 勾选记住后的生存时间
     */
    int REMEMBERME_EXPIRED_TIME =  24 * 3600;

    /**
     * 帖子评论 状态量
     */
    int ENTITY_TYPE_COMMENT = 1;

    /**
     * 评论回复 状态量
     */
    int ENTITY_TYPE_REPLY = 2;
}
