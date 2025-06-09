/*
 * {{{ header & license
 * Copyright (c) 2016 Stanimir Stamenkov
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
package org.openpdf.pdf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.openpdf.pdf.ITextOutputDevice.Bookmark;
import org.openpdf.render.Box;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

class HTMLOutline {

    private static final Pattern HEADING = Pattern.compile("h(\\d+)", CASE_INSENSITIVE);

    /** <a href="https://www.w3.org/TR/html51/sections.html#sectioning-roots">sectioning roots</a> */
    private static final Pattern ROOT = Pattern.compile("blockquote|details|fieldset|figure|td", CASE_INSENSITIVE);

    private static final Pattern WS = Pattern.compile("\\s+");

    private static final int MAX_NAME_LENGTH = 200;

    private final HTMLOutline parent;
    private final int level;
    private final Bookmark bookmark;

    private HTMLOutline() {
        this(0, "root", null);
    }

    private HTMLOutline(int level, String name, HTMLOutline parent) {
        this.level = level;
        this.bookmark = new Bookmark(name, "");
        this.parent = parent;
        if (parent != null) {
            parent.bookmark.addChild(bookmark);
        }
    }

    /**
     * Creates a bookmark list of the document outline generated for the given
     * element context (usually the root document element).
     * <p>
     * The current algorithm is more simple than the one suggested in the HTML5
     * specification such as it is not affected by
     * <a href="https://www.w3.org/TR/html51/dom.html#sectioning-content">sectioning
     * content</a> but just the heading level.  For
     * <a href="https://www.w3.org/TR/html51/sections.html#example-d42b7aaf">example</a>:</p>
     * <pre>
     * &lt;body>
     *   &lt;h1>Foo&lt;/h1>
     *   &lt;h3>Bar&lt;/h3>
     *   &lt;blockquote>
     *     &lt;h5>Bla&lt;/h5>
     *   &lt;/blockquote>
     *   &lt;p>Baz&lt;/p>
     *   &lt;h2>Quux&lt;/h2>
     *   &lt;section>
     *     &lt;h3>Thud&lt;/h3>
     *   &lt;/section>
     *   &lt;h4>Grunt&lt;/h4>
     * &lt;/body></pre>
     * <p>
     * Should generate outline as:</p>
     * <ol>
     * <li>Foo
     *   <ol>
     *   <li>Bar</li>
     *   <li>Quux</li>
     *   <li>Thud</li>
     *   <li>Grunt</li>
     *   </ol></li>
     * </ol>
     * <p>
     * But it generates outline as:</p>
     * <ol>
     * <li>Foo
     *   <ol>
     *   <li>Bar</li>
     *   <li>Quux
     *     <ol>
     *     <li>Thud
     *       <ol>
     *       <li>Grunt</li>
     *       </ol></li>
     *     </ol></li>
     *   </ol></li>
     * </ol>
     *
     * <h4>Example document customizations</h4>
     *
     * <h5>Include non-heading element as bookmark (level 4)</h5>
     * <pre>
     * &lt;strong data-pdf-bookmark="4">Foo bar&lt;/strong></pre>
     *
     * <h5>Specify bookmark name</h5>
     * <pre>
     * &lt;tr data-pdf-bookmark="5" data-pdf-bookmark-name="Bar baz">...&lt;/tr></pre>
     *
     * <h5>Exclude individual heading from bookmarks</h5>
     * <pre>
     * &lt;h3 data-pdf-bookmark="none">Baz qux&lt;/h3></pre>
     *
     * <h5>Prevent automatic bookmarks for the whole of the document</h5>
     * <pre>
     * &lt;html data-pdf-bookmark="exclude">...&lt;/html></pre>
     *
     * @param   context  the top element a sectioning outline would be generated for;
     * @param   box  box hierarchy the outline bookmarks would get mapped into.
     * @return  Bookmarks of the outline generated for the given element context.
     * @see     <a href="https://www.w3.org/TR/html51/sections.html#creating-an-outline">Creating an outline</a>
     */
    public static List<Bookmark> generate(Element context, Box box) {
        NodeIterator iterator = NestedSectioningFilter.iterator(context);

        if (iterator == null) {
            return Collections.emptyList();
        }

        HTMLOutline root = new HTMLOutline();
        HTMLOutline current = root;
        Map<Element, Bookmark> map = new IdentityHashMap<>();

        for (Element element = (Element) iterator.nextNode();
                element != null; element = (Element) iterator.nextNode()) {
            int level;
            try {
                level = Integer.parseInt(getOutlineLevel(element));
                if (level < 1) {
                    continue; // Illegal value
                }
            } catch (NumberFormatException ignore) {
                continue; // Invalid value
            }

            String name = getBookmarkName(element);

            while (current.level >= level) {
                current = current.parent;
            }
            current = new HTMLOutline(level, name, current);
            map.put(element, current.bookmark);
        }
        initBoxRefs(map, box);
        return root.bookmark.getChildren();
    }

    private static void initBoxRefs(Map<Element,Bookmark> map, Box box) {
        Bookmark bookmark = map.get(box.getElement());
        if (bookmark != null) {
            bookmark.setBox(box);
        }
        for (int i = 0, len = box.getChildCount(); i < len; i++) {
            initBoxRefs(map, box.getChild(i));
        }
    }

    private static String getBookmarkName(Element element) {
        String name = element.getAttribute("data-pdf-bookmark-name").trim();
        if (name.isEmpty()) {
            name = element.getTextContent();
        }
        name = WS.matcher(name.trim()).replaceAll(" ");
        if (name.length() > MAX_NAME_LENGTH) {
            name = name.substring(0, MAX_NAME_LENGTH);
        }
        return name;
    }

    private static String getOutlineLevel(Element element) {
        String bookmark = element.getAttribute("data-pdf-bookmark").trim();
        return bookmark.isEmpty() ?
                getOutlineLevelFromTagName(element.getTagName()) :
                bookmark;
    }

    static String getOutlineLevelFromTagName(String tagName) {
        Matcher heading = HEADING.matcher(tagName);
        if (heading.matches()) {
            return heading.group(1);
        } else if (ROOT.matcher(tagName).matches()) {
            return "exclude";
        } else {
            return "none";
        }
    }


    private static class NestedSectioningFilter implements NodeFilter {
        private static final NestedSectioningFilter INSTANCE = new NestedSectioningFilter();

        private static NodeIterator iterator(Element root) {
            Document ownerDocument = root.getOwnerDocument();
            return (ownerDocument instanceof DocumentTraversal)
                ? ((DocumentTraversal) ownerDocument).createNodeIterator(root, SHOW_ELEMENT, INSTANCE, true)
                : null;
        }

        @Override
        public short acceptNode(Node n) {
            String outlineLevel = getOutlineLevel((Element) n);
            if (outlineLevel.equalsIgnoreCase("none")) {
                return FILTER_SKIP;
            }
            return outlineLevel.equalsIgnoreCase("exclude")
                    ? FILTER_REJECT
                    : FILTER_ACCEPT;
        }
    }
}
