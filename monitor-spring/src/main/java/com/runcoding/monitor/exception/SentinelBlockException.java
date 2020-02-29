package com.runcoding.monitor.exception;

import com.runcoding.monitor.dto.Resp;

/**
 * @author: runcoding
 * @Date: 2019/07/06 上午13:22
 * @Description: 哨兵限流异常
 */
public class SentinelBlockException extends BusinessException {


    public SentinelBlockException(Resp resp) {
        super(resp);
    }
}
