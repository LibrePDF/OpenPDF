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
 * Color space implementations for PDF rendering.
 * 
 * <p>This package provides color space classes that handle color conversions
 * and transformations required for PDF rendering. It supports various PDF
 * color spaces including:</p>
 * 
 * <ul>
 *   <li>DeviceRGB - Standard RGB color space</li>
 *   <li>DeviceCMYK - CMYK color space for print production</li>
 *   <li>DeviceGray - Grayscale color space</li>
 *   <li>ICCBased - ICC profile-based color spaces</li>
 *   <li>Indexed - Palette-based color spaces</li>
 *   <li>Separation - Spot color support</li>
 *   <li>Pattern - Pattern-based colors</li>
 * </ul>
 * 
 * <p>The classes in this package extend Java's {@link java.awt.color.ColorSpace}
 * to integrate with Java2D rendering while providing PDF-specific color
 * transformations.</p>
 * 
 * @see java.awt.color.ColorSpace
 * @see org.openpdf.renderer.colorspace.PDFColorSpace
 */
package org.openpdf.renderer.colorspace;
