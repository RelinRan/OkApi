package androidx.api;


/**
 * 请求监听
 */
public interface OnRequestListener {

    /**
     * 请求成功
     *
     * @param request     请求包
     * @param response 响应数据
     */
    void onRequestSucceed(Request request, Response response);

    /**
     * 请求失败
     *
     * @param request      请求包 Pack
     * @param exception 异常
     */
    void onRequestFailed(Request request, Exception exception);

}
