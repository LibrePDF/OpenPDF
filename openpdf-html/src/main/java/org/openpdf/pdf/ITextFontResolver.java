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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.pdf;

import org.openpdf.text.DocumentException;
import org.openpdf.text.pdf.BaseFont;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.FSFunction;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.FontFaceRule;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.FSDerivedValue;
import org.openpdf.css.style.derived.ListValue;
import org.openpdf.css.value.FontSpecification;
import org.openpdf.extend.FontResolver;
import org.openpdf.extend.UserAgentCallback;
import org.openpdf.layout.SharedContext;
import org.openpdf.render.FSFont;
import org.openpdf.util.IOUtil;
import org.openpdf.util.SupportedEmbeddedFontTypes;
import org.openpdf.util.XRLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Locale.ROOT;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;
import static org.openpdf.pdf.TrueTypeUtil.extractDescription;
import static org.openpdf.util.FontUtil.isEmbeddedBase64Font;
import static org.openpdf.util.SupportedEmbeddedFontTypes.getExtension;

public class ITextFontResolver implements FontResolver {
    private static final Logger log = LoggerFactory.getLogger(ITextFontResolver.class);
    private static final String OTF = ".otf";
    private static final String TTF = ".ttf";
    private static final String AFM = ".afm";
    private static final String PFM = ".pfm";
    private static final String PFB = ".pfb";
    private static final String PFA = ".pfa";
    private static final String TTC = ".ttc";
    private static final String TTC_COMMA = ".ttc,";

    private final Map<String, String> _embedFontFaces = new HashMap<>();
    private final Map<String, FontFamily> _fontFamilies = new HashMap<>();
    private final Map<String, FontDescription> _fontCache = new ConcurrentHashMap<>();

    public Map<String, FontFamily> getFonts() {
        if (_fontFamilies.isEmpty()) {
            synchronized (_fontFamilies) {
                if (_fontFamilies.isEmpty()) {
                    _fontFamilies.putAll(loadFonts());
                }
            }
        }
        return _fontFamilies;
    }

    /**
     * Utility method which uses iText libraries to determine the family name(s) for the font at the given path.
     * The iText APIs seem to indicate there can be more than one name, but this method will return a set of them.
     * Use a name from this list when referencing the font in CSS for PDF output. Note that family names as reported
     * by iText may vary from those reported by the AWT Font class, e.g. "Arial Unicode MS" for iText and
     * "ArialUnicodeMS" for AWT.
     *
     * @param path local path to the font file
     * @param encoding same as what you would use for {@link #addFont(String, String, boolean)}
     * @param embedded same as what you would use for {@link #addFont(String, String, boolean)}
     * @return set of all family names for the font file, as reported by iText libraries
     */
    public static Set<String> getDistinctFontFamilyNames(String path, String encoding, boolean embedded) {
        try {
            BaseFont font = BaseFont.createFont(path, encoding, embedded);
            Collection<String> fontFamilyNames = TrueTypeUtil.getFamilyNames(font);
            return new HashSet<>(fontFamilyNames);
        } catch (DocumentException | IOException e) {
            throw new RuntimeException(
                    "Failed to read font family names from %s (encoding: %s, embedded: %s)".formatted(path, encoding, embedded), e);
        }
    }

    @Nullable
    @Override
    public FSFont resolveFont(SharedContext renderingContext, FontSpecification spec) {
        return resolveFont(spec.families, spec.size, spec.fontWeight, spec.fontStyle);
    }

    @Override
    public void flushCache() {
        synchronized (_fontFamilies) {
            _fontFamilies.clear();
        }
        _fontCache.clear();
    }

    public void flushFontFaceFonts() {
        _fontCache.clear();

        for (Iterator<FontFamily> i = getFonts().values().iterator(); i.hasNext(); ) {
            FontFamily family = i.next();
            family.getFontDescriptions().removeIf(FontDescription::isFromFontFace);
            if (family.getFontDescriptions().isEmpty()) {
                i.remove();
            }
        }
    }

    public void addEmbedFontFace(String fontFamily, String encoding) {
        _embedFontFaces.put(fontFamily, encoding);
    }

    public void resetEmbedFontFace() {
        _embedFontFaces.clear();
    }

    public void importFontFaces(List<FontFaceRule> fontFaces, UserAgentCallback userAgentCallback) {
        for (FontFaceRule rule : fontFaces) {
            importFontFace(rule, userAgentCallback);
        }
    }

