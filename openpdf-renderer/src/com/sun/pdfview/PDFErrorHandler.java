package com.sun.pdfview;

/**
 * Simple class to handle exceptions - as default we just print the stack trace
 * but it's possible to inject another behaviour
 * @author xond
 *
 */
public class PDFErrorHandler {

    public void publishException(Throwable e){
       e.printStackTrace(); 
    }
}
