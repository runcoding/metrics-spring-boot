package com.runcoding.monitor.support.webhook.dingtalk.model;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/03 14:24
 * @describe: 钉钉消息类型
 * https://open-doc.dingtalk.com/docs/doc.htm?spm=a219a.7629140.0.0.1BIP18&treeId=257&articleId=105735&docType=1
 **/
public enum DTMessageType {

    /**text*/
    text,

    /**link*/
    link,

    /**markdown*/
    markdown,

    /**独立跳转*/
    feedCard,

    /**整体跳转*/
    actionCard;

}
