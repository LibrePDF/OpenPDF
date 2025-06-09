/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
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
package org.openpdf.render;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.extend.FSImage;

/**
 * A bean containing information necessary to draw a list marker.  This includes
 * font information from the block (for selecting the correct font when drawing
 * a text marker) or the data necessary to draw other types of markers.  It
 * also includes a reference to the first line box in the block box (which in
 * turn may be nested inside of other block boxes).  All markers are drawn
 * relative to the baseline of this line box.
 */
public class MarkerData {
    private final StrutMetrics _structMetrics;

    @Nullable
    private final TextMarker _textMarker;
    @Nullable
    private final GlyphMarker _glyphMarker;
    @Nullable
    private final ImageMarker _imageMarker;

    @Nullable
    private LineBox _referenceLine;
    @Nullable
    private LineBox _previousReferenceLine;

    public MarkerData(StrutMetrics structMetrics, @Nullable ImageMarker imageMarker, @Nullable GlyphMarker glyphMarker, @Nullable TextMarker textMarker) {
        _structMetrics = structMetrics;
        _imageMarker = imageMarker;
        _glyphMarker = glyphMarker;
        _textMarker = textMarker;
    }

    @Nullable
    @CheckReturnValue
    public GlyphMarker getGlyphMarker() {
        return _glyphMarker;
    }

    @Nullable
    @CheckReturnValue
    public TextMarker getTextMarker() {
        return _textMarker;
    }

    @Nullable
    @CheckReturnValue
    public ImageMarker getImageMarker() {
        return _imageMarker;
    }

    public StrutMetrics getStructMetrics() {
        return _structMetrics;
    }

    public int getLayoutWidth() {
        if (_textMarker != null) {
            return _textMarker.getLayoutWidth();
        } else if (_glyphMarker != null) {
            return _glyphMarker.getLayoutWidth();
        } else if (_imageMarker != null) {
            return _imageMarker.getLayoutWidth();
        } else {
            return 0;
        }
    }

    @Nullable
    @CheckReturnValue
    public LineBox getReferenceLine() {
        return _referenceLine;
    }

    public void setReferenceLine(LineBox referenceLine) {
        _previousReferenceLine = _referenceLine;
        _referenceLine = referenceLine;
    }

    public void restorePreviousReferenceLine(LineBox current) {
        if (current == _referenceLine) {
            _referenceLine = _previousReferenceLine;
        }
    }

    public static class ImageMarker {
        private final FSImage _image;
        private final int _layoutWidth;

        public ImageMarker(FSImage image, int layoutWidth) {
            _image = image;
            _layoutWidth = layoutWidth;
        }

        @CheckReturnValue
        public FSImage getImage() {
            return _image;
        }
        @CheckReturnValue
        public int getLayoutWidth() {
            return _layoutWidth;
        }
    }

    public static class GlyphMarker {
        private final int _diameter;
        private final int _layoutWidth;

        public GlyphMarker(int diameter, int layoutWidth) {
            _diameter = diameter;
            _layoutWidth = layoutWidth;
        }

        @CheckReturnValue
        public int getDiameter() {
            return _diameter;
        }

        @CheckReturnValue
        public int getLayoutWidth() {
            return _layoutWidth;
        }
    }

    public static class TextMarker {
        private final String _text;
        private final int _layoutWidth;

        public TextMarker(String text, int width) {
            _text = text;
            _layoutWidth = width;
        }

        @CheckReturnValue
        public String getText() {
            return _text;
        }

        @CheckReturnValue
        public int getLayoutWidth() {
            return _layoutWidth;
        }
    }
}
