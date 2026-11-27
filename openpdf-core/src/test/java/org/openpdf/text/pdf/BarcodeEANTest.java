package org.openpdf.text.pdf;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BarcodeEANTest {

    @ParameterizedTest
    @CsvSource({
            "EAN13, 4006381333931",
            "EAN8, 96385074",
            "UPCA, 036000291452",
            "UPCE, 04252614",
            "UPCE, 14252611",
            "SUPP2, 12",
            "SUPP5, 12345",
    })
    void validCode(String type, String code) {
        assertThat(barcode(type, code).createAwtImage(BLACK, WHITE)).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({
            "EAN13, 40063813339310, 13",
            "EAN13, 400638133393, 13",
            "EAN13, 400638133393A, 13",
            "EAN8, 963850745, 8",
            "EAN8, 9638507, 8",
            "UPCA, 0360002914520, 12",
            "UPCA, 03600029145, 12",
            "UPCE, 042526140, 8",
            "UPCE, 0425261, 8",
            "SUPP2, 123, 2",
            "SUPP2, 1, 2",
            "SUPP5, 123456, 5",
            "SUPP5, 1234, 5",
            "SUPP5, 1234X, 5",
    })
    void rejectsCodeWithWrongNumberOfDigits(String type, String code, int digits) {
        Barcode barcode = barcode(type, code);

        assertThatThrownBy(() -> barcode.createAwtImage(BLACK, WHITE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The %s barcode must contain exactly %s digits, instead of '%s'.", type, digits, code);
    }

    @Test
    void rejectsLettersInsteadOfSilentlyEncodingAnotherValue() {
        assertThatThrownBy(() -> BarcodeEAN.getBarsEAN13("4006381333 31"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The EAN13 barcode must contain exactly 13 digits, instead of '4006381333 31'.");
    }

    @Test
    void rejectsUpceNumberSystemOtherThan0Or1() {
        // before the fix, "24252611" silently produced the same barcode as "14252611"
        assertThatThrownBy(() -> BarcodeEAN.getBarsUPCE("24252611"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The UPC-E number system (first digit) must be 0 or 1, instead of '24252611'.");
    }

    @Test
    void validatesCodeWhenPlacingBarcodeOnPage() {
        Barcode barcode = barcode("EAN13", "40063813339310");
        PdfContentByte cb = new PdfContentByte(null);

        assertThatThrownBy(() -> barcode.placeBarcode(cb, BLACK, BLACK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The EAN13 barcode must contain exactly 13 digits, instead of '40063813339310'.");
    }

    private static Barcode barcode(String type, String code) {
        BarcodeEAN barcode = new BarcodeEAN();
        barcode.setCodeType(switch (type) {
            case "EAN13" -> Barcode.EAN13;
            case "EAN8" -> Barcode.EAN8;
            case "UPCA" -> Barcode.UPCA;
            case "UPCE" -> Barcode.UPCE;
            case "SUPP2" -> Barcode.SUPP2;
            case "SUPP5" -> Barcode.SUPP5;
            default -> throw new IllegalArgumentException(type);
        });
        barcode.setCode(code);
        return barcode;
    }
}
