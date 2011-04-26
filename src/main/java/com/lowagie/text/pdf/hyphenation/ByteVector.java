/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lowagie.text.pdf.hyphenation;

import java.io.Serializable;

/**
 * This class implements a simple byte vector with access to the
 * underlying array.
 *
 * @author Carlos Villegas <cav@uniscope.co.jp>
 */
public class ByteVector implements Serializable {

    private static final long serialVersionUID = -1096301185375029343L;
	/**
     * Capacity increment size
     */
    private static final int DEFAULT_BLOCK_SIZE = 2048;
    private int blockSize;

    /**
     * The encapsulated array
     */
    private byte[] array;

    /**
     * Points to next free item
     */
    private int n;

    public ByteVector() {
        this(DEFAULT_BLOCK_SIZE);
    }

    public ByteVector(int capacity) {
        if (capacity > 0) {
            blockSize = capacity;
        } else {
            blockSize = DEFAULT_BLOCK_SIZE;
        }
        array = new byte[blockSize];
        n = 0;
    }

    public ByteVector(byte[] a) {
        blockSize = DEFAULT_BLOCK_SIZE;
        array = a;
        n = 0;
    }

    public ByteVector(byte[] a, int capacity) {
        if (capacity > 0) {
            blockSize = capacity;
        } else {
            blockSize = DEFAULT_BLOCK_SIZE;
        }
        array = a;
        n = 0;
    }

    public byte[] getArray() {
        return array;
    }

    /**
     * return number of items in array
     */
    public int length() {
        return n;
    }

    /**
     * returns current capacity of array
     */
    public int capacity() {
        return array.length;
    }

    public void put(int index, byte val) {
        array[index] = val;
    }

    public byte get(int index) {
        return array[index];
    }

    /**
     * This is to implement memory allocation in the array. Like malloc().
     */
    public int alloc(int size) {
        int index = n;
        int len = array.length;
        if (n + size >= len) {
            byte[] aux = new byte[len + blockSize];
            System.arraycopy(array, 0, aux, 0, len);
            array = aux;
        }
        n += size;
        return index;
    }

    public void trimToSize() {
        if (n < array.length) {
            byte[] aux = new byte[n];
            System.arraycopy(array, 0, aux, 0, n);
            array = aux;
        }
    }

}
