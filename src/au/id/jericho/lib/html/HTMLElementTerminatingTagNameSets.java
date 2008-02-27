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

package au.id.jericho.lib.html;

import java.util.Set;

final class HTMLElementTerminatingTagNameSets {
	// all fields are guaranteed not null and contain unique sets.
	public final Set TerminatingStartTagNameSet; // Set of start tags that terminate the element
	public final Set TerminatingEndTagNameSet; // Set of end tags that terminate the element (the end tag of this element is assumed and not included in this set)
	public final Set NonterminatingElementNameSet; // Set of elements that can be inside this element, which may contain tags from TerminatingStartTagNameSet and TerminatingEndTagNameSet that must be ignored

	public HTMLElementTerminatingTagNameSets(final Set terminatingStartTagNameSet, final Set terminatingEndTagNameSet, final Set nonterminatingElementNameSet) {
		this.TerminatingStartTagNameSet=terminatingStartTagNameSet;
		this.TerminatingEndTagNameSet=terminatingEndTagNameSet;
		this.NonterminatingElementNameSet=nonterminatingElementNameSet;
	}
}
