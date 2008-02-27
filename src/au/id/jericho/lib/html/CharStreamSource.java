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
 * Represents a character stream source.
 * <p>
 * The main purpose of a class that implements this interface is to output a stream of characters.
 * By implementing this interface, the "active" stream source can easily be converted into a "passive" stream source if required by the class needing to consume the data.
 * <p>
 * An <i><a name="Active">active stream source</a></i> is a stream source that actively outputs to a passive receiver ("sink").
 * The {@link #writeTo(Writer)} method in this interface signifies an active source as the transmission of the entire data stream takes place when this method is executed.
 * In this case the sink is the object that supplies the <code>Writer</code> object, and would typically contain a <code>getWriter()</code> method.
 * The sink is passive because it just supplies a <code>Writer</code> object to be written to by the code in some other class.
 * <p>
 * A <i><a name="Passive">passive stream source</a></i> is a stream source that is read from by an active sink.
 * For character streams, a passive stream source simply supplies a <code>Reader</code> object.
 * The active sink would typically contain a <code>readFrom(Reader)</code> method which actively reads the entire data stream from the <code>Reader</code> object.
 * <p>
 * The {@link CharStreamSourceUtil#getReader(CharStreamSource)} method coverts a <code>CharStreamSource</code> into a <code>Reader</code>,
 * allowing the data from the active <code>CharStreamSource</code> to be consumed by an active sink with a <code>readFrom(Reader)</code> method.
 * <p>
 * The {@link CharStreamSourceUtil#toString(CharStreamSource)} method converts a <code>CharStreamSource</code> into a <code>String</code>.
 * Every class implementing <code>CharStreamSource</code> should include a <code>toString()</code> method that calls
 * {@link CharStreamSourceUtil#toString(CharStreamSource) CharStreamSourceUtil.toString(this)}, so that the data can be obtained as a string simply by
 * calling <code>charStreamSource.toString()</code>.
 * <p>
 * @see OutputDocument
 * @see Source#indent(String,boolean,boolean,boolean) Source.indent
 */
public interface CharStreamSource {
	/**
	 * Writes the output to the specified <code>Writer</code>.
	 *
	 * @param writer  the destination <code>java.io.Writer</code> for the output.
	 * @throws IOException if an I/O exception occurs.
	 */
	void writeTo(Writer writer) throws IOException;

	/**
	 * Returns the estimated maximum number of characters in the output, or <code>-1</code> if no estimate is available.
	 * <p>
	 * The returned value should be used as a guide for efficiency purposes only, for example to set an initial <code>StringBuffer</code> capacity.
	 * There is no guarantee that the length of the output is indeed less than this value,
	 * as classes implementing this method often use assumptions based on typical usage to calculate the estimate.
	 *
	 * @return the estimated maximum number of characters in the output, or <code>-1</code> if no estimate is available.
	 */
	long getEstimatedMaximumOutputLength();
}