    private void importFontFace(FontFaceRule rule, UserAgentCallback userAgentCallback) {
        CalculatedStyle style = rule.getCalculatedStyle();

        FSDerivedValue src = style.valueByName(CSSName.SRC);
        if (src == IdentValue.NONE) {
            return;
        }

        List<FontSrc> fontSources = parseFontSources(src);
        if (fontSources.isEmpty()) {
            XRLog.exception("No valid font sources found in src property");
            return;
        }

        // Try each font source in order until one works
        for (FontSrc fontSrc : fontSources) {
            String uri = fontSrc.uri;
            String format = fontSrc.format;

            // Check if format is supported
            if (!fontSupported(uri, format)) {
                continue;
            }
            byte[] font1 = userAgentCallback.getBinaryResource(uri);
            if (font1 == null) {
                XRLog.exception("Could not load font " + uri);
                continue;
            }

            byte[] font2 = null;
            FSDerivedValue metricsSrc = style.valueByName(CSSName.FS_FONT_METRIC_SRC);
            if (metricsSrc != IdentValue.NONE) {
                font2 = userAgentCallback.getBinaryResource(metricsSrc.asString());
                if (font2 == null) {
                    XRLog.exception("Could not load font metric data " + uri);
                    continue;
                }
            }

            if (font2 != null) {
                byte[] t = font1;
                font1 = font2;
                font2 = t;
            }

            boolean embedded = style.isIdent(CSSName.FS_PDF_FONT_EMBED, IdentValue.EMBED);
            String encoding = style.getStringProperty(CSSName.FS_PDF_FONT_ENCODING);
            String fontFamily = rule.hasFontFamily() ? style.valueByName(CSSName.FONT_FAMILY).asString() : null;
            if (_embedFontFaces.containsKey(fontFamily)) {
                embedded = true;
                encoding = _embedFontFaces.get(fontFamily);
            }
            IdentValue fontWeight = rule.hasFontWeight() ? style.getIdent(CSSName.FONT_WEIGHT) : null;
            IdentValue fontStyle = rule.hasFontStyle() ? style.getIdent(CSSName.FONT_STYLE) : null;

            try {
                addFontFaceFont(fontFamily, fontWeight, fontStyle, uri, format, encoding, embedded, font1, font2);
                // Successfully added font, no need to try other sources
                return;
            } catch (DocumentException | IOException e) {
                XRLog.exception("Could not load font " + uri, e);
                // Continue to try next font source
            }
        }

        XRLog.exception("Failed to load any font from src property");
    }

    /**
     * Represents a font source with URI and optional format.
     */
    private static class FontSrc {
        final String uri;
        @Nullable final String format;

        FontSrc(String uri, @Nullable String format) {
            this.uri = uri;
            this.format = format;
        }
    }

    /**
     * Parse font sources from the src property value.
     * Handles both single values and comma-separated lists of url()/format() pairs.
     */
    private List<FontSrc> parseFontSources(FSDerivedValue src) {
        List<FontSrc> result = new ArrayList<>();

        // Check if it's a list value (multiple sources)
        if (src instanceof ListValue) {
            ListValue listValue = (ListValue) src;
            List<Object> values = listValue.getValues();
            if (values != null) {
                String currentUri = null;
                String currentFormat = null;

                for (Object value : values) {
                    if (value instanceof PropertyValue) {
                        PropertyValue propValue = (PropertyValue) value;

                        if (propValue.getPrimitiveType() == CSSPrimitiveValue.CSS_URI) {
                            // If we have a pending URI, add it before starting a new one
                            if (currentUri != null) {
                                result.add(new FontSrc(currentUri, currentFormat));
                                currentFormat = null;
                            }
                            currentUri = propValue.getStringValue();
                        } else if (propValue.getPropertyValueType() == PropertyValue.Type.VALUE_TYPE_FUNCTION) {
                            FSFunction function = propValue.getFunction();
                            if (function != null && "format".equals(function.getName())) {
                                List<PropertyValue> params = function.getParameters();
                                if (!params.isEmpty() && params.get(0).getStringValue() != null) {
                                    currentFormat = params.get(0).getStringValue();
                                }
                            }
                        }
                    }
                }

                // Add the last URI if present
                if (currentUri != null) {
                    result.add(new FontSrc(currentUri, currentFormat));
                }
            }
        } else {
            // Single value (just a URI)
            result.add(new FontSrc(src.asString(), null));
        }

        return result;
    }

    /**
     * Add all fonts from given directory with encoding "CP1252" (don't ask me why :) )
     */
    public void addFontDirectory(String dir, boolean embedded) throws DocumentException, IOException {
        addFontDirectory(dir, BaseFont.CP1252, embedded);
    }

