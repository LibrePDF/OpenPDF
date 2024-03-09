/*
 * $Id: Events.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */
package com.lowagie.examples.directcontent.pageevents;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ElementTags;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.TextElementArray;
import com.lowagie.text.html.Markup;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.xml.SAXmyHandler;
import com.lowagie.text.xml.TagMap;
import com.lowagie.text.xml.XmlPeer;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EmptyStackException;
import java.util.Map;
import java.util.TreeSet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Example that takes an XML file, converts it to PDF and adds all kinds of extra's, such as an alternating header, a
 * footer with page x of y, a page with metadata,...
 */
public class Events {

    /**
     * Converts a play in XML into PDF.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Romeo and Juliet");

        // step 1: creation of a document-object
        Document document = new Document(PageSize.A4, 80, 50, 30, 65);

        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a XML-stream to a file
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream("RomeoJuliet.pdf"));

            // create add the event handler
            MyPageEvents events = new Events().getPageEvents();
            writer.setPageEvent(events);

            // step 3: we create a parser and set the document handler
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            SAXParser parser = factory.newSAXParser();

            // step 4: we parse the document
            parser.parse("playRomeoJuliet.xml", new Events().getXmlHandler(document));

            document.newPage();
            Speaker speaker;
            for (Object o : events.getSpeakers()) {
                speaker = (Speaker) o;
                document.add(new Paragraph(speaker.getName() + ": "
                        + speaker.getOccurrence() + " speech blocks"));
            }
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    /**
     * Gets a PageEvents object.
     *
     * @return a new PageEvents object
     */
    private MyPageEvents getPageEvents() {
        return new MyPageEvents();
    }

