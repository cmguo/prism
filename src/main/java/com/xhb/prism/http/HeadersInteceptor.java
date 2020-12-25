package com.xhb.prism.http;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public abstract class HeadersInteceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Headers.Builder builder = request.headers().newBuilder();
        if (updateHeaders(request, builder)) {
            request = request.newBuilder().headers(builder.build()).build();
        }
        return chain.proceed(request);
    }

    protected boolean updateHeaders(Request request, Headers.Builder builder) {
        return updateHeaders(builder);
    }

    protected boolean updateHeaders(Headers.Builder builder) {
        return false;
    }

}
