package com.androidx.okapi;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * 证书信任管理
 */
public class HttpsTrustManager {

    /**
     * 证书流
     */
    private Map<String, InputStream> cert;
    /**
     * 信任管理器工厂
     */
    private TrustManagerFactory trustManagerFactory;
    /**
     * 密钥管理器工厂
     */
    private KeyManagerFactory keyManagerFactory;
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


    public HttpsTrustManager() {
        cert = new HashMap<>();
    }

    /**
     * 设置证书
     *
     * @param alias  证书别名
     * @param stream 证书文件流
     * @return
     */
    public HttpsTrustManager addCert(String alias, InputStream stream) {
        cert.put(alias, stream);
        return this;
    }

    /**
     * 设置证书类型
     *
     * @param type 证书类型
     * @return
     */
    public HttpsTrustManager type(String type) {
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
    public HttpsTrustManager provider(String provider) {
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
    public HttpsTrustManager password(String password) {
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
     * 初始化信任管理器
     */
    public HttpsTrustManager build() {
        if (cert != null && cert.size() != 0) {
            try {
                CertificateFactory certificateFactory;
                if (provider == null || provider.length() == 0) {
                    certificateFactory = CertificateFactory.getInstance(type);
                } else {
                    certificateFactory = CertificateFactory.getInstance(type, provider);
                }
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                for (String alias : cert.keySet()) {
                    InputStream stream = cert.get(alias);
                    keyStore.setCertificateEntry(alias, certificateFactory.generateCertificate(stream));
                    stream.close();
                }
                trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                //客户端证书密码
                if (password != null) {
                    keyManagerFactory.init(keyStore, password.toCharArray());
                }
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    /**
     * 密钥管理器工厂
     *
     * @return
     */
    public KeyManager[] keyManager() {
        return keyManagerFactory().getKeyManagers();
    }

    /**
     * 获取密钥管理器工厂
     *
     * @return
     */
    public KeyManagerFactory keyManagerFactory() {
        return keyManagerFactory;
    }

    /**
     * 获取信任管理器
     *
     * @return
     */
    public TrustManager[] trustManager() {
        if (trustManagerFactory == null) {
            //信任所有证书
            return new TrustManager[]{new HttpsX509TrustManager()};
        }
        return trustManagerFactory.getTrustManagers();
    }

    /**
     * 获取证书Map
     *
     * @return
     */
    public Map<String, InputStream> cert() {
        return cert;
    }

}
