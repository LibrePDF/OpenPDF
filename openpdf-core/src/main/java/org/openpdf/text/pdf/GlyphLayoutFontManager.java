/*
 * Copyright 2020-2026 Volker Kunert.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
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
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

package org.openpdf.text.pdf;

import java.awt.FontFormatException;
import java.awt.font.TextAttribute;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.error_messages.MessageLocalization;

/**
 * Provides font loading for GlyphLayoutManager
 */
public class GlyphLayoutFontManager {

    private final Map<BaseFont, java.awt.Font> awtFontMap = new HashMap<>();
    private FontOptions defaultFontOptions = new FontOptions();
    private FontCaching fontCaching = FontCaching.DEFAULT;

    /**
     * Creates a new GlyphLayoutFontManager
     */
    public GlyphLayoutFontManager() {
    }

    /**
     * Creates a bytee array from an input stream
     *
     * @param inputStream the input stream
     * @return a byte array
     * @throws IOException if input stream can not be read
     */
    protected static byte[] createByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int count;
        byte[] buffer = new byte[8192];
        while ((count = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, count);
        }
        byte[] fontBytes = outputStream.toByteArray();
        return fontBytes;
    }

    /**
     * Set font caching
     *
     * @param fontCaching Strategy for font caching: DEFAULT, ON or OFF
     * @return this GlyphLayoutFontManager
     */
    public GlyphLayoutFontManager setFontCaching(FontCaching fontCaching) {
        this.fontCaching = fontCaching;
        return this;
    }

    /**
     * Checks if the baseFont is loaded and supported
     *
     * @param baseFont BaseFont
     * @return true if the baseFont is supported
     * @throws UnsupportedOperationException if the font has not been loaded with GlyphLayoutManager.loadFont
     */
    public boolean supportsFont(BaseFont baseFont) {
        boolean isSupported = awtFontMap.get(baseFont) != null;
        if (!isSupported) {
            throw new UnsupportedOperationException(
                    "Font " + baseFont.getPostscriptFontName()
                            + " has not been loaded"
                            + " with GlyphLayoutManager.loadFont(...)");
        }
        return true;
    }

    /**
     * Returns, the AWT-Font for the given baseFont
     *
     * @param baseFont base font
     * @param fontSize size of the font
     * @return the AWT-Font for the given baseFont
     */
    public java.awt.Font getFont(BaseFont baseFont, float fontSize) {
        return awtFontMap.get(baseFont).deriveFont(fontSize);
    }

    /**
     * Loads OpenPdf font and AWT font from path with alias Sets textAttributes for AWT font
     *
     * @param inputStream inputStream or font
     * @param fontSize    font size
     * @return Loaded OpenPdf font
     */
    public org.openpdf.text.Font loadFont(String name, InputStream inputStream, float fontSize) {
        return loadFont(name, inputStream, fontSize, null);
    }

    /**
     * Loads OpenPdf font and AWT font from path with alias Sets textAttributes for AWT font
     *
     * @param inputStream inputStream or font
     * @param fontSize    font size
     * @param fontOptions Options for the AWT-font
     * @return Loaded OpenPdf font
     */
    public org.openpdf.text.Font loadFont(String name, InputStream inputStream, float fontSize,
            FontOptions fontOptions) {

        if (inputStream == null) {
            throw new NullPointerException("inputStream is null for " + name);
        }
        if (!(name.toLowerCase().endsWith(".ttf")
                || name.toLowerCase().endsWith(".otf")
                || name.toLowerCase().indexOf(".ttc,") > 0)) {
            throw new IllegalArgumentException("Name has to end with '.ttf', 'otf', or '.ttc,[number]'. name=" + name);
        }

        Font font = null;
        Map<TextAttribute, Object> textAttributes = getTextAttributes(fontOptions);
        boolean caching = isCaching(fontOptions);

        try {
            // See AdvanceTypographyTest
            // Load font from inputStream
            byte[] fontBytes = createByteArray(inputStream);
            BaseFont baseFont = BaseFont.createFont(name, BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED, caching, fontBytes, null, false, false);
            font = new Font(baseFont, fontSize);

            if (!(baseFont instanceof TrueTypeFontUnicode)) {
                throw new RuntimeException("Only OpenType/TrueTypeFonts are allowed. Font=" + font);
            }
            if (awtFontMap.get(baseFont) == null) {
                InputStream inputStream2 = new ByteArrayInputStream(fontBytes);
                loadAwtFontFromInputStream(textAttributes, inputStream2, baseFont);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading font " + name, e);
        }
        return font;
    }

    /**
     * Loads OpenPdf font and AWT font from the given path with alias
     *
     * @param path     Path of font file
     * @param fontSize font size
     * @return Loaded OpenPdf font
     */
    public org.openpdf.text.Font loadFont(String path, float fontSize) {
        return loadFont(path, fontSize, null);

    }

    /**
     * Loads OpenPdf font and AWT font from path with alias Sets textAttributes for AWT font
     *
     * @param path        Path of font file
     * @param fontSize    font size
     * @param fontOptions Options for the AWT-font
     * @return Loaded OpenPdf font
     */
    public org.openpdf.text.Font loadFont(String path, float fontSize, FontOptions fontOptions) {
        // cached has to be set to 'false', to allow different sizes and attributes for instances of one font
        Map<TextAttribute, Object> textAttributes = getTextAttributes(fontOptions);

        FontFactory.register(path, null);
        boolean caching = isCaching(fontOptions);
        org.openpdf.text.Font font = FontFactory.getFont(path, BaseFont.IDENTITY_H, true, fontSize, Font.UNDEFINED,
                null, caching);

        BaseFont baseFont = font.getBaseFont();
        if (!(baseFont instanceof TrueTypeFontUnicode)) {
            throw new RuntimeException("Only OpenType/TrueTypeFonts are allowed. Path=" + path);
        }
        loadAwtFont(font, path, textAttributes);
        return font;
    }

    protected boolean isCaching(FontOptions fontOptions) {
        // cached has to be set to 'false', to allow different text attributes for instances of one font
        boolean caching = switch (fontCaching) {
            case DEFAULT -> fontOptions == null && defaultFontOptions.getTextAttributes().isEmpty();
            case ON -> true;
            case OFF -> false;
        };
        return caching;
    }

    protected Map<TextAttribute, Object> getTextAttributes(FontOptions fontOptions) {
        Map<TextAttribute, Object> textAttributes = new HashMap<>(defaultFontOptions.getTextAttributes());
        if (fontOptions != null) {
            textAttributes.putAll(fontOptions.getTextAttributes());
        }
        return textAttributes;
    }

    /**
     * Set default font options for all AWT-fonts loaded by this class
     *
     * @param fontOptions font options
     * @return this GlyphLayoutFontManager
     */
    public GlyphLayoutFontManager setDefaultFontOptions(FontOptions fontOptions) {
        defaultFontOptions = fontOptions;
        return this;
    }

    protected InputStream getInputStream(String filename) throws IOException {
        InputStream inputStream = null;
        Exception exception = null;

        try {
            File file = new File(filename);
            if (!file.exists() && FontFactory.isRegistered(filename)) {
                String fontPath = (String) FontFactory.getFontImp().getFontPath(filename);
                file = new File(fontPath);
            }
            if (file.canRead()) {
                inputStream = Files.newInputStream(file.toPath());
            } else if (filename.startsWith("file:/")
                    || filename.startsWith("https://") || filename.startsWith("jar:")
                    || filename.startsWith("wsjar:")) {
                String encodedFilename = java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8);
                inputStream = new URI(encodedFilename).toURL().openStream();
            } else if ("-".equals(filename)) {
                inputStream = System.in;
            } else {
                inputStream = BaseFont.getResourceStream(filename);
            }
        } catch (Exception e) {
            exception = e;
        }
        if (inputStream == null) {
            throw new IOException(
                    MessageLocalization.getComposedMessage("1.not.found.as.file.or.resource", filename), exception);
        }
        return inputStream;
    }

    /**
     * Loads the AWT font needed for layout using given text attributes
     * <p>
     * If baseFont is not instanceof TrueTypeFontUnicode *no* font is loaded.
     *
     * @param font           OpenPdf  font
     * @param filename       of the font file
     * @param textAttributes test attributes of the font
     * @throws RuntimeException if font can not be loaded
     */
    protected void loadAwtFont(org.openpdf.text.Font font, String filename,
            Map<TextAttribute, Object> textAttributes) {

        BaseFont baseFont = font.getBaseFont();

        if (awtFontMap.get(baseFont) != null) {
            return;
        }
        if (!(baseFont instanceof TrueTypeFontUnicode)) {
            return;
        }
        java.awt.Font awtFont;
        InputStream inputStream = null;
        try {
            awtFont = awtFontMap.get(baseFont);
            if (awtFont == null) {
                // getting the inputStream is adapted from org.openpdf.text.pdf.RandomAccessFileOrArray
                inputStream = getInputStream(filename);
                loadAwtFontFromInputStream(textAttributes, inputStream, baseFont);
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Font creation failed for %s.", filename), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    private void loadAwtFontFromInputStream(Map<TextAttribute, Object> textAttributes, InputStream inputStream,
            BaseFont baseFont)
            throws FontFormatException, IOException {
        java.awt.Font awtFont;
        awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, inputStream);
        if (awtFont != null) {
            if (textAttributes != null && !textAttributes.isEmpty()) {
                awtFont = awtFont.deriveFont(textAttributes);
            }
            awtFontMap.put(baseFont, awtFont);
        }
    }


    public enum FontCaching {
        DEFAULT,
        ON,
        OFF
    }

    /**
     * Specify Options for an AWT font
     */
    public static class FontOptions {

        private final Map<TextAttribute, Object> textAttributes = new HashMap<>();

        /**
         * Creates a new instance
         */
        public FontOptions() {

        }

        public Map<TextAttribute, Object> getTextAttributes() {
            // always return an unmodifiableMap, so that internal state can not be changed
            // by changing the returned map
            return Collections.unmodifiableMap(textAttributes);
        }

        public FontOptions setKerningOn() {
            textAttributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
            return this;
        }

        public FontOptions setLigaturesOn() {
            textAttributes.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
            return this;
        }

        public FontOptions setRunDirectionLtr() {
            textAttributes.put(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_LTR);
            return this;
        }

        public FontOptions setRunDirectionRtl() {
            textAttributes.put(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
            return this;
        }
    }
}
