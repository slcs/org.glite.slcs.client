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


/**
 * Defines the syntax for an end tag type.
 * <p>
 * An end tag type is a {@link TagType} that {@linkplain #getStartDelimiter() starts} with the characters '<code>&lt;/</code>'.
 * <p>
 * Instances of all the <a href="TagType.html#Standard">standard</a> end tag types are available in this class as static
 * <a href="#field_summary">fields</a>.
 *
 * @see StartTagType
 */
public abstract class EndTagType extends TagType {
	static final String START_DELIMITER_PREFIX="</";

	/**
	 * The tag type given to an {@linkplain Tag#isUnregistered() unregistered} {@linkplain EndTag end tag} (<code>&lt;/ </code>&#46;&#46;&#46;<code> &gt;</code>).
	 * <p>
	 * See the documentation of the {@link Tag#isUnregistered()} method for details.
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property/Method<th>Value
	 *     <tr><td>{@link #getDescription() Description}<td>/unregistered
	 *     <tr><td>{@link #getStartDelimiter() StartDelimiter}<td><code>&lt;/</code>
	 *     <tr><td>{@link #getClosingDelimiter() ClosingDelimiter}<td><code>&gt;</code>
	 *     <tr><td>{@link #isServerTag() IsServerTag}<td><code>false</code>
	 *     <tr><td>{@link #getNamePrefix() NamePrefix}<td><i>(empty string)</i>
	 *     <tr><td>{@link #getCorrespondingStartTagType() CorrespondingStartTagType}<td><code>null</code>
	 *     <tr><td>{@link #generateHTML(String) generateHTML}<code>("</code><i>StartTagName</i><code>")</code><td><code>&lt;/</code><i>StartTagName</i><code>&gt;</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;/ "This is not recognised as any of the predefined end tag types in this library"&gt;</code></dd>
	 * </dl>
	 * @see StartTagType#UNREGISTERED
	 */
	public static final EndTagType UNREGISTERED=EndTagTypeUnregistered.INSTANCE;

	/**
	 * The tag type given to a normal HTML or XML {@linkplain EndTag end tag} (<code>&lt;/</code><i>name</i><code>&gt;</code>).
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property/Method<th>Value
	 *     <tr><td>{@link #getDescription() Description}<td>/normal
	 *     <tr><td>{@link #getStartDelimiter() StartDelimiter}<td><code>&lt;/</code>
	 *     <tr><td>{@link #getClosingDelimiter() ClosingDelimiter}<td><code>&gt;</code>
	 *     <tr><td>{@link #isServerTag() IsServerTag}<td><code>false</code>
	 *     <tr><td>{@link #getNamePrefix() NamePrefix}<td><i>(empty string)</i>
	 *     <tr><td>{@link #getCorrespondingStartTagType() CorrespondingStartTagType}<td>{@link StartTagType#NORMAL}
	 *     <tr><td>{@link #generateHTML(String) generateHTML}<code>("</code><i>StartTagName</i><code>")</code><td><code>&lt;/</code><i>StartTagName</i><code>&gt;</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;/div&gt;</code></dd>
	 * </dl>
	 */
	public static final EndTagType NORMAL=EndTagTypeNormal.INSTANCE;

	/**
	 * Constructs a new <code>EndTagType</code> object with the specified properties.
	 * <br />(<a href="TagType.html#ImplementationAssistance">implementation assistance</a> method)
	 * <p>
	 * As <code>EndTagType</code> is an abstract class, this constructor is only called from sub-class constructors.
	 *
	 * @param description  a {@linkplain #getDescription() description} of the new end tag type useful for debugging purposes.
	 * @param startDelimiter  the {@linkplain #getStartDelimiter() start delimiter} of the new end tag type.
	 * @param closingDelimiter  the {@linkplain #getClosingDelimiter() closing delimiter} of the new end tag type.
	 * @param isServerTag  indicates whether the new end tag type is a {@linkplain #isServerTag() server tag}.
	 */
	protected EndTagType(final String description, final String startDelimiter, final String closingDelimiter, final boolean isServerTag) {
		super(description,startDelimiter.toLowerCase(),closingDelimiter,isServerTag,START_DELIMITER_PREFIX);
		if (!getStartDelimiter().startsWith(START_DELIMITER_PREFIX)) throw new IllegalArgumentException("startDelimiter of an end tag must start with \""+START_DELIMITER_PREFIX+'"');
	}

