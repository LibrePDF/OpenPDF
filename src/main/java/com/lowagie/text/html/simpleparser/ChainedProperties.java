/*
 * Copyright 2004 Paulo Soares
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

package com.lowagie.text.html.simpleparser;

import com.lowagie.text.ElementTags;
import java.util.ArrayList;
import java.util.HashMap;

public class ChainedProperties {

	public final static int fontSizes[] = { 8, 10, 12, 14, 18, 24, 36 };

	public ArrayList chain = new ArrayList();

	/** Creates a new instance of ChainedProperties */
	public ChainedProperties() {
	}

	public String getProperty(String key) {
		for (int k = chain.size() - 1; k >= 0; --k) {
			Object obj[] = (Object[]) chain.get(k);
			HashMap prop = (HashMap) obj[1];
			String ret = (String) prop.get(key);
			if (ret != null)
				return ret;
		}
		return null;
	}

	public boolean hasProperty(String key) {
		for (int k = chain.size() - 1; k >= 0; --k) {
			Object obj[] = (Object[]) chain.get(k);
			HashMap prop = (HashMap) obj[1];
			if (prop.containsKey(key))
				return true;
		}
		return false;
	}

	public void addToChain(String key, HashMap prop) {
		// adjust the font size
		String value = (String) prop.get(ElementTags.SIZE);
		if (value != null) {
			if (value.endsWith("pt")) {
				prop.put(ElementTags.SIZE, value.substring(0,
						value.length() - 2));
			} else {
				int s = 0;
				if (value.startsWith("+") || value.startsWith("-")) {
					String old = getProperty("basefontsize");
					if (old == null)
						old = "12";
					float f = Float.parseFloat(old);
					int c = (int) f;
					for (int k = fontSizes.length - 1; k >= 0; --k) {
						if (c >= fontSizes[k]) {
							s = k;
							break;
						}
					}
					int inc = Integer.parseInt(value.startsWith("+") ? value
							.substring(1) : value);
					s += inc;
				} else {
					try {
						s = Integer.parseInt(value) - 1;
					} catch (NumberFormatException nfe) {
						s = 0;
					}
				}
				if (s < 0)
					s = 0;
				else if (s >= fontSizes.length)
					s = fontSizes.length - 1;
				prop.put(ElementTags.SIZE, Integer.toString(fontSizes[s]));
			}
		}
		chain.add(new Object[] { key, prop });
	}

	public void removeChain(String key) {
		for (int k = chain.size() - 1; k >= 0; --k) {
			if (key.equals(((Object[]) chain.get(k))[0])) {
				chain.remove(k);
				return;
			}
		}
	}
}
