package org.openpdf.layout.breaker;

import org.junit.jupiter.api.Test;

import java.text.BreakIterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class UrlAwareLineBreakIteratorTest {

    @Test
    public void breakAtSpace() {
        assertBreaksCorrectly("Hello World! World foo",
                new String[] {"Hello ", "World! ", "World ", "foo"});
    }

    @Test
    public void breakAtPunctuation() {
        assertBreaksCorrectly("The.quick,brown:fox;jumps!over?the(lazy)[dog]",
                new String[] {"The.", "quick,", "brown:", "fox;", "jumps!", "over?", "the", "(lazy)", "[dog]"});
    }

    @Test
    public void breakAtHyphen() {
        assertBreaksCorrectly("Pseudo-element",
                new String[] {"Pseudo-", "element"});
    }

    @Test
    public void breakAtSlash() {
        assertBreaksCorrectly("Justice/Law",
                new String[] {"Justice", "/Law"});
    }

    @Test
    public void wordBeginsWithSlash() {
        assertBreaksCorrectly("Justice /Law",
                new String[] {"Justice ", "/Law"});
    }

    @Test
    public void wordEndsWithSlash() {
        assertBreaksCorrectly("Justice/ Law",
                new String[] {"Justice/ ", "Law"});
    }

    @Test
    public void wordEndsWithSlashMultipleWhitespace() {
        assertBreaksCorrectly("Justice/    Law",
                new String[] {"Justice/    ", "Law"});
    }

    @Test
    public void slashSeparatedSequence() {
        assertBreaksCorrectly("/this/is/a/long/path/name/",
                new String[] {"/this", "/is", "/a", "/long", "/path", "/name/"});
    }

    @Test
    public void urlInside() {
        assertBreaksCorrectly("Sentence with url https://github.com/flyingsaucerproject/flyingsaucer?test=true&param2=false inside.",
                new String[] {"Sentence ", "with ", "url ", "https://github.", "com", "/flyingsaucerproject", "/flyingsaucer?",
                        "test=true&param2=false ", "inside."});
    }

    @Test
    public void multipleSlashesInWord() {
        assertBreaksCorrectly("word/////word",
                new String[] {"word", "/////word"});
    }

    @Test
    public void multipleSlashesBeforeWord() {
        assertBreaksCorrectly("hello /////world",
                new String[] {"hello ", "/////world"});
    }

    @Test
    public void multipleSlashesAfterWord() {
        assertBreaksCorrectly("hello world/////",
                new String[] {"hello ", "world/////"});
    }

    @Test
    public void multipleSlashesAroundWord() {
        assertBreaksCorrectly("hello /////world/////",
                new String[] {"hello ", "/////world/////"});
    }

    @Test
    public void whitespaceAfterTrailingSlashes() {
        assertBreaksCorrectly("hello world///    ",
                new String[] {"hello ", "world///    "});
    }

    @Test
    public void shortUrl() {
        assertBreaksCorrectly("http://localhost",
                new String[] {"http://localhost"});
    }

    @Test
    public void incompleteUrl() {
        assertBreaksCorrectly("http://", 
                new String[] {"http://"});
    }


    private void assertBreaksCorrectly(String input, String[] segments) {
        BreakIterator iterator = new UrlAwareLineBreakIterator(input);

        int segmentIndex = 0;
        int lastBreakPoint = 0;
        int breakpoint;
        while ((breakpoint = iterator.next()) != BreakIterator.DONE) {
            if (segmentIndex < segments.length) {
                String segment = segments[segmentIndex++];
                assertThat(input.substring(lastBreakPoint, breakpoint))
                        .as("Segment #" + segmentIndex + " does not match.")
                        .isEqualTo(segment);
                lastBreakPoint = breakpoint;
            } else {
                fail("Too few segments.");
            }
        }
        assertThat(lastBreakPoint).as("Last breakpoint is wrong.").isEqualTo(input.length());
        if (segmentIndex != segments.length) {
            fail("Too many segments.");
        }
    }

}