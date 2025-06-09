/*
 * {{{ header & license
 * Copyright (c) 2007 Sean Bright
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
package org.openpdf.simple.extend.form;

import org.w3c.dom.Element;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.FSDerivedValue;
import org.openpdf.css.style.derived.BorderPropertySet;
import org.openpdf.css.style.derived.LengthValue;
import org.openpdf.css.style.derived.RectPropertySet;
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.BlockBox;
import org.openpdf.simple.extend.XhtmlForm;
import org.openpdf.util.GeneralUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.plaf.basic.BasicTextUI;
import java.awt.*;

class TextField extends InputField {
    TextField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    @Override
    public JComponent create() {
        TextFieldJTextField textfield = new TextFieldJTextField();

        if (hasAttribute("size")) {
            int size = GeneralUtil.parseIntRelaxed(getAttribute("size"));

            // Size of 0 doesn't make any sense, so use default value
            if (size == 0) {
                textfield.setColumns(15);
            } else {
                textfield.setColumns(size);
            }
        } else {
            textfield.setColumns(15);
        }

        if (hasAttribute("maxlength")) {
            textfield.setDocument(
                    new SizeLimitedDocument(
                            GeneralUtil.parseIntRelaxed(getAttribute("maxlength"))));
        }

        if (hasAttribute("readonly") &&
                getAttribute("readonly").equalsIgnoreCase("readonly")) {
            textfield.setEditable(false);
        }

        applyComponentStyle(textfield);

        return textfield;
    }

    @Override
    protected void applyComponentStyle(JComponent component) {
        super.applyComponentStyle(component);

        TextFieldJTextField field = (TextFieldJTextField)component;

        CalculatedStyle style = getBox().getStyle();
        BorderPropertySet border = style.getBorder(null);
        boolean disableOSBorder = (border.leftStyle() != null && border.rightStyle() != null || border.topStyle() != null || border.bottomStyle() != null);

        RectPropertySet padding = style.getCachedPadding();

        Integer paddingTop = getLengthValue(style, CSSName.PADDING_TOP);
        Integer paddingLeft = getLengthValue(style, CSSName.PADDING_LEFT);
        Integer paddingBottom = getLengthValue(style, CSSName.PADDING_BOTTOM);
        Integer paddingRight = getLengthValue(style, CSSName.PADDING_RIGHT);


        int top = paddingTop == null ? 2 : Math.max(2, paddingTop);
        int left = paddingLeft == null ? 3 : Math.max(3, paddingLeft);
        int bottom = paddingBottom == null ? 2 : Math.max(2, paddingBottom);
        int right = paddingRight == null ? 3 : Math.max(3, paddingRight);

        //if a border is set or a background color is set, then use a special JButton with the BasicButtonUI.
        if (disableOSBorder) {
            //when background color is set, need to use the BasicButtonUI, certainly when using XP l&f
            BasicTextUI ui = new BasicTextFieldUI();
            field.setUI(ui);
            Border fieldBorder = BorderFactory.createEmptyBorder(top, left, bottom, right);
            field.setBorder(fieldBorder);
        }
        else {
            field.setMargin(new Insets(top, left, bottom, right));
        }

        padding.setRight(0);
        padding.setLeft(0);
        padding.setTop(0);
        padding.setBottom(0);

        FSDerivedValue widthValue = style.valueByName(CSSName.WIDTH);
        if (widthValue instanceof LengthValue) {
            intrinsicWidth = getBox().getContentWidth() + left + right;
        }

        FSDerivedValue heightValue = style.valueByName(CSSName.HEIGHT);
        if (heightValue instanceof LengthValue) {
            intrinsicHeight = getBox().getHeight() + top + bottom;
        }
    }



    @Override
    protected void applyOriginalState() {
        JTextField textfield = (JTextField) getComponent();

        textfield.setText(getOriginalState().getValue());

        // Make sure we are showing the front of 'value' instead of the end.
        textfield.setCaretPosition(0);
    }

    @Override
    protected String[] getFieldValues() {
        JTextField textfield = (JTextField) getComponent();

        return new String[] {
                textfield.getText()
        };
    }

    private static class TextFieldJTextField extends JTextField {
        //override getColumnWidth to base on 'o' instead of 'm'.  more like other browsers
        private int columnWidth = 0;
        @Override
        protected int getColumnWidth() {
            if (columnWidth == 0) {
                FontMetrics metrics = getFontMetrics(getFont());
                columnWidth = metrics.charWidth('o');
            }
            return columnWidth;
        }
    }
}
