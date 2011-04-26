/*
 * Copyright 2005 by Paulo Soares.
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

import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Rectangle;

/**
 * Creates a radio or a check field.
 * <p>
 * Example usage:
 * <p>
 * <PRE>
 * Document document = new Document(PageSize.A4, 50, 50, 50, 50);
 * PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("output.pdf"));
 * document.open();
 * PdfContentByte cb = writer.getDirectContent();
 * RadioCheckField bt = new RadioCheckField(writer, new Rectangle(100, 100, 200, 200), "radio", "v1");
 * bt.setCheckType(RadioCheckField.TYPE_CIRCLE);
 * bt.setBackgroundColor(Color.cyan);
 * bt.setBorderStyle(PdfBorderDictionary.STYLE_SOLID);
 * bt.setBorderColor(Color.red);
 * bt.setTextColor(Color.yellow);
 * bt.setBorderWidth(BaseField.BORDER_WIDTH_THICK);
 * bt.setChecked(false);
 * PdfFormField f1 = bt.getRadioField();
 * bt.setOnValue("v2");
 * bt.setChecked(true);
 * bt.setBox(new Rectangle(100, 300, 200, 400));
 * PdfFormField f2 = bt.getRadioField();
 * bt.setChecked(false);
 * PdfFormField top = bt.getRadioGroup(true, false);
 * bt.setOnValue("v3");
 * bt.setBox(new Rectangle(100, 500, 200, 600));
 * PdfFormField f3 = bt.getRadioField();
 * top.addKid(f1);
 * top.addKid(f2);
 * top.addKid(f3);
 * writer.addAnnotation(top);
 * bt = new RadioCheckField(writer, new Rectangle(300, 300, 400, 400), "check1", "Yes");
 * bt.setCheckType(RadioCheckField.TYPE_CHECK);
 * bt.setBorderWidth(BaseField.BORDER_WIDTH_THIN);
 * bt.setBorderColor(Color.black);
 * bt.setBackgroundColor(Color.white);
 * PdfFormField ck = bt.getCheckField();
 * writer.addAnnotation(ck);
 * document.close();
 * </PRE>
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class RadioCheckField extends BaseField {

    /** A field with the symbol check */
    public static final int TYPE_CHECK = 1;
    /** A field with the symbol circle */
    public static final int TYPE_CIRCLE = 2;
    /** A field with the symbol cross */
    public static final int TYPE_CROSS = 3;
    /** A field with the symbol diamond */
    public static final int TYPE_DIAMOND = 4;
    /** A field with the symbol square */
    public static final int TYPE_SQUARE = 5;
    /** A field with the symbol star */
    public static final int TYPE_STAR = 6;
    
    private static String typeChars[] = {"4", "l", "8", "u", "n", "H"};
    
    /**
     * Holds value of property checkType.
     */
    private int checkType;
    
    /**
     * Holds value of property onValue.
     */
    private String onValue;
    
    /**
     * Holds value of property checked.
     */
    private boolean checked;
    
    /**
     * Creates a new instance of RadioCheckField
     * @param writer the document <CODE>PdfWriter</CODE>
     * @param box the field location and dimensions
     * @param fieldName the field name. It must not be <CODE>null</CODE>
     * @param onValue the value when the field is checked
     */
    public RadioCheckField(PdfWriter writer, Rectangle box, String fieldName, String onValue) {
        super(writer, box, fieldName);
        setOnValue(onValue);
        setCheckType(TYPE_CIRCLE);
    }
    
    /**
     * Getter for property checkType.
     * @return Value of property checkType.
     */
    public int getCheckType() {
        return this.checkType;
    }
    
    /**
     * Sets the checked symbol. It can be
     * <CODE>TYPE_CHECK</CODE>,
     * <CODE>TYPE_CIRCLE</CODE>,
     * <CODE>TYPE_CROSS</CODE>,
     * <CODE>TYPE_DIAMOND</CODE>,
     * <CODE>TYPE_SQUARE</CODE> and
     * <CODE>TYPE_STAR</CODE>.
     * @param checkType the checked symbol
     */
    public void setCheckType(int checkType) {
        if (checkType < TYPE_CHECK || checkType > TYPE_STAR)
            checkType = TYPE_CIRCLE;
        this.checkType = checkType;
        setText(typeChars[checkType - 1]);
        try {
            setFont(BaseFont.createFont(BaseFont.ZAPFDINGBATS, BaseFont.WINANSI, false));
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
    
    /**
     * Getter for property onValue.
     * @return Value of property onValue.
     */
    public String getOnValue() {
        return this.onValue;
    }
    
    /**
     * Sets the value when the field is checked.
     * @param onValue the value when the field is checked
     */
    public void setOnValue(String onValue) {
        this.onValue = onValue;
    }
    
    /**
     * Getter for property checked.
     * @return Value of property checked.
     */
    public boolean isChecked() {
        return this.checked;
    }
    
    /**
     * Sets the state of the field to checked or unchecked.
     * @param checked the state of the field, <CODE>true</CODE> for checked
     * and <CODE>false</CODE> for unchecked
     */
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    
    /**
     * Gets the field appearance.
     * @param isRadio <CODE>true</CODE> for a radio field and <CODE>false</CODE>
     * for a check field
     * @param on <CODE>true</CODE> for the checked state, <CODE>false</CODE>
     * otherwise
     * @throws IOException on error
     * @throws DocumentException on error
     * @return the appearance
     */    
    public PdfAppearance getAppearance(boolean isRadio, boolean on) throws IOException, DocumentException {
        if (isRadio && checkType == TYPE_CIRCLE)
            return getAppearanceRadioCircle(on);
        PdfAppearance app = getBorderAppearance();
        if (!on)
            return app;
        BaseFont ufont = getRealFont();
        boolean borderExtra = borderStyle == PdfBorderDictionary.STYLE_BEVELED || borderStyle == PdfBorderDictionary.STYLE_INSET;
        float h = box.getHeight() - borderWidth * 2;
        float bw2 = borderWidth;
        if (borderExtra) {
            h -= borderWidth * 2;
            bw2 *= 2;
        }
        float offsetX = (borderExtra ? 2 * borderWidth : borderWidth);
        offsetX = Math.max(offsetX, 1);
        float offX = Math.min(bw2, offsetX);
        float wt = box.getWidth() - 2 * offX;
        float ht = box.getHeight() - 2 * offX;
        float fsize = fontSize;
        if (fsize == 0) {
            float bw = ufont.getWidthPoint(text, 1);
            if (bw == 0)
                fsize = 12;
            else
                fsize = wt / bw;
            float nfsize = h / (ufont.getFontDescriptor(BaseFont.ASCENT, 1));
            fsize = Math.min(fsize, nfsize);
        }
        app.saveState();
        app.rectangle(offX, offX, wt, ht);
        app.clip();
        app.newPath();
        if (textColor == null)
            app.resetGrayFill();
        else
            app.setColorFill(textColor);
        app.beginText();
        app.setFontAndSize(ufont, fsize);
        app.setTextMatrix((box.getWidth() - ufont.getWidthPoint(text, fsize)) / 2, 
            (box.getHeight() - ufont.getAscentPoint(text, fsize)) / 2);
        app.showText(text);
        app.endText();
        app.restoreState();
        return app;
    }

    /**
     * Gets the special field appearance for the radio circle.
     * @param on <CODE>true</CODE> for the checked state, <CODE>false</CODE>
     * otherwise
     * @return the appearance
     */    
    public PdfAppearance getAppearanceRadioCircle(boolean on) {
        PdfAppearance app = PdfAppearance.createAppearance(writer, box.getWidth(), box.getHeight());
        switch (rotation) {
            case 90:
                app.setMatrix(0, 1, -1, 0, box.getHeight(), 0);
                break;
            case 180:
                app.setMatrix(-1, 0, 0, -1, box.getWidth(), box.getHeight());
                break;
            case 270:
                app.setMatrix(0, -1, 1, 0, 0, box.getWidth());
                break;
        }
        Rectangle box = new Rectangle(app.getBoundingBox());
        float cx = box.getWidth() / 2;
        float cy = box.getHeight() / 2;
        float r = (Math.min(box.getWidth(), box.getHeight()) - borderWidth) / 2;
        if (r <= 0)
            return app;
        if (backgroundColor != null) {
            app.setColorFill(backgroundColor);
            app.circle(cx, cy, r + borderWidth / 2);
            app.fill();
        }
        if (borderWidth > 0 && borderColor != null) {
            app.setLineWidth(borderWidth);
            app.setColorStroke(borderColor);
            app.circle(cx, cy, r);
            app.stroke();
        }
        if (on) {
            if (textColor == null)
                app.resetGrayFill();
            else
                app.setColorFill(textColor);
            app.circle(cx, cy, r / 2);
            app.fill();
        }
        return app;
    }
    
    /**
     * Gets a radio group. It's composed of the field specific keys, without the widget
     * ones. This field is to be used as a field aggregator with {@link PdfFormField#addKid(PdfFormField) addKid()}.
     * @param noToggleToOff if <CODE>true</CODE>, exactly one radio button must be selected at all
     * times; clicking the currently selected button has no effect.
     * If <CODE>false</CODE>, clicking
     * the selected button deselects it, leaving no button selected.
     * @param radiosInUnison if <CODE>true</CODE>, a group of radio buttons within a radio button field that
     * use the same value for the on state will turn on and off in unison; that is if
     * one is checked, they are all checked. If <CODE>false</CODE>, the buttons are mutually exclusive
     * (the same behavior as HTML radio buttons)
     * @return the radio group
     */    
    public PdfFormField getRadioGroup(boolean noToggleToOff, boolean radiosInUnison) {
        PdfFormField field = PdfFormField.createRadioButton(writer, noToggleToOff);
        if (radiosInUnison)
            field.setFieldFlags(PdfFormField.FF_RADIOSINUNISON);
        field.setFieldName(fieldName);
        if ((options & READ_ONLY) != 0)
            field.setFieldFlags(PdfFormField.FF_READ_ONLY);
        if ((options & REQUIRED) != 0)
            field.setFieldFlags(PdfFormField.FF_REQUIRED);
        field.setValueAsName(checked ? onValue : "Off");
        return field;
    }
    
    /**
     * Gets the radio field. It's only composed of the widget keys and must be used
     * with {@link #getRadioGroup(boolean,boolean)}.
     * @return the radio field
     * @throws IOException on error
     * @throws DocumentException on error
     */    
    public PdfFormField getRadioField() throws IOException, DocumentException {
        return getField(true);
    }
    
    /**
     * Gets the check field.
     * @return the check field
     * @throws IOException on error
     * @throws DocumentException on error
     */    
    public PdfFormField getCheckField() throws IOException, DocumentException {
        return getField(false);
    }
    
    /**
     * Gets a radio or check field.
     * @param isRadio <CODE>true</CODE> to get a radio field, <CODE>false</CODE> to get
     * a check field
     * @throws IOException on error
     * @throws DocumentException on error
     * @return the field
     */    
    protected PdfFormField getField(boolean isRadio) throws IOException, DocumentException {
        PdfFormField field = null;
        if (isRadio)
            field = PdfFormField.createEmpty(writer);
        else
            field = PdfFormField.createCheckBox(writer);
        field.setWidget(box, PdfAnnotation.HIGHLIGHT_INVERT);
        if (!isRadio) {
            field.setFieldName(fieldName);
            if ((options & READ_ONLY) != 0)
                field.setFieldFlags(PdfFormField.FF_READ_ONLY);
            if ((options & REQUIRED) != 0)
                field.setFieldFlags(PdfFormField.FF_REQUIRED);
            field.setValueAsName(checked ? onValue : "Off");
            setCheckType(TYPE_CHECK);
        }
        if (text != null)
            field.setMKNormalCaption(text);
        if (rotation != 0)
            field.setMKRotation(rotation);
        field.setBorderStyle(new PdfBorderDictionary(borderWidth, borderStyle, new PdfDashPattern(3)));
        PdfAppearance tpon = getAppearance(isRadio, true);
        PdfAppearance tpoff = getAppearance(isRadio, false);
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, onValue, tpon);
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, "Off", tpoff);
        field.setAppearanceState(checked ? onValue : "Off");
        PdfAppearance da = (PdfAppearance)tpon.getDuplicate();
        da.setFontAndSize(getRealFont(), fontSize);
        if (textColor == null)
            da.setGrayFill(0);
        else
            da.setColorFill(textColor);
        field.setDefaultAppearanceString(da);
        if (borderColor != null)
            field.setMKBorderColor(borderColor);
        if (backgroundColor != null)
            field.setMKBackgroundColor(backgroundColor);
        switch (visibility) {
            case HIDDEN:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_HIDDEN);
                break;
            case VISIBLE_BUT_DOES_NOT_PRINT:
                break;
            case HIDDEN_BUT_PRINTABLE:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_NOVIEW);
                break;
            default:
                field.setFlags(PdfAnnotation.FLAGS_PRINT);
                break;
        }
        return field;
    }
}