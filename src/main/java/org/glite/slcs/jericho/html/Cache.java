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

import java.util.Iterator;
import java.util.List;

/**
 * Represents a cached map of character positions to tags.
 * The allTagTypesSubCache object is used to cache all tags.
 * Additional subcaches are used to cache single tag types, which increases the performance when searching for those tag types.
 * A list of tag types to be cached separately is specified in the SeparatelyCachedTagTypes property.
 * The standard implementation caches only COMMENT tag types separately, as these tag types are searched extensively
 * in the process of ensuring that every non-server tag is not located inside a comment.
 */
final class Cache {
	public final Source source;
	private final SubCache allTagTypesSubCache;
	private final SubCache[] subCaches; // contains allTagTypesSubCache plus a SubCache object for each separately cached tag type

	public Cache(final Source source) {
		this.source=source;
		allTagTypesSubCache=new SubCache(this,null);
		TagType[] separatelyCachedTagTypes=getSeparatelyCachedTagTypes();
		subCaches=new SubCache[separatelyCachedTagTypes.length+1];
		subCaches[0]=allTagTypesSubCache;
		for (int i=0; i<separatelyCachedTagTypes.length; i++)
			subCaches[i+1]=new SubCache(this,separatelyCachedTagTypes[i]);
	}

	public void clear() {
		for (int i=0; i<subCaches.length; i++) subCaches[i].clear();
	}

	public Tag getTagAt(final int pos) {
		return source.useAllTypesCache
			?	allTagTypesSubCache.getTagAt(pos)
			: Tag.getTagAtUncached(source,pos);
	}

	public Tag findPreviousOrNextTag(final int pos, final boolean previous) {
		// returns null if pos is out of range.
		return allTagTypesSubCache.findPreviousOrNextTag(pos,previous);
	}

	public Tag findPreviousOrNextTag(final int pos, final TagType tagType, final boolean previous) {
		// returns null if pos is out of range.
		for (int i=source.useAllTypesCache ? 0 : 1; i<subCaches.length; i++)
			if (tagType==subCaches[i].tagType) return subCaches[i].findPreviousOrNextTag(pos,previous);
		return Tag.findPreviousOrNextTagUncached(source,pos,tagType,previous,ParseText.NO_BREAK);
	}

	public Tag addTagAt(final int pos) {
		final Tag tag=Tag.getTagAtUncached(source,pos);
		allTagTypesSubCache.addTagAt(pos,tag);
		if (tag==null) return tag;
		final TagType tagType=tag.getTagType();
		for (int i=1; i<subCaches.length; i++) {
			if (tagType==subCaches[i].tagType) {
				subCaches[i].addTagAt(pos,tag);
				return tag;
			}
		}
		return tag;
	}

	public int getTagCount() {
		return allTagTypesSubCache.size()-2;
	}

	public Iterator<Tag> getTagIterator() {
		return allTagTypesSubCache.getTagIterator();
	}

	public void loadAllTags(final List<Tag> tags, final Tag[] allRegisteredTags, final StartTag[] allRegisteredStartTags) {
		// assumes the tags list implements RandomAccess
		final int tagCount=tags.size();
		allTagTypesSubCache.bulkLoad_Init(tagCount);
		int registeredTagIndex=0;
		int registeredStartTagIndex=0;
		for (int i=0; i<tagCount; i++) {
			Tag tag=(Tag)tags.get(i);
			if (!tag.isUnregistered()) {
				allRegisteredTags[registeredTagIndex++]=tag;
				if (tag instanceof StartTag) allRegisteredStartTags[registeredStartTagIndex++]=(StartTag)tag;
			}
			allTagTypesSubCache.bulkLoad_Set(i,tag);
			for (int x=1; x<subCaches.length; x++) {
				if (tag.getTagType()==subCaches[x].tagType) {
					subCaches[x].bulkLoad_AddToTypeSpecificCache(tag);
					break;
				}
			}
		}
		for (int x=1; x<subCaches.length; x++)
			subCaches[x].bulkLoad_FinaliseTypeSpecificCache();
	}

	public String toString() {
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<subCaches.length; i++) subCaches[i].appendTo(sb);
		return sb.toString();
	}

	protected int getSourceLength() {
		return source.end;
	}
	
	private static TagType[] getSeparatelyCachedTagTypes() {
		return TagType.getTagTypesIgnoringEnclosedMarkup();
	}
}
