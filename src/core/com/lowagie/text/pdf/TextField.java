/*
 * Copyright 2003-2005 by Paulo Soares.
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
import java.io.IOException;
import java.util.ArrayList;

import com.lowagie.text.Chunk;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;

/**
 * Supports text, combo and list fields generating the correct appearances.
 * All the option in the Acrobat GUI are supported in an easy to use API.
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class TextField extends BaseField {
    
    /** Holds value of property defaultText. */
    private String defaultText;
    
    /** Holds value of property choices. */
    private String[] choices;
    
    /** Holds value of property choiceExports. */
    private String[] choiceExports;
    
    /** Holds value of property choiceSelection. */
    private ArrayList choiceSelections = new ArrayList();
    
    private int topFirst;
    
    private float extraMarginLeft;
    private float extraMarginTop;
    
    /**
     * Creates a new <CODE>TextField</CODE>.
     * @param writer the document <CODE>PdfWriter</CODE>
     * @param box the field location and dimensions
     * @param fieldName the field name. If <CODE>null</CODE> only the widget keys
     * will be included in the field allowing it to be used as a kid field.
     */
    public TextField(PdfWriter writer, Rectangle box, String fieldName) {
        super(writer, box, fieldName);
    }
    
    private static boolean checkRTL(String text) {
        if (text == null || text.length() == 0)
            return false;
        char[] cc = text.toCharArray();
        for (int k = 0; k < cc.length; ++k) {
            int c = cc[k];
            if (c >= 0x590 && c < 0x0780)
                return true;
        }
        return false;
    }
    
    private static void changeFontSize(Phrase p, float size) {
        for (int k = 0; k < p.size(); ++k)
            ((Chunk)p.get(k)).getFont().setSize(size);
    }
    
    private Phrase composePhrase(String text, BaseFont ufont, Color color, float fontSize) {
        Phrase phrase = null;
        if (extensionFont == null && (substitutionFonts == null || substitutionFonts.isEmpty()))
            phrase = new Phrase(new Chunk(text, new Font(ufont, fontSize, 0, color)));
        else {
            FontSelector fs = new FontSelector();
            fs.addFont(new Font(ufont, fontSize, 0, color));
            if (extensionFont != null)
                fs.addFont(new Font(extensionFont, fontSize, 0, color));
            if (substitutionFonts != null) {
                for (int k = 0; k < substitutionFonts.size(); ++k)
                    fs.addFont(new Font((BaseFont)substitutionFonts.get(k), fontSize, 0, color));
            }
            phrase = fs.process(text);
        }
        return phrase;
    }
    
    /**
     * Removes CRLF from a <code>String</code>.
     * 
     * @param text
     * @return String
     * @since	2.1.5
     */
    public static String removeCRLF(String text) {
        if (text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
            char[] p = text.toCharArray();
            StringBuffer sb = new StringBuffer(p.length);
            for (int k = 0; k < p.length; ++k) {
                char c = p[k];
                if (c == '\n')
                    sb.append(' ');
                else if (c == '\r') {
                    sb.append(' ');
                    if (k < p.length - 1 && p[k + 1] == '\n')
                        ++k;
                }
                else
                    sb.append(c);
            }
            return sb.toString();
        }
        return text;
    }
    
    /**
     * Obfuscates a password <code>String</code>.
     * Every character is replaced by an asterisk (*).
     * 
     * @param text 
     * @return String
     * @since	2.1.5
     */
    public static String obfuscatePassword(String text) {
    	char[] pchar = new char[text.length()];
    	for (int i = 0; i < text.length(); i++)
    		pchar[i] = '*';
    	return new String(pchar);
    }
    
    /**
     * Get the <code>PdfAppearance</code> of a text or combo field
     * @throws IOException on error
     * @throws DocumentException on error
     * @return A <code>PdfAppearance</code>
     */
    public PdfAppearance getAppearance() throws IOException, DocumentException {
        PdfAppearance app = getBorderAppearance();
        app.beginVariableText();
        if (text == null || text.length() == 0) {
            app.endVariableText();
            return app;
        }
        
        boolean borderExtra = borderStyle == PdfBorderDictionary.STYLE_BEVELED || borderStyle == PdfBorderDictionary.STYLE_INSET;
        float h = box.getHeight() - borderWidth * 2 - extraMarginTop;
        float bw2 = borderWidth;
        if (borderExtra) {
            h -= borderWidth * 2;
            bw2 *= 2;
        }
        float offsetX = Math.max(bw2, 1);
        float offX = Math.min(bw2, offsetX);
        app.saveState();
        app.rectangle(offX, offX, box.getWidth() - 2 * offX, box.getHeight() - 2 * offX);
        app.clip();
        app.newPath();
        String ptext;
        if ((options & PASSWORD) != 0)
        	ptext = obfuscatePassword(text);
        else if ((options & MULTILINE) == 0)
            ptext = removeCRLF(text);
        else
        	ptext = text; //fixed by Kazuya Ujihara (ujihara.jp)
        BaseFont ufont = getRealFont();
        Color fcolor = (textColor == null) ? GrayColor.GRAYBLACK : textColor;
        int rtl = checkRTL(ptext) ? PdfWriter.RUN_DIRECTION_LTR : PdfWriter.RUN_DIRECTION_NO_BIDI;
        float usize = fontSize;
        Phrase phrase = composePhrase(ptext, ufont, fcolor, usize);
        if ((options & MULTILINE) != 0) {
            float width = box.getWidth() - 4 * offsetX - extraMarginLeft;
            float factor = ufont.getFontDescriptor(BaseFont.BBOXURY, 1) - ufont.getFontDescriptor(BaseFont.BBOXLLY, 1);
            ColumnText ct = new ColumnText(null);
            if (usize == 0) {
                usize = h / factor;
                if (usize > 4) {
                    if (usize > 12)
                        usize = 12;
                    float step = Math.max((usize - 4) / 10, 0.2f);
                    ct.setSimpleColumn(0, -h, width, 0);
                    ct.setAlignment(alignment);
                    ct.setRunDirection(rtl);
                    for (; usize > 4; usize -= step) {
                        ct.setYLine(0);
                        changeFontSize(phrase, usize);
                        ct.setText(phrase);
                        ct.setLeading(factor * usize);
                        int status = ct.go(true);
                        if ((status & ColumnText.NO_MORE_COLUMN) == 0)
                            break;
                    }
                }
                if (usize < 4)
                    usize = 4;
            }
            changeFontSize(phrase, usize);
            ct.setCanvas(app);
            float leading = usize * factor;
            float offsetY = offsetX + h - ufont.getFontDescriptor(BaseFont.BBOXURY, usize);
            ct.setSimpleColumn(extraMarginLeft + 2 * offsetX, -20000, box.getWidth() - 2 * offsetX, offsetY + leading);
            ct.setLeading(leading);
            ct.setAlignment(alignment);
            ct.setRunDirection(rtl);
            ct.setText(phrase);
            ct.go();
        }
        else {
            if (usize == 0) {
                float maxCalculatedSize = h / (ufont.getFontDescriptor(BaseFont.BBOXURX, 1) - ufont.getFontDescriptor(BaseFont.BBOXLLY, 1));
                changeFontSize(phrase, 1);
                float wd = ColumnText.getWidth(phrase, rtl, 0);
                if (wd == 0)
                    usize = maxCalculatedSize;
                else
                	usize = Math.min(maxCalculatedSize, (box.getWidth() - extraMarginLeft - 4 * offsetX) / wd);
                if (usize < 4)
                    usize = 4;
            }
            changeFontSize(phrase, usize);
            float offsetY = offX + ((box.getHeight() - 2*offX) - ufont.getFontDescriptor(BaseFont.ASCENT, usize)) / 2;
            if (offsetY < offX)
                offsetY = offX;
            if (offsetY - offX < -ufont.getFontDescriptor(BaseFont.DESCENT, usize)) {
                float ny = -ufont.getFontDescriptor(BaseFont.DESCENT, usize) + offX;
                float dy = box.getHeight() - offX - ufont.getFontDescriptor(BaseFont.ASCENT, usize);
                offsetY = Math.min(ny, Math.max(offsetY, dy));
            }
            if ((options & COMB) != 0 && maxCharacterLength > 0) {
                int textLen = Math.min(maxCharacterLength, ptext.length());
                int position = 0;
                if (alignment == Element.ALIGN_RIGHT)
                    position = maxCharacterLength - textLen;
                else if (alignment == Element.ALIGN_CENTER)
                    position = (maxCharacterLength - textLen) / 2;
                float step = (box.getWidth() - extraMarginLeft) / maxCharacterLength;
                float start = step / 2 + position * step;
                if (textColor == null)
                    app.setGrayFill(0);
                else
                    app.setColorFill(textColor);
                app.beginText();
                for (int k = 0; k < phrase.size(); ++k) {
                    Chunk ck = (Chunk)phrase.get(k);
                    BaseFont bf = ck.getFont().getBaseFont();
                    app.setFontAndSize(bf, usize);
                    StringBuffer sb = ck.append("");
                    for (int j = 0; j < sb.length(); ++j) {
                        String c = sb.substring(j, j + 1);
                        float wd = bf.getWidthPoint(c, usize);
                        app.setTextMatrix(extraMarginLeft + start - wd / 2, offsetY - extraMarginTop);
                        app.showText(c);
                        start += step;
                    }
                }
                app.endText();
            }
            else {
            	float x;
            	switch (alignment) {
            	case Element.ALIGN_RIGHT:
            		x = extraMarginLeft + box.getWidth() - (2 * offsetX);
            		break;
            	case Element.ALIGN_CENTER:
            		x = extraMarginLeft + (box.getWidth() / 2);
            		break;
            	default:
            		x = extraMarginLeft + (2 * offsetX);
            	}
            	ColumnText.showTextAligned(app, alignment, phrase, x, offsetY - extraMarginTop, 0, rtl, 0);
            }
        }
        app.restoreState();
        app.endVariableText();
        return app;
    }

    /**
     * Get the <code>PdfAppearance</code> of a list field
     * @throws IOException on error
     * @throws DocumentException on error
     * @return A <code>PdfAppearance</code>
     */
    PdfAppearance getListAppearance() throws IOException, DocumentException {
        PdfAppearance app = getBorderAppearance();
        if (choices == null || choices.length == 0) {
            return app;
        }
        app.beginVariableText();
        
        int topChoice = getTopChoice();

        BaseFont ufont = getRealFont();
        float usize = fontSize;
        if (usize == 0)
            usize = 12;

        boolean borderExtra = borderStyle == PdfBorderDictionary.STYLE_BEVELED || borderStyle == PdfBorderDictionary.STYLE_INSET;
        float h = box.getHeight() - borderWidth * 2;
        float offsetX = borderWidth;
        if (borderExtra) {
            h -= borderWidth * 2;
            offsetX *= 2;
        }
        
        float leading = ufont.getFontDescriptor(BaseFont.BBOXURY, usize) - ufont.getFontDescriptor(BaseFont.BBOXLLY, usize);
        int maxFit = (int)(h / leading) + 1;
        int first = 0;
        int last = 0;
        first = topChoice;
        last = first + maxFit;
        if (last > choices.length)
            last = choices.length;
        topFirst = first;
        app.saveState();
        app.rectangle(offsetX, offsetX, box.getWidth() - 2 * offsetX, box.getHeight() - 2 * offsetX);
        app.clip();
        app.newPath();
        Color fcolor = (textColor == null) ? GrayColor.GRAYBLACK : textColor;
        
        
        // background boxes for selected value[s]
        app.setColorFill(new Color(10, 36, 106));
        for (int curVal = 0; curVal < choiceSelections.size(); ++curVal) {
        	int curChoice = ((Integer)choiceSelections.get( curVal )).intValue();
        	// only draw selections within our display range... not strictly necessary with 
        	// that clipping rect from above, but it certainly doesn't hurt either 
        	if (curChoice >= first && curChoice <= last) {
        		app.rectangle(offsetX, offsetX + h - (curChoice - first + 1) * leading, box.getWidth() - 2 * offsetX, leading);
        		app.fill();
        	}
        }
        float xp = offsetX * 2;
        float yp = offsetX + h - ufont.getFontDescriptor(BaseFont.BBOXURY, usize);
        for (int idx = first; idx < last; ++idx, yp -= leading) {
            String ptext = choices[idx];
            int rtl = checkRTL(ptext) ? PdfWriter.RUN_DIRECTION_LTR : PdfWriter.RUN_DIRECTION_NO_BIDI;
            ptext = removeCRLF(ptext);
            // highlight selected values against their (presumably) darker background
            Color textCol = (choiceSelections.contains( new Integer( idx ))) ? GrayColor.GRAYWHITE : fcolor;
            Phrase phrase = composePhrase(ptext, ufont, textCol, usize);
            ColumnText.showTextAligned(app, Element.ALIGN_LEFT, phrase, xp, yp, 0, rtl, 0);
        }
        app.restoreState();
        app.endVariableText();
        return app;
    }

    /**
     * Gets a new text field.
     * @throws IOException on error
     * @throws DocumentException on error
     * @return a new text field
     */    
    public PdfFormField getTextField() throws IOException, DocumentException {
        if (maxCharacterLength <= 0)
            options &= ~COMB;
        if ((options & COMB) != 0)
            options &= ~MULTILINE;
        PdfFormField field = PdfFormField.createTextField(writer, false, false, maxCharacterLength);
        field.setWidget(box, PdfAnnotation.HIGHLIGHT_INVERT);
        switch (alignment) {
            case Element.ALIGN_CENTER:
                field.setQuadding(PdfFormField.Q_CENTER);
                break;
            case Element.ALIGN_RIGHT:
                field.setQuadding(PdfFormField.Q_RIGHT);
                break;
        }
        if (rotation != 0)
            field.setMKRotation(rotation);
        if (fieldName != null) {
            field.setFieldName(fieldName);
            if (!"".equals(text))
            	field.setValueAsString(text);
            if (defaultText != null)
                field.setDefaultValueAsString(defaultText);
            if ((options & READ_ONLY) != 0)
                field.setFieldFlags(PdfFormField.FF_READ_ONLY);
            if ((options & REQUIRED) != 0)
                field.setFieldFlags(PdfFormField.FF_REQUIRED);
            if ((options & MULTILINE) != 0)
                field.setFieldFlags(PdfFormField.FF_MULTILINE);
            if ((options & DO_NOT_SCROLL) != 0)
                field.setFieldFlags(PdfFormField.FF_DONOTSCROLL);
            if ((options & PASSWORD) != 0)
                field.setFieldFlags(PdfFormField.FF_PASSWORD);
            if ((options & FILE_SELECTION) != 0)
                field.setFieldFlags(PdfFormField.FF_FILESELECT);
            if ((options & DO_NOT_SPELL_CHECK) != 0)
                field.setFieldFlags(PdfFormField.FF_DONOTSPELLCHECK);
            if ((options & COMB) != 0)
                field.setFieldFlags(PdfFormField.FF_COMB);
        }
        field.setBorderStyle(new PdfBorderDictionary(borderWidth, borderStyle, new PdfDashPattern(3)));
        PdfAppearance tp = getAppearance();
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp);
        PdfAppearance da = (PdfAppearance)tp.getDuplicate();
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
    
    /**
     * Gets a new combo field.
     * @throws IOException on error
     * @throws DocumentException on error
     * @return a new combo field
     */    
    public PdfFormField getComboField() throws IOException, DocumentException {
        return getChoiceField(false);
    }
    
    /**
     * Gets a new list field.
     * @throws IOException on error
     * @throws DocumentException on error
     * @return a new list field
     */    
    public PdfFormField getListField() throws IOException, DocumentException {
        return getChoiceField(true);
    }
    
    private int getTopChoice() {
    	if (choiceSelections == null || choiceSelections.size() ==0) {
    		return 0;
    	}
    	
    	Integer firstValue = (Integer)choiceSelections.get(0);
    	
    	if (firstValue == null) {
    		return 0;
    	}
    	
    	int topChoice = 0;
    	if (choices != null) {
    		topChoice = firstValue.intValue();
    		topChoice = Math.min( topChoice, choices.length );
    		topChoice = Math.max( 0, topChoice);
    	} // else topChoice still 0
    	return topChoice;
    }

    protected PdfFormField getChoiceField(boolean isList) throws IOException, DocumentException {
        options &= (~MULTILINE) & (~COMB);
        String uchoices[] = choices;
        if (uchoices == null)
            uchoices = new String[0];

        int topChoice = getTopChoice();

        if (text == null)
        	text = ""; //fixed by Kazuya Ujihara (ujihara.jp)

        if (topChoice >= 0)
            text = uchoices[topChoice];
        
        PdfFormField field = null;
        String mix[][] = null;
        
        if (choiceExports == null) {
            if (isList)
                field = PdfFormField.createList(writer, uchoices, topChoice);
            else
                field = PdfFormField.createCombo(writer, (options & EDIT) != 0, uchoices, topChoice);
        }
        else {
            mix = new String[uchoices.length][2];
            for (int k = 0; k < mix.length; ++k)
                mix[k][0] = mix[k][1] = uchoices[k];
            int top = Math.min(uchoices.length, choiceExports.length);
            for (int k = 0; k < top; ++k) {
                if (choiceExports[k] != null)
                    mix[k][0] = choiceExports[k];
            }
            if (isList)
                field = PdfFormField.createList(writer, mix, topChoice);
            else
                field = PdfFormField.createCombo(writer, (options & EDIT) != 0, mix, topChoice);
        }
        field.setWidget(box, PdfAnnotation.HIGHLIGHT_INVERT);
        if (rotation != 0)
            field.setMKRotation(rotation);
        if (fieldName != null) {
            field.setFieldName(fieldName);
            if (uchoices.length > 0) {
                if (mix != null) {
                	if (choiceSelections.size() < 2) {
                		field.setValueAsString(mix[topChoice][0]);
                		field.setDefaultValueAsString(mix[topChoice][0]);
                	} else {
                		writeMultipleValues( field, mix);
                	}
                } else {
                	if (choiceSelections.size() < 2) {
                		field.setValueAsString(text);
                		field.setDefaultValueAsString(text);
                	} else {
                		writeMultipleValues( field, null );
                	}
                }
            }
            if ((options & READ_ONLY) != 0)
                field.setFieldFlags(PdfFormField.FF_READ_ONLY);
            if ((options & REQUIRED) != 0)
                field.setFieldFlags(PdfFormField.FF_REQUIRED);
            if ((options & DO_NOT_SPELL_CHECK) != 0)
                field.setFieldFlags(PdfFormField.FF_DONOTSPELLCHECK);
            if ((options & MULTISELECT) != 0) {
            	field.setFieldFlags( PdfFormField.FF_MULTISELECT );
            }
        }
        field.setBorderStyle(new PdfBorderDictionary(borderWidth, borderStyle, new PdfDashPattern(3)));
        PdfAppearance tp;
        if (isList) {
            tp = getListAppearance();
            if (topFirst > 0)
                field.put(PdfName.TI, new PdfNumber(topFirst));
        }
        else
            tp = getAppearance();
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp);
        PdfAppearance da = (PdfAppearance)tp.getDuplicate();
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

    private void writeMultipleValues( PdfFormField field, String mix[][] ) {
		PdfArray indexes = new PdfArray();
		PdfArray values = new PdfArray();
		for (int i = 0; i < choiceSelections.size(); ++i) {
			int idx = ((Integer)choiceSelections.get( i )).intValue();
			indexes.add( new PdfNumber( idx ) );
			
			if (mix != null) 
				values.add( new PdfString( mix[idx][0] ) );
			else if (choices != null)
				values.add( new PdfString( choices[ idx ] ) );
		}
		
		field.put( PdfName.V, values );
		field.put( PdfName.I, indexes );

    }
    
    /**
     * Gets the default text.
     * @return the default text
     */
    public String getDefaultText() {
        return this.defaultText;
    }
    
    /**
     * Sets the default text. It is only meaningful for text fields.
     * @param defaultText the default text
     */
    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }
    
    /**
     * Gets the choices to be presented to the user in list/combo fields.
     * @return the choices to be presented to the user
     */
    public String[] getChoices() {
        return this.choices;
    }
    
    /**
     * Sets the choices to be presented to the user in list/combo fields.
     * @param choices the choices to be presented to the user
     */
    public void setChoices(String[] choices) {
        this.choices = choices;
    }
    
    /**
     * Gets the export values in list/combo fields.
     * @return the export values in list/combo fields
     */
    public String[] getChoiceExports() {
        return this.choiceExports;
    }
    
    /**
     * Sets the export values in list/combo fields. If this array
     * is <CODE>null</CODE> then the choice values will also be used
     * as the export values.
     * @param choiceExports the export values in list/combo fields
     */
    public void setChoiceExports(String[] choiceExports) {
        this.choiceExports = choiceExports;
    }
    
    /**
     * Gets the zero based index of the selected item.
     * @return the zero based index of the selected item
     */
    public int getChoiceSelection() {
    	return getTopChoice();
    }
    
    public ArrayList gteChoiceSelections() {
    	return choiceSelections;
    }

    /**
     * Sets the zero based index of the selected item.
     * @param choiceSelection the zero based index of the selected item
     */
    public void setChoiceSelection(int choiceSelection) {
        choiceSelections = new ArrayList();
        choiceSelections.add( new Integer( choiceSelection ) );
    }
    
    /**
     * adds another (or a first I suppose) selection to a MULTISELECT list.
     * This doesn't do anything unless this.options & MUTLISELECT != 0 
     * @param selection new selection
     */
    public void addChoiceSelection( int selection) {
    	if ((this.options & BaseField.MULTISELECT) != 0) {
    		choiceSelections.add( new Integer( selection ) );
    	}
    }
    
    /**
     * replaces the existing selections with the param. If this field isn't a MULTISELECT
     * list, all but the first element will be removed.
     * @param selections new selections.  If null, it clear()s the underlying ArrayList.
     */
    public void setChoiceSelections( ArrayList selections ) {
    	if (selections != null) {
    		choiceSelections = new ArrayList( selections );
    		if (choiceSelections.size() > 1 && (options & BaseField.MULTISELECT) == 0 ) {
    			// can't have multiple selections in a single-select field
    			while (choiceSelections.size() > 1) {
    				choiceSelections.remove( 1 );
    			}
    		}
    		
    	} else { 
    		choiceSelections.clear();
    	}
    }
    
    int getTopFirst() {
        return topFirst;
    }
    
    /**
     * Sets extra margins in text fields to better mimic the Acrobat layout.
     * @param extraMarginLeft the extra margin left
     * @param extraMarginTop the extra margin top
     */    
    public void setExtraMargin(float extraMarginLeft, float extraMarginTop) {
        this.extraMarginLeft = extraMarginLeft;
        this.extraMarginTop = extraMarginTop;
    }

    /**
     * Holds value of property substitutionFonts.
     */
    private ArrayList substitutionFonts;

    /**
     * Gets the list of substitution fonts. The list is composed of <CODE>BaseFont</CODE> and can be <CODE>null</CODE>. The fonts in this list will be used if the original
     * font doesn't contain the needed glyphs.
     * @return the list
     */
    public ArrayList getSubstitutionFonts() {
        return this.substitutionFonts;
    }

    /**
     * Sets a list of substitution fonts. The list is composed of <CODE>BaseFont</CODE> and can also be <CODE>null</CODE>. The fonts in this list will be used if the original
     * font doesn't contain the needed glyphs.
     * @param substitutionFonts the list
     */
    public void setSubstitutionFonts(ArrayList substitutionFonts) {
        this.substitutionFonts = substitutionFonts;
    }

    /**
     * Holds value of property extensionFont.
     */
    private BaseFont extensionFont;

    /**
     * Gets the extensionFont. This font will be searched before the
     * substitution fonts. It may be <code>null</code>.
     * @return the extensionFont
     */
    public BaseFont getExtensionFont() {
        return this.extensionFont;
    }

    /**
     * Sets the extensionFont. This font will be searched before the
     * substitution fonts. It may be <code>null</code>.
     * @param extensionFont New value of property extensionFont.
     */
    public void setExtensionFont(BaseFont extensionFont) {
        this.extensionFont = extensionFont;
    }
}