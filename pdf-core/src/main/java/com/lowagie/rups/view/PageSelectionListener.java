/*
 * $Id: PageNavigationListener.java 4031 2009-07-23 11:09:59Z blowagie $
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

package com.lowagie.rups.view;

/**
 * Interface that has to be implemented by every class that listens
 * to page navigation features; keys that are pressed, buttons that
 * are pushed,...
 * @since	iText 5.0.0 (renamed from PageNavigationListener)
 */
public interface PageSelectionListener {

	/**
	 * Goes to a specific page number in a document.
	 * @param	pageNumber
	 * @return	the resulting page number.
	 * 		Can be different from pageNumber if pageNumber doesn't exist.
	 */
	public int gotoPage(int pageNumber);
}
