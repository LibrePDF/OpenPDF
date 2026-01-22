package org.openpdf.text.pdf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;


class FopGlyphProcessorTest {

    private static final Log log = LogFactory.getLog(FopGlyphProcessorTest.class);

    @Test
    void textCreateDocument() throws IOException {

        URL fontUrl = FopGlyphProcessorTest.class.getResource("/fonts/Sarabun/Sarabun-Regular.ttf");
        assertThat(fontUrl).isNotNull();

        try (Document document = new Document(PageSize.A4)) {
            var tmp = Files.createTempFile("ThaiFopTest", ".pdf");

            FileOutputStream os = new FileOutputStream(tmp.toFile());
            PdfWriter.getInstance(document, os);
            document.open();

            BaseFont baseFont = BaseFont.createFont(fontUrl.toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font thaiFont = new Font(baseFont, 16);

            // 3. เขียนเนื้อหาทดสอบ
            document.add(new Paragraph("คำทดสอบ: จำ ทำ น้ำ ต่ำ ก้ำ", thaiFont));

            String longText = "Lorem Ipsum คือ เนื้อหาจำลองที่ใช้กันในธุรกิจงานพิมพ์หรืองานเรียงพิมพ์ " +
                    "มันได้กลายมาเป็นเนื้อหาจำลองมาตรฐานของธุรกิจดังกล่าวมาตั้งแต่ศตวรรษที่ 16";
            document.add(new Paragraph(longText, thaiFont));

            FopGlyphProcessorTest.log.info("Save to: " + tmp.toAbsolutePath());
        }
    }

}
