/*
 * {{{ header & license
 * Copyright (c) 2007 Christophe Marchand
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.swing;

import org.openpdf.simple.XHTMLPanel;

/**
 * ScaleChangeEvent is used to notify interested parties that XHTMLPanel's scale factor has changed.
 *
 * @author Christophe Marchand (christophe.marchand@contactoffice.net)
 */
public class ScaleChangeEvent {

    private final XHTMLPanel pane;
    private final double scale;

    /**
     * Constructs a new ScaleChangeEvent
     *
     * @param pane  The panel where scale factor has changed
     * @param scale The new scale
     */
    public ScaleChangeEvent(XHTMLPanel pane, double scale) {
        super();
        this.pane = pane;
        this.scale = scale;
    }

    public XHTMLPanel getComponent() {
        return pane;
    }

    public double getScale() {
        return scale;
    }
}
