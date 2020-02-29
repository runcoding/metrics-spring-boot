package com.runcoding.monitor.dto;

/**
 * @author runcoding
 * @desc
 */
public enum RespStatus {

    /**业务处理成功*/
    SUCCESS("200", "业务处理成功"),

    /**默认的业务处理异常，特殊情况需要业务系统自定义*/
    FAILURE("400", "业务处理失败"),

    SESSION_NOT_AVAILABLE("401", "会话过期请重新登录"),

    FORBIDDEN("403", "无权限访问"),

    METHOD_NOT_ALLOWED("405", "请求方法不支持"),

    /**签名验证不通过*/
    PRECONDITION_FAILED("412", "签名验证不通过"),

    COMPULSORYRENEWAL("436","强制更新"),

    /**系统异常*/
    INTERNAL_SERVER_ERROR("500", "服务器内部异常")
    ;

    public final String code;

    public final String name;

    RespStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
