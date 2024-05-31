package androidx.api;

import android.content.Context;

import androidx.annotation.RawRes;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CacheControl;
import okhttp3.ConnectionPool;
import okhttp3.CookieJar;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

/**
 * 请求配置
 */
public class Configure {

    public static final String TAG = Configure.class.getSimpleName();
    /**
     * 配置对象
     */
    private static Configure config;
    /**
     * 调试模式
     */
    private boolean debug;
    /**
     * 上下文对象
     */
    private Context context;
    /**
     * 客户端单列模式
     */
    private boolean singleton = true;
    /**
     * 当前服务器地址
     */
    private String address;
    /**
     * URL - Map
     */
    private Map<Integer, String> urls;
    /**
     * 内容类型
     */
    private String contentType;
    /**
     * 协议
     */
    private List<Protocol> protocols;
    /**
     * 连接超时 - 单位秒
     */
    private long connectTimeout = 60;
    /**
     * 读取超时 - 单位秒
     */
    private long readTimeout = 60;
    /**
     * 写入超时 - 单位秒
     */
    private long writeTimeout = 60;
    /**
     * 连接池
     */
    private ConnectionPool connectionPool;
    /**
     * 最大空闲连接数
     */
    private int maxIdleConnections = 10;
    /**
     * 保活时间 - 单位秒
     */
    private int keepAliveDuration = 10;
    /**
     * Cookie管理
     */
    private CookieJar cookieJar;
    /**
     * 调度器
     */
    private Dispatcher dispatcher;
    /**
     * 拦截器
     */
    private List<Interceptor> interceptors;
    /**
     * 失败是否重试
     */
    private boolean retryOnConnectionFailure;
    /**
     * 请求证书
     */
    private Certificate requestCert;
    /**
     * SSL套接字工厂
     */
    private SSLSocketFactory socketFactory;
    /**
     * X509信任管理器
     */
    private X509TrustManager x509TrustManager;
    /**
     * 主机名验证器
     */
    private HostnameVerifier hostnameVerifier;
    /**
     * 转义工具
     */
    private EscapeJar escapeJar;
    /**
     * 缓存控制
     */
    private CacheControl cacheControl;
    /**
     * 客户端
     */
    private OkHttpClient client;
    /**
     * 拦截器缓存
     */
    private boolean interceptorCache = true;
    /**
     * 拦截器缓存个数
     */
    private int interceptorCacheSize = 50;

    /**
     * 构造参数
     *
     * @param context 上下文
     */
    private Configure(Context context) {
        this.context = context;
        initialConfiguration(context);
    }

