package com.runcoding.monitor.e2etest;

import com.alibaba.fastjson.util.TypeUtils;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * @author: runcoding
 * @email: runcoding@163.com
 * @created Time: 2019/07/9 10:26
 * @description 模拟用户请求http
 * Copyright (C), 2017-2018, runcoding
 **/
public class MockHttpClient {

    private static  final Logger logger = LoggerFactory.getLogger(MockHttpClient.class);

    private static RestTemplate restTemplate;

    static {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(60000);
        factory.setConnectTimeout(60000);
        restTemplate = new RestTemplate(factory);
    }

    /**post json请求*/
    public static <T> T postForEntity(String url ,String requestBody,   Class<T> responseType){
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = Lists.newArrayList(MediaType.APPLICATION_JSON_UTF8);
        headers.setAccept(mediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        HttpEntity entity = new HttpEntity(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return TypeUtils.castToJavaBean(response.getBody(),responseType);
    }

    /**post json请求*/
    public static <T> T putForEntity(String url ,String requestBody,  Class<T> responseType){
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = Lists.newArrayList(MediaType.APPLICATION_JSON_UTF8);
        headers.setAccept(mediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        HttpEntity entity = new HttpEntity(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.PUT, entity, String.class);
        return TypeUtils.castToJavaBean(response.getBody(),responseType);
    }

    /**get json请求*/
    public static <T> T getForEntity(String url ,String requestBody,  Class<T> responseType){
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = Lists.newArrayList(MediaType.APPLICATION_JSON_UTF8);
        headers.setAccept(mediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity entity = new HttpEntity(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entity, String.class);
        return TypeUtils.castToJavaBean(response.getBody(),responseType);
    }


}
