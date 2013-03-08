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

import java.util.List;

/**
 * Defines the syntax for a tag type that can be recognised by the parser.
 * <p>
 * This class is the root abstract class common to all tag types, and contains methods to {@linkplain #register() register}
 * and {@linkplain #deregister() deregister} tag types as well as various methods to aid in their implementation.
 * <p>
 * Every tag type is represented by an instance of a class (usually a singleton) that must be a subclass of either 
 * {@link StartTagType} or {@link EndTagType}.  These two abstract classes, the only direct descendants of this class,
 * represent the two major classifications under which every tag type exists.
 * <p>
 * The term <i><a name="Predefined">predefined tag type</a></i> refers to any of the tag types defined in this library,
 * including both <a href="#Standard">standard</a> and <a href="#Extended">extended</a> tag types.
 * <p>
 * The term <i><a name="Standard">standard tag type</a></i> refers to any of the tag types represented by instances
 * in static fields of the {@link StartTagType} and {@link EndTagType} subclasses.
 * Standard tag types are registered by default, and define the tags most commonly found in HTML documents.
 * <p>
 * The term <i><a name="Extended">extended tag type</a></i> refers to any <a href="#Predefined">predefined</a> tag type
 * that is not a <a href="#Standard">standard</a> tag type.
 * The {@link PHPTagTypes} and {@link MasonTagTypes} classes contain extended tag types related to their respective server platforms.
 * The tag types defined within them must be registered by the user before they are recognised by the parser.
 * <p>
 * The term <i><a name="Custom">custom tag type</a></i> refers to any user-defined tag type, or any tag type that is
 * not a <a href="#Predefined">predefined</a> tag type.
 * <p>
 * The tag recognition process of the parser gives each tag type a <i><a name="Precedence">precedence</a></i> level,
 * which is primarily determined by the length of its {@linkplain #getStartDelimiter() start delimiter}.
 * A tag type with a more specific start delimiter is chosen in preference to one with a less specific start delimiter,
 * assuming they both share the same prefix.  If two tag types have exactly the same start delimiter, the one which was
 * {@linkplain #register() registered} later has the higher precedence.
 * <p>
 * The two special tag types {@link StartTagType#UNREGISTERED} and {@link EndTagType#UNREGISTERED} represent
 * tags that do not match the syntax of any other tag type.  They have the lowest <a href="#Precedence">precedence</a> 
 * of all the tag types.  The {@link Tag#isUnregistered()} method provides a detailed explanation of unregistered tags.
 * <p>
 * See the documentation of the <a href="Tag.html#ParsingProcess">tag parsing process</a> for more information
 * on how each tag is identified by the parser.
 * <p>
 * Note that the standard {@linkplain HTMLElementName HTML element names} do not represent different
 * tag <i>types</i>.  All standard HTML tags have a tag type of {@link StartTagType#NORMAL} or {@link EndTagType#NORMAL}.
 * <p>
 * Apart from the <a href="#Registration">registration related</a> methods, all of the methods in this class and its
 * subclasses relate to the implementation of <a href="#Custom">custom tag types</a> and are not relevant to the majority of users 
 * who just use the <a href="#Predefined">predefined tag types</a>.
 * <p>
 * For perfomance reasons, this library only allows tag types that {@linkplain #getStartDelimiter() start}
 * with a '<code>&lt;</code>' character.
 * The character following this defines the immediate subclass of the tag type.
 * An {@link EndTagType} always has a slash ('<code>/</code>') as the second character, while a {@link StartTagType}
 * has any character other than a slash as the second character.
 * This definition means that tag types which are not intuitively classified as either start tag types or end tag types
 * (such as an HTML {@linkplain StartTagType#COMMENT comment}) are mostly classified as start tag types.
 * <p>
 * Every method in this and the {@link StartTagType} and {@link EndTagType} abstract classes can be categorised
 * as one of the following:
 * <dl>
 *  <dt><a name="Property">Properties:</a>
 *   <dd>Simple properties (marked final) that were either specified as parameters
 *    during construction or are derived from those parameters.
 *  <dt><a name="AbstractImplementation">Abstract implementation methods:</a>
 *   <dd>Methods that must be implemented in a subclass.
 *  <dt><a name="DefaultImplementation">Default implementation methods:</a>
 *   <dd>Methods (not marked final) that implement common behaviour, but may be overridden in a subclass.
 *  <dt><a name="ImplementationAssistance">Implementation assistance methods:</a>
 *   <dd>Protected methods that provide low-level functionality and are only of use within other implementation methods.
 *  <dt><a name="RegistrationRelated">Registration related methods:</a>
 *   <dd>Utility methods (marked final) relating to the {@linkplain #register() registration} of tag type instances.
 * </dl>
 */
