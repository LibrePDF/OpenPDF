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
import org.openpdf.css.style.derived.LengthValue;
import org.openpdf.extend.FSImage;
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.BlockBox;
import org.openpdf.simple.extend.XhtmlForm;
import org.openpdf.swing.AWTFSImage;
import org.openpdf.util.XRLog;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

class ImageField extends InputField {
    ImageField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    @Override
    public JComponent create() {
        JButton button;
        Image image = null;

        if (hasAttribute("src")) {
            FSImage fsImage = getUserAgentCallback().getImageResource(getAttribute("src")).getImage();

            if (fsImage != null) {
                image = ((AWTFSImage) fsImage).getImage();
            }
        }

        if (image == null) {
            button = new JButton("Image unreachable. " + getAttribute("alt"));
        } else {
            final ImageIcon imgIcon = new ImageIcon(image, getAttribute("alt"));
            final Image img = imgIcon.getImage();
            button = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(imgIcon.getIconWidth(), imgIcon.getIconHeight());
                }
            };
        }

        button.setUI(new BasicButtonUI());
        button.setContentAreaFilled(false);


        CalculatedStyle style = getStyle();

        FSDerivedValue widthValue = style.valueByName(CSSName.WIDTH);
        if (widthValue instanceof LengthValue) {
            intrinsicWidth = getBox().getContentWidth();
        }

        FSDerivedValue heightValue = style.valueByName(CSSName.HEIGHT);
        if (heightValue instanceof LengthValue) {
            intrinsicHeight = getBox().getHeight();
        }

        button.addActionListener(event -> {
            XRLog.layout("Image pressed: Submit");
            getParentForm().submit(getComponent());
        });

        return button;
    }
}
