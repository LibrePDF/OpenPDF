package org.librepdf.openpdf.examples.fonts.languages;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.plugins.watermarker.Watermarker;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Chinese {

    public static void main(String[] args) throws IOException {
        // step 0: prepare font with chinese symbols
        // Downloaded from:
        // https://github.com/adobe-fonts/source-han-serif/blob/release/OTF/SimplifiedChinese/SourceHanSerifSC-Regular.otf
        final String fontFile = "SourceHanSerifSC-Regular.otf";
        BaseFont baseFont = BaseFont
                .createFont(fontFile, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        final Font chineseFont = new Font(baseFont, 12, Font.NORMAL);
        // step 1: Prepare document for chinese text
        Document document = new Document();
        ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOutput);
        document.open();
        // step 2: we add content to the document
        document.add(new Chunk("Chinese Poetry: 中文", chineseFont));
        document.add(new Paragraph("李白《赠汪伦》", chineseFont));
        document.add(new Paragraph("李白乘舟将欲行，", chineseFont));
        document.add(new Paragraph("忽闻岸上踏歌声。", chineseFont));
        document.add(new Paragraph("桃花潭水深千尺，", chineseFont));
        document.add(new Paragraph("不及汪伦送我行。", chineseFont));
        // step 3: we close the document
        document.close();

        // step 4: (optional) some watermark with chinese text
        String watermark = "水印 (Watermark)";
        final byte[] pdfBytesWithWaterMark =
                new Watermarker(pdfOutput.toByteArray(), watermark, 64, 0.3f)
                        .withColor(Color.RED)
                        .withFont(chineseFont.getBaseFont())
                        .write();

        // step 5: write the output to a file
        final Path target = Paths.get(Chinese.class.getSimpleName() + ".pdf");
        Files.write(target, pdfBytesWithWaterMark);
    }
}
