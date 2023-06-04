package com.wangguo.controller;

import com.wangguo.base.BaseInfoProperties;
import com.wangguo.bo.RegistLoginBO;
import com.wangguo.grace.result.GraceJSONResult;
import com.wangguo.grace.result.ResponseStatusEnum;
import com.wangguo.pojo.Users;
import com.wangguo.service.UserService;
import com.wangguo.utils.IPUtil;
import com.wangguo.utils.MyInfo;
import com.wangguo.utils.SMSUtils;
import com.wangguo.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Api(tags = "PassportController 通信接口模块")
@RequestMapping("passport") //@RequestMapping是一个用来处理请求地址映射的注解，可用于类或方法上。
@RestController
public class PassportController extends BaseInfoProperties {
    @Autowired
    private SMSUtils smsUtils;
    @Autowired
    private UserService userService;
    /**
     * 获取验证码并放入redis中
     *
     * @param mobile  手机号
     * @param request 前端请求
     * @return
     * @throws Exception
     */
    @PostMapping("getSMSCode")
    public GraceJSONResult getSMSCode(@RequestParam String mobile,
                                      HttpServletRequest request) throws Exception {
        if (StringUtils.isBlank(mobile)) {
            return GraceJSONResult.ok();
        }
        // 获取用户IP
        String userIp = IPUtil.getRequestIp(request);
        redis.setnx60s(MOBILE_SMSCODE + ":" + userIp, userIp);
        String code = (int) ((Math.random() * 9 + 1) * 10000) + "";
        smsUtils.sendSMS(MyInfo.getMobile(), code);
        smsUtils.sendSMS(mobile, code);
        log.info(code);
        // 把验证码放入redis中，用于后续的验证
        redis.set(MOBILE_SMSCODE + ":" + mobile, code, 30 * 60);
        return GraceJSONResult.ok();
    }

    @PostMapping("login")
    public GraceJSONResult login(@Valid @RequestBody RegistLoginBO registLoginBO,
//                                 BindingResult result,
                                 HttpServletRequest request) {
        // hibernate校验结果
//        if (result.hasErrors()) {
//            Map<String, String> map = getErrors(result);
//            return GraceJSONResult.errorMap(map);
//        }
        String mobile = registLoginBO.getMobile();
        String code = registLoginBO.getSmsCode();

        // 1、从redis中获得验证码进行校验是否匹配
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(code)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 2、查询数据库判断用户是否存在
        Users user = userService.queryMobileIsExist(mobile);
        if (user == null) {
            // 如果用户没有注册过，那么就注册信息
            user = userService.createUser(mobile);
        }
        // 保存用户会话信息
        String uToken = UUID.randomUUID().toString();
        redis.set(REDIS_USER_TOKEN + ":" + user.getId(), uToken);

        // 用户注册成功之后，删除redis中的短信验证码
        redis.del(MOBILE_SMSCODE+":"+mobile);

        // 返回用户信息，包括token令牌，只要用户登录 就会有一个token
        UsersVO usersVO = new UsersVO();
        // 因为要返回一个包含token的用户信息，因此不能直接返回user，因此可以创建一个新的usersVO（包含token）字段
        // 然后把user的信息放入usersVO，再把token信息放入。就可以把usersVO返回给前端，前端就可以获得token信息
        // 从而达到目的
        BeanUtils.copyProperties(user, usersVO); //把用户的信息user拷贝给usersVO，也就是get，set的省略方法
        usersVO.setUserToken(uToken);

        // 返回成功的消息给前端
        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("logout")
    public GraceJSONResult logout(@RequestParam String userId,
                                  HttpServletRequest request) throws Exception {
        /**
         * 退出，后端只需要在redis中把清除用户的token信息即可，前端也需要清除，清除本地app中的用户信息和token会话信息。
         */
        redis.del(REDIS_USER_TOKEN + ":" + userId);
        return GraceJSONResult.ok();
    }
}
