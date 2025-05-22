package com.github.librepdf.kotlin

import com.lowagie.text.pdf.PdfReader
import java.io.File

/**
 * Utility object for extracting plain text from PDF files.
 */
object PdfTextExtractorUtil {

  /**
   * Extracts text from all pages of the given PDF file.
   *
   * @param pdfFile File object representing the PDF.
   * @return Combined text from all pages.
   */
  fun extractText(pdfFile: File): String {
    val reader = PdfReader(pdfFile.absolutePath)
    val text = buildString {
      for (i in 1..reader.numberOfPages) {
        val contentBytes = reader.getPageContent(i)
        val contentString = String(contentBytes)
        appendLine(contentString)
      }
    }
    reader.close()
    return text
  }


}
