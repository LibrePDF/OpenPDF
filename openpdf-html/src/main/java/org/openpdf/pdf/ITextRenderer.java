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
import org.openpdf.text.pdf.PdfPageEvent;
import org.openpdf.text.pdf.PdfWriter;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.openpdf.css.style.CalculatedStyle.Edge;
import org.openpdf.css.style.derived.RectPropertySet;
import org.openpdf.extend.FontResolver;
import org.openpdf.extend.NamespaceHandler;
import org.openpdf.extend.ReplacedElementFactory;
import org.openpdf.extend.TextRenderer;
import org.openpdf.extend.UserInterface;
import org.openpdf.layout.BoxBuilder;
import org.openpdf.layout.Layer;
import org.openpdf.layout.LayoutContext;
import org.openpdf.layout.SharedContext;
import org.openpdf.render.BlockBox;
import org.openpdf.render.PageBox;
import org.openpdf.render.RenderingContext;
import org.openpdf.render.ViewportBox;
import org.openpdf.resource.XMLResource;
import org.openpdf.simple.extend.XhtmlNamespaceHandler;
import org.openpdf.util.Configuration;
import org.xml.sax.InputSource;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.openpdf.layout.Layer.PagedMode.PAGED_MODE_PRINT;

public class ITextRenderer {
    // These two defaults combine to produce an effective resolution of 96 px to the inch
    public static final float DEFAULT_DOTS_PER_POINT = 20f * 4f / 3f;
    public static final int DEFAULT_DOTS_PER_PIXEL = 20;

    private final SharedContext _sharedContext;
    private final ITextOutputDevice _outputDevice;

    @Nullable
    private Document _doc;
    @Nullable
    private BlockBox _root;

    private final float _dotsPerPoint;

    private org.openpdf.text.Document _pdfDoc;
    @Nullable
    private PdfWriter _writer;

    @Nullable
    private PDFEncryption _pdfEncryption;

    // note: not hard-coding a default version in the _pdfVersion field as this
    // may change between iText releases
    // check for null before calling writer.setPdfVersion()
    // use one of the values in PDFWriter.VERSION...
    @Nullable
    private String _pdfVersion;

    @Nullable
    private PdfPageEvent pdfPageEvent;

    @Nullable
    private Dimension _dim;

    private boolean scaleToFit;

    private final String[] validPdfVersions = {
            PdfWriter.VERSION_1_2,
            PdfWriter.VERSION_1_3,
            PdfWriter.VERSION_1_4,
            PdfWriter.VERSION_1_5,
            PdfWriter.VERSION_1_6,
            PdfWriter.VERSION_1_7,
            PdfWriter.VERSION_2_0
    };

    @Nullable
    private Integer _pdfXConformance;

    @Nullable
    private PDFCreationListener _listener;

    public ITextRenderer(File file) throws IOException {
        this();
        File parent = file.getAbsoluteFile().getParentFile();
        setDocument(loadDocument(file.toURI().toURL().toExternalForm()), (parent == null ? "" : parent.toURI().toURL().toExternalForm()));
    }

    public ITextRenderer() {
        this(DEFAULT_DOTS_PER_POINT, DEFAULT_DOTS_PER_PIXEL);
    }

    public ITextRenderer(FontResolver fontResolver) {
        this(DEFAULT_DOTS_PER_POINT, DEFAULT_DOTS_PER_PIXEL, fontResolver);
    }

    public ITextRenderer(float dotsPerPoint, int dotsPerPixel) {
        this(dotsPerPoint, dotsPerPixel, new ITextOutputDevice(dotsPerPoint));
    }

    public ITextRenderer(float dotsPerPoint, int dotsPerPixel, FontResolver fontResolver) {
        this(dotsPerPoint, dotsPerPixel, new ITextOutputDevice(dotsPerPoint), fontResolver);
    }

    public ITextRenderer(ITextOutputDevice outputDevice, ITextUserAgent userAgent) {
        this(outputDevice.getDotsPerPoint(), userAgent.getDotsPerPixel(), outputDevice, userAgent, new ITextFontResolver());
    }

