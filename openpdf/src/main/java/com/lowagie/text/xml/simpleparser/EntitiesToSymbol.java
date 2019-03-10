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

import com.lowagie.text.Chunk;
import com.lowagie.text.Font;

import java.util.HashMap;

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
        map.put("169", (char) 227);
        map.put("172", (char) 216);
        map.put("174", (char) 210);
        map.put("177", (char) 177);
        map.put("215", (char) 180);
        map.put("247", (char) 184);
        map.put("8230", (char) 188);
        map.put("8242", (char) 162);
        map.put("8243", (char) 178);
        map.put("8260", (char) 164);
        map.put("8364", (char) 240);
        map.put("8465", (char) 193);
        map.put("8472", (char) 195);
        map.put("8476", (char) 194);
        map.put("8482", (char) 212);
        map.put("8501", (char) 192);
        map.put("8592", (char) 172);
        map.put("8593", (char) 173);
        map.put("8594", (char) 174);
        map.put("8595", (char) 175);
        map.put("8596", (char) 171);
        map.put("8629", (char) 191);
        map.put("8656", (char) 220);
        map.put("8657", (char) 221);
        map.put("8658", (char) 222);
        map.put("8659", (char) 223);
        map.put("8660", (char) 219);
        map.put("8704", (char) 34);
        map.put("8706", (char) 182);
        map.put("8707", (char) 36);
        map.put("8709", (char) 198);
        map.put("8711", (char) 209);
        map.put("8712", (char) 206);
        map.put("8713", (char) 207);
        map.put("8717", (char) 39);
        map.put("8719", (char) 213);
        map.put("8721", (char) 229);
        map.put("8722", (char) 45);
        map.put("8727", (char) 42);
        map.put("8729", (char) 183);
        map.put("8730", (char) 214);
        map.put("8733", (char) 181);
        map.put("8734", (char) 165);
        map.put("8736", (char) 208);
        map.put("8743", (char) 217);
        map.put("8744", (char) 218);
        map.put("8745", (char) 199);
        map.put("8746", (char) 200);
        map.put("8747", (char) 242);
        map.put("8756", (char) 92);
        map.put("8764", (char) 126);
        map.put("8773", (char) 64);
        map.put("8776", (char) 187);
        map.put("8800", (char) 185);
        map.put("8801", (char) 186);
        map.put("8804", (char) 163);
        map.put("8805", (char) 179);
        map.put("8834", (char) 204);
        map.put("8835", (char) 201);
        map.put("8836", (char) 203);
        map.put("8838", (char) 205);
        map.put("8839", (char) 202);
        map.put("8853", (char) 197);
        map.put("8855", (char) 196);
        map.put("8869", (char) 94);
        map.put("8901", (char) 215);
        map.put("8992", (char) 243);
        map.put("8993", (char) 245);
        map.put("9001", (char) 225);
        map.put("9002", (char) 241);
        map.put("913", (char) 65);
        map.put("914", (char) 66);
        map.put("915", (char) 71);
        map.put("916", (char) 68);
        map.put("917", (char) 69);
        map.put("918", (char) 90);
        map.put("919", (char) 72);
        map.put("920", (char) 81);
        map.put("921", (char) 73);
        map.put("922", (char) 75);
        map.put("923", (char) 76);
        map.put("924", (char) 77);
        map.put("925", (char) 78);
        map.put("926", (char) 88);
        map.put("927", (char) 79);
        map.put("928", (char) 80);
        map.put("929", (char) 82);
        map.put("931", (char) 83);
        map.put("932", (char) 84);
        map.put("933", (char) 85);
        map.put("934", (char) 70);
        map.put("935", (char) 67);
        map.put("936", (char) 89);
        map.put("937", (char) 87);
        map.put("945", (char) 97);
        map.put("946", (char) 98);
        map.put("947", (char) 103);
        map.put("948", (char) 100);
        map.put("949", (char) 101);
        map.put("950", (char) 122);
        map.put("951", (char) 104);
        map.put("952", (char) 113);
        map.put("953", (char) 105);
        map.put("954", (char) 107);
        map.put("955", (char) 108);
        map.put("956", (char) 109);
        map.put("957", (char) 110);
        map.put("958", (char) 120);
        map.put("959", (char) 111);
        map.put("960", (char) 112);
        map.put("961", (char) 114);
        map.put("962", (char) 86);
        map.put("963", (char) 115);
        map.put("964", (char) 116);
        map.put("965", (char) 117);
        map.put("966", (char) 102);
        map.put("967", (char) 99);
        map.put("9674", (char) 224);
        map.put("968", (char) 121);
        map.put("969", (char) 119);
        map.put("977", (char) 74);
        map.put("978", (char) 161);
        map.put("981", (char) 106);
        map.put("982", (char) 118);
        map.put("9824", (char) 170);
        map.put("9827", (char) 167);
        map.put("9829", (char) 169);
        map.put("9830", (char) 168);
        map.put("Alpha", (char) 65);
        map.put("Beta", (char) 66);
        map.put("Chi", (char) 67);
        map.put("Delta", (char) 68);
        map.put("Epsilon", (char) 69);
        map.put("Eta", (char) 72);
        map.put("Gamma", (char) 71);
        map.put("Iota", (char) 73);
        map.put("Kappa", (char) 75);
        map.put("Lambda", (char) 76);
        map.put("Mu", (char) 77);
        map.put("Nu", (char) 78);
        map.put("Omega", (char) 87);
        map.put("Omicron", (char) 79);
        map.put("Phi", (char) 70);
        map.put("Pi", (char) 80);
        map.put("Prime", (char) 178);
        map.put("Psi", (char) 89);
        map.put("Rho", (char) 82);
        map.put("Sigma", (char) 83);
        map.put("Tau", (char) 84);
        map.put("Theta", (char) 81);
        map.put("Upsilon", (char) 85);
        map.put("Xi", (char) 88);
        map.put("Zeta", (char) 90);
        map.put("alefsym", (char) 192);
        map.put("alpha", (char) 97);
        map.put("and", (char) 217);
        map.put("ang", (char) 208);
        map.put("asymp", (char) 187);
        map.put("beta", (char) 98);
        map.put("cap", (char) 199);
        map.put("chi", (char) 99);
        map.put("clubs", (char) 167);
        map.put("cong", (char) 64);
        map.put("copy", (char) 211);
        map.put("crarr", (char) 191);
        map.put("cup", (char) 200);
        map.put("dArr", (char) 223);
        map.put("darr", (char) 175);
        map.put("delta", (char) 100);
        map.put("diams", (char) 168);
        map.put("divide", (char) 184);
        map.put("empty", (char) 198);
        map.put("epsilon", (char) 101);
        map.put("equiv", (char) 186);
        map.put("eta", (char) 104);
        map.put("euro", (char) 240);
        map.put("exist", (char) 36);
        map.put("forall", (char) 34);
        map.put("frasl", (char) 164);
        map.put("gamma", (char) 103);
        map.put("ge", (char) 179);
        map.put("hArr", (char) 219);
        map.put("harr", (char) 171);
        map.put("hearts", (char) 169);
        map.put("hellip", (char) 188);
        map.put("horizontal arrow extender", (char) 190);
        map.put("image", (char) 193);
        map.put("infin", (char) 165);
        map.put("int", (char) 242);
        map.put("iota", (char) 105);
        map.put("isin", (char) 206);
        map.put("kappa", (char) 107);
        map.put("lArr", (char) 220);
        map.put("lambda", (char) 108);
        map.put("lang", (char) 225);
        map.put("large brace extender", (char) 239);
        map.put("large integral extender", (char) 244);
        map.put("large left brace (bottom)", (char) 238);
        map.put("large left brace (middle)", (char) 237);
        map.put("large left brace (top)", (char) 236);
        map.put("large left bracket (bottom)", (char) 235);
        map.put("large left bracket (extender)", (char) 234);
        map.put("large left bracket (top)", (char) 233);
        map.put("large left parenthesis (bottom)", (char) 232);
        map.put("large left parenthesis (extender)", (char) 231);
        map.put("large left parenthesis (top)", (char) 230);
        map.put("large right brace (bottom)", (char) 254);
        map.put("large right brace (middle)", (char) 253);
        map.put("large right brace (top)", (char) 252);
        map.put("large right bracket (bottom)", (char) 251);
        map.put("large right bracket (extender)", (char) 250);
        map.put("large right bracket (top)", (char) 249);
        map.put("large right parenthesis (bottom)", (char) 248);
        map.put("large right parenthesis (extender)", (char) 247);
        map.put("large right parenthesis (top)", (char) 246);
        map.put("larr", (char) 172);
        map.put("le", (char) 163);
        map.put("lowast", (char) 42);
        map.put("loz", (char) 224);
        map.put("minus", (char) 45);
        map.put("mu", (char) 109);
        map.put("nabla", (char) 209);
        map.put("ne", (char) 185);
        map.put("not", (char) 216);
        map.put("notin", (char) 207);
        map.put("nsub", (char) 203);
        map.put("nu", (char) 110);
        map.put("omega", (char) 119);
        map.put("omicron", (char) 111);
        map.put("oplus", (char) 197);
        map.put("or", (char) 218);
        map.put("otimes", (char) 196);
        map.put("part", (char) 182);
        map.put("perp", (char) 94);
        map.put("phi", (char) 102);
        map.put("pi", (char) 112);
        map.put("piv", (char) 118);
        map.put("plusmn", (char) 177);
        map.put("prime", (char) 162);
        map.put("prod", (char) 213);
        map.put("prop", (char) 181);
        map.put("psi", (char) 121);
        map.put("rArr", (char) 222);
        map.put("radic", (char) 214);
        map.put("radical extender", (char) 96);
        map.put("rang", (char) 241);
        map.put("rarr", (char) 174);
        map.put("real", (char) 194);
        map.put("reg", (char) 210);
        map.put("rho", (char) 114);
        map.put("sdot", (char) 215);
        map.put("sigma", (char) 115);
        map.put("sigmaf", (char) 86);
        map.put("sim", (char) 126);
        map.put("spades", (char) 170);
        map.put("sub", (char) 204);
        map.put("sube", (char) 205);
        map.put("sum", (char) 229);
        map.put("sup", (char) 201);
        map.put("supe", (char) 202);
        map.put("tau", (char) 116);
        map.put("there4", (char) 92);
        map.put("theta", (char) 113);
        map.put("thetasym", (char) 74);
        map.put("times", (char) 180);
        map.put("trade", (char) 212);
        map.put("uArr", (char) 221);
        map.put("uarr", (char) 173);
        map.put("upsih", (char) 161);
        map.put("upsilon", (char) 117);
        map.put("vertical arrow extender", (char) 189);
        map.put("weierp", (char) 195);
        map.put("xi", (char) 120);
        map.put("zeta", (char) 122);
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
        Character symbol = (Character) map.get(name);
        if (symbol == null) {
            return (char)0;
        }
        return symbol;
    }
}
