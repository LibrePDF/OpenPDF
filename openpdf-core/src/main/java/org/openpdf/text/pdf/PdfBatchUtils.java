/*
 * Copyright 2025 OpenPDF
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

package org.openpdf.text.pdf;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Rectangle;
import org.openpdf.text.utils.PdfBatch;
import org.openpdf.text.utils.PdfBatch.BatchResult;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import java.util.function.Consumer;

/**
 * The PdfBatchUtils class provides high-level utilities for performing common PDF operations—such as merging, watermarking, encrypting, and splitting—in batch mode 
 * using Java 21 virtual threads for efficient concurrent execution.
 *
 */
public final class PdfBatchUtils {

    private PdfBatchUtils() {}

    // ------------------------- Common job records -------------------------

    /** Merge several PDFs into one. */
    public record MergeJob(List<Path> inputs, Path output) {}

    /** Add a semi-transparent text watermark to all pages. */
    public record WatermarkJob(Path input, Path output, String text, float fontSize, float opacity) {}

    /** Encrypt a PDF with given passwords and permissions. */
    public record EncryptJob(Path input, Path output,
                             String userPassword, String ownerPassword,
                             int permissions, int encryptionType) {}

    /** Split one PDF into per-page PDFs in the given directory (files will be named baseName_pageX.pdf). */
    public record SplitJob(Path input, Path outputDir, String baseName) {}


    // ------------------------- Merge -------------------------

    /** Merge one set of inputs into a single output file. */
    public static Path merge(List<Path> inputs, Path output) throws IOException, DocumentException {
        Objects.requireNonNull(inputs, "inputs");
        Objects.requireNonNull(output, "output");
        Files.createDirectories(output.getParent());

        try (var fos = new FileOutputStream(output.toFile())) {
            Document doc = new Document();
            PdfCopy copy = new PdfCopy(doc, fos);
            doc.open();
            for (Path in : inputs) {
                try (PdfReader reader = new PdfReader(Files.readAllBytes(in))) {
                    int n = reader.getNumberOfPages();
                    for (int i = 1; i <= n; i++) {
                        copy.addPage(copy.getImportedPage(reader, i));
                    }
                }
            }
            doc.close();
        }
        return output;
    }

    /** Batch merge. */
    public static BatchResult<Path> batchMerge(List<MergeJob> jobs, Consumer<Path> onSuccess, Consumer<Throwable> onFailure) {
        return PdfBatch.run(jobs.stream().map(job -> (Callable<Path>) () -> merge(job.inputs, job.output)).toList(),
                onSuccess, onFailure);
    }

    // ------------------------- Watermark -------------------------

    /** Watermark one PDF with text on every page (centered, diagonal). */
    public static Path watermark(Path input, Path output, String text, float fontSize, float opacity)
            throws IOException, DocumentException {
        Files.createDirectories(output.getParent());
        try (PdfReader reader = new PdfReader(Files.readAllBytes(input));
                var out = new FileOutputStream(output.toFile())) {
            PdfStamper stamper = new PdfStamper(reader, out);
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
            Font font = new Font(bf, fontSize);

            PdfGState gs = new PdfGState();
            gs.setFillOpacity(opacity);

            int n = reader.getNumberOfPages();
            for (int i = 1; i <= n; i++) {
                Rectangle pageSize = reader.getPageSizeWithRotation(i);
                float x = (pageSize.getLeft() + pageSize.getRight()) / 2f;
                float y = (pageSize.getTop() + pageSize.getBottom()) / 2f;

                PdfContentByte over = stamper.getOverContent(i);
                over.saveState();
                over.setGState(gs);
                over.beginText();
                over.setFontAndSize(bf, font.getSize());
                over.showTextAligned(Element.ALIGN_CENTER, text, x, y, 45f);
                over.endText();
                over.restoreState();
            }
            stamper.close();
        }
        return output;
    }

    /** Batch watermark. */
    public static BatchResult<Path> batchWatermark(List<WatermarkJob> jobs, Consumer<Path> onSuccess, Consumer<Throwable> onFailure) {
        return PdfBatch.run(jobs.stream().map(j -> (Callable<Path>) () -> watermark(j.input, j.output, j.text, j.fontSize, j.opacity)).toList(), onSuccess, onFailure);
    }

    // ------------------------- Encrypt -------------------------

    /** Encrypt one PDF. */
    public static Path encrypt(Path input, Path output,
            String userPassword, String ownerPassword,
            int permissions, int encryptionType)
            throws IOException, DocumentException {
        Files.createDirectories(output.getParent());
        try (PdfReader reader = new PdfReader(Files.readAllBytes(input));
                var out = new FileOutputStream(output.toFile())) {
            PdfStamper stamper = new PdfStamper(reader, out);
            stamper.setEncryption(
                    userPassword != null ? userPassword.getBytes() : null,
                    ownerPassword != null ? ownerPassword.getBytes() : null,
                    permissions,
                    encryptionType);
            stamper.close();
        }
        return output;
    }

    /** Batch encrypt. */
    public static BatchResult<Path> batchEncrypt(List<EncryptJob> jobs, Consumer<Path> onSuccess, Consumer<Throwable> onFailure) {
        return PdfBatch.run(jobs.stream().map(j -> (Callable<Path>) () -> encrypt(j.input, j.output, j.userPassword, j.ownerPassword, j.permissions, j.encryptionType)).toList(), onSuccess, onFailure);
    }

    // ------------------------- Split -------------------------

    /** Split one PDF to per-page PDFs. */
    public static List<Path> split(Path input, Path outputDir, String baseName) throws IOException, DocumentException {
        Files.createDirectories(outputDir);
        List<Path> outputs = new ArrayList<>();
        try (PdfReader reader = new PdfReader(Files.readAllBytes(input))) {
            int n = reader.getNumberOfPages();
            for (int i = 1; i <= n; i++) {
                Path out = outputDir.resolve(baseName + "_page" + i + ".pdf");
                try (var fos = new FileOutputStream(out.toFile())) {
                    Document doc = new Document(reader.getPageSizeWithRotation(i));
                    PdfCopy copy = new PdfCopy(doc, fos);
                    doc.open();
                    copy.addPage(copy.getImportedPage(reader, i));
                    doc.close();
                }
                outputs.add(out);
            }
        }
        return outputs;
    }

    /** Batch split. */
    public static BatchResult<List<Path>> batchSplit(List<SplitJob> jobs, Consumer<List<Path>> onSuccess, Consumer<Throwable> onFailure) {
        return PdfBatch.run(jobs.stream().map(j -> (Callable<List<Path>>) () -> split(j.input, j.outputDir, j.baseName)).toList(), onSuccess, onFailure);
    }

    // ------------------------- Convenience helpers -------------------------

    /** Quick permissions helper. */
    public static int perms(boolean print, boolean modify, boolean copy, boolean annotate) {
        int p = 0;
        if (print) p |= PdfWriter.ALLOW_PRINTING;
        if (modify) p |= PdfWriter.ALLOW_MODIFY_CONTENTS;
        if (copy) p |= PdfWriter.ALLOW_COPY;
        if (annotate) p |= PdfWriter.ALLOW_MODIFY_ANNOTATIONS;
        return p;
    }

    /** AES 128 vs 256 convenience. */
    public static int aes128() { return PdfWriter.ENCRYPTION_AES_128; }
    public static int aes256() { return PdfWriter.ENCRYPTION_AES_256_V3; }

    /** Small utility for closing Closeables, ignoring exceptions. */
    private static void closeQuietly(Closeable c) { try { if (c != null) c.close(); } catch (Exception ignored) {} }

}
