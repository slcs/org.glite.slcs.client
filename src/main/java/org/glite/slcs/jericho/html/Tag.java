/*
 * Copyright (c) 2010-2013 SWITCH
 * Copyright (c) 2006-2010 Members of the EGEE Collaboration
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Represents either a {@link StartTag} or {@link EndTag} in a specific {@linkplain Source source} document.
 *
 * <h3><a name="ParsingProcess">Tag Parsing Process</a></h3>
 * The following process describes how each tag is identified by the parser:
 * <ol class="Separated">
 *  <li>
 *   Every '<code>&lt;</code>' character found in the source document is considered to be the start of a tag.
 *   The characters following it are compared with the {@linkplain TagType#getStartDelimiter() start delimiters}
 *   of all the {@linkplain TagType#register() registered} {@linkplain TagType tag types}, and a list of matching tag types
 *   is determined.
 *  <li>
 *   A more detailed analysis of the source is performed according to the features of each matching tag type from the first step,
 *   in order of <a href="TagType.html#Precedence">precedence</a>, until a valid tag is able to be constructed.
 *   <p>
 *   The analysis performed in relation to each candidate tag type is a two-stage process:
 *   <ol>
 *    <li>
 *     The position of the tag is checked to determine whether it is {@linkplain TagType#isValidPosition(Source,int) valid}.
 *     In theory, a {@linkplain TagType#isServerTag() server tag} is valid in any position, but a non-server tag is not valid inside another non-server tag.
 *     <p>
 *     The {@link TagType#isValidPosition(Source, int pos)} method is responsible for this check and has a common default implementation for all tag types
 *     (although <a href="TagType.html#custom">custom</a> tag types can override it if necessary).
 *     Its behaviour differs depending on whether or not a {@linkplain Source#fullSequentialParse() full sequential parse} is peformed.
 *     See the documentation of the {@link TagType#isValidPosition(Source,int) isValidPosition} method for full details.
 *    <li>
 *     A final analysis is performed by the {@link TagType#constructTagAt(Source, int pos)} method of the candidate tag type.
 *     This method returns a valid {@link Tag} object if all conditions of the candidate tag type are met, otherwise it returns
 *     <code>null</code> and the process continues with the next candidate tag type.
 *   </ol>
 *  <li>
 *   If the source does not match the start delimiter or syntax of any registered tag type, the segment spanning it and the next
 *   '<code>&gt;</code>' character is taken to be an {@linkplain #isUnregistered() unregistered} tag.
 *   Some tag search methods ignore unregistered tags.  See the {@link #isUnregistered()} method for more information.
 * </ol>
 * <p>
 * See the documentation of the {@link TagType} class for more details on how tags are recognised.
 *
 * <h3><a name="TagSearchMethods">Tag Search Methods</a></h3>
 * <p>
 * Methods that find tags in a source document are collectively referred to as <i>Tag Search Methods</i>.
 * They are found mostly in the {@link Source} and {@link Segment} classes, and can be generally categorised as follows:
 * <dl class="Separated">
 *  <dt><a name="OpenSearch">Open Search:</a>
 *   <dd>These methods search for tags of any {@linkplain #getName() name} and {@linkplain #getTagType() type}.
 *    <ul class="Unseparated">
 *     <li>{@link Tag#findNextTag()}
 *     <li>{@link Tag#findPreviousTag()}
 *     <li>{@link Segment#findAllElements()}
 *     <li>{@link Segment#findAllTags()}
 *     <li>{@link Source#getTagAt(int pos)}
 *     <li>{@link Source#findPreviousTag(int pos)}
 *     <li>{@link Source#findNextTag(int pos)}
 *     <li>{@link Source#findEnclosingTag(int pos)}
 *     <li>{@link Segment#findAllStartTags()}
 *     <li>{@link Source#findPreviousStartTag(int pos)}
 *     <li>{@link Source#findNextStartTag(int pos)}
 *     <li>{@link Source#findPreviousEndTag(int pos)}
 *     <li>{@link Source#findNextEndTag(int pos)}
 *    </ul>
 *  <dt><a name="NamedSearch">Named Search:</a>
 *   <dd>These methods usually include a parameter called <code>name</code> which is used to specify the {@linkplain #getName() name} of the
 *    tag to search for.  In some cases named search methods do not require this parameter because the context or name of the method implies
 *    the name to search for.
 *    In tag search methods specifically looking for start tags, specifying a name that ends in a colon (<code>:</code>)
 *    searches for all start tags in the specified XML namespace.
 *    <ul class="Unseparated">
 *     <li>{@link Segment#findAllElements(String name)}
 *     <li>{@link Segment#findAllStartTags(String name)}
 *     <li>{@link Source#findPreviousStartTag(int pos, String name)}
 *     <li>{@link Source#findNextStartTag(int pos, String name)}
 *     <li>{@link Source#findPreviousEndTag(int pos, String name)}
 *     <li>{@link Source#findNextEndTag(int pos, String name)}
 *     <li>{@link Source#findNextEndTag(int pos, String name, EndTagType)}
 *    </ul>
 *  <dt><a name="TagTypeSearch">Tag Type Search:</a>
 *   <dd>These methods usually include a parameter called <code>tagType</code> which is used to specify the {@linkplain #getTagType() type} of the
 *    tag to search for.  In some methods the search parameter is restricted to the {@link StartTagType} subclass of <code>TagType</code>.
 *    <ul class="Unseparated">
 *     <li>{@link Segment#findAllElements(StartTagType)}
 *     <li>{@link Segment#findAllTags(TagType)}
 *     <li>{@link Source#findPreviousTag(int pos, TagType)}
 *     <li>{@link Source#findNextTag(int pos, TagType)}
 *     <li>{@link Source#findEnclosingTag(int pos, TagType)}
 *     <li>{@link Source#findNextEndTag(int pos, String name, EndTagType)}
 *    </ul>
 *  <dt><a name="OtherSearch">Other Search:</a>
 *   <dd>A small number of methods do not fall into any of the above categories, such as the methods that search on
 *    {@linkplain Source#findNextStartTag(int pos, String attributeName, String value, boolean valueCaseSensitive) attribute values}.
 *    <ul class="Unseparated">
 *     <li>{@link Segment#findAllStartTags(String attributeName, String value, boolean valueCaseSensitive)}
 *     <li>{@link Source#findNextStartTag(int pos, String attributeName, String value, boolean valueCaseSensitive)}
 *    </ul>
 * </dl>
 */