public abstract class TagType {
	private final String description;
	private final String startDelimiter;
	private final char[] startDelimiterCharArray;
	private final String closingDelimiter;
	private final boolean isServerTag;
	private final String namePrefix;
	final String startDelimiterPrefix;

	TagType(final String description, final String startDelimiter, final String closingDelimiter, final boolean isServerTag, final String startDelimiterPrefix) {
		// startDelimiterPrefix is either "<" or "</"
		this.description=description;
		this.startDelimiter=startDelimiter;
		startDelimiterCharArray=startDelimiter.toCharArray();
		this.closingDelimiter=closingDelimiter;
		this.isServerTag=isServerTag;
		this.namePrefix=startDelimiter.substring(startDelimiterPrefix.length());
		this.startDelimiterPrefix=startDelimiterPrefix;
	}

	/**
	 * Registers this tag type for recognition by the parser.
	 * <br />(<a href="TagType.html#RegistrationRelated">registration related</a> method)
	 * <p>
	 * The order of registration affects the <a href="TagType.html#Precedence">precedence</a> of the tag type when a potential tag is being parsed.
	 *
	 * @see #deregister()
	 */
	public final void register() {
		TagTypeRegister.add(this);
	}
	
	/**
	 * Deregisters this tag type.
	 * <br />(<a href="TagType.html#RegistrationRelated">registration related</a> method)
	 *
	 * @see #register()
	 */
	public final void deregister() {
		TagTypeRegister.remove(this);
	}

	/**
	 * Returns a list of all the currently registered tag types in order of lowest to highest <a href="TagType.html#Precedence">precedence</a>.
	 * <br />(<a href="TagType.html#RegistrationRelated">registration related</a> method)
	 * @return a list of all the currently registered tag types in order of lowest to highest <a href="TagType.html#Precedence">precedence</a>.
	 */
	public static final List<TagType> getRegisteredTagTypes() {
		return TagTypeRegister.getList();
	}

