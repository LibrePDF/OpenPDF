/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.sun.pdfview;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sun.pdfview.decode.PDFDecoder;
import com.sun.pdfview.decrypt.IdentityDecrypter;
import com.sun.pdfview.decrypt.PDFDecrypter;

/**
 * a class encapsulating all the possibilities of content for
 * an object in a PDF file.
 * <p>
 * A PDF object can be a simple type, like a Boolean, a Number,
 * a String, or the Null value.  It can also be a NAME, which
 * looks like a string, but is a special type in PDF files, like
 * "/Name".
 * <p>
 * A PDF object can also be complex types, including Array;
 * Dictionary; Stream, which is a Dictionary plus an array of
 * bytes; or Indirect, which is a reference to some other
 * PDF object.  Indirect references will always be dereferenced
 * by the time any data is returned from one of the methods
 * in this class.
 *
 * @author Mike Wessler
 */
public class PDFObject {

    /** an indirect reference*/
    public static final int INDIRECT = 0;      // PDFXref
    /** a Boolean */
    public static final int BOOLEAN = 1;      // Boolean
    /** a Number, represented as a double */
    public static final int NUMBER = 2;       // Double
    /** a String */
    public static final int STRING = 3;       // String
    /** a special string, seen in PDF files as /Name */
    public static final int NAME = 4;         // String
    /** an array of PDFObjects */
    public static final int ARRAY = 5;        // Array of PDFObject
    /** a Hashmap that maps String names to PDFObjects */
    public static final int DICTIONARY = 6;   // HashMap(String->PDFObject)
    /** a Stream: a Hashmap with a byte array */
    public static final int STREAM = 7;        // HashMap + byte[]
    /** the NULL object (there is only one) */
    public static final int NULL = 8;         // null
    /** a special PDF bare word, like R, obj, true, false, etc */
    public static final int KEYWORD = 9;      // String
    /**
     * When a value of {@link #getObjGen objNum} or {@link #getObjGen objGen},
     * indicates that the object is not top-level, and is embedded in another
     * object
     */
    public static final int OBJ_NUM_EMBEDDED = -2;

    /**
     * When a value of {@link #getObjGen objNum} or {@link #getObjGen objGen},
     * indicates that the object is not top-level, and is embedded directly
     * in the trailer.
     */
    public static final int OBJ_NUM_TRAILER = -1;

    /** the NULL PDFObject */
    public static final PDFObject nullObj = new PDFObject(null, NULL, null);
    /** the type of this object */
    private int type;
    /** the value of this object. It can be a wide number of things, defined by type */
    private Object value;
    /** the encoded stream, if this is a STREAM object */
    private ByteBuffer stream;
    /** a cached version of the decoded stream */
    private SoftReference decodedStream;
    /** The filter limits used to generate the cached decoded stream */
    private Set<String> decodedStreamFilterLimits = null;
    /**
     * the PDFFile from which this object came, used for
     * dereferences
     */
    private final PDFFile owner;
    /**
     * a cache of translated data.  This data can be
     * garbage collected at any time, after which it will
     * have to be rebuilt.
     */
    private SoftReference cache;

    /** @see #getObjNum() */
    private int objNum = OBJ_NUM_EMBEDDED;

    /** @see #getObjGen() */
    private int objGen = OBJ_NUM_EMBEDDED;

    /**
     * create a new simple PDFObject with a type and a value
     * @param owner the PDFFile in which this object resides, used
     * for dereferencing.  This may be null.
     * @param type the type of object
     * @param value the value.  For DICTIONARY, this is a HashMap.
     * for ARRAY it's an ArrayList.  For NUMBER, it's a Double.
     * for BOOLEAN, it's Boolean.TRUE or Boolean.FALSE.  For
     * everything else, it's a String.
     */
    public PDFObject(PDFFile owner, int type, Object value) {
        this.type = type;
        if (type == NAME) {
            value = ((String) value).intern();
        } else if (type == KEYWORD && value.equals("true")) {
            this.type = BOOLEAN;
            value = Boolean.TRUE;
        } else if (type == KEYWORD && value.equals("false")) {
            this.type = BOOLEAN;
            value = Boolean.FALSE;
        }
        this.value = value;
        this.owner = owner;
    }

