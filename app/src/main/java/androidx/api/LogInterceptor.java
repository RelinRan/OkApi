package androidx.api;


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 日志拦截器
 */
public class LogInterceptor implements Interceptor {

    public final String TAG = "OkApi";
    private StringBuffer sb;
    private static List<InterceptorCache> interceptorCaches;

    /**
     * @return 缓存日志
     */
    public static List<InterceptorCache> interceptorCaches() {
        return interceptorCaches;
    }

    /**
     * 创建缓存日志
     */
    public static void create() {
        interceptorCaches = new ArrayList<>();
    }

    /**
     * 销毁缓存日志
     */
    public static void destroy() {
        if (interceptorCaches != null) {
            interceptorCaches.clear();
        }
        interceptorCaches = null;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        okhttp3.Request request = buildRequest(chain);
        HttpUrl httpUrl = request.url();
        String url = httpUrl.toString();
        String method = request.method();
        okhttp3.Response response = chain.proceed(request);
        if (Configure.Config().isInterceptorCache() || Configure.Config().isDebug()) {
            InterceptorCache logCache = new InterceptorCache();
            sb = new StringBuffer();
            sb.append(ApiLog.NEW_LINE);
            sb.append(ApiLog.HEAD_LINE).append(ApiLog.NEW_LINE);
            sb.append(ApiLog.LEFT_LINE + url).append(ApiLog.NEW_LINE);
            logCache.setUrl(url);
            sb.append(ApiLog.MIDDLE_LINE).append(ApiLog.NEW_LINE);
            sb.append(ApiLog.LEFT_LINE + "Method: " + method).append(ApiLog.NEW_LINE);
            logCache.setMethod(method);
            StringBuffer headerBuffer = new StringBuffer();
            for (String name : request.headers().names()) {
                String value = request.header(name);
                sb.append(ApiLog.LEFT_LINE + name + ": " + value).append(ApiLog.NEW_LINE);
                headerBuffer.append(name + ": " + value).append(ApiLog.NEW_LINE);
            }
            logCache.setHeaders(headerBuffer.toString());
            sb.append(ApiLog.MIDDLE_LINE).append(ApiLog.NEW_LINE);
            String requestBody = getRequestBody(request.body());
            sb.append(ApiLog.LEFT_LINE + requestBody).append(ApiLog.NEW_LINE);
            logCache.setParams(requestBody);
            int code = response.code();
            sb.append(ApiLog.MIDDLE_LINE).append(ApiLog.NEW_LINE);
            sb.append(ApiLog.LEFT_LINE + "code: " + code).append(ApiLog.NEW_LINE);
            logCache.setCode(code);
            String body = decodeUnicode(getResponseBody(response.body()));
            sb.append(ApiLog.LEFT_LINE + "body: " + body).append(ApiLog.NEW_LINE);
            logCache.setBody(body);
            sb.append(ApiLog.BOTTOM_LINE).append(ApiLog.NEW_LINE);
            if (Configure.Config().isDebug()) {
                ApiLog.i(TAG, sb.toString());
            }
            if (interceptorCaches != null) {
                int size = interceptorCaches.size();
                if (size + 1 > Configure.Config().interceptorCacheSize()) {
                    create();
                }
                interceptorCaches.add(logCache);
            }
        }
        return response;
    }

    /**
     * 构建新的服务器地址
     *
     * @param chain
     * @return
     */
    public okhttp3.Request buildRequest(Chain chain) {
        okhttp3.Request request = chain.request();
        HttpUrl httpUrl = request.url();
        String domain = request.header(Api.DOMAIN);
        if (domain != null && domain.length() > 0) {
            //替换服务器
            HttpUrl domainUrl = HttpUrl.parse(domain);
            HttpUrl newUrl = httpUrl.newBuilder().scheme(domainUrl.scheme()).host(domainUrl.host()).port(domainUrl.port()).build();
            //构建新的请求方式
            okhttp3.Request.Builder builder = request.newBuilder();
            builder.url(newUrl);
            request = builder.build();
        }
        return request;
    }

    /**
     * 获取请求参数
     *
     * @param requestBody
     * @return
     */
    private String getRequestBody(RequestBody requestBody) {
        if (requestBody == null) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        if (requestBody instanceof MultipartBody) {
            MultipartBody multipartBody = (MultipartBody) requestBody;
            for (MultipartBody.Part part : multipartBody.parts()) {
                String key = part.headers().get("Content-Disposition");
                key = key.replace("form-data; name=", "");
                RequestBody body = part.body();
                if (body != null) {
                    if (!key.equals("\"\"")) {
                        if (key.contains("filename")) {
                            key.replace("\"", "");
                            String keySplit[] = key.split(";");
                            buffer.append(keySplit[0]);
                            buffer.append(":");
                            buffer.append(keySplit[1].split("=")[1]);
                            buffer.append(",");
                        } else {
                            buffer.append(key);
                            buffer.append(":");
                            buffer.append("\"");
                            buffer.append(getRequestBodyString(body));
                            buffer.append("\"");
                            buffer.append(",");
                        }
                    }
                }
            }
            if (buffer.toString().contains(",")) {
                buffer.deleteCharAt(buffer.lastIndexOf(","));
            }
        } else {
            buffer.append(getRequestBodyString(requestBody));
        }
        return buffer.toString();
    }

    /**
     * 请求参数字符串
     *
     * @param requestBody 请求参数
     * @return
     */
    private String getRequestBodyString(RequestBody requestBody) {
        Buffer buffer = new Buffer();
        try {
            requestBody.writeTo(buffer);
        } catch (IOException e) {
            return "";
        }
        return buffer.readUtf8();
    }

    /**
     * 解析Unicode
     *
     * @param unicode
     * @return
     */
    private String decodeUnicode(String unicode) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(unicode);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            unicode = unicode.replace(matcher.group(1), String.valueOf(ch));
        }
        return unicode;
    }

    /**
     * 获取响应内容
     *
     * @param responseBody
     * @return
     */
    private String getResponseBody(okhttp3.ResponseBody responseBody) {
        BufferedSource source = responseBody.source();
        try {
            source.request(Long.MAX_VALUE);
        } catch (IOException e) {
           return "";
        }
        Buffer buffer = source.buffer();
        Charset charset = Charset.forName("UTF-8");
        if (responseBody.contentLength() != 0) {
            return buffer.clone().readString(charset);
        }
        return "";
    }

}
