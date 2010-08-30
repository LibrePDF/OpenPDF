/*
 * $Id: PdfEncryptionSettings.java 3117 2008-01-31 05:53:22Z xlv $
 *
 * Copyright 2006 Bruno Lowagie
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

package com.lowagie.text.pdf.interfaces;

import java.security.cert.Certificate;

import com.lowagie.text.DocumentException;

/**
 * Encryption settings are described in section 3.5 (more specifically
 * section 3.5.2) of the PDF Reference 1.7.
 * They are explained in section 3.3.3 of the book 'iText in Action'.
 * The values of the different  preferences were originally stored
 * in class PdfWriter, but they have been moved to this separate interface
 * for reasons of convenience.
 */

public interface PdfEncryptionSettings {

    
    /**
     * Sets the encryption options for this document. The userPassword and the
     * ownerPassword can be null or have zero length. In this case the ownerPassword
     * is replaced by a random string. The open permissions for the document can be
     * AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     * AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     * The permissions can be combined by ORing them.
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * @param encryptionType the type of encryption. It can be one of STANDARD_ENCRYPTION_40, STANDARD_ENCRYPTION_128 or ENCRYPTION_AES128.
     * Optionally DO_NOT_ENCRYPT_METADATA can be ored to output the metadata in cleartext
     * @throws DocumentException if the document is already open
     */
    public void setEncryption(byte userPassword[], byte ownerPassword[], int permissions, int encryptionType) throws DocumentException;

    /**
     * Sets the certificate encryption options for this document. An array of one or more public certificates
     * must be provided together with an array of the same size for the permissions for each certificate.
     *  The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * Optionally DO_NOT_ENCRYPT_METADATA can be ored to output the metadata in cleartext
     * @param certs the public certificates to be used for the encryption
     * @param permissions the user permissions for each of the certificates
     * @param encryptionType the type of encryption. It can be one of STANDARD_ENCRYPTION_40, STANDARD_ENCRYPTION_128 or ENCRYPTION_AES128.
     * @throws DocumentException if the document is already open
     */
    public void setEncryption(Certificate[] certs, int[] permissions, int encryptionType) throws DocumentException;
}