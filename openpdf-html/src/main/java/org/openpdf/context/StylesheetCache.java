package org.openpdf.context;

import org.openpdf.css.sheet.Stylesheet;

import java.util.LinkedHashMap;
import java.util.Map;

class StylesheetCache extends LinkedHashMap<String, Stylesheet> {
    private static final int cacheCapacity = 16;

    StylesheetCache() {
        super(cacheCapacity,  0.75f, true);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Stylesheet> eldest) {
        return size() > cacheCapacity;
    }
}
