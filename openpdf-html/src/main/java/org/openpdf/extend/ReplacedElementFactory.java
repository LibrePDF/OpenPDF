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
package org.openpdf.extend;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.BlockBox;
import org.openpdf.simple.extend.FormSubmissionListener;

public interface ReplacedElementFactory {

    /**
     * <b>NOTE:</b> Only block equivalent elements can be replaced.
     *
     * @param cssWidth The CSS width of the element in dots (or {@code -1} if
     * width is {@code auto})
     * @param cssHeight The CSS height of the element in dots (or {@code -1}
     * if the height should be treated as {@code auto})
     * @return The {@code ReplacedElement} or {@code null} if no
     * {@code ReplacedElement} applies
     */
    @Nullable
    ReplacedElement createReplacedElement(
            LayoutContext c, BlockBox box,
            UserAgentCallback uac, int cssWidth, int cssHeight);

    /**
     * Instructs the {@code ReplacedElementFactory} to discard any cached
     * data (typically because a new page is about to be loaded).
     */
    void reset();

    /**
     * Removes any reference to {@code Element} {@code e}.
     */
    void remove(Element e);

    /**
     * Identifies the FSL which will be used for callbacks when a form submit action is executed; you can use a
     * {@link org.openpdf.simple.extend.DefaultFormSubmissionListener} if you don't want any action to be taken.
     *
     * @param listener the listener instance to receive callbacks on form submission.
     */
    void setFormSubmissionListener(FormSubmissionListener listener);
}
