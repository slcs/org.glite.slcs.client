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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a segment of a {@link Source} document.
 * <p>
 * The <i>span</i> of a segment is defined by the combination of its begin and end character positions.
 */
public class Segment implements Comparable, CharSequence {
	final int begin;
	final int end;
	final Source source;
	
	List childElements=null;

	private static final char[] WHITESPACE={' ','\n','\r','\t','\f','\u200B'}; // see comments in isWhiteSpace(char) method

	/**
	 * Constructs a new <code>Segment</code> within the specified {@linkplain Source source} document with the specified begin and end character positions.
	 * @param source  the {@link Source} document, must not be <code>null</code>.
	 * @param begin  the character position in the source where this segment begins.
	 * @param end  the character position in the source where this segment ends.
	 */
	public Segment(final Source source, final int begin, final int end) {
		if (begin==-1 || end==-1 || begin>end) throw new IllegalArgumentException();
		this.begin=begin;
		this.end=end;
		if (source==null) throw new IllegalArgumentException("source argument must not be null");
		this.source=source;
	}

	// Only called from Source constructor
	Segment(final int length) {
		begin=0;
		this.end=length;
		source=(Source)this;
	}

	// Only used for creating dummy flag instances of this type (see Element.NOT_CACHED)
	Segment() {
		begin=0;
		end=0;
		source=null;
	}

	/**
	 * Returns the character position in the {@link Source} document at which this segment begins.
	 * @return the character position in the {@link Source} document at which this segment begins.
	 */
	public final int getBegin() {
		return begin;
	}

	/**
	 * Returns the character position in the {@link Source} document immediately after the end of this segment.
	 * <p>
	 * The character at the position specified by this property is <b>not</b> included in the segment.
	 *
	 * @return the character position in the {@link Source} document immediately after the end of this segment.
	 */
	public final int getEnd() {
		return end;
	}

	/**
	 * Compares the specified object with this <code>Segment</code> for equality.
	 * <p>
	 * Returns <code>true</code> if and only if the specified object is also a <code>Segment</code>,
	 * and both segments have the same {@link Source}, and the same begin and end positions.
	 * @param object  the object to be compared for equality with this <code>Segment</code>.
	 * @return <code>true</code> if the specified object is equal to this <code>Segment</code>, otherwise <code>false</code>.
	 */
	public final boolean equals(final Object object) {
		if (object==null || !(object instanceof Segment)) return false;
		final Segment segment=(Segment)object;
		return segment.begin==begin && segment.end==end && segment.source==source;
	}

	/**
	 * Returns a hash code value for the segment.
	 * <p>
	 * The current implementation returns the sum of the begin and end positions, although this is not
	 * guaranteed in future versions.
	 *
	 * @return a hash code value for the segment.
	 */
	public int hashCode() {
		return begin+end;
	}

	/**
	 * Returns the length of the segment.
	 * This is defined as the number of characters between the begin and end positions.
	 * @return the length of the segment.
	 */
	public final int length() {
		return end-begin;
	}

	/**
	 * Indicates whether this <code>Segment</code> encloses the specified <code>Segment</code>.
	 * <p>
	 * This is the case if {@link #getBegin()}<code>&lt;=segment.</code>{@link #getBegin()}<code> &amp;&amp; </code>{@link #getEnd()}<code>&gt;=segment.</code>{@link #getEnd()}.
	 *
	 * @param segment  the segment to be tested for being enclosed by this segment.
	 * @return <code>true</code> if this <code>Segment</code> encloses the specified <code>Segment</code>, otherwise <code>false</code>.
	 */
	public final boolean encloses(final Segment segment) {
		return begin<=segment.begin && end>=segment.end;
	}

	/**
	 * Indicates whether this segment encloses the specified character position in the source document.
	 * <p>
	 * This is the case if {@link #getBegin()}<code> &lt;= pos &lt; </code>{@link #getEnd()}.
	 *
	 * @param pos  the position in the {@link Source} document.
	 * @return <code>true</code> if this segment encloses the specified character position in the source document, otherwise <code>false</code>.
	 */
	public final boolean encloses(final int pos) {
		return begin<=pos && pos<end;
	}

