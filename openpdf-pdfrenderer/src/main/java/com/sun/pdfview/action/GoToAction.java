package com.sun.pdfview.action;

import java.io.IOException;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFDestination;
import com.sun.pdfview.PDFParseException;

/**
 * An action which specifies going to a particular destination
 */
public class GoToAction extends PDFAction {
    /** the destination to go to */
    private PDFDestination dest;
    
    /** 
     * Creates a new instance of GoToAction from an object
     *
     * @param obj the PDFObject with the action information
     */
    public GoToAction(PDFObject obj, PDFObject root) throws IOException {
        super("GoTo");
        
        // find the destination
        PDFObject destObj = obj.getDictRef("D");
        if (destObj == null) {
            throw new PDFParseException("No destination in GoTo action " + obj);
        }
        
        // parse it
        this.dest = PDFDestination.getDestination(destObj, root);
    }
    
    /**
     * Create a new GoToAction from a destination
     */
    public GoToAction(PDFDestination dest) {
        super("GoTo");
    
        this.dest = dest;
    }
      
    /**
     * Get the destination this action refers to
     */
    public PDFDestination getDestination() {
        return this.dest;
    }
}
