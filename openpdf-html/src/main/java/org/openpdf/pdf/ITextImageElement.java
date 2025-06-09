/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.pdf;

import org.openpdf.extend.FSImage;
import org.openpdf.extend.ReplacedElement;
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.BlockBox;
import org.openpdf.render.RenderingContext;

import java.awt.*;

public class ITextImageElement implements ITextReplacedElement {
    private final FSImage _image;

    private Point _location = new Point(0, 0);

    public ITextImageElement(FSImage image) {
        _image = image;
    }

    @Override
    public int getIntrinsicWidth() {
        return _image.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return _image.getHeight();
    }

    @Override
    public Point getLocation() {
        return _location;
    }

    @Override
    public void setLocation(int x, int y) {
        _location = new Point(x, y);
    }

    public FSImage getImage() {
        return _image;
    }

    @Override
    public void detach(LayoutContext c) {
    }

    @Override
    public boolean isRequiresInteractivePaint() {
        // N/A
        return false;
    }

    @Override
    public void paint(RenderingContext c, ITextOutputDevice outputDevice, BlockBox box) {
        Rectangle contentBounds = box.getContentAreaEdge(box.getAbsX(), box.getAbsY(), c);
        ReplacedElement element = box.getReplacedElement();
        outputDevice.drawImage(
            ((ITextImageElement)element).getImage(),
            contentBounds.x, contentBounds.y);
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
