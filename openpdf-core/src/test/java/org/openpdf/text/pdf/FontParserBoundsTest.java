package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.Font;
import org.openpdf.text.Paragraph;

/**
 * Sizes, ranges and indexes that are read from a font file must be checked before they are used.
 */
class FontParserBoundsTest {

    private static final String TTF_RESOURCE = "fonts/liberation/LiberationSerif-Regular.ttf";
    private static final String OTF_RESOURCE = "fonts/jaldi/Jaldi-Bold.otf";
    private static final int CALLSUBR = 10;
    private static final int RETURN = 11;
    private static final int NUMBER_ZERO = 139;

    // cmap format 12

    @Test
    void format12GroupCountMustFitInTable() {
        assertThatCode(() -> TrueTypeFont.checkFormat12GroupCount(0, 0)).doesNotThrowAnyException();
        assertThatCode(() -> TrueTypeFont.checkFormat12GroupCount(2, 24)).doesNotThrowAnyException();
        assertThatThrownBy(() -> TrueTypeFont.checkFormat12GroupCount(3, 24)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> TrueTypeFont.checkFormat12GroupCount(-1, 24)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> TrueTypeFont.checkFormat12GroupCount(Integer.MAX_VALUE, 1000))
                .isInstanceOf(IOException.class);
    }

    @Test
    void format12GroupRangeIsChecked() throws IOException {
        assertThat(TrueTypeFont.addFormat12Group(0, 0x41, 0x5A, 36)).isEqualTo(26);
        assertThat(TrueTypeFont.addFormat12Group(26, 0x10FFFF, 0x10FFFF, 0)).isEqualTo(27);
        assertThat(TrueTypeFont.addFormat12Group(0, 0, 0x10FFFF, 0)).isEqualTo(0x110000);

        assertThatThrownBy(() -> TrueTypeFont.addFormat12Group(0, 0, Integer.MAX_VALUE, 0))
                .isInstanceOf(IOException.class);
        assertThatThrownBy(() -> TrueTypeFont.addFormat12Group(0, 0, 0x110000, 0)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> TrueTypeFont.addFormat12Group(0, 0x5A, 0x41, 0)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> TrueTypeFont.addFormat12Group(0, -1, 0x41, 0)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> TrueTypeFont.addFormat12Group(0, Integer.MIN_VALUE, -1, 0))
                .isInstanceOf(IOException.class);
        assertThatThrownBy(() -> TrueTypeFont.addFormat12Group(0, 0x41, 0x5A, -1)).isInstanceOf(IOException.class);
    }

    @Test
    void format12TotalNumberOfMappingsIsLimited() {
        assertThatThrownBy(() -> TrueTypeFont.addFormat12Group(0x110000, 0x41, 0x41, 0))
                .isInstanceOf(IOException.class);
        assertThatThrownBy(() -> TrueTypeFont.addFormat12Group(0x100000, 0, 0x10FFFF, 0))
                .isInstanceOf(IOException.class);
    }

