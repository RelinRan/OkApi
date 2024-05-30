package androidx.api;

import android.content.Context;

import androidx.annotation.RawRes;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Https证书
 */
public class Certificate {

    /**
     * 传输层安全协议
     */
    public static final String TSL = "TSL";
    /**
     * 安全套接字层
     */
    public static final String SSL = "SSL";
    /**
     * 协议
     */
    private String protocol = "SSL";
    /**
     * 证书类型
     */
    private String type = "X.509";
    /**
     * 证书提供者
     */
    private String provider;
    /**
     * KeyStore密码
     */
    private String password = "";
    /**
     * 套接字上下文
     */
    private SSLContext sslContext;
    /**
     * 信任管理器
     */
    private TrustManager[] trustManagers;
    /**
     * HTTPS 信任管理器
     */
    private HttpsTrustManager httpsTrustManager;

    /**
     * 构造请求证书 - 信任所有
     */
    public Certificate() {
        httpsTrustManager = new HttpsTrustManager();
        build();
    }

    /**
     * 设置协议
     *
     * @param protocol 协议
     * @return
     */
    public Certificate protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * 获取
     *
     * @return
     */
    public String protocol() {
        return protocol;
    }

    /**
     * 设置证书类型
     *
     * @param type 证书类型
     * @return
     */
    public Certificate type(String type) {
        this.type = type;
        return this;
    }

    /**
     * 获取证书类型
     *
     * @return
     */
    public String type() {
        return type;
    }

    /**
     * 设置证书提供者
     *
     * @param provider 证书提供者
     */
    public Certificate provider(String provider) {
        this.provider = provider;
        return this;
    }

    /**
     * 获取证书提供者
     *
     * @return
     */
    public String provider() {
        return provider;
    }

    /**
     * 设置密钥库密码
     *
     * @param password 密钥库密码
     * @return
     */
    public Certificate password(String password) {
        this.password = password;
        return this;
    }

    /**
     * 获取密钥库密码
     *
     * @return
     */
    public String password() {
        return password;
    }

    /**
     * 获取Raw文件夹下文件流
     *
     * @param context 上下文
     * @param id      资源id
     * @return
     */
    public InputStream openRawResource(Context context, @RawRes int id) {
        return context.getResources().openRawResource(id);
    }

    /**
     * 获取Assets资源文件流
     *
     * @param context  上下文
     * @param filename 文件名，例如：cert.crt
     * @return
     */
    public InputStream openAssetsResource(Context context, String filename) {
        try {
            return context.getAssets().open(filename);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

    /**
     * 添加证书
     *
     * @param alias    别名
     * @param filename assets文件全称
     */
    public Certificate add(Context context, String alias, String filename) {
        InputStream stream = openAssetsResource(context, filename);
        httpsTrustManager.addCert(alias, stream);
        return this;
    }

    /**
     * 添加证书
     *
     * @param context 上下文
     * @param alias   别名
     * @param resId   raw资源ID
     * @return
     */
    public Certificate add(Context context, String alias, @RawRes int resId) {
        InputStream stream = openRawResource(context, resId);
        httpsTrustManager.addCert(alias, stream);
        return this;
    }


    /**
     * 添加证书
     *
     * @param alias  别名
     * @param stream 文件流
     */
    public Certificate add(String alias, InputStream stream) {
        httpsTrustManager.addCert(alias, stream);
        return this;
    }

    /**
     * 信任证书
     */
    protected Certificate build() {
        try {
            sslContext = SSLContext.getInstance(protocol);
            httpsTrustManager.type(type).provider(provider).password(password).build();
            trustManagers = httpsTrustManager.trustManager();
            sslContext.init(null, trustManagers, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HttpsHostnameVerifier());
        } catch (NoSuchAlgorithmException e) {
           throw new RuntimeException(e);
        } catch (KeyManagementException e) {
           throw new RuntimeException(e);
        }
        return this;
    }


    /**
     * 套接字上下文
     *
     * @return
     */
    public SSLContext getSSLContext() {
        return sslContext;
    }

    /**
     * 套接字工厂
     *
     * @return
     */
    public SSLSocketFactory getSSLSocketFactory() {
        return sslContext.getSocketFactory();
    }

    /**
     * 信任管理器
     *
     * @return
     */
    public TrustManager[] getTrustManagers() {
        return trustManagers;
    }

    /**
     * 获取HTTPS信任管理器
     *
     * @return
     */
    public HttpsTrustManager getHttpsTrustManager() {
        return httpsTrustManager;
    }
}
