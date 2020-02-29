package com.runcoding.monitor.support.webhook.dingtalk.model.content;
/**
 * title : 杭州天气
 * text : ##杭州天气
 > 9度，@1825718XXXX 西北风1级，空气良89，相对温度73%

 > ![screenshot](http://i01.lw.aliimg.com/media/lALPBbCc1ZhJGIvNAkzNBLA_1200_588.png)
 > ####10点20分发布 [天气](http://www.thinkpage.cn/)
 */
public  class DTMarkdownContent {

    private String title;

    private String text;

    public DTMarkdownContent() {
    }

    public DTMarkdownContent(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}