    @Test
    void format12SubtableIsRead() throws IOException {
        TrueTypeFont font = format12Font(2, 0x41, 0x43, 1, 0x1F600, 0x1F600, 7);

        HashMap<Integer, int[]> map = font.readFormat12();

        assertThat(map).hasSize(4);
        assertThat(map.get(0x41)).containsExactly(1, 100);
        assertThat(map.get(0x43)).containsExactly(3, 300);
        assertThat(map.get(0x1F600)).containsExactly(7, 300);
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void format12GroupCoveringAllIntegersIsRejected() throws IOException {
        TrueTypeFont font = format12Font(1, 0, Integer.MAX_VALUE, 0);

        assertThatThrownBy(font::readFormat12).isInstanceOf(IOException.class);
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void format12OverlappingGroupsAreRejected() throws IOException {
        TrueTypeFont font = format12Font(3, 0, 0x10FFFF, 0, 0, 0x10FFFF, 0, 0, 0x10FFFF, 0);

        assertThatThrownBy(font::readFormat12).isInstanceOf(IOException.class);
    }

    @Test
    void format12GroupCountLargerThanTableIsRejected() throws IOException {
        TrueTypeFont font = format12Font(1000, 0x41, 0x43, 1);

        assertThatThrownBy(font::readFormat12).isInstanceOf(IOException.class);
    }

    // sizes of tables

    @Test
    void tableMustBeInsideFile() {
        assertThatCode(() -> TrueTypeFont.checkTableLocation("loca", 100, 900, 1000, "font"))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> TrueTypeFont.checkTableLocation("loca", 100, 901, 1000, "font"))
                .isInstanceOf(DocumentException.class);
        assertThatThrownBy(() -> TrueTypeFont.checkTableLocation("loca", 100, -4, 1000, "font"))
                .isInstanceOf(DocumentException.class);
        assertThatThrownBy(() -> TrueTypeFont.checkTableLocation("loca", -1, 4, 1000, "font"))
                .isInstanceOf(DocumentException.class);
        assertThatThrownBy(() -> TrueTypeFont.checkTableLocation("loca", Integer.MAX_VALUE, Integer.MAX_VALUE,
                1000, "font")).isInstanceOf(DocumentException.class);
    }

    @Test
    void locaTableLargerThanFileIsRejected() throws IOException {
        byte[] font = readResource(TTF_RESOURCE);
        int entry = findTableEntry(font, "loca");
        ByteBuffer.wrap(font).putInt(entry + 12, 0x7FFFFFF0);

        assertThatThrownBy(() -> BaseFont.createFont("broken-loca.ttf", BaseFont.CP1252, false, false, font, null))
                .isInstanceOf(DocumentException.class);
    }

    @Test
    void locaTableWithNegativeLengthIsRejected() throws IOException {
        byte[] font = readResource(TTF_RESOURCE);
        int entry = findTableEntry(font, "loca");
        ByteBuffer.wrap(font).putInt(entry + 12, -8);

        assertThatThrownBy(() -> BaseFont.createFont("negative-loca.ttf", BaseFont.CP1252, false, false, font, null))
                .isInstanceOf(DocumentException.class);
    }

    @Test
    void unitsPerEmOfZeroIsRejected() throws IOException {
        byte[] font = readResource(TTF_RESOURCE);
        int head = ByteBuffer.wrap(font).getInt(findTableEntry(font, "head") + 8);
        ByteBuffer.wrap(font).putShort(head + 18, (short) 0);

        assertThatThrownBy(() -> BaseFont.createFont("no-units.ttf", BaseFont.CP1252, false, false, font, null))
                .isInstanceOf(DocumentException.class)
                .hasMessageContaining("unitsPerEm");
    }

    @Test
    void unchangedFontIsStillRead() throws IOException {
        byte[] font = readResource(TTF_RESOURCE);

        BaseFont baseFont = BaseFont.createFont("unchanged.ttf", BaseFont.CP1252, false, false, font, null);

        assertThat(baseFont.getWidth("A")).isPositive();
        assertThat(baseFont.getCharBBox('A')).isNotNull();
    }

    // TrueType collections

    @Test
    void numberOfFontsInCollectionMustFitInFile() {
        assertThatCode(() -> EnumerateTTC.checkDirCount(3, 12)).doesNotThrowAnyException();
        assertThatThrownBy(() -> EnumerateTTC.checkDirCount(4, 12)).isInstanceOf(DocumentException.class);
        assertThatThrownBy(() -> EnumerateTTC.checkDirCount(-1, 12)).isInstanceOf(DocumentException.class);
    }

    @Test
    void collectionWithHugeNumberOfFontsIsRejected() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.write("ttcf".getBytes(StandardCharsets.ISO_8859_1));
        out.writeInt(0x00010000);
        out.writeInt(Integer.MAX_VALUE);
        out.writeInt(12);

        assertThatThrownBy(() -> new EnumerateTTC(bytes.toByteArray())).isInstanceOf(DocumentException.class);
        assertThatThrownBy(() -> BaseFont.enumerateTTCNames(bytes.toByteArray()))
                .isInstanceOf(DocumentException.class);
    }

    // CFF subroutines

    @Test
    void subroutineCallingItselfIsRejected() throws IOException {
        CffWithSubrs cff = cffWithSubrs(new byte[][]{{(byte) NUMBER_ZERO, CALLSUBR}});

        assertThatThrownBy(() -> cff.calcHints(0))
                .isInstanceOf(ExceptionConverter.class)
                .hasMessageContaining("nested too deeply");
    }

    @Test
    void subroutineIndexOutsideIndexIsRejected() throws IOException {
        CffWithSubrs tooHigh = cffWithSubrs(new byte[][]{{(byte) (NUMBER_ZERO + 1), CALLSUBR}});
        CffWithSubrs negative = cffWithSubrs(new byte[][]{{(byte) (NUMBER_ZERO - 1), CALLSUBR}});

        assertThatThrownBy(() -> tooHigh.calcHints(0))
                .isInstanceOf(ExceptionConverter.class)
                .hasMessageContaining("Invalid subroutine index");
        assertThatThrownBy(() -> negative.calcHints(0))
                .isInstanceOf(ExceptionConverter.class)
                .hasMessageContaining("Invalid subroutine index");
    }

