/*
 * Created on Oct 28, 2009
 * (c) 2009 Trumpet, Inc.
 *
 */
package com.lowagie.text.pdf.parser;

/**
 * Defines an interface for {@link RenderListener}s that can return text
 */
public interface TextProvidingRenderListener extends RenderListener {
    /**
     * Returns the result so far.
     * @return  a String with the resulting text.
     */
    public String getResultantText();
}
