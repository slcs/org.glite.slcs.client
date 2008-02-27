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
 * Contains {@linkplain TagType tag types} related to the <a target="_blank" href="http://www.masonhq.com/">Mason</a> server platform.
 * <p>
 * There is no specific tag type defined for the
 * <a target="_blank" href="http://www.masonbook.com/book/chapter-2.mhtml#CHP-2-SECT-3.1">Mason substitution tag</a>
 * as it is recognised using the {@linkplain StartTagType#SERVER_COMMON common server tag type}.
 * <p>
 * The tag types defined in this class are not {@linkplain TagType#register() registered} by default.
 * The {@link #register()} method is provided as a convenient way to register them all at once.
 */
public final class MasonTagTypes {

	/**
	 * The tag type given to a
	 * <a target="_blank" href="http://www.masonbook.com/book/chapter-2.mhtml#CHP-2-SECT-3.3">Mason component call</a>
	 * (<code>&lt;&amp; </code>&#46;&#46;&#46;<code> &amp;&gt;</code>).
 	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property<th>Value
	 *     <tr><td>{@link StartTagType#getDescription() Description}<td>mason component call
	 *     <tr><td>{@link StartTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;&amp;</code>
	 *     <tr><td>{@link StartTagType#getClosingDelimiter() ClosingDelimiter}<td><code>&amp;&gt;</code>
	 *     <tr><td>{@link StartTagType#isServerTag() IsServerTag}<td><code>true</code>
	 *     <tr><td>{@link StartTagType#getNamePrefix() NamePrefix}<td><code>&amp;</code>
	 *     <tr><td>{@link StartTagType#getCorrespondingEndTagType() CorrespondingEndTagType}<td><code>null</code>
	 *     <tr><td>{@link StartTagType#hasAttributes() HasAttributes}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#isNameAfterPrefixRequired() IsNameAfterPrefixRequired}<td><code>false</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;&amp; menu &amp;&gt;</code></dd>
	 * </dl>
	 */
	public static final StartTagType MASON_COMPONENT_CALL=StartTagTypeMasonComponentCall.INSTANCE;

	/**
	 * The tag type given to the start tag of a
	 * <a target="_blank" href="http://www.masonbook.com/book/chapter-2.mhtml#CHP-2-SECT-3.3.1">Mason component called with content</a>
	 * (<code>&lt;&amp;| </code>&#46;&#46;&#46;<code> &amp;&gt; </code>&#46;&#46;&#46;<code> &lt;/&amp;&gt;</code>).
 	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property<th>Value
	 *     <tr><td>{@link StartTagType#getDescription() Description}<td>mason component called with content
	 *     <tr><td>{@link StartTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;&amp;|</code>
	 *     <tr><td>{@link StartTagType#getClosingDelimiter() ClosingDelimiter}<td><code>&amp;&gt;</code>
	 *     <tr><td>{@link StartTagType#isServerTag() IsServerTag}<td><code>true</code>
	 *     <tr><td>{@link StartTagType#getNamePrefix() NamePrefix}<td><code>&amp;|</code>
	 *     <tr><td>{@link StartTagType#getCorrespondingEndTagType() CorrespondingEndTagType}<td>{@link #MASON_COMPONENT_CALLED_WITH_CONTENT_END}
	 *     <tr><td>{@link StartTagType#hasAttributes() HasAttributes}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#isNameAfterPrefixRequired() IsNameAfterPrefixRequired}<td><code>false</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><pre> &lt;&amp;| /sql/select, query =&gt; 'SELECT name, age FROM User' &amp;&gt;
	 *   &lt;tr&gt;&lt;td&gt;%name&lt;/td&gt;&lt;td&gt;%age&lt;/td&gt;&lt;/tr&gt;
	 * &lt;/&amp;&gt;</pre></dd>
	 * </dl>
	 */
	public static final StartTagType MASON_COMPONENT_CALLED_WITH_CONTENT=StartTagTypeMasonComponentCalledWithContent.INSTANCE;

	/**
	 * The tag type given to the end tag of a
	 * <a target="_blank" href="http://www.masonbook.com/book/chapter-2.mhtml#CHP-2-SECT-3.3.1">Mason component called with content</a>.
	 * <p>
	 * See the {@linkplain EndTagType#getCorrespondingStartTagType() corresponding start tag type}
	 * {@link #MASON_COMPONENT_CALLED_WITH_CONTENT} for more details.
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property/Method<th>Value
	 *     <tr><td>{@link EndTagType#getDescription() Description}<td>/mason component called with content
	 *     <tr><td>{@link EndTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;/&amp;</code>
	 *     <tr><td>{@link EndTagType#getClosingDelimiter() ClosingDelimiter}<td><code>&gt;</code>
	 *     <tr><td>{@link EndTagType#isServerTag() IsServerTag}<td><code>true</code>
	 *     <tr><td>{@link EndTagType#getNamePrefix() NamePrefix}<td><code>/&amp;</code>
	 *     <tr><td>{@link EndTagType#getCorrespondingStartTagType() CorrespondingStartTagType}<td>{@link #MASON_COMPONENT_CALLED_WITH_CONTENT}
	 *     <tr><td>{@link EndTagType#generateHTML(String) generateHTML}<code>("</code><i>StartTagName</i><code>")</code><td><code>&lt;/&amp;&gt;</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;/&amp;&gt;</code></dd>
	 * </dl>
	 * @see #MASON_COMPONENT_CALLED_WITH_CONTENT
	 */
	public static final EndTagType MASON_COMPONENT_CALLED_WITH_CONTENT_END=EndTagTypeMasonComponentCalledWithContent.INSTANCE;
	
	/**
	 * The tag type given to the start tag of a
	 * <a target="_blank" href="http://www.masonbook.com/book/chapter-2.mhtml#CHP-2-SECT-3.4">Mason named block</a>
	 * (<code>&lt;%<i>name</i> </code>&#46;&#46;&#46;<code> &gt; </code>&#46;&#46;&#46;<code> &lt;/%<i>name</i>&gt;</code>).
	 * <p>
	 * A tag of this type <b>must not</b> have a '<code>%</code>' character before its 
	 * {@linkplain StartTagType#getClosingDelimiter() closing delimiter}, otherwise it is most likely a
	 * {@linkplain StartTagType#SERVER_COMMON common server tag}.
	 * <p>
	 * For the start tag to be recognised, a {@linkplain StartTagType#getCorrespondingEndTagType() corresponding} end tag of the
	 * {@linkplain #MASON_NAMED_BLOCK_END correct type} <b>must</b> exist somewhere in the source document following the start tag.
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property<th>Value
	 *     <tr><td>{@link StartTagType#getDescription() Description}<td>mason named block
	 *     <tr><td>{@link StartTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;%</code>
	 *     <tr><td>{@link StartTagType#getClosingDelimiter() ClosingDelimiter}<td><code>&gt;</code>
	 *     <tr><td>{@link StartTagType#isServerTag() IsServerTag}<td><code>true</code>
	 *     <tr><td>{@link StartTagType#getNamePrefix() NamePrefix}<td><code>%</code>
	 *     <tr><td>{@link StartTagType#getCorrespondingEndTagType() CorrespondingEndTagType}<td>{@link #MASON_NAMED_BLOCK_END}
	 *     <tr><td>{@link StartTagType#hasAttributes() HasAttributes}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#isNameAfterPrefixRequired() IsNameAfterPrefixRequired}<td><code>true</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;%perl&gt; print "hello world"; &lt;/%perl&gt;</code></dd>
	 * </dl>
	 */
	public static final StartTagType MASON_NAMED_BLOCK=StartTagTypeMasonNamedBlock.INSTANCE;

	/**
	 * The tag type given to the end tag of a
	 * <a target="_blank" href="http://www.masonbook.com/book/chapter-2.mhtml#CHP-2-SECT-3.4">Mason named block</a>.
	 * <p>
	 * See the {@linkplain EndTagType#getCorrespondingStartTagType() corresponding start tag type}
	 * {@link #MASON_NAMED_BLOCK} for more details.
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property/Method<th>Value
	 *     <tr><td>{@link EndTagType#getDescription() Description}<td>/mason named block
	 *     <tr><td>{@link EndTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;/%</code>
	 *     <tr><td>{@link EndTagType#getClosingDelimiter() ClosingDelimiter}<td><code>&gt;</code>
	 *     <tr><td>{@link EndTagType#isServerTag() IsServerTag}<td><code>true</code>
	 *     <tr><td>{@link EndTagType#getNamePrefix() NamePrefix}<td><code>/%</code>
	 *     <tr><td>{@link EndTagType#getCorrespondingStartTagType() CorrespondingStartTagType}<td>{@link #MASON_NAMED_BLOCK}
	 *     <tr><td>{@link EndTagType#generateHTML(String) generateHTML}<code>("</code><i>StartTagName</i><code>")</code><td><code>&lt;/%</code><i>StartTagName</i><code>&gt;</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;/%perl&gt;</code></dd>
	 * </dl>
	 * @see #MASON_NAMED_BLOCK
	 */
	public static final EndTagType MASON_NAMED_BLOCK_END=EndTagTypeMasonNamedBlock.INSTANCE;

	private static final TagType[] TAG_TYPES={
		MASON_COMPONENT_CALL,
		MASON_COMPONENT_CALLED_WITH_CONTENT,
		MASON_COMPONENT_CALLED_WITH_CONTENT_END,
		MASON_NAMED_BLOCK,
		MASON_NAMED_BLOCK_END
	};

	private MasonTagTypes() {}
	
	/** 
	 * {@linkplain TagType#register() Registers} all of the tag types defined in this class at once.
	 * <p>
	 * The tag types must be registered before the parser will recognise them.
	 */
	public static void register() {
		for (int i=0; i<TAG_TYPES.length; i++) TAG_TYPES[i].register();
	}

	/**
	 * Indicates whether the specified tag type is defined in this class.
	 *
	 * @param tagType  the {@link TagType} to test.
	 * @return <code>true</code> if the specified tag type is defined in this class, otherwise <code>false</code>.
	 */
	public static boolean defines(final TagType tagType) {
		for (int i=0; i<TAG_TYPES.length; i++)
			if (tagType==TAG_TYPES[i]) return true;
		return false;
	}
	
	/** 
	 * Indicates whether the specified tag type is recognised by a <a target="_blank" href="http://www.masonhq.com/">Mason</a> parser.
	 * <p>
	 * This is true if the specified tag type is {@linkplain #defines(TagType) defined in this class} or if it is the 
	 * {@linkplain StartTagType#SERVER_COMMON common server tag type}.
	 * 
	 * @param tagType  the {@link TagType} to test.
	 * @return <code>true</code> if the specified tag type is recognised by a <a target="_blank" href="http://www.masonhq.com/">Mason</a> parser, otherwise <code>false</code>.
	 */
	public static boolean isParsedByMason(final TagType tagType) {
		return tagType==StartTagType.SERVER_COMMON || defines(tagType);
	}
}
