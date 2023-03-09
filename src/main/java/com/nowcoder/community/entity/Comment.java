package com.nowcoder.community.entity;

import java.util.Date;

public class Comment {
    //CREATE TABLE `comment` (
    //  `id` int(11) NOT NULL AUTO_INCREMENT,
    //  `user_id` int(11) DEFAULT NULL,
    //  `entity_type` int(11) DEFAULT NULL,
    //  `entity_id` int(11) DEFAULT NULL,
    //  `target_id` int(11) DEFAULT NULL,
    //  `content` text,
    //  `status` int(11) DEFAULT NULL,
    //  `create_time` timestamp NULL DEFAULT NULL,
    //  PRIMARY KEY (`id`),
    //  KEY `index_user_id` (`user_id`),
    //  KEY `index_entity_id` (`entity_id`)
    //) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8

    private int id;
    private int userId;
    private int entityType;
    private int entityId;
    private int targetId;
    private String content;
    private int status;
    private Date createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
