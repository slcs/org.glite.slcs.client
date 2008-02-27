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
import java.util.Map;
import java.util.Set;

/**
 * Represents the <a target="_blank" href="http://www.w3.org/TR/html401/intro/sgmltut.html#didx-element-2">start tag</a> of an 
 * {@linkplain Element element} in a specific {@linkplain Source source} document.
 * <p>
 * A start tag always has a {@linkplain #getTagType() type} that is a subclass of {@link StartTagType}, meaning that any tag
 * that does <b>not</b> start with the characters '<code>&lt;/</code>' is categorised as a start tag.
 * <p>
 * This includes many tags which stand alone, without a {@linkplain StartTagType#getCorrespondingEndTagType() corresponding end tag},
 * and would not intuitively be categorised as a "start tag".
 * For example, an HTML {@linkplain StartTagType#COMMENT comment} is represented as a single start tag that spans the whole comment,
 * and does not have an end tag at all.
 * <p>
 * See the <a href="StartTagType.html#field_summary">static fields</a> defined in the {@link StartTagType} class for a list of the 
 * <a href="TagType.html#Standard">standard</a> start tag types.
 * <p>
 * <code>StartTag</code> instances are obtained using one of the following methods:
 * <ul>
 *  <li>{@link Element#getStartTag()}
 *  <li>{@link Tag#findNextTag()}
 *  <li>{@link Tag#findPreviousTag()}
 *  <li>{@link Source#findPreviousStartTag(int pos)}
 *  <li>{@link Source#findPreviousStartTag(int pos, String name)}
 *  <li>{@link Source#findPreviousTag(int pos)}
 *  <li>{@link Source#findPreviousTag(int pos, TagType)}
 *  <li>{@link Source#findNextStartTag(int pos)}
 *  <li>{@link Source#findNextStartTag(int pos, String name)}
 *  <li>{@link Source#findNextStartTag(int pos, String attributeName, String value, boolean valueCaseSensitive)}
 *  <li>{@link Source#findNextTag(int pos)}
 *  <li>{@link Source#findNextTag(int pos, TagType)}
 *  <li>{@link Source#findEnclosingTag(int pos)}
 *  <li>{@link Source#findEnclosingTag(int pos, TagType)}
 *  <li>{@link Source#getTagAt(int pos)}
 *  <li>{@link Segment#findAllStartTags()}
 *  <li>{@link Segment#findAllStartTags(String name)}
 *  <li>{@link Segment#findAllStartTags(String attributeName, String value, boolean valueCaseSensitive)}
 *  <li>{@link Segment#findAllTags()}
 *  <li>{@link Segment#findAllTags(TagType)}
 * </ul>
 * <p>
 * The methods above which accept a <code>name</code> parameter are categorised as <a href="Tag.html#NamedSearch">named search</a> methods.
 * <p>
 * In such methods dealing with start tags, specifying an argument to the <code>name</code> parameter that ends in a
 * colon (<code>:</code>) searches for all start tags in the specified XML namespace.
 * <p>
 * The constants defined in the {@link HTMLElementName} interface can be used directly as arguments to these <code>name</code> parameters.
 * For example, <code>source.findAllStartTags(</code>{@link HTMLElementName#A}<code>)</code> is equivalent to
 * <code>source.findAllStartTags("a")</code>, and finds all hyperlink start tags.
 * <p>
 * The {@link Tag} superclass defines a method called {@link Tag#getName() getName()} to get the name of this start tag.
 * <p>
 * See also the XML 1.0 specification for <a target="_blank" href="http://www.w3.org/TR/REC-xml#dt-stag">start tags</a>.
 *
 * @see Tag
 * @see Element
 * @see EndTag
 */
public final class StartTag extends Tag {
	private final Attributes attributes;
	final StartTagType startTagType;

	/**
	 * Constructs a new <code>StartTag</code>.
	 *
	 * @param source  the {@link Source} document.
	 * @param begin  the character position in the source document where this tag {@linkplain Segment#getBegin() begins}.
	 * @param end  the character position in the source document where this tag {@linkplain Segment#getEnd() ends}.
	 * @param startTagType  the {@linkplain #getStartTagType() type} of the start tag.
	 * @param name  the {@linkplain Tag#getName() name} of the tag.
	 * @param attributes  the {@linkplain #getAttributes() attributes} of the tag.
	 */
	StartTag(final Source source, final int begin, final int end, final StartTagType startTagType, final String name, final Attributes attributes) {
		super(source,begin,end,name);
		this.attributes=attributes;
		this.startTagType=startTagType;
	}

