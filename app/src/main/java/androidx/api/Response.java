package androidx.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

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
     * 响应字节
     */
    private byte[] bytes;
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
     * 请求
     */
    private okhttp3.Call call;
    /**
     * JSON解析
     */
    private JSON json;
    private JSONArray jsonArray;
    private JSONObject jsonObject;

    public Response() {

    }

    /**
     * json解析对象
     *
     * @return
     */
    public JSON json() {
        if (json == null) {
            json = JSON.acquire();
        }
        return json;
    }

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
        return isJsonObject(body) || isJsonArray(body);
    }

    /**
     * @return 是否是JSONObject
     */
    public boolean isJsonObject(String body) {
        if (body == null || body.length() == 0) {
            return false;
        }
        try {
            jsonObject = new JSONObject(body);
        } catch (JSONException e) {
            return false;
        } finally {
            jsonObject = null;
        }
        return true;
    }

    /**
     * @return 是否是JSONArray
     */
    public boolean isJsonArray(String body) {
        if (body == null || body.length() == 0) {
            return false;
        }
        try {
            jsonArray = new JSONArray(body);
        } catch (JSONException e) {
            return false;
        } finally {
            jsonArray = null;
        }
        return true;
    }

    /**
     * 转换为实体
     *
     * @param target 实体类
     * @param <T>    类名
     * @return
     */
    public <T> T toObject(Class<T> target) {
        if (isJsonBody()) {
            return json().toObject(body(), target);
        } else {
            Log.e(Response.class.getSimpleName(), "The returned data is not json and cannot be converted normally.");
        }
        return null;
    }

    /**
     * 转换为实体
     *
     * @param target 实体类
     * @param <T>    类名
     * @return
     */
    public <T, C extends List> C toList(Class<T> target) {
        if (isJsonArray(body())) {
            return json().toList(body(), target);
        } else {
            Log.e(Response.class.getSimpleName(), "The returned data is not json and cannot be converted normally.");
        }
        return null;
    }

    /**
     * 转换为实体
     *
     * @param collectionType 集合类型
     * @param clazz          对象类
     * @param <T>            类名
     * @return
     */
    public <T, C extends Collection> C toCollection(Class<?> collectionType, Class<T> clazz) {
        if (isJsonArray(body())) {
            return json().toCollection(body(), collectionType, clazz);
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

    public byte[] bytes() {
        return bytes;
    }

    public void bytes(byte[] bytes) {
        this.bytes = bytes;
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

    /**
     * 请求体
     *
     * @return
     */
    public RequestBody requestBody() {
        return request.body();
    }

    /**
     * 请求体字符串
     *
     * @return
     */
    public String requestBodyString() {
        RequestBody requestBody = request.body();
        okio.Buffer buffer = new okio.Buffer();
        try {
            requestBody.writeTo(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Charset charset = StandardCharsets.UTF_8;
        MediaType contentType = requestBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(StandardCharsets.UTF_8);
        }
        return buffer.readString(charset);
    }

    /**
     * 资源释放
     */
    public void close() {
        message = null;
        body = null;
        headers = null;
        protocol = null;
        request = null;
        if (call != null) {
            call.cancel();
            call = null;
        }
        if (bytes != null) {
            bytes = null;
        }
        jsonObject = null;
        jsonArray = null;
        json = null;
    }

}
