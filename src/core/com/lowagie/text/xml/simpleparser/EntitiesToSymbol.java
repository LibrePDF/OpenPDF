/*
 * $Id: EntitiesToSymbol.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 1999, 2000, 2001, 2002 Bruno Lowagie.
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
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.xml.simpleparser;

import java.util.HashMap;

import com.lowagie.text.Chunk;
import com.lowagie.text.Font;

/**
 * This class contains entities that can be used in an entity tag.
 */

public class EntitiesToSymbol {
    
    /**
     * This is a map that contains all possible id values of the entity tag
     * that can be translated to a character in font Symbol.
     */
    public static final HashMap map;
    
    static {
        map = new HashMap();
        map.put("169", new Character((char)227));
        map.put("172", new Character((char)216));
        map.put("174", new Character((char)210));
        map.put("177", new Character((char)177));
        map.put("215", new Character((char)180));
        map.put("247", new Character((char)184));
        map.put("8230", new Character((char)188));
        map.put("8242", new Character((char)162));
        map.put("8243", new Character((char)178));
        map.put("8260", new Character((char)164));
        map.put("8364", new Character((char)240));
        map.put("8465", new Character((char)193));
        map.put("8472", new Character((char)195));
        map.put("8476", new Character((char)194));
        map.put("8482", new Character((char)212));
        map.put("8501", new Character((char)192));
        map.put("8592", new Character((char)172));
        map.put("8593", new Character((char)173));
        map.put("8594", new Character((char)174));
        map.put("8595", new Character((char)175));
        map.put("8596", new Character((char)171));
        map.put("8629", new Character((char)191));
        map.put("8656", new Character((char)220));
        map.put("8657", new Character((char)221));
        map.put("8658", new Character((char)222));
        map.put("8659", new Character((char)223));
        map.put("8660", new Character((char)219));
        map.put("8704", new Character((char)34));
        map.put("8706", new Character((char)182));
        map.put("8707", new Character((char)36));
        map.put("8709", new Character((char)198));
        map.put("8711", new Character((char)209));
        map.put("8712", new Character((char)206));
        map.put("8713", new Character((char)207));
        map.put("8717", new Character((char)39));
        map.put("8719", new Character((char)213));
        map.put("8721", new Character((char)229));
        map.put("8722", new Character((char)45));
        map.put("8727", new Character((char)42));
        map.put("8729", new Character((char)183));
        map.put("8730", new Character((char)214));
        map.put("8733", new Character((char)181));
        map.put("8734", new Character((char)165));
        map.put("8736", new Character((char)208));
        map.put("8743", new Character((char)217));
        map.put("8744", new Character((char)218));
        map.put("8745", new Character((char)199));
        map.put("8746", new Character((char)200));
        map.put("8747", new Character((char)242));
        map.put("8756", new Character((char)92));
        map.put("8764", new Character((char)126));
        map.put("8773", new Character((char)64));
        map.put("8776", new Character((char)187));
        map.put("8800", new Character((char)185));
        map.put("8801", new Character((char)186));
        map.put("8804", new Character((char)163));
        map.put("8805", new Character((char)179));
        map.put("8834", new Character((char)204));
        map.put("8835", new Character((char)201));
        map.put("8836", new Character((char)203));
        map.put("8838", new Character((char)205));
        map.put("8839", new Character((char)202));
        map.put("8853", new Character((char)197));
        map.put("8855", new Character((char)196));
        map.put("8869", new Character((char)94));
        map.put("8901", new Character((char)215));
        map.put("8992", new Character((char)243));
        map.put("8993", new Character((char)245));
        map.put("9001", new Character((char)225));
        map.put("9002", new Character((char)241));
        map.put("913", new Character((char)65));
        map.put("914", new Character((char)66));
        map.put("915", new Character((char)71));
        map.put("916", new Character((char)68));
        map.put("917", new Character((char)69));
        map.put("918", new Character((char)90));
        map.put("919", new Character((char)72));
        map.put("920", new Character((char)81));
        map.put("921", new Character((char)73));
        map.put("922", new Character((char)75));
        map.put("923", new Character((char)76));
        map.put("924", new Character((char)77));
        map.put("925", new Character((char)78));
        map.put("926", new Character((char)88));
        map.put("927", new Character((char)79));
        map.put("928", new Character((char)80));
        map.put("929", new Character((char)82));
        map.put("931", new Character((char)83));
        map.put("932", new Character((char)84));
        map.put("933", new Character((char)85));
        map.put("934", new Character((char)70));
        map.put("935", new Character((char)67));
        map.put("936", new Character((char)89));
        map.put("937", new Character((char)87));
        map.put("945", new Character((char)97));
        map.put("946", new Character((char)98));
        map.put("947", new Character((char)103));
        map.put("948", new Character((char)100));
        map.put("949", new Character((char)101));
        map.put("950", new Character((char)122));
        map.put("951", new Character((char)104));
        map.put("952", new Character((char)113));
        map.put("953", new Character((char)105));
        map.put("954", new Character((char)107));
        map.put("955", new Character((char)108));
        map.put("956", new Character((char)109));
        map.put("957", new Character((char)110));
        map.put("958", new Character((char)120));
        map.put("959", new Character((char)111));
        map.put("960", new Character((char)112));
        map.put("961", new Character((char)114));
        map.put("962", new Character((char)86));
        map.put("963", new Character((char)115));
        map.put("964", new Character((char)116));
        map.put("965", new Character((char)117));
        map.put("966", new Character((char)102));
        map.put("967", new Character((char)99));
        map.put("9674", new Character((char)224));
        map.put("968", new Character((char)121));
        map.put("969", new Character((char)119));
        map.put("977", new Character((char)74));
        map.put("978", new Character((char)161));
        map.put("981", new Character((char)106));
        map.put("982", new Character((char)118));
        map.put("9824", new Character((char)170));
        map.put("9827", new Character((char)167));
        map.put("9829", new Character((char)169));
        map.put("9830", new Character((char)168));
        map.put("Alpha", new Character((char)65));
        map.put("Beta", new Character((char)66));
        map.put("Chi", new Character((char)67));
        map.put("Delta", new Character((char)68));
        map.put("Epsilon", new Character((char)69));
        map.put("Eta", new Character((char)72));
        map.put("Gamma", new Character((char)71));
        map.put("Iota", new Character((char)73));
        map.put("Kappa", new Character((char)75));
        map.put("Lambda", new Character((char)76));
        map.put("Mu", new Character((char)77));
        map.put("Nu", new Character((char)78));
        map.put("Omega", new Character((char)87));
        map.put("Omicron", new Character((char)79));
        map.put("Phi", new Character((char)70));
        map.put("Pi", new Character((char)80));
        map.put("Prime", new Character((char)178));
        map.put("Psi", new Character((char)89));
        map.put("Rho", new Character((char)82));
        map.put("Sigma", new Character((char)83));
        map.put("Tau", new Character((char)84));
        map.put("Theta", new Character((char)81));
        map.put("Upsilon", new Character((char)85));
        map.put("Xi", new Character((char)88));
        map.put("Zeta", new Character((char)90));
        map.put("alefsym", new Character((char)192));
        map.put("alpha", new Character((char)97));
        map.put("and", new Character((char)217));
        map.put("ang", new Character((char)208));
        map.put("asymp", new Character((char)187));
        map.put("beta", new Character((char)98));
        map.put("cap", new Character((char)199));
        map.put("chi", new Character((char)99));
        map.put("clubs", new Character((char)167));
        map.put("cong", new Character((char)64));
        map.put("copy", new Character((char)211));
        map.put("crarr", new Character((char)191));
        map.put("cup", new Character((char)200));
        map.put("dArr", new Character((char)223));
        map.put("darr", new Character((char)175));
        map.put("delta", new Character((char)100));
        map.put("diams", new Character((char)168));
        map.put("divide", new Character((char)184));
        map.put("empty", new Character((char)198));
        map.put("epsilon", new Character((char)101));
        map.put("equiv", new Character((char)186));
        map.put("eta", new Character((char)104));
        map.put("euro", new Character((char)240));
        map.put("exist", new Character((char)36));
        map.put("forall", new Character((char)34));
        map.put("frasl", new Character((char)164));
        map.put("gamma", new Character((char)103));
        map.put("ge", new Character((char)179));
        map.put("hArr", new Character((char)219));
        map.put("harr", new Character((char)171));
        map.put("hearts", new Character((char)169));
        map.put("hellip", new Character((char)188));
        map.put("horizontal arrow extender", new Character((char)190));
        map.put("image", new Character((char)193));
        map.put("infin", new Character((char)165));
        map.put("int", new Character((char)242));
        map.put("iota", new Character((char)105));
        map.put("isin", new Character((char)206));
        map.put("kappa", new Character((char)107));
        map.put("lArr", new Character((char)220));
        map.put("lambda", new Character((char)108));
        map.put("lang", new Character((char)225));
        map.put("large brace extender", new Character((char)239));
        map.put("large integral extender", new Character((char)244));
        map.put("large left brace (bottom)", new Character((char)238));
        map.put("large left brace (middle)", new Character((char)237));
        map.put("large left brace (top)", new Character((char)236));
        map.put("large left bracket (bottom)", new Character((char)235));
        map.put("large left bracket (extender)", new Character((char)234));
        map.put("large left bracket (top)", new Character((char)233));
        map.put("large left parenthesis (bottom)", new Character((char)232));
        map.put("large left parenthesis (extender)", new Character((char)231));
        map.put("large left parenthesis (top)", new Character((char)230));
        map.put("large right brace (bottom)", new Character((char)254));
        map.put("large right brace (middle)", new Character((char)253));
        map.put("large right brace (top)", new Character((char)252));
        map.put("large right bracket (bottom)", new Character((char)251));
        map.put("large right bracket (extender)", new Character((char)250));
        map.put("large right bracket (top)", new Character((char)249));
        map.put("large right parenthesis (bottom)", new Character((char)248));
        map.put("large right parenthesis (extender)", new Character((char)247));
        map.put("large right parenthesis (top)", new Character((char)246));
        map.put("larr", new Character((char)172));
        map.put("le", new Character((char)163));
        map.put("lowast", new Character((char)42));
        map.put("loz", new Character((char)224));
        map.put("minus", new Character((char)45));
        map.put("mu", new Character((char)109));
        map.put("nabla", new Character((char)209));
        map.put("ne", new Character((char)185));
        map.put("not", new Character((char)216));
        map.put("notin", new Character((char)207));
        map.put("nsub", new Character((char)203));
        map.put("nu", new Character((char)110));
        map.put("omega", new Character((char)119));
        map.put("omicron", new Character((char)111));
        map.put("oplus", new Character((char)197));
        map.put("or", new Character((char)218));
        map.put("otimes", new Character((char)196));
        map.put("part", new Character((char)182));
        map.put("perp", new Character((char)94));
        map.put("phi", new Character((char)102));
        map.put("pi", new Character((char)112));
        map.put("piv", new Character((char)118));
        map.put("plusmn", new Character((char)177));
        map.put("prime", new Character((char)162));
        map.put("prod", new Character((char)213));
        map.put("prop", new Character((char)181));
        map.put("psi", new Character((char)121));
        map.put("rArr", new Character((char)222));
        map.put("radic", new Character((char)214));
        map.put("radical extender", new Character((char)96));
        map.put("rang", new Character((char)241));
        map.put("rarr", new Character((char)174));
        map.put("real", new Character((char)194));
        map.put("reg", new Character((char)210));
        map.put("rho", new Character((char)114));
        map.put("sdot", new Character((char)215));
        map.put("sigma", new Character((char)115));
        map.put("sigmaf", new Character((char)86));
        map.put("sim", new Character((char)126));
        map.put("spades", new Character((char)170));
        map.put("sub", new Character((char)204));
        map.put("sube", new Character((char)205));
        map.put("sum", new Character((char)229));
        map.put("sup", new Character((char)201));
        map.put("supe", new Character((char)202));
        map.put("tau", new Character((char)116));
        map.put("there4", new Character((char)92));
        map.put("theta", new Character((char)113));
        map.put("thetasym", new Character((char)74));
        map.put("times", new Character((char)180));
        map.put("trade", new Character((char)212));
        map.put("uArr", new Character((char)221));
        map.put("uarr", new Character((char)173));
        map.put("upsih", new Character((char)161));
        map.put("upsilon", new Character((char)117));
        map.put("vertical arrow extender", new Character((char)189));
        map.put("weierp", new Character((char)195));
        map.put("xi", new Character((char)120));
        map.put("zeta", new Character((char)122));
    }
    
    /**
     * Gets a chunk with a symbol character.
     * @param e a symbol value (see Entities class: alfa is greek alfa,...)
     * @param font the font if the symbol isn't found (otherwise Font.SYMBOL)
     * @return a Chunk
     */
    public static Chunk get(String e, Font font) {
        char s = getCorrespondingSymbol(e);
        if (s == (char)0) {
            try {
                return new Chunk(String.valueOf((char)Integer.parseInt(e)), font);
            }
            catch(Exception exception) {
                return new Chunk(e, font);
            }
        }
        Font symbol = new Font(Font.SYMBOL, font.getSize(), font.getStyle(), font.getColor());
        return new Chunk(String.valueOf(s), symbol);
    }
    
    /**
     * Looks for the corresponding symbol in the font Symbol.
     *
     * @param	name	the name of the entity
     * @return	the corresponding character in font Symbol
     */
    public static char getCorrespondingSymbol(String name) {
        Character symbol = (Character) map.get(name);
        if (symbol == null) {
            return (char)0;
        }
        return symbol.charValue();
    }
}
