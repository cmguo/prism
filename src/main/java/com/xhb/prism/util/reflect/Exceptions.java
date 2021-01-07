package com.xhb.prism.util.reflect;

public class Exceptions {

    @SuppressWarnings("unchecked")
    public static <E extends Exception> 
    void rethrow(Throwable e, Class<E> cls) throws E {
        if (cls.isInstance(e))
            throw (E) e;
        if (e instanceof Error)
            throw (Error) e;
        if (e instanceof RuntimeException)
            throw (RuntimeException) e;
        ClassWrapper<E> clazz = new ClassWrapper<E>(cls);
        if (clazz.hasConstructor(Throwable.class))
            throw clazz.newInstance(ObjectWrapper.wrap(Throwable.class, e));
        else if (clazz.hasConstructor(String.class))
            throw clazz.newInstance(e.getMessage());
        else
            throw clazz.newInstance();
    }

    @SuppressWarnings("unchecked")
    public static <E1 extends Exception, E2  extends Exception>
    void rethrow(Throwable e, Class<E1> cls1, Class<E2> cls2) throws E1, E2 {
        if (cls2.isInstance(e))
            throw (E2) e;
        rethrow(e, cls1);
    }

    @SuppressWarnings("unchecked")
    public static <E1 extends Exception, E2  extends Exception, E3  extends Exception>
    void rethrow(Throwable e, Class<E1> cls1, Class<E2> cls2, Class<E3> cls3) throws E1, E2, E3 {
        if (cls3.isInstance(e))
            throw (E3) e;
        rethrow(e, cls1, cls2);
    }

    @SuppressWarnings("unchecked")
    public static <E1 extends Exception, E2  extends Exception, E3  extends Exception, E4  extends Exception>
    void rethrow(Throwable e, Class<E1> cls1, Class<E2> cls2, Class<E3> cls3, Class<E4> cls4) throws E1, E2, E3, E4 {
        if (cls4.isInstance(e))
            throw (E4) e;
        rethrow(e, cls1, cls2, cls3);
    }

}
