package com.lowagie.text.pdf.encryption;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.lowagie.text.pdf.PdfEncryption;
import com.lowagie.text.pdf.PdfLiteral;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

public class PdfEncryptionTest {

    @Test
    public void getFileIdChangingPartTest() {
        // HEX encoded, valid format
        assertArrayEquals(Hex.decode("c547234acb212b7cd65ba961f9e9acae"),
                PdfEncryption.getFileIdChangingPart(
                        new PdfLiteral("<c547234acb212b7cd65ba961f9e9acae><c547234acb212b7cd65ba961f9e9acae>")));
        assertArrayEquals(Hex.decode("1527b6788ff5fb71d8c0bd6642fcbe47"),
                PdfEncryption.getFileIdChangingPart(
                        new PdfLiteral("<c547234acb212b7cd65ba961f9e9acae><1527b6788ff5fb71d8c0bd6642fcbe47>")));
        assertArrayEquals(Hex.decode("c547234acb212b7cd65ba961f9e9acae"),
                PdfEncryption.getFileIdChangingPart(
                        new PdfLiteral("[<c547234acb212b7cd65ba961f9e9acae><c547234acb212b7cd65ba961f9e9acae>]")));
        assertArrayEquals(Hex.decode("1527b6788ff5fb71d8c0bd6642fcbe47"),
                PdfEncryption.getFileIdChangingPart(
                        new PdfLiteral("[<c547234acb212b7cd65ba961f9e9acae><1527b6788ff5fb71d8c0bd6642fcbe47>]")));

        // not-HEX encoded, valid format
        assertArrayEquals("abc".getBytes(),
                PdfEncryption.getFileIdChangingPart(new PdfLiteral("<abc><abc>")));
        assertArrayEquals("def".getBytes(),
                PdfEncryption.getFileIdChangingPart(new PdfLiteral("<abc><def>")));

        // invalid format
        assertArrayEquals("c547234acb212b7cd65ba961f9e9acae".getBytes(),
                PdfEncryption.getFileIdChangingPart(new PdfLiteral("c547234acb212b7cd65ba961f9e9acae")));
        assertArrayEquals("<c547234acb212b7cd65ba961f9e9acae>1527b6788ff5fb71d8c0bd6642fcbe47".getBytes(),
                PdfEncryption.getFileIdChangingPart(
                        new PdfLiteral("<c547234acb212b7cd65ba961f9e9acae>1527b6788ff5fb71d8c0bd6642fcbe47")));
        assertArrayEquals("c547234acb212b7cd65ba961f9e9acae".getBytes(),
                PdfEncryption.getFileIdChangingPart(new PdfLiteral("c547234acb212b7cd65ba961f9e9acae")));
    }

}
