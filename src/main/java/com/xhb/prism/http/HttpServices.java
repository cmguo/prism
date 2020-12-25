package com.xhb.prism.http;

import android.content.Context;

import com.xhb.prism.http.RetryInterceptor;
import com.xhb.prism.http.annotation.BaseUri;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

public class HttpServices {

    // share thread pool
    private static final OkHttpClient sHttp = new OkHttpClient();

    private static Map<Class<?>, Object> mServices = new HashMap<>();

    public static <I> I get(Context context, Class<I> clazz, List<Interceptor> interceptors, ResultConverterFactory.ResultInfo<?> resultInfo) {
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
