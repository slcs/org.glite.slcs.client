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

import java.util.Arrays;
import java.util.List;

/**
 * An enumerated type representing the three major output styles of a {@linkplain FormControl form control's}
 * <a href="FormControl.html#OutputElement">output element</a>.
 * <p>
 * A form control's output style is set using the {@link FormControl#setOutputStyle(FormControlOutputStyle)} method.
 */
public final class FormControlOutputStyle {
	private String description;

	/**
	 * Normal display of the <a href="FormControl.html#OutputElement">output element</a>.
	 * <p>
	 * This is the default display style.
	 */
	public static final FormControlOutputStyle NORMAL=new FormControlOutputStyle("NORMAL");

	/**
	 * Remove the <a href="FormControl.html#OutputElement">output element</a> from the {@linkplain OutputDocument output document} completely.
	 */
	public static final FormControlOutputStyle REMOVE=new FormControlOutputStyle("REMOVE");

	/**
	 * The {@linkplain #NORMAL normal} <a href="FormControl.html#OutputElement">output element</a> is replaced with a simple representation
	 * of the {@linkplain FormControl form control's} <a href="FormControl.html#SubmissionValue">submission value(s)</a>.
	 * <p>
	 * The implementation of this functionality is highly subjective, but provides a more aesthetic way of displaying a read-only version
	 * of a form without having to resort to using {@linkplain FormControl#isDisabled() disabled} controls.
	 * <p>
	 * The representation is dependent on the {@linkplain FormControlType form control type}, and can be configured using the
	 * static properties of the {@link ConfigDisplayValue ConfigDisplayValue} nested class.
	 * <p>
	 * Unless specified otherwise below, the {@linkplain #NORMAL normal} <a href="FormControl.html#OutputElement">output element</a> is 
	 * replaced with a <i><a name="DisplayValueElement">display value element</a></i> having the {@linkplain Element#getName() name}
	 * specified in the static {@link ConfigDisplayValue#ElementName ConfigDisplayValue.ElementName} property
	 * (<code>div</code> by default).
	 * The attributes specified in the static {@link ConfigDisplayValue#AttributeNames ConfigDisplayValue.AttributeNames} list
	 * (<code>id</code>, <code>class</code> and <code>style</code> by default) are copied from
	 * the {@linkplain #NORMAL normal} <a href="FormControl.html#OutputElement">output element</a> into the
	 * <a href="#DisplayValueElement">display value element</a>.
	 * <p>
	 * Details of the content of the <a href="#DisplayValueElement">display value element</a> or other representation of the
	 * control value are as follows:
	 * <dl>
	 *  <dt>{@link FormControlType#TEXT TEXT}, {@link FormControlType#FILE FILE}
	 *   <dd>The content of the <a href="#DisplayValueElement">display value element</a> is the
	 *    {@linkplain CharacterReference#reencode(CharSequence) re-encoded} value of the
	 *    {@linkplain #NORMAL normal} <a href="FormControl.html#OutputElement">output element's</a> <code>value</code> attribute.
	 *  <dt>{@link FormControlType#TEXTAREA TEXTAREA}
	 *   <dd>The content of the <a href="#DisplayValueElement">display value element</a> is the content of the <code>TEXTAREA</code> element
	 *    re-encoded {@linkplain CharacterReference#encodeWithWhiteSpaceFormatting(CharSequence) with white space formatting}.
	 *  <dt>{@link FormControlType#CHECKBOX CHECKBOX}, {@link FormControlType#RADIO RADIO}
	 *   <dd>The {@linkplain #NORMAL normal} <a href="FormControl.html#OutputElement">output element</a> is replaced with the
	 *    un-encoded content specified in the {@link ConfigDisplayValue#CheckedHTML ConfigDisplayValue.CheckedHTML} or
	 *    {@link ConfigDisplayValue#UncheckedHTML ConfigDisplayValue.UncheckedHTML} static property, depending on
	 *    whether the {@linkplain #NORMAL normal} <a href="FormControl.html#OutputElement">output element</a> contains a
	 *    <code>checked</code> attribute.
	 *    If the relevant static property has a value of <code>null</code> (the default), the 
	 *    <a href="FormControl.html#OutputElement">output element</a> is simply a {@linkplain FormControl#setDisabled(boolean) disabled}
	 *    version of the form control.
	 *    Attempting to determine which labels might apply to which checkbox or radio button, allowing only the
	 *    selected controls to be displayed, would require a very complex and inexact algorithm, so is best left to the developer
	 *    to implement if required.
	 *  <dt>{@link FormControlType#SELECT_SINGLE SELECT_SINGLE}, {@link FormControlType#SELECT_MULTIPLE SELECT_MULTIPLE}
	 *   <dd>The content of the <a href="#DisplayValueElement">display value element</a> is the
	 *    {@linkplain CharacterReference#reencode(CharSequence) re-encoded} label of the currently selected option.
	 *    In the case of a {@link FormControlType#SELECT_MULTIPLE SELECT_MULTIPLE} control, all labels of selected options
	 *    are listed, separated by the text specified in the static
	 *    {@link ConfigDisplayValue#MultipleValueSeparator ConfigDisplayValue.MultipleValueSeparator} property
	 *    ("<code>, </code>" by default).
	 *  <dt>{@link FormControlType#PASSWORD PASSWORD}
	 *   <dd>The content of the <a href="#DisplayValueElement">display value element</a> is the
	 *    {@linkplain CharacterReference#encode(CharSequence) encoded} character specified in the
	 *    {@link ConfigDisplayValue#PasswordChar ConfigDisplayValue.PasswordChar} static property ('<code>*</code>' by default),
	 *    repeated <i>n</i> times, where <i>n</i> is the number of characters in the control's
	 *    <a href="FormControl.html#SubmissionValue">submission value</a>.
	 *  <dt>{@link FormControlType#HIDDEN HIDDEN}
	 *   <dd>The <a href="FormControl.html#OutputElement">output element</a> is {@linkplain #REMOVE removed} completely.
	 *  <dt>{@link FormControlType#BUTTON BUTTON}, {@link FormControlType#SUBMIT SUBMIT}, {@link FormControlType#IMAGE IMAGE}
	 *   <dd>The <a href="FormControl.html#OutputElement">output element</a>
	 *    is a {@linkplain FormControl#setDisabled(boolean) disabled} version of the original form control.
	 * </dl>
	 * <p>
	 * If the <a href="FormControl.html#SubmissionValue">submission value</a> of the control is <code>null</code> or an empty string,
	 * the <a href="#DisplayValueElement">display value element</a> is given the un-encoded content specified in the
	 * {@link ConfigDisplayValue#EmptyHTML ConfigDisplayValue.EmptyHTML} static property.
	 */
	public static final FormControlOutputStyle DISPLAY_VALUE=new FormControlOutputStyle("DISPLAY_VALUE");

