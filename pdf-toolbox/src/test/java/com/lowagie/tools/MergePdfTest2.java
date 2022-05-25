package com.lowagie.tools;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author SE_SUSTech, group: Lanrand
 * @version 1.0
 * @issue OpenPdf#737
 * @date 2022/5/25 17:23
 * <p> General MERGE PDF example. </p>
 */

@UtilityClass
public class MergePdfTest2 {
    /**
     * MergePdfTest2: Merge two only one-page PDFs into one PDF.
     */
    public static void main(final String[] args) throws IOException {
        final List files = new ArrayList();
        files.add(new File("./test-pdf/groups.pdf"));
        files.add(new File("./test-pdf/pattern.pdf"));
        files.add(new File("./test-pdf/templates.pdf"));
        files.add(new File("./test-pdf/layers.pdf"));
        MergePdf.mulFile2One(files, "./test-pdf/mergePdf2.pdf");
    }
}
