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
 * This is an internal class used to efficiently map integers to strings, which is used in the CharacterEntityReference class.
 */
final class IntStringHashMap {
	private static final int DEFAULT_INITIAL_CAPACITY=15;
	private static final float DEFAULT_LOAD_FACTOR=0.75f;
	private transient Entry[] entries; // length must always be a power of 2.
	private transient int size;
	private int threshold;
	private float loadFactor;
	private int bitmask; // always entries.length-1

	public IntStringHashMap(int initialCapacity, final float loadFactor) {
		this.loadFactor=loadFactor;
		int capacity=1;
		while (capacity<initialCapacity) capacity<<=1;
		threshold=(int)(capacity*loadFactor);
		entries=new Entry[capacity];
		bitmask=capacity-1;
	}

	public IntStringHashMap(final int initialCapacity) {
		this(initialCapacity,DEFAULT_LOAD_FACTOR);
	}

	public IntStringHashMap() {
		this(DEFAULT_INITIAL_CAPACITY,DEFAULT_LOAD_FACTOR);
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size==0;
	}

	private int getIndex(final int key) {
		return key&bitmask; // equivalent to (key%entries.length) but more efficient.
	}

	public String get(final int key) {
		Entry entry=entries[getIndex(key)];
		while (entry!=null) {
			if (key==entry.key) return entry.value;
			entry=entry.next;
		}
		return null;
	}

	private Entry getEntry(final int key) {
		Entry entry=entries[getIndex(key)];
		while (entry!=null && key!=entry.key) entry=entry.next;
		return entry;
	}

	public boolean containsKey(final int key) {
		return getEntry(key)!=null;
	}

	public String put(final int key, final String value) {
		final int index=getIndex(key);
		for (Entry entry=entries[index]; entry!= null; entry=entry.next) {
			if (key==entry.key) {
				final String oldValue=entry.value;
				entry.value=value;
				return oldValue;
			}
		}
		entries[index]=new Entry(key,value,entries[index]);
		if (size++>=threshold) increaseCapacity();
		return null;
	}

	private void increaseCapacity() {
		final int oldCapacity=entries.length;
		final Entry[] oldEntries=entries;
		entries=new Entry[oldCapacity<<1];
		bitmask=entries.length-1;
		for (int i=0; i<oldCapacity; i++) {
			Entry entry=oldEntries[i];
			while (entry!=null) {
				final Entry next=entry.next;
				final int index=getIndex(entry.key);
				entry.next=entries[index];
				entries[index]=entry;
				entry=next;
			}
		}
		threshold=(int)(entries.length*loadFactor);
	}

	public String remove(final int key) {
		final int index=getIndex(key);
		Entry previous=null;
		for (Entry entry=entries[index]; entry!=null; entry=(previous=entry).next) {
			if (key==entry.key) {
				if (previous==null)
					entries[index]=entry.next;
				else
					previous.next=entry.next;
				size--;
				return entry.value;
			}
		}
		return null;
	}

	public void clear() {
		for (int i=bitmask; i>=0; i--) entries[i]=null;
		size=0;
	}

	public boolean containsValue(final String value) {
		if (value==null) {
			for (int i=bitmask; i>=0; i--)
				for (Entry entry=entries[i]; entry!=null; entry=entry.next)
					if (entry.value==null) return true;
		} else {
			for (int i=bitmask; i>=0; i--)
				for (Entry entry=entries[i]; entry!=null; entry=entry.next)
					if (value.equals(entry.value)) return true;
		}
		return false;
	}

	private static final class Entry {
		final int key;
		String value;
		Entry next;

		public Entry(final int key, final String value, final Entry next) {
			this.key=key;
			this.value=value;
			this.next=next;
		}
	}
}
