/*
 * $Id: ObjectLoader.java 3117 2008-01-31 05:53:22Z xlv $
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

package com.lowagie.rups.model;

import java.util.Observable;

import com.lowagie.text.pdf.PdfReader;

/**
 * Loads the necessary iText PDF objects in Background.
 */
public class ObjectLoader extends BackgroundTask {
	/** This is the object that will forward the updates to the observers. */
	protected Observable observable;
	/** iText's PdfReader object. */
	protected PdfReader reader;
	/** The factory that can provide PDF objects. */
	protected IndirectObjectFactory objects;
	/** The factory that can provide tree nodes. */
	protected TreeNodeFactory nodes;
	
	/**
	 * Creates a new ObjectLoader.
	 * @param	observable	the object that will forward the changes.
	 * @param	reader		the PdfReader from which the objects will be read.
	 */
	public ObjectLoader(Observable observable, PdfReader reader) {
		this.observable = observable;
		this.reader = reader;
		start();
	}
	
	/**
	 * Getter for the PdfReader object.
	 * @return	a reader object
	 */
	public PdfReader getReader() {
		return reader;
	}

	/**
	 * Getter for the object factory.
	 * @return	an indirect object factory
	 */
	public IndirectObjectFactory getObjects() {
		return objects;
	}

	/**
	 * Getter for the tree node factory.
	 * @return	a tree node factory
	 */
	public TreeNodeFactory getNodes() {
		return nodes;
	}
	
	/**
	 * @see com.lowagie.rups.model.BackgroundTask#doTask()
	 */
	@Override
	public void doTask() {
		ProgressDialog progress = new ProgressDialog(null, "Reading PDF file");
		objects = new IndirectObjectFactory(reader);
		int n = objects.getXRefMaximum();
		progress.setMessage("Reading the Cross-Reference table");
		progress.setTotal(n);
		while (objects.storeNextObject()) {
			progress.setValue(objects.getCurrent());
		}
		progress.setTotal(0);
		nodes = new TreeNodeFactory(objects);
		progress.setMessage("Updating GUI");
		observable.notifyObservers(this);
		progress.dispose();
	}
}