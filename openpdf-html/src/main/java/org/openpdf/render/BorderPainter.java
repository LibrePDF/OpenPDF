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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.render;

import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.FSRGBColor;
import org.openpdf.css.style.BorderRadiusCorner;
import org.openpdf.css.style.derived.BorderPropertySet;
import org.openpdf.extend.OutputDevice;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Path2D;


public class BorderPainter {
    public static final int TOP = 1;
    public static final int LEFT = 2;
    public static final int BOTTOM = 4;
    public static final int RIGHT = 8;
    public static final int ALL = TOP | LEFT | BOTTOM | RIGHT;
    /**
     * Generates a full round rectangle that is made of bounds and border
     * @param bounds Dimensions of the rect
     * @param border The border specs
     * @param inside Set true if you want the inner bounds of borders
     * @return A Path that is all sides of the round rectangle
     */
    public static Shape generateBorderBounds(Rectangle bounds, BorderPropertySet border, boolean inside) {
        Path2D path = generateBorderShape(bounds, TOP, border, false, inside ? 1 : 0, 1, false);
        path.append(generateBorderShape(bounds, RIGHT, border, false, inside ? 1 : 0, 1, false), true);
        path.append(generateBorderShape(bounds, BOTTOM, border, false, inside ? 1 : 0, 1, false), true);
        path.append(generateBorderShape(bounds, LEFT, border, false, inside ? 1 : 0, 1, false), true);
        return path;
    }


    /**
     * Generates one side of a border
     * @param bounds bounds of the container
     * @param side what side you want
     * @param border border props
     * @param drawInterior if you want it to be 2d or not, if false it will be just a line
     * @return a path for the side chosen!
     */
    public static Path2D generateBorderShape(Rectangle bounds, int side, BorderPropertySet border, boolean drawInterior) {
        return generateBorderShape(bounds, side, border, drawInterior, 0, 1, true);
    }
    /**
     * Generates one side of a border
     * @param bounds bounds of the container
     * @param side what side you want
     * @param border border props
     * @param drawInterior if you want it to be 2d or not, if false it will be just a line
     * @param scaledOffset insets the border by multiplying border widths by this variable, best use would be 1 or .5, cant see it for much other than that
     * @return a path for the side chosen!
     */
    public static Path2D generateBorderShape(Rectangle bounds, int side, BorderPropertySet border, boolean drawInterior, float scaledOffset) {
        return generateBorderShape(bounds, side, border, drawInterior, scaledOffset, 1, true);
    }

