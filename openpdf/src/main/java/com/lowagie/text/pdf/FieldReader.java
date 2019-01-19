package com.lowagie.text.pdf;

import java.util.HashMap;
import java.util.List;

/**
 * Represents the basic needs for reading fields.
 */
public interface FieldReader {

    HashMap<String, String> getFields();

    String getFieldValue(String fieldKey);

    List<String> getListValues(String fieldKey);

}
