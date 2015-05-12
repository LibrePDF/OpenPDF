/*
/*
 * Copyright 2002 Paulo Soares
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import com.lowagie.text.DocumentException;

/** This class takes any PDF and returns exactly the same but
 * encrypted. All the content, links, outlines, etc, are kept.
 * It is also possible to change the info dictionary.
 */
public final class PdfEncryptor {
    
    private PdfEncryptor(){
    }
    
    /** Entry point to encrypt a PDF document. The encryption parameters are the same as in
     * <code>PdfWriter</code>. The userPassword and the
     *  ownerPassword can be null or have zero length. In this case the ownerPassword
     *  is replaced by a random string. The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * @param reader the read PDF
     * @param os the output destination
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * @param strength128Bits <code>true</code> for 128 bit key length, <code>false</code> for 40 bit key length
     * @throws DocumentException on error
     * @throws IOException on error */
    public static void encrypt(PdfReader reader, OutputStream os, byte userPassword[], byte ownerPassword[], int permissions, boolean strength128Bits) throws DocumentException, IOException {
        PdfStamper stamper = new PdfStamper(reader, os);
        stamper.setEncryption(userPassword, ownerPassword, permissions, strength128Bits);
        stamper.close();
    }
    
    /** Entry point to encrypt a PDF document. The encryption parameters are the same as in
     * <code>PdfWriter</code>. The userPassword and the
     *  ownerPassword can be null or have zero length. In this case the ownerPassword
     *  is replaced by a random string. The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * @param reader the read PDF
     * @param os the output destination
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * @param strength128Bits <code>true</code> for 128 bit key length, <code>false</code> for 40 bit key length
     * @param newInfo an optional <CODE>String</CODE> map to add or change
     * the info dictionary. Entries with <CODE>null</CODE>
     * values delete the key in the original info dictionary
     * @throws DocumentException on error
     * @throws IOException on error
     */
    public static void encrypt(PdfReader reader, OutputStream os, byte userPassword[], byte ownerPassword[], int permissions, boolean strength128Bits, HashMap newInfo) throws DocumentException, IOException {
        PdfStamper stamper = new PdfStamper(reader, os);
        stamper.setEncryption(userPassword, ownerPassword, permissions, strength128Bits);
        stamper.setMoreInfo(newInfo);
        stamper.close();
    }
    
    /** Entry point to encrypt a PDF document. The encryption parameters are the same as in
     * <code>PdfWriter</code>. The userPassword and the
     *  ownerPassword can be null or have zero length. In this case the ownerPassword
     *  is replaced by a random string. The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * @param reader the read PDF
     * @param os the output destination
     * @param strength <code>true</code> for 128 bit key length, <code>false</code> for 40 bit key length
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * @throws DocumentException on error
     * @throws IOException on error */
    public static void encrypt(PdfReader reader, OutputStream os, boolean strength, String userPassword, String ownerPassword, int permissions) throws DocumentException, IOException {
        PdfStamper stamper = new PdfStamper(reader, os);
        stamper.setEncryption(strength, userPassword, ownerPassword, permissions);
        stamper.close();
    }
    
    /** Entry point to encrypt a PDF document. The encryption parameters are the same as in
     * <code>PdfWriter</code>. The userPassword and the
     *  ownerPassword can be null or have zero length. In this case the ownerPassword
     *  is replaced by a random string. The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * @param reader the read PDF
     * @param os the output destination
     * @param strength <code>true</code> for 128 bit key length, <code>false</code> for 40 bit key length
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * @param newInfo an optional <CODE>String</CODE> map to add or change
     * the info dictionary. Entries with <CODE>null</CODE>
     * values delete the key in the original info dictionary
     * @throws DocumentException on error
     * @throws IOException on error
     */
    public static void encrypt(PdfReader reader, OutputStream os, boolean strength, String userPassword, String ownerPassword, int permissions, HashMap newInfo) throws DocumentException, IOException {
        PdfStamper stamper = new PdfStamper(reader, os);
        stamper.setEncryption(strength, userPassword, ownerPassword, permissions);
        stamper.setMoreInfo(newInfo);
        stamper.close();
    }

    
    /** Entry point to encrypt a PDF document. The encryption parameters are the same as in
     * <code>PdfWriter</code>. The userPassword and the
     *  ownerPassword can be null or have zero length. In this case the ownerPassword
     *  is replaced by a random string. The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * @param reader the read PDF
     * @param os the output destination
     * @param type the type of encryption. It can be one of STANDARD_ENCRYPTION_40, STANDARD_ENCRYPTION_128 or ENCRYPTION_AES128.
     * Optionally DO_NOT_ENCRYPT_METADATA can be ored to output the metadata in cleartext
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * @param newInfo an optional <CODE>String</CODE> map to add or change
     * the info dictionary. Entries with <CODE>null</CODE>
     * values delete the key in the original info dictionary
     * @throws DocumentException on error
     * @throws IOException on error
     */
    public static void encrypt(PdfReader reader, OutputStream os, int type, String userPassword, String ownerPassword, int permissions, HashMap newInfo) throws DocumentException, IOException {
        PdfStamper stamper = new PdfStamper(reader, os);
        stamper.setEncryption(type, userPassword, ownerPassword, permissions);
        stamper.setMoreInfo(newInfo);
        stamper.close();
    }
    
