package com.xhb.prism.http;

import java.io.IOException;
import java.net.URL;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public abstract class URLInteceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        URL url = request.url().url();
        URL url2 = updateURL(url);
        if (url2 != url) {
            request = request.newBuilder().url(url2).build();
        }
        return chain.proceed(request);
    }

    protected abstract URL updateURL(URL url);

}