    /**
     * Add all fonts from given directory (all files with extension ".otf" and ".ttf")
     */
    public void addFontDirectory(String dir, String encoding, boolean embedded) throws DocumentException, IOException {
        File f = new File(dir);
        if (!f.isDirectory()) {
            throw new IllegalArgumentException("%s is not a directory".formatted(dir));
        }
        for (File file : filesWithExtensions(f, OTF, TTF)) {
            addFont(file.getAbsolutePath(), encoding, embedded);
        }
    }

    private File[] filesWithExtensions(File f, String... extensions) {
        return requireNonNull(f.listFiles((d, name) -> {
            String lower = name.toLowerCase(ROOT);
            return Stream.of(extensions).anyMatch(extension -> lower.endsWith(extension));
        }));
    }

    /**
     * Add the font with encoding "CP1252" (don't ask me why :) )
     */
    public void addFont(String path, boolean embedded)
            throws DocumentException, IOException {
        addFont(path, BaseFont.CP1252, embedded);
    }

    public void addFont(String path, String encoding, boolean embedded)
            throws DocumentException, IOException {
        addFont(path, encoding, embedded, null);
    }

    public void addFont(String path, String encoding, boolean embedded, @Nullable String pathToPFB)
            throws DocumentException, IOException {
        addFont(path, null, encoding, embedded, pathToPFB);
    }

    public void addFont(String path, @Nullable String fontFamilyNameOverride,
                        String encoding, boolean embedded, @Nullable String pathToPFB)
            throws DocumentException, IOException {
        String lower = path.toLowerCase(ROOT);
        if (lower.endsWith(OTF) || lower.endsWith(TTF) || lower.contains(TTC_COMMA)) {
            BaseFont font = BaseFont.createFont(path, encoding, embedded);
            addFont(font, path, fontFamilyNameOverride);
        } else if (lower.endsWith(TTC)) {
            String[] names = BaseFont.enumerateTTCNames(path);
            for (int i = 0; i < names.length; i++) {
                addFont(path + "," + i, fontFamilyNameOverride, encoding, embedded, null);
            }
        } else if (lower.endsWith(AFM) || lower.endsWith(PFM)) {
            if (embedded && pathToPFB == null) {
                throw new IOException("When embedding a font, path to PFB/PFA file must be specified (path: %s)".formatted(path));
            }

            BaseFont font = BaseFont.createFont(
                    path, encoding, embedded, false, null, readFile(pathToPFB));

            String fontFamilyName = requireNonNullElseGet(fontFamilyNameOverride, () -> font.getFamilyFontName()[0][3]);
            FontFamily fontFamily = getFontFamily(fontFamilyName);

            FontDescription description = new FontDescription(font);
            // XXX Need to set weight, underline position, etc.  This information
            // is contained in the AFM file (and even parsed by Type1Font), but
            // unfortunately it isn't exposed to the caller.
            fontFamily.addFontDescription(description);
        } else {
            throw new IOException("Unsupported font type: %s".formatted(path));
        }
    }

    public void addFont(BaseFont font, String path, @Nullable String fontFamilyNameOverride) {
        Collection<String> fontFamilyNames = getFontFamilyNames(font, fontFamilyNameOverride);

        for (String fontFamilyName : fontFamilyNames) {
            getFontFamily(fontFamilyName)
                    .addFontDescription(extractDescription(path, font, null));
        }
    }

    private static Collection<String> getFontFamilyNames(BaseFont font, @Nullable String fontFamilyNameOverride) {
        if (fontFamilyNameOverride != null) {
            return singletonList(fontFamilyNameOverride);
        } else {
            return TrueTypeUtil.getFamilyNames(font);
        }
    }

    private boolean fontSupported(String uri, @Nullable String format) {
        if (format != null) {
            return format.equals("opentype") || format.equals("truetype");
        }
        String lower = uri.toLowerCase(ROOT);
        if (isEmbeddedBase64Font(uri)) {
            return SupportedEmbeddedFontTypes.isSupported(uri);
        } else {
            return lower.endsWith(OTF) || lower.endsWith(TTF);
        }
    }

    private String getFontName(String uri, @Nullable String format, @Nullable String fontFamilyName) {
        if (fontFamilyName != null) {
            if (isEmbeddedBase64Font(uri)) {
                return fontFamilyName + getExtension(uri);
            } else if (format != null) {
                String lower = uri.toLowerCase();
                if (!lower.endsWith(OTF) && !lower.endsWith(TTF)) {
                    String ext = "";
                    if (format.equals("opentype")) {
                        ext = OTF;
                    } else if (format.equals("truetype")) {
                        ext = TTF;
                    }
                    return fontFamilyName + ext;
                }
            }
        }
        return uri;
    }

