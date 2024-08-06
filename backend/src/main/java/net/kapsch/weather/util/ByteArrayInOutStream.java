package net.kapsch.weather.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ByteArrayInOutStream extends ByteArrayOutputStream {

    public ByteArrayInOutStream() {
        super();
    }

    public ByteArrayInputStream getInputStream() {
        ByteArrayInputStream in = new ByteArrayInputStream(this.buf, 0, this.count);
        this.buf = null;
        return in;
    }
}
