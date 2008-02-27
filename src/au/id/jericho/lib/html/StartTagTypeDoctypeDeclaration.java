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

final class StartTagTypeDoctypeDeclaration extends StartTagTypeGenericImplementation {
	static final StartTagTypeDoctypeDeclaration INSTANCE=new StartTagTypeDoctypeDeclaration();

	private StartTagTypeDoctypeDeclaration() {
		super("document type declaration","<!doctype",">",null,false,false,false);
	}

	protected int findEnd(final Source source, int pos) {
		final ParseText parseText=source.getParseText();
		boolean insideQuotes=false;
		boolean insideSquareBrackets=false;
		do {
			final char c=parseText.charAt(pos);
			if (insideQuotes) {
				if (c=='"') insideQuotes=false;
			} else {
				switch (c) {
					case '>':
						if (!insideSquareBrackets) return pos+1;
						break;
					case '"':
						insideQuotes=true;
						break;
					case '[':
						insideSquareBrackets=true;
						break;
					case ']':
						insideSquareBrackets=false;
						break;
				}
			}
		} while ((++pos)<source.getEnd());
		return -1;
	}
}
