package com.nowcoder.community.entity;

import java.util.Date;

public class Message {

//    Create Table
//
//    CREATE TABLE `message` (
//            `id` int(11) NOT NULL AUTO_INCREMENT,
//  `from_id` int(11) DEFAULT NULL,
//  `to_id` int(11) DEFAULT NULL,
//  `conversation_id` varchar(45) NOT NULL,
//  `content` text,
//            `status` int(11) DEFAULT NULL COMMENT '0-未读;1-已读;2-删除;',
//            `create_time` timestamp NULL DEFAULT NULL,
//    PRIMARY KEY (`id`),
//    KEY `index_from_id` (`from_id`),
//    KEY `index_to_id` (`to_id`),
//    KEY `index_conversation_id` (`conversation_id`)
//            ) ENGINE=InnoDB AUTO_INCREMENT=355 DEFAULT CHARSET=utf8
    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
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

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", fromId=" + fromId +
                ", toId=" + toId +
                ", conversationId='" + conversationId + '\'' +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
