package androidx.api;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * HTTPS主机名验证程序
 */
public class HttpsHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}

