package com.github.librepdf.kotlin

import com.lowagie.text.Document
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfImportedPage
import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Utility class to merge multiple PDF files into one.
 */
object PdfMerger {

  /**
   * Merges multiple PDF files into a single output stream.
   *
   * @param inputFiles List of PDF files to merge.
   * @param outputStream Stream to write the merged PDF.
   */
  fun merge(inputFiles: List<File>, outputStream: OutputStream) {
    val document = Document()
    val writer = PdfWriter.getInstance(document, outputStream)
    document.open()
    val cb: PdfContentByte = writer.directContent

    inputFiles.forEach { file ->
      val reader = PdfReader(file.absolutePath)
      for (pageNum in 1..reader.numberOfPages) {
        document.newPage()
        val page: PdfImportedPage = writer.getImportedPage(reader, pageNum)
        cb.addTemplate(page, 0f, 0f)
      }
      reader.close()
    }

    document.close()
  }

  /**
   * Overload to merge and write directly to a file.
   */
  fun mergeToFile(inputFiles: List<File>, outputFile: File) {
    FileOutputStream(outputFile).use { out ->
      merge(inputFiles, out)
    }
  }
}
