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


/**
 * Provides a generic implementation of the abstract {@link EndTagType} class based on the most common end tag behaviour.
 * <p>
 * This class is only of interest to users who wish to create <a href="TagType.html#Custom">custom tag types</a>.
 * <p>
 * The differences between this class and its abstract superclass {@link EndTagType} are:
 * <ul>
 *  <li>The introduction of the {@link #isStatic() IsStatic} property.
 *  <li>The {@link #constructTagAt(Source, int pos)} method has a default implementation.
 * </ul>
 * <p>
 * Most of the <a href="Tag.html#Predefined">predefined</a> end tag types are implemented using this class or a subclass of it.
 *
 * @see StartTagTypeGenericImplementation
 */
public class EndTagTypeGenericImplementation extends EndTagType {
	private final String staticString;

	/**
	 * Constructs a new <code>EndTagTypeGenericImplementation</code> object based on the specified properties.
	 * <br />(<a href="TagType.html#ImplementationAssistance">implementation assistance</a> method)
	 * <p>
	 * The purpose of the <code>isStatic</code> parameter is explained in the {@link #isStatic() IsStatic} property description.
	 *
	 * @param description  a {@linkplain #getDescription() description} of the new end tag type useful for debugging purposes.
	 * @param startDelimiter  the {@linkplain #getStartDelimiter() start delimiter} of the new end tag type.
	 * @param closingDelimiter  the {@linkplain #getClosingDelimiter() closing delimiter} of the new end tag type.
	 * @param isServerTag  indicates whether the new end tag type is a {@linkplain #isServerTag() server tag}.
	 * @param isStatic  determines whether the end tag text {@linkplain #isStatic() is static}.
	 */
	protected EndTagTypeGenericImplementation(final String description, final String startDelimiter, final String closingDelimiter, final boolean isServerTag, final boolean isStatic) {
		super(description,startDelimiter,closingDelimiter,isServerTag);
		staticString=isStatic ? (startDelimiter+closingDelimiter) : null;
	}
	
	/**
	 * Indicates whether the {@linkplain #generateHTML(String) end tag text} is static.
	 * <br />(<a href="TagType.html#Property">property</a> and <a href="#ImplementationAssistance">implementation assistance</a> method)
	 * <p>
	 * The purpose of this property is to determine the behaviour of the {@link #generateHTML(String startTagName)} method.
	 * <p>
	 * If this property is <code>true</code>, the {@linkplain #generateHTML(String) end tag text} is constant for all tags of this type.
	 * <p>
	 * If this property is <code>false</code>, the {@linkplain #generateHTML(String) end tag text} includes the
	 * {@linkplain StartTag#getName() name} of the {@linkplain #getCorrespondingStartTagType corresponding} 
	 * {@linkplain StartTag start tag}.
	 * <p>
	 * {@link MasonTagTypes#MASON_COMPONENT_CALLED_WITH_CONTENT_END} is the only <a href="TagType.html#Predefined">predefined</a> end tag
	 * for which this property is <code>true</code>.
	 * All tags of this type have the constant tag text "<code>&lt;/&amp;&gt;</code>".
	 *
	 * @return <code>true</code> if the {@linkplain #generateHTML(String) end tag text} is static, otherwise <code>false</code>.
	 */
	protected final boolean isStatic() {
		return staticString!=null;
	}

