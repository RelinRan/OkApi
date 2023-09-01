package androidx.api;

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
    private ApiMessenger messenger;
    /**
     * 请求监听
     */
    private OnRequestListener onRequestListener;

    public OkCallback(ApiMessenger messenger, OnRequestListener onRequestListener) {
        this.messenger = messenger;
        this.onRequestListener = onRequestListener;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        if (onRequestListener != null) {
            messenger.send(ApiMessenger.FAILED, call, null, e, onRequestListener);
        }
    }

    @Override
    public void onResponse(Call call, okhttp3.Response response) {
        if (onRequestListener != null) {
            messenger.send(ApiMessenger.SUCCEED, call, response, null, onRequestListener);
        }
    }
}
