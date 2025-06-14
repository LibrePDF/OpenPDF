/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
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
package com.sun.pdfview;

import java.io.IOException;

/**
 * Represents a destination in a PDF file. Destinations take 3 forms:
 * <ul>
 * <li>An explicit destination, which contains a reference to a page as well as
 * some stuff about how to fit it into the window.
 * <li>A named destination, which uses the PDF file's Dests entry in the
 * document catalog to map a name to an explicit destination
 * <li>A string destintation, which uses the PDF file's Dests entry. in the name
 * directory to map a string to an explicit destination.
 * </ul>
 *
 * All three of these cases are handled by the getDestination() method.
 */
public class PDFDestination {

	/** The known types of destination */
	public static final int XYZ = 0;
	public static final int FIT = 1;
	public static final int FITH = 2;
	public static final int FITV = 3;
	public static final int FITR = 4;
	public static final int FITB = 5;
	public static final int FITBH = 6;
	public static final int FITBV = 7;
	/** the type of this destination (from the list above) */
	private int type;
	/** the page we refer to */
	private PDFObject pageObj;
	/** the left coordinate of the fit area, if applicable */
	private float left;
	/** the right coordinate of the fit area, if applicable */
	private float right;
	/** the top coordinate of the fit area, if applicable */
	private float top;
	/** the bottom coordinate of the fit area, if applicable */
	private float bottom;
	/** the zoom, if applicable */
	private float zoom;

	/**
	 * Creates a new instance of PDFDestination
	 *
	 * @param pageObj
	 *            the page object this destination refers to
	 * @param type
	 *            the type of page this object refers to
	 */
	protected PDFDestination(PDFObject pageObj, int type) {
		this.pageObj = pageObj;
		this.type = type;
	}

	/**
	 * Get a destination from either an array (explicit destination), a name
	 * (named destination) or a string (name tree destination).
	 *
	 * @param obj
	 *            the PDFObject representing this destination
	 * @param root
	 *            the root of the PDF object tree
	 */
	public static PDFDestination getDestination(PDFObject obj, PDFObject root) throws IOException {
		// resolve string and name issues
		if (obj.getType() == PDFObject.NAME) {
			obj = getDestFromName(obj, root);
		} else if (obj.getType() == PDFObject.STRING) {
			obj = getDestFromString(obj, root);
		}

		// make sure we have the right kind of object
		if (obj == null || obj.getType() != PDFObject.ARRAY) {
			throw new PDFParseException("Can't create destination from: " + obj);
		}

		// the array is in the form [page type args ... ]
		PDFObject[] destArray = obj.getArray();

		// create the destination based on the type
		PDFDestination dest = null;
		String type = destArray[1].getStringValue();
		if (type.equals("XYZ")) {
			dest = new PDFDestination(destArray[0], XYZ);
		} else if (type.equals("Fit")) {
			dest = new PDFDestination(destArray[0], FIT);
		} else if (type.equals("FitH")) {
			dest = new PDFDestination(destArray[0], FITH);
		} else if (type.equals("FitV")) {
			dest = new PDFDestination(destArray[0], FITV);
		} else if (type.equals("FitR")) {
			dest = new PDFDestination(destArray[0], FITR);
		} else if (type.equals("FitB")) {
			dest = new PDFDestination(destArray[0], FITB);
		} else if (type.equals("FitBH")) {
			dest = new PDFDestination(destArray[0], FITBH);
		} else if (type.equals("FitBV")) {
			dest = new PDFDestination(destArray[0], FITBV);
		} else {
			throw new PDFParseException("Unknown destination type: " + type);
		}

		// now fill in the arguments based on the type
		switch (dest.getType()) {
		case XYZ:
			dest.setLeft(destArray[2].getFloatValue());
			dest.setTop(destArray[3].getFloatValue());
			dest.setZoom(destArray[4].getFloatValue());
			break;
		case FITH:
			if (destArray.length > 2) {
				dest.setTop(destArray[2].getFloatValue());
			} else {
				dest.setTop(0.0F);
			}
			break;
		case FITV:
			if (destArray.length > 2) {
				dest.setTop(destArray[2].getFloatValue());
			} else {
				dest.setTop(0.0F);
			}
			break;
		case FITR:
			dest.setLeft(destArray[2].getFloatValue());
			dest.setBottom(destArray[3].getFloatValue());
			dest.setRight(destArray[4].getFloatValue());
			dest.setTop(destArray[5].getFloatValue());
			break;
		case FITBH:
			if (destArray.length > 2) {
				dest.setTop(destArray[2].getFloatValue());
			} else {
				dest.setTop(0.0F);
			}
			break;
		case FITBV:
			if (destArray.length > 2) {
				dest.setTop(destArray[2].getFloatValue());
			} else {
				dest.setTop(0.0F);
			}
			break;
		}

		return dest;
	}

	/**
	 * Get the type of this destination
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * Get the PDF Page object associated with this destination
	 */
	public PDFObject getPage() {
		return this.pageObj;
	}

	/**
	 * Get the left coordinate value
	 */
	public float getLeft() {
		return this.left;
	}

	/**
	 * Set the left coordinate value
	 */
	public void setLeft(float left) {
		this.left = left;
	}

	/**
	 * Get the right coordinate value
	 */
	public float getRight() {
		return this.right;
	}

	/**
	 * Set the right coordinate value
	 */
	public void setRight(float right) {
		this.right = right;
	}

	/**
	 * Get the top coordinate value
	 */
	public float getTop() {
		return this.top;
	}

	/**
	 * Set the top coordinate value
	 */
	public void setTop(float top) {
		this.top = top;
	}

	/**
	 * Get the bottom coordinate value
	 */
	public float getBottom() {
		return this.bottom;
	}

	/**
	 * Set the bottom coordinate value
	 */
	public void setBottom(float bottom) {
		this.bottom = bottom;
	}

	/**
	 * Get the zoom value
	 */
	public float getZoom() {
		return this.zoom;
	}

	/**
	 * Set the zoom value
	 */
	public void setZoom(float zoom) {
		this.zoom = zoom;
	}

	/**
	 * Get a destination, given a name. This means the destination is in the
	 * root node's dests dictionary.
	 */
	private static PDFObject getDestFromName(PDFObject name, PDFObject root) throws IOException {
		// find the dests object in the root node
		PDFObject dests = root.getDictRef("Dests");
		if (dests != null) {
			// find this name in the dests dictionary
			return dests.getDictRef(name.getStringValue());
		}

		// not found
		return null;
	}

	/**
	 * Get a destination, given a string. This means the destination is in the
	 * root node's names dictionary.
	 */
	private static PDFObject getDestFromString(PDFObject str, PDFObject root) throws IOException {
		// find the names object in the root node
		PDFObject names = root.getDictRef("Names");
		if (names != null) {
			// find the dests entry in the names dictionary
			PDFObject dests = names.getDictRef("Dests");
			if (dests != null) {
				// create a name tree object
				NameTree tree = new NameTree(dests);

				// find the value we're looking for
				PDFObject obj = tree.find(str.getStringValue());

				// if we get back a dictionary, look for the /D value
				if (obj != null && obj.getType() == PDFObject.DICTIONARY) {
					obj = obj.getDictRef("D");
				}

				// found it
				return obj;
			}
		}

		// not found
		return null;
	}
}