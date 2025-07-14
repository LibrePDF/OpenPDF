package org.openpdf.pdf;

import org.openpdf.text.DocumentException;
import org.w3c.dom.Document;
import org.openpdf.resource.FSEntityResolver;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

import static java.util.Objects.requireNonNull;

public class Html2Pdf {
    public static byte[] fromClasspathResource(String fileName) {
        URL htmlUrl = requireNonNull(Thread.currentThread().getContextClassLoader().getResource(fileName),
                () -> "Resource not found in classpath: " + fileName);
        return fromUrl(htmlUrl);
    }

    public static byte[] fromUrl(URL html) {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.getSharedContext().setInteractive(false);
        renderer.getSharedContext().getTextRenderer().setSmoothingThreshold(0);

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.setEntityResolver(FSEntityResolver.instance());

            Document doc = builder.parse(html.toString());
            return renderer.createPDF(doc);
        }
        catch (DocumentException | IOException | SAXException | ParserConfigurationException e) {
            throw new IllegalArgumentException("Failed to parse XML from " + html, e);
        }
    }
}
