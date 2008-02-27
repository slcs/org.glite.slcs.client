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

import java.io.IOException;
import java.io.Writer;

/**
 * Implements an {@link OutputSegment} whose content is a <code>CharSequence</code>.
 * <p>
 * This class has been deprecated as of version 2.2 and the functionality replaced with the
 * {@link OutputDocument#replace(Segment, CharSequence text)} method.
 *
 * @deprecated  Use the {@link OutputDocument#replace(Segment, CharSequence text)} method instead.
 */
public final class StringOutputSegment implements OutputSegment {
	private int begin;
	private int end;
	private CharSequence text;

	/**
	 * Constructs a new <code>StringOutputSegment</code> with the specified begin and end positions and the specified content.
	 * <p>
	 * Specifying a <code>null</code> argument to the <code>text</code> parameter is exactly equivalent to specifying an empty string,
	 * and results in the segment being completely removed from the output document.
	 *
	 * @param begin  the position in the <code>OutputDocument</code> where this output segment begins.
	 * @param end  the position in the <code>OutputDocument</code> where this output segment ends.
	 * @param text  the textual content of the new output segment, or <code>null</code> if no content.
	 */
	public StringOutputSegment(final int begin, final int end, final CharSequence text) {
		this.begin=begin;
		this.end=end;
		this.text=(text==null ? "" : text);
	}

	/**
	 * Constructs a new StringOutputSegment</code> with the same span as the specified {@link Segment}.
	 * <p>
	 * Specifying a <code>null</code> argument to the <code>text</code> parameter is exactly equivalent to specifying an empty string,
	 * and results in the segment being completely removed from the output document.
	 *
	 * @param segment  a segment defining the beginning and ending positions of the new output segment.
	 * @param text  the textual content of the new output segment, or <code>null</code> if no content.
	 */
	public StringOutputSegment(final Segment segment, final CharSequence text) {
		this(segment.begin,segment.end,text);
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	public void writeTo(final Writer writer) throws IOException {
		Util.appendTo(writer,text);
	}

	public long getEstimatedMaximumOutputLength() {
		return text.length();
	}

	public String toString() {
		return text.toString();
	}

	public String getDebugInfo() {
		return "("+begin+','+end+"):\""+text+'"';
	}

	public void output(final Writer writer) throws IOException {
		writeTo(writer);
	}
}
