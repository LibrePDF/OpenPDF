package com.lowagie.text;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled  //ignored until we can fix headless GUI on Travis CI.
class SwingExamplesTest {

    static void main(String[] args) {
        SwingExamplesTest r = new SwingExamplesTest();
        r.testJTable2Pdf();
    }
    
    void runSingleTest(Class c, String... args) {
        try {
            Method m = c.getMethod("main", String[].class);
            m.invoke(null, new Object[] {args});
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test " + c.getName() + " failed: " + e.getCause());
        }
    }

    @Test
    void testJTable2Pdf() {
        runSingleTest(com.lowagie.examples.objects.tables.alternatives.JTable2Pdf.class);
    }

}
