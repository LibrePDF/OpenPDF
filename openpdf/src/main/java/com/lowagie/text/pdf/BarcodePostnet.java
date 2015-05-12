/*
 * Copyright 2002 by Paulo Soares.
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
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

import com.lowagie.text.Rectangle;

/** Implements the Postnet and Planet barcodes. The default parameters are:
 * <pre>
 *n = 72f / 22f; // distance between bars
 *x = 0.02f * 72f; // bar width
 *barHeight = 0.125f * 72f; // height of the tall bars
 *size = 0.05f * 72f; // height of the short bars
 *codeType = POSTNET; // type of code
 * </pre>
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class BarcodePostnet extends Barcode{

    /** The bars for each character.
     */    
	private static final byte BARS[][] = 
    {
        {1,1,0,0,0},
        {0,0,0,1,1},
        {0,0,1,0,1},
        {0,0,1,1,0},
        {0,1,0,0,1},
        {0,1,0,1,0},
        {0,1,1,0,0},
        {1,0,0,0,1},
        {1,0,0,1,0},
        {1,0,1,0,0}
    };
    
    /** Creates new BarcodePostnet */
    public BarcodePostnet() {
        n = 72f / 22f; // distance between bars
        x = 0.02f * 72f; // bar width
        barHeight = 0.125f * 72f; // height of the tall bars
        size = 0.05f * 72f; // height of the short bars
        codeType = POSTNET; // type of code
    }
    
    /** Creates the bars for Postnet.
     * @param text the code to be created without checksum
     * @return the bars
     */    
    public static byte[] getBarsPostnet(String text) {
        int total = 0;
        for (int k = text.length() - 1; k >= 0; --k) {
            int n = text.charAt(k) - '0';
            total += n;
        }
        text += (char)(((10 - (total % 10)) % 10) + '0');
        byte bars[] = new byte[text.length() * 5 + 2];
        bars[0] = 1;
        bars[bars.length - 1] = 1;
        for (int k = 0; k < text.length(); ++k) {
            int c = text.charAt(k) - '0';
            System.arraycopy(BARS[c], 0, bars, k * 5 + 1, 5);
        }
        return bars;
    }

    /** Gets the maximum area that the barcode and the text, if
     * any, will occupy. The lower left corner is always (0, 0).
     * @return the size the barcode occupies.
     */
    public Rectangle getBarcodeSize() {
        float width = ((code.length() + 1) * 5 + 1) * n + x;
        return new Rectangle(width, barHeight);
    }
    
    /** Places the barcode in a <CODE>PdfContentByte</CODE>. The
     * barcode is always placed at coordinates (0, 0). Use the
     * translation matrix to move it elsewhere.<p>
     * The bars and text are written in the following colors:<p>
     * <P><TABLE BORDER=1>
     * <TR>
     *   <TH><P><CODE>barColor</CODE></TH>
     *   <TH><P><CODE>textColor</CODE></TH>
     *   <TH><P>Result</TH>
     *   </TR>
     * <TR>
     *   <TD><P><CODE>null</CODE></TD>
     *   <TD><P><CODE>null</CODE></TD>
     *   <TD><P>bars and text painted with current fill color</TD>
     *   </TR>
     * <TR>
     *   <TD><P><CODE>barColor</CODE></TD>
     *   <TD><P><CODE>null</CODE></TD>
     *   <TD><P>bars and text painted with <CODE>barColor</CODE></TD>
     *   </TR>
     * <TR>
     *   <TD><P><CODE>null</CODE></TD>
     *   <TD><P><CODE>textColor</CODE></TD>
     *   <TD><P>bars painted with current color<br>text painted with <CODE>textColor</CODE></TD>
     *   </TR>
     * <TR>
     *   <TD><P><CODE>barColor</CODE></TD>
     *   <TD><P><CODE>textColor</CODE></TD>
     *   <TD><P>bars painted with <CODE>barColor</CODE><br>text painted with <CODE>textColor</CODE></TD>
     *   </TR>
     * </TABLE>
     * @param cb the <CODE>PdfContentByte</CODE> where the barcode will be placed
     * @param barColor the color of the bars. It can be <CODE>null</CODE>
     * @param textColor the color of the text. It can be <CODE>null</CODE>
     * @return the dimensions the barcode occupies
     */
    public Rectangle placeBarcode(PdfContentByte cb, Color barColor, Color textColor) {
        if (barColor != null)
            cb.setColorFill(barColor);
        byte bars[] = getBarsPostnet(code);
        byte flip = 1;
        if (codeType == PLANET) {
            flip = 0;
            bars[0] = 0;
            bars[bars.length - 1] = 0;
        }
        float startX = 0;
        for (int k = 0; k < bars.length; ++k) {
            cb.rectangle(startX, 0, x - inkSpreading, bars[k] == flip ? barHeight : size);
            startX += n;
        }
        cb.fill();
        return getBarcodeSize();
    }
    
    /** Creates a <CODE>java.awt.Image</CODE>. This image only
     * contains the bars without any text.
     * @param foreground the color of the bars
     * @param background the color of the background
     * @return the image
     *
     */
    public java.awt.Image createAwtImage(Color foreground, Color background) {
        int f = foreground.getRGB();
        int g = background.getRGB();
        Canvas canvas = new Canvas();
        int barWidth = (int)x;
        if (barWidth <= 0)
            barWidth = 1;
        int barDistance = (int)n;
        if (barDistance <= barWidth)
            barDistance = barWidth + 1;
        int barShort = (int)size;
        if (barShort <= 0)
            barShort = 1;
        int barTall = (int)barHeight;
        if (barTall <= barShort)
            barTall = barShort + 1;
        int width = ((code.length() + 1) * 5 + 1) * barDistance + barWidth;
        int pix[] = new int[width * barTall];
        byte bars[] = getBarsPostnet(code);
        byte flip = 1;
        if (codeType == PLANET) {
            flip = 0;
            bars[0] = 0;
            bars[bars.length - 1] = 0;
        }
        int idx = 0;
        for (int k = 0; k < bars.length; ++k) {
            boolean dot = (bars[k] == flip);
            for (int j = 0; j < barDistance; ++j) {
                pix[idx + j] = ((dot && j < barWidth) ? f : g);
            }
            idx += barDistance;
        }
        int limit = width * (barTall - barShort);
        for (int k = width; k < limit; k += width)
            System.arraycopy(pix, 0, pix, k, width);
        idx = limit;
        for (int k = 0; k < bars.length; ++k) {
            for (int j = 0; j < barDistance; ++j) {
                pix[idx + j] = ((j < barWidth) ? f : g);
            }
            idx += barDistance;
        }
        for (int k = limit + width; k < pix.length; k += width)
            System.arraycopy(pix, limit, pix, k, width);
        Image img = canvas.createImage(new MemoryImageSource(width, barTall, pix, 0, width));
        
        return img;
    }
}