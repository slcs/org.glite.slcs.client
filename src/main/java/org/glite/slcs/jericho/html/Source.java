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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a source HTML document.
 * <p>
 * The first step in parsing an HTML document is always to construct a <code>Source</code> object from the source data, which can be a
 * <code>String</code>, <code>Reader</code>, <code>InputStream</code> or <code>URL</code>.
 * Each constructor uses all the evidence available to determine the original {@linkplain #getEncoding() character encoding} of the data.
 * <p>
 * Once the <code>Source</code> object has been created, you can immediately start searching for {@linkplain Tag tags} or {@linkplain Element elements} within the document
 * using the <a href="Tag.html#TagSearchMethods">tag search methods</a>.
 * It is strongly advised however to first think about how many of the document's tags you will need to parse.
 * If you will be searching for all or most of the tags, performance can be greatly improved by first calling the {@link #fullSequentialParse()} method.
 * If you only need to parse a few tags, performance will probably be better if you use the default <a href="#ParseOnDemand">parse on demand</a> mode.
 * <p>
 * It can also be useful to {@linkplain #setLogWriter(Writer) set the location of the log writer} before calling any tag search methods
 * so that important log messages can be traced while the document is being parsed.
 * <p>
 * Note that many of the useful functions which can be performed on the source document are
 * defined in its superclass, {@link Segment}.
 * The source object is itself a segment which spans the entire document.
 * <p>
 * Most of the methods defined in this class are useful for determining the elements and tags
 * surrounding or neighbouring a particular character position in the document.
 * <p>
 * For information on how to create a modified version of this source document, see the {@link OutputDocument} class.
 *
 * @see Segment
 */
public class Source extends Segment {
	final String string;
	String documentSpecifiedEncoding=UNINITIALISED;
	String encoding=UNINITIALISED;
	String encodingSpecificationInfo;
	private ParseText parseText=null;
	private OutputDocument parseTextOutputDocument=null;
	private Writer logWriter=null;
	private RowColumnVector[] rowColumnVectorCacheArray=null;
	final Cache cache=new Cache(this);
	boolean useAllTypesCache=true;
	boolean useSpecialTypesCache=true;
	int endOfLastTagIgnoringEnclosedMarkup=-1; // Always has a value of -1 unless doing full sequential parse.  Used in TagType.isValidPosition() method.
	// cached result lists:
	Tag[] allTagsArray=null; // non-null iff fullSequentialParse was called
	List<Tag> allTags=null;
	List<StartTag> allStartTags=null;
	private List<Element> allElements=null;

	private static final String UNINITIALISED="";

	/**
	 * Constructs a new <code>Source</code> object from the specified text.
	 * @param text  the source text.
	 * @see #setLogWriter(Writer)
	 */
	public Source(final CharSequence text) {
		super(text.length());
		string=text.toString();
	}

	private Source(final EncodedSource encodedSource) throws IOException {
		this(Util.getString(encodedSource.Reader));
		encoding=encodedSource.Encoding;
		encodingSpecificationInfo=encodedSource.EncodingSpecificationInfo;
		// if (encodedSource.HttpURLConnection!=null) encodedSource.HttpURLConnection.disconnect();
	}

	private Source(final Reader reader, final String inputStreamReaderEncoding) throws IOException {
		this(Util.getString(reader));
		if (inputStreamReaderEncoding!=null) {
			encoding=inputStreamReaderEncoding;
			encodingSpecificationInfo="InputStreamReader.getEncoding() of constructor argument";
		}
	}

	/**
	 * Constructs a new <code>Source</code> object by loading the content from the specified <code>Reader</code>.
	 * <p>
	 * If the specified reader is an instance of <code>InputStreamReader</code>, the {@link #getEncoding()} method of the
	 * created source object returns the encoding from <code>InputStreamReader.getEncoding()</code>.
	 *
	 * @param reader  the <code>java.io.Reader</code> from which to load the source text.
	 * @throws java.io.IOException if an I/O error occurs.
	 * @see #setLogWriter(Writer)
	 */
	public Source(final Reader reader) throws IOException {
		this(reader,(reader instanceof InputStreamReader) ? ((InputStreamReader)reader).getEncoding() : null);
	}

	/**
	 * Constructs a new <code>Source</code> object by loading the content from the specified <code>InputStream</code>.
	 * <p>
	 * The algorithm for detecting the character {@linkplain #getEncoding() encoding} of the source document from the raw bytes
	 * of the specified input stream is the same as that for the {@link #Source(URL)} constructor with the following exceptions:
	 * <ul class="HalfSeparated">
	 *  <li>Step 1 is not possible as there is no <code>Content-Type</code> header to check.
	 *  <li>Step 6 is not performed as it is not possible to know whether the input stream was aquired from an HTTP connection.
	 * </ul>
	 *
	 * @param inputStream  the <code>java.io.InputStream</code> from which to load the source text.
	 * @throws java.io.IOException if an I/O error occurs.
	 * @see #getEncoding()
	 * @see #setLogWriter(Writer)
	 */
	public Source(final InputStream inputStream) throws IOException {
		this(EncodedSource.construct(inputStream,null));
	}

	/**
	 * Constructs a new <code>Source</code> object by loading the content from the specified URL.
	 * <p>
	 * The algorithm for detecting the character {@linkplain #getEncoding() encoding} of the source document is as follows:
	 * <ol class="HalfSeparated">
	 *  <li>If the <code>URLConnection.getContentType()</code> specifies an encoding
	 *   (where a <code>charset</code> parameter is included in the value of the the stream's 
	 *   <a target="_blank" href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17">Content-Type</a> header),
	 *   then this is used to decode the input stream and is returned verbatim by the {@link #getEncoding()}
	 *   method of the created source object.  Otherwise:
	 *  <li>Get the content input stream via the <code>URLConnection.getInputStream()</code> method.
	 *  <li>If the input stream is empty, the created source document has zero length and its {@link #getEncoding()} method 
	 *   returns <code>null</code>.  Otherwise:
	 *  <li>Determine a <i>preliminary encoding</i> by examining the first 4 bytes of the input stream:
	 *   <ul class="Unseparated">
	 *    <li>If the first two bytes match the byte order mark (U+FEFF) in either big or little endian order:
	 *     <ul>
	 *      <li>If the third byte is 00, assume a 32-bit encoding (UTF-32).
	 *      <li>Otherwise, assume a 16-bit encoding (UTF-16).
	 *     </ul>
	 *    <li>If the first byte is 00:
	 *     <ul>
	 *      <li>If the second or fourth byte is 00, assume a 32-bit encoding (UTF-32).
	 *      <li>Otherwise, assume a big endian 16-bit encoding without byte order mark (UTF-16BE).
	 *     </ul>
	 *    <li>If the second byte is 00:
	 *     <ul>
	 *      <li>If the third byte is 00, assume a 32-bit encoding (UTF-32).
	 *      <li>Otherwise, assume a little endian 16-bit encoding without byte order mark (UTF-16LE).
	 *     </ul>
	 *    <li>If the first four bytes match the EBDIC encoding of "<code>&lt;?xm</code>", the preliminary encoding is Cp037.
	 *    <li>Otherwise, assume an 8-bit encoding (UTF-8).
	 *   </ul>
	 *  <li>Preview the first 2048 characters of the source document (hereafter referred to as the <i>preview segment</i>)
	 *   using the preliminary encoding.  If the preview segment contains an <a href="#EncodingSpecification">encoding specification</a>
	 *   (which is always at or near the top of the document),
	 *   the specified encoding is used to decode the input stream and is returned verbatim
	 *   by the {@link #getEncoding()} method of the created source object.  Otherwise:
	 *  <li>If the preview segment does not contain an encoding specification, and the <code>URLConnection</code> is an instance of
	 *   <code>HttpURLConnection</code>, then the
	 *   <a target="_blank" href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7.1">HTTP protocol section 3.7.1</a>
	 *   specifies that an encoding of ISO-8859-1 can be assumed.
	 *   An XML document should not assume this as it would require an XML declaration to specify this encoding, which would have been
	 *   detected in one of the previous steps.  So if the preview segment {@linkplain #isXML() is not determined to be XML},
	 *   and the preliminary encoding is 8-bit, then the encoding ISO-8859-1 is used to decode the input stream
	 *   and is returned by the {@link #getEncoding()} method of the created source object.
	 *  <li>Otherwise, the preliminary encoding is used to decode the input stream
	 *   and is returned by the {@link #getEncoding()} method of the created source object.
	 * </ol>
	 *
	 * @param url  the URL from which to load the source text.
	 * @throws java.io.IOException if an I/O error occurs.
	 * @see #getEncoding()
	 * @see #setLogWriter(Writer)
	 */
	public Source(final URL url) throws IOException {
		this(EncodedSource.construct(url));
	}

	private String setEncoding(final String encoding, final String encodingSpecificationInfo) {
		if (this.encoding==UNINITIALISED) {
			this.encoding=encoding;
			this.encodingSpecificationInfo=encodingSpecificationInfo;
		}
		return encoding;
	}

	/**
	 * Returns the document {@linkplain #getEncoding() encoding} specified within the text of the document.
	 * <p>
	 * The document encoding can be specified within the document text in two ways.
	 * They are referred to generically in this library as an <i><a name="EncodingSpecification">encoding specification</a></i>,
	 * and are listed below in order of precedence:
	 * <ol class="HalfSeparated">
	 *  <li>
	 *   An <a target="_blank" href="http://www.w3.org/TR/REC-xml/#sec-TextDecl">XML text declaration</a> at the start of the document,
	 *   which is essentially an {@linkplain StartTagType#XML_DECLARATION XML declaration} with an <code>encoding</code> attribute.
	 *   This is only used in XML documents, and must be present if an XML document has an encoding other than UTF-8 or UTF-16.
	 *   <pre>&lt;?xml version="1.0" encoding="ISO-8859-1" ?&gt;</pre>
	 *  <li>
	 *   A <a target="_blank" href="http://www.w3.org/TR/html401/charset.html#spec-char-encoding">META declaration</a>,
	 *   which is in the form of a {@link HTMLElementName#META META} tag with attribute <code>http-equiv="Content-Type"</code>.
	 *   The encoding is specified in the <code>charset</code> parameter of a
	 *   <code><a target="_blank" href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17">Content-Type</a></code>
	 *   HTTP header value, which is placed in the value of the meta tag's <code>content</code> attribute.
	 *   This META declaration should appear as early as possible in the {@link HTMLElementName#HEAD HEAD} element.
	 *   <pre>&lt;META http-equiv=Content-Type content="text/html; charset=iso-8859-1"&gt;</pre>
	 * </ol>
	 * <p>
	 * Both of these tags must only use unicode characters in the range U+0000 to U+007F, and in the case of the META declaration
	 * must use ASCII encoding.  This, along with the fact that they must occur at or near the beginning of the document,
	 * assists in their detection and decoding without the need to know the exact encoding of the full text.
	 *
	 * @return the document {@linkplain #getEncoding() encoding} specified within the text of the document.
	 * @see #getEncoding()
	 */
	public String getDocumentSpecifiedEncoding() {
		if (documentSpecifiedEncoding!=UNINITIALISED) return documentSpecifiedEncoding;
		final Tag xmlDeclarationTag=getTagAt(0);
		if (xmlDeclarationTag!=null && xmlDeclarationTag.getTagType()==StartTagType.XML_DECLARATION) {
			documentSpecifiedEncoding=((StartTag)xmlDeclarationTag).getAttributeValue("encoding");
			if (documentSpecifiedEncoding!=null) return setEncoding(documentSpecifiedEncoding,xmlDeclarationTag.toString());
		}
		// Check for Content-Type http-equiv meta tag:
		final StartTag contentTypeMetaTag=findNextStartTag(0,"http-equiv","Content-Type",false);
		if (contentTypeMetaTag!=null) {
			final String contentValue=contentTypeMetaTag.getAttributeValue("content");
			if (contentValue!=null) {
				documentSpecifiedEncoding=getCharsetParameterFromHttpHeaderValue(contentValue);
				if (documentSpecifiedEncoding!=null) return setEncoding(documentSpecifiedEncoding,contentTypeMetaTag.toString());
			}
		}
		return setEncoding(null,"no encoding specified in document");
	}

	/**
	 * Returns the original encoding of the source document.
	 * <p>
	 * The encoding of a document defines how the original byte stream was encoded into characters.
	 * The <a taget="_blank" href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.4">HTTP specification section 3.4</a>
	 * defines the term "character set" to refer to the encoding, and the term "charset" is similarly used in Java
	 * (see the class <code>java.nio.charset.Charset</code>).  This is an unfortunate convention that often causes confusion,
	 * as a character set is not the same thing as a character encoding.
	 * For example, the <a target="_blank" href="http://www.unicode.org/">Unicode</a> character set has several encodings, such as
	 * <a target="_blank" href="http://www.unicode.org/faq/utf_bom.html">UTF-8, UTF-16, and UTF-32</a>.
	 * <p>
	 * This method makes the best possible effort to return the name of the encoding used to decode the original source text byte stream
	 * into character data.  This decoding takes place in the constructor when a parameter based on a byte stream such as an
	 * <code>InputStream</code> or <code>URL</code> is used to specify the source text.
	 * The documentation of the {@link #Source(InputStream)} and {@link #Source(URL)} constructors describe how the return value of this
	 * method is determined in these cases.
	 * It is also possible in some circumstances for the encoding to be determined in the {@link #Source(Reader)} constructor.
	 * <p>
	 * If a constructor was used that specifies the source text directly in character form (not requiring the decoding of a byte sequence)
	 * then the document itself is searched for an <a href="#EncodingSpecification">encoding specification</a>.  In this case, this
	 * method returns the same value as the {@link #getDocumentSpecifiedEncoding()} method.
	 * <p>
	 * The {@link #getEncodingSpecificationInfo()} method returns a simple description of how the value of this method was determined.
	 *
	 * @return the original encoding of the source document.
	 * @see #getEncodingSpecificationInfo()
	 */
	public String getEncoding() {
		if (encoding==UNINITIALISED) getDocumentSpecifiedEncoding();
		return encoding;
	}

	/**
	 * Returns a simple description of how the {@linkplain #getEncoding() encoding} of the source document was determined.
	 * <p>
	 * The description is intended for informational purposes only.
	 * It is not guaranteed to have any particular format and can not be reliably parsed.
	 *
	 * @return a simple description of how the {@linkplain #getEncoding() encoding} of the source document was determined.
	 * @see #getEncoding()
	 */
	public String getEncodingSpecificationInfo() {
		if (encoding==UNINITIALISED) getDocumentSpecifiedEncoding();
		return encodingSpecificationInfo;
	}

	/**
	 * Indicates whether the source document is likely to be <a target="_blank" href="http://www.w3.org/TR/REC-xml/">XML</a>.
	 * <p>
	 * The algorithm used to determine this is designed to be relatively inexpensive and to provide an accurate result in
	 * most normal situations.
	 * An exact determination of whether the source document is XML would require a much more complex analysis of the text.
	 * <p>
	 * The algorithm is as follows:
	 * <p>
	 * <ol>
	 *  <li>If the document begins with an {@linkplain StartTagType#XML_DECLARATION XML declaration}, it is an XML document.
	 *  <li>If the document contains a {@linkplain StartTagType#DOCTYPE_DECLARATION document type declaration} that contains the text
	 *   "<code>xhtml</code>", it is an <a target="_blank" href="http://www.w3.org/TR/xhtml1/">XHTML</a> document, and hence
	 *   also an XML document.
	 *  <li>If the document does NOT have an {@link HTMLElementName#HTML HTML} element, assume it is XML.
	 *   This assumption is based on the premise that the library is used to parse HTML or XML documents only.
	 *  <li>If none of the above conditions are met, assume the document is normal HTML, and therefore not an XML document.
	 * </ol>
	 *
	 * @return <code>true</code> if the source document is likely to be <a target="_blank" href="http://www.w3.org/TR/REC-xml/">XML</a>, otherwise <code>false</code>.
	 */
	public boolean isXML() {
		final Tag xmlDeclarationTag=getTagAt(0);
		if (xmlDeclarationTag!=null && xmlDeclarationTag.getTagType()==StartTagType.XML_DECLARATION) return true;
		final Tag doctypeTag=findNextTag(0,StartTagType.DOCTYPE_DECLARATION);
		// if document has a DOCTYPE declaration and it contains the text "xhtml", it is an XML document:
		if (doctypeTag!=null && getParseText().indexOf("xhtml",doctypeTag.begin,doctypeTag.end)!=-1) return true;
		// if document doesn't have an HTML element, it is also most likely an XML document, otherwise assume it is normal HTML:
		return findNextStartTag(0,HTMLElementName.HTML)==null;
	}

	/**
	 * Returns the row number of the specified character position in the source document.
	 * @param pos  the position in the source document.
	 * @return the row number of the specified character position in the source document.
	 * @throws IndexOutOfBoundsException if the specified position is not within the bounds of the document.
	 * @see #getColumn(int pos)
	 * @see #getRowColumnVector(int pos)
	 */
	public int getRow(final int pos) {
		return getRowColumnVector(pos).getRow();
	}

	/**
	 * Returns the column number of the specified character position in the source document.
	 * @param pos  the position in the source document.
	 * @return the column number of the specified character position in the source document.
	 * @throws IndexOutOfBoundsException if the specified position is not within the bounds of the document.
	 * @see #getRow(int pos)
	 * @see #getRowColumnVector(int pos)
	 */
	public int getColumn(final int pos) {
		return getRowColumnVector(pos).getColumn();
	}

	/**
	 * Returns a {@link RowColumnVector} object representing the row and column number of the specified character position in the source document.
	 * @param pos  the position in the source document.
	 * @return a {@link RowColumnVector} object representing the row and column number of the specified character position in the source document.
	 * @throws IndexOutOfBoundsException if the specified position is not within the bounds of the document.
	 * @see #getRow(int pos)
	 * @see #getColumn(int pos)
	 */
	public RowColumnVector getRowColumnVector(final int pos) {
		if (pos>end) throw new IndexOutOfBoundsException();
		if (rowColumnVectorCacheArray==null) rowColumnVectorCacheArray=RowColumnVector.getCacheArray(this);
		return RowColumnVector.get(rowColumnVectorCacheArray,pos);
	}
	
	/**
	 * Returns the source text as a <code>String</code>.
	 * @return the source text as a <code>String</code>.
	 */
	public String toString() {
		return string;
	}

	/**
	 * Parses all of the {@linkplain Tag tags} in this source document sequentially from beginning to end.
	 * <p>
	 * Calling this method can greatly improve performance if most or all of the tags in the document need to be parsed.
	 * It is typically called before any of the <a href="Tag.html#TagSearchMethods">tag search methods</a> are called on this <code>Source</code> object,
	 * directly after {@linkplain #setLogWriter(Writer) setting the location of the log writer}.
	 * <p>
	 * By default, tags are parsed only as needed, which is referred to as <i><a name="ParseOnDemand">parse on demand</a></i> mode.
	 * In this mode, every call to a tag search method that is not returning previously cached tags must perform a relatively complex check to determine whether a
	 * potential tag is in a {@linkplain TagType#isValidPosition(Source,int) valid position}.
	 * <p>
	 * Generally speaking, a tag is in a valid position if it does not appear inside any another tag.
	 * {@linkplain TagType#isServerTag() Server tags} can appear anywhere in a document, including inside other tags, so this relates only to non-server tags.
	 * Theoretically, checking whether a specified position in the document is enclosed in another tag is only possible if every preceding tag has been parsed,
	 * otherwise it is impossible to tell whether one of the delimiters of the enclosing tag was in fact enclosed by some other tag before it, thereby invalidating it.
	 * <p>
	 * When this method is called, each tag is parsed in sequence starting from the beginning of the document, making it easy to check whether each potential
	 * tag is in a valid position.
	 * In <i>parse on demand</i> mode a compromise technique must be used for this check, since the theoretical requirement of having parsed all preceding tags 
	 * is no longer practical.  
	 * This compromise involves only checking whether the position is enclosed by other tags with {@linkplain TagType#getTagTypesIgnoringEnclosedMarkup() certain tag types}.
	 * The added complexity of this technique makes parsing each tag slower compared to when a full sequential parse is performed, but when only a few tags need
	 * parsing this is an extremely beneficial trade-off.
	 * <p>
	 * The documentation of the {@link TagType#isValidPosition(Source, int pos)} method, which is called internally by the parser to perform the valid position check,
	 * includes a more detailed explanation of the differences between the two modes of operation.
	 * <p>
	 * If the {@link #findAllTags()}, {@link #findAllStartTags()} or {@link #findAllElements()} method is called on the <code>Source</code> object
	 * without having called this method first, a {@linkplain #setLogWriter(Writer) log} message is generated recommending its use.
	 * <p>
	 * This method returns the same list of tags as the {@link Source#findAllTags() Source.findAllTags()} method, but as an array instead of a list.
	 * <p>
	 * If this method is called after any of the <a href="Tag.html#TagSearchMethods">tag search methods</a> are called,
	 * the {@linkplain #getCacheDebugInfo() cache} is cleared of any previously found tags before being restocked via the full sequential parse.
	 * This is significant if the {@link Segment#ignoreWhenParsing()} method has been called since the tags were first found, as any tags inside the
	 * ignored segments will no longer be returned by any of the <a href="Tag.html#TagSearchMethods">tag search methods</a>.
	 * <p>
	 * See also the {@link Tag} class documentation for more general details about how tags are parsed.
	 *
	 * @return an array of all {@linkplain Tag tags} in this source document.
	 */
	public Tag[] fullSequentialParse() {
		// The assumeNoNestedTags flag tells the parser not to bother checking for tags inside other tags
		// if the user knows that the document doesn't contain any server tags.
		// This results in a more efficient search, but the difference during benchmark tests was only minimal -
		// about 12% speed improvement in a 1MB document containing 70,000 tags, 75% of which were inside a comment tag.
		// With such a small improvement in a document specifically designed to show an an exaggerated improvement,
		// it is not worth documenting this feature.
		// The flag has been retained internally however as it does not have a measurable performance impact to check for it.
		final boolean assumeNoNestedTags=false;
		if (cache.getTagCount()!=0) cache.clear();
		final boolean useAllTypesCacheSave=useAllTypesCache;
		try {
			useAllTypesCache=false;
			useSpecialTypesCache=false;
			return Tag.parseAll(this,assumeNoNestedTags);
		} finally {
			useAllTypesCache=useAllTypesCacheSave;
			useSpecialTypesCache=true;
			endOfLastTagIgnoringEnclosedMarkup=-1;
		}
	}

	/**
	 * Returns a list of the top-level {@linkplain Element elements} in the document element hierarchy.
	 * <p>
	 * The {@link Source#fullSequentialParse()} method should be called after construction of the <code>Source</code> object if this method is to be used.
	 * <p>
	 * The objects in the list are all of type {@link Element}.
	 * <p>
	 * The term <i><a name="TopLevelElement">top-level element</a></i> refers to an element that is not nested within any other element in the document.
	 * <p>
	 * The term <i><a name="DocumentElementHierarchy">document element hierarchy</a></i> refers to the hierarchy of elements that make up this source document.
	 * While the document itself is theoretically at the top of the hierarchy, this library only considers {@link Element} objects to be part of the hierarchy,
	 * so the top-level elements are the immediate children of the source document.
	 * <p>
	 * The {@link Element#getChildElements()} method can be used to get the decendents of the top-level elements.
	 * <p>
	 * The document element hierarchy differs from that of the <a target="_blank" href="http://en.wikipedia.org/wiki/Document_Object_Model">Document Object Model</a>
	 * in that it is only a representation of the elements that are physically present in the source text.  Unlike the DOM, it does not include any "implied" HTML elements
	 * such as {@link HTMLElementName#TBODY TBODY} if they are not present in the source text.
	 * <p>
	 * Elements formed from {@linkplain TagType#isServerTag() server tags} are not included in the hierarchy at all.
	 * <p>
	 * Structural errors in this source document such as overlapping elements are reported in the {@linkplain #setLogWriter(Writer) log}.
	 * In the case that two elements are found to overlap, the position of the start tag determines the location of the element in the hierarchy.
	 * <p>
	 * A visual representation of the document element hierarchy can be obtained by calling
	 * {@link #indent(String,boolean,boolean,boolean) indent("&nbsp;&nbsp;",true,true,true)}.
	 *
	 * @return a list of the top-level {@linkplain Element elements} in the document element hierarchy, guaranteed not <code>null</code>.
	 * @see Element#getParentElement()
	 * @see Element#getChildElements()
	 * @see Element#getDepth()
	 */
	public List<Element> getChildElements() {
		if (childElements==null) {
			if (length()==0) {
				childElements=Collections.emptyList();
			} else {
				if (allTags==null) log("NOTE: Calling Source.fullSequentialParse() can significantly improve the performance of this operation");
				childElements=new ArrayList<Element>();
				int pos=0;
				while (true) {
					final StartTag childStartTag=source.findNextStartTag(pos);
					if (childStartTag==null) break;
					if (!Config.IncludeServerTagsInElementHierarchy && childStartTag.getTagType().isServerTag()) {
						pos=childStartTag.end;
						continue;
					}
					final Element childElement=childStartTag.getElement();
					childElement.parentElement=null;
					childElements.add(childElement);
					childElement.getChildElements(0);
					pos=childElement.end;
				}
			}
		}
		return childElements;
	}

	/**
	 * Returns a list of all {@linkplain Tag tags} in this source document.
	 * <p>
	 * The {@link #fullSequentialParse()} method should be called after construction of the <code>Source</code> object if this method is to be used.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @return a list of all {@linkplain Tag tags} in this source document.
	 */
	public List<Tag> findAllTags() {
		if (allTags==null) {
			log("NOTE: Calling Source.fullSequentialParse() can significantly improve the performance of this operation");
			allTags=super.findAllTags();
		}
		return allTags;
	}

	/**
	 * Returns a list of all {@linkplain StartTag start tags} in this source document.
	 * <p>
	 * The {@link #fullSequentialParse()} method should be called after construction of the <code>Source</code> object if this method is to be used.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @return a list of all {@linkplain StartTag start tags} in this source document.
	 */
	public List<StartTag> findAllStartTags() {
		if (allStartTags==null) {
			final List<Tag> allTags=findAllTags();
			allStartTags= new ArrayList<StartTag>(allTags.size());
			for (Tag tag : allTags) {
				if (tag instanceof StartTag) allStartTags.add((StartTag) tag);
			}
		}
		return allStartTags;
	}

	/**
	 * Returns a list of all {@linkplain Element elements} in this source document.
	 * <p>
	 * The {@link #fullSequentialParse()} method should be called after construction of the <code>Source</code> object if this method is to be used.
	 * <p>
	 * The elements returned correspond exactly with the start tags returned in the {@link #findAllStartTags()} method.
	 *
	 * @return a list of all {@linkplain Element elements} in this source document.
	 */
	public List<Element> findAllElements() {
		if (allElements==null) {
			final List<?> allStartTags=findAllStartTags();
			if (allStartTags.isEmpty()) return Collections.emptyList();
			allElements=new ArrayList<Element>(allStartTags.size());
			for (final Iterator<?> i=allStartTags.iterator(); i.hasNext();) {
				final StartTag startTag=(StartTag)i.next();
				allElements.add(startTag.getElement());
			}
		}
		return allElements;
	}

	/**
	 * Returns the {@link Element} with the specified <code>id</code> attribute value.
	 * <p>
	 * This simulates the script method
	 * <code><a target="_blank" href="http://www.w3.org/TR/1998/REC-DOM-Level-1-19981001/level-one-html.html#ID-36113835">getElementById</a></code>
	 * defined in DOM HTML level 1.
	 * <p>
	 * This is equivalent to {@link #findNextStartTag(int,String,String,boolean) findNextStartTag}<code>(0,"id",id,true).</code>{@link StartTag#getElement() getElement()}, assuming that the element exists.
	 * <p>
	 * A well formed HTML document should have no more than one element with any given <code>id</code> attribute value.
	 *
	 * @param id  the <code>id</code> attribute value (case sensitive) to search for, must not be <code>null</code>.
	 * @return the {@link Element} with the specified <code>id</code> attribute value, or <code>null</code> if no such element exists.
	 */
	public Element getElementById(final String id) {
		final StartTag startTag=findNextStartTag(0,Attribute.ID,id,true);
		return startTag==null ? null : startTag.getElement();
	}

	/**
	 * Returns the {@link Tag} at the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 * <p>
	 * This method also returns {@linkplain Tag#isUnregistered() unregistered} tags.
	 *
	 * @param pos  the position in the source document, may be out of bounds.
	 * @return the {@link Tag} at the specified position in the source document, or <code>null</code> if no tag exists at the specified position or it is out of bounds.
	 */
	public final Tag getTagAt(final int pos) {
		return Tag.getTagAt(this,pos);
	}

	/**
	 * Returns the {@link Tag} beginning at or immediately preceding (or {@linkplain Segment#encloses(int) enclosing}) the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @return the {@link Tag} beginning at or immediately preceding the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public Tag findPreviousTag(final int pos) {
		return Tag.findPreviousOrNextTag(this,pos,true);
	}

	/**
	 * Returns the {@link Tag} of the specified {@linkplain TagType type} beginning at or immediately preceding (or {@linkplain Segment#encloses(int) enclosing}) the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @param tagType  the <code>TagType</code> to search for.
	 * @return the {@link Tag} with the specified {@linkplain TagType type} beginning at or immediately preceding the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public Tag findPreviousTag(final int pos, final TagType tagType) {
		return Tag.findPreviousOrNextTag(this,pos,tagType,true);
	}
	
	/**
	 * Returns the {@link Tag} beginning at or immediately following the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @return the {@link Tag} beginning at or immediately following the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public Tag findNextTag(final int pos) {
		return Tag.findPreviousOrNextTag(this,pos,false);
	}

	/**
	 * Returns the {@link Tag} of the specified {@linkplain TagType type} beginning at or immediately following the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @param tagType  the <code>TagType</code> to search for.
	 * @return the {@link Tag} with the specified {@linkplain TagType type} beginning at or immediately following the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public Tag findNextTag(final int pos, final TagType tagType) {
		return Tag.findPreviousOrNextTag(this,pos,tagType,false);
	}

	/**
	 * Returns the {@link Tag} that {@linkplain Segment#encloses(int) encloses} the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document, may be out of bounds.
	 * @return the {@link Tag} that {@linkplain Segment#encloses(int) encloses} the specified position in the source document, or <code>null</code> if the position is not within a tag or is out of bounds.
	 */
	public Tag findEnclosingTag(final int pos) {
		return findEnclosingTag(pos,null);
	}

	/**
	 * Returns the {@link Tag} of the specified {@linkplain TagType type} that {@linkplain Segment#encloses(int) encloses} the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document, may be out of bounds.
	 * @param tagType  the <code>TagType</code> to search for.
	 * @return the {@link Tag} of the specified {@linkplain TagType type} that {@linkplain Segment#encloses(int) encloses} the specified position in the source document, or <code>null</code> if the position is not within a tag of the specified type or is out of bounds.
	 */
	public Tag findEnclosingTag(final int pos, final TagType tagType) {
		final Tag tag=findPreviousTag(pos,tagType);
		if (tag==null || tag.end<=pos) return null;
		return tag;
	}

	/**
	 * Returns the {@link Element} beginning at or immediately following the specified position in the source document.
	 * <p>
	 * This is equivalent to {@link #findNextStartTag(int) findNextStartTag(pos)}<code>.</code>{@link StartTag#getElement() getElement()},
	 * assuming the result is not <code>null</code>.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @return the {@link Element} beginning at or immediately following the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public Element findNextElement(final int pos) {
		final StartTag startTag=findNextStartTag(pos);
		return startTag==null ? null : startTag.getElement();
	}

	/**
	 * Returns the {@link Element} with the specified {@linkplain Element#getName() name} beginning at or immediately following the specified position in the source document.
	 * <p>
	 * This is equivalent to {@link #findNextStartTag(int,String) findNextStartTag(pos,name)}<code>.</code>{@link StartTag#getElement() getElement()},
	 * assuming the result is not <code>null</code>.
	 * <p>
	 * Specifying a <code>null</code> argument to the <code>name</code> parameter is equivalent to 
	 * {@link #findNextStartTag(int) findNextElement(pos)}.
	 * <p>
	 * Specifying an argument to the <code>name</code> parameter that ends in a colon (<code>:</code>) searches for all elements 
	 * in the specified XML namespace.
	 * <p>
	 * This method also returns elements consisting of {@linkplain Tag#isUnregistered() unregistered} tags if the specified name is not a valid {@linkplain Tag#isXMLName(CharSequence) XML tag name}.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @param name  the {@linkplain Element#getName() name} of the element to search for.
	 * @return the {@link Element} with the specified {@linkplain Element#getName() name} beginning at or immediately following the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public Element findNextElement(final int pos, String name) {
		final StartTag startTag=findNextStartTag(pos,name);
		return startTag==null ? null : startTag.getElement();
	}

	/**
	 * Returns the {@link StartTag} at or immediately preceding (or {@linkplain Segment#encloses(int) enclosing}) the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @return the {@link StartTag} at or immediately preceding the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public StartTag findPreviousStartTag(final int pos) {
		return StartTag.findPreviousOrNext(this,pos,true);
	}

	/**
	 * Returns the {@link StartTag} with the specified {@linkplain StartTag#getName() name} at or immediately preceding (or {@linkplain Segment#encloses(int) enclosing}) the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 * <p>
	 * Specifying a <code>null</code> argument to the <code>name</code> parameter is equivalent to
	 * {@link #findPreviousStartTag(int) findPreviousStartTag(pos)}.
	 * <p>
	 * This method also returns {@linkplain Tag#isUnregistered() unregistered} tags if the specified name is not a valid {@linkplain Tag#isXMLName(CharSequence) XML tag name}.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @param name  the {@linkplain StartTag#getName() name} of the start tag to search for.
	 * @return the {@link StartTag} with the specified {@linkplain StartTag#getName() name} at or immediately preceding the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public StartTag findPreviousStartTag(final int pos, String name) {
		if (name!=null) name=name.toLowerCase();
		final boolean isXMLTagName=Tag.isXMLName(name);
		return StartTag.findPreviousOrNext(this,pos,name,isXMLTagName,true);
	}

	/**
	 * Returns the {@link StartTag} beginning at or immediately following the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @return the {@link StartTag} beginning at or immediately following the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public StartTag findNextStartTag(final int pos) {
		return StartTag.findPreviousOrNext(this,pos,false);
	}

	/**
	 * Returns the {@link StartTag} with the specified {@linkplain StartTag#getName() name} beginning at or immediately following the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 * <p>
	 * Specifying a <code>null</code> argument to the <code>name</code> parameter is equivalent to 
	 * {@link #findNextStartTag(int) findNextStartTag(pos)}.
	 * <p>
	 * Specifying an argument to the <code>name</code> parameter that ends in a colon (<code>:</code>) searches for all start tags 
	 * in the specified XML namespace.
	 * <p>
	 * This method also returns {@linkplain Tag#isUnregistered() unregistered} tags if the specified name is not a valid {@linkplain Tag#isXMLName(CharSequence) XML tag name}.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @param name  the {@linkplain StartTag#getName() name} of the start tag to search for.
	 * @return the {@link StartTag} with the specified {@linkplain StartTag#getName() name} beginning at or immediately following the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public StartTag findNextStartTag(final int pos, String name) {
		if (name!=null) name=name.toLowerCase();
		final boolean isXMLTagName=Tag.isXMLName(name);
		return StartTag.findPreviousOrNext(this,pos,name,isXMLTagName,false);
	}

	/**
	 * Returns the {@link StartTag} with the specified attribute name/value pair beginning at or immediately following the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @param attributeName  the attribute name (case insensitive) to search for, must not be <code>null</code>.
	 * @param value  the value of the specified attribute to search for, must not be <code>null</code>.
	 * @param valueCaseSensitive  specifies whether the attribute value matching is case sensitive.
	 * @return the {@link StartTag} with the specified attribute name/value pair beginning at or immediately following the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public StartTag findNextStartTag(final int pos, final String attributeName, final String value, final boolean valueCaseSensitive) {
		return StartTag.findNext(this,pos,attributeName,value,valueCaseSensitive);
	}

	/**
	 * Returns the {@link EndTag} beginning at or immediately preceding the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @return the {@link EndTag} beginning at or immediately preceding the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public EndTag findPreviousEndTag(final int pos) {
		return EndTag.findPreviousOrNext(this,pos,true);
	}

	/**
	 * Returns the {@linkplain EndTagType#NORMAL normal} {@link EndTag} with the specified {@linkplain EndTag#getName() name} at or immediately preceding (or {@linkplain Segment#encloses(int) enclosing}) the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @param name  the {@linkplain StartTag#getName() name} of the end tag to search for, must not be <code>null</code>.
	 * @return the {@linkplain EndTagType#NORMAL normal} {@link EndTag} with the specified {@linkplain EndTag#getName() name} at or immediately preceding (or {@linkplain Segment#encloses(int) enclosing}) the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public EndTag findPreviousEndTag(final int pos, final String name) {
		if (name==null) throw new IllegalArgumentException("name argument must not be null");
		return EndTag.findPreviousOrNext(this,pos,name.toLowerCase(),EndTagType.NORMAL,true);
	}

	/**
	 * Returns the {@link EndTag} beginning at or immediately following the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @return the {@link EndTag} beginning at or immediately following the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public EndTag findNextEndTag(final int pos) {
		return EndTag.findPreviousOrNext(this,pos,false);
	}

	/**
	 * Returns the {@linkplain EndTagType#NORMAL normal} {@link EndTag} with the specified {@linkplain EndTag#getName() name} beginning at or immediately following the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @param name  the {@linkplain StartTag#getName() name} of the end tag to search for, must not be <code>null</code>.
	 * @return the {@linkplain EndTagType#NORMAL normal} {@link EndTag} with the specified {@linkplain EndTag#getName() name} beginning at or immediately following the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public EndTag findNextEndTag(final int pos, final String name) {
		return findNextEndTag(pos,name,EndTagType.NORMAL);
	}

	/**
	 * Returns the {@link EndTag} with the specified {@linkplain EndTag#getName() name} and {@linkplain EndTagType type} beginning at or immediately following the specified position in the source document.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @param name  the {@linkplain StartTag#getName() name} of the end tag to search for, must not be <code>null</code>.
	 * @param endTagType  the {@linkplain EndTagType type} of the end tag to search for, must not be <code>null</code>.
	 * @return the {@link EndTag} with the specified {@linkplain EndTag#getName() name} and {@linkplain EndTagType type} beginning at or immediately following the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public EndTag findNextEndTag(final int pos, final String name, final EndTagType endTagType) {
		if (name==null) throw new IllegalArgumentException("name argument must not be null");
		return EndTag.findPreviousOrNext(this,pos,name.toLowerCase(),endTagType,false);
	}

	/**
	 * Returns the most nested {@link Element} that {@linkplain Segment#encloses(int) encloses} the specified position in the source document.
	 * <p>
	 * The specified position can be anywhere inside the {@linkplain Element#getStartTag() start tag}, {@linkplain Element#getEndTag() end tag},
	 * or {@linkplain Element#getContent() content} of the element.  There is no requirement that the returned element has an end tag, and it
	 * may be a {@linkplain TagType#isServerTag() server tag} or HTML {@linkplain StartTagType#COMMENT comment}.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 *
	 * @param pos  the position in the source document, may be out of bounds.
	 * @return the most nested {@link Element} that {@linkplain Segment#encloses(int) encloses} the specified position in the source document, or <code>null</code> if the position is not within an element or is out of bounds.
	 */
	public Element findEnclosingElement(final int pos) {
		return findEnclosingElement(pos,null);
	}

	/**
	 * Returns the most nested {@link Element} with the specified {@linkplain Element#getName() name} that {@linkplain Segment#encloses(int) encloses} the specified position in the source document.
	 * <p>
	 * The specified position can be anywhere inside the {@linkplain Element#getStartTag() start tag}, {@linkplain Element#getEndTag() end tag},
	 * or {@linkplain Element#getContent() content} of the element.  There is no requirement that the returned element has an end tag, and it
	 * may be a {@linkplain TagType#isServerTag() server tag} or HTML {@linkplain StartTagType#COMMENT comment}.
	 * <p>
	 * See the {@link Tag} class documentation for more details about the behaviour of this method.
	 * <p>
	 * This method also returns elements consisting of {@linkplain Tag#isUnregistered() unregistered} tags if the specified name is not a valid {@linkplain Tag#isXMLName(CharSequence) XML tag name}.
	 *
	 * @param pos  the position in the source document, may be out of bounds.
	 * @param name  the {@linkplain Element#getName() name} of the element to search for.
	 * @return the most nested {@link Element} with the specified {@linkplain Element#getName() name} that {@linkplain Segment#encloses(int) encloses} the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public Element findEnclosingElement(final int pos, String name) {
		int startBefore=pos;
		if (name!=null) name=name.toLowerCase();
		final boolean isXMLTagName=Tag.isXMLName(name);
		while (true) {
			StartTag startTag=StartTag.findPreviousOrNext(this,startBefore,name,isXMLTagName,true);
			if (startTag==null) return null;
			Element element=startTag.getElement();
			if (pos < element.end) return element;
			startBefore=startTag.begin-1;
		}
	}

	/**
	 * Returns the {@link CharacterReference} at or immediately preceding (or {@linkplain Segment#encloses(int) enclosing}) the specified position in the source document.
	 * <p>
	 * Character references positioned within an HTML {@linkplain StartTagType#COMMENT comment} are <b>NOT</b> ignored.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @return the {@link CharacterReference} beginning at or immediately preceding the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public CharacterReference findPreviousCharacterReference(final int pos) {
		return CharacterReference.findPreviousOrNext(this,pos,true);
	}

	/**
	 * Returns the {@link CharacterReference} beginning at or immediately following the specified position in the source document.
	 * <p>
	 * Character references positioned within an HTML {@linkplain StartTagType#COMMENT comment} are <b>NOT</b> ignored.
	 *
	 * @param pos  the position in the source document from which to start the search, may be out of bounds.
	 * @return the {@link CharacterReference} beginning at or immediately following the specified position in the source document, or <code>null</code> if none exists or the specified position is out of bounds.
	 */
	public CharacterReference findNextCharacterReference(final int pos) {
		return CharacterReference.findPreviousOrNext(this,pos,false);
	}

	/**
	 * Returns the end position of the <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Name">XML Name</a> that starts at the
	 * specified position.
	 * <p>
	 * This implementation first checks that the character at the specified position is a valid XML Name start character as defined by the
	 * {@link Tag#isXMLNameStartChar(char)} method.  If this is not the case, the value <code>-1</code> is returned.
	 * <p>
	 * Once the first character has been checked, subsequent characters are checked using the {@link Tag#isXMLNameChar(char)} method until
	 * one is found that is not a valid XML Name character or the end of the document is reached.  This position is then returned.
	 *
	 * @param pos  the position in the source document of the first character of the XML Name.
	 * @return the end position of the <a target="_blank" href="http://www.w3.org/TR/REC-xml/#NT-Name">XML Name</a> that starts at the specified position.
	 * @throws IndexOutOfBoundsException if the specified position is not within the bounds of the document.
	 */
	public int findNameEnd(int pos) {
		if (!Tag.isXMLNameStartChar(string.charAt(pos++))) return -1;
		while (pos<string.length() && Tag.isXMLNameChar(string.charAt(pos))) pos++;
		return pos;
	}

	/**
	 * Parses any {@link Attributes} starting at the specified position.
	 * This method is only used in the unusual situation where attributes exist outside of a start tag.
	 * The {@link StartTag#getAttributes()} method should be used in normal situations.
	 * <p>
	 * The returned Attributes segment always begins at <code>pos</code>,
	 * and ends at the end of the last attribute before either <code>maxEnd</code> or 
	 * the first occurrence of "/&gt;" or "&gt;" outside of a quoted attribute value, whichever comes first.
	 * <p>
	 * Only returns <code>null</code> if the segment contains a major syntactical error
	 * or more than the {@linkplain Attributes#getDefaultMaxErrorCount() default maximum} number of
	 * minor syntactical errors.
	 * <p>
	 * This is equivalent to
	 * {@link #parseAttributes(int,int,int) parseAttributes}<code>(pos,maxEnd,</code>{@link Attributes#getDefaultMaxErrorCount()}<code>)}</code>.
	 *
	 * @param pos  the position in the source document at the beginning of the attribute list, may be out of bounds.
	 * @param maxEnd  the maximum end position of the attribute list, or -1 if no maximum.
	 * @return the {@link Attributes} starting at the specified position, or <code>null</code> if too many errors occur while parsing or the specified position is out of bounds.
	 * @see StartTag#getAttributes()
	 * @see Segment#parseAttributes()
	 */
	public Attributes parseAttributes(final int pos, final int maxEnd) {
		return parseAttributes(pos,maxEnd,Attributes.getDefaultMaxErrorCount());
	}

	/**
	 * Parses any {@link Attributes} starting at the specified position.
	 * This method is only used in the unusual situation where attributes exist outside of a start tag.
	 * The {@link StartTag#getAttributes()} method should be used in normal situations.
	 * <p>
	 * Only returns <code>null</code> if the segment contains a major syntactical error
	 * or more than the specified number of minor syntactical errors.
	 * <p>
	 * The <code>maxErrorCount</code> argument overrides the {@linkplain Attributes#getDefaultMaxErrorCount() default maximum error count}.
	 * <p>
	 * See {@link #parseAttributes(int pos, int maxEnd)} for more information.
	 *
	 * @param pos  the position in the source document at the beginning of the attribute list, may be out of bounds.
	 * @param maxEnd  the maximum end position of the attribute list, or -1 if no maximum.
	 * @param maxErrorCount  the maximum number of minor errors allowed while parsing.
	 * @return the {@link Attributes} starting at the specified position, or <code>null</code> if too many errors occur while parsing or the specified position is out of bounds.
	 * @see StartTag#getAttributes()
	 * @see #parseAttributes(int pos, int MaxEnd)
	 */
	public Attributes parseAttributes(final int pos, final int maxEnd, final int maxErrorCount) {
		return Attributes.construct(this,pos,maxEnd,maxErrorCount);
	}

	/**
	 * Causes the specified range of the source text to be ignored when parsing.
	 * <p>
	 * See the documentation of the {@link Segment#ignoreWhenParsing()} method for more information.
	 *
	 * @param begin  the beginning character position in the source text.
	 * @param end  the end character position in the source text.
	 */
	public void ignoreWhenParsing(final int begin, final int end) {
		if (parseTextOutputDocument==null) {
			parseTextOutputDocument=new OutputDocument(getParseText());
			parseText=null;
		}
		parseTextOutputDocument.replaceWithSpaces(begin,end);
	}

	/**
	 * Causes all of the segments in the specified collection to be ignored when parsing.
	 * <p>
	 * This is equivalent to calling {@link Segment#ignoreWhenParsing()} on each segment in the collection.
	 */
	public void ignoreWhenParsing(final Collection<?> segments) {
		for (final Iterator<?> i=segments.iterator(); i.hasNext();) {
			((Segment)i.next()).ignoreWhenParsing();
		}
	}

	/**
	 * Reproduces the source text with indenting that represents the <a href="#DocumentElementHierarchy">document element hierarchy</a> of this source document.
	 * Any indenting present in the original source text is removed.
	 * <p>
	 * The output text is functionally equivalent to the original source and should be rendered identically unless specified below.
	 * <p>
	 * The following points describe the process in general terms.
	 * Any aspect of the algorithm not specifically mentioned here is subject to change without notice in future versions.
	 * <p>
	 * <ul>
	 *  <li>Every element that is not an {@linkplain HTMLElements#getInlineLevelElementNames() inline-level element} appears on a new line
	 *   with an indent corresponding to its {@linkplain Element#getDepth() depth} in the <a href="#DocumentElementHierarchy">document element hierarchy</a>.
	 *  <li>The indent is formed by writing <i>n</i> repetitions of the string specified in the <code>indentText</code> argument,
	 *   where <i>n</i> is the depth of the indent.
	 *  <li>The {@linkplain Element#getContent() content} of an indented element starts on a new line and is indented at a depth one greater than that of the element,
	 *   with the end tag appearing on a new line at the same depth as the start tag.
	 *   If the content contains only text and {@linkplain HTMLElements#getInlineLevelElementNames() inline-level elements},
	 *   it may continue on the same line as the start tag.  Additionally, if the output content contains no new lines, the end tag may also continue on the same line.
	 *  <li>The content of preformatted elements such as {@link HTMLElementName#PRE PRE} and {@link HTMLElementName#TEXTAREA TEXTAREA} are not indented,
	 *   nor is the white space modified in any way.
	 *  <li>Only {@linkplain StartTagType#NORMAL normal} and {@linkplain StartTagType#DOCTYPE_DECLARATION document type declaration} elements are indented.
	 *   All others are treated as {@linkplain HTMLElements#getInlineLevelElementNames() inline-level elements}.
	 *  <li>White space and indenting inside HTML {@linkplain StartTagType#COMMENT comments}, {@linkplain StartTagType#CDATA_SECTION CDATA sections}, or any
	 *   {@linkplain TagType#isServerTag() server tag} is preserved, 
	 *   but with the indenting of new lines starting at a depth one greater than that of the surrounding text.
	 *  <li>White space and indenting inside {@link HTMLElementName#SCRIPT SCRIPT} elements is preserved, 
	 *   but with the indenting of new lines starting at a depth one greater than that of the <code>SCRIPT</code> element.
	 *  <li>If the <code>tidyTags</code> option is used, every tag in the document is replaced with the output from its {@link Tag#tidy()} method.
	 *   If this argument is set to <code>false</code>, the tag from the original text is used, including all white space,
	 *   but with any new lines indented at a depth one greater than that of the element.
	 *  <li>If the <code>collapseWhiteSpace</code> option is used, every string of one or more {@linkplain Segment#isWhiteSpace(char) white space} characters
	 *   located outside of a tag is replaced with a single space in the output.
	 *   White space located adjacent to a non-inline-level element tag (except {@linkplain TagType#isServerTag() server tags}) may be removed.
	 *  <li>If the <code>indentAllElements</code> option is used, every element appears indented on a new line, including
	 *   {@linkplain Element#isInline(String) inline-level elements}.
	 *   This generates output that is a good representation of the actual <a href="#DocumentElementHierarchy">document element hierarchy</a>,
	 *   but is very likely to introduce white space that affects the functional equivalency of the document.
	 *  <li>If the source document contains {@linkplain TagType#isServerTag() server tags}, the functional equivalency of the output document may be compromised.
	 * </ul>
	 * <p>
	 * Use one of the following methods to obtain the output from the returned {@link CharStreamSource} object:<br />
	 * {@link CharStreamSource#writeTo(Writer)}<br />
	 * {@link CharStreamSourceUtil#toString(CharStreamSource)}<br />
	 * {@link CharStreamSourceUtil#getReader(CharStreamSource)}
	 *
	 * @param indentText  the string to use for each indent, must not be <code>null</code>.
	 * @param tidyTags  specifies whether to replace the original text of each tag with the output from its {@link Tag#tidy()} method.
	 * @param collapseWhiteSpace  specifies whether to collapse the white space in the text between the tags.
	 * @param indentAllElements  specifies whether to indent all elements, including {@linkplain Element#isInline(String) inline-level elements} and those with preformatted contents.
	 * @return a {@link CharStreamSource} from which an indented copy of this source document can be obtained.
	 */
	public CharStreamSource indent(final String indentText, final boolean tidyTags, final boolean collapseWhiteSpace, final boolean indentAllElements) {
		return new Indent(this,indentText,tidyTags,collapseWhiteSpace,indentAllElements);
	}

	/**
	 * Returns the destination <code>Writer</code> for log messages.
	 * <p>
	 * By default, the log writer is set to <code>null</code>, which supresses log messages.
	 *
	 * @return the destination <code>Writer</code> for log messages.
	 */
	public Writer getLogWriter() {
		return logWriter;
	}

	/**
	 * Sets the destination <code>Writer</code> for log messages.
	 * <p>
	 * When required, this method should normally be called immediately after the construction of the <code>Source</code> object.
	 *
	 * @param writer  the destination <code>java.io.Writer</code> for log messages.
	 * @see #getLogWriter()
	 */
	public void setLogWriter(final Writer writer) {
		logWriter=writer;
	}

	/**
	 * Indicates whether logging is currently enabled.
	 * <p>
	 * The current implementation of this method is equivalent to {@link #getLogWriter()}<code>!=null</code>.
	 * <p>
	 * For best performance you should check that this method returns <code>true</code> before constructing the string to send to
	 * the {@link #log(String message)} method.
	 *
	 * @return <code>true</code> if logging is currently enabled, otherwise <code>false</code>.
	 */
	public boolean isLoggingEnabled() {
		return logWriter!=null;
	}

	/**
	 * Writes the specified message to the log.
	 * <p>
	 * The log destination is set via the {@link #setLogWriter(Writer)} method.
	 * By default, log messages are not sent anywhere.
	 * <p>
	 * A newline character is added to the message and the <code>Writer</code> is flushed after every call to this method.
	 * <p>
	 * If an <code>IOException</code> is thrown while writing to the log, this method throws a <code>RuntimeException</code> with
	 * the original <code>IOException</code> as its cause.
	 *
	 * @param message  the message to log
	 */
	public void log(final String message) {
		if (logWriter==null) return;
		try {
			logWriter.write(message);
			logWriter.write('\n');
			logWriter.flush();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Clears the {@linkplain #getCacheDebugInfo() tag cache} of all tags.
	 * <p>
	 * This method may be useful after calling the {@link Segment#ignoreWhenParsing()} method so that any tags previously found within the ignored segments
	 * will no longer be returned by the <a href="Tag.html#TagSearchMethods">tag search methods</a>.
	 */
	public void clearCache() {
		cache.clear();
		allTagsArray=null;
		allTags=null;
		allStartTags=null;
		allElements=null;
	}

	/**
	 * Returns a string representation of the tag cache, useful for debugging purposes.
	 * @return a string representation of the tag cache, useful for debugging purposes.
	 */
	public String getCacheDebugInfo() {
		return cache.toString();
	}

	/**
	 * Gets a list of all the tags that have been parsed so far.
	 * <p>
	 * This information may be useful for debugging purposes.
	 * Execution of this method collects information from the internal cache and is relatively expensive.
	 *
	 * @return a list of all the tags that have been parsed so far.
	 * @see #getCacheDebugInfo()
	 */
	List<Tag> getParsedTags() {
		final List<Tag> list=new ArrayList<Tag>();
		for (final Iterator<Tag> i=cache.getTagIterator(); i.hasNext();) list.add(i.next());
		return list;
	}

	/**
	 * Returns the {@linkplain ParseText parse text} of this source document.
	 * <p>
	 * This method is normally only of interest to users who wish to create <a href="TagType.html#Custom">custom tag types</a>.
	 * <p>
	 * The parse text is defined as the entire text of the source document in lower case, with all
	 * {@linkplain Segment#ignoreWhenParsing() ignored} segments replaced by space characters.
	 *
	 * @return the {@linkplain ParseText parse text} of this source document.
	 */
	public final ParseText getParseText() {
		if (parseText==null) {
			if (parseTextOutputDocument!=null) {
				parseText=new ParseText(parseTextOutputDocument);
				parseTextOutputDocument=null;
			} else {
				parseText=new ParseText(this);
			}
		}
		return parseText;
	}

	/**
	 * Returns the {@link StartTag} that {@linkplain Segment#encloses(int) encloses} the specified position in the source document.
	 * <p>
	 * This method has been deprecated as of version 2.0 in favour of the more generic {@link #findEnclosingTag(int pos)} method.
	 * <p>
	 * Caveat - The returned tag from {@link #findEnclosingTag(int pos)} may be an instance of {@link EndTag}.
	 * In most cases this should be interpreted in the same way as if this method returned a <code>null</code>,
	 * since an end tag normally does not exist inside of a start tag.
	 * There is however one situation where this may occur legitimately, where a {@linkplain TagType#isServerTag() server-side} end tag
	 * appears within a normal start tag.
	 * It is up to the developer to decide whether this situation requires special handling when updating code that uses this
	 * deprecated method.
	 *
	 * @param pos  the position in the source document.
	 * @return the {@link StartTag} that {@linkplain Segment#encloses(int) encloses} the specified position in the source document, or <code>null</code> if the position is not within a start tag.
	 * @deprecated  Use {@link #findEnclosingTag(int pos)} instead. (see caveat)
	 */
	public StartTag findEnclosingStartTag(final int pos) {
		final StartTag startTag=findPreviousStartTag(pos);
		if (startTag==null || startTag.end<=pos) return null;
		return startTag;
	}

	/**
	 * Returns the {@link StartTag} object representing the HTML {@linkplain StartTagType#COMMENT comment} beginning at or immediately following the specified position in the source document.
	 * <p>
	 * This method has been deprecated as of version 2.0 in favour of the more generic {@link #findNextTag(int pos, TagType)} method.
	 *
	 * @param pos  the position in the source document from which to start the search.
	 * @return the {@link StartTag} object representing the HTML {@linkplain StartTagType#COMMENT comment} beginning at or immediately following the specified position in the source document, or <code>null</code> if none exists.
	 * @deprecated  Use {@link #findNextTag(int,TagType) findNextTag}<code>(pos,</code>{@link StartTagType#COMMENT}<code>)</code> instead.
	 */
	public StartTag findNextComment(final int pos) {
		return (StartTag)findNextTag(pos,StartTagType.COMMENT);
	}

	/**
	 * Returns the <code>Segment</code> object representing the HTML {@linkplain StartTagType#COMMENT comment} that {@linkplain Segment#encloses(int) encloses} the specified position in the source document.
	 * <p>
	 * This method has been deprecated as of version 2.0 in favour of the more generic {@link #findEnclosingTag(int pos, TagType)} method.
	 *
	 * @param pos  the position in the source document.
	 * @return the <code>Segment</code> object representing the HTML {@linkplain StartTagType#COMMENT comment} that {@linkplain Segment#encloses(int) encloses} the specified position in the source document, or <code>null</code> if the position is not within a comment.
	 * @deprecated  Use {@link #findEnclosingTag(int,TagType) findEnclosingTag}<code>(pos,</code>{@link StartTagType#COMMENT}<code>)</code> instead.
	 */
	public Segment findEnclosingComment(final int pos) {
		return findEnclosingTag(pos,StartTagType.COMMENT);
	}

	/**
	 * Returns an iterator of {@link Tag} objects beginning at and following the specified position in the source document.
	 * <p>
	 * This method has been deprecated as of version 2.2 as it was originally only included because it was more efficient than
	 * consecutive calls to {@link #findNextTag(int pos)}.
	 * The most efficient replacement is to use multiple calls to {@link Tag#findNextTag()} if a {@linkplain #fullSequentialParse() full sequential parse} was peformed, 
	 * otherwise use {@link #findAllTags()}<code>.iterator()</code> and skip over the tags that begin before
	 * the position specified in the <code>pos</code> argument of this method.
	 * 
	 * @param pos  the position in the source document from which to start the iteration.
	 * @return an iterator of {@link Tag} objects beginning at and following the specified position in the source document.
	 * @deprecated  Use {@link #findAllTags()}<code>.iterator()</code> instead, or multiple calls to the {@link Tag#findNextTag()} method.
	 */
	public Iterator<?> getNextTagIterator(final int pos) {
		return Tag.getNextTagIterator(this,pos);
	}

	static String getCharsetParameterFromHttpHeaderValue(final String httpHeaderValue) {
		final int charsetParameterPos=httpHeaderValue.toLowerCase().indexOf("charset=");
		if (charsetParameterPos==-1) return null;
		final int charsetBegin=charsetParameterPos+8;
		int charsetEnd=httpHeaderValue.indexOf(';',charsetBegin);
		final String charset=(charsetEnd==-1) ? httpHeaderValue.substring(charsetBegin) : httpHeaderValue.substring(charsetBegin,charsetEnd);
		return charset.trim();
	}
}
