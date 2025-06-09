/*
 * Breaker.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm,
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
 *
 */
package org.openpdf.layout.breaker;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.layout.LayoutContext;
import org.openpdf.layout.LineBreakContext;
import org.openpdf.layout.TextUtil;
import org.openpdf.layout.WhitespaceStripper;
import org.openpdf.render.FSFont;

import java.text.BreakIterator;

/**
 * A utility class that scans the text of a single inline box, looking for the
 * next break point.
 * @author Torbjoern Gannholm
 */
public class Breaker {

    private static final String DEFAULT_LANGUAGE = System.getProperty("org.openpdf.layout.breaker.default-language", "en");

    public static void breakFirstLetter(LayoutContext c, LineBreakContext context,
            int avail, CalculatedStyle style) {
        FSFont font = style.getFSFont(c);
        context.setEnd(getFirstLetterEnd(context.getMaster(), context.getStart()));
        context.setWidth(c.getTextRenderer().getWidth(
                c.getFontContext(), font, context.getCalculatedSubstring()));

        if (context.getWidth() > avail) {
            context.setNeedsNewLine(true);
            context.setUnbreakable(true);
        }
    }

    private static int getFirstLetterEnd(String text, int start) {
        boolean letterFound = false;
        int end = text.length();
        char currentChar;
        for ( int i = start; i < end; i++ ) {
            currentChar = text.charAt(i);
            if (!TextUtil.isFirstLetterSeparatorChar(currentChar)) {
                if (letterFound) {
                    return i;
                } else {
                    letterFound = true;
                }
            }
        }
        return end;
    }

    public static void breakText(LayoutContext c,
            LineBreakContext context, int avail, CalculatedStyle style) {
        FSFont font = style.getFSFont(c);
        IdentValue whitespace = style.getWhitespace();

        // ====== handle nowrap
        if (whitespace == IdentValue.NOWRAP) {
            context.setEnd(context.getLast());
            context.setWidth(c.getTextRenderer().getWidth(
                    c.getFontContext(), font, context.getCalculatedSubstring()));
            return;
        }

        //check if we should break on the next newline
        if (whitespace == IdentValue.PRE ||
                whitespace == IdentValue.PRE_WRAP ||
                whitespace == IdentValue.PRE_LINE) {
            int n = context.getStartSubstring().indexOf(WhitespaceStripper.EOL);
            if (n > -1) {
                context.setEnd(context.getStart() + n + 1);
                context.setWidth(c.getTextRenderer().getWidth(
                        c.getFontContext(), font, context.getCalculatedSubstring()));
                context.setNeedsNewLine(true);
                context.setEndsOnNL(true);
            } else if (whitespace == IdentValue.PRE) {
                context.setEnd(context.getLast());
                context.setWidth(c.getTextRenderer().getWidth(
                        c.getFontContext(), font, context.getCalculatedSubstring()));
            }
        }

        //check if we may wrap
        if (whitespace == IdentValue.PRE ||
                (context.isNeedsNewLine() && context.getWidth() <= avail)) {
            return;
        }

        context.setEndsOnNL(false);

        boolean tryToBreakAnywhere = style.getWordBreak() == IdentValue.BREAK_ALL;

        doBreakText(c, context, avail, style, tryToBreakAnywhere);
    }

    private static int getWidth(LayoutContext c, FSFont f, String text) {
        return c.getTextRenderer().getWidth(c.getFontContext(), f, text);
    }

    public static BreakPointsProvider getBreakPointsProvider(String text, LayoutContext c, Element element, CalculatedStyle style) {
        return c.getSharedContext().getLineBreakingStrategy().getBreakPointsProvider(text, getLanguage(c, element), style);
    }

    public static BreakPointsProvider getBreakPointsProvider(String text, LayoutContext c, Text textNode, CalculatedStyle style) {
        return c.getSharedContext().getLineBreakingStrategy().getBreakPointsProvider(text, getLanguage(c, textNode), style);
    }

    @CheckReturnValue
    private static String getLanguage(LayoutContext c, @Nullable Element element) {
        String language = element == null ? null : c.getNamespaceHandler().getLang(element);
        if (language == null || language.isEmpty()) {
            language = DEFAULT_LANGUAGE;
        }
        return language;
    }

    @CheckReturnValue
    private static String getLanguage(LayoutContext c, @Nullable Text textNode) {
        if (textNode != null) {
            Node parentNode = textNode.getParentNode();
            if (parentNode instanceof Element) {
                return getLanguage(c, (Element) parentNode);
            }
        }
        return DEFAULT_LANGUAGE;
    }

    private static void doBreakText(LayoutContext c,
            LineBreakContext context, int avail, CalculatedStyle style,
            boolean tryToBreakAnywhere) {
        FSFont f = style.getFSFont(c);
        String currentString = context.getStartSubstring();
        BreakPointsProvider iterator = getBreakPointsProvider(currentString, c, context.getTextNode(), style);
        if (tryToBreakAnywhere) {
            iterator = new BreakAnywhereLineBreakStrategy(currentString);
        }
        BreakPoint bp = iterator.next();
        BreakPoint lastBreakPoint = null;
        int right = -1;
        int previousWidth = 0;
        int previousPosition = 0;
        while (bp != null && bp.getPosition() != BreakIterator.DONE) {
            int currentWidth = getWidth(c, f, currentString.substring(previousPosition, bp.getPosition()) + bp.getHyphen());
            int widthWithHyphen = previousWidth + currentWidth;
            previousWidth = widthWithHyphen;
            previousPosition = bp.getPosition();
            if (widthWithHyphen > avail) break;
            right = previousPosition;
            lastBreakPoint = bp;
            bp = iterator.next();
        }

        // add hyphen if needed
        if (bp != null && bp.getPosition() != BreakIterator.DONE // it fits
                && right >= 0 // some break point found
                && !lastBreakPoint.getHyphen().isEmpty()) {
            context.setMaster(new StringBuilder(context.getMaster()).insert(context.getStart() + right, lastBreakPoint.getHyphen()).toString());
            right += lastBreakPoint.getHyphen().length();
        }

        if (bp != null && bp.getPosition() == BreakIterator.DONE) {
            context.setWidth(getWidth(c, f, currentString));
            context.setEnd(context.getMaster().length());
            //It fits!
            return;
        }

        context.setNeedsNewLine(true);
        if (right <= 0 && style.getWordWrap() == IdentValue.BREAK_WORD) {
            if (!tryToBreakAnywhere) {
                doBreakText(c, context, avail, style, true);
                return;
            }
        }

        if (right > 0) { // found a place to wrap
            context.setEnd(context.getStart() + right);
            context.setWidth(getWidth(c, f, context.getMaster().substring(context.getStart(), context.getStart() + right)));
            return;
        }

        // unbreakable string
        context.setEnd(context.getStart() + currentString.length());
        context.setUnbreakable(true);
        context.setWidth(getWidth(c, f, context.getCalculatedSubstring()));
    }

}


