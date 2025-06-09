/*
 * StyleReference.java
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.extend.AttributeResolver;
import org.openpdf.css.extend.lib.DOMTreeResolver;
import org.openpdf.css.newmatch.CascadedStyle;
import org.openpdf.css.newmatch.Matcher;
import org.openpdf.css.newmatch.PageInfo;
import org.openpdf.css.sheet.FontFaceRule;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.Stylesheet;
import org.openpdf.css.sheet.StylesheetInfo;
import org.openpdf.extend.NamespaceHandler;
import org.openpdf.extend.UserAgentCallback;
import org.openpdf.extend.UserInterface;
import org.openpdf.layout.SharedContext;
import org.openpdf.util.XRLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


/**
 * @author Torbjoern Gannholm
 */
public class StyleReference {
    @Nullable
    private NamespaceHandler _nsh;
    @Nullable
    private Document _doc;
    private final StylesheetFactoryImpl _stylesheetFactory;

    /**
     * Instance of our element-styles matching class. Will be null if new rules
     * have been added since last match.
     */
    @Nullable
    private Matcher _matcher;

    private UserAgentCallback _uac;

    public StyleReference(UserAgentCallback userAgent) {
        _uac = userAgent;
        _stylesheetFactory = new StylesheetFactoryImpl(userAgent);
    }

    /**
     * Sets the documentContext attribute of the StyleReference object
     *
     * @param context The Context this StyleReference operates in; used for property resolution.
     * @param nsh     The new documentContext value
     * @param doc     The new documentContext value
     * @param ui      The new documentContext value
     */
    public void setDocumentContext(SharedContext context, NamespaceHandler nsh, Document doc, UserInterface ui) {
        _nsh = nsh;
        _doc = doc;
        AttributeResolver attRes = new StandardAttributeResolver(_nsh, _uac, ui);

        List<StylesheetInfo> infos = getStylesheets();
        XRLog.match("media = " + context.getMedia());
        _matcher = new Matcher(
                new DOMTreeResolver(),
                attRes,
                _stylesheetFactory,
                readAndParseAll(infos, context.getMedia()),
                context.getMedia());
    }

    private List<Stylesheet> readAndParseAll(List<StylesheetInfo> infos, String medium) {
        List<Stylesheet> result = new ArrayList<>(infos.size() + 15);
        for (StylesheetInfo info : infos) {
            if (info.appliesToMedia(medium)) {
                Stylesheet sheet = _stylesheetFactory.getStylesheet(info);

                if (sheet != null) {
                    if (!sheet.getImportRules().isEmpty()) {
                        result.addAll(readAndParseAll(sheet.getImportRules(), medium));
                    }

                    result.add(sheet);
                } else {
                    XRLog.load(Level.WARNING, "Unable to load CSS from " + info.getUri());
                }
            }
        }

        return result;
    }

    public boolean isHoverStyled(Element e) {
        return _matcher.isHoverStyled(e);
    }

    /**
     * Returns a Map keyed by CSS property names (e.g. 'border-width'), and the
     * assigned value as a SAC CSSValue instance. The properties should have
     * been matched to the element when the Context was established for this
     * StyleReference on the Document to which the Element belongs. See {@link
     * org.openpdf.swing.BasicPanel#setDocument(Document, String)}
     * for an example of how to establish a StyleReference and associate to a
     * Document.
     *
     * @param e The DOM Element for which to find properties
     * @return Map of CSS property names to CSSValue instance assigned to it.
     */
    public Map<String, CSSPrimitiveValue> getCascadedPropertiesMap(Element e) {
        CascadedStyle cs = _matcher.getCascadedStyle(e, false);//this is only for debug, I think
        Map<String, CSSPrimitiveValue> props = new LinkedHashMap<>();
        for (Iterator<PropertyDeclaration> i = cs.getCascadedPropertyDeclarations(); i.hasNext();) {
            PropertyDeclaration pd = i.next();

            String propName = pd.getPropertyName();
            CSSName cssName = CSSName.getByPropertyName(propName);
            props.put(propName, cs.propertyByName(cssName).getValue());
        }
        return props;
    }

    /**
     * Gets the pseudoElementStyle attribute of the StyleReference object
     */
    @Nullable
    public CascadedStyle getPseudoElementStyle(Node node, String pseudoElement) {
        Element e;
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            e = (Element) node;
        } else {
            e = (Element) node.getParentNode();
        }
        return _matcher.getPECascadedStyle(e, pseudoElement);
    }

    /**
     * Gets the CascadedStyle for an element. This must then be converted in the
     * current context to a CalculatedStyle (use getDerivedStyle)
     */
    public CascadedStyle getCascadedStyle(@Nullable Element e, boolean restyle) {
        if (e == null) return CascadedStyle.emptyCascadedStyle;
        return _matcher.getCascadedStyle(e, restyle);
    }

    @NonNull
    @CheckReturnValue
    public PageInfo getPageStyle(@Nullable String pageName, String pseudoPage) {
        return _matcher.getPageCascadedStyle(pageName, pseudoPage);
    }

    /**
     * Flushes any stylesheet associated with this style reference (based on the user agent callback) that are in cache.
     */
    public void flushStyleSheets() {
        String uri = _uac.getBaseURL();
        if (_stylesheetFactory.containsStylesheet(uri)) {
            _stylesheetFactory.removeCachedStylesheet(uri);
            XRLog.cssParse("Removing stylesheet '" + uri + "' from cache by request.");
        } else {
            XRLog.cssParse("Requested removing stylesheet '" + uri + "', but it's not in cache.");
        }
    }

    public void flushAllStyleSheets() {
        _stylesheetFactory.flushCachedStylesheets();
    }

    /**
     * Gets StylesheetInfos for all stylesheets and inline styles associated
     * with the current document. Default (user agent) stylesheet and the inline
     * style for the current media are loaded and cached in the
     * StyleSheetFactory by URI.
     *
     * @return The stylesheets value
     */
    private List<StylesheetInfo> getStylesheets() {
        List<StylesheetInfo> infos = new ArrayList<>();
        long st = System.currentTimeMillis();

        _nsh.getDefaultStylesheet().ifPresent(defaultStylesheet -> infos.add(defaultStylesheet));

        infos.addAll(_nsh.getStylesheets(_doc));

        // TODO: here we should also get user stylesheet from userAgent

        XRLog.load("TIME: parse stylesheets in " + (System.currentTimeMillis() - st) + " ms.");
        return infos;
    }

    public List<FontFaceRule> getFontFaceRules() {
        return _matcher.getFontFaceRules();
    }

    public void setUserAgentCallback(UserAgentCallback userAgentCallback) {
        _uac = userAgentCallback;
        _stylesheetFactory.setUserAgentCallback(userAgentCallback);
    }

    public void setSupportCMYKColors(boolean b) {
        _stylesheetFactory.setSupportCMYKColors(b);
    }
}

