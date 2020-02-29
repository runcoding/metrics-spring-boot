package com.runcoding.monitor.web.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author runcoding
 * @date 2019-08-09
 * @desc: 密码编码类
 */
public class PasswordEncoder {

    private static BCryptPasswordEncoder passwordEncoder;

    /**密码编码*/
    public static String encode(String  password){
        if(StringUtils.isBlank(password)){
            return "";
        }
        return getPasswordEncoder().encode(password);
    }

    /**
     * 密码匹配
     * @param rawPassword     原始密码
     * @param encodedPassword 加密编号(含有盐)
     */
    public static boolean matches(String rawPassword, String encodedPassword){
        return getPasswordEncoder().matches(rawPassword,encodedPassword);
    }

    private static BCryptPasswordEncoder getPasswordEncoder() {
        if(passwordEncoder != null){
           return passwordEncoder ;
        }
        synchronized (PasswordEncoder.class){
            if(passwordEncoder != null){
                return passwordEncoder ;
            }
            passwordEncoder = new BCryptPasswordEncoder();
        }
        return passwordEncoder;
    }
}
