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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.context;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.extend.ContentFunction;
import org.openpdf.css.parser.FSFunction;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.layout.CounterFunction;
import org.openpdf.layout.InlineBoxing;
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.Box;
import org.openpdf.render.InlineLayoutBox;
import org.openpdf.render.InlineText;
import org.openpdf.render.LineBox;
import org.openpdf.render.RenderingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.w3c.dom.css.CSSPrimitiveValue.CSS_IDENT;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_STRING;

public class ContentFunctionFactory {
    private final List<ContentFunction> _functions = new ArrayList<>();

    public ContentFunctionFactory() {
        _functions.add(new PageCounterFunction());
        _functions.add(new PagesCounterFunction());
        _functions.add(new TargetCounterFunction());
        _functions.add(new LeaderFunction());
    }

    @Nullable
    public ContentFunction lookupFunction(LayoutContext c, FSFunction function) {
        for (ContentFunction f : _functions) {
            if (f.canHandle(c, function)) {
                return f;
            }
        }
        return null;
    }

    public void registerFunction(ContentFunction function) {
        _functions.add(function);
    }

    private abstract static class PageNumberFunction implements ContentFunction {
        @Override
        public boolean isStatic() {
            return false;
        }

        @Nullable
        @Override
        public String calculate(LayoutContext c, FSFunction function) {
            return null;
        }

        @Override
        public String getLayoutReplacementText() {
            return "999";
        }

        protected IdentValue getListStyleType(FSFunction function) {
            IdentValue result = IdentValue.DECIMAL;

            List<PropertyValue> parameters = function.getParameters();
            if (parameters.size() == 2) {
                PropertyValue pValue = parameters.get(1);
                IdentValue iValue = IdentValue.valueOf(pValue.getStringValue());
                if (iValue != null) {
                    result = iValue;
                }
            }

            return result;
        }

        protected boolean isCounter(FSFunction function, String counterName) {
            if (function.is("counter")) {
                List<PropertyValue> parameters = function.getParameters();
                if (parameters.size() == 1 || parameters.size() == 2) {
                    PropertyValue param = parameters.get(0);
                    if (param.getPrimitiveType() != CSS_IDENT ||
                            !Objects.equals(param.getStringValue(), counterName)) {
                        return false;
                    }

                    if (parameters.size() == 2) {
                        param = parameters.get(1);
                        return param.getPrimitiveType() == CSS_IDENT;
                    }

                    return true;
                }
            }

            return false;
        }
    }

    private static class PageCounterFunction extends PageNumberFunction implements ContentFunction {
        @Override
        public String calculate(RenderingContext c, FSFunction function, InlineText text) {
            int value = c.getRootLayer().getRelativePageNo(c) + 1;
            return CounterFunction.createCounterText(getListStyleType(function), value);
        }

        @Override
        public boolean canHandle(LayoutContext c, FSFunction function) {
            return c.isPrint() && isCounter(function, "page");
        }
    }

    private static class PagesCounterFunction extends PageNumberFunction implements ContentFunction {
        @Override
        public String calculate(RenderingContext c, FSFunction function, InlineText text) {
            int value = c.getRootLayer().getRelativePageCount(c);
            return CounterFunction.createCounterText(getListStyleType(function), value);
        }

        @Override
        public boolean canHandle(LayoutContext c, FSFunction function) {
            return c.isPrint() && isCounter(function, "pages");
        }
    }

    /**
     * Partially implements target counter as specified here:
     * <a href="http://www.w3.org/TR/2007/WD-css3-gcpm-20070504/#cross-references">...</a>
     */
    private static class TargetCounterFunction implements ContentFunction {
        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public String calculate(RenderingContext c, FSFunction function, InlineText text) {
            String uri = text.getParent().getElement().getAttribute("href");
            if (uri.startsWith("#")) {
                String anchor = uri.substring(1);
                Box target = c.getBoxById(anchor);
                if (target != null) {
                    int pageNo = c.getRootLayer().getRelativePageNo(c, target.getAbsY());
                    return CounterFunction.createCounterText(IdentValue.DECIMAL, pageNo + 1);
                }
            }
            return "";
        }

        @Nullable
        @Override
        public String calculate(LayoutContext c, FSFunction function) {
            return null;
        }

        @Override
        public String getLayoutReplacementText() {
            return "999";
        }

