/*
 * Copyright (c) 2009 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the
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
 */

package org.openpdf.swing;

import org.openpdf.render.Box;

import java.awt.event.MouseEvent;

/**
 * Default, no-op implementation of an FSMouseListener. Override the method as needed in your own subclass.
 */
public class DefaultFSMouseListener implements FSMouseListener {
    @Override
    public void onMouseOver(BasicPanel panel, Box box) { }

    @Override
    public void onMouseOut(BasicPanel panel, Box box) { }

    @Override
    public void onMouseUp(BasicPanel panel, Box box) { }

    @Override
    public void onMousePressed(BasicPanel panel, MouseEvent e) { }

    @Override
    public void onMouseDragged(BasicPanel panel, MouseEvent e) { }

    @Override
    public void reset() { }
}
