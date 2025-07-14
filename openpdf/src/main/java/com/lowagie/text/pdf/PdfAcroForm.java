/*
 * $Id: PdfAcroForm.java 3912 2009-04-26 08:38:15Z blowagie $
 *
 * Copyright 2002 Bruno Lowagie
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

import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Rectangle;
import java.util.HashMap;
import java.util.Map;

/**
 * Each PDF document can contain maximum 1 AcroForm.
 */

public class PdfAcroForm extends PdfDictionary {

    private PdfWriter writer;


    /**
     * This is a map containing FieldTemplates.
     */
    private Map<PdfTemplate, Object> fieldTemplates = new HashMap<>();

    /**
     * This is an array containing DocumentFields.
     */
    private PdfArray documentFields = new PdfArray();

    /**
     * This is an array containing the calculationorder of the fields.
     */
    private PdfArray calculationOrder = new PdfArray();

    /**
     * Contains the signature flags.
     */
    private int sigFlags = 0;

    /**
     * Creates new PdfAcroForm
     *
     * @param writer {@link PdfWriter}
     */
    public PdfAcroForm(PdfWriter writer) {
        this.writer = writer;
    }

    public void setNeedAppearances(boolean value) {
        put(PdfName.NEEDAPPEARANCES, new PdfBoolean(value));
    }


    /**
     * Adds fieldTemplates.
     *
     * @param ft field templates
     */

    public void addFieldTemplates(Map<PdfTemplate, Object> ft) {
        fieldTemplates.putAll(ft);
    }

    /**
     * Adds documentFields.
     *
     * @param ref an object of {@link PdfIndirectReference}
     */

    public void addDocumentField(PdfIndirectReference ref) {
        documentFields.add(ref);
    }

    /**
     * Checks if the Acroform is valid
     *
     * @return true if the Acroform is valid
     */

    public boolean isValid() {
        if (documentFields.size() == 0) {
            return false;
        }
        put(PdfName.FIELDS, documentFields);
        if (sigFlags != 0) {
            put(PdfName.SIGFLAGS, new PdfNumber(sigFlags));
        }
        if (calculationOrder.size() > 0) {
            put(PdfName.CO, calculationOrder);
        }
        if (fieldTemplates.isEmpty()) {
            return true;
        }
        PdfDictionary dic = new PdfDictionary();
        for (Object o : fieldTemplates.keySet()) {
            PdfTemplate template = (PdfTemplate) o;
            PdfFormField.mergeResources(dic, (PdfDictionary) template.getResources());
        }
        put(PdfName.DR, dic);
        put(PdfName.DA, new PdfString("/Helv 0 Tf 0 g "));
        PdfDictionary fonts = (PdfDictionary) dic.get(PdfName.FONT);
        if (fonts != null) {
            writer.eliminateFontSubset(fonts);
        }
        return true;
    }

    /**
     * Adds an object to the calculationOrder.
     *
     * @param formField an object of {@link PdfFormField}
     */

    public void addCalculationOrder(PdfFormField formField) {
        calculationOrder.add(formField.getIndirectReference());
    }

    /**
     * Sets the signature flags.
     *
     * @param f signature flag
     */

    public void setSigFlags(int f) {
        sigFlags |= f;
    }

    /**
     * Adds a formfield to the AcroForm.
     *
     * @param formField an object of {@link PdfFormField}
     */

    public void addFormField(PdfFormField formField) {
        writer.addAnnotation(formField);
    }

