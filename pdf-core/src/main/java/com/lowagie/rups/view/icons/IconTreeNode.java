/*
 * $Id: IconTreeNode.java 3117 2008-01-31 05:53:22Z xlv $
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

package com.lowagie.rups.view.icons;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A tree node with a specific icon.
 */
public class IconTreeNode extends DefaultMutableTreeNode {

	/** The icon that has to be used for this TreeNode. */
	protected Icon icon;
	
	/**
	 * Constructs an IconTreeNode.
	 * @param	icon	the filename of the icon to use for this node.
	 */
	public IconTreeNode(String icon) {
		super();
		this.icon = IconFetcher.getIcon(icon);
	}
	
	/**
	 * Constructs an IconTreeNode.
	 * @param	icon	the filename of the icon to use for this node.
	 * @param	userobject	the userobject for this node
	 */
	public IconTreeNode(String icon, Object userobject) {
		super(userobject);
		this.icon = IconFetcher.getIcon(icon);
	}
	
	/** Getter for the icon. */
	public Icon getIcon() {
		return icon;
	}

	/** A Serial Version UID. */
	private static final long serialVersionUID = -5900308991182960842L;
}