    /** Entry point to encrypt a PDF document. The encryption parameters are the same as in
     * <code>PdfWriter</code>. The userPassword and the
     *  ownerPassword can be null or have zero length. In this case the ownerPassword
     *  is replaced by a random string. The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * @param reader the read PDF
     * @param os the output destination
     * @param type the type of encryption. It can be one of STANDARD_ENCRYPTION_40, STANDARD_ENCRYPTION_128 or ENCRYPTION_AES128.
     * Optionally DO_NOT_ENCRYPT_METADATA can be ored to output the metadata in cleartext
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * values delete the key in the original info dictionary
     * @throws DocumentException on error
     * @throws IOException on error
     */
    public static void encrypt(PdfReader reader, OutputStream os, int type, String userPassword, String ownerPassword, int permissions) throws DocumentException, IOException {
        PdfStamper stamper = new PdfStamper(reader, os);
        stamper.setEncryption(type, userPassword, ownerPassword, permissions);
        stamper.close();
    }
    
    /**
     * Give you a verbose analysis of the permissions.
     * @param permissions the permissions value of a PDF file
     * @return a String that explains the meaning of the permissions value
     */
    public static String getPermissionsVerbose(int permissions) {
    	StringBuffer buf = new StringBuffer("Allowed:");
    	if ((PdfWriter.ALLOW_PRINTING & permissions) == PdfWriter.ALLOW_PRINTING) buf.append(" Printing");
        if ((PdfWriter.ALLOW_MODIFY_CONTENTS & permissions) == PdfWriter.ALLOW_MODIFY_CONTENTS) buf.append(" Modify contents");
        if ((PdfWriter.ALLOW_COPY & permissions) == PdfWriter.ALLOW_COPY) buf.append(" Copy");
        if ((PdfWriter.ALLOW_MODIFY_ANNOTATIONS & permissions) == PdfWriter.ALLOW_MODIFY_ANNOTATIONS) buf.append(" Modify annotations");
        if ((PdfWriter.ALLOW_FILL_IN & permissions) == PdfWriter.ALLOW_FILL_IN) buf.append(" Fill in");
        if ((PdfWriter.ALLOW_SCREENREADERS & permissions) == PdfWriter.ALLOW_SCREENREADERS) buf.append(" Screen readers");
        if ((PdfWriter.ALLOW_ASSEMBLY & permissions) == PdfWriter.ALLOW_ASSEMBLY) buf.append(" Assembly");
        if ((PdfWriter.ALLOW_DEGRADED_PRINTING & permissions) == PdfWriter.ALLOW_DEGRADED_PRINTING) buf.append(" Degraded printing");
        return buf.toString();
    }
    
    /**
     * Tells you if printing is allowed.
     * @param permissions the permissions value of a PDF file
     * @return	true if printing is allowed
     *
     * @since 2.0.7
     */
    public static boolean isPrintingAllowed(int permissions) {
        return (PdfWriter.ALLOW_PRINTING & permissions) == PdfWriter.ALLOW_PRINTING;
    }
    
    /**
     * Tells you if modifying content is allowed.
     * @param permissions the permissions value of a PDF file
     * @return	true if modifying content is allowed
     *
     * @since 2.0.7
     */
    public static boolean isModifyContentsAllowed(int permissions) {
        return (PdfWriter.ALLOW_MODIFY_CONTENTS & permissions) == PdfWriter.ALLOW_MODIFY_CONTENTS;
    } 
    
    /**
     * Tells you if copying is allowed.
     * @param permissions the permissions value of a PDF file
     * @return	true if copying is allowed
     *
     * @since 2.0.7
     */
    public static boolean isCopyAllowed(int permissions) {
        return (PdfWriter.ALLOW_COPY & permissions) == PdfWriter.ALLOW_COPY;
    }
    
    /**
     * Tells you if modifying annotations is allowed.
     * @param permissions the permissions value of a PDF file
     * @return	true if modifying annotations is allowed
     *
     * @since 2.0.7
     */
    public static boolean isModifyAnnotationsAllowed(int permissions) {
        return (PdfWriter.ALLOW_MODIFY_ANNOTATIONS & permissions) == PdfWriter.ALLOW_MODIFY_ANNOTATIONS;
    }
    
    /**
     * Tells you if filling in fields is allowed.
     * @param permissions the permissions value of a PDF file
     * @return	true if filling in fields is allowed
     *
     * @since 2.0.7
     */
    public static boolean isFillInAllowed(int permissions) {
        return (PdfWriter.ALLOW_FILL_IN & permissions) == PdfWriter.ALLOW_FILL_IN;
    }
    
    /**
     * Tells you if repurposing for screenreaders is allowed.
     * @param permissions the permissions value of a PDF file
     * @return	true if repurposing for screenreaders is allowed
     *
     * @since 2.0.7
     */
    public static boolean isScreenReadersAllowed(int permissions) {
        return (PdfWriter.ALLOW_SCREENREADERS & permissions) == PdfWriter.ALLOW_SCREENREADERS;
    }
    
    /**
     * Tells you if document assembly is allowed.
     * @param permissions the permissions value of a PDF file
     * @return	true if document assembly is allowed
     *
     * @since 2.0.7
     */
    public static boolean isAssemblyAllowed(int permissions) {
        return (PdfWriter.ALLOW_ASSEMBLY & permissions) == PdfWriter.ALLOW_ASSEMBLY;
    }
    
    /**
     * Tells you if degraded printing is allowed.
     * @param permissions the permissions value of a PDF file
     * @return	true if degraded printing is allowed
     *
     * @since 2.0.7
     */
    public static boolean isDegradedPrintingAllowed(int permissions) {
        return (PdfWriter.ALLOW_DEGRADED_PRINTING & permissions) == PdfWriter.ALLOW_DEGRADED_PRINTING;
    }
}