    /**
     * Generates one side of a border
     * @param bounds bounds of the container
     * @param side what side you want
     * @param border border props
     * @param drawInterior if you want it to be 2d or not, if false it will be just a line
     * @param scaledOffset insets the border by multiplying border widths by this variable, best use would be 1 or .5, cant see it for much other than that
     * @param widthScale scales the border widths by this factor, useful for drawing half borders for border types like groove or double
     * @return a path for the side chosen!
     */
    public static Path2D generateBorderShape(Rectangle bounds, int side, BorderPropertySet border, boolean drawInterior, float scaledOffset, float widthScale, boolean overlap) {
        /*
         * Function overview: Prior to creating the path we check what side were building this on. All the coordinates in this function assume its building a top border
         * the border is then rotated and translated to its appropriate side. Uses of "left" and "right" are assuming a perspective of inside the shape looking out.
         */
        // we do not want any overlap for shape used to render background area
        // we want overlap for borders slightly, reducing the integer roundoff error in painting. This removes the tiny white line
        // between the 2 different borders. The better way would be to calculate the end location of the other border side and use that instead.
        int overlapAngle = overlap ? 1 : 0;
        border = border.normalizedInstance(new Rectangle(bounds.width, bounds.height));

        RelativeBorderProperties props = new RelativeBorderProperties(border, side, widthScale);
        float sideWidth;
        if (props.isDimensionsSwapped()) {
            sideWidth = bounds.height - (1 + scaledOffset) * widthScale * (border.top() + border.bottom());
        } else {
            sideWidth = bounds.width - (1 + scaledOffset) * widthScale * (border.left() + border.right());
        }
        Path2D path = new Path2D.Float();

        float fullAngle = 90;
        float defaultAngle = fullAngle / 2;
        float angle = defaultAngle;
        float widthSum = props.getTop() + props.getLeft();
        if (widthSum != 0.0f) { // Avoid NaN
            angle = fullAngle * props.getTop() / widthSum;
        }
        appendPath(path, 0-props.getLeft(), 0-props.getTop(), props.getLeftCorner().left(), props.getLeftCorner().right(), 90+angle+overlapAngle, -angle-overlapAngle, props.getTop(), props.getLeft(), scaledOffset, true);

        angle = defaultAngle;
        widthSum = props.getTop() + props.getRight();
        if (widthSum != 0.0f) { // Avoid NaN
            angle = fullAngle * props.getTop() / widthSum;
        }
        appendPath(path, sideWidth+props.getRight(), 0-props.getTop(), props.getRightCorner().right(), props.getRightCorner().left(), 90, -angle-overlapAngle, props.getTop(), props.getRight(), scaledOffset, false);

        if(drawInterior) {
            //border = border.normalizeBorderRadius(new Rectangle((int)(bounds.width), (int)(bounds.height)));
            //props = new RelativeBorderProperties(bounds, border, 0f, side, 1+scaledOffset, 1);

            appendPath(path, sideWidth, 0, props.getRightCorner().right(), props.getRightCorner().left(), 90-angle-overlapAngle, angle+overlapAngle, props.getTop(), props.getRight(), scaledOffset+1, false);

            angle = defaultAngle;
            widthSum = props.getTop() + props.getLeft();
            if (widthSum != 0.0f) { // Avoid NaN
                angle = fullAngle * props.getTop() / widthSum;
            }
            appendPath(path, 0, 0, props.getLeftCorner().left(), props.getLeftCorner().right(), 90, angle+overlapAngle, props.getTop(), props.getLeft(), scaledOffset+1, true);
            path.closePath();
        }


        path.transform(AffineTransform.getTranslateInstance(
                (!props.isDimensionsSwapped() ? -bounds.width/2f : -bounds.height/2f) + (scaledOffset+1)*props.getLeft(),
                (props.isDimensionsSwapped() ? -bounds.width/2f : -bounds.height/2f) + (scaledOffset+1)*props.getTop()));
        path.transform(AffineTransform.getRotateInstance(
                props.getRotation()));
        // empirical: add 0.5 to better play with rasterization rules
        path.transform(AffineTransform.getTranslateInstance(
                bounds.width/2f+bounds.x, bounds.height/2f+bounds.y));

        return path;
    }

    private static void appendPath(Path2D path, float xOffset, float yOffset, float radiusVert, float radiusHoriz, float startAngle, float distance, float topWidth, float sideWidth, float scaleOffset, boolean left) {
        float innerWidth = 2 * radiusHoriz - scaleOffset * sideWidth - scaleOffset * sideWidth;
        float innerHeight = 2 * radiusVert - scaleOffset * topWidth - scaleOffset * topWidth;

        if (innerWidth > 0 && innerHeight > 0) {
            // do arc
            Arc2D arc = new Arc2D.Float(
                    xOffset - (left ? 0 : innerWidth),
                    yOffset,
                    innerWidth,
                    innerHeight, startAngle, distance, Arc2D.OPEN);
            path.append(arc, true);
        } else {
            // do line
            if (path.getCurrentPoint() == null) {
                path.moveTo(xOffset, yOffset);
            } else {
                path.lineTo(xOffset, yOffset);
            }
        }
    }

    private static class RelativeBorderProperties {
        private final float _top;
        private final float _left;
        private final float _right;
        private final BorderRadiusCorner _leftCorner;
        private final BorderRadiusCorner _rightCorner;

        private final double _rotation;
        private final boolean _dimensionsSwapped;

