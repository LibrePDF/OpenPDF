/*
 * {{{ header & license
 * ValueConstants.java
 * Copyright (c) 2004, 2005 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.css.constants;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.openpdf.util.GeneralUtil;
import org.openpdf.util.XRLog;
import org.openpdf.util.XRRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;

import static java.lang.Float.parseFloat;


/**
 * Utility class for working with {@code CSSValue} instances.
 */
public final class ValueConstants {
    /**
     * Type descriptions--a crude approximation taken by scanning CSSValue statics
     */
    private static final List<String> TYPE_DESCRIPTIONS = new ArrayList<>();
    private static final Map<Short, String> sacTypesStrings = new HashMap<>(25);

    /**
     * A text representation of the CSS type for this value.
     */
    public static String cssType(int cssType, int primitiveValueType) {
        if (cssType == CSSValue.CSS_PRIMITIVE_VALUE) {
            if (primitiveValueType >= TYPE_DESCRIPTIONS.size()) {
                return "{unknown: " + primitiveValueType + "}";
            } else {
                String desc = TYPE_DESCRIPTIONS.get(primitiveValueType);
                return desc == null ? "{UNKNOWN VALUE TYPE}" : desc;
            }
        } else {
            return "{value list}";
        }
    }

    public static short sacPrimitiveTypeForString(@Nullable String type) {
        if ("deg".equals(type)) {
            return CSSPrimitiveValue.CSS_DEG;
        } else if ("rad".equals(type)) {
            return CSSPrimitiveValue.CSS_RAD;
        } else if (type == null) {
            //this is only valid if length is 0
            return CSSPrimitiveValue.CSS_PX;
        }

        return switch (type) {
            case "em" -> CSSPrimitiveValue.CSS_EMS;
            case "ex" -> CSSPrimitiveValue.CSS_EXS;
            case "px" -> CSSPrimitiveValue.CSS_PX;
            case "%" -> CSSPrimitiveValue.CSS_PERCENTAGE;
            case "in" -> CSSPrimitiveValue.CSS_IN;
            case "cm" -> CSSPrimitiveValue.CSS_CM;
            case "mm" -> CSSPrimitiveValue.CSS_MM;
            case "pt" -> CSSPrimitiveValue.CSS_PT;
            case "pc" -> CSSPrimitiveValue.CSS_PC;
            default -> throw new XRRuntimeException("Unknown type on CSS value: " + type);
        };
    }

    public static String stringForSACPrimitiveType(short type) {
        return sacTypesStrings.get(type);
    }

    /**
     * Returns true if the specified value was absolute (even if we have a
     * computed value for it), meaning that either the value can be used
     * directly (e.g. pixels) or there is a fixed context-independent conversion
     * for it (e.g. inches). Proportional types (e.g. %) return false.
     *
     * @param primitive The CSSValue instance to check.
     */
    //TODO: method may be unnecessary (tobe)
    public static boolean isAbsoluteUnit(CSSPrimitiveValue primitive) {
        short type = primitive.getPrimitiveType();
        return isAbsoluteUnit(type);
    }

    /**
     * Returns true if the specified type absolute (even if we have a computed
     * value for it), meaning that either the value can be used directly (e.g.
     * pixels) or there is a fixed context-independent conversion for it (e.g.
     * inches). Proportional types (e.g. %) return false.
     *
     * @param type The CSSValue type to check.
     */
    //TODO: method may be unnecessary (tobe)
    public static boolean isAbsoluteUnit(short type) {
        // TODO: check this list...

        // note, all types are included here to make sure none are missed
        switch (type) {
            // proportional length or size
            case CSSPrimitiveValue.CSS_PERCENTAGE:
                return false;
                // refer to values known to the DerivedValue instance (tobe)
            case CSSPrimitiveValue.CSS_EMS:
            case CSSPrimitiveValue.CSS_EXS:
                // length
            case CSSPrimitiveValue.CSS_IN:
            case CSSPrimitiveValue.CSS_CM:
            case CSSPrimitiveValue.CSS_MM:
            case CSSPrimitiveValue.CSS_PT:
            case CSSPrimitiveValue.CSS_PC:
            case CSSPrimitiveValue.CSS_PX:

                // color
            case CSSPrimitiveValue.CSS_RGBCOLOR:

                // ?
            case CSSPrimitiveValue.CSS_ATTR:
            case CSSPrimitiveValue.CSS_DIMENSION:
            case CSSPrimitiveValue.CSS_NUMBER:
            case CSSPrimitiveValue.CSS_RECT:

                // counters
            case CSSPrimitiveValue.CSS_COUNTER:

                // angles
            case CSSPrimitiveValue.CSS_DEG:
            case CSSPrimitiveValue.CSS_GRAD:
            case CSSPrimitiveValue.CSS_RAD:

                // aural - freq
            case CSSPrimitiveValue.CSS_HZ:
            case CSSPrimitiveValue.CSS_KHZ:

                // time
            case CSSPrimitiveValue.CSS_S:
            case CSSPrimitiveValue.CSS_MS:

                // URI
            case CSSPrimitiveValue.CSS_URI:

            case CSSPrimitiveValue.CSS_IDENT:
            case CSSPrimitiveValue.CSS_STRING:
                return true;
            case CSSPrimitiveValue.CSS_UNKNOWN:
                XRLog.cascade(Level.WARNING, "Asked whether type was absolute, given CSS_UNKNOWN as the type. " +
                        "Might be one of those funny values like background-position.");
                GeneralUtil.dumpShortException(new Exception("Taking a thread dump..."));
                // fall-through
            default:
                return false;
        }
    }

