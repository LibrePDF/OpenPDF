package org.openpdf.rups.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openpdf.rups.io.OutputStreamResource;

class XfaFileTest {

    private static final String SECRET = "content-of-a-local-file";

    private static OutputStreamResource resource(String xml) {
        return os -> os.write(xml.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void plainXfaIsParsed() throws IOException, DocumentException {
        XfaFile xfa = new XfaFile(resource(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<xdp:xdp xmlns:xdp=\"http://ns.adobe.com/xdp/\"><template>text &amp; more</template>"
                        + "</xdp:xdp>"));

        assertThat(xfa.getXfaDocument().getRootElement().getName()).isEqualTo("xdp");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        xfa.writeTo(out);
        assertThat(out.toString(StandardCharsets.UTF_8)).contains("text &amp; more");
    }

    @Test
    void externalEntityIsNotResolved(@TempDir Path dir) throws IOException {
        Path secretFile = dir.resolve("secret.txt");
        Files.writeString(secretFile, SECRET);
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE xdp [<!ENTITY ext SYSTEM \"" + secretFile.toUri() + "\">]>"
                + "<xdp><template>&ext;</template></xdp>";

        assertThat(readOrReject(xml)).doesNotContain(SECRET);
    }

    @Test
    void doctypeIsRejected() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE xdp [<!ENTITY int \"value\">]>"
                + "<xdp><template>&int;</template></xdp>";

        assertThatThrownBy(() -> new XfaFile(resource(xml))).isInstanceOf(DocumentException.class);
    }

    /**
     * Returns the serialized document, or an empty string when the parser refused the input.
     */
    private static String readOrReject(String xml) throws IOException {
        try {
            XfaFile xfa = new XfaFile(resource(xml));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            xfa.writeTo(out);
            return out.toString(StandardCharsets.UTF_8);
        } catch (DocumentException e) {
            return "";
        }
    }
}
