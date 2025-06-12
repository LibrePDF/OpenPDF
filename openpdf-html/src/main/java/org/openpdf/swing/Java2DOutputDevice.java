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
package org.openpdf.swing;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.css.parser.FSColor;
import org.openpdf.css.parser.FSRGBColor;
import org.openpdf.css.style.derived.FSLinearGradient;
import org.openpdf.css.style.derived.FSLinearGradient.StopValue;
import org.openpdf.extend.FSGlyphVector;
import org.openpdf.extend.FSImage;
import org.openpdf.extend.OutputDevice;
import org.openpdf.extend.ReplacedElement;
import org.openpdf.render.AbstractOutputDevice;
import org.openpdf.render.BlockBox;
import org.openpdf.render.FSFont;
import org.openpdf.render.InlineLayoutBox;
import org.openpdf.render.InlineText;
import org.openpdf.render.JustificationInfo;
import org.openpdf.render.RenderingContext;

import javax.swing.*;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class Java2DOutputDevice extends AbstractOutputDevice implements OutputDevice {
    private final Graphics2D _graphics;

    public Java2DOutputDevice(Graphics2D graphics) {
        _graphics = graphics;
    }

    public Java2DOutputDevice(BufferedImage outputImage) {
        this(outputImage.createGraphics());
    }

@Override
    public void drawSelection(RenderingContext c, InlineText inlineText) {
        if (inlineText.isSelected()) {
            InlineLayoutBox iB = inlineText.getParent();
            String text = inlineText.getSubstring();
            if (text != null && !text.isEmpty()) {
                FSFont font = iB.getStyle().getFSFont(c);
                FSGlyphVector glyphVector = c.getTextRenderer().getGlyphVector(
                        c.getOutputDevice(),
                        font,
                        inlineText.getSubstring());

                Rectangle start = c.getTextRenderer().getGlyphBounds(
                        c.getOutputDevice(),
                        font,
                        glyphVector,
                        inlineText.getSelectionStart(),
                        (float) (iB.getAbsX() + inlineText.getX()),
                        (float) (iB.getAbsY() + iB.getBaseline()));

                Rectangle end = c.getTextRenderer().getGlyphBounds(
                        c.getOutputDevice(),
                        font,
                        glyphVector,
                        inlineText.getSelectionEnd() - 1,
                        (float) (iB.getAbsX() + inlineText.getX()),
                        (float) (iB.getAbsY() + iB.getBaseline()));
                Graphics2D graphics = getGraphics();
                double scaleX = graphics.getTransform().getScaleX();
                boolean allSelected = (text.length() == inlineText.getSelectionEnd() - inlineText.getSelectionStart());
                int startX = (inlineText.getSelectionStart() == inlineText.getStart()) ? iB.getAbsX() + inlineText.getX() : (int) Math.round(start.x / scaleX);
                int endX = allSelected ? startX + inlineText.getWidth() : (int) Math.round((end.x + end.width) / scaleX);
                _graphics.setColor(UIManager.getColor("TextArea.selectionBackground"));  // FIXME
                fillRect(
                        startX,
                        iB.getAbsY(),
                        endX - startX,
                        iB.getHeight());

                _graphics.setColor(Color.WHITE); // FIXME
                setFont(iB.getStyle().getFSFont(c));

                drawSelectedText(c, inlineText, iB, glyphVector);
            }
        }
    }

    private void drawSelectedText(RenderingContext c, InlineText inlineText, InlineLayoutBox iB, FSGlyphVector glyphVector) {
        GlyphVector vector = ((AWTFSGlyphVector)glyphVector).getGlyphVector();

        // We'd like to draw only the characters that are actually selected, but
        // unfortunately vector.getGlyphPixelBounds() doesn't give us accurate
        // results with the result that text can appear to jump around as it's
        // selected.  To work around this, we draw the whole string, but move
        // non-selected characters offscreen.
        for (int i = 0; i < inlineText.getSelectionStart(); i++) {
            vector.setGlyphPosition(i, new Point2D.Float(-100000, -100000));
        }
        for (int i = inlineText.getSelectionEnd(); i < inlineText.getSubstring().length(); i++) {
            vector.setGlyphPosition(i, new Point2D.Float(-100000, -100000));
        }
        if(inlineText.getParent().getStyle().isTextJustify()) {
            JustificationInfo info = inlineText.getParent().getLineBox().getJustificationInfo();
            if(info!=null) {
                String string = inlineText.getSubstring();
                float adjust = 0.0f;
                for (int i = inlineText.getSelectionStart(); i < inlineText.getSelectionEnd(); i++) {
                    char ch = string.charAt(i);
                    if (i != 0) {
                        Point2D point = vector.getGlyphPosition(i);
                        vector.setGlyphPosition(
                                i, new Point2D.Double(point.getX() + adjust, point.getY()));
                    }
                    if (ch == ' ' || ch == '\u00a0' || ch == '\u3000') {
                        adjust += info.spaceAdjust();
                    } else {
                        adjust += info.nonSpaceAdjust();
                    }
                }

            }
        }
        c.getTextRenderer().drawGlyphVector(
                c.getOutputDevice(),
                glyphVector,
                (float) (iB.getAbsX() + inlineText.getX()),
                (float) (iB.getAbsY() + iB.getBaseline()));
    }

    @Override
    public void drawBorderLine(
            Shape bounds, int side, int lineWidth, boolean solid) {
       /* int x = bounds.x;
        int y = bounds.y;
        int w = bounds.width;
        int h = bounds.height;

        int adj = solid ? 1 : 0;

        if (side == BorderPainter.TOP) {
            drawLine(x, y + (int) (lineWidth / 2), x + w - adj, y + (int) (lineWidth / 2));
        } else if (side == BorderPainter.LEFT) {
            drawLine(x + (int) (lineWidth / 2), y, x + (int) (lineWidth / 2), y + h - adj);
        } else if (side == BorderPainter.RIGHT) {
            int offset = (int)(lineWidth / 2);
            if (lineWidth % 2 != 0) {
                offset += 1;
            }
            drawLine(x + w - offset, y, x + w - offset, y + h - adj);
        } else if (side == BorderPainter.BOTTOM) {
            int offset = (int)(lineWidth / 2);
            if (lineWidth % 2 != 0) {
                offset += 1;
            }
            drawLine(x, y + h - offset, x + w - adj, y + h - offset);
        }*/
        draw(bounds);
    }

    @Override
    public void paintReplacedElement(RenderingContext c, BlockBox box) {
        ReplacedElement replaced = box.getReplacedElement();
        if (replaced instanceof SwingReplacedElement) {
            Rectangle contentBounds = box.getContentAreaEdge(box.getAbsX(), box.getAbsY(), c);
            JComponent component = ((SwingReplacedElement)box.getReplacedElement()).getJComponent();
            RootPanel canvas = (RootPanel)c.getCanvas();
            CellRendererPane pane = canvas.getCellRendererPane();
            pane.paintComponent(_graphics, component, canvas, contentBounds.x,  contentBounds.y, contentBounds.width, contentBounds.height,true);
        } else if (replaced instanceof ImageReplacedElement) {
            Image image = ((ImageReplacedElement)replaced).getImage();

            Point location = replaced.getLocation();
            _graphics.drawImage(
                    image, (int)location.getX(), (int)location.getY(), null);
        }
    }

    @Override
    public void setOpacity(float opacity) {
        _graphics.setComposite(opacity == 1 ?
            AlphaComposite.SrcOver :
            AlphaComposite.SrcOver.derive(opacity)
        );
	}


    @Override
    public void setColor(FSColor color) {
        if (color instanceof FSRGBColor rgb) {
            _graphics.setColor(new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(),(int) (rgb.getAlpha() * 255)));
        } else {
            throw new RuntimeException("internal error: unsupported color class " + color.getClass().getName());
        }
    }

    @Override
    protected void drawLine(int x1, int y1, int x2, int y2) {
        _graphics.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
        _graphics.drawRect(x, y, width, height);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        _graphics.fillRect(x, y, width, height);
    }

    @Override
    public void setClip(Shape s) {
        _graphics.setClip(s);
    }

    @Nullable
    @CheckReturnValue
    @Override
    public Shape getClip() {
        return _graphics.getClip();
    }