        public RelativeBorderProperties(BorderPropertySet props, int side, float widthScale) {

            if ((side & BorderPainter.TOP) == BorderPainter.TOP) {
                _top = props.top()*widthScale;
                _left = props.left()*widthScale;
                _right = props.right()*widthScale;
                _leftCorner = props.getTopLeft();
                _rightCorner = props.getTopRight();
                _rotation = 0;
                _dimensionsSwapped = false;
            } else if ((side & BorderPainter.RIGHT) == BorderPainter.RIGHT) {
                _top = props.right()*widthScale;
                _left = props.top()*widthScale;
                _right = props.bottom()*widthScale;
                _leftCorner = props.getTopRight();
                _rightCorner = props.getBottomRight();
                _rotation = Math.PI/2;
                _dimensionsSwapped = true;
            } else if ((side & BorderPainter.BOTTOM) == BorderPainter.BOTTOM) {
                _top = props.bottom()*widthScale;
                _left = props.right()*widthScale;
                _right = props.left()*widthScale;
                _leftCorner = props.getBottomRight();
                _rightCorner = props.getBottomLeft();
                _rotation = Math.PI;
                _dimensionsSwapped = false;
            } else if ((side & BorderPainter.LEFT) == BorderPainter.LEFT) {
                _top = props.left()*widthScale;
                _left = props.bottom()*widthScale;
                _right = props.top()*widthScale;
                _leftCorner = props.getBottomLeft();
                _rightCorner = props.getTopLeft();
                _rotation = 3*Math.PI/2;
                _dimensionsSwapped = true;
            } else {
                throw new IllegalArgumentException("No side found");
            }
        }

        public BorderRadiusCorner getRightCorner() {
            return _rightCorner;
        }
        public BorderRadiusCorner getLeftCorner() {
            return _leftCorner;
        }
        public float getTop() {
            return _top;
        }
        public float getLeft() {
            return _left;
        }
        public float getRight() {
            return _right;
        }

        private double getRotation() {
            return _rotation;
        }

        private boolean isDimensionsSwapped() {
            return _dimensionsSwapped;
        }
    }

    /**
     * @param xOffset for determining starting point for patterns
     */
    public static void paint(
            Rectangle bounds, int sides, BorderPropertySet border,
            RenderingContext ctx, int xOffset) {
        if ((sides & BorderPainter.TOP) == BorderPainter.TOP && border.noTop()) {
            sides -= BorderPainter.TOP;
        }
        if ((sides & BorderPainter.LEFT) == BorderPainter.LEFT && border.noLeft()) {
            sides -= BorderPainter.LEFT;
        }
        if ((sides & BorderPainter.BOTTOM) == BorderPainter.BOTTOM && border.noBottom()) {
            sides -= BorderPainter.BOTTOM;
        }
        if ((sides & BorderPainter.RIGHT) == BorderPainter.RIGHT && border.noRight()) {
            sides -= BorderPainter.RIGHT;
        }

        //Now paint!
        if ((sides & BorderPainter.TOP) == BorderPainter.TOP && border.topColor() != FSRGBColor.TRANSPARENT) {
            paintBorderSide(ctx.getOutputDevice(),
                    border, bounds, BorderPainter.TOP, border.topStyle(), xOffset);
        }
        if ((sides & BorderPainter.BOTTOM) == BorderPainter.BOTTOM && border.bottomColor() != FSRGBColor.TRANSPARENT) {
            paintBorderSide(ctx.getOutputDevice(),
                    border, bounds, BorderPainter.BOTTOM, border.bottomStyle(), xOffset);
        }
        if ((sides & BorderPainter.LEFT) == BorderPainter.LEFT && border.leftColor() != FSRGBColor.TRANSPARENT) {
            paintBorderSide(ctx.getOutputDevice(),
                    border, bounds, BorderPainter.LEFT, border.leftStyle(), xOffset);
        }
        if ((sides & BorderPainter.RIGHT) == BorderPainter.RIGHT && border.rightColor() != FSRGBColor.TRANSPARENT) {
            paintBorderSide(ctx.getOutputDevice(),
                    border, bounds, BorderPainter.RIGHT, border.rightStyle(), xOffset);
        }
    }

