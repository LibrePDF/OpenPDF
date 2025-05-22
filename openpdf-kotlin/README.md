**OpenPDF Kotlin** is an *experimental* Kotlin extension module for [OpenPDF](https://github.com/LibrePDF/OpenPDF). 
It provides idiomatic Kotlin APIs, utilities, and builder-style functionality for working with PDF documents.

This submodule makes OpenPDF more convenient and expressive for Kotlin developers by offering:

- Extension functions for common PDF tasks
- A lightweight Kotlin-style `PdfBuilder`
- DSL-inspired document generation syntax

Kotlin pull requests are welcome!

---

## Example Usage

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
```


## TODO

- [ ] Improve `PdfBuilder` DSL syntax and flexibility (e.g. nested tables, headers, footers)
- [ ] Add unit tests for all Kotlin utilities
- [ ] Add `PdfFormUtil` for working with AcroForms (fill/read fields)
- [ ] Create `PdfMetadataUtil` for reading document properties
- [ ] Write more Kotlin usage examples and tutorials
- [ ] Explore multiplatform or Android compatibility
- [ ] Set up `markdownlint` and Kotlin style linter
