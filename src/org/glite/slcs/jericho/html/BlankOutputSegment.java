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

import java.io.IOException;
import java.io.Writer;

/**
 * Implements an {@link OutputSegment} whose content is a string of spaces with the same length as the segment.
 * <p>
 * This class has been removed from the public API as of version 2.2 and the functionality replaced with the 
 * {@link OutputDocument#ReplaceWithSpaces(int begin, int end)} method.
 */
final class BlankOutputSegment implements OutputSegment {
	private int begin;
	private int end;

	/**
	 * Constructs a new <code>BlankOutputSegment</code> with the specified begin and end positions.
	 * @param begin  the position in the {@link OutputDocument} where this <code>OutputSegment</code> begins.
	 * @param end  the position in the {@link OutputDocument} where this <code>OutputSegment</code> ends.
	 */
	public BlankOutputSegment(final int begin, final int end) {
		this.begin=begin;
		this.end=end;
	}

	/**
	 * Constructs a new <code>BlankOutputSegment</code> with the same span as the specified {@link Segment}.
	 * @param segment  a {@link Segment} defining the begin and end character positions of the new <code>OutputSegment</code>.
	 */
	public BlankOutputSegment(final Segment segment) {
		this(segment.getBegin(),segment.getEnd());
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	public void writeTo(final Writer writer) throws IOException {
		for (int i=begin; i<end; i++) writer.write(' ');
	}

	public long getEstimatedMaximumOutputLength() {
		return end-begin;
	}

	public String toString() {
		StringBuffer sb=new StringBuffer(end-begin);
		for (int i=begin; i<end; i++) sb.append(' ');
		return sb.toString();
	}

	public String getDebugInfo() {
		return "("+begin+','+end+')';
	}
}
