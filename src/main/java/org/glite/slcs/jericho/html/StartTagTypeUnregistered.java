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

final class StartTagTypeUnregistered extends StartTagType {
	static final StartTagTypeUnregistered INSTANCE=new StartTagTypeUnregistered();

	private StartTagTypeUnregistered() {
		super("unregistered",START_DELIMITER_PREFIX,">",null,false,false,false);
	}

	protected Tag constructTagAt(final Source source, final int pos) {
		final int closingDelimiterPos=source.getParseText().indexOf('>',pos+1);
		if (closingDelimiterPos==-1) return null;
		final Tag tag=constructStartTag(source,pos,closingDelimiterPos+1,"",null);
		if (source.isLoggingEnabled()) source.log(source.getRowColumnVector(tag.getBegin()).appendTo(new StringBuffer(200).append("Encountered possible StartTag ").append(tag).append(" at ")).append(" whose content does not match a registered StartTagType").toString());
		return tag;
	}
}
