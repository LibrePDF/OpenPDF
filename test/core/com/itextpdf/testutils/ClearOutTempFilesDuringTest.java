/*
 * Created on Dec 21, 2009
 * (c) 2009 Trumpet, Inc.
 *
 */
package com.itextpdf.testutils;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author kevin
 */
public class ClearOutTempFilesDuringTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void purgeTempFiles(){
        TestResourceUtils.purgeTempFiles();
    }
}
