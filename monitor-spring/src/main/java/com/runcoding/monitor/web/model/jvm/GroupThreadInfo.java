package com.runcoding.monitor.web.model.jvm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by xukai on 2017/7/27.
 * 线程信息
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupThreadInfo {

    /**线程名称*/
    private String threadName;

    /**线程id*/
    private long   threadId;

    /**线程的状态*/
    private  Thread.State threadState;


}