	/**
	 * Returns a description of this tag type useful for debugging purposes. 
	 * <br />(<a href="TagType.html#Property">property</a> method)
	 *
	 * @return a description of this tag type useful for debugging purposes.
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * Returns the character sequence that marks the start of the tag.
	 * <br />(<a href="TagType.html#Property">property</a> method)
	 * <p>
	 * The character sequence must be all in lower case.
	 * <p>
	 * The first character in this property <b>must</b> be '<code>&lt;</code>'.
	 * This is a deliberate limitation of the system which is necessary to retain reasonable performance.
	 * <p>
	 * The second character in this property must be '<code>/</code>' if the implementing class is an {@link EndTagType}.
	 * It must <b>not</b> be '<code>/</code>' if the implementing class is a {@link StartTagType}.
	 * <p>
	 * <dl>
	 *  <dt>Standard Tag Type Values:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Tag Type<th>Start Delimiter
	 *     <tr><td>{@link StartTagType#UNREGISTERED}<td><code>&lt;</code>
	 *     <tr><td>{@link StartTagType#NORMAL}<td><code>&lt;</code>
	 *     <tr><td>{@link StartTagType#COMMENT}<td><code>&lt;!--</code>
	 *     <tr><td>{@link StartTagType#XML_DECLARATION}<td><code>&lt;?xml</code>
	 *     <tr><td>{@link StartTagType#XML_PROCESSING_INSTRUCTION}<td><code>&lt;?</code>
	 *     <tr><td>{@link StartTagType#DOCTYPE_DECLARATION}<td><code>&lt;!doctype</code>
	 *     <tr><td>{@link StartTagType#MARKUP_DECLARATION}<td><code>&lt;!</code>
	 *     <tr><td>{@link StartTagType#CDATA_SECTION}<td><code>&lt;![cdata[</code>
	 *     <tr><td>{@link StartTagType#SERVER_COMMON}<td><code>&lt;%</code>
	 *     <tr><td>{@link EndTagType#UNREGISTERED}<td><code>&lt;/</code>
	 *     <tr><td>{@link EndTagType#NORMAL}<td><code>&lt;/</code>
	 *    </table>
	 * </dl>
	 * <dl>
	 *  <dt>Extended Tag Type Values:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Tag Type<th>Start Delimiter
	 *     <tr><td>{@link PHPTagTypes#PHP_SCRIPT}<td><code>&lt;script</code>
	 *     <tr><td>{@link PHPTagTypes#PHP_SHORT}<td><code>&lt;?</code>
	 *     <tr><td>{@link PHPTagTypes#PHP_STANDARD}<td><code>&lt;?php</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALL}<td><code>&lt;&amp;</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT}<td><code>&lt;&amp;|</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT_END}<td><code>&lt;/&amp;</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_NAMED_BLOCK}<td><code>&lt;%</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_NAMED_BLOCK_END}<td><code>&lt;/%</code>
	 *    </table>
	 * </dl>
	 *
	 * @return the character sequence that marks the start of the tag.
	 */
	public final String getStartDelimiter() {
		return startDelimiter;
	}

	/**
	 * Returns the character sequence that marks the end of the tag.
	 * <br />(<a href="TagType.html#Property">property</a> method)
	 * <p>
	 * The character sequence must be all in lower case.
	 * <p>
	 * In a {@link StartTag} of a {@linkplain StartTagType type} that {@linkplain StartTagType#hasAttributes() has attributes},
	 * characters appearing inside a quoted attribute value are ignored when determining the location of the closing delimiter.
	 * <p>
	 * Note that the optional '<code>/</code>' character preceding the closing '<code>&gt;</code>' in an
	 * {@linkplain StartTag#isEmptyElementTag() empty-element tag} is not considered part of the end delimiter.
	 * This property must define the closing delimiter common to all instances of the tag type.
	 * <p>
	 * <dl>
	 *  <dt>Standard Tag Type Values:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Tag Type<th>Closing Delimiter
	 *     <tr><td>{@link StartTagType#UNREGISTERED}<td><code>&gt;</code>
	 *     <tr><td>{@link StartTagType#NORMAL}<td><code>&gt;</code>
	 *     <tr><td>{@link StartTagType#COMMENT}<td><code>--&gt;</code>
	 *     <tr><td>{@link StartTagType#XML_DECLARATION}<td><code>?&gt;</code>
	 *     <tr><td>{@link StartTagType#XML_PROCESSING_INSTRUCTION}<td><code>?&gt;</code>
	 *     <tr><td>{@link StartTagType#DOCTYPE_DECLARATION}<td><code>&gt;</code>
	 *     <tr><td>{@link StartTagType#MARKUP_DECLARATION}<td><code>&gt;</code>
	 *     <tr><td>{@link StartTagType#CDATA_SECTION}<td><code>]]&gt;</code>
	 *     <tr><td>{@link StartTagType#SERVER_COMMON}<td><code>%&gt;</code>
	 *     <tr><td>{@link EndTagType#UNREGISTERED}<td><code>&gt;</code>
	 *     <tr><td>{@link EndTagType#NORMAL}<td><code>&gt;</code>
	 *    </table>
	 * </dl>
	 * <dl>
	 *  <dt>Extended Tag Type Values:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Tag Type<th>Closing Delimiter
	 *     <tr><td>{@link PHPTagTypes#PHP_SCRIPT}<td><code>&gt;</code>
	 *     <tr><td>{@link PHPTagTypes#PHP_SHORT}<td><code>?&gt;</code>
	 *     <tr><td>{@link PHPTagTypes#PHP_STANDARD}<td><code>?&gt;</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALL}<td><code>&amp;&gt;</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT}<td><code>&amp;&gt;</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT_END}<td><code>&gt;</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_NAMED_BLOCK}<td><code>&gt;</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_NAMED_BLOCK_END}<td><code>&gt;</code>
	 *    </table>
	 * </dl>
	 *
	 * @return the character sequence that marks the end of the tag.
	 */
	public final String getClosingDelimiter() {
		return closingDelimiter;
	}

