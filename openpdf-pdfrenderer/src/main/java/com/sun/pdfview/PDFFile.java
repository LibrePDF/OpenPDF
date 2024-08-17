/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
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
package com.sun.pdfview;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.sun.pdfview.action.GoToAction;
import com.sun.pdfview.action.PDFAction;
import com.sun.pdfview.annotation.PDFAnnotation;
import com.sun.pdfview.decrypt.EncryptionUnsupportedByPlatformException;
import com.sun.pdfview.decrypt.EncryptionUnsupportedByProductException;
import com.sun.pdfview.decrypt.IdentityDecrypter;
import com.sun.pdfview.decrypt.PDFAuthenticationFailureException;
import com.sun.pdfview.decrypt.PDFDecrypter;
import com.sun.pdfview.decrypt.PDFDecrypterFactory;
import com.sun.pdfview.decrypt.PDFPassword;
import com.sun.pdfview.decrypt.UnsupportedEncryptionException;

/**
 * An encapsulation of a .pdf file.  The methods of this class
 * can parse the contents of a PDF file, but those methods are
 * hidden.  Instead, the public methods of this class allow
 * access to the pages in the PDF file.  Typically, you create
 * a new PDFFile, ask it for the number of pages, and then
 * request one or more PDFPages.
 * @author Mike Wessler
 */
public class PDFFile {

    public final static int             NUL_CHAR = 0;
    public final static int             FF_CHAR = 12;

    private String versionString = "1.1";
    private int majorVersion = 1;
    private int minorVersion = 1;
    /** the end of line character */
    /** the comment text to begin the file to determine it's version */
    private final static String VERSION_COMMENT = "%PDF-";
    /**
     * A ByteBuffer containing the file data
     */
    ByteBuffer buf;
    /**
     * the cross reference table mapping object numbers to locations
     * in the PDF file
     */
    PDFXref[] objIdx;
    /** the root PDFObject, as specified in the PDF file */
    PDFObject root = null;
    /** the Encrypt PDFObject, from the trailer */
    PDFObject encrypt = null;

    /** The Info PDFPbject, from the trailer, for simple metadata */
    PDFObject info = null;

    /** a mapping of page numbers to parsed PDF commands */
    Cache cache;
    /**
     * whether the file is printable or not (trailer -> Encrypt -> P & 0x4)
     */
    private boolean printable = true;
    /**
     * whether the file is saveable or not (trailer -> Encrypt -> P & 0x10)
     */
    private boolean saveable = true;

    /**
     * The default decrypter for streams and strings. By default, no
     * encryption is expected, and thus the IdentityDecrypter is used.
     */
    private PDFDecrypter defaultDecrypter = IdentityDecrypter.getInstance();

    /**
     * get a PDFFile from a .pdf file.  The file must me a random access file
     * at the moment.  It should really be a file mapping from the nio package.
     * <p>
     * Use the getPage(...) methods to get a page from the PDF file.
     * @param buf the RandomAccessFile containing the PDF.
     * @throws IOException if there's a problem reading from the buffer
     * @throws PDFParseException if the document appears to be malformed, or
     *  its features are unsupported. If the file is encrypted in a manner that
     *  the product or platform does not support then the exception's {@link
     *  PDFParseException#getCause() cause} will be an instance of {@link
     *  UnsupportedEncryptionException}.
     * @throws PDFAuthenticationFailureException if the file is password
     *  protected and requires a password
     */
    public PDFFile(ByteBuffer buf) throws IOException {
	this(buf, null);
    }

    public PDFFile(ByteBuffer buf, boolean doNotParse) throws IOException {
    	this.buf = buf;
    }
    
    /**
     * get a PDFFile from a .pdf file.  The file must me a random access file
     * at the moment.  It should really be a file mapping from the nio package.
     * <p>
     * Use the getPage(...) methods to get a page from the PDF file.
     * @param buf the RandomAccessFile containing the PDF.
     * @param password the user or owner password
     * @throws IOException if there's a problem reading from the buffer
     * @throws PDFParseException if the document appears to be malformed, or
     *  its features are unsupported. If the file is encrypted in a manner that
     *  the product or platform does not support then the exception's {@link
     *  PDFParseException#getCause() cause} will be an instance of {@link
     *  UnsupportedEncryptionException}.
     * @throws PDFAuthenticationFailureException if the file is password
     *  protected and the supplied password does not decrypt the document
     */
    public PDFFile(ByteBuffer buf, PDFPassword password) throws IOException {
        this.buf = buf;

        this.cache = new Cache();

        parseFile(password);
    }

    /**
     * Gets whether the owner of the file has given permission to print
     * the file.
     * @return true if it is okay to print the file
     */
    public boolean isPrintable() {
        return this.printable;
    }

    /**
     * Gets whether the owner of the file has given permission to save
     * a copy of the file.
     * @return true if it is okay to save the file
     */
    public boolean isSaveable() {
        return this.saveable;
    }

    /**
     * get the root PDFObject of this PDFFile.  You generally shouldn't need
     * this, but we've left it open in case you want to go spelunking.
     */
    public PDFObject getRoot() {
        return this.root;
    }

    /**
     * return the number of pages in this PDFFile.  The pages will be
     * numbered from 1 to getNumPages(), inclusive.
     */
    public int getNumPages() {
        try {
            return this.root.getDictRef("Pages").getDictRef("Count").getIntValue();
        } catch (Exception ioe) {
            return 0;
        }
    }

    /**
     * Get metadata (e.g., Author, Title, Creator) from the Info dictionary
     * as a string.
     * @param name the name of the metadata key (e.g., Author)
     * @return the info
     * @throws IOException if the metadata cannot be read
     */
    public String getStringMetadata(String name)
            throws IOException {
        if (this.info != null) {
            final PDFObject meta = this.info.getDictRef(name);
            return meta != null ? meta.getTextStringValue() : null;
        } else {
            return null;
        }
    }

    /**
     * Get the keys into the Info metadata, for use with
     * {@link #getStringMetadata(String)}
     * @return the keys present into the Info dictionary
     * @throws IOException if the keys cannot be read
     */
    public Iterator<String> getMetadataKeys()
            throws IOException {
        if (this.info != null) {
            return this.info.getDictKeys();
        } else {
            return Collections.<String>emptyList().iterator();
        }
    }


