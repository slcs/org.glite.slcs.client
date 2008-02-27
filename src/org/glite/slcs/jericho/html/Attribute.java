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
 * Represents a single <a target="_blank" href="http://www.w3.org/TR/html401/intro/sgmltut.html#h-3.2.2">attribute</a>
 * name/value segment within a {@link StartTag}.
 * <p>
 * An instance of this class is a representation of a single attribute in the source document and is not modifiable.
 * The {@link AttributesOutputSegment} class provides the means to add, delete or modify attributes and
 * their values for inclusion in an {@link OutputDocument}.
 * <p>
 * Obtained using the {@link Attributes#get(String key)} method.
 * <p>
 * See also the XML 1.0 specification for <a target="_blank" href="http://www.w3.org/TR/REC-xml#dt-attr">attributes</a>.
 *
 * @see Attributes
 */
public final class Attribute extends Segment {
	private final String key;
	private final Segment nameSegment;
	private final Segment valueSegment;
	private final Segment valueSegmentIncludingQuotes;

	static final String CHECKED="checked";
	static final String CLASS="class";
	static final String DISABLED="disabled";
	static final String ID="id";
	static final String MULTIPLE="multiple";
	static final String NAME="name";
	static final String SELECTED="selected";
	static final String STYLE="style";
	static final String TYPE="type";
	static final String VALUE="value";

	/**
	 * Constructs a new Attribute with no value part, called from Attributes class.
	 * <p>
	 * Note that the resulting Attribute segment has the same span as the supplied nameSegment.
	 *
	 * @param source  the {@link Source} document.
	 * @param key  the name of this attribute in lower case.
	 * @param nameSegment  the segment representing the name.
	 */
	Attribute(final Source source, final String key, final Segment nameSegment) {
		this(source,key,nameSegment,null,null);
	}

	/**
	 * Constructs a new Attribute, called from Attributes class.
	 * <p>
	 * The resulting Attribute segment begins at the start of the nameSegment
	 * and finishes at the end of the valueSegmentIncludingQuotes.  If this attribute
	 * has no value, it finishes at the end of the nameSegment.
	 * <p>
	 * If this attribute has no value, the <code>valueSegment</code> and <code>valueSegmentIncludingQuotes</code> must be null.
	 * The <valueSegmentIncludingQuotes</code> parameter must not be null if the <code>valueSegment</code> is not null, and vice versa
	 *
	 * @param source  the {@link Source} document.
	 * @param key  the name of this attribute in lower case.
	 * @param nameSegment  the segment spanning the name.
	 * @param valueSegment  the segment spanning the value.
	 * @param valueSegmentIncludingQuotes  the segment spanning the value, including quotation marks if any.
	 */
	Attribute(final Source source, final String key, final Segment nameSegment, final Segment valueSegment, final Segment valueSegmentIncludingQuotes) {
		super(source,nameSegment.getBegin(),(valueSegmentIncludingQuotes==null ? nameSegment.getEnd() : valueSegmentIncludingQuotes.getEnd()));
		this.key=key;
		this.nameSegment=nameSegment;
		this.valueSegment=valueSegment;
		this.valueSegmentIncludingQuotes=valueSegmentIncludingQuotes;
	}

	/**
	 * Returns the name of this attribute in lower case.
	 * <p>
	 * This package treats all attribute names as case insensitive, consistent with
	 * <a target="_blank" href="http://www.w3.org/TR/html401/">HTML</a> but not consistent with
	 * <a target="_blank" href="http://www.w3.org/TR/xhtml1/">XHTML</a>.
	 *
	 * @return the name of this attribute in lower case.
	 * @see #getName()
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Returns the name of this attribute in original case.
	 * <p>
	 * This is exactly equivalent to {@link #getNameSegment()}<code>.toString()</code>.
	 *
	 * @return the name of this attribute in original case.
	 * @see #getKey()
	 */
	public String getName() {
		return nameSegment.toString();
	}

	/**
	 * Returns the segment spanning the {@linkplain #getName() name} of this attribute.
	 * @return the segment spanning the {@linkplain #getName() name} of this attribute.
	 * @see #getName()
	 */
	public Segment getNameSegment() {
		return nameSegment;
	}

	/**
	 * Indicates whether this attribute has a value.
	 * <p>
	 * This method also returns <code>true</code> if this attribute has been assigned a zero-length value.
	 * <p>
	 * It only returns <code>false</code> if this attribute appears in
	 * <a target="_blank" href="http://www.w3.org/TR/html401/intro/sgmltut.html#didx-boolean_attribute-1">minimized form</a>.
	 *
	 * @return <code>true</code> if this attribute has a value, otherwise <code>false</code>.
	 */
	public boolean hasValue() {
		return valueSegment!=null;
	}

	/**
	 * Returns the {@linkplain CharacterReference#decode(CharSequence,boolean) decoded} value of this attribute,
	 * or <code>null</code> if it {@linkplain #hasValue() has no value}.
	 * <p>
	 * This is equivalent to {@link CharacterReference}<code>.</code>{@link CharacterReference#decode(CharSequence,boolean) decode}<code>(</code>{@link #getValueSegment()}<code>,true)</code>.
	 * <p>
	 * Note that before version 1.4.1 this method returned the raw value of the attribute as it appears in the source document,
	 * without {@linkplain CharacterReference#decode(CharSequence,boolean) decoding}.
	 * <p>
	 * To obtain the raw value without decoding, use {@link #getValueSegment()}<code>.toString()</code>.
	 * <p>
	 * Special attention should be given to attributes that contain URLs, such as the
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/struct/links.html#adef-href">href</a></code> attribute.
	 * When such an attribute contains a URL with parameters (as described in the
	 * <a target="_blank" href="http://www.w3.org/MarkUp/html-spec/html-spec_8.html#SEC8.2.1">form-urlencoded media type</a>),
	 * the ampersand (<code>&amp;</code>) characters used to separate the parameters should be
	 * {@linkplain CharacterReference#encode(CharSequence) encoded} to prevent the parameter names from being
	 * unintentionally interpreted as {@linkplain CharacterEntityReference character entity references}.
	 * This requirement is explicitly stated in the 
	 * <a target="_blank" href="http://www.w3.org/TR/html401/charset.html#h-5.3.2">HTML 4.01 specification section 5.3.2</a>.
	 * <p>
	 * For example, take the following element in the source document:
	 * <div style="margin: 0.5em"><code>&lt;a href="Report.jsp?chapt=2&amp;sect=3"&gt;next&lt;/a&gt;</code></div>
	 * By default, calling 
	 * {@link Element#getAttributes() getAttributes()}<code>.</code>{@link Attributes#getValue(String) getValue}<code>("href")</code>
	 * on this element returns the string
	 * "<code>Report.jsp?chapt=2&sect;=3</code>", since the text "<code>&amp;sect</code>" is interpreted as the rarely used
	 * character entity reference {@link CharacterEntityReference#_sect &amp;sect;} (U+00A7), despite the fact that it is
	 * missing the {@linkplain CharacterReference#isTerminated() terminating semicolon} (<code>;</code>).
	 * <p>
	 * Most browsers recognise <a href="CharacterReference.html#Unterminated">unterminated</a> character entity references
	 * in attribute values representing a codepoint of U+00FF or below, but ignore those representing codepoints above this value.
 	 * One relatively popular browser only recognises those representing a codepoint of U+003E or below, meaning it would
 	 * have interpreted the URL in the above example differently to most other browsers.
	 * Most browsers also use different rules depending on whether the unterminated character reference is inside or outside
	 * of an attribute value, with both of these possibilities further split into different rules for
	 * {@linkplain CharacterEntityReference character entity references},
	 * <a href="NumericCharacterReference.html#DecimalCharacterReference">decimal character references</a>, and
	 * <a href="NumericCharacterReference.html#HexadecimalCharacterReference">hexadecimal character references</a>.
	 * <p>
	 * The behaviour of this library is determined by the current {@linkplain Config.CompatibilityMode compatibility mode} setting,
	 * which is determined by the {@link Config#CurrentCompatibilityMode} property.
	 *
	 * @return the {@linkplain CharacterReference#decode(CharSequence,boolean) decoded} value of this attribute, or <code>null</code> if it {@linkplain #hasValue() has no value}.
	 */
	public String getValue() {
		return CharacterReference.decode(valueSegment,true);
	}

	/**
	 * Returns the segment spanning the {@linkplain #getValue() value} of this attribute, or <code>null</code> if it {@linkplain #hasValue() has no value}.
	 * @return the segment spanning the {@linkplain #getValue() value} of this attribute, or <code>null</code> if it {@linkplain #hasValue() has no value}.
	 * @see #getValue()
	 */
	public Segment getValueSegment() {
		return valueSegment;
	}

	/**
	 * Returns the segment spanning the {@linkplain #getValue() value} of this attribute, including quotation marks if any,
	 * or <code>null</code> if it {@linkplain #hasValue() has no value}.
	 * <p>
	 * If the value is not enclosed by quotation marks, this is the same as the {@linkplain #getValueSegment() value segment}
	 *
	 * @return the segment spanning the {@linkplain #getValue() value} of this attribute, including quotation marks if any, or <code>null</code> if it {@linkplain #hasValue() has no value}.
	 */
	public Segment getValueSegmentIncludingQuotes() {
		return valueSegmentIncludingQuotes;
	}

	/**
	 * Returns the character used to quote the value.
	 * <p>
	 * The return value is either a double-quote (<code>"</code>), a single-quote (<code>'</code>), or a space.
	 *
	 * @return the character used to quote the value, or a space if the value is not quoted or this attribute has no value.
	 */
	public char getQuoteChar() {
		if (valueSegment==valueSegmentIncludingQuotes) return ' '; // no quotes
		return source.charAt(valueSegmentIncludingQuotes.getBegin());
	}

	/**
	 * Returns a string representation of this object useful for debugging purposes.
	 * @return a string representation of this object useful for debugging purposes.
	 */
	public String getDebugInfo() {
		final StringBuffer sb=new StringBuffer().append(key).append(super.getDebugInfo()).append(",name=").append(nameSegment.getDebugInfo());
		if (hasValue())
			sb.append(",value=").append(valueSegment.getDebugInfo()).append('"').append(valueSegment).append("\"\n");
		else
			sb.append(",NO VALUE\n");
		return sb.toString();
	}

	Tag appendTidy(final StringBuffer sb, Tag nextTag) {
		sb.append(' ');
		Util.appendTo(sb,nameSegment);
		if (valueSegment!=null) {
			sb.append("=\"");
			while (nextTag!=null && nextTag.begin<valueSegment.begin) nextTag=nextTag.findNextTag();
			if (nextTag==null || nextTag.begin>=valueSegment.end) {
				appendTidyValue(sb,valueSegment);
			} else {
				int i=valueSegment.begin;
				while (nextTag!=null && nextTag.begin<valueSegment.end) {
					appendTidyValue(sb,new Segment(source,i,nextTag.begin));
					if (nextTag.end>valueSegment.end) {
						sb.append(new Segment(source,nextTag.begin,i=valueSegment.end));
						break;
					}
					sb.append(nextTag);
					i=nextTag.end;
					nextTag=nextTag.findNextTag();
				}
				if (i<valueSegment.end) appendTidyValue(sb,new Segment(source,i,valueSegment.end));
			}
			sb.append('"');
		}
		return nextTag;
	}

	private static void appendTidyValue(final StringBuffer sb, final CharSequence unencodedValue) {
		CharacterReference.appendEncode(sb,CharacterReference.decode(unencodedValue,true),false);
	}

	static StringBuffer appendHTML(final StringBuffer sb, final CharSequence name, final CharSequence value) {
		sb.append(' ');
		Util.appendTo(sb,name);
		if (value!=null) {
			sb.append("=\"");
			CharacterReference.appendEncode(sb,value,false);
			sb.append('"');
		}
		return sb;
	}

	static Writer appendHTML(final Writer writer, final CharSequence name, final CharSequence value) throws IOException {
		writer.write(' ');
		Util.appendTo(writer,name);
		if (value!=null) {
			writer.write("=\"");
			writer.write(CharacterReference.encode(value));
			writer.write('"');
		}
		return writer;
	}
}
