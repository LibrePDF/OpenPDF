/*
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
 */
package org.openpdf.simple.extend;

import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Locale.ROOT;

/**
 * Checks the values of presentational HTML attributes before they are written into a CSS declaration.
 * An attribute value is text from the document. Only a value that is a single CSS value of the expected kind
 * may become part of a style, otherwise the attribute could add declarations of its own.
 */
final class AttributeValues {
    private static final Pattern INTEGER = Pattern.compile("\\+?\\d+");
    private static final Pattern LENGTH = Pattern.compile("[+-]?(\\d+|\\d*\\.\\d+)(%|[a-zA-Z]+)?|auto|inherit",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern COLOR = Pattern.compile("#?[0-9a-f]+|[a-z]+|[a-z]+\\([0-9a-z.,%/ +-]*\\)",
            Pattern.CASE_INSENSITIVE);
    private static final Set<String> TEXT_ALIGN = Set.of("left", "right", "center", "justify", "inherit");
    private static final Set<String> VERTICAL_ALIGN = Set.of("baseline", "sub", "super", "top", "text-top",
            "middle", "bottom", "text-bottom", "inherit");

    private AttributeValues() {
    }

    /**
     * @return true if the value is a non-negative integer, as used for colspan, rowspan and span
     */
    static boolean isInteger(@Nullable String value) {
        return value != null && INTEGER.matcher(value).matches();
    }

    /**
     * @return true if the value is a number with an optional unit, a percentage, or the keyword auto or inherit
     */
    static boolean isLength(@Nullable String value) {
        return value != null && LENGTH.matcher(value).matches();
    }

    /**
     * @return true if the value is a color name, a hexadecimal color or a color function such as rgb(1, 2, 3)
     */
    static boolean isColor(@Nullable String value) {
        return value != null && COLOR.matcher(value).matches();
    }

    /**
     * @return true if the value is a keyword of the CSS property text-align
     */
    static boolean isTextAlign(@Nullable String value) {
        return value != null && TEXT_ALIGN.contains(value.toLowerCase(ROOT));
    }

    /**
     * @return true if the value is a keyword of the CSS property vertical-align
     */
    static boolean isVerticalAlign(@Nullable String value) {
        return value != null && VERTICAL_ALIGN.contains(value.toLowerCase(ROOT));
    }

    /**
     * Writes a URL as a quoted CSS <code>url("...")</code> value. Characters that could end the string are
     * written as CSS escapes, so the whole attribute value stays inside the URL.
     */
    static String cssUrl(String url) {
        StringBuilder result = new StringBuilder(url.length() + 8).append("url(\"");
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == '"' || c == '\\' || c < ' ' || c == 0x7f) {
                result.append('\\').append(Integer.toHexString(c)).append(' ');
            } else {
                result.append(c);
            }
        }
        return result.append("\")").toString();
    }
}
