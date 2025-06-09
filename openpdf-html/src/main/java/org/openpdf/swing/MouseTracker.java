/*
 * {{{ header & license
 * Copyright (c) 2007 Sean Bright
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

import org.openpdf.render.Box;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * A MouseTracker is used to delegate mouse events to the {@link org.openpdf.swing.FSMouseListener} instances
 *  associated with a {@link org.openpdf.swing.BasicPanel}. The tracker will start receiving events as soon
 * as the first listener is added (via {@link #addListener(FSMouseListener)}) and will stop receiving events as soon
 * as the last listener is removed via {@link #removeListener(FSMouseListener)}. This binding is handled automatically
 * via the add and remove methods and the tracker will remain active as long as the tracker has at least one listener.
 * The MouseTracker is also responsible for using MouseEvent coordinates to locate the Box on which the mouse is
 * acting.
 */
public class MouseTracker extends MouseInputAdapter {
    private final BasicPanel _panel;
    private final Map<FSMouseListener, FSMouseListener> _handlers = new LinkedHashMap<>();
    private Box _last;
    private boolean _enabled;

    /**
     * Instantiates a MouseTracker to listen to mouse events for the given panel.
     * @param panel the panel for which mouse events should be delegated.
     */
    public MouseTracker(BasicPanel panel) {
        _panel = panel;
    }

    /**
     * Adds a listener to receive callbacks on mouse events.
     *
     * @param l the listener
     */
    public void addListener(FSMouseListener l) {
        if (l == null) {
            return;
        }

        if (!_handlers.containsKey(l)) {
            _handlers.put(l, l);
        }

        if (!_enabled) {
            _panel.addMouseListener(this);
            _panel.addMouseMotionListener(this);

            _enabled = true;
        }
    }

    /**
     * Removes the given listener, after which it will no longer receive callbacks on mouse events.
     *
     * @param l the listener to remove
     */
    public void removeListener(FSMouseListener l) {
        if (l == null) {
            return;
        }

        _handlers.remove(l);

        if (_enabled && _handlers.isEmpty()) {
            _panel.removeMouseListener(this);
            _panel.removeMouseMotionListener(this);

            _enabled = false;
        }
    }

    /**
     * Returns a (new) list of all listeners currently tracked for receiving events.
     * @return a (new) list of all listeners currently tracked for receiving events.
     */
    public List<FSMouseListener> getListeners() {
        return new ArrayList<>(_handlers.keySet());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        handleMouseMotion(_panel.find(e));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        handleMouseMotion(_panel.find(e));
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        handleMouseMotion(_panel.find(e));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        handleMouseUp(_panel.find(e));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        fireMousePressed(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        fireMouseDragged(e);
    }

    /**
     * Utility method; calls {@link FSMouseListener#reset()} for all listeners currently being tracked.
     */
    public void reset() {
        _last = null;
        for (FSMouseListener fsMouseListener : _handlers.keySet()) {
            fsMouseListener.reset();
        }
    }

    // handles delegation of mouse events to listeners
    private void handleMouseMotion(Box box) {
        if (box == null || box.equals(_last)) {
            return;
        }

        if (_last != null) {
            fireMouseOut(_last);
        }

        fireMouseOver(box);

        _last = box;
    }


    private void handleMouseUp(Box box) {
        if (box == null) {
            return;
        }

        fireMouseUp(box);
    }

    // delegates onMouseOver() to all listeners
    private void fireMouseOver(Box box) {
        for (FSMouseListener fsMouseListener : _handlers.keySet()) {
            fsMouseListener.onMouseOver(_panel, box);
        }
    }

    // delegates onMouseOut() to all listeners
    private void fireMouseOut(Box box) {
        for (FSMouseListener fsMouseListener : _handlers.keySet()) {
            fsMouseListener.onMouseOut(_panel, box);
        }
    }

    // delegates onMouseUp() to all listeners
    private void fireMouseUp(Box box) {
        for (FSMouseListener fsMouseListener : _handlers.keySet()) {
            fsMouseListener.onMouseUp(_panel, box);
        }
    }

    // delegates onMousePressed() to all listeners
    private void fireMousePressed(MouseEvent e) {
        for (FSMouseListener fsMouseListener : _handlers.keySet()) {
            fsMouseListener.onMousePressed(_panel, e);
        }
    }

    // delegates onMouseDragged() to all listeners
    private void fireMouseDragged(MouseEvent e) {
        for (FSMouseListener fsMouseListener : _handlers.keySet()) {
            fsMouseListener.onMouseDragged(_panel, e);
        }
    }
}