public abstract class Tag extends Segment implements HTMLElementName {
	String name=null; // always lower case, can always use == operator to compare with constants in HTMLElementName interface
	Element element=Element.NOT_CACHED; // cache
	int allTagsArrayIndex=-1;
	private Object userData=null;

	/**
	 * {@linkplain StartTagType#XML_PROCESSING_INSTRUCTION XML processing instruction}
	 * @deprecated  Use {@link StartTagType#XML_PROCESSING_INSTRUCTION} in combination with <a href="#TagTypeSearch">tag type search</a> methods instead.
	 */
	public static final String PROCESSING_INSTRUCTION=StartTagType.XML_PROCESSING_INSTRUCTION.getNamePrefixForTagConstant();

	/**
	 * {@linkplain StartTagType#XML_DECLARATION XML declaration}
	 * @deprecated  Use {@link StartTagType#XML_DECLARATION} in combination with <a href="#TagTypeSearch">tag type search</a> methods instead.
	 */
	public static final String XML_DECLARATION=StartTagType.XML_DECLARATION.getNamePrefixForTagConstant();

	/**
	 * {@linkplain StartTagType#DOCTYPE_DECLARATION document type declaration}
	 * @deprecated  Use {@link StartTagType#DOCTYPE_DECLARATION} in combination with <a href="#TagTypeSearch">tag type search</a> methods instead.
	 */
	public static final String DOCTYPE_DECLARATION=StartTagType.DOCTYPE_DECLARATION.getNamePrefixForTagConstant();

	/**
	 * {@linkplain PHPTagTypes#PHP_STANDARD Standard PHP} tag (<code>&lt;&#63;php &#46;&#46;&#46; &#63;&gt;</code>)
	 * @deprecated  Use {@link PHPTagTypes#PHP_STANDARD} in combination with <a href="#TagTypeSearch">tag type search</a> methods instead.
	 */
	public static final String SERVER_PHP=PHPTagTypes.PHP_STANDARD.getNamePrefixForTagConstant();

