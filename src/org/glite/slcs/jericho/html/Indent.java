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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This is an internal class for encapsulating the HTML indenting functionality.
 */
final class Indent implements CharStreamSource {
	private final Segment segment;
	private final CharSequence sourceText;
	private final String indentText;
	private final boolean tidyTags;
	private final boolean collapseWhiteSpace;
	private final boolean indentAllElements;
	private final boolean indentScriptElements;
	private Writer writer;
	
	private Tag nextTag;
	private int index;

	public Indent(final Segment segment, final String indentText, final boolean tidyTags, final boolean collapseWhiteSpace, final boolean indentAllElements) {
		this.segment=segment;
		sourceText=segment.source.toString();
		this.indentText=indentText;
		this.tidyTags=tidyTags;
		this.collapseWhiteSpace=collapseWhiteSpace;
		this.indentAllElements=indentAllElements;
		this.indentScriptElements=indentAllElements; // SCRIPT elements need to be inline to keep functional equivalency of output
	}

	public void writeTo(final Writer writer) throws IOException {
		this.writer=writer;
		nextTag=segment.source.findNextTag(segment.begin);
		index=segment.begin;
		writeContent(segment.end,segment.getChildElements(),0);
		writer.flush();
	}

	public long getEstimatedMaximumOutputLength() {
		return sourceText.length()*2;
	}

	private void writeContent(final int end, final List childElements, final int depth) throws IOException {
		// sets index to end
		for (final Iterator i=childElements.iterator(); i.hasNext();) {
			final Element element=(Element)i.next();
			final int elementBegin=element.begin;
			if (elementBegin>=end) break;
			if (indentAllElements) {
				writeText(elementBegin,depth,false,false,false,collapseWhiteSpace);
				writeElement(element,depth,end,false,false);
			} else {
				final String elementName=element.getName();
				if (!indent(element)) continue;
				writeText(elementBegin,depth,false,false,false,collapseWhiteSpace);
				if (elementName==HTMLElementName.PRE || elementName==HTMLElementName.TEXTAREA) {
					writeElement(element,depth,end,true,true);
				} else if (elementName==HTMLElementName.SCRIPT) {
					writeElement(element,depth,end,true,false);
				} else {
					writeElement(element,depth,end,false,!containsNonInlineLevelChildElements(element));
				}
			}
		}
		writeText(end,depth,false,false,false,collapseWhiteSpace);
	}

	private boolean indent(final Element element) {
		final StartTagType startTagType=element.getStartTag().getStartTagType();
		if (startTagType==StartTagType.DOCTYPE_DECLARATION) return true;
		if (startTagType!=StartTagType.NORMAL) return false;
		final String elementName=element.getName();
		if (elementName==HTMLElementName.SCRIPT) return indentScriptElements;
		if (!HTMLElements.getInlineLevelElementNames().contains(elementName)) return true;
		return containsNonInlineLevelChildElements(element);
	}

	private void writeText(final int end, int depth, final boolean beginInline, final boolean endInline, final boolean increaseIndentAfterFirstLineBreak, final boolean collapseWhiteSpace) throws IOException {
		// sets index to end
		if (index==end) return;
		while (Segment.isWhiteSpace(sourceText.charAt(index))) if (++index==end) return; // trim whitespace.
		if (!beginInline) writeIndent(depth);
		writeTextInline(end,depth,increaseIndentAfterFirstLineBreak,collapseWhiteSpace);
		if (!endInline) writer.write('\n');
	}

	private void writeElement(final Element element, final int depth, final int end, final boolean preformatted, boolean renderContentInline) throws IOException {
		// sets index to minimum of element.end or end
		final StartTag startTag=element.getStartTag();
		final EndTag endTag=element.getEndTag();
		writeIndent(depth);
		writeTag(startTag,depth,end);
		if (index==end) {
			writer.write('\n');
			return;
		}
		if (!renderContentInline) writer.write('\n');
		int contentEnd=element.getContentEnd();
		if (end<contentEnd) contentEnd=end;
		if (preformatted) {
			if (renderContentInline) {
				// Preformatted element such as PRE, TEXTAREA
				writeContentPreformatted(contentEnd,depth);
			} else {
				// SCRIPT element
				writeIndentedScriptContent(contentEnd,depth+1);
			}
		} else {
			if (renderContentInline) {
				// Inline-level element
				if (collapseWhiteSpace) {
					writeTextCollapseWhiteSpace(contentEnd,depth);
				} else {
					if (!writeTextInline(contentEnd,depth,true,false)) {
						writer.write('\n');
						renderContentInline=false;
					}
				}
			} else {
				// Block-level element
				writeContent(contentEnd,element.getChildElements(),depth+1);
			}
		}
		if (endTag!=null && end>endTag.begin) {
			if (!renderContentInline) writeIndent(depth);
			// assert index=endTag.begin
			writeTag(endTag,depth,end);
			writer.write('\n');
		} else if (renderContentInline) {
			writer.write('\n');
		}
	}

