package org.openpdf.pdf;

import org.openpdf.text.pdf.PdfAcroForm;
import org.openpdf.text.pdf.PdfWriter;
import org.w3c.dom.Element;
import org.openpdf.render.BlockBox;
import org.openpdf.render.RenderingContext;

import java.awt.*;

/**
 * User: beck
 * Date: 11/4/11
 */
public class EmptyReplacedElement extends AbstractFormField {
  private static final String FIELD_TYPE = "Hidden";

  private final int _width;
  private final int _height;

  private Point _location = new Point(0, 0);

  public EmptyReplacedElement(int width, int height) {
    _width = width;
    _height = height;
  }

  @Override
  public void paint(RenderingContext c, ITextOutputDevice outputDevice, BlockBox box) {
    PdfWriter writer = outputDevice.getWriter();

    PdfAcroForm acroForm = writer.getAcroForm();
    Element elem = box.getElement();
    String name = getFieldName(outputDevice, elem);
    String value = getValue(elem);
    /*ISO-32000-1 defines the limit for a name in a PDF file to be at maximum 127 bytes.
     *Source(http://www.adobe.com/content/dam/Adobe/en/devnet/acrobat/pdfs/PDF32000_2008.pdf)
     *  see Annex C § 2 Architectural limits "Table C.1" pages 649 and 650.
     *iText stores the hidden field value as a PDFName
     */
    if (value.length() > 127) {
        value = value.substring(0, 127);
    }
    acroForm.addHiddenField(name, value);
  }

  @Override
  public int getIntrinsicWidth() {
    return _width;
  }

  @Override
  public int getIntrinsicHeight() {
    return _height;
  }

  @Override
  public Point getLocation() {
    return _location;
  }

  @Override
  public void setLocation(int x, int y) {
    _location = new Point(0, 0);
  }

  @Override
  protected String getFieldType() {
    return FIELD_TYPE;
  }

  @Override
  public boolean hasBaseline() {
    return false;
  }

  @Override
  public int getBaseline() {
    return 0;
  }
}
