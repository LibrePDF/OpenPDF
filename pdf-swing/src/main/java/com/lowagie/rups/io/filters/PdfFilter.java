package com.lowagie.rups.io.filters;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 * Filters PDF files in a JFileChooser.
 */
public class PdfFilter extends FileFilter {

    /**
     * A public instance of the PdfFilter.
     */
    public static final PdfFilter INSTANCE = new PdfFilter();

    /**
     * @param f File
     * @return boolean
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        return f.getName().toLowerCase().endsWith(".pdf");
    }

    /**
     * @return String
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    public String getDescription() {
        return "*.pdf PDF files";
    }

}
