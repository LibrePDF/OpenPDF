package org.openpdf.html;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Html2PdfBatchUtils to ensure it runs batch jobs on virtual threads.
 */
class HtmlToPdfBatchUtilsTest {

    @Test
    void testBatchHtmlStringsRunsOnVirtualThreads() throws Exception {
        String html = "<html><body><h1>Hello PDF Batch test</h1></body></html>";

        Path out1 = Files.createTempFile("vt-batch-1-", ".pdf");
        Path out2 = Files.createTempFile("vt-batch-2-", ".pdf");
        Path out3 = Files.createTempFile("vt-batch-3-", ".pdf");

        // Assert virtual-thread execution from inside each batch task.
        var vtAssertCustomizer = HtmlToPdfBatchUtils.setDpi(96).andThen(renderer ->
                assertTrue(Thread.currentThread().isVirtual(), "Batch task should run on a virtual thread")
        );

        var jobs = List.of(
                new HtmlToPdfBatchUtils.HtmlStringJob(
                        html, null, out1,
                        Optional.of(HtmlToPdfBatchUtils.CSS_A4_20MM),
                        Optional.of(vtAssertCustomizer)
                ),
                new HtmlToPdfBatchUtils.HtmlStringJob(
                        html, null, out2,
                        Optional.of(HtmlToPdfBatchUtils.CSS_A4_20MM),
                        Optional.of(vtAssertCustomizer)
                ),
                new HtmlToPdfBatchUtils.HtmlStringJob(
                        html, null, out3,
                        Optional.of(HtmlToPdfBatchUtils.CSS_A4_20MM),
                        Optional.of(vtAssertCustomizer)
                )
        );

        var result = HtmlToPdfBatchUtils.batchHtmlStrings(jobs, p -> {}, t -> fail(t));

        assertTrue(result.isAllSuccessful(), "All batch jobs should succeed");

        for (Path p : List.of(out1, out2, out3)) {
            assertTrue(Files.exists(p), "PDF should exist: " + p);
            assertTrue(Files.size(p) > 0, "PDF should be non-empty: " + p);
            Files.deleteIfExists(p);
        }
    }
}