    /**
     * 初始化配置
     *
     * @param context 上下文
     */
    protected void initialConfiguration(Context context) {
        ApiLog.i(TAG, "initialConfiguration");
        urls = new HashMap<>();
        singleton = false;
        protocols = Collections.singletonList(Protocol.HTTP_1_1);
        connectionPool = new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.SECONDS);
        dispatcher = new Dispatcher();
        interceptors = new ArrayList<>();
        cookieJar = new OkCookieJar(context);
        interceptors.add(new LogInterceptor());
        retryOnConnectionFailure = false;
        requestCert = new Certificate().build();
        socketFactory = requestCert.getSSLSocketFactory();
        x509TrustManager = new HttpsX509TrustManager();
        hostnameVerifier = new HttpsHostnameVerifier();
        escapeJar = new OkEscapeJar();
        cacheControl = CacheControl.FORCE_NETWORK;
        contentType = Api.JSON;
    }

    /**
     * 配置对象
     *
     * @return
     */
    public static Configure Config() {
        if (config == null) {
            ApiLog.e(TAG, "Request未初始化", "Request.initialize(context)方法进行初始化。");
        }
        return config;
    }


    /***
     * 初始化
     * @param context
     * @return
     */
    public static Configure initialize(Context context) {
        if (config == null) {
            synchronized (Configure.class) {
                config = new Configure(context);
            }
        }
        return config;
    }

    /**
     * 获取上下文
     *
     * @return
     */
    public Context getContext() {
        return context;
    }

    /**
     * 设置客户端是否单例模式
     *
     * @param isSingle 否单例模式
     */
    public void singleton(boolean isSingle) {
        this.singleton = isSingle;
    }

    /**
     * @return 客服端是否单例模式
     */
    public boolean isSingleton() {
        return singleton;
    }

    /**
     * 设置单例客户端
     *
     * @param client 客户端
     */
    public void setHttpClient(OkHttpClient client) {
        this.client = client;
    }

    /**
     * @return 单例客户端
     */
    public OkHttpClient getHttpClient() {
        return client;
    }

    /**
     * 是否是调试模式
     *
     * @return
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * 设置调试模式
     *
     * @param debug
     */
    public Configure debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * 服务器地址
     *
     * @return
     */
    public String address() {
        if (address == null || address.length() == 0) {
            ApiLog.e(TAG, "服务器地址URL未配置", "请调用RequestConfigure的url(Request.URL_BETA,xxx)和address(Request.URL_BETA)");
        }
        return address;
    }

    /**
     * 设置当前环境
     *
     * @param key
     */
    public Configure address(int key) {
        address = urls.get(key);
        return this;
    }

    /**
     * 添加请求环境
     *
     * @param key
     * @param url
     */
    public Configure url(int key, String url) {
        urls.put(key, url);
        return this;
    }

    /**
     * 获取内容类型
     *
     * @return
     */
    public String contentType() {
        return contentType;
    }

    /**
     * 设置内容类型
     *
     * @param contentType
     */
    public Configure contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 获取请求协议
     *
     * @return
     */
    public List<Protocol> protocols() {
        return protocols;
    }

    /**
     * 设置请求协议
     *
     * @param protocols
     */
    public Configure protocols(List<Protocol> protocols) {
        this.protocols = protocols;
        return this;
    }

    /**
     * 获取超时 - 单位秒
     *
     * @return
     */
    public long connectTimeout() {
        return connectTimeout;
    }

    /**
     * 设置获取超时 - 单位秒
     *
     * @param connectTimeout
     */
    public Configure connectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 获取读取超时 - 单位秒
     *
     * @return
     */
    public long readTimeout() {
        return readTimeout;
    }

    /**
     * 设置读取超时 - 单位秒
     *
     * @param readTimeout
     */
    public Configure readTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * 获取写入超市 - 单位秒
     *
     * @return
     */
    public long writeTimeout() {
        return writeTimeout;
    }

    /**
     * 获取写入超市 - 单位秒
     *
     * @param writeTimeout
     */
    public Configure writeTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    /**
     * 获取连接池
     *
     * @return
     */
    public ConnectionPool connectionPool() {
        return connectionPool;
    }

    /**
     * 设置连接池
     *
     * @param connectionPool
     */
    public Configure connectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        return this;
    }

    /**
     * 获取cookieJar
     *
     * @return
     */
    public CookieJar cookieJar() {
        return cookieJar;
    }

    /**
     * 设置CookieJar
     *
     * @param cookieJar
     */
    public Configure cookieJar(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
        return this;
    }

    /**
     * 获取调度
     *
     * @return
     */
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * 设置调度
     *
     * @param dispatcher
     */
    public Configure dispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        return this;
    }

    /**
     * 获取拦截器
     *
     * @return
     */
    public List<Interceptor> interceptors() {
        return interceptors;
    }

    /**
     * 设置拦截器
     *
     * @param interceptors
     */
    public Configure interceptors(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
        return this;
    }

    /**
     * 添加拦截器
     *
     * @param interceptor
     */
    public Configure addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
        return this;
    }

    /**
     * 是否失败尝试重连
     *
     * @return
     */
    public boolean isRetryOnConnectionFailure() {
        return retryOnConnectionFailure;
    }

    /**
     * 设置失败尝试重连
     *
     * @param retryOnConnectionFailure
     */
    public Configure retryOnConnectionFailure(boolean retryOnConnectionFailure) {
        this.retryOnConnectionFailure = retryOnConnectionFailure;
        return this;
    }

    /**
     * 获取SSL 套接字工厂
     *
     * @return
     */
    public SSLSocketFactory socketFactory() {
        return socketFactory;
    }

    /**
     * 设置SSL 套接字工厂
     *
     * @param socketFactory
     */
    public Configure socketFactory(SSLSocketFactory socketFactory) {
        this.socketFactory = socketFactory;
        return this;
    }

    /**
     * 获取X509信任管理器
     *
     * @return
     */
    public X509TrustManager x509TrustManager() {
        return x509TrustManager;
    }

    /**
     * 设置X509信任管理器
     *
     * @param x509TrustManager
     */
    public Configure x509TrustManager(X509TrustManager x509TrustManager) {
        this.x509TrustManager = x509TrustManager;
        return this;
    }

    /**
     * 获取主机名验证器
     *
     * @return
     */
    public HostnameVerifier hostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * 设置主机名验证器
     *
     * @param hostnameVerifier
     */
    public Configure hostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    /**
     * 获取转义工具
     *
     * @return
     */
    public EscapeJar escapeJar() {
        return escapeJar;
    }

    /**
     * 设置转义工具
     *
     * @param escapeJar
     */
    public Configure escapeJar(EscapeJar escapeJar) {
        this.escapeJar = escapeJar;
        return this;
    }

    /**
     * 获取最大空闲连接数
     *
     * @return
     */
    public int maxIdleConnections() {
        return maxIdleConnections;
    }

    /**
     * 设置最大空闲连接数
     *
     * @param maxIdleConnections
     */
    public Configure maxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
        connectionPool = new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.SECONDS);
        return this;
    }

    /**
     * 获取保活时间 - 单位秒
     *
     * @return
     */
    public int keepAliveDuration() {
        return keepAliveDuration;
    }

    /**
     * 设置保活时间 - 单位秒
     *
     * @param keepAliveDuration
     */
    public Configure keepAliveDuration(int keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
        connectionPool = new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.SECONDS);
        return this;
    }

    /**
     * 获取请求证书
     *
     * @return
     */
    public Certificate requestCert() {
        return requestCert;
    }

    /**
     * 设置请求证书
     *
     * @param requestCert
     */
    public Configure requestCert(Certificate requestCert) {
        socketFactory = requestCert.getSSLSocketFactory();
        x509TrustManager = new HttpsX509TrustManager();
        hostnameVerifier = new HttpsHostnameVerifier();
        this.requestCert = requestCert;
        return this;
    }

    /**
     * 添加证书
     *
     * @param alias 别名
     * @param resId raw资源ID
     */
    public Configure cert(String alias, @RawRes int resId) {
        requestCert.add(getContext(), alias, resId).build();
        return this;
    }

    /**
     * 添加证书
     *
     * @param alias    别名
     * @param resId    raw资源ID
     * @param password 密钥密码
     */
    public Configure cert(String alias, @RawRes int resId, String password) {
        requestCert.password(password).add(getContext(), alias, resId).build();
        return this;
    }

    /**
     * 添加证书
     *
     * @param alias    别名
     * @param filename assets文件全名
     */
    public Configure cert(String alias, String filename) {
        requestCert.add(getContext(), alias, filename).build();
        return this;
    }

    /**
     * 添加证书
     *
     * @param alias    别名
     * @param filename assets文件全名
     * @param password 密钥密码
     */
    public Configure cert(String alias, String filename, String password) {
        requestCert.password(password).add(getContext(), alias, filename).build();
        return this;
    }

    /**
     * 添加证书
     *
     * @param alias    别名
     * @param stream   文件流
     * @param password 密码
     */
    public Configure cert(String alias, InputStream stream, String password) {
        requestCert.password(password).add(alias, stream).build();
        return this;
    }

    /**
     * 添加证书
     *
     * @param alias    别名
     * @param filename assets文件全名
     * @param type     证书类型
     * @param provider 证书提供者
     * @param password 密钥密码
     */
    public Configure cert(String alias, String filename, String protocol, String type, String provider, String password) {
        requestCert.protocol(protocol).type(type).provider(provider).password(password).add(getContext(), alias, filename).build();
        return this;
    }

    /**
     * 添加证书
     *
     * @param alias    别名
     * @param stream   文件流
     * @param type     证书类型
     * @param provider 证书提供者
     * @param password 密钥密码
     */
    public Configure cert(String alias, InputStream stream, String protocol, String type, String provider, String password) {
        requestCert.protocol(protocol).type(type).provider(provider).password(password).add(alias, stream).build();
        return this;
    }

    /**
     * 获取缓存控制
     *
     * @return
     */
    public CacheControl cacheControl() {
        return cacheControl;
    }

    /**
     * 设置缓存控制
     *
     * @param cacheControl
     */
    public Configure cacheControl(CacheControl cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }

    /**
     * @return 是否拦截器缓存
     */
    public boolean isInterceptorCache() {
        return interceptorCache;
    }

    /**
     * 设置是否拦截器缓存
     *
     * @param interceptorCache
     * @return
     */
    public Configure interceptorCache(boolean interceptorCache) {
        this.interceptorCache = interceptorCache;
        return this;
    }

    /**
     * @return 拦截器缓存
     */
    public int interceptorCacheSize() {
        return interceptorCacheSize;
    }

    /**
     * 拦截器缓存最大值
     *
     * @param interceptorCacheSize
     * @return
     */
    public Configure interceptorCacheSize(int interceptorCacheSize) {
        this.interceptorCacheSize = interceptorCacheSize;
        return this;
    }


}
