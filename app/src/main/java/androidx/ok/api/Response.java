package androidx.ok.api;

import okhttp3.Headers;
import okhttp3.Protocol;

/**
 * 请求响应
 */
public class Response {

    /**
     * 响应代码
     */
    private int code;
    /**
     * 响应消息
     */
    private String message;
    /**
     * 响应结果
     */
    private String body;
    /**
     * 头部参数
     */
    private Headers headers;
    /**
     * 协议
     */
    private Protocol protocol;
    /**
     * 请求
     */
    private okhttp3.Request request;

    /**
     * 是否成功
     *
     * @return
     */
    public boolean isSuccessful() {
        return this.code >= 200 && this.code < 300;
    }

    /**
     * 转换为实体
     *
     * @param target 实体类
     * @param <T>    类名
     * @return
     */
    public <T> T convert(Class<T> target) {
        return JSON.toObject(body(), target);
    }

    public String message() {
        return message;
    }

    public void message(String message) {
        this.message = message;
    }

    public Headers headers() {
        return headers;
    }

    public void headers(Headers headers) {
        this.headers = headers;
    }

    public Protocol protocol() {
        return protocol;
    }

    public void protocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public int code() {
        return code;
    }

    public void code(int code) {
        this.code = code;
    }

    public String body() {
        return body;
    }

    public void body(String body) {
        this.body = body;
    }

    public okhttp3.Request request() {
        return request;
    }

    public void request(okhttp3.Request request) {
        this.request = request;
    }

}
