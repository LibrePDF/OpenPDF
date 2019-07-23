/*
 * Copyright 2005 by Paulo Soares.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 * 
 * Contributions by:
 * Lubos Strapko
 * 
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.pdf.hyphenation;


import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.xml.simpleparser.SimpleXMLDocHandler;
import com.lowagie.text.xml.simpleparser.SimpleXMLParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/** Parses the xml hyphenation pattern.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class SimplePatternParser implements SimpleXMLDocHandler,
        PatternConsumer {
    int currElement;

    PatternConsumer consumer;

    StringBuffer token;

    List<Object> exception;

    char hyphenChar;

    SimpleXMLParser parser;

    static final int ELEM_CLASSES = 1;

    static final int ELEM_EXCEPTIONS = 2;

    static final int ELEM_PATTERNS = 3;

    static final int ELEM_HYPHEN = 4;

    /** Creates a new instance of PatternParser2 */
    public SimplePatternParser() {
        token = new StringBuffer();
        hyphenChar = '-'; // default
    }

    public void parse(InputStream stream, PatternConsumer consumer) {
        this.consumer = consumer;
        try {
            SimpleXMLParser.parse(this, stream);
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        } finally {
            try {
                stream.close();
            } catch (Exception e) {
            }
        }
    }

    protected static String getPattern(String word) {
        StringBuilder pat = new StringBuilder();
        int len = word.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isDigit(word.charAt(i))) {
                pat.append(word.charAt(i));
            }
        }
        return pat.toString();
    }

    protected List<Object> normalizeException(List<Object> ex) {
        List<Object> res = new ArrayList<>();
        for (Object item : ex) {
            if (item instanceof String) {
                String str = (String) item;
                StringBuilder buf = new StringBuilder();
                for (int j = 0; j < str.length(); j++) {
                    char c = str.charAt(j);
                    if (c != hyphenChar) {
                        buf.append(c);
                    } else {
                        res.add(buf.toString());
                        buf.setLength(0);
                        char[] h = new char[1];
                        h[0] = hyphenChar;
                        // we use here hyphenChar which is not necessarily
                        // the one to be printed
                        res.add(new Hyphen(new String(h), null, null));
                    }
                }
                if (buf.length() > 0) {
                    res.add(buf.toString());
                }
            } else {
                res.add(item);
            }
        }
        return res;
    }

    protected String getExceptionWord(List<Object> ex) {
        StringBuilder res = new StringBuilder();
        for (Object item : ex) {
            if (item instanceof String) {
                res.append((String) item);
            } else {
                if (((Hyphen) item).noBreak != null) {
                    res.append(((Hyphen) item).noBreak);
                }
            }
        }
        return res.toString();
    }

    protected static String getInterletterValues(String pat) {
        StringBuilder il = new StringBuilder();
        String word = pat + "a"; // add dummy letter to serve as sentinel
        int len = word.length();
        for (int i = 0; i < len; i++) {
            char c = word.charAt(i);
            if (Character.isDigit(c)) {
                il.append(c);
                i++;
            } else {
                il.append('0');
            }
        }
        return il.toString();
    }

    public void endDocument() {
    }

    public void endElement(String tag) {
        if (token.length() > 0) {
            String word = token.toString();
            switch (currElement) {
            case ELEM_CLASSES:
                consumer.addClass(word);
                break;
            case ELEM_EXCEPTIONS:
                exception.add(word);
                exception = normalizeException(exception);
                consumer.addException(getExceptionWord(exception),
                        (ArrayList) ((ArrayList) exception).clone());
                break;
            case ELEM_PATTERNS:
                consumer.addPattern(getPattern(word),
                        getInterletterValues(word));
                break;
            case ELEM_HYPHEN:
                // nothing to do
                break;
            }
            if (currElement != ELEM_HYPHEN) {
                token.setLength(0);
            }
        }
        if (currElement == ELEM_HYPHEN) {
            currElement = ELEM_EXCEPTIONS;
        } else {
            currElement = 0;
        }
    }

    @Override
    public void startDocument() {
    }

    /**
     * @deprecated use {@link SimplePatternParser#startElement(String, Map)}
     */
    @Override
    @Deprecated
    @SuppressWarnings("unchecked")
    public void startElement(String tag, HashMap h) {
        startElement(tag, ((Map<String, String>) h));
    }

    @Override
    public void startElement(String tag, Map<String, String> h) {
        switch (tag) {
            case "hyphen-char":
                String hh = h.get("value");
                if (hh != null && hh.length() == 1) {
                    hyphenChar = hh.charAt(0);
                }
                break;
            case "classes":
                currElement = ELEM_CLASSES;
                break;
            case "patterns":
                currElement = ELEM_PATTERNS;
                break;
            case "exceptions":
                currElement = ELEM_EXCEPTIONS;
                exception = new ArrayList<>();
                break;
            case "hyphen":
                if (token.length() > 0) {
                    exception.add(token.toString());
                }
                exception.add(new Hyphen(h.get("pre"), h.get("no"), h.get("post")));
                currElement = ELEM_HYPHEN;
                break;
        }
        token.setLength(0);
    }

    public void text(String str) {
        StringTokenizer tk = new StringTokenizer(str);
        while (tk.hasMoreTokens()) {
            String word = tk.nextToken();
            // System.out.println("\"" + word + "\"");
            switch (currElement) {
            case ELEM_CLASSES:
                consumer.addClass(word);
                break;
            case ELEM_EXCEPTIONS:
                exception.add(word);
                exception = normalizeException(exception);
                consumer.addException(getExceptionWord(exception),
                        (ArrayList)((ArrayList) exception).clone());
                exception.clear();
                break;
            case ELEM_PATTERNS:
                consumer.addPattern(getPattern(word),
                        getInterletterValues(word));
                break;
            }
        }
    }

    // PatternConsumer implementation for testing purposes
    public void addClass(String c) {
        System.out.println("class: " + c);
    }

    public void addException(String w, ArrayList e) {
        System.out.println("exception: " + w + " : " + e.toString());
    }

    public void addPattern(String p, String v) {
        System.out.println("pattern: " + p + " : " + v);
    }

    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                SimplePatternParser pp = new SimplePatternParser();
                pp.parse(new FileInputStream(args[0]), pp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
