package com.androidx.okapi;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.RequestBody;

/**
 * 请求包
 */
public class Request {

    /**
     * 网络URL
     */
    private HttpUrl url;
    /**
     * 请求方法
     */
    private String method;
    /**
     * 头部参数
     */
    private Headers headers;
    /**
     * 请求体
     */
    private RequestBody body;

    public HttpUrl url() {
        return url;
    }

    public void url(HttpUrl url) {
        this.url = url;
    }

    public String method() {
        return method;
    }

    public void method(String method) {
        this.method = method;
    }

    public Headers headers() {
        return headers;
    }

    public void headers(Headers headers) {
        this.headers = headers;
    }

    public RequestBody body() {
        return body;
    }

    public void body(RequestBody body) {
        this.body = body;
    }
}
