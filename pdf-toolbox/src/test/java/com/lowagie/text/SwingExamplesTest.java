package com.lowagie.text;

import static org.junit.jupiter.api.Assertions.fail;

import com.lowagie.examples.objects.tables.alternatives.JTable2Pdf;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled  //ignored until we can fix headless GUI on Travis CI.
class SwingExamplesTest {

    static void main(String[] args) {
        SwingExamplesTest r = new SwingExamplesTest();
        r.testJTable2Pdf();
    }

    void runSingleTest(String... args) {
        try {
            JTable2Pdf.main(args);
        } catch (Exception e) {
            fail("Test " + JTable2Pdf.class.getName() + " failed: " + e.getCause());
        }
    }

    @Test
    void testJTable2Pdf() {
        runSingleTest();
    }

}
