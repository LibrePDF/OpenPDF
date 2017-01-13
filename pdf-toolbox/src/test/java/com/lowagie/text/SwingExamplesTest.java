package com.lowagie.text;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Ignore;
import org.junit.Test;

@Ignore  //ignored until we can fix headless GUI on Travis CI.
public class SwingExamplesTest {

    public static void main(String args[]) throws Exception {
        SwingExamplesTest r = new SwingExamplesTest();
        r.testJTable2Pdf();
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

}
