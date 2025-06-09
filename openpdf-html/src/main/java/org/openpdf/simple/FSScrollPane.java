/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Patrick Wright
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
 * }}}
 */
package org.openpdf.simple;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

/**
 * <p>{@code FSScrollPane} is a JScrollPane set up to support keyboard navigation of an XHTML/XML
 * document rendered with Flying Saucer. In particular, it assigns key bindings to the view's {@link javax.swing.InputMap}
 * for page-up, page-down, line-up/down, page-start and page-end.
 * The amount the document scrolls is based on the current viewport and the current line height.
 * If the view is resized, the scroll increment is automatically adjusted. Using FSScrollPane
 * to display an {@link XHTMLPanel} should save you time as your users
 * will have standard keyboard navigation out of the box.</p>
 *
 * <p>To use {@code FSScrollPane}, just instantiate it and add your XHTMLPanel on instantiation:
 * <pre>
 * XHTMLPanel panel = new XHTMLPanel();
 * FSScrollPane scroll = new FSScrollPane(view);
 * </pre>
 * <p>The current input mappings to keys are:
 * <dl>
 * <dt>Scroll to Start<dt><dd>CONTROL-HOME or HOME</dd>
 * <dt>Scroll Up 1 Page<dt><dd>PAGEUP</dd>
 * <dt>Scroll Up 1 Line<dt><dd>UP-ARROW</dd>
 * <dt>Scroll to Bottom<dt><dd>CONTROL-END or END</dd>
 * <dt>Scroll Down 1 Page<dt><dd>PAGEDOWN</dd>
 * <dt>Scroll Down 1 Line<dt><dd>DOWN-ARROW</dd>
 * </dl>
 * This class declares six constant strings you can use if you want to override one of these default
 * settings on the {@code InputMap}; these Strings will be trigger the relevant {@code Action}
 * associated with the scrolling. To change the key binding for "Scroll to Top" to {@code Alt-Home},
 * do this:
 * <pre>
 * panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.ALT_MASK), FSScrollPane.PAGE_START);
 * </pre>
 *
 *
 * @author Patrick Wright
 */
public class FSScrollPane extends JScrollPane {
    /** Constant used for mapping a key binding to "scroll down 1 page" */
    public static final String PAGE_DOWN = "page-down";

    /** Constant used for mapping a key binding to "scroll up 1 page" */
    public static final String PAGE_UP = "page-up";

    /** Constant used for mapping a key binding to "scroll down 1 line" */
    public static final String LINE_DOWN = "down";

    /** Constant used for mapping a key binding to "scroll up 1 line" */
    public static final String LINE_UP = "up";

    /** Constant used for mapping a key binding to "scroll to end of document" */
    public static final String PAGE_END = "page-end";

    /** Constant used for mapping a key binding to "scroll to top of document" */
    public static final String PAGE_START = "page-start";

    /** Instantiates a new FSScrollPane around the given Panel; see class documentation. */
    public FSScrollPane(JPanel view) {
        super(view, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);

        // TODO: need to get line-height, I think; this should not be fixed (PWW 28-01-05)
        getVerticalScrollBar().setUnitIncrement(15);
    }

    @Override
    public void setViewportView(Component view)
    {
        setPreferredSize(new Dimension((int)view.getSize().getWidth(), (int)view.getSize().getHeight()));
        if (view instanceof JComponent) {
            setDefaultInputMap((JComponent) view);
            setDefaultActionMap((JComponent) view);
        }
        addResizeListener(view);
        super.setViewportView(view);
    }

    /** Assigns the default keyboard bindings on the view for document navigation. */
    private void setDefaultInputMap(JComponent view) {
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), PAGE_DOWN);
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), PAGE_UP);
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), LINE_DOWN);
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), LINE_UP);
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.CTRL_DOWN_MASK), PAGE_END);
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), PAGE_END);
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.CTRL_DOWN_MASK), PAGE_START);
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), PAGE_START);

    }

    /** Assigns the default Actions for document navigation on the view. */
    private void setDefaultActionMap(JComponent view) {
        view.getActionMap().put(PAGE_DOWN,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        JScrollBar sb = getVerticalScrollBar();
                        sb.getModel().setValue(sb.getModel().getValue() + sb.getBlockIncrement(1));
                    }
                });
        view.getActionMap().put(PAGE_END,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        JScrollBar sb = getVerticalScrollBar();
                        sb.getModel().setValue(sb.getModel().getMaximum());
                    }
                });
        view.getActionMap().put(PAGE_UP,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        JScrollBar sb = getVerticalScrollBar();
                        sb.getModel().setValue(sb.getModel().getValue() - sb.getBlockIncrement(-1));
                    }
                });
        view.getActionMap().put(PAGE_START,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        JScrollBar sb = getVerticalScrollBar();
                        sb.getModel().setValue(0);
                    }
                });
        view.getActionMap().put(LINE_DOWN,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        JScrollBar sb = getVerticalScrollBar();
                        sb.getModel().setValue(sb.getModel().getValue() + sb.getUnitIncrement(1));
                    }
                });
        view.getActionMap().put(LINE_UP,
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        JScrollBar sb = getVerticalScrollBar();
                        sb.getModel().setValue(sb.getModel().getValue() - sb.getUnitIncrement(-1));
                    }
                });
    }

    /** Adds a component listener on the view for resize events, to adjust the scroll increment. */
    private void addResizeListener(Component view) {
        view.addComponentListener( new ComponentAdapter() {
            /** Invoked when the component's size changes. Reset scrollable increment, because
             * page-down/up is relative to current view size.
             */
            @Override
            public void componentResized(ComponentEvent e) {
                JScrollBar bar = getVerticalScrollBar();

                // NOTE: use the scroll pane size--the XHTMLPanel size is a virtual size of the entire
                // page

                // want to page down leaving the current line at the bottom be the first at the top
                // TODO: this will only work once unit increment is set correctly; multiplier is a workaround (PWW 28-01-05)
                int incr = (int)(getSize().getHeight() - (bar.getUnitIncrement(1) * 3));
                getVerticalScrollBar().setBlockIncrement(incr);
            }
        });
    }
}
