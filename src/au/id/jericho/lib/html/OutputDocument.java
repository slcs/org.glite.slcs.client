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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents a modified version of an original {@link Source} document.
 * <p>
 * An <code>OutputDocument</code> represents an original source document that
 * has been modified by substituting segments of it with other text.
 * Each of these substitutions must be registered in the output document,
 * which is most commonly done using the various <code>replace</code>, <code>remove</code> or <code>insert</code> methods in this class.
 * These methods internally {@linkplain #register(OutputSegment) register} one or more {@link OutputSegment} objects to define each substitution.
 * 
 * After all of the substitutions have been registered, the modified text can be retrieved using the
 * {@link #writeTo(Writer)} or {@link #toString()} methods.
 * <p>
 * The registered {@linkplain OutputSegment output segments} must not overlap each other, but may be adjacent.
 * Multiple output segments may be added at the same {@linkplain OutputSegment#getBegin() begin} position provided that they are all
 * zero-length, with the exception of one segment which may {@linkplain OutputSegment#getEnd() end} at a different position.
 * <p>
 * For efficiency reasons, violations of the above rules on overlapping segments do not throw an exception when the segment is registered,
 * but an {@link OverlappingOutputSegmentsException} is thrown when the {@linkplain #writeTo(Writer) output is generated}.
 * <p>
 * The following example converts all externally referenced style sheets to internal style sheets:
 * <p>
 * <pre>
 *  URL sourceUrl=new URL(sourceUrlString);
 *  String htmlText=Util.getString(new InputStreamReader(sourceUrl.openStream()));
 *  Source source=new Source(htmlText);
 *  OutputDocument outputDocument=new OutputDocument(source);
 *  StringBuffer sb=new StringBuffer();
 *  List linkStartTags=source.findAllStartTags(Tag.LINK);
 *  for (Iterator i=linkStartTags.iterator(); i.hasNext();) {
 *    StartTag startTag=(StartTag)i.next();
 *    Attributes attributes=startTag.getAttributes();
 *    String rel=attributes.getValue("rel");
 *    if (!"stylesheet".equalsIgnoreCase(rel)) continue;
 *    String href=attributes.getValue("href");
 *    if (href==null) continue;
 *    String styleSheetContent;
 *    try {
 *      styleSheetContent=Util.getString(new InputStreamReader(new URL(sourceUrl,href).openStream()));
 *    } catch (Exception ex) {
 *      continue; // don't convert if URL is invalid
 *    }
 *    sb.setLength(0);
 *    sb.append("&lt;style");
 *    Attribute typeAttribute=attributes.get("type");
 *    if (typeAttribute!=null) sb.append(' ').append(typeAttribute);
 *    sb.append("&gt;\n").append(styleSheetContent).append("\n&lt;/style&gt;");
 *    outputDocument.replace(startTag,sb);
 *  }
 *  String convertedHtmlText=outputDocument.toString();
 * </pre>
 *
 * @see OutputSegment
 * @see StringOutputSegment
 */
public final class OutputDocument implements CharStreamSource {
	private CharSequence sourceText;
	private ArrayList outputSegments=new ArrayList();

	/**
	 * Constructs a new output document based on the specified source document.
	 * @param source  the source document.
	 */
	public OutputDocument(final Source source) {
	  if (source==null) throw new IllegalArgumentException("source argument must not be null");
		this.sourceText=source;
	}

	OutputDocument(final ParseText parseText) {
		this.sourceText=parseText;
	}

	/**
	 * Returns the original source text upon which this output document is based.
	 * @return the original source text upon which this output document is based.
	 */
	public CharSequence getSourceText() {
		return sourceText;
	}

	/**
	 * Removes the specified {@linkplain Segment segment} from this output document.
	 * <p>
	 * This is equivalent to {@link #replace(Segment,CharSequence) replace}<code>(segment,null)</code>.
	 *
	 * @param segment  the segment to remove.
	 */
	public void remove(final Segment segment) {
		replace(segment,(CharSequence)null);
	}

	/**
	 * Removes all the segments from this output document represented by the specified source {@linkplain Segment} objects.
	 * <p>
	 * This is equivalent to the following code:<pre>
	 *  for (Iterator i=segments.iterator(); i.hasNext();)
	 *    {@link #remove(Segment) remove}((Segment)i.next());</pre>
	 *
	 * @param segments  a collection of segments to remove, represented by source {@link Segment} objects.
	 */
	public void remove(final Collection segments) {
		for (Iterator i=segments.iterator(); i.hasNext();) remove((Segment)i.next());
	}

	/**
	 * Inserts the specified text at the specified character position in this output document.
	 * @param pos  the character position at which to insert the text.
	 * @param text  the replacement text.
	 */
	public void insert(final int pos, final CharSequence text) {
		register(new StringOutputSegment(pos,pos,text));
	}

	/**
	 * Replaces the specified {@linkplain Segment segment} in this output document with the specified text.
	 * <p>
	 * Specifying a <code>null</code> argument to the <code>text</code> parameter is exactly equivalent to specifying an empty string,
	 * and results in the segment being completely removed from the output document.
	 *
	 * @param segment  the segment to replace.
	 * @param text  the replacement text, or <code>null</code> to remove the segment.
	 */
	public void replace(final Segment segment, final CharSequence text) {
		replace(segment.getBegin(),segment.getEnd(),text);
	}

	/**
	 * Replaces the specified segment of this output document with the specified text.
	 * <p>
	 * Specifying a <code>null</code> argument to the <code>text</code> parameter is exactly equivalent to specifying an empty string,
	 * and results in the segment being completely removed from the output document.
	 *
	 * @param begin  the character position at which to begin the replacement.
	 * @param end  the character position at which to end the replacement.
	 * @param text  the replacement text, or <code>null</code> to remove the segment.
	 */
	public void replace(final int begin, final int end, final CharSequence text) {
		register(new StringOutputSegment(begin,end,text));
	}

	/**
	 * Replaces the specified segment of this output document with the specified character.
	 *
	 * @param begin  the character position at which to begin the replacement.
	 * @param end  the character position at which to end the replacement.
	 * @param ch  the replacement character.
	 */
	public void replace(final int begin, final int end, final char ch) {
		register(new CharOutputSegment(begin,end,ch));
	}

	/**
	 * Replaces the specified {@link FormControl} in this output document.
	 * <p>
	 * The effect of this method is to {@linkplain #register(OutputSegment) register} zero or more
	 * {@linkplain OutputSegment output segments} in the output document as required to reflect
	 * previous modifications to the control's state.
	 * The state of a control includes its <a href="FormControl.html#SubmissionValue">submission value</a>,
	 * {@linkplain FormControl#setOutputStyle(FormControlOutputStyle) output style}, and whether it has been
	 * {@linkplain FormControl#setDisabled(boolean) disabled}.
	 * <p>
	 * The state of the form control should not be modified after this method is called, as there is no guarantee that
	 * subsequent changes either will or will not be reflected in the final output.
	 * A second call to this method with the same parameter is not allowed.
	 * It is therefore recommended to call this method as the last action before the output is generated.
	 * <p>
	 * Although the specifics of the number and nature of the output segments added in any particular circumstance
	 * is not defined in the specification, it can generally be assumed that only the minimum changes necessary
	 * are made to the original document.  If the state of the control has not been modified, calling this method
	 * has no effect at all.
	 *
	 * @param formControl  the form control to replace.
	 * @see #replace(FormFields)
	 */
	public void replace(final FormControl formControl) {
		formControl.replaceInOutputDocument(this);
	}

	/**
	 * {@linkplain #replace(FormControl) Replaces} all the constituent {@linkplain FormControl form controls}
	 * from the specified {@link FormFields} in this output document.
	 * <p>
	 * This is equivalent to the following code:
	 * <pre>for (Iterator i=formFields.{@link FormFields#getFormControls() getFormControls()}.iterator(); i.hasNext();)
	 *   {@link #replace(FormControl) replace}((FormControl)i.next());</pre>
	 * <p>
	 * The state of any of the form controls in the specified form fields should not be modified after this method is called,
	 * as there is no guarantee that subsequent changes either will or will not be reflected in the final output.
	 * A second call to this method with the same parameter is not allowed.
	 * It is therefore recommended to call this method as the last action before the output is generated.
	 *
	 * @param formFields  the form fields to replace.
	 * @see #replace(FormControl)
	 */
	public void replace(final FormFields formFields) {
		formFields.replaceInOutputDocument(this);
	}

	/**
	 * Replaces the specified {@link Attributes} segment in this output document with the name/value entries
	 * in the returned <code>Map</code>.
	 * The returned map initially contains entries representing the attributes from the source document,
	 * which can be modified before output.
	 * <p>
	 * The documentation of the {@link #replace(Attributes,Map)} method contains more information about the requirements
	 * of the map entries.
	 * <p>
	 * Specifying a value of <code>true</code> as an argument to the <code>convertNamesToLowerCase</code> parameter
	 * causes all original attribute names to be converted to lower case in the map.
	 * This simplifies the process of finding/updating specific attributes since map keys are case sensitive.
	 * <p>
	 * Attribute values are automatically {@linkplain CharacterReference#decode(CharSequence) decoded} before
	 * being loaded into the map.
	 * <p>
	 * This method is logically equivalent to:<br />
	 * {@link #replace(Attributes,Map) replace}<code>(attributes, attributes.</code>{@link Attributes#populateMap(Map,boolean) populateMap(new LinkedHashMap(),convertNamesToLowerCase)}<code>)</code>
	 * <p>
	 * The use of <code>LinkedHashMap</code> to implement the map ensures (probably unnecessarily) that
	 * existing attributes are output in the same order as they appear in the source document, and new
	 * attributes are output in the same order as they are added.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *  <dd><pre>
	 *  Source source=new Source(htmlDocument);
	 *  Attributes bodyAttributes
	 *    =source.findNextStartTag(0,Tag.BODY).getAttributes();
	 *  OutputDocument outputDocument=new OutputDocument(source);
	 *  Map attributesMap=outputDocument.replace(bodyAttributes,true);
	 *  attributesMap.put("bgcolor","green");
	 *  String htmlDocumentWithGreenBackground=outputDocument.toString();</pre></dl>
	 *
	 * @param attributes  the <code>Attributes</code> segment defining the span of the segment and initial name/value entries of the returned map.
	 * @param convertNamesToLowerCase  specifies whether all attribute names are converted to lower case in the map.
	 * @return a <code>Map</code> containing the name/value entries to be output.
	 * @see #replace(Attributes,Map)
	 */
	public Map replace(final Attributes attributes, boolean convertNamesToLowerCase) {
		AttributesOutputSegment attributesOutputSegment=new AttributesOutputSegment(attributes,convertNamesToLowerCase);
		register(attributesOutputSegment);
		return attributesOutputSegment.getMap();
	}

	/**
	 * Replaces the specified attributes segment in this source document with the name/value entries in the specified <code>Map</code>.
	 * <p>
	 * This method might be used if the <code>Map</code> containing the new attribute values
	 * should not be preloaded with the same entries as the source attributes, or a map implementation
	 * other than <code>LinkedHashMap</code> is required.
	 * Otherwise, the {@link #replace(Attributes, boolean convertNamesToLowerCase)} method is generally more useful.
	 * <p>
	 * Keys in the map must be <code>String</code> objects, and values must implement the <code>CharSequence</code> interface.
	 * <p>
	 * An attribute with no value is represented by a map entry with a <code>null</code> value.
	 * <p>
	 * Attribute values are stored unencoded in the map, and are automatically
	 * {@linkplain CharacterReference#encode(CharSequence) encoded} if necessary during output.
	 * <p>
	 * The use of invalid characters in attribute names results in unspecified behaviour.
	 * <p>
	 * Note that methods in the <code>Attributes</code> class treat attribute names as case insensitive,
	 * whereas the <code>Map</code> treats them as case sensitive.
	 *
	 * @param attributes  the <code>Attributes</code> object defining the span of the segment to replace.
	 * @param map  the <code>Map</code> containing the name/value entries.
	 * @see #replace(Attributes, boolean convertNamesToLowerCase)
	 */
	public void replace(final Attributes attributes, final Map map) {
		register(new AttributesOutputSegment(attributes,map));
	}

	/**
	 * Replaces the specified segment of this output document with a string of spaces of the same length.
	 * <p>
	 * This method is used internally to implement the functionality available through the
	 * {@link Segment#ignoreWhenParsing()} method.
	 * It is included in the public API in the unlikely event it has other practical uses
	 * for the developer.
	 * To remove a segment from the output document completely, use the {@link #remove(Segment)} method instead.
	 *
	 * @param begin  the character position at which to begin the replacement.
	 * @param end  the character position at which to end the replacement.
	 */
	public void replaceWithSpaces(final int begin, final int end) {
		register(new BlankOutputSegment(begin,end));
	}

	/**
	 * Registers the specified {@linkplain OutputSegment output segment} in this output document.
	 * <p>
	 * Use this method if you want to use a customised {@link OutputSegment} class.
	 *
	 * @param outputSegment  the output segment to register.
	 */
	public void register(final OutputSegment outputSegment) {
		outputSegments.add(outputSegment);
	}

	/**
	 * Writes the final content of this output document to the specified <code>Writer</code>.
	 * <p>
	 * An {@link OverlappingOutputSegmentsException} is thrown if any of the output segments overlap.
	 * For efficiency reasons this condition is not caught when the offending output segment is {@linkplain #add(OutputSegment) added}.
	 * <p>
	 * If the output is required in the form of a <code>Reader</code>, use {@link CharStreamSourceUtil#getReader(CharStreamSource) CharStreamSourceUtil.getReader(this)} instead.
	 *
	 * @param writer  the destination <code>java.io.Writer</code> for the output.
	 * @throws IOException if an I/O exception occurs.
	 * @throws OverlappingOutputSegmentsException if any of the output segments overlap.
	 * @see #toString()
	 */
	public void writeTo(final Writer writer) throws IOException {
		if (outputSegments.isEmpty()) {
			Util.appendTo(writer,sourceText);
			return;
		}
		int pos=0;
		Collections.sort(outputSegments,OutputSegment.COMPARATOR);
		OutputSegment lastOutputSegment=null;
		for (final Iterator i=outputSegments.iterator(); i.hasNext();) {
			final OutputSegment outputSegment=(OutputSegment)i.next();
			if (outputSegment==lastOutputSegment) continue; // silently ignore duplicate output segment
			if (outputSegment.getBegin()<pos) throw new OverlappingOutputSegmentsException(lastOutputSegment,outputSegment);
			if (outputSegment.getBegin()>pos) Util.appendTo(writer,sourceText,pos,outputSegment.getBegin());
			outputSegment.writeTo(writer);
			lastOutputSegment=outputSegment;
			pos=outputSegment.getEnd();
		}
		if (pos<sourceText.length()) Util.appendTo(writer,sourceText,pos,sourceText.length());
		writer.close();
	}

	public long getEstimatedMaximumOutputLength() {
		long estimatedMaximumOutputLength=sourceText.length();
		for (final Iterator i=outputSegments.iterator(); i.hasNext();) {
			final OutputSegment outputSegment=(OutputSegment)i.next();
			final int outputSegmentOriginalLength=outputSegment.getEnd()-outputSegment.getBegin();
			estimatedMaximumOutputLength+=(outputSegment.getEstimatedMaximumOutputLength()-outputSegmentOriginalLength);
		}
		return estimatedMaximumOutputLength;
	}

	/**
	 * Returns the final content of this output document as a <code>String</code>.
	 * @return the final content of this output document as a <code>String</code>.
	 * @throws OverlappingOutputSegmentsException if any of the output segments overlap.
	 * @see #writeTo(Writer)
	 */
	public String toString() {
		return CharStreamSourceUtil.toString(this);
	}

	/**
	 * Constructs a new output document based on the specified source text.
	 * <p>
 	 * This constructor has been deprecated as of version 2.2 in favour of the {@link #OutputDocument(Source)} method
 	 * as most of the methods in this class assume that the argument supplied to this constructor is the entire source document.
	 *
	 * @param sourceText  the source text.
	 * @deprecated  Use the {@link #OutputDocument(Source)} constructor instead.
	 */
	public OutputDocument(final CharSequence sourceText) {
	  if (sourceText==null) throw new IllegalArgumentException("sourceText argument must not be null");
		this.sourceText=sourceText;
	}

	/**
	 * Registers the specified {@linkplain OutputSegment output segment} in this output document.
	 * <p>
 	 * This method has been deprecated as of version 2.2 in favour of the identical {@link #register(OutputSegment)} method
 	 * in an effort to make this class and its methods more intuitive.
	 *
	 * @param outputSegment  the output segment to register.
	 * @deprecated  Use the {@link #register(OutputSegment)} method instead.
	 */
	public void add(final OutputSegment outputSegment) {
		register(outputSegment);
	}

	/**
	 * Replaces the specified {@link FormControl} in this output document.
	 * <p>
 	 * This method has been deprecated as of version 2.2 in favour of the identical {@link #replace(FormControl)} method
 	 * in an effort to make this class and its methods more intuitive.
	 *
	 * @param formControl  the form control to replace.
	 * @deprecated  Use the {@link #replace(FormControl)} method instead.
	 */
	public void add(final FormControl formControl) {
		replace(formControl);
	}

	/**
	 * {@linkplain #replace(FormControl) Replaces} all the constituent {@linkplain FormControl form controls}
	 * from the specified {@link FormFields} in this output document.
	 * <p>
 	 * This method has been deprecated as of version 2.2 in favour of the identical {@link #replace(FormFields)} method
 	 * in an effort to make this class and its methods more intuitive.
	 *
	 * @param formFields  the form fields to replace.
	 * @deprecated  Use the {@link #replace(FormFields)} method instead.
	 */
	public void add(final FormFields formFields) {
		formFields.replaceInOutputDocument(this);
	}

	/**
	 * Outputs the final content of this output document to the specified <code>Writer</code>.
	 * <p>
	 * This method has been deprecated as of version 2.2 in favour of the identical {@link #writeTo(Writer)} method in order for this class to implement {@link CharStreamSource}.
	 *
	 * @param writer  the destination <code>java.io.Writer</code> for the output.
	 * @throws IOException if an I/O exception occurs.
	 * @throws OverlappingOutputSegmentsException if any of the output segments overlap.
	 * @deprecated  Use the {@link #writeTo(Writer)} method instead.
	 */
	public void output(final Writer writer) throws IOException {
		writeTo(writer);
	}
	
	/**
	 * Returns a <code>Reader</code> that reads the final content of this output document.
	 * <p>
	 * This method has been deprecated as of version 2.2 in favour of calling the {@link CharStreamSourceUtil#getReader(CharStreamSource)} method,
	 * passing this object as the argument.
	 *
	 * @return a <code>Reader</code> that reads the final content of this output document.
	 * @throws OverlappingOutputSegmentsException if any of the output segments overlap.
	 * @deprecated  Use {@link CharStreamSourceUtil#getReader(CharStreamSource) CharStreamSourceUtil.getReader(this)} instead.
	 */
	public Reader getReader() {
		return CharStreamSourceUtil.getReader(this);
	}
}
