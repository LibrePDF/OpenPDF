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
package org.openpdf.context;

import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.value.FontSpecification;
import org.openpdf.extend.FontResolver;
import org.openpdf.layout.SharedContext;
import org.openpdf.render.FSFont;
import org.openpdf.swing.AWTFSFont;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

public class AWTFontResolver implements FontResolver {
    private final Map<String, Font> fontsCache = new HashMap<>();

    private final Set<String> availableFontNames = new HashSet<>();
    private final Map<String, Font> availableFonts = new HashMap<>();

    public AWTFontResolver() {
        init();
    }

    private void init() {
        fontsCache.clear();

        GraphicsEnvironment gfx = GraphicsEnvironment.getLocalGraphicsEnvironment();
        availableFontNames.clear();
        availableFontNames.addAll(asList(gfx.getAvailableFontFamilyNames()));

        // preload sans, serif, and monospace
        availableFonts.clear();
        availableFonts.put("Serif", new Font("Serif", Font.PLAIN, 1));
        availableFonts.put("SansSerif", new Font("SansSerif", Font.PLAIN, 1));
        availableFonts.put("Monospaced", new Font("Monospaced", Font.PLAIN, 1));
    }

    @Override
    public void flushCache() {
        init();
    }

    public FSFont resolveFont(SharedContext ctx, String[] families, float size, IdentValue weight, IdentValue style, IdentValue variant) {
        // for each font family
        if (families != null) {
            for (String family : families) {
                Font font = resolveFont(ctx, family, size, weight, style, variant);
                if (font != null) {
                    return new AWTFSFont(font);
                }
            }
        }

        // if we get here then no font worked, so just return default sans
        String family = "SansSerif";
        if (style == IdentValue.ITALIC) {
            family = "Serif";
        }

        Font fnt = createFont(ctx, availableFonts.get(family), size, weight, style, variant);
        fontsCache.put(getFontInstanceHashName(ctx, family, size, weight, style, variant), fnt);
        return new AWTFSFont(fnt);
    }

    /**
     * Sets the fontMapping attribute of the FontResolver object
     *
     * @param name The new fontMapping value
     * @param font The new fontMapping value
     */
    public void setFontMapping(String name, Font font) {
        availableFonts.put(name, font.deriveFont(1.0f));
    }

    protected static Font createFont(SharedContext ctx, Font root_font, float size,
                                     IdentValue weight,
                                     IdentValue style,
                                     IdentValue variant) {
        int font_const = Font.PLAIN;
        if (weight != null &&
                (weight == IdentValue.BOLD ||
                weight == IdentValue.FONT_WEIGHT_700 ||
                weight == IdentValue.FONT_WEIGHT_800 ||
                weight == IdentValue.FONT_WEIGHT_900)) {

            font_const = font_const | Font.BOLD;
        }
        if (style != null && (style == IdentValue.ITALIC || style == IdentValue.OBLIQUE)) {
            font_const = font_const | Font.ITALIC;
        }

        // scale vs font scale value too
        size *= ctx.getTextRenderer().getFontScale();

        Font fnt = root_font.deriveFont(font_const, size);
        if (variant != null) {
            if (variant == IdentValue.SMALL_CAPS) {
                fnt = fnt.deriveFont((float) (((float) fnt.getSize()) * 0.6));
            }
        }

        return fnt;
    }

    protected Font resolveFont(SharedContext ctx, String font, float size, IdentValue weight, IdentValue style, IdentValue variant) {
        // strip off the "s if they are there
        if (font.startsWith("\"")) {
            font = font.substring(1);
        }
        if (font.endsWith("\"")) {
            font = font.substring(0, font.length() - 1);
        }

        // normalize the font name
        if (font.equalsIgnoreCase("serif")) {
            font = "Serif";
        }
        if (font.equalsIgnoreCase("sans-serif")) {
            font = "SansSerif";
        }
        if (font.equalsIgnoreCase("monospace")) {
            font = "Monospaced";
        }

        if (font.equals("Serif") && style == IdentValue.OBLIQUE) {
            font = "SansSerif";
        }
        if (font.equals("SansSerif") && style == IdentValue.ITALIC) {
            font = "Serif";
        }

        // assemble a font instance hash name
        String font_instance_name = getFontInstanceHashName(ctx, font, size, weight, style, variant);
        // check if the font instance exists in the hash table
        if (fontsCache.containsKey(font_instance_name)) {
            // if so then return it
            return fontsCache.get(font_instance_name);
        }

        Font root_font = availableFonts.get(font);
        if (root_font == null && availableFontNames.contains(font)) {
            root_font = new Font(font, Font.PLAIN, 1);
            availableFonts.put(font, root_font);
        }

        if (root_font != null) {
            // now that we have a root font, we need to create the correct version of it
            Font fnt = createFont(ctx, root_font, size, weight, style, variant);

            // add the font to the hash, so we don't have to do this again
            fontsCache.put(font_instance_name, fnt);
            return fnt;
        }

        // we didn't find any possible matching font, so just return null
        return null;
    }

    /**
     * Gets the fontInstanceHashName attribute of the FontResolverTest object
     */
    protected static String getFontInstanceHashName(SharedContext ctx, String name, float size, IdentValue weight, IdentValue style, IdentValue variant) {
        return name + "-" + (size * ctx.getTextRenderer().getFontScale()) + "-" + weight + "-" + style + "-" + variant;
    }

    @Override
    public FSFont resolveFont(SharedContext renderingContext, FontSpecification spec) {
        return resolveFont(renderingContext, spec.families, spec.size, spec.fontWeight, spec.fontStyle, spec.variant);
    }
}
