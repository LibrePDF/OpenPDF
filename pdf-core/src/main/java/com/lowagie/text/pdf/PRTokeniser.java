/*
 * $Id: PRTokeniser.java 4083 2009-10-30 21:25:10Z trumpetinc $
 *
 * Copyright 2001, 2002 by Paulo Soares.
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
import com.lowagie.text.exceptions.InvalidPdfException;
import com.lowagie.text.error_messages.MessageLocalization;
/**
 *
 * @author  Paulo Soares (psoares@consiste.pt)
 */
public class PRTokeniser {
    
    public static final int TK_NUMBER = 1;
    public static final int TK_STRING = 2;
    public static final int TK_NAME = 3;
    public static final int TK_COMMENT = 4;
    public static final int TK_START_ARRAY = 5;
    public static final int TK_END_ARRAY = 6;
    public static final int TK_START_DIC = 7;
    public static final int TK_END_DIC = 8;
    public static final int TK_REF = 9;
    public static final int TK_OTHER = 10;
    public static final int TK_ENDOFFILE = 11;
    public static final boolean delims[] = {
        true,  true,  false, false, false, false, false, false, false, false,
        true,  true,  false, true,  true,  false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, true,  false, false, false, false, true,  false,
        false, true,  true,  false, false, false, false, false, true,  false,
        false, false, false, false, false, false, false, false, false, false,
        false, true,  false, true,  false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, true,  false, true,  false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false};
    
    static final String EMPTY = "";

    
    protected RandomAccessFileOrArray file;
    protected int type;
    protected String stringValue;
    protected int reference;
    protected int generation;
    protected boolean hexString;
       
    public PRTokeniser(String filename) throws IOException {
        file = new RandomAccessFileOrArray(filename);
    }

    public PRTokeniser(byte pdfIn[]) {
        file = new RandomAccessFileOrArray(pdfIn);
    }
    
    public PRTokeniser(RandomAccessFileOrArray file) {
        this.file = file;
    }
    
    public void seek(int pos) throws IOException {
        file.seek(pos);
    }
    
    public int getFilePointer() throws IOException {
        return file.getFilePointer();
    }

    public void close() throws IOException {
        file.close();
    }
    
    public int length() throws IOException {
        return file.length();
    }

    public int read() throws IOException {
        return file.read();
    }
    
    public RandomAccessFileOrArray getSafeFile() {
        return new RandomAccessFileOrArray(file);
    }
    
    public RandomAccessFileOrArray getFile() {
        return file;
    }
    
    public String readString(int size) throws IOException {
        StringBuffer buf = new StringBuffer();
        int ch;
        while ((size--) > 0) {
            ch = file.read();
            if (ch == -1)
                break;
            buf.append((char)ch);
        }
        return buf.toString();
    }

    public static final boolean isWhitespace(int ch) {
        return (ch == 0 || ch == 9 || ch == 10 || ch == 12 || ch == 13 || ch == 32);
    }
    
    public static final boolean isDelimiter(int ch) {
        return (ch == '(' || ch == ')' || ch == '<' || ch == '>' || ch == '[' || ch == ']' || ch == '/' || ch == '%');
    }

    public static final boolean isDelimiterWhitespace(int ch) {
        return delims[ch + 1];
    }

    public int getTokenType() {
        return type;
    }
    
    public String getStringValue() {
        return stringValue;
    }
    
    public int getReference() {
        return reference;
    }
    
    public int getGeneration() {
        return generation;
    }
    
    public void backOnePosition(int ch) {
        if (ch != -1)
            file.pushBack((byte)ch);
    }
    
    public void throwError(String error) throws IOException {
        throw new InvalidPdfException(MessageLocalization.getComposedMessage("1.at.file.pointer.2", error, String.valueOf(file.getFilePointer())));
    }
    
    public char checkPdfHeader() throws IOException {
        file.setStartOffset(0);
        String str = readString(1024);
        int idx = str.indexOf("%PDF-");
        if (idx < 0)
            throw new InvalidPdfException(MessageLocalization.getComposedMessage("pdf.header.not.found"));
        file.setStartOffset(idx);
        return str.charAt(idx + 7);
    }
    
    public void checkFdfHeader() throws IOException {
        file.setStartOffset(0);
        String str = readString(1024);
        int idx = str.indexOf("%FDF-1.2");
        if (idx < 0)
            throw new InvalidPdfException(MessageLocalization.getComposedMessage("fdf.header.not.found"));
        file.setStartOffset(idx);
    }