    /**
     * create a new PDFObject that is the closest match to a
     * given Java object.  Possibilities include Double, String,
     * PDFObject[], HashMap, Boolean, or PDFParser.Tok,
     * which should be "true" or "false" to turn into a BOOLEAN.
     *
     * @param obj the sample Java object to convert to a PDFObject.
     * @throws PDFParseException if the object isn't one of the
     * above examples, and can't be turned into a PDFObject.
     */
    public PDFObject(Object obj) throws PDFParseException {
        this.owner = null;
        this.value = obj;
        if ((obj instanceof Double) || (obj instanceof Integer)) {
            this.type = NUMBER;
        } else if (obj instanceof String) {
            this.type = NAME;
        } else if (obj instanceof PDFObject[]) {
            this.type = ARRAY;
        } else if (obj instanceof Object[]) {
            Object[] srcary = (Object[]) obj;
            PDFObject[] dstary = new PDFObject[srcary.length];
            for (int i = 0; i < srcary.length; i++) {
                dstary[i] = new PDFObject(srcary[i]);
            }
            value = dstary;
            this.type = ARRAY;
        } else if (obj instanceof HashMap) {
            this.type = DICTIONARY;
        } else if (obj instanceof Boolean) {
            this.type = BOOLEAN;
        } else if (obj instanceof PDFParser.Tok) {
            PDFParser.Tok tok = (PDFParser.Tok) obj;
            if (tok!=null && tok.name!=null && tok.name.equals("true")) {
                this.value = Boolean.TRUE;
                this.type = BOOLEAN;
            } else if (tok!=null && tok.name!=null && tok.name.equals("false")) {
                this.value = Boolean.FALSE;
                this.type = BOOLEAN;
            } else {
                this.value = tok.name;
                this.type = NAME;
            }
        } else {
            throw new PDFParseException("Bad type for raw PDFObject: " + obj);
        }
    }

    /**
     * create a new PDFObject based on a PDFXref
     * @param owner the PDFFile from which the PDFXref was drawn
     * @param xref the PDFXref to turn into a PDFObject
     */
    public PDFObject(PDFFile owner, PDFXref xref) {
        this.type = INDIRECT;
        this.value = xref;
        this.owner = owner;
    }
    
    /**
     * Convenient method to get a dictionary value as String
     * @param name of the dictionary value
     * @return the value or null if no entry exists with that name
     * @throws IOException
     */
    public String getDictRefAsString(String name) throws IOException {
    	PDFObject ref = getDictRef(name);
    	return ref == null ? null : ref.getStringValue();
    }
    
    /**
     * Convenient method to get a dictionary value as String
     * @param name of the dictionary value
     * @return the value or null if no entry exists with that name
     * @throws IOException
     */
    public Boolean getDictRefAsBoolean(String name) throws IOException {
    	PDFObject ref = getDictRef(name);
    	return ref == null ? null : ref.getBooleanValue();
    }
    
    /**
     * Convenient method to get a dictionary value as Integer
     * @param name of the dictionary value
     * @return the value or null if no entry exists with that name
     * @throws IOException
     */
    public Integer getDictRefAsInt(String name) throws IOException {
    	PDFObject ref = getDictRef(name);
    	return ref == null ? null : ref.getIntValue();
    }
        