    public ITextRenderer(float dotsPerPoint, int dotsPerPixel, ITextOutputDevice outputDevice) {
        this(dotsPerPoint, dotsPerPixel, outputDevice, new ITextUserAgent(outputDevice, dotsPerPixel));
    }

    public ITextRenderer(float dotsPerPoint, int dotsPerPixel, ITextOutputDevice outputDevice, FontResolver fontResolver) {
        this(dotsPerPoint, dotsPerPixel, outputDevice, new ITextUserAgent(outputDevice, dotsPerPixel), fontResolver);
    }

    public ITextRenderer(float dotsPerPoint, int dotsPerPixel, ITextOutputDevice outputDevice, ITextUserAgent userAgent) {
        this(dotsPerPoint, dotsPerPixel, outputDevice, userAgent, new ITextFontResolver());
    }

    public ITextRenderer(float dotsPerPoint, int dotsPerPixel, ITextOutputDevice outputDevice, ITextUserAgent userAgent,
            FontResolver fontResolver) {
        this(dotsPerPoint, dotsPerPixel, outputDevice, userAgent, fontResolver,
                new ITextReplacedElementFactory(outputDevice), new ITextTextRenderer());
    }

    public ITextRenderer(float dotsPerPoint, int dotsPerPixel, ITextOutputDevice outputDevice, ITextUserAgent userAgent,
            FontResolver fontResolver, ReplacedElementFactory replacedElementFactory,
            TextRenderer textRenderer) {
        _dotsPerPoint = dotsPerPoint;
        _outputDevice = outputDevice;
        _sharedContext = new SharedContext(userAgent, fontResolver, replacedElementFactory, textRenderer,
                72 * _dotsPerPoint, dotsPerPixel);

        _outputDevice.setSharedContext(_sharedContext);
    }

    @Nullable
    public Document getDocument() {
        return _doc;
    }

    public ITextFontResolver getFontResolver() {
        return (ITextFontResolver) _sharedContext.getFontResolver();
    }

    private Document loadDocument(final String uri) {
        return _sharedContext.getUac().getXMLResource(uri).getDocument();
    }

    public static ITextRenderer fromUrl(String uri) {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocument(renderer.loadDocument(uri), uri);
        return renderer;
    }

    public void setDocument(Document doc) {
        setDocument(doc, null);
    }

    public void setDocument(Document doc, @Nullable String url) {
        setDocument(doc, url, new XhtmlNamespaceHandler());
    }

    public static ITextRenderer fromString(String content) {
        return fromString(content, null);
    }

    public static ITextRenderer fromString(String content, @Nullable String baseUrl) {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(content, baseUrl);
        return renderer;
    }

    public final void setDocumentFromString(String content) {
        setDocument(parse(content), null);
    }

    public final void setDocumentFromString(String content, @Nullable String baseUrl) {
        setDocument(parse(content), baseUrl);
    }

    private Document parse(String content) {
        try (var is = new StringReader(content)) {
            return XMLResource.load(new InputSource(is)).getDocument();
        }
    }

    public void setDocument(Document doc, @Nullable String url, NamespaceHandler nsh) {
        _doc = doc;

        getFontResolver().flushFontFaceFonts();

        _sharedContext.reset();
        if (Configuration.isTrue("xr.cache.stylesheets", true)) {
            _sharedContext.getCss().flushStyleSheets();
        } else {
            _sharedContext.getCss().flushAllStyleSheets();
        }
        _sharedContext.setBaseURL(url);
        _sharedContext.setNamespaceHandler(nsh);
        _sharedContext.getCss().setDocumentContext(_sharedContext, _sharedContext.getNamespaceHandler(), doc, new NullUserInterface());
        getFontResolver().importFontFaces(_sharedContext.getCss().getFontFaceRules(), _sharedContext.getUac());
    }

    @Nullable
    public PDFEncryption getPDFEncryption() {
        return _pdfEncryption;
    }

    public void setPDFEncryption(PDFEncryption pdfEncryption) {
        _pdfEncryption = pdfEncryption;
    }

