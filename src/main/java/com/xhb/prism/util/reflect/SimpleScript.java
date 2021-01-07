package com.xhb.prism.util.reflect;

import com.xhb.prism.util.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleScript {

    private static final String TAG = "SimpleScript";
    
    private Pattern sPattern = Pattern.compile("(\\w+)(?:\\(([\\w\\s\\\",-]*)\\))?(?:\\.|$)");
    private Pattern sPattern2 = Pattern.compile("\\s*([\\w\\\"-]+)\\s*(?:,|$)");
    
    private Map<String, ObjectWrapper<?>> mScriptObjects = new TreeMap<String, ObjectWrapper<?>>();

    public <E> void addScriptObject(String name, E object) {
        mScriptObjects.put(name, ObjectWrapper.wrap(object));
    }
    
    public boolean invokeScript(String script) {
        Log.d(TAG, "invokeScript script: " + script);
        Matcher m = sPattern.matcher(script);
        ObjectWrapper<?> wrapper = null;
        while (m.find()) {
            String member = m.group(1);
            Log.d(TAG, "invokeScript member: " + member);
            String args1 = m.group(2);
            if (args1 == null) {
                if (wrapper == null)
                    wrapper = mScriptObjects.get(member);
                else
                    wrapper = ObjectWrapper.wrap(wrapper.get(member));
                if (wrapper == null)
                    return false;
            } else {
                Log.d(TAG, "invokeScript args: " + args1);
                Matcher m2 = sPattern2.matcher(args1);
                List<Object> args = new ArrayList<Object>();
                while (m2.find()) {
                    String arg = m2.group(1);
                    Log.d(TAG, "invokeScript arg: " + arg);
                    if ("true".equals(arg)) {
                        args.add(true);
                    } else if ("false".equals(arg)) {
                        args.add(false);
                    } else if (arg.startsWith("\"") && arg.endsWith("\"")) {
                        args.add(arg.substring(1, arg.length() - 1));
                    } else {
                        args.add(Integer.parseInt(arg));
                    }
                }
                Object result = wrapper.invoke(member, args.toArray());
                wrapper = ObjectWrapper.wrap(result);
            }
        }
        return true;
    }
    
}
