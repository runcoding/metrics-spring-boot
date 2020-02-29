package com.runcoding.monitor.support.server;

import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.runcoding.monitor.exception.BusinessException;
import com.runcoding.monitor.dto.Resp;
import com.runcoding.monitor.dto.RespStatus;
import com.runcoding.monitor.web.model.monitor.MonitorUserInfo;
import com.runcoding.monitor.web.utils.PasswordEncoder;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author runcoding
 * @date 2019-07-16
 * @desc: monitor监控会话管理
 */
@Service
public class MonitorSessionService {

    @Value("${spring.profiles.active:local}")
    private String appEnv;

    @Value("${runcoding.monitor.username:admin}")
    private String username;

    @Value("${runcoding.monitor.password:admin}")
    private String password;

    private static String OPEN_ENV = "local-dev-test-stresstest";

    private static String OPEN_SERVER_NAME = "localhost-127.0.0.1";

    public String login( MonitorUserInfo user){
        if(username.equals(user.getUsername()) && password.equals(user.getPassword())){
            return getToken() ;
        }
        throw  new BusinessException(Resp.failure("用户名或密码错误"));
    }

    public void isLogin(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String token = request.getHeader("Monitor-Authorization");
        if(StringUtils.isNotBlank(token)){
            if(PasswordEncoder.matches(password,token)){
                return ;
            }
        }

        /**生产和预发环境需要登录*/
        if(StringUtils.containsAny(OPEN_ENV,appEnv)){
           return ;
        }

        /**本服务调用不需要登录*/
        String serverName = request.getServerName();
        if(StringUtils.containsAny(OPEN_SERVER_NAME,serverName) ||
           StringUtils.equalsIgnoreCase(serverName, HostNameUtil.getIp())){
            return ;
        }
        //throw  new BusinessException(new Resp(RespStatus.SESSION_NOT_AVAILABLE.code,"需要登录",null));
    }

    public String getToken() {
        return PasswordEncoder.encode(password);
    }

}
