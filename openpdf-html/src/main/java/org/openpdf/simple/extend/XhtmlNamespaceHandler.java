/*
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
package org.openpdf.simple.extend;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.regex.Pattern;

import static java.util.Locale.ROOT;
import static org.w3c.dom.Node.ELEMENT_NODE;

/**
 * Handles xhtml documents, including presentational html attributes (see css 2.1 spec, 6.4.4).
 * In this class ONLY handling (css equivalents) of presentational properties
 * (according to css 2.1 spec, section 6.4.4) should be specified.
 *
 * @author Torbjoern Gannholm
 */
public class XhtmlNamespaceHandler extends XhtmlCssOnlyNamespaceHandler {
    private static final Pattern RE_MANGLED_COLOR = Pattern.compile("[0-9a-f]{6}");

    @Override
    @CheckReturnValue
    public boolean isImageElement(Element e) {
        return e.getNodeName().equalsIgnoreCase("img");
    }

    @Override
    @CheckReturnValue
    public boolean isFormElement(Element e) {
        return e.getNodeName().equalsIgnoreCase("form");
    }

    @Override
    @CheckReturnValue
    public String getImageSourceURI(Element e) {
        return e.getAttribute("src");
    }

    @Override
    @CheckReturnValue
    public String getNonCssStyling(Element e) {
        return switch (e.getNodeName()) {
            case "table" -> applyTableStyles(e);
            case "td", "th" -> applyTableCellStyles(e);
            case "tr" -> applyTableRowStyles(e);
            case "img" -> applyImgStyles(e);
            case "p", "div" -> applyBlockAlign(e);
            default -> "";
        };
    }

    private String applyBlockAlign(Element e) {
        String s = e.getAttribute("align").trim().toLowerCase(ROOT);
        return switch (s) {
            case "left",
                 "right",
                 "center",
                 "justify" -> "text-align: " + s + ";";
            default -> "";
        };
    }

    private String applyImgStyles(Element e) {
        StringBuilder style = new StringBuilder();
        applyFloatingAlign(e, style);
        return style.toString();
    }

    private String applyTableCellStyles(Element e) {
        StringBuilder style = new StringBuilder();
        String s;
        // check for cell padding
        Element table = findTable(e);
        if (table != null) {
            s = getAttribute(table, "cellpadding");
            if (s != null) {
                style.append("padding: ");
                style.append(convertToLength(s));
                style.append(";");
            }
            s = getAttribute(table, "border");
            if (s != null && ! s.equals("0")) {
                style.append("border: 1px outset black;");
            }
        }
        s = getAttribute(e, "width");
        if (s != null) {
            style.append("width: ");
            style.append(convertToLength(s));
            style.append(";");
        }
        s = getAttribute(e, "height");
        if (s != null) {
            style.append("height: ");
            style.append(convertToLength(s));
            style.append(";");
        }
        applyTableContentAlign(e, style);
        s = getAttribute(e, "bgcolor");
        if (s != null) {
            s = s.toLowerCase(ROOT);
            style.append("background-color: ");
            if (looksLikeAMangledColor(s)) {
                style.append('#');
                style.append(s);
            } else {
                style.append(s);
            }
            style.append(';');
        }
        s = getAttribute(e, "background");
        if (s != null) {
            style.append("background-image: url(");
            style.append(s);
            style.append(");");
        }
        return style.toString();
    }

    private String applyTableStyles(Element e) {
        StringBuilder style = new StringBuilder();
        String s;
        s = getAttribute(e, "width");
        if (s != null) {
            style.append("width: ");
            style.append(convertToLength(s));
            style.append(";");
        }
        s = getAttribute(e, "border");
        if (s != null) {
            style.append("border: ");
            style.append(convertToLength(s));
            style.append(" inset black;");
        }
        s = getAttribute(e, "cellspacing");
        if (s != null) {
            style.append("border-collapse: separate; border-spacing: ");
            style.append(convertToLength(s));
            style.append(";");
        }
        s = getAttribute(e, "bgcolor");
        if (s != null) {
            s = s.toLowerCase(ROOT);
            style.append("background-color: ");
            if (looksLikeAMangledColor(s)) {
                style.append('#');
                style.append(s);
            } else {
                style.append(s);
            }
            style.append(';');
        }
        s = getAttribute(e, "background");
        if (s != null) {
            style.append("background-image: url(");
            style.append(s);
            style.append(");");
        }
        applyFloatingAlign(e, style);
        return style.toString();
    }

    private String applyTableRowStyles(Element e) {
        StringBuilder style = new StringBuilder();
        applyTableContentAlign(e, style);
        return style.toString();
    }

    private void applyFloatingAlign(Element e, StringBuilder style) {
        String s = getAttribute(e, "align");
        if (s != null) {
            s = s.toLowerCase(ROOT).trim();
            switch (s) {
                case "left":
                    style.append("float: left;");
                    break;
                case "right":
                    style.append("float: right;");
                    break;
                case "center":
                    style.append("margin-left: auto; margin-right: auto;");
                    break;
            }
        }
    }

    private void applyTableContentAlign(Element e, StringBuilder style) {
        String s = getAttribute(e, "align");
        if (s != null) {
            style.append("text-align: ");
            style.append(s.toLowerCase(ROOT));
            style.append(";");
        }
        s = getAttribute(e, "valign");
        if (s != null) {
            style.append("vertical-align: ");
            style.append(s.toLowerCase(ROOT));
            style.append(";");
        }
    }

    boolean looksLikeAMangledColor(String s) {
        return RE_MANGLED_COLOR.matcher(s).matches();
    }

    @Nullable
    Element findTable(Node cell) {
        return ancestor(cell, "table", 5);
    }

    @Nullable
    Element ancestor(Node element, String tagName, int maxDepth) {
        Node parent = element.getParentNode();
        if (parent == null || maxDepth <= 0) {
            return null;
        }

        return parent.getNodeType() == ELEMENT_NODE && parent.getNodeName().equals(tagName) ?
            (Element) parent :
            ancestor(parent, tagName, maxDepth - 1);
    }
}

