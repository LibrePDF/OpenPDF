/*
 * {{{ header & license
 * Copyright (c) 2009 Patrick Wright
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
package org.openpdf.util;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;

public class ArrayUtil {
    @CheckReturnValue
    public static String[] cloneOrEmpty(String @Nullable [] source){
        return source == null ? Constants.EMPTY_STR_ARR : source.clone();
    }

    @CheckReturnValue
    public static byte[] cloneOrEmpty(byte @Nullable [] source){
        return source == null ? Constants.EMPTY_BYTE_ARR : source.clone();
    }

    @CheckReturnValue
    public static int[] cloneOrEmpty(int @Nullable [] source) {
        return source == null ? Constants.EMPTY_INT_ARR : source.clone();
    }
}
