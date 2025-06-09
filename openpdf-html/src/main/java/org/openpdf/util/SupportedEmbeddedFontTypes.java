package org.openpdf.util;

public enum SupportedEmbeddedFontTypes {
    OTF("font/otf", ".otf"),
    TTF("font/ttf", ".ttf");

    public final String typeString;
    public final String extension;

    SupportedEmbeddedFontTypes(String typeString, String extension) {
        this.typeString = typeString;
        this.extension = extension;
    }

    public static boolean isSupported(String uri) {
        for(SupportedEmbeddedFontTypes type: SupportedEmbeddedFontTypes.values()) {
            if(uri.contains(type.typeString))
                return true;
        }
        return false;
    }

    public static String getExtension(String uri) {
        for(SupportedEmbeddedFontTypes type: SupportedEmbeddedFontTypes.values()) {
            if(uri.contains(type.typeString))
                return type.extension;
        }
        return "";
    }
}
