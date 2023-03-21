package com.nowcoder.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.CookieValue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTest {
    @Autowired
    private producer p;

    @Test
    public void testKafka(){
        p.send("test","测试！");
        p.send("test","我要吃筷知香！");
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

@Component
class producer{
        @Autowired
        private KafkaTemplate kafkaTemplate;
        public void send(String topic, String data){
            kafkaTemplate.send(topic,data);
        }
}

@Component
class consumer{
    @KafkaListener(topics = {"test"})
    public void listen(ConsumerRecord record){
        System.out.println(record.value());
    }
}
