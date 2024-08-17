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
package com.sun.pdfview.font;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.sun.pdfview.PDFDebugger;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFObject;

/**
 * A representation, with parser, of an Adobe Type 1 font.
 * @author Mike Wessler
 */
public class Type1Font extends OutlineFont {

    String chr2name[];
    int password;
    byte[] subrs[];
    int lenIV;
    Map<String,Object> name2outline;
    Map<String,FlPoint> name2width;
    AffineTransform at;
    /** the Type1 stack of command values */
    float stack[] = new float[100];
    /** the current position in the Type1 stack */
    int sloc = 0;
    /** the stack of postscript commands (used by callothersubr) */
    float psStack[] = new float[3];
    /** the current position in the postscript stack */
    int psLoc = 0;

    /**
     * create a new Type1Font based on a font data stream and an encoding.
     * @param baseName the postscript name of this font
     * @param src the Font object as a stream with a dictionary
     * @param descriptor the descriptor for this font
     */
    public Type1Font(String baseName, PDFObject src,
            PDFFontDescriptor descriptor) throws IOException {
        super(baseName, src, descriptor);

        if (descriptor != null && descriptor.getFontFile() != null) {
            // parse that file, filling name2outline and chr2name
            int start = descriptor.getFontFile().getDictRef("Length1").getIntValue();
            int len = descriptor.getFontFile().getDictRef("Length2").getIntValue();
            byte font[] = descriptor.getFontFile().getStream();

            parseFont(font, start, len);
        }
    }

    /** Read a font from it's data, start position and length */
    protected void parseFont(byte[] font, int start, int len) {
        this.name2width = new HashMap<String,FlPoint>();

        byte data[] = null;

        if (isASCII(font, start)) {
            byte[] bData = readASCII(font, start, start + len);
            data = decrypt(bData, 0, bData.length, 55665, 4);
        } else {
            data = decrypt(font, start, start + len, 55665, 4);
        }

        // encoding is in cleartext area
        this.chr2name = readEncoding(font);
        int lenIVLoc = findSlashName(data, "lenIV");
        PSParser psp = new PSParser(data, 0);
        if (lenIVLoc < 0) {
            this.lenIV = 4;
        } else {
            psp.setLoc(lenIVLoc + 6);
            this.lenIV = Integer.parseInt(psp.readThing());
        }
        this.password = 4330;
        int matrixloc = findSlashName(font, "FontMatrix");
        if (matrixloc < 0) {
            PDFDebugger.debug("No FontMatrix!");
            this.at = new AffineTransform(0.001f, 0, 0, 0.001f, 0, 0);
        } else {
            PSParser psp2 = new PSParser(font, matrixloc + 11);
            // read [num num num num num num]
            float xf[] = psp2.readArray(6);
            this.at = new AffineTransform(xf);
        }

        this.subrs = readSubrs(data);
        this.name2outline = new TreeMap<String,Object>(readChars(data));
    // at this point, name2outline holds name -> byte[].
    }

