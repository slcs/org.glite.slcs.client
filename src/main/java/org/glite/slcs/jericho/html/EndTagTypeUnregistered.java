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

final class EndTagTypeUnregistered extends EndTagType {
	static final EndTagTypeUnregistered INSTANCE=new EndTagTypeUnregistered();

	private EndTagTypeUnregistered() {
		super("/unregistered",START_DELIMITER_PREFIX,">",false);
	}

	protected Tag constructTagAt(final Source source, final int pos) {
		final ParseText parseText=source.getParseText();
		final int nameBegin=pos+getStartDelimiter().length();
		final int nameEnd=parseText.indexOf(getClosingDelimiter(),nameBegin);
		final String name=parseText.substring(nameBegin,nameEnd); // throws IndexOutOfBoundsException if nameEnd==-1
		final EndTag endTag=constructEndTag(source,pos,nameEnd+getClosingDelimiter().length(),name);
		if (source.isLoggingEnabled()) source.log(source.getRowColumnVector(pos).appendTo(new StringBuffer(200).append("Encountered possible EndTag ").append(endTag).append(" at ")).append(" whose content does not match a registered EndTagType").toString());
		return endTag;
	}
}
