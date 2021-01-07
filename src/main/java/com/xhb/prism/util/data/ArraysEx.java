package com.xhb.prism.util.data;

import java.lang.reflect.Array;
import java.util.Collection;

public class ArraysEx {

    @SuppressWarnings("unchecked")
    public static <E, F extends E> E[] fromList(Collection<F> lst, Class<E> clazz) {
        return lst.toArray((E[]) Array.newInstance(clazz, lst.size()));
    }
}
