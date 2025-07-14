/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.pdf;

import org.openpdf.text.pdf.PdfAnnotation;
import org.openpdf.text.pdf.PdfAppearance;
import org.openpdf.text.pdf.PdfBorderDictionary;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfFormField;
import org.openpdf.text.pdf.PdfWriter;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.openpdf.css.parser.FSColor;
import org.openpdf.css.parser.FSRGBColor;
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.BlockBox;
import org.openpdf.render.Box;
import org.openpdf.render.PageBox;
import org.openpdf.render.RenderingContext;

import java.awt.*;
import java.util.List;

public final class RadioButtonFormField extends AbstractFormField {
    private static final String FIELD_TYPE = "RadioButton";

    private final ITextReplacedElementFactory _factory;
    private final Box _box;

    @Override
    protected String getFieldType() {
        return FIELD_TYPE;
    }

    public RadioButtonFormField(
            ITextReplacedElementFactory factory, LayoutContext c, BlockBox box, int cssWidth, int cssHeight) {
        _factory = factory;
        _box = box;

        initDimensions(c, box, cssWidth, cssHeight);
    }

    @Override
    public void paint(RenderingContext c, ITextOutputDevice outputDevice, BlockBox box) {
        String fieldName = getFieldName(outputDevice, box.getElement());
        List<RadioButtonFormField> radioBoxes = _factory.getRadioButtons(fieldName);

        // iText wants all radio buttons in a group added at once across all pages

        if (radioBoxes == null) {
            // Already added to document
            return;
        }

        PdfContentByte cb = outputDevice.getCurrentPage();
        PdfWriter writer = outputDevice.getWriter();

        PdfFormField group = PdfFormField.createRadioButton(writer, true);
        group.setFieldName(fieldName);

        RadioButtonFormField checked = getChecked(radioBoxes);
        if (checked != null) {
            group.setValueAsString(getValue(checked.getBox().getElement()));
        }

        for (RadioButtonFormField fieldElem : radioBoxes) {
            createField(c, outputDevice, cb, writer, group, fieldElem, checked);
        }

        writer.addAnnotation(group);

        _factory.remove(fieldName);
    }

    @Nullable
    private RadioButtonFormField getChecked(List<RadioButtonFormField> fields) {
        for (RadioButtonFormField f : fields) {
            if (isChecked(f.getBox().getElement())) {
                return f;
            }
        }
        return null;
    }

    private void createField(RenderingContext c,
            ITextOutputDevice outputDevice, PdfContentByte cb,
            PdfWriter writer, PdfFormField group,
            RadioButtonFormField fieldElem, RadioButtonFormField checked) {
        Box box = fieldElem.getBox();

        Element e = box.getElement();
        String onValue = getValue(e);

        float width = outputDevice.getDeviceLength(fieldElem.getWidth());
        float height = outputDevice.getDeviceLength(fieldElem.getHeight());

        PdfFormField field = PdfFormField.createEmpty(writer);

        FSColor color = box.getStyle().getColor();
//        FSColor darker = box.getEffBackgroundColor(c).darkenColor();
        FSColor darker = new FSRGBColor(255, 255, 255);
        createAppearances(cb, field, onValue, width, height, true, color, darker);
        createAppearances(cb, field, onValue, width, height, false, color, darker);

        field.setWidget(
                outputDevice.createTargetArea(c, box),
                PdfAnnotation.HIGHLIGHT_INVERT);

        // XXX createTargetArea already looks up the page, but hopefully a document
        // won't have enough radio buttons to matter
        Rectangle bounds = box.getContentAreaEdge(box.getAbsX(), box.getAbsY(), c);
        PageBox page = c.getRootLayer().getPage(c, bounds.y);
        field.setPlaceInPage(page.getPageNo() + 1);

        field.setBorderStyle(new PdfBorderDictionary(0.0f, 0));

        field.setAppearanceState(fieldElem == checked ? onValue : OFF_STATE);

        if (isReadOnly(e)) {
            field.setFieldFlags(PdfFormField.FF_READ_ONLY);
        }

        group.addKid(field);
    }

    private void createAppearances(
            PdfContentByte cb, PdfFormField field,
            String onValue, float width, float height,
            boolean normal, FSColor color, FSColor darker) {
        // XXX Should cache this by width and height, but they're small so
        // don't bother for now...
        PdfAppearance tpOff = cb.createAppearance(width, height);
        PdfAppearance tpOn = cb.createAppearance(width, height);

        float diameter = Math.min(width, height);

        setStrokeColor(tpOff, color);
        setStrokeColor(tpOn, color);

        if (! normal) {
            setStrokeColor(tpOff, darker);
            setStrokeColor(tpOn, darker);
        }

        float strokeWidth = Math.max(1.0f, reduce(diameter));

        tpOff.setLineWidth(strokeWidth);
        tpOn.setLineWidth(strokeWidth);

        tpOff.circle(width / 2, height / 2, diameter / 2 - strokeWidth / 2);
        tpOn.circle(width / 2, height / 2, diameter / 2 - strokeWidth / 2);

        if (! normal) {
            tpOff.fillStroke();
            tpOn.fillStroke();
        } else {
            tpOff.stroke();
            tpOn.stroke();
        }

        setFillColor(tpOn, color);
        if (! normal) {
            tpOn.circle(width / 2, height / 2, diameter * 0.23f);
        } else {
            tpOn.circle(width / 2, height / 2, diameter * 0.20f);
        }
        tpOn.fill();

        if (normal) {
            field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, OFF_STATE, tpOff);
            field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, onValue, tpOn);
        } else {
            field.setAppearance(PdfAnnotation.APPEARANCE_DOWN, OFF_STATE, tpOff);
            field.setAppearance(PdfAnnotation.APPEARANCE_DOWN, onValue, tpOn);
        }
    }

    private float reduce(float value) {
        return Math.min(value, Math.max(1.0f, 0.05f*value));
    }

    @Override
    public void detach(LayoutContext c) {
        super.detach(c);

        _factory.remove(_box.getElement());
    }

    public Box getBox() {
        return _box;
    }

    @Override
    public int getBaseline() {
        return 0;
    }

    @Override
    public boolean hasBaseline() {
        return false;
    }
}
