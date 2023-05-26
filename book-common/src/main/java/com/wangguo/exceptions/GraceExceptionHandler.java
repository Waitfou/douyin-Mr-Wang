package com.wangguo.exceptions;

import com.wangguo.grace.result.GraceJSONResult;
import com.wangguo.grace.result.ResponseStatusEnum;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一异常拦截处理
 * 可以针对异常的类型进行捕获，然后返回json信息到前端
 */
@ControllerAdvice
public class GraceExceptionHandler {

    @ExceptionHandler(MyCustomException.class)
    @ResponseBody //@Responsebody注解表示该方法的返回的结果直接写入 HTTP 响应正文中，一般在异步获取数据时使用；
    public GraceJSONResult returnMyException(MyCustomException e) {
        e.printStackTrace();
        return GraceJSONResult.exception(e.getResponseStatusEnum()); //把异常信息放入回复实体中
    }

    // 如果发生了MethodArgumentNotValidException.class异常，就会被这个拦截器拦截处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public GraceJSONResult returnMethodArgumentNotValid(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        Map<String, String> map = getErrors(result);
        return GraceJSONResult.errorMap(map);
    }
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    @ResponseBody
//    public GraceJSONResult returnMethodArgumentNotValid(MethodArgumentNotValidException e) {
//        BindingResult result = e.getBindingResult();
//        Map<String, String> map = getErrors(result);
//        return GraceJSONResult.errorMap(map);
//    }
//
//    @ExceptionHandler(MaxUploadSizeExceededException.class)
//    @ResponseBody
//    public GraceJSONResult returnMaxUploadSize(MaxUploadSizeExceededException e) {
////        e.printStackTrace();
//        return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_MAX_SIZE_2MB_ERROR);
//    }
//
    public Map<String, String> getErrors(BindingResult result) {
        Map<String, String> map = new HashMap<>();
        List<FieldError> errorList = result.getFieldErrors();
        for (FieldError ff : errorList) {
            // 错误所对应的属性字段名
            String field = ff.getField();
            // 错误的信息
            String msg = ff.getDefaultMessage();
            map.put(field, msg);
        }
        return map;
    }
}