    public void setPDFVersion(String _v) {
        if (Arrays.binarySearch(validPdfVersions, _v) < 0) {
            throw new IllegalArgumentException("""
                    Invalid PDF version character: "%s"; use one of constants PdfWriter.VERSION_1_N.
                    """.formatted(_v).trim());
        }
        _pdfVersion = _v;
    }

    public String getPDFVersion() {
        return _pdfVersion == null ? null : _pdfVersion;
    }

    public void setPDFXConformance(int pdfXConformance) {
        _pdfXConformance = pdfXConformance;
    }

    public int getPDFXConformance() {
        return _pdfXConformance == null ? '0' : _pdfXConformance;
    }

    public void layout() {
        LayoutContext c = newLayoutContext();
        BlockBox root = BoxBuilder.createRootBox(c, _doc);
        root.setContainingBlock(new ViewportBox(getInitialExtents(c)));
        root.layout(c);
        _dim = root.getLayer().getPaintingDimension(c);
        root.getLayer().trimEmptyPages(_dim.height);
        root.getLayer().layoutPages(c);
        _root = root;
    }

    private Rectangle getInitialExtents(LayoutContext c) {
        PageBox first = Layer.createPageBox(c, "first");

        return new Rectangle(0, 0, first.getContentWidth(c), first.getContentHeight(c));
    }

    private RenderingContext newRenderingContext(int initialPageNo) {
        ITextFontContext fontContext = new ITextFontContext();
        _sharedContext.getTextRenderer().setup(fontContext);
        return _sharedContext.newRenderingContextInstance(_outputDevice, fontContext, _root.getLayer(), initialPageNo);
    }

    private LayoutContext newLayoutContext() {
        ITextFontContext fontContext = new ITextFontContext();
        LayoutContext result = _sharedContext.newLayoutContextInstance(fontContext);
        _sharedContext.getTextRenderer().setup(fontContext);

        return result;
    }

    public byte[] createPDF(Document source) throws DocumentException {
        setDocument(source, source.getDocumentURI());
        layout();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        createPDF(bos);
        finishPDF();
        return bos.toByteArray();
    }

    public void createPDF(Document source, OutputStream os) throws DocumentException {
        setDocument(source, source.getDocumentURI());
        layout();
        createPDF(os);
        finishPDF();
    }

    public void createPDF(OutputStream os) throws DocumentException {
        createPDF(os, true, 0);
    }

    public void writeNextDocument() {
        writeNextDocument(0);
    }

    public void writeNextDocument(int initialPageNo) {
        List<PageBox> pages = _root.getLayer().getPages();

        RenderingContext c = newRenderingContext(initialPageNo);
        PageBox firstPage = pages.get(0);
        org.openpdf.text.Rectangle firstPageSize =
                new org.openpdf.text.Rectangle(0, 0, firstPage.getWidth(c) / _dotsPerPoint,
                        firstPage.getHeight(c) / _dotsPerPoint);

        _outputDevice.setStartPageNo(_writer.getPageNumber());

        _pdfDoc.setPageSize(firstPageSize);
        _pdfDoc.newPage();

        writePDF(pages, c, firstPageSize, _pdfDoc, _writer);
    }

    public void finishPDF() {
        if (_pdfDoc != null) {
            fireOnClose();
            _pdfDoc.close();
        }
    }

    public void createPDF(OutputStream os, boolean finish) throws DocumentException {
        createPDF(os, finish, 0);
    }

