package com.runcoding.monitor.web.model.endpoint;

public enum MonitorRespStatus {

    Success(200, "业务处理成功"),
    Failure(400, "业务处理失败")
    ;

    public final int code;
    public final String name;

    MonitorRespStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

}