	/**
	 * Returns the source text of this segment as a <code>String</code>.
	 * <p>
	 * The returned <code>String</code> is newly created with every call to this method, unless this
	 * segment is itself an instance of {@link Source}.
	 * <p>
	 * Note that before version 2.0 this returned a representation of this object useful for debugging purposes,
	 * which can now be obtained via the {@link #getDebugInfo()} method.
	 *
	 * @return the source text of this segment as a <code>String</code>.
	 */
	public String toString() {
		return source.string.substring(begin,end).toString();
	}

	/**
	 * Extracts the text content of this segment.
	 * <p>
	 * This method removes all of the tags from the segment and
	 * {@linkplain CharacterReference#decodeCollapseWhiteSpace(CharSequence) decodes the result, collapsing all white space}.
	 * <p>
	 * See the documentation of the {@link #extractText(boolean includeAttributes)} method for more details.
	 * <p>
	 * This is equivalent to calling {@link #extractText(boolean) extractText(false)}.
	 *
	 * @return the text content of this segment.
	 */
	public String extractText() {
		return extractText(false);
	}

	/**
	 * Extracts the text content of this segment.
	 * <p>
	 * This method removes all of the tags from the segment and
	 * {@linkplain CharacterReference#decodeCollapseWhiteSpace(CharSequence) decodes the result, collapsing all white space}.
	 * Tags are also converted to whitespace unless they belong to an
	 * {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} element.
	 * An exception to this is the {@link HTMLElementName#BR BR} element, which is also converted to whitespace despite being an inline-level element.
	 * <p>
	 * Text inside {@link HTMLElementName#SCRIPT SCRIPT} and {@link HTMLElementName#STYLE STYLE} elements contained within this segment
	 * is ignored.
	 * <p>
	 * Specifying a value of <code>true</code> as an argument to the <code>includeAttributes</code> parameter causes the values of 
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#adef-title">title</a>,
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/objects.html#adef-alt">alt</a>,
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-label-OPTION">label</a>, and
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/tables.html#adef-summary">summary</a>
	 * attributes of {@linkplain StartTagType#NORMAL normal} tags to be included in the extracted text.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *  <dd>source segment "<code>&lt;div&gt;&lt;b&gt;O&lt;/b&gt;ne&lt;/div&gt;&lt;div&gt;&lt;b&gt;T&lt;/b&gt;&lt;script&gt;//a&nbsp;script&nbsp;&lt;/script&gt;wo&lt;/div&gt;</code>"
	 *   produces the text "<code>One Two</code>".
	 * </dl>
	 * <p>
	 * Note that in version 2.1, no tags were converted to whitespace and text inside {@link HTMLElementName#SCRIPT SCRIPT} and
	 * {@link HTMLElementName#STYLE STYLE} elements was included.  The example above produced the text "<code>OneT//a script wo</code>".
	 *
	 * @param includeAttributes  indicates whether the values of <a target="_blank" href="http://www.w3.org/TR/html401/struct/global.html#adef-title">title</a>, <a target="_blank" href="http://www.w3.org/TR/html401/struct/objects.html#adef-alt">alt</a>, <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-label-OPTION">label</a>, and <a target="_blank" href="http://www.w3.org/TR/html401/struct/tables.html#adef-summary">summary</a> attributes are included.
	 * @return the text content of this segment.
	 */
	public String extractText(final boolean includeAttributes) {
		final StringBuffer sb=new StringBuffer(length());
		int textBegin=begin;
		// use findAllTags().iterator() instead of source.findNextTag(textBegin) to take advantage of allTags cache in Source object
		for (final Iterator i=findAllTags().iterator(); i.hasNext();) {
			final Tag tag=(Tag)i.next();
			final int textEnd=tag.begin;
			if (textEnd<textBegin) continue;
			while (textBegin<textEnd) sb.append(source.charAt(textBegin++));
			if (tag.getTagType()==StartTagType.NORMAL) {
				if (tag.name==HTMLElementName.SCRIPT || tag.name==HTMLElementName.STYLE) {
					final EndTag endTag=source.findNextEndTag(tag.end,tag.name,EndTagType.NORMAL);
					if (endTag!=null) {
						textBegin=endTag.end;
						while (i.hasNext() && i.next()!=endTag) {}
						continue;
					}
				}
				if (includeAttributes) {
					final StartTag startTag=(StartTag)tag;
					// add title attribute:
					final Attribute titleAttribute=startTag.getAttributes().get("title");
					if (titleAttribute!=null) sb.append(' ').append(titleAttribute.getValueSegment()).append(' ');
					// add alt attribute (APPLET, AREA, IMG and INPUT elements):
					final Attribute altAttribute=startTag.getAttributes().get("alt");
					if (altAttribute!=null) sb.append(' ').append(altAttribute.getValueSegment()).append(' ');
					// add label attribute (OPTION and OPTGROUP elements):
					final Attribute labelAttribute=startTag.getAttributes().get("label");
					if (labelAttribute!=null) sb.append(' ').append(labelAttribute.getValueSegment()).append(' ');
					// add summary attribute (TABLE element):
					final Attribute summaryAttribute=startTag.getAttributes().get("summary");
					if (summaryAttribute!=null) sb.append(' ').append(summaryAttribute.getValueSegment()).append(' ');
					// don't bother with the prompt attribute from the ININDEX element as the element is deprecated and very rarely used.
				}
			}
			// Treat tags not belonging to inline-level elements as whitespace:
			if (tag.getName()==HTMLElementName.BR || !HTMLElements.getInlineLevelElementNames().contains(tag.getName())) sb.append('\n');
			textBegin=tag.end;
		}
		while (textBegin<end) sb.append(source.charAt(textBegin++));
		final String decodedText=CharacterReference.decodeCollapseWhiteSpace(sb);
		return decodedText;
	}

