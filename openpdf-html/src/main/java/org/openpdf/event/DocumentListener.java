/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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
package org.openpdf.event;

/**
 * Implementations of this listener interface receive notifications about
 * various document and layout events. Events are called on the Event Dispatch Thread, and will block
 * any EDT activity until the methods return; make sure you do as little as possible in each method, or where necessary,
 * spin the task off to a separate thread.
 */
public interface DocumentListener {
    /**
     * Indicates document has been requested (e.g. a new document is going to be
     * loaded). This will be called before any activity takes place for the
     * document.
     */
    void documentStarted();

    /**
     * Indicates document layout has complete, e.g. document is fully "loaded"
     * for display; this is not a callback for the document source (e.g. XML)
     * being loaded. This method will be called on every layout run (including,
     * for example, after panel resizes).
     */
    void documentLoaded();

    /**
     * Called when document layout failed with an exception. All
     * {@code Throwable} objects thrown (except for
     * {@code ThreadDeath}) during layout and not otherwise handled will
     * be provided to this method. If a {@code DocumentListener} has been
     * defined an XHTML panel, the listener is entirely responsible for
     * handling the exception. No other action will be taken.
     */
    void onLayoutException(Throwable t);

    /**
     * Called when document render failed with an exception. All
     * {@code Throwable} objects thrown (except for
     * {@code ThreadDeath}) during render and not otherwise handled will
     * be provided to this method. If a {@code DocumentListener} has been
     * defined an XHTML panel, the listener is entirely responsible for
     * handling the exception. No other action will be taken.
     */
    void onRenderException(Throwable t);
}
