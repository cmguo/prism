package com.xhb.prism.util.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

public class WriterOutputStream extends OutputStream {
    
    private final Writer mWriter;
    private CharsetDecoder mDecoder;
    private CharBuffer mBuffer = CharBuffer.allocate(1024);

    public WriterOutputStream(Writer writer, String charsetName) {
        mWriter = writer;
        mDecoder = Charset.forName(charsetName).newDecoder();
    }

    @Override
    public void write(byte[] buffer, int offset, int count)
            throws IOException {
        ByteBuffer bytes = ByteBuffer.wrap(buffer, offset, count);
        CoderResult result = CoderResult.OVERFLOW;
        while (result == CoderResult.OVERFLOW) {
            result = mDecoder.decode(bytes, mBuffer, false);
            mWriter.write(mBuffer.array(), 0, mBuffer.position());
            mBuffer.position(0);
        }
        if (result.isMalformed() || result.isUnmappable()) {
            result.throwException();
        }
    }
    
    @Override
    public void write(int arg0) throws IOException {
        byte onebyte[] = new byte[1];
        write(onebyte, 0, 1);
    }
    
    @Override
    public void flush() throws IOException {
        CoderResult result = mDecoder.decode(ByteBuffer.allocate(0), mBuffer, true);
        if (result.isMalformed() || result.isUnmappable()) {
            result.throwException();
        }
        mWriter.write(mBuffer.array(), 0, mBuffer.position());
        mBuffer.position(0);
        mWriter.flush();
    }

}