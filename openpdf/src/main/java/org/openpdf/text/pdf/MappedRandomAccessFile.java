/*
 * $Id: MappedRandomAccessFile.java 3314 2008-05-01 23:48:39Z xlv $
 *
 * Copyright 2006 Joakim Sandstroem
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
package org.openpdf.text.pdf;

import org.openpdf.text.utils.LongMappedByteBuffer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * A {@link java.nio.MappedByteBuffer} wrapped as a {@link java.io.RandomAccessFile}
 *
 * @author Joakim Sandstroem Created on 6.9.2006
 */
public class MappedRandomAccessFile implements AutoCloseable {

    private LongMappedByteBuffer mappedByteBuffer = null;
    private FileChannel channel = null;

    /**
     * Constructs a new MappedRandomAccessFile instance
     *
     * @param filename String
     * @param mode     String r, w or rw
     * @throws FileNotFoundException on error
     * @throws IOException           on error
     */
    public MappedRandomAccessFile(String filename, String mode)
            throws IOException {

        if (mode.equals("rw")) {
            init(
                    new java.io.RandomAccessFile(filename, mode).getChannel(),
                    FileChannel.MapMode.READ_WRITE);
        } else {
            init(
                    new FileInputStream(filename).getChannel(),
                    FileChannel.MapMode.READ_ONLY);
        }

    }

    /**
     * initializes the channel and mapped bytebuffer
     *
     * @param channel FileChannel
     * @param mapMode FileChannel.MapMode
     * @throws IOException
     */
    private void init(FileChannel channel, FileChannel.MapMode mapMode)
            throws IOException {


        this.channel = channel;
        this.mappedByteBuffer = new LongMappedByteBuffer(channel, mapMode);

        mappedByteBuffer.load();
    }

    /**
     * @return FileChannel
     * @since 2.0.8
     */
    public FileChannel getChannel() {
        return channel;
    }

    /**
     * @return int next byte value or -1 on EOF
     * @see java.io.RandomAccessFile#read()
     */
    public int read() {
        long pos = mappedByteBuffer.position();
        long limit = mappedByteBuffer.limit();

        if (pos >= limit) {
            return -1; // EOF
        }

        byte b = mappedByteBuffer.get(pos);
        mappedByteBuffer.position(pos + 1);
        return b & 0xFF;
    }



    /**
     * @param bytes byte[]
     * @param off   int offset
     * @param len   int length
     * @return int bytes read or -1 on EOF
     * @see java.io.RandomAccessFile#read(byte[], int, int)
     */
    public int read(byte[] bytes, int off, int len) {
        if (off < 0 || len < 0 || off + len > bytes.length) {
            throw new IndexOutOfBoundsException();
        }

        long pos = mappedByteBuffer.position();
        long limit = mappedByteBuffer.limit();

        if (pos >= limit) {
            return -1;
        }

        int remaining = (int) Math.min(len, limit - pos);
        if (remaining <= 0) {
            return -1;
        }

        int totalRead = 0;
        while (totalRead < remaining) {
            int n = mappedByteBuffer.read(bytes, off + totalRead, remaining - totalRead);
            if (n <= 0) {
                break; // EOF or read failed
            }
            totalRead += n;
        }

        return totalRead == 0 ? -1 : totalRead;
    }



    /**
     * @return long
     * @see java.io.RandomAccessFile#getFilePointer()
     */
    public long getFilePointer() {
        return mappedByteBuffer.position();
    }

    /**
     * @param pos long position
     * @see java.io.RandomAccessFile#seek(long)
     */
    public void seek(long pos) {
        mappedByteBuffer.position(pos);
    }

    /**
     * @return long length
     * @see java.io.RandomAccessFile#length()
     */
    public long length() {
        return mappedByteBuffer.limit();
    }

    /**
     * Cleans the mapped bytebuffer and closes the channel
     *
     * @throws IOException on error
     * @see java.io.RandomAccessFile#close()
     */
    public void close() throws IOException {
        mappedByteBuffer = null;
        if (channel != null) {
            channel.close();
        }
        channel = null;
    }

}
