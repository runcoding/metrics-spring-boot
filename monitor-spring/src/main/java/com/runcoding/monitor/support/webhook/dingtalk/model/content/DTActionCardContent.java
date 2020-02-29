package com.runcoding.monitor.support.webhook.dingtalk.model.content;

/**
 * title : 乔布斯 20 年前想打造一间苹果咖啡厅，而它正是 Apple Store 的前身
 * text : ![screenshot](@lADOpwk3K80C0M0FoA)
 ##乔布斯 20 年前想打造的苹果咖啡厅

 Apple Store 的设计正从原来满满的科技感走向生活化，而其生活化的走向其实可以追溯到 20 年前苹果一个建立咖啡馆的计划
 * hideAvatar : 0
 * btnOrientation : 0
 * singleTitle : 阅读全文
 * singleURL : https://www.dingtalk.com/
 */
public  class DTActionCardContent {

        private String title;

        private String text;

        private String hideAvatar;

        private String btnOrientation;

        private String singleTitle;

        private String singleURL;

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

        public String getHideAvatar() {
            return hideAvatar;
        }

        public void setHideAvatar(String hideAvatar) {
            this.hideAvatar = hideAvatar;
        }

        public String getBtnOrientation() {
            return btnOrientation;
        }

        public void setBtnOrientation(String btnOrientation) {
            this.btnOrientation = btnOrientation;
        }

        public String getSingleTitle() {
            return singleTitle;
        }

        public void setSingleTitle(String singleTitle) {
            this.singleTitle = singleTitle;
        }

        public String getSingleURL() {
            return singleURL;
        }

        public void setSingleURL(String singleURL) {
            this.singleURL = singleURL;
        }
    }