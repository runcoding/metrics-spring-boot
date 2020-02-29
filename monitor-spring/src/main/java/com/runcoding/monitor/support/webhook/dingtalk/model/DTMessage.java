package com.runcoding.monitor.support.webhook.dingtalk.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.runcoding.monitor.support.webhook.dingtalk.model.content.DTActionCardContent;
import com.runcoding.monitor.support.webhook.dingtalk.model.content.DTMarkdownContent;
import com.runcoding.monitor.support.webhook.dingtalk.model.content.DTLinkContent;
import com.runcoding.monitor.support.webhook.dingtalk.model.content.DTTextContent;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/03 14:17
 * @describe: 钉钉消息
 *   * msgtype : text
 *      * text : {"content":"我就是我,  @1825718XXXX 是不一样的烟火"}
 *      * link : {"text":"群机器人是钉钉群的高级扩展功能。群机器人可以将第三方服务的信息聚合到群聊中，实现自动化的信息同步。例如：通过聚合GitHub，GitLab等源码管理服务，实现源码更新同步；通过聚合Trello，JIRA等项目协调服务，实现项目信息同步。不仅如此，群机器人支持Webhook协议的自定义接入，支持更多可能性，例如：你可将运维报警提醒通过自定义机器人聚合到钉钉群。","title":"自定义机器人协议","picUrl":"","messageUrl":"https://open-doc.dingtalk.com/docs/doc.htm?spm=a219a.7629140.0.0.Rqyvqo&treeId=257&articleId=105735&docType=1"}
 *      * markdown : {"title":"杭州天气","text":"##杭州天气  \n > 9度，@1825718XXXX 西北风1级，空气良89，相对温度73%\n\n > ![screenshot](http://i01.lw.aliimg.com/media/lALPBbCc1ZhJGIvNAkzNBLA_1200_588.png)\n  > ####10点20分发布 [天气](http://www.thinkpage.cn/) "}
 *      * actionCard : {"title":"乔布斯 20 年前想打造一间苹果咖啡厅，而它正是 Apple Store 的前身","text":"![screenshot](@lADOpwk3K80C0M0FoA) \n ##乔布斯 20 年前想打造的苹果咖啡厅 \n\n Apple Store 的设计正从原来满满的科技感走向生活化，而其生活化的走向其实可以追溯到 20 年前苹果一个建立咖啡馆的计划","hideAvatar":"0","btnOrientation":"0","singleTitle":"阅读全文","singleURL":"https://www.dingtalk.com/"}
 *      * at : {"atMobiles":["1825718XXXX"],"isAtAll":false}
 **/
public class DTMessage {

    @JSONField(name = "msgtype")
    private DTMessageType msgType;

    private DTTextContent text;

    private DTLinkContent link;

    private DTMarkdownContent markdown;

    private DTActionCardContent actionCard;

    private DTAt at;

    public DTMessage() {
    }


    public DTMessage(DTMessageType msgType) {
        this.msgType = msgType;
    }

    public DTMessageType getMsgType() {
        return msgType;
    }

    public void setMsgType(DTMessageType msgType) {
        this.msgType = msgType;
    }

    public DTTextContent getText() {
        return text;
    }

    public void setText(DTTextContent text) {
        this.text = text;
    }

    public DTLinkContent getLink() {
        return link;
    }

    public void setLink(DTLinkContent link) {
        this.link = link;
    }

    public DTMarkdownContent getMarkdown() {
        return markdown;
    }

    public void setMarkdown(DTMarkdownContent markdown) {
        this.markdown = markdown;
    }

    public DTActionCardContent getActionCard() {
        return actionCard;
    }

    public void setActionCard(DTActionCardContent actionCard) {
        this.actionCard = actionCard;
    }

    public DTAt getAt() {
        return at;
    }

    public void setAt(DTAt at) {
        this.at = at;
    }



}
