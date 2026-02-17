/*
 * This file is part of the OpenPDF HTML module.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.openpdf.resource;

import com.google.errorprone.annotations.CheckReturnValue;
import org.htmlunit.cyberneko.HTMLScanner;
import org.htmlunit.cyberneko.html.dom.HTMLDocumentImpl;
import org.htmlunit.cyberneko.parsers.DOMParser;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;
import org.jspecify.annotations.Nullable;
import org.openpdf.util.XRLog;
import org.openpdf.util.XRRuntimeException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.logging.Level;

/**
 * HtmlResource provides HTML5-compliant parsing using htmlunit-neko's DOMParser.
 * <p>
 * This class leverages the htmlunit-neko parser
 * (<a href="https://github.com/HtmlUnit/htmlunit-neko">https://github.com/HtmlUnit/htmlunit-neko</a>)
 * for error-tolerant HTML parsing with the following features:
 * <ul>
 *   <li>HTML5 compliant parsing</li>
 *   <li>Error tolerant - handles malformed HTML gracefully</li>
 *   <li>Automatic tag balancing and fixing</li>
 *   <li>Support for modern HTML5 semantic elements</li>
 *   <li>Configurable parsing features</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * // Parse HTML string with default settings
 * HtmlResource resource = HtmlResource.load("&lt;html&gt;&lt;body&gt;Hello&lt;/body&gt;&lt;/html&gt;");
 * Document doc = resource.getDocument();
 *
 * // Parse with custom configuration
 * HtmlParserConfig config = HtmlParserConfig.builder()
 *     .reportErrors(true)
 *     .allowSelfClosingTags(true)
 *     .build();
 * HtmlResource resource = HtmlResource.load(html, config);
 * </pre>
 *
 * @see XMLResource for XML/XHTML parsing
 * @see HtmlParserConfig for configuration options
 */
public class HtmlResource extends AbstractResource {

    private final Document document;
    private final long elapsedLoadTime;

    private HtmlResource(@Nullable InputSource source, Document document, long elapsedLoadTime) {
        super(source);
        this.document = document;
        this.elapsedLoadTime = elapsedLoadTime;
    }

    /**
     * Load and parse an HTML document from a URL.
     *
     * @param source the URL to load HTML from
     * @return the parsed HtmlResource
     */
    public static HtmlResource load(URL source) {
        return load(source, HtmlParserConfig.defaults());
    }

    /**
     * Load and parse an HTML document from a URL with custom configuration.
     *
     * @param source the URL to load HTML from
     * @param config parser configuration
     * @return the parsed HtmlResource
     */
    public static HtmlResource load(URL source, HtmlParserConfig config) {
        try (InputStream stream = source.openStream()) {
            return load(stream, config, source.toExternalForm());
        } catch (IOException e) {
            throw new XRRuntimeException("Failed to load HTML from URL: " + source, e);
        }
    }

    /**
     * Load and parse an HTML document from an InputStream.
     *
     * @param stream the InputStream containing HTML content
     * @return the parsed HtmlResource
     */
    public static HtmlResource load(InputStream stream) {
        return load(stream, HtmlParserConfig.defaults(), null);
    }

    /**
     * Load and parse an HTML document from an InputStream with custom configuration.
     *
     * @param stream the InputStream containing HTML content
     * @param config parser configuration
     * @param systemId optional system identifier for the document
     * @return the parsed HtmlResource
     */
    public static HtmlResource load(InputStream stream, HtmlParserConfig config, @Nullable String systemId) {
        long start = System.currentTimeMillis();
        try {
            XMLInputSource inputSource = new XMLInputSource(null, systemId, null, stream, config.getEncoding());
            Document doc = parseWithDomParser(inputSource, config);
            long elapsed = System.currentTimeMillis() - start;
            XRLog.load("Loaded HTML document in " + elapsed + "ms using htmlunit-neko DOMParser");
            return new HtmlResource(null, doc, elapsed);
        } catch (Exception e) {
            throw new XRRuntimeException("Failed to parse HTML from stream", e);
        }
    }

    /**
     * Load and parse an HTML document from a Reader.
     *
     * @param reader the Reader containing HTML content
     * @return the parsed HtmlResource
     */
    public static HtmlResource load(Reader reader) {
        return load(reader, HtmlParserConfig.defaults());
    }

