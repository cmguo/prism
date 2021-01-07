package com.xhb.prism.prop;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PropJsonArchive extends PropArchive {

    public PropJsonArchive() {
    }
    
    @Override
    @SuppressWarnings({ "unchecked" })
    public void load(InputStream in, 
            Map<String, Map<String, String>> props) throws IOException {
        Gson json = new Gson();
        InputStreamReader reader = new InputStreamReader(in, "UTF-8");
        Map<String, Map<String, String>> prop = json.fromJson(reader, props.getClass());
        super.merge(props, prop, true);
        reader.close();
    }

    @Override
    public void save(OutputStream out, 
            Map<String, Map<String, String>> props) throws IOException {
        PrintStream printStream = new PrintStream(out, false, "UTF-8");
        Gson json = new GsonBuilder().setPrettyPrinting().create();
        printStream.write(
                json.toJson(props).getBytes("UTF-8"));
        printStream.flush();
        printStream.close();
    }

}
