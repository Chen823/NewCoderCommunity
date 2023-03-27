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

    /**
     * 用户 状态量
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题 关注
     */
    String TOPIC_TYPE_FOLLOW = "follow";

    /**
     * 主题 回复
     */
    String TOPIC_TYPE_COMMENT = "comment";

    /**
     * 主题 点赞
     */
    String TOPIC_TYPE_LIKE = "like";

    /**
     * 主题 帖子
     */
    String TOPIC_TYPE_POST = "post";

    /**
     * 主题 置顶
     */
    String TOPIC_TYPE_TOP = "top";
    /**
     * 主题 置顶
     */
    String TOPIC_TYPE_WONDERFUL = "wonderful";
    /**
     * 主题 删除帖子
     */
    String TOPIC_TYPE_DELETE = "delete";
    /**
     * 权限 普通用户
     */
    String AUTHORITY_USER = "user";
    /**
     * 权限 管理员
     */
    String AUTHORITY_ADMIN = "admin";
    /**
     * 权限 版主
     */
    String AUTHORITY_MODERATOR = "moderator";
}
