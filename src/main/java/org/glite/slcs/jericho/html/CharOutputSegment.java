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
import java.io.Writer;

/**
 * Implements an {@link OutputSegment} whose content is a single character constant.
 * <p>
 * This class has been removed from the public API as of version 2.2 and the functionality replaced with the 
 * {@link OutputDocument#Replace(int begin, int end, char ch)} method.
 */
final class CharOutputSegment implements OutputSegment {
	private int begin;
	private int end;
	private char ch;

	/**
	 * Constructs a new <code>CharOutputSegment</code> with the specified begin and end character positions and the specified content.
	 * @param begin  the position in the {@link OutputDocument} where this <code>OutputSegment</code> begins.
	 * @param end  the position in the {@link OutputDocument} where this <code>OutputSegment</code> ends.
	 * @param ch  the character output of the new <code>OutputSegment</code>.
	 */
	public CharOutputSegment(final int begin, final int end, final char ch) {
		this.begin=begin;
		this.end=end;
		this.ch=ch;
	}

	/**
	 * Constructs a new <code>CharOutputSegment</code> with the same span as the specified {@link Segment}.
	 * @param segment  a {@link Segment} defining the begin and end character positions of the new <code>OutputSegment</code>.
	 * @param ch  the character output of the new <code>OutputSegment</code>.
	 */
	public CharOutputSegment(final Segment segment, final char ch) {
		begin=segment.begin;
		end=segment.end;
		this.ch=ch;
	}

	/**
	 * Constructs a new <code>CharOutputSegment</code> which converts the specified {@link CharacterReference} to a normal character.
	 * @param characterReference  the character reference to convert.
	 */
	public CharOutputSegment(final CharacterReference characterReference) {
		this(characterReference,characterReference.getChar());
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	public void writeTo(final Writer writer) throws IOException {
		writer.write(ch);
	}

	public long getEstimatedMaximumOutputLength() {
		return 1;
	}

	public String toString() {
		return Character.toString(ch);
	}

	public String getDebugInfo() {
		return "("+begin+','+end+"):"+ch;
	}
}
