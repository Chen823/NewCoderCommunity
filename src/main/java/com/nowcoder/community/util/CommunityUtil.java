package com.nowcoder.community.util;


import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

public  class CommunityUtil {
    public static String generateUUID(){
        //生成一个字符串类型的随机UUID,将其中的“-”替换为空就生成了一个随机的字符串
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    public static String md5(String key){
        //在校验一个String类型的变量是否为空时，可以使用StringUtils.isBlank方法，它可以校验三种情况：是否为null、是否为""、是否为空字符串(引号中间有空格)" "、制表符、换行符、换页符和回车。
        if(StringUtils.isBlank(key)){
            return null;
        }
        //将字符串转换为一个16进制的序列
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String, Object> map){
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if(map != null){
            for(String key : map.keySet()){
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg){
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code){
        return getJSONString(code, null, null);
    }
}
