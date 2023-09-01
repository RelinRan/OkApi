package androidx.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.RequestBody;

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

    private okhttp3.Call call;

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
            new JSONObject(body);
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
            new JSONArray(body);
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
            return new JSON().toObject(body(), target);
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

    public Call call() {
        return call;
    }

    public void call(Call call) {
        this.call = call;
    }

    public String header(String key) {
        return headers.get(key);
    }

    public String requestTag() {
        if (call == null) {
            return "";
        }
        if (call.request().tag() == null) {
            return "";
        }
        return (String) call.request().tag();
    }

    public RequestBody requestBody() {
        return request.body();
    }

    public String requestBodyString() {
        RequestBody requestBody = request.body();
        okio.Buffer buffer = new okio.Buffer();
        try {
            requestBody.writeTo(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Charset charset = StandardCharsets.UTF_8;
        MediaType contentType = requestBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(StandardCharsets.UTF_8);
        }
        return buffer.readString(charset);
    }

}
