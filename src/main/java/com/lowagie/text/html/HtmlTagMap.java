/*
 * $Id: HtmlTagMap.java 3528 2008-07-07 14:46:09Z Howard_s $
 *
 * Copyright 2001, 2002 by Bruno Lowagie.
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
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
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
 * Contributions by:
 * Lubos Strapko
 * 
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.html;

import java.util.HashMap;

import com.lowagie.text.ElementTags;
import com.lowagie.text.FontFactory;

/**
 * The <CODE>Tags</CODE>-class maps several XHTML-tags to iText-objects.
 */

public class HtmlTagMap extends HashMap {

	private static final long serialVersionUID = 5287430058473705350L;

	/**
	 * Constructs an HtmlTagMap.
	 */

	public HtmlTagMap() {
		super();
		HtmlPeer peer;

		peer = new HtmlPeer(ElementTags.ITEXT, HtmlTags.HTML);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PHRASE, HtmlTags.SPAN);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.CHUNK, HtmlTags.CHUNK);
		peer.addAlias(ElementTags.FONT, HtmlTags.FONT);
		peer.addAlias(ElementTags.SIZE, HtmlTags.SIZE);
		peer.addAlias(ElementTags.COLOR, HtmlTags.COLOR);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.ANCHOR, HtmlTags.ANCHOR);
		peer.addAlias(ElementTags.NAME, HtmlTags.NAME);
		peer.addAlias(ElementTags.REFERENCE, HtmlTags.REFERENCE);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PARAGRAPH, HtmlTags.PARAGRAPH);
		peer.addAlias(ElementTags.ALIGN, HtmlTags.ALIGN);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PARAGRAPH, HtmlTags.DIV);
		peer.addAlias(ElementTags.ALIGN, HtmlTags.ALIGN);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PARAGRAPH, HtmlTags.H[0]);
		peer.addValue(ElementTags.SIZE, "20");
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PARAGRAPH, HtmlTags.H[1]);
		peer.addValue(ElementTags.SIZE, "18");
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PARAGRAPH, HtmlTags.H[2]);
		peer.addValue(ElementTags.SIZE, "16");
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PARAGRAPH, HtmlTags.H[3]);
		peer.addValue(ElementTags.SIZE, "14");
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PARAGRAPH, HtmlTags.H[4]);
		peer.addValue(ElementTags.SIZE, "12");
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PARAGRAPH, HtmlTags.H[5]);
		peer.addValue(ElementTags.SIZE, "10");
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.LIST, HtmlTags.ORDEREDLIST);
		peer.addValue(ElementTags.NUMBERED, "true");
		peer.addValue(ElementTags.SYMBOLINDENT, "20");
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.LIST, HtmlTags.UNORDEREDLIST);
		peer.addValue(ElementTags.NUMBERED, "false");
		peer.addValue(ElementTags.SYMBOLINDENT, "20");
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.LISTITEM, HtmlTags.LISTITEM);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PHRASE, HtmlTags.I);
		peer.addValue(ElementTags.STYLE, Markup.CSS_VALUE_ITALIC);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PHRASE, HtmlTags.EM);
		peer.addValue(ElementTags.STYLE, Markup.CSS_VALUE_ITALIC);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PHRASE, HtmlTags.B);
		peer.addValue(ElementTags.STYLE, Markup.CSS_VALUE_BOLD);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PHRASE, HtmlTags.STRONG);
		peer.addValue(ElementTags.STYLE, Markup.CSS_VALUE_BOLD);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PHRASE, HtmlTags.S);
		peer.addValue(ElementTags.STYLE, Markup.CSS_VALUE_LINETHROUGH);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PHRASE, HtmlTags.CODE);
		peer.addValue(ElementTags.FONT, FontFactory.COURIER);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PHRASE, HtmlTags.VAR);
		peer.addValue(ElementTags.FONT, FontFactory.COURIER);
		peer.addValue(ElementTags.STYLE, Markup.CSS_VALUE_ITALIC);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.PHRASE, HtmlTags.U);
		peer.addValue(ElementTags.STYLE, Markup.CSS_VALUE_UNDERLINE);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.CHUNK, HtmlTags.SUP);
		peer.addValue(ElementTags.SUBSUPSCRIPT, "6.0");
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.CHUNK, HtmlTags.SUB);
		peer.addValue(ElementTags.SUBSUPSCRIPT, "-6.0");
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.HORIZONTALRULE, HtmlTags.HORIZONTALRULE);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.TABLE, HtmlTags.TABLE);
		peer.addAlias(ElementTags.WIDTH, HtmlTags.WIDTH);
		peer.addAlias(ElementTags.BACKGROUNDCOLOR, HtmlTags.BACKGROUNDCOLOR);
		peer.addAlias(ElementTags.BORDERCOLOR, HtmlTags.BORDERCOLOR);
		peer.addAlias(ElementTags.COLUMNS, HtmlTags.COLUMNS);
		peer.addAlias(ElementTags.CELLPADDING, HtmlTags.CELLPADDING);
		peer.addAlias(ElementTags.CELLSPACING, HtmlTags.CELLSPACING);
		peer.addAlias(ElementTags.BORDERWIDTH, HtmlTags.BORDERWIDTH);
		peer.addAlias(ElementTags.ALIGN, HtmlTags.ALIGN);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.ROW, HtmlTags.ROW);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.CELL, HtmlTags.CELL);
		peer.addAlias(ElementTags.WIDTH, HtmlTags.WIDTH);
		peer.addAlias(ElementTags.BACKGROUNDCOLOR, HtmlTags.BACKGROUNDCOLOR);
		peer.addAlias(ElementTags.BORDERCOLOR, HtmlTags.BORDERCOLOR);
		peer.addAlias(ElementTags.COLSPAN, HtmlTags.COLSPAN);
		peer.addAlias(ElementTags.ROWSPAN, HtmlTags.ROWSPAN);
		peer.addAlias(ElementTags.NOWRAP, HtmlTags.NOWRAP);
		peer.addAlias(ElementTags.HORIZONTALALIGN, HtmlTags.HORIZONTALALIGN);
		peer.addAlias(ElementTags.VERTICALALIGN, HtmlTags.VERTICALALIGN);
		peer.addValue(ElementTags.HEADER, "false");
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.CELL, HtmlTags.HEADERCELL);
		peer.addAlias(ElementTags.WIDTH, HtmlTags.WIDTH);
		peer.addAlias(ElementTags.BACKGROUNDCOLOR, HtmlTags.BACKGROUNDCOLOR);
		peer.addAlias(ElementTags.BORDERCOLOR, HtmlTags.BORDERCOLOR);
		peer.addAlias(ElementTags.COLSPAN, HtmlTags.COLSPAN);
		peer.addAlias(ElementTags.ROWSPAN, HtmlTags.ROWSPAN);
		peer.addAlias(ElementTags.NOWRAP, HtmlTags.NOWRAP);
		peer.addAlias(ElementTags.HORIZONTALALIGN, HtmlTags.HORIZONTALALIGN);
		peer.addAlias(ElementTags.VERTICALALIGN, HtmlTags.VERTICALALIGN);
		peer.addValue(ElementTags.HEADER, "true");
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.IMAGE, HtmlTags.IMAGE);
		//        peer.addAlias(ElementTags.URL, HtmlTags.URL);
		peer.addAlias(ElementTags.URL, ElementTags.SRC); // contributed by Lubos Strapko

		peer.addAlias(ElementTags.ALT, HtmlTags.ALT);
		peer.addAlias(ElementTags.PLAINWIDTH, HtmlTags.PLAINWIDTH);
		peer.addAlias(ElementTags.PLAINHEIGHT, HtmlTags.PLAINHEIGHT);
		put(peer.getAlias(), peer);

		peer = new HtmlPeer(ElementTags.NEWLINE, HtmlTags.NEWLINE);
		put(peer.getAlias(), peer);
	}

	/**
	 * Checks if this is the root tag.
	 * @param tag a tagvalue
	 * @return true if tag is HTML or html
	 */

	public static boolean isHtml(String tag) {
		return HtmlTags.HTML.equalsIgnoreCase(tag);
	}

	/**
	 * Checks if this is the head tag.
	 * @param tag a tagvalue
	 * @return true if tag is HEAD or head
	 */

	public static boolean isHead(String tag) {
		return HtmlTags.HEAD.equalsIgnoreCase(tag);
	}

	/**
	 * Checks if this is the meta tag.
	 * @param tag a tagvalue
	 * @return true if tag is META or meta
	 */

	public static boolean isMeta(String tag) {
		return HtmlTags.META.equalsIgnoreCase(tag);
	}

	/**
	 * Checks if this is the link tag.
	 * @param tag a tagvalue
	 * @return true if tag is LINK or link
	 */

	public static boolean isLink(String tag) {
		return HtmlTags.LINK.equalsIgnoreCase(tag);
	}

	/**
	 * Checks if this is the title tag.
	 * @param tag a tagvalue
	 * @return true if tag is TITLE or title
	 */

	public static boolean isTitle(String tag) {
		return HtmlTags.TITLE.equalsIgnoreCase(tag);
	}

	/**
	 * Checks if this is the root tag.
	 * @param tag a tagvalue
	 * @return true if tag is BODY or body
	 */

	public static boolean isBody(String tag) {
		return HtmlTags.BODY.equalsIgnoreCase(tag);
	}

	/**
	 * Checks if this is a special tag.
	 * @param tag a tagvalue
	 * @return true if tag is a HTML, HEAD, META, LINK or BODY tag (case insensitive)
	 */
	public static boolean isSpecialTag(String tag) {
		return isHtml(tag) || isHead(tag) || isMeta(tag) || isLink(tag)
				|| isBody(tag);
	}
}