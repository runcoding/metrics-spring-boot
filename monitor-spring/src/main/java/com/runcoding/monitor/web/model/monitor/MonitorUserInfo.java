package com.runcoding.monitor.web.model.monitor;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: runcoding
 * @email: runcoding@163.com
 * @created Time: 2018/6/2 10:31
 * @description 统计用户信息
 * Copyright (C), 2017-2018,
 **/
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorUserInfo {


    private String username;

    @JSONField(serialize = false)
    private String password;


}