	private void updateNextTag() {
		// ensures that nextTag is up to date
		while (nextTag!=null) {
			if (nextTag.begin>=index) return;
			nextTag=nextTag.findNextTag();
		}
	}

	private void writeIndentedScriptContent(final int end, final int depth) throws IOException {
		// sets index to end
		if (index==end) return;
		int startOfLinePos=getStartOfLinePos(end,false);
		if (index==end) return;
		if (startOfLinePos==-1) {
			// Script started on same line as start tag.  Use the start of the next line to determine the original indent.
			writeIndent(depth);
			writeLineKeepWhiteSpace(end,depth);
			writer.write('\n');
			if (index==end) return;
			startOfLinePos=getStartOfLinePos(end,true);
			if (index==end) return;
		}
		writeTextPreserveIndenting(end,depth,index-startOfLinePos);
		writer.write('\n');
	}

	private boolean writeTextPreserveIndenting(final int end, final int depth) throws IOException {
		// sets index to end
		// returns true if all text was on one line, otherwise false
		// assert index==tag.begin;
		// end is normally tag.end, but in rare cases may be < tag.end
		// Use the start of the next line to determine the original indent.
		writeLineKeepWhiteSpace(end,depth);
		if (index==end) return true;
		int startOfLinePos=getStartOfLinePos(end,true);
		if (index==end) return true;
		writer.write('\n');
		writeTextPreserveIndenting(end,depth+1,index-startOfLinePos);
		return false;
	}

	private void writeTextPreserveIndenting(final int end, final int depth, final int originalIndentLength) throws IOException {
		// sets index to end
		writeIndent(depth);
		writeLineKeepWhiteSpace(end,depth);
		while (index!=end) {
			// Skip over the original indent:
			for (int x=0; x<originalIndentLength; x++) {
				final char ch=sourceText.charAt(index);
				if (!(ch==' ' || ch=='\t')) break;
				if (++index==end) return;
			}
			writer.write('\n');
			// Insert our indent:
			writeIndent(depth);
			// Write the rest of the line including any indent greater than the first line's indent:
			writeLineKeepWhiteSpace(end,depth);
		}
	}

	private int getStartOfLinePos(final int end, final boolean atStartOfLine) {
		// returns the starting position of the next complete line containing text, or -1 if texts starts on the current line (hence not a complete line).
		// sets index to the start of the text following the returned position, or end, whichever comes first.
		int startOfLinePos=atStartOfLine ? index : -1;
		while (true) {
			final char ch=sourceText.charAt(index);
			if (ch=='\n' || ch=='\r') {
				startOfLinePos=index+1;
			} else if (!(ch==' ' || ch=='\t')) break;
			if (++index==end) break;
		}
		return startOfLinePos;
	}

	private void writeSpecifiedTextInline(final CharSequence text, int depth) throws IOException {
		final int textLength=text.length();
		int i=writeSpecifiedLine(text,0);
		if (i<textLength) {
			final int subsequentLineDepth=depth+1;
			do {
				while (Segment.isWhiteSpace(text.charAt(i))) if (++i>=textLength) return; // trim whitespace.
				writer.write('\n');
				writeIndent(subsequentLineDepth);
				i=writeSpecifiedLine(text,i);
			} while (i<textLength);
		}
	}

	private int writeSpecifiedLine(final CharSequence text, int i) throws IOException {
		// Writes the first line from the specified text starting from the specified position.
		// The line break characters are not written.
		// Returns the position following the first line break character(s), or text.length() if the text contains no line breaks.
		final int textLength=text.length();
		while (true) {
			final char ch=text.charAt(i);
			if (ch=='\r') {
				final int nexti=i+1;
				if (nexti<textLength && text.charAt(nexti)=='\n') return i+2;
			}
			if (ch=='\n') return i+1;
			writer.write(ch);
			if (++i>=textLength) return i;
		}
	}

	private boolean writeTextInline(final int end, int depth, final boolean increaseIndentAfterFirstLineBreak, final boolean collapseWhiteSpace) throws IOException {
		// returns true if all text was on one line, otherwise false
		// sets index to end
		if (index==end) return true;
		writeLine(end,depth,collapseWhiteSpace);
		if (index==end) return true;
		final int subsequentLineDepth=increaseIndentAfterFirstLineBreak ? depth+1 : depth;
		do {
			while (Segment.isWhiteSpace(sourceText.charAt(index))) if (++index==end) return false; // trim whitespace.
			writer.write('\n');
			writeIndent(subsequentLineDepth);
			writeLine(end,subsequentLineDepth,collapseWhiteSpace);
		} while (index<end);
		return false;
	}

