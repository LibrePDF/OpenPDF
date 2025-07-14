package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LayoutProcessor534Test {

    @BeforeAll
    static void beforeAll() {
        assumeThat(LayoutProcessor.isSet(Font.LAYOUT_RIGHT_TO_LEFT)).isFalse();
        LayoutProcessor.enable(java.awt.Font.LAYOUT_RIGHT_TO_LEFT);
    }

    @AfterAll
    static void afterAll() {
        LayoutProcessor.disable();
    }

    @BeforeEach
    void beforeEach() {
        assumeThat(LayoutProcessor.isSet(Font.LAYOUT_RIGHT_TO_LEFT)).isTrue();
    }

    @Test
    void whenLayoutRightToLeftLatinThenRevertCharOrder() throws IOException {
        // given
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        document.open();
        // when
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
        String textFromPage = extractor.getTextFromPage(1);
        assertThat(textFromPage)
                .isEqualTo("owTytnewt enOεytnewt ytnewt neetenin neethgie neetneves neetxis neetfif neetruof neetriht "
                        + "evlewt nevele net enin thgie neves xis evif ruof eerht owt eno\n"
                        + " evlewt nevele net enin thgie neves xis evif ruof eerht owt eno ytriht eniNytnewt thgiEytnewt "
                        + "neveSytnewt xiSytnewt eviFytnewt ruoFytnewt eerhTytnewt\n"
                        + " neveSytnewt xiSytnewt eviFytnewt ruoFytnewt eerhTytnewt owTytnewt enOεytnewt ytnewt neetenin n"
                        + "eethgie neetneves neetxis neetfif neetruof neetriht\n"
                        + " ytriht eniNytnewt thgiEytnewt");
    }

    @Test
    void whenLayoutRightToLeftHebrewThenRevertCharOrder() throws IOException {
        // given
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        document.open();
        // when
        String text = "שוב היתה זו שעת לילה. דממה שררה בפונדק אבן־הדרך, והיתה זו דממה בת שלושה חלקים." +
                "החלק המתבקש מאליו היה שקט חלול, מהדהד, עשוי מן הדברים שלא היו. אילו היתה רוח, כי" +
                "אז היתה נאנחת בעוברה בין העצים, מטלטלת את שלט הפונדק בחריקה על ציריו וסוחפת את" +
                "הדממה במורד הדרך, כפי שהיא גורפת עלי סתיו. אילו היה קהל בפונדק, אפילו קומץ אנשים, כי" +
                "אז היו ממלאים את הדממה בשיחה ובצחוק, בהמולה ובשאון שהיה מקום לצַפות להם במסבאה," +
                "בשעות הלילה החשוכות. אילו היתה מוסיקה... אבל לא , ודאי שלא היתה מוסיקה. למען האמת, אף" +
                "לא אחד מהדברים האלה היה שם, ולכן נותרה הדממה בעינה.";

        Paragraph paragraph = new Paragraph(new Chunk(text));
        paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(paragraph);
        document.close();

        // then
        byte[] pdfBytes = pdfOut.toByteArray();
        PdfTextExtractor extractor = new PdfTextExtractor(new PdfReader(pdfBytes));
        String expected = ".ויה אלש םירבדה ןמ יושע ,דהדהמ ,לולח טקש היה וילאמ"
                + " שקבתמה קלחה.םיקלח השולש תב הממד וז התיהו ,ךרדה־ןבא קדנופב הררש הממד .הליל תעש וז התיה בוש\n"
                + " וליא .ויתס ילע תפרוג איהש יפכ ,ךרדה דרומב הממדהתא"
                + " תפחוסו ויריצ לע הקירחב קדנופה טלש תא תלטלטמ ,םיצעה ןיב הרבועב תחנאנ התיה זאיכ ,חור התיה וליא\n"
                + " וליא .תוכושחה הלילה תועשב,האבסמב םהל תופַצל"
                + " םוקמ היהש ןואשבו הלומהב ,קוחצבו החישב הממדה תא םיאלממ ויה זאיכ ,םישנא ץמוק וליפא ,קדנופב להק היה\n"
                + " .הניעב הממדה"
                + " הרתונ ןכלו ,םש היה הלאה םירבדהמ דחא אלףא ,תמאה ןעמל .הקיסומ התיה אלש יאדו , אל לבא ...הקיסומ התיה";
        assertThat(extractor.getTextFromPage(1))
                .isEqualTo(expected);
    }

}