	/**
	 * Generates the HTML text of an {@linkplain EndTag end tag} of this type given the {@linkplain StartTag#getName() name} of a {@linkplain #getCorrespondingStartTagType() corresponding} {@linkplain StartTag start tag}.
	 * <br />(<a href="TagType.html#Property">property</a> method)
	 * <p>
	 * This implementation overrides the default implementation in {@link EndTagType#generateHTML(String startTagName)}.
	 * <p>
	 * If the value of the {@link #isStatic() IsStatic} property is <code>false</code>, it returns the same string
	 * as the default implementation:<br />
	 * {@link #getStartDelimiter() getStartTagDelimiter()}<code>+startTagName+</code>{@link #getClosingDelimiter() getClosingDelimiter()}.
	 * <p>
	 * If the value of the {@link #isStatic() IsStatic} property is <code>true</code>, it returns the cached static string:<br />
	 * {@link #getStartDelimiter() getStartDelimiter()}<code>+</code>{@link #getClosingDelimiter() getClosingDelimiter()}.
	 *
	 * @param startTagName  the {@linkplain StartTag#getName() name} of a {@linkplain #getCorrespondingStartTagType() corresponding} {@linkplain StartTag start tag}.
	 * @return the HTML text of an {@linkplain EndTag end tag} of this type given the {@linkplain StartTag#getName() name} of a {@linkplain #getCorrespondingStartTagType() corresponding} {@linkplain StartTag start tag}.
	 */
	public String generateHTML(final String startTagName) {
		return staticString!=null ? staticString : START_DELIMITER_PREFIX+startTagName+getClosingDelimiter();
	}

	/**
	 * Constructs a tag of this type at the specified position in the specified source document if it matches all of the required features.
	 * <br />(<a href="TagType.html#DefaultImplementation">default implementation</a> method)
	 * <p>
	 * This default implementation ensures that the source text matches the possible output of the
	 * {@link #generateHTML(String startTagName)} method.
	 * <p>
	 * If the value of the {@link #isStatic() IsStatic} property is <code>false</code>, this implementation ensures that the
	 * source text matches the expression:<br />
	 * {@link #getStartDelimiter() getStartTagDelimiter()}<code>+"</code><i>name</i><code>"+</code>{@link #getClosingDelimiter() getClosingDelimiter()}<br />
	 * where <i>name</i> is a valid {@linkplain Tag#isXMLName(CharSequence) XML tag name}.
	 * The {@linkplain Tag#getName() name} of the constructed end tag becomes {@link #getNamePrefix() getNamePrefix()}<code>+"</code><i>name</i><code>"</code>.
	 * <p>
	 * If the value of the {@link #isStatic() IsStatic} property is <code>true</code>, this implementation ensures that the 
	 * source text matches the static expression:<br />
	 * {@link #getStartDelimiter() getStartTagDelimiter()}<code>+</code>{@link #getClosingDelimiter() getClosingDelimiter()}<br />
	 * The {@linkplain Tag#getName() name} of the constructed end tag is the value of the {@link #getNamePrefix() getNamePrefix()} method.
	 * <p>
	 * See {@link TagType#constructTagAt(Source, int pos)} for more important information about this method.
	 *
	 * @param source  the {@link Source} document.
	 * @param pos  the position in the source document.
	 * @return a tag of this type at the specified position in the specified source document if it meets all of the required features, or <code>null</code> if it does not meet the criteria.
	 */
	protected Tag constructTagAt(final Source source, final int pos) {
		final ParseText parseText=source.getParseText();
		final int nameBegin=pos+START_DELIMITER_PREFIX.length();
		String name=null;
		final int startDelimiterEnd=pos+getStartDelimiter().length();
		int end=-1;
		if (isStatic()) {
			name=getNamePrefix();
			if (!parseText.containsAt(getClosingDelimiter(),startDelimiterEnd)) {
				if (source.isLoggingEnabled()) source.log(source.getRowColumnVector(pos).appendTo(new StringBuffer(200).append("EndTag of expected format ").append(staticString).append(" at ")).append(" not recognised as type '").append(getDescription()).append("' because it is missing the closing delimiter").toString());
				return null;
			}
			end=startDelimiterEnd+getClosingDelimiter().length();
		} else {
			final int nameEnd=source.findNameEnd(startDelimiterEnd);
			if (nameEnd==-1) return null;
			name=parseText.substring(nameBegin,nameEnd);
			if (!parseText.containsAt(getClosingDelimiter(),nameEnd)) {
				if (source.isLoggingEnabled()) source.log(source.getRowColumnVector(pos).appendTo(new StringBuffer(200).append("EndTag ").append(name).append(" at ")).append(" not recognised as type '").append(getDescription()).append("' because its closing delimiter does not immediately follow its name").toString());
				return null;
			}
			end=nameEnd+getClosingDelimiter().length();
		}
		return constructEndTag(source,pos,end,name);
	}
}
