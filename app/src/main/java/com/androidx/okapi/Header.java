package com.androidx.okapi;

import java.util.TreeMap;

/**
 * 请求头部
 */
public class Header extends TreeMap<String, String> {

    /**
     * Header - Content-Type
     */
    public static final String CONTENT_TYPE = "Content-Type";
    /**
     * Header - User-Agent
     */
    public static final String USER_AGENT = "User-Agent";
    /**
     * 连接
     */
    public static final String CONNECTION = "Connection";
    /**
     * Header[自定义] - Cookie-Expires
     */
    public static final String COOKIE_EXPIRES = "Cookie-Expires";

    public Header() {
        add(USER_AGENT, "Android");
        add(CONTENT_TYPE, Api.JSON);
    }

    /**
     * 添加Header
     *
     * @param key   键
     * @param value 值
     */
    public void add(String key, String value) {
        put(key, value);
    }

}
