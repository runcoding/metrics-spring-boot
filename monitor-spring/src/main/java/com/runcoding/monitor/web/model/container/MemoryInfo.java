package com.runcoding.monitor.web.model.container;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by runcoding on 2017/7/27.
 * 内存信息
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoryInfo {

    private String name;

    /**初始(M)*/
    private long init;

    /**最大(上限)(M)*/
    private long max;

    /**当前(已使用)(M)*/
    private long used;

    /**提交的内存(已申请)(M)*/
    private long committed;

    /**使用率(%)*/
    private long usedRate;


}
