package org.openpdf.layout.breaker;

import org.jspecify.annotations.Nullable;

import java.text.BreakIterator;
import java.text.CharacterIterator;

/**
 * BreakIterator implementation that improves line breaking for URLs. Break points are supported
 * before path fragments.
 */
public class UrlAwareLineBreakIterator extends BreakIterator {

    private static final String BREAKING_CHARS = ".,:;!?- \n\r\t/";

    private final BreakIterator delegate = BreakIterator.getLineInstance();
    @Nullable
    private String text;
    @Nullable
    private Range currentRange;

    public UrlAwareLineBreakIterator(String text) {
        setText(text);
    }

    @Override
    public int preceding(int offset) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int last() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int previous() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int next() {
        checkNotAheadOfDelegate();

        Range searchRange = currentRange; // the range in which we search for slashes

        if (isDelegateInSync()) {
            boolean reachedEnd = advanceDelegate();
            if (reachedEnd) {
                return BreakIterator.DONE;
            }

            if ("://".equals(substring(new Range(currentRange.getStop(), -1, 2)))) {
                searchRange = searchRange.withStart(currentRange.getStop() + 2);
                advanceDelegate(); // no reached-end check needed here, because there are at least two slashes ahead
            }
        }
        searchRange = searchRange.withStop(currentRange.getStop());

        searchRange = trimSearchRange(searchRange);
        int nextSlash = findSlashInRange(searchRange);
        currentRange = currentRange.withStart(nextSlash > -1 ? nextSlash : delegate.current());

        return currentRange.getStart();
    }

    private Range trimSearchRange(Range searchRange) {
        // Exclude leading breaking characters (should really only be a slash).
        while (searchRange.getStart() < currentRange.getStop() && BREAKING_CHARS.indexOf(text.charAt(searchRange.getStart())) > -1) {
            searchRange = searchRange.incrementStart();
        }

        // Exclude trailing breaking characters.
        while (searchRange.getStop() > searchRange.getStart() && BREAKING_CHARS.indexOf(text.charAt(searchRange.getStop() - 1)) > -1) {
            searchRange = searchRange.decrementStop();
        }

        return searchRange;
    }

    private int findSlashInRange(Range searchRange) {
        int nextSlash = text.indexOf('/', searchRange.getStart());
        return nextSlash < searchRange.getStop() ? nextSlash : -1;
    }

    private String substring(Range range) {
        return text.substring(Math.max(0, range.getStart()), Math.min(text.length(), range.getStop()));
    }

    private void checkNotAheadOfDelegate() {
        // This is a sanity check. We should never be in this state.
        if (currentRange.getStart() > delegate.current()) {
            throw new IllegalStateException("Iterator ahead of delegate.");
        }
    }

    private boolean isDelegateInSync() {
        return currentRange.getStart() == delegate.current();
    }

    private boolean advanceDelegate() {
        int next = delegate.next();
        currentRange = currentRange.withStop(next);
        return next == BreakIterator.DONE;
    }

    @Override
    public int next(int n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isBoundary(int offset) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int following(int offset) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int first() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setText(CharacterIterator newText) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int current() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public CharacterIterator getText() {
        return delegate.getText();
    }

    @Override
    public final void setText(String newText) {
        delegate.setText(newText);
        text = newText;
        currentRange = new Range(delegate.current(), delegate.current());
    }

    private static final class Range {
        private final int start;
        private final int stop;

        public Range(int start, int stop) {
            this.start = start;
            this.stop = Math.max(start, stop);
        }

        public Range(int referencePoint, int startOffset, int stopOffset) {
            this(referencePoint + startOffset, referencePoint + stopOffset);
        }

        public Range withStart(int start) {
            return new Range(start, stop);
        }

        public Range withStop(int stop) {
            return new Range(start, stop);
        }

        public Range incrementStart() {
            int newStart = start + 1;
            return new Range(newStart, Math.max(newStart, stop));
        }

        public Range decrementStop() {
            int newStop = stop - 1;
            return new Range(Math.min(start, newStop), newStop);
        }

        public int getStart() {
            return start;
        }

        public int getStop() {
            return stop;
        }

        @Override
        public String toString() {
            return "[" + start + ", " + stop + ")";
        }
    }
}
