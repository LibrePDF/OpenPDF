/*
 * $Id: Pfm2afm.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 1991 Ken Borgendale
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
 * the Initial Developer are Copyright (C) 1999-2007 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2007 by Paulo Soares. All Rights Reserved.
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
/********************************************************************
 *                                                                  *
 *  Title:  pfm2afm - Convert Windows .pfm files to .afm files      *
 *                                                                  *
 *  Author: Ken Borgendale   10/9/91  Version 1.0                   *
 *                                                                  *
 *  Function:                                                       *
 *      Convert a Windows .pfm (Printer Font Metrics) file to a     *
 *      .afm (Adobe Font Metrics) file.  The purpose of this is     *
 *      to allow fonts put out for Windows to be used with OS/2.    *
 *                                                                  *
 *  Syntax:                                                         *
 *      pfm2afm  infile  [outfile] -a                               *
 *                                                                  *
 *  Copyright:                                                      *
 *      pfm2afm - Copyright (C) IBM Corp., 1991                     *
 *                                                                  *
 *      This code is released for public use as long as the         *
 *      copyright remains intact.  This code is provided asis       *
 *      without any warrenties, express or implied.                 *
 *                                                                  *
 *  Notes:                                                          *
 *      1. Much of the information in the original .afm file is     *
 *         lost when the .pfm file is created, and thus cannot be   *
 *         reconstructed by this utility.  This is especially true  *
 *         of data for characters not in the Windows character set. *
 *                                                                  *
 *      2. This module is coded to be compiled by the MSC 6.0.      *
 *         For other compilers, be careful of the packing of the    *
 *         PFM structure.                                           *
 *                                                                  *
 ********************************************************************/

/********************************************************************
 *                                                                  *
 *  Modifications by Rod Smith, 5/22/96                             *
 *                                                                  *
 *  These changes look for the strings "italic", "bold", "black",   *
 *  and "light" in the font's name and set the weight accordingly   *
 *  and adds an ItalicAngle line with a value of "0" or "-12.00".   *
 *  This allows OS/2 programs such as DeScribe to handle the bold   *
 *  and italic attributes appropriately, which was not the case     *
 *  when I used the original version on fonts from the KeyFonts     *
 *  Pro 2002 font CD.                                               *
 *                                                                  *
 *  I've also increased the size of the buffer used to load the     *
 *  .PFM file; the old size was inadequate for most of the fonts    *
 *  from the SoftKey collection.                                    *
 *                                                                  *
 *  Compiled with Watcom C 10.6                                     *
 *                                                                  *
 ********************************************************************/
 
/********************************************************************
 *                                                                  *
 *  Further modifications, 4/21/98, by Rod Smith                    *
 *                                                                  *
 *  Minor changes to get the program to compile with gcc under      *
 *  Linux (Red Hat 5.0, to be precise).  I had to add an itoa       *
 *  function from the net (the function was buggy, so I had to fix  *
 *  it, too!).  I also made the program more friendly towards       *
 *  files with mixed-case filenames.                                *
 *                                                                  *
 ********************************************************************/

/********************************************************************
 *                                                                  *
 *  1/31/2005, by Paulo Soares                                      *
 *                                                                  *
 *  This code was integrated into iText.                            * 
 *  Note that the itoa function mentioned in the comment by Rod     *
 *  Smith is no longer in the code because Java has native support  *
 *  in PrintWriter to convert integers to strings                   *
 *                                                                  *
 ********************************************************************/

/********************************************************************
 *                                                                  *
 *  7/16/2005, by Bruno Lowagie                                     *
 *                                                                  *
 *  I solved an Eclipse Warning.                                    *
 *                                                                  *
 ********************************************************************/

/********************************************************************
 *                                                                  *
 *  9/14/2006, by Xavier Le Vourch                                  *
 *                                                                  *
 *  expand import clauses (import java.io.*)                        *                                           
 *  the removal of an exception in readString was restored on 9/16  *
 *                                                                  *
 ********************************************************************/
package com.lowagie.text.pdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import com.lowagie.text.error_messages.MessageLocalization;

/**
 * Converts a PFM file into an AFM file.
 */
public final class Pfm2afm {
    private RandomAccessFileOrArray in;
    private PrintWriter out;
    
    /** Creates a new instance of Pfm2afm */
    private Pfm2afm(RandomAccessFileOrArray in, OutputStream out) throws IOException {
        this.in = in;
        this.out = new PrintWriter(new OutputStreamWriter(out, "ISO-8859-1"));
    }
    
