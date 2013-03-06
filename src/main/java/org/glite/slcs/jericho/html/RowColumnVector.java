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

/**
 * Represents the row and column number of a character position in the source document.
 * <p>
 * Obtained using the {@link Source#getRowColumnVector(int pos)} method.
 */
public final class RowColumnVector {
	private int row;
	private int column;
	private int pos;
	
	private static final RowColumnVector FIRST=new RowColumnVector(1,1,0);

	private RowColumnVector(final int row, final int column, final int pos) {
		this.row=row;
		this.column=column;
		this.pos=pos;
	}

	/**
	 * Returns the row number of this character position in the source document.
	 * @return the row number of this character position in the source document.
	 */
	public int getRow() {
		return row;
	}
	
	/**
	 * Returns the column number of this character position in the source document.
	 * @return the column number of this character position in the source document.
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * Returns the character position in the source document.
	 * @return the character position in the source document.
	 */
	public int getPos() {
		return pos;
	}
	
	/**
	 * Returns a string representation of this character position.
	 * <p>
	 * The returned string has the format "<code>(</code><i>row</i><code>,</code><i>column</i><code>:</code><i>pos</i><code>)</code>".
	 *
	 * @return a string representation of this character position.
	 */
	public String toString() {
		return appendTo(new StringBuffer(20)).toString();
	}

	StringBuffer appendTo(final StringBuffer sb) {
		return sb.append('(').append(row).append(',').append(column).append(':').append(pos).append(')');
	}
	
	static RowColumnVector[] getCacheArray(final Source source) {
		final int lastSourcePos=source.end-1;
		final ArrayList list=new ArrayList();
		int pos=0;
		list.add(FIRST);
		int row=1;
		while (pos<=lastSourcePos) {
			final char ch=source.charAt(pos);
			if (ch=='\n' || (ch=='\r' && (pos==lastSourcePos || source.charAt(pos+1)!='\n'))) list.add(new RowColumnVector(++row,1,pos+1));
			pos++;
		}
		return (RowColumnVector[])list.toArray(new RowColumnVector[list.size()]);
	}

	static RowColumnVector get(final RowColumnVector[] cacheArray, final int pos) {
		int low=0;
		int high=cacheArray.length-1;
		while (true) {
			int mid=(low+high) >> 1;
			final RowColumnVector rowColumnVector=cacheArray[mid];
			if (rowColumnVector.pos<pos) {
				if (mid==high) return new RowColumnVector(rowColumnVector.row,pos-rowColumnVector.pos+1,pos);
				low=mid+1;
			} else if (rowColumnVector.pos>pos) {
				high=mid-1;
			} else {
				return rowColumnVector;
			}
		}
	}
}
