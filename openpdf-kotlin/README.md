# OpenPDF Kotlin (Experimental)

**OpenPDF Kotlin** is an experimental Kotlin extension module for [OpenPDF](https://github.com/LibrePDF/OpenPDF) that provides idiomatic Kotlin APIs, utilities, and builder-style functionality for working with PDF documents.

This submodule is designed to make OpenPDF more convenient and expressive for Kotlin developers, using features such as:

- Extension functions for common tasks
- A lightweight Kotlin-style `PdfBuilder`
- DSL-inspired document generation

---

## 📦 Example Usage

```kotlin
import com.github.librepdf.kotlin.PdfBuilder
import java.io.FileOutputStream

fun main() {
    FileOutputStream("example.pdf").use { output ->
        PdfBuilder(output).build {
            title("Hello from Kotlin")
            subtitle("Using OpenPDF Kotlin")
            paragraph("This is a paragraph in a PDF file.")
            image("logo.png", scalePercent = 50f)
        }
    }
}
