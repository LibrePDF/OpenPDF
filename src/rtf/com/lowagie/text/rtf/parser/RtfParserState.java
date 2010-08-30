/*
 * $Id: RtfParserState.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.text.rtf.parser;

import java.util.Stack;

import com.lowagie.text.rtf.parser.destinations.RtfDestination;
import com.lowagie.text.rtf.parser.destinations.RtfDestinationNull;
import com.lowagie.text.rtf.parser.properties.RtfProperty;

/**
 * The <code>RtfParserState</code> contains the state information
 * for the parser. The current state object is pushed/popped in a stack
 * when a group change is made.
 * 
 * When an open group is encountered, the current state is copied and 
 * then pushed on the top of the stack
 * When a close group is encountered, the current state is overwritten with
 * the popped value from the top of the stack 
 * 
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.0.8
 */
public class RtfParserState {
	/**
	 * The parser state.
	 */
	public int parserState = RtfParser.PARSER_IN_UNKNOWN;
	/**
	 * The tokeniser state.
	 */
	public int tokeniserState = RtfParser.TOKENISER_STATE_IN_UNKOWN;
	/**
	 * The control word set as the group handler. 
	 */
	public Object groupHandler = null;
	/**
	 * The parsed value for the current group/control word.
	 */
	public StringBuffer text = null;
	/**
	 * Stack containing control word handlers. There could be multiple
	 * control words in a group.
	 */
	public Stack ctrlWordHandlers = null;
	/**
	 * The current control word handler.
	 */
	public Object ctrlWordHandler = null;
	/**
	 * The current destination.
	 */
	public RtfDestination destination = null;
	/**
	 * Flag indicating if this is an extended destination \* control word
	 */
	public boolean isExtendedDestination = false;
	/**
	 * Flag to indicate if last token was an open group token '{'
	 */
	public boolean newGroup = false;
	
	public RtfProperty properties = null;
	/**
	 * Default constructor
	 *
	 */
	public RtfParserState() {
		this.text = new StringBuffer();
		this.ctrlWordHandlers = new Stack();
		this.properties = new RtfProperty();
		this.destination = RtfDestinationNull.getInstance();
		this.newGroup = false;
	}
	/**
	 * Copy constructor
	 * @param orig The object to copy
	 */
	public RtfParserState(RtfParserState orig) {
		this.properties = orig.properties;
		this.parserState = orig.parserState;
		this.tokeniserState = orig.tokeniserState;
		this.groupHandler = null;
		this.destination = orig.destination;
		this.text = new StringBuffer();
		this.ctrlWordHandlers = new Stack();
		this.destination = orig.destination;
		this.newGroup = false;
	}
	
}
