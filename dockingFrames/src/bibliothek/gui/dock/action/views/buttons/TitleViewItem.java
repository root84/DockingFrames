/**
 * Bibliothek - DockingFrames
 * Library built on Java/Swing, allows the user to "drag and drop"
 * panels containing any Swing-Component the developer likes to add.
 * 
 * Copyright (C) 2007 Benjamin Sigg
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Benjamin Sigg
 * benjamin_sigg@gmx.ch
 * CH - Switzerland
 */

package bibliothek.gui.dock.action.views.buttons;

import bibliothek.gui.dock.action.views.ViewItem;
import bibliothek.gui.dock.title.DockTitle;

/**
 * A view item that will be shown between some buttons on a DockTitle.
 * @author Benjamin Sigg
 *
 * @param <A> the type of item wrapped in this object
 */
public interface TitleViewItem<A> extends ViewItem<A> {
	/**
	 * Informs the item about the orientation of the title that uses this 
	 * item.
	 * @param orientation the orientation
	 */
	public void setOrientation( DockTitle.Orientation orientation );
}
