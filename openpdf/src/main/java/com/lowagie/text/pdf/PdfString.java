/*
 * $Id: PdfString.java 3759 2009-03-06 16:05:00Z blowagie $
 *
 * Copyright 1999, 2000, 2001, 2002 Bruno Lowagie
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

package com.lowagie.text.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * A <CODE>PdfString</CODE>-class is the PDF-equivalent of a
 * JAVA-<CODE>String</CODE>-object.
 * <P>
 * A string is a sequence of characters delimited by parenthesis.
 * If a string is too long to be conveniently placed on a single line, it may
 * be split across multiple lines by using the backslash character (\) at the
 * end of a line to indicate that the string continues on the following line.
 * Within a string, the backslash character is used as an escape to specify
 * unbalanced parenthesis, non-printing ASCII characters, and the backslash
 * character itself. Use of the \<I>ddd</I> escape sequence is the preferred
 * way to represent characters outside the printable ASCII character set.<BR>
 * This object is described in the 'Portable Document Format Reference Manual
 * version 1.7' section 3.2.3 (page 53-56).
 *
 * @see PdfObject
 * @see BadPdfFormatException
 */
public class PdfString extends PdfObject {
    
    // CLASS VARIABLES
    
    /** The value of this object. */
    protected String value = NOTHING;
    
    protected String originalValue = null;
    
    /** The encoding. */
    protected String encoding = TEXT_PDFDOCENCODING;
    
    protected int objNum = 0;
    
    protected int objGen = 0;
    
    protected boolean hexWriting = false;

    // CONSTRUCTORS
    
    /**
     * Constructs an empty <CODE>PdfString</CODE>-object.
     */
    public PdfString() {
        super(STRING);
    }
    
    /**
     * Constructs a <CODE>PdfString</CODE>-object containing a string in the
     * standard encoding <CODE>TEXT_PDFDOCENCODING</CODE>.
     *
     * @param value    the content of the string
     */
    public PdfString(String value) {
        super(STRING);
        this.value = value;
    }
    
    /**
     * Constructs a <CODE>PdfString</CODE>-object containing a string in the
     * specified encoding.
     *
     * @param value    the content of the string
     * @param encoding an encoding
     */
    public PdfString(String value, String encoding) {
        super(STRING);
        this.value = value;
        this.encoding = encoding;
    }
    
    /**
     * Constructs a <CODE>PdfString</CODE>-object.
     *
     * @param bytes    an array of <CODE>byte</CODE>
     */
    public PdfString(byte[] bytes) {
        super(STRING);
        value = PdfEncodings.convertToString(bytes, null);
        encoding = NOTHING;
    }
    
    // methods overriding some methods in PdfObject
    
    /**
     * Writes the PDF representation of this <CODE>PdfString</CODE> as an array
     * of <CODE>byte</CODE> to the specified <CODE>OutputStream</CODE>.
     * 
     * @param writer for backwards compatibility
     * @param os The <CODE>OutputStream</CODE> to write the bytes to.
     */
    public void toPdf(PdfWriter writer, OutputStream os) throws IOException {
        byte[] b = getBytes();
        PdfEncryption crypto = null;
        if (writer != null)
            crypto = writer.getEncryption();
        if (crypto != null && !crypto.isEmbeddedFilesOnly())
            b = crypto.encryptByteArray(b);
        if (hexWriting) {
            ByteBuffer buf = new ByteBuffer();
            buf.append('<');
            int len = b.length;
            for (byte b1 : b) buf.appendHex(b1);
            buf.append('>');
            os.write(buf.toByteArray());
        }
        else
            os.write(PdfContentByte.escapeString(b));
    }
    
    /**
     * Returns the <CODE>String</CODE> value of this <CODE>PdfString</CODE>-object.
     *
     * @return A <CODE>String</CODE>
     */
    public String toString() {
        return value;
    }
    
    public byte[] getBytes() {
        if (bytes == null) {
            if (encoding != null && encoding.equals(TEXT_UNICODE) && PdfEncodings.isPdfDocEncoding(value))
                bytes = PdfEncodings.convertToBytes(value, TEXT_PDFDOCENCODING);
            else
                bytes = PdfEncodings.convertToBytes(value, encoding);
        }
        return bytes;
    }
    
    // other methods
    
    /**
     * Returns the Unicode <CODE>String</CODE> value of this
     * <CODE>PdfString</CODE>-object.
     *
     * @return A <CODE>String</CODE>
     */
    public String toUnicodeString() {
        if (encoding != null && encoding.length() != 0)
            return value;
        getBytes();
        if (bytes.length >= 2 && bytes[0] == (byte)254 && bytes[1] == (byte)255)
            return PdfEncodings.convertToString(bytes, PdfObject.TEXT_UNICODE);
        else
            return PdfEncodings.convertToString(bytes, PdfObject.TEXT_PDFDOCENCODING);
    }
    
    /**
     * Gets the encoding of this string.
     *
     * @return a <CODE>String</CODE>
     */
    public String getEncoding() {
        return encoding;
    }
    
    void setObjNum(int objNum, int objGen) {
        this.objNum = objNum;
        this.objGen = objGen;
    }
    
    /**
     * Decrypt an encrypted <CODE>PdfString</CODE>
     */
    void decrypt(PdfReader reader) {
        PdfEncryption decrypt = reader.getDecrypt();
        if (decrypt != null) {
            originalValue = value;
            decrypt.setHashKey(objNum, objGen);
            bytes = PdfEncodings.convertToBytes(value, null);
            bytes = decrypt.decryptByteArray(bytes);
            value = PdfEncodings.convertToString(bytes, null);
        }
    }
    
    /**
     * @return The original bytes used to create this PDF string, or the bytes of our current value
     *         if the original bytes are missing.
     */
    public byte[] getOriginalBytes() {
        if (originalValue == null)
            return getBytes();
        return PdfEncodings.convertToBytes(originalValue, null);
    }
    
    /**
     * return the characters in our value without any translation. This allows
     * a string to be built that holds 2-byte or one-byte character codes, as needed
     * for processing by fonts when extracting text.
     * 
     * Intended for use when no encoding transformations are desired.
     * @return The code points in this font as chars.
     */
    public char[] getOriginalChars() {
        char[] chars;
        if (encoding == null || encoding.length() == 0) {
            byte [] bytes = getOriginalBytes();
            chars = new char[bytes.length];
            for (int i = 0; i<bytes.length; i++)
                chars[i] = (char) (bytes[i]&0xff);
        } else if(encoding.equals("IDENTITY_H2")){
            //change it to char array according to two byte mapping.
            byte [] bytes = value.getBytes(StandardCharsets.ISO_8859_1);
            chars = new char[bytes.length/2];
            for (int i = 0; i<bytes.length/2; i++)
                chars[i] = (char) (((bytes[2 * i]&255)<<8) + (bytes[2 * i + 1]&255));
        } else if(encoding.equals("IDENTITY_H1")){
            //change it to char array according to one byte mapping.
            byte [] bytes = value.getBytes(StandardCharsets.ISO_8859_1);
            chars = new char[bytes.length];
            for (int i = 0; i<bytes.length; i++)
                chars[i] = (char) (bytes[i]&0xff);
        } else {
            chars = new char[0];
        }
        return chars;
    }
    
    public PdfString setHexWriting(boolean hexWriting) {
        this.hexWriting = hexWriting;
        return this;
    }
    
    public boolean isHexWriting() {
        return hexWriting;
    }
}
