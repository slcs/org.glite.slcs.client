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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Implements an {@link OutputSegment} whose content is a list of attribute name/value pairs.
 * <p>
 * This output segment is designed to replace the original {@link Attributes} segment in the source,
 * providing a simple means of adding, modifying and removing attributes.
 * <p>
 * Each instance of this class contains a <code>java.util.Map</code> of name/value pairs which can either be
 * specified directly in the constructor or initialised to the same entries as the source {@link Attributes}
 * specified in the constructor.
 * This map can be accessed via the {@link #getMap()} method, and its entries modified as required before output.
 * <p>
 * Keys in the map must be <code>String</code> objects, and values must implement the <code>CharSequence</code> interface.
 * <p>
 * An attribute with no value is represented by a map entry with a <code>null</code> value.
 * <p>
 * Attribute values are stored unencoded in the map, and are automatically
 * {@linkplain CharacterReference#encode(CharSequence) encoded} if necessary during output.
 * <p>
 * The use of invalid characters in attribute names results in unspecified behaviour.
 * <p>
 * Note that methods in the <code>Attributes</code> class treat attribute names as case insensitive,
 * whereas the <code>Map</code> treats them as case sensitive.
 * <h4>Example of Usage:</h4>
 * <pre>
 *  Source source=new Source(htmlDocument);
 *  Attributes bodyAttributes
 *    =source.findNextStartTag(0,Tag.BODY).getAttributes();
 *  AttributesOutputSegment bodyAttributesOutputSegment
 *    =new AttributesOutputSegment(bodyAttributes,true);
 *  bodyAttributesOutputSegment.getMap().put("bgcolor","green");
 *  OutputDocument outputDocument=new OutputDocument(source);
 *  outputDocument.register(bodyAttributesOutputSegment);
 *  String htmlDocumentWithGreenBackground=outputDocument.toString();
 * </pre>
 * <p>
 * This class has been deprecated as of version 2.2 and the functionality replaced with the
 * {@link OutputDocument#replace(Attributes, Map)} and {@link OutputDocument#replace(Attributes, boolean convertNamesToLowerCase)} methods.
 *
 * @see OutputDocument
 * @see Attributes
 * @deprecated  Use the {@link OutputDocument#replace(Attributes, Map)} and {@link OutputDocument#replace(Attributes, boolean convertNamesToLowerCase)} methods instead.
 */
public class AttributesOutputSegment implements OutputSegment {
	private int begin;
	private int end;
	private Map map;

	/**
	 * Constructs a new <code>AttributesOutputSegment</code> with the same span and initial name/value entries as the specified source {@link Attributes}.
	 * <p>
	 * Specifying a value of <code>true</code> as an argument to the <code>convertNamesToLowerCase</code> parameter
	 * causes all attribute names to be converted to lower case in the map.
	 * This simplifies the process of finding/updating specific attributes since map keys are case sensitive.
	 * <p>
	 * Attribute values are automatically {@linkplain CharacterReference#decode(CharSequence) decoded} before
	 * being loaded into the map.
	 * <p>
	 * Calling this constructor with the following code:
	 * <div style="margin-left: 2em"><code>new AttributesOutputSegment(attributes, convertNamesToLowerCase)</code></div>
	 * is logically equivalent to calling:
	 * <div style="margin-left: 2em"><code>new AttributesOutputSegment(attributes, attributes.populateMap(new LinkedHashMap(), convertNamesToLowerCase))</code></div>
	 * <p>
	 * The use of <code>LinkedHashMap</code> to implement the map ensures (probably unnecessarily) that
	 * existing attributes are output in the same order as they appear in the source document, and new
	 * attributes are output in the same order as they are added.
	 *
	 * @param attributes  the <code>Attributes</code> defining the span and initial name/value entries of the new <code>AttributesOutputSegment</code>.
	 * @param convertNamesToLowerCase  specifies whether all attribute names are converted to lower case in the map.
	 * @see #AttributesOutputSegment(Attributes,Map)
	 */
	public AttributesOutputSegment(final Attributes attributes, final boolean convertNamesToLowerCase) {
		this(attributes,attributes.getMap(convertNamesToLowerCase));
	}

	/**
	 * Constructs a new <code>AttributesOutputSegment</code> with the same span
	 * as the specified source {@link Attributes}, using the specified <code>Map</code> to
	 * store the entries.
	 * <p>
	 * This constructor might be used if the <code>Map</code> containing the new attribute values
	 * should not be preloaded with the same entries as the source attributes, or a map implementation
	 * other than <code>LinkedHashMap</code> is required.
	 *
	 * @param attributes  the <code>Attributes</code> defining the span of the new <code>AttributesOutputSegment</code>.
	 * @param map  the <code>Map</code> containing the name/value entries.
	 * @see #AttributesOutputSegment(Attributes, boolean convertNamesToLowerCase)
	 */
	public AttributesOutputSegment(final Attributes attributes, final Map map) {
		if (map==null || attributes==null) throw new IllegalArgumentException("both arguments must be non-null");
		begin=attributes.getBegin();
		end=attributes.getEnd();
		this.map=map;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	/**
	 * Returns the <code>Map</code> containing the name/value entries to be output.
	 * @return the <code>Map</code> containing the name/value entries to be output.
	 */
	public Map getMap() {
		return map;
	}

	/**
	 * Writes the contents of the {@linkplain #getMap() map} as HTML attribute name/value pairs to the specified <code>Writer</code>.
	 * <p>
	 * Each attribute is preceded by a single space, and all values are
	 * {@linkplain CharacterReference#encode(CharSequence) encoded} and enclosed in double quotes.
	 *
	 * @param writer  the destination <code>java.io.Writer</code> for the output.
	 * @throws IOException if an I/O exception occurs.
	 * @see Attributes#generateHTML(Map attributesMap)
	 */
	public void writeTo(final Writer writer) throws IOException {
		Attributes.appendHTML(writer,map);
	}

	public long getEstimatedMaximumOutputLength() {
		return (end-begin)*2;
	}

	public String toString() {
		return Attributes.generateHTML(map);
	}

	public String getDebugInfo() {
		StringWriter stringWriter=new StringWriter();
		stringWriter.getBuffer().append('(').append(begin).append(',').append(end).append("):");
		try {output(stringWriter);} catch (IOException ex) {} // IOException never occurs in StringWriter
		return stringWriter.toString();
	}

	/**
	 * Outputs the contents of the {@linkplain #getMap() map} as HTML attribute name/value pairs to the specified <code>Writer</code>.
	 *
	 * @param writer  the destination <code>java.io.Writer</code> for the output.
	 * @throws IOException if an I/O exception occurs.
	 * @deprecated  Use {@link #writeTo(Writer)} instead.
	 */
	public void output(final Writer writer) throws IOException {
		writeTo(writer);
	}
}
