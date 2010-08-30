package com.itextpdf.text.pdf;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.itextpdf.testutils.TestResourceUtils;

public class AcroFieldsTest {
    
    @Test
    public void testSetFields() throws Exception {
        singleTest("register.xfdf");
    }

    @Test
    public void testListInSetFields() throws Exception {
        singleTest("list_register.xfdf");
    }
    
    private void singleTest(String xfdfFileName) throws Exception {
        // merging the FDF file
        PdfReader pdfreader = TestResourceUtils.getResourceAsPdfReader(this, "SimpleRegistrationForm.pdf");
        PdfStamper stamp = new PdfStamper(pdfreader, new ByteArrayOutputStream());
        XfdfReader fdfreader = new XfdfReader(xfdfFileName);
        AcroFields form = stamp.getAcroFields();
        form.setFields(fdfreader);
        stamp.close();
    }
}