	/**
	 * Returns the {@linkplain Element element} that is started by this start tag.
	 * Guaranteed not <code>null</code>.
	 * <h4>Example 1: Elements for which the {@linkplain HTMLElements#getEndTagRequiredElementNames() end tag is required}</h4>
	 * <pre>
	 * 1. &lt;div&gt;
	 * 2.   &lt;div&gt;
	 * 3.     &lt;div&gt;
	 * 4.       &lt;div&gt;This is line 4&lt;/div&gt;
	 * 5.     &lt;/div&gt;
	 * 6.     &lt;div&gt;This is line 6&lt;/div&gt;
	 * 7.   &lt;/div&gt;</pre>
	 * <ul>
	 *  <li>The start tag on line 1 returns an empty element spanning only the start tag.
	 *   This is because the end tag of a <code>&lt;div&gt;</code> element is required,
	 *   making the sample code invalid as all the end tags are matched with other start tags.
	 *  <li>The start tag on line 2 returns an element spanning to the end of line 7.
	 *  <li>The start tag on line 3 returns an element spanning to the end of line 5.
	 *  <li>The start tag on line 4 returns an element spanning to the end of line 4.
	 *  <li>The start tag on line 6 returns an element spanning to the end of line 6.
	 * </ul>
	 * <h4>Example 2: Elements for which the {@linkplain HTMLElements#getEndTagOptionalElementNames() end tag is optional}</h4>
	 * <pre>
	 * 1. &lt;ul&gt;
	 * 2.   &lt;li&gt;item 1
	 * 3.   &lt;li&gt;item 2
	 * 4.     &lt;ul&gt;
	 * 5.       &lt;li&gt;subitem 1&lt;/li&gt;
	 * 6.       &lt;li&gt;subitem 2
	 * 7.     &lt;/ul&gt;
	 * 8.   &lt;li&gt;item 3&lt;/li&gt;
	 * 9. &lt;/ul&gt;</pre>
	 * <ul>
	 *  <li>The start tag on line 1 returns an element spanning to the end of line 9.
	 *  <li>The start tag on line 2 returns an element spanning to the start of the <code>&lt;li&gt;</code> start tag on line 3.
	 *  <li>The start tag on line 3 returns an element spanning to the start of the <code>&lt;li&gt;</code> start tag on line 8.
	 *  <li>The start tag on line 4 returns an element spanning to the end of line 7.
	 *  <li>The start tag on line 5 returns an element spanning to the end of line 5.
	 *  <li>The start tag on line 6 returns an element spanning to the start of the <code>&lt;/ul&gt;</code> end tag on line 7.
	 *  <li>The start tag on line 8 returns an element spanning to the end of line 8.
	 * </ul>
	 *
	 * @return the {@linkplain Element element} that is started by this start tag.
	 */
	public Element getElement() {
		if (element==Element.NOT_CACHED) {
			final EndTag endTag=findEndTagInternal();
			element=new Element(source,this,endTag);
			if (endTag!=null) {
				if (endTag.element!=Element.NOT_CACHED)
					if (source.isLoggingEnabled()) source.log(source.getRowColumnVector(endTag.begin).appendTo(new StringBuffer(200).append("End tag ").append(endTag).append(" at ")).append(" terminates more than one element").toString()); // presumably impossible, but log it just in case
				endTag.element=element;
			}
		}
		return element;
	}

	/**
	 * Indicates whether this start tag is syntactically an <a target="_blank" href="http://www.w3.org/TR/REC-xml#dt-eetag">empty-element tag</a>.
	 * <p>
	 * This is signified by the characters "/&gt;" at the end of the start tag.
	 * <p>
	 * Only a {@linkplain StartTagType#NORMAL normal} start tag can be an empty-element tag.
	 * <p>
	 * This property simply reports whether the syntax of the start tag is consistent with that of an empty-element tag,
	 * it does not guarantee that this start tag's {@linkplain #getElement() element} is actually {@linkplain Element#isEmpty() empty}.
	 * <p>
	 * This possible discrepancy reflects the way major browsers interpret illegal empty element tags used in
	 * <a href="HTMLElements.html#HTMLElement">HTML elements</a>, and is explained further in the documentation of the
	 * {@link Element#isEmptyElementTag()} property.
	 * <p>
	 * Compare this property with the {@link Element#isEmptyElementTag()} property, which does check that the element is actually empty.
	 *
	 * @return <code>true</code> if this start tag is syntactically an <a target="_blank" href="http://www.w3.org/TR/REC-xml#dt-eetag">empty-element tag</a>, otherwise <code>false</code>.
	 */
	public boolean isEmptyElementTag() {
		return startTagType==StartTagType.NORMAL && source.charAt(end-2)=='/';
	}

	/**
	 * Returns the {@linkplain StartTagType type} of this start tag.	
	 * <p>
	 * This is equivalent to <code>(StartTagType)</code>{@link #getTagType()}.
	 *
	 * @return the {@linkplain StartTagType type} of this start tag.	
	 */
	public StartTagType getStartTagType() {
		return startTagType;
	}

	// Documentation inherited from Tag
	public TagType getTagType() {
		return startTagType;
	}

	/**
	 * Returns the attributes specified in this start tag.
	 * <p>
	 * Return value is not <code>null</code> if and only if
	 * {@link #getStartTagType()}<code>.</code>{@link StartTagType#hasAttributes() hasAttributes()}<code>==true</code>.
	 * <p>
	 * To force the parsing of attributes in other start tag types, use the {@link #parseAttributes()} method instead.
	 *
	 * @return the attributes specified in this start tag, or <code>null</code> if the {@linkplain #getStartTagType() type} of this start tag does not {@linkplain StartTagType#hasAttributes() have attributes}.
	 * @see #parseAttributes()
	 * @see Source#parseAttributes(int pos, int maxEnd)
	 */
	public Attributes getAttributes() {
		return attributes;
	}

	/**
	 * Returns the {@linkplain CharacterReference#decode(CharSequence) decoded} value of the attribute with the specified name (case insensitive).
	 * <p>
	 * Returns <code>null</code> if this start tag does not {@linkplain StartTagType#hasAttributes() have attributes},
	 * no attribute with the specified name exists or the attribute {@linkplain Attribute#hasValue() has no value}.
	 * <p>
	 * This is equivalent to {@link #getAttributes()}<code>.</code>{@link Attributes#getValue(String) getValue(attributeName)},
	 * except that it returns <code>null</code> if this start tag does not have attributes instead of throwing a
	 * <code>NullPointerException</code>.
	 *
	 * @param attributeName  the name of the attribute to get.
	 * @return the {@linkplain CharacterReference#decode(CharSequence) decoded} value of the attribute with the specified name, or <code>null</code> if the attribute does not exist or {@linkplain Attribute#hasValue() has no value}.
	 */
	public String getAttributeValue(final String attributeName) {
		return attributes==null ? null : attributes.getValue(attributeName);
	}

