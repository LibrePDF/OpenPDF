/*
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
 *
 */
package org.openpdf.render;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.extend.ContentFunction;
import org.openpdf.css.parser.FSFunction;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.CalculatedStyle.Edge;
import org.openpdf.layout.LayoutContext;
import org.openpdf.layout.Styleable;
import org.openpdf.layout.TextUtil;
import org.openpdf.layout.WhitespaceStripper;
import org.openpdf.layout.breaker.BreakPointsProvider;
import org.openpdf.layout.breaker.Breaker;

import java.text.BreakIterator;

/**
 * A class which represents a portion of an inline element. If an inline element
 * does not contain any nested elements, then a single {@code InlineBox}
 * object will contain the content for the entire element. Otherwise, multiple
 * {@code InlineBox} objects will be created corresponding to each
 * discrete chunk of text appearing in the element. It is not rendered directly
 * (and hence does not extend from {@link Box}), but does play an important
 * role in layout (for example, when calculating min/max widths). Note that it
 * does not contain children. Inline content is stored as a flat list in the
 * layout tree. However, {@code InlineBox} does contain enough
 * information to reconstruct the original element nesting and this is, in fact,
 * done during inline layout.
 *
 * @see InlineLayoutBox
 */
public class InlineBox implements Styleable {
    @Nullable
    private Element _element;

    private String _originalText;
    private String _text;
    private boolean _removableWhitespace;
    private boolean _startsHere;
    private boolean _endsHere;

    @Nullable
    private CalculatedStyle _style;

    @Nullable
    private final ContentFunction _contentFunction;
    @Nullable
    private final FSFunction _function;

    private boolean _minMaxCalculated;
    private int _maxWidth;
    private int _minWidth;

    private int _firstLineWidth;

    @Nullable
    private final String _pseudoElementOrClass;

    @Nullable
    private final Text _textNode;

    public InlineBox(String text, @Nullable Text textNode) {
        this(text, textNode, null, null, null, null);
    }

    public InlineBox(String text, @Nullable Text textNode,
                     @Nullable ContentFunction contentFunction, @Nullable FSFunction function,
                     @Nullable Element element, @Nullable String pseudoElementOrClass) {
        this(text, textNode, contentFunction, function, element, pseudoElementOrClass, null);
    }

    public InlineBox(String text, @Nullable Text textNode,
                     @Nullable ContentFunction contentFunction, @Nullable FSFunction function,
                     @Nullable Element element, @Nullable String pseudoElementOrClass,
                     @Nullable CalculatedStyle style) {
        _text = text;
        _originalText = text;
        _textNode = textNode;
        _contentFunction = contentFunction;
        _function = function;
        _element = element;
        _pseudoElementOrClass = pseudoElementOrClass;
        _style = style;
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
        _originalText = text;
    }

    public void applyTextTransform() {
        _text = _originalText;
        _text = TextUtil.transformText(_text, getStyle());
    }

    public boolean isRemovableWhitespace() {
        return _removableWhitespace;
    }

    public void setRemovableWhitespace(boolean removableWhitespace) {
        _removableWhitespace = removableWhitespace;
    }

    public boolean isEndsHere() {
        return _endsHere;
    }

    public void setEndsHere(boolean endsHere) {
        _endsHere = endsHere;
    }

    public boolean isStartsHere() {
        return _startsHere;
    }

    public void setStartsHere(boolean startsHere) {
        _startsHere = startsHere;
    }

    @CheckReturnValue
    @Nullable
    @Override
    public CalculatedStyle getStyle() {
        return _style;
    }

    @Override
    public void setStyle(@Nullable CalculatedStyle style) {
        _style = style;
    }

    @Nullable
    @CheckReturnValue
    @Override
    public Element getElement() {
        return _element;
    }

    @Override
    public void setElement(@Nullable Element element) {
        _element = element;
    }

    @Nullable
    @CheckReturnValue
    public ContentFunction getContentFunction() {
        return _contentFunction;
    }

    public boolean isDynamicFunction() {
        return _contentFunction != null;
    }

    @CheckReturnValue
    private int getTextWidth(LayoutContext c, String s) {
        return c.getTextRenderer().getWidth(
                c.getFontContext(),
                c.getFont(getStyle().getFont(c)),
                s);
    }

    @CheckReturnValue
    private int getMaxCharWidth(LayoutContext c, String s) {
        char[] chars = s.toCharArray();
        int result = 0;
        for (char aChar : chars) {
            int width = getTextWidth(c, Character.toString(aChar));
            if (width > result) {
                result = width;
            }
        }
        return result;
    }