    public int getStartxref() throws IOException {
        int size = Math.min(1024, file.length());
        int pos = file.length() - size;
        file.seek(pos);
        String str = readString(1024);
        int idx = str.lastIndexOf("startxref");
        if (idx < 0)
            throw new InvalidPdfException(MessageLocalization.getComposedMessage("pdf.startxref.not.found"));
        return pos + idx;
    }

    public static int getHex(int v) {
        if (v >= '0' && v <= '9')
            return v - '0';
        if (v >= 'A' && v <= 'F')
            return v - 'A' + 10;
        if (v >= 'a' && v <= 'f')
            return v - 'a' + 10;
        return -1;
    }
    
    public void nextValidToken() throws IOException {
        int level = 0;
        String n1 = null;
        String n2 = null;
        int ptr = 0;
        while (nextToken()) {
            if (type == TK_COMMENT)
                continue;
            switch (level) {
                case 0:
                {
                    if (type != TK_NUMBER)
                        return;
                    ptr = file.getFilePointer();
                    n1 = stringValue;
                    ++level;
                    break;
                }
                case 1:
                {
                    if (type != TK_NUMBER) {
                        file.seek(ptr);
                        type = TK_NUMBER;
                        stringValue = n1;
                        return;
                    }
                    n2 = stringValue;
                    ++level;
                    break;
                }
                default:
                {
                    if (type != TK_OTHER || !stringValue.equals("R")) {
                        file.seek(ptr);
                        type = TK_NUMBER;
                        stringValue = n1;
                        return;
                    }
                    type = TK_REF;
                    reference = Integer.parseInt(n1);
                    generation = Integer.parseInt(n2);
                    return;
                }
            }
        }
        // if we hit here, the file is either corrupt (stream ended unexpectedly),
        // or the last token ended exactly at the end of a stream.  This last
        // case can occur inside an Object Stream.
    }
    
    public boolean nextToken() throws IOException {
        int ch = 0;
        do {
            ch = file.read();
        } while (ch != -1 && isWhitespace(ch));
        if (ch == -1){
            type = TK_ENDOFFILE;
            return false;
        }

        // Note:  We have to initialize stringValue here, after we've looked for the end of the stream,
        // to ensure that we don't lose the value of a token that might end exactly at the end
        // of the stream
        StringBuffer outBuf = null;
        stringValue = EMPTY;

        switch (ch) {
            case '[':
                type = TK_START_ARRAY;
                break;
            case ']':
                type = TK_END_ARRAY;
                break;
            case '/':
            {
                outBuf = new StringBuffer();
                type = TK_NAME;
                while (true) {
                    ch = file.read();
                    if (delims[ch + 1])
                        break;
                    if (ch == '#') {
                        ch = (getHex(file.read()) << 4) + getHex(file.read());
                    }
                    outBuf.append((char)ch);
                }
                backOnePosition(ch);
                break;
            }
            case '>':
                ch = file.read();
                if (ch != '>')
                    throwError(MessageLocalization.getComposedMessage("greaterthan.not.expected"));
                type = TK_END_DIC;
                break;
            case '<':
            {
                int v1 = file.read();
                if (v1 == '<') {
                    type = TK_START_DIC;
                    break;
                }
                outBuf = new StringBuffer();
                type = TK_STRING;
                hexString = true;
                int v2 = 0;
                while (true) {
                    while (isWhitespace(v1))
                        v1 = file.read();
                    if (v1 == '>')
                        break;
                    v1 = getHex(v1);
                    if (v1 < 0)
                        break;
                    v2 = file.read();
                    while (isWhitespace(v2))
                        v2 = file.read();
                    if (v2 == '>') {
                        ch = v1 << 4;
                        outBuf.append((char)ch);
                        break;
                    }
                    v2 = getHex(v2);
                    if (v2 < 0)
                        break;
                    ch = (v1 << 4) + v2;
                    outBuf.append((char)ch);
                    v1 = file.read();
                }
                if (v1 < 0 || v2 < 0)
                    throwError(MessageLocalization.getComposedMessage("error.reading.string"));
                break;
            }
            case '%':
                type = TK_COMMENT;
                do {
                    ch = file.read();
                } while (ch != -1 && ch != '\r' && ch != '\n');
                break;
            case '(':
            {
                outBuf = new StringBuffer();
                type = TK_STRING;
                hexString = false;
                int nesting = 0;
                while (true) {
                    ch = file.read();
                    if (ch == -1)
                        break;
                    if (ch == '(') {
                        ++nesting;
                    }
                    else if (ch == ')') {
                        --nesting;
                    }
                    else if (ch == '\\') {
                        boolean lineBreak = false;
                        ch = file.read();
                        switch (ch) {
                            case 'n':
                                ch = '\n';
                                break;
                            case 'r':
                                ch = '\r';
                                break;
                            case 't':
                                ch = '\t';
                                break;
                            case 'b':
                                ch = '\b';
                                break;
                            case 'f':
                                ch = '\f';
                                break;
                            case '(':
                            case ')':
                            case '\\':
                                break;
                            case '\r':
                                lineBreak = true;
                                ch = file.read();
                                if (ch != '\n')
                                    backOnePosition(ch);
                                break;
                            case '\n':
                                lineBreak = true;
                                break;
                            default:
                            {
                                if (ch < '0' || ch > '7') {
                                    break;
                                }
                                int octal = ch - '0';
                                ch = file.read();
                                if (ch < '0' || ch > '7') {
                                    backOnePosition(ch);
                                    ch = octal;
                                    break;
                                }
                                octal = (octal << 3) + ch - '0';
                                ch = file.read();
                                if (ch < '0' || ch > '7') {
                                    backOnePosition(ch);
                                    ch = octal;
                                    break;
                                }
                                octal = (octal << 3) + ch - '0';
                                ch = octal & 0xff;
                                break;
                            }
                        }
                        if (lineBreak)
                            continue;
                        if (ch < 0)
                            break;
                    }
                    else if (ch == '\r') {
                        ch = file.read();
                        if (ch < 0)
                            break;
                        if (ch != '\n') {
                            backOnePosition(ch);
                            ch = '\n';
                        }
                    }
                    if (nesting == -1)
                        break;
                    outBuf.append((char)ch);
                }
                if (ch == -1)
                    throwError(MessageLocalization.getComposedMessage("error.reading.string"));
                break;
            }
            default:
            {
                outBuf = new StringBuffer();
                if (ch == '-' || ch == '+' || ch == '.' || (ch >= '0' && ch <= '9')) {
                    type = TK_NUMBER;
                    do {
                        outBuf.append((char)ch);
                        ch = file.read();
                    } while (ch != -1 && ((ch >= '0' && ch <= '9') || ch == '.'));
                }
                else {
                    type = TK_OTHER;
                    do {
                        outBuf.append((char)ch);
                        ch = file.read();
                    } while (!delims[ch + 1]);
                }
                backOnePosition(ch);
                break;
            }
        }
        if (outBuf != null)
            stringValue = outBuf.toString();
        return true;
    }
    
