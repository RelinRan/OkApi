package androidx.api;

import android.content.Context;

/**
 * 请求
 */
public interface Api {

    /**
     * 内网环境
     */
    int INTRANET = 0;
    /**
     * 测试环境
     */
    int TEST = 1;
    /**
     * 开发环境
     */
    int DEVELOPMENT = 2;
    /**
     * 正式环境
     */
    int RELEASE = 3;
    /**
     * 可变环境
     */
    int SETTINGS = 4;
    /**
     * 请求标识
     */
    String REQUEST_TAG = "Request-Tag";
    /**
     * 网络路径
     */
    String DOMAIN = "Cross-Domain";
    /**
     * json
     */
    String JSON = "application/json; charset=utf-8";
    /**
     * 表单
     */
    String FORM_DATA = "multipart/form-data; charset=utf-8";
    /**
     * 二进制
     */
    String BINARY = "application/octet-stream; charset=utf-8";
    /**
     * 文本
     */
    String RAW = "text/plain; charset=utf-8";
    /**
     * XML
     */
    String XML = "application/xml; charset=utf-8";

    /**
     * @param context Application的上下文
     * @return 初始化配置
     */
    static Configure initialize(Context context) {
        return Configure.initialize(context);
    }

    /**
     * Get请求
     *
     * @param context  上下文
     * @param path     路径
     * @param params   参数
     * @param listener 请求监听
     */
    void get(Context context, String path, RequestParams params, OnRequestListener listener);

    /**
     * Post请求
     *
     * @param context  上下文
     * @param path     路径
     * @param params   参数
     * @param listener 请求监听
     */
    void post(Context context, String path, RequestParams params, OnRequestListener listener);

    /**
     * Put请求
     *
     * @param context  上下文
     * @param path     路径
     * @param params   参数
     * @param listener 请求监听
     */
    void put(Context context, String path, RequestParams params, OnRequestListener listener);

    /**
     * Delete请求
     *
     * @param context  上下文
     * @param path     路径
     * @param params   参数
     * @param listener 请求监听
     */
    void delete(Context context, String path, RequestParams params, OnRequestListener listener);


    /**
     * Patch请求
     *
     * @param context  上下文
     * @param path     路径
     * @param params   参数
     * @param listener 请求监听
     */
    void patch(Context context, String path, RequestParams params, OnRequestListener listener);

    /**
     * 文件上传
     *
     * @param context         上下文
     * @param path            路径
     * @param params          参数
     * @param sinkListener    文件写入监听
     * @param requestListener 请求监听
     */
    void upload(Context context, String path, RequestParams params, OnBufferedSinkListener sinkListener, OnRequestListener requestListener);

    /**
     * 取消请求
     *
     * @param context 上下文
     */
    void cancel(Context context);

    /**
     * 取消请求
     *
     * @param tag 标识
     */
    void cancel(String tag);

    /**
     * 资源释放
     */
    void release();

}
