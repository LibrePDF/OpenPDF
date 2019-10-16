package com.lowagie.text.alignment;

import com.lowagie.text.Element;

/**
 * Represents a possible vertical alignment modes for document elements that can be aligned vertically.
 *
 * @author noavarice
 * @see WithVerticalAlignment
 * @since 1.2.7
 */
public enum VerticalAlignment {

    UNDEFINED(Element.ALIGN_UNDEFINED),
    TOP(Element.ALIGN_TOP),
    CENTER(Element.ALIGN_MIDDLE),
    BOTTOM(Element.ALIGN_BOTTOM),
    BASELINE(Element.ALIGN_BASELINE),
    ;

    private final int id;

    VerticalAlignment(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
    * Constructs {@link VerticalAlignment} instance from passed unique alignment {@code id}.
    *
    * @param id Alignment unique ID
    * @see Element#ALIGN_UNDEFINED
    * @see Element#ALIGN_TOP
    * @see Element#ALIGN_MIDDLE
    * @see Element#ALIGN_BOTTOM
    * @see Element#ALIGN_BASELINE
     * @return the alignment instance or null if {@code id} is not recognized,
    */
    public static VerticalAlignment of(final int id) {
        for (final VerticalAlignment alignment: values()) {
            if (alignment.id == id) {
                return alignment;
            }
        }

        return UNDEFINED;
    }
}
