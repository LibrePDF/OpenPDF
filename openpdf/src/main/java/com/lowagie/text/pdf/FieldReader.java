package com.lowagie.text.pdf;

import java.util.HashMap;
import java.util.List;

/**
 * Represents the basic needs for reading fields.
 */
public interface FieldReader {

    HashMap getFields();

    String getFieldValue(String fieldKey);

    List getListValues(String fieldKey);

}
