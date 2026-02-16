/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Font parsing and rendering support for PDF documents.
 * 
 * <p>This package provides comprehensive font support for PDF rendering,
 * including parsing and rendering of various font formats used in PDF documents:</p>
 * 
 * <ul>
 *   <li><strong>Type1 Fonts</strong> - PostScript Type 1 fonts with full glyph support</li>
 *   <li><strong>TrueType Fonts</strong> - TrueType fonts with Unicode mapping</li>
 *   <li><strong>Type3 Fonts</strong> - User-defined fonts with custom glyphs</li>
 *   <li><strong>CID Fonts</strong> - Character Identifier fonts for CJK text</li>
 *   <li><strong>Builtin Fonts</strong> - Standard PDF base-14 fonts</li>
 * </ul>
 * 
 * <p>The font classes handle character encoding, glyph metrics, kerning,
 * and proper text rendering on Java2D graphics contexts.</p>
 * 
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link org.openpdf.renderer.font.PDFFont} - Base class for all PDF fonts</li>
 *   <li>{@link org.openpdf.renderer.font.Type1Font} - Type1 font implementation</li>
 *   <li>{@link org.openpdf.renderer.font.TrueTypeFont} - TrueType font implementation</li>
 *   <li>{@link org.openpdf.renderer.font.Type3Font} - Type3 font implementation</li>
 *   <li>{@link org.openpdf.renderer.font.PDFCMap} - Character mapping for CID fonts</li>
 * </ul>
 * 
 * @see org.openpdf.renderer.font.PDFFont
 * @see org.openpdf.renderer.font.ttf
 */
package org.openpdf.renderer.font;
