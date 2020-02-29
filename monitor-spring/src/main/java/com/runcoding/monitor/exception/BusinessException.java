package com.runcoding.monitor.exception;

import com.runcoding.monitor.dto.Resp;

/**
 * @author: runcoding
 * @Date: 2019/07/06 上午13:22
 * @Description: 业务处理异常
 */
public class BusinessException extends RuntimeException {

    private Resp resp;

    public BusinessException(Resp resp) {
        this.resp = resp;
    }

    public Resp getResp() {
        return resp;
    }

    public void setResp(Resp resp) {
        this.resp = resp;
    }
}
