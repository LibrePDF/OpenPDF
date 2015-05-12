/*
 * $Id: PdfObjectTreeNode.java 3693 2009-02-13 17:37:31Z xlv $
 *
 * Copyright 2007 Bruno Lowagie.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.lowagie.rups.view.itext.treenodes;

import com.lowagie.rups.view.icons.IconFetcher;
import com.lowagie.rups.view.icons.IconTreeNode;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfIndirectReference;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfString;

/**
 * Every node in our tree corresponds with a PDF object.
 * This class is the superclass of all tree nodes used.
 */
public class PdfObjectTreeNode extends IconTreeNode {

	/** the PDF object corresponding with this node. */
	protected PdfObject object;
    /** the key if the parent of this node is a dictionary. */
	protected PdfName key = null;
	/** if the object is indirect, the number of the PDF object. */
	protected int number = -1;
	/** indicates if the object is indirect and recursive. */
	protected boolean recursive = false;

	/**
	 * Creates a tree node for a PDF object.
	 * @param object	the PDF object represented by this tree node.
	 */
	protected PdfObjectTreeNode(PdfObject object) {
		super(null, getCaption(object));
		this.object = object;
    	switch(object.type()) {
    	case PdfObject.INDIRECT:
            if (isRecursive())
    			icon = IconFetcher.getIcon("ref_recursive.png");
    		else
    			icon = IconFetcher.getIcon("ref.png");
    		return;
    	case PdfObject.ARRAY:
    		icon = IconFetcher.getIcon("array.png");
    		return;
    	case PdfObject.DICTIONARY:
    		icon = IconFetcher.getIcon("dictionary.png");
    		return;
		case PdfObject.STREAM:
			icon = IconFetcher.getIcon("stream.png");
			return;
		case PdfObject.BOOLEAN:
			icon = IconFetcher.getIcon("boolean.png");
			return;
		case PdfObject.NAME:
			icon = IconFetcher.getIcon("name.png");
			return;
		case PdfObject.NULL:
			icon = IconFetcher.getIcon("null.png");
			return;
		case PdfObject.NUMBER:
			icon = IconFetcher.getIcon("number.png");
			return;
		case PdfObject.STRING:
			icon = IconFetcher.getIcon("string.png");
			return;
    	}
	}

	/**
	 * Creates a tree node for a PDF object.
	 * @param icon		the file with the icon
	 * @param object	the PDF object represented by this tree node.
	 */
	protected PdfObjectTreeNode(String icon, PdfObject object) {
		super(icon, getCaption(object));
		this.object = object;
        }


	/**
	 * Creates an instance of a tree node for a PDF object.
	 * @param object	the PDF object represented by this tree node.
	 * @return	a PdfObjectTreeNode
	 */
	public static PdfObjectTreeNode getInstance(PdfObject object) {
		if (object.isDictionary()) {
			if (PdfName.PAGE.equals(((PdfDictionary)object).get(PdfName.TYPE))) {
				return new PdfPageTreeNode((PdfDictionary)object);
			}
			else if (PdfName.PAGES.equals(((PdfDictionary)object).get(PdfName.TYPE))) {
				return new PdfPagesTreeNode((PdfDictionary)object);
			}
		}
		return new PdfObjectTreeNode(object);
	}

	/**
	 * Creates an instance of a tree node for an indirect object.
	 * @param object	the PDF object represented by this tree node.
	 * @param number	the xref number of the indirect object
	 * @return	a PdfObjectTreeNode
	 */
	public static PdfObjectTreeNode getInstance(PdfObject object, int number) {
		PdfObjectTreeNode node = getInstance(object);
		node.number = number;
		return node;
	}

	/**
	 * Creates an instance of a tree node for the object corresponding with a key in a dictionary.
	 * @param dict	the dictionary that is the parent of this tree node.
	 * @param key	the dictionary key corresponding with the PDF object in this tree node.
	 * @return	a PdfObjectTreeNode
	 */
	public static PdfObjectTreeNode getInstance(PdfDictionary dict, PdfName key) {
		PdfObjectTreeNode node = getInstance(dict.get(key));
		node.setUserObject(getDictionaryEntryCaption(dict, key));
		node.key = key;
		return node;
	}

