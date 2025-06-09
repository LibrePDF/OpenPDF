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
package org.openpdf.swing;

import org.openpdf.extend.ReplacedElement;
import org.openpdf.layout.LayoutContext;

import javax.swing.*;
import java.awt.*;

import static java.util.Objects.requireNonNull;

public class SwingReplacedElement implements ReplacedElement {
    private final JComponent _component;
    private final Dimension intrinsicSize;

    public SwingReplacedElement(JComponent component, Dimension intrinsicSize) {
        _component = requireNonNull(component);
        this.intrinsicSize = requireNonNull(intrinsicSize);
    }

    public JComponent getJComponent() {
        return _component;
    }

    @Override
    public int getIntrinsicHeight() {
        return intrinsicSize.height;
    }

    @Override
    public int getIntrinsicWidth() {
        return intrinsicSize.width;
    }

    @Override
    public void setLocation(int x, int y) {
        _component.setLocation(x, y);
    }

    @Override
    public Point getLocation() {
        return _component.getLocation();
    }

    @Override
    public void detach(LayoutContext c) {
        if (c.isInteractive()) {
            ((RootPanel)c.getCanvas()).remove(getJComponent());
        }
    }

    @Override
    public boolean isRequiresInteractivePaint() {
        return false;
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
