package com.runcoding.monitor.support.webhook.dingtalk;

import com.alibaba.fastjson.JSON;
import com.runcoding.monitor.web.utils.date.DatePattern;
import com.runcoding.monitor.web.utils.date.LocalDateUtil;
import com.runcoding.monitor.support.webhook.dingtalk.model.DTAt;
import com.runcoding.monitor.support.webhook.dingtalk.model.DTResult;
import com.runcoding.monitor.support.webhook.dingtalk.model.content.DTMarkdownContent;
import com.runcoding.monitor.support.metric.MetricProcessor;
import com.runcoding.monitor.support.webhook.dingtalk.model.DTMessage;
import com.runcoding.monitor.support.webhook.dingtalk.model.DTMessageType;
import com.runcoding.monitor.web.model.MonitorConstants;
import com.runcoding.monitor.web.utils.RestTemplateUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/31 11:54
 * @describe: 钉钉webhook自定义机器人。异常监控报警
 * https://open-doc.dingtalk.com/docs/doc.htm?spm=a219a.7629140.0.0.Rqyvqo&treeId=257&articleId=105735&docType=1
 *
 * https://oapi.dingtalk.com/robot/send?access_token=xxxxxxxx
 **/
public class DTWebHookProcessor {

    private static Logger  logger = LoggerFactory.getLogger(DTWebHookProcessor.class);

    private static String robotApiUrl = "https://oapi.dingtalk.com/robot/send?access_token=";
    /**每个机器人都不同*/
    private static String accessToken = "";

    /**控制发送次数{key:发送主题,value:发送时间}，一分钟发送一次*/
    private static Map<String ,Long> topicMap = new ConcurrentHashMap<>(16);

    /**被@人的手机号(在text内容里要有@手机号)*/
    private static Set<String> atMobiles      = null;

    /**启用发送*/
    public static boolean enableSend         = true;

    /**发送失败超过10次，将关闭发送*/
    public static int  errCnt                = 10;

    /**发送钉钉消息*/
    public static void  chatbotSend(DTMessage talkMessage){
        if(!enableSend){
            if(logger.isDebugEnabled()){
                logger.debug("发送钉钉消息已关闭");
            }
            return;
        }
        try {
            DTResult result = RestTemplateUtils.postForEntity(robotApiUrl + accessToken, JSON.toJSONString(talkMessage));
            if(result.getErrcode() == 0){
                return;
            }
            /**token is not exist*/
            if(result.getErrcode() == 300001 || result.getErrcode() == 40035 ){
                logger.warn("推送钉钉失败:accessToken={},配置有误。"+accessToken);
                enableSend = false;
            }
            logger.error("推送钉钉失败:{}"+JSON.toJSONString(result));
        }catch (Exception e){
            errCnt--;
            logger.error("推送钉钉失败:",e);
            if(errCnt <= 0 ){
                enableSend = false;
            }
        }
    }

    /**判断方法是否可以发送(1分钟发同一个方法，同一个异常只发一条)*/
    public static boolean checkEnableSend(String topic){
        /**判断当前线程是否可以发送。*/
        boolean enableSend = MetricProcessor.enableSend();
        if(!enableSend){
            return false;
        }
        long curSendTime = System.currentTimeMillis();
        Long sendTime    = topicMap.get(topic);
        if(sendTime != null && (curSendTime - sendTime) <= 60 * MonitorConstants.millisecond){
            logger.debug("减少异常报警：上一次：{}",sendTime);
            return false;
        }
        sendTime = topicMap.put(topic,curSendTime);
        if(sendTime == null || (curSendTime - sendTime) > 60 * MonitorConstants.millisecond){
            return true;
        }
        logger.debug("出现并发，减少异常报警：上一次：{}",sendTime);
        return false;
    }

    /**发送markdown类型的钉钉消息*/
    public static void  chatbotSendByMarkdown(String title, String text,boolean isAtAll){
        DTMarkdownContent content = new DTMarkdownContent(title, getContent(text,isAtAll));
        DTMessage dtMessage = new DTMessage(DTMessageType.markdown);
        dtMessage.setMarkdown(content);
        dtMessage.setAt(new DTAt(isAtAll,atMobiles));
        chatbotSend(dtMessage);
    }


    /**更新被@人名单*/
    public static void setAtMobiles(Set<String> atMobiles) {
        DTWebHookProcessor.atMobiles = atMobiles;
    }

    /**配置每个机器人accountToken*/
    public static void setAccessToken(String accessToken) {
        DTWebHookProcessor.accessToken = accessToken;
    }

    /**获取被@的人*/
    public static String getContent(String text, boolean isAtAll) {
        /**{"errcode":101002,"errmsg":"markdown is too long"}*/
        if(StringUtils.length(text) > 10000){
            text =  StringUtils.substring(text,0,10000);
        }

        String currTime = LocalDateUtil.getDateTimeNow().toString(DatePattern.LONG.pattern);
        StringBuilder content = new StringBuilder("### ");
        content.append(text);
        if(isAtAll || CollectionUtils.isEmpty(atMobiles)){
            content.append("\n - 问题处理人:@所有人").append("\n - 时间:"+currTime);
            return content.toString();
        }

        content.append("\n - 问题处理人:");
        atMobiles.forEach(mobile->{
            content.append("@");
            content.append(mobile);
        });
            content.append("\n - 时间:"+currTime);
        return content.toString();
    }
}
