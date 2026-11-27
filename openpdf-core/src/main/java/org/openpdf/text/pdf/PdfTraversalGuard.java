package org.openpdf.text.pdf;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Keeps a walk through a structure of a PDF document finite. Structures such as the field hierarchy, the outlines
 * or a name tree are trees or chains in a valid document, but a damaged or hostile document can link a node back
 * to one that was visited before, or nest nodes very deeply.
 * <p>
 * A walker asks the guard before it follows a link: {@link #visit(PdfObject)} tells whether a node is seen for the
 * first time, and {@link #enter()} and {@link #leave()} count how deep the walk is. One guard is used for one walk.
 */
final class PdfTraversalGuard {

    /**
     * The deepest nesting a walk follows. The structures this guard is used for are a few levels deep in practice.
     */
    static final int MAX_DEPTH = 256;

    private final Set<Integer> visitedNumbers = new HashSet<>();
    private final Set<PdfObject> visitedObjects = Collections.newSetFromMap(new IdentityHashMap<>());
    private int depth;

    /**
     * Registers a node that the walk is about to process. An indirect reference is recognised by its object
     * number, any other object by its identity.
     *
     * @param node the node, or the reference to it; may be {@code null}
     * @return {@code true} if the node was not registered before, {@code false} if it was or if it is {@code null}
     */
    boolean visit(PdfObject node) {
        if (node == null) {
            return false;
        }
        if (node instanceof PdfIndirectReference) {
            return visitedNumbers.add(((PdfIndirectReference) node).getNumber());
        }
        return visitedObjects.add(node);
    }

    /**
     * Goes one level deeper. Every call that returns {@code true} must be followed by a call to {@link #leave()}.
     *
     * @return {@code true} if the walk may go deeper, {@code false} if it reached {@link #MAX_DEPTH}
     */
    boolean enter() {
        if (depth >= MAX_DEPTH) {
            return false;
        }
        ++depth;
        return true;
    }

    /**
     * Registers a node and goes one level deeper to process it. Every call that returns {@code true} must be
     * followed by a call to {@link #leave()}.
     *
     * @param node the node, or the reference to it; may be {@code null}
     * @return {@code true} if the node may be processed, {@code false} if it was registered before, is
     *         {@code null}, or lies too deep
     */
    boolean enter(PdfObject node) {
        return visit(node) && enter();
    }

    /**
     * Goes back up one level.
     */
    void leave() {
        --depth;
    }
}
