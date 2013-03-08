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

import java.util.Map;

/**
 * Represents the <a target="_blank" href="http://www.w3.org/TR/html401/intro/sgmltut.html#didx-element-3">end tag</a> of an
 * {@linkplain Element element} in a specific {@linkplain Source source} document.
 * <p>
 * An end tag always has a {@linkplain #getTagType() type} that is a subclass of {@link EndTagType}, meaning it
 * always starts with the characters '<code>&lt;/</code>'.
 * <p>
 * <code>EndTag</code> instances are obtained using one of the following methods:
 * <ul>
 *  <li>{@link Element#getEndTag()}
 *  <li>{@link Tag#findNextTag()}
 *  <li>{@link Tag#findPreviousTag()}
 *  <li>{@link Source#findPreviousEndTag(int pos)}
 *  <li>{@link Source#findPreviousEndTag(int pos, String name)}
 *  <li>{@link Source#findPreviousTag(int pos)}
 *  <li>{@link Source#findPreviousTag(int pos, TagType)}
 *  <li>{@link Source#findNextEndTag(int pos)}
 *  <li>{@link Source#findNextEndTag(int pos, String name)}
 *  <li>{@link Source#findNextEndTag(int pos, String name, EndTagType)}
 *  <li>{@link Source#findNextTag(int pos)}
 *  <li>{@link Source#findNextTag(int pos, TagType)}
 *  <li>{@link Source#findEnclosingTag(int pos)}
 *  <li>{@link Source#findEnclosingTag(int pos, TagType)}
 *  <li>{@link Source#getTagAt(int pos)}
 *  <li>{@link Segment#findAllTags()}
 *  <li>{@link Segment#findAllTags(TagType)}
 * </ul>
 * <p>
 * The {@link Tag} superclass defines the {@link Tag#getName() getName()} method used to get the name of this end tag.
 * <p>
 * See also the XML 1.0 specification for <a target="_blank" href="http://www.w3.org/TR/REC-xml#dt-etag">end tags</a>.
 *
 * @see Tag
 * @see StartTag
 * @see Element
 */
public final class EndTag extends Tag {
	private final EndTagType endTagType;

	/**
	 * Constructs a new <code>EndTag</code>.
	 *
	 * @param source  the {@link Source} document.
	 * @param begin  the character position in the source document where this tag {@linkplain Segment#getBegin() begins}.
	 * @param end  the character position in the source document where this tag {@linkplain Segment#getEnd() ends}.
	 * @param endTagType  the {@linkplain #getEndTagType() type} of the end tag.
	 * @param name  the {@linkplain Tag#getName() name} of the tag.
	 */
	EndTag(final Source source, final int begin, final int end, final EndTagType endTagType, final String name) {
		super(source,begin,end,name);
		this.endTagType=endTagType;
	}

	/**
	 * Returns the {@linkplain Element element} that is ended by this end tag.
	 * <p>
	 * Returns <code>null</code> if this end tag is not properly matched to any {@linkplain StartTag start tag} in the source document.
	 * <p>
	 * This method is much less efficient than the {@link StartTag#getElement()} method.
	 * <p>
	 * IMPLEMENTATION NOTE: The explanation for why this method is relatively inefficient lies in the fact that more than one
	 * {@linkplain StartTagType start tag type} can have the same 
	 * {@linkplain StartTagType#getCorrespondingEndTagType() corresponding end tag type}, so it is not possible to know for certain
	 * which type of start tag this end tag is matched to (see {@link EndTagType#getCorrespondingStartTagType()} for more explanation).
	 * Because of this uncertainty, the implementation of this method must check every start tag preceding this end tag, calling its
	 * {@link StartTag#getElement()} method to see whether it is terminated by this end tag.
	 *
	 * @return the {@linkplain Element element} that is ended by this end tag.
	 */
	public Element getElement() {
		if (element!=Element.NOT_CACHED) return element;
		int pos=begin;
		while (pos!=0) {
			StartTag startTag=source.findPreviousStartTag(pos-1);
			if (startTag==null) break;
			Element foundElement=startTag.getElement(); // this automatically sets foundElement.getEndTag().element cache
			if (foundElement.getEndTag()==this) return foundElement; // no need to set element as it was already done in previous statement
			pos=startTag.begin;
		}
		return element=null;
	}

	/**
	 * Returns the {@linkplain EndTagType type} of this end tag.	
	 * <p>
	 * This is equivalent to <code>(EndTagType)</code>{@link #getTagType()}.
	 *
	 * @return the {@linkplain EndTagType type} of this end tag.	
	 */
	public EndTagType getEndTagType() {
		return endTagType;
	}

	// Documentation inherited from Tag
	public TagType getTagType() {
		return endTagType;
	}

	// Documentation inherited from Tag
	public boolean isUnregistered() {
		return endTagType==EndTagType.UNREGISTERED;
	}

	/**
	 * Returns an XML representation of this end tag.
	 * <p>
	 * This method is included for symmetry with the {@link StartTag#tidy()} method and simply
	 * returns the {@linkplain Segment#toString() source text} of the tag.
	 *
	 * @return an XML representation of this end tag.
	 */
	public String tidy() {
		return toString();
	}

	/**
	 * Generates the HTML text of a {@linkplain EndTagType#NORMAL normal} end tag with the specified tag {@linkplain #getName() name}.
	 * <p>
	 * <h4>Example:</h4>
	 * The following method call:
	 * <p>
	 * <code>EndTag.generateHTML("INPUT")</code>
	 * <p>
	 * returns the following output:
	 * <p>
	 * <code>&lt;/INPUT&gt;</code>
	 *
	 * @param tagName  the {@linkplain #getName() name} of the end tag.
	 * @return the HTML text of a {@linkplain EndTagType#NORMAL normal} end tag with the specified tag {@linkplain #getName() name}.
	 * @see StartTag#generateHTML(String tagName, Map attributesMap, boolean emptyElementTag)
	 */
	public static String generateHTML(final String tagName) {
		return EndTagType.NORMAL.generateHTML(tagName);
	}