    /**
     * Load and parse an HTML document from a Reader with custom configuration.
     *
     * @param reader the Reader containing HTML content
     * @param config parser configuration
     * @return the parsed HtmlResource
     */
    public static HtmlResource load(Reader reader, HtmlParserConfig config) {
        long start = System.currentTimeMillis();
        try {
            XMLInputSource inputSource = new XMLInputSource(null, null, null, reader, config.getEncoding());
            Document doc = parseWithDomParser(inputSource, config);
            long elapsed = System.currentTimeMillis() - start;
            XRLog.load("Loaded HTML document in " + elapsed + "ms using htmlunit-neko DOMParser");
            return new HtmlResource(null, doc, elapsed);
        } catch (Exception e) {
            throw new XRRuntimeException("Failed to parse HTML from reader", e);
        }
    }

    /**
     * Load and parse an HTML document from a String.
     *
     * @param html the HTML content as a String
     * @return the parsed HtmlResource
     */
    public static HtmlResource load(String html) {
        return load(html, HtmlParserConfig.defaults());
    }

    /**
     * Load and parse an HTML document from a String with custom configuration.
     *
     * @param html the HTML content as a String
     * @param config parser configuration
     * @return the parsed HtmlResource
     */
    public static HtmlResource load(String html, HtmlParserConfig config) {
        return load(new StringReader(html), config);
    }

    /**
     * Parse HTML using htmlunit-neko's DOMParser with the specified configuration.
     */
    private static Document parseWithDomParser(XMLInputSource inputSource, HtmlParserConfig config)
            throws SAXException, IOException {
        DOMParser parser = new DOMParser(HTMLDocumentImpl.class);

        // Configure parser features based on HtmlParserConfig
        configureParser(parser, config);

        parser.parse(inputSource);
        return parser.getDocument();
    }

    /**
     * Configure the DOMParser with the specified options.
     */
    private static void configureParser(DOMParser parser, HtmlParserConfig config) {
        try {
            // Report parsing errors if enabled
            if (config.isReportErrors()) {
                parser.setFeature(HTMLScanner.REPORT_ERRORS, true);
            }

            // Allow XHTML-style self-closing tags if enabled
            if (config.isAllowSelfClosingTags()) {
                parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_TAGS, true);
            }

            // Allow self-closing iframe tags
            if (config.isAllowSelfClosingIframe()) {
                parser.setFeature(HTMLScanner.ALLOW_SELFCLOSING_IFRAME, true);
            }

            // Parse noscript content as markup
            if (config.isParseNoScriptContent()) {
                parser.setFeature(HTMLScanner.PARSE_NOSCRIPT_CONTENT, true);
            }

            // Strip comment delimiters from script content
            if (config.isScriptStripCommentDelims()) {
                parser.setFeature(HTMLScanner.SCRIPT_STRIP_COMMENT_DELIMS, true);
            }

            // Strip comment delimiters from style content
            if (config.isStyleStripCommentDelims()) {
                parser.setFeature(HTMLScanner.STYLE_STRIP_COMMENT_DELIMS, true);
            }

            // Configure element name handling
            if (config.getElementNameCase() != null) {
                parser.setProperty(HTMLScanner.NAMES_ELEMS, config.getElementNameCase());
            }

            // Configure attribute name handling
            if (config.getAttributeNameCase() != null) {
                parser.setProperty(HTMLScanner.NAMES_ATTRS, config.getAttributeNameCase());
            }

            // Set default encoding if specified
            if (config.getEncoding() != null) {
                parser.setProperty(HTMLScanner.DEFAULT_ENCODING, config.getEncoding());
            }

        } catch (SAXException e) {
            XRLog.load(Level.WARNING, "Could not configure htmlunit-neko DOMParser feature", e);
        }
    }

    /**
     * Get the parsed DOM Document.
     *
     * @return the DOM Document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Get the time taken to load and parse the document.
     *
     * @return elapsed time in milliseconds
     */
    @CheckReturnValue
    public long getElapsedLoadTime() {
        return elapsedLoadTime;
    }
}
