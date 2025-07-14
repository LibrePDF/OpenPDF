/*
 * Copyright (C) 2008 Permeance Technologies Pty Ltd. All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.openpdf.pdf.util;

import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfObject;
import org.openpdf.text.pdf.PdfString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.openpdf.pdf.DefaultPDFCreationListener;
import org.openpdf.pdf.ITextRenderer;
import org.openpdf.pdf.PDFCreationListener;
import org.openpdf.util.XRLog;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


/**
 * <h1>Description</h1>
 * <p>
 * This PDF Creation Listener parses meta data elements from an (X)HTML document and appends them to
 * the info dictionary of a PDF document.
 * </p>
 *
 * <p>
 * The XHTML document is parsed for relevant PDF meta data during construction, then adds the meta
 * data to the PDF document when the PDF document is closed by the calling ITextRenderer.
 * </p>
 *
 * <p>
 * Valid (X)HTML tags are:
 * <ul>
 * <li>TITLE</li>
 * </ul>
 * </p>
 *
 * <p>
 * Valid (X)HTML meta tag attribute names are:
 * <ul>
 * <li>TITLE (optional), DC.TITLE</li>
 * <li>CREATOR, AUTHOR, DC.CREATOR</li>
 * <li>SUBJECT, DC.SUBJECT</li>
 * <li>KEYWORDS</li>
 * </ul>
 * </p>
 *
 * <p>
 * Valid PDF meta names are defined in Adobe's PDF Reference (Sixth Edition),
 * section "10.2.1 - Document Information Dictionary", table 10.2, pg.844
 * <a href="http://www.adobe.com/devnet/pdf/pdf_reference.html">...</a>
 * </p>
 *
 * <h1>Usage</h1>
 * <pre>
 * // Setup output stream
 * OutputStream outputStream = ...
 *
 * // Create W3C document model
 * Document doc = ...
 *
 * // Create new PDF renderer
 * ITextRenderer renderer = new ITextRenderer();
 *
 * // Add PDF creation listener
 * PDFCreationListener pdfCreationListener = new XHtmlMetaToPdfInfoAdapter( doc );
 * renderer.setListener( pdfCreationListener);
 *
 * // Add W3C document to renderer
 * renderer.setDocument( doc, null );
 *
 * // Layout PDF document
 * renderer.layout();
 *
 * // Write PDF document
 * renderer.createPDF( outputStream, true );
 * </pre>
 *
 * <h1>Notes</h1>
 * This class was derived from a sample PDF creation listener
 * at "http://markmail.org/message/46t3bw7q6mbhvra2"
 * by Jesse Keller <jesse.keller@roche.com>.
 *
 * @author Tim Telcik <tim.telcik@permeance.com.au>
 * @see DefaultPDFCreationListener
 * @see PDFCreationListener
 * @see ITextRenderer
 * @see <a href="https://markmail.org/message/46t3bw7q6mbhvra2">...</a>
 * @see <a href="https://www.adobe.com/devnet/pdf/pdf_reference.html">...</a>
 * @see <a href="https://www.seoconsultants.com/meta-tags/dublin/">...</a>
 */
public class XHtmlMetaToPdfInfoAdapter extends DefaultPDFCreationListener {
    private static final String HTML_TAG_TITLE = "title";
    private static final String HTML_TAG_HEAD = "head";
    private static final String HTML_TAG_META = "meta";
    private static final String HTML_META_KEY_TITLE = "title";
    private static final String HTML_META_KEY_DC_TITLE = "DC.title";
    private static final String HTML_META_KEY_CREATOR = "creator";
    private static final String HTML_META_KEY_DC_CREATOR = "DC.creator";
    private static final String HTML_META_KEY_SUBJECT = "subject";
    private static final String HTML_META_KEY_DC_SUBJECT = "DC.subject";
    private static final String HTML_META_KEY_KEYWORDS = "keywords";
    private static final String HTML_META_ATTR_NAME = "name";
    private static final String HTML_META_ATTR_CONTENT = "content";

    private final Map<PdfName, PdfString> pdfInfoValues = new HashMap<>();


    /**
     * Creates a new adapter from the given XHTML document.
     *
     * @param doc XHTML document
     */
    public XHtmlMetaToPdfInfoAdapter( Document doc ) {
        parseHtmlTags( doc );
    }

    /**
     * PDFCreationListener onClose event handler.
     *
     * @see PDFCreationListener
     */
    @Override
    public void onClose( ITextRenderer renderer ) {
        XRLog.render(Level.FINEST, "handling onClose event ..." );
        addPdfMetaValuesToPdfDocument( renderer );
    }

    private void parseHtmlTags( Document doc ) {
        XRLog.render(Level.FINEST, "parsing (X)HTML tags ..." );
        parseHtmlTitleTag( doc );
        parseHtmlMetaTags( doc );
        if ( XRLog.isLoggingEnabled() ) {
            XRLog.render(Level.FINEST, "PDF info map = " + pdfInfoValues );
        }
    }

