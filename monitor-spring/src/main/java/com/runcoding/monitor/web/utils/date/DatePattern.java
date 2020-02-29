package com.runcoding.monitor.web.utils.date;

/**
 * @author runcoding
 * @desc  常用日期格式
 */
public enum DatePattern {

        SHORT("yyyy-MM-dd"),

        LONG("yyyy-MM-dd HH:mm:ss"),

        FULL("yyyy-MM-dd HH:mm:ss.S"),

        VIRGULE_SHORT("yyyy/MM/dd"),

        VIRGULE_LONG("yyyy/MM/dd HH:mm:ss"),

        VIRGULE_FULL("yyyy/MM/dd HH:mm:ss.S"),

        INT_MINITE("yyyyMMddHHmmss"),

        INT_DATE("yyyyMMdd"),

        YEAR_MONTH_CN("yyyy年MM月"),

        SHORT_CN("yyyy年MM月dd日"),

        LONG_CN("yyyy年MM月dd日 HH时mm分ss秒"),

        FULL_CN("yyyy年MM月dd日 HH时mm分ss秒SSS毫秒"),

        SPOT_DATE("yyyy.MM.dd"),

        YEAR("YYYY"),

        MONTH("MM"),

        DAY("dd");

        public final String pattern;

        DatePattern(String pattern) {
            this.pattern = pattern;
        }

    }