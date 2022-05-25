package com.lowagie.tools;

import lombok.experimental.UtilityClass;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author YouYu, SE_SUSTech, group: Lanrand
 * @version 1.0
 * @date 2022/5/25 13:42
 */
@UtilityClass
public class MergePdf {

    /**
     * @param files      File list
     * @param targetPath
     * @return
     * @throws IOException
     * @Title:mulFile2One
     * @Description: pdf merge together TODO: Merge multiple PDFs into one PDF.
     */
    public static File mulFile2One(final List<File> files, final String targetPath) throws IOException {
        // PDF merge utility class
        final PDFMergerUtility mergePdf = new PDFMergerUtility();
        for (final File f : files) {
            if (f.exists() && f.isFile()) {
                // Loop to add the PDF to merge
                mergePdf.addSource(f);
            }
        }
        // Set the name of the generated PDF file
        mergePdf.setDestinationFileName(targetPath);
        // Merged PDF
        mergePdf.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        return new File(targetPath);
    }
}


