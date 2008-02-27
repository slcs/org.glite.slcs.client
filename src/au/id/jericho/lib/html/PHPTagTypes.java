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
 * Contains {@linkplain TagType tag types} related to the <a target="_blank" href="http://www.php.net">PHP</a> server platform.
 * <p>
 * There is no specific tag type defined for the
 * <a target="_blank" href="http://www.php.net/manual/en/ini.core.php#ini.asp-tags">ASP-style PHP tag</a>
 * as it is recognised using the {@linkplain StartTagType#SERVER_COMMON common server tag type}.
 * <p>
 * The tag types defined in this class are not {@linkplain TagType#register() registered} by default.
 * The {@link #register()} method is provided as a convenient way to register them all at once.
 */
public final class PHPTagTypes {

	/**
	 * The tag type given to a 
	 * <a target="_blank" href="http://www.php.net/manual/en/language.basic-syntax.php#language.basic-syntax.phpmode">standard PHP tag</a>
	 * (<code>&lt;&#63;php </code>&#46;&#46;&#46;<code> &#63;&gt;</code>).
	 * <p>
	 * Note that the standard PHP processor includes as part of the tag any newline characters directly following the
	 * {@linkplain TagType#getClosingDelimiter() closing delimiter}, but PHP tags recognised by this library do not include
	 * trailing newlines.  They must be removed manually if required.
	 * <p>
	 * This library only correctly recognises standard PHP tags that comply with the XML syntax for processing instructions.
	 * Specifically, the tag is terminated by the first occurrence of the {@linkplain TagType#getClosingDelimiter() closing delimiter}
	 * "<code>?&gt;</code>", even if it occurs within a PHP string expression.
	 * Unfortunately there is no reliable way to determine the end of a PHP tag without the use of a PHP parser.
	 * The following code is an example of a standard PHP tag that is <b>not</b> recognised correctly by this parser
	 * because of the presence of the closing delimiter within a string expression:
	 * <p>
	 * <pre>&lt;?php echo("&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n"); ?&gt;</pre>
	 * <p>
	 * This is recognised as the PHP tag: <code>&lt;?php echo("&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;</code><br />
	 * followed by the plain text: <code>\n"); ?&gt;</code>
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property<th>Value
	 *     <tr><td>{@link StartTagType#getDescription() Description}<td>PHP standard tag
	 *     <tr><td>{@link StartTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;?php</code>
	 *     <tr><td>{@link StartTagType#getClosingDelimiter() ClosingDelimiter}<td><code>?&gt;</code>
	 *     <tr><td>{@link StartTagType#isServerTag() IsServerTag}<td><code>true</code>
	 *     <tr><td>{@link StartTagType#getNamePrefix() NamePrefix}<td><code>?php</code>
	 *     <tr><td>{@link StartTagType#getCorrespondingEndTagType() CorrespondingEndTagType}<td><code>null</code>
	 *     <tr><td>{@link StartTagType#hasAttributes() HasAttributes}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#isNameAfterPrefixRequired() IsNameAfterPrefixRequired}<td><code>false</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;?php echo '&lt;p&gt;Hello World&lt;/p&gt;'; ?&gt;</code></dd>
	 * </dl>
	 */
	public static final StartTagType PHP_STANDARD=StartTagTypePHPStandard.INSTANCE;

	/**
	 * The tag type given to a 
	 * <a target="_blank" href="http://www.php.net/manual/en/ini.core.php#ini.short-open-tag">short-form PHP tag</a>
	 * (<code>&lt;&#63; </code>&#46;&#46;&#46;<code> &#63;&gt;</code>).
	 * <p>
	 * When this tag type is {@linkplain TagType#register() registered}, all 
	 * {@linkplain StartTagType#XML_PROCESSING_INSTRUCTION XML processing instructions} are recognised as short-form PHP tags instead.
	 * <p>
	 * The comments in the documentation of the {@link #PHP_STANDARD} tag type regarding the termination of PHP tags and
	 * trailing newlines are also applicable to this tag type.
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property<th>Value
	 *     <tr><td>{@link StartTagType#getDescription() Description}<td>PHP short tag
	 *     <tr><td>{@link StartTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;?</code>
	 *     <tr><td>{@link StartTagType#getClosingDelimiter() ClosingDelimiter}<td><code>?&gt;</code>
	 *     <tr><td>{@link StartTagType#isServerTag() IsServerTag}<td><code>true</code>
	 *     <tr><td>{@link StartTagType#getNamePrefix() NamePrefix}<td><code>?</code>
	 *     <tr><td>{@link StartTagType#getCorrespondingEndTagType() CorrespondingEndTagType}<td><code>null</code>
	 *     <tr><td>{@link StartTagType#hasAttributes() HasAttributes}<td><code>false</code>
	 *     <tr><td>{@link StartTagType#isNameAfterPrefixRequired() IsNameAfterPrefixRequired}<td><code>false</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;? echo '&lt;p&gt;Hello World&lt;/p&gt;'; ?&gt;</code></dd>
	 * </dl>
	 */
	public static final StartTagType PHP_SHORT=StartTagTypePHPShort.INSTANCE;

	/**
	 * The tag type given to a 
	 * <a target="_blank" href="http://www.php.net/manual/en/language.basic-syntax.php#language.basic-syntax.phpmode">script-style PHP start tag</a>
	 * (<code>&lt;script language="php"&gt; </code>&#46;&#46;&#46;<code> &lt;/script&gt;</code>).
	 * <p>
	 * <dl>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <table class="bordered" style="margin: 15px" cellspacing="0">
	 *     <tr><th>Property<th>Value
	 *     <tr><td>{@link StartTagType#getDescription() Description}<td>PHP script
	 *     <tr><td>{@link StartTagType#getStartDelimiter() StartDelimiter}<td><code>&lt;script</code>
	 *     <tr><td>{@link StartTagType#getClosingDelimiter() ClosingDelimiter}<td><code>&gt;</code>
	 *     <tr><td>{@link StartTagType#isServerTag() IsServerTag}<td><code>true</code>
	 *     <tr><td>{@link StartTagType#getNamePrefix() NamePrefix}<td><code>script</code>
	 *     <tr><td>{@link StartTagType#getCorrespondingEndTagType() CorrespondingEndTagType}<td>{@link EndTagType#NORMAL}
	 *     <tr><td>{@link StartTagType#hasAttributes() HasAttributes}<td><code>true</code>
	 *     <tr><td>{@link StartTagType#isNameAfterPrefixRequired() IsNameAfterPrefixRequired}<td><code>false</code>
	 *    </table>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;script language="php"&gt; echo '&lt;p&gt;Hello World&lt;/p&gt;'; &lt;/script&gt;</code></dd>
	 * </dl>
	 */
	public static final StartTagType PHP_SCRIPT=StartTagTypePHPScript.INSTANCE;

	private static final TagType[] TAG_TYPES={
		PHP_STANDARD,
		PHP_SHORT,
		PHP_SCRIPT
	};

	private PHPTagTypes() {}

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
	 * Indicates whether the specified tag type is recognised by a <a target="_blank" href="http://www.php.net">PHP</a> parser.
	 * <p>
	 * This is true if the specified tag type is {@linkplain #defines(TagType) defined in this class} or if it is the 
	 * {@linkplain StartTagType#SERVER_COMMON common server tag type}.
	 * 
	 * @param tagType  the {@link TagType} to test.
	 * @return <code>true</code> if the specified tag type is recognised by a <a target="_blank" href="http://www.php.net">PHP</a> parser, otherwise <code>false</code>.
	 */
	public static boolean isParsedByPHP(final TagType tagType) {
		return tagType==StartTagType.SERVER_COMMON || defines(tagType);
	}
}
