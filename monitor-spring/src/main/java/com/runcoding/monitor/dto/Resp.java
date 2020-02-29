package com.runcoding.monitor.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.runcoding.monitor.exception.BusinessException;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * 返回实体包装类
 * @author runcoding
 * @date 2017年9月3日 下午8:19:10
 */
public class Resp<E> implements Serializable {

    @ApiModelProperty("请求响应状态")
    private String status;

    @ApiModelProperty("消息提示")
    private String message;

    @ApiModelProperty("返回结果")
    private E data;

    public Resp(String status, String message, E data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Resp() {
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JSONField(serialize = false)
    public boolean isSuccess() {
        return StringUtils.equals(RespStatus.SUCCESS.code,status);
    }

    @JSONField(serialize = false)
    public boolean isFailure() {
        return  !StringUtils.equals(RespStatus.SUCCESS.code,status);
    }

    /** 业务处理成功 */
    public static <T> Resp<T> success(String message) {
        return success(message, null);
    }

    public static <T> Resp<T> success() {
        return success(null, null);
    }

    public static <T> Resp<T> success(T data) {
        return success(null, data);
    }

    public static <T> Resp<T> success(String message, T data) {
        return new Resp(RespStatus.SUCCESS.code, message, data);
    }

    /**  业务处理失败 */
    public static <T> Resp<T> failure(String message) {
        return failure(message, null);
    }

    public static <T> Resp<T> failure(String message, T data) {
        return new Resp(RespStatus.FAILURE.code, message, data);
    }

    /**抛出异常*/
    public static <T> void throwFailure(String message) {
        throw  new BusinessException(failure(message, null));
    }

    /** 系统异常 */
    public static <T> Resp<T> error(String message) {
        return new Resp(RespStatus.INTERNAL_SERVER_ERROR.code, message, null);
    }

}
