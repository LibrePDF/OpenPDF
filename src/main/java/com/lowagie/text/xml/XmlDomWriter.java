/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

package com.lowagie.text.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author psoares
 */
public class XmlDomWriter {
    
    /** Print writer. */
    protected PrintWriter fOut;
    
    /** Canonical output. */
    protected boolean fCanonical;
    
    /** Processing XML 1.1 document. */
    protected boolean fXML11;
    
    //
    // Constructors
    //
    
    /** Default constructor. */
    public XmlDomWriter() {
    } // <init>()
    
    public XmlDomWriter(boolean canonical) {
        fCanonical = canonical;
    } // <init>(boolean)
    
    //
    // Public methods
    //
    
    /** Sets whether output is canonical. */
    public void setCanonical(boolean canonical) {
        fCanonical = canonical;
    } // setCanonical(boolean)
    
    /** Sets the output stream for printing. */
    public void setOutput(OutputStream stream, String encoding)
    throws UnsupportedEncodingException {
        
        if (encoding == null) {
            encoding = "UTF8";
        }
        
        java.io.Writer writer = new OutputStreamWriter(stream, encoding);
        fOut = new PrintWriter(writer);
        
    } // setOutput(OutputStream,String)
    
    /** Sets the output writer. */
    public void setOutput(java.io.Writer writer) {
        
        fOut = writer instanceof PrintWriter
                ? (PrintWriter)writer : new PrintWriter(writer);
        
    } // setOutput(java.io.Writer)
    
    /** Writes the specified node, recursively. */
    public void write(Node node) {
        
        // is there anything to do?
        if (node == null) {
            return;
        }
        
        short type = node.getNodeType();
        switch (type) {
            case Node.DOCUMENT_NODE: {
                Document document = (Document)node;
                fXML11 = false; //"1.1".equals(getVersion(document));
                if (!fCanonical) {
                    if (fXML11) {
                        fOut.println("<?xml version=\"1.1\" encoding=\"UTF-8\"?>");
                    } else {
                        fOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    }
                    fOut.flush();
                    write(document.getDoctype());
                }
                write(document.getDocumentElement());
                break;
            }
            
            case Node.DOCUMENT_TYPE_NODE: {
                DocumentType doctype = (DocumentType)node;
                fOut.print("<!DOCTYPE ");
                fOut.print(doctype.getName());
                String publicId = doctype.getPublicId();
                String systemId = doctype.getSystemId();
                if (publicId != null) {
                    fOut.print(" PUBLIC '");
                    fOut.print(publicId);
                    fOut.print("' '");
                    fOut.print(systemId);
                    fOut.print('\'');
                } else if (systemId != null) {
                    fOut.print(" SYSTEM '");
                    fOut.print(systemId);
                    fOut.print('\'');
                }
                String internalSubset = doctype.getInternalSubset();
                if (internalSubset != null) {
                    fOut.println(" [");
                    fOut.print(internalSubset);
                    fOut.print(']');
                }
                fOut.println('>');
                break;
            }
            
            case Node.ELEMENT_NODE: {
                fOut.print('<');
                fOut.print(node.getNodeName());
                Attr attrs[] = sortAttributes(node.getAttributes());
                for (int i = 0; i < attrs.length; i++) {
                    Attr attr = attrs[i];
                    fOut.print(' ');
                    fOut.print(attr.getNodeName());
                    fOut.print("=\"");
                    normalizeAndPrint(attr.getNodeValue(), true);
                    fOut.print('"');
                }
                fOut.print('>');
                fOut.flush();
                
                Node child = node.getFirstChild();
                while (child != null) {
                    write(child);
                    child = child.getNextSibling();
                }
                break;
            }
            
            case Node.ENTITY_REFERENCE_NODE: {
                if (fCanonical) {
                    Node child = node.getFirstChild();
                    while (child != null) {
                        write(child);
                        child = child.getNextSibling();
                    }
                } else {
                    fOut.print('&');
                    fOut.print(node.getNodeName());
                    fOut.print(';');
                    fOut.flush();
                }
                break;
            }
            
            case Node.CDATA_SECTION_NODE: {
                if (fCanonical) {
                    normalizeAndPrint(node.getNodeValue(), false);
                } else {
                    fOut.print("<![CDATA[");
                    fOut.print(node.getNodeValue());
                    fOut.print("]]>");
                }
                fOut.flush();
                break;
            }
            
            case Node.TEXT_NODE: {
                normalizeAndPrint(node.getNodeValue(), false);
                fOut.flush();
                break;
            }
            
            case Node.PROCESSING_INSTRUCTION_NODE: {
                fOut.print("<?");
                fOut.print(node.getNodeName());
                String data = node.getNodeValue();
                if (data != null && data.length() > 0) {
                    fOut.print(' ');
                    fOut.print(data);
                }
                fOut.print("?>");
                fOut.flush();
                break;
            }
            
            case Node.COMMENT_NODE: {
                if (!fCanonical) {
                    fOut.print("<!--");
                    String comment = node.getNodeValue();
                    if (comment != null && comment.length() > 0) {
                        fOut.print(comment);
                    }
                    fOut.print("-->");
                    fOut.flush();
                }
            }
        }
        
        if (type == Node.ELEMENT_NODE) {
            fOut.print("</");
            fOut.print(node.getNodeName());
            fOut.print('>');
            fOut.flush();
        }
        
    } // write(Node)
    