    /**
     * <B>NOTE:</B> Caller is responsible for cleaning up the OutputStream if
     * something goes wrong.
     */
    public void createPDF(OutputStream os, boolean finish, int initialPageNo) throws DocumentException {
        List<PageBox> pages = _root.getLayer().getPages();

        RenderingContext c = newRenderingContext(initialPageNo);

        PageBox firstPage = pages.get(0);

        int pageWidth = calculateWidth(c, firstPage);

        org.openpdf.text.Rectangle firstPageSize =
                new org.openpdf.text.Rectangle(0, 0, pageWidth / _dotsPerPoint,
                        firstPage.getHeight(c) / _dotsPerPoint);

        org.openpdf.text.Document doc = new org.openpdf.text.Document(firstPageSize, 0, 0, 0, 0);
        PdfWriter writer = PdfWriter.getInstance(doc, os);
        if (_pdfVersion != null) {
            writer.setPdfVersion(_pdfVersion);
        }

        if (_pdfXConformance != null) {
            writer.setPDFXConformance(_pdfXConformance);
        }

        if (pdfPageEvent != null) {
            writer.setPageEvent(pdfPageEvent);
        }

        if (_pdfEncryption != null) {
            writer.setEncryption(_pdfEncryption.getUserPassword(), _pdfEncryption.getOwnerPassword(),
                    _pdfEncryption.getAllowedPrivileges(), _pdfEncryption.getEncryptionType());
        }
        _pdfDoc = doc;
        _writer = writer;

        firePreOpen();
        doc.open();

        writePDF(pages, c, firstPageSize, doc, writer);

        if (finish) {
            fireOnClose();
            doc.close();
        }
    }

    private void firePreOpen() {
        if (_listener != null) {
            _listener.preOpen(this);
        }
    }

    private void firePreWrite(int pageCount) {
        if (_listener != null) {
            _listener.preWrite(this, pageCount);
        }
    }

    private void fireOnClose() {
        if (_listener != null) {
            _listener.onClose(this);
        }
    }

    private void writePDF(List<PageBox> pages, RenderingContext c, org.openpdf.text.Rectangle firstPageSize,
            org.openpdf.text.Document doc,
            PdfWriter writer) {
        _outputDevice.setRoot(_root);

        _outputDevice.start(_doc);
        _outputDevice.setWriter(writer);
        _outputDevice.initializePage(writer.getDirectContent(), firstPageSize.getHeight());

        _root.getLayer().assignPagePaintingPositions(c, PAGED_MODE_PRINT);

        int pageCount = _root.getLayer().getPages().size();
        c.setPageCount(pageCount);
        firePreWrite(pageCount); // opportunity to adjust meta data
        setDidValues(doc); // set PDF header fields from meta data
        for (int i = 0; i < pageCount; i++) {

            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException("Timeout occurred");
            }

            PageBox currentPage = pages.get(i);
            c.setPage(i, currentPage);
            paintPage(c, writer, currentPage);
            _outputDevice.finishPage();
            if (i != pageCount - 1) {
                PageBox nextPage = pages.get(i + 1);
                int pageWidth = calculateWidth(c, nextPage);
                org.openpdf.text.Rectangle nextPageSize =
                        new org.openpdf.text.Rectangle(0, 0, pageWidth / _dotsPerPoint,
                                nextPage.getHeight(c) / _dotsPerPoint);
                doc.setPageSize(nextPageSize);
                doc.newPage();
                _outputDevice.initializePage(writer.getDirectContent(), nextPageSize.getHeight());
            }
        }