    private void calcMaxWidthFromLineLength(LayoutContext c, int cbWidth, boolean trim) {
        int last = 0;
        int current;

        while ( (current = _text.indexOf(WhitespaceStripper.EOL, last)) != -1) {
            String target = _text.substring(last, current);
            if (trim) {
                target = target.trim();
            }
            int length = getTextWidth(c, target);
            if (last == 0) {
                length += getStyle().getMarginBorderPadding(c, cbWidth, Edge.LEFT);
            }
            if (length > _maxWidth) {
                _maxWidth = length;
            }
            if (last == 0) {
                _firstLineWidth = length;
            }
            last = current + 1;
        }

        String target = _text.substring(last);
        if (trim) {
            target = target.trim();
        }
        int length = getTextWidth(c, target);
        length += getStyle().getMarginBorderPadding(c, cbWidth, Edge.RIGHT);
        if (length > _maxWidth) {
            _maxWidth = length;
        }
        if (last == 0) {
            _firstLineWidth = length;
        }
    }

    @CheckReturnValue
    public int getSpaceWidth(LayoutContext c) {
        return c.getTextRenderer().getWidth(
                c.getFontContext(),
                getStyle().getFSFont(c),
                WhitespaceStripper.SPACE);

    }

    @CheckReturnValue
    public int getTrailingSpaceWidth(LayoutContext c) {
        if (!_text.isEmpty() && _text.charAt(_text.length()-1) == ' ') {
            return getSpaceWidth(c);
        } else {
            return 0;
        }
    }

    private int calcMinWidthFromWordLength(
            LayoutContext c, int cbWidth, boolean trimLeadingSpace, boolean includeWS) {
        int spaceWidth = getSpaceWidth(c);

        int last = 0;
        int current;
        int maxWidth = 0;
        int spaceCount = 0;

        boolean haveFirstWord = false;
        int firstWord = 0;
        int lastWord = 0;

        String text = getText(trimLeadingSpace);

        BreakPointsProvider breakIterator = Breaker.getBreakPointsProvider(text, c, getElement(), getStyle());

        // Breaker should be used
        while ( (current = breakIterator.next().getPosition()) != BreakIterator.DONE) {
            String currentWord = text.substring(last, current);
            int wordWidth = getTextWidth(c, currentWord);
            int minWordWidth;
            if (getStyle().getWordWrap() == IdentValue.BREAK_WORD) {
                minWordWidth = getMaxCharWidth(c, currentWord);
            } else {
                minWordWidth = wordWidth;
            }

            if (spaceCount > 0) {
                if (includeWS) {
                    for (int i = 0; i < spaceCount; i++) {
                        wordWidth += spaceWidth;
                        minWordWidth += spaceWidth;
                    }
                } else {
                    maxWidth += spaceWidth;
                }
                spaceCount = 0;
            }
            if (minWordWidth > 0) {
                if (! haveFirstWord) {
                    firstWord = minWordWidth;
                }
                lastWord = minWordWidth;
            }

            if (minWordWidth > _minWidth) {
                _minWidth = minWordWidth;
            }
            maxWidth += wordWidth;

            last = current;
            for (int i = current; i < text.length(); i++) {
                if (text.charAt(i) == ' ') {
                    spaceCount++;
                    last++;
                } else {
                    break;
                }
            }
        }

        String currentWord = text.substring(last);
        int wordWidth = getTextWidth(c, currentWord);
        int minWordWidth;
        if (getStyle().getWordWrap() == IdentValue.BREAK_WORD) {
            minWordWidth = getMaxCharWidth(c, currentWord);
        } else {
            minWordWidth = wordWidth;
        }
        if (spaceCount > 0) {
            if (includeWS) {
                for (int i = 0; i < spaceCount; i++) {
                    wordWidth += spaceWidth;
                    minWordWidth += spaceWidth;
                }
            } else {
                maxWidth += spaceWidth;
            }
            spaceCount = 0;
        }
        if (minWordWidth > 0) {
            if (! haveFirstWord) {
                firstWord = minWordWidth;
            }
            lastWord = minWordWidth;
        }
        if (minWordWidth > _minWidth) {
            _minWidth = minWordWidth;
        }
        maxWidth += wordWidth;

        if (isStartsHere()) {
            int leftMBP = getStyle().getMarginBorderPadding(c, cbWidth, Edge.LEFT);
            if (firstWord + leftMBP > _minWidth) {
                _minWidth = firstWord + leftMBP;
            }
            maxWidth += leftMBP;
        }

        if (isEndsHere()) {
            int rightMBP = getStyle().getMarginBorderPadding(c, cbWidth, Edge.RIGHT);
            if (lastWord + rightMBP > _minWidth) {
                _minWidth = lastWord + rightMBP;
            }
            maxWidth += rightMBP;
        }

        return maxWidth;
    }

