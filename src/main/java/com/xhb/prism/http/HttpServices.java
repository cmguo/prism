package com.xhb.prism.http;

import com.xhb.prism.http.annotation.BaseUri;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

public class HttpServices {

    // share thread pool
    private static final OkHttpClient sHttp = defaultClient();

    private static Map<Class<?>, Object> mServices = new HashMap<>();

    private static OkHttpClient defaultClient() {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        try {
            Class.forName("com/google/j2objc/net/ssl/IosSslContextSpi");
            client.sslSocketFactory(SSLContext.getDefault().getSocketFactory(), new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            });
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
        }
        return client.build();
    }

    public static <I> I get(Class<I> clazz, List<Interceptor> interceptors, ResultConverterFactory.ResultInfo<?> resultInfo) {
        I service = (I) mServices.get(clazz);
        if (service != null)
            return service;
        BaseUri baseUri = clazz.getAnnotation(BaseUri.class);
        OkHttpClient.Builder client = sHttp.newBuilder();
        if (interceptors != null) {
            for (Interceptor i : interceptors)
                client.addInterceptor(i);
        }
        client.addInterceptor(new RetryInterceptor());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUri.value())
                .client(client.build())
                .addConverterFactory(ResultConverterFactory.create(resultInfo))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
        service = retrofit.create(clazz);
        mServices.put(clazz, service);
        return service;
    }

}
