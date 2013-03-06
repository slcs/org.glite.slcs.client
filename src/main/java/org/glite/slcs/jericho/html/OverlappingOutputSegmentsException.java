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

import java.io.Writer;

/**
 * Signals that overlapping {@linkplain OutputSegment output segments} have been detected in the {@link OutputDocument}.
 * <p>
 * This exception is only thrown when an attempt is made to {@linkplain OutputDocument#writeTo(Writer) generate the output}
 * of the <code>OutputDocument</code>.
 *
 * @see OutputDocument#toString()
 * @see OutputDocument#writeTo(Writer)
 */
public class OverlappingOutputSegmentsException extends RuntimeException {
	private OutputSegment[] overlappingOutputSegments=new OutputSegment[2];

	OverlappingOutputSegmentsException(final OutputSegment outputSegment1, final OutputSegment outputSegment2) {
		super("Overlapping output segments detected in output document:\n"+outputSegment1.getDebugInfo()+'\n'+outputSegment2.getDebugInfo());
		overlappingOutputSegments[0]=outputSegment1;
		overlappingOutputSegments[1]=outputSegment2;
	}

	/**
	 * Returns the two overlapping output segments in an array.
	 * <p>
	 * Only the first two detected overlapping segments are returned,
	 * even if other overlapping segments were added to the {@link OutputDocument}.
	 *
	 * @return the two overlapping output segments in an array.
	 */
	public OutputSegment[] getOverlappingOutputSegments() {
		return overlappingOutputSegments;
	}
}

