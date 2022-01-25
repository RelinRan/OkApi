package com.androidx.okapi;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;


import java.io.IOException;

public class ApiHandler extends Handler {

    public static final int SUCCEED = 200;
    public static final int FAILED = 500;
    public static final String TAG = "RequestHandler";

    public ApiHandler() {
    }

    /**
     * 发送信息
     *
     * @param what      类型
     * @param call      请求
     * @param result    响应结果
     * @param exception 异常
     * @param listener  监听
     */
    public void send(int what, okhttp3.Call call, okhttp3.Response result, Exception exception, OnRequestListener listener) {
        Message message = obtainMessage();
        message.what = what;
        ResponseBody responseBody = new ResponseBody();
        //请求参数
        Request request = new Request();
        request.url(call.request().url());
        request.method(call.request().method());
        request.headers(call.request().headers());
        request.body(call.request().body());
        responseBody.setRequest(request);
        //异常
        responseBody.setException(exception);
        //响应内容
        Response response = new Response();
        if (result != null) {
            response.code(result.code());
            response.headers(result.headers());
            response.message(result.message());
            response.protocol(result.protocol());
            response.request(result.request());
            try {
                String body = result.body().string();
                response.body(body);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        responseBody.setResponse(response);
        //监听
        responseBody.setListener(listener);
        message.obj = responseBody;
        sendMessage(message);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        ResponseBody params = (ResponseBody) msg.obj;
        Request request = params.getRequest();
        Exception exception = params.getException();
        Response response = params.getResponse();
        OnRequestListener listener = params.getListener();
        switch (msg.what) {
            case SUCCEED:
                if (listener != null) {
                    listener.onRequestSucceed(request, response);
                }
                break;
            case FAILED:
                if (exception.getMessage().contains("not permitted by network security policy")) {
                    ApiLog.e(TAG, "服务器请求异常", "当前为http请求，建议安全使用https，如果一定要使用http,可在AndroidManifest.xml配置android:usesCleartextTraffic=\"true\"");
                }
                if (listener != null) {
                    listener.onRequestFailed(request, exception);
                }
                break;
        }
    }

}
