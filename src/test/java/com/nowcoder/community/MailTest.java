package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void MailTest(){
        mailClient.sendMail("346675888@qq.com","Test","welcome!");
    }

    @Test
    public void MailHtmlTest(){
        Context context = new Context();
        context.setVariable("username","毕可松");
        String content = templateEngine.process("mail/demo", context);
        mailClient.sendMail("346675888@qq.com","TestHtml",content);
    }
}