	/**
	 * Common {@linkplain StartTagType#SERVER_COMMON server} tag (<code>&lt;% &#46;&#46;&#46; %&gt;</code>)
	 * @deprecated  Use {@link StartTagType#SERVER_COMMON} in combination with <a href="#TagTypeSearch">tag type search</a> methods instead.
	 */
	public static final String SERVER_COMMON=StartTagType.SERVER_COMMON.getNamePrefixForTagConstant();

	/**
	 * {@linkplain MasonTagTypes#MASON_NAMED_BLOCK Mason named block} (<code>&lt;%<i>name</i> &#46;&#46;&#46; &gt; &#46;&#46;&#46; &lt;/%<i>name</i>&gt;</code>)
	 * @deprecated  Use {@link MasonTagTypes#MASON_NAMED_BLOCK} in combination with <a href="#TagTypeSearch">tag type search</a> methods instead.
	 */
	public static final String SERVER_MASON_NAMED_BLOCK=MasonTagTypes.MASON_NAMED_BLOCK.getNamePrefixForTagConstant(); // NOTE: this value is the same value as SERVER_COMMON

	/**
	 * {@linkplain MasonTagTypes#MASON_COMPONENT_CALL Mason component call} (<code>&lt;&amp; &#46;&#46;&#46; &amp;&gt;</code>)
	 * @deprecated  Use {@link MasonTagTypes#MASON_COMPONENT_CALL} in combination with <a href="#TagTypeSearch">tag type search</a> methods instead.
	 */
	public static final String SERVER_MASON_COMPONENT_CALL=MasonTagTypes.MASON_COMPONENT_CALL.getNamePrefixForTagConstant();

	/**
	 * {@linkplain MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT Mason component called with content} (<code>&lt;&amp;| &#46;&#46;&#46; &amp;&gt; &#46;&#46;&#46; &lt;/&amp;&gt;</code>)
	 * @deprecated  Use {@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT} in combination with <a href="#TagTypeSearch">tag type search</a> methods instead.
	 */
	public static final String SERVER_MASON_COMPONENT_CALLED_WITH_CONTENT=MasonTagTypes.MASON_COMPONENT_CALLED_WITH_CONTENT.getNamePrefixForTagConstant();

	private static final boolean INCLUDE_UNREGISTERED_IN_SEARCH=false; // determines whether unregistered tags are included in searches

	Tag(final Source source, final int begin, final int end, final String name) {
		super(source, begin, end);
		this.name=HTMLElements.getConstantElementName(name.toLowerCase());
	}

	/**
	 * Returns the {@linkplain Element element} that is started or ended by this tag.
	 * <p>
	 * {@link StartTag#getElement()} is guaranteed not <code>null</code>.
	 * <p>
	 * {@link EndTag#getElement()} can return <code>null</code> if the end tag is not properly matched to a start tag.
	 *
	 * @return the {@linkplain Element element} that is started or ended by this tag.
	 */
	public abstract Element getElement();

	/**
	 * Returns the name of this tag, always in lower case.
	 * <p>
	 * The name always starts with the {@linkplain TagType#getNamePrefix() name prefix} defined in this tag's {@linkplain TagType type}.
	 * For some tag types, the name consists only of this prefix, while in others it must be followed by a valid
	 * <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Name">XML name</a>
	 * (see {@link StartTagType#isNameAfterPrefixRequired()}).
	 * <p>
	 * If the name is equal to one of the constants defined in the {@link HTMLElementName} interface, this method is guaranteed to return
	 * the constant itself.
	 * This allows comparisons to be performed using the <code>==</code> operator instead of the less efficient
	 * <code>String.equals(Object)</code> method.
	 * <p>
	 * For example, the following expression can be used to test whether a {@link StartTag} is from a
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-SELECT">SELECT</a></code> element:
	 * <br /><code>startTag.getName()==HTMLElementName.SELECT</code>
	 * <p>
	 * To get the name of this tag in its original case, use {@link #getNameSegment()}<code>.toString()</code>.
	 *
	 * @return the name of this tag, always in lower case.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Returns the segment spanning the {@linkplain #getName() name} of this tag.
	 * <p>
	 * The code <code>getNameSegment().toString()</code> can be used to retrieve the name of this tag in its original case.
	 * <p>
	 * Every call to this method constructs a new <code>Segment</code> object.
	 *
	 * @return the segment spanning the {@linkplain #getName() name} of this tag.
	 * @see #getName()
	 */
	public Segment getNameSegment() {
		final int nameSegmentBegin=begin+getTagType().startDelimiterPrefix.length();
		return new Segment(source,nameSegmentBegin,nameSegmentBegin+name.length());
	}

