package com.runcoding.monitor.web.model.container;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author runcoding
 * @date 2019-07-19
 * @desc: GC信息
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GarbageCollectorInfo {

    private String gcName;

    private Long gcCount;

    /**总gc耗时毫秒*/
    private Long gcTime;

}
