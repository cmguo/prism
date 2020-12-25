package com.xhb.prism.http;

import com.xhb.prism.http.annotation.Retry;

import java.io.IOException;
import java.net.HttpRetryException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Retry r = RetrifitAnnotation.getAnnotaion(chain.request(), Retry.class);
        if (r == null)
            return chain.proceed(chain.request());
        int t = r.times();
        while (true){
            try {
                return chain.proceed(chain.request());
            } catch (IOException e) {
                if (t == 0 || !recovable(e))
                    throw e;
                --t;
                try {
                    Thread.sleep(r.interval());
                } catch (InterruptedException e1) {
                    throw new IOException(e1);
                }
            }
        }
    }

    protected boolean recovable(Exception e) {
        return e == null
                || e instanceof HttpRetryException
                || e instanceof NoRouteToHostException
                || e instanceof PortUnreachableException
                || e instanceof SocketTimeoutException
                || e instanceof UnknownHostException
                ;
    }

}
