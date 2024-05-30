package androidx.api;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.charset.Charset;

public class ApiMessenger extends Handler {

    public static final int SUCCEED = 200;
    public static final int FAILED = 500;
    public static final int PROGRESS = 600;
    public static final String TAG = ApiMessenger.class.getSimpleName();

    public ApiMessenger() {
    }

    public ApiMessenger(@NonNull Looper looper) {
        super(looper);
    }

    /**
     * 创建响应结果体
     *
     * @param call      请求
     * @param result    响应结果
     * @param exception 异常
     * @return
     */
    private ResponseBody createResponseBody(okhttp3.Call call, okhttp3.Response result, Exception exception) {
        ResponseBody responseBody = new ResponseBody();
        //请求参数
        Request request = new Request();
        request.call(call);
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
            response.call(call);
            response.code(result.code());
            response.headers(result.headers());
            response.message(result.message());
            response.protocol(result.protocol());
            response.request(result.request());
            byte[] bytes = new byte[0];
            try {
                bytes = result.body().bytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
            response.bytes(bytes);
            response.body(new String(bytes, Charset.forName("UTF-8")));
        }
        responseBody.setResponse(response);
        return responseBody;
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
        ResponseBody responseBody = createResponseBody(call, result, exception);
        responseBody.setOnRequestListener(listener);
        message.obj = responseBody;
        sendMessage(message);
    }

    /**
     * 发送信息
     *
     * @param what          类型
     * @param contentLength 大小
     * @param progress      进度
     * @param listener      监听
     */
    public void send(int what, long contentLength, long progress, OnBufferedSinkListener listener) {
        Message message = obtainMessage();
        message.what = what;
        ResponseBody responseBody = new ResponseBody();
        responseBody.setBufferedSinkListener(listener);
        message.obj = responseBody;
        Bundle bundle = new Bundle();
        bundle.putLong("contentLength", contentLength);
        bundle.putLong("progress", progress);
        message.setData(bundle);
        sendMessage(message);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        ResponseBody params = (ResponseBody) msg.obj;
        Request request = params.getRequest();
        Exception exception = params.getException();
        Response response = params.getResponse();
        OnRequestListener requestListener = params.getRequestListener();
        OnBufferedSinkListener bufferedSinkListener = params.getBufferedSinkListener();
        if (msg.what == SUCCEED) {
            if (requestListener != null) {
                requestListener.onRequestSucceed(request, response);
            }
        }
        if (msg.what == FAILED) {
            if (exception != null && exception.getMessage() != null) {
                if (exception.getMessage().contains("not permitted by network security policy")) {
                    ApiLog.e(TAG, "服务器请求异常", "当前为http请求，建议安全使用https，如果一定要使用http,可在AndroidManifest.xml配置android:usesCleartextTraffic=\"true\"");
                }
            }
            if (requestListener != null) {
                requestListener.onRequestFailed(request, exception);
            }
        }
        if (msg.what == PROGRESS) {
            if (bufferedSinkListener != null) {
                Bundle data = msg.getData();
                long contentLength = data.getLong("contentLength");
                long progress = data.getLong("progress");
                bufferedSinkListener.onBufferedSinkWrite(contentLength, progress);
            }
        }
    }

}
