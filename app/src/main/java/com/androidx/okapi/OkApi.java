package com.androidx.okapi;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.internal.platform.Platform;

/**
 * 网络请求<br/>
 * 基于OkHttp的网络工具，支持Get、Post、Put、Delete、Patch请求方式<br/>
 */
public final class OkApi implements Api {

    public final String TAG = OkApi.class.getSimpleName();
    /**
     * Get请求
     */
    private final int GET = 1;
    /**
     * Post请求
     */
    private final int POST = 2;
    /**
     * Put请求
     */
    private final int PUT = 3;
    /**
     * Delete请求
     */
    private final int DELETE = 4;
    /**
     * Patch请求
     */
    private final int PATCH = 5;
    /**
     * 请求Handler处理
     */
    private ApiHandler handler;

    public OkApi() {
        handler = new ApiHandler();
    }

    @Override
    public void get(Context context, String path, RequestParams params, OnRequestListener listener) {
        request(context, GET, path, params, listener);
    }

    @Override
    public void post(Context context, String path, RequestParams params, OnRequestListener listener) {
        request(context, POST, path, params, listener);
    }

    @Override
    public void put(Context context, String path, RequestParams params, OnRequestListener listener) {
        request(context, PUT, path, params, listener);
    }

    @Override
    public void delete(Context context, String path, RequestParams params, OnRequestListener listener) {
        request(context, DELETE, path, params, listener);
    }

    @Override
    public void patch(Context context, String path, RequestParams params, OnRequestListener listener) {
        request(context, PATCH, path, params, listener);
    }

    @Override
    public void cancel(Context context) {
        List<Call> calls = getClient().dispatcher().runningCalls();
        String tag = getTag(context);
        for (Call call : calls) {
            if (call.request().tag() != null) {
                String requestTag = (String) call.request().tag();
                if (requestTag.equals(tag)) {
                    call.cancel();
                }
            }
        }
    }

    /**
     * 获取客户端
     *
     * @return
     */
    public OkHttpClient getClient() {
        Configure config = Configure.Config();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.protocols(config.protocols());
        builder.connectTimeout(config.connectTimeout(), TimeUnit.SECONDS);
        builder.readTimeout(config.readTimeout(), TimeUnit.SECONDS);
        builder.writeTimeout(config.writeTimeout(), TimeUnit.SECONDS);
        builder.connectionPool(config.connectionPool());
        builder.cookieJar(config.cookieJar());
        builder.dispatcher(config.dispatcher());
        List<Interceptor> interceptors = config.interceptors();
        int interceptorSize = interceptors == null ? 0 : interceptors.size();
        for (int i = 0; i < interceptorSize; i++) {
            builder.addInterceptor(interceptors.get(i));
        }
        builder.retryOnConnectionFailure(config.isRetryOnConnectionFailure());
        builder.sslSocketFactory(config.socketFactory(), Platform.get().trustManager(config.socketFactory()));
        builder.hostnameVerifier(config.hostnameVerifier());
        return builder.build();
    }

    /**
     * 创建表单数据
     *
     * @param params 请求参数
     * @return
     */
    protected MultipartBody createMultipartBody(RequestParams params) {
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        if (params == null || (params != null && (params.data() == null || params.file() == null))) {
            multipartBodyBuilder.addFormDataPart("", "");
        }
        //一般参数
        if (params != null && params.data() != null) {
            TreeMap<String, String> stringParams = params.data();
            for (String key : stringParams.keySet()) {
                String value = stringParams.get(key);
                multipartBodyBuilder.addFormDataPart(key, value);
            }
        }
        //文件参数
        if (params != null && params.file() != null) {
            TreeMap<String, File> fileParams = params.file();
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            //设置文件处理类型
            for (String key : fileParams.keySet()) {
                File file = fileParams.get(key);
                String fileName = file.getName();
                MediaType type = MediaType.parse(fileNameMap.getContentTypeFor(fileName));
                RequestBody fileBody = RequestBody.create(type, file);
                multipartBodyBuilder.addFormDataPart(key, fileName, fileBody);
            }
        }
        return multipartBodyBuilder.build();
    }

    /**
     * 添加头文件
     *
     * @param params  请求参数
     * @param builder 请求构建者
     */
    protected void addHeaders(RequestParams params, okhttp3.Request.Builder builder) {
        builder.addHeader("Connection", "close");
        builder.addHeader("Content-Type", Configure.Config().contentType());
        if (params != null && params.header() != null) {
            String agent = params.header().get(Header.USER_AGENT);
            builder.addHeader(Header.USER_AGENT, TextUtils.isEmpty(agent) ? "Android" : agent);
            TreeMap<String, String> headerParams = params.header();
            for (String key : headerParams.keySet()) {
                builder.addHeader(key, headerParams.get(key));
            }
        }
    }

    /**
     * 获取标识
     *
     * @param context 上下文
     * @return
     */
    protected String getTag(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return activity.getClass().getSimpleName();
        }
        if (context instanceof ContextWrapper) {
            ContextWrapper wrapper = (ContextWrapper) context;
            if (wrapper instanceof Activity) {
                Activity activity = (Activity) wrapper;
                return activity.getClass().getSimpleName();
            }
        }
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 获取完整路径
     *
     * @param path 路径
     * @return
     */
    protected String getUrl(String path) {
        String address = Configure.Config().address();
        return path.toLowerCase().startsWith("http") ? path : address + path;
    }