    private static void paintBorderSide(OutputDevice outputDevice, final BorderPropertySet border,
                                        final Rectangle bounds, int currentSide,
                                        final IdentValue borderSideStyle, int xOffset) {
        if (borderSideStyle == IdentValue.RIDGE || borderSideStyle == IdentValue.GROOVE) {
           BorderPropertySet borderA;
           BorderPropertySet borderB;
           if (borderSideStyle == IdentValue.RIDGE) {
                borderA = border;
                borderB = border.darken();
            } else {
                borderA = border.darken();
                borderB = border;
            }
           paintBorderSideShape(
                   outputDevice, bounds, borderA,
                   borderB,
                   0, 1, currentSide);
           paintBorderSideShape(
                   outputDevice, bounds, borderB,
                   borderA,
                   1, .5f, currentSide);
        } else if (borderSideStyle == IdentValue.OUTSET) {
            paintBorderSideShape(outputDevice, bounds,
                    border,
                    border.darken(),
                    0, 1, currentSide);
        } else if (borderSideStyle == IdentValue.INSET) {
            paintBorderSideShape(outputDevice, bounds,
                    border.darken(),
                    border,
                    0, 1, currentSide);
        } else if (borderSideStyle == IdentValue.SOLID) {
            outputDevice.setStroke(new BasicStroke(1f));
            if(currentSide == TOP) {
                outputDevice.setColor(border.topColor());
                outputDevice.fill(generateBorderShape(bounds, TOP, border, true, 0, 1, true));
            }
            if(currentSide == RIGHT) {
                outputDevice.setColor(border.rightColor());
                outputDevice.fill(generateBorderShape(bounds, RIGHT, border, true, 0, 1, true));
            }
            if(currentSide == BOTTOM) {
                outputDevice.setColor(border.bottomColor());
                outputDevice.fill(generateBorderShape(bounds, BOTTOM, border, true, 0, 1, true));
            }
            if(currentSide == LEFT) {
                outputDevice.setColor(border.leftColor());
                outputDevice.fill(generateBorderShape(bounds, LEFT, border, true, 0, 1, true));
            }

        } else if (borderSideStyle == IdentValue.DOUBLE) {
            paintDoubleBorder(outputDevice, border, bounds, currentSide);
        } else {
            int thickness = 0;
            if (currentSide == BorderPainter.TOP) thickness = (int) border.top();
            if (currentSide == BorderPainter.BOTTOM) thickness = (int) border.bottom();
            if (currentSide == BorderPainter.RIGHT) thickness = (int) border.right();
            if (currentSide == BorderPainter.LEFT) thickness = (int) border.left();
            if (borderSideStyle == IdentValue.DASHED) {
                //outputDevice.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                paintPatternedRect(outputDevice, bounds, border, border, new float[]{8.0f + thickness * 2, 4.0f + thickness}, currentSide, xOffset);
                //outputDevice.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            if (borderSideStyle == IdentValue.DOTTED) {
                // turn off antialiasing or the dots will be all blurry
                //outputDevice.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                paintPatternedRect(outputDevice, bounds, border, border, new float[]{thickness, thickness}, currentSide, xOffset);
                //outputDevice.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
        }
    }

    private static void paintDoubleBorder(
            OutputDevice outputDevice, BorderPropertySet border,
            Rectangle bounds, int currentSide) {
        // draw outer border
        paintSolid(outputDevice, bounds, border, 0, 1/3f, currentSide);
        // draw inner border
        //paintSolid(outputDevice, bounds, border, 1, 1/3f, sides, currentSide, bevel);
        paintSolid(outputDevice, bounds, border, 2, 1/3f, currentSide);
    }

    /**
     * @param xOffset     for inline borders, to determine dash_phase of top and bottom
     */
    private static void paintPatternedRect(OutputDevice outputDevice,
            final Rectangle bounds, final BorderPropertySet border,
            final BorderPropertySet color, final float[] pattern,
                                           final int currentSide, int xOffset) {
        Stroke old_stroke = outputDevice.getStroke();

        Path2D path = generateBorderShape(bounds, currentSide, border, false, .5f, 1, true);
        Area clip = new Area(generateBorderShape(bounds, currentSide, border, true, 0, 1, false));
        Shape old_clip = outputDevice.getClip();
        if (old_clip != null) {
            // we need to respect the clip sent to us, get the intersection between the old and the new
            clip.intersect(new Area(old_clip));
        }
        outputDevice.setClip(clip);
        if (currentSide == BorderPainter.TOP) {
            outputDevice.setColor(color.topColor());
            outputDevice.setStroke(new BasicStroke(2f * border.top(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, xOffset));
            outputDevice.drawBorderLine(
                    path, BorderPainter.TOP, (int)border.top(), false);
        } else if (currentSide == BorderPainter.LEFT) {
            outputDevice.setColor(color.leftColor());
            outputDevice.setStroke(new BasicStroke(2f * border.left(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
            outputDevice.drawBorderLine(
                    path, BorderPainter.LEFT, (int)border.left(), false);
        } else if (currentSide == BorderPainter.RIGHT) {
            outputDevice.setColor(color.rightColor());
            outputDevice.setStroke(new BasicStroke(2f * border.right(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, 0));
            outputDevice.drawBorderLine(
                    path, BorderPainter.RIGHT, (int)border.right(), false);
        } else if (currentSide == BorderPainter.BOTTOM) {
            outputDevice.setColor(color.bottomColor());
            outputDevice.setStroke(new BasicStroke(2f * border.bottom(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern, xOffset));
            outputDevice.drawBorderLine(
                    path, BorderPainter.BOTTOM, (int)border.bottom(), false);
        }

        outputDevice.setClip(old_clip);
        outputDevice.setStroke(old_stroke);
    }

    private static void paintBorderSideShape(OutputDevice outputDevice,
            final Rectangle bounds, final BorderPropertySet high, final BorderPropertySet low,
            final float offset, final float scale, int currentSide) {
        if (currentSide == BorderPainter.TOP) {
            paintSolid(outputDevice, bounds, high, offset, scale, currentSide);
        } else if (currentSide == BorderPainter.BOTTOM) {
            paintSolid(outputDevice, bounds, low, offset, scale, currentSide);
        } else if (currentSide == BorderPainter.RIGHT) {
            paintSolid(outputDevice, bounds, low, offset, scale, currentSide);
        } else if (currentSide == BorderPainter.LEFT) {
            paintSolid(outputDevice, bounds, high, offset, scale, currentSide);
        }
    }

    private static void paintSolid(OutputDevice outputDevice,
                                   final Rectangle bounds, final BorderPropertySet border,
                                   final float offset, final float scale, int currentSide) {

        if (currentSide == BorderPainter.TOP) {
            outputDevice.setColor(border.topColor());
            // draw a 1px border with a line instead of a polygon
            if ((int) border.top() == 1) {
                Shape line = generateBorderShape(bounds, currentSide, border, false, offset, scale, true);
                outputDevice.draw(line);
            } else {
                Shape line = generateBorderShape(bounds, currentSide, border, true, offset, scale, true);
                // use polygons for borders over 1px wide
                outputDevice.fill(line);
            }
        } else if (currentSide == BorderPainter.BOTTOM) {
            outputDevice.setColor(border.bottomColor());
            if ((int) border.bottom() == 1) {
                Shape line = generateBorderShape(bounds, currentSide, border, false, offset, scale, true);
                outputDevice.draw(line);
            } else {
                Shape line = generateBorderShape(bounds, currentSide, border, true, offset, scale, true);
                // use polygons for borders over 1px wide
                outputDevice.fill(line);
            }
        } else if (currentSide == BorderPainter.RIGHT) {
            outputDevice.setColor(border.rightColor());
            if ((int) border.right() == 1) {
                Shape line = generateBorderShape(bounds, currentSide, border, false, offset, scale, true);
                outputDevice.draw(line);
            } else {
                Shape line = generateBorderShape(bounds, currentSide, border, true, offset, scale, true);
                // use polygons for borders over 1px wide
                outputDevice.fill(line);
            }
        } else if (currentSide == BorderPainter.LEFT) {
            outputDevice.setColor(border.leftColor());
            if ((int) border.left() == 1) {
                Shape line = generateBorderShape(bounds, currentSide, border, false, offset, scale, true);
                outputDevice.draw(line);
            } else {
                Shape line = generateBorderShape(bounds, currentSide, border, true, offset, scale, true);
                // use polygons for borders over 1px wide
                outputDevice.fill(line);
            }
        }
    }
}
