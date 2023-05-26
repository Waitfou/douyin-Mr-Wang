package com.wangguo.intercepter;

import com.wangguo.base.BaseInfoProperties;
import com.wangguo.exceptions.GraceException;
import com.wangguo.grace.result.ResponseStatusEnum;
import com.wangguo.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class PassportInterceptor extends BaseInfoProperties implements HandlerInterceptor {
    /**
     * 对来自前端的请求进行处理
     * @param request 前端的请求
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取用户的ip
        String userIp = IPUtil.getRequestIp(request);

        // 判断在redis中是否存在
        boolean keyIsExist = redis.keyIsExist(MOBILE_SMSCODE + ":" + userIp);
        System.out.println("test");
        if(keyIsExist) {
            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            log.info("短信发送频率太大！请稍后重试");
            return false;
        }
        /**
         * true 请求放行
         * false 请求拦截
         */
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
