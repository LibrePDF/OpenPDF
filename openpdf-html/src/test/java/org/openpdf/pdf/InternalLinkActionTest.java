package org.openpdf.pdf;

import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InternalLinkActionTest {
    private static final String HTML = """
            <html><body>
            <p><a href="#target" onclick="app.alert('clicked');">link with onclick</a></p>
            <p><a href="#target">plain link</a></p>
            <p id="target">target</p>
            </body></html>""";

    @Test
    void onclickIsIgnoredByDefault() throws IOException {
        ITextRenderer renderer = new ITextRenderer();

        assertThat(renderer.getOutputDevice().isOnclickJavaScriptEnabled()).isFalse();
        assertThat(linkActionTypes(render(renderer))).containsExactly(PdfName.GOTO, PdfName.GOTO);
    }

    @Test
    void onclickBecomesJavaScriptActionWhenEnabled() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getOutputDevice().setOnclickJavaScriptEnabled(true);

        assertThat(linkActionTypes(render(renderer))).containsExactly(PdfName.JAVASCRIPT, PdfName.GOTO);
    }

    private static byte[] render(ITextRenderer renderer) {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        renderer.setDocumentFromString(HTML);
        renderer.layout();
        renderer.createPDF(pdf);
        return pdf.toByteArray();
    }

    private static List<PdfName> linkActionTypes(byte[] pdf) throws IOException {
        List<PdfName> types = new ArrayList<>();
        PdfReader reader = new PdfReader(pdf);
        try {
            PdfArray annotations = reader.getPageN(1).getAsArray(PdfName.ANNOTS);
            for (int i = 0; i < annotations.size(); i++) {
                PdfDictionary action = annotations.getAsDict(i).getAsDict(PdfName.A);
                types.add(action.getAsName(PdfName.S));
            }
        } finally {
            reader.close();
        }
        return types;
    }
}