        @Override
        public boolean canHandle(LayoutContext c, FSFunction function) {
            if (c.isPrint() && function.is("target-counter")) {
                List<PropertyValue> parameters = function.getParameters();
                if (parameters.size() == 2 || parameters.size() == 3) {
                    FSFunction f = parameters.get(0).getFunction();
                    if (f == null ||
                            f.getParameters().size() != 1 ||
                            f.getParameters().get(0).getPrimitiveType() != CSS_IDENT ||
                            ! "href".equals(f.getParameters().get(0).getStringValue())) {
                        return false;
                    }

                    PropertyValue param = parameters.get(1);
                    return param.getPrimitiveType() == CSS_IDENT &&
                            Objects.equals(param.getStringValue(), "page");
                }
            }

            return false;
        }
    }

    /**
     * Partially implements leaders as specified here:
     * <a href="http://www.w3.org/TR/2007/WD-css3-gcpm-20070504/#leaders">...</a>
     */
    private static class LeaderFunction implements ContentFunction {
        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public String calculate(RenderingContext c, FSFunction function, InlineText text) {
            InlineLayoutBox iB = text.getParent();
            LineBox lineBox = iB.getLineBox();

            // There might be a target-counter function after this function.
            // Because the leader should fill up the line, we need the correct
            // width and must first compute the target-counter function.
            boolean dynamic = false;
            for (Box child : lineBox.getChildren()) {
                if (child == iB) {
                    dynamic = true;
                } else if (dynamic && child instanceof InlineLayoutBox) {
                    ((InlineLayoutBox) child).lookForDynamicFunctions(c);
                }
            }
            if (dynamic) {
                int totalLineWidth = InlineBoxing.positionHorizontally(c, lineBox, 0);
                lineBox.setContentWidth(totalLineWidth);
            }

            // Get leader value and value width
            String value = getLeaderValue(function);

            // Compute value width using 100x string to get more precise width.
            // Otherwise, there might be a small gap on the right side. This is
            // necessary because a TextRenderer usually use double/float for width.
            String tmp = value.repeat(100);
            float valueWidth = c.getTextRenderer().getWidth(c.getFontContext(),
                    iB.getStyle().getFSFont(c), tmp) / 100.0f;
            int spaceWidth = c.getTextRenderer().getWidth(c.getFontContext(),
                    iB.getStyle().getFSFont(c), " ");

            // compute leader width and necessary count of values
            int leaderWidth = iB.getContainingBlockWidth() - iB.getLineBox().getWidth() + text.getWidth();
            int count = (int) ((leaderWidth - (2 * spaceWidth)) / valueWidth);

            String leaderString = ' ' + value.repeat(Math.max(0, count)) + ' ';

            // set left margin to ensure that the leader is right aligned (for TOC)
            int leaderStringWidth = c.getTextRenderer().getWidth(c.getFontContext(),
                    iB.getStyle().getFSFont(c), leaderString);
            iB.setMarginLeft(c, leaderWidth - leaderStringWidth);

            return leaderString;
        }

        @Nullable
        @CheckReturnValue
        private String getLeaderValue(FSFunction function) {
            final PropertyValue param = function.getParameters().get(0);
            final String value = param.getStringValue();
            if (param.getPrimitiveType() == CSS_IDENT) {
                return switch (value) {
                    case "dotted" -> ". ";
                    case "solid" -> "_";
                    case "space" -> " ";
                    default -> value;
                };
            }
            return value;
        }

        @Nullable
        @Override
        public String calculate(LayoutContext c, FSFunction function) {
            return null;
        }

        @Override
        @CheckReturnValue
        public String getLayoutReplacementText() {
            return " . ";
        }

        @Override
        @CheckReturnValue
        public boolean canHandle(LayoutContext c, FSFunction function) {
            if (c.isPrint() && function.is("leader")) {
                List<PropertyValue> parameters = function.getParameters();
                if (parameters.size() == 1) {
                    PropertyValue param = parameters.get(0);
                    return param.getPrimitiveType() == CSS_STRING ||
                            (param.getPrimitiveType() == CSS_IDENT &&
                                    ("dotted".equals(param.getStringValue()) ||
                                            "solid".equals(param.getStringValue()) ||
                                            "space".equals(param.getStringValue())));
                }
            }

            return false;
        }
    }
}
