package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    //创建日志对象
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    //处理Exception的所有子类
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        //记录Error
        logger.error("服务器发生异常:"+e.getMessage());
        for(StackTraceElement element : e.getStackTrace()){
            //将所有error遍历记录
            logger.error(element.toString());
        }
        //获取服务器的请求格式(普通请求or异步请求)
        String xRequestedWith = response.getHeader("x-requested-with");
        //异步请求则会需要XML
        if("XMLHttpRequest".equals(xRequestedWith)){
            //返回格式为普通的字符串
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常！"));
        }else{
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