@Override
    public void clip(Shape s) {
        _graphics.clip(s);
    }

    @Override
    public void translate(double tx, double ty) {
        _graphics.translate(tx, ty);
    }

    public Graphics2D getGraphics() {
        return _graphics;
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        _graphics.drawOval(x, y, width, height);
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        _graphics.fillOval(x, y, width, height);
    }

    @Nullable
    @CheckReturnValue
    @Override
    public Object getRenderingHint(RenderingHints.Key key) {
        return _graphics.getRenderingHint(key);
    }

    @Override
    public void setRenderingHint(RenderingHints.Key key, Object value) {
        _graphics.setRenderingHint(key, value);
    }

    @Override
    public void setFont(FSFont font) {
        _graphics.setFont(((AWTFSFont)font).getAWTFont());
    }

    @Override
    public void setStroke(Stroke s) {
        _graphics.setStroke(s);
    }

    @Override
    public Stroke getStroke() {
        return _graphics.getStroke();
    }

    @Override
    public void fill(Shape s) {
        _graphics.fill(s);
    }
@Override
    public void draw(Shape s) {
        _graphics.draw(s);
    }
@Override
    public void drawImage(FSImage image, int x, int y) {
        _graphics.drawImage(((AWTFSImage)image).getImage(), x, y, null);
    }
@Override
    public boolean isSupportsSelection() {
        return true;
    }
@Override
    public boolean isSupportsCMYKColors() {
        return true;
    }

	@Override
	public void drawLinearGradient(FSLinearGradient gradient, int x, int y, int width, int height)
	{
		float[] fractions = new float[gradient.getStopPoints().size()];
		Color[] colors = new Color[gradient.getStopPoints().size()];

		float range = gradient.getStopPoints().get(gradient.getStopPoints().size() - 1).getDotsValue() -
				gradient.getStopPoints().get(0).getDotsValue();

		int i = 0;
		for (StopValue pt : gradient.getStopPoints())
		{
	        if (pt.getColor() instanceof FSRGBColor rgb) {
                colors[i] = new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
	        } else {
	            throw new RuntimeException("internal error: unsupported color class " + pt.getColor().getClass().getName());
	        }

	        if (range != 0)
	        	fractions[i] = pt.getDotsValue() / range;

	        i++;
		}

        LinearGradientPaint paint = new LinearGradientPaint(
                (float)(gradient.getStartX() + x), (float)(gradient.getStartY() + y),
                (float)(gradient.getEndX() + x), (float)(gradient.getEndY() + y),
                fractions, colors);
		_graphics.setPaint(paint);
		_graphics.fillRect(x, y, width, height);
		_graphics.setPaint(null);
	}
}
