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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import au.id.jericho.lib.html.nodoc.SequentialListSegment;

/**
 * Represents the list of {@link Attribute} objects present within a particular {@link StartTag}.
 * <p>
 * This segment starts at the end of the start tag's {@linkplain StartTag#getName() name}
 * and ends at the end of the last attribute.
 * <p>
 * The attributes in this list are a representation of those found in the source document and are not modifiable.
 * The {@link AttributesOutputSegment} class provides the means to add, delete or modify attributes and
 * their values for inclusion in an {@link OutputDocument}.
 * <p>
 * If too many syntax errors are encountered while parsing a start tag's attributes, the parser rejects the entire start tag
 * and generates a {@linkplain Source#setLogWriter(Writer) log} entry.
 * The threshold for the number of errors allowed can be set using the {@link #setDefaultMaxErrorCount(int)} static method.
 * <p>
 * Obtained using the {@link StartTag#getAttributes()} method, or explicitly using the {@link Source#parseAttributes(int pos, int maxEnd)} method.
 * <p>
 * It is common for instances of this class to contain no attributes.
 * <p>
 * See also the XML 1.0 specification for <a target="_blank" href="http://www.w3.org/TR/REC-xml#dt-attr">attributes</a>.
 * <p>
 * Note that before version 2.0 the segment ended just before the tag's
 * {@linkplain StartTagType#getClosingDelimiter() closing delimiter} instead of at the end of the last attribute.
 *
 * @see StartTag
 * @see Attribute
 */
public final class Attributes extends SequentialListSegment {
	private final LinkedList attributeList; // never null

	// parsing states:
	private static final int AFTER_TAG_NAME=0;
	private static final int BETWEEN_ATTRIBUTES=1;
	private static final int IN_NAME=2;
	private static final int AFTER_NAME=3; // this only happens if an attribute name is followed by whitespace
	private static final int START_VALUE=4;
	private static final int IN_VALUE=5;
	private static final int AFTER_VALUE_FINAL_QUOTE=6;

	private static int defaultMaxErrorCount=2; // defines maximum number of minor errors that can be encountered in attributes before entire start tag is rejected.

	private Attributes(final Source source, final int begin, final int end, final LinkedList attributeList) {
		super(source,begin,end);
		this.attributeList=attributeList;
	}

	/** called from StartTagType.parseAttributes(Source, int startTagBegin, String tagName) */
	static Attributes construct(final Source source, final int startTagBegin, final StartTagType startTagType, final String tagName) {
		return construct(source,"StartTag",AFTER_TAG_NAME,startTagBegin,-1,-1,startTagType,tagName,defaultMaxErrorCount);
	}

	/** called from StartTag.parseAttributes(int maxErrorCount) */
	static Attributes construct(final Source source, final int startTagBegin, final int attributesBegin, final int maxEnd, final StartTagType startTagType, final String tagName, final int maxErrorCount) {
		return construct(source,"Attributes for StartTag",BETWEEN_ATTRIBUTES,startTagBegin,attributesBegin,maxEnd,startTagType,tagName,maxErrorCount);
	}

	/** called from Source.parseAttributes(int pos, int maxEnd, int maxErrorCount) */
	static Attributes construct(final Source source, final int begin, final int maxEnd, final int maxErrorCount) {
		return construct(source,"Attributes",BETWEEN_ATTRIBUTES,begin,-1,maxEnd,StartTagType.NORMAL,null,maxErrorCount);
	}