	/**
	 * Returns a list of all {@link Tag} objects that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @return a list of all {@link Tag} objects that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 */
	public List findAllTags() {
		return findAllTags(null);
	}

	/**
	 * Returns a list of all {@link Tag} objects of the specified {@linkplain TagType type} that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 * <p>
	 * Specifying a <code>null</code> argument to the <code>tagType</code> parameter is equivalent to {@link #findAllTags()}.
	 *
	 * @param tagType  the {@linkplain TagType type} of tags to find.
	 * @return a list of all {@link Tag} objects of the specified {@linkplain TagType type} that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 */
	public List findAllTags(final TagType tagType) {
		Tag tag=checkEnclosure(Tag.findPreviousOrNextTag(source,begin,tagType,false));
		if (tag==null) return Collections.EMPTY_LIST;
		final ArrayList list=new ArrayList();
		do {
			list.add(tag);
			tag=checkEnclosure(Tag.findPreviousOrNextTag(source,tag.begin+1,tagType,false));
		} while (tag!=null);
		return list;
	}

	/**
	 * Returns a list of all {@link StartTag} objects that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @return a list of all {@link StartTag} objects that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 */
	public List findAllStartTags() {
		return findAllStartTags(null);
	}

	/**
	 * Returns a list of all {@link StartTag} objects with the specified name that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 * <p>
	 * Specifying a <code>null</code> argument to the <code>name</code> parameter is equivalent to {@link #findAllStartTags()}.
	 * <p>
	 * This method also returns {@linkplain Tag#isUnregistered() unregistered} tags if the specified name is not a valid {@linkplain Tag#isXMLName(CharSequence) XML tag name}.
	 *
	 * @param name  the {@linkplain StartTag#getName() name} of the start tags to find.
	 * @return a list of all {@link StartTag} objects with the specified name that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 */
	public List findAllStartTags(String name) {
		if (name!=null) name=name.toLowerCase();
		final boolean isXMLTagName=Tag.isXMLName(name);
		StartTag startTag=(StartTag)checkEnclosure(StartTag.findPreviousOrNext(source,begin,name,isXMLTagName,false));
		if (startTag==null) return Collections.EMPTY_LIST;
		final ArrayList list=new ArrayList();
		do {
			list.add(startTag);
			startTag=(StartTag)checkEnclosure(StartTag.findPreviousOrNext(source,startTag.begin+1,name,isXMLTagName,false));
		} while (startTag!=null);
		return list;
	}

