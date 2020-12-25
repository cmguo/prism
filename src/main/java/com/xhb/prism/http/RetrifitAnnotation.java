package com.xhb.prism.http;

import java.lang.annotation.Annotation;

import okhttp3.Request;
import retrofit2.Invocation;

public class RetrifitAnnotation {

    public static <T extends Annotation> T getAnnotaion(Request request, Class<T> key) {
        T t = request.tag(key);
        if (t != null)
            return t;
        Invocation invocation = request.tag(Invocation.class);
        if (invocation == null)
            return null;
        t = invocation.method().getAnnotation(key);
        if (t != null)
            return t;
        return invocation.method().getDeclaringClass().getAnnotation(key);
    }
}
