package net.doridian.amplifi;

import com.google.gson.JsonObject;
import org.bouncycastle.util.encoders.Base64;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.math.BigInteger;
import java.security.cert.X509Certificate;

public class Utils {
    public static SSLSocketFactory getAllTrustFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]{tm}, null);

            return sslContext.getSocketFactory();
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static BigInteger decodeBase64BigInt(JsonObject payload, String key) {
        return new BigInteger(1, Base64.decode(payload.getAsJsonPrimitive(key).getAsString()));
    }
}