	/**
	 * Returns the {@linkplain TagType type} of this tag.	
	 * @return the {@linkplain TagType type} of this tag.	
	 */
	public abstract TagType getTagType();

	/**
	 * Returns the general purpose user data object that has previously been associated with this tag via the {@link #setUserData(Object)} method.
	 * <p>
	 * If {@link #setUserData(Object)} has not been called, this method returns <code>null</code>.
	 *
	 * @return the generic data object that has previously been associated with this tag via the {@link #setUserData(Object)} method.
	 */
	public Object getUserData() {
		return userData;
	}

	/**
	 * Associates the specified general purpose user data object with this tag.
	 * <p>
	 * This property can be useful for applications that need to associate extra information with tags.
	 * The object can be retrieved later via the {@link #getUserData()} method.
	 *
	 * @param userData  general purpose user data of any type.
	 */
	public void setUserData(final Object userData) {
		this.userData=userData;
	}

	/**
	 * Returns the next tag in the source document.
	 * <p>
	 * If a {@linkplain Source#fullSequentialParse() full sequential parse} has been performed, this method is very efficient.
	 * <p>
	 * If not, it is equivalent to <code>source.</code>{@link Source#findNextTag(int) findNextTag}<code>(</code>{@link #getBegin()}<code>+1)</code>.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @return the next tag in the source document, or <code>null</code> if this is the last tag.
	 */
	public Tag findNextTag() {
		final Tag[] allTagsArray=source.allTagsArray;
		if (allTagsArray!=null) {
			final int nextAllTagsArrayIndex=allTagsArrayIndex+1;
			if (allTagsArray.length==nextAllTagsArrayIndex) return null;
			return allTagsArray[nextAllTagsArrayIndex];
		} else {
			return source.findNextTag(begin+1);
		}
	}

	/**
	 * Returns the previous tag in the source document.
	 * <p>
	 * If a {@linkplain Source#fullSequentialParse() full sequential parse} has been performed, this method is very efficient.
	 * <p>
	 * If not, it is equivalent to <code>source.</code>{@link Source#findPreviousTag(int) findPreviousTag}<code>(</code>{@link #getBegin()}<code>-1)</code>.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @return the previous tag in the source document, or <code>null</code> if this is the first tag.
	 */
	public Tag findPreviousTag() {
		final Tag[] allTagsArray=source.allTagsArray;
		if (allTagsArray!=null) {
			if (allTagsArrayIndex==0) return null;
			return allTagsArray[allTagsArrayIndex-1];
		} else {
			if (begin==0) return null;
			return source.findPreviousTag(begin-1);
		}
	}

