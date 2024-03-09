package com.lowagie.text.pdf.encryption;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.exceptions.InvalidPdfException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * This class tests the OpenPDF decryption feature for AES256 encrypted files according to ISO 32000-2, i.e. for <code>R
 * = 6</code>.
 * <p>
 * See also <a href="https://github.com/LibrePDF/OpenPDF/issues/375">OpenPDF issue 375</a>
 *
 * @author mkl
 */
class DecryptAES256R6Test {

    static Field ownerPasswordUsedField;

    static boolean isOwnerPasswordUsed(PdfReader pdfReader) {
        try {
            return ownerPasswordUsedField.getBoolean(pdfReader);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        ownerPasswordUsedField = PdfReader.class.getDeclaredField("ownerPasswordUsed");
        ownerPasswordUsedField.setAccessible(true);
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * <a href="https://github.com/LibrePDF/OpenPDF/files/4700100/pwProtectedAES256_openPDFiss375.pdf">
     * pwProtectedAES256_openPDFiss375.pdf
     * </a>
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The empty user password is used.
     */
    @Test
    void testReadPwProtectedAES256_openPDFiss375() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/pwProtectedAES256_openPDFiss375.pdf")) {
            PdfReader pdfReader = new PdfReader(resource);
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("TEST", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * Demo1_encrypted_.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The empty user password is used.
     */
    @Test
    void testReadDemo1Encrypted() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/Demo1_encrypted_.pdf")) {
            PdfReader pdfReader = new PdfReader(resource);
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals(
                    "Demo   Name   Signature   Date  Elizabeth Schultz (Apr 24, 2018) Elizabeth Schultz Apr 24, 2018 "
                            + "Elizabeth Schultz Sue Northrop (Apr 24, 2018) Apr 24, 2018 Sue Northrop",
                    new PdfTextExtractor(pdfReader).getTextFromPage(1), "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * copied-positive-P.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The empty user password is used.
     */
    @Test
    void testReadCopiedPositiveP() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/copied-positive-P.pdf")) {
            PdfReader pdfReader = new PdfReader(resource);
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * c-r6-in-pw=owner4.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadCR6InPwOwner4() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/c-r6-in-pw=owner4.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "owner4".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * encrypted_hello_world_r6-pw=hôtel.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty user password is used.
     */
    @Test
    void testReadEncryptedHelloWorldR6PwHotelUtf8() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/encrypted_hello_world_r6-pw=hôtel.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "hôtel".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Hello, world!\n Goodbye, world!",
                    new PdfTextExtractor(pdfReader).getTextFromPage(1), "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * encrypted-positive-P.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The empty user password is used.
     */
    @Test
    void testReadEncryptedPositiveP() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/encrypted-positive-P.pdf")) {
            PdfReader pdfReader = new PdfReader(resource);
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-long-password=qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcv.pdf
     * provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty identical owner and user password is used.
     */
    @Test
    void testReadEncXiLongPassword() throws IOException {
        try (
                InputStream resource = getClass().getResourceAsStream(
                        "/issue375/enc-XI-long-password"
                                +
                                "=qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcv.pdf")
        ) {
            PdfReader pdfReader = new PdfReader(resource,
                    "qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcv".getBytes(
                            UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,O=master.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The empty user password is used.
     */
    @Test
    void testReadEncXiR6V5OMaster_User() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/enc-XI-R6,V5,O=master.pdf")) {
            PdfReader pdfReader = new PdfReader(resource);
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,O=master.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadEncXiR6V5OMaster_Owner() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/enc-XI-R6,V5,O=master.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "master".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,U=attachment,encrypted-attachments.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * TODO: OpenPdf currently only supports all-or-nothing encryption
     * (except Metadata and signatures) but in this test file only the
     * embedded file is encrypted.
     */
    @Test
    void testReadEncXiR6V5UAttachmentEncryptedAttachments() {
        Assertions.assertThrows(InvalidPdfException.class, () -> {
            try (InputStream resource = getClass().getResourceAsStream(
                    "/issue375/enc-XI-R6,V5,U=attachment,encrypted-attachments.pdf")) {
                PdfReader pdfReader = new PdfReader(resource, "attachment".getBytes(UTF_8));
                Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
                Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                        "PdfReader fails to report the correct number of pages");
                Assertions.assertEquals("TEST", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                        "Wrong text extracted from page 1");
                pdfReader.close();
            }
        });
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,U=view,attachments,cleartext-metadata.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty identical owner and user password is used.
     */
    @Test
    void testReadEncXiR6V5UViewAttachmentsCleartextMetadata() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream(
                "/issue375/enc-XI-R6,V5,U=view,attachments,cleartext-metadata.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "view".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,U=view,O=master.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty user password is used.
     */
    @Test
    void testReadEncXiR6V5UViewOMaster_User() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/enc-XI-R6,V5,U=view,O=master.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "view".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,U=view,O=master.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty user password is used.
     */
    @Test
    void testReadEncXiR6V5UViewOMaster_Owner() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/enc-XI-R6,V5,U=view,O=master.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "master".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,U=wwwww,O=wwwww.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty identical owner and user password is used.
     */
    @Test
    void testReadEncXiR6V5UWwwwwOWwwww() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/enc-XI-R6,V5,U=wwwww,O=wwwww.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "wwwww".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * graph-encrypted-pw=user.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty user password is used.
     */
    @Test
    void testReadGraphEncryptedPwUser() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/graph-encrypted-pw=user.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "user".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * issue6010_1-pw=owner.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadIssue60101PwOwner() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/issue6010_1-pw=owner.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "owner".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Issue 6010", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * issue6010_2-pw=æøå.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadIssue60102PaeoeaaUtf8() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/issue6010_2-pw=æøå.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "æøå".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(10, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Sample PDF Document\n"
                            + " Robert Maron\n"
                            + " Grzegorz Grudzi´ nski\n"
                            + " February 20, 1999", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * MuPDF-AES256-R6-u=user-o=owner.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty user password is used.
     */
    @Test
    void testReadMuPDFAes256R6UUserOOwner_User() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/MuPDF-AES256-R6-u=user-o=owner.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "user".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Mu PD F  a lightweight PDF and XPS viewer",
                    new PdfTextExtractor(pdfReader).getTextFromPage(1), "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * MuPDF-AES256-R6-u=user-o=owner.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadMuPDFAes256R6UUserOOwner_Owner() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/MuPDF-AES256-R6-u=user-o=owner.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "owner".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Mu PD F  a lightweight PDF and XPS viewer",
                    new PdfTextExtractor(pdfReader).getTextFromPage(1), "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * nontrivial-crypt-filter.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * TODO: OpenPdf currently only supports all-or-nothing encryption
     * (except Metadata and signatures) but in this test file only the
     * embedded file is encrypted.
     */
    @Test
    void testReadNonTrivialCryptFilter() {
        Assertions.assertThrows(BadPasswordException.class, () -> {
            try (InputStream resource = getClass().getResourceAsStream("/issue375/nontrivial-crypt-filter.pdf")) {
                PdfReader pdfReader = new PdfReader(resource);
                Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
                Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                        "PdfReader fails to report the correct number of pages");
                Assertions.assertEquals("TEST", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                        "Wrong text extracted from page 1");
                pdfReader.close();
            }
        });
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * pr6531_1-pw=asdfasdf.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadPr65311PwAsdfasdf() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/pr6531_1-pw=asdfasdf.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "asdfasdf".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * pr6531_2-pw=asdfasdf.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty user password is used.
     */
    @Test
    void testReadPr65312PwAsdfasdf() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/pr6531_2-pw=asdfasdf.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "asdfasdf".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            // Page has no static content, so no text extraction test
            pdfReader.close();
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption type R = 6" support AES256
     * </a>
     * <br>
     * unfilterable-with-crypt.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * TODO: OpenPdf currently only supports all-or-nothing encryption
     * (except Metadata and signatures) but in this test file only
     * certain streams with Crypt filters are encrypted.
     */
    @Test
    void testReadUnfilterableWithCrypt() {
        Assertions.assertThrows(BadPasswordException.class, () -> {
            try (InputStream resource = getClass().getResourceAsStream("/issue375/unfilterable-with-crypt.pdf")) {
                PdfReader pdfReader = new PdfReader(resource);
                Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
                Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                        "PdfReader fails to report the correct number of pages");
                Assertions.assertEquals("TEST", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                        "Wrong text extracted from page 1");
                pdfReader.close();
            }
        });
    }

    /**
     * <a
     * href="https://stackoverflow.com/questions/68760143/how-to-remove-password-in-password-protected-pdf-using-itext-7">
     * How to remove password in password-protected pdf using iText 7
     * </a>
     * <br>
     * <a href="https://drive.google.com/drive/folders/16yWf46KquogkRH_mHf9atTLSHc6z5ITn?usp=sharing">
     * THISISATEST_PWP.pdf
     * </a>
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadTHISISATEST_PWP() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/THISISATEST_PWP.pdf")) {
            PdfReader pdfReader = new PdfReader(resource, "password".getBytes(UTF_8));
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(2, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertTrue(new PdfTextExtractor(pdfReader).getTextFromPage(1).startsWith("THIS IS A TEST"),
                    "Wrong text extracted from page 1");
            pdfReader.close();
        }
    }
}
