package androidx.ok.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
     * @return 返回内容是否是JSON
     */
    public boolean isJsonBody() {
        if (body == null || body.length() == 0) {
            return false;
        }
        return isJsonObject(body) || isJsonArray(body);
    }

    /**
     * @param body 内容
     * @return 是否是JSONObject
     */
    public boolean isJsonObject(String body) {
        try {
            JSONObject jsonObject = new JSONObject(body);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param body 内容
     * @return 是否是JSONArray
     */
    public boolean isJsonArray(String body) {
        try {
            JSONArray jsonArray = new JSONArray(body);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 转换为实体
     *
     * @param target 实体类
     * @param <T>    类名
     * @return
     */
    public <T> T convert(Class<T> target) {
        if (isJsonBody()) {
            return JSON.toObject(body(), target);
        } else {
            Log.e(Response.class.getSimpleName(), "The returned data is not json and cannot be converted normally.");
        }
        return null;
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
