package com.xhb.prism.http;

import android.net.Uri;

import java.io.IOException;
import java.net.URL;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public abstract class UriInteceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String url = request.url().toString();
        Uri.Builder builder = Uri.parse(url).buildUpon();
        if (updateUri(builder)) {
            request = request.newBuilder().url(
                    new URL(builder.build().toString())).build();
        }
        return chain.proceed(request);
    }

    protected abstract boolean updateUri(Uri.Builder builder);

}
