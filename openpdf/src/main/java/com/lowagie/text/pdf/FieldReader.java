package com.lowagie.text.pdf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the basic needs for reading fields.
 */
public interface FieldReader {

    @Deprecated
    HashMap<String, String> getFields();

    Map<String, String> getAllFields();

    String getFieldValue(String fieldKey);

    List<String> getListValues(String fieldKey);

}