    @CheckReturnValue
    private String getText(boolean trimLeadingSpace) {
        if (! trimLeadingSpace) {
            return getText();
        } else {
            if (!_text.isEmpty() && _text.charAt(0) == ' ') {
                return _text.substring(1);
            } else {
                return _text;
            }
        }
    }

    @CheckReturnValue
    private int getInlineMBP(LayoutContext c, int cbWidth) {
        return getStyle().getMarginBorderPadding(c, cbWidth, Edge.LEFT) +
            getStyle().getMarginBorderPadding(c, cbWidth, Edge.RIGHT);
    }

    public void calcMinMaxWidth(LayoutContext c, int cbWidth, boolean trimLeadingSpace) {
        if (! _minMaxCalculated) {
            IdentValue whitespace = getStyle().getWhitespace();
            if (whitespace == IdentValue.NOWRAP) {
                _minWidth = _maxWidth = getInlineMBP(c, cbWidth) + getTextWidth(c, getText(trimLeadingSpace));
            } else if (whitespace == IdentValue.PRE) {
                calcMaxWidthFromLineLength(c, cbWidth, false);
                _minWidth = _maxWidth;
            } else if (whitespace == IdentValue.PRE_WRAP) {
                calcMinWidthFromWordLength(c, cbWidth, false, true);
                calcMaxWidthFromLineLength(c, cbWidth, false);
            } else if (whitespace == IdentValue.PRE_LINE) {
                calcMinWidthFromWordLength(c, cbWidth, trimLeadingSpace, false);
                calcMaxWidthFromLineLength(c, cbWidth, true);
            } else /* if (whitespace == IdentValue.NORMAL) */ {
                _maxWidth = calcMinWidthFromWordLength(c, cbWidth, trimLeadingSpace, false);
            }
            _minWidth = Math.min(_maxWidth, _minWidth);
            _minMaxCalculated = true;
        }
    }

    public int getMaxWidth() {
        return _maxWidth;
    }

    public int getMinWidth() {
        return _minWidth;
    }

    public int getFirstLineWidth() {
        return _firstLineWidth;
    }

    @Nullable
    @CheckReturnValue
    @Override
    public String getPseudoElementOrClass() {
        return _pseudoElementOrClass;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("InlineBox: ");
        if (getElement() != null) {
            result.append("<");
            result.append(getElement().getNodeName());
            result.append("> ");
        } else {
            result.append("(anonymous) ");
        }
        if (getPseudoElementOrClass() != null) {
            result.append(':');
            result.append(getPseudoElementOrClass());
            result.append(' ');
        }
        if (isStartsHere() || isEndsHere()) {
            result.append("(");
            if (isStartsHere()) {
                result.append("S");
            }
            if (isEndsHere()) {
                result.append("E");
            }
            result.append(") ");
        }

        appendPositioningInfo(result);

        result.append("(");
        result.append(shortText());
        result.append(") ");
        return result.toString();
    }

    protected void appendPositioningInfo(StringBuilder result) {
        if (getStyle().isRelative()) {
            result.append("(relative) ");
        }
        if (getStyle().isFixed()) {
            result.append("(fixed) ");
        }
        if (getStyle().isAbsolute()) {
            result.append("(absolute) ");
        }
        if (getStyle().isFloated()) {
            result.append("(floated) ");
        }
    }

    private String shortText() {
        if (_text == null) {
            return null;
        } else {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < _text.length() && i < 40; i++) {
                char c = _text.charAt(i);
                if (c == '\n') {
                    result.append(' ');
                } else {
                    result.append(c);
                }
            }
            if (result.length() == 40) {
                result.append("...");
            }
            return result.toString();
        }
    }

    @Nullable
    public FSFunction getFunction() {
        return _function;
    }

    public void truncateText() {
        _text = "";
        _originalText = "";
    }

    @Nullable
    @CheckReturnValue
    public Text getTextNode() {
        return _textNode;
    }
}
