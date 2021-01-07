package com.xhb.prism.util.system;

import com.xhb.prism.util.reflect.ClassWrapper;
import com.xhb.prism.util.reflect.ObjectWrapper;

public class Os {

    private static ObjectWrapper<?> sBlockGuardOs =
            ObjectWrapper.wrap(ClassWrapper.wrap("libcore.io.Libcore").get("os"));
    
    public static int gettid() {
        return 0;//Process.myTid();
        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return (int) Thread.currentThread().getId();
        else
            return sOs.invoke("gettid");
            */
    }
    
    public static int getpid() {
        return 0;// Process.myPid();
    }

}
