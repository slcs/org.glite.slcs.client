// Jericho HTML Parser - Java based library for analysing and manipulating HTML
// Version 2.2
// Copyright (C) 2006 Martin Jericho
// http://sourceforge.net/projects/jerichohtml/
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// http://www.gnu.org/copyleft/lesser.html
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package org.glite.slcs.jericho.html;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

final class HTMLElementNameSet extends HashSet {
	public HTMLElementNameSet() {
		super(1);
	}

	public HTMLElementNameSet(final String[] items) {
		super(items.length*2);
		for (int i=0; i<items.length; i++) add(items[i]);
	}

	public HTMLElementNameSet(final Collection collection) {
		super(collection.size()*2);
		union(collection);
	}

	public HTMLElementNameSet(final String item) {
		super(2);
		add(item);
	}

	HTMLElementNameSet union(final String item) {
		add(item);
		return this;
	}

	HTMLElementNameSet union(final Collection collection) {
		for (final Iterator i=collection.iterator(); i.hasNext();) add(i.next());
		return this;
	}

	HTMLElementNameSet minus(final String item) {
		remove(item);
		return this;
	}

	HTMLElementNameSet minus(final Collection collection) {
		for (final Iterator i=collection.iterator(); i.hasNext();) remove(i.next());
		return this;
	}
}
