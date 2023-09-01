package androidx.api;

public interface OnBufferedSinkListener {

    /**
     * 缓冲接收器写入监听
     *
     * @param contentLength 文件大小
     * @param bytes         写入大小
     */
    void onBufferedSinkWrite(long contentLength, long bytes);

}
