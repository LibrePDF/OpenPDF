/*
 * $Id: ProgressDialog.java 3146 2008-02-20 18:10:07Z blowagie $
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

/**
 * An informational dialog window showing the progress of a certain action.
 */
public class ProgressDialog extends JDialog {

	/** a serial version uid. */
	private static final long serialVersionUID = -8286949678008659120L;
	/** label showing the message describing what's in progress. */
	protected JLabel message;
	/** the progress bar */
	protected JProgressBar progress;
	/** the icon used for this dialog box. */
	public static final JLabel INFO = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
	
	/**
	 * Creates a Progress frame displaying a certain message
	 * and a progress bar in indeterminate mode.
	 * @param	parent the parent frame of this dialog (used to position the dialog)
	 * @param	msg	the message that will be displayed.
	 */
	public ProgressDialog(JFrame parent, String msg) {
		super();
		this.setTitle("Progress...");
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	    setSize(300, 100);
	    this.setLocationRelativeTo(parent);
	    
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 2;
		getContentPane().add(INFO, constraints);
		constraints.gridheight = 1;
		constraints.gridx = 1;
		constraints.insets = new Insets(5, 5, 5, 5);
	    message = new JLabel(msg);
	    getContentPane().add(message, constraints);
		constraints.gridy = 1;
	    progress = new JProgressBar();
	    progress.setIndeterminate(true);
	    getContentPane().add(progress, constraints);
	    
	    setVisible(true);
	}
	
	/**
	 * Changes the message describing what's in progress
	 * @param msg	the message describing what's in progress
	 */
	public void setMessage(String msg) {
		message.setText(msg);
	}

	/**
	 * Changes the value of the progress bar.
	 * @param value	the current value
	 */
	public void setValue(int value) {
		progress.setValue(value);
	}
	
	/**
	 * Sets the maximum value for the progress bar.
	 * If 0 or less, sets the progress bar to indeterminate mode.
	 * @param n	the maximum value for the progress bar
	 */
	public void setTotal(int n) {
		if (n > 0) {
			progress.setMaximum(n);
			progress.setIndeterminate(false);
			progress.setStringPainted(true);
		}
		else {
			progress.setIndeterminate(true);
			progress.setStringPainted(false);
		}
	}
}
