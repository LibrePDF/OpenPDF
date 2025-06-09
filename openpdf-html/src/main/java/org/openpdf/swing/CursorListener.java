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
package org.openpdf.swing;

import org.openpdf.render.Box;

import java.awt.*;


/**
 * A CursorListener is used to modify the current cursor in response to mouse events over the current
 * document. This implementation changes the cursor according to the cursor property that applies to the Box
 * on which the mouse is located.
 */
public class CursorListener extends DefaultFSMouseListener {
    @Override
    public void onMouseOver(BasicPanel panel, Box box) {
        Cursor c = box.getStyle().getCursor();

        if (!panel.getCursor().equals(c)) {
            panel.setCursor(c);
        }
    }
}
