package com.androidx.okapi;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * OkHttp回调处理
 */
public class OkCallback implements Callback {

    /**
     * 请求Handler
     */
    private ApiHandler handler;
    /**
     * 请求监听
     */
    private OnRequestListener listener;

    public OkCallback(ApiHandler handler, OnRequestListener listener) {
        this.handler = handler;
        this.listener = listener;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        handler.send(ApiHandler.FAILED, call, null, e, listener);
    }

    @Override
    public void onResponse(Call call, okhttp3.Response response) throws IOException {
        handler.send(ApiHandler.SUCCEED, call, response, null, listener);
    }

}
