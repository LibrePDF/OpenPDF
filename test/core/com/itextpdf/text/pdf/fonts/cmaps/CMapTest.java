/*
 * Created on Jul 9, 2009
 * (c) 2009 Trumpet, Inc.
 *
 */
package com.itextpdf.text.pdf.fonts.cmaps;


import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author kevin
 */
public class CMapTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    private void checkInsertAndRetrieval(byte[] bytes, String uni) throws Exception{
        CMap c = new CMap();
        c.addMapping(bytes, uni);
        String lookupResult = c.lookup(bytes, 0, bytes.length);
        Assert.assertEquals(uni, lookupResult);
        
    }
    
    @Test
    public void testHighOrderBytes() throws Exception {
        
        checkInsertAndRetrieval(new byte[]{(byte)0x91}, "\u2018");
        checkInsertAndRetrieval(new byte[]{(byte)0x91, (byte)0x92}, "\u2018");

        checkInsertAndRetrieval(new byte[]{(byte)0x20}, "\u2018");
        checkInsertAndRetrieval(new byte[]{(byte)0x23, (byte)0x21}, "\u2018");
        checkInsertAndRetrieval(new byte[]{(byte)0x22, (byte)0xf0}, "\u2018");
        checkInsertAndRetrieval(new byte[]{(byte)0xf1, (byte)0x25}, "\u2018");

    }
}
