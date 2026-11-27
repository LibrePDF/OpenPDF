# AGENTS.md

Guidance for AI coding agents working on OpenPDF. Human contributors should start with
[CONTRIBUTING.md](CONTRIBUTING.md).

## Project

OpenPDF is an open source Java library for creating, editing, rendering and encrypting PDF files.
It is licensed under LGPL 2.1 and MPL 2.0. The `master` branch requires Java 21 or later.

## Modules

| Module                | Purpose                                              |
|-----------------------|------------------------------------------------------|
| `openpdf-core`        | The PDF library (`org.openpdf.text`)                 |
| `openpdf-html`        | HTML and CSS to PDF                                  |
| `openpdf-renderer`    | Renders PDF pages to images                          |
| `openpdf-kotlin`      | Kotlin extensions                                    |
| `openpdf-fonts-extra` | Additional fonts                                     |
| `pdf-swing`           | Swing tools, including the RUPS PDF inspector        |
| `pdf-toolbox`         | Toolbox application, command line tools and examples |

## Build and test

Use the Maven wrapper from the repository root:

```shell
./mvnw clean install                                   # build and test all modules
./mvnw -pl openpdf-core test                           # test one module
./mvnw -pl openpdf-core test -Dtest=PdfSmartCopyTest   # run one test class
./mvnw checkstyle:check                                # check the code style
```

Run the tests and `checkstyle:check` before opening a pull request.

## Code style

- Google Java Style with 4 spaces indentation, 8 spaces continuation indent and 120 characters
  line length. The rules are in `checkstyle.xml` and `.editorconfig`.
- Keep changes small and focused. Do not reformat or reorganise code you are not changing.
- Add or update JUnit tests for every behaviour change. Tests use JUnit 6 and AssertJ.
- Keep public API changes to a minimum and call them out in the pull request description.
- New dependencies need a good reason. Prefer optional dependencies for optional features.

## Pull requests

- Open pull requests against `master` and fill in the pull request template.
- Contributors must use their real name, see [CONTRIBUTING.md](CONTRIBUTING.md).
- The CI runs the Maven build on Java 21 and later, CodeQL, SonarCloud, Codacy and CodeFactor.
  Fix what they report on the lines you changed.
- Reference the issue a pull request fixes, for example `Fixes #1234`.

## Security

PDF files are untrusted input. Code that parses PDF content must not throw unexpected runtime
exceptions, loop forever or allocate unbounded memory on malformed files. Report vulnerabilities
as described in [Security.md](Security.md).
