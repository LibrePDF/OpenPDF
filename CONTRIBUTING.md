# Contributing to OpenPDF

To contribute code to the OpenPDF project, your GitHub account must contain your real name, so that 
we can verify your identity. This is to ensure the trust, security and integrity of the OpenPDF 
project, and to prevent security incidents such as the "XZ Utils backdoor". Knowning the real name
of the contributors will also identify and prevent conflict of interests. 

## How to contribute with Code

1. Create a GitHub account
2. Fork the project on GitHub
3. Make any improvements you want in your fork (in any branch)
4. Create a pull request

### Building the project

1. Clone the repository (or your fork)
2. Run `mvn clean install` in the root directory

### Compatibility matrix

| OpenPDF version | Java version |
|-----------------|--------------|
| 1.3             | 8            |
| 1.4             | 11           |
| 2.0 (master)    | 17           |

### Please note

- We use the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) with
  some exceptions
  - Indentation: 4 spaces
  - Continuation indent: 8 spaces
  - Line length: 120 characters
- Make sure to add tests for your changes
- Fix any checkstyle issues before submitting your pull request (`mvn checkstyle:check`)
- Fix any issues found by the CI build after submitting your pull request

### Configuring your IDE

1. Configure the formatter (configuration files are in the `config` directory) or
2. Use the editorconfig plugin for your IDE
3. Use checkstyle plugin for your IDE (configuration is `checkstyle.xml`), or run `mvn checkstyle:check`
4. Use sonarlint plugin for your IDE

### Other code quality tools we use

1. Codacy: https://app.codacy.com/gh/LibrePDF/OpenPDF/dashboard
2. CodeFactor: https://www.codefactor.io/repository/github/librepdf/openpdf
3. CodeQL: https://github.com/LibrePDF/OpenPDF/actions/workflows/codeql.yml
4. SonarCloud: https://sonarcloud.io/dashboard?id=LibrePDF_OpenPDF

### How to contribute with Documentation

- Feel free to add content to the [Wiki](https://github.com/LibrePDF/OpenPDF/wiki)
- Also participate in the [Discussions](https://github.com/LibrePDF/OpenPDF/discussions), answering and asking questions
- You may also contribute with translations in `src/main/java/com/lowagie/text/error_messages`
- Or add some examples in the module `pdf-toolbox`
- Our [Migration Guide](https://github.com/LibrePDF/OpenPDF/wiki/Migrating-from-iText-2-and-4) need some love, so
  feel free to contribute. Please describe there the minor changes you had to do to migrate from iText to OpenPDF.

### How to contribute with Bug Reports

- Please be as detailed as possible, including the version of OpenPDF you are using, the Java version, and the operating
  system
- If possible, provide a minimal code example that reproduces the problem
- Describe the expected behavior and the actual behavior
- Describe how to reproduce the bug

## Become a maintainer

We are looking for maintainers to help us with the project. We need help in:

- Reviewing and accepting pull requests
- Answering questions in the discussions or issues
- Triaging issues which are important to fix

Interested in making OpenPDF better? Please contact @asturio (Claudio Clemens).

Further interesting article: [How to Contribute to Open Source](https://opensource.guide/how-to-contribute/)
