package androidx.api;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
        call.cancel();
    }

    @Override
    public void onResponse(Call call, okhttp3.Response response) {
        if (onRequestListener != null) {
            if (response.isSuccessful()) {
                messenger.send(ApiMessenger.SUCCEED, call, response, null, onRequestListener);
            } else {
                String content = null;
                try {
                    content = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (content == null) {
                    content = String.valueOf(response.code());
                }
                messenger.send(ApiMessenger.FAILED, call, response, new Exception(content), onRequestListener);
            }
        }
        call.cancel();
        response.close();
    }
}
