package org.openpdf.pdf;

import org.jspecify.annotations.Nullable;
import org.openpdf.css.constants.IdentValue;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparingInt;

public class FontFamily {
    private final String _name;
    private final List<FontDescription> _fontDescriptions = new ArrayList<>();

    FontFamily(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public List<FontDescription> getFontDescriptions() {
        return _fontDescriptions;
    }

    public void addFontDescription(FontDescription description) {
        _fontDescriptions.add(description);
        _fontDescriptions.sort(comparingInt(FontDescription::getWeight));
    }

    @Nullable
    public FontDescription match(int desiredWeight, IdentValue style) {
        List<FontDescription> candidates = new ArrayList<>();

        for (FontDescription description : _fontDescriptions) {
            if (description.getStyle() == style) {
                candidates.add(description);
            }
        }

        if (candidates.isEmpty()) {
            if (style == IdentValue.ITALIC) {
                return match(desiredWeight, IdentValue.OBLIQUE);
            } else if (style == IdentValue.OBLIQUE) {
                return match(desiredWeight, IdentValue.NORMAL);
            } else {
                candidates.addAll(_fontDescriptions);
            }
        }

        FontDescription result = findByWeight(candidates, desiredWeight, SM_EXACT);

        if (result != null) {
            return result;
        } else {
            if (desiredWeight <= 500) {
                return findByWeight(candidates, desiredWeight, SM_LIGHTER_OR_DARKER);
            } else {
                return findByWeight(candidates, desiredWeight, SM_DARKER_OR_LIGHTER);
            }
        }
    }

    private static final int SM_EXACT = 1;
    private static final int SM_LIGHTER_OR_DARKER = 2;
    private static final int SM_DARKER_OR_LIGHTER = 3;

    @Nullable
    private FontDescription findByWeight(List<FontDescription> matches, int desiredWeight, int searchMode) {
        if (searchMode == SM_EXACT) {
            for (FontDescription description : matches) {
                if (description.getWeight() == desiredWeight) {
                    return description;
                }
            }
            return null;
        } else if (searchMode == SM_LIGHTER_OR_DARKER) {
            int offset;
            FontDescription description = null;
            for (offset = 0; offset < matches.size(); offset++) {
                description = matches.get(offset);
                if (description.getWeight() > desiredWeight) {
                    break;
                }
            }

            if (offset > 0 && description.getWeight() > desiredWeight) {
                return matches.get(offset - 1);
            } else {
                return description;
            }

        } else if (searchMode == SM_DARKER_OR_LIGHTER) {
            int offset;
            FontDescription description = null;
            for (offset = matches.size() - 1; offset >= 0; offset--) {
                description = matches.get(offset);
                if (description.getWeight() < desiredWeight) {
                    break;
                }
            }

            if (offset != matches.size() - 1 && description != null && description.getWeight() < desiredWeight) {
                return matches.get(offset + 1);
            } else {
                return description;
            }
        }

        return null;
    }
}
