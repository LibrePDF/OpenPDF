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

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.BlockBox;
import org.openpdf.simple.extend.XhtmlForm;

import static java.util.Objects.requireNonNull;

public class FormFieldFactory {
    private FormFieldFactory() {
    }

    @Nullable
    public static FormField create(XhtmlForm form, LayoutContext context, BlockBox box) {
        Element e = requireNonNull(box.getElement());

        String typeKey = getTypeKey(e);
        if (typeKey == null) return null;

        return switch (typeKey) {
            case "submit" -> new SubmitField(e, form, context, box);
            case "reset" -> new ResetField(e, form, context, box);
            case "button" -> new ButtonField(e, form, context, box);
            case "image" -> new ImageField(e, form, context, box);
            case "hidden" -> new HiddenField(e, form, context, box);
            case "password" -> new PasswordField(e, form, context, box);
            case "checkbox" -> new CheckboxField(e, form, context, box);
            case "radio" -> new RadioButtonField(e, form, context, box);
            case "file" -> new FileField(e, form, context, box);
            case "textarea" -> new TextAreaField(e, form, context, box);
            case "select" -> new SelectField(e, form, context, box);
            default -> new TextField(e, form, context, box);
        };
    }

    @Nullable
    private static String getTypeKey(Element e) {
        return switch (e.getNodeName()) {
            case "input" -> e.getAttribute("type");
            case "textarea" -> "textarea";
            case "select" -> "select";
            default -> null;
        };
    }
}
