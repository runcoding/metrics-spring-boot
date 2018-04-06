package com.runcoding.monitor.web.model.endpoint;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * 返回实体包装类
 * @author xukai
 * @date 2018年3月3日 下午8:19:10
 */
public class MonitorResp<E> implements Serializable {

    private Integer status;

    private String message;

    private E data;

    public MonitorResp(Integer status, String message, E data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public MonitorResp() {
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
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
        return status == MonitorRespStatus.Success.code;
    }

    @JSONField(serialize = false)
    public boolean isFailure() {
        return status != MonitorRespStatus.Success.code;
    }

    /**
     * 业务处理成功
     */
    public static <T> MonitorResp<T> success(String message) {
        return success(message, null);
    }

    public static <T> MonitorResp<T> success() {
        return success(null, null);
    }

    public static <T> MonitorResp<T> success(T data) {
        return success(null, data);
    }

    public static <T> MonitorResp<T> success(String message, T data) {
        return new MonitorResp(MonitorRespStatus.Success.code, message, data);
    }

    /**
     * 业务处理失败
     */
    public static <T> MonitorResp<T> failure(String message) {
        return failure(message, null);
    }

    public static <T> MonitorResp<T> failure(String message, T data) {
        return new MonitorResp(MonitorRespStatus.Failure.code, message, data);
    }

    public static <T> MonitorResp<T> set(MonitorRespStatus respStatus) {
        return set(respStatus, respStatus.name, null);
    }
    public static <T> MonitorResp<T> set(MonitorRespStatus respStatus, String message) {
        return set(respStatus, message, null);
    }
    public static <T> MonitorResp<T> set(MonitorRespStatus respStatus, String message, T data) {
        return new MonitorResp(respStatus.code, message, data);
    }
    public static <T> MonitorResp<T> valueOf(MonitorResp resp) {
        return new MonitorResp(resp.getStatus(), resp.getMessage(), null);
    }


}