	/**
	 * Returns a list of all {@link StartTag} objects with the specified attribute name/value pair 
	 * that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param attributeName  the attribute name (case insensitive) to search for, must not be <code>null</code>.
	 * @param value  the value of the specified attribute to search for, must not be <code>null</code>.
	 * @param valueCaseSensitive  specifies whether the attribute value matching is case sensitive.
	 * @return a list of all {@link StartTag} objects with the specified attribute name/value pair that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 */
	public List findAllStartTags(final String attributeName, final String value, final boolean valueCaseSensitive) {
		StartTag startTag=(StartTag)checkEnclosure(source.findNextStartTag(begin,attributeName,value,valueCaseSensitive));
		if (startTag==null) return Collections.EMPTY_LIST;
		final ArrayList list=new ArrayList();
		do {
			list.add(startTag);
			startTag=(StartTag)checkEnclosure(source.findNextStartTag(startTag.begin+1,attributeName,value,valueCaseSensitive));
		} while (startTag!=null);
		return list;
	}

	/**
	 * Returns a list of the immediate children of this segment in the document element hierarchy.
	 * <p>
	 * The returned list may include an element that extends beyond the end of this segment, as long as it begins within this segment.
	 * <p>
	 * The objects in the list are all of type {@link Element}.
	 * <p>
	 * See the {@link Source#getChildElements()} method for more details.
	 *
	 * @return the a list of the immediate children of this segment in the document element hierarchy, guaranteed not <code>null</code>.
	 * @see Element#getParentElement()
	 */
	public List getChildElements() {
		if (childElements==null) {
			if (length()==0) {
				childElements=Collections.EMPTY_LIST;
			} else {
				childElements=new ArrayList();
				int pos=begin;
				while (true) {
					final StartTag childStartTag=source.findNextStartTag(pos);
					if (childStartTag==null || childStartTag.begin>=end) break;
					if (!Config.IncludeServerTagsInElementHierarchy && childStartTag.getTagType().isServerTag()) {
						pos=childStartTag.end;
						continue;
					}
					final Element childElement=childStartTag.getElement();
					childElements.add(childElement);
					childElement.getChildElements();
					pos=childElement.end;
				}
			}
		}
		return childElements;
	}

	/**
	 * Returns a list of all {@link Element} objects that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * <p>
	 * The elements returned correspond exactly with the start tags returned in the {@link #findAllStartTags()} method.
	 *
	 * @return a list of all {@link Element} objects that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 */
	public List findAllElements() {
		return findAllElements((String)null);
	}

	/**
	 * Returns a list of all {@link Element} objects with the specified name that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * <p>
	 * The elements returned correspond exactly with the start tags returned in the {@link #findAllStartTags(String name)} method.
	 * <p>
	 * Specifying a <code>null</code> argument to the <code>name</code> parameter is equivalent to {@link #findAllElements()}.
	 * <p>
	 * This method also returns elements consisting of {@linkplain Tag#isUnregistered() unregistered} tags if the specified name is not a valid {@linkplain Tag#isXMLName(CharSequence) XML tag name}.
	 *
	 * @param name  the {@linkplain Element#getName() name} of the elements to find.
	 * @return a list of all {@link Element} objects with the specified name that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 */
	public List findAllElements(String name) {
		if (name!=null) name=name.toLowerCase();
		final List startTags=findAllStartTags(name);
		if (startTags.isEmpty()) return Collections.EMPTY_LIST;
		final ArrayList elements=new ArrayList(startTags.size());
		for (final Iterator i=startTags.iterator(); i.hasNext();) {
			final StartTag startTag=(StartTag)i.next();
			final Element element=startTag.getElement();
			if (element.end>end) break;
			elements.add(element);
		}
		return elements;
	}

	/**
	 * Returns a list of all {@link Element} objects with start tags of the specified {@linkplain StartTagType type} that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * <p>
	 * The elements returned correspond exactly with the start tags returned in the {@link #findAllTags(TagType)} method.
	 *
	 * @param startTagType  the {@linkplain StartTagType type} of start tags to find, must not be <code>null</code>.
	 * @return a list of all {@link Element} objects with start tags of the specified {@linkplain StartTagType type} that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 */
	public List findAllElements(final StartTagType startTagType) {
		final List startTags=findAllTags(startTagType);
		if (startTags.isEmpty()) return Collections.EMPTY_LIST;
		final ArrayList elements=new ArrayList(startTags.size());
		for (final Iterator i=startTags.iterator(); i.hasNext();) {
			final StartTag startTag=(StartTag)i.next();
			final Element element=startTag.getElement();
			if (element.end>end) break;
			elements.add(element);
		}
		return elements;
	}

