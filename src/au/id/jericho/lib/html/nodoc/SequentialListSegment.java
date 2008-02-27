// Jericho HTML Parser - Java based library for analysing and manipulating HTML
// Version 2.1
// Copyright (C) 2005 Martin Jericho
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

package au.id.jericho.lib.html.nodoc;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import au.id.jericho.lib.html.Attributes;
import au.id.jericho.lib.html.Segment;
import au.id.jericho.lib.html.Source;

/**
 * A base class used internally to simulate multiple inheritance of {@link Segment} and <code>java.util.AbstractSequentialList</code>.
 * <p>
 * It allows a {@link Segment} based class to implement <code>java.util.List</code> without having to implement
 * all of the <code>List</code> methods explicitly, which would clutter the API documentation with mostly irrelevant methods.
 * By extending this class, most of the list implementation methods are simply listed in the inherited methods list.
 *
 * @see Attributes
 */
public abstract class SequentialListSegment extends Segment implements List {
	public SequentialListSegment(final Source source, final int begin, final int end) {
		super(source,begin,end);
	}

	/**
	 * Returns the number of items in the list.
	 * @return the number of items in the list.
	 */
	public abstract int getCount();

	/**
	 * Returns a list iterator of the items in this list (in proper sequence), starting at the specified position in the list.
	 * <p>
	 * The specified index indicates the first item that would be returned by an initial call to the <code>next()</code> method.
	 * An initial call to the <code>previous()</code> method would return the item with the specified index minus one.
	 *
	 * @param index  index of the first item to be returned from the list iterator (by a call to the <code>next()</code> method).
	 * @return a list iterator of the items in this list (in proper sequence), starting at the specified position in the list.
	 * @throws IndexOutOfBoundsException if the specified index is out of range (<code>index &lt; 0 || index &gt; size()</code>).
	 */
	public abstract ListIterator listIterator(int index);

	/**
	 * Returns the item at the specified position in this list.
	 * <p>
	 * This implementation first gets a list iterator pointing to the indexed item (with <code>listIterator(index)</code>).
	 * Then, it gets the element using <code>ListIterator.next</code> and returns it.
	 *
	 * @param index  the index of the item to return.
	 * @return the item at the specified position in this list.
	 * @throws IndexOutOfBoundsException if the specified index is out of range (<code>index &lt; 0 || index &gt;= size()</code>).
	 */
	public Object get(final int index) {
		final ListIterator li=listIterator(index);
		try {
			return(li.next());
		} catch(NoSuchElementException ex) {
			throw(new IndexOutOfBoundsException("index="+index));
		}
	}

	/**
	 * Returns the number of items in the list.
	 * <p>
	 * This is equivalent to {@link #getCount()},
	 * and is necessary to for the implementation of the <code>java.util.Collection</code> interface.
	 *
	 * @return the number of items in the list.
	 */
	public int size() {
		return getCount();
	}

	/**
	 * Indicates whether this list is empty.
	 * @return <code>true</code> if there are no items in the list, otherwise <code>false</code>.
	 */
	public boolean isEmpty() {
		return getCount()==0;
	}

	/**
	 * Indicates whether this list contains the specified object.
	 *
	 * @param o  object to be checked for containment in this list.
	 * @return <code>true</code> if this list contains the specified object, otherwise <code>false</code>.
	 */
	public boolean contains(final Object o) {
		return indexOf(o)>=0;
	}

	/**
	 * Returns an array containing all of the items in this list.
	 * @return an array containing all of the items in this list.
	 */
	public Object[] toArray() {
		final Object[] array=new Object[getCount()];
		int x=0;
		for (final ListIterator li=listIterator(0); li.hasNext();) array[x++]=li.next();
		return array;
	}