    /**
     * @param ttfAfm the font as a byte array, possibly null
     */
    private void addFontFaceFont(@Nullable String fontFamilyNameOverride, @Nullable IdentValue fontWeightOverride,
                                 @Nullable IdentValue fontStyleOverride, String uri, @Nullable String format,
                                 String encoding, boolean embedded, byte[] ttfAfm, byte @Nullable [] pfb)
            throws DocumentException, IOException {
        String fontName = getFontName(uri, format, fontFamilyNameOverride);
        BaseFont font = BaseFont.createFont(fontName, encoding, embedded, false, ttfAfm, pfb);

        Collection<String> fontFamilyNames = getFontFamilyNames(font, fontFamilyNameOverride);

        for (String fontFamilyName : fontFamilyNames) {
            FontFamily fontFamily = getFontFamily(fontFamilyName);
            fontFamily.addFontDescription(
                    fontDescription(fontWeightOverride, fontStyleOverride, uri, ttfAfm, font)
            );
        }
    }

    private static FontDescription fontDescription(@Nullable IdentValue fontWeightOverride, @Nullable IdentValue fontStyleOverride,
                                                   String uri, byte[] ttfAfm, BaseFont font) {
        return extractDescription(uri, ttfAfm, font, true, fontWeightOverride, fontStyleOverride);
    }

    private byte[] readFile(String path) throws IOException {
        return IOUtil.readBytes(Paths.get(path));
    }

    private FontFamily getFontFamily(String fontFamilyName) {
        FontFamily fontFamily = getFonts().get(fontFamilyName);
        if (fontFamily == null) {
            fontFamily = new FontFamily(fontFamilyName);
            getFonts().put(fontFamilyName, fontFamily);
        }
        return fontFamily;
    }

    @Nullable
    private FSFont resolveFont(String @Nullable [] families, float size, IdentValue weight, IdentValue style) {
        if (!(style == IdentValue.NORMAL || style == IdentValue.OBLIQUE
                || style == IdentValue.ITALIC)) {
            style = IdentValue.NORMAL;
        }
        if (families != null) {
            for (String family : families) {
                FSFont font = resolveFont(family, size, weight, style);
                if (font != null) {
                    log.debug("Resolved font {}:{}:{} -> {}", family, weight, style, font);
                    return font;
                }
            }
        }

        log.debug("Could not resolve font {}:{}:{} - fallback to Serif", Arrays.toString(families), weight, style);
        return resolveFont("Serif", size, weight, style);
    }

    String normalizeFontFamily(String fontFamily) {
        String result = stripQuotes(fontFamily);

        if (result.equalsIgnoreCase("serif")) {
            return "Serif";
        }
        else if (result.equalsIgnoreCase("sans-serif")) {
            return "SansSerif";
        }
        else if (result.equalsIgnoreCase("monospace")) {
            return "Monospaced";
        }

        return result;
    }

