/*
 * $Id: Permissions.java 3117 2008-01-31 05:53:22Z xlv $
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

import com.lowagie.text.pdf.PdfWriter;

/**
 * This class can tell you more about the permissions that are allowed
 * on the PDF file.
 */
public class Permissions {

	/** Was the file encrypted? */
	protected boolean encrypted = true;
	/** Which owner password was provided to open the file? */
	protected byte[] ownerPassword = null;
	/** What is the user password? */
	protected byte[] userPassword = null;
	/** What are the document permissions? */
	protected int permissions = 0;
	/** How was the document encrypted? */
	protected int cryptoMode = 0;
	
	/**
	 * Tells you if the document was encrypted.
	 * @return true is the document was encrypted
	 */
	public boolean isEncrypted() {
		return encrypted;
	}
	/**
	 * Setter for the encrypted variable.
	 * @param encrypted	set this to true if the document was encrypted
	 */
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}
	/**
	 * Returns the owner password of the PDF file (if any).
	 * @return	the owner password that was provided upon opening the document
	 */
	public byte[] getOwnerPassword() {
		return ownerPassword;
	}
	/**
	 * Setter for the owner password.
	 * @param ownerPassword	the owner password
	 */
	public void setOwnerPassword(byte[] ownerPassword) {
		this.ownerPassword = ownerPassword;
	}
	/**
	 * Returns the user password (if any).
	 * @return	the user password
	 */
	public byte[] getUserPassword() {
		return userPassword;
	}
	/**
	 * Setter for the user password.
	 * @param userPassword the user password of a PDF file
	 */
	public void setUserPassword(byte[] userPassword) {
		this.userPassword = userPassword;
	}
	/**
	 * Returns the permissions in the form of an int (each bit is a specific permission)
	 * @return the value for the permissions
	 */
	public int getPermissions() {
		return permissions;
	}
	/**
	 * Setter for the permissions.
	 * @param permissions	the permissions in the form of an int
	 */
	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}
	/**
	 * Returns the crypto mode.
	 * @return	the crypto mode
	 */
	public int getCryptoMode() {
		return cryptoMode;
	}
	/**
	 * Setter for the crypto mode
	 * @param cryptoMode	the crypto mode
	 */
	public void setCryptoMode(int cryptoMode) {
		this.cryptoMode = cryptoMode;
	}
	
	/**
	 * Tells you if printing is allowed.
	 * @return	true if printing is allowed
	 */
	public boolean isAllowPrinting() {
		return
			!encrypted
			|| (PdfWriter.ALLOW_PRINTING & permissions) == PdfWriter.ALLOW_PRINTING;
	}
	/**
	 * Tells you if modifying the contents is allowed.
	 * @return true if modifying contents is allowed
	 */
	public boolean isAllowModifyContents(boolean decrypted) {
		return
			!encrypted
			|| (PdfWriter.ALLOW_MODIFY_CONTENTS & permissions) == PdfWriter.ALLOW_MODIFY_CONTENTS;
	}
	/**
	 * Tells you if copying is allowed.
	 * @return true if copying is allowed
	 */
	public boolean isAllowCopy(boolean decrypted) {
		return
			!encrypted
			|| (PdfWriter.ALLOW_COPY & permissions) == PdfWriter.ALLOW_COPY;
	}
	/**
	 * Tells you if modifying annotations is allowed
	 * @return true if modifying annotations is allowed
	 */
	public boolean isAllowModifyAnnotations() {
		return
			!encrypted
			|| (PdfWriter.ALLOW_MODIFY_ANNOTATIONS & permissions) == PdfWriter.ALLOW_MODIFY_ANNOTATIONS;
	}
	/**
	 * Tells you if filling in forms is allowed.
	 * @return true if filling in forms is allowed
	 */
	public boolean isAllowFillIn() {
		return
			!encrypted
			|| (PdfWriter.ALLOW_FILL_IN & permissions) == PdfWriter.ALLOW_FILL_IN;
	}
	/**
	 * Tells you if modifying the layout for screenreaders is allowed.
	 * @return true if modifying the layout for screenreaders is allowed
	 */
	public boolean isAllowScreenReaders() {
		return
			!encrypted
			|| (PdfWriter.ALLOW_SCREENREADERS & permissions) == PdfWriter.ALLOW_SCREENREADERS;
	}
	/**
	 * Tells you if document assembly is allowed.
	 * @return true if document assembly is allowed
	 */
	public boolean isAllowAssembly() {
		return
			!encrypted
			|| (PdfWriter.ALLOW_ASSEMBLY & permissions) == PdfWriter.ALLOW_ASSEMBLY;
	}
	/**
	 * Tells you if degraded printing is allowed.
	 * @return true if degraded printing is allowed
	 */
	public boolean isAllowDegradedPrinting() {
		return
			!encrypted
			|| (PdfWriter.ALLOW_DEGRADED_PRINTING & permissions) == PdfWriter.ALLOW_DEGRADED_PRINTING;
	}
}