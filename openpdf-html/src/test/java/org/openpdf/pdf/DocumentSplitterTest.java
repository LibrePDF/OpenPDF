package org.openpdf.pdf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentSplitterTest {
    private final SAXParserFactory factory = SAXParserFactory.newInstance();
    private XMLReader reader;
    private final DocumentSplitter splitter = new DocumentSplitter();

    @BeforeEach
    public final void setUp() throws ParserConfigurationException, SAXException {
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        reader = factory.newSAXParser().getXMLReader();
        reader.setErrorHandler(new StrictErrorHandler());
        reader.setContentHandler(splitter);
    }

    @Test
    public void splitDocumentWithoutHead() throws Exception {
        reader.parse(new InputSource(new StringReader("<h1>no head</h1>")));
        assertThat(splitter.getDocuments()).hasSize(0);
    }

    @Test
    public void splitDocumentWithHead() throws Exception {
        reader.parse(new InputSource(new StringReader("<html>" +
                "<head><title>The head</title></head>" +
                "<body><h1>I have head</h1></body>" +
                "</html>")));
        assertThat(splitter.getDocuments()).hasSize(1);
        Document doc = splitter.getDocuments().get(0);
        assertThat(doc.getElementsByTagName("h1").getLength()).isEqualTo(1);

        assertThat(serialize(doc))
                .as("I just have fixed how it works de-facto. Seems to be an invalid result.")
                .isEqualTo("<body><head><title>The head</title></head><h1>I have head</h1></body>");
    }

    @Test
    public void splitDocumentWithMultipleBodies() throws Exception {
        reader.parse(new InputSource(new StringReader("<html>" +
                "<head><title>The head</title></head>" +
                "<body><h1>I have head</h1></body>" +
                "<body><h2>Second head</h2></body>" +
                "</html>")));

        assertThat(splitter.getDocuments()).hasSize(2);
        Document doc1 = splitter.getDocuments().get(0);
        assertThat(doc1.getElementsByTagName("h1").getLength()).isEqualTo(1);
        Document doc2 = splitter.getDocuments().get(1);
        assertThat(doc2.getElementsByTagName("h2").getLength()).isEqualTo(1);

        assertThat(serialize(doc1))
                .as("I just have fixed how it works de-facto. Seems to be an invalid result.")
                .isEqualTo("<body><head><title>The head</title></head><h1>I have head</h1></body>");
        assertThat(serialize(doc2))
                .as("I just have fixed how it works de-facto. Seems to be an invalid result.")
                .isEqualTo("<body><head><title>2&gt;Second</title></head><h2>Second head</h2></body>");
    }

    private static String serialize(Document document) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer serializer = factory.newTransformer();
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        DOMSource source = new DOMSource(document);
        OutputStream out = new ByteArrayOutputStream();
        StreamResult output = new StreamResult(out);
        serializer.transform(source, output);
        return out.toString();
    }
}