	/**
	 * Indicates whether this tag type represents a server tag.
	 * <br />(<a href="TagType.html#Property">property</a> method)
	 * <p>
	 * Server tags are typically parsed by some process on the web server and substituted with other text or markup before delivery to the
	 * <a target="_blank" href="http://www.w3.org/TR/html401/conform.html#didx-user_agent">user agent</a>.
	 * This parser therefore handles them differently to non-server tags in that they can occur at any position in the document
	 * without regard for the HTML document structure.  As a result they can occur anywhere inside any other tag and vice versa.
	 * <p>
	 * To avoid the problem of server tags interfering with the proper parsing of the rest of the document, the
	 * {@link Segment#ignoreWhenParsing()} method can be called on all server tags found in the document before parsing the non-server tags.
	 * <p>
	 * The documentation of the <a href="Tag.html#ParsingProcess">tag parsing process</a> explains in detail 
	 * how the value of this property affects the recognition of a tag.
	 * <p>
	 * <dl>
	 *  <dt>Standard Tag Type Values:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Tag Type<th>Is Server Tag
	 *     <tr><td>{@link StartTagType#UNREGISTERED}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#NORMAL}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#COMMENT}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#XML_DECLARATION}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#XML_PROCESSING_INSTRUCTION}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#DOCTYPE_DECLARATION}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#MARKUP_DECLARATION}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#CDATA_SECTION}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#SERVER_COMMON}<td><code>true</code>
	 *     <tr><td>{@link EndTagType#UNREGISTERED}<td><code>false</code>
	 *     <tr><td>{@link EndTagType#NORMAL}<td><code>false</code>
	 *    </table>
	 * </dl>
	 * <dl>
	 *  <dt>Extended Tag Type Values:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Tag Type<th>Is Server Tag
	 *     <tr><td>{@link PHPTagTypes#PHP_SCRIPT}<td><code>true</code>
	 *     <tr><td>{@link PHPTagTypes#PHP_SHORT}<td><code>true</code>
	 *     <tr><td>{@link PHPTagTypes#PHP_STANDARD}<td><code>true</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALL}<td><code>true</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT}<td><code>true</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT_END}<td><code>true</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_NAMED_BLOCK}<td><code>true</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_NAMED_BLOCK_END}<td><code>true</code>
	 *    </table>
	 * </dl>
	 *
	 * @return <code>true</code> if this tag type represents a server tag, otherwise <code>false</code>.
	 */
	public final boolean isServerTag() {
		return isServerTag;
	}

