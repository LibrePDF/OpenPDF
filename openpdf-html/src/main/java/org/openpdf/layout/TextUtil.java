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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.layout;

import com.google.errorprone.annotations.CheckReturnValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.util.Uu;

import static java.lang.Character.END_PUNCTUATION;
import static java.lang.Character.FINAL_QUOTE_PUNCTUATION;
import static java.lang.Character.INITIAL_QUOTE_PUNCTUATION;
import static java.lang.Character.OTHER_PUNCTUATION;
import static java.lang.Character.SPACE_SEPARATOR;
import static java.lang.Character.START_PUNCTUATION;
import static java.util.Locale.ROOT;

public class TextUtil {

    public static String transformText(String text, CalculatedStyle style) {
        IdentValue transform = style.getIdent(CSSName.TEXT_TRANSFORM);
        IdentValue fontVariant = style.getIdent(CSSName.FONT_VARIANT);
        return transformText(text, transform, fontVariant);
    }

    static String transformText(String text, IdentValue transform, IdentValue fontVariant) {
        if (transform == IdentValue.LOWERCASE) {
            text = text.toLowerCase(ROOT);
        }
        if (transform == IdentValue.UPPERCASE) {
            text = text.toUpperCase(ROOT);
        }
        if (transform == IdentValue.CAPITALIZE) {
            text = capitalizeWords(text);
        }

        if (fontVariant == IdentValue.SMALL_CAPS) {
            text = text.toUpperCase(ROOT);
        }
        return text;
    }

    public static String transformFirstLetterText(String text, CalculatedStyle style) {
        if (!text.isEmpty()) {
            IdentValue transform = style.getIdent(CSSName.TEXT_TRANSFORM);
            IdentValue fontVariant = style.getIdent(CSSName.FONT_VARIANT);
            char currentChar;
            for (int i = 0, end = text.length(); i < end; i++) {
                currentChar = text.charAt(i);
                if (!isFirstLetterSeparatorChar(currentChar)) {
                    if (transform == IdentValue.LOWERCASE) {
                        currentChar = Character.toLowerCase(currentChar);
                        text = replaceChar(text, currentChar, i);
                    } else if (transform == IdentValue.UPPERCASE || transform == IdentValue.CAPITALIZE || fontVariant == IdentValue.SMALL_CAPS) {
                        currentChar = Character.toUpperCase(currentChar);
                        text = replaceChar(text, currentChar, i);
                    }
                    break;
                }
            }
        }
        return text;
    }

    /**
     * Replace character at the specified index by another.
     *
     * @param text    Source text
     * @param newChar Replacement character
     * @return Returns the new text
     */
    public static String replaceChar(String text, char newChar, int index) {
        int textLength = text.length();
        StringBuilder b = new StringBuilder(textLength);
        for (int i = 0; i < textLength; i++) {
            if (i == index) {
                b.append(newChar);
            } else {
                b.append(text.charAt(i));
            }
        }
        return b.toString();
    }

    public static boolean isFirstLetterSeparatorChar(char c) {
        return switch (Character.getType(c)) {
            case START_PUNCTUATION,
                    END_PUNCTUATION,
                    INITIAL_QUOTE_PUNCTUATION,
                    FINAL_QUOTE_PUNCTUATION,
                    OTHER_PUNCTUATION,
                    SPACE_SEPARATOR -> true;
            default -> false;
        };
    }


    private static String capitalizeWords(String text) {
        if (text.isEmpty()) {
            return text;
        }

        String result = doCapitalizeWords(text);
        if (result.length() != text.length()) {
            Uu.p("error! to strings arent the same length = -" + result + "-" + text + "-");
        }
        return result;
    }

    @CheckReturnValue
    private static String doCapitalizeWords(String text) {
        StringBuilder sb = new StringBuilder();
        boolean cap = true;
        for (int i = 0; i < text.length(); i++) {
            String ch = text.substring(i, i + 1);
            if (cap) {
                sb.append(ch.toUpperCase(ROOT));
            } else {
                sb.append(ch);
            }
            cap = ch.equals(" ");
        }
        return sb.toString();
    }
}
