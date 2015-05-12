package com.lowagie.rups.io.filters;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * Filters PDF files in a JFileChooser.
 */
public class PdfFilter extends FileFilter {

	/** A public instance of the PdfFilter. */
	public static final PdfFilter INSTANCE = new PdfFilter();
	
    /**
     *
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     * @param f File
     * @return boolean
     */
    public boolean accept(File f) {
		if (f.isDirectory()) return true;
		if (f.getName().toLowerCase().endsWith(".pdf")) return true;
		return false;
	}

    /**
     *
     * @see javax.swing.filechooser.FileFilter#getDescription()
     * @return String
     */
    public String getDescription() {
		return "*.pdf PDF files";
	}

}