    /**
     * @param name     name of the field
     * @param value    value of the field
     * @param url      url for the action
     * @param caption  caption
     * @param font     an object of {@link BaseFont}
     * @param fontSize font size
     * @param llx      lower-left-x
     * @param lly      lower-left-y
     * @param urx      upper-right-x
     * @param ury      upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addHtmlPostButton(String name, String caption, String value, String url, BaseFont font,
            float fontSize, float llx, float lly, float urx, float ury) {
        PdfAction action = PdfAction.createSubmitForm(url, null, PdfAction.SUBMIT_HTML_FORMAT);
        PdfFormField button = new PdfFormField(writer, llx, lly, urx, ury, action);
        setButtonParams(button, PdfFormField.FF_PUSHBUTTON, name, value);
        drawButton(button, caption, font, fontSize, llx, lly, urx, ury);
        addFormField(button);
        return button;
    }

    /**
     * @param name     name of the field
     * @param value    value of the field
     * @param caption  caption
     * @param font     an object of {@link BaseFont}
     * @param fontSize font size
     * @param llx      lower-left-x
     * @param lly      lower-left-y
     * @param urx      upper-right-x
     * @param ury      upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addResetButton(String name, String caption, String value, BaseFont font, float fontSize,
            float llx, float lly, float urx, float ury) {
        PdfAction action = PdfAction.createResetForm(null, 0);
        PdfFormField button = new PdfFormField(writer, llx, lly, urx, ury, action);
        setButtonParams(button, PdfFormField.FF_PUSHBUTTON, name, value);
        drawButton(button, caption, font, fontSize, llx, lly, urx, ury);
        addFormField(button);
        return button;
    }

    /**
     * @param name       name of the field
     * @param value      value of the field
     * @param url        url of the action
     * @param appearance an object of {@link PdfContentByte}
     * @param llx        lower-left-x
     * @param lly        lower-left-y
     * @param urx        upper-right-x
     * @param ury        upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addMap(String name, String value, String url, PdfContentByte appearance, float llx, float lly,
            float urx, float ury) {
        PdfAction action = PdfAction.createSubmitForm(url, null,
                PdfAction.SUBMIT_HTML_FORMAT | PdfAction.SUBMIT_COORDINATES);
        PdfFormField button = new PdfFormField(writer, llx, lly, urx, ury, action);
        setButtonParams(button, PdfFormField.FF_PUSHBUTTON, name, null);
        PdfAppearance pa = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        pa.add(appearance);
        button.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, pa);
        addFormField(button);
        return button;
    }

    /**
     * @param button          an object of {@link PdfFormField}
     * @param characteristics characteristics
     * @param name            name of the field
     * @param value           value of the field
     */
    public void setButtonParams(PdfFormField button, int characteristics, String name, String value) {
        button.setButton(characteristics);
        button.setFlags(PdfAnnotation.FLAGS_PRINT);
        button.setPage();
        button.setFieldName(name);
        if (value != null) {
            button.setValueAsString(value);
        }
    }

