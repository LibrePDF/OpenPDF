package com.lowagie.text.pdf.collection;

import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfString;

public class PdfTargetDictionary extends PdfDictionary {
	
	/**
	 * Creates dictionary referring to a target document that is the parent of the current document.
	 * @param nested	null if this is the actual target, another target if this is only an intermediate target.
	 */
	public PdfTargetDictionary(PdfTargetDictionary nested) {
		super();
		put(PdfName.R, PdfName.P);
		if (nested != null)
			setAdditionalPath(nested);
	}
	
	/**
	 * Creates a dictionary referring to a target document.
	 * @param child	if false, this refers to the parent document; if true, this refers to a child document, and you'll have to specify where to find the child using the other methods of this class
	 */
	public PdfTargetDictionary(boolean child) {
		super();
		if (child) {
			put(PdfName.R, PdfName.C);
		}
		else {
			put(PdfName.R, PdfName.P);
		}
	}
	
	/**
	 * If this dictionary refers to a child that is a document level attachment,
	 * you need to specify the name that was used to attach the document.
	 * @param	target	the name in the EmbeddedFiles name tree
	 */
	public void setEmbeddedFileName(String target) {
		put(PdfName.N, new PdfString(target, null));
	}
	
	/**
	 * If this dictionary refers to a child that is a file attachment added to a page,
	 * you need to specify the name of the page (or use setFileAttachmentPage to specify the page number).
	 * Once you have specified the page, you still need to specify the attachment using another method.
	 * @param name	the named destination referring to the page with the file attachment.
	 */
	public void setFileAttachmentPagename(String name) {
		put(PdfName.P, new PdfString(name, null));
	}
	
	/**
	 * If this dictionary refers to a child that is a file attachment added to a page,
	 * you need to specify the page number (or use setFileAttachmentPagename to specify a named destination).
	 * Once you have specified the page, you still need to specify the attachment using another method.
	 * @param page	the page number of the page with the file attachment.
	 */
	public void setFileAttachmentPage(int page) {
		put(PdfName.P, new PdfNumber(page));
	}
	
	/**
	 * If this dictionary refers to a child that is a file attachment added to a page,
	 * you need to specify the page with setFileAttachmentPage or setFileAttachmentPageName,
	 * and then specify the name of the attachment added to this page (or use setFileAttachmentIndex).
	 * @param name		the name of the attachment
	 */
	public void setFileAttachmentName(String name) {
		put(PdfName.A, new PdfString(name, PdfObject.TEXT_UNICODE));
	}
	
	/**
	 * If this dictionary refers to a child that is a file attachment added to a page,
	 * you need to specify the page with setFileAttachmentPage or setFileAttachmentPageName,
	 * and then specify the index of the attachment added to this page (or use setFileAttachmentName).
	 * @param annotation		the number of the attachment
	 */
	public void setFileAttachmentIndex(int annotation) {
		put(PdfName.A, new PdfNumber(annotation));
	}
	
	/**
	 * If this dictionary refers to an intermediate target, you can
	 * add the next target in the sequence.
	 * @param nested	the next target in the sequence
	 */
	public void setAdditionalPath(PdfTargetDictionary nested) {
		put(PdfName.T, nested);
	}
}
