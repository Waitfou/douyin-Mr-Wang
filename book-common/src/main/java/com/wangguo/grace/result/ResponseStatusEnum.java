package com.wangguo.grace.result;

/**
 * 响应结果枚举，用于提供给GraceJSONResult返回给前端的
 * 本枚举类包含了很多的不同状态码供使用，可以自定义
 * 便于更优雅的对状态码进行管理，一目了然
 */
public enum ResponseStatusEnum {
    SUCCESS(200, true, "操作成功"),
    FAILED(500, false, "操作失败"),

    //
    UN_LOGIN(501, false, "请登录后再继续操作"),
    TICKET_INVALID(502, false, "会话失效，请重新登录"),
    NO_AUTH(503, false, "您的权限不足，无法继续操作"),
    MOBILE_ERROR(504, false, "短信发送失败，请稍后重试"),

    SMS_NEED_WAIT_ERROR(505, false, "短信发送太快啦， 请稍后再试");
    private final Integer status;
    private final Boolean success;
    private final String msg;

    ResponseStatusEnum(Integer status, Boolean success, String msg) {
        this.success = success;
        this.status = status;
        this.msg = msg;
    }
    public Integer status() {
        return status;
    }
    public Boolean success() {
        return success;
    }
    public String msg() {
        return msg;
    }
}
