package com.nowcoder.community;

import com.nowcoder.community.util.SensetiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensetiveTest {
    @Autowired
    SensetiveFilter sensetiveFilter1;

    @Test
    public void setSensetiveFilterTest(){
        String s = "这里可以赌博,吸毒,开票,太爽了！";
        String filter = sensetiveFilter1.filter(s);
        System.out.println(filter );
        s = "这里可以赌→博→,可以→嫖→娼→,可以吸→毒, 可以→***→...fabc";
        filter = sensetiveFilter1.filter(s);
        System.out.println(filter );
    }

}
