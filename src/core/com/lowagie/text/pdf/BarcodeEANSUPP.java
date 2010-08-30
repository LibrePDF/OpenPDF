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
import java.awt.Color;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.Rectangle;

/** This class takes 2 barcodes, an EAN/UPC and a supplemental
 * and creates a single barcode with both combined in the
 * expected layout. The UPC/EAN should have a positive text
  * baseline and the supplemental a negative one (in the supplemental
 * the text is on the top of the barcode.<p>
 * The default parameters are:
 * <pre>
 *n = 8; // horizontal distance between the two barcodes
 * </pre>
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class BarcodeEANSUPP extends Barcode{
    
    /** The barcode with the EAN/UPC.
     */    
    protected Barcode ean;
    /** The barcode with the supplemental.
     */    
    protected Barcode supp;
    
    /** Creates new combined barcode.
     * @param ean the EAN/UPC barcode
     * @param supp the supplemental barcode
     */
    public BarcodeEANSUPP(Barcode ean, Barcode supp) {
        n = 8; // horizontal distance between the two barcodes
        this.ean = ean;
        this.supp = supp;
    }
    
    /** Gets the maximum area that the barcode and the text, if
     * any, will occupy. The lower left corner is always (0, 0).
     * @return the size the barcode occupies.
     */
    public Rectangle getBarcodeSize() {
        Rectangle rect = ean.getBarcodeSize();
        rect.setRight(rect.getWidth() + supp.getBarcodeSize().getWidth() + n);
        return rect;
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
        if (supp.getFont() != null)
            supp.setBarHeight(ean.getBarHeight() + supp.getBaseline() - supp.getFont().getFontDescriptor(BaseFont.CAPHEIGHT, supp.getSize()));
        else
            supp.setBarHeight(ean.getBarHeight());
        Rectangle eanR = ean.getBarcodeSize();
        cb.saveState();
        ean.placeBarcode(cb, barColor, textColor);
        cb.restoreState();
        cb.saveState();
        cb.concatCTM(1, 0, 0, 1, eanR.getWidth() + n, eanR.getHeight() - ean.getBarHeight());
        supp.placeBarcode(cb, barColor, textColor);
        cb.restoreState();
        return getBarcodeSize();
    }
    
    /** Creates a <CODE>java.awt.Image</CODE>. This image only
     * contains the bars without any text.
     * @param foreground the color of the bars
     * @param background the color of the background
     * @return the image
     */    
    public java.awt.Image createAwtImage(Color foreground, Color background) {
        throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("the.two.barcodes.must.be.composed.externally"));
    }    
}