	public String getDebugInfo() {
		final StringBuffer sb=new StringBuffer();
		sb.append("\"/").append(name).append("\" ");
		if (endTagType!=EndTagType.NORMAL) sb.append('(').append(endTagType.getDescription()).append(") ");
		sb.append(super.getDebugInfo());
		return sb.toString();
	}

	/**
	 * Regenerates the HTML text of this end tag.
	 * <p>
	 * This method has been deprecated as of version 2.2 and replaced with the exactly equivalent {@link #tidy()} method.
	 *
	 * @return the regenerated HTML text of this end tag.
	 * @deprecated  Use {@link #tidy()} instead.
	 */
	public String regenerateHTML() {
		return toString();
	}

	/**
	 * Indicates whether the end tag of an <a href="HTMLElements.html#HTMLElement">HTML element</a> with the specified name is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}.
	 * <p>
	 * This method has been deprecated as of version 2.0 and replaced with the {@link HTMLElements#getEndTagForbiddenElementNames()} method.
	 *
	 * @return <code>true</code> if the end tag of an <a href="HTMLElements.html#HTMLElement">HTML element</a> with the specified name is {@linkplain HTMLElements#getEndTagForbiddenElementNames() forbidden}, otherwise <code>false</code>.
	 * @deprecated  Use {@link HTMLElements#getEndTagForbiddenElementNames()}<code>.contains(name.toLowerCase())</code> instead.
	 */
	public static boolean isForbidden(String name) {
		return HTMLElements.getEndTagForbiddenElementNames().contains(name.toLowerCase());
	}

	/**
	 * Indicates whether the end tag of an <a href="HTMLElements.html#HTMLElement">HTML element</a> with the specified name is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}.
	 * <p>
	 * This method has been deprecated as of version 2.0 and replaced with the {@link HTMLElements#getEndTagOptionalElementNames()} method.
	 *
	 * @return <code>true</code> if the end tag of an <a href="HTMLElements.html#HTMLElement">HTML element</a> with the specified name is {@linkplain HTMLElements#getEndTagOptionalElementNames() optional}, otherwise <code>false</code>.
	 * @deprecated  Use {@link HTMLElements#getEndTagOptionalElementNames()}<code>.contains(name.toLowerCase())</code> instead.
	 */
	public static boolean isOptional(String name) {
		return HTMLElements.getEndTagOptionalElementNames().contains(name.toLowerCase());
	}

	/**
	 * Indicates whether the end tag of an <a href="HTMLElements.html#HTMLElement">HTML element</a> with the specified name is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}.
	 * <p>
	 * This method has been deprecated as of version 2.0 and replaced with the {@link HTMLElements#getEndTagRequiredElementNames()} method.
	 *
	 * @return <code>true</code> if the end tag of an <a href="HTMLElements.html#HTMLElement">HTML element</a> with the specified name is {@linkplain HTMLElements#getEndTagRequiredElementNames() required}, otherwise <code>false</code>.
	 * @deprecated  Use {@link HTMLElements#getEndTagRequiredElementNames()}<code>.contains(name.toLowerCase())</code> instead.
	 */
	public static boolean isRequired(String name) {
		return HTMLElements.getEndTagRequiredElementNames().contains(name.toLowerCase());
	}

	/**
	 * Returns the previous or next end tag matching the specified {@linkplain #getName() name}
	 * and {@linkplain EndTagType type}, starting at the specified position.
	 * <p>
	 * Called from {@link Source#findPreviousEndTag(int pos, String name)} and {@link Source#findNextEndTag(int pos, String name, EndTagType endTagType)}.
	 *
	 * @param source  the {@link Source} document.
	 * @param pos  the position to search from.
	 * @param name  the {@linkplain #getName() name} of the tag (must be lower case and not null).
	 * @param endTagType the {@linkplain EndTagType type} of end tag to search for.
	 * @param previous  search backwards if true, otherwise search forwards.
	 * @return the previous or next end tag matching the specified {@linkplain #getName() name} and {@linkplain EndTagType type}, starting at the specified position, or null if none is found.
	 */
	static EndTag findPreviousOrNext(final Source source, final int pos, final String searchName, final EndTagType endTagType, final boolean previous) {
		if (searchName==null) return (EndTag)Tag.findPreviousOrNextTag(source,pos,endTagType,previous);
		if (searchName.length()==0) throw new IllegalArgumentException("searchName argument must not be zero length");
		final String searchString=endTagType.generateHTML(searchName);
		try {
			final ParseText parseText=source.getParseText();
			int begin=pos;
			do {
				begin=previous?parseText.lastIndexOf(searchString,begin):parseText.indexOf(searchString,begin);
				if (begin==-1) return null;
				final EndTag endTag=(EndTag)source.getTagAt(begin);
				if (endTag!=null && endTag.getEndTagType()==endTagType) return endTag;
			} while (previous ? (begin-=1)>=0 : (begin+=1)<source.end);
		} catch (IndexOutOfBoundsException ex) {
			// this should only happen when the end of file is reached in the middle of a tag.
			// we don't have to do anything to handle it as there will be no more tags anyway.
		}
		return null;
	}

	static EndTag findPreviousOrNext(final Source source, int pos, final boolean previous) {
		while (true) {
			final Tag tag=Tag.findPreviousOrNextTag(source,pos,previous);
			if (tag==null) return null;
			if (tag instanceof EndTag) return (EndTag)tag;
			pos+=previous?-1:1;
		}
	}
}