	/**
	 * Parses the attributes specified in this start tag, regardless of the type of start tag.
	 * This method is only required in the unusual situation where attributes exist in a start tag whose 
	 * {@linkplain #getStartTagType() type} doesn't {@linkplain StartTagType#hasAttributes() have attributes}.
	 * <p>
	 * This method returns the cached attributes from the {@link StartTag#getAttributes()} method
	 * if its value is not <code>null</code>, otherwise the source is physically parsed with each call to this method.
	 * <p>
	 * This is equivalent to {@link #parseAttributes(int) parseAttributes}<code>(</code>{@link Attributes#getDefaultMaxErrorCount()}<code>)}</code>.
	 *
	 * @return the attributes specified in this start tag, or <code>null</code> if too many errors occur while parsing.
	 * @see #getAttributes()
	 * @see Source#parseAttributes(int pos, int maxEnd)
	 */
	public Attributes parseAttributes() {
		return parseAttributes(Attributes.getDefaultMaxErrorCount());
	}

	/**
	 * Parses the attributes specified in this start tag, regardless of the type of start tag.
	 * This method is only required in the unusual situation where attributes exist in a start tag whose 
	 * {@linkplain #getStartTagType() type} doesn't {@linkplain StartTagType#hasAttributes() have attributes}.
	 * <p>
	 * See the documentation of the {@link #parseAttributes()} method for more information.
	 *
	 * @param maxErrorCount  the maximum number of minor errors allowed while parsing
	 * @return the attributes specified in this start tag, or <code>null</code> if too many errors occur while parsing.
	 * @see #getAttributes()
	 */
	public Attributes parseAttributes(final int maxErrorCount) {
		if (attributes!=null) return attributes;
		final int maxEnd=end-startTagType.getClosingDelimiter().length();
		int attributesBegin=begin+1+name.length();
		// skip any non-name characters directly after the name (which are quite common)
		while (!isXMLNameStartChar(source.charAt(attributesBegin))) {
			attributesBegin++;
			if (attributesBegin==maxEnd) return null;
		}
		return Attributes.construct(source,begin,attributesBegin,maxEnd,startTagType,name,maxErrorCount);
	}

	/**
	 * Returns the segment between the end of the tag's {@linkplain #getName() name} and the start of its <a href="#EndDelimiter">end delimiter</a>.
	 * <p>
	 * This method is normally only of use for start tags whose content is something other than {@linkplain #getAttributes() attributes}.
	 * <p>
	 * A new {@link Segment} object is created with each call to this method.
	 *
	 * @return the segment between the end of the tag's {@linkplain #getName() name} and the start of the <a href="#EndDelimiter">end delimiter</a>.
	 */
	public Segment getTagContent() {
		return new Segment(source,begin+1+name.length(),end-startTagType.getClosingDelimiter().length());
	}

	/**
	 * Returns the {@link FormControl} defined by this start tag.
	 * <p>
	 * This is equivalent to {@link #getElement()}<code>.</code>{@link Element#getFormControl() getFormControl()}.
	 *
	 * @return the {@link FormControl} defined by this start tag, or <code>null</code> if it is not a <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#form-controls">control</a>.
	 */
	public FormControl getFormControl() {
		return getElement().getFormControl();
	}

	/**
	 * Indicates whether a matching end tag is forbidden.
	 * <p>
	 * This property returns <code>true</code> if one of the following conditions is met:
	 * <ul>
	 *  <li>The {@linkplain #getStartTagType() type} of this start tag does not specify a
	 *   {@linkplain StartTagType#getCorrespondingEndTagType() corresponding end tag type}.
	 *  <li>The {@linkplain #getName() name} of this start tag indicates it is the start of an
	 *   <a href="Element.html#HTML">HTML element</a> whose {@linkplain HTMLElements#getEndTagForbiddenElementNames() end tag is forbidden}.
	 *  <li>This start tag is syntactically an {@linkplain #isEmptyElementTag() empty-element tag} and its
	 *   {@linkplain #getName() name} indicates it is the start of a <a href="HTMLElements.html#NonHTMLElement">non-HTML element</a>.
	 * </ul>
	 * <p>
	 * If this property returns <code>true</code> then this start tag's {@linkplain #getElement() element} will always be a
	 * <a href="Element.html#SingleTag">single tag element</a>.
	 *
	 * @return  <code>true</code> if a matching end tag is forbidden, otherwise <code>false</code>.
	 */
	public boolean isEndTagForbidden() {
		if (getStartTagType()!=StartTagType.NORMAL)
			return getStartTagType().getCorrespondingEndTagType()==null;
		if (HTMLElements.getEndTagForbiddenElementNames().contains(name)) return true;
		if (HTMLElements.getElementNames().contains(name)) return false;
		return isEmptyElementTag();
	}

	/**
	 * Indicates whether a matching end tag is required.
	 * <p>
	 * This property returns <code>true</code> if one of the following conditions is met:
	 * <ul>
	 *  <li>The {@linkplain #getStartTagType() type} of this start tag is NOT {@link StartTagType#NORMAL}, but specifies a
	 *   {@linkplain StartTagType#getCorrespondingEndTagType() corresponding end tag type}.
	 *  <li>The {@linkplain #getName() name} of this start tag indicates it is the start of an
	 *   <a href="Element.html#HTML">HTML element</a> whose {@linkplain HTMLElements#getEndTagRequiredElementNames() end tag is required}.
	 *  <li>This start tag is syntactically NOT an {@linkplain #isEmptyElementTag() empty-element tag} and its
	 *   {@linkplain #getName() name} indicates it is the start of a <a href="HTMLElements.html#NonHTMLElement">non-HTML element</a>.
	 * </ul>
	 *
	 * @return  <code>true</code> if a matching end tag is required, otherwise <code>false</code>.
	 */
	public boolean isEndTagRequired() {
		if (getStartTagType()!=StartTagType.NORMAL)
			return getStartTagType().getCorrespondingEndTagType()!=null;
		if (HTMLElements.getEndTagRequiredElementNames().contains(name)) return true;
		if (HTMLElements.getElementNames().contains(name)) return false;
		return !isEmptyElementTag();
	}

