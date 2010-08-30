/*
 * Created on Jul 9, 2009
 * (c) 2009 Trumpet, Inc.
 *
 */
package com.itextpdf.text.pdf.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ListIterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.testutils.TestResourceUtils;
import com.itextpdf.text.pdf.PRIndirectReference;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;

public class PdfContentStreamProcessorTest
{
  private DebugRenderListener _renderListener;

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception{
      _renderListener = new DebugRenderListener();
  }
  
  // Replicates iText bug 2817030
  @Test
  public void testPositionAfterTstar()
    throws Exception
  {
    processBytes("yaxiststar.pdf", 1);
  }



  private void processBytes(
      final String resourceName,
      final int pageNumber)
    throws IOException
  {
    final PdfReader pdfReader = TestResourceUtils.getResourceAsPdfReader(this, resourceName);

    final PdfDictionary pageDictionary = pdfReader.getPageN(pageNumber);

    final PdfDictionary resourceDictionary = pageDictionary.getAsDict(PdfName.RESOURCES);

    final PdfObject contentObject = pageDictionary.get(PdfName.CONTENTS);
    final byte[] contentBytes = readContentBytes(contentObject);
    PdfContentStreamProcessor processor = new PdfContentStreamProcessor(_renderListener);
    processor.processContent(contentBytes, resourceDictionary);
    
  }


  private byte[] readContentBytes(
      final PdfObject contentObject)
    throws IOException
  {
    final byte[] result;
    switch (contentObject.type())
    {
      case PdfObject.INDIRECT:
        final PRIndirectReference ref = (PRIndirectReference) contentObject;
        final PdfObject directObject = PdfReader.getPdfObject(ref);
        result = readContentBytes(directObject);
        break;
      case PdfObject.STREAM:
        final PRStream stream = (PRStream) PdfReader.getPdfObject(contentObject);
        result = PdfReader.getStreamBytes(stream);
        break;
      case PdfObject.ARRAY:
        // Stitch together all content before calling processContent(), because
        // processContent() resets state.
        final ByteArrayOutputStream allBytes = new ByteArrayOutputStream();
        final PdfArray contentArray = (PdfArray) contentObject;
        final ListIterator iter = contentArray.listIterator();
        while (iter.hasNext())
        {
          final PdfObject element = (PdfObject) iter.next();
          allBytes.write(readContentBytes(element));
        }
        result = allBytes.toByteArray();
        break;
      default:
        final String msg = "Unable to handle Content of type " + contentObject.getClass();
        throw new IllegalStateException(msg);
    }
    return result;
  }


  private class DebugRenderListener
    implements RenderListener
  {
    private float _lastY = Float.MAX_VALUE;

    @Override
    public void reset() {
        _lastY = Float.MAX_VALUE;
    }
    
    @Override
    public void renderText(TextRenderInfo renderInfo) {
        Vector start = renderInfo.getStartPoint();
        final float x = start.get(Vector.I1);
        final float y = start.get(Vector.I2);
        System.out.println("Display text: '" + renderInfo.getText() + "' (" + x + "," + y + ")");
        if (y > _lastY){
          Assert.fail("Text has jumped back up the page");
        }
        _lastY = y;
        
    }

  }

}

