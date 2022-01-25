package com.androidx.okapi;


/**
 * 响应参数
 */
public class ResponseBody {

    /**
     * 请求参数
     */
    private Request request;
    /**
     * 响应
     */
    private Response response;
    /**
     * 异常
     */
    private Exception exception;
    /**
     * 结果监听
     */
    private OnRequestListener listener;

    /**
     * 获取请求
     *
     * @return
     */
    public Request getRequest() {
        return request;
    }

    /**
     * 设置请求
     *
     * @param request
     */
    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * 获取响应内容
     * @return
     */
    public Response getResponse() {
        return response;
    }

    /**
     * 设置响应
     * @param response
     */
    public void setResponse(Response response) {
        this.response = response;
    }

    /**
     * 获取异常
     *
     * @return
     */
    public Exception getException() {
        return exception;
    }

    /**
     * 设置异常
     *
     * @param exception
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * 获取监听
     *
     * @return
     */
    public OnRequestListener getListener() {
        return listener;
    }

    /**
     * 设置监听
     *
     * @param listener
     */
    public void setListener(OnRequestListener listener) {
        this.listener = listener;
    }

}
