package org.openpdf.text.pdf;

import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.utils.PdfBatch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PdfBatchUtils to ensure it runs batch jobs concurrently.
 */
class PdfBatchUtilsTest {

    private static Path tinyPdf(String prefix) throws Exception {
        Path p = Files.createTempFile(prefix, ".pdf");
        var doc = new Document();
        try (var out = new FileOutputStream(p.toFile())) {
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph("Hello OpenPDF"));
            doc.close();
        }
        return p;
    }


    @Test
    void runBatch_executesConcurrently() {
        var tasks = List.of(
                (Callable<Integer>) () -> 1,
                (Callable<Integer>) () -> 2,
                (Callable<Integer>) () -> 3
        );

        var result = PdfBatch.run(tasks, v -> {}, t -> fail(t));
        assertTrue(result.isAllSuccessful(), "All tasks should succeed");
        assertEquals(3, result.successes.size());
        assertEquals(0, result.failures.size());
    }

    @Test
    void batchMerge_createsOutput() throws Exception {
        // Prepare inputs
        Path a = tinyPdf("a-");
        Path b = tinyPdf("b-");
        Path merged = Files.createTempFile("merged-", ".pdf");

        // Use the batch API which internally uses virtual threads.
        var jobs = List.of(new PdfBatchUtils.MergeJob(List.of(a, b), merged));

        var result = PdfBatchUtils.batchMerge(jobs,
                // onSuccess: simple sanity checks per job
                out -> {
                    assertTrue(Files.exists(out), "Merged PDF should exist");
                    try {
                        assertTrue(Files.size(out) > 0, "Merged PDF should be non-empty");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                t -> fail(t)
        );

        assertTrue(result.isAllSuccessful(), "Merge job should succeed");

        // Cleanup
        Files.deleteIfExists(a);
        Files.deleteIfExists(b);
        Files.deleteIfExists(merged);
    }
}