	/**
	 * Returns a list of all {@link CharacterReference} objects that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * @return a list of all {@link CharacterReference} objects that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 */
	public List findAllCharacterReferences() {
		CharacterReference characterReference=findNextCharacterReference(begin);
		if (characterReference==null) return Collections.EMPTY_LIST;
		final ArrayList list=new ArrayList();
		do {
			list.add(characterReference);
			characterReference=findNextCharacterReference(characterReference.end);
		} while (characterReference!=null);
		return list;
	}

	/**
	 * Returns a list of the {@link FormControl} objects that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * @return a list of the {@link FormControl} objects that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 */
	public List findFormControls() {
		return FormControl.findAll(this);
	}

	/**
	 * Returns the {@link FormFields} object representing all form fields that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * <p>
	 * This is equivalent to {@link FormFields#FormFields(Collection) new FormFields}<code>(</code>{@link #findFormControls()}<code>)</code>.
	 *
	 * @return the {@link FormFields} object representing all form fields that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * @see #findFormControls()
	 */
	public FormFields findFormFields() {
		return new FormFields(findFormControls());
	}

	/**
	 * Parses any {@link Attributes} within this segment.
	 * This method is only used in the unusual situation where attributes exist outside of a start tag.
	 * The {@link StartTag#getAttributes()} method should be used in normal situations.
	 * <p>
	 * This is equivalent to <code>source.</code>{@link Source#parseAttributes(int,int) parseAttributes}<code>(</code>{@link #getBegin()}<code>,</code>{@link #getEnd()}<code>)</code>.
	 *
	 * @return the {@link Attributes} within this segment, or <code>null</code> if too many errors occur while parsing.
	 */
	public Attributes parseAttributes() {
		return source.parseAttributes(begin,end);
	}

	/**
	 * Causes the this segment to be ignored when parsing.
	 * <p>
	 * This method is usually used to exclude {@linkplain TagType#isServerTag() server tags} or other non-HTML segments from the source text
	 * so that they do not interfere with the parsing of the surrounding HTML.
	 * <p>
	 * This is necessary because many server tags are used as attribute values and in other places within
	 * HTML tags, and very often contain characters that prevent the parser from recognising the surrounding tag.
	 * <p>
	 * Any tags appearing in this segment that are found before this method is called will remain in the {@linkplain Source#getCacheDebugInfo() tag cache},
	 * and so will continue to be found by the <a href="Tag.html#TagSearchMethods">tag search methods</a>.
	 * If this is undesirable, the {@link Source#clearCache()} method can be called to remove them from the cache.
	 * Calling the {@link Source#fullSequentialParse()} method after this method clears the cache automatically.
	 * <p>
	 * For efficiency reasons, this method should be called on all segments that need to be ignored without calling
	 * any of the <a href="Tag.html#TagSearchMethods">tag search methods</a> in between.
	 *
	 * @see Source#ignoreWhenParsing(Collection segments)
	 */
	public void ignoreWhenParsing() {
		source.ignoreWhenParsing(begin,end);
	}

	/**
	 * Compares this <code>Segment</code> object to another object.
	 * <p>
	 * If the argument is not a <code>Segment</code>, a <code>ClassCastException</code> is thrown.
	 * <p>
	 * A segment is considered to be before another segment if its begin position is earlier,
	 * or in the case that both segments begin at the same position, its end position is earlier.
	 * <p>
	 * Segments that begin and end at the same position are considered equal for
	 * the purposes of this comparison, even if they relate to different source documents.
	 * <p>
	 * Note: this class has a natural ordering that is inconsistent with equals.
	 * This means that this method may return zero in some cases where calling the
	 * {@link #equals(Object)} method with the same argument returns <code>false</code>.
	 *
	 * @param o  the segment to be compared
	 * @return a negative integer, zero, or a positive integer as this segment is before, equal to, or after the specified segment.
	 * @throws ClassCastException if the argument is not a <code>Segment</code>
	 */
	public int compareTo(final Object o) {
		if (this==o) return 0;
		final Segment segment=(Segment)o;
		if (begin<segment.begin) return -1;
		if (begin>segment.begin) return 1;
		if (end<segment.end) return -1;
		if (end>segment.end) return 1;
		return 0;
	}

