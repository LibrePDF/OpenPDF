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

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.parser.FSColor;
import org.openpdf.css.parser.FSRGBColor;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.FSDerivedValue;
import org.openpdf.css.style.derived.LengthValue;
import org.openpdf.extend.UserAgentCallback;
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.BlockBox;
import org.openpdf.render.FSFont;
import org.openpdf.simple.extend.URLUTF8Encoder;
import org.openpdf.simple.extend.XhtmlForm;
import org.openpdf.swing.AWTFSFont;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public abstract class FormField {
    private final XhtmlForm _parentForm;
    private final Element _element;
    @Nullable
    private FormFieldState _originalState;
    private JComponent _component;
    private final LayoutContext context;
    private final BlockBox box;
    @Nullable
    protected Integer intrinsicWidth;
    @Nullable
    protected Integer intrinsicHeight;

    protected FormField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        _element = e;
        _parentForm = form;
        this.context = context;
        this.box = box;

        initialize();
    }

    protected Element getElement() {
        return _element;
    }

    public JComponent getComponent() {
        return _component;
    }

    public XhtmlForm getParentForm() {
        return _parentForm;
    }

    @CheckReturnValue
    public Dimension getIntrinsicSize(){

        int width = intrinsicWidth == null ? 0 : intrinsicWidth;
        int height = intrinsicHeight == null ? 0 : intrinsicHeight;

        return new Dimension(width, height);
    }


    public void reset() {
        applyOriginalState();
    }

    protected UserAgentCallback getUserAgentCallback() {
        return _parentForm.getUserAgentCallback();
    }

    protected FormFieldState getOriginalState() {
        if (_originalState == null) {
            _originalState = loadOriginalState();
        }

        return _originalState;
    }

    protected boolean hasAttribute(String attributeName) {
        return !getElement().getAttribute(attributeName).isEmpty();
    }

    protected String getAttribute(String attributeName) {
        return getElement().getAttribute(attributeName);
    }

    private void initialize() {
        _component = create();

        if (_component != null) {
            if (intrinsicWidth == null)
                intrinsicWidth = _component.getPreferredSize().width;
            if (intrinsicHeight == null)
                intrinsicHeight = _component.getPreferredSize().height;

            _component.setSize(getIntrinsicSize());

            String d = _element.getAttribute("disabled");
            if (d.equalsIgnoreCase("disabled")) {
                _component.setEnabled(false);
            }
        }

        applyOriginalState();
    }

    @Nullable
    public abstract JComponent create();

    protected FormFieldState loadOriginalState() {
        return FormFieldState.fromString("");
    }

    protected void applyOriginalState() {
        // Do nothing
    }

    /**
     * Returns true if the value of the current FormField should be
     * sent along with the current submission.  This is used so that
     * only the value of the submit button that is used to trigger the
     * form's submission is sent.
     *
     * @param source The JComponent that caused the submission
     * @return true if it should
     */
    public boolean includeInSubmission(JComponent source) {
        return true;
    }

    // These two methods are temporary, but I am using them to clean up
    // the code in XhtmlForm
    public Collection<String> getFormDataStrings() {
        // Fields MUST have at least a name attribute to get sent.  The attr
        // can be empty, or just white space, but it must be present
        if (!hasAttribute("name")) {
            return emptyList();
        }

        String name = getAttribute("name");
        String[] values = getFieldValues();

        return Stream.of(values)
                .map(rawValue -> URLUTF8Encoder.encode(name) + "=" + URLUTF8Encoder.encode(rawValue))
                .collect(Collectors.toList());
    }

    protected abstract String[] getFieldValues();


    public BlockBox getBox() {
        return box;
    }

    public LayoutContext getContext() {
        return context;
    }

    public CalculatedStyle getStyle() {
        return getBox().getStyle();
    }

    protected void applyComponentStyle(JComponent comp) {
        Font font = getFont();
        if (font != null) {
            comp.setFont(font);
        }

        CalculatedStyle style = getStyle();

        FSColor foreground = style.getColor();
        if (foreground != null) {
            comp.setForeground(toColor(foreground));
        }

        FSColor background = style.getBackgroundColor();
        if (background != null) {
            comp.setBackground(toColor(background));
        }
    }

    private static Color toColor(FSColor color)
    {
        if (color instanceof FSRGBColor rgb) {
            return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
        }
        throw new RuntimeException("internal error: unsupported color class " + color.getClass().getName());
    }

    public Font getFont() {
        FSFont font = getStyle().getFSFont(getContext());
        if (font instanceof AWTFSFont) {
            return ((AWTFSFont) font).getAWTFont();
        }
        return null;
    }

    protected static Integer getLengthValue(CalculatedStyle style, CSSName cssName) {
        FSDerivedValue widthValue = style.valueByName(cssName);
        if (widthValue instanceof LengthValue) {
            return (int) widthValue.asFloat();
        }

        return null;
    }
}
