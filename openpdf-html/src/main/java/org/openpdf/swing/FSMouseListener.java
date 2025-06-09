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

import java.awt.event.MouseEvent;

import org.openpdf.render.Box;


/**
 * An FSMouseListener is used to track mouse events on a subclass of {@link BasicPanel}.
 * FSMouseListener instances can be added to the panel via {@link BasicPanel#addMouseTrackingListener(FSMouseListener)}.
 * The listener will automatically receive callbacks as the user moves over the panel. It's the developer's
 * responsibility to decide how to handle the events, in particular, locating the particular Box instance in the
 * tree which should be acted upon. You may find it more useful to use one of the existing subclasses, for
 * example {@link org.openpdf.swing.LinkListener}, {@link org.openpdf.swing.HoverListener}, or
 * {@link org.openpdf.swing.CursorListener}.
 */
public interface FSMouseListener {
    /**
     * Sent when the mouse moves over a Box in the render tree.
     *
     * @param panel the panel where the box is displayed
     * @param box the box the mouse has just moved over
     */
    void onMouseOver(BasicPanel panel, Box box);
    /**
     * Sent when the mouse leaves a Box in the render tree after entering it.
     *
     * @param panel the panel where the box is displayed
     * @param box the box the mouse has just left
     */
    void onMouseOut(BasicPanel panel, Box box);
    /**
     * Sent when the mouse button is released while hovering over a Box in the render tree.
     *
     * @param panel the panel where the box is displayed
     * @param box the box where the mouse is currently located
     */
    void onMouseUp(BasicPanel panel, Box box);

    /**
     * Sent when the mouse button is pressed.
     *
     * @param panel the panel where the box is displayed
     * @param e information about the mouse position, etc.
     */
    void onMousePressed(BasicPanel panel, MouseEvent e);
    /**
     * Sent when the mouse button is held and the mouse is moved.
     *
     * @param panel the panel where the box is displayed
     * @param e information about the mouse position, etc.
     */
    void onMouseDragged(BasicPanel panel, MouseEvent e);

    /**
     * Implementation-dependent. Sent via {@link BasicPanel#resetMouseTracker()}.
     */
    void reset();
}
