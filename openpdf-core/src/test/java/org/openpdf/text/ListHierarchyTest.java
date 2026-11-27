package org.openpdf.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ListHierarchyTest {

    @Test
    void numberedAndLetteredListConfiguresItemsAndNestedLists() {
        List parent = new List(List.ORDERED, List.ALPHABETICAL, 15f);
        parent.setLowercase(true);
        parent.add("first");

        List nested = new List(List.ORDERED, 10f);
        nested.add("nested-item");
        parent.add((Element) nested);

        assertThat(parent.isLowercase()).isTrue();
        assertThat(parent.getItems()).hasSize(2);
        ListItem firstItem = (ListItem) parent.getItems().get(0);
        assertThat(firstItem.getListSymbol().getContent()).isEqualTo("a. ");
        assertThat(nested.getIndentationLeft()).isEqualTo(15f);
    }

    @Test
    void specializedListsConfigureSymbolsAndItemsCorrectly() {
        GreekList greekList = new GreekList(true, 12);
        greekList.add("alpha");
        ListItem greekItem = (ListItem) greekList.getItems().get(0);
        assertThat(greekItem.getListSymbol().getFont().getBaseFont().getPostscriptFontName())
                .isEqualTo(FontFactory.SYMBOL);
        assertThat(greekItem.getListSymbol().getContent()).isEqualTo("a. ");

        RomanList romanList = new RomanList(true, 12);
        romanList.add("one");
        ListItem romanItem = (ListItem) romanList.getItems().get(0);
        assertThat(romanItem.getListSymbol().getContent()).isEqualTo("i. ");

        ZapfDingbatsList dingbatsList = new ZapfDingbatsList(42, 10);
        dingbatsList.add("star");
        ListItem dingbatsItem = (ListItem) dingbatsList.getItems().get(0);
        assertThat(dingbatsItem.getListSymbol().getContent()).isEqualTo("* ");

        ZapfDingbatsNumberList numberList = new ZapfDingbatsNumberList(0, 10);
        assertThat(numberList.add("num")).isTrue();
        ListItem numberItem = (ListItem) numberList.getItems().get(0);
        assertThat(numberItem.getListSymbol().getContent()).isEqualTo((char) 172 + " ");
    }

    @Test
    void jpegParsesApp0DpiAppeAndFrameDimensions() throws Exception {
        byte[] minimalJpeg = new byte[] {
                (byte) 0xFF, (byte) 0xD8, // SOI
                // APP0 JFIF (length 16, units=1 (inch), dpiX=300 (0x012C), dpiY=150 (0x0096))
                (byte) 0xFF, (byte) 0xE0, 0x00, 0x10,
                'J', 'F', 'I', 'F', 0x00, 0x01, 0x01, 0x01, 0x01, 0x2C, 0x00, (byte) 0x96, 0x00, 0x00,
                // APPE Adobe marker (length 14 = 2 + 12 bytes)
                (byte) 0xFF, (byte) 0xEE, 0x00, 0x0E,
                'A', 'd', 'o', 'b', 'e', 0, 0, 0, 0, 0, 0, 0,
                // SOF0 valid frame marker (length 11, 8 bpc, height=24, width=48, components=3)
                (byte) 0xFF, (byte) 0xC0, 0x00, 0x0B, 0x08, 0x00, 0x18, 0x00, 0x30, 0x03, 0x01, 0x11, 0x00
        };

        Jpeg jpeg = new Jpeg(minimalJpeg);

        assertThat(jpeg.getDpiX()).isEqualTo(300);
        assertThat(jpeg.getDpiY()).isEqualTo(150);
        assertThat(jpeg.isInverted()).isTrue();
        assertThat(jpeg.getScaledWidth()).isEqualTo(48f);
        assertThat(jpeg.getScaledHeight()).isEqualTo(24f);
    }
}