    @Test
    void subroutinesNestedTenDeepAreAccepted() throws IOException {
        // the charstring calls subroutine 1, which calls subroutine 2, and so on up to subroutine 10
        CffWithSubrs cff = cffWithSubrs(subrChain(10));

        assertThatCode(() -> cff.calcHints(0)).doesNotThrowAnyException();
    }

    @Test
    void subroutinesNestedElevenDeepAreRejected() throws IOException {
        CffWithSubrs cff = cffWithSubrs(subrChain(11));

        assertThatThrownBy(() -> cff.calcHints(0))
                .isInstanceOf(ExceptionConverter.class)
                .hasMessageContaining("nested too deeply");
    }

    @Test
    void cffFontIsStillSubset() throws Exception {
        BaseFont baseFont = BaseFont.createFont("Jaldi-Bold.otf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false,
                readResource(OTF_RESOURCE), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph("The quick brown fox jumps over the lazy dog 0123456789",
                new Font(baseFont, 12)));
        document.close();

        assertThat(out.size()).isPositive();
    }

    /**
     * Creates a font that is positioned behind the format field of a 'cmap' format 12 subtable.
     *
     * @param nGroups the number of groups to declare
     * @param groups  the start character code, end character code and start glyph of the groups to write
     */
    private static TrueTypeFont format12Font(int nGroups, int... groups) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeShort(12);
        out.writeShort(0);
        out.writeInt(16 + groups.length * 4);
        out.writeInt(0);
        out.writeInt(nGroups);
        for (int value : groups) {
            out.writeInt(value);
        }
        TrueTypeFont font = new TrueTypeFont();
        font.GlyphWidths = new int[]{0, 100, 200, 300};
        font.tables = new HashMap<>();
        font.tables.put("cmap", new int[]{0, bytes.size()});
        font.rf = new RandomAccessFileOrArray(bytes.toByteArray());
        font.rf.seek(2);
        return font;
    }

    /**
     * Builds a chain of charstrings: the first one calls subroutine 1, subroutine 1 calls subroutine 2 and so on. The
     * last subroutine returns.
     */
    private static byte[][] subrChain(int depth) {
        byte[][] subrs = new byte[depth + 1][];
        for (int k = 0; k < depth; ++k) {
            subrs[k] = new byte[]{(byte) (NUMBER_ZERO + k + 1), CALLSUBR};
        }
        subrs[depth] = new byte[]{RETURN};
        return subrs;
    }

    /**
     * Opens the CFF data of a font and puts the given charstrings behind it, where they can be used as subroutines.
     */
    private static CffWithSubrs cffWithSubrs(byte[][] subrs) throws IOException {
        TrueTypeFont font = (TrueTypeFont) BaseFont.createFont("Jaldi-Bold.otf", BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED, false, readResource(OTF_RESOURCE), null);
        byte[] cff = font.readCffFont();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(cff);
        int[] offsets = new int[subrs.length + 1];
        for (int k = 0; k < subrs.length; ++k) {
            offsets[k] = bytes.size();
            bytes.write(subrs[k]);
        }
        offsets[subrs.length] = bytes.size();
        CFFFontSubset subset = new CFFFontSubset(new RandomAccessFileOrArray(bytes.toByteArray()), new HashMap<>());
        return new CffWithSubrs(subset, offsets);
    }

    private static byte[] readResource(String name) throws IOException {
        try (InputStream stream = BaseFont.getResourceStream(name, null)) {
            assertThat(stream).isNotNull();
            return stream.readAllBytes();
        }
    }

    /**
     * Finds the entry of a table in the table directory of a font file.
     */
    private static int findTableEntry(byte[] font, String tag) {
        int numTables = ByteBuffer.wrap(font).getShort(4);
        for (int k = 0; k < numTables; ++k) {
            int entry = 12 + k * 16;
            if (tag.equals(new String(font, entry, 4, StandardCharsets.ISO_8859_1))) {
                return entry;
            }
        }
        throw new IllegalArgumentException("No table " + tag);
    }

    private record CffWithSubrs(CFFFontSubset subset, int[] offsets) {

        void calcHints(int subr) {
            subset.CalcHints(offsets[subr], offsets[subr + 1], 0, 0, offsets);
        }
    }
}
