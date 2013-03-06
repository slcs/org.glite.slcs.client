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
import java.util.Comparator;

/**
 * Defines the interface for an output segment, which is used in an {@link OutputDocument} to
 * replace segments of the source document with other text.
 * <p>
 * All text in the <code>OutputDocument</code> between the character positions defined by {@link #getBegin()} and {@link #getEnd()}
 * is replaced by the content of this output segment.
 * If the begin and end character positions are the same, the content is simply
 * inserted at this position without replacing any text.
 *
 * @see OutputDocument#register(OutputSegment)
 */
public interface OutputSegment extends CharStreamSource {

	/**
	 * The comparator used to sort output segments in the {@link OutputDocument} before output.
	 * <p>
	 * The following rules are applied in order compare two output segments:
	 * <ol>
	 *  <li>The output segment that {@linkplain #getBegin() begins} earlier in the document comes first.
	 *  <li>If both output segments begin at the same position, the one that has zero-length comes first.
	 *  <li>If both output segments are zero-length, neither is guaranteed to come before the other.
	 *  <li>If neither segment is zero-length, the result is undefined as the segments are overlapping.
	 *   Note that this condition is detected at a later stage, so this comparator returns normally without throwing a
	 *   {@link OverlappingOutputSegmentsException}.
	 * </ol>
 	 * <p>
	 * Note: this comparator has a natural ordering that may be inconsistent with the <code>equals</code>
	 * method of classes implementing this interface.
	 * This means that the comparator may treat two output segments as equal where calling the
	 * <code>equals(Object)</code> method with the same two output segments returns <code>false</code>.
	 */
	public static final Comparator COMPARATOR=new OutputSegmentComparator();

	/**
	 * Returns the character position in the {@linkplain OutputDocument#getSourceText() source text of the output document} where this segment begins.
	 * @return the character position in the {@linkplain OutputDocument#getSourceText() source text of the output document} where this segment begins.
	 */
	public int getBegin();

	/**
	 * Returns the character position in the {@linkplain OutputDocument#getSourceText() source text of the output document} where this segment ends.
	 * @return the character position in the {@linkplain OutputDocument#getSourceText() source text of the output document} where this segment ends.
	 */
	public int getEnd();

	/**
	 * Writes the content of this output segment to the specified <code>Writer</code>.
	 * @param writer  the destination <code>java.io.Writer</code> for the output.
	 * @throws IOException if an I/O exception occurs.
	 */
	public void writeTo(Writer writer) throws IOException;

	/**
	 * Returns the content of this output segment as a <code>String</code>.
	 * <p>
	 * Note that before version 2.0 this returned a representation of this object useful for debugging purposes,
	 * which can now be obtained via the {@link #getDebugInfo() getDebugInfo()} method.
	 *
	 * @return the content of this output segment as a <code>String</code>, guaranteed not <code>null</code>.
	 * @see #writeTo(Writer)
	 */
	public String toString();

	/**
	 * Returns a string representation of this object useful for debugging purposes.
	 * @return a string representation of this object useful for debugging purposes.
	 */
	public String getDebugInfo();
}
