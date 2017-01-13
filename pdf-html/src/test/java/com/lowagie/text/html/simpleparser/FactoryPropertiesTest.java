package com.lowagie.text.html.simpleparser;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Serhiy Yakovyn
 */
public class FactoryPropertiesTest {

    @Test
    public void shouldCreateRelativeLeadingForLineHeightNUmber() throws Exception {
        // given
        final Map<String, String> h = new HashMap<>();
        final String style = "line-height:1.4";
        h.put("style", style);
        final ChainedProperties cprops = new ChainedProperties();
        // when
        FactoryProperties.insertStyle(h, cprops);
        // then
        Assertions.assertThat(h.get("leading")).isEqualTo("0,1.4");
    }
}