    /**
     * Gets the cssValueTypeDesc attribute of the {@link CSSValue} object
     */
    public static String getCssValueTypeDesc(CSSValue cssValue) {
        return switch (cssValue.getCssValueType()) {
            case CSSValue.CSS_CUSTOM -> "CSS_CUSTOM";
            case CSSValue.CSS_INHERIT -> "CSS_INHERIT";
            case CSSValue.CSS_PRIMITIVE_VALUE -> "CSS_PRIMITIVE_VALUE";
            case CSSValue.CSS_VALUE_LIST -> "CSS_VALUE_LIST";
            default -> "UNKNOWN";
        };
    }

    /**
     * Returns true if the SAC primitive value type is a number unit--a unit
     * that can only contain a numeric value. This is a shorthand way of saying,
     * did the user declare this as a number unit (like px)?
     */
    public static boolean isNumber(short cssPrimitiveType) {
        return switch (cssPrimitiveType) {
            // fall through on all these
            // relative length or size
            case CSSPrimitiveValue.CSS_EMS, CSSPrimitiveValue.CSS_EXS, CSSPrimitiveValue.CSS_PERCENTAGE ->
                // relatives will be treated separately from lengths;
                    false;
            // length
            case CSSPrimitiveValue.CSS_PX, CSSPrimitiveValue.CSS_IN, CSSPrimitiveValue.CSS_CM, CSSPrimitiveValue.CSS_MM, CSSPrimitiveValue.CSS_PT, CSSPrimitiveValue.CSS_PC ->
                    true;
            default -> false;
        };
    }

    static {
        SortedMap<Short, String> map = new TreeMap<>();
        try {
            Field[] fields = CSSPrimitiveValue.class.getFields();
            for (Field f : fields) {
                int mod = f.getModifiers();
                if (Modifier.isFinal(mod) &&
                        Modifier.isStatic(mod) &&
                        Modifier.isPublic(mod)) {

                    Short val = (Short) f.get(null);
                    String name = f.getName();
                    if (name.startsWith("CSS_")) {
                        if (!name.equals("CSS_INHERIT") &&
                                !name.equals("CSS_PRIMITIVE_VALUE") &&
                                !name.equals("CSS_VALUE_LIST") &&
                                !name.equals("CSS_CUSTOM")) {

                            map.put(val, name.substring("CSS_".length()));
                        }
                    }
                }
            }
            // now sort by the key--the short constant for the public fields
            List<Short> keys = new ArrayList<>(map.keySet());
            Collections.sort(keys);

            // then add to our static list, in the order the keys appear. this means
            // list.get(index) will return the item at index, which should be the description
            // for that constant
            for (Short key : keys) {
                TYPE_DESCRIPTIONS.add(map.get(key));
            }
        } catch (Exception ex) {
            throw new XRRuntimeException("Could not build static list of CSS type descriptions.", ex);
        }

        // HACK: this is a quick way to perform the lookup, but dumb if the short assigned are > 100; but the compiler will tell us that (PWW 21-01-05)
        sacTypesStrings.put(CSSPrimitiveValue.CSS_EMS, "em");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_EXS, "ex");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_PX, "px");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_PERCENTAGE, "%");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_IN, "in");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_CM, "cm");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_MM, "mm");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_PT, "pt");
        sacTypesStrings.put(CSSPrimitiveValue.CSS_PC, "pc");
    }

    /**
     * Incomplete routine to try and determine the
     * CSSPrimitiveValue short code for a given value,
     * e.g. 14pt is CSS_PT.
     */
    public static short guessType(@Nullable String value) {
        if (value != null && value.length() > 1) {
            if (value.endsWith("%")) {
                return CSSPrimitiveValue.CSS_PERCENTAGE;
            } else if (value.startsWith("rgb") || value.startsWith("#")) {
                return CSSPrimitiveValue.CSS_RGBCOLOR;
            } else {
                String hmm = value.substring(value.length() - 2);
                return guessTypeByFont(value, hmm);
            }
        }
        return CSSPrimitiveValue.CSS_STRING;
    }

    private static short guessTypeByFont(String value, String hmm) {
        switch (hmm) {
            case "pt":
                return CSSPrimitiveValue.CSS_PT;
            case "px":
                return CSSPrimitiveValue.CSS_PX;
            case "em":
                return CSSPrimitiveValue.CSS_EMS;
            case "ex":
                return CSSPrimitiveValue.CSS_EXS;
            case "in":
                return CSSPrimitiveValue.CSS_IN;
            case "cm":
                return CSSPrimitiveValue.CSS_CM;
            case "mm":
                return CSSPrimitiveValue.CSS_MM;
            default:
                if (Character.isDigit(value.charAt(value.length() - 1))) {
                    try {
                        parseFloat(value);
                        return CSSPrimitiveValue.CSS_NUMBER;
                    } catch (NumberFormatException ex) {
                        return CSSPrimitiveValue.CSS_STRING;
                    }
                } else {
                    return CSSPrimitiveValue.CSS_STRING;
                }
        }
    }
}

