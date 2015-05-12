/*
 * $Id: RtfDestinationMgr.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2007 by Howard Shank
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999-2006 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2006 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the ?GNU LIBRARY GENERAL PUBLIC LICENSE?), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */
package com.lowagie.text.rtf.parser.destinations;

import java.util.HashMap;

import com.lowagie.text.rtf.parser.RtfParser;

/**
 * <code>RtfDestinationMgr</code> manages destination objects for the parser
 * 
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.0.8
 */
public final class RtfDestinationMgr {
	/*
	 * Destinations
	 */
	private static RtfDestinationMgr instance = null;
	private static Object lock = new Object();
	
	/**
	 * CtrlWord <-> Destination map object.
	 * 
	 * Maps control words to their destinations objects.
	 * Null destination is a special destination used for
	 * discarding unwanted data. This is primarily used when
	 * skipping groups, binary data or unwanted/unknown data.
	 */
	private static HashMap destinations = new HashMap(300, 0.95f);
	/**
	 * Destination objects.
	 * There is only one of each destination.
	 */
	private static HashMap destinationObjects = new HashMap(10, 0.95f);
	
	private static boolean ignoreUnknownDestinations = false;
	
	private static RtfParser rtfParser = null;

	/**
	 * String representation of null destination.
	 */
	public static final String DESTINATION_NULL = "null";
	/**
	 * String representation of document destination.
	 */
	public static final String DESTINATION_DOCUMENT = "document";
	
	/**
	 * Hidden default constructor becuase
	 */
	private RtfDestinationMgr() {
	}
	
	public static void setParser(RtfParser parser) {
		rtfParser = parser;
	}
	public static RtfDestinationMgr getInstance() {
		synchronized(lock) {
			if(instance == null) {
				instance = new RtfDestinationMgr();
				// 2 required destinations for all documents
				RtfDestinationMgr.addDestination(RtfDestinationMgr.DESTINATION_DOCUMENT, new Object[] { "RtfDestinationDocument", "" } );
				RtfDestinationMgr.addDestination(RtfDestinationMgr.DESTINATION_NULL, new Object[] { "RtfDestinationNull", "" } );
			}
			return instance;
		}
	}
	public static RtfDestinationMgr getInstance(RtfParser parser) {
		synchronized(lock) {
			RtfDestinationMgr.setParser(parser);
			if(instance == null) {
				instance = new RtfDestinationMgr();
				// 2 required destinations for all documents
				RtfDestinationMgr.addDestination(RtfDestinationMgr.DESTINATION_DOCUMENT, new Object[] { "RtfDestinationDocument", "" } );
				RtfDestinationMgr.addDestination(RtfDestinationMgr.DESTINATION_NULL, new Object[] { "RtfDestinationNull", "" } );
			}
			return instance;
		}
	}
	
	public static RtfDestination getDestination(String destination) {
		RtfDestination dest = null;
		if(destinations.containsKey(destination)) {
			dest = (RtfDestination)destinations.get(destination);
		} else {
			if(ignoreUnknownDestinations) {
				dest = (RtfDestination)destinations.get(DESTINATION_NULL);
			} else {
				dest = (RtfDestination)destinations.get(DESTINATION_DOCUMENT);
			}
		}
		dest.setParser(RtfDestinationMgr.rtfParser);
		return dest;
	}
	
	public static boolean addDestination(String destination, Object[] args) {
		if(destinations.containsKey(destination)) {
			return true;
		}
		
		String thisClass =  "com.lowagie.text.rtf.parser.destinations." + (String)args[0];

		if(thisClass.indexOf("RtfDestinationNull") >= 0) {
			destinations.put(destination, RtfDestinationNull.getInstance());
			return true;
		}
		
		Class value = null;
	
		try {
			value = Class.forName(thisClass);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		
		RtfDestination c = null;
		
		if(destinationObjects.containsKey(value.getName())) {
			c = (RtfDestination)destinationObjects.get(value.getName());		
		} else {
			try {
				c = (RtfDestination)value.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
		c.setParser(rtfParser);
		
		if(value.isInstance(RtfDestinationInfo.class)) {
				((RtfDestinationInfo)c).setElementName(destination);
		}
		
		if(value.isInstance(RtfDestinationStylesheetTable.class)) {
				((RtfDestinationStylesheetTable)c).setElementName(destination);
				((RtfDestinationStylesheetTable)c).setType((String)args[1]);
		}

		destinations.put(destination, c);
		destinationObjects.put(value.getName(), c);
		return true;
	}
	
	// listener methods

	/**
	 * Adds a <CODE>RtfDestinationListener</CODE> to the appropriate <CODE>RtfDestination</CODE>.
	 *
	 * @param destination the destination string for the listener
	 * @param listener
	 *            the new RtfDestinationListener.
	 */
	public static boolean addListener(String destination, RtfDestinationListener listener) {
		RtfDestination dest = getDestination(destination);
		if(dest != null) {
			return dest.addListener(listener);
		}
		return false;
	}

	/**
	 * Removes a <CODE>RtfDestinationListener</CODE> from the appropriate <CODE>RtfDestination</CODE>.
	 *
	 * @param destination the destination string for the listener
	 * @param listener
	 *            the RtfCtrlWordListener that has to be removed.
	 */
	public static boolean removeListener(String destination, RtfDestinationListener listener) {
		RtfDestination dest = getDestination(destination);
		if(dest != null) {
			return dest.removeListener(listener);
		}
		return false;
	}
}
