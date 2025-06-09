package org.openpdf.css.newmatch;

import org.junit.jupiter.api.Test;
import org.openpdf.css.newmatch.Condition.LangCondition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openpdf.css.newmatch.Condition.createLangCondition;

class LangConditionTest {
    private final LangCondition condition = (LangCondition) createLangCondition("et");

    @Test
    void langAttributeEqualsExpectedLanguage() {
        assertThat(condition.matches("et")).isTrue();
    }

    @Test
    void countryPartIsIgnored() {
        assertThat(condition.matches("et-EE")).isTrue();
        assertThat(condition.matches("et-FI")).isTrue();
    }

    @Test
    void langIsCaseInsensitive() {
        assertThat(condition.matches("ET-EE")).isTrue();
        assertThat(condition.matches("ET-EE")).isTrue();
    }

    @Test
    void langAttributeNotEqualToExpectedLanguage() {
        assertThat(condition.matches("en")).isFalse();
        assertThat(condition.matches("en-ET")).isFalse();
    }
}
