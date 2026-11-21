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
        numberList.add("num");
        ListItem numberItem = (ListItem) numberList.getItems().get(0);
        assertThat(numberItem.getListSymbol().getContent()).isEqualTo((char) 172 + " ");
    }
}