	// Documentation inherited from Tag
	public boolean isUnregistered() {
		return startTagType==StartTagType.UNREGISTERED;
	}

	/**
	 * Returns an XML representation of this start tag.
	 * <p>
	 * This is equivalent to {@link #tidy(boolean) tidy(false)}, thereby keeping the {@linkplain #getName() name} of the tag in its original case.
	 * <p>
	 * See the documentation of the {@link #tidy(boolean toXHTML)} method for more details.
	 *
	 * @return an XML representation of this start tag, or the {@linkplain Segment#toString() source text} if it is of a {@linkplain #getStartTagType() type} that does not {@linkplain StartTagType#hasAttributes() have attributes}.
	 */
	public String tidy() {
		return tidy(false);
	}

	/**
	 * Returns an XML or XHTML representation of this start tag.
	 * <p>
	 * The tidying of the tag is carried out as follows:
	 * <ul>
	 *  <li>if this start tag is of a {@linkplain #getStartTagType() type} that does not {@linkplain StartTagType#hasAttributes() have attributes},
	 *   then the original {@linkplain Segment#toString() source text} is returned.
	 *  <li>name converted to lower case if the <code>toXHTML</code> argument is <code>true</code> and this is a {@linkplain StartTagType#NORMAL normal} start tag
	 *  <li>attributes separated by a single space
	 *  <li>attribute names in original case
	 *  <li>attribute values are enclosed in double quotes and {@linkplain CharacterReference#reencode(CharSequence) re-encoded}
	 *  <li>if this start tag forms an <a href="Element.html#HTML">HTML element</a> that has no {@linkplain Element#getEndTag() end tag},
	 *   a slash is inserted before the closing angle bracket, separated from the {@linkplain #getName() name} or last attribute by a single space.
	 *  <li>if an attribute value contains a {@linkplain TagType#isServerTag() server tag} it is inserted verbatim instead of being
	 *   {@linkplain CharacterReference#encode(CharSequence) encoded}.
	 * </ul>
	 * <p>
	 * The <code>toXHTML</code> parameter determines only whether the name is converted to lower case for {@linkplain StartTagType#NORMAL normal} tags.
	 * In all other respects the generated tag is already valid XHTML.
	 * <h4>Example:</h4>
	 * The following source text:
	 * <p>
	 * <code>&lt;INPUT name=Company value='G&amp;uuml;nter O&amp#39;Reilly &amp;amp Associ&eacute;s'&gt;</code>
	 * <p>
	 * produces the following regenerated HTML:
	 * <p>
	 * <code>&lt;input name="Company" value="G&amp;uuml;nter O'Reilly &amp;amp; Associ&amp;eacute;s" /&gt;</code>
	 *
	 * @param toXHTML  specifies whether the output is XHTML.
	 * @return an XML or XHTML representation of this start tag, or the {@linkplain Segment#toString() source text} if it is of a {@linkplain #getStartTagType() type} that does not {@linkplain StartTagType#hasAttributes() have attributes}.
	 */
	public String tidy(boolean toXHTML) {
		if (attributes==null) return toString();
		final StringBuffer sb=new StringBuffer();
		sb.append('<');
		if (toXHTML && startTagType==StartTagType.NORMAL) {
			sb.append(name);
		} else {
			int i=begin+startTagType.startDelimiterPrefix.length();
			final int nameSegmentEnd=i+name.length();
			while (i<nameSegmentEnd) {
				sb.append(source.charAt(i));
				i++;
			}
		}
		attributes.appendTidy(sb,findNextTag());
		if (startTagType==StartTagType.NORMAL && getElement().getEndTag()==null && !HTMLElements.getEndTagOptionalElementNames().contains(name)) sb.append(" /");
		sb.append(startTagType.getClosingDelimiter());
		return sb.toString();
	}

	/**
	 * Generates the HTML text of a {@linkplain StartTagType#NORMAL normal} start tag with the specified tag name and {@linkplain Attributes#populateMap(Map,boolean) attributes map}.
	 * <p>
	 * The output of the attributes is as described in the {@link Attributes#generateHTML(Map attributesMap)} method.
	 * <p>
	 * The <code>emptyElementTag</code> parameter specifies whether the start tag should be an
	 * <a target="_blank" href="http://www.w3.org/TR/REC-xml#dt-eetag">empty-element tag</a>,
	 * in which case a slash is inserted before the closing angle bracket, separated from the name
	 * or last attribute by a single space.
	 * <h4>Example:</h4>
	 * The following code:
	 * <pre>
	 * LinkedHashMap attributesMap=new LinkedHashMap();
	 * attributesMap.put("name","Company");
	 * attributesMap.put("value","G\n00fcnter O'Reilly & Associ&eacute;s");
	 * System.out.println(StartTag.generateHTML("INPUT",attributesMap,true));</pre>
	 * generates the following output:
	 * <p>
	 * <code>&lt;INPUT name="Company" value="G&amp;uuml;nter O'Reilly &amp;amp; Associ&amp;eacute;s" /&gt;</code>
	 *
	 * @param tagName  the name of the start tag.
	 * @param attributesMap  a map containing attribute name/value pairs.
	 * @param emptyElementTag  specifies whether the start tag should be an <a target="_blank" href="http://www.w3.org/TR/REC-xml#dt-eetag">empty-element tag</a>.
	 * @return the HTML text of a {@linkplain StartTagType#NORMAL normal} start tag with the specified tag name and {@linkplain Attributes#populateMap(Map,boolean) attributes map}.
	 * @see EndTag#generateHTML(String tagName)
	 */
	public static String generateHTML(final String tagName, final Map attributesMap, final boolean emptyElementTag) {
		final StringWriter stringWriter=new StringWriter();
		final StringBuffer sb=stringWriter.getBuffer();
		sb.append('<').append(tagName);
		try {Attributes.appendHTML(stringWriter,attributesMap);} catch (IOException ex) {} // IOException never occurs in StringWriter
		if (emptyElementTag)
			sb.append(" />");
		else
			sb.append('>');
		return sb.toString();
	}

