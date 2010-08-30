/*
 * Created on Oct 10, 2008
 * (c) 2008 Trumpet, Inc.
 *
 */
package com.itextpdf.text.pdf;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;

/**
 * @author kevin day, Trumpet, Inc.
 */
public class TestPdfCopyAndStamp {

    File base = new File(".");
    File[] in;
    File stamp;
    File multiPageStamp;
    File out;
    
    private void createTempFile(String filename, String[] pageContents) throws Exception{
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        
        for (int i = 0; i < pageContents.length; i++) {
            if (i != 0)
                document.newPage();

            String content = pageContents[i];
            Chunk contentChunk = new Chunk(content);
            document.add(contentChunk);
        }
        
        
        document.close();
    }
    
    private void cleanTempFiles(){
        for (int i = 0; i < in.length; i++) {
            File f = in[i];
            
            if (f.exists() && !f.delete())
                fail("Unable to delete temp content " + f + " prior to running test");
        }
        
        if (stamp.exists() && !stamp.delete())
            fail("Unable to delete stamp file " + stamp + " prior to running test");
        
        if (multiPageStamp.exists() && !multiPageStamp.delete())
            fail("Unable to delete multi stamp file " + multiPageStamp + " prior to running test");
        
//        if (out.exists() && !out.delete())
//            fail("Unable to delete output file " + out + " prior to running test");
        
    }
    
    @Before
    public void setUp() throws Exception {
        
        in = new File[]{
                new File(base, "content1.pdf"),
                new File(base, "content2.pdf"),
                };
        
        stamp = new File(base, "Stamp.PDF");
        multiPageStamp = new File(base, "MultiStamp.PDF");
        out = new File(base, "test.pdf");

        cleanTempFiles();
        
        createTempFile(in[0].getCanonicalPath(), new String[]{"content 1"});
        createTempFile(in[1].getCanonicalPath(), new String[]{"content 2"});

        createTempFile(stamp.getCanonicalPath(), new String[]{"          This is a stamp"});
        createTempFile(multiPageStamp.getCanonicalPath(), new String[]{"          This is a stamp - page 1", "          This is a stamp - page 2"});
    }

    @After
    public void tearDown() throws Exception {
        cleanTempFiles();
    }

    public void mergeAndStampPdf(boolean resetStampEachPage, File[] in, File out, File stamp) throws Exception {
        Document document = new Document();
        
        PdfCopy writer = new PdfSmartCopy(document, new FileOutputStream(out));
        
        document.open();
        
        int stampPageNum = 1;

        PdfReader stampReader = new PdfReader(stamp.getPath());
        for (int inNum = 0; inNum < in.length; inNum++){
            // create a reader for the input document
            PdfReader documentReader = new PdfReader(in[inNum].getPath());
            
            for (int pageNum = 1; pageNum <= documentReader.getNumberOfPages(); pageNum++){
            
                // import a page from the main file
                PdfImportedPage mainPage = writer.getImportedPage(documentReader, pageNum);
        
                // make a stamp from the page and get under content...
                PdfCopy.PageStamp pageStamp = writer.createPageStamp(mainPage);
         
                // import a page from a file with the stamp...
                if (resetStampEachPage)
                    stampReader = new PdfReader(stamp.getPath());
                PdfImportedPage stampPage = writer.getImportedPage(stampReader, stampPageNum++);
        
                // add the stamp template, update stamp, and add the page
                pageStamp.getOverContent().addTemplate(stampPage, 0, 0);
                pageStamp.alterContents();
                writer.addPage(mainPage);
                
                if (stampPageNum > stampReader.getNumberOfPages())
                    stampPageNum = 1;
            }
        }        
        
        writer.close(); 
        document.close();
    }
    
    protected void testXObject(boolean shouldExist, int page, String xObjectName) throws Exception{
        PdfReader reader = null;
        RandomAccessFileOrArray raf = null;
        raf = new RandomAccessFileOrArray(out.getCanonicalPath());
        reader = new PdfReader(raf, null);
        try{
            PdfDictionary dictionary = reader.getPageN(page);
            
            PdfDictionary resources = (PdfDictionary)dictionary.get(PdfName.RESOURCES);
            PdfDictionary xobject = (PdfDictionary)resources.get(PdfName.XOBJECT);
            PdfObject directXObject = xobject.getDirectObject(new PdfName(xObjectName));
            PdfObject indirectXObject = xobject.get(new PdfName(xObjectName));
            
            if (shouldExist){
                assertNotNull(indirectXObject);
                assertNotNull(directXObject);
            } else {
                assertNull(indirectXObject);
                assertNull(directXObject);
            }
        } finally {        
            reader.close();
        }
        
        
    }
    
    @Test
    public void testWithReloadingStampReader() throws Exception{
        mergeAndStampPdf(true, in, out, stamp);

        testXObject(true, 1, "Xi0");
        testXObject(true, 2, "Xi1");
        
    }

    @Ignore
    @Test
    public void testWithoutReloadingStampReader() throws Exception{
        mergeAndStampPdf(false, in, out, stamp);

        //openFile(out); // if you open the resultant PDF at this point and go to page 2, you will get a nice error message
        
        testXObject(true, 1, "Xi0");
        testXObject(true, 2, "Xi1"); // if we are able to optimize iText so it re-uses the same XObject for multiple imports of the same page from the same PdfReader, then switch this to false
        
    }

    @Ignore
    @Test
    public void testMultiPageStampWithoutReloadingStampReader() throws Exception{
        mergeAndStampPdf(false, in, out, multiPageStamp);

        // openFile(out); // if you open the resultant PDF at this point and go to page 2, you will get a nice error message
        
        testXObject(true, 1, "Xi0");
        testXObject(true, 2, "Xi1");
        
    }

    @Test
    public void testMultiPageStampWithReloadingStampReader() throws Exception{
        mergeAndStampPdf(true, in, out, multiPageStamp);

        // openFile(out); // if you open the resultant PDF at this point and go to page 2, you will get a nice error message
        
        testXObject(true, 1, "Xi0");
        testXObject(true, 2, "Xi1");
        
    }

    
//    private void openFile(File f) throws IOException{
//        String[] params = new String[]{
//                "rundll32",
//                "url.dll,FileProtocolHandler",
//                "\"" + f.getCanonicalPath() + "\""
//        };
//        Runtime.getRuntime().exec(params); 
//    }

}
