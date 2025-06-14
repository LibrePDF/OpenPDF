/*
 * Copyright 2010 Pirion Systems Pty Ltd, 139 Warry St,
 * Fortitude Valley, Queensland, Australia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.sun.pdfview;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Exposes a {@link ByteBuffer} as an {@link InputStream}.
 *
 * @author Luke Kirby
 */
public class ByteBufferInputStream extends InputStream {

    /** The underlying byte buffer */
    private ByteBuffer buffer;

    /**
     * Class constructor
     * @param buffer the buffer to present as an input stream, positioned
     *  at the current read position of the byte buffer
     */
    public ByteBufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        if (b == null) {
	    throw new NullPointerException();
	} else if (off < 0 || len < 0 || len > b.length - off) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return 0;
	}

        final int remaining = buffer.remaining();
        if (remaining == 0) {
            return -1;
        } else if (remaining < len) {
            buffer.get(b, off, remaining);
            return remaining;
        } else {
            buffer.get(b, off, len);
            return len;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        } else {
            final int remaining = buffer.remaining();
            if (n < remaining) {
                buffer.position(buffer.position() + remaining);
                return remaining;
            } else {
                buffer.position((int) (buffer.position() + n));
                return n;
            }
        }
    }

    @Override
    public int read() throws IOException {
        return buffer.get();
    }

    @Override
    public int available() throws IOException {
        return buffer.remaining();
    }

    @Override
    public void mark(int readlimit) {
        buffer.mark();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void reset() throws IOException {
        buffer.reset();
    }
}