	/**
	 * Returns the {@linkplain Tag#getName() name} prefix required by this tag type.
	 * <br />(<a href="TagType.html#Property">property</a> method)
	 * <p>
	 * This string is identical to the {@linkplain #getStartDelimiter() start delimiter}, except that it does not include the
	 * initial "<code>&lt;</code>" or "<code>&lt;/</code>" characters that always prefix the start delimiter of a
	 * {@link StartTagType} or {@link EndTagType} respectively.
	 * <p>
	 * The {@linkplain Tag#getName() name} of a tag of this type may or may not include extra characters after the prefix.
	 * This is determined by properties such as {@link StartTagType#isNameAfterPrefixRequired()}
	 * or {@link EndTagTypeGenericImplementation#isStatic()}. 
	 * <p>
	 * <dl>
	 *  <dt>Standard Tag Type Values:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Tag Type<th>Name Prefix
	 *     <tr><td>{@link StartTagType#UNREGISTERED}<td><i>(empty string)</i>
	 *     <tr><td>{@link StartTagType#NORMAL}<td><i>(empty string)</i>
	 *     <tr><td>{@link StartTagType#COMMENT}<td><code>!--</code>
	 *     <tr><td>{@link StartTagType#XML_DECLARATION}<td><code>?xml</code>
	 *     <tr><td>{@link StartTagType#XML_PROCESSING_INSTRUCTION}<td><code>?</code>
	 *     <tr><td>{@link StartTagType#DOCTYPE_DECLARATION}<td><code>!doctype</code>
	 *     <tr><td>{@link StartTagType#MARKUP_DECLARATION}<td><code>!</code>
	 *     <tr><td>{@link StartTagType#CDATA_SECTION}<td><code>![cdata[</code>
	 *     <tr><td>{@link StartTagType#SERVER_COMMON}<td><code>%</code>
	 *     <tr><td>{@link EndTagType#UNREGISTERED}<td><i>(empty string)</i>
	 *     <tr><td>{@link EndTagType#NORMAL}<td><i>(empty string)</i>
	 *    </table>
	 * </dl>
	 * <dl>
	 *  <dt>Extended Tag Type Values:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Tag Type<th>Name Prefix
	 *     <tr><td>{@link PHPTagTypes#PHP_SCRIPT}<td><code>script</code>
	 *     <tr><td>{@link PHPTagTypes#PHP_SHORT}<td><code>?</code>
	 *     <tr><td>{@link PHPTagTypes#PHP_STANDARD}<td><code>?php</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALL}<td><code>&amp;</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT}<td><code>&amp;|</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT_END}<td><code>&amp;</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_NAMED_BLOCK}<td><code>%</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_NAMED_BLOCK_END}<td><code>%</code>
	 *    </table>
	 * </dl>
	 *
	 * @return the {@linkplain Tag#getName() name} prefix required by this tag type.
	 * @see #getStartDelimiter()
	 */
	protected final String getNamePrefix() {
		return namePrefix;
	}

