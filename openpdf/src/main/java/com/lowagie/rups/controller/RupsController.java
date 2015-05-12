/*
 * $Id: RupsController.java 4039 2009-07-24 10:29:47Z blowagie $
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

package com.lowagie.rups.controller;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Observable;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import com.lowagie.rups.io.FileChooserAction;
import com.lowagie.rups.io.FileCloseAction;
import com.lowagie.rups.model.PdfFile;
import com.lowagie.rups.view.Console;
import com.lowagie.rups.view.PageSelectionListener;
import com.lowagie.rups.view.RupsMenuBar;
import com.lowagie.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.lowagie.rups.view.itext.treenodes.PdfTrailerTreeNode;
import com.lowagie.text.DocumentException;

/**
 * This class controls all the GUI components that are shown in
 * the RUPS application: the menu bar, the panels,...
 */
public class RupsController extends Observable
	implements TreeSelectionListener, PageSelectionListener {
	
	// member variables
	
	/* file and controller */
	/** The Pdf file that is currently open in the application. */
	protected PdfFile pdfFile;
	/**
	 * Object with the GUI components for iText.
	 * @since	iText 5.0.0 (renamed from reader which was confusing because reader is normally used for a PdfReader instance)
	 */
	protected PdfReaderController readerController;

	/* main components */
	/** The JMenuBar for the RUPS application. */
	protected RupsMenuBar menuBar;
	/** Contains all other components: the page panel, the outline tree, etc. */
	protected JSplitPane masterComponent;
	
	
	// constructor
	/**
	 * Constructs the GUI components of the RUPS application.
	 */
	public RupsController(Dimension dimension) {
		// creating components and controllers
        menuBar = new RupsMenuBar(this);
        addObserver(menuBar);
		Console console = Console.getInstance();
		addObserver(console);
		readerController = new PdfReaderController(this, this);
		addObserver(readerController);

        // creating the master component
		masterComponent = new JSplitPane();
		masterComponent.setOrientation(JSplitPane.VERTICAL_SPLIT);
		masterComponent.setDividerLocation((int)(dimension.getHeight() * .70));
		masterComponent.setDividerSize(2);
		
		JSplitPane content = new JSplitPane();
		masterComponent.add(content, JSplitPane.TOP);
		JSplitPane info = new JSplitPane();
		masterComponent.add(info, JSplitPane.BOTTOM);
		
		content.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		content.setDividerLocation((int)(dimension.getWidth() * .6));
		content.setDividerSize(1);
        content.add(new JScrollPane(readerController.getPdfTree()), JSplitPane.LEFT);
		content.add(readerController.getNavigationTabs(), JSplitPane.RIGHT);
        
		info.setDividerLocation((int) (dimension.getWidth() * .3));
		info.setDividerSize(1);
		info.add(readerController.getObjectPanel(), JSplitPane.LEFT);
		JTabbedPane editorPane = readerController.getEditorTabs();
		JScrollPane cons = new JScrollPane(console.getTextArea());
		editorPane.addTab("Console", null, cons, "Console window (System.out/System.err)");
		editorPane.setSelectedComponent(cons);
		info.add(editorPane, JSplitPane.RIGHT);
		
	}

	/** Getter for the menubar. */
	public RupsMenuBar getMenuBar() {
		return menuBar;
	}
	
	/** Getter for the master component. */
	public Component getMasterComponent() {
		return masterComponent;
	}

	// Observable
	
	/**
	 * @see java.util.Observable#notifyObservers(java.lang.Object)
	 */
	@Override
	public void notifyObservers(Object obj) {
		if (obj instanceof FileChooserAction) {
			File file = ((FileChooserAction)obj).getFile();
			try {
				pdfFile = new PdfFile(file);
				setChanged();
				super.notifyObservers(RupsMenuBar.OPEN);
				readerController.startObjectLoader(pdfFile);
			}
			catch(IOException ioe) {
				JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
			}
			catch (DocumentException de) {
				JOptionPane.showMessageDialog(masterComponent, de.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
			}
			return;
		}
		if (obj instanceof FileCloseAction) {
			pdfFile = null;
			setChanged();
			super.notifyObservers(RupsMenuBar.CLOSE);
			return;
		}
	}

	// tree selection
	
	/**
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent evt) {
		Object selectednode = readerController.getPdfTree().getLastSelectedPathComponent();
		if (selectednode instanceof PdfTrailerTreeNode) {
			menuBar.update(this, RupsMenuBar.FILE_MENU);
			return;
		}
		if (selectednode instanceof PdfObjectTreeNode) {
			readerController.update(this, selectednode);
		}
	}

	// page navigation

	/**
	 * @see com.lowagie.rups.view.PageSelectionListener#gotoPage(int)
	 */
	public int gotoPage(int pageNumber) {
		readerController.gotoPage(pageNumber);
		return pageNumber;
	}
}
