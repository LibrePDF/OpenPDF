/*
 * $Id: ChineseJapaneseKorean.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.fonts.getting;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Using CJK Fonts.
 */
public class ChineseJapaneseKorean {

    /**
     * Using CJK fonts
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("CJK Fonts");

        // step 1: creation of a document-object
        Document document = new Document();
        try {

            // step 2: creation of the writer
            PdfWriter.getInstance(document, new FileOutputStream("cjk.pdf"));

            // step 3: we open the document
            document.open();
            String chinese = "\u53d6\u6e96\u53d7\u4fdd\u4eba\u5728\u6211\u56fd\u7ecf\u6d4e\u7ed3\u6784"
                    + "\u8fdb\u884c\u6218\u7565\u6027\u8c03\u6574\u7684"
                    + "\u80CC\u666F\u4e0B\uff0c\u4FE1\u606f\u4ea7\u4e1a"
                    + "\u5c06\u6210\u4E3A\u62c9\u52A8\u7ecf\u6d4e\u589e"
                    + "\u957f\u7684\u65b0\u52A8\u529b\uff0c\u800c\u4f5c"
                    + "\u4E3A\u4FE1\u606f\u6280\u672f\u6838\u5fc3\u4e4b"
                    + "\u4e00\u7684\u8f6f\u4ef6\u4ea7\u4e1a\u5fc5\u7136"
                    + "\u6210\u4E3A\u4e4a\u540e\u56fd\u5bb6\u4ea7\u4e1a"
                    + "\u54d1\u5c55\u7684\u6218\u7565\u91cd\u70b9\uff0c"
                    + "\u6211\u56fd\u7684\u8f6f\u4ef6\u4ea7\u4e1a\u5c06"
                    + "\u4f1a\u5f97\u5230\u98de\u901f\u7684\u54d1\u5c55"
                    + "\u3002\u540c\u65f6\u4F34\u968f\u7740\u6211\u56fd"
                    + "\u52a0\u5165\u4e16\u8d38\u7ec4\u7ec7\uff0c\u8de8"
                    + "\u56fd\u4f01\u4e1a\u4e5f\u5c06\u5927\u89c4\uc4a3"
                    + "\u8fdb\u9a7b\u4e2d\u56fd\u8f6f\u4ef6\ucad0\u573a"
                    + "\u3002\u56e0\u6b64\uff0c\u9ad8\u7d20\u8d28\u5f0c"
                    + "\u4ef6\u4eba\u624d\u7684\u7ade\u4e89\ucac6\u5fc5"
                    + "\uc8d5\uc7f7\ubca4\uc1d2\u3002\u800c\u4e4b\u6b64"
                    + "\u7678\u5bf9\u5e94\u7684\u662f\u76ee\u524d\u6211"
                    + "\u56fd\u5bf9\u5f0c\u4ef6\u4eba\u624d\u57f9\u517b"
                    + "\u7684\u529b\u5ea6\u4e0d\u591f\uff0c\u6240\u80fd"
                    + "\u63d0\u4f9b\u4e13\u4e1a\u4eba\u624d\u8fdc\u8fdc"
                    + "\u6ee1\u8db3\u4e0d\u4e86\u5e02\u573a\u7684\u9700"
                    + "\u6c42\u3002\u56e0\u6b64\u4ee5\u5e02\u573a\u9700"
                    + "\u6c42\u4E3A\u7740\u773c\u70b9\uff0c\u6539\u9769"
                    + "\u5f0c\u4ef6\u4eba\u624d\u57f9\u517b\u7684\u6a21"
                    + "\u5f0f\uff0c\u52a0\u5927\u5f0c\u4ef6\u4eba\u624d"
                    + "\u57f9\u517b\u7684\u529b\u5ea6\uff0c\u5b9e\u73b0"
                    + "\u6211\u56fd\u5f0c\u4ef6\u4eba\u624d\u57f9\u517b"
                    + "\u7684\u5d88\u8d8a\u5f0f\u54d1\u5c55\uff0c\u5df2"
                    + "\u7ecf\u6210\u4E3A\u5f53\u524d\u6c0a\u7b49\u6559"
                    + "\u80b2\u6539\u9769\u4e0e\u54d1\u5c55\u7684\u4e00"
                    + "\u9879\u6781\u5176\u91cd\u8981\u800c\u4e14\u7678"
                    + "\u5f53\u7d27\u8feb\u7684\u4efb\u52a1\u3002";

            // step 4: we add content to the document
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font FontChinese = new Font(bfChinese, 12, Font.NORMAL);
            Paragraph p = new Paragraph(chinese, FontChinese);
            document.add(p);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}