	public String getDebugInfo() {
		final StringBuffer sb=new StringBuffer();
		appendStartTagDebugInfo(sb);
		sb.append(super.getDebugInfo());
		return sb.toString();
	}

	StringBuffer appendStartTagDebugInfo(final StringBuffer sb) {
		sb.append('"').append(name).append("\" ");
		if (startTagType!=StartTagType.NORMAL) sb.append('(').append(startTagType.getDescription()).append(") ");
		return sb;
	}

	/**
	 * Returns an XML representation of this start tag.
	 * <p>
	 * This method has been deprecated as of version 2.2 and replaced with the exactly equivalent {@link #tidy()} method.
	 *
	 * @return an XML representation of this start tag, or the {@linkplain Segment#toString() source text} if it is of a {@linkplain #getStartTagType() type} that does not {@linkplain StartTagType#hasAttributes() have attributes}
	 * @deprecated  Use {@link #tidy()} instead.
	 */
	public String regenerateHTML() {
		return tidy();
	}

	/**
	 * Indicates whether a matching end tag is <i>optional</i> according to the HTML 4.01 specification.
	 * <p>
	 * This method has been deprecated as of version 2.0 and replaced with the {@link HTMLElements#getEndTagOptionalElementNames()}
	 * static method.
	 * <p>
	 * This property is only relevant to start tags forming part of an <a href="Element.html#HTML">HTML element</a>
	 * and returns <code>false</code> in all other cases.
	 *
	 * @return  <code>true</code> if a matching end tag is <i>optional</i> according to the HTML 4.01 specification, otherwise <code>false</code>.
	 * @deprecated  Use {@link #getStartTagType()}<code>==</code>{@link StartTagType#NORMAL}<code> && </code>{@link HTMLElements#getEndTagOptionalElementNames()}<code>.contains(</code>{@link #getName() getName()}<code>)</code> instead.
	 */
	public boolean isEndTagOptional() {
		return getStartTagType()==StartTagType.NORMAL && HTMLElements.getEndTagOptionalElementNames().contains(name);
	}

	/**
	 * Returns the end tag that corresponds to this start tag.
	 * <p>
	 * This method has been deprecated as of version 2.0 as it has existed mainly for backward compatability with version 1.0.
	 * <p>
	 * The {@link #getElement()} method is much more useful as it determines the span of the
	 * element even if the end tag is {@linkplain #isEndTagOptional() optional} and is not present in the source document.
	 * <p>
	 * This method on the other hand just returns <code>null</code> in the above case, revealing nothing about where the element ends.
	 *
	 * @return the end tag that corresponds to this start tag, or <code>null</code> if it does not exist in the source document.
	 * @deprecated  Use {@link #getElement()}<code>.</code>{@link Element#getEndTag() getEndTag()} instead.
	 */
	public EndTag findEndTag() {
		return getElement().getEndTag();
	}

	/**
	 * Returns the {@link FormControlType} of this start tag.
	 * <p>
	 * This method has been deprecated as of version 2.0 as it is no longer used internally and
	 * has no practical use as a public method.
	 *
	 * @return the form control type of this start tag, or <code>null</code> if it is not a <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#form-controls">control</a>.
	 * @see Element#getFormControl()
	 * @deprecated  Use {@link #getFormControl()}<code>.</code>{@link FormControl#getFormControlType() getFormControlType()} instead.
	 */
	public FormControlType getFormControlType() {
		final FormControl formControl=getFormControl();
		if (formControl==null) return null;
		return formControl.getFormControlType();
	}

	/**
	 * Returns the segment containing the text that immediately follows this start tag up until the start of the following tag.
	 * <p>
	 * Guaranteed not <code>null</code>.
	 * <p>
 	 * This method has been deprecated as of version 2.0 as it is no longer used internally and
	 * has no practical use as a public method.
	 *
	 * @return the segment containing the text that immediately follows this start tag up until the start of the following tag.
	 * @deprecated  Use {@link Segment#Segment(Source,int,int) new Segment}<code>(source,</code>{@link #getEnd() getEnd()}<code>,</code>{@link #findNextTag() findNextTag()}<code>.</code>{@link #getBegin() getBegin()}<code>)</code> instead.
	 */
	public Segment getFollowingTextSegment() {
		int endData=source.getParseText().indexOf('<',end);
		if (endData==-1) endData=source.end;
		return new Segment(source,end,endData);
	}

	/**
	 * Indicates whether the start tag is a server tag.
	 * <p>
	 * This method has been deprecated as of version 2.0 as its functionality is now easily performed without a dedicated method.
	 *
	 * @return <code>true</code> if the start tag is a server tag, otherwise <code>false</code>.
	 * @deprecated  Use {@link #getTagType() getTagType()}.{@link TagType#isServerTag() isServerTag()} instead.
	 */
	public boolean isServerTag() {
		return getTagType().isServerTag();
	}

