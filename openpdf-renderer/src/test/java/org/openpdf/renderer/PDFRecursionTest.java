package org.openpdf.renderer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openpdf.renderer.function.PDFFunction;

/**
 * Tests for files in which an object refers back to itself, and for watchables that stop with an error.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PDFRecursionTest {

    private static final String CATALOG = "<< /Type /Catalog /Pages 2 0 R >>";
    private static final String PAGES = "<< /Type /Pages /Kids [3 0 R] /Count 1 >>";

    @Test
    void formThatDrawsItselfIsParsed() throws IOException {
        PDFFile file = open(CATALOG, PAGES,
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] /Contents 4 0 R"
                        + " /Resources << /XObject << /Fm0 5 0 R >> >> >>",
                stream("", "/Fm0 Do"),
                stream("/Type /XObject /Subtype /Form /BBox [0 0 10 10] /Resources << /XObject << /Fm0 5 0 R >> >>",
                        "0 0 5 5 re f /Fm0 Do /Fm0 Do"));

        PDFPage page = file.getPage(1, true);

        assertThat(page).isNotNull();
        assertThat(page.isFinished()).isTrue();
        assertThat(page.getCommandCount()).isBetween(1, 100);
    }

    @Test
    void formsThatDrawEachOtherAreParsed() throws IOException {
        String resources = "/Resources << /XObject << /A 5 0 R /B 6 0 R >> >>";
        PDFFile file = open(CATALOG, PAGES,
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] /Contents 4 0 R " + resources + " >>",
                stream("", "/A Do /B Do"),
                stream("/Type /XObject /Subtype /Form /BBox [0 0 10 10] " + resources, "/B Do /B Do /A Do"),
                stream("/Type /XObject /Subtype /Form /BBox [0 0 10 10] " + resources, "/A Do /A Do /B Do"));

        PDFPage page = file.getPage(1, true);

        assertThat(page).isNotNull();
        assertThat(page.isFinished()).isTrue();
    }

    @Test
    void pageThatIsItsOwnParentIsRead() throws IOException {
        PDFFile file = open(CATALOG, PAGES,
                "<< /Type /Page /Parent 3 0 R /MediaBox [0 0 100 100] /Contents 4 0 R >>",
                stream("", "0 0 5 5 re f"));

        PDFPage page = file.getPage(1, true);

        assertThat(page).isNotNull();
        assertThat(page.isFinished()).isTrue();
        assertThat(page.getBBox().getWidth()).isEqualTo(100);
    }

    @Test
    void pageTreeThatContainsItselfHasNoPage() throws IOException {
        PDFFile file = open(CATALOG, "<< /Type /Pages /Kids [2 0 R] /Count 1 >>");

        assertThat(file.getPage(1, true)).isNull();
    }

    @Test
    void imageThatIsItsOwnMaskIsRead() throws IOException {
        PDFFile file = open(CATALOG, PAGES,
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] /Contents 4 0 R"
                        + " /Resources << /XObject << /Im0 5 0 R >> >> >>",
                stream("", "q 10 0 0 10 0 0 cm /Im0 Do Q"),
                stream("/Type /XObject /Subtype /Image /Width 1 /Height 1 /ColorSpace /DeviceGray"
                        + " /BitsPerComponent 8 /SMask 5 0 R", "x"));

        PDFPage page = file.getPage(1, true);
        assertThat(page).isNotNull();
        assertThat(page.isFinished()).isTrue();

        PDFObject imageObj = file.getRoot().getDictRef("Pages").getDictRef("Kids").getAt(0)
                .getDictRef("Resources").getDictRef("XObject").getDictRef("Im0");
        PDFImage image = PDFImage.createImage(imageObj, new HashMap<>(), false);
        int masks = 0;
        while (image.getSMask() != null) {
            image = image.getSMask();
            masks++;
        }
        assertThat(masks).isBetween(1, 20);
    }

    @Test
    void nameTreeThatContainsItselfIsRejected() {
        HashMap<String, PDFObject> node = new HashMap<>();
        PDFObject root = new PDFObject(null, PDFObject.DICTIONARY, node);
        node.put("Limits", array(string("a"), string("z")));
        node.put("Kids", array(root));

        assertThatThrownBy(() -> new NameTree(root).find("m")).isInstanceOf(PDFParseException.class);
    }

    @Test
    void stitchingFunctionThatContainsItselfIsRejected() {
        HashMap<String, PDFObject> function = new HashMap<>();
        PDFObject functionObj = new PDFObject(null, PDFObject.DICTIONARY, function);
        function.put("FunctionType", number(3));
        function.put("Domain", array(number(0), number(1)));
        function.put("Functions", array(functionObj));
        function.put("Bounds", array());
        function.put("Encode", array(number(0), number(1)));

        assertThatThrownBy(() -> PDFFunction.getFunction(functionObj)).isInstanceOf(PDFParseException.class);
    }

    @Test
    void waitersAreReleasedWhenAWatchableStopsWithAnError() {
        BaseWatchable watchable = new BaseWatchable() {
            @Override
            protected int iterate() {
                throw new StackOverflowError("test");
            }
        };

        watchable.go();
        watchable.waitForFinish();

        assertThat(watchable.getStatus()).isEqualTo(Watchable.ERROR);
        assertThat(watchable.getException()).hasCauseInstanceOf(StackOverflowError.class);
    }

    @Test
    void pageIsFinishedWhenItsParserStopsWithAnError() throws InterruptedException {
        PDFPage page = new PDFPage(new Rectangle2D.Float(0, 0, 10, 10), 0);
        PDFParser parser = failingParser(page);

        parser.go();
        page.waitForFinish();

        assertThat(page.isFinished()).isTrue();
        parser.waitForFinish();
        assertThat(parser.getStatus()).isEqualTo(Watchable.ERROR);
    }

    @Test
    void errorOfASynchronousParserReachesTheCaller() {
        PDFPage page = new PDFPage(new Rectangle2D.Float(0, 0, 10, 10), 0);
        PDFParser parser = failingParser(page);

        assertThatThrownBy(() -> parser.go(true)).isInstanceOf(StackOverflowError.class);

        assertThat(page.isFinished()).isTrue();
        assertThat(parser.getStatus()).isEqualTo(Watchable.ERROR);
    }

    private static PDFParser failingParser(PDFPage page) {
        return new PDFParser(page, "0 0 5 5 re f".getBytes(StandardCharsets.ISO_8859_1), new HashMap<>()) {
            @Override
            public int iterate() {
                throw new StackOverflowError("test");
            }
        };
    }

    private static PDFObject number(int value) {
        return new PDFObject(null, PDFObject.NUMBER, Double.valueOf(value));
    }

    private static PDFObject string(String value) {
        return new PDFObject(null, PDFObject.STRING, value);
    }

    private static PDFObject array(PDFObject... items) {
        return new PDFObject(null, PDFObject.ARRAY, items);
    }

    private static String stream(String entries, String data) {
        return "<< " + entries + " /Length " + data.length() + " >>\nstream\n" + data + "\nendstream";
    }

    /**
     * Opens a file with the given objects, which are numbered from 1.
     */
    private static PDFFile open(String... objects) throws IOException {
        StringBuilder content = new StringBuilder("%PDF-1.4\n");
        StringBuilder xref = new StringBuilder("xref\n0 " + (objects.length + 1) + "\n0000000000 65535 f \n");
        for (int i = 0; i < objects.length; i++) {
            xref.append(String.format("%010d 00000 n \n", content.length()));
            content.append(i + 1).append(" 0 obj\n").append(objects[i]).append("\nendobj\n");
        }
        int startxref = content.length();
        content.append(xref).append("trailer\n<< /Size ").append(objects.length + 1).append(" /Root 1 0 R >>\n")
                .append("startxref\n").append(startxref).append("\n%%EOF\n");
        return new PDFFile(ByteBuffer.wrap(content.toString().getBytes(StandardCharsets.ISO_8859_1)));
    }
}
