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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Contains static utility methods for manipulating the way data is retrieved from a {@link CharStreamSource} object.
 * <p>
 * See the documentation of the {@link CharStreamSource} class for details.
 */
public final class CharStreamSourceUtil {
	private static final int DEFAULT_ESTIMATED_MAXIMUM_OUTPUT_LENGTH=2048;

	private CharStreamSourceUtil() {}

	/**
	 * Returns a <code>Reader</code> that reads the output of the specified {@link CharStreamSource}.
	 * <p>
	 * The current implementation of this method simply returns <code>new StringReader(</code>{@link #toString(CharStreamSource) toString(charStreamSource)}<code>)</code>,
	 * but a future version may implement this method in a more memory efficient manner.
	 *
	 * @param charStreamSource  the character stream source producing the output.
	 * @return a <code>Reader</code> that reads the output of the specified {@link CharStreamSource}.
	 */
	public static Reader getReader(final CharStreamSource charStreamSource) {
		return new StringReader(toString(charStreamSource));
	}

	/**
	 * Returns the output of the specified {@link CharStreamSource} as a string.
	 * <p>
	 * The current implementation of this method simply returns <code>new StringReader(</code>{@link #toString(CharStreamSource) toString(charStreamSource)}<code>)</code>,
	 * but a future version may implement this method in a more memory efficient manner.
	 *
	 * @param charStreamSource  the character stream source producing the output.
	 * @return the output of the specified {@link CharStreamSource} as a string.
	 */
	public static String toString(final CharStreamSource charStreamSource) {
		long estimatedMaximumOutputLength=charStreamSource.getEstimatedMaximumOutputLength();
		if (estimatedMaximumOutputLength==-1L) estimatedMaximumOutputLength=DEFAULT_ESTIMATED_MAXIMUM_OUTPUT_LENGTH;
		final StringWriter writer=new StringWriter((int)(estimatedMaximumOutputLength));
		try {
			charStreamSource.writeTo(writer);
		} catch (IOException ex) {throw new RuntimeException(ex);} // assume the IOException is not thrown explicitly by the charStreamSource.output method
		return writer.toString();
	}
}
