/***********************************************************************
 * This file contains proprietary information of Cardiff.
 * Copying or reproduction without prior written approval is prohibited.
 * Copyright (c) 2000-2008 Cardiff. All rights reserved.
 ***********************************************************************/

package com.itextpdf.text.pdf;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * ********************************************************************
 * The <code>PdfRectangleTest</code> class ...
 *
 * @author mstorer
 *         *********************************************************************
 */

public class PdfRectangleTest {
    @Test
    public void testRectSafety() {
        PdfRectangle rect = new PdfRectangle( 10, 10 );
        int initialSize = rect.size();

        // none of the following should alter the underlying rectangle in any way.
        assertTrue( !rect.add( PdfName.N ) );
        int iVals[] = {1, 2};
        assertTrue( !rect.add( iVals ) );
        float fVals[] = {1f, 2f};
        assertTrue( !rect.add( fVals ) );

        rect.addFirst( PdfName.N );
        assertTrue( initialSize == rect.size() );
    }
}

