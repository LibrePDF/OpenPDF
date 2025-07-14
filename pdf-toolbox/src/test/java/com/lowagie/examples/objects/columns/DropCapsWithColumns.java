package com.lowagie.examples.objects.columns;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;

/**
 * Demonstrates the use of ColumnText to implement drop caps
 *
 * @author Matthias Luppi
 */
public class DropCapsWithColumns {

    /**
     * Demonstrating the use of ColumnText to implement drop caps
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Drop caps with ColumnText");

        final String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. In vestibulum, lorem ut "
                + "ultrices faucibus, lorem diam vestibulum elit, in finibus massa augue ut elit. In ac tortor vel "
                + "felis venenatis laoreet sed vitae ligula. Aliquam eu velit et lacus facilisis consequat. Interdum "
                + "et malesuada fames ac ante ipsum primis in faucibus. Maecenas sed pellentesque ante. Aliquam "
                + "convallis varius facilisis. Proin aliquet cursus neque ac tempor. Nunc iaculis pulvinar nibh, nec "
                + "dictum diam maximus sed. Vivamus quis semper velit. Erduce non enim enim. Pellentesque maximus "
                + "laoreet malesuada fames ac ante ipsum primis in faucibus. Maecenas sed pellentesque ante. Quisque "
                + "ultricies scelerisque quis dictum.";

        final float paragraphLeading = 16f;
        final Font paragraphFont = FontFactory.getFont(BaseFont.HELVETICA, 14f);
        final int dropCapLineHeight = 4;
        final float dropCapCorrectMarginX = 0f; // increases the indent of the text right to the drop cap
        final float dropCapCorrectMarginY = 0f; // increases drop cap bottom margin and reduces drop cap size
        final Font dropCapsFont = FontFactory.getFont(BaseFont.HELVETICA); // size will be calculated automatically
        final boolean showGuides = true; // flag to indicate whether graphic guides and bounding boxes should be shown

        // create a new document-object
        Document document = new Document(PageSize.A5.rotate(), 60, 60, 60, 60);
        try {
            // create a new writer to write the document to a file
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("ColumnTextDropCaps.pdf"));

            // open the document to add content to the body
            document.open();

            // get the direct content for this document
            final PdfContentByte cb = writer.getDirectContent();

            // determine the size of the drop cap
            final String dcCharacter = text.substring(0, 1);
            final float initialLinesHeight = dropCapLineHeight * paragraphLeading;
            final BaseFont dcBaseFont = dropCapsFont.getBaseFont();
            final int dcSizeNorm = dcBaseFont.getAscent(dcCharacter) - dcBaseFont.getDescent(dcCharacter);
            final float dcHeight =
                    initialLinesHeight - paragraphLeading + paragraphFont.getSize() - paragraphFont.getSize() / 4
                            - dropCapCorrectMarginY;
            final float dcFontSize = dcHeight / (dcSizeNorm * 0.001f);
            dropCapsFont.setSize(dcFontSize); // set font size depending on actual size of character
            final float dcWidth = dcBaseFont.getWidthPoint(dcCharacter, dcFontSize);

            // draw drop cap
            cb.beginText();
            cb.setFontAndSize(dcBaseFont, dcFontSize);
            cb.setTextMatrix(document.left(), document.top() - dcBaseFont.getAscentPoint(dcCharacter, dcFontSize));
            cb.showText(dcCharacter);
            cb.endText();

            // draw first part of paragraph text
            ColumnText ct = new ColumnText(cb);
            ct.setUseAscender(true);
            ct.setSimpleColumn(document.left() + dcWidth + paragraphFont.getSize() / 3 + dropCapCorrectMarginX,
                    document.top() - initialLinesHeight, document.right(), document.top());
            ct.setText(new Phrase(text.substring(1), paragraphFont));
            ct.setLeading(paragraphLeading);
            ct.setAlignment(Element.ALIGN_JUSTIFIED);
            ct.go();

            // draw second part of paragraph text
            ct.setSimpleColumn(document.left(), document.bottom(), document.right(),
                    document.top() - initialLinesHeight);
            ct.go(); // continuation need to be handled if text exceeds page

            // draw guides and bounding boxes if requested
            if (showGuides) {

                // lower bound of drop cap
                cb.setColorStroke(Color.GRAY);
                cb.moveTo(0, document.top() - dcHeight);
                cb.lineTo(document.getPageSize().getWidth(), document.top() - dcHeight);
                cb.stroke();

                // box of document
                cb.setColorStroke(Color.GREEN);
                cb.rectangle(document.left(), document.bottom(), document.right() - document.left(),
                        document.top() - document.bottom());
                cb.stroke();

                // box for upper text
                cb.setColorStroke(Color.RED);
                cb.rectangle(document.left() + dcWidth + paragraphFont.getSize() / 3 + dropCapCorrectMarginX,
                        document.top() - initialLinesHeight,
                        document.right() - document.left() - dcWidth - paragraphFont.getSize() / 3
                                - dropCapCorrectMarginX, initialLinesHeight);
                cb.stroke();

                // box for lower text
                cb.setColorStroke(Color.BLUE);
                cb.rectangle(document.left(), document.bottom(), document.right() - document.left(),
                        document.top() - document.bottom() - initialLinesHeight);
                cb.stroke();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // close the document
        document.close();
    }
}
