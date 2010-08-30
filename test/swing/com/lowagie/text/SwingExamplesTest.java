package com.lowagie.text;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Test;

public class SwingExamplesTest {

    public static void main(String args[]) throws Exception {
        SwingExamplesTest r = new SwingExamplesTest();
        r.testJTable2Pdf();
        r.testAwtImage();
    }
    
    public void runSingleTest(Class c, String... args) {
        try {
            Method m = c.getMethod("main", String[].class);
            m.invoke(null, new Object[] {args});
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test " + c.getName() + " failed: " + e.getCause());
        }
    }

    @Test
    public void testJTable2Pdf() {
        runSingleTest(com.lowagie.examples.objects.tables.alternatives.JTable2Pdf.class);
    }

    @Test
    public void testAwtImage() {
        runSingleTest(com.lowagie.examples.objects.images.AwtImage.class);
    }

}
