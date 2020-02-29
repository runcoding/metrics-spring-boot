package com.runcoding.monitor.web.utils;

import com.alibaba.csp.sentinel.util.HostNameUtil;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/17 16:17
 * @describe: 获取服务器运行ip
 **/
public class IpUtils {

    private static String ip;

    static {
        ip = HostNameUtil.getIp();
    }

    public static String getIp() {
        return ip;
    }

    public static void setIp(String ip) {
        if(StringUtils.isBlank(ip)){
            return;
        }
        IpUtils.ip = ip;
    }
}