	private FormControlOutputStyle(final String description) {
		this.description=description;
	}

	/**
	 * Returns a string representation of this object useful for debugging purposes.
	 * @return a string representation of this object useful for debugging purposes.
	 */
	public String getDebugInfo() {
		return description;
	}

	/**
	 * Returns a string representation of this object useful for debugging purposes.
	 * <p>
	 * This is equivalent to {@link #getDebugInfo()}.
	 *
	 * @return a string representation of this object useful for debugging purposes.
	 */
	public String toString() {
		return getDebugInfo();
	}

	/**
	 * Contains static properties that configure the {@link #DISPLAY_VALUE} form control output style.
	 * <p>
	 * None of the properties should be assigned a <code>null</code> value.
	 * <p>
	 * See the documentation of the {@link #DISPLAY_VALUE} output style for details on how these properties are used.
	 */
	public static final class ConfigDisplayValue {

		/**
		 * Defines the text that is used to separate multiple values in a 
		 * <a href="FormControlOutputStyle.html#DisplayValueElement">display value element</a>.
		 * <p>
		 * This property is only relevant to {@link FormControlType#SELECT_MULTIPLE SELECT_MULTIPLE} form controls, and is only used
		 * if multiple items in the control are selected.
		 * <p>
		 * The default value is "<code>, </code>".
		 */
		public static volatile String MultipleValueSeparator=", ";

