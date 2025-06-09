package org.openpdf.css.newmatch;

import org.junit.jupiter.api.Test;
import org.openpdf.css.newmatch.Condition.ClassCondition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openpdf.css.newmatch.Condition.createClassCondition;

class ClassConditionTest {
    private final ClassCondition condition = (ClassCondition) createClassCondition("active");

    @Test
    void classAttributeEqualsExpectedClassName() {
        assertThat(condition.containsClassName("active")).isTrue();
        assertThat(condition.containsClassName("activex")).isFalse();
    }

    @Test
    void classAttributeContainsExpectedClassName() {
        assertThat(condition.containsClassName("foo active bar")).isTrue();
        assertThat(condition.containsClassName("foo active ")).isTrue();
        assertThat(condition.containsClassName(" active bar")).isTrue();
        assertThat(condition.containsClassName(" active ")).isTrue();
    }

    @Test
    void classAttributeStartsWithExpectedClassName() {
        assertThat(condition.containsClassName("active foo bar")).isTrue();
        assertThat(condition.containsClassName(" active foo bar")).isTrue();
        assertThat(condition.containsClassName("activex foo bar")).isFalse();
        assertThat(condition.containsClassName("inactive foo bar")).isFalse();
    }

    @Test
    void classAttributeEndsWithExpectedClassName() {
        assertThat(condition.containsClassName("foo bar active")).isTrue();
        assertThat(condition.containsClassName("foo bar active ")).isTrue();
        assertThat(condition.containsClassName("foo bar inactive")).isFalse();
        assertThat(condition.containsClassName("foo bar activeactive")).isFalse();
        assertThat(condition.containsClassName("foo bar active-active")).isFalse();
        assertThat(condition.containsClassName("foo bar active_active")).isFalse();
        assertThat(condition.containsClassName("foo bar activex")).isFalse();
    }

    @Test
    void classAttributeContainsSimilarClassNames() {
        assertThat(condition.containsClassName("activex active inactive")).isTrue();
        assertThat(condition.containsClassName("activex _active inactive")).isFalse();
    }

    @Test
    void classNotMatches() {
        assertThat(condition.containsClassName("")).isFalse();
        assertThat(condition.containsClassName("_active")).isFalse();
        assertThat(condition.containsClassName("active_")).isFalse();
        assertThat(condition.containsClassName("act ive")).isFalse();
    }
}