    /**
     * Gets a Handler object.
     *
     * @param document the document on which the handler operates
     * @return a Handler object
     */
    private MyHandler getXmlHandler(Document document) {
        try {
            return new MyHandler(document, new RomeoJulietMap());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This is an example of a PageEvents class you should write. This is an inner class to keep all the code of the
     * example in one file. If you want to use a PageEvent, you may want to put the code in a separate class.
     */

    class MyPageEvents extends PdfPageEventHelper {

        /**
         * we will keep a list of speakers
         */
        TreeSet<Speaker> speakers = new TreeSet<>();

        /**
         * This is the contentbyte object of the writer
         */
        PdfContentByte cb;

        /**
         * we will put the final number of pages in a template
         */
        PdfTemplate template;

        /**
         * this is the BaseFont we are going to use for the header / footer
         */
        BaseFont bf = null;

        /**
         * this is the current act of the play
         */
        String act = "";

        /**
         * Every speaker will be tagged, so that he can be added to the list of speakers.
         *
         * @see com.lowagie.text.pdf.PdfPageEventHelper#onGenericTag(com.lowagie.text.pdf.PdfWriter,
         * com.lowagie.text.Document, com.lowagie.text.Rectangle, java.lang.String)
         */
        public void onGenericTag(PdfWriter writer, Document document,
                Rectangle rect, String text) {
            speakers.add(new Speaker(text));
        }

        /**
         * The first thing to do when the document is opened, is to define the BaseFont, get the Direct Content object
         * and create the template that will hold the final number of pages.
         *
         * @see com.lowagie.text.pdf.PdfPageEventHelper#onOpenDocument(com.lowagie.text.pdf.PdfWriter,
         * com.lowagie.text.Document)
         */
        public void onOpenDocument(PdfWriter writer, Document document) {
            try {
                bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252,
                        BaseFont.NOT_EMBEDDED);
                cb = writer.getDirectContent();
                template = cb.createTemplate(50, 50);
            } catch (DocumentException | IOException ignored) {
            }
        }

        /**
         * Every ACT is seen as a Chapter. We get the title of the act, so that we can display it in the header.
         *
         * @see com.lowagie.text.pdf.PdfPageEventHelper#onChapter(com.lowagie.text.pdf.PdfWriter,
         * com.lowagie.text.Document, float, com.lowagie.text.Paragraph)
         */
        public void onChapter(PdfWriter writer, Document document,
                float paragraphPosition, Paragraph title) {
            StringBuilder buf = new StringBuilder();
            for (Object o : title.getChunks()) {
                Chunk chunk = (Chunk) o;
                buf.append(chunk.getContent());
            }
            act = buf.toString();
        }

        /**
         * After the content of the page is written, we put page X of Y at the bottom of the page and we add either
         * "Romeo and Juliet" of the title of the current act as a header.
         *
         * @see com.lowagie.text.pdf.PdfPageEventHelper#onEndPage(com.lowagie.text.pdf.PdfWriter,
         * com.lowagie.text.Document)
         */
        public void onEndPage(PdfWriter writer, Document document) {
            int pageN = writer.getPageNumber();
            String text = "Page " + pageN + " of ";
            float len = bf.getWidthPoint(text, 8);
            cb.beginText();
            cb.setFontAndSize(bf, 8);
            cb.setTextMatrix(280, 30);
            cb.showText(text);
            cb.endText();
            cb.addTemplate(template, 280 + len, 30);
            cb.beginText();
            cb.setFontAndSize(bf, 8);
            cb.setTextMatrix(280, 820);
            if (pageN % 2 == 1) {
                cb.showText("Romeo and Juliet");
            } else {
                cb.showText(act);
            }
            cb.endText();
        }

        /**
         * Just before the document is closed, we add the final number of pages to the template.
         *
         * @see com.lowagie.text.pdf.PdfPageEventHelper#onCloseDocument(com.lowagie.text.pdf.PdfWriter,
         * com.lowagie.text.Document)
         */
        public void onCloseDocument(PdfWriter writer, Document document) {
            template.beginText();
            template.setFontAndSize(bf, 8);
            template.showText(String.valueOf(writer.getPageNumber() - 1));
            template.endText();
        }

        /**
         * Getting the list of speakers.
         *
         * @return a list of speakers and the number of occurrences.
         */
        TreeSet getSpeakers() {
            return speakers;
        }
    }

    /**
     * Special implementation of the XML handler. It adds a paragraph after each SPEAKER block and avoids closing the
     * document after the final closing tag.
     */
    class MyHandler extends SAXmyHandler {

        /**
         * We have to override the constructor
         *
         * @param document the Document object
         * @param tagmap   the tagmap
         */
        MyHandler(Document document, Map<String, XmlPeer> tagmap) {
            super(document, tagmap);
        }

        /**
         * We only alter the handling of some endtags.
         *
         * @param uri   the uri of the namespace
         * @param lname the local name of the tag
         * @param name  the name of the tag
         */
        public void endElement(String uri, String lname, String name) {
            if (myTags.containsKey(name)) {
                XmlPeer peer = myTags.get(name);
                // we don't want the document to be close
                // because we are going to add a page after the xml is parsed
                if (isDocumentRoot(peer.getTag())) {
                    return;
                }
                handleEndingTags(peer.getTag());
                // we want to add a paragraph after the speaker chunk
                if ("SPEAKER".equals(name)) {
                    try {
                        TextElementArray previous = (TextElementArray) stack
                                .pop();
                        previous.add(new Paragraph(16));
                        stack.push(previous);
                    } catch (EmptyStackException ignored) {
                    }
                }
            } else {
                handleEndingTags(name);
            }
        }
    }

    /**
     * Normally you either choose to use a HashMap with XmlPeer objects, or a TagMap object that reads a TagMap in XML.
     * Here we used a hybrid solution (for educational purposes only!) with on one side the tags in the XML tagmap, on
     * the other side an XmlPeer object that overrides the properties of one of the tags.
     */

    class RomeoJulietMap extends TagMap {

        private static final long serialVersionUID = 1024517625414654121L;

        /**
         * Constructs a TagMap based on an XML file and/or on XmlPeer objects that are added.
         */
        RomeoJulietMap() throws IOException {
            super(new FileInputStream("tagmapRomeoJuliet.xml"));
            XmlPeer peer = new XmlPeer(ElementTags.CHUNK, "SPEAKER");
            peer.addValue(Markup.CSS_KEY_FONTSIZE, "10");
            peer.addValue(Markup.CSS_KEY_FONTWEIGHT, Markup.CSS_VALUE_BOLD);
            peer.addValue(ElementTags.GENERICTAG, "");
            put(peer.getAlias(), peer);
        }
    }

    /**
     * This object contains a speaker and a number of occurrences in the play
     */

    class Speaker implements Comparable {

        // name of the speaker
        private String name;

        // number of occurrences
        private int occurrence = 1;

        /**
         * One of the speakers in the play.
         */
        public Speaker(String name) {
            this.name = name;
        }

        /**
         * Gets the name of the speaker.
         *
         * @return a name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the number of occurrences of the speaker.
         *
         * @return a number of textblocks
         */
        int getOccurrence() {
            return occurrence;
        }

        /**
         * There is something odd going on in this compareTo. Do you see it?
         *
         * @param o an other speaker object
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o) {
            Speaker otherSpeaker = (Speaker) o;
            if (otherSpeaker.getName().equals(name)) {
                this.occurrence += otherSpeaker.getOccurrence();
                otherSpeaker.occurrence = this.occurrence;
                return 0;
            }
            return name.compareTo(otherSpeaker.getName());
        }
    }
}