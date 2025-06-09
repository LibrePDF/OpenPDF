/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.layout;

import org.openpdf.css.constants.IdentValue;

import java.util.Iterator;
import java.util.List;

import static java.util.Locale.ROOT;

public class CounterFunction {
    private final IdentValue _listStyleType;
    private int _counterValue;
    private List<Integer> _counterValues;
    private String _separator;

    public CounterFunction(int counterValue, IdentValue listStyleType) {
        _counterValue = counterValue;
        _listStyleType = listStyleType;
    }

    public CounterFunction(List<Integer> counterValues, String separator, IdentValue listStyleType) {
        _counterValues = counterValues;
        _separator = separator;
        _listStyleType = listStyleType;
    }

    public String evaluate() {
        if (_counterValues == null) {
            return createCounterText(_listStyleType, _counterValue);
        }
        StringBuilder sb = new StringBuilder();
        for (Iterator<Integer> i = _counterValues.iterator(); i.hasNext();) {
            Integer value = i.next();
            sb.append(createCounterText(_listStyleType, value));
            if (i.hasNext()) sb.append(_separator);
        }
        return sb.toString();
    }

    public static String createCounterText(IdentValue listStyle, int listCounter) {
        if (listStyle == IdentValue.LOWER_LATIN || listStyle == IdentValue.LOWER_ALPHA) {
            return toLatin(listCounter).toLowerCase(ROOT);
        } else if (listStyle == IdentValue.UPPER_LATIN || listStyle == IdentValue.UPPER_ALPHA) {
            return toLatin(listCounter).toUpperCase(ROOT);
        } else if (listStyle == IdentValue.LOWER_ROMAN) {
            return toRoman(listCounter).toLowerCase(ROOT);
        } else if (listStyle == IdentValue.UPPER_ROMAN) {
            return toRoman(listCounter).toUpperCase(ROOT);
        } else if (listStyle == IdentValue.DECIMAL_LEADING_ZERO) {
            return (listCounter >= 10 ? "" : "0") + listCounter;
        } else { // listStyle == IdentValue.DECIMAL or anything else
            return Integer.toString(listCounter);
        }
    }


    private static String toLatin(int index) {
        StringBuilder result = new StringBuilder(5);
        int val = index - 1;
        while (val >= 0) {
            int letter = val % 26;
            val = val / 26 - 1;
            result.insert(0, (char) (letter + 65));
        }
        return result.toString();
    }

    private static String toRoman(int val) {
        int[] ints = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] nums = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ints.length; i++) {
            int count = val / ints[i];
            sb.append(nums[i].repeat(Math.max(0, count)));
            val -= ints[i] * count;
        }
        return sb.toString();
    }
}