	/**
	 * Any &lt; character found within the start tag is treated as though it is part of the attribute
	 * list, which is consistent with the way IE treats it.
	 * In some cases an invalid character results in the entire start tag being rejected.
	 * This may seem strict, but we have to be able to distinguish whether any
	 * particular < found in the source is actually the start of a tag or not.
	 * Being too lenient with attributes means more chance of false positives, which in turn
	 * means surrounding tags may be ignored.
	 * @param logBegin  the position of the beginning of the object being searched (for logging)
	 * @param attributesBegin  the position of the beginning of the attribute list, or -1 if it should be calculated automatically from logBegin.
	 * @param maxEnd  the position at which the attributes must end if a terminating character is not found, or -1 if no maximum.
	 * @param tagName  the name of the enclosing StartTag, or null if constucting attributes directly.
	 */
	private static Attributes construct(final Source source, final String logType, int state, final int logBegin, int attributesBegin, final int maxEnd, final StartTagType startTagType, final String tagName, final int maxErrorCount) {
		boolean isClosingSlashIgnored=false;
		if (tagName!=null) {
			// 'logBegin' parameter is the start of the associated start tag
			if (attributesBegin==-1) attributesBegin=logBegin+1+tagName.length();
			if (startTagType==StartTagType.NORMAL && HTMLElements.isClosingSlashIgnored(tagName)) isClosingSlashIgnored=true;
		} else {
			attributesBegin=logBegin;
		}
		int attributesEnd=attributesBegin;
		final LinkedList attributeList=new LinkedList();
		final ParseText parseText=source.getParseText();
		int i=attributesBegin;
		char quote=' ';
		Segment nameSegment=null;
		String key=null;
		int currentBegin=-1;
		boolean isTerminatingCharacter=false;
		int errorCount=0;
		try {
			while (!isTerminatingCharacter) {
				if (i==maxEnd || startTagType.atEndOfAttributes(source,i,isClosingSlashIgnored)) isTerminatingCharacter=true;
				final char ch=parseText.charAt(i);
				switch (state) {
					case IN_VALUE:
						if (isTerminatingCharacter || ch==quote || (quote==' ' && isWhiteSpace(ch))) {
							Segment valueSegment;
							Segment valueSegmentIncludingQuotes;
							if (quote==' ') {
								valueSegment=valueSegmentIncludingQuotes=new Segment(source,currentBegin,i);
							} else {
								if (isTerminatingCharacter) {
									if (i==maxEnd) {
										if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"terminated in the middle of a quoted attribute value",i);
										if (reachedMaxErrorCount(++errorCount,source,logType,tagName,logBegin,maxErrorCount)) return null;
										valueSegment=new Segment(source,currentBegin,i);
										valueSegmentIncludingQuotes=new Segment(source,currentBegin-1,i); // this is missing the end quote
									} else {
										// don't want to terminate, only encountered a terminating character in the middle of a quoted value
										isTerminatingCharacter=false;
										break;
									}
								} else {
									valueSegment=new Segment(source,currentBegin,i);
									valueSegmentIncludingQuotes=new Segment(source,currentBegin-1,i+1);
								}
							}
							attributeList.add(new Attribute(source, key, nameSegment, valueSegment, valueSegmentIncludingQuotes));
							attributesEnd=valueSegmentIncludingQuotes.getEnd();
							state=BETWEEN_ATTRIBUTES;
						} else if (ch=='<' && quote==' ') {
							if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"rejected because of '<' character in unquoted attribute value",i);
							return null;
						}
						break;
					case IN_NAME:
						if (isTerminatingCharacter || ch=='=' || isWhiteSpace(ch)) {
							nameSegment=new Segment(source,currentBegin,i);
							key=nameSegment.toString().toLowerCase();
							if (isTerminatingCharacter) {
								attributeList.add(new Attribute(source,key,nameSegment)); // attribute with no value
								attributesEnd=i;
							} else {
								state=(ch=='=' ? START_VALUE : AFTER_NAME);
							}
						} else if (!Tag.isXMLNameChar(ch)) {
							// invalid character detected in attribute name.
							// only reject whole start tag if it is a < character or if the error count is exceeded.
							if (ch=='<') {
								if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"rejected because of '<' character in attribute name",i);
								return null;
							}
							if (isInvalidEmptyElementTag(startTagType,source,i,logType,tagName,logBegin)) break;
							if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"contains attribute name with invalid character",i);
							if (reachedMaxErrorCount(++errorCount,source,logType,tagName,logBegin,maxErrorCount)) return null;
						}
						break;
					case AFTER_NAME:
						// attribute name has been followed by whitespace, but may still be followed by an '=' character.
						if (isTerminatingCharacter || !(ch=='=' || isWhiteSpace(ch))) {
							attributeList.add(new Attribute(source,key,nameSegment)); // attribute with no value
							attributesEnd=nameSegment.getEnd();
							if (isTerminatingCharacter) break;
							// The current character is the first character of an attribute name
							state=BETWEEN_ATTRIBUTES;
							i--; // want to reparse the same character again, so decrement i.  Note we could instead just fall into the next case statement without a break, but such code is always discouraged.
						} else if (ch=='=') {
							state=START_VALUE;
						}
						break;
					case BETWEEN_ATTRIBUTES:
						if (!isTerminatingCharacter) {
							// the quote variable is used here to make sure whitespace has come after the last quoted attribute value
							if (isWhiteSpace(ch)) {
								quote=' ';
							} else {
								if (quote!=' ') {
									if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"has missing whitespace after quoted attribute value",i);
									// log this as an error but don't count it
								}
								if (!Tag.isXMLNameStartChar(ch)) {
									// invalid character detected as first character of attribute name.
									// only reject whole start tag if it is a < character or if the error count is exceeded.
									if (ch=='<') {
										if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"rejected because of '<' character",i);
										return null;
									}
									if (isInvalidEmptyElementTag(startTagType,source,i,logType,tagName,logBegin)) break;
									if (startTagType==StartTagType.NORMAL && startTagType.atEndOfAttributes(source,i,false)) {
										// This checks whether we've found the characters "/>" but it wasn't recognised as the closing delimiter because isClosingSlashIgnored is true.
										if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"contains a '/' character before the closing '>', which is ignored because tags of this name cannot be empty-element tags");
										break;
									}
									if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"contains attribute name with invalid first character",i);
									if (reachedMaxErrorCount(++errorCount,source,logType,tagName,logBegin,maxErrorCount)) return null;
								}
								state=IN_NAME;
								currentBegin=i;
							}
						}
						break;
					case START_VALUE:
						currentBegin=i;
						if (isTerminatingCharacter) {
							if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"has missing attribute value after '=' sign",i);
							// log this as an error but don't count it
							final Segment valueSegment=new Segment(source,i,i);
							attributeList.add(new Attribute(source,key,nameSegment,valueSegment,valueSegment));
							attributesEnd=i;
							state=BETWEEN_ATTRIBUTES;
							break;
						}
						if (isWhiteSpace(ch)) break; // just ignore whitespace after the '=' sign as nearly all browsers do.
						if (ch=='<') {
							if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"rejected because of '<' character at start of attribuite value",i);
							return null;
						} else if (ch=='\'' || ch=='"') {
							quote=ch;
							currentBegin++;
						} else {
							quote=' ';
						}
						state=IN_VALUE;
						break;
					case AFTER_TAG_NAME:
						if (!isTerminatingCharacter) {
							if (!isWhiteSpace(ch)) {
								if (isInvalidEmptyElementTag(startTagType,source,i,logType,tagName,logBegin)) break;
								if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"rejected because name contains invalid character",i);
								return null;
							}
							state=BETWEEN_ATTRIBUTES;
						}
						break;
				}
				i++;
			}
			return new Attributes(source,attributesBegin,attributesEnd,attributeList); // used to end at i-1
		} catch (IndexOutOfBoundsException ex) {
			if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"rejected because it has no closing '>' character");
			return null;
		}
	}

	private static boolean reachedMaxErrorCount(final int errorCount, final Source source, final String logType, final String tagName, final int logBegin, final int maxErrorCount) {
		if (errorCount<=maxErrorCount) return false;
		if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"rejected because it contains too many errors");
		return true;
	}

	private static boolean isInvalidEmptyElementTag(final StartTagType startTagType, final Source source, final int i, final String logType, final String tagName, final int logBegin) {
		// This checks whether we've found the characters "/>" but it wasn't recognised as the closing delimiter because isClosingSlashIgnored is true.
		if (startTagType!=StartTagType.NORMAL || !startTagType.atEndOfAttributes(source,i,false)) return false;
		if (source.isLoggingEnabled()) log(source,logType,tagName,logBegin,"contains a '/' character before the closing '>', which is ignored because tags of this name cannot be empty-element tags");
		return true;
	}

	/**
	 * Returns the {@link Attribute} with the specified name (case insensitive).
	 * <p>
	 * If more than one attribute exists with the specified name (which is illegal HTML),
	 * the first is returned.
	 *
	 * @param name  the name of the attribute to get.
	 * @return the attribute with the specified name, or <code>null</code> if no attribute with the specified name exists.
	 * @see #getValue(String name)
	 */
	public Attribute get(final String name) {
		if (size()==0) return null;
		for (int i=0; i<size(); i++) {
			final Attribute attribute=(Attribute)get(i);
			if (attribute.getKey().equalsIgnoreCase(name)) return attribute;
		}
		return null;
	}

	/**
	 * Returns the {@linkplain CharacterReference#decode(CharSequence) decoded} value of the attribute with the specified name (case insensitive).
	 * <p>
	 * Returns <code>null</code> if no attribute with the specified name exists or
	 * the attribute {@linkplain Attribute#hasValue() has no value}.
	 * <p>
	 * This is equivalent to {@link #get(String) get(name)}<code>.</code>{@link Attribute#getValue() getValue()},
	 * except that it returns <code>null</code> if no attribute with the specified name exists instead of throwing a
	 * <code>NullPointerException</code>.
	 *
	 * @param name  the name of the attribute to get.
	 * @return the {@linkplain CharacterReference#decode(CharSequence) decoded} value of the attribute with the specified name, or <code>null</code> if the attribute does not exist or {@linkplain Attribute#hasValue() has no value}.
	 * @see Attribute#getValue()
	 */
	public String getValue(final String name) {
		final Attribute attribute=get(name);
		return attribute==null ? null : attribute.getValue();
	}

	/**
	 * Returns the raw (not {@linkplain CharacterReference#decode(CharSequence) decoded}) value of the attribute, or null if the attribute {@linkplain Attribute#hasValue() has no value}.
	 * <p>
	 * This is an internal convenience method.
	 *
	 * @return the raw (not {@linkplain CharacterReference#decode(CharSequence) decoded}) value of the attribute, or null if the attribute {@linkplain Attribute#hasValue() has no value}.
	 */
	String getRawValue(final String name) {
		final Attribute attribute=get(name);
		return attribute==null || !attribute.hasValue() ? null : attribute.getValueSegment().toString();
	}

	/**
	 * Returns the number of attributes.
	 * <p>
	 * This is equivalent to calling the <code>size()</code> method specified in the <code>List</code> interface.
	 *
	 * @return the number of attributes.
	 */
	public int getCount() {
		return attributeList.size();
	}

	/**
	 * Returns an iterator over the {@link Attribute} objects in this list in order of appearance.
	 * @return an iterator over the {@link Attribute} objects in this list in order of appearance.
	 */
	public Iterator iterator() {
		return listIterator();
	}

	/**
	 * Returns a list iterator of the {@link Attribute} objects in this list in order of appearance,
	 * starting at the specified position in the list.
	 * <p>
	 * The specified index indicates the first item that would be returned by an initial call to the <code>next()</code> method.
	 * An initial call to the <code>previous()</code> method would return the item with the specified index minus one.
	 * <p>
	 * IMPLEMENTATION NOTE: For efficiency reasons this method does not return an immutable list iterator.
	 * Calling any of the <code>add(Object)</code>, <code>remove()</code> or <code>set(Object)</code> methods on the returned
	 * <code>ListIterator</code> does not throw an exception but could result in unexpected behaviour.
	 *
	 * @param index  the index of the first item to be returned from the list iterator (by a call to the <code>next()</code> method).
	 * @return a list iterator of the items in this list (in proper sequence), starting at the specified position in the list.
	 * @throws IndexOutOfBoundsException if the specified index is out of range (<code>index &lt; 0 || index &gt; size()</code>).
	 */
	public ListIterator listIterator(final int index) {
		return attributeList.listIterator(index);
	}

	/**
	 * Populates the specified <code>Map</code> with the name/value pairs from these attributes.
	 * <p>
	 * Both names and values are stored as <code>String</code> objects.
	 * <p>
	 * The entries are added in order of apprearance in the source document.
	 * <p>
	 * An attribute with {@linkplain Attribute#hasValue() no value} is represented by a map entry with a <code>null</code> value.
	 * <p>
	 * Attribute values are automatically {@linkplain CharacterReference#decode(CharSequence) decoded}
	 * before storage in the map.
	 *
	 * @param attributesMap  the map to populate, must not be <code>null</code>.
	 * @param convertNamesToLowerCase  specifies whether all attribute names are converted to lower case in the map.
	 * @return the same map specified as the argument to the <code>attributesMap</code> parameter, populated with the name/value pairs from these attributes.
	 * @see #generateHTML(Map attributesMap)
	 */
	public Map populateMap(final Map attributesMap, final boolean convertNamesToLowerCase) {
		for (final Iterator i=listIterator(0); i.hasNext();) {
			final Attribute attribute=(Attribute)i.next();
			attributesMap.put(convertNamesToLowerCase ? attribute.getKey() : attribute.getName(),attribute.getValue());
		}
		return attributesMap;
	}

	/**
	 * Returns a string representation of this object useful for debugging purposes.
	 * @return a string representation of this object useful for debugging purposes.
	 */
	public String getDebugInfo() {
		final StringBuffer sb=new StringBuffer();
		sb.append("Attributes ").append(super.getDebugInfo()).append(": ");
		if (isEmpty()) {
			sb.append("EMPTY");
		} else {
			sb.append('\n');
			for (final Iterator i=listIterator(0); i.hasNext();) {
				Attribute attribute=(Attribute)i.next();
				sb.append("  ").append(attribute.getDebugInfo());
			}
		}
		return sb.toString();
	}

	/**
	 * Returns the default maximum error count allowed when parsing attributes.
	 * <p>
	 * The system default value is 2.
	 * <p>
	 * When searching for start tags, the parser can find the end of the start tag only by
	 * {@linkplain StartTagType#parseAttributes(Source,int,String) parsing}
	 * the attributes, as it is valid HTML for attribute values to contain '&gt;' characters
	 * (see the <a target="_blank" href="http://www.w3.org/TR/html401/charset.html#h-5.3.2">HTML 4.01 specification section 5.3.2</a>).
	 * <p>
	 * If the source text being parsed does not follow the syntax of an attribute list at all, the parser assumes
	 * that the text which was originally identified as the beginning of of a start tag is in fact some other text,
	 * such as an invalid '&lt;' character in the middle of some text, or part of a script element.
	 * In this case the entire start tag is rejected.
	 * <p>
	 * On the other hand, it is quite common for attributes to contain minor syntactical errors,
	 * such as an invalid character in an attribute name, or a couple of special characters in
	 * {@linkplain TagType#isServerTag() server tags} that otherwise contain only attributes.
	 * For this reason the parser allows a certain number of minor errors to occur while parsing an
	 * attribute list before the entire start tag or attribute list is rejected.
	 * This property indicates the number of minor errors allowed.
	 * <p>
	 * Major syntactical errors cause the start tag or attribute list to be rejected immediately, regardless
	 * of the maximum error count setting.
	 * <p>
	 * Some errors are considered too minor to count at all (ignorable), such as missing whitespace between the end
	 * of a quoted attribute value and the start of the next attribute name.
	 * <p>
	 * The classification of particular syntax errors in attribute lists into major, minor, and ignorable is
	 * not part of the specification and may change in future versions.
	 * <p>
	 * To track errors as they occur, use the {@link Source#setLogWriter(Writer writer)} method to set the
	 * destination of the error log.
	 * <p>
	 * The value of this property is set using the {@link #setDefaultMaxErrorCount(int)} method.
	 *
	 * @return the default maximum error count allowed when parsing attributes.
	 * @see Source#parseAttributes(int pos, int maxEnd, int maxErrorCount)
	 */
	public static int getDefaultMaxErrorCount() {
		return defaultMaxErrorCount;
	}

	/**
	 * Sets the default maximum error count allowed when parsing attributes.
	 * <p>
	 * See the {@link #getDefaultMaxErrorCount()} method for a full description of this property.
	 *
	 * @param value  the default maximum error count allowed when parsing attributes.
	 */
	public static void setDefaultMaxErrorCount(final int value) {
		defaultMaxErrorCount=value;
	}

	/**
	 * Returns the contents of the specified {@linkplain #populateMap(Map,boolean) attributes map} as HTML attribute name/value pairs.
	 * <p>
	 * Each attribute (including the first) is preceded by a single space, and all values are
	 * {@linkplain CharacterReference#encode(CharSequence) encoded} and enclosed in double quotes.
	 * <p>
	 * The map keys must be of type <code>String</code> and values must be objects that implement the <code>CharSequence</code> interface.
	 * <p>
	 * A <code>null</code> value represents an attribute with no value.
	 *
	 * @param attributesMap  a map containing attribute name/value pairs.
	 * @return the contents of the specified {@linkplain #populateMap(Map,boolean) attributes map} as HTML attribute name/value pairs.
	 * @see StartTag#generateHTML(String tagName, Map attributesMap, boolean emptyElementTag)
	 */
	public static String generateHTML(final Map attributesMap) {
		final StringWriter stringWriter=new StringWriter();
		try {appendHTML(stringWriter,attributesMap);} catch (IOException ex) {} // IOException never occurs in StringWriter
		return stringWriter.toString();
	}

	/**
	 * Returns this instance.
	 * <p>
 	 * This method has been deprecated as of version 2.0 as the <code>Attributes</code> class now implements
 	 * the <code>List</code> interface, so the instance itself can be used instead.
	 *
	 * @return this instance.
	 * @deprecated  Use the {@link Attributes} object itself instead.
	 */
	public List getList() {
		return this;
	}

	/**
	 * Outputs the contents of the specified {@linkplain #populateMap(Map,boolean) attributes map} as HTML attribute name/value pairs to the specified <code>Writer</code>.
	 * <p>
	 * Each attribute is preceded by a single space, and all values are
	 * {@linkplain CharacterReference#encode(CharSequence) encoded} and enclosed in double quotes.
	 *
	 * @param out  the <code>Writer</code> to which the output is to be sent.
	 * @param attributesMap  a map containing attribute name/value pairs.
	 * @throws IOException if an I/O exception occurs.
	 * @see #populateMap(Map attributesMap, boolean convertNamesToLowerCase)
	 */
	static void appendHTML(final Writer writer, final Map attributesMap) throws IOException {
		for (final Iterator i=attributesMap.entrySet().iterator(); i.hasNext();) {
			final Map.Entry entry=(Map.Entry)i.next();
			Attribute.appendHTML(writer,(String)entry.getKey(),(CharSequence)entry.getValue());
		}
	}

	StringBuffer appendTidy(final StringBuffer sb, Tag nextTag) {
		for (final Iterator i=listIterator(0); i.hasNext();)
			nextTag=((Attribute)i.next()).appendTidy(sb,nextTag);
		return sb;
	}

	Map getMap(final boolean convertNamesToLowerCase) {
		return populateMap(new LinkedHashMap(getCount()*2,1.0F),convertNamesToLowerCase);
	}
	
	private static void log(final Source source, final String part1, final CharSequence part2, final int begin, final String part3, final int pos) {
		source.log(source.getRowColumnVector(pos).appendTo(source.getRowColumnVector(begin).appendTo(new StringBuffer(200).append(part1).append(' ').append(part2).append(" at ")).append(' ').append(part3).append(" at position ")).toString());
	}

	private static void log(final Source source, final String part1, final CharSequence part2, final int begin, final String part3) {
		source.log(source.getRowColumnVector(begin).appendTo(new StringBuffer(200).append(part1).append(' ').append(part2).append(" at ")).append(' ').append(part3).toString());
	}
}
