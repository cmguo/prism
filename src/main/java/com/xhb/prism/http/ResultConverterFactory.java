package com.xhb.prism.http;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class ResultConverterFactory extends Converter.Factory {

    public abstract static class ResultInfo<R>
    {
        public String mStatusField;
        public String mMessageField;
        public String mDataField;

        public abstract R newResult();
        public abstract void setStatus(R result, int status);
        public abstract void setMessage(R result, String message);
        public abstract void setData(R result, Object data);

        public abstract void check(R result);
        public abstract Object data(R result);
    }

    private ResultInfo<?> mResultInfo;

    public static ResultConverterFactory create(ResultInfo<?> resultInfo) {
        return new ResultConverterFactory(resultInfo);
    }

    private final Gson gson = new Gson();

    private ResultConverterFactory(ResultInfo<?> resultInfo) {
        mResultInfo = resultInfo;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new ResultResponseBodyConverter<>(gson, mResultInfo, adapter);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations,
                                                          Annotation[] methodAnnotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new ResultRequestBodyConverter<>(gson, adapter);
    }

    static final class ResultResultTypeAdapter<Result> extends TypeAdapter<Result> {

        private final ResultInfo<Result> mResultInfo;
        private final TypeAdapter mDataAdapter;

        ResultResultTypeAdapter(ResultInfo<Result> resultType, TypeAdapter dataAdapter) {
            mResultInfo = resultType;
            mDataAdapter = dataAdapter;
        }

        @Override
        public void write(JsonWriter out, Result value) throws IOException {
        }

        @Override
        public Result read(JsonReader in) throws IOException {
            Result result = null;
            try {
                result = (Result) mResultInfo.newResult();
            } catch (Exception e) {
                throw new IOException(e);
            }
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (mResultInfo.mStatusField.equals(name)) {
                    mResultInfo.setStatus(result, in.nextInt());
                } else if (mResultInfo.mMessageField.equals(name)) {
                    JsonToken token = in.peek();
                    if (token.equals(JsonToken.STRING))
                        mResultInfo.setMessage(result, in.nextString());
                    else
                        in.skipValue();
                } else if (mResultInfo.mDataField.equals(name)) {
                    mResultInfo.setData(result, mDataAdapter.read(in));
                } else {
                    in.skipValue();
                }
            }
            in.endObject();
            return result;
        }
    }

    static final class ResultResponseBodyConverter<Result, T> implements Converter<ResponseBody, T> {
        private final Gson gson;
        private final ResultInfo<Result> resultInfo;
        private final TypeAdapter<T> adapter;

        ResultResponseBodyConverter(Gson gson, ResultInfo<Result> resultInfo,  TypeAdapter<T> adapter) {
            this.gson = gson;
            this.resultInfo = resultInfo;
            this.adapter = adapter;
        }

        @Override public T convert(ResponseBody value) throws IOException {
            JsonReader jsonReader = gson.newJsonReader(value.charStream());
            try {
                ResultResultTypeAdapter<Result> a = new ResultResultTypeAdapter<Result>(resultInfo, adapter);
                Result result = a.read(jsonReader);
                resultInfo.check(result);
                return (T) resultInfo.data(result);
            } finally {
                value.close();
            }
        }

    }

/*
    static final class ResultPostTypeAdapter extends TypeAdapter<ResultPost> {

        private final TypeAdapter mDataAdapter;

        ResultPostTypeAdapter(TypeAdapter dataAdapter) {
            mDataAdapter = dataAdapter;
        }

        @Override
        public void write(JsonWriter out, ResultPost value) throws IOException {
        }

        @Override
        public ResultPost read(JsonReader in) throws IOException {
            return null;
        }
    }
*/
    final static class ResultRequestBodyConverter<T> implements Converter<T, RequestBody> {
        private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
        private static final Charset UTF_8 = Charset.forName("UTF-8");

        private final Gson gson;
        private final TypeAdapter<T> adapter;

        ResultRequestBodyConverter(Gson gson, TypeAdapter<T> adapter) {
            this.gson = gson;
            this.adapter = adapter;
        }

        @Override public RequestBody convert(T value) throws IOException {
            Buffer buffer = new Buffer();
            Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
            JsonWriter jsonWriter = gson.newJsonWriter(writer);
            adapter.write(jsonWriter, value);
            jsonWriter.close();
            return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
        }
    }
}