	/**
	 * Indicates whether this start tag is of type {@link StartTagType#COMMENT}.
	 * <p>
	 * This method has been deprecated as of version 2.0 as its functionality is now easily performed without a dedicated method.
	 *
	 * @return <code>true</code> if this start tag is of type {@link StartTagType#COMMENT}, otherwise <code>false</code>.
	 * @deprecated  Use {@link #getTagType() getTagType()}<code>==</code>{@link StartTagType#COMMENT} instead.
	 */
	public boolean isComment() {
		return startTagType==StartTagType.COMMENT;
	}

	/**
	 * Indicates whether this start tag has a {@linkplain #getTagType() type} of {@link StartTagType#XML_PROCESSING_INSTRUCTION} or is any other tag starting with "&lt;?".
	 * <p>
	 * This method has been deprecated as of version 2.0 as its functionality is now easily performed without a dedicated method.
	 *
	 * @return <code>true</code> if this start tag has a {@linkplain #getTagType() type} of {@link StartTagType#XML_PROCESSING_INSTRUCTION} or is any other tag starting with "&lt;?", otherwise <code>false</code>.
	 * @deprecated  Use <code>charAt(1)=='?'</code> instead for backward compatibility.
	 */
	public boolean isProcessingInstruction() {
		return charAt(1)=='?';
	}

	/**
	 * Indicates whether this start tag has a {@linkplain #getTagType() type} of {@link StartTagType#XML_DECLARATION}.
	 * <p>
	 * This method has been deprecated as of version 2.0 as its functionality is now easily performed without a dedicated method.
	 *
	 * @return <code>true</code> if this start tag has a {@linkplain #getTagType() type} of {@link StartTagType#XML_DECLARATION}, otherwise <code>false</code>.
	 * @deprecated  Use {@link #getTagType() getTagType()}<code>==</code>{@link StartTagType#XML_DECLARATION} instead.
	 */
	public boolean isXMLDeclaration() {
		return startTagType==StartTagType.XML_DECLARATION;
	}

	/**
	 * Indicates whether this start tag has a {@linkplain #getTagType() type} of {@link StartTagType#DOCTYPE_DECLARATION}.
	 * <p>
	 * This method has been deprecated as of version 2.0 as its functionality is now easily performed without a dedicated method.
	 *
	 * @return <code>true</code> if this start tag has a {@linkplain #getTagType() type} of {@link StartTagType#DOCTYPE_DECLARATION}, otherwise <code>false</code>.
	 * @deprecated  Use {@link #getTagType() getTagType()}<code>==</code>{@link StartTagType#DOCTYPE_DECLARATION} instead.
	 */
	public boolean isDocTypeDeclaration() {
		return startTagType==StartTagType.DOCTYPE_DECLARATION;
	}

	/**
	 * Indicates whether this start tag has a {@linkplain #getTagType() type} of {@linkplain StartTagType#SERVER_COMMON}.
	 * <p>
	 * This method has been deprecated as of version 2.0 as its functionality is now easily performed without a dedicated method.
	 *
	 * @return <code>true</code> if this start tag has a {@linkplain #getTagType() type} of {@linkplain StartTagType#SERVER_COMMON}, otherwise <code>false</code>.
	 * @deprecated  Use {@link #getTagType() getTagType()}<code>==</code>{@link StartTagType#SERVER_COMMON} instead.
	 */
	public boolean isCommonServerTag() {
		return startTagType==StartTagType.SERVER_COMMON;
	}

	/**
	 * Indicates whether this start tag has a {@linkplain #getTagType() type} of {@link PHPTagTypes#PHP_STANDARD}.
	 * <p>
	 * This method has been deprecated as of version 2.0 as its functionality is now easily performed without a dedicated method.
	 *
	 * @return <code>true</code> if this start tag has a {@linkplain #getTagType() type} of {@link PHPTagTypes#PHP_STANDARD}, otherwise <code>false</code>.
	 * @deprecated  Use {@link #getTagType() getTagType()}<code>==</code>{@link PHPTagTypes#PHP_STANDARD} instead.
	 */
	public boolean isPHPTag() {
		return startTagType==PHPTagTypes.PHP_STANDARD;
	}

	/**
	 * Indicates whether this start tag would be {@linkplain MasonTagTypes#isParsedByMason(TagType) parsed by a Mason server}.
	 * <p>
	 * This method has been deprecated as of version 2.0 as its functionality is now easily performed without a dedicated method.
	 *
	 * @return <code>true</code> if this start tag would be {@linkplain MasonTagTypes#isParsedByMason(TagType) parsed by a Mason server}, otherwise <code>false</code>.
	 * @deprecated  Use {@link MasonTagTypes}<code>.</code>{@link MasonTagTypes#isParsedByMason(TagType) isParsedByMason}<code>(</code>{@link #getTagType() getTagType()}<code>)</code> instead.
	 */
	public boolean isMasonTag() {
		return startTagType==StartTagType.SERVER_COMMON || startTagType==MasonTagTypes.MASON_NAMED_BLOCK || startTagType==MasonTagTypes.MASON_COMPONENT_CALL || startTagType==MasonTagTypes.MASON_COMPONENT_CALLED_WITH_CONTENT;
	}

	/**
	 * Indicates whether this start tag has a {@linkplain #getTagType() type} of {@linkplain MasonTagTypes#MASON_NAMED_BLOCK}.
	 * <p>
	 * This method has been deprecated as of version 2.0 as its functionality is now easily performed without a dedicated method.
	 *
	 * @return <code>true</code> if this start tag has a {@linkplain #getTagType() type} of {@linkplain MasonTagTypes#MASON_NAMED_BLOCK}, otherwise <code>false</code>.
	 * @deprecated  Use {@link #getTagType() getTagType()}<code>==</code>{@link MasonTagTypes#MASON_NAMED_BLOCK} instead.
	 */
	public boolean isMasonNamedBlock() {
		return startTagType==MasonTagTypes.MASON_NAMED_BLOCK;
	}

