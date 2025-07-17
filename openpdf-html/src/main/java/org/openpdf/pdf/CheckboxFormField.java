/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.pdf;

import org.openpdf.text.DocumentException;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.BaseField;
import org.openpdf.text.pdf.PdfBorderDictionary;
import org.openpdf.text.pdf.PdfFormField;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.RadioCheckField;
import org.w3c.dom.Element;
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.BlockBox;
import org.openpdf.render.RenderingContext;
import org.openpdf.util.XRRuntimeException;

import java.awt.*;
import java.io.IOException;

public class CheckboxFormField extends AbstractFormField {
  private static final String FIELD_TYPE = "Checkbox";

  public CheckboxFormField(LayoutContext c, BlockBox box, int cssWidth, int cssHeight) {
    initDimensions(c, box, cssWidth, cssHeight);
  }

  @Override
  protected String getFieldType()
  {
    return FIELD_TYPE;
  }

  @Override
  public void paint(RenderingContext c, ITextOutputDevice outputDevice, BlockBox box) {
    PdfWriter writer = outputDevice.getWriter();
    Element elm = box.getElement();

    Rectangle targetArea = outputDevice.createLocalTargetArea(c, box);
    String onValue = getValue(elm);

    String fieldName = getFieldName(outputDevice, elm);
    RadioCheckField field = new RadioCheckField(writer, targetArea, fieldName, onValue);

    field.setChecked(isChecked(elm));
    field.setCheckType(RadioCheckField.TYPE_CHECK);
    field.setBorderStyle(PdfBorderDictionary.STYLE_SOLID);
    //TODO Consider if we can get some more correct color
    field.setBorderColor(Color.black);

    field.setBorderWidth(BaseField.BORDER_WIDTH_THIN);

    try {
      PdfFormField formField = field.getFullField();
      if (isReadOnly(elm)) {
        formField.setFieldFlags(PdfFormField.FF_READ_ONLY);
      }
      writer.addAnnotation(formField);
    } catch (IOException | DocumentException ioe) {
      throw new XRRuntimeException("Failed to paint field %s".formatted(fieldName), ioe);
    }
  }

  @Override
  public int getBaseline() {
    return 0;
  }

  @Override
  public boolean hasBaseline() {
    return false;
  }
}
