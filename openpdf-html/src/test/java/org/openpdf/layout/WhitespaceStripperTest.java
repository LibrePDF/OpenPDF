package org.openpdf.layout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class WhitespaceStripperTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "a",
            "a b",
            "a\nb",
            "a \nb",
            "a\n b",
            "a \n b",
            "a \t\r\n\f b",
            "a\n\nb",
            "a\n\n\nb",
            " \n\n b",
            "\n",
            "\n\n",
            " \n",
            "\n ",
            " \n ",
            "a  b \n c\nd \n\n e ",
            "a \n b",
            "a \u000B\n\u000B b"})
    void collapsesWhiteSpaceAroundLinefeedsLikeThePattern(String text) {
        assertThat(WhitespaceStripper.collapseAroundLinefeeds(text)).isEqualTo(withPattern(text));
    }

    @Test
    void collapsesRandomTextLikeThePattern() {
        char[] alphabet = {'a', ' ', ' ', '\n', '\t', '\r', '\f', '\u000B', ' '};
        Random random = new Random(1);
        for (int k = 0; k < 20_000; k++) {
            StringBuilder text = new StringBuilder();
            int length = random.nextInt(12);
            for (int i = 0; i < length; i++) {
                text.append(alphabet[random.nextInt(alphabet.length)]);
            }
            assertThat(WhitespaceStripper.collapseAroundLinefeeds(text.toString()))
                    .as("for <%s>", text.toString().replace("\n", "\\n"))
                    .isEqualTo(withPattern(text.toString()));
        }
    }

    @Test
    void collapsesLongRunsOfWhiteSpaceInLinearTime() {
        // the pattern needs minutes for each of these
        String spaces = " ".repeat(2_000_000);
        String runs = " a".repeat(1_000_000) + " \n ";
        String linefeedAtTheEnd = " ".repeat(2_000_000) + "\n";

        assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
            assertThat(WhitespaceStripper.collapseAroundLinefeeds(spaces)).isEqualTo(spaces);
            assertThat(WhitespaceStripper.collapseAroundLinefeeds(spaces + "\n" + spaces)).isEqualTo("\n");
            assertThat(WhitespaceStripper.collapseAroundLinefeeds(runs)).isEqualTo(" a".repeat(1_000_000) + "\n");
            assertThat(WhitespaceStripper.collapseAroundLinefeeds(linefeedAtTheEnd)).isEqualTo(linefeedAtTheEnd);
        });
    }

    @SuppressWarnings("deprecation")
    private static String withPattern(String text) {
        return WhitespaceStripper.linefeed_space_collapse.matcher(text).replaceAll(WhitespaceStripper.EOL);
    }
}
