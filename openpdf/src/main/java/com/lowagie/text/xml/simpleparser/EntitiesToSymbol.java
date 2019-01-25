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
import java.util.Map;

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
    public static final Map<String,Character> map = new HashMap<>();
    static {
        map.put("169", Character.valueOf((char)227));
        map.put("172", Character.valueOf((char)216));
        map.put("174", Character.valueOf((char)210));
        map.put("177", Character.valueOf((char)177));
        map.put("215", Character.valueOf((char)180));
        map.put("247", Character.valueOf((char)184));
        map.put("8230", Character.valueOf((char)188));
        map.put("8242", Character.valueOf((char)162));
        map.put("8243", Character.valueOf((char)178));
        map.put("8260", Character.valueOf((char)164));
        map.put("8364", Character.valueOf((char)240));
        map.put("8465", Character.valueOf((char)193));
        map.put("8472", Character.valueOf((char)195));
        map.put("8476", Character.valueOf((char)194));
        map.put("8482", Character.valueOf((char)212));
        map.put("8501", Character.valueOf((char)192));
        map.put("8592", Character.valueOf((char)172));
        map.put("8593", Character.valueOf((char)173));
        map.put("8594", Character.valueOf((char)174));
        map.put("8595", Character.valueOf((char)175));
        map.put("8596", Character.valueOf((char)171));
        map.put("8629", Character.valueOf((char)191));
        map.put("8656", Character.valueOf((char)220));
        map.put("8657", Character.valueOf((char)221));
        map.put("8658", Character.valueOf((char)222));
        map.put("8659", Character.valueOf((char)223));
        map.put("8660", Character.valueOf((char)219));
        map.put("8704", Character.valueOf((char)34));
        map.put("8706", Character.valueOf((char)182));
        map.put("8707", Character.valueOf((char)36));
        map.put("8709", Character.valueOf((char)198));
        map.put("8711", Character.valueOf((char)209));
        map.put("8712", Character.valueOf((char)206));
        map.put("8713", Character.valueOf((char)207));
        map.put("8717", Character.valueOf((char)39));
        map.put("8719", Character.valueOf((char)213));
        map.put("8721", Character.valueOf((char)229));
        map.put("8722", Character.valueOf((char)45));
        map.put("8727", Character.valueOf((char)42));
        map.put("8729", Character.valueOf((char)183));
        map.put("8730", Character.valueOf((char)214));
        map.put("8733", Character.valueOf((char)181));
        map.put("8734", Character.valueOf((char)165));
        map.put("8736", Character.valueOf((char)208));
        map.put("8743", Character.valueOf((char)217));
        map.put("8744", Character.valueOf((char)218));
        map.put("8745", Character.valueOf((char)199));
        map.put("8746", Character.valueOf((char)200));
        map.put("8747", Character.valueOf((char)242));
        map.put("8756", Character.valueOf((char)92));
        map.put("8764", Character.valueOf((char)126));
        map.put("8773", Character.valueOf((char)64));
        map.put("8776", Character.valueOf((char)187));
        map.put("8800", Character.valueOf((char)185));
        map.put("8801", Character.valueOf((char)186));
        map.put("8804", Character.valueOf((char)163));
        map.put("8805", Character.valueOf((char)179));
        map.put("8834", Character.valueOf((char)204));
        map.put("8835", Character.valueOf((char)201));
        map.put("8836", Character.valueOf((char)203));
        map.put("8838", Character.valueOf((char)205));
        map.put("8839", Character.valueOf((char)202));
        map.put("8853", Character.valueOf((char)197));
        map.put("8855", Character.valueOf((char)196));
        map.put("8869", Character.valueOf((char)94));
        map.put("8901", Character.valueOf((char)215));
        map.put("8992", Character.valueOf((char)243));
        map.put("8993", Character.valueOf((char)245));
        map.put("9001", Character.valueOf((char)225));
        map.put("9002", Character.valueOf((char)241));
        map.put("913", Character.valueOf((char)65));
        map.put("914", Character.valueOf((char)66));
        map.put("915", Character.valueOf((char)71));
        map.put("916", Character.valueOf((char)68));
        map.put("917", Character.valueOf((char)69));
        map.put("918", Character.valueOf((char)90));
        map.put("919", Character.valueOf((char)72));
        map.put("920", Character.valueOf((char)81));
        map.put("921", Character.valueOf((char)73));
        map.put("922", Character.valueOf((char)75));
        map.put("923", Character.valueOf((char)76));
        map.put("924", Character.valueOf((char)77));
        map.put("925", Character.valueOf((char)78));
        map.put("926", Character.valueOf((char)88));
        map.put("927", Character.valueOf((char)79));
        map.put("928", Character.valueOf((char)80));
        map.put("929", Character.valueOf((char)82));
        map.put("931", Character.valueOf((char)83));
        map.put("932", Character.valueOf((char)84));
        map.put("933", Character.valueOf((char)85));
        map.put("934", Character.valueOf((char)70));
        map.put("935", Character.valueOf((char)67));
        map.put("936", Character.valueOf((char)89));
        map.put("937", Character.valueOf((char)87));
        map.put("945", Character.valueOf((char)97));
        map.put("946", Character.valueOf((char)98));
        map.put("947", Character.valueOf((char)103));
        map.put("948", Character.valueOf((char)100));
        map.put("949", Character.valueOf((char)101));
        map.put("950", Character.valueOf((char)122));
        map.put("951", Character.valueOf((char)104));
        map.put("952", Character.valueOf((char)113));
        map.put("953", Character.valueOf((char)105));
        map.put("954", Character.valueOf((char)107));
        map.put("955", Character.valueOf((char)108));
        map.put("956", Character.valueOf((char)109));
        map.put("957", Character.valueOf((char)110));
        map.put("958", Character.valueOf((char)120));
        map.put("959", Character.valueOf((char)111));
        map.put("960", Character.valueOf((char)112));
        map.put("961", Character.valueOf((char)114));
        map.put("962", Character.valueOf((char)86));
        map.put("963", Character.valueOf((char)115));
        map.put("964", Character.valueOf((char)116));
        map.put("965", Character.valueOf((char)117));
        map.put("966", Character.valueOf((char)102));
        map.put("967", Character.valueOf((char)99));
        map.put("9674", Character.valueOf((char)224));
        map.put("968", Character.valueOf((char)121));
        map.put("969", Character.valueOf((char)119));
        map.put("977", Character.valueOf((char)74));
        map.put("978", Character.valueOf((char)161));
        map.put("981", Character.valueOf((char)106));
        map.put("982", Character.valueOf((char)118));
        map.put("9824", Character.valueOf((char)170));
        map.put("9827", Character.valueOf((char)167));
        map.put("9829", Character.valueOf((char)169));
        map.put("9830", Character.valueOf((char)168));
        map.put("Alpha", Character.valueOf((char)65));
        map.put("Beta", Character.valueOf((char)66));
        map.put("Chi", Character.valueOf((char)67));
        map.put("Delta", Character.valueOf((char)68));
        map.put("Epsilon", Character.valueOf((char)69));
        map.put("Eta", Character.valueOf((char)72));
        map.put("Gamma", Character.valueOf((char)71));
        map.put("Iota", Character.valueOf((char)73));
        map.put("Kappa", Character.valueOf((char)75));
        map.put("Lambda", Character.valueOf((char)76));
        map.put("Mu", Character.valueOf((char)77));
        map.put("Nu", Character.valueOf((char)78));
        map.put("Omega", Character.valueOf((char)87));
        map.put("Omicron", Character.valueOf((char)79));
        map.put("Phi", Character.valueOf((char)70));
        map.put("Pi", Character.valueOf((char)80));
        map.put("Prime", Character.valueOf((char)178));
        map.put("Psi", Character.valueOf((char)89));
        map.put("Rho", Character.valueOf((char)82));
        map.put("Sigma", Character.valueOf((char)83));
        map.put("Tau", Character.valueOf((char)84));
        map.put("Theta", Character.valueOf((char)81));
        map.put("Upsilon", Character.valueOf((char)85));
        map.put("Xi", Character.valueOf((char)88));
        map.put("Zeta", Character.valueOf((char)90));
        map.put("alefsym", Character.valueOf((char)192));
        map.put("alpha", Character.valueOf((char)97));
        map.put("and", Character.valueOf((char)217));
        map.put("ang", Character.valueOf((char)208));
        map.put("asymp", Character.valueOf((char)187));
        map.put("beta", Character.valueOf((char)98));
        map.put("cap", Character.valueOf((char)199));
        map.put("chi", Character.valueOf((char)99));
        map.put("clubs", Character.valueOf((char)167));
        map.put("cong", Character.valueOf((char)64));
        map.put("copy", Character.valueOf((char)211));
        map.put("crarr", Character.valueOf((char)191));
        map.put("cup", Character.valueOf((char)200));
        map.put("dArr", Character.valueOf((char)223));
        map.put("darr", Character.valueOf((char)175));
        map.put("delta", Character.valueOf((char)100));
        map.put("diams", Character.valueOf((char)168));
        map.put("divide", Character.valueOf((char)184));
        map.put("empty", Character.valueOf((char)198));
        map.put("epsilon", Character.valueOf((char)101));
        map.put("equiv", Character.valueOf((char)186));
        map.put("eta", Character.valueOf((char)104));
        map.put("euro", Character.valueOf((char)240));
        map.put("exist", Character.valueOf((char)36));
        map.put("forall", Character.valueOf((char)34));
        map.put("frasl", Character.valueOf((char)164));
        map.put("gamma", Character.valueOf((char)103));
        map.put("ge", Character.valueOf((char)179));
        map.put("hArr", Character.valueOf((char)219));
        map.put("harr", Character.valueOf((char)171));
        map.put("hearts", Character.valueOf((char)169));
        map.put("hellip", Character.valueOf((char)188));
        map.put("horizontal arrow extender", Character.valueOf((char)190));
        map.put("image", Character.valueOf((char)193));
        map.put("infin", Character.valueOf((char)165));
        map.put("int", Character.valueOf((char)242));
        map.put("iota", Character.valueOf((char)105));
        map.put("isin", Character.valueOf((char)206));
        map.put("kappa", Character.valueOf((char)107));
        map.put("lArr", Character.valueOf((char)220));
        map.put("lambda", Character.valueOf((char)108));
        map.put("lang", Character.valueOf((char)225));
        map.put("large brace extender", Character.valueOf((char)239));
        map.put("large integral extender", Character.valueOf((char)244));
        map.put("large left brace (bottom)", Character.valueOf((char)238));
        map.put("large left brace (middle)", Character.valueOf((char)237));
        map.put("large left brace (top)", Character.valueOf((char)236));
        map.put("large left bracket (bottom)", Character.valueOf((char)235));
        map.put("large left bracket (extender)", Character.valueOf((char)234));
        map.put("large left bracket (top)", Character.valueOf((char)233));
        map.put("large left parenthesis (bottom)", Character.valueOf((char)232));
        map.put("large left parenthesis (extender)", Character.valueOf((char)231));
        map.put("large left parenthesis (top)", Character.valueOf((char)230));
        map.put("large right brace (bottom)", Character.valueOf((char)254));
        map.put("large right brace (middle)", Character.valueOf((char)253));
        map.put("large right brace (top)", Character.valueOf((char)252));
        map.put("large right bracket (bottom)", Character.valueOf((char)251));
        map.put("large right bracket (extender)", Character.valueOf((char)250));
        map.put("large right bracket (top)", Character.valueOf((char)249));
        map.put("large right parenthesis (bottom)", Character.valueOf((char)248));
        map.put("large right parenthesis (extender)", Character.valueOf((char)247));
        map.put("large right parenthesis (top)", Character.valueOf((char)246));
        map.put("larr", Character.valueOf((char)172));
        map.put("le", Character.valueOf((char)163));
        map.put("lowast", Character.valueOf((char)42));
        map.put("loz", Character.valueOf((char)224));
        map.put("minus", Character.valueOf((char)45));
        map.put("mu", Character.valueOf((char)109));
        map.put("nabla", Character.valueOf((char)209));
        map.put("ne", Character.valueOf((char)185));
        map.put("not", Character.valueOf((char)216));
        map.put("notin", Character.valueOf((char)207));
        map.put("nsub", Character.valueOf((char)203));
        map.put("nu", Character.valueOf((char)110));
        map.put("omega", Character.valueOf((char)119));
        map.put("omicron", Character.valueOf((char)111));
        map.put("oplus", Character.valueOf((char)197));
        map.put("or", Character.valueOf((char)218));
        map.put("otimes", Character.valueOf((char)196));
        map.put("part", Character.valueOf((char)182));
        map.put("perp", Character.valueOf((char)94));
        map.put("phi", Character.valueOf((char)102));
        map.put("pi", Character.valueOf((char)112));
        map.put("piv", Character.valueOf((char)118));
        map.put("plusmn", Character.valueOf((char)177));
        map.put("prime", Character.valueOf((char)162));
        map.put("prod", Character.valueOf((char)213));
        map.put("prop", Character.valueOf((char)181));
        map.put("psi", Character.valueOf((char)121));
        map.put("rArr", Character.valueOf((char)222));
        map.put("radic", Character.valueOf((char)214));
        map.put("radical extender", Character.valueOf((char)96));
        map.put("rang", Character.valueOf((char)241));
        map.put("rarr", Character.valueOf((char)174));
        map.put("real", Character.valueOf((char)194));
        map.put("reg", Character.valueOf((char)210));
        map.put("rho", Character.valueOf((char)114));
        map.put("sdot", Character.valueOf((char)215));
        map.put("sigma", Character.valueOf((char)115));
        map.put("sigmaf", Character.valueOf((char)86));
        map.put("sim", Character.valueOf((char)126));
        map.put("spades", Character.valueOf((char)170));
        map.put("sub", Character.valueOf((char)204));
        map.put("sube", Character.valueOf((char)205));
        map.put("sum", Character.valueOf((char)229));
        map.put("sup", Character.valueOf((char)201));
        map.put("supe", Character.valueOf((char)202));
        map.put("tau", Character.valueOf((char)116));
        map.put("there4", Character.valueOf((char)92));
        map.put("theta", Character.valueOf((char)113));
        map.put("thetasym", Character.valueOf((char)74));
        map.put("times", Character.valueOf((char)180));
        map.put("trade", Character.valueOf((char)212));
        map.put("uArr", Character.valueOf((char)221));
        map.put("uarr", Character.valueOf((char)173));
        map.put("upsih", Character.valueOf((char)161));
        map.put("upsilon", Character.valueOf((char)117));
        map.put("vertical arrow extender", Character.valueOf((char)189));
        map.put("weierp", Character.valueOf((char)195));
        map.put("xi", Character.valueOf((char)120));
        map.put("zeta", Character.valueOf((char)122));
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
     * @param    name    the name of the entity
     * @return    the corresponding character in font Symbol
     */
    public static char getCorrespondingSymbol(String name) {
        Character symbol = map.get(name);
        if (symbol == null) {
            return (char)0;
        }
        return symbol.charValue();
    }
}
