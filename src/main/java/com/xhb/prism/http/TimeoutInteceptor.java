package com.xhb.prism.http;

import com.xhb.prism.util.log.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Response;
import okio.AsyncTimeout;

public class TimeoutInteceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        AsyncTimeout timeout = (AsyncTimeout) chain.call().timeout();
        timeout.timeout(3, TimeUnit.SECONDS);
        timeout.exit();
        timeout.enter();
        try {
            return chain.proceed(chain.request());
        } finally {
            // TODO: retrofit will ignore canceled call, fix it
            Log.w("TimeoutInteceptor", "timeout");
        }
    }

}
