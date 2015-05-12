/*
 * $Id: FileChooserAction.java 3117 2008-01-31 05:53:22Z xlv $
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

package com.lowagie.rups.io;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Allows you to browse the file system and forwards the file
 * to the object that is waiting for you to choose a file.
 */
public class FileChooserAction extends AbstractAction {
	
	/** An object that is expecting the result of the file chooser action. */
	protected Observable observable;
	/** A file filter to apply when browsing for a file. */
	protected FileFilter filter;
	/** Indicates if you're browsing to create a new or an existing file. */
	protected boolean newFile;
	/** The file that was chosen. */
	protected File file;
	
	/**
	 * Creates a new file chooser action.
	 * @param observable	the object waiting for you to select file
	 * @param caption	a description for the action
	 * @param filter	a filter to apply when browsing
	 * @param newFile	indicates if you should browse for a new or existing file
	 */
	public FileChooserAction(Observable observable, String caption, FileFilter filter, boolean newFile) {
		super(caption);
		this.observable = observable;
		this.filter = filter;
		this.newFile = newFile;
	}
	
	/**
	 * Getter for the file.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		JFileChooser fc = new JFileChooser();
		if (filter != null) {
			fc.setFileFilter(filter);
		}
		int okCancel;
		if (newFile) {
			okCancel = fc.showSaveDialog(null);
		}
		else {
			okCancel = fc.showOpenDialog(null);
		}
		if (okCancel == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			observable.notifyObservers(this);
		}
	}

	/** A serial version UID. */
	private static final long serialVersionUID = 2225830878098387118L;

}
