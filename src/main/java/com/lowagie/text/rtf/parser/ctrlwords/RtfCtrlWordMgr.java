/* 
 * $Id: RtfCtrlWordMgr.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2007 by Howard Shank (hgshank@yahoo.com)
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

package com.lowagie.text.rtf.parser.ctrlwords;

import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import com.lowagie.text.rtf.parser.RtfParser;

/**
 * <code>RtfCtrlWordMgr</code> handles the dispatching of control words from
 * the table of known control words.
 * 
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.0.8
 */
public final class RtfCtrlWordMgr {
	public static final boolean debug = false;
	public static final boolean debugFound = false;
	public static final boolean debugNotFound = true;
	private PushbackInputStream reader = null;
	private RtfParser rtfParser = null;
	private RtfCtrlWordMap ctrlWordMap = null;
	
	/** The <code>RtfCtrlWordListener</code>. */
    private ArrayList listeners = new ArrayList();

//	// TIMING DEBUG INFO
//	private long endTime = 0;
//	private Date endDate = null;		
//	private long endFree = 0;
//	private DecimalFormat df = new DecimalFormat("#,##0");
//	private Date startDate = new Date();
//	private long startTime = System.currentTimeMillis();
//	private long startFree = Runtime.getRuntime().freeMemory();
	
	/**
	 * Constructor
	 * @param rtfParser The parser object this manager works with.
	 * @param reader the PushbackReader from the tokeniser.
	 */
	public RtfCtrlWordMgr(RtfParser rtfParser, PushbackInputStream reader) {
		this.rtfParser = rtfParser;	// set the parser
		this.reader = reader;	// set the reader value
		ctrlWordMap = new RtfCtrlWordMap(rtfParser);
		
//		// TIMING DEBUG INFO
//		endFree = Runtime.getRuntime().freeMemory();
//		endTime = System.currentTimeMillis();
//		endDate = new Date();
//		System.out.println("RtfCtrlWordMgr start date: " + startDate.toLocaleString());
//		System.out.println("RtfCtrlWordMgr end date  : " + endDate.toLocaleString());
//		System.out.println("  Elapsed time    : " + Long.toString(endTime - startTime) + " milliseconds.");
//		System.out.println("Begin Constructor RtfCtrlWordMgr , free mem is " + df.format(startFree / 1024) + "k");
//		System.out.println("End Constructor RtfCtrlWordMgr , free mem is " + df.format(endFree / 1024) + "k");
//        System.out.println("RtfCtrlWordMgr used approximately " + df.format((startFree - endFree) / 1024) + "k");
	}
	
	/**
	 * Internal to control word manager class.
	 * 
	 * @param ctrlWordData The <code>RtfCtrlWordData</code> object with control word and param
	 * @param groupLevel The current document group parsing level
	 * @return errOK if ok, otherwise an error code.
	 */
	public int handleKeyword(RtfCtrlWordData ctrlWordData, int groupLevel) {
		//TODO: May be used for event handling.
		int result = RtfParser.errOK;
		
		// Call before handler event here
		beforeCtrlWord(ctrlWordData);
		
		result = dispatchKeyword(ctrlWordData, groupLevel);
		
		// call after handler event here
		afterCtrlWord(ctrlWordData);
		
		return result;
	}
	
	/**
	 * Dispatch the token to the correct control word handling object.
	 *  
	 * @param ctrlWordData The <code>RtfCtrlWordData</code> object with control word and param
	 * @param groupLevel The current document group parsing level
	 * @return errOK if ok, otherwise an error code.
	 */
	private int dispatchKeyword(RtfCtrlWordData ctrlWordData, int groupLevel) {
		int result = RtfParser.errOK;
		if(ctrlWordData != null) {
			RtfCtrlWordHandler ctrlWord = ctrlWordMap.getCtrlWordHandler(ctrlWordData.ctrlWord);
			if(ctrlWord != null) {
				ctrlWord.handleControlword(ctrlWordData);
				if(debug && debugFound) {
					System.out.println("Keyword found:" +
						" New:" + ctrlWordData.ctrlWord + 
						" Param:" + ctrlWordData.param + 
						" bParam=" + ctrlWordData.hasParam);
				}
			} else {
				result = RtfParser.errCtrlWordNotFound;
				//result = RtfParser2.errAssertion;
				if(debug && debugNotFound) {
					System.out.println("Keyword unknown:" + 
						" New:" + ctrlWordData.ctrlWord + 
						" Param:" + ctrlWordData.param + 
						" bParam=" + ctrlWordData.hasParam);
				}
			}	
		}
		return result;
	}
	

    // listener methods

	/**
	 * Adds a <CODE>RtfCtrlWordListener</CODE> to the <CODE>RtfCtrlWordMgr</CODE>.
	 *
	 * @param listener
	 *            the new RtfCtrlWordListener.
	 */
	public void addRtfCtrlWordListener(RtfCtrlWordListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a <CODE>RtfCtrlWordListener</CODE> from the <CODE>RtfCtrlWordMgr</CODE>.
	 *
	 * @param listener
	 *            the RtfCtrlWordListener that has to be removed.
	 */
	public void removeRtfCtrlWordListener(RtfCtrlWordListener listener) {
		listeners.remove(listener);
	}
	
	private boolean beforeCtrlWord(RtfCtrlWordData ctrlWordData) {
		RtfCtrlWordListener listener;
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            listener = (RtfCtrlWordListener) iterator.next();
            listener.beforeCtrlWord(ctrlWordData);
        }
		return true;
	}
	
	private boolean onCtrlWord(RtfCtrlWordData ctrlWordData) {
		RtfCtrlWordListener listener;
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            listener = (RtfCtrlWordListener) iterator.next();
            listener.onCtrlWord(ctrlWordData);
        }
		return true;
	}
	
	private boolean afterCtrlWord(RtfCtrlWordData ctrlWordData) {
		RtfCtrlWordListener listener;
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            listener = (RtfCtrlWordListener) iterator.next();
            listener.afterCtrlWord(ctrlWordData);
        }
		return true;
	}
}
