# Contributing to OpenPDF

## How to contribute with Code

1. Create a GitHub account
2. Fork the project on GitHub
3. Make any improvements you want in your fork (in any branch)
4. Create a pull request

### Building the project

1. Clone the repository (or your fork)
2. Run `mvn clean install -Dgpg.skip` in the root directory

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

### How to contribute with Documentation

- Feel free to add content to the [Wiki](https://github.com/LibrePDF/OpenPDF/wiki)
- Also participate in the [Discussions](https://github.com/LibrePDF/OpenPDF/discussions), answering and asking questions
- You may also contribute with translations in `src/main/java/com/lowagie/text/error_messages`

### How to contribute with Bug Reports

- Please be as detailed as possible, including the version of OpenPDF you are using, the Java version, and the operating system
- If possible, provide a minimal code example that reproduces the problem
- Describe the expected behavior and the actual behavior
- Describe how to reproduce the bug