	/**
	 * Indicates whether this tag has a syntax that does not match any of the {@linkplain TagType#register() registered} {@linkplain TagType tag types}.
	 * <p>
 	 * The only requirement of an unregistered tag type is that it {@linkplain TagType#getStartDelimiter() starts} with
 	 * '<code>&lt;</code>' and there is a {@linkplain TagType#getClosingDelimiter() closing} '<code>&gt;</code>' character
 	 * at some position after it in the source document.
	 * <p>
	 * The absence or presence of a '<code>/</code>' character after the initial '<code>&lt;</code>' determines whether an
	 * unregistered tag is respectively a
	 * {@link StartTag} with a {@linkplain #getTagType() type} of {@link StartTagType#UNREGISTERED} or an
	 * {@link EndTag} with a {@linkplain #getTagType() type} of {@link EndTagType#UNREGISTERED}.
	 * <p>
	 * There are no restrictions on the characters that might appear between these delimiters, including other '<code>&lt;</code>'
	 * characters.  This may result in a '<code>&gt;</code>' character that is identified as the closing delimiter of two
	 * separate tags, one an unregistered tag, and the other a tag of any type that {@linkplain #getBegin() begins} in the middle 
	 * of the unregistered tag.  As explained below, unregistered tags are usually only found when specifically looking for them,
	 * so it is up to the user to detect and deal with any such nonsensical results.
	 * <p>
	 * Unregistered tags are only returned by the {@link Source#getTagAt(int pos)} method,
	 * <a href="Tag.html#NamedSearch">named search</a> methods, where the specified <code>name</code>
	 * matches the first characters inside the tag, and by <a href="Tag.html#TagTypeSearch">tag type search</a> methods, where the
	 * specified <code>tagType</code> is either {@link StartTagType#UNREGISTERED} or {@link EndTagType#UNREGISTERED}.
	 * <p>
	 * <a href="Tag.html#OpenSearch">Open</a> tag searches and <a href="Tag.html#OtherSearch">other</a> searches always ignore
	 * unregistered tags, although every discovery of an unregistered tag is {@linkplain Source#setLogWriter(Writer) logged} by the parser.
	 * <p>
	 * The logic behind this design is that unregistered tag types are usually the result of a '<code>&lt;</code>' character 
	 * in the text that was mistakenly left {@linkplain CharacterReference#encode(CharSequence) unencoded}, or a less-than 
	 * operator inside a script, or some other occurrence which is of no interest to the user.
	 * By returning unregistered tags in <a href="Tag.html#NamedSearch">named</a> and <a href="Tag.html#TagTypeSearch">tag type</a>
	 * search methods, the library allows the user to specifically search for tags with a certain syntax that does not match any
	 * existing {@link TagType}.  This expediency feature avoids the need for the user to create a
	 * <a href="TagType.html#Custom">custom tag type</a> to define the syntax before searching for these tags.
	 * By not returning unregistered tags in the less specific search methods, it is providing only the information that 
	 * most users are interested in.
	 *
	 * @return <code>true</code> if this tag has a syntax that does not match any of the {@linkplain TagType#register() registered} {@linkplain TagType tag types}, otherwise <code>false</code>.
	 */
	public abstract boolean isUnregistered();

	/**
	 * Returns an XML representation of this tag.
	 * <p>
	 * This is an abstract method which is implemented in the {@link StartTag} and {@link EndTag} subclasses.
	 * See the documentation of the {@link StartTag#tidy()} and {@link EndTag#tidy()} methods for details.
	 *
	 * @return an XML representation of this tag.
	 */
	public abstract String tidy();

	/**
	 * Indicates whether the specified text is a valid <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Name">XML Name</a>.
	 * <p>
	 * This implementation first checks that the first character of the specified text is a valid XML Name start character
	 * as defined by the {@link #isXMLNameStartChar(char)} method, and then checks that the rest of the characters are valid
	 * XML Name characters as defined by the {@link #isXMLNameChar(char)} method.
	 * <p>
	 * Note that this implementation does not exactly adhere to the
	 * <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Name">formal definition of an XML Name</a>,
	 * but the differences are unlikely to be significant in real-world XML or HTML documents.
	 *
	 * @param text  the text to test.
	 * @return <code>true</code> if the specified text is a valid <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Name">XML Name</a>, otherwise <code>false</code>.
	 * @see Source#findNameEnd(int pos)
	 */
	public static final boolean isXMLName(final CharSequence text) {
		if (text==null || text.length()==0 || !isXMLNameStartChar(text.charAt(0))) return false;
		for (int i=1; i<text.length(); i++)
			if (!isXMLNameChar(text.charAt(i))) return false;
		return true;
	}

	/**
	 * Indicates whether the specified character is valid at the start of an
	 * <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Name">XML Name</a>.
	 * <p>
	 * The <a target="_blank" href="http://www.w3.org/TR/REC-xml/#sec-common-syn">XML 1.0 specification section 2.3</a> defines a
	 * <code><a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Name">Name</a></code> as starting with one of the characters
	 * <br /><code>(<a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Letter">Letter</a> | '_' | ':')</code>.
	 * <p>
	 * This method uses the expression
	 * <br /><code>Character.isLetter(ch) || ch=='_' || ch==':'</code>.
	 * <p>
	 * Note that there are many differences between the <code>Character.isLetter()</code> definition of a Letter and the
	 * <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Letter">XML definition of a Letter</a>,
	 * but these differences are unlikely to be significant in real-world XML or HTML documents.
	 *
	 * @param ch  the character to test.
	 * @return <code>true</code> if the specified character is valid at the start of an <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Name">XML Name</a>, otherwise <code>false</code>.
	 * @see Source#findNameEnd(int pos)
	 */
	public static final boolean isXMLNameStartChar(final char ch) {
		return Character.isLetter(ch) || ch=='_' || ch==':';
	}

