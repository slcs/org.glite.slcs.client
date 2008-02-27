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

import java.util.Comparator;

final class OutputSegmentComparator implements Comparator {
	public int compare(final Object o1, final Object o2) {
		final OutputSegment outputSegment1=(OutputSegment)o1;
		final OutputSegment outputSegment2=(OutputSegment)o2;
		if (outputSegment1.getBegin()<outputSegment2.getBegin()) return -1;
		if (outputSegment1.getBegin()>outputSegment2.getBegin()) return 1;
		if (outputSegment1.getEnd()<outputSegment2.getEnd()) return -1;
		if (outputSegment1.getEnd()>outputSegment2.getEnd()) return 1;
		return 0;
	}
}

