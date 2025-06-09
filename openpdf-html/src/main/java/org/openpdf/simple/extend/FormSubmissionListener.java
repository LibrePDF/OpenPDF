/*
 * {{{ header & license
 * Copyright (c) 2009 Christophe Marchand
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

package org.openpdf.simple.extend;


/**
 * FormSubmissionListener is used to receive callbacks when an XhtmlForm has its submit action called. The entire
 * query string is given over to the {@link #submit(String)} method, which can then be submitted back to the panel
 * for loading.
 *
 *
 * @author Christophe Marchand
 */
public interface FormSubmissionListener {
    /**
     * Called by XhtmlForm when a form is submitted.
     *
     * @param query the entire query string as composed of form elements and the form's action URL
     */
    void submit(String query);

}