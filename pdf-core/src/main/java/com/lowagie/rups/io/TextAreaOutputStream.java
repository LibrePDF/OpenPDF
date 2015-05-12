/*
 * $Id: TextAreaOutputStream.java 3117 2008-01-31 05:53:22Z xlv $
 *
 * Copyright 2007 Bruno Lowagie.
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

package com.lowagie.rups.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

/**
 * Everything writing to this OutputStream will be shown in a JTextArea.
 */
public class TextAreaOutputStream extends OutputStream {
	/** The text area to which we want to write. */
	protected JTextArea text;
	/** Keeps track of the offset of the text in the text area. */
	protected int offset;
	
	/**
	 * Constructs a TextAreaOutputStream.
	 * @param text	the text area to which we want to write.
	 * @throws IOException
	 */
	public TextAreaOutputStream(JTextArea text) throws IOException {
		this.text = text;
		clear();
	}

	/**
	 * Clear the text area.
	 */
	public void clear() {
		text.setText(null);
		offset = 0;
	}
	
	/**
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int i) throws IOException {
		byte[] b = { (byte)i };
		write(b, 0, 1);
	}

	/**
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		String snippet = new String(b, off, len);
		text.insert(snippet, offset);
		offset += len - off;
	}

	/**
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] b) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		byte[] snippet = new byte[1024];
		int bytesread;
		while ((bytesread = bais.read(snippet)) > 0) {
			write(snippet, 0, bytesread);
		}
	}
	
}
