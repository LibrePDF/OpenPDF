package org.openpdf.text;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openpdf.text.html.simpleparser.HTMLWorker;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.codec.wmf.MetaDo;

/**
 * Font names that come from a document (the face name of a font in a WMF image, the face of a font in HTML) must
 * only be looked up as font names. They must never be opened as a file or as a URL.
 */
class DocumentFontNameTest {

    private static final String FONT_RESOURCE = "/fonts/liberation/LiberationSerif-Regular.ttf";

    @TempDir
    Path tempDir;

    private HttpServer server;
    private final AtomicInteger requests = new AtomicInteger();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void wmfFaceNameIsNotOpenedAsUrl() throws Exception {
        byte[] wmf = wmfWithText(fontUrl());

        Image image = Image.getInstance(wmf);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(image);
        document.close();

        assertThat(out.size()).isPositive();
        assertThat(requests).hasValue(0);
    }

    @Test
    void htmlFontFaceIsNotOpenedAsUrl() throws Exception {
        List<Font> fonts = parseHtmlFonts("<p><font face=\"" + fontUrl() + "\">text</font></p>");

        assertThat(fonts).isNotEmpty().allSatisfy(font -> assertThat(font.getBaseFont()).isNull());
        assertThat(requests).hasValue(0);
    }

    @Test
    void htmlFontFamilyIsNotOpenedAsUrl() throws Exception {
        List<Font> fonts = parseHtmlFonts("<p><span style=\"font-family: " + fontUrl() + "\">text</span></p>");

        assertThat(fonts).isNotEmpty().allSatisfy(font -> assertThat(font.getBaseFont()).isNull());
        assertThat(requests).hasValue(0);
    }

    @Test
    void htmlFontFaceIsNotOpenedAsFile() throws Exception {
        String path = copyFont("face.ttf").toString();

        List<Font> fonts = parseHtmlFonts("<p><font face=\"" + path + "\">text</font></p>");

        assertThat(fonts).isNotEmpty().allSatisfy(font -> assertThat(font.getBaseFont()).isNull());
    }

    @Test
    void registeredFontLookupDoesNotOpenFilesOrUrls() throws Exception {
        FontFactoryImp fontFactory = new FontFactoryImp();
        String path = copyFont("lookup.ttf").toString();

        assertThat(registeredFont(fontFactory, path).getBaseFont()).isNull();
        assertThat(registeredFont(fontFactory, path + ",Bold").getBaseFont()).isNull();
        assertThat(registeredFont(fontFactory, tempDir.resolve("missing.afm").toString()).getBaseFont()).isNull();
        assertThat(registeredFont(fontFactory, fontUrl()).getBaseFont()).isNull();
        assertThat(requests).hasValue(0);
    }

    @Test
    void applicationCanStillPassFontPath() throws Exception {
        FontFactoryImp fontFactory = new FontFactoryImp();
        String path = copyFont("application.ttf").toString();

        Font font = fontFactory.getFont(path, BaseFont.CP1252, true, 10, Font.NORMAL, null);

        assertThat(font.getBaseFont()).isNotNull();
    }

    @Test
    void registeredAndBuiltInNamesStillResolve() throws Exception {
        FontFactoryImp fontFactory = new FontFactoryImp();
        String path = copyFont("registered.ttf").toString();
        fontFactory.register(path, "my-alias");
        fontFactory.register(path, "my-alias.ttf");

        assertThat(registeredFont(fontFactory, "my-alias").getBaseFont()).isNotNull();
        assertThat(registeredFont(fontFactory, "MY-ALIAS").getBaseFont()).isNotNull();
        assertThat(registeredFont(fontFactory, "my-alias.ttf").getBaseFont()).isNotNull();
        assertThat(registeredFont(fontFactory, "Liberation Serif").getBaseFont()).isNotNull();
        assertThat(registeredFont(fontFactory, FontFactory.HELVETICA).getBaseFont()).isNotNull();
        assertThat(registeredFont(fontFactory, "times").getBaseFont()).isNotNull();
        assertThat(registeredFont(fontFactory, "no-such-font").getBaseFont()).isNull();
        assertThat(registeredFont(fontFactory, null).getBaseFont()).isNull();
    }

    private static Font registeredFont(FontFactoryImp fontFactory, String name) {
        return fontFactory.getRegisteredFont(name, BaseFont.CP1252, true, 10, Font.NORMAL, null);
    }

    private String fontUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/x.ttf";
    }

    private Path copyFont(String fileName) throws IOException {
        Path target = tempDir.resolve(fileName);
        try (InputStream in = DocumentFontNameTest.class.getResourceAsStream(FONT_RESOURCE)) {
            Files.copy(in, target);
        }
        return target;
    }

    private static List<Font> parseHtmlFonts(String html) throws IOException {
        List<Font> fonts = new ArrayList<>();
        for (Element element : HTMLWorker.parseToList(new StringReader(html), null)) {
            for (Element chunk : element.getChunks()) {
                fonts.add(((Chunk) chunk).getFont());
            }
        }
        return fonts;
    }

    /**
     * Builds a placeable WMF that creates a font with the given face name, selects it and draws a text with it.
     */
    private static byte[] wmfWithText(String faceName) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // placeable header
        MetaDo.writeDWord(os, 0x9AC6CDD7);
        MetaDo.writeWord(os, 0);
        MetaDo.writeWord(os, 0);
        MetaDo.writeWord(os, 0);
        MetaDo.writeWord(os, 1000);
        MetaDo.writeWord(os, 1000);
        MetaDo.writeWord(os, 1000);
        MetaDo.writeDWord(os, 0);
        MetaDo.writeWord(os, 0);
        // metafile header
        os.write(new byte[18]);
        // META_CREATEFONTINDIRECT: 3 words header, 18 bytes of font attributes and 32 bytes for the face name
        MetaDo.writeDWord(os, 28);
        MetaDo.writeWord(os, MetaDo.META_CREATEFONTINDIRECT);
        MetaDo.writeWord(os, 100);
        os.write(new byte[16]);
        byte[] name = new byte[32];
        byte[] face = faceName.getBytes(StandardCharsets.ISO_8859_1);
        assertThat(face.length).isLessThan(name.length);
        System.arraycopy(face, 0, name, 0, face.length);
        os.write(name);
        // META_SELECTOBJECT
        MetaDo.writeDWord(os, 4);
        MetaDo.writeWord(os, MetaDo.META_SELECTOBJECT);
        MetaDo.writeWord(os, 0);
        // META_TEXTOUT
        MetaDo.writeDWord(os, 7);
        MetaDo.writeWord(os, MetaDo.META_TEXTOUT);
        MetaDo.writeWord(os, 2);
        os.write('H');
        os.write('i');
        MetaDo.writeWord(os, 500);
        MetaDo.writeWord(os, 100);
        // end of file
        MetaDo.writeDWord(os, 3);
        MetaDo.writeWord(os, 0);
        return os.toByteArray();
    }
}
