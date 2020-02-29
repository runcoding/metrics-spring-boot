package com.runcoding.monitor.web.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/12 10:32
 * @describe:
 **/
public class MethodUtils {

    private static Logger logger = LoggerFactory.getLogger(MethodUtils.class);

    /**方法参数*/
    public static String methodArgs(Object[] args){
        if(args == null){
            return "";
        }
        try{
            String  argJsonStr = JSON.toJSONString(args,SerializerFeature.IgnoreNonFieldGetter);
            return StringUtils.length(argJsonStr) > 200 ? StringUtils.substring(argJsonStr,0,200) : argJsonStr;
        }catch (Exception e){
            logger.warn(e.getMessage());
        }
        return "";
    }

}
