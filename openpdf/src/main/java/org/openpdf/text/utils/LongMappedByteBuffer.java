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
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


/**
 * A utility class that allows random access to files larger than 2GB by internally
 * mapping them into multiple {@link MappedByteBuffer} chunks of up to 2GB each.
 *
 *  @since 2.0.4
 */
public class LongMappedByteBuffer {

    private static final long CHUNK_SIZE = Integer.MAX_VALUE; // 2 GB
    private final MappedByteBuffer[] chunks;
    private final long size;

    private long position = 0;

    /**
     * Constructs a new LongMappedByteBuffer by chunk-mapping the file channel.
     */
    public LongMappedByteBuffer(FileChannel channel, FileChannel.MapMode mode) throws IOException {
        this.size = channel.size();
        int numChunks = (int) ((size + CHUNK_SIZE - 1) / CHUNK_SIZE);
        this.chunks = new MappedByteBuffer[numChunks];

        for (int i = 0; i < numChunks; i++) {
            long pos = i * CHUNK_SIZE;
            long chunkSize = Math.min(CHUNK_SIZE, size - pos);
            chunks[i] = channel.map(mode, pos, chunkSize);
        }
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
        int chunkIndex = (int) (pos / CHUNK_SIZE);
        int offset = (int) (pos % CHUNK_SIZE);
        MappedByteBuffer chunk = chunks[chunkIndex];
        return chunk.get(offset);
    }



    public void get(long pos, byte[] dst, int off, int len) {
        if (off < 0 || len < 0 || off + len > dst.length) {
            throw new IndexOutOfBoundsException("Invalid offset/length");
        }

        long readPos = pos;  
        int remaining = len;
        int dstPos = off;

        while (remaining > 0) {
            int chunkIndex = (int) (readPos / CHUNK_SIZE);
            int chunkOffset = (int) (readPos % CHUNK_SIZE);
            int chunkRemaining = chunks[chunkIndex].limit() - chunkOffset;
            int toRead = Math.min(remaining, chunkRemaining);

            ByteBuffer dup = chunks[chunkIndex].duplicate();
            dup.position(chunkOffset);
            dup.get(dst, dstPos, toRead);

            readPos += toRead;
            dstPos += toRead;
            remaining -= toRead;
        }
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
        int chunkIndex = (int) (pos / CHUNK_SIZE);
        int offset = (int) (pos % CHUNK_SIZE);
        chunks[chunkIndex].put(offset, value);
    }

    public void put(byte[] src, int off, int len) {
        if (off < 0 || len < 0 || off + len > src.length) {
            throw new IndexOutOfBoundsException("Invalid offset/length");
        }

        int remaining = len;
        int srcPos = off;
        long pos = position;

        while (remaining > 0) {
            int chunkIndex = (int) (pos / CHUNK_SIZE);
            int chunkOffset = (int) (pos % CHUNK_SIZE);
            int chunkRemaining = chunks[chunkIndex].limit() - chunkOffset;
            int toWrite = Math.min(remaining, chunkRemaining);

            ByteBuffer dup = chunks[chunkIndex].duplicate();
            dup.position(chunkOffset);
            dup.put(src, srcPos, toWrite);

            pos += toWrite;
            srcPos += toWrite;
            remaining -= toWrite;
        }

        position = pos;
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
        for (MappedByteBuffer chunk : chunks) {
            chunk.load();
        }
        return this;
    }

    public boolean isLoaded() {
        for (MappedByteBuffer chunk : chunks) {
            if (!chunk.isLoaded()) {
                return false;
            }
        }
        return true;
    }

    public void force() {
        for (MappedByteBuffer chunk : chunks) {
            chunk.force();
        }
    }
}
