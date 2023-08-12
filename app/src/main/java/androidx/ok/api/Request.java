package androidx.ok.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
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

    public String requestTag() {
        return headers.get(Api.REQUEST_TAG);
    }

    public String requestTag(String key) {
        return headers.get(key);
    }

    public String bodyString() {
        RequestBody requestBody = body();
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