        _outputDevice.finish(c, _root);
    }

    // Sets the document information dictionary values from html metadata
    private void setDidValues(org.openpdf.text.Document doc) {
        String v = _outputDevice.getMetadataByName("title");
        if (v != null) {
            doc.addTitle(v);
        }
        v = _outputDevice.getMetadataByName("author");
        if (v != null) {
            doc.addAuthor(v);
        }
        v = _outputDevice.getMetadataByName("subject");
        if (v != null) {
            doc.addSubject(v);
        }
        v = _outputDevice.getMetadataByName("keywords");
        if (v != null) {
            doc.addKeywords(v);
        }
    }

    private void paintPage(RenderingContext c, PdfWriter writer, PageBox page) {
        provideMetadataToPage(writer, page);

        page.paintBackground(c, 0, PAGED_MODE_PRINT);
        page.paintMarginAreas(c, 0, PAGED_MODE_PRINT);
        page.paintBorder(c, 0, PAGED_MODE_PRINT);

        Shape working = _outputDevice.getClip();

        Rectangle content = page.getPrintClippingBounds(c);
        if (isScaleToFit()) {
            int pageWidth = calculateWidth(c, page);
            content.setSize(pageWidth, (int) content.getSize().getHeight());//RTD - to change
        }
        _outputDevice.clip(content);

        int top = -page.getPaintingTop() + page.getMarginBorderPadding(c, Edge.TOP);

        int left = page.getMarginBorderPadding(c, Edge.LEFT);

        _outputDevice.translate(left, top);
        _root.getLayer().paint(c);
        _outputDevice.translate(-left, -top);

        _outputDevice.setClip(working);
    }

    private void provideMetadataToPage(PdfWriter writer, PageBox page) {
        byte[] metadata = null;
        if (page.getMetadata() != null) {
            String metadataBody = stringifyMetadata(page.getMetadata());
            if (metadataBody != null) {
                metadata = createXPacket(stringifyMetadata(page.getMetadata())).getBytes(UTF_8);
            }
        }

        if (metadata != null) {
            writer.setPageXmpMetadata(metadata);
        }
    }

    @Nullable
    private String stringifyMetadata(Element element) {
        Element target = getFirstChildElement(element);
        if (target == null) {
            return null;
        }

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter output = new StringWriter();
            transformer.transform(new DOMSource(target), new StreamResult(output));

            return output.toString();
        } catch (TransformerConfigurationException e) {
            // Things must be in pretty bad shape to get here so
            // rethrow as runtime exception
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private static Element getFirstChildElement(Element element) {
        Node n = element.getFirstChild();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) n;
            }
            n = n.getNextSibling();
        }
        return null;
    }

    private String createXPacket(String metadata) {
        return "<?xpacket begin='\uFEFF' id='W5M0MpCehiHzreSzNTczkc9d'?>\n" +
                metadata +
                "\n<?xpacket end='r'?>";
    }

    public ITextOutputDevice getOutputDevice() {
        return _outputDevice;
    }

    public SharedContext getSharedContext() {
        return _sharedContext;
    }

    public void exportText(Writer writer) throws IOException {
        RenderingContext c = newRenderingContext(0);
        c.setPageCount(_root.getLayer().getPages().size());
        _root.exportText(c, writer);
    }

    @Nullable
    public BlockBox getRootBox() {
        return _root;
    }

    public float getDotsPerPoint() {
        return _dotsPerPoint;
    }

    public List<PagePosition> findPagePositionsByID(Pattern pattern) {
        return _outputDevice.findPagePositionsByID(newLayoutContext(), pattern);
    }


    private static final class NullUserInterface implements UserInterface {
        @Override
        public boolean isHover(Element e) {
            return false;
        }

        @Override
        public boolean isActive(Element e) {
            return false;
        }

        @Override
        public boolean isFocus(Element e) {
            return false;
        }
    }

    private int calculateWidth(RenderingContext c, PageBox firstPage) {
        if (isScaleToFit()) {
            int pageWidth = firstPage.getWidth(c);
            Rectangle pageRec = firstPage.getPrintClippingBounds(c);
            if(_dim.getWidth() > pageRec.getWidth()) {
                RectPropertySet margin = firstPage.getMargin(c);
                pageWidth = (int) (_dim.getWidth() + margin.left() + margin.right());
            }
            return pageWidth;
        } else {
            return firstPage.getWidth(c);
        }
    }

    @Nullable
    public PDFCreationListener getListener() {
        return _listener;
    }

    public void setListener(PDFCreationListener listener) {
        _listener = listener;
    }

    @Nullable
    public PdfWriter getWriter() {
        return _writer;
    }

    @Nullable
    public PdfPageEvent getPdfPageEvent() {
        return pdfPageEvent;
    }

    public void setPdfPageEvent(@Nullable PdfPageEvent pdfPageEvent) {
        this.pdfPageEvent = pdfPageEvent;
    }

    public void setScaleToFit(boolean scaleToFit) {
        this.scaleToFit = scaleToFit;
    }

    public boolean isScaleToFit() {
        return scaleToFit;
    }
}
