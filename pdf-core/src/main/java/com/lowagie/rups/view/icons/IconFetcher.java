/*
 * $Id: IconFetcher.java 3117 2008-01-31 05:53:22Z xlv $
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

import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Class that fetches the icons in com.lowagie.trapeze.icons.
 */
public class IconFetcher {
	
	/** Cache with icons. */
	protected static HashMap<String, Icon> cache = new HashMap<String, Icon>();
	
	/**
	 * Gets an Icon with a specific name.
	 * @param	filename	the filename of the Icon.
	 * @return	an Icon
	 */
	public static Icon getIcon(String filename) {
		if (filename == null) {
			return null;
		}
		Icon icon = cache.get(filename);
		if (icon == null) {
			try {
				icon = new ImageIcon(IconFetcher.class.getResource(filename));
				cache.put(filename, icon);
			}
			catch(Exception e) {
				System.err.println("Can't find file: " + filename);
				return null;
			}
		}
		return icon;
	}
}
