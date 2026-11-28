/*
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 */
package org.openpdf.text;

import org.openpdf.text.pdf.PdfPCell;

/**
 * Encapsulates spacing and padding dimensions for {@link SimpleCell}, providing NaN-safe resolution
 * and layout calculations.
 */
class CellSpacingPadding {

    private float spacingLeft = Float.NaN;
    private float spacingRight = Float.NaN;
    private float spacingTop = Float.NaN;
    private float spacingBottom = Float.NaN;
    private float paddingLeft = Float.NaN;
    private float paddingRight = Float.NaN;
    private float paddingTop = Float.NaN;
    private float paddingBottom = Float.NaN;

    float getSpacingLeft() {
        return spacingLeft;
    }

    void setSpacingLeft(float spacingLeft) {
        this.spacingLeft = spacingLeft;
    }

    float getSpacingRight() {
        return spacingRight;
    }

    void setSpacingRight(float spacingRight) {
        this.spacingRight = spacingRight;
    }

    float getSpacingTop() {
        return spacingTop;
    }

    void setSpacingTop(float spacingTop) {
        this.spacingTop = spacingTop;
    }

    float getSpacingBottom() {
        return spacingBottom;
    }

    void setSpacingBottom(float spacingBottom) {
        this.spacingBottom = spacingBottom;
    }

    void setSpacing(float spacing) {
        this.spacingLeft = spacing;
        this.spacingRight = spacing;
        this.spacingTop = spacing;
        this.spacingBottom = spacing;
    }

    float getPaddingLeft() {
        return paddingLeft;
    }

    void setPaddingLeft(float paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    float getPaddingRight() {
        return paddingRight;
    }

    void setPaddingRight(float paddingRight) {
        this.paddingRight = paddingRight;
    }

    float getPaddingTop() {
        return paddingTop;
    }

    void setPaddingTop(float paddingTop) {
        this.paddingTop = paddingTop;
    }

    float getPaddingBottom() {
        return paddingBottom;
    }

    void setPaddingBottom(float paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    void setDefaultPadding(float padding) {
        if (Float.isNaN(paddingRight)) {
            paddingRight = padding;
        }
        if (Float.isNaN(paddingLeft)) {
            paddingLeft = padding;
        }
        if (Float.isNaN(paddingTop)) {
            paddingTop = padding;
        }
        if (Float.isNaN(paddingBottom)) {
            paddingBottom = padding;
        }
    }

    void copySpacingTo(SimpleCell target) {
        target.setSpacing_left(spacingLeft);
        target.setSpacing_right(spacingRight);
        target.setSpacing_top(spacingTop);
        target.setSpacing_bottom(spacingBottom);
    }

    void applyEffectivePaddingTo(PdfPCell cell) {
        cell.setPaddingLeft(zeroIfNaN(paddingLeft) + zeroIfNaN(spacingLeft));
        cell.setPaddingRight(zeroIfNaN(paddingRight) + zeroIfNaN(spacingRight));
        cell.setPaddingTop(zeroIfNaN(paddingTop) + zeroIfNaN(spacingTop));
        cell.setPaddingBottom(zeroIfNaN(paddingBottom) + zeroIfNaN(spacingBottom));
    }

    Rectangle createSpacedRectangle(Rectangle position) {
        return new Rectangle(
                position.getLeft(zeroIfNaN(spacingLeft)),
                position.getBottom(zeroIfNaN(spacingBottom)),
                position.getRight(zeroIfNaN(spacingRight)),
                position.getTop(zeroIfNaN(spacingTop)));
    }

    private static float zeroIfNaN(float value) {
        return Float.isNaN(value) ? 0f : value;
    }
}
