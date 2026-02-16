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
 * OpenPDF Renderer - A pure Java library for rendering PDF files as images.
 * 
 * <p>This package provides the core classes for parsing and rendering PDF documents
 * using Java2D. The main entry points are:</p>
 * 
 * <ul>
 *   <li>{@link org.openpdf.renderer.PDFFile} - Represents a parsed PDF document</li>
 *   <li>{@link org.openpdf.renderer.PDFPage} - Represents a single page that can be rendered</li>
 *   <li>{@link org.openpdf.renderer.PDFRenderer} - Provides rendering utilities</li>
 * </ul>
 * 
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * // Load a PDF file
 * File file = new File("document.pdf");
 * RandomAccessFile raf = new RandomAccessFile(file, "r");
 * FileChannel channel = raf.getChannel();
 * ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
 * 
 * PDFFile pdfFile = new PDFFile(buf);
 * 
 * // Get first page (1-based index)
 * PDFPage page = pdfFile.getPage(1);
 * 
 * // Render page to image
 * Rectangle rect = new Rectangle(0, 0,
 *     (int) page.getBBox().getWidth(),
 *     (int) page.getBBox().getHeight());
 * 
 * Image img = page.getImage(rect.width, rect.height, rect, null, true, true);
 * }</pre>
 * 
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Supports PDF 1.4 specification (subset)</li>
 *   <li>TrueType, Type1, Type3, and CID font support</li>
 *   <li>RGB, CMYK, Gray, and ICC color spaces</li>
 *   <li>Pattern and shader rendering</li>
 *   <li>Basic annotation support</li>
 *   <li>Pure Java implementation using Java2D</li>
 * </ul>
 * 
 * <h2>Important Notes</h2>
 * <ul>
 *   <li>Page numbering is 1-based (first page is page 1, not 0)</li>
 *   <li>PDF decryption functionality has been removed from this module</li>
 *   <li>Thread safety: {@link org.openpdf.renderer.PDFFile} and 
 *       {@link org.openpdf.renderer.PDFPage} objects are not thread-safe</li>
 * </ul>
 * 
 * @see org.openpdf.renderer.PDFFile
 * @see org.openpdf.renderer.PDFPage
 * @see org.openpdf.renderer.PDFRenderer
 */
package org.openpdf.renderer;
