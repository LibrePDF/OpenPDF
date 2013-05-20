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
 * http://www.lowagie.com/iText/
 */
package com.lowagie.text.pdf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A {@link java.nio.MappedByteBuffer} wrapped as a {@link java.io.RandomAccessFile}
 *
 * @author Joakim Sandstroem
 * Created on 6.9.2006
 */
public class MappedRandomAccessFile {
    
    private MappedByteBuffer mappedByteBuffer = null;
    private FileChannel channel = null;
    
    /**
     * Constructs a new MappedRandomAccessFile instance
     * @param filename String
     * @param mode String r, w or rw
     * @throws FileNotFoundException
     * @throws IOException
     */
    public MappedRandomAccessFile(String filename, String mode)
    throws FileNotFoundException, IOException {
        
        if (mode.equals("rw"))
            init(
                    new java.io.RandomAccessFile(filename, mode).getChannel(),
                    FileChannel.MapMode.READ_WRITE);
        else
            init(
                    new FileInputStream(filename).getChannel(),
                    FileChannel.MapMode.READ_ONLY);
        
    }
    
    /**
     * initializes the channel and mapped bytebuffer
     * @param channel FileChannel
     * @param mapMode FileChannel.MapMode
     * @throws IOException
     */
    private void init(FileChannel channel, FileChannel.MapMode mapMode)
    throws IOException {
        
        this.channel = channel;
        this.mappedByteBuffer = channel.map(mapMode, 0L, channel.size());
        mappedByteBuffer.load();
    }

    /**
     * @since 2.0.8
     */
    public FileChannel getChannel() {
    	return channel;
    }
    
    /**
     * @see java.io.RandomAccessFile#read()
     * @return int next integer or -1 on EOF
     */
    public int read() {
        try {
            byte b = mappedByteBuffer.get();
            int n = b & 0xff;
            
            return n;
        } catch (BufferUnderflowException e) {
            return -1; // EOF
        }
    }
    
    /**
     * @see java.io.RandomAccessFile#read(byte[], int, int)
     * @param bytes byte[]
     * @param off int offset
     * @param len int length
     * @return int bytes read or -1 on EOF
     */
    public int read(byte bytes[], int off, int len) {
        int pos = mappedByteBuffer.position();
        int limit = mappedByteBuffer.limit();
        if (pos == limit)
            return -1; // EOF
        int newlimit = pos + len - off;
        if (newlimit > limit) {
            len = limit - pos; // don't read beyond EOF
        }
        mappedByteBuffer.get(bytes, off, len);
        return len;
    }
    
    /**
     * @see java.io.RandomAccessFile#getFilePointer()
     * @return long
     */
    public long getFilePointer() {
        return mappedByteBuffer.position();
    }
    
    /**
     * @see java.io.RandomAccessFile#seek(long)
     * @param pos long position
     */
    public void seek(long pos) {
        mappedByteBuffer.position((int) pos);
    }
    
    /**
     * @see java.io.RandomAccessFile#length()
     * @return long length
     */
    public long length() {
        return mappedByteBuffer.limit();
    }
    
    /**
     * @see java.io.RandomAccessFile#close()
     * Cleans the mapped bytebuffer and closes the channel
     */
    public void close() throws IOException {
        clean(mappedByteBuffer);
        mappedByteBuffer = null;
        if (channel != null)
            channel.close();
        channel = null;
    }
    
    /**
     * invokes the close method
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
    
    /**
     * invokes the clean method on the ByteBuffer's cleaner
     * @param buffer ByteBuffer
     * @return boolean true on success
     */
    public static boolean clean(final java.nio.ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect())
            return false;
        
        Boolean b = (Boolean) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Boolean success = Boolean.FALSE;
                try {
                    Method getCleanerMethod = buffer.getClass().getMethod("cleaner", (Class[])null);
                    getCleanerMethod.setAccessible(true);
                    Object cleaner = getCleanerMethod.invoke(buffer, (Object[])null);
                    Method clean = cleaner.getClass().getMethod("clean", (Class[])null);
                    clean.invoke(cleaner, (Object[])null);
                    success = Boolean.TRUE;
                } catch (Exception e) {
                    // This really is a show stopper on windows
                    //e.printStackTrace();
                }
                return success;
            }
        });
        
        return b.booleanValue();
    }
    
}
