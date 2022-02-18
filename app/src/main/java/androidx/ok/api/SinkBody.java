package androidx.ok.api;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

/**
 * 文件上传接收器
 */
public class SinkBody extends RequestBody {

    /**
     * 进度
     */
    private int bytes;
    /**
     * 上传内容
     */
    private RequestBody body;
    /**
     * 消息传递者
     */
    private ApiMessenger messenger;
    /**
     * 上传监听
     */
    private OnBufferedSinkListener onBufferedSinkListener;

    /**
     * 构造函数
     *
     * @param body                   上传内容
     * @param messenger              消息传递者
     * @param onBufferedSinkListener 上传监听
     */
    public SinkBody(RequestBody body, ApiMessenger messenger, OnBufferedSinkListener onBufferedSinkListener) {
        this.body = body;
        this.messenger = messenger;
        this.onBufferedSinkListener = onBufferedSinkListener;
    }

    @Override
    public MediaType contentType() {
        return body.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return body.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        ForwardingSink forwardingSink = new ForwardingSink(sink) {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                bytes += byteCount;
                if (messenger != null) {
                    messenger.send(ApiMessenger.PROGRESS, contentLength(), bytes, onBufferedSinkListener);
                }
                super.write(source, byteCount);
            }
        };
        BufferedSink bufferedSink = Okio.buffer(forwardingSink);
        body.writeTo(bufferedSink);
        bufferedSink.flush();
    }

}