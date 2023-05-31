package com.wangguo.intercepter;

import com.wangguo.base.BaseInfoProperties;
import com.wangguo.exceptions.GraceException;
import com.wangguo.grace.result.ResponseStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserTokenInterceptor extends BaseInfoProperties implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("headerUserId"); // 从请求头中获取用户id
        String userToken = request.getHeader("headerUserToken");

        // 判断header中用户id和token不能为空
        if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)) {
            // redis服务器中的token信息已经过期
            String redisToken = redis.get(REDIS_USER_TOKEN + ":" + userId);
            if (StringUtils.isBlank(redisToken)) {
                GraceException.display(ResponseStatusEnum.UN_LOGIN);
                return false;
            } else {
                // 如果token和redis数据库中的token不一致，表示该用户已经在别的设备登录了。要限制登录
                if (!redisToken.equalsIgnoreCase(userToken)) {
                    GraceException.display(ResponseStatusEnum.TICKET_INVALID);
                    return false;
                }
            }
        } else {
            GraceException.display(ResponseStatusEnum.UN_LOGIN);
        }
        /**
         * 成功通过，放行
         */
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
