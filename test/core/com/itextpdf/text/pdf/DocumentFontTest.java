/*
 * Created on Jul 9, 2009
 * (c) 2009 Trumpet, Inc.
 *
 */
package com.itextpdf.text.pdf;


import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.testutils.TestResourceUtils;

/**
 * @author kevin
 */
public class DocumentFontTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructionForType0WithoutToUnicodeMap() throws Exception{
        int pageNum = 2;
        PdfName fontIdName = new PdfName("TT9");
        
        File testFile = TestResourceUtils.getResourceAsTempFile(this, "type0FontWithoutToUnicodeMap.pdf");
        RandomAccessFileOrArray f = new RandomAccessFileOrArray(testFile.getAbsolutePath());
        PdfReader reader = new PdfReader(f, null);
        
        PdfDictionary fontsDic = reader.getPageN(pageNum).getAsDict(PdfName.RESOURCES).getAsDict(PdfName.FONT);
        PdfDictionary fontDicDirect = fontsDic.getAsDict(fontIdName);
        PRIndirectReference fontDicIndirect = (PRIndirectReference)fontsDic.get(fontIdName);
        
        Assert.assertEquals(PdfName.TYPE0, fontDicDirect.getAsName(PdfName.SUBTYPE));
        Assert.assertEquals("/Identity-H", fontDicDirect.getAsName(PdfName.ENCODING).toString());
        Assert.assertNull("This font should not have a ToUnicode map", fontDicDirect.get(PdfName.TOUNICODE));
        
        new DocumentFont(fontDicIndirect); // this used to throw an NPE
    }
}
