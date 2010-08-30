package com.itextpdf.text.pdf;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.itextpdf.testutils.TestResourceUtils;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;

public class PdfReaderTest {

    @Test
    public void testPartialReadOpenFile() throws Exception {
        /* commit 3265 incorrectly closed the input stream, make sure
         * the constructor contract is kept, i.e. file is still open
         */
        File testFile = TestResourceUtils.getResourceAsTempFile(this, "RomeoJuliet.pdf");
        RandomAccessFileOrArray f = new RandomAccessFileOrArray(testFile.getAbsolutePath());
        PdfReader r = new PdfReader(f, null);

        assertTrue("kept open", f.isOpen());
    }

    @Ignore("validity of test needs to be resolved")
    @Test
    public void testGetLink() throws Exception {
        File testFile = TestResourceUtils.getResourceAsTempFile(this, "getLinkTest1.pdf");
        PdfReader currentReader = new PdfReader(testFile.getAbsolutePath());
        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
        PdfWriter writer = PdfWriter.getInstance(document, new
                ByteArrayOutputStream());
        document.open();
        document.newPage();
        List links = currentReader.getLinks(1);
        PdfAnnotation.PdfImportedLink link =
            (PdfAnnotation.PdfImportedLink) links.get(0);
        writer.addAnnotation(link.createAnnotation(writer));
        document.close();
    }

    @Test
    public void testGetLink2() throws Exception {
        File testFile = TestResourceUtils.getResourceAsTempFile(this, "getLinkTest2.pdf");
        String filename = testFile.getAbsolutePath();
        PdfReader rdr = new PdfReader(new RandomAccessFileOrArray(filename), new byte[0]);
        // this one works: PdfReader rdr = new PdfReader(filename);
        rdr.consolidateNamedDestinations(); // does not help
        rdr.getLinks(1);
    }

}