    /**
     * Used internally to track down PDFObject references.  You should never
     * need to call this.
     * <p>
     * Since this is the only public method for tracking down PDF objects,
     * it is synchronized.  This means that the PDFFile can only hunt down
     * one object at a time, preventing the file's location from getting
     * messed around.
     * <p>
     * This call stores the current buffer position before any changes are made
     * and restores it afterwards, so callers need not know that the position
     * has changed.
     *
     */
    public synchronized PDFObject dereference(PDFXref ref, PDFDecrypter decrypter)
            throws IOException {
        int id = ref.getID();

        // make sure the id is valid and has been read
        if (id >= this.objIdx.length || this.objIdx[id] == null) {
            return PDFObject.nullObj;
        }

        // check to see if this is already dereferenced
        PDFObject obj = this.objIdx[id].getObject();
        if (obj != null) {
            return obj;
        }

        // store the current position in the buffer
        int startPos = this.buf.position();

        boolean compressed = this.objIdx[id].getCompressed();
        if (!compressed) {
	        int loc = this.objIdx[id].getFilePos();
	        if (loc < 0) {
	            return PDFObject.nullObj;
	        }
	
	        // move to where this object is
	        this.buf.position(loc);
	
	        // read the object and cache the reference
	        obj= readObject(ref.getID(), ref.getGeneration(), decrypter);
        }
        else { // compressed
	        int compId = this.objIdx[id].getID();
	        int idx = this.objIdx[id].getIndex();
	        if (idx < 0)
	            return PDFObject.nullObj;
	        PDFXref compRef = new PDFXref(compId, 0);
	        PDFObject compObj = dereference(compRef, decrypter);
	        int first = compObj.getDictionary().get("First").getIntValue();
	        int length = compObj.getDictionary().get("Length").getIntValue();
	        int n = compObj.getDictionary().get("N").getIntValue();
	        if (idx >= n)
	            return PDFObject.nullObj;
	        ByteBuffer strm = compObj.getStreamBuffer();
	        
        	ByteBuffer oldBuf = this.buf;
        	this.buf = strm;
	        // skip other nums
	        for (int i=0; i<idx; i++) {
	        	PDFObject skip1num= readObject(-1, -1, true, IdentityDecrypter.getInstance());
	        	PDFObject skip2num= readObject(-1, -1, true, IdentityDecrypter.getInstance());
	        }
        	PDFObject objNumPO= readObject(-1, -1, true, IdentityDecrypter.getInstance());
        	PDFObject offsetPO= readObject(-1, -1, true, IdentityDecrypter.getInstance());
        	int objNum = objNumPO.getIntValue();
        	int offset = offsetPO.getIntValue();
        	if (objNum != id)
	            return PDFObject.nullObj;
        	
        	this.buf.position(first+offset);
        	obj= readObject(objNum, 0, IdentityDecrypter.getInstance());
        	this.buf = oldBuf;
        }
        
        if (obj == null) {
            obj = PDFObject.nullObj;
        }

        this.objIdx[id].setObject(obj);

        // reset to the previous position
        this.buf.position(startPos);

        return obj;
    }

    /**
     * Is the argument a white space character according to the PDF spec?.
     * ISO Spec 32000-1:2008 - Table 1
     */
    public static boolean isWhiteSpace(int c) {
        if (c == ' ' || c == NUL_CHAR || c == '\t' || c == '\n' || c == '\r' || c == FF_CHAR) return true;
        return false;
    	/*switch (c) { 
            case NUL_CHAR:  // Null (NULL)
            case '\t':      // Horizontal Tab (HT)
            case '\n':      // Line Feed (LF)
            case FF_CHAR:   // Form Feed (FF)
            case '\r':      // Carriage Return (CR)
            case ' ':       // Space (SP)
                return true;
            default:
                return false;
        }*/
    }

    /**
     * Is the argument a delimiter according to the PDF spec?<p>
     *
     * ISO 32000-1:2008 - Table 2
     *
     * @param c the character to test
     */
    public static boolean isDelimiter(int c) {
        switch (c) {
            case '(':   // LEFT PARENTHESIS
            case ')':   // RIGHT PARENTHESIS
            case '<':   // LESS-THAN-SIGN
            case '>':   // GREATER-THAN-SIGN
            case '[':   // LEFT SQUARE BRACKET
            case ']':   // RIGHT SQUARE BRACKET
            case '{':   // LEFT CURLY BRACKET
            case '}':   // RIGHT CURLY BRACKET
            case '/':   // SOLIDUS
            case '%':   // PERCENT SIGN
                return true;
            default:
                return false;
        }
    }

    /**
     * return true if the character is neither a whitespace or a delimiter.
     *
     * @param c the character to test
     * @return boolean
     */
    public static boolean isRegularCharacter (int c) {
        return !(isWhiteSpace(c) || isDelimiter(c));
    }

    /**
     * read the next object from the file
     * @param objNum the object number of the object containing the object
     *  being read; negative only if the object number is unavailable (e.g., if
     *  reading from the trailer, or reading at the top level, in which
     *  case we can expect to be reading an object description)
     * @param objGen the object generation of the object containing the object
     *  being read; negative only if the objNum is unavailable
     * @param decrypter the decrypter to use
     */
    private PDFObject readObject(
            int objNum, int objGen, PDFDecrypter decrypter) throws IOException {
	return readObject(objNum, objGen, false, decrypter);
    }

