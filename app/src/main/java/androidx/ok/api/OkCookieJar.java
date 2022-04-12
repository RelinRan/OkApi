package androidx.ok.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;


import java.io.Serializable;
import java.util.ArrayList;
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

    public OkCookieJar(Context context) {
        this.context = context;
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
        int size = list == null ? 0 : list.size();
        for (int i = 0; i < size; i++) {
            Cookie cookie = list.get(i);
            OkCookie okCookie = new OkCookie();
            okCookie.setHost(httpUrl.host());
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
        String cookieJson = JSON.toJson(cookies);
        setCache(context, PREFIX + httpUrl.host(), cookieJson);
    }

    /**
     * 保存Cookie
     *
     * @param httpUrl http请求数据
     * @return 缓存的Cookie数据
     */
    private List<Cookie> load(HttpUrl httpUrl) {
        String requestHost = httpUrl.host();
        String cookieJson = getCache(context, PREFIX + requestHost, "[]");
        List<OkCookie> okCookies = JSON.toCollection(cookieJson, OkCookie.class);
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
     * @param hostKey 服务器key
     * @return
     */
    public static List<Cookie> getCookies(Context context, String hostKey) {
        String cookieJson = getCache(context, PREFIX + hostKey, "[]");
        List<OkCookie> okCookies = JSON.toCollection(cookieJson, OkCookie.class);
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
    public static void remove(Context context, String host) {
        setCache(context, PREFIX + host, "[]");
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
        String name = PACKAGE_NAME +"_"+ getVersionCode(context.getApplicationContext()) + PREFIX;
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
            e.printStackTrace();
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