    public int intValue() {
        return Integer.parseInt(stringValue);
    }
    
    public boolean readLineSegment(byte input[]) throws IOException {
        int c = -1;
        boolean eol = false;
        int ptr = 0;
        int len = input.length;
	// ssteward, pdftk-1.10, 040922: 
	// skip initial whitespace; added this because PdfReader.rebuildXref()
	// assumes that line provided by readLineSegment does not have init. whitespace;
	if ( ptr < len ) {
	    while ( isWhitespace( (c = read()) ) );
	}
	while ( !eol && ptr < len ) {
	    switch (c) {
                case -1:
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    int cur = getFilePointer();
                    if ((read()) != '\n') {
                        seek(cur);
                    }
                    break;
                default:
                    input[ptr++] = (byte)c;
                    break;
            }

	    // break loop? do it before we read() again
	    if( eol || len <= ptr ) {
		break;
	    }
	    else {
		c = read();
	    }
        }
        if (ptr >= len) {
            eol = false;
            while (!eol) {
                switch (c = read()) {
                    case -1:
                    case '\n':
                        eol = true;
                        break;
                    case '\r':
                        eol = true;
                        int cur = getFilePointer();
                        if ((read()) != '\n') {
                            seek(cur);
                        }
                        break;
                }
            }
        }
        
        if ((c == -1) && (ptr == 0)) {
            return false;
        }
        if (ptr + 2 <= len) {
            input[ptr++] = (byte)' ';
            input[ptr] = (byte)'X';
        }
        return true;
    }
    
    public static int[] checkObjectStart(byte line[]) {
        try {
            PRTokeniser tk = new PRTokeniser(line);
            int num = 0;
            int gen = 0;
            if (!tk.nextToken() || tk.getTokenType() != TK_NUMBER)
                return null;
            num = tk.intValue();
            if (!tk.nextToken() || tk.getTokenType() != TK_NUMBER)
                return null;
            gen = tk.intValue();
            if (!tk.nextToken())
                return null;
            if (!tk.getStringValue().equals("obj"))
                return null;
            return new int[]{num, gen};
        }
        catch (Exception ioe) {
            // empty on purpose
        }
        return null;
    }
    
    public boolean isHexString() {
        return this.hexString;
    }
    
}