    /**
     * strip off the leading and trailing quote if they are there
     */
    private String stripQuotes(String text) {
        String result = text;
        if (result.startsWith("\"")) {
            result = result.substring(1);
        }
        if (result.endsWith("\"")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    @Nullable
    private FSFont resolveFont(String fontFamily, float size, IdentValue weight, IdentValue style) {
        String normalizedFontFamily = normalizeFontFamily(fontFamily);

        String cacheKey = String.format("%s-%s-%s", normalizedFontFamily, weight, style);
        FontDescription result = _fontCache.get(cacheKey);

        if (result != null) {
            log.debug("Resolved font {}:{}:{} -> {}", fontFamily, weight, style, result);
            return new ITextFSFont(result, size);
        }

        FontFamily family = getFonts().get(normalizedFontFamily);
        if (family != null) {
            result = family.match(convertWeightToInt(weight), style);
            if (result != null) {
                _fontCache.put(cacheKey, result);
                return new ITextFSFont(result, size);
            }
        }

        return null;
    }

    public static int convertWeightToInt(IdentValue weight) {
        if (weight == IdentValue.NORMAL) {
            return 400;
        } else if (weight == IdentValue.BOLD) {
            return 700;
        } else if (weight == IdentValue.FONT_WEIGHT_100) {
            return 100;
        } else if (weight == IdentValue.FONT_WEIGHT_200) {
            return 200;
        } else if (weight == IdentValue.FONT_WEIGHT_300) {
            return 300;
        } else if (weight == IdentValue.FONT_WEIGHT_400) {
            return 400;
        } else if (weight == IdentValue.FONT_WEIGHT_500) {
            return 500;
        } else if (weight == IdentValue.FONT_WEIGHT_600) {
            return 600;
        } else if (weight == IdentValue.FONT_WEIGHT_700) {
            return 700;
        } else if (weight == IdentValue.FONT_WEIGHT_800) {
            return 800;
        } else if (weight == IdentValue.FONT_WEIGHT_900) {
            return 900;
        } else if (weight == IdentValue.LIGHTER) {
            // FIXME
            return 400;
        } else if (weight == IdentValue.BOLDER) {
            // FIXME
            return 700;
        }
        throw new IllegalArgumentException("Cannot convert weight to integer: " + weight);
    }

    protected Map<String, FontFamily> loadFonts() {
        Map<String, FontFamily> result = new HashMap<>();
        addCourier(result);
        addTimes(result);
        addHelvetica(result);
        addSymbol(result);
        addZapfDingbats(result);
        return result;
    }

    private BaseFont createFont(String name) {
        return createFont(name, "winansi", true);
    }

    private BaseFont createFont(String name, String encoding, boolean embedded) {
        try {
            return BaseFont.createFont(name, encoding, embedded);
        }
        catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to load font %s (encoding: %s, embedded: %s)".formatted(name, encoding, embedded), e);
        }
    }

    private void addCourier(Map<String, FontFamily> result) {
        FontFamily courier = new FontFamily("Courier");

        courier.addFontDescription(new FontDescription(
                createFont(BaseFont.COURIER_BOLDOBLIQUE), IdentValue.OBLIQUE, 700));
        courier.addFontDescription(new FontDescription(
                createFont(BaseFont.COURIER_OBLIQUE), IdentValue.OBLIQUE, 400));
        courier.addFontDescription(new FontDescription(
                createFont(BaseFont.COURIER_BOLD), IdentValue.NORMAL, 700));
        courier.addFontDescription(new FontDescription(
                createFont(BaseFont.COURIER), IdentValue.NORMAL, 400));

        result.put("DialogInput", courier);
        result.put("Monospaced", courier);
        result.put("Courier", courier);
    }

    private void addTimes(Map<String, FontFamily> result) {
        FontFamily times = new FontFamily("Times");

        times.addFontDescription(new FontDescription(
                createFont(BaseFont.TIMES_BOLDITALIC), IdentValue.ITALIC, 700));
        times.addFontDescription(new FontDescription(
                createFont(BaseFont.TIMES_ITALIC), IdentValue.ITALIC, 400));
        times.addFontDescription(new FontDescription(
                createFont(BaseFont.TIMES_BOLD), IdentValue.NORMAL, 700));
        times.addFontDescription(new FontDescription(
                createFont(BaseFont.TIMES_ROMAN), IdentValue.NORMAL, 400));

        result.put("Serif", times);
        result.put("TimesRoman", times);
    }

    private void addHelvetica(Map<String, FontFamily> result) {
        FontFamily helvetica = new FontFamily("Helvetica");

        helvetica.addFontDescription(new FontDescription(
                createFont(BaseFont.HELVETICA_BOLDOBLIQUE), IdentValue.OBLIQUE, 700));
        helvetica.addFontDescription(new FontDescription(
                createFont(BaseFont.HELVETICA_OBLIQUE), IdentValue.OBLIQUE, 400));
        helvetica.addFontDescription(new FontDescription(
                createFont(BaseFont.HELVETICA_BOLD), IdentValue.NORMAL, 700));
        helvetica.addFontDescription(new FontDescription(
                createFont(BaseFont.HELVETICA), IdentValue.NORMAL, 400));

        result.put("Dialog", helvetica);
        result.put("SansSerif", helvetica);
        result.put("Helvetica", helvetica);
    }

    private void addSymbol(Map<String, FontFamily> result) {
        FontFamily fontFamily = new FontFamily("Symbol");
        fontFamily.addFontDescription(new FontDescription(createFont(BaseFont.SYMBOL, BaseFont.CP1252, false), IdentValue.NORMAL, 400));
        result.put("Symbol", fontFamily);
    }

    private void addZapfDingbats(Map<String, FontFamily> result) {
        FontFamily fontFamily = new FontFamily("ZapfDingbats");
        fontFamily.addFontDescription(new FontDescription(createFont(BaseFont.ZAPFDINGBATS, BaseFont.CP1252, false), IdentValue.NORMAL, 400));
        result.put("ZapfDingbats", fontFamily);
    }

}