	/**
	 * Indicates whether a tag of this type is valid in the specified position of the specified source document.
	 * <br />(<a href="TagType.html#ImplementationAssistance">implementation assistance</a> method)
	 * <p>
	 * This method is called immediately before {@link #constructTagAt(Source, int pos)}
	 * to do a preliminary check on the validity of a tag of this type in the specified position.
	 * <p>
	 * This check is not performed as part of the {@link #constructTagAt(Source, int pos)} call because the same
	 * validation is used for all the <a href="TagType.html#Standard">standard</a> tag types, and is likely to be sufficient
	 * for all <a href="TagType.html#Custom">custom tag types</a>.
	 * Having this check separated into a different method helps to isolate common code from the code that is unique to each tag type.
	 * <p>
	 * In theory, a {@linkplain TagType#isServerTag() server tag} is valid in any position, but a non-server tag is not valid inside another non-server tag.
	 * <p>
	 * The common implementation of this method always returns <code>true</code> for server tags, but for non-server tags it behaves slightly differently
	 * depending upon whether or not a {@linkplain Source#fullSequentialParse() full sequential parse} is being peformed.
	 * If so, it implements the exact theoretical check and rejects a non-server tag if it is inside any other non-server tag.
	 * If a full sequential parse was not performed (i.e. in <a href="Source.html#ParseOnDemand">parse on demand</a> mode),
	 * practical constraints do not permit the implementation of the exact theoretical check, and non-server tags are only rejected 
	 * if they are found inside HTML {@linkplain StartTagType#COMMENT comments} or {@linkplain StartTagType#CDATA_SECTION CDATA sections}.
	 * <p>
	 * This behaviour is configurable by manipulating the static {@link TagType#getTagTypesIgnoringEnclosedMarkup() TagTypesIgnoringEnclosedMarkup} array
	 * to determine which tag types can not contain non-server tags.
	 * The {@linkplain TagType#getTagTypesIgnoringEnclosedMarkup() documentation of this property} contains
	 * a more detailed analysis of the subject and explains why only the {@linkplain StartTagType#COMMENT comment} and 
	 * {@linkplain StartTagType#CDATA_SECTION CDATA section} tag types are included by default.
	 * <p>
	 * See the documentation of the <a href="Tag.html#ParsingProcess">tag parsing process</a> for more information about how this method fits into the whole tag parsing process.
	 * <p>
	 * This method can be overridden in <a href="TagType.html#Custom">custom tag types</a> if the default implementation is unsuitable.
	 *
	 * @param source  the {@link Source} document.
	 * @param pos  the character position in the source document to check.
	 * @return <code>true</code> if a tag of this type is valid in the specified position of the specified source document, otherwise <code>false</code>.
	 */
	protected boolean isValidPosition(final Source source, final int pos) {
		if (isServerTag()) return true;
		if (source.endOfLastTagIgnoringEnclosedMarkup!=-1) {
			// use simplified check when doing full sequential parse.  Normally we are only able to check whether a tag is inside specially cached
			// tag types for efficiency reasons, but during a full sequential parse we can reject a tag if it is inside normal tags as well.
			return pos>=source.endOfLastTagIgnoringEnclosedMarkup;
		}
		// Use the normal method of checking whether the position is inside a tag of a tag type that ignores enclosed markup:
		final TagType[] tagTypesIgnoringEnclosedMarkup=getTagTypesIgnoringEnclosedMarkup();
		for (int i=0; i<tagTypesIgnoringEnclosedMarkup.length; i++)
			if (tagTypesIgnoringEnclosedMarkup[i].tagEncloses(source,pos)) return false;
		return true;
	}

