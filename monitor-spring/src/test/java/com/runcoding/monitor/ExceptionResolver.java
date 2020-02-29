package com.runcoding.monitor;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.runcoding.monitor.dto.Resp;
import com.runcoding.monitor.exception.SentinelBlockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Component
public class ExceptionResolver {

    private Logger  logger  = LoggerFactory.getLogger(ExceptionResolver.class);

    @ExceptionHandler(value = SentinelBlockException.class)
    @ResponseBody
    public Resp jsonErrorHandler(Exception e) {
        logger.error("出错了:",e);
        if (BlockException.isBlockException(e) || e instanceof SentinelBlockException){
            return Resp.failure("服务繁忙，请稍后重试……");
        }
        return Resp.failure("服务异常:"+e.getMessage());
    }

}
