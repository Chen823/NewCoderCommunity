package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {
    public static String getCookie(HttpServletRequest request,String CookieName){
        if(request == null || CookieName == null){
            throw new IllegalArgumentException("输入参数为空");
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals(CookieName)) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
