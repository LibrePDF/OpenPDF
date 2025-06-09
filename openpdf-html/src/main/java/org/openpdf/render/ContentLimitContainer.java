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
package org.openpdf.render;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.layout.LayoutContext;

import java.util.ArrayList;
import java.util.List;

public class ContentLimitContainer {
    @Nullable
    private ContentLimitContainer _parent;

    private final int _initialPageNo;
    private final List<ContentLimit> _contentLimits = new ArrayList<>();

    @Nullable
    private PageBox _lastPage;

    public ContentLimitContainer(LayoutContext c, int startAbsY) {
        _initialPageNo = getPage(c, startAbsY).getPageNo();
    }

    public int getInitialPageNo() {
        return _initialPageNo;
    }

    public int getLastPageNo() {
        return _initialPageNo + _contentLimits.size() - 1;
    }

    @Nullable
    @CheckReturnValue
    public ContentLimit getContentLimit(int pageNo) {
        return getContentLimit(pageNo, false);
    }

    @Nullable
    @CheckReturnValue
    private ContentLimit getContentLimit(int pageNo, boolean addAsNeeded) {
        if (addAsNeeded) {
            while (_contentLimits.size() < (pageNo - _initialPageNo + 1)) {
                _contentLimits.add(new ContentLimit());
            }
        }

        int target = pageNo - _initialPageNo;
        if (target >= 0 && target < _contentLimits.size()) {
            return _contentLimits.get(pageNo - _initialPageNo);
        } else {
            return null;
        }
    }

    public void updateTop(LayoutContext c, int absY) {
        PageBox page = getPage(c, absY);

        getContentLimit(page.getPageNo(), true).updateTop(absY);

        ContentLimitContainer parent = getParent();
        if (parent != null) {
            parent.updateTop(c, absY);
        }
    }

    public void updateBottom(LayoutContext c, int absY) {
        PageBox page = getPage(c, absY);

        getContentLimit(page.getPageNo(), true).updateBottom(absY);

        ContentLimitContainer parent = getParent();
        if (parent != null) {
            parent.updateBottom(c, absY);
        }
    }

    public PageBox getPage(LayoutContext c, int absY) {
        PageBox page;
        PageBox last = getLastPage();
        if (last != null && absY >= last.getTop() && absY < last.getBottom()) {
            page = last;
        } else {
            page = c.getRootLayer().getPage(c, absY);
            setLastPage(page);
        }
        return page;
    }

    private PageBox getLastPage() {
        ContentLimitContainer c = this;
        while (c.getParent() != null) {
            c = c.getParent();
        }
        return c._lastPage;
    }

    private void setLastPage(PageBox page) {
        ContentLimitContainer c = this;
        while (c.getParent() != null) {
            c = c.getParent();
        }
        c._lastPage = page;
    }

    @Nullable
    @CheckReturnValue
    public ContentLimitContainer getParent() {
        return _parent;
    }

    public void setParent(@Nullable ContentLimitContainer parent) {
        _parent = parent;
    }

    public boolean isContainsMultiplePages() {
        return _contentLimits.size() > 1;
    }

    @Override
    public String toString() {
        return "[initialPageNo=" + _initialPageNo + ", limits=" + _contentLimits + "]";
    }
}