	/**
	 * Returns the {@linkplain StartTagType type} of {@linkplain StartTag start tag} that is <i>usually</i> paired with an 
	 * {@linkplain EndTag end tag} of this type to form an {@link Element}.
	 * <br />(<a href="TagType.html#DefaultImplementation">default implementation</a> method)
	 * <p>
	 * The default implementation returns <code>null</code>.
	 * <p>
	 * This property is informational only and is not used by the parser in any way.
	 * <p>
	 * The mapping of end tag type to the corresponding start tag type is in any case one-to-many, which is why the definition
	 * emphasises the word "usually".
	 * An example of this is the {@link PHPTagTypes#PHP_SCRIPT} start tag type,
	 * whose {@linkplain StartTagType#getCorrespondingEndTagType() corresponding end tag type} is {@link #NORMAL EndTagType.NORMAL},
	 * while the converse is not true.
	 * <p>
	 * The only <a href="TagType.html#Predefined">predefined</a> end tag type that returns <code>null</code> for this property is the
	 * special {@link #UNREGISTERED} end tag type.
	 * <p>
	 * Although this method is used like a <a href="TagType.html#Property">property</a> method, it is implemented as a
	 * <a href="TagType.html#DefaultImplementation">default implementation</a> method to avoid cyclic references between statically
	 * instantiated {@link StartTagType} and <code>EndTagType</code> objects.
	 * <p>
	 * <dl>
	 *  <dt>Standard Tag Type Values:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>End Tag Type<th>Corresponding Start Tag Type
	 *     <tr><td>{@link EndTagType#UNREGISTERED}<td><code>null</code>
	 *     <tr><td>{@link EndTagType#NORMAL}<td>{@link StartTagType#NORMAL}
	 *    </table>
	 * </dl>
	 * <dl>
	 *  <dt>Extended Tag Type Values:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>End Tag Type<th>Corresponding Start Tag Type
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT_END}<td>{@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT}
	 *     <tr><td>{@link MasonTagTypes#MASON_NAMED_BLOCK_END}<td>{@link MasonTagTypes#MASON_NAMED_BLOCK}
	 *    </table>
	 * </dl>
	 *
	 * @return the {@linkplain StartTagType type} of {@linkplain StartTag start tag} that is <i>usually</i> paired with an {@linkplain EndTag end tag} of this type to form an {@link Element}.
	 * @see StartTagType#getCorrespondingEndTagType()
	 */
	public StartTagType getCorrespondingStartTagType() {
		return null;
	}

	/**
	 * Generates the HTML text of an {@linkplain EndTag end tag} of this type given the {@linkplain StartTag#getName() name} of a {@linkplain #getCorrespondingStartTagType() corresponding} {@linkplain StartTag start tag}.
	 * <br />(<a href="TagType.html#Property">property</a> method)
	 * <p>
	 * This default implementation returns {@link #getStartDelimiter()}<code>+startTagName+</code>{@link #getClosingDelimiter()}.
	 * <p>
	 * <dl>
	 *  <dt>Standard Tag Type Values:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>End Tag Type<th>HTML Generated for specified <i>StartTagName</i>
	 *     <tr><td>{@link EndTagType#UNREGISTERED}<td><code>&lt;/</code><i>StartTagName</i><code>&gt;</code>
	 *     <tr><td>{@link EndTagType#NORMAL}<td><code>&lt;/</code><i>StartTagName</i><code>&gt;</code>
	 *    </table>
	 * </dl>
	 * <dl>
	 *  <dt>Extended Tag Type Values:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>End Tag Type<th>HTML Generated for specified <i>StartTagName</i>
	 *     <tr><td>{@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT_END}<td><code>&lt;/&amp;&gt;</code>
	 *     <tr><td>{@link MasonTagTypes#MASON_NAMED_BLOCK_END}<td><code>&lt;/%</code><i>StartTagName</i><code>&gt;</code>
	 *    </table>
	 * </dl>
	 *
	 * @param startTagName  the {@linkplain StartTag#getName() name} of a {@linkplain #getCorrespondingStartTagType() corresponding} {@linkplain StartTag start tag}.
	 * @return the HTML text of an {@linkplain EndTag end tag} of this type given the {@linkplain StartTag#getName() name} of a {@linkplain #getCorrespondingStartTagType() corresponding} {@linkplain StartTag start tag}.
	 */
	public String generateHTML(final String startTagName) {
		return getStartDelimiter()+startTagName+getClosingDelimiter();
	}
	
	/**
	 * Internal method for the construction of an {@link EndTag} object of this type.
	 * <br />(<a href="TagType.html#ImplementationAssistance">implementation assistance</a> method)
	 * <p>
	 * Intended for use from within the {@link #constructTagAt(Source,int) constructTagAt(Source, int pos)} method.
	 *
	 * @param source  the {@link Source} document.
	 * @param begin  the character position in the source document where this tag {@linkplain Segment#getBegin() begins}.
	 * @param end  the character position in the source document where this tag {@linkplain Segment#getEnd() ends}.
	 * @param name  the {@linkplain Tag#getName() name} of the tag.
	 * @return the new {@link EndTag} object.
	 */
	protected final EndTag constructEndTag(final Source source, final int begin, final int end, final String name) {
		return new EndTag(source,begin,end,this,name);
	}
}
