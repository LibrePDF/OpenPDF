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
package org.openpdf.pdf;

import org.jspecify.annotations.NonNull;
import org.openpdf.extend.FSImage;

import java.net.URI;

public class PDFAsImage implements FSImage {
    private final URI _source;

    private final float _width;
    private final float _height;
    private final float _unscaledWidth;
    private final float _unscaledHeight;

    public PDFAsImage(URI source, float width, float height) {
        _source = source;
        _width = width;
        _unscaledWidth = width;
        _height = height;
        _unscaledHeight = height;
    }

    private PDFAsImage(URI source, float unscaledWidth, float unscaledHeight, float width, float height) {
        _source = source;
        _width = width;
        _unscaledWidth = unscaledWidth;
        _height = height;
        _unscaledHeight = unscaledHeight;
    }

    @Override
    public int getWidth() {
        return (int)_width;
    }

    @Override
    public int getHeight() {
        return (int)_height;
    }

    @NonNull
    @Override
    public FSImage scale(int width, int height) {
        float targetWidth = width;
        float targetHeight = height;

        if (width == -1) {
            targetWidth = getWidthAsFloat() * (targetHeight / getHeight());
        }

        if (height == -1) {
            targetHeight = getHeightAsFloat() * (targetWidth / getWidth());
        }

        return new PDFAsImage(_source, _width, _height, targetWidth, targetHeight);
    }

    public URI getURI() {
        return _source;
    }

    public float getWidthAsFloat() {
        return _width;
    }

    public float getHeightAsFloat() {
        return _height;
    }

    public float getUnscaledHeight() {
        return _unscaledHeight;
    }

    public float getUnscaledWidth() {
        return _unscaledWidth;
    }

    public float scaleHeight() {
        return _height / _unscaledHeight;
    }

    public float scaleWidth() {
        return _width / _unscaledWidth;
    }

}
