package org.openpdf.pdf;

import org.openpdf.text.DocumentException;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.RandomAccessFileOrArray;
import org.jspecify.annotations.Nullable;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.pdf.FontDescription.Decorations;
import org.openpdf.util.XRRuntimeException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Locale.ROOT;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.Optional.ofNullable;
import static org.openpdf.css.constants.IdentValue.NORMAL;
import static org.openpdf.pdf.ITextFontResolver.convertWeightToInt;

/**
 * Uses code from iText's DefaultFontMapper and TrueTypeFont classes.  See
 * <a href="http://sourceforge.net/projects/itext/">http://sourceforge.net/projects/itext/</a> for license information.
 */
public class TrueTypeUtil {

    /**
     * Avoid using memory-mapped files for loading fonts.
     * On Windows, these memory-mapped files are not released from memory, causing memory leaks.
     * See <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/385#issuecomment-2352080728">github issue 385</a>.
     */
    private static final boolean AVOID_MEMORY_MAPPED_FILES = true;

    private static IdentValue guessStyle(BaseFont font) {
        String[][] names = font.getFullFontName();

        for (String[] name : names) {
            String lower = name[3].toLowerCase(ROOT);
            if (lower.contains("italic")) {
                return IdentValue.ITALIC;
            } else if (lower.contains("oblique")) {
                return IdentValue.OBLIQUE;
            }
        }

        return NORMAL;
    }

    public static Collection<String> getFamilyNames(BaseFont font) {
        String[][] names = font.getFamilyFontName();

        if (names.length == 1) {
            return singletonList(names[0][3]);
        }

        List<String> result = new ArrayList<>();
        for (String[] name : names) {
            if ((name[0].equals("1") && name[1].equals("0")) || name[2].equals("1033")) {
                result.add(name[3]);
            }
        }

        return result;
    }

    // HACK No accessor
    private static Map<String, int[]> extractTables(BaseFont font)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> current = font.getClass();

        while (current != null) {
            if (current.getName().endsWith(".TrueTypeFont")) {
                Field field = current.getDeclaredField("tables");
                field.setAccessible(AVOID_MEMORY_MAPPED_FILES);
                //noinspection unchecked
                return (Map<String, int[]>) field.get(font);
            }

            current = current.getSuperclass();
        }

        throw new NoSuchFieldException("Could not find tables field");
    }

    private static String getTTCName(String name) {
        int index = name.toLowerCase(ROOT).indexOf(".ttc,");
        return index < 0 ? name : name.substring(0, index + 4);
    }

    public static FontDescription extractDescription(String path, BaseFont font, @Nullable IdentValue fontWeightOverride) {
        try {
            Decorations decorations = readFontDecorations(path, font, fontWeightOverride);
            return new FontDescription(font, false, guessStyle(font), decorations);
        } catch (DocumentException | IOException | NoSuchFieldException | IllegalAccessException e) {
            throw new XRRuntimeException("Failed to read font description from %s".formatted(path), e);
        }
    }

    private static Decorations readFontDecorations(String path, BaseFont font, @Nullable IdentValue fontWeightOverride)
            throws IOException, NoSuchFieldException, IllegalAccessException, DocumentException {

        try (RandomAccessFileOrArray rf = new RandomAccessFileOrArray(getTTCName(path), false, AVOID_MEMORY_MAPPED_FILES)) {
            return readFontDecorations(path, font, rf, fontWeightOverride);
        }
    }

    public static FontDescription extractDescription(String path, byte[] contents,
                                                     BaseFont font, boolean isFromFontFace,
                                                     @Nullable IdentValue fontWeightOverride,
                                                     @Nullable IdentValue fontStyleOverride) {
        try {
            IdentValue style = requireNonNullElseGet(fontStyleOverride, () -> guessStyle(font));
            Decorations decorations = readFontDecorations(path, contents, font, fontWeightOverride);
            return new FontDescription(font, isFromFontFace, style, decorations);
        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
            throw new XRRuntimeException("Failed to read font description from %s".formatted(path), e);
        }
    }

    private static Decorations readFontDecorations(String path, byte[] contents, BaseFont font, @Nullable IdentValue fontWeightOverride)
            throws IOException, NoSuchFieldException, IllegalAccessException, DocumentException {
        try (RandomAccessFileOrArray rf = new RandomAccessFileOrArray(contents)) {
            return readFontDecorations(path, font, rf, fontWeightOverride);
        }
    }

    private static Decorations readFontDecorations(String path, BaseFont font, RandomAccessFileOrArray rf, @Nullable IdentValue fontWeightOverride)
            throws NoSuchFieldException, IllegalAccessException, DocumentException, IOException {
        Map<String, int[]> tables = extractTables(font);

        int[] location = tables.get("OS/2");
        if (location == null) {
            throw new DocumentException("Table 'OS/2' does not exist in " + path);
        }

        rf.seek(location[0]);
        int want = 4;
        long got = rf.skip(want);
        if (got < want) {
            throw new DocumentException("Skip TT font weight, expect read " + want + " bytes, but only got " + got);
        }

        int fontWeight = rf.readUnsignedShort();
        int weight = ofNullable(fontWeightOverride).map(w -> convertWeightToInt(w)).orElse(fontWeight);

        want = 20;
        got = rf.skip(want);
        if (got < want) {
            throw new DocumentException("Skip TT font strikeout, expect read " + want + " bytes, but only got " + got);
        }

        float yStrikeoutSize = rf.readShort();
        float yStrikeoutPosition = rf.readShort();
        float underlinePosition = 0;
        float underlineThickness = 0;

        location = tables.get("post");

        if (location != null) {
            rf.seek(location[0]);
            want = 8;
            got = rf.skip(want);
            if (got < want) {
                throw new DocumentException("Skip TT font underline, expect read " + want + " bytes, but only got " + got);
            }
            underlinePosition = rf.readShort();
            underlineThickness = rf.readShort();
        }

        return new Decorations(weight, yStrikeoutSize, yStrikeoutPosition, underlinePosition, underlineThickness);
    }
}
