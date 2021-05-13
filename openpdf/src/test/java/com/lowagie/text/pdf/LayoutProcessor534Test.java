package com.lowagie.text.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class LayoutProcessor534Test {

    @Test
    public void whenLayoutRightToLeft_thenRevertCharOrder() throws IOException {
        // given
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        document.open();
        // when
        LayoutProcessor.enable(java.awt.Font.LAYOUT_RIGHT_TO_LEFT);
        String text = "one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen "
            + "sixteen seventeen eighteen nineteen twenty twentyεOne twentyTwo twentyThree twentyFour twentyFive "
            + "twentySix twentySeven twentyEight twentyNine thirty ";
        text = text + text;

        Paragraph paragraph = new Paragraph(new Chunk(text));
        paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(paragraph);
        document.close();

        // then
        PdfTextExtractor extractor = new PdfTextExtractor(new PdfReader(pdfOut.toByteArray()));
        Assertions.assertThat(extractor.getTextFromPage(1))
            .isEqualTo("owTytnewt enOεytnewt ytnewt neetenin neethgie neetneves neetxis neetfif neetruof neetriht "
                + "evlewt nevele net enin thgie neves xis evif ruof eerht owt eno\n"
                + " evlewt nevele net enin thgie neves xis evif ruof eerht owt eno ytriht eniNytnewt thgiEytnewt "
                + "neveSytnewt xiSytnewt eviFytnewt ruoFytnewt eerhTytnewt\n"
                + " neveSytnewt xiSytnewt eviFytnewt ruoFytnewt eerhTytnewt owTytnewt enOεytnewt ytnewt neetenin n"
                + "eethgie neetneves neetxis neetfif neetruof neetriht\n"
                + " ytriht eniNytnewt thgiEytnewt");
    }

}