    /**
     * Convenient method to get a dictionary value as int[]
     * @param name of the dictionary value
     * @return the value or null if no entry exists with that name
     * @throws IOException
     */
    public int[] getDictRefAsIntArray(String name) throws IOException {
    	PDFObject ref = getDictRef(name);
    	if (ref == null) {
    		return null;
    	}
    	PDFObject[] values = ref.getArray();
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
        	result[i] = values[i].getIntValue();
        }
    	return result;
    }
    
    /**
     * Convenient method to get a dictionary value as float[]
     * @param name of the dictionary value
     * @return the value or null if no entry exists with that name
     * @throws IOException
     */
    public float[] getDictRefAsFloatArray(String name) throws IOException {
    	PDFObject ref = getDictRef(name);
    	if (ref == null) {
    		return null;
    	}
    	PDFObject[] values = ref.getArray();
    	float[] result = new float[values.length];
        for (int i = 0; i < values.length; i++) {
        	result[i] = values[i].getFloatValue();
        }
    	return result;
    }
    
    
    /**
     * Convenient method to get a dictionary value as Float
     * @param name of the dictionary value
     * @return the value or null if no entry exists with that name
     * @throws IOException
     */
    public Float getDictRefAsFloat(String name) throws IOException {
    	PDFObject ref = getDictRef(name);
    	return ref == null ? null : ref.getFloatValue();
    }
    
    /**
     * Convenient method to get a dictionary value as Double
     * @param name of the dictionary value
     * @return the value or null if no entry exists with that name
     * @throws IOException
     */
    public Double getDictRefAsDouble(String name) throws IOException {
    	PDFObject ref = getDictRef(name);
    	return ref == null ? null : ref.getDoubleValue();
    }

    /**
     * get the type of this object.  The object will be
     * dereferenced, so INDIRECT will never be returned.
     * @return the type of the object
     */
    public int getType() throws IOException {
        if (type == INDIRECT) {
            return dereference().getType();
        }

        return type;
    }

    /**
     * set the stream of this object.  It should have been
     * a DICTIONARY before the call.
     * @param data the data, as a ByteBuffer.
     */
    public void setStream(ByteBuffer data) {
        this.type = STREAM;
        this.stream = data;
    }

    /**
     * get the value in the cache.  May become null at any time.
     * @return the cached value, or null if the value has been
     * garbage collected.
     */
    public Object getCache() throws IOException {
        if (type == INDIRECT) {
            return dereference().getCache();
        } else if (cache != null) {
            return cache.get();
        } else {
            return null;
        }
    }

    /**
     * set the cached value.  The object may be garbage collected
     * if no other reference exists to it.
     * @param obj the object to be cached
     */
    public void setCache(Object obj) throws IOException {
        if (type == INDIRECT) {
            dereference().setCache(obj);
            return;
        } else {
            cache = new SoftReference<Object>(obj);
        }
    }

    public byte[] getStream(Set<String> filterLimits) throws IOException
    {
        if (type == INDIRECT) {
            return dereference().getStream(filterLimits);
        } else if (type == STREAM && stream != null) {
            byte[] data = null;

            synchronized (stream) {
                // decode
                ByteBuffer streamBuf = decodeStream(filterLimits);
                // ByteBuffer streamBuf = stream;

                // First try to use the array with no copying.  This can only
                // be done if the buffer has a backing array, and is not a slice
                if (streamBuf.hasArray() && streamBuf.arrayOffset() == 0) {
                    byte[] ary = streamBuf.array();

                    // make sure there is no extra data in the buffer
                    if (ary.length == streamBuf.remaining()) {
                        return ary;
                    }
                }

                // Can't use the direct buffer, so copy the data (bad)
                data = new byte[streamBuf.remaining()];
                streamBuf.get(data);

                // return the stream to its starting position
                streamBuf.flip();
            }

            return data;
        } else if (type == STRING) {
            return PDFStringUtil.asBytes(getStringValue());
        } else {
            // wrong type
            return null;
        }
    }

    /**
     * get the stream from this object.  Will return null if this
     * object isn't a STREAM.
     * @return the stream, or null, if this isn't a STREAM.
     */
    public byte[] getStream() throws IOException {
       return getStream(Collections.<String>emptySet());
    }

    /**
     * get the stream from this object as a byte buffer.  Will return null if
     * this object isn't a STREAM.
     * @return the buffer, or null, if this isn't a STREAM.
     */
    public ByteBuffer getStreamBuffer() throws IOException {
        return getStreamBuffer(Collections.<String>emptySet());
    }

    /**
     * get the stream from this object as a byte buffer.  Will return null if 
     * this object isn't a STREAM.
     * @return the buffer, or null, if this isn't a STREAM.
     */
    public ByteBuffer getStreamBuffer(Set<String> filterLimits) throws IOException {
        if (type == INDIRECT) {
            return dereference().getStreamBuffer(filterLimits);
        } else if (type == STREAM && stream != null) {
            synchronized (stream) {
                ByteBuffer streamBuf = decodeStream(filterLimits);
                // ByteBuffer streamBuf = stream;
                return streamBuf.duplicate();
            }
        } else if (type == STRING) {
            String src = getStringValue();
            return ByteBuffer.wrap(src.getBytes());
        }

        // wrong type
        return null;
    }

    /**
     * Get the decoded stream value
     */
    private ByteBuffer decodeStream(Set<String> filterLimits) throws IOException {
        ByteBuffer outStream = null;

        // first try the cache
        if (decodedStream != null && filterLimits.equals(decodedStreamFilterLimits)) {
            outStream = (ByteBuffer) decodedStream.get();
        }

        // no luck in the cache, do the actual decoding
        if (outStream == null) {
            stream.rewind();
            outStream = PDFDecoder.decodeStream(this, stream, filterLimits);
            decodedStreamFilterLimits = new HashSet<String>(filterLimits);
            decodedStream = new SoftReference(outStream);
        }

        return outStream;
    }

    /**
     * get the value as an int.  Will return 0 if this object
     * isn't a NUMBER.
     */
    public int getIntValue() throws IOException {
        if (type == INDIRECT) {
            return dereference().getIntValue();
        } else if (type == NUMBER) {
            return ((Number) value).intValue();
        }

        // wrong type
        return 0;
    }

    /**
     * get the value as a float.  Will return 0 if this object
     * isn't a NUMBER
     */
    public float getFloatValue() throws IOException {
        if (type == INDIRECT) {
            return dereference().getFloatValue();
        } else if (type == NUMBER) {
            return ((Double) value).floatValue();
        }

        // wrong type
        return 0;
    }

    /**
     * get the value as a double.  Will return 0 if this object
     * isn't a NUMBER.
     */
    public double getDoubleValue() throws IOException {
        if (type == INDIRECT) {
            return dereference().getDoubleValue();
        } else if (type == NUMBER) {
            return ((Number) value).doubleValue();
        }

        // wrong type
        return 0;
    }

    /**
     * get the value as a String.  Will return null if the object
     * isn't a STRING, NAME, or KEYWORD.  This method will <b>NOT</b>
     * convert a NUMBER to a String. If the string is actually
     * a text string (i.e., may be encoded in UTF16-BE or PdfDocEncoding),
     * then one should use {@link #getTextStringValue()} or use one
     * of the {@link PDFStringUtil} methods on the result from this
     * method. The string value represents exactly the sequence of 8 bit
     * characters present in the file, decrypted and decoded as appropriate,
     * into a string containing only 8 bit character values - that is, each
     * char will be between 0 and 255.
     */
    public String getStringValue() throws IOException {
        if (type == INDIRECT) {
            return dereference().getStringValue();
        } else if (type == STRING || type == NAME || type == KEYWORD) {
            return (String) value;
        }

        // wrong type
        return null;
    }

    /**
     * Get the value as a text string; i.e., a string encoded in UTF-16BE
     * or PDFDocEncoding. Simple latin alpha-numeric characters are preserved in
     * both these encodings.
     * @return the text string value
     * @throws IOException
     */
    public String getTextStringValue() throws IOException {
	return PDFStringUtil.asTextString(getStringValue());
    }

    /**
     * get the value as a PDFObject[].  If this object is an ARRAY,
     * will return the array.  Otherwise, will return an array
     * of one element with this object as the element.
     */
    public PDFObject[] getArray() throws IOException {
        if (type == INDIRECT) {
            return dereference().getArray();
        } else if (type == ARRAY) {
            PDFObject[] ary = (PDFObject[]) value;
            return ary;
        } else {
            PDFObject[] ary = new PDFObject[1];
            ary[0] = this;
            return ary;
        }
    }

    /**
     * get the value as a boolean.  Will return false if this
     * object is not a BOOLEAN
     */
    public boolean getBooleanValue() throws IOException {
        if (type == INDIRECT) {
            return dereference().getBooleanValue();
        } else if (type == BOOLEAN) {
            return value == Boolean.TRUE;
        }

        // wrong type
        return false;
    }

    /**
     * if this object is an ARRAY, get the PDFObject at some
     * position in the array.  If this is not an ARRAY, returns
     * null.
     */
    public PDFObject getAt(int idx) throws IOException {
        if (type == INDIRECT) {
            return dereference().getAt(idx);
        } else if (type == ARRAY) {
            PDFObject[] ary = (PDFObject[]) value;
            return ary[idx];
        }

        // wrong type
        return null;
    }

    /**
     * get an Iterator over all the keys in the dictionary.  If
     * this object is not a DICTIONARY or a STREAM, returns an
     * Iterator over the empty list.
     */
    public Iterator getDictKeys() throws IOException {
        if (type == INDIRECT) {
            return dereference().getDictKeys();
        } else if (type == DICTIONARY || type == STREAM) {
            return ((HashMap) value).keySet().iterator();
        }

        // wrong type
        return new ArrayList().iterator();
    }

    /**
     * get the dictionary as a HashMap.  If this isn't a DICTIONARY
     * or a STREAM, returns null
     */
    public HashMap<String,PDFObject> getDictionary() throws IOException {
        if (type == INDIRECT) {
            return dereference().getDictionary();
        } else if (type == DICTIONARY || type == STREAM) {
            return (HashMap<String,PDFObject>) value;
        }

        // wrong type
        return new HashMap<String,PDFObject>();
    }

    /**
     * get the value associated with a particular key in the
     * dictionary.  If this isn't a DICTIONARY or a STREAM,
     * or there is no such key, returns null.
     */
    public PDFObject getDictRef(String key) throws IOException {
        if (type == INDIRECT) {
            return dereference().getDictRef(key);
        } else if (type == DICTIONARY || type == STREAM) {
            key = key.intern();
            HashMap h = (HashMap) value;
            PDFObject obj = (PDFObject) h.get(key.intern());
            return obj;
        }

        // wrong type
        return null;
    }

    /**
     * returns true only if this object is a DICTIONARY or a
     * STREAM, and the "Type" entry in the dictionary matches a
     * given value.
     * @param match the expected value for the "Type" key in the
     * dictionary
     * @return whether the dictionary is of the expected type
     */
    public boolean isDictType(String match) throws IOException {
        if (type == INDIRECT) {
            return dereference().isDictType(match);
        } else if (type != DICTIONARY && type != STREAM) {
            return false;
        }

        PDFObject obj = getDictRef("Type");
        return obj != null && obj.getStringValue().equals(match);
    }

    public PDFDecrypter getDecrypter() {
        // PDFObjects without owners are always created as part of
        // content instructions. Such an object will never have encryption
        // applied to it, as the stream that contains it is the
        // unit of encryption, with no further encryption being applied
        // within. So if someone asks for the decrypter for
        // one of these in-stream objects, no decryption should
        // ever be applied. This can be seen with inline images.
        return owner != null ?
                owner.getDefaultDecrypter() :
                IdentityDecrypter.getInstance();
    }

     /**
     * Set the object identifiers
     * @param objNum the object number
     * @param objGen the object generation number
     */
    public void setObjectId(int objNum, int objGen) {
        assert objNum >= OBJ_NUM_TRAILER;
        assert objGen >= OBJ_NUM_TRAILER;
        this.objNum = objNum;
        this.objGen = objGen;
    }

    /**
     * Get the object number of this object; a negative value indicates that
     * the object is not numbered, as it's not a top-level object: if the value
     * is {@link #OBJ_NUM_EMBEDDED}, it is because it's embedded within
     * another object. If the value is {@link #OBJ_NUM_TRAILER}, it's because
     * it's an object from the trailer.
     * @return the object number, if positive
     */
    public int getObjNum() {
        return objNum;
    }

    /**
     * Get the object generation number of this object; a negative value
     * indicates that the object is not numbered, as it's not a top-level
     * object: if the value is {@link #OBJ_NUM_EMBEDDED}, it is because it's
     * embedded within another object. If the value is {@link
     * #OBJ_NUM_TRAILER}, it's because it's an object from the trailer.
     * @return the object generation number, if positive
     */
    public int getObjGen() {
        return objGen;
    }

    /**
     * return a representation of this PDFObject as a String.
     * Does NOT dereference anything:  this is the only method
     * that allows you to distinguish an INDIRECT PDFObject.
     */
    @Override
    public String toString() {
        try {
            if (type == INDIRECT) {
                StringBuffer str = new StringBuffer ();
                str.append("Indirect to #" + ((PDFXref) value).getID());
                try {
                    str.append("\n" + dereference().toString());
                } catch (Throwable t) {
                    str.append(t.toString());
                }
                return str.toString();
            } else if (type == BOOLEAN) {
                return "Boolean: " + (getBooleanValue() ? "true" : "false");
            } else if (type == NUMBER) {
                return "Number: " + getDoubleValue();
            } else if (type == STRING) {
                return "String: " + getStringValue();
            } else if (type == NAME) {
                return "Name: /" + getStringValue();
            } else if (type == ARRAY) {
                return "Array, length=" + ((PDFObject[]) value).length;
            } else if (type == DICTIONARY) {
                StringBuffer sb = new StringBuffer();
                PDFObject obj = getDictRef("Type");
                if (obj != null) {
                    sb.append(obj.getStringValue());
                    obj = getDictRef("Subtype");
                    if (obj == null) {
                        obj = getDictRef("S");
                    }
                    if (obj != null) {
                        sb.append("/" + obj.getStringValue());
                    }
                } else {
                    sb.append("Untyped");
                }
                sb.append(" dictionary. Keys:");
                HashMap hm = (HashMap) value;
                Iterator it = hm.entrySet().iterator();
                Map.Entry entry;
                while (it.hasNext()) {
                    entry = (Map.Entry) it.next();
                    sb.append("\n   " + entry.getKey() + "  " + entry.getValue());
                }
                return sb.toString();
            } else if (type == STREAM) {
                byte[] st = getStream();
                if (st == null) {
                    return "Broken stream";
                }
                return "Stream: [[" + new String(st, 0, st.length > 30 ? 30 : st.length) + "]]";
            } else if (type == NULL) {
                return "Null";
            } else if (type == KEYWORD) {
                return "Keyword: " + getStringValue();
            /*	    } else if (type==IMAGE) {
            StringBuffer sb= new StringBuffer();
            java.awt.Image im= (java.awt.Image)stream;
            sb.append("Image ("+im.getWidth(null)+"x"+im.getHeight(null)+", with keys:");
            HashMap hm= (HashMap)value;
            Iterator it= hm.keySet().iterator();
            while(it.hasNext()) {
            sb.append(" "+(String)it.next());
            }
            return sb.toString();*/
            } else {
                return "Whoops!  big error!  Unknown type";
            }
        } catch (IOException ioe) {
            return "Caught an error: " + ioe;
        }
    }

    /**
     * Make sure that this object is dereferenced.  Use the cache of
     * an indirect object to cache the dereferenced value, if possible.
     */
    public PDFObject dereference() throws IOException {
        if (type == INDIRECT) {
            PDFObject obj = null;

            if (cache != null) {
                obj = (PDFObject) cache.get();
            }

            if (obj == null || obj.value == null) {
                if (owner == null) {
                    PDFDebugger.debug("Bad seed (owner==null)!  Object=" + this);
                }

                obj = owner.dereference((PDFXref)value, getDecrypter());

                cache = new SoftReference<PDFObject>(obj);
            }

            return obj;
        } else {
            // not indirect, no need to dereference
            return this;
        }
    }

    /**
     * Identify whether the object is currently an indirect/cross-reference
     * @return whether currently indirect
     */
    public boolean isIndirect() {
        return (type == INDIRECT);
    }

    /** 
     * Test whether two PDFObject are equal.  Objects are equal IFF they
     * are the same reference OR they are both indirect objects with the
     * same id and generation number in their xref
     */
    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            // they are the same object
            return true;
        } else if (type == INDIRECT && o instanceof PDFObject) {
            // they are both PDFObjects.  Check type and xref.
            PDFObject obj = (PDFObject) o;

            if (obj.type == INDIRECT) {
                PDFXref lXref = (PDFXref) value;
                PDFXref rXref = (PDFXref) obj.value;

                return ((lXref.getID() == rXref.getID()) &&
                        (lXref.getGeneration() == rXref.getGeneration()));
            }
        }

        return false;
    }
	
	    /**
     * Returns the root of this object.
     * @return
     */
    public PDFObject getRoot() {
    	return owner.getRoot();
    }
}