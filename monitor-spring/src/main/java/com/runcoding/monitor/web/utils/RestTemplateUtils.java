package com.runcoding.monitor.web.utils;

import com.alibaba.fastjson.JSON;
import com.runcoding.monitor.dto.Resp;
import com.runcoding.monitor.support.webhook.dingtalk.model.DTResult;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/04 10:30
 * @describe: http utils
 **/
public class RestTemplateUtils {


    private static RestTemplate restTemplate;

    static {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(30000);
        factory.setConnectTimeout(3000);
        restTemplate = new RestTemplate(factory);
    }


    /**post json请求*/
    public static DTResult postForEntity(String url , String requestBody){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity entity = new HttpEntity(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        DTResult resp = JSON.parseObject(response.getBody(), DTResult.class);
        return resp;
    }

    /**put json请求*/
    public static Resp putForEntity(String url , String authorization, String requestBody){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        if(StringUtils.isNotBlank(authorization)){
            headers.set("Monitor-Authorization", authorization);
        }
        HttpEntity entity = new HttpEntity(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url,HttpMethod.PUT, entity, String.class);
        Resp resp = JSON.parseObject(response.getBody(), Resp.class);
        return resp;
    }


}