    private void parseHtmlTitleTag( Document doc ) {

        NodeList headNodeList = doc.getDocumentElement().getElementsByTagName( HTML_TAG_HEAD );
        XRLog.render(Level.FINEST, "headNodeList=" + headNodeList );
        Element rootHeadNodeElement = (Element) headNodeList.item( 0 );
        NodeList titleNodeList = rootHeadNodeElement.getElementsByTagName( HTML_TAG_TITLE );
        XRLog.render(Level.FINEST, "titleNodeList=" + titleNodeList );
        Element titleElement = (Element) titleNodeList.item( 0 );
        if ( titleElement != null ) {
            XRLog.render(Level.FINEST, "titleElement=" + titleElement );
            XRLog.render(Level.FINEST, "titleElement.name=" + titleElement.getTagName() );
            XRLog.render(Level.FINEST, "titleElement.value=" + titleElement.getNodeValue() );
            XRLog.render(Level.FINEST, "titleElement.content=" + titleElement.getTextContent() );
            String titleContent = titleElement.getTextContent();
            PdfName pdfName = PdfName.TITLE;
            PdfString pdfString = new PdfString( titleContent );
            this.pdfInfoValues.put( pdfName, pdfString );
        }
    }

    private void parseHtmlMetaTags( Document doc ) {

        NodeList headNodeList = doc.getDocumentElement().getElementsByTagName( HTML_TAG_HEAD );
        XRLog.render(Level.FINEST, "headNodeList=" + headNodeList );
        Element rootHeadNodeElement = (Element) headNodeList.item( 0 );
        NodeList metaNodeList = rootHeadNodeElement.getElementsByTagName( HTML_TAG_META );
        XRLog.render(Level.FINEST, "metaNodeList=" + metaNodeList );

        for (int inode = 0; inode < metaNodeList.getLength(); ++inode) {
            XRLog.render(Level.FINEST, "node " + inode + " = "+ metaNodeList.item( inode ).getNodeName() );
            Element thisNode = (Element) metaNodeList.item( inode );
            XRLog.render(Level.FINEST, "node " + thisNode );
            String metaName = thisNode.getAttribute( HTML_META_ATTR_NAME );
            String metaContent = thisNode.getAttribute( HTML_META_ATTR_CONTENT );
            XRLog.render(Level.FINEST, "metaName=" + metaName + ", metaContent=" + metaContent );
            if (!metaName.isEmpty() && !metaContent.isEmpty()) {

                if ( HTML_META_KEY_TITLE.equalsIgnoreCase( metaName )
                        || HTML_META_KEY_DC_TITLE.equalsIgnoreCase( metaName ) ) {
                    PdfName pdfName = PdfName.TITLE;
                    PdfString pdfString = new PdfString( metaContent, PdfObject.TEXT_UNICODE );
                    this.pdfInfoValues.put( pdfName, pdfString );

                } else if ( HTML_META_KEY_CREATOR.equalsIgnoreCase( metaName )
                        || HTML_META_KEY_DC_CREATOR.equalsIgnoreCase( metaName ) ) {
                    PdfName pdfName = PdfName.AUTHOR;
                    PdfString pdfString = new PdfString( metaContent, PdfObject.TEXT_UNICODE );
                    this.pdfInfoValues.put( pdfName, pdfString );

                } else if ( HTML_META_KEY_SUBJECT.equalsIgnoreCase( metaName )
                        || HTML_META_KEY_DC_SUBJECT.equalsIgnoreCase( metaName ) ) {
                    PdfName pdfName = PdfName.SUBJECT;
                    PdfString pdfString = new PdfString( metaContent, PdfObject.TEXT_UNICODE );
                    this.pdfInfoValues.put( pdfName, pdfString );

                } else if ( HTML_META_KEY_KEYWORDS.equalsIgnoreCase( metaName ) ) {
                    PdfName pdfName = PdfName.KEYWORDS;
                    PdfString pdfString = new PdfString( metaContent, PdfObject.TEXT_UNICODE );
                    this.pdfInfoValues.put( pdfName, pdfString );
                }
            }
        }
    }

    /**
     * Add PDF meta values to the target PDF document.
     */
    private void addPdfMetaValuesToPdfDocument( ITextRenderer renderer ) {

        for (Map.Entry<PdfName, PdfString> entry : pdfInfoValues.entrySet()) {
            PdfName pdfName = entry.getKey();
            PdfString pdfString = entry.getValue();
            XRLog.render(Level.FINEST, "pdfName=" + pdfName + ", pdfString=" + pdfString);
            renderer.getOutputDevice().getWriter().getInfo().put(pdfName, pdfString);
        }
        if ( XRLog.isLoggingEnabled() ) {
            XRLog.render(Level.FINEST, "added " + renderer.getOutputDevice().getWriter().getInfo().getKeys() );
        }
    }

}
