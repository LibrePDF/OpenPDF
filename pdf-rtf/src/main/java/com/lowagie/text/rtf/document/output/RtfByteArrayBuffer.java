/*
 * Copyright 2007 Thomas Bickel
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
 * LGPL license (the ?GNU LIBRARY GENERAL PUBLIC LICENSE?), in which case the
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

package com.lowagie.text.rtf.document.output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.lowagie.text.error_messages.MessageLocalization;

/**
 * A RtfByteArrayBuffer works much like {@link ByteArrayOutputStream} but is cheaper and faster in most cases
 * (exception: large writes when reusing buffers).
 * 
 * @version $Id: RtfByteArrayBuffer.java 4065 2009-09-16 23:09:11Z psoares33 $
 * @author Thomas Bickel (tmb99@inode.at)
 */
public final class RtfByteArrayBuffer extends OutputStream
{
	private final java.util.List arrays = new java.util.ArrayList();
	private byte[] buffer;
	private int pos = 0;
	private int size = 0;
	
	/**
	 * Constructs a new buffer with a default initial size of 128 bytes.
	 */
	public RtfByteArrayBuffer()
	{    		
		this(256);
	}
	/**
	 * Creates a new buffer with the given initial size.
	 * 
	 * @param bufferSize desired initial size in bytes
	 */
	public RtfByteArrayBuffer(final int bufferSize)
	{
		if((bufferSize <= 0) || (bufferSize > 1<<30)) throw new IllegalArgumentException(MessageLocalization.getComposedMessage("buffersize.1", bufferSize));
		
		int n = 1<<5;
		while(n < bufferSize) {
			n <<= 1;
		}
		buffer = new byte[n];
	}
	
	public String toString()
	{
		return("RtfByteArrayBuffer: size="+size()+" #arrays="+arrays.size()+" pos="+pos);
	}
	
	/**
	 * Resets this buffer.
	 */
	public void reset()
	{
		arrays.clear();
		pos = 0;
		size = 0;
	}
	
	/**
	 * Returns the number of bytes that have been written to this buffer so far.
	 * 
     * @return number of bytes written to this buffer
	 */
	public long size()
	{
		return size;
	}
	
	private void flushBuffer()
	{
		flushBuffer(1);
	}
	private void flushBuffer(final int reqSize)
	{
		if(reqSize < 0) throw new IllegalArgumentException();
		
		if(pos == 0) return;

		if(pos == buffer.length) {
			//add old buffer, alloc new (possibly larger) buffer
			arrays.add(buffer);
			int newSize = buffer.length;
			buffer = null;
			final int MAX = Math.max(1, size>>24) << 16;
			while(newSize < MAX) {
				newSize <<= 1;
				if(newSize >= reqSize) break;
			}
			buffer = new byte[newSize];
		} else {
			//copy buffer contents to newly allocated buffer
			final byte[] c = new byte[pos];
			System.arraycopy(buffer, 0, c, 0, pos);
			arrays.add(c);    			
		}
		pos = 0;    		
	}
	
	/**
	 * Copies the given byte to the internal buffer.
	 * 
	 * @param b
	 */
	public void write(final int b)
	{
		buffer[pos] = (byte)b;
		size++;
		if(++pos == buffer.length) flushBuffer();
	}    	
	/**
	 * Copies the given array to the internal buffer.
	 * 
	 * @param src
	 */
	public void write(final byte[] src)
	{
		if(src == null) throw new NullPointerException();

		if(src.length < buffer.length - pos) {
			System.arraycopy(src, 0, buffer, pos, src.length);
			pos += src.length;
			size += src.length;
			return;
		}
		writeLoop(src, 0, src.length);
	}
	/**
	 * Copies len bytes starting at position off from the array src to the internal buffer.
	 * 
	 * @param src
	 * @param off
	 * @param len
	 */
	public void write(final byte[] src, int off, int len)
	{
		if(src == null) throw new NullPointerException();
		if((off < 0) || (off > src.length) || (len < 0) || ((off + len) > src.length) || ((off + len) < 0)) throw new IndexOutOfBoundsException();

		writeLoop(src, off, len);		
	}
	private void writeLoop(final byte[] src, int off, int len)
	{
		while(len > 0) {
			final int room = buffer.length - pos;
			final int n = len > room ? room : len;
			System.arraycopy(src, off, buffer, pos, n);
			len -= n;
			off += n;
			pos += n;
			size += n;
			if(pos == buffer.length) flushBuffer(len);
		}		
	}
	
	/**
	 * Writes all bytes available in the given inputstream to this buffer. 
	 * 
	 * @param in
     * @return number of bytes written
	 * @throws IOException
	 */
	public long write(final InputStream in) throws IOException
	{
		if(in == null) throw new NullPointerException();
		
		final long sizeStart = size;
		while(true) {
			final int n = in.read(buffer, pos, buffer.length - pos);
			if(n < 0) break;
			pos += n;
			size += n;
			if(pos == buffer.length) flushBuffer();
		}
		return(size - sizeStart);
	}
	
	/**
	 * Appends the given array to this buffer without copying (if possible). 
	 * 
	 * @param a
	 */
	public void append(final byte[] a)
	{
		if(a == null) throw new NullPointerException();
		if(a.length == 0) return;
		
		if(a.length <= 8) {
			write(a, 0, a.length);		
		} else
		if((a.length <= 16) && (pos > 0) && ((buffer.length - pos) > a.length)) {
			write(a, 0, a.length);
		} else {
			flushBuffer();
			arrays.add(a);
			size += a.length;
		}
	}
	/**
	 * Appends all arrays to this buffer without copying (if possible).
	 * 
	 * @param a
	 */
	public void append(final byte[][] a)
	{
		if(a == null) throw new NullPointerException();

		for(int k = 0; k < a.length; k++) {
			append(a[k]);
		}
	}
	
	/**
	 * Returns the internal list of byte array buffers without copying the buffer contents. 
	 * 
     * @return number of bytes written
	 */
	public byte[][] toByteArrayArray()
	{
		flushBuffer();
		return(byte[][])arrays.toArray(new byte[arrays.size()][]);
	}
	
	/**
	 * Allocates a new array and copies all data that has been written to this buffer to the newly allocated array.
	 * 
     * @return a new byte array
	 */
	public byte[] toByteArray()
	{
		final byte[] r = new byte[size];
		int off = 0;
		final int n = arrays.size();
		for(int k = 0; k < n; k++) {
			byte[] src = (byte[])arrays.get(k);
			System.arraycopy(src, 0, r, off, src.length);
			off += src.length;
		}
		if(pos > 0) System.arraycopy(buffer, 0, r, off, pos);
		return r;
	}

	/**
	 * Writes all data that has been written to this buffer to the given output stream.
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void writeTo(final OutputStream out) throws IOException
	{
		if(out == null) throw new NullPointerException();
		
		final int n = arrays.size();
		for(int k = 0; k < n; k++) {
			byte[] src = (byte[])arrays.get(k);
			out.write(src);
		}
		if(pos > 0) out.write(buffer, 0, pos);
	}
}
