/*
 * $Id: FileCloseAction.java 3117 2008-01-31 05:53:22Z xlv $
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
import java.awt.event.ActionListener;
import java.util.Observable;

/**
 * Action that tells an observable to close a file and/or releases
 * resources relevant for the file that is being closed.
 */
public class FileCloseAction implements ActionListener {
	
	/** An object that allows you to close a file. */
	protected Observable observable;
	
	/**
	 * Creates the close action.
	 * @param	observable	the object that expects you to close a file.
	 */
	public FileCloseAction(Observable observable) {
		this.observable = observable;
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		observable.notifyObservers(this);
	}

}