    /** Returns a sorted list of attributes. */
    protected Attr[] sortAttributes(NamedNodeMap attrs) {
        
        int len = (attrs != null) ? attrs.getLength() : 0;
        Attr array[] = new Attr[len];
        for (int i = 0; i < len; i++) {
            array[i] = (Attr)attrs.item(i);
        }
        for (int i = 0; i < len - 1; i++) {
            String name = array[i].getNodeName();
            int index = i;
            for (int j = i + 1; j < len; j++) {
                String curName = array[j].getNodeName();
                if (curName.compareTo(name) < 0) {
                    name = curName;
                    index = j;
                }
            }
            if (index != i) {
                Attr temp = array[i];
                array[i] = array[index];
                array[index] = temp;
            }
        }
        
        return array;
        
    } // sortAttributes(NamedNodeMap):Attr[]
    
    //
    // Protected methods
    //
    
    /** Normalizes and prints the given string. */
    protected void normalizeAndPrint(String s, boolean isAttValue) {
        
        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            normalizeAndPrint(c, isAttValue);
        }
        
    } // normalizeAndPrint(String,boolean)
    
    /** Normalizes and print the given character. */
    protected void normalizeAndPrint(char c, boolean isAttValue) {
        
        switch (c) {
            case '<': {
                fOut.print("&lt;");
                break;
            }
            case '>': {
                fOut.print("&gt;");
                break;
            }
            case '&': {
                fOut.print("&amp;");
                break;
            }
            case '"': {
                // A '"' that appears in character data
                // does not need to be escaped.
                if (isAttValue) {
                    fOut.print("&quot;");
                } else {
                    fOut.print("\"");
                }
                break;
            }
            case '\r': {
                // If CR is part of the document's content, it
                // must not be printed as a literal otherwise
                // it would be normalized to LF when the document
                // is reparsed.
                fOut.print("&#xD;");
                break;
            }
            case '\n': {
                if (fCanonical) {
                    fOut.print("&#xA;");
                    break;
                }
                // else, default print char
            }
            default: {
                // In XML 1.1, control chars in the ranges [#x1-#x1F, #x7F-#x9F] must be escaped.
                //
                // Escape space characters that would be normalized to #x20 in attribute values
                // when the document is reparsed.
                //
                // Escape NEL (0x85) and LSEP (0x2028) that appear in content
                // if the document is XML 1.1, since they would be normalized to LF
                // when the document is reparsed.
                if (fXML11 && ((c >= 0x01 && c <= 0x1F && c != 0x09 && c != 0x0A)
                || (c >= 0x7F && c <= 0x9F) || c == 0x2028)
                || isAttValue && (c == 0x09 || c == 0x0A)) {
                    fOut.print("&#x");
                    fOut.print(Integer.toHexString(c).toUpperCase());
                    fOut.print(";");
                } else {
                    fOut.print(c);
                }
            }
        }
    } // normalizeAndPrint(char,boolean)
    
    /** Extracts the XML version from the Document. */
//    protected String getVersion(Document document) {
//        if (document == null) {
//            return null;
//        }
//        String version = null;
//        Method getXMLVersion = null;
//        try {
//            getXMLVersion = document.getClass().getMethod("getXmlVersion", new Class[]{});
//            // If Document class implements DOM L3, this method will exist.
//            if (getXMLVersion != null) {
//                version = (String) getXMLVersion.invoke(document, (Object[]) null);
//            }
//        } catch (Exception e) {
//            // Either this locator object doesn't have
//            // this method, or we're on an old JDK.
//        }
//        return version;
//    } // getVersion(Document)
}
