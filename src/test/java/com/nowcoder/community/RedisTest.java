package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void RedisTest(){
        String key = "test:count";
        redisTemplate.opsForValue().set(key,1);
        Object o = redisTemplate.opsForValue().get(key);
        System.out.println(o);
        redisTemplate.opsForValue().increment(key);

    }

    @Test
    public void testTransactional(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String key = "test:tx";
                //开启事务
                operations.multi();
                //数据操作
                operations.opsForValue().set(key,1);
                //提交
                return operations.exec();
            }
        });
        System.out.println(obj);
    }
}