	/**
	 * Indicates whether the specified character is valid anywhere in an
	 * <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Name">XML Name</a>.
	 * <p>
	 * The <a target="_blank" href="http://www.w3.org/TR/REC-xml/#sec-common-syn">XML 1.0 specification section 2.3</a> uses the
	 * entity <code><a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-NameChar">NameChar</a></code> to represent this set of
	 * characters, which is defined as
	 * <br /><code>(<a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Letter">Letter</a>
	 * | <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Digit">Digit</a> | '.' | '-' | '_' | ':'
	 * | <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-CombiningChar">CombiningChar</a>
	 * | <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Extender">Extender</a>)</code>.
	 * <p>
	 * This method uses the expression
	 * <br /><code>Character.isLetterOrDigit(ch) || ch=='.' || ch=='-' || ch=='_' || ch==':'</code>.
	 * <p>
	 * Note that there are many differences between these definitions,
	 * but these differences are unlikely to be significant in real-world XML or HTML documents.
	 *
	 * @param ch  the character to test.
	 * @return <code>true</code> if the specified character is valid anywhere in an <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Name">XML Name</a>, otherwise <code>false</code>.
	 * @see Source#findNameEnd(int pos)
	 */
	public static final boolean isXMLNameChar(final char ch) {
		return Character.isLetterOrDigit(ch) || ch=='.' || ch=='-' || ch=='_' || ch==':';
	}

	/**
	 * Regenerates the HTML text of this tag.
	 * <p>
	 * This method has been deprecated as of version 2.2 and replaced with the exactly equivalent {@link #tidy()} method.
	 *
	 * @return the regenerated HTML text of this tag.
	 * @deprecated  Use {@link #tidy()} instead.
	 */
	public abstract String regenerateHTML();

	final boolean includeInSearch() {
		return INCLUDE_UNREGISTERED_IN_SEARCH || !isUnregistered();
	}

	static final Tag findPreviousOrNextTag(final Source source, final int pos, final boolean previous) {
		// returns null if pos is out of range.
		return source.useAllTypesCache
			? source.cache.findPreviousOrNextTag(pos,previous)
			: findPreviousOrNextTagUncached(source,pos,previous,ParseText.NO_BREAK);
	}
		
	static final Tag findPreviousOrNextTagUncached(final Source source, final int pos, final boolean previous, final int breakAtPos) {
		// returns null if pos is out of range.
		try {
			final ParseText parseText=source.getParseText();
			int begin=pos;
			do {
				begin=previous?parseText.lastIndexOf('<',begin,breakAtPos):parseText.indexOf('<',begin,breakAtPos); // this assumes that all tags start with '<'
				// parseText.lastIndexOf and indexOf return -1 if pos is out of range.
				if (begin==-1) return null;
				final Tag tag=getTagAt(source,begin);
				if (tag!=null && tag.includeInSearch()) return tag;
			} while (previous ? (begin-=1)>=0 : (begin+=1)<source.end);
		} catch (IndexOutOfBoundsException ex) {
			// this should only happen when the end of file is reached in the middle of a tag.
			// we don't have to do anything to handle it as there are no more tags anyway.
		}
		return null;
	}

	static final Tag findPreviousOrNextTag(final Source source, final int pos, final TagType tagType, final boolean previous) {
		// returns null if pos is out of range.
		if (source.useSpecialTypesCache) return source.cache.findPreviousOrNextTag(pos,tagType,previous);
		return findPreviousOrNextTagUncached(source,pos,tagType,previous,ParseText.NO_BREAK);
	}

	static final Tag findPreviousOrNextTagUncached(final Source source, final int pos, final TagType tagType, final boolean previous, final int breakAtPos) {
		// returns null if pos is out of range.
		if (tagType==null) return findPreviousOrNextTagUncached(source,pos,previous,breakAtPos);
		final char[] startDelimiterCharArray=tagType.getStartDelimiterCharArray();
		try {
			final ParseText parseText=source.getParseText();
			int begin=pos;
			do {
				begin=previous?parseText.lastIndexOf(startDelimiterCharArray,begin,breakAtPos):parseText.indexOf(startDelimiterCharArray,begin,breakAtPos);
				// parseText.lastIndexOf and indexOf return -1 if pos is out of range.
				if (begin==-1) return null;
				final Tag tag=getTagAt(source,begin);
				if (tag!=null && tag.getTagType()==tagType) return tag;
			} while (previous ? (begin-=1)>=0 : (begin+=1)<source.end);
		} catch (IndexOutOfBoundsException ex) {
			// this should only happen when the end of file is reached in the middle of a tag.
			// we don't have to do anything to handle it as there are no more tags anyway.
		}
		return null;
	}

