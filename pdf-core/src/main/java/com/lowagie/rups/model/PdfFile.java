/*
 * $Id: PdfFile.java 4033 2009-07-23 13:08:02Z blowagie $
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

package com.lowagie.rups.model;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import com.lowagie.text.DocumentException;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.RandomAccessFileOrArray;

/**
 * Wrapper for both iText's PdfReader (referring to a PDF file to read)
 * and SUN's PDFFile (referring to the same PDF file to render).
 */
public class PdfFile {

	// member variables
	
	/** The directory where the file can be found (if the PDF was passed as a file). */
	protected File directory = null;
	
	/** The original filename. */
	protected String filename = null;
	
	/** The PdfReader object. */
	protected PdfReader reader = null;
	
	/** The file permissions */
	protected Permissions permissions = null;
	
	// constructors
	/**
	 * Constructs a PdfFile object.
	 * @param	file	the File to read
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public PdfFile(File file) throws IOException, DocumentException {
		if (file == null)
			throw new IOException("No file selected.");
		RandomAccessFileOrArray pdf = new RandomAccessFileOrArray(file.getAbsolutePath());
		directory = file.getParentFile();
		filename = file.getName();
		readFile(pdf);
	}
	
	/**
	 * Constructs a PdfFile object.
	 * @param	file	the byte[] to read
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public PdfFile(byte[] file) throws IOException, DocumentException {
		RandomAccessFileOrArray pdf = new RandomAccessFileOrArray(file);
		readFile(pdf);
	}
	
	/**
	 * Does the actual reading of the file into PdfReader and PDFFile.
	 * @param pdf	a Random Access File or Array
	 * @throws IOException
	 * @throws DocumentException
	 */
	protected void readFile(RandomAccessFileOrArray pdf) throws IOException, DocumentException {
		// reading the file into PdfReader
		permissions = new Permissions();
		try {
			reader = new PdfReader(pdf, null);
			permissions.setEncrypted(false);
		} catch(BadPasswordException bpe) {
		    JPasswordField passwordField = new JPasswordField(32);
		    JOptionPane.showConfirmDialog(null, passwordField, "Enter the User or Owner Password of this PDF file", JOptionPane.OK_CANCEL_OPTION);
		    byte[] password = new String(passwordField.getPassword()).getBytes();
		    reader = new PdfReader(pdf, password);
		    permissions.setEncrypted(true);
		    permissions.setCryptoMode(reader.getCryptoMode());
		    permissions.setPermissions(reader.getPermissions());
		    if (reader.isOpenedWithFullPermissions()) {
		    	permissions.setOwnerPassword(password);
		    	permissions.setUserPassword(reader.computeUserPassword());
		    }
		    else {
		    	throw new IOException("You need the owner password of this file to open it in iText Trapeze.");
		    }
		}
	}

	/**
	 * Getter for iText's PdfReader object.
	 * @return	a PdfReader object
	 */
	public PdfReader getPdfReader() {
		return reader;
	}
}