    /**
     * 获取带有参数的完整路径
     *
     * @param path    路径
     * @param params  参数
     * @return
     */
    protected String getParamsUrl(String path, RequestParams params) {
        StringBuffer buffer = new StringBuffer();
        String url = getUrl(path);
        buffer.append(url);
        if (params != null && params.data() != null) {
            buffer.append("?");
            TreeMap<String, String> stringParams = params.data();
            for (String key : stringParams.keySet()) {
                String value = stringParams.get(key);
                buffer.append(key);
                buffer.append("=");
                buffer.append(value);
                buffer.append("&");
            }
            if (buffer.toString().contains("&")) {
                buffer.deleteCharAt(buffer.lastIndexOf("&"));
            }
        }
        return buffer.toString();
    }

    /**
     * 创建请求对象
     *
     * @param context     上下文
     * @param url         地址
     * @param method      方法
     * @param builder     请求构建者
     * @param requestBody 请求内容
     * @return
     */
    protected okhttp3.Request createRequest(Context context, int method, String url, okhttp3.Request.Builder builder, RequestBody requestBody) {
        String tag = getTag(context);
        okhttp3.Request request = null;
        if (method == GET) {
            request = builder.url(url).tag(tag).build();
        }
        if (method == POST) {
            request = builder.url(url).post(requestBody).tag(tag).build();
        }
        if (method == PUT) {
            request = builder.url(url).put(requestBody).tag(tag).build();
        }
        if (method == DELETE) {
            request = builder.url(url).delete(requestBody).tag(tag).build();
        }
        if (method == PATCH) {
            request = builder.url(url).patch(requestBody).tag(tag).build();
        }
        return request;
    }

    /**
     * 多表单请求
     *
     * @param context  上下文
     * @param method   方法
     * @param params   参数
     * @param path     路径
     * @param listener 监听
     */
    protected void multipartBodyRequest(Context context, int method, RequestParams params, String path, OnRequestListener listener) {
        //生成请求体
        RequestBody body = createMultipartBody(params);
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        //请求路径
        String url = getUrl(path);
        builder.url(url);
        //添加Header
        addHeaders(params, builder);
        //缓存控制
        builder.cacheControl(Configure.Config().cacheControl());
        //传参数、文件或者混合
        builder.post(body);
        okhttp3.Request request = createRequest(context, method, url, builder, body);
        Call call = getClient().newCall(request);
        call.enqueue(new OkCallback(handler, listener));
    }

    /**
     * 单内容请求(JSON|BINARY)
     *
     * @param context  上下文
     * @param method   方法
     * @param params   参数
     * @param path     路径
     * @param listener 监听
     */
    protected void singleBodyRequest(Context context, int method, RequestParams params, String path, OnRequestListener listener) {
        String contentType = params.header().get(Header.CONTENT_TYPE);
        MediaType mediaType = MediaType.parse(contentType);
        String bodyString = params.body();
        String data = new Gson().toJson(params.data());
        String content = TextUtils.isEmpty(bodyString) ? data : bodyString;
        //字符转义处理
        if (contentType.equals(Api.JSON)) {
            content = Configure.Config().escapeJar().escape(content);
        }
        RequestBody body = RequestBody.create(mediaType, content);
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        //添加Header
        addHeaders(params, builder);
        //缓存控制
        builder.cacheControl(Configure.Config().cacheControl());
        String url = getUrl(path);
        okhttp3.Request request = createRequest(context, method, url, builder, body);
        Call call = getClient().newCall(request);
        call.enqueue(new OkCallback(handler, listener));
    }

    /**
     * Get请求
     *
     * @param context  上下文
     * @param params   参数
     * @param path     路径
     * @param listener 监听
     */
    protected void getRequest(Context context, RequestParams params, String path, OnRequestListener listener) {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        builder.addHeader("Connection", "close");
        //添加头文件
        addHeaders(params, builder);
        //缓存控制
        builder.cacheControl(Configure.Config().cacheControl());
        String url = getParamsUrl(path, params);
        okhttp3.Request request = createRequest(context, GET, url, builder, null);
        //请求加入调度
        Call call = getClient().newCall(request);
        call.enqueue(new OkCallback(handler, listener));
    }

    /**
     * 请求数据
     *
     * @param method   方法
     * @param path     接口
     * @param params   参数
     * @param listener 监听
     */
    public void request(Context context, int method, String path, RequestParams params, OnRequestListener listener) {
        if (Configure.Config() == null) {
            return;
        }
        if (method == GET) {
            getRequest(context, params, path, listener);
        } else {
            String contentType = params.header().get(Header.CONTENT_TYPE);
            if (contentType.equals(Api.FORM_DATA)) {
                multipartBodyRequest(context, method, params, path, listener);
            } else {
                singleBodyRequest(context, method, params, path, listener);
            }
        }
    }

}
