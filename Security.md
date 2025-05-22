# Security Policy for OpenPDF

## Responsibility Disclaimer

OpenPDF is a general-purpose PDF generation and manipulation library. It is **not a sandboxed or hardened environment**. OpenPDF processes input data such as file paths, image sources, font names, and HTML content **as-is**, without performing input validation, authentication, or permission checks.

 **It is the sole responsibility of the application developer to ensure that all input passed into OpenPDF is trusted, sanitized, and safe.**

OpenPDF does not implement any built-in mechanisms to protect against:
- Local file access
- Network access via file URLs
- Path traversal
- SSRF (Server-Side Request Forgery)
- Unsafe base64 content
- Memory exhaustion or denial-of-service from large or malformed PDFs

---

## Common Security Considerations

When using OpenPDF, application developers must consider the following security risks and implement proper countermeasures:

### 1. **Image Sources**
- OpenPDF supports image loading via:
  - Local file paths
  - Absolute file URIs (`file:///`)
  - Base64-encoded strings
  - External URLs (depending on usage)
- **Risk**: May allow attackers to read files like `/etc/passwd` or perform SSRF.
- **Recommended**: Disallow or strictly validate all image paths before passing them to OpenPDF.

### 2. **HTML Content and Inline Styles**
- OpenPDF parses HTML input and may honor embedded `<img src>`, `<style>`, font settings, colors, and alignment.
- **Risk**: Malicious or excessive styles can lead to layout breaking, memory overuse, or invisible data (e.g., white text on white background).
- **Recommended**: Sanitize HTML input using an HTML sanitizer such as [jsoup](https://jsoup.org/) before rendering.

### 3. **Fonts and Font Paths**
- Font names are passed to the configured font provider, which may include filesystem lookups.
- **Risk**: May trigger unintended font loading if input is attacker-controlled.
- **Recommended**: Restrict available fonts and avoid dynamic font registration based on user input.

### 4. **Large or Malformed PDFs**
- Processing or merging very large PDF files (>2 GB) may lead to:
  - Memory exhaustion
  - JVM crashes
  - Unbounded resource usage
- **Recommended**: Validate size limits before processing and consider using `MappedRandomAccessFile` for large files.

### 5. **Annotations and Links**
- OpenPDF allows adding links, including remote URLs and file links.
- **Risk**: May cause phishing or access to unintended resources.
- **Recommended**: Never render user-controlled URLs without filtering and validation.

---

## Security Best Practices

- **Sanitize all input** before using it in OpenPDF.
- **Avoid rendering untrusted HTML** directly.
- **Use input validation and whitelisting** for attributes such as `src`, `href`, `face`, `style`, etc.
- **Run PDF generation in a restricted environment** (e.g., container or sandbox) if user content is involved.
- **Impose size limits** on uploaded files and input data to prevent DoS.

---

Do not create and report CVE (Common Vulnerabilities and Exposures) for OpenPDF for non-real vulnerabilities.
This is like saying Apache Commons Net or Commons IO is insecure because they allow access to files and the Internet.

---

## Related Links

- [OpenPDF GitHub Repository](https://github.com/LibrePDF/OpenPDF)
- [OWASP Input Validation](https://owasp.org/www-community/Input_Validation)
- [OWASP Server Side Request Forgery (SSRF)](https://owasp.org/www-community/attacks/Server_Side_Request_Forgery)
- [OWASP Path Traversal](https://owasp.org/www-community/attacks/Path_Traversal)

