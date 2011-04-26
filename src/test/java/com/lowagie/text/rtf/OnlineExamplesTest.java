package com.lowagie.text.rtf;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Test;

public class OnlineExamplesTest {

    public static void main(String args[]) throws Exception {       
        OnlineExamplesTest r = new OnlineExamplesTest();

        r.testRtfExamples();
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
    public void testRtfExamples() {
        runSingleTest(com.lowagie.examples.general.HelloWorldMultiple.class);
        runSingleTest(com.lowagie.examples.objects.tables.alternatives.TablePdfPTable.class);
        runSingleTest(com.lowagie.examples.fonts.styles.ExtraStyles.class);

        runSingleTest(com.lowagie.examples.rtf.HelloWorld.class);
        runSingleTest(com.lowagie.examples.rtf.extensions.hf.ExtendedHeaderFooter.class);
        runSingleTest(com.lowagie.examples.rtf.extensions.hf.ChapterHeaderFooter.class);
        runSingleTest(com.lowagie.examples.rtf.extensions.hf.MultipleHeaderFooter.class);
        runSingleTest(com.lowagie.examples.rtf.extensions.table.ExtendedTableCell.class);
        runSingleTest(com.lowagie.examples.rtf.extensions.font.ExtendedFontStyles.class);
        runSingleTest(com.lowagie.examples.rtf.extensions.font.ExtendedFont.class);
        runSingleTest(com.lowagie.examples.rtf.features.pagenumber.PageNumber.class);
        runSingleTest(com.lowagie.examples.rtf.features.pagenumber.TotalPageNumber.class);
        runSingleTest(com.lowagie.examples.rtf.features.shape.DrawingFreeform.class);
        runSingleTest(com.lowagie.examples.rtf.features.shape.DrawingAnchor.class);
        runSingleTest(com.lowagie.examples.rtf.features.shape.DrawingObjects.class);
        runSingleTest(com.lowagie.examples.rtf.features.shape.DrawingText.class);
        runSingleTest(com.lowagie.examples.rtf.features.shape.DrawingWrap.class);
        runSingleTest(com.lowagie.examples.rtf.features.toc.TableOfContents.class);
        runSingleTest(com.lowagie.examples.rtf.features.tabs.BasicTabs.class);
        runSingleTest(com.lowagie.examples.rtf.features.tabs.TabGroups.class);
        runSingleTest(com.lowagie.examples.rtf.features.styles.BasicStylesheets.class);
        runSingleTest(com.lowagie.examples.rtf.features.styles.ChangingStylesheets.class);
        runSingleTest(com.lowagie.examples.rtf.features.styles.ExtendingStylesheets.class);
        runSingleTest(com.lowagie.examples.rtf.features.direct.SoftLineBreak.class);
        runSingleTest(com.lowagie.examples.rtf.RtfTest.class);
        runSingleTest(com.lowagie.examples.rtf.documentsettings.DocumentSettings.class);
        runSingleTest(com.lowagie.examples.rtf.RtfTOCandCellborders.class);
    }

}
