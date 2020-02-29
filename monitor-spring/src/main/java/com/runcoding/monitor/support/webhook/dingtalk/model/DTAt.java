package com.runcoding.monitor.support.webhook.dingtalk.model;

import java.util.Set;

public  class DTAt {
    /**
     * atMobiles : ["1825718XXXX"]
     * isAtAll : false
     */

    private boolean isAtAll;


    public DTAt() {
    }

    public DTAt(boolean isAtAll, Set<String> atMobiles) {
        this.isAtAll = isAtAll;
        this.atMobiles = isAtAll ? null : atMobiles;
    }

    private Set<String> atMobiles;

    public boolean isIsAtAll() {
        return isAtAll;
    }

    public boolean isAtAll() {
        return isAtAll;
    }

    public void setAtAll(boolean atAll) {
        isAtAll = atAll;
    }

    public Set<String> getAtMobiles() {
        return atMobiles;
    }

    public void setAtMobiles(Set<String> atMobiles) {
        this.atMobiles = atMobiles;
    }
}