	static final Tag getTagAt(final Source source, final int pos) {
		// returns null if pos is out of range.
		return source.useAllTypesCache
			? source.cache.getTagAt(pos)
			: getTagAtUncached(source,pos);
	}

	static final Tag getTagAtUncached(final Source source, final int pos) {
		// returns null if pos is out of range.
		return TagType.getTagAt(source,pos,false);
	}

	static final Tag[] parseAll(final Source source, final boolean assumeNoNestedTags) {
		int registeredTagCount=0;
		int registeredStartTagCount=0;
		final List<Tag> list=new ArrayList<Tag>();
		if (source.end!=0) {
			final ParseText parseText=source.getParseText();
			Tag tag=parseAllFindNextTag(source,parseText,0,assumeNoNestedTags);
			while (tag!=null) {
				list.add(tag);
				if (!tag.isUnregistered()) {
					registeredTagCount++;
					if (tag instanceof StartTag) registeredStartTagCount++;
				}
				// Look for next tag after end of next tag if we're assuming tags don't appear inside other tags, as long as the last tag found was not an unregistered tag:
				final int pos=(assumeNoNestedTags && !tag.isUnregistered()) ? tag.end : tag.begin+1;
				if (pos==source.end) break;
				tag=parseAllFindNextTag(source,parseText,pos,assumeNoNestedTags);
			}
		}
		final Tag[] allRegisteredTags=new Tag[registeredTagCount];
		final StartTag[] allRegisteredStartTags=new StartTag[registeredStartTagCount];
		source.cache.loadAllTags(list,allRegisteredTags,allRegisteredStartTags);
		source.allTagsArray=allRegisteredTags;
		source.allTags=Arrays.asList(allRegisteredTags);
		source.allStartTags=Arrays.asList(allRegisteredStartTags);
		for (int i=0; i<allRegisteredTags.length; i++) allRegisteredTags[i].allTagsArrayIndex=i;
		return allRegisteredTags;
	}

	private static final Tag parseAllFindNextTag(final Source source, final ParseText parseText, final int pos, final boolean assumeNoNestedTags) {
		try {
			int begin=pos;
			do {
				begin=parseText.indexOf('<',begin); // this assumes that all tags start with '<'
				if (begin==-1) return null;
				final Tag tag=TagType.getTagAt(source,begin,assumeNoNestedTags);
				if (tag!=null) {
					if (!assumeNoNestedTags) {
						final TagType tagType=tag.getTagType();
						if (tag.end>source.endOfLastTagIgnoringEnclosedMarkup
								&& !tagType.isServerTag()
								&& tagType!=StartTagType.DOCTYPE_DECLARATION
								&& tagType!=StartTagType.UNREGISTERED && tagType!=EndTagType.UNREGISTERED)
							source.endOfLastTagIgnoringEnclosedMarkup=tag.end;
					}
					return tag;
				}
			} while ((begin+=1)<source.end);
		} catch (IndexOutOfBoundsException ex) {
			// this should only happen when the end of file is reached in the middle of a tag.
			// we don't have to do anything to handle it as there are no more tags anyway.
		}
		return null;
	}

	// delete when deprecated Source.getNextTagIterator method is removed
	static Iterator<Tag> getNextTagIterator(final Source source, final int pos) {
		return new NextTagIterator(source,pos);
	}

	private static final class NextTagIterator implements Iterator<Tag> {
		private Tag nextTag=null;

		public NextTagIterator(final Source source, final int pos) {
			nextTag=findPreviousOrNextTag(source,pos,false);
		}

		public boolean hasNext() {
			return nextTag!=null;
		}

		public Tag next() {
			final Tag result=nextTag;
			try {
				nextTag=findPreviousOrNextTag(result.source,result.begin+1,false);
			} catch (NullPointerException ex) {
				throw new NoSuchElementException();
			}
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
