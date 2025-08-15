/*
 * {{{ header & license
 * Copyright (c) 2004-2008 Joshua Marinacci, Torbjoern Gannholm, Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.swing;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.FSRGBColor;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.derived.ColorValue;
import org.openpdf.css.style.derived.LengthValue;
import org.openpdf.css.style.derived.StringValue;
import org.openpdf.event.DocumentListener;
import org.openpdf.extend.FSCanvas;
import org.openpdf.extend.NamespaceHandler;
import org.openpdf.extend.UserInterface;
import org.openpdf.layout.BoxBuilder;
import org.openpdf.layout.Layer;
import org.openpdf.layout.LayoutContext;
import org.openpdf.layout.SharedContext;
import org.openpdf.render.BlockBox;
import org.openpdf.render.Box;
import org.openpdf.render.PageBox;
import org.openpdf.render.RenderingContext;
import org.openpdf.render.ViewportBox;
import org.openpdf.util.Configuration;
import org.openpdf.util.XRLog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static java.util.Objects.requireNonNull;

public class RootPanel extends JPanel implements Scrollable, UserInterface, FSCanvas, RepaintListener {
    @Nullable
    private Box rootBox;
    private boolean needRelayout;
    @Nullable
    private CellRendererPane cellRendererPane;
    private final Set<DocumentListener> documentListeners = new HashSet<>();
    private boolean defaultFontFromComponent;
    private final SharedContext sharedContext;
    @Nullable
    private volatile LayoutContext layoutContext;
    @Nullable
    private JScrollPane enclosingScrollPane;
    private boolean viewportMatchWidth = true;

    private int default_scroll_mode = JViewport.BLIT_SCROLL_MODE;

    @Nullable
    protected Document doc;

    /*
     * ========= UserInterface implementation ===============
     */
    @Nullable Element hovered_element;
    @Nullable Element active_element;
    @Nullable Element focus_element;

    // On-demand repaint requests for async image loading
    private long lastRepaintRunAt = System.currentTimeMillis();
    private final long maxRepaintRequestWaitMs = 50;
    private boolean repaintRequestPending;
    private long pendingRepaintCount;

    public RootPanel(SharedContext sharedContext) {
        this.sharedContext = sharedContext;
    }

    public SharedContext getSharedContext() {
        return sharedContext;
    }

    @Nullable
    @CheckReturnValue
    public LayoutContext getLayoutContext() {
        return layoutContext;
    }

    public void setDocument(Document doc, @Nullable String url, NamespaceHandler nsh) {
        fireDocumentStarted();
        resetScrollPosition();
        setRootBox(null);
        this.doc = doc;

        //have to do this first
        if (Configuration.isTrue("xr.cache.stylesheets", true)) {
            getSharedContext().getCss().flushStyleSheets();
        } else {
            getSharedContext().getCss().flushAllStyleSheets();
        }

        getSharedContext().reset();
        getSharedContext().setBaseURL(url);
        getSharedContext().setNamespaceHandler(nsh);
        getSharedContext().getCss().setDocumentContext(getSharedContext(), getSharedContext().getNamespaceHandler(), doc, this);

        repaint();
    }

    public void resetScrollPosition() {
        if (enclosingScrollPane != null) {
            JScrollBar scrollBar = enclosingScrollPane.getVerticalScrollBar();
            if(scrollBar != null) {
                scrollBar.setValue(0);
            }
        }
    }

    /**
     * The method is invoked by {@link #addNotify} and {@link #removeNotify} to
     * ensure that any enclosing {@link JScrollPane} works correctly with this
     * panel. This method can be safely invoked with a <tt>null</tt> scrollPane.
     *
     * @param scrollPane the enclosing {@link JScrollPane} or <tt>null</tt> if
     *                   the panel is no longer enclosed in a {@link JScrollPane}.
     */
    protected void setEnclosingScrollPane(@Nullable JScrollPane scrollPane) {

        enclosingScrollPane = scrollPane;

        if (enclosingScrollPane != null) {
            JViewport viewPort = enclosingScrollPane.getViewport();
            if(viewPort != null) {
                default_scroll_mode = viewPort.getScrollMode();
            }
        }
    }

    @Nullable
    protected JScrollPane getEnclosingScrollPane() {
        return enclosingScrollPane;
    }

    /**
     * Gets the fixedRectangle attribute of the BasicPanel object
     *
     * @return The fixedRectangle value
     */
    @Override
    public Rectangle getFixedRectangle() {
        if (enclosingScrollPane != null) {
            return enclosingScrollPane.getViewportBorderBounds();
        }
        Dimension dim = getSize();
        return new Rectangle(0, 0, dim.width, dim.height);
    }

    /**
     * Overrides the default implementation to test for and configure any {@link
     * JScrollPane} parent.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        XRLog.general(Level.FINE, "add notify called");
        Container p = getParent();
        if (p instanceof JViewport) {
            Container vp = p.getParent();
            if (vp instanceof JScrollPane) {
                setEnclosingScrollPane((JScrollPane) vp);
            }
        }
    }

    /**
     * Overrides the default implementation un-configure any {@link JScrollPane}
     * parent.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        setEnclosingScrollPane(null);
    }

    protected final void init() {
        setBackground(Color.white);
        super.setLayout(null);
    }

    public RenderingContext newRenderingContext(Graphics2D g) {
        XRLog.layout(Level.FINEST, "new context begin");

        getSharedContext().setCanvas(this);

        XRLog.layout(Level.FINEST, "new context end");

        Java2DFontContext fontContext = new Java2DFontContext(g);
        getSharedContext().getTextRenderer().setup(fontContext);
        final Box rb = getRootBox();
        Layer rootLayer = rb == null ? null : rb.getLayer();
        return getSharedContext().newRenderingContextInstance(new Java2DOutputDevice(g), fontContext, rootLayer, 0);
    }

    protected LayoutContext newLayoutContext(Graphics2D g) {
        XRLog.layout(Level.FINEST, "new context begin");
        getSharedContext().setCanvas(this);
        XRLog.layout(Level.FINEST, "new context end");

        Graphics2D layoutGraphics = g.getDeviceConfiguration().createCompatibleImage(1, 1).createGraphics();
        Java2DFontContext fontContext = new Java2DFontContext(layoutGraphics);
        LayoutContext result = getSharedContext().newLayoutContextInstance(fontContext);
        getSharedContext().getTextRenderer().setup(fontContext);

        return result;
    }

    private Rectangle getInitialExtents(LayoutContext c) {
        if (! c.isPrint()) {
            Rectangle extents = getScreenExtents();

            // HACK avoid bogus warning
            if (extents.width == 0 && extents.height == 0) {
                extents = new Rectangle(0, 0, 1, 1);
            }

            return extents;
        } else {
            PageBox first = Layer.createPageBox(c, "first");
            return new Rectangle(0, 0,
                    first.getContentWidth(c), first.getContentHeight(c));
        }
    }

    public Rectangle getScreenExtents() {
        Rectangle extents;
        if (enclosingScrollPane != null) {
            Rectangle bounds = enclosingScrollPane.getViewportBorderBounds();
            extents = new Rectangle(0, 0, bounds.width, bounds.height);
        } else {
            extents = new Rectangle(getWidth(), getHeight());
            Insets insets = getInsets();
            extents.width -= insets.left + insets.right;
            extents.height -= insets.top + insets.bottom;
        }
        return extents;
    }

    public void doDocumentLayout(@Nullable Graphics g) {
        try {
            removeAll();
            if (g == null) {
                return;
            }
            if (doc == null) {
                return;
            }

            LayoutContext c = newLayoutContext((Graphics2D) g);
            synchronized (this) {
                this.layoutContext = c;
            }

            long start = System.currentTimeMillis();

            BlockBox root = (BlockBox)getRootBox();
            if (root != null && isNeedRelayout()) {
                root.reset(c);
            } else {
                root = BoxBuilder.createRootBox(c, doc);
                setRootBox(root);
            }

            initFontFromComponent(root);

            Rectangle initialExtents = getInitialExtents(c);
            root.setContainingBlock(new ViewportBox(initialExtents));

            root.layout(c);

            long end = System.currentTimeMillis();

            XRLog.layout(Level.INFO, "Layout took " + (end - start) + "ms");

            /*
            System.out.println(root.dump(c, "", BlockBox.DUMP_LAYOUT));
            */

            // if there is a fixed child then we need to set opaque to false
            // so that the entire viewport will be repainted. this is slower
            // but that's the hit you get from using fixed layout
            super.setOpaque(!root.getLayer().containsFixedContent());

            XRLog.layout(Level.FINEST, "after layout: " + root);

            Dimension intrinsic_size = root.getLayer().getPaintingDimension(c);

            if (c.isPrint()) {
                root.getLayer().trimEmptyPages(intrinsic_size.height);
                root.getLayer().layoutPages(c);
            }

            // If the initial size we fed into the layout matches the width
            // of the layout generated then we can set the scrollable property
            // that matches width of the view pane to the width of this panel.
            // Otherwise, if the intrinsic width is different then we can't
            // couple the width of the view pane to the width of this panel
            // (we hit the minimum size threshold).
            viewportMatchWidth = (initialExtents.width == intrinsic_size.width);

            setPreferredSize(intrinsic_size);
            revalidate();

            if (enclosingScrollPane != null) {
                JViewport viewPort = enclosingScrollPane.getViewport();
                if(viewPort != null) {
                    // turn on simple scrolling mode if there's any fixed elements
                    if (root.getLayer().containsFixedContent()) {
                        viewPort.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
                    }
                    else {
                        viewPort.setScrollMode(default_scroll_mode);
                    }
                }
            }

            fireDocumentLoaded();
            /* FIXME
            if (Configuration.isTrue("xr.image.background.greedy", false)) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        XRLog.load("loading images in document and css greedily");
                        requestBGImages(getRootBox());
                    }
                });
            }*/
        } catch (Error | RuntimeException t) {
            if (hasDocumentListeners()) {
                fireOnLayoutException(t);
            } else {
                throw t;
            }
        }
    }

    private void initFontFromComponent(BlockBox root) {
        if (isDefaultFontFromComponent()) {
            CalculatedStyle style = root.getStyle();
            PropertyValue fontFamilyProp = new PropertyValue(CSSPrimitiveValue.CSS_STRING, getFont().getFamily(),
                    getFont().getFamily(), new String[]{getFont().getFamily()}, null);
            style.setDefaultValue(CSSName.FONT_FAMILY, new StringValue(CSSName.FONT_FAMILY, fontFamilyProp));
            style.setDefaultValue(CSSName.FONT_SIZE, new LengthValue(style, CSSName.FONT_SIZE,
                    new PropertyValue(CSSPrimitiveValue.CSS_PX, getFont().getSize(), Integer
                            .toString(getFont().getSize()))));
            Color c = getForeground();
            style.setDefaultValue(CSSName.COLOR, new ColorValue(CSSName.COLOR,
                    new PropertyValue(new FSRGBColor(c.getRed(), c.getGreen(), c.getBlue()))));

            if (getFont().isBold()) {
                style.setDefaultValue(CSSName.FONT_WEIGHT, IdentValue.BOLD);
            }

            if (getFont().isItalic()) {
                style.setDefaultValue(CSSName.FONT_STYLE, IdentValue.ITALIC);
            }
        }
    }

    /**
     * Adds the specified Document listener to receive Document events from this
     * component. If listener l is null, no exception is thrown and no action is
     * performed.
     *
     * @param listener Contains the DocumentListener for DocumentEvent data.
     */
    public void addDocumentListener(DocumentListener listener) {
        documentListeners.add(requireNonNull(listener));
    }

    /**
     * Removes the specified Document listener from receive Document events from this
     * component. If listener l is null, no exception is thrown and no action is
     * performed.
     *
     * @param listener Contains the DocumentListener to remove.
     */
    public void removeDocumentListener(DocumentListener listener) {
        documentListeners.remove(requireNonNull(listener));
    }

    protected boolean hasDocumentListeners() {
        return !documentListeners.isEmpty();
    }

    protected void fireDocumentStarted() {
        for (DocumentListener list : documentListeners) {
            try {
                list.documentStarted();
            } catch (Exception e) {
                XRLog.load(Level.WARNING, "Document listener threw an exception; continuing processing", e);
            }
        }
    }

    protected void fireDocumentLoaded() {
        for (DocumentListener list : documentListeners) {
            try {
                list.documentLoaded();
            } catch (Exception e) {
                XRLog.load(Level.WARNING, "Document listener threw an exception; continuing processing", e);
            }
        }
    }

    protected void fireOnLayoutException(Throwable t) {
        for (DocumentListener list : documentListeners) {
            try {
                list.onLayoutException(t);
            } catch (Exception e) {
                XRLog.load(Level.WARNING, "Document listener threw an exception; continuing processing", e);
            }
        }
    }

    protected void fireOnRenderException(Throwable t) {
        for (DocumentListener list : documentListeners) {
            try {
                list.onRenderException(t);
            } catch (Exception e) {
                XRLog.load(Level.WARNING, "Document listener threw an exception; continuing processing", e);
            }
        }
    }

    /**
     * @return a CellRendererPane suitable for drawing components in (with CellRendererPane.paintComponent)
     */
    public CellRendererPane getCellRendererPane() {
        if (cellRendererPane == null || cellRendererPane.getParent() != this) {
            cellRendererPane = new CellRendererPane();
            this.add(cellRendererPane);
        }

        return cellRendererPane;
    }


    @Override
    public boolean isHover(org.w3c.dom.Element e) {
        return e == hovered_element;
    }

    @Override
    public boolean isActive(org.w3c.dom.Element e) {
        return e == active_element;
    }

    @Override
    public boolean isFocus(org.w3c.dom.Element e) {
        return e == focus_element;
    }


    /**
     * Lays out the current document again, and re-renders.
     */
    protected void relayout() {
        getSharedContext().flushFonts();
        if (doc != null) {
            setNeedRelayout(true);
            repaint();
        }
    }

    public double getLayoutWidth() {
        if (enclosingScrollPane != null) {
            return enclosingScrollPane.getViewportBorderBounds().width;
        } else {
            return getSize().width;
        }
    }

    public boolean isPrintView() {
        return false;
    }

    @Nullable
    public synchronized Box getRootBox() {
        return rootBox;
    }

    public synchronized void setRootBox(@Nullable Box rootBox) {
        this.rootBox = rootBox;
    }

    @Nullable
    public synchronized Layer getRootLayer() {
        return getRootBox() == null ? null : getRootBox().getLayer();
    }

    public Box find(MouseEvent e) {
        return find(e.getX(), e.getY());
    }

    @Nullable
    public Box find(int x, int y) {
        Layer l = getRootLayer();
        if (l != null) {
            return l.find(layoutContext, x, y, false);
        }
        return null;
    }

    @Override
    public void doLayout() {
        if (isExtentsHaveChanged()) {
            setNeedRelayout(true);
        }
        super.doLayout();
    }

    @Override
    public void validate() {
        super.validate();

        if (isExtentsHaveChanged()) {
            setNeedRelayout(true);
        }
    }

    protected boolean isExtentsHaveChanged() {
        if (rootBox == null) {
            return true;
        } else {
            Rectangle oldExtents = ((ViewportBox)rootBox.getContainingBlock()).getExtents();
            return !oldExtents.equals(getScreenExtents());
        }
    }

    protected synchronized boolean isNeedRelayout() {
        return needRelayout;
    }

    protected synchronized void setNeedRelayout(boolean needRelayout) {
        this.needRelayout = needRelayout;
    }

    @Override
    public void repaintRequested(final boolean doLayout) {
        final long now = System.currentTimeMillis();
        final long el = now - lastRepaintRunAt;
        if (!doLayout || el > maxRepaintRequestWaitMs || pendingRepaintCount > 5) {
            XRLog.general(Level.FINE, "*** Repainting panel, by request, el: " + el + " pending " + pendingRepaintCount);
            if (doLayout) {
                relayout();
            } else {
                repaint();
            }
            lastRepaintRunAt = System.currentTimeMillis();
            repaintRequestPending = false;
            pendingRepaintCount = 0;
        } else {
            if (!repaintRequestPending) {
                XRLog.general(Level.FINE, "... Queueing new repaint request, el: " + el + " < " + maxRepaintRequestWaitMs);
                repaintRequestPending = true;

                int delay = (int) Math.min(maxRepaintRequestWaitMs, Math.abs(maxRepaintRequestWaitMs - el));
                new javax.swing.Timer(delay, e -> {
                    XRLog.general(Level.FINE, "--> running queued repaint request");
                    repaintRequested(doLayout);
                    repaintRequestPending = false;
                }).start();

            } else {
                pendingRepaintCount++;
                XRLog.general("hmm... repaint request, but already have one");
            }
        }
    }


    public boolean isDefaultFontFromComponent() {
        return defaultFontFromComponent;
    }

    public void setDefaultFontFromComponent(boolean defaultFontFromComponent) {
        this.defaultFontFromComponent = defaultFontFromComponent;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
      int dif = 1;
      if (orientation == SwingConstants.VERTICAL) {
        dif = visibleRect.height;
      }
      else if (orientation == SwingConstants.HORIZONTAL) {
        dif = visibleRect.width;
      }
      return Math.min(35, dif);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
      int dif = 1;
      if (orientation == SwingConstants.VERTICAL) {
        dif = Math.max(visibleRect.height - 10, dif);
      }
      else if (orientation == SwingConstants.HORIZONTAL) {
        dif = Math.max(visibleRect.width, dif);
      }
      return dif;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        // If the last layout successfully filled the desired width then
        // viewport should match the component size.
        return viewportMatchWidth;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        // If the last layout height of this component is <= the viewport
        // height then we make the viewport height match the component size.
        if(enclosingScrollPane != null) {
            JViewport viewPort = enclosingScrollPane.getViewport();
            if (viewPort != null) {
                return getPreferredSize().height <= viewPort.getHeight();
            }
        }
        return false;
    }

}