	/**
	 * Indicates whether this start tag has a {@linkplain #getTagType() type} of {@linkplain MasonTagTypes#MASON_COMPONENT_CALL}.
	 * <p>
	 * This method has been deprecated as of version 2.0 as its functionality is now easily performed without a dedicated method.
	 *
	 * @return <code>true</code> if this start tag has a {@linkplain #getTagType() type} of {@linkplain MasonTagTypes#MASON_COMPONENT_CALL}, otherwise <code>false</code>.
	 * @deprecated  Use {@link #getTagType() getTagType()}<code>==</code>{@link MasonTagTypes#MASON_COMPONENT_CALL} instead.
	 */
	public boolean isMasonComponentCall() {
		return startTagType==MasonTagTypes.MASON_COMPONENT_CALL;
	}

	/**
	 * Indicates whether this start tag has a {@linkplain #getTagType() type} of {@linkplain MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT}.
	 * <p>
	 * This method has been deprecated as of version 2.0 as its functionality is now easily performed without a dedicated method.
	 *
	 * @return <code>true</code> if this start tag has a {@linkplain #getTagType() type} of {@linkplain MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT}, otherwise <code>false</code>.
	 * @deprecated  Use {@link #getTagType() getTagType()}<code>==</code>{@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT} instead.
	 */
	public boolean isMasonComponentCalledWithContent() {
		return startTagType==MasonTagTypes.MASON_COMPONENT_CALLED_WITH_CONTENT;
	}

	private EndTag findEndTagInternal() {
		boolean checkForEmptyElementTag=true;
		// A missing optional end tag returns a zero length EndTag instead of null
		if (startTagType==StartTagType.NORMAL) {
			final HTMLElementTerminatingTagNameSets terminatingTagNameSets=HTMLElements.getTerminatingTagNameSets(name);
			if (terminatingTagNameSets!=null) // end tag is optional
				return findOptionalEndTag(terminatingTagNameSets);
			if (HTMLElements.getEndTagForbiddenElementNames().contains(name)) // end tag is forbidden
				return null;
			checkForEmptyElementTag=!HTMLElements.getEndTagRequiredElementNames().contains(name); // check for empty-element tags if tag is not an HTML element
			if (checkForEmptyElementTag && isEmptyElementTag()) // non-html empty-element tag
				return null; 
		} else if (startTagType.getCorrespondingEndTagType()==null) {
			return null;
		}
		// This is either a start tag type other than NORMAL that requires an end tag, or an HTML element tag that requires an end tag,
		// or a non-HTML element tag that is not an empty-element tag.
		// In all of these cases the end tag is required.
		final EndTag nextEndTag=source.findNextEndTag(end,name,startTagType.getCorrespondingEndTagType());
		if (nextEndTag!=null) {
			if (HTMLElements.END_TAG_REQUIRED_NESTING_FORBIDDEN_SET.contains(name)) {
				final StartTag nextStartTag=source.findNextStartTag(end,name);
				if (nextStartTag==null || nextStartTag.begin>nextEndTag.begin) return nextEndTag;
				if (source.isLoggingEnabled()) source.log(source.getRowColumnVector(begin).appendTo(new StringBuffer(200).append("StartTag at ")).append(" missing required end tag - invalid nested start tag encountered before end tag").toString());
				// Terminate the element at the start of the invalidly nested start tag.
				// This is how IE and Mozilla treat illegally nested A elements, but other elements may vary.
				return new EndTag(source,nextStartTag.begin,nextStartTag.begin,EndTagType.NORMAL,name);
			}
			final Segment[] findResult=findEndTag(nextEndTag,checkForEmptyElementTag);
			if (findResult!=null) return (EndTag)findResult[0];
		}
		if (source.isLoggingEnabled()) source.log(source.getRowColumnVector(begin).appendTo(new StringBuffer(200).append("StartTag at ")).append(" missing required end tag").toString());
		return null;
	}

	private EndTag findOptionalEndTag(final HTMLElementTerminatingTagNameSets terminatingTagNameSets) {
		int pos=end;
		while (pos<source.end) {
			final Tag tag=Tag.findPreviousOrNextTag(source,pos,false);
			if (tag==null) break;
			Set terminatingTagNameSet;
			if (tag instanceof EndTag) {
				if (tag.name==name) return (EndTag)tag;
				terminatingTagNameSet=terminatingTagNameSets.TerminatingEndTagNameSet;
			} else {
				terminatingTagNameSet=terminatingTagNameSets.NonterminatingElementNameSet;
				if (terminatingTagNameSet!=null && terminatingTagNameSet.contains(tag.name)) {
					Element nonterminatingElement=((StartTag)tag).getElement();
					pos=nonterminatingElement.end;
					continue;
				}
				terminatingTagNameSet=terminatingTagNameSets.TerminatingStartTagNameSet;
			}
			if (terminatingTagNameSet!=null && terminatingTagNameSet.contains(tag.name)) return new EndTag(source,tag.begin,tag.begin,EndTagType.NORMAL,name);
			pos=tag.begin+1;
		}
		// Ran out of tags. The only legitimate case of this happening is if the HTML end tag is missing, in which case the end of the element is the end of the source document
		return new EndTag(source,source.end,source.end,EndTagType.NORMAL,name);
	}