	/**
	 * Indicates whether this segment consists entirely of {@linkplain #isWhiteSpace(char) white space}.
	 * @return <code>true</code> if this segment consists entirely of {@linkplain #isWhiteSpace(char) white space}, otherwise <code>false</code>.
	 */
	public final boolean isWhiteSpace() {
		for (int i=begin; i<end; i++)
			if (!isWhiteSpace(source.charAt(i))) return false;
		return true;
	}

	/**
	 * Indicates whether the specified character is <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#h-9.1">white space</a>.
	 * <p>
	 * The <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#h-9.1">HTML 4.01 specification section 9.1</a>
	 * specifies the following white space characters:
	 * <ul>
	 *  <li>space (U+0020)
	 *  <li>tab (U+0009)
	 *  <li>form feed (U+000C)
	 *  <li>line feed (U+000A)
	 *  <li>carriage return (U+000D)
	 *  <li>zero-width space (U+200B)
	 * </ul>
	 * <p>
	 * Despite the explicit inclusion of the zero-width space in the HTML specification, Microsoft IE6 does not
	 * recognise them as whitespace and renders them as an unprintable character (empty square).
	 * Even zero-width spaces included using the numeric character reference <code>&amp;#x200B;</code> are rendered this way.
	 *
	 * @param ch  the character to test.
	 * @return <code>true</code> if the specified character is <a target="_blank" href="http://www.w3.org/TR/html401/struct/text.html#h-9.1">white space</a>, otherwise <code>false</code>.
	 */
	public static final boolean isWhiteSpace(final char ch) {
		for (int i=0; i<WHITESPACE.length; i++)
			if (ch==WHITESPACE[i]) return true;
		return false;
	}

	/**
	 * Returns a string representation of this object useful for debugging purposes.
	 * @return a string representation of this object useful for debugging purposes.
	 */
	public String getDebugInfo() {
		final StringBuffer sb=new StringBuffer(50);
		sb.append('(');
		source.getRowColumnVector(begin).appendTo(sb);
		sb.append('-');
		source.getRowColumnVector(end).appendTo(sb);
		sb.append(')');
		return sb.toString();
	}

	/**
	 * Returns the character at the specified index.
	 * <p>
	 * This is logically equivalent to <code>toString().charAt(index)</code>
	 * for valid argument values <code>0 <= index < length()</code>.
	 * <p>
	 * However because this implementation works directly on the underlying document source string,
	 * it should not be assumed that an <code>IndexOutOfBoundsException</code> is thrown
	 * for an invalid argument value.
	 *
	 * @param index  the index of the character.
	 * @return the character at the specified index.
	 */
	public final char charAt(final int index) {
		return source.string.charAt(begin+index);
	}

	/**
	 * Returns a new character sequence that is a subsequence of this sequence.
	 * <p>
	 * This is logically equivalent to <code>toString().subSequence(beginIndex,endIndex)</code>
	 * for valid values of <code>beginIndex</code> and <code>endIndex</code>.
	 * <p>
	 * However because this implementation works directly on the underlying document source string,
	 * it should not be assumed that an <code>IndexOutOfBoundsException</code> is thrown
	 * for invalid argument values as described in the <code>String.subSequence(int,int)</code> method.
	 *
	 * @param beginIndex  the begin index, inclusive.
	 * @param endIndex  the end index, exclusive.
	 * @return a new character sequence that is a subsequence of this sequence.
	 */
	public final CharSequence subSequence(final int beginIndex, final int endIndex) {
		return source.string.subSequence(begin+beginIndex,begin+endIndex);
	}

	/**
	 * Indicates whether this segment is a {@link Tag} of type {@link StartTagType#COMMENT}.
	 * <p>
	 * This method has been deprecated as of version 2.0 as it is not a robust method of checking whether an HTML comment spans this segment.
	 *
	 * @return <code>true</code> if this segment is a {@link Tag} of type {@link StartTagType#COMMENT}, otherwise <code>false</code>.
	 * @deprecated  Use <code>this instanceof </code>{@link Tag}<code> && ((Tag)this).</code>{@link Tag#getTagType() getTagType()}<code>==</code>{@link StartTagType#COMMENT} instead.
	 */
	public boolean isComment() {
		return false; // overridden in StartTag
	}

