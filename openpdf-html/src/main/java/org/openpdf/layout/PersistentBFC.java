/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjoern Gannholm
 * Copyright (c) 2005 Wisconsin Court System
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
package org.openpdf.layout;

import org.openpdf.render.BlockBox;

/**
 * The portion of a {@link BlockFormattingContext} which is saved with a box
 * which defines a BFC.
 *
 * XXX This class can go away
 */
public class PersistentBFC {
    private final FloatManager _floatManager;

    public PersistentBFC(BlockBox master, LayoutContext c) {
        master.setPersistentBFC(this);
        _floatManager = new FloatManager(master);
    }

    public FloatManager getFloatManager() {
        return _floatManager;
    }
}
