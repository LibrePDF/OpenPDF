/*
 * OpenPDF, LongMappedByteBuffer.
 *
 * Copyright 2025 Andreas Røsdal
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

package org.openpdf.text.utils;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.BufferUnderflowException;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;


/**
 * A utility class that allows random access to memory-mapped files, including files larger than 2GB.
 * <p>
 * This is the Java 22+ variant of this class, packaged in {@code META-INF/versions/22} of the multi-release jar. It
 * has the same public API and behavior as the Java 21 implementation, but maps the file with the Foreign Function
 * &amp; Memory API (JEP 454) into a single {@link MemorySegment} that belongs to its own {@link Arena}. Closing this
 * buffer closes the arena, which unmaps the file immediately instead of waiting for the garbage collector. This
 * releases the lock that Windows holds on mapped files, so they can be deleted or moved right after
 * {@link #close()}.
 * <p>
 * A shared arena is used, because a {@code PdfReader} may be created in one thread and read in another. Accessing
 * the buffer after it has been closed throws an {@link IllegalStateException}; it never accesses unmapped memory.
 *
 *  @since 2.0.4
 */
public class LongMappedByteBuffer implements AutoCloseable {

    private final Arena arena;
    private final MemorySegment segment;
    private final long size;

    private long position = 0;

    /**
     * Constructs a new LongMappedByteBuffer by mapping the file channel.
     */
    public LongMappedByteBuffer(FileChannel channel, FileChannel.MapMode mode) throws IOException {
        this.size = channel.size();
        Arena mappingArena = Arena.ofShared();
        try {
            this.segment = channel.map(mode, 0, size, mappingArena);
        } catch (Throwable t) {
            mappingArena.close();
            throw t;
        }
        this.arena = mappingArena;
    }

    public byte get() {
        byte b = get(position);
        position++;
        return b;
    }

    public byte get(long pos) {
        if (pos >= size) {
            throw new BufferUnderflowException(); // triggers EOF handling in MappedRandomAccessFile
        }
        return segment.get(ValueLayout.JAVA_BYTE, pos);
    }

    public void get(long pos, byte[] dst, int off, int len) {
        if (off < 0 || len < 0 || off + len > dst.length) {
            throw new IndexOutOfBoundsException("Invalid offset/length");
        }
        if (len == 0) {
            return;
        }
        MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, pos, dst, off, len);
    }

    public void get(byte[] dst, int off, int len) {
        get(position, dst, off, len);
        position += len;
    }

    public void put(byte value) {
        put(position, value);
        position++;
    }

    public void put(long pos, byte value) {
        checkWritable();
        segment.set(ValueLayout.JAVA_BYTE, pos, value);
    }

    public void put(byte[] src, int off, int len) {
        if (off < 0 || len < 0 || off + len > src.length) {
            throw new IndexOutOfBoundsException("Invalid offset/length");
        }
        if (len == 0) {
            return;
        }
        checkWritable();
        MemorySegment.copy(src, off, segment, ValueLayout.JAVA_BYTE, position, len);
        position += len;
    }

    public int read(byte[] bytes, int off, int len) {
        long pos = position();
        long limit = limit();

        if (pos >= limit) {
            return -1;
        }

        int available = (int) Math.min(len, limit - pos);

        get(pos, bytes, off, available); // will throw if something is wrong
        position(pos + available);

        return available;
    }

    public long position() {
        return position;
    }

    public LongMappedByteBuffer position(long newPosition) {
        if (newPosition < 0 || newPosition > size) {
            throw new IllegalArgumentException("Position out of bounds");
        }

        this.position = newPosition;
        return this;
    }

    public long size() {
        return size;
    }

    public long limit() {
        return size;
    }

    public LongMappedByteBuffer load() {
        segment.load();
        return this;
    }

    public boolean isLoaded() {
        return segment.isLoaded();
    }

    public void force() {
        segment.force();
    }

    /**
     * Unmaps the file immediately. The buffer must not be used after it has been closed. Calling this method more
     * than once has no effect.
     *
     * @since 3.0.6
     */
    @Override
    public void close() {
        // not synchronized in the signature: the public API must be identical to the Java 21 class (jar --validate)
        synchronized (this) {
            if (arena.scope().isAlive()) {
                arena.close();
            }
        }
    }

    private void checkWritable() {
        // same exception as the MappedByteBuffer based implementation, instead of an IllegalArgumentException
        if (segment.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
    }
}
