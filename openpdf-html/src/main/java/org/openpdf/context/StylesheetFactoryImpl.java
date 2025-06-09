/*
 * StylesheetFactoryImpl.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
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
 *
 */
package org.openpdf.context;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.css.extend.StylesheetFactory;
import org.openpdf.css.parser.CSSParser;
import org.openpdf.css.sheet.Ruleset;
import org.openpdf.css.sheet.Stylesheet;
import org.openpdf.css.sheet.StylesheetInfo;
import org.openpdf.css.sheet.StylesheetInfo.Origin;
import org.openpdf.extend.UserAgentCallback;
import org.openpdf.resource.CSSResource;
import org.openpdf.util.Configuration;
import org.openpdf.util.XRLog;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.synchronizedMap;

/**
 * A Factory class for Cascading Style Sheets. Sheets are parsed using a single
 * parser instance for all sheets. Sheets are cached by URI using LRU test,
 * but timestamp of file is not checked.
 *
 * @author Torbjoern Gannholm
 */
public class StylesheetFactoryImpl implements StylesheetFactory {
    /**
     * the UserAgentCallback to resolve uris
     */
    private UserAgentCallback _userAgentCallback;

    /**
     * an LRU cache
     */
    private final Map<String, Stylesheet> _cache = synchronizedMap(new StylesheetCache());
    private final CSSParser _cssParser;

    public StylesheetFactoryImpl(UserAgentCallback userAgentCallback) {
        _userAgentCallback = userAgentCallback;
        _cssParser = new CSSParser((uri, message) -> XRLog.cssParse(Level.WARNING, "(" + uri + ") " + message));
    }

    @Override
    public Stylesheet parse(Reader reader, StylesheetInfo info) {
        return parse(reader, info.getUri(), info.getOrigin());
    }

    @Override
    public Stylesheet parse(Reader reader, String uri, Origin origin) {
        try {
            return _cssParser.parseStylesheet(uri, origin, reader);
        } catch (IOException e) {
            XRLog.cssParse(Level.WARNING, "Couldn't parse stylesheet at URI " + uri + ": " + e.getMessage(), e);
            return new Stylesheet(uri, origin);
        }
    }

    /**
     * @return Returns null if uri could not be loaded
     */
    @Nullable
    private Stylesheet parse(StylesheetInfo info) {
        CSSResource cr = info.getContent()
                .map(css -> new CSSResource(new ByteArrayInputStream(css.getBytes(UTF_8))))
                .orElseGet(() -> _userAgentCallback.getCSSResource(info.getUri()));

        // Whether by accident or design, InputStream will never be null
        // since the null resource stream is wrapped in a BufferedInputStream
        InputSource inputSource=cr.getResourceInputSource();
        if (inputSource==null) return null;
        try (InputStream is = inputSource.getByteStream()) {
            if (is == null) return null;
            String charset = Configuration.valueFor("xr.stylesheets.charset-name", "UTF-8");
            return parse(new InputStreamReader(is, charset), info);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Ruleset parseStyleDeclaration(Origin origin, String styleDeclaration) {
        return _cssParser.parseDeclaration(origin, styleDeclaration);
    }

    /**
     * Adds a stylesheet to the factory cache. Will overwrite older entry for
     * same key.
     *
     * @param key   Key to use to reference sheet later; must be unique in
     *              factory.
     * @param sheet The sheet to cache.
     */
    public void putStylesheet(String key, Stylesheet sheet) {
        _cache.put(key, sheet);
    }

    /**
     * @return true if a Stylesheet with this key has been put in the cache.
     *         Note that the Stylesheet may be null.
     */
    //TODO: work out how to handle caching properly, with cache invalidation
    public boolean containsStylesheet(String key) {
        return _cache.containsKey(key);
    }

    /**
     * Removes a cached sheet by its key.
     *
     * @param key The key for this sheet; same as key passed to
     *            putStylesheet();
     */
    public void removeCachedStylesheet(String key) {
        _cache.remove(key);
    }

    void flushCachedStylesheets() {
        _cache.clear();
    }

    /**
     * Returns a cached sheet by its key; loads and caches it if not in cache;
     * null if not able to load
     *
     * @param info The StylesheetInfo for this sheet
     * @return The stylesheet
     */
    //TODO: this looks a bit odd
    @Nullable
    @CheckReturnValue
    @Override
    public Stylesheet getStylesheet(StylesheetInfo info) {
        XRLog.load("Requesting stylesheet: " + info.getUri());

        Stylesheet s = _cache.get(info.getUri());
        if (s == null && !containsStylesheet(info.getUri())) {
            s = parse(info);
            putStylesheet(info.getUri(), s);
        }
        return s;
    }

    void setUserAgentCallback(UserAgentCallback userAgent) {
        _userAgentCallback = userAgent;
    }

    void setSupportCMYKColors(boolean b) {
        _cssParser.setSupportCMYKColors(b);
    }
}
