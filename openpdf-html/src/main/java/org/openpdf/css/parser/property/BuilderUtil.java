package org.openpdf.css.parser.property;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.css.parser.CSSParseException;
import org.openpdf.css.parser.FSFunction;

import java.util.Set;

/**
 * Static utility functions to check types, etc for builders to use.
 */
public class BuilderUtil {
    private static final Set<Short> LENGTH_VALUES = Set.of(
            CSSPrimitiveValue.CSS_EMS,
            CSSPrimitiveValue.CSS_EXS,
            CSSPrimitiveValue.CSS_PX,
            CSSPrimitiveValue.CSS_IN,
            CSSPrimitiveValue.CSS_CM,
            CSSPrimitiveValue.CSS_MM,
            CSSPrimitiveValue.CSS_PT,
            CSSPrimitiveValue.CSS_PC
    );

    public static boolean isLength(CSSPrimitiveValue value) {
        short unit = value.getPrimitiveType();
        return LENGTH_VALUES.contains(unit) || (
            unit == CSSPrimitiveValue.CSS_NUMBER && value.getFloatValue(CSSPrimitiveValue.CSS_IN) == 0.0f
        );
    }

    public static void checkFunctionsAllowed(final FSFunction func, String... allowed) {
        for (String allow : allowed) {
            if (allow.equals(func.getName()))
                return;
        }

        throw new CSSParseException(String.format("Function %s not supported here", func.getName()), -1);
    }
}
