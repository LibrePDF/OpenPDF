/*
 * UserAgentCallback.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.openpdf.extend;

import org.jspecify.annotations.Nullable;
import org.openpdf.resource.CSSResource;
import org.openpdf.resource.ImageResource;
import org.openpdf.resource.XMLResource;

/**
 * <p>To be implemented by any user agent using the panel. "User agent" is a
 * term defined by the W3C in the documentation for XHTML and CSS; in most
 * cases, you can think of this as the rendering component for a browser.</p>
 * <p>
 *
 * <p>This interface defines a simple callback mechanism for Flying Saucer to
 * interact with a user agent. The FS toolkit provides a default implementation
 * for this interface which in most cases you can leave as is. You can provide your
 * own UserAgentCallback when constructing an {@link org.openpdf.simple.XHTMLPanel}
 * or {@link org.openpdf.swing.BasicPanel}.</p>
 *
 * <p>The user agent in this case is responsible for retrieving external resources. For
 * privacy reasons, if using the library in an application that can access URIs
 * in an unrestricted fashion, you may decide to restrict access to XML, CSS or images
 * retrieved from external sources; that's one of the purposes of the UAC.</p>
 *
 * <p>To understand how to create your own UAC, it's best to look at some of the
 * implementations shipped with the library, like the {@link org.openpdf.swing.NaiveUserAgent}.
 * </p>
 *
 * @author Torbjoern Gannholm
 */
public interface UserAgentCallback {
    /**
     * Retrieves the CSS at the given URI. This is a synchronous call.
     *
     * @param uri Location of the CSS
     * @return A CSSResource for the content at the URI.
     */
    CSSResource getCSSResource(String uri);

    /**
     * Retrieves the Image at the given URI. This is a synchronous call.
     *
     * @param uri Location of the image
     * @return An ImageResource for the content at the URI.
     */
    ImageResource getImageResource(String uri);

    /**
     * Retrieves the XML at the given URI. This is a synchronous call.
     *
     * @param uri Location of the XML
     * @return A XMLResource for the content at the URI.
     */
    @Nullable
    XMLResource getXMLResource(String uri);

    /**
     * Retrieves a binary resource located at a given URI and returns its contents
     * as a byte array or {@code null} if the resource could not be loaded.
     */
    byte @Nullable [] getBinaryResource(String uri);

    /**
     * Normally, returns true if the user agent has visited this URI. UserAgent should consider
     * if it should answer truthfully or not for privacy reasons.
     *
     * @param uri A URI which may have been visited by this user agent.
     * @return The visited value
     */
    boolean isVisited(@Nullable String uri);

    /**
     * Does not need to be a correct URL, only an identifier that the
     * implementation can resolve.
     *
     * @param url A URL against which relative references can be resolved.
     */
    void setBaseURL(@Nullable String url);

    /**
     * @return the base uri, possibly in the implementations private uri-space
     */
    @Nullable
    String getBaseURL();

    /**
     * Used to find an uri that may be relative to the BaseURL.
     * The returned value will always only be used via methods in the same
     * implementation of this interface, therefore may be a private uri-space.
     *
     * @param uri an absolute or relative (to baseURL) uri to be resolved.
     * @return the full uri in uri-spaces known to the current implementation.
     */
    @Nullable
    String resolveURI(@Nullable String uri);
}

