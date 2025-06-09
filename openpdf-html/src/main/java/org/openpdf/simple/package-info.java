/**
 * <p>Includes those classes you need to render XHTML documents
 * quickly, right out of the box, and with no special setup needed; start here! All the classes
 * in this package are oriented towards ease-of-use. You should be able to render documents on screen,
 * convert them to image files and print them with almost no work at all. We'll document the most
 * important classes here; see the individual class documents for details on how to use them in
 * your programs.</p>
 * <p>All classes in this package are intended for ease-of-use, with no customization
 * required. To render a document quickly, use {@link org.openpdf.simple.XHTMLPanel}--just instantiate
 * the panel, add it to a scroll pane or {@link org.openpdf.simple.FSScrollPane}, and call
 * {@link org.openpdf.simple.XHTMLPanel#setDocument(org.w3c.dom.Document)}. You can render from a {@link org.w3c.dom.Document},
 * from a {@link java.net.URL}, from a file, and from an {@link java.io.InputStream}.</p>
 * <p>The {@link org.openpdf.simple.Graphics2DRenderer} allows you to render XHTML right to image files--
 * without displaying them onscreen at all. You can use any XHTML/XML/CSS combination and
 * dump it straight to a JPEG, GIF, or other file format supported by the Java image APIs.</p>
 * <p>{@link org.openpdf.simple.FSScrollPane} is a JScrollPane with key bindings for scrolling through a document--
 * just as you would expect from a browsable XHTML document. Drop your {@link org.openpdf.simple.XHTMLPanel}
 * in a {@link org.openpdf.simple.FSScrollPane} and your users can move up or down by line or page, and jump to
 * the start or end of the document, just as they are used to.</p>
 *
 * <h2>Related Documentation</h2>
 * For overviews, tutorials, examples, guides, and tool documentation, please see:
 * <ul>
 * <li><a href="http://openpdf.dev.java.net">The Flying Saucer Project Home Page</a>
 * </ul>
 * <!-- Put @see and @since tags down here. -->
 */
@NullMarked
package org.openpdf.simple;

import org.jspecify.annotations.NullMarked;