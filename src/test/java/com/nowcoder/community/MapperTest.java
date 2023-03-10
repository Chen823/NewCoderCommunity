package com.nowcoder.community;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private MessageMapper messageMapper;
    @Test
    public void selectTest() {
        User user = userMapper.selectById(101);
        System.out.println(user);
    }

    @Test
    public void insertTest(){
        User user = new User();
        user.setEmail("test@qq.com");
        user.setActivationCode("test");
        user.setPassword("test");
        user.setSalt("test");
        user.setCreateTime(new Date());
        user.setStatus(1);
        user.setType(1);
        user.setHeaderUrl("test.png");
        user.setUsername("testUser");
        userMapper.insertUser(user);
    }

    @Test
    public void updateTest(){
        userMapper.updateStatus(151,2);
    }


    @Test
    public void selectDPTest(){
        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
        List<DiscussPost> list = discussPostMapper.selectPostMapper(0, 0, 5);
        for(DiscussPost i : list){
            System.out.println(i);
        }
    }

    @Test
    public void MessageTest(){
        List<Message> messages = messageMapper.selectConversationById(111, 0, 20);
        for(Message message : messages){
            System.out.println(message);
        }

        System.out.println(messageMapper.selectConversationCount(111));

        List<Message> messages1 = messageMapper.selectMessageById("111_112", 0, 20);
        for(Message message : messages1){
            System.out.println(message);
        }
        System.out.println(messageMapper.selectMessageCount("111_112"));
        System.out.println(messageMapper.selectUncheckedCount(131, "111_131"));


    }


}
