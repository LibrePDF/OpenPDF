package com.lowagie.text.utils;

public final class NumberUtilities {

    private NumberUtilities() {
    }

    /**
     * Try parse float from string and return null in case of {@link NumberFormatException}
     *
     * @param value string value
     * @return parsed value or null
     */
    public static Float parseFloat(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static float parseFloat(String value, float defaultValue) {
        Float result = parseFloat(value);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }


    /**
     * Try parse int from string and return null in case of {@link NumberFormatException}
     *
     * @param value string value
     * @return parsed value or null
     */
    public static Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
