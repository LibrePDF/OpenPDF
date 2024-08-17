/*
 * Copyright 2008 Pirion Systems Pty Ltd, 139 Warry St,
 * Fortitude Valley, Queensland, Australia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.sun.pdfview.decrypt;

/**
 * Identifies that the specified encryption mechanism is not
 * supported by this product, that is, PDFRenderer, as opposed to
 * a {@link EncryptionUnsupportedByPlatformException limitation in
 * the platform}. 
 *
 * @author Luke Kirby
 */
public class EncryptionUnsupportedByProductException
        extends UnsupportedEncryptionException {

    public EncryptionUnsupportedByProductException(String message) {
        super(message);
    }
}