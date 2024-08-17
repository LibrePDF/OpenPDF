package com.sun.pdfview.action;

import java.io.IOException;

import com.sun.pdfview.PDFDestination;
import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;

/*****************************************************************************
 * Utility class for parsing values from a PDFObject
 *
 * @author  Katja Sondermann
 * @since 08.07.2009
 ****************************************************************************/
public class PdfObjectParseUtil {
	
	/*************************************************************************
	 * Parse a String value with the given key from parent object. If it's mandatory
	 * and not available, an exception will be thrown.
	 * @param key
	 * @param parent
	 * @param mandatory
	 * @return String - can be <code>null</code> if not mandatory
	 * @throws IOException - in case of a parsing error 
	 ************************************************************************/
	public static String parseStringFromDict(String key, PDFObject parent, boolean mandatory) throws IOException{
		PDFObject val = parent;
		while (val.getType() == PDFObject.DICTIONARY) {
			val = val.getDictRef(key);
			if(val == null){
				if(mandatory){
					throw new PDFParseException(key + "value could not be parsed : " + parent.toString());	
				}
				return null;
			}
		}
		return val.getStringValue();
	}

	/*************************************************************************
	 * Parse a Boolean value with the given key from parent object. If it's mandatory
	 * and not available, an exception will be thrown.
	 * @param key
	 * @param parent
	 * @param mandatory
	 * @return boolean - <code>false</code> if not available and not mandatory
	 * @throws IOException 
	 ************************************************************************/
	public static boolean parseBooleanFromDict(String key, PDFObject parent, boolean mandatory) throws IOException{
		PDFObject val = parent.getDictRef(key);
		if(val == null){
			if(mandatory){
				throw new PDFParseException(key + "value could not be parsed : " + parent.toString());	
			}
			return false;
		}
		return val.getBooleanValue();
	}
	
	/*************************************************************************
	 * Parse a integer value with the given key from parent object. If it's mandatory
	 * and not available, an exception will be thrown.
	 * @param key
	 * @param parent
	 * @param mandatory
	 * @return int - returns "0" in case the value is not a number
	 * @throws IOException 
	 ************************************************************************/
	public static int parseIntegerFromDict(String key, PDFObject parent, boolean mandatory) throws IOException{
		PDFObject val = parent.getDictRef(key);
		if(val == null){
			if(mandatory){
				throw new PDFParseException(key + "value could not be parsed : " + parent.toString());	
			}
			return 0;
		}
		return val.getIntValue();
	}
	
	/*************************************************************************
	 * Parse a destination object
	 * @param key
	 * @param parent
	 * @param root
	 * @param mandatory
	 * @return PDFDestination  - can be <code>null</code> if not mandatory
	 * @throws IOException
	 ************************************************************************/
	public static PDFDestination parseDestination(String key, PDFObject parent, PDFObject root, boolean mandatory) throws IOException{
		PDFObject destObj = parent.getDictRef(key);
		if (destObj == null) {
			if(mandatory){
				throw new PDFParseException("Error parsing destination " + parent);
			}
			return null;
		}
		return PDFDestination.getDestination(destObj, root);

	}
}
