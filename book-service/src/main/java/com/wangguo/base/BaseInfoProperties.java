package com.wangguo.base;

import com.wangguo.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseInfoProperties {
    @Autowired
    public RedisOperator redis;

    // 我的关注总数
    public static final String REDIS_MY_FOLLOWS_COUNTS = "redis_my_follows_counts";
    // 我的粉丝总数
    public static final String REDIS_MY_FANS_COUNTS = "redis_my_fans_counts";
    // 视频和发布者获赞数
    public static final String REDIS_VLOG_BE_LIKED_COUNTS = "redis_vlog_be_liked_counts";
    public static final String REDIS_VLOGER_BE_LIKED_COUNTS = "redis_vloger_be_liked_counts";
    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";

    public Map<String, String> getErrors(BindingResult result) {
        Map<String, String> map = new HashMap<>();
        List<FieldError> errorList = result.getFieldErrors();
        for (FieldError ff : errorList) {
            String field = ff.getField();
            String msg = ff.getDefaultMessage();
            map.put(field, msg);
        }
        return map;
    }
}