	/**
	 * Returns a list of all {@link StartTag} objects representing HTML {@linkplain StartTagType#COMMENT comments} that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * <p>
	 * This method has been deprecated as of version 2.0 in favour of the more generic {@link #findAllTags(TagType)} method.
	 *
	 * @return a list of all {@link StartTag} objects representing HTML {@linkplain StartTagType#COMMENT comments} that are {@linkplain #encloses(Segment) enclosed} by this segment.
	 * @deprecated  Use {@link #findAllTags(TagType) findAllTags}<code>(</code>{@link StartTagType#COMMENT}<code>)</code> instead.
	 */
	public List findAllComments() {
		return findAllTags(StartTagType.COMMENT);
	}

	/**
	 * Returns the source text of this segment.
	 * <p>
 	 * This method has been deprecated as of version 2.0 as it now duplicates the functionality of the {@link #toString()} method.
	 *
	 * @return the source text of this segment.
	 * @deprecated  Use {@link #toString() toString()} instead.
	 */
	public String getSourceText() {
		return toString();
	}

	/**
	 * Returns the source text of this segment without {@linkplain #isWhiteSpace(char) white space}.
	 * <p>
	 * All leading and trailing white space is omitted, and any sections of internal white space are replaced by a single space.
	 * <p>
 	 * This method has been deprecated as of version 2.0 as it is no longer used internally and
	 * has no practical use as a public method.
	 * It is similar to the new {@link CharacterReference#decodeCollapseWhiteSpace(CharSequence)} method, but
	 * does not {@linkplain CharacterReference#decode(CharSequence) decode} the text after collapsing the white space.
	 * <p>
	 * @return the source text of this segment without white space.
	 * @deprecated  Use the more useful {@link CharacterReference#decodeCollapseWhiteSpace(CharSequence)} method instead.
	 */
	public final String getSourceTextNoWhitespace() {
		return appendCollapseWhiteSpace(new StringBuffer(length()),this).toString();
	}

	/**
	 * Returns a list of <code>Segment</code> objects representing every word in this segment separated by {@linkplain #isWhiteSpace(char) white space}.
	 * Note that any markup contained in this segment is regarded as normal text for the purposes of this method.
	 * <p>
 	 * This method has been deprecated as of version 2.0 as it has no discernable use.
	 *
	 * @return a list of <code>Segment</code> objects representing every word in this segment separated by white space.
	 * @deprecated  no replacement
	 */
	public final List findWords() {
		final ArrayList words=new ArrayList();
		int wordBegin=-1;
		for (int i=begin; i<end; i++) {
			if (isWhiteSpace(source.charAt(i))) {
				if (wordBegin==-1) continue;
				words.add(new Segment(source,wordBegin,i));
				wordBegin=-1;
			} else {
				if (wordBegin==-1) wordBegin=i;
			}
		}
		if (wordBegin!=-1) words.add(new Segment(source, wordBegin,end));
		return words;
	}

	/**
	 * Collapses the {@linkplain #isWhiteSpace(char) white space} in the specified text.
	 * All leading and trailing white space is omitted, and any sections of internal white space are replaced by a single space.
	 */
	static final StringBuffer appendCollapseWhiteSpace(final StringBuffer sb, final CharSequence text) {
		final int textLength=text.length();
		int i=0;
		boolean lastWasWhiteSpace=false;
		while (true) {
			if (i>=textLength) return sb;
			if (!isWhiteSpace(text.charAt(i))) break;
			i++;
		}
		do {
			final char ch=text.charAt(i++);
			if (isWhiteSpace(ch)) {
				lastWasWhiteSpace=true;
			} else {
				if (lastWasWhiteSpace) {
					sb.append(' ');
					lastWasWhiteSpace=false;
				}
				sb.append(ch);
			}
		} while (i<textLength);
		return sb;
	}

	private Tag checkEnclosure(final Tag tag) {
		if (tag==null || tag.end>end) return null;
		return tag;
	}

	private CharacterReference findNextCharacterReference(final int pos) {
		final CharacterReference characterReference=source.findNextCharacterReference(pos);
		if (characterReference==null || characterReference.end>end) return null;
		return characterReference;
	}
}