	/**
	 * Returns an array of all the tag types inside which the parser ignores all other non-{@linkplain #isServerTag() server} tags
	 * in <a href="Source.html#ParseOnDemand">parse on demand</a> mode.
	 * <br />(<a href="TagType.html#ImplementationAssistance">implementation assistance</a> method)
	 * <p>
	 * The tag types returned by this property (referred to in the following paragraphs as the "listed types") default to
	 * {@link StartTagType#COMMENT} and {@link StartTagType#CDATA_SECTION}.
	 * <p>
	 * In <a href="Source.html#ParseOnDemand">parse on demand</a> mode,
	 * every new non-server tag found by the parser (referred to as a "new tag") undergoes a check to see whether it is enclosed
	 * by a tag of one of the listed types, including new tags of the listed types themselves.
	 * The recursive nature of this check means that <i>all</i> tags of the listed types occurring before the new tag must be found 
	 * by the parser before it can determine whether the new tag should be ignored.
	 * To mitigate any performance issues arising from this process, the listed types are given special treatment in the tag cache.
	 * This dramatically decreases the time taken to search on these tag types, so adding a tag type to this array that 
	 * is easily recognised and occurs infrequently only results in a small degradation in overall performance.
	 * <p>
	 * Theoretically, non-server tags appearing inside <i>any</i> other non-server tag should be ignored.
	 * One situation where a tag can legitimately contain a sequence of characters that resembles a tag,
	 * which shouldn't be recognised as a tag by the parser, is within an attribute value.
	 * The <a target="_blank" href="http://www.w3.org/TR/html401/charset.html#h-5.3.2">HTML 4.01 specification section 5.3.2</a>
	 * specifically allows the presence of '<code>&lt;</code>' and '<code>&gt;</code>' characters within attribute values.
	 * A common occurrence of this is in <a target="_blank" href="http://www.w3.org/TR/html401/interact/scripts.html#events">event</a>
	 * attributes such as <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/scripts.html#adef-onclick">onclick</a></code>,
	 * which contain scripts that often dynamically load new HTML into the document
	 * (see the file <code><a target="_blank" href="../../../../../../../samples/data/Test.html#TagInsideTag">samples/data/Test.html</a></code> for an example).
	 * <p>
	 * Performing a {@linkplain Source#fullSequentialParse() full sequential parse} of the source document prevents these attribute values from being
	 * recognised as tags, but can be very expensive if only a few tags in the document need to be parsed.
	 * The penalty of not parsing every tag in the document is that the exactness of this check is compromised, but in practical terms the difference is inconsequential.
	 * The default listed types of {@linkplain StartTagType#COMMENT comments} and {@linkplain StartTagType#CDATA_SECTION CDATA sections} yields sensible results 
	 * in the vast majority of practical applications with only a minor impact on performance.
	 * <p>
	 * In <a target="_blank" href="http://www.w3.org/TR/xhtml1/">XHTML</a>, '<code>&lt;</code>' and '<code>&gt;</code>' characters 
	 * must be represented in attribute values as {@linkplain CharacterReference character references}
	 * (see the XML 1.0 specification section <a target="_blank" href="http://www.w3.org/TR/REC-xml#CleanAttrVals">3.1</a>),
	 * so the situation should never arise that a tag is found inside another tag unless one of them is a
	 * {@linkplain #isServerTag() server tag}.
	 * <p>
	 * This method is called from the default implementation of the {@link #isValidPosition(Source, int pos)} method.
	 *
	 * @return an array of all the tag types inside which the parser ignores all other non-{@linkplain #isServerTag() server} tags.
	 */
	public static final TagType[] getTagTypesIgnoringEnclosedMarkup() {
		return TagTypesIgnoringEnclosedMarkup.array;
	}

	/**
	 * Sets the tag types inside which the parser ignores all other non-{@linkplain #isServerTag() server} tags.
	 * <br />(<a href="TagType.html#ImplementationAssistance">implementation assistance</a> method)
	 * <p>
	 * See {@link #getTagTypesIgnoringEnclosedMarkup()} for the documentation of this property.
	 *
	 * @param tagTypes  an array of tag types.
	 */
	public static final void setTagTypesIgnoringEnclosedMarkup(TagType[] tagTypes) {
		if (tagTypes==null) throw new IllegalArgumentException();
		TagTypesIgnoringEnclosedMarkup.array=tagTypes;
	}

	/**
	 * Constructs a tag of this type at the specified position in the specified source document if it matches all of the required features.
	 * <br />(<a href="TagType.html#AbstractImplementation">abstract implementation</a> method)
	 * <p>
	 * The implementation of this method must check that the text at the specified position meets all of
	 * the criteria of this tag type, including such checks as the presence of the correct or well formed
	 * {@linkplain #getClosingDelimiter() closing delimiter}, {@linkplain Tag#getName() name}, {@linkplain Attributes attributes},
	 * {@linkplain EndTag end tag}, or any other distinguishing features.
	 * <p>
	 * It can be assumed that the specified position starts with the {@linkplain #getStartDelimiter() start delimiter} of this tag type,
	 * and that all other tag types with higher <a href="TagType.html#Precedence">precedence</a> (if any) have already been rejected as candidates.
	 * Tag types with lower precedence will be considered if this method returns <code>null</code>.
	 * <p>
	 * This method is only called after a successful check of the tag's position, i.e.
	 * {@link #isValidPosition(Source,int) isValidPosition(source,pos)}<code>==true</code>.
	 * <p>
	 * The {@link StartTagTypeGenericImplementation} and {@link EndTagTypeGenericImplementation} subclasses provide default
	 * implementations of this method that allow the use of much simpler <a href="TagType.html#Property">properties</a> and
	 * <a href="TagType.html#ImplementationAssistance">implementation assistance</a> methods and to carry out the required functions.
	 *
	 * @param source  the {@link Source} document.
	 * @param pos  the position in the source document.
	 * @return a tag of this type at the specified position in the specified source document if it meets all of the required features, or <code>null</code> if it does not meet the criteria.
	 */
	protected abstract Tag constructTagAt(Source source, int pos);