		/**
		 * Defines the {@linkplain Element#getName() name} of
		 * <a href="FormControlOutputStyle.html#DisplayValueElement">display value elements</a>.
		 * <p>
		 * The default value is "<code>div</code>".
		 * <p>
		 * Although all form control {@linkplain FormControl#getElement() elements} are
		 * {@linkplain HTMLElements#getInlineLevelElementNames() inline-level} elements, the default replacement is the
		 * {@linkplain HTMLElements#getBlockLevelElementNames() block-level} {@link HTMLElementName#DIV DIV} element, which allows
		 * richer stylesheet formatting than the most common alternative, the {@link HTMLElementName#SPAN SPAN} element, 
		 * such as the ability to set its <code>width</code> and <code>height</code>.
		 * <p>
		 * This has the undesired effect in some cases of displaying the value on a new line, whereas the original form control
		 * was not on a new line.  In practical use however, many form controls are placed inside table cells for better control
		 * over their positioning.  In this case replacing the original inline form control with the block <code>DIV</code>
		 * element does not alter its position.
		 */
		public static volatile String ElementName=HTMLElementName.DIV;

		/**
		 * Defines the names of the {@linkplain Attributes attributes} that are copied from the normal form control
		 * <a href="FormControl.html#OutputElement">output element</a> to a
		 * <a href="FormControlOutputStyle.html#DisplayValueElement">display value element</a>.
		 * <p>
		 * The names included in the list by default are "<code>id</code>", "<code>class</code>" and "<code>style</code>".
		 * <p>
		 * These attributes are usually all that is needed to identify the elements in style sheets or specify the styles directly.
		 * <p>
		 * The default list is modifiable.
		 */
		public static volatile List<String> AttributeNames= Arrays.asList(new String[] {Attribute.ID,Attribute.CLASS,Attribute.STYLE});

		/**
		 * Defines the content of a <a href="FormControlOutputStyle.html#DisplayValueElement">display value element</a>
		 * if the <a href="FormControl.html#SubmissionValue">submission value</a> of the control is <code>null</code> or an empty string.
		 * <p>
		 * The content is not {@linkplain CharacterReference#encode(CharSequence) encoded} before output.
		 * <p>
		 * The default content is "<code>&amp;nbsp;</code>".
		 */
		public static volatile String EmptyHTML="&nbsp;";
		
		/**
		 * Defines the character used to represent the value of a {@link FormControlType#PASSWORD PASSWORD} form control
		 * in a <a href="#DisplayValueElement">display value element</a>.
		 * <p>
		 * The character is repeated <i>n</i> times, where <i>n</i> is the number of characters in the control's
		 * <a href="FormControl.html#SubmissionValue">submission value</a>.
		 * <p>
		 * The resulting string is {@linkplain CharacterReference#encode(CharSequence) encoded} before output.
		 * <p>
		 * The default password character is '<code>*</code>'.
		 */
		public static volatile char PasswordChar='*';
		
		/**
		 * Defines the HTML which replaces the {@linkplain #NORMAL normal} <a href="FormControl.html#OutputElement">output element</a>
		 * of a {@link FormControlType#CHECKBOX CHECKBOX} or {@link FormControlType#RADIO RADIO} form control if it contains a
		 * <code>checked</code> attribute.
		 * <p>
		 * If this property is <code>null</code>, the <a href="FormControl.html#OutputElement">output element</a> is simply a 
		 * {@linkplain FormControl#setDisabled(boolean) disabled} version of the form control.
		 * <p>	
		 * The HTML is not {@linkplain CharacterReference#encode(CharSequence) encoded} before output.
		 * <p>
		 * The default value is <code>null</code>.
		 */
		public static volatile String CheckedHTML=null;

		/**
		 * Defines the HTML which replaces the {@linkplain #NORMAL normal} <a href="FormControl.html#OutputElement">output element</a>
		 * of a {@link FormControlType#CHECKBOX CHECKBOX} or {@link FormControlType#RADIO RADIO} form control if it does not contain a
		 * <code>checked</code> attribute.
		 * <p>
		 * If this property is <code>null</code>, the <a href="FormControl.html#OutputElement">output element</a> is simply a 
		 * {@linkplain FormControl#setDisabled(boolean) disabled} version of the form control.
		 * <p>	
		 * The HTML is not {@linkplain CharacterReference#encode(CharSequence) encoded} before output.
		 * <p>
		 * The default value is <code>null</code>.
		 */
		public static volatile String UncheckedHTML=null;

		private ConfigDisplayValue() {}
	}
}