	/**
	 * Returns an array containing all of the items in this list in the correct order;
	 * the runtime type of the returned array is that of the specified array.
	 * If the list fits in the specified array, it is returned therein.
	 * Otherwise, a new array is allocated with the runtime type of the specified array and the size of this list.
	 * <p>
	 * If the list fits in the specified array with room to spare (i.e., the array has more elements than the list),
	 * the item in the array immediately following the end of the collection is set to <code>null</code>.
	 * This is useful in determining the length of the list <i>only</i>
	 * if the caller knows that the list does not contain any <code>null</code> items. 
	 *
	 * @param a the array into which the items of the list are to be stored, if it is big enough; otherwise, a new array of the same runtime type is allocated for this purpose.
	 * @return an array containing the items of the list.
	 * @throws NullPointerException if the specified array is <code>null</code>.
	 * @throws ArrayStoreException if the runtime type of the specified array is not a supertype of the runtime type of every item in this list.
	 */
	public Object[] toArray(Object a[]) {
		final int count=getCount();
		if (a.length<count) a=(Object[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(),count);
		int x=0;
		for (final ListIterator li=listIterator(0); li.hasNext();) a[x++]=li.next();
		if (a.length>count) a[count]=null;
		return a;
	}

	/**
	 * This list is unmodifiable, so this method always throws an <code>UnsupportedOperationException</code>.
	 * @throws UnsupportedOperationException
	 */
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Indicates whether this list contains all of the items in the specified collection.
	 * @param collection  the collection to be checked for containment in this list.
	 * @return <code>true</code> if this list contains all of the items in the specified collection, otherwise <code>false</code>.
	 * @throws NullPointerException if the specified collection is null.
	 * @see #contains(Object)
	 */
	public boolean containsAll(final Collection collection) {
		for (final Iterator i=collection.iterator(); i.hasNext();)
			if(!contains(i.next())) return false;
		return true;
	}

	/**
	 * This list is unmodifiable, so this method always throws an <code>UnsupportedOperationException</code>.
	 * @throws UnsupportedOperationException
	 */
	public boolean addAll(Collection collection) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This list is unmodifiable, so this method always throws an <code>UnsupportedOperationException</code>.
	 * @throws UnsupportedOperationException
	 */
	public boolean removeAll(Collection collection) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This list is unmodifiable, so this method always throws an <code>UnsupportedOperationException</code>.
	 * @throws UnsupportedOperationException
	 */
	public boolean retainAll(Collection collection) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This list is unmodifiable, so this method always throws an <code>UnsupportedOperationException</code>.
	 * @throws UnsupportedOperationException
	 */
	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This list is unmodifiable, so this method always throws an <code>UnsupportedOperationException</code>.
	 * @throws UnsupportedOperationException
	 */
	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This list is unmodifiable, so this method always throws an <code>UnsupportedOperationException</code>.
	 * @throws UnsupportedOperationException
	 */
	public void add(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This list is unmodifiable, so this method always throws an <code>UnsupportedOperationException</code>.
	 * @throws UnsupportedOperationException
	 */
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the index in this list of the first occurence of the specified object, or -1 if the list does not contain this object.
	 *
	 * @param o  object to search for.
	 * @return the index in this list of the first occurence of the specified object, or -1 if the list does not contain this object.
	 */
	public int indexOf(final Object o) {
		final ListIterator li=listIterator(0);
		if (o==null) {
			while (li.hasNext()) if (li.next()==null) return li.previousIndex();
		} else {
			while (li.hasNext()) if (o.equals(li.next())) return li.previousIndex();
		}
		return -1;
	}

	/**
	 * Returns the index in this list of the last occurence of the specified object, or -1 if the list does not contain this object.
	 *
	 * @param o  object to search for.
	 * @return the index in this list of the last occurence of the specified object, or -1 if the list does not contain this object.
	 */
	public int lastIndexOf(final Object o) {
		final ListIterator li=listIterator(getCount());
		if (o==null) {
			while (li.hasPrevious()) if (li.previous()==null) return li.nextIndex();
		} else {
			while (li.hasPrevious()) if (o.equals(li.previous())) return li.nextIndex();
		}
		return -1;
	}

	/**
	 * This list is unmodifiable, so this method always throws an <code>UnsupportedOperationException</code>.
	 * @throws UnsupportedOperationException
	 */
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * This list is unmodifiable, so this method always throws an <code>UnsupportedOperationException</code>.
	 * @throws UnsupportedOperationException
	 */
	public boolean addAll(int index, Collection collection) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns an iterator over the items in the list in proper sequence.
	 * @return an iterator over the items in the list in proper sequence.
	 */
	public Iterator iterator() {
		return listIterator();
	}

	/**
	 * Returns a list iterator of the items in this list (in proper sequence), starting with the first item in the list.
	 * @return a list iterator of the items in this list (in proper sequence), starting with the first item in the list.
	 * @see #listIterator(int)
	 */
	public ListIterator listIterator() {
		return listIterator(0);
	}

	/**
	 * Returns a view of the portion of this list between <code>fromIndex</code>, inclusive, and <code>toIndex</code>, exclusive.
	 * (If <code>fromIndex</code> and <code>toIndex</code> are equal, the returned list is empty.)
	 * The returned list is backed by this list, so changes in the returned list are reflected in this list, and vice-versa.
	 * The returned list supports all of the optional list operations supported by this list.
	 *
	 * @param fromIndex low endpoint (inclusive) of the subList.
	 * @param toIndex high endpoint (exclusive) of the subList.
	 * @return a view of the specified range within this list.
	 * @throws IndexOutOfBoundsException endpoint index value out of range <code>(fromIndex &lt; 0 || toIndex &gt; size)</code>
	 * @throws IllegalArgumentException endpoint indices out of order <code>(fromIndex &gt; toIndex)</code>
	 * @see java.util.List#subList(int fromIndex, int toIndex)
	 */
	public List subList(final int fromIndex, final int toIndex) {
		return (new SubList(this,fromIndex,toIndex));
	}

	private static class SubList extends AbstractList {
		private final List list;
		private final int offset;
		private final int size;

		SubList(final List list, final int fromIndex, final int toIndex) {
			if (fromIndex<0) throw new IndexOutOfBoundsException("fromIndex="+fromIndex);
			if (toIndex>list.size()) throw new IndexOutOfBoundsException("toIndex="+toIndex);
			if (fromIndex>toIndex) throw new IllegalArgumentException("fromIndex("+fromIndex+") > toIndex("+toIndex+")");
			this.list=list;
			offset=fromIndex;
			size=toIndex-fromIndex;
		}

		public Object get(final int index) {
			return list.get(getSuperListIndex(index));
		}

		public int size() {
			return size;
		}

		public Iterator iterator() {
			return listIterator();
		}

		public ListIterator listIterator(final int index) {
			return new ListIterator() {
				private final ListIterator i=list.listIterator(getSuperListIndex(index));
				public boolean hasNext() {
					return nextIndex()<size;
				}
				public Object next() {
					if (!hasNext()) throw new NoSuchElementException();
					return i.next();
				}
				public boolean hasPrevious() {
					return previousIndex()>=0;
				}
				public Object previous() {
					if (!hasPrevious()) throw new NoSuchElementException();
					return i.previous();
				}
				public int nextIndex() {
					return i.nextIndex()-offset;
				}
				public int previousIndex() {
					return i.previousIndex()-offset;
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
				public void set(Object o) {
					throw new UnsupportedOperationException();
				}
				public void add(Object o) {
					throw new UnsupportedOperationException();
				}
			};
		}

		public List subList(final int fromIndex, final int toIndex) {
			return new SubList(this,fromIndex,toIndex);
		}

		private int getSuperListIndex(final int index) {
			if (index<0 || index>=size) throw new IndexOutOfBoundsException("index="+index+", size="+size);
			return index+offset;
		}
	}
}