    /**
     * parse the encoding portion of the font definition
     * @param d the font definition stream
     * @return an array of the glyphs corresponding to each byte
     */
    private String[] readEncoding(byte[] d) {
        byte[][] ary = readArray(d, "Encoding", "def");
        String res[] = new String[256];
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] != null) {
                if (ary[i][0] == '/') {
                    res[i] = new String(ary[i]).substring(1);
                } else {
                    res[i] = new String(ary[i]);
                }
            } else {
                res[i] = null;
            }
        }
        return res;
    }

    /**
     * read the subroutines out of the font definition
     * @param d the font definition stream
     * @return an array of the subroutines, each as a byte array.
     */
    private byte[][] readSubrs(byte[] d) {
        return readArray(d, "Subrs", "index");
    }

    /**
     * read a named array out of the font definition.
     * <p>
     * this function attempts to parse an array out of a postscript
     * definition without doing any postscript.  It's actually looking
     * for things that look like "dup <i>id</i> <i>elt</i> put", and
     * placing the <i>elt</i> at the <i>i</i>th position in the array.
     * @param d the font definition stream
     * @param key the name of the array
     * @param end a string that appears at the end of the array
     * @return an array consisting of a byte array for each entry
     */
    private byte[][] readArray(byte[] d, String key, String end) {
        int i = findSlashName(d, key);
        if (i < 0) {
            // not found.
            return new byte[0][];
        }
        // now find things that look like "dup id elt put"
        // end at "def"
        PSParser psp = new PSParser(d, i);
        String type = psp.readThing();     // read the key (i is the start of the key)
        double val;
        type = psp.readThing();
        if (type.equals("StandardEncoding")) {
            byte[] stdenc[] = new byte[FontSupport.standardEncoding.length][];
            for (i = 0; i < stdenc.length; i++) {
                stdenc[i] = FontSupport.getName(FontSupport.standardEncoding[i]).getBytes();
            }
            return stdenc;
        }
        int len = Integer.parseInt(type);
        byte[] out[] = new byte[len][];
        byte[] line;
        while (true) {
            String s = psp.readThing();
            if (s.equals("dup")) {
                String thing = psp.readThing();
                int id = 0;
                try {
                    id = Integer.parseInt(thing);
                } catch (Exception e) {
                    break;
                }
                String elt = psp.readThing();
                line = elt.getBytes();
                if (Character.isDigit(elt.charAt(0))) {
                    int hold = Integer.parseInt(elt);
                    String special = psp.readThing();
                    if (special.equals("-|") || special.equals("RD")) {
                        psp.setLoc(psp.getLoc() + 1);
                        line = psp.getNEncodedBytes(hold, this.password, this.lenIV);
                    }
                }
                out[id] = line;
            } else if (s.equals(end)) {
                break;
            }
        }
        return out;
    }

    /**
     * decrypt an array using the Adobe Type 1 Font decryption algorithm.
     * @param d the input array of bytes
     * @param start where in the array to start decoding
     * @param end where in the array to stop decoding
     * @param key the decryption key
     * @param skip how many bytes to skip initially
     * @return the decrypted bytes.  The length of this array will be
     * (start-end-skip) bytes long
     */
    private byte[] decrypt(byte[] d, int start, int end, int key, int skip) {
        if (end - start - skip < 0) {
            skip = 0;
        }
        byte[] o = new byte[end - start - skip];
        int r = key;
        int ipos;
        int c1 = 52845;
        int c2 = 22719;
        for (ipos = start; ipos < end; ipos++) {
            int c = d[ipos] & 0xff;
            int p = (c ^ (r >> 8)) & 0xff;
            r = ((c + r) * c1 + c2) & 0xffff;
            if (ipos - start - skip >= 0) {
                o[ipos - start - skip] = (byte) p;
            }
        }
        return o;
    }

    /**
     * Read data formatted as ASCII strings as binary data
     *
     * @param data the data, formatted as ASCII strings
     * @param start where in the array to start decrypting
     * @param end where in the array to stop decrypting
     */
    private byte[] readASCII(byte[] data, int start, int end) {
        // each byte of output is derived from one character (two bytes) of
        // input
        byte[] o = new byte[(end - start) / 2];

        int count = 0;
        int bit = 0;

        for (int loc = start; loc < end; loc++) {
            char c = (char) (data[loc] & 0xff);
            byte b = (byte) 0;

            if (c >= '0' && c <= '9') {
                b = (byte) (c - '0');
            } else if (c >= 'a' && c <= 'f') {
                b = (byte) (10 + (c - 'a'));
            } else if (c >= 'A' && c <= 'F') {
                b = (byte) (10 + (c - 'A'));
            } else {
                // linefeed or something.  Skip.
                continue;
            }

            // which half of the byte are we?
            if ((bit++ % 2) == 0) {
                o[count] = (byte) (b << 4);
            } else {
                o[count++] |= b;
            }
        }

        return o;
    }

    /** 
     * Determine if data is in ASCII or binary format.  According to the spec,
     * if any of the first 4 bytes are not character codes ('0' - '9' or
     * 'A' - 'F' or 'a' - 'f'), then the data is binary.  Otherwise it is
     * ASCII
     */
    private boolean isASCII(byte[] data, int start) {
        // look at the first 4 bytes
        for (int i = start; i < start + 4; i++) {
            // get the byte as a character
            char c = (char) (data[i] & 0xff);

            if (c >= '0' && c <= '9') {
                continue;
            } else if (c >= 'a' && c <= 'f') {
                continue;
            } else if (c >= 'A' && c <= 'F') {
                continue;
            } else {
                // out of range
                return false;
            }
        }

        // all were in range, so it is ASCII
        return true;
    }

    /**
     * PostScript reader (not a parser, as the name would seem to indicate).
     */
    class PSParser {

        byte[] data;
        int loc;

        /**
         * create a PostScript reader given some data and an initial offset
         * into that data.
         * @param data the bytes of the postscript information
         * @param start an initial offset into the data
         */
        public PSParser(byte[] data, int start) {
            this.data = data;
            this.loc = start;
        }

        /**
         * get the next postscript "word".  This is basically the next
         * non-whitespace block between two whitespace delimiters.
         * This means that something like " [2 4 53]" will produce
         * three items, while " [2 4 56 ]" will produce four.
         */
        public String readThing() {
            // skip whitespace
            while (PDFFile.isWhiteSpace(this.data[this.loc])) {
                this.loc++;
            }
            // read thing
            int start = this.loc;
            while (!PDFFile.isWhiteSpace(this.data[this.loc])) {
                this.loc++;
                if (!PDFFile.isRegularCharacter(this.data[this.loc])) {
                    break;  // leave with the delimiter included
                }
            }
            String s = new String(this.data, start, this.loc - start);
            return s;
        }

        /**
         * read a set of numbers from the input.  This method doesn't
         * pay any attention to "[" or "]" delimiters, and reads any
         * non-numeric items as the number 0.
         * @param count the number of items to read
         * @return an array of count floats
         */
        public float[] readArray(int count) {
            float[] ary = new float[count];
            int idx = 0;
            while (idx < count) {
                String thing = readThing();
                if (thing.charAt(0) == '[') {
                    thing = thing.substring(1);
                }
                if (thing.endsWith("]")) {
                    thing = thing.substring(0, thing.length() - 1);
                }
                if (thing.length() > 0) {
                    ary[idx++] = Float.parseFloat(thing);
                }
            }
            return ary;
        }

        /**
         * get the current location within the input stream
         */
        public int getLoc() {
            return this.loc;
        }

        /**
         * set the current location within the input stream
         */
        public void setLoc(int loc) {
            this.loc = loc;
        }

        /**
         * treat the next n bytes of the input stream as encoded
         * information to be decrypted.
         * @param n the number of bytes to decrypt
         * @param key the decryption key
         * @param skip the number of bytes to skip at the beginning of the
         * decryption
         * @return an array of decrypted bytes.  The length of the array
         * will be n-skip.
         */
        public byte[] getNEncodedBytes(int n, int key, int skip) {
            byte[] result = decrypt(this.data, this.loc, this.loc + n, key, skip);
            this.loc += n;
            return result;
        }
    }

    /**
     * get the index into the byte array of a slashed name, like "/name".
     * @param d the search array
     * @param name the name to look for, without the initial /
     * @return the index of the first occurance of /name in the array.
     */
    private int findSlashName(byte[] d, String name) {
        int i;
        for (i = 0; i < d.length; i++) {
            if (d[i] == '/') {
                // check for key
                boolean found = true;
                for (int j = 0; j < name.length(); j++) {
                    if (d[i + j + 1] != name.charAt(j)) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * get the character definitions of the font.
     * @param d the font data
     * @return a HashMap that maps string glyph names to byte arrays of
     * decoded font data.
     */
    private HashMap<String,byte[]> readChars(byte[] d) {
        // skip thru data until we find "/"+key
        HashMap<String,byte[]> hm = new HashMap<String,byte[]>();
        int i = findSlashName(d, "CharStrings");
        if (i < 0) {
            // not found
            return hm;
        }
        PSParser psp = new PSParser(d, i);
        // read /name len -| [len bytes] |-
        // until "end"
        while (true) {
            String s = psp.readThing();
            char c = s.charAt(0);
            if (c == '/') {
                int len = Integer.parseInt(psp.readThing());
                String go = psp.readThing();  // it's -| or RD
                if (go.equals("-|") || go.equals("RD")) {
                    psp.setLoc(psp.getLoc() + 1);
                    byte[] line = psp.getNEncodedBytes(len, this.password, this.lenIV);
                    hm.put(s.substring(1), line);
                }
            } else if (s.equals("end")) {
                break;
            }
        }
        return hm;
    }

    /**
     * pop the next item off the stack
     */
    private float pop() {
        float val = 0;
        if (this.sloc > 0) {
            val = this.stack[--this.sloc];
        }
        return val;
    }
    int callcount = 0;

    /**
     * parse glyph data into a GeneralPath, and return the advance width.
     * The working point is passed in as a parameter in order to allow
     * recursion.
     * @param cs the decrypted glyph data
     * @param gp a GeneralPath into which the glyph shape will be stored
     * @param pt a FlPoint object that will be used to generate the path
     * @param wid a FlPoint into which the advance width will be placed.
     */
    private void parse(byte[] cs, GeneralPath gp, FlPoint pt, FlPoint wid) {
        int loc = 0;
        float x1, x2, x3, y1, y2, y3;
        boolean flexMode = false;
        float[] flexArray = new float[16];
        int flexPt = 0;
        while (loc < cs.length) {
            int v = (cs[loc++]) & 0xff;
            if (v == 255) {
                this.stack[this.sloc++] = (((cs[loc]) & 0xff) << 24) +
                        (((cs[loc + 1]) & 0xff) << 16) +
                        (((cs[loc + 2]) & 0xff) << 8) +
                        (((cs[loc + 3]) & 0xff));
                loc += 4;
            } else if (v >= 251) {
                this.stack[this.sloc++] = -((v - 251) << 8) - ((cs[loc]) & 0xff) - 108;
                loc++;
            } else if (v >= 247) {
                this.stack[this.sloc++] = ((v - 247) << 8) + ((cs[loc]) & 0xff) + 108;
                loc++;
            } else if (v >= 32) {
                this.stack[this.sloc++] = v - 139;
            } else {
                switch (v) {
                    case 0:   // x
                        throw new RuntimeException("Bad command (" + v + ")");
                    case 1:   // hstem
                        this.sloc = 0;
                        break;
                    case 2:   // x
                        throw new RuntimeException("Bad command (" + v + ")");
                    case 3:   // vstem
                        this.sloc = 0;
                        break;
                    case 4:   // y vmoveto
                		pt.y += pop();
                    	if (flexMode) {
                    		flexArray[flexPt++] = pt.x;
                    		flexArray[flexPt++] = pt.y;
                    	}
                    	else{
                    		gp.moveTo(pt.x, pt.y);
                    	}
                        this.sloc = 0;
                        break;
                    case 5:   // x y rlineto
                        pt.y += pop();
                        pt.x += pop();
                        gp.lineTo(pt.x, pt.y);
                        this.sloc = 0;
                        break;
                    case 6:   // x hlineto
                    	pt.x += pop();
                        gp.lineTo(pt.x, pt.y);
                        this.sloc = 0;
                        break;
                    case 7:   // y vlineto
                    	pt.y += pop();
                    	gp.lineTo(pt.x, pt.y);
                        this.sloc = 0;
                        break;
                    case 8:   // x1 y1 x2 y2 x3 y3 rcurveto
                        y3 = pop();
                        x3 = pop();
                        y2 = pop();
                        x2 = pop();
                        y1 = pop();
                        x1 = pop();
                        gp.curveTo(pt.x + x1, pt.y + y1,
                                pt.x + x1 + x2, pt.y + y1 + y2,
                                pt.x + x1 + x2 + x3, pt.y + y1 + y2 + y3);
                        pt.x += x1 + x2 + x3;
                        pt.y += y1 + y2 + y3;                        
                        this.sloc = 0;
                        break;
                    case 9:   // closepath
                        gp.closePath();
                        this.sloc = 0;
                        break;
                    case 10:  // n callsubr
                        int n = (int) pop();
                        if (n == 1) {
                        	flexMode = true;
                        	flexPt = 0;
                        	this.sloc = 0;
                        	break;
                        }
                        if (n == 0) {
                        	if (flexPt != 14) {
                        	    PDFDebugger.debug("There must be 14 flex entries!");
                        	}
                        	else {
                        		gp.curveTo(flexArray[2], flexArray[3], flexArray[4], 
                        				flexArray[5],
                        				flexArray[6], flexArray[7]);
                        		gp.curveTo(flexArray[8], flexArray[9], flexArray[10], 
                        				flexArray[11],
                        				flexArray[12], flexArray[13]);
                        		flexMode = false;
                        		this.sloc = 0;
                        		//System.out.println("End Flex " + flexPt);
                        		break;
                        	}
                        }
                        if (n == 2) {
                        	if (flexMode == false) {
                        	    PDFDebugger.debug("Flex mode assumed");
                        	} 
                        	else {
                        		this.sloc = 0;
                        		break;
                        	}
                        }
                        if (this.subrs[n] == null) {
                            PDFDebugger.debug("No subroutine #" + n);
                        } else {
                            this.callcount++;
                            if (this.callcount > 10) {
                                PDFDebugger.debug("Call stack too large");
                            } else {
                                parse(this.subrs[n], gp, pt, wid);
                            }
                            this.callcount--;
                        }
                        break;
                    case 11:  // return
                        return;
                    case 12:  // ext...
                        v = (cs[loc++]) & 0xff;
                        if (v == 6) {  // s x y a b seac
                        char a = (char) pop();
                            char b = (char) pop();
                            float y = pop();
                            float x = pop();
                            buildAccentChar(x, y, a, b, gp);
                            this.sloc = 0;
                        } else if (v == 7) {  // x y w h sbw
                            wid.y = pop();
                            wid.x = pop();
                            pt.y = pop();
                            pt.x = pop();
                            this.sloc = 0;
                        } else if (v == 12) {  // a b div -> a/b
                            float b = pop();
                            float a = pop();
                            this.stack[this.sloc++] = a / b;
                        } else if (v == 33) {  // a b setcurrentpoint
                            pt.y = pop();
                            pt.x = pop();
                            gp.moveTo(pt.x, pt.y);
                            this.sloc = 0;
                        } else if (v == 0) { // dotsection
                            this.sloc = 0;
                        } else if (v == 1) {  // vstem3
                            this.sloc = 0;
                        } else if (v == 2) {  // hstem3
                            this.sloc = 0;
                        } else if (v == 16) {  // n callothersubr
                            int cn = (int) pop();
                            int countargs = (int) pop();

                            switch (cn) {
                                case 0:
                                    // push args2 and args3 onto stack
                                    this.psStack[this.psLoc++] = pop();
                                    this.psStack[this.psLoc++] = pop();
                                    pop();
                                    break;
                                case 3:
                                    // push 3 onto the postscript stack
                                    this.psStack[this.psLoc++] = 3;
                                    break;
                                default:
                                    // push arguments onto the postscript stack
                                    for (int i = 0; i > countargs; i--) {
                                        this.psStack[this.psLoc++] = pop();
                                    }
                                    break;
                            }
                        } else if (v == 17) {  // pop
                            // pop from the postscript stack onto the type1 stack
                            this.stack[this.sloc++] = this.psStack[this.psLoc - 1];
                            this.psLoc--;
                        } else {
                            throw new RuntimeException("Bad command (" + v + ")");
                        }
                        break;
                    case 13:  // s w hsbw
                        wid.x = pop();
                        wid.y = 0;
                        pt.x = pop();
                        pt.y = 0;
                        //		    gp.moveTo(pt.x, pt.y);
                        this.sloc = 0;
                        break;
                    case 14:  // endchar
                        //		    return;
                        break;
                    case 15:  // x
                    case 16:  // x
                    case 17:  // x
                    case 18:  // x
                    case 19:  // x
                    case 20:  // x
                        throw new RuntimeException("Bad command (" + v + ")");
                    case 21:  // x y rmoveto
                		pt.y += pop();
                		pt.x += pop();
                    	if (flexMode) {
                    		flexArray[flexPt++] = pt.x;
                    		flexArray[flexPt++] = pt.y;
                    	}
                    	else{
                    		gp.moveTo(pt.x, pt.y);
                    	}
                		this.sloc = 0;
                        break;
                    case 22:  // x hmoveto
                		pt.x += pop();
                    	if (flexMode) {
                    		flexArray[flexPt++] = pt.x;
                    		flexArray[flexPt++] = pt.y;
                    	}
                    	else {
                    		gp.moveTo(pt.x, pt.y);
                    	}
                        this.sloc = 0;
                        break;
                    case 23:  // x
                    case 24:  // x
                    case 25:  // x
                    case 26:  // x
                    case 27:  // x
                    case 28:  // x
                    case 29:  // x
                        throw new RuntimeException("Bad command (" + v + ")");
                    case 30:  // y1 x2 y2 x3 vhcurveto
                        x3 = pop();
                        y2 = pop();
                        x2 = pop();
                        y1 = pop();
                        x1 = y3 = 0;
                        gp.curveTo(pt.x, pt.y + y1,
                                pt.x + x2, pt.y + y1 + y2,
                                pt.x + x2 + x3, pt.y + y1 + y2);
                        pt.x += x2 + x3;
                        pt.y += y1 + y2;
                        this.sloc = 0;
                        break;
                    case 31:  // x1 x2 y2 y3 hvcurveto
                        y3 = pop();
                        y2 = pop();
                        x2 = pop();
                        x1 = pop();
                        y1 = x3 = 0;
                        gp.curveTo(pt.x + x1, pt.y,
                                pt.x + x1 + x2, pt.y + y2,
                                pt.x + x1 + x2, pt.y + y2 + y3);
                        pt.x += x1 + x2;
                        pt.y += y2 + y3;
                        this.sloc = 0;
                        break;
                }
            }
        }
    }

    /**
     * build an accented character out of two pre-defined glyphs.
     * @param x the x offset of the accent relativ to the sidebearing of the base char
     * @param y the y offset of the accent relativ to the sidebearing of the base char
     * @param a the index of the accent glyph
     * @param b the index of the base glyph
     * @param gp the GeneralPath into which the combined glyph will be
     * written.
     */
    private void buildAccentChar(float x, float y, char a, char b,
            GeneralPath gp) {
        // get the outline of the accent
        GeneralPath pathA = getOutline(a, getWidth(a, null));
        // don't manipulate the original glyph
        pathA = (GeneralPath) pathA.clone();
        try {
            final AffineTransform xformA = at.createInverse();
            pathA.transform(xformA);
            // Best x can't be calculated cause we don't know the left sidebearing of the base character.
            // Leaving x=0 gives the best results.
            // see Chapter 6 of http://partners.adobe.com/public/developer/en/font/5015.Type1_Supp.pdf
            // and the definition of the seac-Command in http://partners.adobe.com/public/developer/en/font/T1_SPEC.PDF
            final AffineTransform xformA2 = AffineTransform.getTranslateInstance(0, y);
            pathA.transform(xformA2);
        } catch (NoninvertibleTransformException nte) {
            pathA.transform(AffineTransform.getTranslateInstance(x, y));
        }

        GeneralPath pathB = getOutline(b, getWidth(b, null));

        try {
            AffineTransform xformB = this.at.createInverse();
            pathB.transform(xformB);
        } catch (NoninvertibleTransformException nte) {
            // ignore
        }

        gp.append(pathB, false);
        gp.append(pathA, false);
    }

    /** 
     * Get the width of a given character
     *
     * This method is overridden to work if the width array hasn't been
     * populated (as for one of the 14 base fonts)
     */
    @Override
    public float getWidth(char code, String name) {
        // we don't have first and last chars, so therefore no width array
        if (getFirstChar() == -1 || getLastChar() == -1) {
            String key = this.chr2name[code & 0xff];

            // use a name if one is provided
            if (name != null) {
                key = name;
            }

            if (key != null && this.name2outline.containsKey(key)) {
                if (!this.name2width.containsKey(key)) {
                    // glyph has not yet been parsed
                    // getting the outline will force it to get read
                    getOutline(key, 0);
                }

                FlPoint width = this.name2width.get(key);
                if (width != null) {
                    return width.x / getDefaultWidth();
                }
            }

            return 0;
        }

        // return the width that has been specified
        return super.getWidth(code, name);
    }

    /**
     * Decrypt a glyph stored in byte form
     */
    private synchronized GeneralPath parseGlyph(byte[] cs, FlPoint advance,
            AffineTransform at) {
        GeneralPath gp = new GeneralPath();
        FlPoint curpoint = new FlPoint();

        this.sloc = 0;
        parse(cs, gp, curpoint, advance);

        gp.transform(at);
        return gp;
    }

    /**
     * Get a glyph outline by name
     *
     * @param name the name of the desired glyph
     * @return the glyph outline, or null if unavailable
     */
    @Override
	protected GeneralPath getOutline(String name, float width) {
        // make sure we have a valid name
        if (name == null || !this.name2outline.containsKey(name)) {
            name = ".notdef";
        }

        // get whatever is stored in name. Could be a GeneralPath, could be byte[]
        Object obj = this.name2outline.get(name);

        // if it's a byte array, it needs to be parsed
        // otherwise, just return the path
        if (obj instanceof GeneralPath) {
            return (GeneralPath) obj;
        } else {
            byte[] cs = (byte[]) obj;
            FlPoint advance = new FlPoint();

            GeneralPath gp = parseGlyph(cs, advance, this.at);

            if (width != 0 && advance.x != 0) {
                // scale the glyph to fit in the width
                Point2D p = new Point2D.Float(advance.x, advance.y);
                this.at.transform(p, p);

                double scale = width / p.getX();
                AffineTransform xform = AffineTransform.getScaleInstance(scale, 1.0);
                gp.transform(xform);
            }

            // put the parsed object in the cache
            this.name2outline.put(name, gp);
            this.name2width.put(name, advance);
            return gp;
        }
    }

    /**
     * Get a glyph outline by character code
     *
     * Note this method must always return an outline 
     *
     * @param src the character code of the desired glyph
     * @return the glyph outline
     */
    @Override
	protected GeneralPath getOutline(char src, float width) {
        return getOutline(this.chr2name[src & 0xff], width);
    }
    
    public boolean isName2OutlineFilled() {
    	return (name2outline!=null) && !name2outline.isEmpty();
    }
}