	/**
	 * Getter for the PDF Object.
	 * @return	the PDF object represented by this tree node.
	 */
	public PdfObject getPdfObject() {
        return object;
	}

	/**
	 * Getter for the object number in case the object is indirect.
	 * @return	-1 for direct objects; the object number for indirect objects
	 */
	public int getNumber() {
		if (isIndirectReference()) {
			return ((PdfIndirectReference)object).getNumber();
		}
		return number;
	}

	/**
	 * Tells you if the node contains an indirect reference.
	 * @return	true if the object is an indirect reference
	 */
	public boolean isIndirectReference() {
		return object.type() == PdfObject.INDIRECT;
	}

	/**
	 * Tells you if the object is indirect.
	 * @return	true for indirect objects; false for direct objects.
	 */
	public boolean isIndirect() {
		return isIndirectReference() || number > -1;
	}

	/**
	 * Tells you if the node contains an array.
	 * @return	true if the object is a PdfArray
	 */
	public boolean isArray() {
		return object.isArray();
	}

	/**
	 * Checks if this node is a dictionary item with a specific key.
	 * @param	key	the key of the node we're looking for
	 */
	public boolean isDictionaryNode(PdfName key) {
		if (key == null) return false;
		return key.equals(this.key);
	}

	/**
	 * Tells you if the node contains a dictionary.
	 * @return	true if the object is a PdfDictionary
	 */
	public boolean isDictionary() {
		return object.isDictionary();
	}

	/**
	 * Tells you if the node contains a stream.
	 * @return	true if the object is a PRStream
	 */
	public boolean isStream() {
		return object.isStream();
	}

	/**
	 * Set this to true if the object is a reference to a node higher up in the tree.
	 * @param	recursive	true if the object is indirect and recursive
	 */
	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

    /**
	 * Tells you if the object is a reference to a node higher up in the tree.
	 * @return	true if the node is used recursively.
	 */
	public boolean isRecursive() {
		return recursive;
	}

	/**
	 * Creates the caption for a PDF object.
	 * @param object	the object for which a caption has to be created.
	 * @return	a caption for a PDF object
	 */
	public static String getCaption(PdfObject object) {
		if (object == null)
			return "null";
		switch (object.type()) {
		case PdfObject.INDIRECT:
            {
                String reffedCaption = getCaption( PdfReader.getPdfObject( object ) );
                return object.toString() + " -> " + reffedCaption;
            }
        case PdfObject.ARRAY:
			return "Array";
		case PdfObject.STREAM:
			return "Stream";
		case PdfObject.STRING:
			return ((PdfString)object).toUnicodeString();
		}
		return object.toString();
	}

	/**
	 * Creates the caption for an object that is a dictionary entry.
	 * @param dict	a dictionary
	 * @param key	a key in the dictionary
	 * @return	a caption for the object corresponding with the key in the dictionary.
	 */
	public static String getDictionaryEntryCaption(PdfDictionary dict, PdfName key) {
		StringBuffer buf = new StringBuffer(key.toString());
		buf.append(": ");
        PdfObject valObj = dict.get(key);
        if (valObj.isIndirect()) {
            buf.append( getCaption( valObj ) );
        } else {
            buf.append(dict.get(key).toString());
        }
        return buf.toString();
	}

	/**
	 * Gets the tree path of an ancestor.
	 * This only works with recursive references
	 * @return	the treepath to an ancestor
	 */
	public PdfObjectTreeNode getAncestor() {
		if (isRecursive()) {
			PdfObjectTreeNode node = this;
			while(true) {
				node = (PdfObjectTreeNode)node.getParent();
				if (node.isIndirectReference() && node.getNumber() == getNumber()) {
					return node;
				}
			}
		}
		return null;
	}

	/** a serial version UID. */
	private static final long serialVersionUID = -5617844659397445879L;
}
