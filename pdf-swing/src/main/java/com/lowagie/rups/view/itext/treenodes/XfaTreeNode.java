/*
 * $Id: XfaTreeNode.java 3117 2008-01-31 05:53:22Z xlv $
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import com.lowagie.rups.io.OutputStreamResource;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfReader;

/**
 * This is the root tree node for the different parts of the XFA resource; it's a child
 * of the root in the FormTree.
 * This resource can be one XDP stream (in which case this root will only have one child)
 * or different streams with individual packets comprising the XML Data Package.
 */
public class XfaTreeNode extends FormTreeNode implements OutputStreamResource {

	/** Start sequence of an artificial boundary between XFA fragments added by RUPS */
	public static final byte[] BOUNDARY_START = "<!--\nRUPS XFA individual packet: end of [".getBytes();
	/** Middle sequence of an artificial boundary between XFA fragments added by RUPS */
	public static final byte[] BOUNDARY_MIDDLE = "]; start of [".getBytes();
	/** End sequence of an artificial boundary between XFA fragments added by RUPS */
	public static final byte[] BOUNDARY_END = "]\n-->".getBytes();
	
	/**
	 * Creates the root node of the XFA tree.
	 * This will be a child of the FormTree root node.
	 * @param	xfa	the XFA node in the PdfTree (a child of the AcroForm node in the PDF catalog)
	 */
	public XfaTreeNode(PdfObjectTreeNode xfa) {
		super(xfa);
	}
	
	/**
	 * Writes (part of) the XFA resource to an OutputStream.
	 * If key is <code>null</code>, the complete resource is written;
	 * if key refers to an individual package, this package only is
	 * written to the OutputStream.
	 * @param os	the OutputStream to which the XML is written.
	 * @throws IOException	usual exception when there's a problem writing to an OutputStream
	 */
	public void writeTo(OutputStream os) throws IOException {
		Enumeration children = this.children();
		FormTreeNode node;
		PRStream stream;
		String key = null;
		String tmp = null;
		while (children.hasMoreElements()) {
			node = (FormTreeNode) children.nextElement();
			if (key != null) {
				os.write(BOUNDARY_START);
				os.write(key.getBytes());
				os.write(BOUNDARY_MIDDLE);
				tmp = (String)node.getUserObject();
				os.write(tmp.getBytes());
				os.write(BOUNDARY_END);
			}
			key = tmp;
			stream = (PRStream)node.getCorrespondingPdfObjectNode().getPdfObject();
			os.write(PdfReader.getStreamBytes(stream));
		}
		os.flush();
		os.close();
	}

	/**
	 * Adds a child node to the XFA root.
	 * The child node either corresponds with the complete XDP stream
	 * (if the XFA root only has one child) or with individual packet. 
	 * @param key	the name of the packet
	 * @param value	the corresponding stream node in the PdfTree
	 */
	public void addPacket(String key, PdfObjectTreeNode value) {
		FormTreeNode node = new FormTreeNode(value);
		node.setUserObject(key);
		this.add(node);
	}
	
	/** A serial version UID. */
	private static final long serialVersionUID = 2463297568233643790L;

}