/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.util.LinkedList;
import java.util.List;

public class SAXEventRecorder implements ContentHandler {
    private final List<Event> _events = new LinkedList<>();

    private interface Event {
        void replay(ContentHandler handler) throws SAXException;
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) {
        _events.add(handler -> handler.characters(ch, start, length));
    }

    @Override
    public void endDocument() {
        _events.add(handler -> handler.endDocument());
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) {
        _events.add(handler -> handler.endElement(uri, localName, qName));

    }

    @Override
    public void endPrefixMapping(final String prefix) {
        _events.add(handler -> handler.endPrefixMapping(prefix));
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) {
        _events.add(handler -> handler.ignorableWhitespace(ch, start, length));
    }

    @Override
    public void processingInstruction(final String target, final String data) {
        _events.add(handler -> handler.processingInstruction(target, data));
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
    }

    @Override
    public void skippedEntity(final String name) {
        _events.add(handler -> handler.skippedEntity(name));
    }

    @Override
    public void startDocument() {
        _events.add(handler -> handler.startDocument());
    }

    @Override
    public void startElement(
            final String uri, final String localName, final String qName, final Attributes attributes) {
        _events.add(handler -> handler.startElement(uri, localName, qName, attributes));
    }

    @Override
    public void startPrefixMapping(final String prefix, final String uri) {
        _events.add(handler -> handler.startPrefixMapping(prefix, uri));
    }

    public void replay(ContentHandler handler) throws SAXException {
        for (Event e : _events) {
            e.replay(handler);
        }
    }
}