	static StartTag findPreviousOrNext(final Source source, final int pos, final String searchName, final boolean isXMLTagName, final boolean previous) {
		// searchName is already in lower case
		if (searchName==null) return findPreviousOrNext(source,pos,previous);
		if (searchName.length()==0) throw new IllegalArgumentException("searchName argument must not be zero length");
		final char[] startDelimiterCharArray=new char[searchName.length()+1];
		startDelimiterCharArray[0]='<';
		for (int i=1; i<startDelimiterCharArray.length; i++) startDelimiterCharArray[i]=searchName.charAt(i-1);
		if (startDelimiterCharArray[1]=='/') throw new IllegalArgumentException("searchName argument \""+searchName+"\" must not start with '/'");
		try {
			final ParseText parseText=source.getParseText();
			int begin=pos;
			do {
				begin=previous?parseText.lastIndexOf(startDelimiterCharArray,begin):parseText.indexOf(startDelimiterCharArray,begin);
				if (begin==-1) return null;
				final StartTag startTag=(StartTag)Tag.getTagAt(source,begin);
				if (startTag==null || (isXMLTagName && startTag.isUnregistered())) continue;
				if (startTag.startTagType.isNameAfterPrefixRequired() && startTag.name.length()>searchName.length()) {
					// The name of the start tag is longer than the search name, and the type of tag indicates 
					// that we are probably looking for an exact match.
					// (eg searchName="a", startTag.name="applet" -> reject)
					// We only require an exact match if the last character of the search name is part of the name, as the
					// search name might be just the prefix of a server tag.
					// (eg searchName="?", startTag.name="?abc" -> accept, but searchName="?a", startTag.name="?abc" -> reject)
					// The only exception to this is if the last character of the search name is a colon (which also forms part of
					// the name), but signifies that we want to search on the entire namespace.
					// (eg searchName="o:", startTag.name="o:p" -> accept)
					char lastSearchNameChar=searchName.charAt(searchName.length()-1);
					if (lastSearchNameChar!=':' && isXMLNameChar(lastSearchNameChar)) continue;
				}
				return startTag;
			} while (previous ? (begin-=2)>=0 : (begin+=1)<source.end);
		} catch (IndexOutOfBoundsException ex) {
			// this should only happen when the end of file is reached in the middle of a tag.
			// we don't have to do anything to handle it as there are no more tags anyway.
		}
		return null;
	}

	static StartTag findPreviousOrNext(final Source source, int pos, final boolean previous) {
		while (true) {
			final Tag tag=Tag.findPreviousOrNextTag(source,pos,previous);
			if (tag==null) return null;
			if (tag instanceof StartTag) return (StartTag)tag;
			pos+=previous?-1:1;
		}
	}
	
	static StartTag findNext(final Source source, int pos, final String attributeName, final String value, final boolean valueCaseSensitive) {
		if (value==null) throw new IllegalArgumentException();
		final char[] valueCharArray=value.toLowerCase().toCharArray();
		final ParseText parseText=source.getParseText();
		while (pos<source.end) {
			pos=parseText.indexOf(valueCharArray,pos);
			if (pos==-1) return null;
			final Tag tag=source.findEnclosingTag(pos);
			if (tag==null || !(tag instanceof StartTag)) {
				pos++;
				continue;
			}
			final StartTag startTag=(StartTag)tag;
			if (startTag.getAttributes()!=null) {
				final String attributeValue=startTag.getAttributes().getValue(attributeName);
				if (attributeValue!=null) {
					if (value.equals(attributeValue)) return startTag;
					if (value.equalsIgnoreCase(attributeValue)) {
						if (!valueCaseSensitive) return startTag;
						if (source.isLoggingEnabled()) source.log(source.getRowColumnVector(pos).appendTo(new StringBuffer(200)).append(": StartTag with attribute ").append(attributeName).append("=\"").append(attributeValue).append("\" ignored during search because its case does not match search value \"").append(value).append('"').toString());
					}
				}
			}
			pos=startTag.end+5; // next attribute value can't be less than 5 chars after last start tag
		}
		return null;
	}

	private Segment[] findEndTag(final EndTag nextEndTag, final boolean checkForEmptyElementTag) {
		StartTag nextStartTag=source.findNextStartTag(end,name);
		if (checkForEmptyElementTag) {
			while (nextStartTag!=null && nextStartTag.isEmptyElementTag())
				nextStartTag=source.findNextStartTag(nextStartTag.end,name);
		}
		return findEndTag(end,nextStartTag,nextEndTag,checkForEmptyElementTag);
	}

	private Segment[] findEndTag(final int afterPos, StartTag nextStartTag, EndTag nextEndTag, final boolean checkForEmptyElementTag) {
		// returns null if no end tag exists in the rest of the file, otherwise the following two segments:
		// first is the matching end tag to this start tag.  Must be present if array is returned.
		// second is the next occurrence after the returned end tag of a start tag of the same name. (null if none exists)
		if (nextEndTag==null) return null;  // no end tag in the rest of the file
		final Segment[] returnArray={nextEndTag, nextStartTag};
		if (nextStartTag==null || nextStartTag.begin>nextEndTag.begin) return returnArray;  // no more start tags of the same name in rest of file, or they occur after the end tag that we found.  This means we have found the matching end tag.
		final Segment[] findResult=nextStartTag.findEndTag(nextEndTag,checkForEmptyElementTag);  // find the matching end tag to the interloping start tag
		if (findResult==null) return null;  // no end tag in the rest of the file
		final EndTag nextStartTagsEndTag=(EndTag)findResult[0];
		nextStartTag=(StartTag)findResult[1];
		nextEndTag=source.findNextEndTag(nextStartTagsEndTag.end, name);  // find end tag after the interloping start tag's end tag
		return findEndTag(nextStartTagsEndTag.end,nextStartTag,nextEndTag,checkForEmptyElementTag);  // recurse to see if this is the matching end tag
	}
}
