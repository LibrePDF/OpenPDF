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

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DocumentSplitter implements ContentHandler {
    private static final String HEAD_ELEMENT_NAME = "head";

    private final List<ProcessingInstruction> _processingInstructions = new LinkedList<>();
    private final SAXEventRecorder _head = new SAXEventRecorder();
    private boolean _inHead;

    private int _depth;

    private boolean _needNewNSScope;
    private NamespaceScope _currentNSScope = new NamespaceScope();

    private boolean _needNSScopePop;

    private Locator _locator;

    private TransformerHandler _handler;
    private boolean _inDocument;

    private final List<Document> _documents = new LinkedList<>();

    private boolean _replayedHead;

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (_inHead) {
            _head.characters(ch, start, length);
        } else if (_inDocument) {
            _handler.characters(ch, start, length);
        }
    }

    @Override
    public void endDocument() {
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        if (_inHead) {
            _head.endPrefixMapping(prefix);
        } else if (_inDocument) {
            _handler.endPrefixMapping(prefix);
        } else {
            _needNSScopePop = true;
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (_inHead) {
            _head.ignorableWhitespace(ch, start, length);
        } else if (_inDocument) {
            _handler.ignorableWhitespace(ch, start, length);
        }
    }

    @Override
    public void processingInstruction(String target, String data) {
        _processingInstructions.add(new ProcessingInstruction(target, data));

    }

    @Override
    public void setDocumentLocator(Locator locator) {
        _locator = locator;
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        if (_inHead) {
            _head.skippedEntity(name);
        } else if (_inDocument) {
            _handler.skippedEntity(name);
        }
    }

    @Override
    public void startDocument() {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (_inHead) {
            _head.startElement(uri, localName, qName, attributes);
        } else if (_inDocument) {
            if (_depth == 2 && ! _replayedHead) {
                if (HEAD_ELEMENT_NAME.equalsIgnoreCase(qName)) {
                    _handler.startElement(uri, localName, qName, attributes);
                    _head.replay(_handler);
                } else {
                    _handler.startElement("", HEAD_ELEMENT_NAME, HEAD_ELEMENT_NAME, new AttributesImpl());
                    _head.replay(_handler);
                    _handler.endElement("", HEAD_ELEMENT_NAME, HEAD_ELEMENT_NAME);

                    _handler.startElement(uri, localName, qName, attributes);
                }

                _replayedHead = true;
            } else {
                _handler.startElement(uri, localName, qName, attributes);
            }
        } else {
            if (_needNewNSScope) {
                _needNewNSScope = false;
                _currentNSScope = new NamespaceScope(_currentNSScope);
            }

            if (_depth == 1) {
                if (HEAD_ELEMENT_NAME.equalsIgnoreCase(qName)) {
                    _inHead = true;
                    _currentNSScope.replay(_head, true);
                } else {
                    try {
                        _inDocument = true;
                        _replayedHead = false;
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        factory.setNamespaceAware(true);
                        factory.setValidating(false);

                        Document doc = factory.newDocumentBuilder().newDocument();
                        _documents.add(doc);
                        _handler =
                            ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();
                        _handler.setResult(new DOMResult(doc));

                        _handler.startDocument();
                        _handler.setDocumentLocator(_locator);
                        for (ProcessingInstruction pI : _processingInstructions) {
                            _handler.processingInstruction(pI.target(), pI.data());
                        }

                        _currentNSScope.replay(_handler, true);
                        _handler.startElement(uri, localName, qName, attributes);
                    } catch (ParserConfigurationException | TransformerConfigurationException e) {
                        throw new SAXException(e.getMessage(), e);
                    }
                }
            }
        }

        _depth++;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        _depth--;

        if (_needNSScopePop) {
            _needNSScopePop = false;
            _currentNSScope = _currentNSScope.getParent();
        }

        if (_inHead) {
            if (_depth == 1) {
                _currentNSScope.replay(_head, false);
                _inHead = false;
            } else {
                _head.endElement(uri, localName, qName);
            }
        } else if (_inDocument) {
            if (_depth == 1) {
                _currentNSScope.replay(_handler, false);
                _handler.endElement(uri, localName, qName);
                _handler.endDocument();
                _inDocument = false;
            } else {
                _handler.endElement(uri, localName, qName);
            }
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (_inHead) {
            _head.startPrefixMapping(prefix, uri);
        } else if (_inDocument) {
            _handler.startPrefixMapping(prefix, uri);
        } else {
            _needNewNSScope = true;
            _currentNSScope.addNamespace(new Namespace(prefix, uri));
        }
    }

    public List<Document> getDocuments() {
        return _documents;
    }

    private static final class Namespace {
        private final String _prefix;
        private final String _uri;

        private Namespace(String prefix, String uri) {
            _prefix = prefix;
            _uri = uri;
        }

        public String getPrefix() {
            return _prefix;
        }

        public String getUri() {
            return _uri;
        }
    }

    private static final class NamespaceScope {
        @Nullable
        private final NamespaceScope _parent;
        private final List<Namespace> _namespaces = new LinkedList<>();

        private NamespaceScope() {
            _parent = null;
        }

        private NamespaceScope(NamespaceScope parent) {
            _parent = parent;
        }

        public void addNamespace(Namespace namespace) {
            _namespaces.add(namespace);
        }

        public void replay(ContentHandler contentHandler, boolean start) throws SAXException {
            replay(contentHandler, new HashSet<>(), start);
        }

        private void replay(ContentHandler contentHandler, Set<String> seen, boolean start)
                throws SAXException {
            for (Namespace ns : _namespaces) {
                if (!seen.contains(ns.getPrefix())) {
                    seen.add(ns.getPrefix());
                    if (start) {
                        contentHandler.startPrefixMapping(ns.getPrefix(), ns.getUri());
                    } else {
                        contentHandler.endPrefixMapping(ns.getPrefix());
                    }
                }
            }

            if (_parent != null) {
                _parent.replay(contentHandler, seen, start);
            }
        }

        @Nullable
        public NamespaceScope getParent() {
            return _parent;
        }
    }

    private record ProcessingInstruction(String target, String data) {
    }
}
