package com.lowagie.text.pdf;

import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import org.junit.jupiter.api.Test;

class PdfDocumentCJKExtensionTest {
	@Test
	void generateDocumentsWithCJKExtension() throws IOException {
		String fontName = "TakaoMjMincho";

		// TakaoMjMincho Version 003.01.01
		// Please download and place it below. 
		// https://launchpad.net/takao-fonts
		// https://launchpad.net/takao-fonts/trunk/15.03/+download/TakaoMjFonts_00301.01.zip
		String fontPath = "src/test/resources/fonts/TakaoMjFonts/TakaoMjMincho.ttf";

		// register font
		FontFactory.register(fontPath, fontName);

		Document document = new Document();

		// FOP off 
		document.setGlyphSubstitutionEnabled(false);

		PdfWriter.getInstance(document, new FileOutputStream("target/" + PdfDocumentCJKExtensionTest.class.getSimpleName() + ".pdf"));

		try {
			Font font = FontFactory.getFont(fontName, "Identity-H", false, 10, 0, null);

			document.open();
			// CJK Unified Ideographs Extension B
			// https://en.wikipedia.org/wiki/CJK_Unified_Ideographs_Extension_B

			// U+20000
			String cjkB_OK = "𠀀";
			// U+2A000 
			String cjkB_NG = "𪀀";


			// CJK Unified Ideographs Extension C
			// https://en.wikipedia.org/wiki/CJK_Unified_Ideographs_Extension_C

			// U+2A746
			String cjkC_NG = "𪝆";


			// CJK Unified Ideographs Extension D
			// https://en.wikipedia.org/wiki/CJK_Unified_Ideographs_Extension_D

			// U+2B746
			String cjkD_NG = "𫝆";

			document.add(new Chunk(cjkB_OK + " " + cjkB_NG + " " + cjkC_NG + " " + cjkD_NG, font));

		} finally {
			document.close();
		}
	}
}
