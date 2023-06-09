//package com.wangguo.controller;
//
//import com.wangguo.grace.result.GraceJSONResult;
//import com.wangguo.model.Stu;
//import com.wangguo.utils.SMSUtils;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@Slf4j
//@Api(tags = "Hello 测试的接口")
//@RestController
//public class HelloController {
//    @ApiOperation(value = "hello - 这是一个hello的测试路由")
//    @GetMapping("hello")
//    public Object hello() {
//        Stu stu = new Stu("imooc", 18);
//        log.debug(stu.toString());
//        log.info(stu.toString());
//        log.warn(stu.toString());
//        log.warn(stu.toString());
//
//        return GraceJSONResult.ok(stu);
//    }
//    @Autowired
//    private SMSUtils smsUtils;
//    @GetMapping("sms")
//    public Object sms() throws Exception {
//
//        String code = "123456";
//        smsUtils.sendSMS("18884103908", code);
//
//        return GraceJSONResult.ok();
//    }
//}
