package com.xhb.prism.util.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public class ReaderInputStream extends InputStream {
    
    private final Reader mReader;
    private CharsetEncoder mEncoder;
    private CharBuffer mBuffer = CharBuffer.allocate(1024);

    public ReaderInputStream(Reader reader, String charsetName) {
        mReader = reader;
        mEncoder = Charset.forName(charsetName).newEncoder();
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount)
            throws IOException {
        ByteBuffer bytes = ByteBuffer.wrap(buffer, byteOffset, byteCount);
        while (bytes.position() < byteOffset + byteCount) {
            if (!mBuffer.hasRemaining()) {
                int charRead = mReader.read(mBuffer.array(), mBuffer.limit(), 
                        mBuffer.capacity() - mBuffer.limit());
                if (charRead < 0) {
                    return bytes.position() > byteOffset 
                            ? bytes.position() - byteOffset : -1;
                }
                mBuffer.limit(mBuffer.limit() + charRead);
            }
            CoderResult result = mEncoder.encode(mBuffer, bytes, false);
            if (result.isOverflow()) {
                break;
            } else if (result.isError()) {
                result.throwException();
            }
        }
        return bytes.position() - byteOffset;
    }

    @Override
    public int read() throws IOException {
        byte onebyte[] = new byte[1];
        int n = read(onebyte, 0, 1);
        return n > 0 ? onebyte[0] : -1;
    }
}