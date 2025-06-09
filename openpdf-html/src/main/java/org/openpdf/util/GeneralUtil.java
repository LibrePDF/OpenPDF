/*
 * {{{ header & license
 * GeneralUtil.java
 * Copyright (c) 2004, 2005 Patrick Wright
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
package org.openpdf.util;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static java.util.Locale.ROOT;
import static java.util.Locale.US;

/**
 * @author Patrick Wright
 */
public class GeneralUtil {
    @CheckReturnValue
    public static boolean ciEquals(final @Nullable String a, final @Nullable String b) {
        return a == null && b == null ||
            a != null && b != null && a.toLowerCase(US).equals(b.toLowerCase(US));
    }

    @Nullable
    @CheckReturnValue
    @SuppressWarnings("resource")
    public static InputStream openStreamFromClasspath(Object obj, String resource) {
        try {
            ClassLoader loader = obj.getClass().getClassLoader();
            InputStream stream = loader == null ?
                ClassLoader.getSystemResourceAsStream(resource) :
                loader.getResourceAsStream(resource);

            return stream != null ? stream : openResourceAsStream(resource);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Could not open stream from CLASSPATH: " + resource, ex);
        }
    }

    @Nullable
    private static InputStream openResourceAsStream(String resource) throws IOException {
        URL stream = resource.getClass().getResource(resource);
        return stream == null ? null : stream.openStream();
    }

    @Nullable
    @CheckReturnValue
    public static URL getURLFromClasspath(Object obj, String resource) {
        URL url = null;
        try {
            ClassLoader loader = obj.getClass().getClassLoader();
            if (loader == null) {
                url = ClassLoader.getSystemResource(resource);
            } else {
                url = loader.getResource(resource);
            }
            if (url == null) {
                url = resource.getClass().getResource(resource);
            }
        } catch (Exception ex) {
            XRLog.exception("Could not get URL from CLASSPATH: " + resource, ex);
        }
        return url;
    }

    /**
     * Dumps an exception to the console, only the last 5 lines of the stack
     * trace.
     */
    public static void dumpShortException(Exception ex) {
        String s = ex.getMessage();
        if (s == null || s.trim().equals("null")) {
            s = "{no ex. message}";
        }
        System.out.println(s + ", " + ex.getClass());
        StackTraceElement[] stackTrace = ex.getStackTrace();
        for (int i = 0; i < stackTrace.length && i < 5; i++) {
            StackTraceElement ste = stackTrace[i];
            System.out.println("  " + ste.getClassName() + "." + ste.getMethodName() + "(ln " + ste.getLineNumber() + ")");
        }
    }

    @CheckReturnValue
    public static boolean isMacOSX() {
        try {
            if (System.getProperty("os.name").toLowerCase(ROOT).startsWith("mac os x")) {
                return true;
            }
        } catch (SecurityException e) {
            System.err.println(e.getLocalizedMessage());
        }
        return false;
    }

    public static StringBuilder htmlEscapeSpace(String uri) {
        StringBuilder sbURI = new StringBuilder((int) (uri.length() * 1.5));
        char ch;
        for (int i = 0; i < uri.length(); ++i) {
            ch = uri.charAt(i);
            if (ch == ' ') {
                sbURI.append("%20");
            } else if (ch == '\\') {
                sbURI.append('/');
            } else {
                sbURI.append(ch);
            }
        }
        return sbURI;
    }

    /**
     * Parses an integer from a string using less restrictive rules about which
     * characters we won't accept.  This scavenges the supplied string for any
     * numeric character, while dropping all others.
     *
     * @param s The string to parse
     * @return The number represented by the passed string, or 0 if the string
     *         is null, empty, white-space only, contains only non-numeric
     *         characters, or simply evaluates to 0 after parsing (e.g. "0")
     */
    public static int parseIntRelaxed(String s) {
        // An edge-case short circuit...
        if (s.isEmpty() || s.trim().isEmpty()) {
            return 0;
        }

        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isDigit(c)) {
                buffer.append(c);
            } else {
                // If we hit a non-numeric with numbers already in the
                // buffer, we're done.
                if (!buffer.isEmpty()) {
                    break;
                }
            }
        }

        if (buffer.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(buffer.toString());
        } catch (NumberFormatException ignored) {
            // The only way we get here now is if s > Integer.MAX_VALUE
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Converts any special characters into their corresponding HTML entities , for example < to &lt;. This is done using a character
     * by character test, so you may consider other approaches for large documents. Make sure you declare the
     * entities that might appear in this replacement, e.g. the latin-1 entities
     * This method was taken from a code-samples website, written and hosted by Real Gagnon, at
     * <a href="http://www.rgagnon.com/javadetails/java-0306.html">...</a>.
     *
     * @param s The String which may contain characters to escape.
     * @return The string with the characters as HTML entities.
     */
    public static String escapeHTML(@Nullable String s){
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                // be careful with this one (non-breaking white space)
                case ' ':
                    sb.append("&nbsp;");
                    break;

                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
}
