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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class TagTypeRegister {
	private TagTypeRegister parent=null;
	private char ch=NULL_CHAR;
	private TagTypeRegister[] children=null; // always in alphabetical order
	private TagType[] tagTypes=null; // in descending order of priority

	private static final char NULL_CHAR='\u0000';

	private static final TagType[] DEFAULT_TAG_TYPES={
		StartTagType.UNREGISTERED,
		StartTagType.NORMAL,
		StartTagType.COMMENT,
		StartTagType.MARKUP_DECLARATION,
		StartTagType.DOCTYPE_DECLARATION,
		StartTagType.CDATA_SECTION,
		StartTagType.XML_PROCESSING_INSTRUCTION,
		StartTagType.XML_DECLARATION,
		StartTagType.SERVER_COMMON,
		EndTagType.UNREGISTERED,
		EndTagType.NORMAL
	};

	private static TagTypeRegister root=new TagTypeRegister();

	static {
		add(DEFAULT_TAG_TYPES);
	}

	private TagTypeRegister() {}

	private static synchronized void add(final TagType[] tagTypes) {
		for (int i=0; i<tagTypes.length; i++) add(tagTypes[i]);
	}

	public static synchronized void add(final TagType tagType) {
		TagTypeRegister cursor=root;
		final String startDelimiter=tagType.getStartDelimiter();
		for (int i=0; i<startDelimiter.length(); i++) {
			final char ch=startDelimiter.charAt(i);
			TagTypeRegister child=cursor.getChild(ch);
			if (child==null) {
				child=new TagTypeRegister();
				child.parent=cursor;
				child.ch=ch;
				cursor.addChild(child);
			}
			cursor=child;
		}
		cursor.addTagType(tagType);
	}

	public static synchronized void remove(final TagType tagType) {
		TagTypeRegister cursor=root;
		final String startDelimiter=tagType.getStartDelimiter();
		for (int i=0; i<startDelimiter.length(); i++) {
			final char ch=startDelimiter.charAt(i);
			final TagTypeRegister child=cursor.getChild(ch);
			if (child==null) return;
			cursor=child;
		}
		cursor.removeTagType(tagType);
		// clean up any unrequired children:
		while (cursor!=root && cursor.tagTypes==null && cursor.children==null) {
			cursor.parent.removeChild(cursor);
			cursor=cursor.parent;
		}
	}

	// list is in order of lowest to highest precedence
	public static List<TagType> getList() {
		final List<TagType> list=new ArrayList<TagType>();
		root.addTagTypesToList(list);
		return list;
	}
	
	private void addTagTypesToList(final List<TagType> list) {
		if (tagTypes!=null)
			for (int i=tagTypes.length-1; i>=0; i--) list.add(tagTypes[i]);
		if (children!=null)
			for (int i=0; i<children.length; i++) children[i].addTagTypesToList(list);
	}

	public static final String getDebugInfo() {
		return root.appendDebugInfo(new StringBuffer(),0).toString();
	}

	static final class ProspectiveTagTypeIterator implements Iterator<TagType> {
		private TagTypeRegister cursor;
		private int tagTypeIndex=0;
		
		public ProspectiveTagTypeIterator(final Source source, final int pos) {
			// returns empty iterator if pos out of range
			final ParseText parseText=source.getParseText();
			cursor=root;
			int posIndex=0;
			try {
				// find deepest node that matches the text at pos:
				while (true) {
					final TagTypeRegister child=cursor.getChild(parseText.charAt(pos+(posIndex++)));
					if (child==null) break;
					cursor=child;
				}
			} catch (IndexOutOfBoundsException ex) {}
			// go back up until we reach a node that contains a list of tag types:
			while (cursor.tagTypes==null) if ((cursor=cursor.parent)==null) break;
		}

		public boolean hasNext() {
			return cursor!=null;
		}

		public TagType getNextTagType() {
			final TagType[] tagTypes=cursor.tagTypes;
			final TagType nextTagType=tagTypes[tagTypeIndex];
			if ((++tagTypeIndex)==tagTypes.length) {
				tagTypeIndex=0;
				do {cursor=cursor.parent;} while (cursor!=null && cursor.tagTypes==null);
			}
			return nextTagType;
		}

		// use getNextTagType() instead to avoid the downcasting
		public TagType next() {
			return getNextTagType();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public String toString() {
		return appendDebugInfo(new StringBuffer(),0).toString();
	}

	private StringBuffer appendDebugInfo(final StringBuffer sb, final int level) {
		for (int i=0; i<level; i++) sb.append(" ");
		if (ch!=NULL_CHAR) sb.append(ch).append(' ');
		if (tagTypes!=null) {
			sb.append('(');
			for (int i=0; i<tagTypes.length; i++) {
				sb.append(tagTypes[i].getDescription()).append(", ");
			}
			sb.setLength(sb.length()-2);
			sb.append(')');
		}
		sb.append('\n');
		if (children!=null) {
			final int childLevel=level+1;
			for (int i=0; i<children.length; i++) {
				children[i].appendDebugInfo(sb,childLevel);
			}
		}
		return sb;
	}

	private TagTypeRegister getChild(final char ch) {
		if (children==null) return null;
		if (children.length==1) return children[0].ch==ch ? children[0] : null;
		// perform binary search:
		int low=0;
		int high=children.length-1;
		while (low<=high) {
			int mid=(low+high) >> 1;
			final char midChar=children[mid].ch;
			if (midChar<ch)
				low=mid+1;
			else if (midChar>ch)
				high=mid-1;
			else
				return children[mid];
		}
		return null;
	}
	
	private void addChild(final TagTypeRegister child) {
		// assumes the character associated with the child register does not already exist in this register's children.
		if (children==null) {
			children=new TagTypeRegister[] {child};
		} else {
			final TagTypeRegister[] newChildren=new TagTypeRegister[children.length+1];
			int i=0;
			while (i<children.length && children[i].ch<=child.ch) {
				newChildren[i]=children[i];
				i++;
			}
			newChildren[i++]=child;
			while (i<newChildren.length) {
				newChildren[i]=children[i-1];
				i++;
			}
			children=newChildren;
		}
	}

	private void removeChild(final TagTypeRegister child) {
		// this method assumes that the specified child exists in the children array
		if (children.length==1) {
			children=null;
			return;
		}
		final TagTypeRegister[] newChildren=new TagTypeRegister[children.length-1];
		int offset=0;
		for (int i=0; i<children.length; i++) {
			if (children[i]==child)
				offset=-1;
			else
				newChildren[i+offset]=children[i];
		}
		children=newChildren;
	}
	
	private int indexOfTagType(final TagType tagType) {
		if (tagTypes==null) return -1;
		for (int i=0; i<tagTypes.length; i++)
			if (tagTypes[i]==tagType) return i;
		return -1;
	}
	
	private void addTagType(final TagType tagType) {
		final int indexOfTagType=indexOfTagType(tagType);
		if (indexOfTagType==-1) {
			if (tagTypes==null) {
				tagTypes=new TagType[] {tagType};
			} else {
				final TagType[] newTagTypes=new TagType[tagTypes.length+1];
				newTagTypes[0]=tagType;
				for (int i=0; i<tagTypes.length; i++) newTagTypes[i+1]=tagTypes[i];
				tagTypes=newTagTypes;
			}
		} else {
			// tagType already exists in the list, just move it to the front
			for (int i=indexOfTagType; i>0; i--) tagTypes[i]=tagTypes[i-1];
			tagTypes[0]=tagType;
		}
	}

	private void removeTagType(final TagType tagType) {
		final int indexOfTagType=indexOfTagType(tagType);
		if (indexOfTagType==-1) return;
		if (tagTypes.length==1) {
			tagTypes=null;
			return;
		}
		final TagType[] newTagTypes=new TagType[tagTypes.length-1];
		for (int i=0; i<indexOfTagType; i++) newTagTypes[i]=tagTypes[i];
		for (int i=indexOfTagType; i<newTagTypes.length; i++) newTagTypes[i]=tagTypes[i+1];
		tagTypes=newTagTypes;
	}
}