    /**
     * @param button   an object of {@link PdfFormField}
     * @param caption  caption
     * @param font     an object of {@link BaseFont}
     * @param fontSize font size
     * @param llx      lower-left-x
     * @param lly      lower-left-y
     * @param urx      upper-right-x
     * @param ury      upper-right-y
     */
    public void drawButton(PdfFormField button, String caption, BaseFont font, float fontSize, float llx, float lly,
            float urx, float ury) {
        PdfAppearance pa = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        pa.drawButton(0f, 0f, urx - llx, ury - lly, caption, font, fontSize);
        button.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, pa);
    }

    /**
     * @param name  name of the field
     * @param value value of the field
     * @return a PdfFormField
     */
    public PdfFormField addHiddenField(String name, String value) {
        PdfFormField hidden = PdfFormField.createEmpty(writer);
        hidden.setFieldName(name);
        hidden.setValueAsName(value);
        addFormField(hidden);
        return hidden;
    }

    /**
     * @param name     name of the field
     * @param text     value for field
     * @param font     an object of {@link BaseFont}
     * @param fontSize font size
     * @param llx      lower-left-x
     * @param lly      lower-left-y
     * @param urx      upper-right-x
     * @param ury      upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addSingleLineTextField(String name, String text, BaseFont font, float fontSize, float llx,
            float lly, float urx, float ury) {
        PdfFormField field = PdfFormField.createTextField(writer, PdfFormField.SINGLELINE, PdfFormField.PLAINTEXT, 0);
        setTextFieldParams(field, text, name, llx, lly, urx, ury);
        drawSingleLineOfText(field, text, font, fontSize, llx, lly, urx, ury);
        addFormField(field);
        return field;
    }

    /**
     * @param name     name of the field
     * @param text     value for field
     * @param font     an object of {@link BaseFont}
     * @param fontSize font size
     * @param llx      lower-left-x
     * @param lly      lower-left-y
     * @param urx      upper-right-x
     * @param ury      upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addMultiLineTextField(String name, String text, BaseFont font, float fontSize, float llx,
            float lly, float urx, float ury) {
        PdfFormField field = PdfFormField.createTextField(writer, PdfFormField.MULTILINE, PdfFormField.PLAINTEXT, 0);
        setTextFieldParams(field, text, name, llx, lly, urx, ury);
        drawMultiLineOfText(field, text, font, fontSize, llx, lly, urx, ury);
        addFormField(field);
        return field;
    }

    /**
     * @param name     name of the field
     * @param text     text for the field
     * @param font     an object of {@link BaseFont}
     * @param fontSize font size
     * @param llx      lower-left-x
     * @param lly      lower-left-y
     * @param urx      upper-right-x
     * @param ury      upper-right-y
     * @return PdfFormField
     */
    public PdfFormField addSingleLinePasswordField(String name, String text, BaseFont font, float fontSize, float llx,
            float lly, float urx, float ury) {
        PdfFormField field = PdfFormField.createTextField(writer, PdfFormField.SINGLELINE, PdfFormField.PASSWORD, 0);
        setTextFieldParams(field, text, name, llx, lly, urx, ury);
        drawSingleLineOfText(field, text, font, fontSize, llx, lly, urx, ury);
        addFormField(field);
        return field;
    }

    /**
     * @param field an object of {@link PdfFormField}
     * @param text  text for the field
     * @param name  name of the field
     * @param llx   lower-left-x
     * @param lly   lower-left-y
     * @param urx   upper-right-x
     * @param ury   upper-right-y
     */
    public void setTextFieldParams(PdfFormField field, String text, String name, float llx, float lly, float urx,
            float ury) {
        field.setWidget(new Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_INVERT);
        field.setValueAsString(text);
        field.setDefaultValueAsString(text);
        field.setFieldName(name);
        field.setFlags(PdfAnnotation.FLAGS_PRINT);
        field.setPage();
    }

    /**
     * @param field    field
     * @param text     text for the field
     * @param font     an object of {@link BaseFont}
     * @param fontSize font size
     * @param llx      lower-left-x
     * @param lly      lower-left-y
     * @param urx      upper-right-x
     * @param ury      upper-right-y
     */
    public void drawSingleLineOfText(PdfFormField field, String text, BaseFont font, float fontSize, float llx,
            float lly, float urx, float ury) {
        PdfAppearance tp = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        PdfAppearance tp2 = (PdfAppearance) tp.getDuplicate();
        tp2.setFontAndSize(font, fontSize);
        tp2.resetRGBColorFill();
        field.setDefaultAppearanceString(tp2);
        tp.drawTextField(0f, 0f, urx - llx, ury - lly);
        tp.beginVariableText();
        tp.saveState();
        tp.rectangle(3f, 3f, urx - llx - 6f, ury - lly - 6f);
        tp.clip();
        tp.newPath();
        tp.beginText();
        tp.setFontAndSize(font, fontSize);
        tp.resetRGBColorFill();
        tp.setTextMatrix(4, (ury - lly) / 2 - (fontSize * 0.3f));
        tp.showText(text);
        tp.endText();
        tp.restoreState();
        tp.endVariableText();
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp);
    }

    /**
     * @param field    field, an object of {@link PdfFormField}
     * @param text     text for field
     * @param font     an object of {@link BaseFont}
     * @param fontSize font size
     * @param llx      lower-left-x
     * @param lly      lower-left-y
     * @param urx      upper-right-x
     * @param ury      upper-right-y
     */
    public void drawMultiLineOfText(PdfFormField field, String text, BaseFont font, float fontSize, float llx,
            float lly, float urx, float ury) {
        PdfAppearance tp = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        PdfAppearance tp2 = (PdfAppearance) tp.getDuplicate();
        tp2.setFontAndSize(font, fontSize);
        tp2.resetRGBColorFill();
        field.setDefaultAppearanceString(tp2);
        tp.drawTextField(0f, 0f, urx - llx, ury - lly);
        tp.beginVariableText();
        tp.saveState();
        tp.rectangle(3f, 3f, urx - llx - 6f, ury - lly - 6f);
        tp.clip();
        tp.newPath();
        tp.beginText();
        tp.setFontAndSize(font, fontSize);
        tp.resetRGBColorFill();
        tp.setTextMatrix(4, 5);
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(text, "\n");
        float yPos = ury - lly;
        while (tokenizer.hasMoreTokens()) {
            yPos -= fontSize * 1.2f;
            tp.showTextAligned(PdfContentByte.ALIGN_LEFT, tokenizer.nextToken(), 3, yPos, 0);
        }
        tp.endText();
        tp.restoreState();
        tp.endVariableText();
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp);
    }

    /**
     * @param name   field name
     * @param status status
     * @param value  value of the field
     * @param llx    lower-left-x
     * @param lly    lower-left-y
     * @param urx    upper-right-x
     * @param ury    upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addCheckBox(String name, String value, boolean status, float llx, float lly, float urx,
            float ury) {
        PdfFormField field = PdfFormField.createCheckBox(writer);
        setCheckBoxParams(field, name, value, status, llx, lly, urx, ury);
        drawCheckBoxAppearences(field, value, llx, lly, urx, ury);
        addFormField(field);
        return field;
    }

    /**
     * @param name   field name
     * @param status status
     * @param field  field, an object of {@link PdfFormField}
     * @param value  value of the field
     * @param llx    lower-left-x
     * @param lly    lower-left-y
     * @param urx    upper-right-x
     * @param ury    upper-right-y
     */
    public void setCheckBoxParams(PdfFormField field, String name, String value, boolean status, float llx, float lly,
            float urx, float ury) {
        field.setWidget(new Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_TOGGLE);
        field.setFieldName(name);
        if (status) {
            field.setValueAsName(value);
            field.setAppearanceState(value);
        } else {
            field.setValueAsName("Off");
            field.setAppearanceState("Off");
        }
        field.setFlags(PdfAnnotation.FLAGS_PRINT);
        field.setPage();
        field.setBorderStyle(new PdfBorderDictionary(1, PdfBorderDictionary.STYLE_SOLID));
    }

    /**
     * @param field field, an object of {@link PdfFormField}
     * @param value value of the field
     * @param llx   lower-left-x
     * @param lly   lower-left-y
     * @param urx   upper-right-x
     * @param ury   upper-right-y
     */
    public void drawCheckBoxAppearences(PdfFormField field, String value, float llx, float lly, float urx, float ury) {
        BaseFont font = null;
        try {
            font = BaseFont.createFont(BaseFont.ZAPFDINGBATS, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        float size = (ury - lly);
        PdfAppearance tpOn = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        PdfAppearance tp2 = (PdfAppearance) tpOn.getDuplicate();
        tp2.setFontAndSize(font, size);
        tp2.resetRGBColorFill();
        field.setDefaultAppearanceString(tp2);
        tpOn.drawTextField(0f, 0f, urx - llx, ury - lly);
        tpOn.saveState();
        tpOn.resetRGBColorFill();
        tpOn.beginText();
        tpOn.setFontAndSize(font, size);
        tpOn.showTextAligned(PdfContentByte.ALIGN_CENTER, "4", (urx - llx) / 2, (ury - lly) / 2 - (size * 0.3f), 0);
        tpOn.endText();
        tpOn.restoreState();
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, value, tpOn);
        PdfAppearance tpOff = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        tpOff.drawTextField(0f, 0f, urx - llx, ury - lly);
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, "Off", tpOff);
    }

    /**
     * @param name          name of the field
     * @param defaultValue  default value
     * @param noToggleToOff noToggleToOff
     * @return a PdfFormField
     */
    public PdfFormField getRadioGroup(String name, String defaultValue, boolean noToggleToOff) {
        PdfFormField radio = PdfFormField.createRadioButton(writer, noToggleToOff);
        radio.setFieldName(name);
        radio.setValueAsName(defaultValue);
        return radio;
    }

    /**
     * @param radiogroup field
     */
    public void addRadioGroup(PdfFormField radiogroup) {
        addFormField(radiogroup);
    }

    /**
     * @param radiogroup radio group field
     * @param value      value of the field
     * @param llx        lower-left-x
     * @param lly        lower-left-y
     * @param urx        upper-right-x
     * @param ury        upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addRadioButton(PdfFormField radiogroup, String value, float llx, float lly, float urx,
            float ury) {
        PdfFormField radio = PdfFormField.createEmpty(writer);
        radio.setWidget(new Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_TOGGLE);
        String name = radiogroup.get(PdfName.V).toString().substring(1);
        if (name.equals(value)) {
            radio.setAppearanceState(value);
        } else {
            radio.setAppearanceState("Off");
        }
        drawRadioAppearences(radio, value, llx, lly, urx, ury);
        radiogroup.addKid(radio);
        return radio;
    }

    /**
     * @param field field, an object of {@link PdfFormField}
     * @param value value
     * @param llx   lower-left-x
     * @param lly   lower-left-y
     * @param urx   upper-right-x
     * @param ury   upper-right-y
     */
    public void drawRadioAppearences(PdfFormField field, String value, float llx, float lly, float urx, float ury) {
        PdfAppearance tpOn = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        tpOn.drawRadioField(0f, 0f, urx - llx, ury - lly, true);
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, value, tpOn);
        PdfAppearance tpOff = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        tpOff.drawRadioField(0f, 0f, urx - llx, ury - lly, false);
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, "Off", tpOff);
    }

    /**
     * @param name         name of the field
     * @param options      options
     * @param defaultValue default value
     * @param font         an object of {@link BaseFont}
     * @param fontSize     font size
     * @param llx          lower-left-x
     * @param lly          lower-left-y
     * @param urx          upper-right-x
     * @param ury          upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addSelectList(String name, String[] options, String defaultValue, BaseFont font, float fontSize,
            float llx, float lly, float urx, float ury) {
        PdfFormField choice = PdfFormField.createList(writer, options, 0);
        setChoiceParams(choice, name, defaultValue, llx, lly, urx, ury);
        StringBuilder text = new StringBuilder();
        for (String option : options) {
            text.append(option).append('\n');
        }
        drawMultiLineOfText(choice, text.toString(), font, fontSize, llx, lly, urx, ury);
        addFormField(choice);
        return choice;
    }

    /**
     * @param name         name of the field
     * @param options      options
     * @param defaultValue default value
     * @param font         an object of {@link BaseFont}
     * @param fontSize     font size
     * @param llx          lower-left-x
     * @param lly          lower-left-y
     * @param urx          upper-right-x
     * @param ury          upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addSelectList(String name, String[][] options, String defaultValue, BaseFont font,
            float fontSize, float llx, float lly, float urx, float ury) {
        PdfFormField choice = PdfFormField.createList(writer, options, 0);
        setChoiceParams(choice, name, defaultValue, llx, lly, urx, ury);
        StringBuilder text = new StringBuilder();
        for (String[] option : options) {
            text.append(option[1]).append('\n');
        }
        drawMultiLineOfText(choice, text.toString(), font, fontSize, llx, lly, urx, ury);
        addFormField(choice);
        return choice;
    }

    /**
     * @param name         name of the field
     * @param options      options
     * @param defaultValue default value
     * @param editable     if field is editable, <code>true</code> or <code>false</code>
     * @param font         an object of {@link BaseFont}
     * @param fontSize     font size
     * @param llx          lower-left-x
     * @param lly          lower-left-y
     * @param urx          upper-right-x
     * @param ury          upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addComboBox(String name, String[] options, String defaultValue, boolean editable, BaseFont font,
            float fontSize, float llx, float lly, float urx, float ury) {
        PdfFormField choice = PdfFormField.createCombo(writer, editable, options, 0);
        setChoiceParams(choice, name, defaultValue, llx, lly, urx, ury);
        if (defaultValue == null) {
            defaultValue = options[0];
        }
        drawSingleLineOfText(choice, defaultValue, font, fontSize, llx, lly, urx, ury);
        addFormField(choice);
        return choice;
    }

    /**
     * @param name         name of the field
     * @param options      options
     * @param defaultValue default value
     * @param editable     if field is editable, <code>true</code> or <code>false</code>
     * @param font         an object of {@link BaseFont}
     * @param fontSize     font size
     * @param llx          lower-left-x
     * @param lly          lower-left-y
     * @param urx          upper-right-x
     * @param ury          upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addComboBox(String name, String[][] options, String defaultValue, boolean editable,
            BaseFont font, float fontSize, float llx, float lly, float urx, float ury) {
        PdfFormField choice = PdfFormField.createCombo(writer, editable, options, 0);
        setChoiceParams(choice, name, defaultValue, llx, lly, urx, ury);
        String value = null;
        for (String[] option : options) {
            if (option[0].equals(defaultValue)) {
                value = option[1];
                break;
            }
        }
        if (value == null) {
            value = options[0][1];
        }
        drawSingleLineOfText(choice, value, font, fontSize, llx, lly, urx, ury);
        addFormField(choice);
        return choice;
    }

    /**
     * @param field        field, an object of {@link PdfFormField}
     * @param name         field name
     * @param defaultValue default calue
     * @param llx          lower-left-x
     * @param lly          lower-left-y
     * @param urx          upper-right-x
     * @param ury          upper-right-y
     */
    public void setChoiceParams(PdfFormField field, String name, String defaultValue, float llx, float lly, float urx,
            float ury) {
        field.setWidget(new Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_INVERT);
        if (defaultValue != null) {
            field.setValueAsString(defaultValue);
            field.setDefaultValueAsString(defaultValue);
        }
        field.setFieldName(name);
        field.setFlags(PdfAnnotation.FLAGS_PRINT);
        field.setPage();
        field.setBorderStyle(new PdfBorderDictionary(2, PdfBorderDictionary.STYLE_SOLID));
    }

    /**
     * @param name field name
     * @param llx  lower-left-x
     * @param lly  lower-left-y
     * @param urx  upper-right-x
     * @param ury  upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addSignature(String name,
            float llx, float lly, float urx, float ury) {
        PdfFormField signature = PdfFormField.createSignature(writer);
        setSignatureParams(signature, name, llx, lly, urx, ury);
        drawSignatureAppearences(signature, llx, lly, urx, ury);
        addFormField(signature);
        return signature;
    }

    /**
     * @param name  field name
     * @param field field, an object of {@link PdfFormField}
     * @param llx   lower-left-x
     * @param lly   lower-left-y
     * @param urx   upper-right-x
     * @param ury   upper-right-y
     */
    public void setSignatureParams(PdfFormField field, String name,
            float llx, float lly, float urx, float ury) {
        field.setWidget(new Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_INVERT);
        field.setFieldName(name);
        field.setFlags(PdfAnnotation.FLAGS_PRINT);
        field.setPage();
        field.setMKBorderColor(java.awt.Color.black);
        field.setMKBackgroundColor(java.awt.Color.white);
    }

    /**
     * @param field field, an object of {@link PdfFormField}
     * @param llx   lower-left-x
     * @param lly   lower-left-y
     * @param urx   upper-right-x
     * @param ury   upper-right-y
     */
    public void drawSignatureAppearences(PdfFormField field,
            float llx, float lly, float urx, float ury) {
        PdfAppearance tp = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        tp.setGrayFill(1.0f);
        tp.rectangle(0, 0, urx - llx, ury - lly);
        tp.fill();
        tp.setGrayStroke(0);
        tp.setLineWidth(1);
        tp.rectangle(0.5f, 0.5f, urx - llx - 0.5f, ury - lly - 0.5f);
        tp.closePathStroke();
        tp.saveState();
        tp.rectangle(1, 1, urx - llx - 2, ury - lly - 2);
        tp.clip();
        tp.newPath();
        tp.restoreState();
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp);
    }
}
