package androidx.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by Relin
 * on 2018-09-25.
 */
public class OkCookieJar implements Serializable, CookieJar {

    public static final String PREFIX = "OK_";
    private Context context;
    private SimpleDateFormat dateFormat;
    private JSON json;

    public OkCookieJar(Context context) {
        this.context = context;
        json = new JSON();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    private List<OkCookie> cookies = new ArrayList<>();

    @Override
    public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
        save(httpUrl, list);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl httpUrl) {
        return load(httpUrl);
    }

    /**
     * 加载Cookie
     *
     * @param httpUrl http请求数据
     * @param list    cookie数据
     */
    private void save(HttpUrl httpUrl, List<Cookie> list) {
        if (hasCookie(httpUrl)) {
            return;
        }
        cookies = new ArrayList<>();
        int size = list == null ? 0 : list.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                Cookie cookie = list.get(i);
                OkCookie okCookie = new OkCookie();
                okCookie.setHost(httpUrl.host());
                okCookie.setPort(httpUrl.port());
                okCookie.setName(cookie.name());
                okCookie.setValue(cookie.value());
                okCookie.setExpiresAt(cookie.expiresAt());
                okCookie.setDomain(cookie.domain());
                okCookie.setPath(cookie.path());
                okCookie.setSecure(cookie.secure());
                okCookie.setHttpOnly(cookie.httpOnly());
                okCookie.setHostOnly(cookie.hostOnly());
                okCookie.setPersistent(cookie.persistent());
                cookies.add(okCookie);
            }
            String cookieJson = json.toJson(cookies);
            Log.i(OkCookieJar.class.getSimpleName(), cookieJson);
            setCache(context, getCacheKey(httpUrl.host(), httpUrl.port()), cookieJson);
        }
    }

    /**
     * @param host 主机名
     * @param port 端口
     * @return 缓存key
     */
    private String getCacheKey(String host, int port) {
        return PREFIX + host + ":" + port;
    }

    /**
     * @param httpUrl
     * @return 是否存在Cookie
     */
    private boolean hasCookie(HttpUrl httpUrl) {
        String requestHost = httpUrl.host();
        int requestPort = httpUrl.port();
        String cookieJson = getCache(context, getCacheKey(requestHost, requestPort), "[]");
        List<OkCookie> okCookies = json.toList(cookieJson, OkCookie.class);
        int okCookieSize = okCookies == null ? 0 : okCookies.size();
        for (int i = 0; i < okCookieSize; i++) {
            OkCookie okCookie = okCookies.get(i);
            String host = okCookie.getHost();
            int port = okCookie.getPort();
            long expiresAt = okCookie.getExpiresAt();
            long expireTime = expiresAt - System.currentTimeMillis();
            if (expireTime > 0 && requestHost.equals(host) && requestPort == port) {
                if (Configure.Config().isDebug()) {
                    String expireDate = dateFormat.format(new Date(expiresAt));
                    Log.i(OkCookieJar.class.getSimpleName(), "host = " + host + ",port = " + port + ",expireDate = " + expireDate);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 保存Cookie
     *
     * @param httpUrl http请求数据
     * @return 缓存的Cookie数据
     */
    private List<Cookie> load(HttpUrl httpUrl) {
        String requestHost = httpUrl.host();
        int requestPort = httpUrl.port();
        String cookieJson = getCache(context, getCacheKey(requestHost, requestPort), "[]");
        List<OkCookie> okCookies = json.toList(cookieJson, OkCookie.class);
        List<Cookie> cookies = new ArrayList<>();
        int okCookieSize = okCookies == null ? 0 : okCookies.size();
        for (int i = 0; i < okCookieSize; i++) {
            OkCookie okCookie = okCookies.get(i);
            String host = okCookie.getHost();
            String name = okCookie.getName();
            String value = okCookie.getValue();
            String domain = okCookie.getDomain();
            String path = okCookie.getPath();
            long expiresAt = okCookie.getExpiresAt();
            if (expiresAt > System.currentTimeMillis() && requestHost.equals(host)) {
                Cookie cookie = new Cookie.Builder()
                        .name(name)
                        .value(value)
                        .expiresAt(expiresAt)
                        .domain(domain)
                        .path(path)
                        .secure()
                        .httpOnly()
                        .build();
                cookies.add(cookie);
            }
        }
        int cookieSize = cookies == null ? 0 : cookies.size();
        if (cookieSize == 0) {
            save(httpUrl, cookies);
        }
        return cookies;
    }

    /**
     * 获取Cookie数据
     *
     * @param context 上下文
     * @param host    主机
     * @param port    端口
     * @return
     */
    public static List<Cookie> getCookies(Context context, String host, int port) {
        String cookieJson = getCache(context, PREFIX + host + ":" + port, "[]");
        List<OkCookie> okCookies = new JSON().toList(cookieJson, OkCookie.class);
        List<Cookie> cookies = new ArrayList<>();
        int okCookiesSize = okCookies == null ? 0 : okCookies.size();
        for (int i = 0; i < okCookiesSize; i++) {
            OkCookie okCookie = okCookies.get(i);
            String name = okCookie.getName();
            String value = okCookie.getValue();
            long expiresAt = okCookie.getExpiresAt();
            String domain = okCookie.getDomain();
            String path = okCookie.getPath();
            if (expiresAt > System.currentTimeMillis()) {
                Cookie cookie = new Cookie.Builder()
                        .name(name)
                        .value(value)
                        .expiresAt(expiresAt)
                        .domain(domain)
                        .path(path)
                        .secure()
                        .httpOnly()
                        .build();
                cookies.add(cookie);
            }
        }
        return cookies;
    }

    /**
     * 删除Cookie缓存
     *
     * @param context 上下文
     * @param host    服务器
     */
    public static void remove(Context context, String host, int port) {
        setCache(context, PREFIX + host + ":" + port, "[]");
    }

    /**
     * 清空Cookie缓存
     *
     * @param context 上下文
     */
    public static void clear(Context context) {
        getSharedPreferences(context).edit().clear().commit();
    }

    /**
     * 获取缓存对象
     *
     * @param context 上下文
     * @return
     */
    protected static SharedPreferences getSharedPreferences(Context context) {
        if (context == null) {
            return null;
        }
        String PACKAGE_NAME = context.getApplicationContext().getPackageName().replace(".", "_").toUpperCase();
        String name = PREFIX + PACKAGE_NAME + "_" + getVersionCode(context.getApplicationContext());
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    /**
     * @param context 上下文
     * @return 版本号
     */
    public static int getVersionCode(Context context) {
        int versionCode = 1;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = pi.versionCode;
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
        return versionCode;
    }

    /**
     * 获取缓存
     *
     * @param context  上下文
     * @param key      键
     * @param defValue 默认值
     * @return
     */
    protected static String getCache(Context context, String key, String defValue) {
        return getSharedPreferences(context).getString(key, defValue);
    }

    /**
     * 设置缓存
     *
     * @param context 上下文
     * @param key     键
     * @param value   值
     */
    protected static void setCache(Context context, String key, String value) {
        getSharedPreferences(context).edit().putString(key, value).apply();
    }

}
