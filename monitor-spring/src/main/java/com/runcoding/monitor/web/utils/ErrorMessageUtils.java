package com.runcoding.monitor.web.utils;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/29 18:31
 * @describe: 异常信息
 **/
public class ErrorMessageUtils {


    public static String getMessage( Throwable t){
        StringBuilder str = new StringBuilder(t.toString());
        str.append("\n StackTrace:");
        StackTraceElement[] stackTrace = t.getStackTrace();
        if(stackTrace != null){
            str.append(stackTrace.length >1 ? stackTrace[0] : "");
        }
        str.append("\n message:");
        str.append(t.getMessage());
        return str.toString();
    }


}
