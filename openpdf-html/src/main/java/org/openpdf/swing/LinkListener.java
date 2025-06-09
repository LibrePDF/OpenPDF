/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 openpdf.dev.java.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.swing;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.openpdf.render.Box;


/**
 * A LinkListener is used to respond to a user clicking Box elements in a {@link org.openpdf.swing.BasicPanel},
 * in particular to handle anchors and navigation. When a Box is clicked, if it has an anchor associated with it, the
 * panel will be requested to navigate to that URI.
 */
public class LinkListener extends DefaultFSMouseListener {
    /**
     * Utility method to request the panel navigate to the given URI.
     *
     * @param panel the panel on which the listener is attached
     * @param uri the URI to navigate to
     */
    public void linkClicked(BasicPanel panel, String uri) {
        panel.setDocumentRelative(uri);
    }

    /**
     * Triggers the click on a box. If the Box's element has an associated URI (e.g. is an anchor), notifies the
     * panel to navigate to that URI.
     *
     * @param panel the panel where the mouse button has been released.
     * @param box the box on which the mouse cursor is currently located
     */
    @Override
    public void onMouseUp(BasicPanel panel, Box box) {
        checkForLink(panel, box);
    }

    // tests whether the element associated with the Box has an associated URI (e.g. is an anchor), and if so, calls
    // back to the panel to navigate to that URI
    private void checkForLink(BasicPanel panel, Box box) {
        if (box == null || box.getElement() == null) {
            return;
        }

        String uri = findLink(panel, box.getElement());

        if (uri != null) {
            linkClicked(panel, uri);
        }
    }

    // looks to see if the given element has a link URI associated with it; if so, returns the URI as a string, if
    // not, returns null
    private String findLink(BasicPanel panel, Element e) {
        String uri = null;

        for (Node node = e; node.getNodeType() == Node.ELEMENT_NODE; node = node.getParentNode()) {
            uri = panel.getSharedContext().getNamespaceHandler().getLinkUri((Element) node);

            if (uri != null) {
                break;
            }
        }

        return uri;
    }
}

