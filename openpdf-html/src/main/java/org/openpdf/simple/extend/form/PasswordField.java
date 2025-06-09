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
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.BlockBox;
import org.openpdf.simple.extend.XhtmlForm;
import org.openpdf.util.GeneralUtil;

import javax.swing.*;

class PasswordField extends InputField {
    PasswordField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    @Override
    public JComponent create() {
        JPasswordField password = new JPasswordField();

        if (hasAttribute("size")) {
            int size = GeneralUtil.parseIntRelaxed(getAttribute("size"));

            // Size of 0 doesn't make any sense, so use default value
            if (size == 0) {
                password.setColumns(15);
            } else {
                password.setColumns(size);
            }
        } else {
            password.setColumns(15);
        }

        if (hasAttribute("maxlength")) {
            password.setDocument(
                    new SizeLimitedDocument(
                            GeneralUtil.parseIntRelaxed(getAttribute("maxlength"))));
        }

        if (hasAttribute("readonly") &&
                getAttribute("readonly").equalsIgnoreCase("readonly")) {
            password.setEditable(false);
        }

        return password;
    }

    @Override
    protected void applyOriginalState() {
        JPasswordField password = (JPasswordField) getComponent();

        password.setText(getOriginalState().getValue());
    }

    @Override
    protected String[] getFieldValues() {
        JPasswordField textfield = (JPasswordField) getComponent();

        return new String [] {
                new String(textfield.getPassword())
        };
    }
}