    /**
     * read the next object with a special catch for numbers
     * @param numscan if true, don't bother trying to see if a number is
     *  an object reference (used when already in the middle of testing for
     *  an object reference, and not otherwise)
     * @param objNum the object number of the object containing the object
     *  being read; negative only if the object number is unavailable (e.g., if
     *  reading from the trailer, or reading at the top level, in which
     *  case we can expect to be reading an object description)
     * @param objGen the object generation of the object containing the object
     *  being read; negative only if the objNum is unavailable
     * @param decrypter the decrypter to use
     */
    private PDFObject readObject(
            int objNum, int objGen,
            boolean numscan, PDFDecrypter decrypter) throws IOException {
        // skip whitespace
        int c;
        PDFObject obj = null;
        while (obj == null && this.buf.hasRemaining()) {
            while (isWhiteSpace(c = this.buf.get())) {
            	if(!buf.hasRemaining()) {
            		break;
            	}
            }
            // check character for special punctuation:
            if (c == '<') {
                // could be start of <hex data>, or start of <<dictionary>>
                c = this.buf.get();
                if (c == '<') {
                    // it's a dictionary
		    obj= readDictionary(objNum, objGen, decrypter);
                } else {
                    this.buf.position(this.buf.position() - 1);
		    obj= readHexString(objNum, objGen, decrypter);
                }
            } else if (c == '(') {
		obj= readLiteralString(objNum, objGen, decrypter);
            } else if (c == '[') {
                // it's an array
		obj= readArray(objNum, objGen, decrypter);
            } else if (c == '/') {
                // it's a name
                obj = readName();
            } else if (c == '%') {
                // it's a comment
                readLine();
            } else if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.') {
                // it's a number
                obj = readNumber((char) c);
                if (!numscan) {
                    // It could be the start of a reference.
                    // Check to see if there's another number, then "R".
                    //
                    // We can't use mark/reset, since this could be called
                    // from dereference, which already is using a mark
                    int startPos = this.buf.position();

		    PDFObject testnum= readObject(-1, -1, true, decrypter);
                    if (testnum != null &&
                            testnum.getType() == PDFObject.NUMBER) {
			PDFObject testR= readObject(-1, -1, true, decrypter);
                        if (testR != null &&
                                testR.getType() == PDFObject.KEYWORD &&
                                testR.getStringValue().equals("R")) {
                            // yup.  it's a reference.
                            PDFXref xref = new PDFXref(obj.getIntValue(),
                                    testnum.getIntValue());
                            // Create a placeholder that will be dereferenced
                            // as needed
                            obj = new PDFObject(this, xref);
                        } else if (testR != null &&
                                testR.getType() == PDFObject.KEYWORD &&
                                testR.getStringValue().equals("obj")) {
                            // it's an object description
			    obj= readObjectDescription(
                                    obj.getIntValue(),
                                    testnum.getIntValue(),
                                    decrypter);
                        } else {
                            this.buf.position(startPos);
                        }
                    } else {
                        this.buf.position(startPos);
                    }
                }
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                // it's a keyword
                obj = readKeyword((char) c);
            } else {
                // it's probably a closing character.
                // throwback
                this.buf.position(this.buf.position() - 1);
                break;
            }
        }
        return obj;
    }

    /**
     * Get the next non-white space character
     * @param buf the buffer to read from
     * @return the next non-whitespace character
     */
    private int nextNonWhitespaceChar(ByteBuffer buf) {
        int c;
        while (isWhiteSpace(c = buf.get())) {
            // nothing
        }
        return c;
    }

    /**
     * Consume all sequential whitespace from the current buffer position,
     * leaving the buffer positioned at non-whitespace
     * @param buf the buffer to read from
     */
    private void consumeWhitespace(ByteBuffer buf) {
        nextNonWhitespaceChar(buf);
        buf.position(buf.position() - 1);
    }

    /**
     * requires the next few characters (after whitespace) to match the
     * argument.
     * @param match the next few characters after any whitespace that
     * must be in the file
     * @return true if the next characters match; false otherwise.
     */
    private boolean nextItemIs(String match) throws IOException {
        // skip whitespace
        int c = nextNonWhitespaceChar(buf);
        for (int i = 0; i < match.length(); i++) {
            if (i > 0) {
                c = this.buf.get();
            }
            if (c != match.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * process a version string, to determine the major and minor versions
     * of the file.
     * 
     * @param versionString
     */
    private void processVersion(String versionString) {
        try {
            StringTokenizer tokens = new StringTokenizer(versionString, ".");
            this.majorVersion = Integer.parseInt(tokens.nextToken());
            this.minorVersion = Integer.parseInt(tokens.nextToken());
            this.versionString = versionString;
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * return the major version of the PDF header.
     * 
     * @return int
     */
    public int getMajorVersion() {
        return this.majorVersion;
    }

    /**
     * return the minor version of the PDF header.
     * 
     * @return int
     */
    public int getMinorVersion() {
        return this.minorVersion;
    }

    /**
     * return the version string from the PDF header.
     * 
     * @return String
     */
    public String getVersionString() {
        return this.versionString;
    }

    /**
     * read an entire &lt;&lt; dictionary &gt;&gt;.  The initial
     * &lt;&lt; has already been read.
     * @param objNum the object number of the object containing the dictionary
     *  being read; negative only if the object number is unavailable, which
     *  should only happen if we're reading a dictionary placed directly
     *  in the trailer
     * @param objGen the object generation of the object containing the object
     *  being read; negative only if the objNum is unavailable
     * @param decrypter the decrypter to use
     * @return the Dictionary as a PDFObject.
     */
    private PDFObject readDictionary(
            int objNum, int objGen, PDFDecrypter decrypter) throws IOException {
        HashMap<String,PDFObject> hm = new HashMap<String,PDFObject>();
        // we've already read the <<.  Now get /Name obj pairs until >>
        PDFObject name;
	while ((name= readObject(objNum, objGen, decrypter))!=null) {
            // make sure first item is a NAME
            if (name.getType() != PDFObject.NAME) {
                throw new PDFParseException("First item in dictionary must be a /Name.  (Was " + name + ")");
            }
	    PDFObject value= readObject(objNum, objGen, decrypter);
            if (value != null) {
                hm.put(name.getStringValue(), value);
            }
        }
        if (!nextItemIs(">>")) {
            throw new PDFParseException("End of dictionary wasn't '>>'");
        }
        return new PDFObject(this, PDFObject.DICTIONARY, hm);
    }

    /**
     * read a character, and return its value as if it were a hexidecimal
     * digit.
     * @return a number between 0 and 15 whose value matches the next
     * hexidecimal character.  Returns -1 if the next character isn't in
     * [0-9a-fA-F]
     */
    private int readHexDigit() throws IOException {
        int a;
        while (isWhiteSpace(a = this.buf.get())) {
        }
        switch (a) {
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                a -= '0';
                break;
            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                a -= 'a' - 10;
                break;
            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                a -= 'A' - 10;
                break;
            default:
                a = -1;
                break;
        }
        return a;
    }

    /**
     * return the 8-bit value represented by the next two hex characters.
     * If the next two characters don't represent a hex value, return -1
     * and reset the read head.  If there is only one hex character,
     * return its value as if there were an implicit 0 after it.
     */
    private int readHexPair() throws IOException {
        int first = readHexDigit();
        if (first < 0) {
            this.buf.position(this.buf.position() - 1);
            return -1;
        }
        int second = readHexDigit();
        if (second < 0) {
            this.buf.position(this.buf.position() - 1);
            return (first << 4);
        } else {
            return (first << 4) + second;
        }
    }

    /**
     * read a < hex string >.  The initial < has already been read.
     * @param objNum the object number of the object containing the dictionary
     *  being read; negative only if the object number is unavailable, which
     *  should only happen if we're reading a string placed directly
     *  in the trailer
     * @param objGen the object generation of the object containing the object
     *  being read; negative only if the objNum is unavailable
     * @param decrypter the decrypter to use
     */
    private PDFObject readHexString(
            int objNum, int objGen, PDFDecrypter decrypter) throws IOException {
        // we've already read the <. Now get the hex bytes until >
        int val;
        StringBuffer sb = new StringBuffer();
        while ((val = readHexPair()) >= 0) {
            sb.append((char) val);
        }
        if (buf.hasRemaining() && this.buf.get() != '>') {
            throw new PDFParseException("Bad character in Hex String");
        }
        return new PDFObject(this, PDFObject.STRING,
                decrypter.decryptString(objNum, objGen, sb.toString()));
    }

    /**
     * <p>read a ( character string ).  The initial ( has already been read.
     * Read until a *balanced* ) appears.</p>
     *
     * <p>Section 3.2.3 of PDF Refernce version 1.7 defines the format of
     * String objects. Regarding literal strings:</p>
     *
     * <blockquote>Within a literal string, the backslash (\) is used as an
     * escape character for various purposes, such as to include newline
     * characters, nonprinting ASCII characters, unbalanced parentheses, or
     * the backslash character itself in the string. The character
     * immediately following the backslash determines its precise
     * interpretation (see Table 3.2). If the character following the
     * backslash is not one of those shown in the table, the backslash
     * is ignored.</blockquote>
     *
     * * <p>This only reads 8 bit basic character 'strings' so as to avoid a
     * text string interpretation when one is not desired (e.g., for byte
     * strings, as used by the decryption mechanism). For an interpretation of
     * a string returned from this method, where the object type is defined
     * as a 'text string' as per Section 3.8.1, Table 3.31 "PDF Data Types",
     * {@link PDFStringUtil#asTextString} ()} or
     * {@link PDFObject#getTextStringValue()} must be employed.</p>
     *
     * @param objNum the object number of the object containing the dictionary
     *  being read; negative only if the object number is unavailable, which
     *  should only happen if we're reading a dictionary placed directly
     *  in the trailer
     * @param objGen the object generation of the object containing the object
     *  being read; negative only if the objNum is unavailable
     * @param decrypter the decrypter to use
     */
    private PDFObject readLiteralString(
            int objNum, int objGen, PDFDecrypter decrypter) throws IOException {
        int c;

        // we've already read the (.  now get the characters until a
        // *balanced* ) appears.  Translate \r \n \t \b \f \( \) \\ \ddd
        // if a cr/lf follows a backslash, ignore the cr/lf
        int parencount = 1;
        StringBuffer sb = new StringBuffer();

        while (buf.hasRemaining() && parencount > 0) {
            c = this.buf.get() & 0xFF;
            // process unescaped parenthesis
            if (c == '(') {
                parencount++;
            } else if (c == ')') {
                parencount--;
                if (parencount == 0) {
                    c = -1;
                    break;
                }
            } else if (c == '\\') {

                // From the spec:
                // Within a literal string, the backslash (\) is used as an
                // escape character for various purposes, such as to include
                // newline characters, nonprinting ASCII characters,
                // unbalanced parentheses, or the backslash character itself
                // in the string. The character immediately following the
                // backslash determines its precise interpretation (see
                // Table 3.2). If the character following the backslash is not
                // one of those shown in the table, the backslash is ignored.
                //
                // summary of rules:
                //
                // \n \r \t \b \f 2-char sequences are used to represent their
                //  1-char counterparts
                //
                // \( and \) are used to escape parenthesis
                //
                // \\ for a literal backslash
                //
                // \ddd (1-3 octal digits) for a character code
                //
                //  \<EOL> is used to put formatting newlines into the
                //  file, but aren't actually part of the string; EOL may be
                //  CR, LF or CRLF
                //
                // any other sequence should see the backslash ignored

                // grab the next character to see what we're dealing with
                c = this.buf.get() & 0xFF;
                if (c >= '0' && c < '8') {
                    // \ddd form - one to three OCTAL digits
                    int count = 0;
                    int val = 0;
                    while (c >= '0' && c < '8' && count < 3) {
                        val = val * 8 + c - '0';
                        c = this.buf.get() & 0xFF;
                        count++;
                    }
                    // we'll have read one character too many
                    this.buf.position(this.buf.position() - 1);
                    c = val;
                } else if (c == 'n') {
                    c = '\n';
                } else if (c == 'r') {
                    c = '\r';
                } else if (c == 't') {
                    c = '\t';
                } else if (c == 'b') {
                    c = '\b';
                } else if (c == 'f') {
                    c = '\f';
                } else if (c == '\r') {
                    // escaped CR to be ignored; look for a following LF
                    c = this.buf.get() & 0xFF;
                    if (c != '\n') {
                        // not an LF, we'll consume this character on
                        // the next iteration
                        this.buf.position(this.buf.position() - 1);
                    }
                    c = -1;
                } else if (c == '\n') {
                    // escaped LF to be ignored
                    c = -1;
                }
                // any other c should be used as is, as it's either
                // one of ()\ in which case it should be used literally,
                // or the backslash should just be ignored
            }
            if (c >= 0) {
                sb.append((char) c);
            }
        }
        return new PDFObject(this, PDFObject.STRING,
                decrypter.decryptString(objNum, objGen, sb.toString()));
    }

    /**
     * Read a line of text.  This follows the semantics of readLine() in
     * DataInput -- it reads character by character until a '\n' is
     * encountered.  If a '\r' is encountered, it is discarded.
     */
    private String readLine() {
        StringBuffer sb = new StringBuffer();

        while (this.buf.remaining() > 0) {
            char c = (char) this.buf.get();

            if (c == '\r') {
                if (this.buf.remaining() > 0) {
                    char n = (char) this.buf.get(this.buf.position());
                    if (n == '\n') {
                        this.buf.get();
                    }
                }
                break;
            } else if (c == '\n') {
                break;
            }

            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * read an [ array ].  The initial [ has already been read.  PDFObjects
     * are read until ].
     * @param objNum the object number of the object containing the dictionary
     *  being read; negative only if the object number is unavailable, which
     *  should only happen if we're reading an array placed directly
     *  in the trailer
     * @param objGen the object generation of the object containing the object
     *  being read; negative only if the objNum is unavailable
     * @param decrypter the decrypter to use
     */
    private PDFObject readArray(
            int objNum, int objGen, PDFDecrypter decrypter) throws IOException {
        // we've already read the [.  Now read objects until ]
        ArrayList<PDFObject> ary = new ArrayList<PDFObject>();
        PDFObject obj;
	while((obj= readObject(objNum, objGen, decrypter))!=null) {
            ary.add(obj);
        }
        if (this.buf.hasRemaining() && this.buf.get() != ']') {
            throw new PDFParseException("Array should end with ']'");
        }
        PDFObject[] objlist = new PDFObject[ary.size()];
        for (int i = 0; i < objlist.length; i++) {
            objlist[i] = ary.get(i);
        }
        return new PDFObject(this, PDFObject.ARRAY, objlist);
    }

    /**
     * read a /name.  The / has already been read.
     */
    private PDFObject readName() throws IOException {
        // we've already read the / that begins the name.
        // all we have to check for is #hh hex notations.
        StringBuffer sb = new StringBuffer();
        int c;
        while (this.buf.hasRemaining() && isRegularCharacter(c = this.buf.get())) {
            if (c < '!' && c > '~') {
                break;      // out-of-range, should have been hex
            }
            // H.3.2.4 indicates version 1.1 did not do hex escapes
            if (c == '#' && (this.majorVersion >= 1 && this.minorVersion > 1)) {
                int hex = readHexPair();
                if (hex >= 0) {
                    c = hex;
                } else {
                    throw new PDFParseException("Bad #hex in /Name");
                }
            }
            sb.append((char) c);
        }
        this.buf.position(this.buf.position() - 1);
        return new PDFObject(this, PDFObject.NAME, sb.toString());
    }

    /**
     * read a number.  The initial digit or . or - is passed in as the
     * argument.
     */
    private PDFObject readNumber(char start) throws IOException {
        // we've read the first digit (it's passed in as the argument)
        boolean neg = start == '-';
        boolean sawdot = start == '.';
        double dotmult = sawdot ? 0.1 : 1;
        double value = (start >= '0' && start <= '9') ? start - '0' : 0;
        while (true && this.buf.hasRemaining()) {
            int c = this.buf.get();
            if (c == '.') {
                if (sawdot) {
                    throw new PDFParseException("Can't have two '.' in a number");
                }
                sawdot = true;
                dotmult = 0.1;
            } else if (c >= '0' && c <= '9') {
                int val = c - '0';
                if (sawdot) {
                    value += val * dotmult;
                    dotmult *= 0.1;
                } else {
                    value = value * 10 + val;
                }
            } else {
                this.buf.position(this.buf.position() - 1);
                break;
            }
        }
        if (neg) {
            value = -value;
        }
        return new PDFObject(this, PDFObject.NUMBER, Double.valueOf(value));
    }

    /**
     * read a bare keyword.  The initial character is passed in as the
     * argument.
     */
    private PDFObject readKeyword(char start) throws IOException {
        // we've read the first character (it's passed in as the argument)
        StringBuffer sb = new StringBuffer(String.valueOf(start));
        int c;
        while (buf.hasRemaining() && isRegularCharacter(c = this.buf.get())) {
            sb.append((char) c);
        }
        this.buf.position(this.buf.position() - 1);
        return new PDFObject(this, PDFObject.KEYWORD, sb.toString());
    }

    /**
     * read an entire PDFObject.  The intro line, which looks something
     * like "4 0 obj" has already been read.
     * @param objNum the object number of the object being read, being
     *  the first number in the intro line (4 in "4 0 obj")
     * @param objGen the object generation of the object being read, being
     *  the second number in the intro line (0 in "4 0 obj").
     * @param decrypter the decrypter to use
     */
    private PDFObject readObjectDescription(
            int objNum, int objGen, PDFDecrypter decrypter) throws IOException {
        // we've already read the 4 0 obj bit.  Next thing up is the object.
        // object descriptions end with the keyword endobj
        long debugpos = this.buf.position();
        PDFObject obj = readObject(objNum, objGen, decrypter);
        // see if it's a dictionary.  If so, this could be a stream.
        PDFObject endkey = readObject(objNum, objGen, decrypter);
        if (endkey.getType() != PDFObject.KEYWORD && endkey.getType() != PDFObject.STREAM) {
            PDFDebugger.debug("WARNING: Expected 'stream' or 'endobj' but was " + endkey.getType() + " " + String.valueOf(endkey.getStringValue()));
        }
        if (obj.getType() == PDFObject.DICTIONARY && endkey.getStringValue() != null && endkey.getStringValue().equals("stream")) {
            // skip until we see \n
            readLine();
            ByteBuffer data = readStream(obj);
            if (data == null) {
                data = ByteBuffer.allocate(0);
            }
            obj.setStream(data);
            endkey = readObject(objNum, objGen, decrypter);
        }
        // at this point, obj is the object, keyword should be "endobj"
        String endcheck = endkey.getStringValue();
        if (endcheck == null || !endcheck.equals("endobj")) {
            PDFDebugger.debug("WARNING: object at " + debugpos + " didn't end with 'endobj'");
        }
        obj.setObjectId(objNum, objGen);
        return obj;
    }

    /**
     * read the stream portion of a PDFObject.  Calls decodeStream to
     * un-filter the stream as necessary.
     *
     * @param dict the dictionary associated with this stream.
     * @return a ByteBuffer with the encoded stream data
     */
    private ByteBuffer readStream(PDFObject dict) throws IOException {
        // pointer is at the start of a stream.  read the stream and
        // decode, based on the entries in the dictionary
        PDFObject lengthObj = dict.getDictRef("Length");
        int length = -1;
        if (lengthObj != null) {
            length = lengthObj.getIntValue();
        }
        if (length < 0) {
            throw new PDFParseException("Unknown length for stream");
        }

        // slice the data
        int start = this.buf.position();
        ByteBuffer streamBuf = this.buf.slice();
        streamBuf.limit(length);

        // move the current position to the end of the data
        this.buf.position(this.buf.position() + length);
        int ending = this.buf.position();

        if (!nextItemIs("endstream")) {
            PDFDebugger.debug("read " + length + " chars from " + start + " to " + ending);
            throw new PDFParseException("Stream ended inappropriately");
        }

        return streamBuf;
    // now decode stream
    // return PDFDecoder.decodeStream(dict, streamBuf);
    }

    /**
     * read the cross reference table from a PDF file.  When this method
     * is called, the file pointer must point to the start of the word
     * "xref" in the file.  Reads the xref table and the trailer dictionary.
     * If dictionary has a /Prev entry, move file pointer
     * and read new trailer
     * @param password
     */
    private void readTrailer(PDFPassword password)
            throws
            IOException,
            PDFAuthenticationFailureException,
            EncryptionUnsupportedByProductException,
            EncryptionUnsupportedByPlatformException {
        // the table of xrefs
        this.objIdx = new PDFXref[50];

        int pos = this.buf.position(); 
        
        PDFDecrypter newDefaultDecrypter = null;

        // read a bunch of nested trailer tables
        while (true) {
            // make sure we are looking at an xref table
            if (!nextItemIs("xref")) {
            	this.buf.position(pos);
            	readTrailer15(password);
            	return;
//                throw new PDFParseException("Expected 'xref' at start of table");
            }

            // read a bunch of linked tabled
            while (true) {
                // read until the word "trailer"
		PDFObject obj=readObject(-1, -1, IdentityDecrypter.getInstance());
                if (obj.getType() == PDFObject.KEYWORD &&
                        obj.getStringValue().equals("trailer")) {
                    break;
                }

                // read the starting position of the reference
                if (obj.getType() != PDFObject.NUMBER) {
                    throw new PDFParseException("Expected number for first xref entry");
                }
                int refstart = obj.getIntValue();

                // read the size of the reference table
                obj = readObject(-1, -1, IdentityDecrypter.getInstance());
                if (obj.getType() != PDFObject.NUMBER) {
                    throw new PDFParseException("Expected number for length of xref table");
                }
                int reflen = obj.getIntValue();

                // skip a line
                readLine();

                if (refstart == 1) {// Check and try to fix incorrect Object Number Start
                    int startPos = this.buf.position();
                    try {
                        byte[] refline = new byte[20];
                        this.buf.get(refline);
                        if (refline[17] == 'f') {// free
                            PDFXref objIndex = new PDFXref(refline);
                            if (objIndex.getID() == 0 && objIndex.getGeneration() == 65535) { // The highest generation number possible
                                refstart--;
                            }
                        }
                    } catch (Exception e) {// in case of error ignore
                    }
                    this.buf.position(startPos);
                }

                // extend the objIdx table, if necessary
                if (refstart + reflen >= this.objIdx.length) {
                    PDFXref nobjIdx[] = new PDFXref[refstart + reflen];
                    System.arraycopy(this.objIdx, 0, nobjIdx, 0, this.objIdx.length);
                    this.objIdx = nobjIdx;
                }

                // read reference lines
                for (int refID = refstart; refID < refstart + reflen; refID++) {
                    // each reference line is 20 bytes long
                    byte[] refline = new byte[20];
                    this.buf.get(refline);

                    // ignore this line if the object ID is already defined
                    if (this.objIdx[refID] != null) {
                        continue;
                    }

                    // see if it's an active object
                    if (refline[17] == 'n') {
                        this.objIdx[refID] = new PDFXref(refline);
                    } else {
                        this.objIdx[refID] = new PDFXref(null);
                    }
                }
            }

            // at this point, the "trailer" word (not EOL) has been read.
	    PDFObject trailerdict = readObject(-1, -1, IdentityDecrypter.getInstance());
            if (trailerdict.getType() != PDFObject.DICTIONARY) {
                throw new IOException("Expected dictionary after \"trailer\"");
            }

            // read the root object location
            if (this.root == null) {
                this.root = trailerdict.getDictRef("Root");
                if (this.root != null) {
                    this.root.setObjectId(PDFObject.OBJ_NUM_TRAILER,
                            PDFObject.OBJ_NUM_TRAILER);
                }
            }

            // read the encryption information
            if (this.encrypt == null) {
                this.encrypt = trailerdict.getDictRef("Encrypt");
                if (this.encrypt != null) {
                    this.encrypt.setObjectId(PDFObject.OBJ_NUM_TRAILER,
                            PDFObject.OBJ_NUM_TRAILER);
                }

                if (this.encrypt != null && !PDFDecrypterFactory.isFilterExist(this.encrypt)) {
                    this.encrypt = null; // the filter is not located at this trailer, we will try later again
                } else {
                    newDefaultDecrypter = PDFDecrypterFactory.createDecryptor(this.encrypt, trailerdict.getDictRef("ID"), password);
                }
            }


            if (this.info == null) {
                this.info = trailerdict.getDictRef("Info");
                if (this.info != null) {
                    if (!this.info.isIndirect()) {
                        throw new PDFParseException(
                                "Info in trailer must be an indirect reference");
                    }
                    this.info.setObjectId(PDFObject.OBJ_NUM_TRAILER,
                            PDFObject.OBJ_NUM_TRAILER);
                }
            }

            // support for hybrid-PDFs containing an additional compressed-xref-stream
            PDFObject xrefstmPos = trailerdict.getDictRef("XRefStm");
            if (xrefstmPos != null) {
                int pos14 = this.buf.position(); 
                this.buf.position(xrefstmPos.getIntValue());
            	readTrailer15(password);
                this.buf.position(pos14);
            }
            
            // read the location of the previous xref table
            PDFObject prevloc = trailerdict.getDictRef("Prev");
            if (prevloc != null) {
                this.buf.position(prevloc.getIntValue());
            } else {
                break;
            }
            // see if we have an optional Version entry


            if (this.root.getDictRef("Version") != null) {
                processVersion(this.root.getDictRef("Version").getStringValue());
            }
        }

        // make sure we found a root
        if (this.root == null) {
            throw new PDFParseException("No /Root key found in trailer dictionary");
        }

        if (this.encrypt != null && newDefaultDecrypter!=null) {
            PDFObject permissions = this.encrypt.getDictRef("P");
            if (permissions!=null && !newDefaultDecrypter.isOwnerAuthorised()) {
                int perms= permissions != null ? permissions.getIntValue() : 0;
                if (permissions!=null) {
                    this.printable = (perms & 4) != 0;
                    this.saveable = (perms & 16) != 0;
                }
            }
            // Install the new default decrypter only after the trailer has
            // been read, as nothing we're reading passing through is encrypted
            this.defaultDecrypter = newDefaultDecrypter;
        }

        // dereference the root object
        this.root.dereference();
    }

    /**
     * read the cross reference table from a PDF file.  When this method
     * is called, the file pointer must point to the start of the word
     * "xref" in the file.  Reads the xref table and the trailer dictionary.
     * If dictionary has a /Prev entry, move file pointer
     * and read new trailer
     * @param password
     */
    private void readTrailer15(PDFPassword password)
            throws
            IOException,
            PDFAuthenticationFailureException,
            EncryptionUnsupportedByProductException,
            EncryptionUnsupportedByPlatformException {
    	
        // the table of xrefs
        // objIdx is initialized from readTrailer(), do not overwrite here data from hybrid PDFs
//        objIdx = new PDFXref[50];
        PDFDecrypter newDefaultDecrypter = null;
        
        while (true) {
			PDFObject xrefObj = readObject(-1, -1, IdentityDecrypter.getInstance());
			if (xrefObj == null) {
				break;
			}
			HashMap<String, PDFObject> trailerdict = xrefObj.getDictionary();
			if (trailerdict == null) {
				break;
			}
			PDFObject pdfObject = trailerdict.get("W");
			if (pdfObject == null) {
				break;
			}
			PDFObject[] wNums = pdfObject.getArray();
			int l1 = wNums[0].getIntValue();
			int l2 = wNums[1].getIntValue();
			int l3 = wNums[2].getIntValue();
	
			int size = trailerdict.get("Size").getIntValue();

			byte[] strmbuf = xrefObj.getStream();
			int strmPos = 0;
			
			PDFObject idxNums = trailerdict.get("Index");
			int[] idxArray;
			if (idxNums == null) {
				idxArray = new int[]{0, size};
			}
			else {
				PDFObject[] idxNumArr = idxNums.getArray();
				idxArray = new int[idxNumArr.length];
				for (int i = 0; i < idxNumArr.length; i++) {
					idxArray[i] = idxNumArr[i].getIntValue();
				}
			}
			int idxLen = idxArray.length;
			int idxPos = 0;
	
			
			while (idxPos<idxLen) {
				int refstart = idxArray[idxPos++];
				int reflen = idxArray[idxPos++];
				
		        // extend the objIdx table, if necessary
		        if (refstart + reflen >= this.objIdx.length) {
		            PDFXref nobjIdx[] = new PDFXref[refstart + reflen];
		            System.arraycopy(this.objIdx, 0, nobjIdx, 0, this.objIdx.length);
		            this.objIdx = nobjIdx;
		        }
	
	            // read reference lines
	            for (int refID = refstart; refID < refstart + reflen; refID++) {
	            	
					int type = readNum(strmbuf, strmPos, l1);
					strmPos += l1;
					int id = readNum(strmbuf, strmPos, l2);
					strmPos += l2;
					int gen = readNum(strmbuf, strmPos, l3);
					strmPos += l3;
	
	                // ignore this line if the object ID is already defined
	                if (this.objIdx[refID] != null) {
	                    continue;
	                }
	
	                // see if it's an active object
	                if (type == 0) { // inactive
	                    this.objIdx[refID] = new PDFXref(null);
	                } else if (type == 1) { // active uncompressed
	                    this.objIdx[refID] = new PDFXref(id, gen);
	                } else { // active compressed
	                    this.objIdx[refID] = new PDFXref(id, gen, true);
	                }
	            	
				}
			}
	
		    // read the root object location
            if (this.root == null) {
                this.root = trailerdict.get("Root");
                if (this.root != null) {
                    this.root.setObjectId(PDFObject.OBJ_NUM_TRAILER,
                            PDFObject.OBJ_NUM_TRAILER);
                }
            }

            // read the encryption information
            if (this.encrypt == null) {
                this.encrypt = trailerdict.get("Encrypt");
                if (this.encrypt != null) {
                    this.encrypt.setObjectId(PDFObject.OBJ_NUM_TRAILER,
                            PDFObject.OBJ_NUM_TRAILER);

                }
                if (this.encrypt != null && !PDFDecrypterFactory.isFilterExist(this.encrypt)) {
                    this.encrypt = null; // the filter is not located at this trailer, we will try later again
                } else {
                    newDefaultDecrypter = PDFDecrypterFactory.createDecryptor(this.encrypt, trailerdict.get("ID"), password);
                }
            }

            if (this.info == null) {
                this.info = trailerdict.get("Info");
                if (this.info != null) {
                    if (!this.info.isIndirect()) {
                        throw new PDFParseException(
                                "Info in trailer must be an indirect reference");
                    }
                    this.info.setObjectId(PDFObject.OBJ_NUM_TRAILER,
                            PDFObject.OBJ_NUM_TRAILER);
                }
            }

            // read the location of the previous xref table
            PDFObject prevloc = trailerdict.get("Prev");
            if (prevloc != null) {
                this.buf.position(prevloc.getIntValue());
            } else {
                break;
            }
            // see if we have an optional Version entry


            if (this.root.getDictRef("Version") != null) {
                processVersion(this.root.getDictRef("Version").getStringValue());
            }
        }

        // make sure we found a root
        if (this.root == null) {
            throw new PDFParseException("No /Root key found in trailer dictionary");
        }

        // check what permissions are relevant
        if (this.encrypt != null && newDefaultDecrypter!=null) {
            PDFObject permissions = this.encrypt.getDictRef("P");
            if (permissions!=null && !newDefaultDecrypter.isOwnerAuthorised()) {
                int perms= permissions != null ? permissions.getIntValue() : 0;
                if (permissions!=null) {
                    this.printable = (perms & 4) != 0;
                    this.saveable = (perms & 16) != 0;
                }
            }
            // Install the new default decrypter only after the trailer has
            // been read, as nothing we're reading passing through is encrypted
            this.defaultDecrypter = newDefaultDecrypter;
        }

        // dereference the root object
        this.root.dereference();
    }

    private int readNum(byte[] sbuf, int pos, int numBytes) {
    	int result = 0;
    	for (int i=0; i<numBytes; i++)
    		result = (result << 8) + (sbuf[pos+i]&0xff);
		return result;
	}

	/**
     * build the PDFFile reference table.  Nothing in the PDFFile actually
     * gets parsed, despite the name of this function.  Things only get
     * read and parsed when they're needed.
     * @param password
     */
    private void parseFile(PDFPassword password) throws IOException {
        // start at the begining of the file
        this.buf.rewind();
        String versionLine = readLine();
        if (versionLine.startsWith(VERSION_COMMENT)) {
            processVersion(versionLine.substring(VERSION_COMMENT.length()));
        }
        this.buf.rewind();

        // back up about 32 characters from the end of the file to find
        // startxref\n
        byte[] scan = new byte[32];
        int scanPos = this.buf.remaining() - scan.length;
        int loc = 0;

        while (scanPos >= 0) {
            this.buf.position(scanPos);
            this.buf.get(scan);

            // find startxref in scan
            String scans = new String(scan);
            loc = scans.indexOf("startxref");
            if (loc >= 0) {
                if (scanPos + loc + scan.length <= this.buf.limit()) {
                    scanPos = scanPos + loc;
                    loc = 0;
                }

                break;
            }
            scanPos -= scan.length - 10;
        }

        if (scanPos < 0) {
            throw new IOException("This may not be a PDF File");
        }

        this.buf.position(scanPos);
        this.buf.get(scan);
        String scans = new String(scan);

        loc += 10;  // skip over "startxref" and first EOL char
        if (scans.charAt(loc) < 32) {
            loc++;
        }  // skip over possible 2nd EOL char
        while (scans.charAt(loc) == 32) {
            loc++;
        } // skip over possible leading blanks
        // read number
        int numstart = loc;
        while (loc < scans.length() &&
                scans.charAt(loc) >= '0' &&
                scans.charAt(loc) <= '9') {
            loc++;
        }
        int xrefpos = Integer.parseInt(scans.substring(numstart, loc));
        this.buf.position(xrefpos);

        try {
            readTrailer(password);
        } catch (UnsupportedEncryptionException e) {
            throw new PDFParseException(e.getMessage(), e);
        }
    }

    /**
     * Gets the outline tree as a tree of OutlineNode, which is a subclass
     * of DefaultMutableTreeNode.  If there is no outline tree, this method
     * returns null.
     */
    public OutlineNode getOutline() throws IOException {
        // find the outlines entry in the root object
        PDFObject oroot = this.root.getDictRef("Outlines");
        OutlineNode work = null;
        OutlineNode outline = null;
        if (oroot != null) {
            // find the first child of the outline root
            PDFObject scan = oroot.getDictRef("First");
            outline = work = new OutlineNode("<top>");

            // scan each sibling in turn
            while (scan != null) {
                // add the new node with it's name
                String title = scan.getDictRef("Title").getTextStringValue();
                OutlineNode build = new OutlineNode(title);
                work.add(build);

                // find the action
                PDFAction action = null;

                PDFObject actionObj = scan.getDictRef("A");
                if (actionObj != null) {
                    try {
                        action = PDFAction.getAction(actionObj, getRoot());
                    }
                    catch (PDFParseException e) {
                    	// oh well
                    }
                } else {
                    // try to create an action from a destination
                    PDFObject destObj = scan.getDictRef("Dest");
                    if (destObj != null) {
                        try {
                            PDFDestination dest =
                                    PDFDestination.getDestination(destObj, getRoot());

                            action = new GoToAction(dest);
                        } catch (IOException ioe) {
                            // oh well
                        }
                    }
                }

                // did we find an action?  If so, add it
                if (action != null) {
                    build.setAction(action);
                }

                // find the first child of this node
                PDFObject kid = scan.getDictRef("First");
                if (kid != null) {
                    work = build;
                    scan = kid;
                } else {
                    // no child.  Process the next sibling
                    PDFObject next = scan.getDictRef("Next");
                    while (next == null) {
                        scan = scan.getDictRef("Parent");
                        next = scan.getDictRef("Next");
                        work = (OutlineNode) work.getParent();
                        if (work == null) {
                            break;
                        }
                    }
                    scan = next;
                }
            }
        }

        return outline;
    }

    /**
     * Gets the page number (starting from 1) of the page represented by
     * a particular PDFObject.  The PDFObject must be a Page dictionary or
     * a destination description (or an action).
     * @return a number between 1 and the number of pages indicating the
     * page number, or 0 if the PDFObject is not in the page tree.
     */
    public int getPageNumber(PDFObject page) throws IOException {
        if (page.getType() == PDFObject.ARRAY) {
            page = page.getAt(0);
        }

        // now we've got a page.  Make sure.
        PDFObject typeObj = page.getDictRef("Type");
        if (typeObj == null || !typeObj.getStringValue().equals("Page")) {
            return 0;
        }

        int count = 0;
        while (true) {
            PDFObject parent = page.getDictRef("Parent");
            if (parent == null) {
                break;
            }
            PDFObject kids[] = parent.getDictRef("Kids").getArray();
            for (int i = 0; i < kids.length; i++) {
                if (kids[i].equals(page)) {
                    break;
                } else {
                    PDFObject kcount = kids[i].getDictRef("Count");
                    if (kcount != null) {
                        count += kcount.getIntValue();
                    } else {
                        count += 1;
                    }
                }
            }
            page = parent;
        }
        return count;
    }

    /**
     * Get the page commands for a given page in a separate thread.
     *
     * @param pagenum the number of the page to get commands for
     */
    public PDFPage getPage(int pagenum) {
        return getPage(pagenum, false);
    }

    /**
     * Get the page commands for a given page.
     *
     * @param pagenum the number of the page to get commands for
     * @param wait if true, do not exit until the page is complete.
     */
    public PDFPage getPage(int pagenum, boolean wait) {
        Integer key = Integer.valueOf(pagenum);
        HashMap<String,PDFObject> resources = null;
        PDFObject pageObj = null;

        PDFPage page = this.cache.getPage(key);
        PDFParser parser = this.cache.getPageParser(key);
        if (page == null) {
            try {
                // hunt down the page!
                resources = new HashMap<String,PDFObject>();

                PDFObject topPagesObj = this.root.getDictRef("Pages");
                pageObj = findPage(topPagesObj, 0, pagenum, resources);

                if (pageObj == null) {
                    return null;
                }

                page = createPage(pagenum, pageObj);

                byte[] stream = getContents(pageObj);
                parser = new PDFParser(page, stream, resources);

                this.cache.addPage(key, page, parser);
            } catch (IOException ioe) {
                return null;
            }
        }

		if (parser != null) {
			if (!parser.isFinished()) {
				parser.go(wait);
			}
			if (parser.getStatus() == Watchable.ERROR) {
				PDFDebugger.debug("Error in parsing the PDF page!");
			}
		}

        return page;
    }

    /**
     * Stop the rendering of a particular image on this page
     */
    public void stop(int pageNum) {
        PDFParser parser = this.cache.getPageParser(Integer.valueOf(pageNum));
        if (parser != null) {
            // stop it
            parser.stop();
        }
    }

    /**
     * get the stream representing the content of a particular page.
     *
     * @param pageObj the page object to get the contents of
     * @return a concatenation of any content streams for the requested
     * page.
     */
    private byte[] getContents(PDFObject pageObj) throws IOException {
        // concatenate all the streams
        PDFObject contentsObj = pageObj.getDictRef("Contents");
        if (contentsObj == null) {
            return new byte[0];
        }

        PDFObject contents[] = contentsObj.getArray();

        // see if we have only one stream (the easy case)
        if (contents.length == 1) {
            return contents[0].getStream();
        }

        // first get the total length of all the streams
        int len = 0;
        for (int i = 0; i < contents.length; i++) {
            byte[] data = contents[i].getStream();
            if (data == null) {
                throw new PDFParseException("No stream on content " + i +
                        ": " + contents[i]);
            }
            len += data.length;
        }

        // now assemble them all into one object
        byte[] stream = new byte[len];
        len = 0;
        for (int i = 0; i < contents.length; i++) {
            byte data[] = contents[i].getStream();
            System.arraycopy(data, 0, stream, len, data.length);
            len += data.length;
        }

        return stream;
    }

    /**
     * Create a PDF Page object by finding the relevant inherited
     * properties
     *
     * @param pageObj the PDF object for the page to be created
     */
    private PDFPage createPage(int pagenum, PDFObject pageObj)
            throws IOException {
        int rotation = 0;
        Rectangle2D mediabox = null; // third choice, if no crop
        Rectangle2D cropbox = null; // second choice
        Rectangle2D trimbox = null; // first choice

        PDFObject mediaboxObj = getInheritedValue(pageObj, "MediaBox");
        if (mediaboxObj != null) {
            mediabox = parseNormalisedRectangle(mediaboxObj);
        }

        PDFObject cropboxObj = getInheritedValue(pageObj, "CropBox");
        if (cropboxObj != null) {
            cropbox = parseNormalisedRectangle(cropboxObj);
        }

        PDFObject trimboxObj = getInheritedValue(pageObj, "TrimBox");
        if (trimboxObj != null) {
            trimbox = parseNormalisedRectangle(trimboxObj);
        }

        PDFObject rotateObj = getInheritedValue(pageObj, "Rotate");
        if (rotateObj != null) {
            rotation = rotateObj.getIntValue();
        }

        // read annotations and add them to the PDF page
        PDFObject annots = getInheritedValue(pageObj, "Annots");
        List<PDFAnnotation> annotationList = new ArrayList<PDFAnnotation>();
        if (annots != null) {
            if (annots.getType() != PDFObject.ARRAY) {
                throw new PDFParseException("Can't parse annotations: " + annots.toString());
            }
        	PDFObject[] array = annots.getArray();
        	for (PDFObject object : array) {
                try {
            		PDFAnnotation pdfAnnot = PDFAnnotation.createAnnotation(object);
            		if(pdfAnnot != null) {
                		annotationList.add(pdfAnnot);
            		}
                }catch (PDFParseException e) {
        			// do nothing, annotations could not be parsed and links will not be displayed.
        		}
			}            
        }
        
        Rectangle2D bbox = (trimbox == null ? ((cropbox == null) ? mediabox : cropbox) : trimbox);
        PDFPage page = new PDFPage(pagenum, bbox, rotation, this.cache);
        page.setAnnots(annotationList);
        return page;
    }

    /**
     * Get the PDFObject representing the content of a particular page. Note
     * that the number of the page need not have anything to do with the
     * label on that page.  If there are two blank pages, and then roman
     * numerals for the page number, then passing in 6 will get page (iv).
     *
     * @param pagedict the top of the pages tree
     * @param start the page number of the first page in this dictionary
     * @param getPage the number of the page to find; NOT the page's label.
     * @param resources a HashMap that will be filled with any resource
     *                  definitions encountered on the search for the page
     */
    private PDFObject findPage(PDFObject pagedict, int start, int getPage,
            Map<String,PDFObject> resources) throws IOException {
        PDFObject rsrcObj = pagedict.getDictRef("Resources");
        if (rsrcObj != null) {
            resources.putAll(rsrcObj.getDictionary());
        }

        PDFObject typeObj = pagedict.getDictRef("Type");
        if (typeObj != null && typeObj.getStringValue().equals("Page")) {
            // we found our page!
            return pagedict;
        }

        // find the first child for which (start + count) > getPage
        PDFObject kidsObj = pagedict.getDictRef("Kids");
        if (kidsObj != null) {
            PDFObject[] kids = kidsObj.getArray();
            for (int i = 0; i < kids.length; i++) {
                int count = 1;
                // BUG: some PDFs (T1Format.pdf) don't have the Type tag.
                // use the Count tag to indicate a Pages dictionary instead.
                PDFObject countItem = kids[i].getDictRef("Count");
                //                if (kids[i].getDictRef("Type").getStringValue().equals("Pages")) {
                if (countItem != null) {
                    count = countItem.getIntValue();
                }

                if (start + count >= getPage) {
                    return findPage(kids[i], start, getPage, resources);
                }

                start += count;
            }
        }

        return null;
    }

    /**
     * Find a property value in a page that may be inherited.  If the value
     * is not defined in the page itself, follow the page's "parent" links
     * until the value is found or the top of the tree is reached.
     *
     * @param pageObj the object representing the page
     * @param propName the name of the property we are looking for
     */
    private PDFObject getInheritedValue(PDFObject pageObj, String propName)
            throws IOException {
        // see if we have the property
        PDFObject propObj = pageObj.getDictRef(propName);
        if (propObj != null) {
            return propObj;
        }

        // recursively see if any of our parent have it
        PDFObject parentObj = pageObj.getDictRef("Parent");
        if (parentObj != null) {
            return getInheritedValue(parentObj, propName);
        }

        // no luck
        return null;
    }

    public static Rectangle2D parseNormalisedRectangle(PDFObject obj)
            throws IOException {

        if (obj != null) {
            if (obj.getType() == PDFObject.ARRAY) {
                PDFObject bounds[] = obj.getArray();
                if (bounds.length == 4) {
                    final double x0 = bounds[0].getDoubleValue();
                    final double y0 = bounds[1].getDoubleValue();
                    final double x1 = bounds[2].getDoubleValue();
                    final double y1 = bounds[3].getDoubleValue();

                    final double minX;
                    final double maxY;
                    final double maxX;
                    final double minY;

                    if (x0 < x1) {
                        minX = x0;
                        maxX = x1;
                    } else {
                        minX = x1;
                        maxX = x0;
                    }
                    if (y0 < y1) {
                        minY = y0;
                        maxY = y1;
                    } else {
                        minY = y1;
                        maxY = y0;
                    }

                    return new Rectangle2D.Double(minX, minY, Math.abs(maxX - minX), Math.abs(maxY - minY));

                } else {
                    throw new PDFParseException("Rectangle definition didn't have 4 elements");
                }
            } else {
                throw new PDFParseException("Rectangle definition not an array");
            }
        } else {
            throw new PDFParseException("Rectangle not present");
        }

    }

    /**
     * Get the default decrypter for the document
     * @return the default decrypter; never null, even for documents that
     *  aren't encrypted
     */
    public PDFDecrypter getDefaultDecrypter() {
        return this.defaultDecrypter;
    }
}
