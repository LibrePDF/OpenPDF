package com.lowagie.tools;

import com.lowagie.text.pdf.PdfReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConcatPdfTest {

    public ConcatPdfTest() {
        super();
    }

    @Test
    public void testConcat1() throws IOException {

        List<File> sources = new ArrayList<>();
        sources.add(new File("src/test/resources/groups.pdf"));
        sources.add(new File("src/test/resources/layers.pdf"));

        File target = new File("target/test-pdfs/concat1.pdf");
        target.getParentFile().mkdirs();
        ConcatPdf.concat(sources, target);

        Assertions.assertEquals(2, countPages(target));
    }

    @Test
    public void testConcat2() throws IOException {

        List<File> sources = new ArrayList<>();
        sources.add(new File("src/test/resources/groups.pdf"));
        sources.add(new File("src/test/resources/pattern.pdf"));
        sources.add(new File("src/test/resources/templates.pdf"));
        sources.add(new File("src/test/resources/layers.pdf"));

        File target = new File("target/test-pdfs/concat2.pdf");
        target.getParentFile().mkdirs();
        ConcatPdf.concat(sources, target);

        Assertions.assertEquals(5, countPages(target));
    }


    private int countPages(File file) {

        try {
            PdfReader reader = new PdfReader(new BufferedInputStream(Files.newInputStream(file.toPath())));
            int count = reader.getNumberOfPages();
            reader.close();
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

}
