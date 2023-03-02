package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

@Component
public class HostHolder {
    private static ThreadLocal<User> users = new ThreadLocal<>();

    public static void setUser(User user){
        users.set(user);
    }

    public static User getUser(){
        return users.get();
    }

    public static void clear(){
        users.remove();
    }

}