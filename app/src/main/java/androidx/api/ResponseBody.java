package androidx.api;


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
     * 上传监听
     */
    private OnRequestListener onRequestListener;
    /**
     * 上传写入监听
     */
    private OnBufferedSinkListener onBufferedSinkListener;

    /**
     * @return 请求
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
     * @return 响应内容
     */
    public Response getResponse() {
        return response;
    }

    /**
     * 设置响应
     *
     * @param response
     */
    public void setResponse(Response response) {
        this.response = response;
    }

    /**
     * @return 异常
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
     * @return 请求监听
     */
    public OnRequestListener getRequestListener() {
        return onRequestListener;
    }

    /**
     * 设置监听
     *
     * @param onRequestListener
     */
    public void setOnRequestListener(OnRequestListener onRequestListener) {
        this.onRequestListener = onRequestListener;
    }

    /**
     * @return 文件上传写入监听
     */
    public OnBufferedSinkListener getBufferedSinkListener() {
        return onBufferedSinkListener;
    }

    /**
     * 设置写入监听
     *
     * @param onBufferedSinkListener 文件上传写入监听
     */
    public void setBufferedSinkListener(OnBufferedSinkListener onBufferedSinkListener) {
        this.onBufferedSinkListener = onBufferedSinkListener;
    }

    /**
     * 资源释放
     */
    public void release() {
        if (request != null) {
            request.cancel();
        }
        request = null;
        if (response != null) {
            response.close();
        }
        response = null;
        exception = null;
        onRequestListener = null;
        onBufferedSinkListener = null;
    }

}
