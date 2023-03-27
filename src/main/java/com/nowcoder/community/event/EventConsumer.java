package com.nowcoder.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostMapperService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.MailClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.lang.ref.PhantomReference;
import java.lang.reflect.Member;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Component
public class EventConsumer implements CommunityConstant {
    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostMapperService discussPostMapperService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @KafkaListener(topics = {TOPIC_TYPE_FOLLOW,TOPIC_TYPE_COMMENT,TOPIC_TYPE_LIKE})
    public void sendEvent(ConsumerRecord record){
        //record判空
        if(record == null || record.value() == null){
            logger.error("内容不能为空!");
            return;
        }
        //event判空
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event == null){
            logger.error("事件不能为空");
            return;
        }

        //发送消息
        Message message = new Message();
        message.setToId(event.getEntityUserId());
        message.setStatus(0);
        message.setFromId(1);
        message.setCreateTime(new Date());
        message.setConversationId(event.getTopic());
        Map<String, Integer> content = new HashMap();
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());
        content.put("userId",event.getUserId());
        Map<String, Object> data = event.getData();
        if(!data.isEmpty()){
            for(Map.Entry<String,Object> entry : data.entrySet()){
                content.put(entry.getKey(),(Integer) entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    @KafkaListener(topics = {TOPIC_TYPE_POST,TOPIC_TYPE_TOP,TOPIC_TYPE_WONDERFUL})
    public void sendPostEvent(ConsumerRecord record){
        //record判空
        if(record == null || record.value() == null){
            logger.error("内容不能为空!");
            return;
        }
        //event判空
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event == null){
            logger.error("事件不能为空");
            return;
        }

        //保存到es服务器
        int postId = event.getEntityId();
        DiscussPost post = discussPostMapperService.findDiscussPostById(postId);
        elasticsearchService.savePost(post);
    }

    @KafkaListener(topics = {TOPIC_TYPE_DELETE})
    public void sendPostDeleteEvent(ConsumerRecord record){
        //record判空
        if(record == null || record.value() == null){
            logger.error("内容不能为空!");
            return;
        }
        //event判空
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event == null){
            logger.error("事件不能为空");
            return;
        }

        //保存到es服务器
        int postId = event.getEntityId();
        elasticsearchService.deletePost(postId);
    }

}
