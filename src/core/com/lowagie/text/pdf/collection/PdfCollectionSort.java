package com.lowagie.text.pdf.collection;

import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfBoolean;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.error_messages.MessageLocalization;

public class PdfCollectionSort extends PdfDictionary {
	
	/**
	 * Constructs a PDF Collection Sort Dictionary.
	 * @param key	the key of the field that will be used to sort entries
	 */
	public PdfCollectionSort(String key) {
		super(PdfName.COLLECTIONSORT);
		put(PdfName.S, new PdfName(key));
	}
	
	/**
	 * Constructs a PDF Collection Sort Dictionary.
	 * @param keys	the keys of the fields that will be used to sort entries
	 */
	public PdfCollectionSort(String[] keys) {
		super(PdfName.COLLECTIONSORT);
		PdfArray array = new PdfArray();
		for (int i = 0; i < keys.length; i++) {
			array.add(new PdfName(keys[i]));
		}
		put(PdfName.S, array);
	}
	
	/**
	 * Defines the sort order of the field (ascending or descending).
	 * @param ascending	true is the default, use false for descending order
	 */
	public void setSortOrder(boolean ascending) {
		PdfObject o = get(PdfName.S);
		if (o instanceof PdfName) {
			put(PdfName.A, new PdfBoolean(ascending));
		}
		else {
			throw new IllegalArgumentException(MessageLocalization.getComposedMessage("you.have.to.define.a.boolean.array.for.this.collection.sort.dictionary"));
		}
	}
	
	/**
	 * Defines the sort order of the field (ascending or descending).
	 * @param ascending	an array with every element corresponding with a name of a field.
	 */
	public void setSortOrder(boolean[] ascending) {
		PdfObject o = get(PdfName.S);
		if (o instanceof PdfArray) {
			if (((PdfArray)o).size() != ascending.length) {
				throw new IllegalArgumentException(MessageLocalization.getComposedMessage("the.number.of.booleans.in.this.array.doesn.t.correspond.with.the.number.of.fields"));
			}
			PdfArray array = new PdfArray();
			for (int i = 0; i < ascending.length; i++) {
				array.add(new PdfBoolean(ascending[i]));
			}
			put(PdfName.A, array);
		}
		else {
			throw new IllegalArgumentException(MessageLocalization.getComposedMessage("you.need.a.single.boolean.for.this.collection.sort.dictionary"));
		}
	}
	
	
}
