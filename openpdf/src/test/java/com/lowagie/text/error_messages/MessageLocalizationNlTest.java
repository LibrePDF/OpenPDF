package com.lowagie.text.error_messages;

import java.io.IOException;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class MessageLocalizationNlTest {

    private static Stream<String> messageKeyProvider() {
        return MessageLocalization.getAllKeys().stream();
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        MessageLocalization.setLanguage("nl", null);
    }

    @AfterAll
    static void afterAll() throws IOException {
        MessageLocalization.setLanguage("en", null);
    }

    @ParameterizedTest
    @MethodSource("messageKeyProvider")
    void messageHasAllParameterSubstituted_nl(String key) throws IOException {
        final String message =
                MessageLocalization.getComposedMessage(key, "P1", "P2", "P3", "P4");
        Assertions.assertThat(message)
                .as("Message for key: {}", key)
                .doesNotContain("{1}", "{2}", "{3}", "{4}", "No message found for");
    }
}