	/**
	 * Indicates whether a tag of this type encloses the specified position of the specified source document.
	 * <br />(<a href="TagType.html#ImplementationAssistance">implementation assistance</a> method)
	 * <p>
	 * This is logically equivalent to <code>source.</code>{@link Source#findEnclosingTag(int,TagType) findEnclosingTag(pos,this)}<code>!=null</code>,
	 * but is safe to use within other implementation methods without the risk of causing an infinite recursion.
	 * <p>
	 * This method is called by the {@link TagType} implementation of {@link #isValidPosition(Source, int pos)}.
	 *
	 * @param source  the {@link Source} document.
	 * @param pos  the character position in the source document to check.
	 * @return <code>true</code> if a tag of this type encloses the specified position of the specified source document, otherwise <code>false</code>.
	 */
	protected final boolean tagEncloses(final Source source, final int pos) {
		if (pos==0) return false;
		final Tag enclosingTag=source.findEnclosingTag(pos-1,this); // use pos-1 otherwise a tag at pos could cause infinite recursion when this is called from constructTagAt
		return enclosingTag!=null && pos!=enclosingTag.getEnd(); // make sure pos!=enclosingTag.getEnd() to compensate for using pos-1 above (important if the tag in question immediately follows an end tag delimiter)
	}

	/**
	 * Returns a string representation of this object useful for debugging purposes.
	 * @return a string representation of this object useful for debugging purposes.
	 */
	public String toString() {
		return getDescription();
	}

	static final Tag getTagAt(final Source source, final int pos, final boolean assumeNoNestedTags) {
		final TagTypeRegister.ProspectiveTagTypeIterator prospectiveTagTypeIterator=new TagTypeRegister.ProspectiveTagTypeIterator(source,pos);
		// prospectiveTagTypeIterator is empty if pos is out of range.
		while (prospectiveTagTypeIterator.hasNext()) {
			final TagType tagType=prospectiveTagTypeIterator.getNextTagType();
			if (assumeNoNestedTags || tagType.isValidPosition(source,pos)) {
				try {
					final Tag tag=tagType.constructTagAt(source,pos);
					if (tag!=null) return tag;
				} catch (IndexOutOfBoundsException ex) {
					if (source.isLoggingEnabled()) source.log(source.getRowColumnVector(pos).appendTo(new StringBuffer(200).append("Tag at ")).append(" not recognised as type '").append(tagType.getDescription()).append("' because it has no end delimiter").toString());
				}
			}
		}
		return null;
	}

	final String getNamePrefixForTagConstant() {
		// this method is only used in deprecated constants and will eventually be removed
		return getNamePrefix();
	}
	
	final char[] getStartDelimiterCharArray() {
		return startDelimiterCharArray;
	}

	private static final class TagTypesIgnoringEnclosedMarkup {
		// This internal class is used to contain the array because its static initialisation can occur after
		// the StartTagType.COMMENT and StartTagType.CDATA_SECTION members have been created.
		public static TagType[] array=new TagType[] {
			StartTagType.COMMENT,
			StartTagType.CDATA_SECTION
		};
	}
}
