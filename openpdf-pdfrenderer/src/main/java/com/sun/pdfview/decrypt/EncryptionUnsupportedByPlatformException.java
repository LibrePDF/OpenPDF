/* Copyright 2008 Pirion Systems Pty Ltd, 139 Warry St,
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
 * Identifies that the specified encryption mechanism, though supported by the
 * product, is not supported by the platform that it is running on; i.e., that
 * either the JCE does not support a required cipher or that its policy is
 * such that a key of a given length can not be used.
 *
 * @author Luke Kirby
 */
public class EncryptionUnsupportedByPlatformException
        extends UnsupportedEncryptionException {

    public EncryptionUnsupportedByPlatformException(String message) {
        super(message);
    }

    public EncryptionUnsupportedByPlatformException(String message, Throwable cause) {
        super(message, cause);
    }
}