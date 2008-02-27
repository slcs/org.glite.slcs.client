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


class StartTagTypeMarkupDeclaration extends StartTagTypeGenericImplementation {
	static final StartTagTypeMarkupDeclaration INSTANCE=new StartTagTypeMarkupDeclaration();

	static final String ELEMENT="!element";
	static final String ATTLIST="!attlist";
	static final String ENTITY="!entity";
	static final String NOTATION="!notation";

	private StartTagTypeMarkupDeclaration() {
		super("markup declaration","<!",">",null,false,false,true);
	}

	protected Tag constructTagAt(final Source source, final int pos) {
		final Tag tag=super.constructTagAt(source,pos);
		if (tag==null) return null;
		final String name=tag.getName();
		if (name!=ELEMENT && name!=ATTLIST && name!=ENTITY && name!=NOTATION) return null; // can use == instead of .equals() because the names are in Tag.TAG_MAP
		return tag;
	}

	protected int findEnd(final Source source, int pos) {
		final ParseText parseText=source.getParseText();
		boolean insideQuotes=false;
		do {
			final char c=parseText.charAt(pos);
			if (c=='"') {
				insideQuotes=!insideQuotes;
			} else if (c=='>' && !insideQuotes) {
				return pos+1;
			}
		} while ((++pos)<source.getEnd());
		return -1;
	}
}