	private void writeLine(final int end, final int depth, final boolean collapseWhiteSpace) throws IOException {
		// sets index to the position following the first line break character(s), or to end if collapseWhiteSpace or the text contains no line breaks.
		if (collapseWhiteSpace) {
			writeTextCollapseWhiteSpace(end,depth);
		} else {
			writeLineKeepWhiteSpace(end,depth);
		}
	}

	private void writeLineKeepWhiteSpace(final int end, final int depth) throws IOException {
		// Writes the first line from the source text starting from the specified position, ending at the specified end position.
		// The line break characters are not written.
		// Sets index to the position following the first line break character(s), or end if the text contains no line breaks. index is guaranteed < end.
		// Any tags encountered are written using the writeTag method, whose output may include line breaks.
		updateNextTag();
		while (true) {
			while (nextTag!=null && index==nextTag.begin) {
				writeTag(nextTag,depth,end);
				if (index==end) return;
			}
			final char ch=sourceText.charAt(index);
			if (ch=='\r') {
				final int nextindex=index+1;
				if (nextindex<end && sourceText.charAt(nextindex)=='\n') {
					index+=2;
					return;
				}
			}
			if (ch=='\n') {
				index++;
				return;
			}
			writer.write(ch);
			if (++index==end) return;
		}
	}		

	private void writeTextCollapseWhiteSpace(final int end, final int depth) throws IOException {
		// sets index to end
		boolean lastWasWhiteSpace=false;
		updateNextTag();
		while (index<end) {
			while (nextTag!=null && index==nextTag.begin) {
				if (lastWasWhiteSpace) {
					writer.write(' ');
					lastWasWhiteSpace=false;
				}
				writeTag(nextTag,depth,end);
				if (index==end) return;
			}
			final char ch=sourceText.charAt(index++);
			if (Segment.isWhiteSpace(ch)) {
				lastWasWhiteSpace=true;
			} else {
				if (lastWasWhiteSpace) {
					writer.write(' ');
					lastWasWhiteSpace=false;
				}
				writer.write(ch);
			}
		}
		if (lastWasWhiteSpace) writer.write(' ');
	}

	private void writeContentPreformatted(final int end, final int depth) throws IOException {
		// sets index to end
		updateNextTag();
		do {
			while (nextTag!=null && index==nextTag.begin) {
				writeTag(nextTag,depth,end);
				if (index==end) return;
			}
			writer.write(sourceText.charAt(index));
		} while (++index<end);
	}

	private void writeTag(final Tag tag, final int depth, final int end) throws IOException {
		// sets index to last position written, guaranteed < end
		// assert index==tag.begin
		nextTag=tag.findNextTag();
		final int tagEnd=(end>tag.end) ? tag.end : end;
		if (tag.getTagType()==StartTagType.COMMENT || tag.getTagType()==StartTagType.CDATA_SECTION) {
			writeTextPreserveIndenting(tagEnd,depth);
		} else if (tidyTags) {
			final String tidyTag=tag.tidy();
			if ((tag instanceof StartTag) && ((StartTag)tag).getAttributes()!=null)
				writer.write(tidyTag);
			else
				writeSpecifiedTextInline(tidyTag,depth);
			index=tagEnd;
		} else {
			writeTextInline(tagEnd,depth,true,false);
		}
		if (end<=tag.end || !(tag instanceof StartTag)) return;
		if ((tag.name==HTMLElementName.SCRIPT && !indentScriptElements) || tag.getTagType().isServerTag()) {
			// this is a server start tag, we may need to write the whole server element:
			final Element element=tag.getElement();
			final EndTag endTag=element.getEndTag();
			if (endTag==null) return;
			final int contentEnd=(end<endTag.begin) ? end : endTag.begin;
			final boolean singleLineContent=writeTextPreserveIndenting(contentEnd,depth);
			//final boolean singleLineContent=writeTextInline(contentEnd,depth+1,false,false); // use this line instead of previous if indenting shouldn't be preserved in server elements.
			if (endTag.begin>=end) return;
			if (!singleLineContent) {
				writer.write('\n');
				writeIndent(depth);
			}
			// assert index==endTag.begin
			writeTag(endTag,depth,end);
		}
	}
	
  private void writeIndent(final int depth) throws IOException {
		for (int x=0; x<depth; x++) writer.write(indentText);
  }

	private boolean containsNonInlineLevelChildElements(final Element element) {
		// returns true if the element contains any non-inline-level elements or SCRIPT elements.
		final Collection childElements=element.getChildElements();
		if (childElements==Collections.EMPTY_LIST) return false;
		for (final Iterator i=childElements.iterator(); i.hasNext();) {
			final Element childElement=(Element)i.next();
			final String elementName=childElement.getName();
			if (elementName==HTMLElementName.SCRIPT || !HTMLElements.getInlineLevelElementNames().contains(elementName)) return true;
			if (containsNonInlineLevelChildElements(childElement)) return true;
		}
		return false;
	}

	public String toString() {
		return CharStreamSourceUtil.toString(this);
	}
}
