package org.openpdf.renderer;

/**
 * Simple class to handle exceptions - as default we just print the stack trace
 * but it's possible to inject another behaviour
 * @author xond
 *
 */
@Deprecated
public class PDFErrorHandler {

    @Deprecated
    public void publishException(Throwable e){
        // TODO: use a logging framework.
        //e.printStackTrace();
    }
}
