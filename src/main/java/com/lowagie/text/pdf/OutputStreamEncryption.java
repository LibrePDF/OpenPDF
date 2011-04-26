/*
 * $Id: OutputStreamEncryption.java 3117 2008-01-31 05:53:22Z xlv $
 *
 * Copyright 2006 Paulo Soares
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
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.pdf.crypto.AESCipher;
import com.lowagie.text.pdf.crypto.IVGenerator;
import com.lowagie.text.pdf.crypto.ARCFOUREncryption;
import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamEncryption extends OutputStream {
    
    protected OutputStream out;
    protected ARCFOUREncryption arcfour;
    protected AESCipher cipher;
    private byte[] sb = new byte[1];
    private static final int AES_128 = 4;
    private boolean aes;
    private boolean finished;
    
    /** Creates a new instance of OutputStreamCounter */
    public OutputStreamEncryption(OutputStream out, byte key[], int off, int len, int revision) {
        try {
            this.out = out;
            aes = revision == AES_128;
            if (aes) {
                byte[] iv = IVGenerator.getIV();
                byte[] nkey = new byte[len];
                System.arraycopy(key, off, nkey, 0, len);
                cipher = new AESCipher(true, nkey, iv);
                write(iv);
            }
            else {
                arcfour = new ARCFOUREncryption();
                arcfour.prepareARCFOURKey(key, off, len);
            }
        } catch (Exception ex) {
            throw new ExceptionConverter(ex);
        }
    }
    
    public OutputStreamEncryption(OutputStream out, byte key[], int revision) {
        this(out, key, 0, key.length, revision);
    }
    
    /** Closes this output stream and releases any system resources
     * associated with this stream. The general contract of <code>close</code>
     * is that it closes the output stream. A closed stream cannot perform
     * output operations and cannot be reopened.
     * <p>
     * The <code>close</code> method of <code>OutputStream</code> does nothing.
     *
     * @exception  IOException  if an I/O error occurs.
     *
     */
    public void close() throws IOException {
        finish();
        out.close();
    }
    
    /** Flushes this output stream and forces any buffered output bytes
     * to be written out. The general contract of <code>flush</code> is
     * that calling it is an indication that, if any bytes previously
     * written have been buffered by the implementation of the output
     * stream, such bytes should immediately be written to their
     * intended destination.
     * <p>
     * The <code>flush</code> method of <code>OutputStream</code> does nothing.
     *
     * @exception  IOException  if an I/O error occurs.
     *
     */
    public void flush() throws IOException {
        out.flush();
    }
    
    /** Writes <code>b.length</code> bytes from the specified byte array
     * to this output stream. The general contract for <code>write(b)</code>
     * is that it should have exactly the same effect as the call
     * <code>write(b, 0, b.length)</code>.
     *
     * @param      b   the data.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.OutputStream#write(byte[], int, int)
     *
     */
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    
    /** Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     * <p>
     * Subclasses of <code>OutputStream</code> must provide an
     * implementation for this method.
     *
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> may be thrown if the
     *             output stream has been closed.
     *
     */
    public void write(int b) throws IOException {
        sb[0] = (byte)b;
        write(sb, 0, 1);
    }
    
    /** Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * The general contract for <code>write(b, off, len)</code> is that
     * some of the bytes in the array <code>b</code> are written to the
     * output stream in order; element <code>b[off]</code> is the first
     * byte written and <code>b[off+len-1]</code> is the last byte written
     * by this operation.
     * <p>
     * The <code>write</code> method of <code>OutputStream</code> calls
     * the write method of one argument on each of the bytes to be
     * written out. Subclasses are encouraged to override this method and
     * provide a more efficient implementation.
     * <p>
     * If <code>b</code> is <code>null</code>, a
     * <code>NullPointerException</code> is thrown.
     * <p>
     * If <code>off</code> is negative, or <code>len</code> is negative, or
     * <code>off+len</code> is greater than the length of the array
     * <code>b</code>, then an <tt>IndexOutOfBoundsException</tt> is thrown.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     *
     */
    public void write(byte[] b, int off, int len) throws IOException {
        if (aes) {
            byte[] b2 = cipher.update(b, off, len);
            if (b2 == null || b2.length == 0)
                return;
            out.write(b2, 0, b2.length);
        }
        else {
            byte[] b2 = new byte[Math.min(len, 4192)];
            while (len > 0) {
                int sz = Math.min(len, b2.length);
                arcfour.encryptARCFOUR(b, off, sz, b2, 0);
                out.write(b2, 0, sz);
                len -= sz;
                off += sz;
            }
        }
    }
    
    public void finish() throws IOException {
        if (!finished) {
            finished = true;
            if (aes) {
                byte[] b;
                try {
                    b = cipher.doFinal();
                } catch (Exception ex) {
                    throw new ExceptionConverter(ex);
                }
                out.write(b, 0, b.length);
            }
        }
    }
}