    /**
     * Converts a PFM file into an AFM file.
     * @param in the PFM file
     * @param out the AFM file
     * @throws IOException on error
     */    
    public static void convert(RandomAccessFileOrArray in, OutputStream out) throws IOException {
        Pfm2afm p = new Pfm2afm(in, out);
        p.openpfm();
        p.putheader();
        p.putchartab();
        p.putkerntab();
        p.puttrailer();
        p.out.flush();
    }
    
    public static void main(String[] args) {
        try {
            RandomAccessFileOrArray in = new RandomAccessFileOrArray(args[0]);
            OutputStream out = new FileOutputStream(args[1]);
            convert(in, out);
            in.close();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String readString(int n) throws IOException {
        byte b[] = new byte[n];
        in.readFully(b);
        int k;
        for (k = 0; k < b.length; ++k) {
            if (b[k] == 0)
                break;
        }
        return new String(b, 0, k, "ISO-8859-1");
    }
    
    private String readString() throws IOException {
        StringBuffer buf = new StringBuffer();
        while (true) {
            int c = in.read();
            if (c <= 0)
                break;
            buf.append((char)c);
        }
        return buf.toString();
    }
    
    private void outval(int n) {
        out.print(' ');
        out.print(n);
    }
    
    /*
     *  Output a character entry
     */
    private void  outchar(int code, int width, String name) {
        out.print("C ");
        outval(code);
        out.print(" ; WX ");
        outval(width);
        if (name != null) {
            out.print(" ; N ");
            out.print(name);
        }
        out.print(" ;\n");
    }
    
    private void openpfm() throws IOException {
        in.seek(0);
        vers = in.readShortLE();
        h_len = in.readIntLE();
        copyright = readString(60);
        type = in.readShortLE();
        points = in.readShortLE();
        verres = in.readShortLE();
        horres = in.readShortLE();
        ascent = in.readShortLE();
        intleading = in.readShortLE();
        extleading = in.readShortLE();
        italic = (byte)in.read();
        uline = (byte)in.read();
        overs = (byte)in.read();
        weight = in.readShortLE();
        charset = (byte)in.read();
        pixwidth = in.readShortLE();
        pixheight = in.readShortLE();
        kind = (byte)in.read();
        avgwidth = in.readShortLE();
        maxwidth = in.readShortLE();
        firstchar = in.read();
        lastchar = in.read();
        defchar = (byte)in.read();
        brkchar = (byte)in.read();
        widthby = in.readShortLE();
        device = in.readIntLE();
        face = in.readIntLE();
        bits = in.readIntLE();
        bitoff = in.readIntLE();
        extlen = in.readShortLE();
        psext = in.readIntLE();
        chartab = in.readIntLE();
        res1 = in.readIntLE();
        kernpairs = in.readIntLE();
        res2 = in.readIntLE();
        fontname = in.readIntLE();
        if (h_len != in.length() || extlen != 30 || fontname < 75 || fontname > 512)
            throw new IOException(MessageLocalization.getComposedMessage("not.a.valid.pfm.file"));
        in.seek(psext + 14);
        capheight = in.readShortLE();
        xheight = in.readShortLE();
        ascender = in.readShortLE();
        descender = in.readShortLE();
    }
    
    private void putheader() throws IOException {
        out.print("StartFontMetrics 2.0\n");
        if (copyright.length() > 0)
            out.print("Comment " + copyright + '\n');
        out.print("FontName ");
        in.seek(fontname);
        String fname = readString();
        out.print(fname);
        out.print("\nEncodingScheme ");
        if (charset != 0)
            out.print("FontSpecific\n");
        else
            out.print("AdobeStandardEncoding\n");
        /*
         * The .pfm is missing full name, so construct from font name by
         * changing the hyphen to a space.  This actually works in a lot
         * of cases.
         */
        out.print("FullName " + fname.replace('-', ' '));
        if (face != 0) {
            in.seek(face);
            out.print("\nFamilyName " + readString());
        }

        out.print("\nWeight ");
        if (weight > 475 || fname.toLowerCase().indexOf("bold") >= 0)
           out.print("Bold");
        else if ((weight < 325 && weight != 0) || fname.toLowerCase().indexOf("light") >= 0)
            out.print("Light");
        else if (fname.toLowerCase().indexOf("black") >= 0)
            out.print("Black");
        else 
            out.print("Medium");

        out.print("\nItalicAngle ");
        if (italic != 0 || fname.toLowerCase().indexOf("italic") >= 0)
            out.print("-12.00");
            /* this is a typical value; something else may work better for a
               specific font */
        else
            out.print("0");

        /*
         *  The mono flag in the pfm actually indicates whether there is a
         *  table of font widths, not if they are all the same.
         */
        out.print("\nIsFixedPitch ");
        if ((kind & 1) == 0 ||                  /* Flag for mono */
            avgwidth == maxwidth ) {  /* Avg width = max width */
            out.print("true");
            isMono = true;
        }
        else {
            out.print("false");
            isMono = false;
        }

        /*
         * The font bounding box is lost, but try to reconstruct it.
         * Much of this is just guess work.  The bounding box is required in
         * the .afm, but is not used by the PM font installer.
         */
        out.print("\nFontBBox");
        if (isMono)
            outval(-20);      /* Just guess at left bounds */
        else 
            outval(-100);
        outval(-(descender+5));  /* Descender is given as positive value */
        outval(maxwidth+10);
        outval(ascent+5);

        /*
         * Give other metrics that were kept
         */
        out.print("\nCapHeight");
        outval(capheight);
        out.print("\nXHeight");
        outval(xheight);
        out.print("\nDescender");
        outval(descender);
        out.print("\nAscender");
        outval(ascender);
        out.print('\n');
    }
    
    private void putchartab() throws IOException {
        int count = lastchar - firstchar + 1;
        int ctabs[] = new int[count];
        in.seek(chartab);
        for (int k = 0; k < count; ++k)
            ctabs[k] = in.readUnsignedShortLE();
        int back[] = new int[256];
        if (charset == 0) {
            for (int i = firstchar; i <= lastchar; ++i) {
                if (Win2PSStd[i] != 0)
                    back[Win2PSStd[i]] = i;
            }
        }
        /* Put out the header */
        out.print("StartCharMetrics");
        outval(count);
        out.print('\n');

        /* Put out all encoded chars */
        if (charset != 0) {
        /*
         * If the charset is not the Windows standard, just put out
         * unnamed entries.
         */
            for (int i = firstchar; i <= lastchar; i++) {
                if (ctabs[i - firstchar] != 0) {
                    outchar(i, ctabs[i - firstchar], null);
                }
            }
        }
        else {
            for (int i = 0; i < 256; i++) {
                int j = back[i];
                if (j != 0) {
                    outchar(i, ctabs[j - firstchar], WinChars[j]);
                    ctabs[j - firstchar] = 0;
                }
            }
            /* Put out all non-encoded chars */
            for (int i = firstchar; i <= lastchar; i++) {
                if (ctabs[i - firstchar] != 0) {
                    outchar(-1, ctabs[i - firstchar], WinChars[i]);
                }
            }
        }
        /* Put out the trailer */
        out.print("EndCharMetrics\n");
        
    }
    
    private void putkerntab() throws IOException {
        if (kernpairs == 0)
            return;
        in.seek(kernpairs);
        int count = in.readUnsignedShortLE();
        int nzero = 0;
        int kerns[] = new int[count * 3];
        for (int k = 0; k < kerns.length;) {
            kerns[k++] = in.read();
            kerns[k++] = in.read();
            if ((kerns[k++] = in.readShortLE()) != 0)
                ++nzero;
        }
        if (nzero == 0)
            return;
        out.print("StartKernData\nStartKernPairs");
        outval(nzero);
        out.print('\n');
        for (int k = 0; k < kerns.length; k += 3) {
            if (kerns[k + 2] != 0) {
                out.print("KPX ");
                out.print(WinChars[kerns[k]]);
                out.print(' ');
                out.print(WinChars[kerns[k + 1]]);
                outval(kerns[k + 2]);
                out.print('\n');
            }
        }
        /* Put out trailer */
        out.print("EndKernPairs\nEndKernData\n");
    }
    

    private void  puttrailer() {
        out.print("EndFontMetrics\n");
    }

    private short  vers;
    private int   h_len;             /* Total length of .pfm file */
    private String   copyright;   /* Copyright string [60]*/
    private short  type;
    private short  points;
    private short  verres;
    private short  horres;
    private short  ascent;
    private short  intleading;
    private short  extleading;
    private byte   italic;
    private byte   uline;
    private byte   overs;
    private short  weight;
    private byte   charset;         /* 0=windows, otherwise nomap */
    private short  pixwidth;        /* Width for mono fonts */
    private short  pixheight;
    private byte   kind;            /* Lower bit off in mono */
    private short  avgwidth;        /* Mono if avg=max width */
    private short  maxwidth;        /* Use to compute bounding box */
    private int   firstchar;       /* First char in table */
    private int   lastchar;        /* Last char in table */
    private byte   defchar;
    private byte   brkchar;
    private short  widthby;
    private int   device;
    private int   face;            /* Face name */
    private int   bits;
    private int   bitoff;
    private short  extlen;
    private int   psext;           /* PostScript extension */
    private int   chartab;         /* Character width tables */
    private int   res1;
    private int   kernpairs;       /* Kerning pairs */
    private int   res2;
    private int   fontname;        /* Font name */

/*
 *  Some metrics from the PostScript extension
 */
    private short  capheight;       /* Cap height */
    private short  xheight;         /* X height */
    private short  ascender;        /* Ascender */
    private short  descender;       /* Descender (positive) */

    
    private boolean isMono;
/**
 * Translate table from 1004 to psstd.  1004 is an extension of the
 * Windows translate table used in PM.
 */
    private int Win2PSStd[] = {
        0,   0,   0,   0, 197, 198, 199,   0, 202,   0,   205, 206, 207, 0,   0,   0,   // 00
        0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   // 10
        32,  33,  34,  35,  36,  37,  38, 169,  40,  41,  42,  43,  44,  45,  46,  47,  // 20
        48,  49,  50,  51,  52,  53,  54,  55,  56,  57,  58,  59,  60,  61,  62,  63,  // 30
        64,  65,  66,  67,  68,  69,  70,  71,  72,  73,  74,  75,  76,  77,  78,  79,  // 40
        80,  81,  82,  83,  84,  85,  86,  87,  88,  89,  90,  91,  92,  93,  94,  95,  // 50
        193, 97,  98,  99,  100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, // 60
        112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, // 70
        0,   0,   184, 166, 185, 188, 178, 179, 195, 189, 0,   172, 234, 0,   0,   0,   // 80
        0,   96,  0,   170, 186, 183, 177, 208, 196, 0,   0,   173, 250, 0,   0,   0,   // 90
        0,   161, 162, 163, 168, 165, 0,   167, 200, 0,   227, 171, 0,   0,   0,   197, // A0
        0,   0,   0,   0,   194, 0,   182, 180, 203, 0,   235, 187, 0,   0,   0,   191, // B0
        0,   0,   0,   0,   0,   0,   225, 0,   0,   0,   0,   0,   0,   0,   0,   0,   // C0
        0,   0,   0,   0,   0,   0,   0,   0,   233, 0,   0,   0,   0,   0,   0,   251, // D0
        0,   0,   0,   0,   0,   0,   241, 0,   0,   0,   0,   0,   0,   0,   0,   0,   // E0
        0,   0,   0,   0,   0,   0,   0,   0,   249, 0,   0,   0,   0,   0,   0,   0    // F0
    };
    
/**
 *  Character class.  This is a minor attempt to overcome the problem that
 *  in the pfm file, all unused characters are given the width of space.
 *  Note that this array isn't used in iText.
 */
    private int WinClass[] = {
        0, 0, 0, 0, 2, 2, 2, 0, 2, 0, 2, 2, 2, 0, 0, 0,   /* 00 */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,   /* 10 */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,   /* 20 */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,   /* 30 */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,   /* 40 */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,   /* 50 */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,   /* 60 */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2,   /* 70 */
        0, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0,   /* 80 */
        0, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2,   /* 90 */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,   /* a0 */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,   /* b0 */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,   /* c0 */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,   /* d0 */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,   /* e0 */
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1   /* f0 */
    };
    
/**
 *  Windows character names.  Give a name to the used locations
 *  for when the all flag is specified.
 */
    private String WinChars[] = {
        "W00",              /*   00    */
        "W01",              /*   01    */
        "W02",              /*   02    */
        "W03",              /*   03    */
        "macron",           /*   04    */
        "breve",            /*   05    */
        "dotaccent",        /*   06    */
        "W07",              /*   07    */
        "ring",             /*   08    */
        "W09",              /*   09    */
        "W0a",              /*   0a    */
        "W0b",              /*   0b    */
        "W0c",              /*   0c    */
        "W0d",              /*   0d    */
        "W0e",              /*   0e    */
        "W0f",              /*   0f    */
        "hungarumlaut",     /*   10    */
        "ogonek",           /*   11    */
        "caron",            /*   12    */
        "W13",              /*   13    */
        "W14",              /*   14    */
        "W15",              /*   15    */
        "W16",              /*   16    */
        "W17",              /*   17    */
        "W18",              /*   18    */
        "W19",              /*   19    */
        "W1a",              /*   1a    */
        "W1b",              /*   1b    */
        "W1c",              /*   1c    */
        "W1d",              /*   1d    */
        "W1e",              /*   1e    */
        "W1f",              /*   1f    */
        "space",            /*   20    */
        "exclam",           /*   21    */
        "quotedbl",         /*   22    */
        "numbersign",       /*   23    */
        "dollar",           /*   24    */
        "percent",          /*   25    */
        "ampersand",        /*   26    */
        "quotesingle",      /*   27    */
        "parenleft",        /*   28    */
        "parenright",       /*   29    */
        "asterisk",         /*   2A    */
        "plus",             /*   2B    */
        "comma",            /*   2C    */
        "hyphen",           /*   2D    */
        "period",           /*   2E    */
        "slash",            /*   2F    */
        "zero",             /*   30    */
        "one",              /*   31    */
        "two",              /*   32    */
        "three",            /*   33    */
        "four",             /*   34    */
        "five",             /*   35    */
        "six",              /*   36    */
        "seven",            /*   37    */
        "eight",            /*   38    */
        "nine",             /*   39    */
        "colon",            /*   3A    */
        "semicolon",        /*   3B    */
        "less",             /*   3C    */
        "equal",            /*   3D    */
        "greater",          /*   3E    */
        "question",         /*   3F    */
        "at",               /*   40    */
        "A",                /*   41    */
        "B",                /*   42    */
        "C",                /*   43    */
        "D",                /*   44    */
        "E",                /*   45    */
        "F",                /*   46    */
        "G",                /*   47    */
        "H",                /*   48    */
        "I",                /*   49    */
        "J",                /*   4A    */
        "K",                /*   4B    */
        "L",                /*   4C    */
        "M",                /*   4D    */
        "N",                /*   4E    */
        "O",                /*   4F    */
        "P",                /*   50    */
        "Q",                /*   51    */
        "R",                /*   52    */
        "S",                /*   53    */
        "T",                /*   54    */
        "U",                /*   55    */
        "V",                /*   56    */
        "W",                /*   57    */
        "X",                /*   58    */
        "Y",                /*   59    */
        "Z",                /*   5A    */
        "bracketleft",      /*   5B    */
        "backslash",        /*   5C    */
        "bracketright",     /*   5D    */
        "asciicircum",      /*   5E    */
        "underscore",       /*   5F    */
        "grave",            /*   60    */
        "a",                /*   61    */
        "b",                /*   62    */
        "c",                /*   63    */
        "d",                /*   64    */
        "e",                /*   65    */
        "f",                /*   66    */
        "g",                /*   67    */
        "h",                /*   68    */
        "i",                /*   69    */
        "j",                /*   6A    */
        "k",                /*   6B    */
        "l",                /*   6C    */
        "m",                /*   6D    */
        "n",                /*   6E    */
        "o",                /*   6F    */
        "p",                /*   70    */
        "q",                /*   71    */
        "r",                /*   72    */
        "s",                /*   73    */
        "t",                /*   74    */
        "u",                /*   75    */
        "v",                /*   76    */
        "w",                /*   77    */
        "x",                /*   78    */
        "y",                /*   79    */
        "z",                /*   7A    */
        "braceleft",        /*   7B    */
        "bar",              /*   7C    */
        "braceright",       /*   7D    */
        "asciitilde",       /*   7E    */
        "W7f",              /*   7F    */
        "euro",             /*   80    */
        "W81",              /*   81    */
        "quotesinglbase",   /*   82    */
        "florin",           /*   83    */
        "quotedblbase",     /*   84    */
        "ellipsis",         /*   85    */
        "dagger",           /*   86    */
        "daggerdbl",        /*   87    */
        "circumflex",       /*   88    */
        "perthousand",      /*   89    */
        "Scaron",           /*   8A    */
        "guilsinglleft",    /*   8B    */
        "OE",               /*   8C    */
        "W8d",              /*   8D    */
        "Zcaron",           /*   8E    */
        "W8f",              /*   8F    */
        "W90",              /*   90    */
        "quoteleft",        /*   91    */
        "quoteright",       /*   92    */
        "quotedblleft",     /*   93    */
        "quotedblright",    /*   94    */
        "bullet",           /*   95    */
        "endash",           /*   96    */
        "emdash",           /*   97    */
        "tilde",            /*   98    */
        "trademark",        /*   99    */
        "scaron",           /*   9A    */
        "guilsinglright",   /*   9B    */
        "oe",               /*   9C    */
        "W9d",              /*   9D    */
        "zcaron",           /*   9E    */
        "Ydieresis",        /*   9F    */
        "reqspace",         /*   A0    */
        "exclamdown",       /*   A1    */
        "cent",             /*   A2    */
        "sterling",         /*   A3    */
        "currency",         /*   A4    */
        "yen",              /*   A5    */
        "brokenbar",        /*   A6    */
        "section",          /*   A7    */
        "dieresis",         /*   A8    */
        "copyright",        /*   A9    */
        "ordfeminine",      /*   AA    */
        "guillemotleft",    /*   AB    */
        "logicalnot",       /*   AC    */
        "syllable",         /*   AD    */
        "registered",       /*   AE    */
        "macron",           /*   AF    */
        "degree",           /*   B0    */
        "plusminus",        /*   B1    */
        "twosuperior",      /*   B2    */
        "threesuperior",    /*   B3    */
        "acute",            /*   B4    */
        "mu",               /*   B5    */
        "paragraph",        /*   B6    */
        "periodcentered",   /*   B7    */
        "cedilla",          /*   B8    */
        "onesuperior",      /*   B9    */
        "ordmasculine",     /*   BA    */
        "guillemotright",   /*   BB    */
        "onequarter",       /*   BC    */
        "onehalf",          /*   BD    */
        "threequarters",    /*   BE    */
        "questiondown",     /*   BF    */
        "Agrave",           /*   C0    */
        "Aacute",           /*   C1    */
        "Acircumflex",      /*   C2    */
        "Atilde",           /*   C3    */
        "Adieresis",        /*   C4    */
        "Aring",            /*   C5    */
        "AE",               /*   C6    */
        "Ccedilla",         /*   C7    */
        "Egrave",           /*   C8    */
        "Eacute",           /*   C9    */
        "Ecircumflex",      /*   CA    */
        "Edieresis",        /*   CB    */
        "Igrave",           /*   CC    */
        "Iacute",           /*   CD    */
        "Icircumflex",      /*   CE    */
        "Idieresis",        /*   CF    */
        "Eth",              /*   D0    */
        "Ntilde",           /*   D1    */
        "Ograve",           /*   D2    */
        "Oacute",           /*   D3    */
        "Ocircumflex",      /*   D4    */
        "Otilde",           /*   D5    */
        "Odieresis",        /*   D6    */
        "multiply",         /*   D7    */
        "Oslash",           /*   D8    */
        "Ugrave",           /*   D9    */
        "Uacute",           /*   DA    */
        "Ucircumflex",      /*   DB    */
        "Udieresis",        /*   DC    */
        "Yacute",           /*   DD    */
        "Thorn",            /*   DE    */
        "germandbls",       /*   DF    */
        "agrave",           /*   E0    */
        "aacute",           /*   E1    */
        "acircumflex",      /*   E2    */
        "atilde",           /*   E3    */
        "adieresis",        /*   E4    */
        "aring",            /*   E5    */
        "ae",               /*   E6    */
        "ccedilla",         /*   E7    */
        "egrave",           /*   E8    */
        "eacute",           /*   E9    */
        "ecircumflex",      /*   EA    */
        "edieresis",        /*   EB    */
        "igrave",           /*   EC    */
        "iacute",           /*   ED    */
        "icircumflex",      /*   EE    */
        "idieresis",        /*   EF    */
        "eth",              /*   F0    */
        "ntilde",           /*   F1    */
        "ograve",           /*   F2    */
        "oacute",           /*   F3    */
        "ocircumflex",      /*   F4    */
        "otilde",           /*   F5    */
        "odieresis",        /*   F6    */
        "divide",           /*   F7    */
        "oslash",           /*   F8    */
        "ugrave",           /*   F9    */
        "uacute",           /*   FA    */
        "ucircumflex",      /*   FB    */
        "udieresis",        /*   FC    */
        "yacute",           /*   FD    */
        "thorn",            /*   FE    */
        "ydieresis"         /*   FF    */
    };
}
