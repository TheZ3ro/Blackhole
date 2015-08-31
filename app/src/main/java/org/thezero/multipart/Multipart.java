package org.thezero.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Multipart {
    private static final byte CR = '\r';
    private static final byte LF = '\n';
    private static final byte HYPHEN = '-';
    private static final byte[] PREFIX = {CR, LF, HYPHEN, HYPHEN};

    private InputStream in;
    private String encoding;
    private byte[] boundary;
    private byte[] buffer = new byte[256];
    private int head;
    private int tail;
    private boolean eos;
    private boolean eof;

    public Multipart(InputStream in, String encoding, byte[] boundary)
            throws IOException {
        this.in = in;
        this.encoding = encoding;
        this.boundary = new byte[PREFIX.length + boundary.length];
        System.arraycopy(PREFIX, 0, this.boundary, 0, PREFIX.length);
        System.arraycopy(boundary, 0, this.boundary, PREFIX.length,
                boundary.length);
        // unread CR LF that may not precede the first occurrence of the
        // boundary string
        unread(CR);
        unread(LF);
        int b = read();
        while (b != -1) {
            b = read();
        }
    }

    public Part nextPart() throws IOException {
        if (eof) {
            return null;
        }
        InputStream stream = nextStream();
        Map<String,List<String>> headers
                = HeaderParser.parse(stream, encoding);
        return new Part(headers, stream);
    }

    private InputStream nextStream() throws IOException {
        if (eof) {
            return null;
        } else if (eos && head == tail) {
            // Nothing left to read, but the last boundary was not found
            throw new IOException("Malformed multipard stream");
        }
        return new InputStream() {
            boolean ended;

            @Override
            public int read() throws IOException {
                if (ended) {
//                    throw new IOException("Malformed multipart stream");
                    return -1;
                }
                int result = Multipart.this.read();
                ended = result < 0;
                return result;
            }

            @Override
            public void close() throws IOException {
                while (!ended) {
                    read();
                }
            }
        };
    }

    private int readByte() throws IOException {
        if (eof) {
            // We shouldn't be reading after the last boundary was found
            throw new IOException("Malformed multipard stream");
        }
        if (head != tail) {
            int result = buffer[head] & 0xFF;
            ++head;
            if (head == buffer.length) {
                head = 0;
            }
            return result;
        } else if (eos) {
            return -1;
        } else {
            int result = in.read();
            if (result < 0) {
                eos = true;
            }
            return result;
        }
    }

    private void unread(byte b) throws IOException {
        buffer[tail] = b;
        ++tail;
        if (tail == buffer.length) {
            tail = 0;
        }
        if (head == tail) {
            throw new IOException("Buffer overflow");
        }
    }

    private void unread(byte[] data, int offs, int len) throws IOException {
        int end = offs + len;
        for (int i = offs; i < end; ++i) {
            unread(data[i]);
        }
    }

    private int read() throws IOException {
        int b = readByte();
        int pos = 0;
        while (pos < boundary.length && b == (boundary[pos] &0xFF)) {
            ++pos;
            b = readByte();
        }
        if (pos == 0) {
            return b;
        } else if (pos == boundary.length) {
            if (b == CR) {
                b = readByte();
                if (b == LF) {
                    // End of a part
                    return -1;
                } else {
                    unread(boundary, 0, pos);
                    unread(CR);
                    if (b >= 0) {
                        unread((byte)b);
                    }
                    return readByte();
                }
            } else if (b == HYPHEN) {
                b = readByte();
                if (b == HYPHEN) {
                    // End of last part
                    eof = true;
                    return -1;
                } else {
                    unread(boundary, 0, pos);
                    unread(HYPHEN);
                    if (b >= 0) {
                        unread((byte)b);
                    }
                    return readByte();
                }
            }
        }
        unread(boundary, 0, pos);
        if (b >= 0) {
            unread((byte)b);
        }
        return readByte();
    }
}
