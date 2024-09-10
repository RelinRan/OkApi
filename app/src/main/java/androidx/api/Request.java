package androidx.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
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
    /**
     * 请求
     */
    private Call call;


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

    public Call call() {
        return call;
    }

    public void call(Call call) {
        this.call = call;
    }

    public Headers headers() {
        return headers;
    }

    public void headers(Headers headers) {
        this.headers = headers;
    }

    public String header(String key) {
        return headers.get(key);
    }

    public String tag() {
        if (call == null) {
            return "";
        }
        if (call.request()==null){
            return "";
        }
        if (call.request().tag() == null) {
            return "";
        }
        return (String) call.request().tag();
    }

    public RequestBody body() {
        return body;
    }

    public void body(RequestBody body) {
        this.body = body;
    }

    public String bodyString() {
        RequestBody requestBody = body();
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
    public void cancel() {
        url = null;
        body = null;
        headers = null;
        method = null;
        if (call != null) {
            call.cancel();
        }
        call = null;
    }

}
