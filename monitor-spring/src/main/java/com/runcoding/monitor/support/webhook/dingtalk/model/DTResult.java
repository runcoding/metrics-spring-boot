package com.runcoding.monitor.support.webhook.dingtalk.model;

/**
 *
 * @author: runcoding@163.com
 * @date: 2019/07/03 14:44
 * @describe:
 **/
public class DTResult {


    /**
     * errmsg : ok
     * errcode : 0
     */

    private String errmsg;

    private int errcode;

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }
}
