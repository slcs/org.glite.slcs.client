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

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Represents an HTML form <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#form-controls">control</a>.
 * <p>
 * A <code>FormControl</code> consists of a single {@linkplain #getElement() element}
 * that matches one of the {@linkplain FormControlType form control types}.
 * <p>
 * The term <i><a name="OutputElement">output element</a></i> is used to describe the element that is
 * {@linkplain OutputSegment#writeTo(Writer) output} if this form control is {@linkplain OutputDocument#replace(FormControl) replaced}
 * in an {@link OutputDocument}.
 * <p>
 * A <i><a name="PredefinedValueControl">predefined value control</a></i> is a form control for which
 * {@link #getFormControlType()}.{@link FormControlType#hasPredefinedValue() hasPredefinedValue()}
 * returns <code>true</code>.  It has a {@linkplain #getFormControlType() control type} of
 * {@link FormControlType#CHECKBOX CHECKBOX}, {@link FormControlType#RADIO RADIO}, {@link FormControlType#BUTTON BUTTON},
 * {@link FormControlType#SUBMIT SUBMIT}, {@link FormControlType#IMAGE IMAGE}, {@link FormControlType#SELECT_SINGLE SELECT_SINGLE}
 * or {@link FormControlType#SELECT_MULTIPLE SELECT_MULTIPLE}.
 * <p>
 * A <i><a name="UserValueControl">user value control</a></i> is a form control for which
 * {@link #getFormControlType()}.{@link FormControlType#hasPredefinedValue() hasPredefinedValue()}
 * returns <code>false</code>.  It has a {@linkplain #getFormControlType() control type} of
 * {@link FormControlType#FILE FILE}, {@link FormControlType#HIDDEN HIDDEN}, {@link FormControlType#PASSWORD PASSWORD},
 * {@link FormControlType#TEXT TEXT} or {@link FormControlType#TEXTAREA TEXTAREA}.
 * <p>
 * The functionality of most significance to users of this class relates to the
 * <i><a name="DisplayCharacteristics">display characteristics</a></i> of the <a href="#OutputElement">output element</a>,
 * manipulated using the {@link #setDisabled(boolean)} and {@link #setOutputStyle(FormControlOutputStyle)} methods.
 * <p>
 * As a general rule, the operations dealing with the control's <a href="#SubmissionValue">submission values</a>
 * are better performed on a {@link FormFields} or {@link FormField} object, which provide a more
 * intuitive interface by grouping form controls of the same {@linkplain #getName() name} together.
 * The higher abstraction level of these classes means they can automatically ensure that the
 * <a href="#SubmissionValue">submission values</a> of their constituent controls are consistent with each other,
 * for example by ensuring that only one {@link FormControlType#RADIO RADIO} control with a given name is
 * {@link #isChecked() checked} at a time.
 * <p>
 * A {@link FormFields} object can be directly {@linkplain FormFields#FormFields(Collection) constructed} from
 * a collection of <code>FormControl</code> objects.
 * <p>
 * <code>FormControl</code> instances are obtained using the {@link Element#getFormControl()} method or are created automatically
 * with the creation of a {@link FormFields} object via the {@link Segment#findFormFields()} method.
 *
 * @see FormControlType
 * @see FormFields
 * @see FormField
 */
public abstract class FormControl extends Segment {
	FormControlType formControlType;
	String name;
	ElementContainer elementContainer;
	FormControlOutputStyle outputStyle=FormControlOutputStyle.NORMAL;

	private static final String CHECKBOX_NULL_DEFAULT_VALUE="on";
	private static Comparator<FormControl> COMPARATOR=new PositionComparator();

	static FormControl construct(final Element element) {
		final String tagName=element.getStartTag().getName();
		if (tagName==Tag.INPUT) {
			final String typeAttributeValue=element.getAttributes().getRawValue(Attribute.TYPE);
			if (typeAttributeValue==null) return new InputFormControl(element,FormControlType.TEXT);
			FormControlType formControlType=FormControlType.getFromInputElementType(typeAttributeValue.toLowerCase());
			if (formControlType==null) {
				if (element.source.isLoggingEnabled()) element.source.log(element.source.getRowColumnVector(element.begin).appendTo(new StringBuffer(200)).append(": INPUT control with unrecognised type \"").append(typeAttributeValue).append("\" assumed to be type \"text\"").toString());
				formControlType=FormControlType.TEXT;
			}
			if (formControlType==FormControlType.TEXT) return new InputFormControl(element,formControlType);
			if (formControlType==FormControlType.CHECKBOX || formControlType==FormControlType.RADIO) return new RadioCheckboxFormControl(element,formControlType);
			if (formControlType==FormControlType.SUBMIT) return new SubmitFormControl(element,formControlType);
			if (formControlType==FormControlType.IMAGE) return new ImageSubmitFormControl(element);
			// formControlType is HIDDEN || PASSWORD || FILE
			return new InputFormControl(element,formControlType);
		} else if (tagName==Tag.SELECT) {
			return new SelectFormControl(element);
		} else if (tagName==Tag.TEXTAREA) {
			return new TextAreaFormControl(element);
		} else if (tagName==Tag.BUTTON) {
			return element.getAttributes().getRawValue(Attribute.TYPE).equalsIgnoreCase("submit") ? new SubmitFormControl(element,FormControlType.BUTTON) : null;
		} else {
			return null;
		}
	}

	private FormControl(final Element element, final FormControlType formControlType, final boolean loadPredefinedValue) {
		super(element.source,element.begin,element.end);
		elementContainer=new ElementContainer(element,loadPredefinedValue);
		this.formControlType=formControlType;
		name=element.getAttributes().getValue(Attribute.NAME);
		verifyName();
	}

	/**
	 * Returns the {@linkplain FormControlType type} of this form control.
	 * @return the {@linkplain FormControlType type} of this form control.
	 */
	public final FormControlType getFormControlType() {
		return formControlType;
	}

	/**
	 * Returns the <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#control-name">name</a> of the control.
	 * <p>
	 * The name comes from the value of the <code>name</code> {@linkplain Attribute attribute} of the
	 * {@linkplain #getElement() form control's element}, not the {@linkplain Element#getName() name of the element} itself.
	 * <p>
	 * Since a {@link FormField} is simply a group of controls with the same name, the terms <i>control name</i> and
	 * <i>field name</i> are for the most part synonymous, with only a possible difference in case differentiating them.
	 * <p>
	 * In contrast to the {@link FormField#getName()} method, this method always returns the name using the original case
	 * from the source document, regardless of the current setting of the
	 * {@link Config#CurrentCompatibilityMode}<code>.</code>{@link Config.CompatibilityMode#isFormFieldNameCaseInsensitive() FormFieldNameCaseInsensitive} property.
	 *
	 * @return the <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#control-name">name</a> of the control.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Returns the {@linkplain Element element} representing this form control in the source document.
	 * <p>
	 * The {@linkplain Element#getAttributes() attributes} of this source element should correspond with the
	 * <a href="#OutputAttributes">output attributes</a> if the
	 * <a href="#DisplayCharacteristics">display characteristics</a> or <a href="FormField.html#SubmissionValue">submission values</a>
	 * have not been modified.
	 *
	 * @return the {@linkplain Element element} representing this form control in the source document.
	 */
	public final Element getElement() {
		return elementContainer.element;
	}

	/**
	 * Returns an iterator over the {@link Tag#OPTION OPTION} {@linkplain Element elements} contained within this control, in order of appearance.
	 * <p>
	 * This method is only relevant to form controls with a {@linkplain #getFormControlType() type} of 
	 * {@link FormControlType#SELECT_SINGLE SELECT_SINGLE} or {@link FormControlType#SELECT_MULTIPLE SELECT_MULTIPLE}.
	 *
	 * @return an iterator over the {@link Tag#OPTION OPTION} {@linkplain Element elements} contained within this control, in order of appearance.
	 * @throws UnsupportedOperationException if the {@linkplain #getFormControlType() type} of this control is not {@link FormControlType#SELECT_SINGLE SELECT_SINGLE} or {@link FormControlType#SELECT_MULTIPLE SELECT_MULTIPLE}.
	 */
	public Iterator<?> getOptionElementIterator() {
		// overridden in SelectFormControl
		throw new UnsupportedOperationException("Only SELECT controls contain OPTION elements");
	}

	/**
	 * Returns the current {@linkplain FormControlOutputStyle output style} of this form control.
	 * <p>
	 * This property affects how this form control is displayed if it has been {@linkplain OutputDocument#replace(FormControl) replaced}
	 * in an {@link OutputDocument}.
	 * See the documentation of the {@link FormControlOutputStyle} class for information on the available output styles.
	 * <p>
	 * The default output style for every form control is {@link FormControlOutputStyle#NORMAL}.
	 *
	 * @return the current {@linkplain FormControlOutputStyle output style} of this form control.
	 * @see #setOutputStyle(FormControlOutputStyle)
	 */
	public FormControlOutputStyle getOutputStyle() {
		return outputStyle;
	}

	/**
	 * Sets the {@linkplain FormControlOutputStyle output style} of this form control.
	 * <p>
	 * See the {@link #getOutputStyle()} method for a full description of this property.
	 *
	 * @param outputStyle  the new {@linkplain FormControlOutputStyle output style} of this form control.
	 */
	public void setOutputStyle(final FormControlOutputStyle outputStyle) {
		this.outputStyle=outputStyle;
	}

	/**
	 * Returns a map of the names and values of this form control's <a href="#OutputAttributes">output attributes</a>.
	 * <p>
	 * The term <i><a name="OutputAttributes">output attributes</a></i> is used in this library to refer to the
	 * <a target="_blank" href="http://www.w3.org/TR/html401/intro/sgmltut.html#h-3.2.2">attributes</a> of a form control's
	 * <a href="#OutputElement">output element</a>.
	 * <p>
	 * The map keys are the <code>String</code> attribute names, which should all be in lower case.
	 * The map values are the corresponding <code>CharSequence</code> attribute values, with a <code>null</code> value given
	 * to an attribute that {@linkplain Attribute#hasValue() has no value}.
	 * <p>
	 * Direct manipulation of the returned map affects the attributes of this form control's <a href="#OutputElement">output element</a>.
	 * It is the responsibility of the user to ensure that all entries added to the map use the correct key and value types,
	 * and that all keys (attribute names) are in lower case.
	 * <p>
	 * It is recommended that the <a href="#SubmissionValueModificationMethods">submission value modification methods</a>
	 * are used to modify attributes that affect the <a href="#SubmissionValue">submission value</a> of the control
	 * rather than manipulating the attributes map directly.
	 * <p>
	 * An iteration over the map entries will return the attributes in the same order as they appeared in the source document, or
	 * at the end if the attribute was not present in the source document.
	 * <p>
	 * The returned attributes only correspond with those of the {@linkplain #getElement() source element} if the control's
	 * <a href="#DisplayCharacteristics">display characteristics</a> and <a href="#SubmissionValue">submission values</a>
	 * have not been modified.
	 *
	 * @return a map of the names and values of this form control's <a href="#OutputAttributes">output attributes</a>.
	 */
	public final Map<String,CharSequence> getAttributesMap() {
		return elementContainer.getAttributesMap();
	}

	/**
	 * Indicates whether this form control is <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-disabled">disabled</a>.
	 * <p>
	 * The form control is disabled if the attribute 
	 * "<code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-disabled">disabled</a></code>" 
	 * is present in its <a href="#OutputElement">output element</a>.
	 * <p>
	 * The return value is is logically equivalent to {@link #getAttributesMap()}<code>.containsKey("disabled")</code>,
	 * but using this property may be more efficient in some circumstances.
	 *
	 * @return <code>true</code> if this form control is <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-disabled">disabled</a>, otherwise <code>false</code>.
	 */
	public final boolean isDisabled() {
		return elementContainer.getBooleanAttribute(Attribute.DISABLED);
	}

	/**
	 * Sets whether this form control is <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-disabled">disabled</a>.
	 * <p>
	 * If the argument supplied to this method is <code>true</code> and the <code>disabled</code> attribute is not already present
	 * in the output element, the full
	 * <a target="_blank" href="http://www.w3.org/TR/xhtml1/">XHTML</a> compatible attribute <code>disabled="disabled"</code> is added.  
	 * If the attribute is already present, it is left unchanged.
	 * <p>
	 * If the argument supplied to this method is <code>false</code>, the attribute is removed from the output element.
	 * <p>
	 * See the {@link #isDisabled()} method for more information.
	 *
	 * @param disabled  the new value of this property.
	 */
	public final void setDisabled(final boolean disabled) {
		elementContainer.setBooleanAttribute(Attribute.DISABLED,disabled);
	}

	/**
	 * Indicates whether this form control is <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-checked">checked</a>.
	 * <p>
	 * The term <i>checked</i> is used to describe a checkbox or radio button control that is selected, which is the case if the attribute
	 * "<code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-checked">checked</a></code>" 
	 * is present in its <a href="#OutputElement">output element</a>.
	 * <p>
	 * This property is only relevant to form controls with a {@linkplain #getFormControlType() type} of 
	 * {@link FormControlType#CHECKBOX} or {@link FormControlType#RADIO}, and throws an <code>UnsupportedOperationException</code>
	 * for other control types.
	 * <p>
	 * Use one of the <a href="#SubmissionValueModificationMethods">submission value modification methods</a> to change the value
	 * of this property.
	 * <p>
	 * If this control is a checkbox, you can set it to checked by calling
	 * {@link #setValue(CharSequence) setValue}<code>(</code>{@link #getName()}<code>)</code>, and set it to unchecked by calling
	 * {@link #clearValues()}.
	 * <p>
	 * If this control is a radio button, you should use the {@link FormField#setValue(CharSequence)} method or one of the other
	 * higher level <a href="#SubmissionValueModificationMethods">submission value modification methods</a>
	 * to set the control to checked, as calling {@link #setValue(CharSequence)} method on this object
	 * in the same way as for a checkbox does not automatically uncheck all other radio buttons with the same name.
	 * Even calling {@link #clearValues()} on this object to ensure that this radio button is unchecked is not recommended, as
	 * it can lead to a situation where all the radio buttons with this name are unchecked.
	 * The <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#radio">HTML 4.01 specification of radio buttons</a>
	 * recommends against this situation because it is not defined how user agents should handle it, and behaviour differs amongst browsers.
	 * <p>
	 * The return value is logically equivalent to {@link #getAttributesMap()}<code>.containsKey("checked")</code>,
	 * but using this property may be more efficient in some circumstances.
	 *
	 * @return <code>true</code> if this form control is <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-checked">checked</a>, otherwise <code>false</code>.
	 * @throws UnsupportedOperationException if the {@linkplain #getFormControlType() type} of this control is not {@link FormControlType#CHECKBOX} or {@link FormControlType#RADIO}.
	 */
	public boolean isChecked() {
		throw new UnsupportedOperationException("This property is only relevant for CHECKBOX and RADIO controls");
	}

	/**
	 * Returns the <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#initial-value">initial value</a> of this control if it has a {@linkplain FormControlType#hasPredefinedValue() predefined value}.
	 * <p>
	 * Only <a href="#PredefinedValueControl">predefined value controls</a> can return a non-<code>null</code> result.
	 * All other control types return <code>null</code>.
	 * <p>
	 * {@link FormControlType#CHECKBOX CHECKBOX} and {@link FormControlType#RADIO RADIO} controls have a guaranteed
	 * predefined value determined by the value of its compulsory
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-value-INPUT">value</a></code>
	 * attribute.  If the attribute is not present in the source document, this library assigns the control a default
	 * predefined value of "<code>on</code>", consistent with popular browsers.
	 * <p>
	 * {@link FormControlType#SUBMIT SUBMIT}, {@link FormControlType#BUTTON BUTTON} and {@link FormControlType#IMAGE IMAGE}
	 * controls have an optional predefined value determined by the value of its
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-value-INPUT">value</a></code>
	 * attribute.  This value is
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#successful-controls">successful</a>
	 * only in the control used to submit the form.
	 * <p>
	 * {@link FormControlType#SELECT_SINGLE} and {@link FormControlType#SELECT_MULTIPLE} controls are special cases
	 * because they usually contain multiple
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-OPTION">OPTION</a></code>
	 * elements, each with its own predefined value.
	 * In this case the {@link #getPredefinedValues()} method should be used instead, which returns a collection of all the
	 * control's predefined values.  Attempting to call this method on a <code>SELECT</code> control results in
	 * a <code>java.lang.UnsupportedOperationException</code>.
	 * <p>
	 * The predefined value of a control is not affected by changes to the
	 * <a href="#SubmissionValue">submission value</a> of the control.
	 *
	 * @return the <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#initial-value">initial value</a> of this control if it has a {@linkplain FormControlType#hasPredefinedValue() predefined value}, or <code>null</code> if none.
	 */
	public String getPredefinedValue() {
		return elementContainer.predefinedValue;
	}

	/**
	 * Returns a collection of all {@linkplain #getPredefinedValue() predefined values} in this control.
	 * <p>
	 * All objects in the returned collection are of type <code>String</code>, with no <code>null</code> entries.
	 * <p>
	 * This method is most useful for
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-SELECT">SELECT</a></code>
	 * controls since they typically contain multiple predefined values.
	 * In other controls it returns a collection with zero or one item based on the output of the
	 * {@link #getPredefinedValue()} method, so for efficiency it is recommended to use the
	 * {@link #getPredefinedValue()} method instead.
	 * <p>
	 * The multiple predefined values of a
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-SELECT">SELECT</a></code>
	 * control are defined by the
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-OPTION">OPTION</a></code>
	 * elements within it.
	 * Each <code>OPTION</code> element has an
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#initial-value">initial value</a>
	 * determined by the value of its
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-value-OPTION">value</a></code>
	 * attribute, or if this attribute is not present, by its
	 * {@linkplain CharacterReference#decode(CharSequence) decoded} {@linkplain Element#getContent() content}
	 * text with {@linkplain CharacterReference#decodeCollapseWhiteSpace(CharSequence) collapsed white space}.
	 * <p>
	 * The predefined values of a control are not affected by changes to the
	 * <a href="#SubmissionValue">submission values</a> of the control.
	 *
	 * @return a collection of all {@linkplain #getPredefinedValue() predefined values} in this control, guaranteed not <code>null</code>.
	 * @see FormField#getPredefinedValues()
	 */
	public Collection<String> getPredefinedValues() {
		if (getPredefinedValue() == null) {
			return Collections.emptySet();
		}
		else {
			return Collections.singleton(getPredefinedValue());
		}
		//		return getPredefinedValue()!=null ? Collections.singleton(getPredefinedValue()) : Collections.EMPTY_SET;
	}

	/**
	 * Returns a collection of the control's <a href="#SubmissionValue">submission values</a>.
	 * <p>
	 * All objects in the returned collection are of type <code>CharSequence</code>, with no <code>null</code> entries.
	 * <p>
	 * The term <i><a name="SubmissionValue">submission value</a></i> is used in this library to refer to the value the control 
	 * would contribute to the
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#form-data-set">form data set</a>
	 * of a <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#submit-format">submitted</a>
	 * form, assuming no modification of the control's
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#current-value">current value</a> by the
	 * <a target="_blank" href="http://www.w3.org/TR/html401/conform.html#didx-user_agent">user agent</a> or by end user interaction.
	 * <p>
	 * For <a href="#UserValueControl">user value controls</a>, the submission value corresponds to the
	 * control's <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#initial-value">initial value</a>.
	 * <p>
	 * The definition of the submission value for each <a href="#PredefinedValueControl">predefined value control</a> type is as follows:
	 * <p>
	 * {@link FormControlType#CHECKBOX CHECKBOX} and {@link FormControlType#RADIO RADIO} controls
	 * have a submission value specified by its {@linkplain #getPredefinedValue() predefined value}
	 * if it is {@link #isChecked() checked}, otherwise it has no submission value.
	 * <p>
	 * {@link FormControlType#SELECT_SINGLE SELECT_SINGLE} and {@link FormControlType#SELECT_MULTIPLE SELECT_MULTIPLE} controls
	 * have submission values specified by the
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-value-OPTION">values</a> of the control's
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-selected">selected</a>
	 * <code>OPTION</code> elements.
	 * <p>
	 * Only a {@link FormControlType#SELECT_MULTIPLE SELECT_MULTIPLE} control can have more than one submission value,
	 * all other {@linkplain FormControlType control types} return a collection containing either one value or no values.
	 * A {@link FormControlType#SELECT_SINGLE SELECT_SINGLE} control only returns multiple submission values
	 * if it illegally contains multiple selected options in the source document.
	 * <p>
	 * {@link FormControlType#SUBMIT SUBMIT}, {@link FormControlType#BUTTON BUTTON}, and {@link FormControlType#IMAGE IMAGE}
	 * controls are only ever
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#successful-controls">successful</a>
	 * when they are activated by the user to
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#submit-format">submit</a> the form.
	 * Because the submission value is intended to be a static representation of a control's data without
	 * interaction by the user, this library never associates submission values with
	 * {@linkplain FormControlType#isSubmit() submit} buttons, so this method always returns an empty collection for these
	 * control types.
	 * <p>
	 * The <a href="#SubmissionValue">submission value(s)</a> of a control can be modified for subsequent output in
	 * an {@link OutputDocument} using the various 
	 * <i><a name="SubmissionValueModificationMethods">submission value modification methods</a></i>, namely:<br />
	 * {@link FormField#setValue(CharSequence)}<br />
	 * {@link FormField#addValue(CharSequence)}<br />
	 * {@link FormField#setValues(Collection)}<br />
	 * {@link FormField#clearValues()}<br />
	 * {@link FormFields#setValue(String fieldName, CharSequence value)}<br />
	 * {@link FormFields#addValue(String fieldName, CharSequence value)}<br />
	 * {@link FormFields#setDataSet(Map)}<br />
	 * {@link FormFields#clearValues()}<br />
	 * {@link #setValue(CharSequence) FormControl.setValue(CharSequence)}<br />
	 * {@link #addValue(CharSequence) FormControl.addValue(CharSequence)}<br />
	 * {@link #clearValues() FormControl.clearValues()}<br />
	 * <p>
	 * The values returned by this method reflect any changes made using the submission value modification methods,
	 * in contrast to methods found in the {@link Attributes} and {@link Attribute} classes, which always reflect the source document.
	 *
	 * @return a collection of the control's <i>submission values</i>, guaranteed not <code>null</code>.
	 * @see #getPredefinedValues()
	 */
	public Collection<String> getValues() {
		final Set<String> values=new HashSet<String>();
		addValuesTo(values);
		return values;
	}

	/**
	 * Clears the control's existing <a href="#SubmissionValue">submission values</a>.
	 * <p>
	 * This is equivalent to {@link #setValue(CharSequence) setValue(null)}.
	 * <p>
	 * NOTE: The {@link FormFields} and {@link FormField} classes provide a more appropriate abstraction level for the modification of form control submission values.
 	 *
 	 * @see FormFields#clearValues()
 	 * @see FormField#clearValues()
	 */
	public final void clearValues() {
		setValue(null);
	}

	/**
	 * Sets the control's <a href="#SubmissionValue">submission value</a> *.
	 * <p>
	 * * NOTE: The {@link FormFields} and {@link FormField} classes provide a more appropriate abstraction level for the modification of form control submission values.
	 * Consider using the {@link FormFields#setValue(String fieldName, CharSequence value)} method instead.
	 * <p>
	 * The specified value replaces any existing <a href="#SubmissionValue">submission values</a> of the control.
	 * <p>
	 * The return value indicates whether the control has "accepted" the value.
	 * For <a href="#UserValueControl">user value controls</a>, the return value is always <code>true</code>.
	 * <p>
	 * For <a href="#PredefinedValueControl">predefined value controls</a>,
	 * calling this method does not affect the control's
	 * {@linkplain #getPredefinedValues() predefined values}, but instead determines whether the control (or its options) become
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-checked">checked</a></code> or
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-selected">selected</a></code>
	 * as detailed below:
	 * <p>
	 * {@link FormControlType#CHECKBOX CHECKBOX} and {@link FormControlType#RADIO RADIO} controls become {@link #isChecked() checked}
	 * and the method returns <code>true</code> if the specified value matches the control's predefined value (case sensitive),
	 * otherwise the control becomes unchecked and the method returns <code>false</code>.
	 * Note that any other controls with the same {@linkplain #getName() name} are not unchecked if this control becomes checked,
	 * possibly resulting in an invalid state where multiple <code>RADIO</code> controls are checked at the same time.
	 * The {@link FormField#setValue(CharSequence)} method avoids such problems and its use is recommended over this method.
	 * <p>
	 * {@link FormControlType#SELECT_SINGLE SELECT_SINGLE} and {@link FormControlType#SELECT_MULTIPLE SELECT_MULTIPLE}
	 * controls receive the specified value by selecting the option with the matching value and deselecting all others.
	 * If none of the options match, all are deselected.
	 * The return value of this method indicates whether one of the options matched.
	 * <p>
	 * {@link FormControlType#SUBMIT SUBMIT}, {@link FormControlType#BUTTON BUTTON}, and {@link FormControlType#IMAGE IMAGE}
	 * controls never have a <a href="#SubmissionValue">submission value</a>, so calling this method has no effect and
	 * always returns <code>false</code>.
	 *
	 * @param value  the new <a href="#SubmissionValue">submission value</a> of this control, or <code>null</code> to clear the control of all submission values.
	 * @return <code>true</code> if the control accepts the value, otherwise <code>false</code>.
	 * @see FormFields#setValue(String fieldName, CharSequence value)
	 */
	public abstract boolean setValue(CharSequence value);

	/**
	 * Adds the specified value to this control's <a href="#SubmissionValue">submission values</a> *.
	 * <p>
	 * * NOTE: The {@link FormFields} and {@link FormField} classes provide a more appropriate abstraction level for the modification of form control submission values.
	 * Consider using the {@link FormFields#addValue(String fieldName, CharSequence value)} method instead.
	 * <p>
	 * This is almost equivalent to {@link #setValue(CharSequence)}, with only the following differences:
	 * <p>
	 * {@link FormControlType#CHECKBOX CHECKBOX} controls retain their existing <a href="#SubmissionValue">submission value</a>
	 * instead of becoming unchecked if the specified value does not match the control's {@linkplain #getPredefinedValue() predefined value}.
	 * <p>
	 * {@link FormControlType#SELECT_MULTIPLE SELECT_MULTIPLE} controls retain their existing
	 * <a href="#SubmissionValue">submission values</a>, meaning that the control's
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-OPTION">OPTION</a></code>
	 * elements whose {@linkplain #getPredefinedValues() predefined values} do not match the specified value are not deselected.
	 * This is the only type of control that can have multiple submission values within the one control.
	 *
	 * @param value  the value to add to this control's <a href="#SubmissionValue">submission values</a>, must not be <code>null</code>.
	 * @return <code>true</code> if the control accepts the value, otherwise <code>false</code>.
	 * @see FormFields#addValue(String fieldName, CharSequence value)
	 */
	public boolean addValue(final CharSequence value) {
		return setValue(value);
	}

	abstract void addValuesTo(Collection<String> collection); // should not add null values, values must be of type CharSequence
	abstract void addToFormFields(FormFields formFields);
	abstract void replaceInOutputDocument(OutputDocument outputDocument);

	public String getDebugInfo() {
		final StringBuffer sb=new StringBuffer();
		sb.append(formControlType).append(" name=\"").append(name).append('"');
		if (elementContainer.predefinedValue!=null) sb.append(" PredefinedValue=\"").append(elementContainer.predefinedValue).append('"');
		sb.append(" - ").append(getElement().getDebugInfo());
		return sb.toString();
	}

	static final class InputFormControl extends FormControl {
		// TEXT, HIDDEN, PASSORD or FILE
		public InputFormControl(final Element element, final FormControlType formControlType) {
			super(element,formControlType,false);
		}
		public boolean setValue(final CharSequence value) {
			elementContainer.setAttributeValue(Attribute.VALUE,value);
			return true;
		}
		void addValuesTo(final Collection<String> collection) {
			addValueTo(collection,elementContainer.getAttributeValue(Attribute.VALUE));
		}
		void addToFormFields(final FormFields formFields) {
			formFields.add(this);
		}
		void replaceInOutputDocument(final OutputDocument outputDocument) {
			if (outputStyle==FormControlOutputStyle.REMOVE) {
				outputDocument.remove(getElement());
			} else if (outputStyle==FormControlOutputStyle.DISPLAY_VALUE) {
				String output=null;
				if (formControlType!=FormControlType.HIDDEN) {
					CharSequence value=elementContainer.getAttributeValue(Attribute.VALUE);
					if (formControlType==FormControlType.PASSWORD && value!=null) value=getString(FormControlOutputStyle.ConfigDisplayValue.PasswordChar,value.length());
					output=getDisplayValueHTML(value,false);
				}
				outputDocument.replace(getElement(),output);
			} else {
				replaceAttributesInOutputDocumentIfModified(outputDocument);
			}
		}
	}

	static final class TextAreaFormControl extends FormControl {
		// TEXTAREA
		public CharSequence value=UNCHANGED;
		private static final String UNCHANGED=new String();
		public TextAreaFormControl(final Element element) {
			super(element,FormControlType.TEXTAREA,false);
		}
		public boolean setValue(final CharSequence value) {
			this.value=value;
			return true;
		}
		void addValuesTo(final Collection<String> collection) {
			addValueTo(collection,getValue());
		}
		void addToFormFields(final FormFields formFields) {
			formFields.add(this);
		}
		void replaceInOutputDocument(final OutputDocument outputDocument) {
			if (outputStyle==FormControlOutputStyle.REMOVE) {
				outputDocument.remove(getElement());
			} else if (outputStyle==FormControlOutputStyle.DISPLAY_VALUE) {
				outputDocument.replace(getElement(),getDisplayValueHTML(getValue(),true));
			} else {
				replaceAttributesInOutputDocumentIfModified(outputDocument);
				if (value!=UNCHANGED)
					outputDocument.replace(getElement().getContent(),CharacterReference.encode(value));
			}
		}
		private CharSequence getValue() {
			return (value==UNCHANGED) ? CharacterReference.decode(getElement().getContent()) : value;
		}
	}

	static final class RadioCheckboxFormControl extends FormControl {
		// RADIO or CHECKBOX
		public RadioCheckboxFormControl(final Element element, final FormControlType formControlType) {
			super(element,formControlType,true);
			if (elementContainer.predefinedValue==null) {
				elementContainer.predefinedValue=CHECKBOX_NULL_DEFAULT_VALUE;
				if (element.source.isLoggingEnabled()) element.source.log(element.source.getRowColumnVector(element.begin).appendTo(new StringBuffer(200)).append(": compulsory \"value\" attribute of ").append(formControlType).append(" control \"").append(name).append("\" is missing, assuming the value \"").append(CHECKBOX_NULL_DEFAULT_VALUE).append('"').toString());
			}
		}
		public boolean setValue(final CharSequence value) {
			return elementContainer.setSelected(value,Attribute.CHECKED,false);
		}
		public boolean addValue(final CharSequence value) {
			return elementContainer.setSelected(value,Attribute.CHECKED,formControlType==FormControlType.CHECKBOX);
		}
		void addValuesTo(final Collection<String> collection) {
			if (isChecked()) addValueTo(collection,getPredefinedValue());
		}
		public boolean isChecked() {
			return elementContainer.getBooleanAttribute(Attribute.CHECKED);
		}
		void addToFormFields(final FormFields formFields) {
			formFields.add(this);
		}
		void replaceInOutputDocument(final OutputDocument outputDocument) {
			if (outputStyle==FormControlOutputStyle.REMOVE) {
				outputDocument.remove(getElement());
			} else {
				if (outputStyle==FormControlOutputStyle.DISPLAY_VALUE) {
					final String html=isChecked() ? FormControlOutputStyle.ConfigDisplayValue.CheckedHTML : FormControlOutputStyle.ConfigDisplayValue.UncheckedHTML;
					if (html!=null) {
						outputDocument.replace(getElement(),html);
						return;
					}
					setDisabled(true);
				}
				replaceAttributesInOutputDocumentIfModified(outputDocument);
			}
		}
	}

	static class SubmitFormControl extends FormControl {
		// BUTTON, SUBMIT or (in subclass) IMAGE
		public SubmitFormControl(final Element element, final FormControlType formControlType) {
			super(element,formControlType,true);
		}
		public boolean setValue(final CharSequence value) {
			return false;
		}
		void addValuesTo(final Collection<String> collection) {}
		void addToFormFields(final FormFields formFields) {
			if (getPredefinedValue()!=null) formFields.add(this);
		}
		void replaceInOutputDocument(final OutputDocument outputDocument) {
			if (outputStyle==FormControlOutputStyle.REMOVE) {
				outputDocument.remove(getElement());
			} else {
				if (outputStyle==FormControlOutputStyle.DISPLAY_VALUE) setDisabled(true);
				replaceAttributesInOutputDocumentIfModified(outputDocument);
			}
		}
	}

	static final class ImageSubmitFormControl extends SubmitFormControl {
		// IMAGE
		public ImageSubmitFormControl(final Element element) {
			super(element,FormControlType.IMAGE);
		}
		void addToFormFields(final FormFields formFields) {
			super.addToFormFields(formFields);
			formFields.addName(this,name+".x");
			formFields.addName(this,name+".y");
		}
	}

	static final class SelectFormControl extends FormControl {
		// SELECT_MULTIPLE or SELECT_SINGLE
		public ElementContainer[] optionElementContainers;
		public SelectFormControl(final Element element) {
			super(element,element.getAttributes().get(Attribute.MULTIPLE)!=null ? FormControlType.SELECT_MULTIPLE : FormControlType.SELECT_SINGLE,false);
			final List<?> optionElements=element.findAllElements(Tag.OPTION);
			optionElementContainers=new ElementContainer[optionElements.size()];
			int x=0;
			for (final Iterator<?> i=optionElements.iterator(); i.hasNext();) {
				final ElementContainer optionElementContainer=new ElementContainer((Element)i.next(),true);
				if (optionElementContainer.predefinedValue==null)
					// use the content of the element if it has no value attribute
					optionElementContainer.predefinedValue=CharacterReference.decodeCollapseWhiteSpace(optionElementContainer.element.getContent());
				optionElementContainers[x++]=optionElementContainer;
			}
		}
		public String getPredefinedValue() {
			throw new UnsupportedOperationException("Use getPredefinedValues() method instead on SELECT controls");
		}
		public Collection<String> getPredefinedValues() {
			final ArrayList<String> arrayList=new ArrayList<String>(optionElementContainers.length);
			for (int i=0; i<optionElementContainers.length; i++)
			arrayList.add(optionElementContainers[i].predefinedValue);
			return arrayList;
		}
		public Iterator<?> getOptionElementIterator() {
			return new OptionElementIterator();
		}
		public boolean setValue(final CharSequence value) {
			return addValue(value,false);
		}
		public boolean addValue(final CharSequence value) {
			return addValue(value,formControlType==FormControlType.SELECT_MULTIPLE);
		}
		private boolean addValue(final CharSequence value, final boolean allowMultipleValues) {
			boolean valueFound=false;
			for (int i=0; i<optionElementContainers.length; i++) {
				if (optionElementContainers[i].setSelected(value,Attribute.SELECTED,allowMultipleValues)) valueFound=true;
			}
			return valueFound;
		}
		void addValuesTo(final Collection<String> collection) {
			for (int i=0; i<optionElementContainers.length; i++) {
				if (optionElementContainers[i].getBooleanAttribute(Attribute.SELECTED))
					addValueTo(collection,optionElementContainers[i].predefinedValue);
			}
		}
		void addToFormFields(final FormFields formFields) {
			for (int i=0; i<optionElementContainers.length; i++)
				formFields.add(this,optionElementContainers[i].predefinedValue);
		}
		void replaceInOutputDocument(final OutputDocument outputDocument) {
			if (outputStyle==FormControlOutputStyle.REMOVE) {
				outputDocument.remove(getElement());
			} else if (outputStyle==FormControlOutputStyle.DISPLAY_VALUE) {
				final StringBuffer sb=new StringBuffer(100);
				for (int i=0; i<optionElementContainers.length; i++) {
					if (optionElementContainers[i].getBooleanAttribute(Attribute.SELECTED)) {
						appendCollapseWhiteSpace(sb,optionElementContainers[i].element.getContent());
						sb.append(FormControlOutputStyle.ConfigDisplayValue.MultipleValueSeparator);
					}
				}
				if (sb.length()>0) sb.setLength(sb.length()-FormControlOutputStyle.ConfigDisplayValue.MultipleValueSeparator.length()); // remove last separator
				outputDocument.replace(getElement(),getDisplayValueHTML(sb,false));
			} else {
				replaceAttributesInOutputDocumentIfModified(outputDocument);
				for (int i=0; i<optionElementContainers.length; i++) {
					optionElementContainers[i].replaceAttributesInOutputDocumentIfModified(outputDocument);
				}
			}
		}
		private final class OptionElementIterator implements Iterator<Object> {
			private int i=0;
			public boolean hasNext() {
				return i<optionElementContainers.length;
			}
			public Object next() {
				if (!hasNext()) throw new NoSuchElementException();
				return optionElementContainers[i++].element;
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		}
	}

	final String getDisplayValueHTML(final CharSequence text, final boolean whiteSpaceFormatting) {
		final StringBuffer sb=new StringBuffer((text==null ? 0 : text.length()*2)+50);
		sb.append('<').append(FormControlOutputStyle.ConfigDisplayValue.ElementName);
		for (final Iterator<?> i=FormControlOutputStyle.ConfigDisplayValue.AttributeNames.iterator(); i.hasNext();) {
			final String attributeName=i.next().toString();
			final CharSequence attributeValue=elementContainer.getAttributeValue(attributeName);
			if (attributeValue==null) continue;
			Attribute.appendHTML(sb,attributeName,attributeValue);
		}
		sb.append('>');
		if (text==null || text.length()==0)
			sb.append(FormControlOutputStyle.ConfigDisplayValue.EmptyHTML);
		else
			CharacterReference.appendEncode(sb,text,whiteSpaceFormatting);
		sb.append(EndTagType.START_DELIMITER_PREFIX).append(FormControlOutputStyle.ConfigDisplayValue.ElementName).append('>');
		return sb.toString();
	}

	final void replaceAttributesInOutputDocumentIfModified(final OutputDocument outputDocument) {
		elementContainer.replaceAttributesInOutputDocumentIfModified(outputDocument);
	}

	static List<FormControl> findAll(final Segment segment) {
		final ArrayList<FormControl> list=new ArrayList<FormControl>();
		findAll(segment,list,Tag.INPUT);
		findAll(segment,list,Tag.TEXTAREA);
		findAll(segment,list,Tag.SELECT);
		findAll(segment,list,Tag.BUTTON);
		Collections.sort(list,COMPARATOR);
		return list;
	}

	private static void findAll(final Segment segment, final ArrayList<FormControl> list, final String tagName) {
		for (final Iterator<?> i=segment.findAllElements(tagName).iterator(); i.hasNext();)
			list.add(((Element)i.next()).getFormControl());
	}

	private static CharSequence getString(final char ch, final int length) {
		if (length==0) return "";
		final StringBuffer sb=new StringBuffer(length);
		for (int i=0; i<length; i++) sb.append(ch);
		return sb.toString();
	}

	private void verifyName() {
		if (formControlType.isSubmit()) return;
		String missingOrBlank;
		if (name==null) {
			missingOrBlank="missing";
		} else {
			if (name.length()!=0) return;
			missingOrBlank="blank";
		}
		final Source source=getElement().source;
		if (source.isLoggingEnabled()) source.log(getElement().source.getRowColumnVector(getElement().begin).appendTo(new StringBuffer(200)).append(": compulsory \"name\" attribute of ").append(formControlType).append(" control is ").append(missingOrBlank).toString());
	}

	private static final void addValueTo(final Collection<String> collection, final CharSequence value) {
		collection.add(value!=null ? (String)value : "");
	}

	private static final class PositionComparator implements Comparator<FormControl> {
		public int compare(final FormControl fc1, final FormControl fc2) {
			int formControl1Begin=fc1.getElement().getBegin();
			int formControl2Begin=fc2.getElement().getBegin();
			if (formControl1Begin<formControl2Begin) return -1;
			if (formControl1Begin>formControl2Begin) return 1;
			return 0;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////

	static final class ElementContainer {
		// Contains the information common to both a FormControl and to each OPTION element
		// within a SELECT FormControl
		public final Element element;
		public Map<String,CharSequence> attributesMap=null;
		public String predefinedValue; // never null for option, checkbox or radio elements

		public ElementContainer(final Element element, final boolean loadPredefinedValue) {
			this.element=element;
			predefinedValue=loadPredefinedValue ? element.getAttributes().getValue(Attribute.VALUE) : null;
		}

		public Map<String, CharSequence> getAttributesMap() {
			if (attributesMap==null) attributesMap=element.getAttributes().getMap(true);
			return attributesMap;
		}

		public boolean setSelected(final CharSequence value, final String selectedOrChecked, final boolean allowMultipleValues) {
			if (value!=null && predefinedValue.equals(value.toString())) {
				setBooleanAttribute(selectedOrChecked,true);
				return true;
			}
			if (!allowMultipleValues) setBooleanAttribute(selectedOrChecked,false);
			return false;
		}

		public CharSequence getAttributeValue(final String attributeName) {
			if (attributesMap!=null)
				return (CharSequence)attributesMap.get(attributeName);
			else
				return element.getAttributes().getValue(attributeName);
		}

		public void setAttributeValue(final String attributeName, final CharSequence value) {
			// null value indicates attribute should be removed.
			if (value==null) {
				setBooleanAttribute(attributeName,false);
				return;
			}
			if (attributesMap!=null) {
				attributesMap.put(attributeName,value);
				return;
			}
			final String valueString=value.toString();
			final CharSequence existingValue=getAttributeValue(attributeName);
			if (existingValue!=null && existingValue.toString().equals(valueString)) return;
			getAttributesMap().put(attributeName,valueString);
		}

		public boolean getBooleanAttribute(final String attributeName) {
			if (attributesMap!=null)
				return attributesMap.containsKey(attributeName);
			else
				return element.getAttributes().get(attributeName)!=null;
		}

		public void setBooleanAttribute(final String attributeName, final boolean value) {
			final boolean oldValue=getBooleanAttribute(attributeName);
			if (value==oldValue) return;
			if (value)
				getAttributesMap().put(attributeName,attributeName); // xhtml compatible attribute
			else
				getAttributesMap().remove(attributeName);
		}

		public void replaceAttributesInOutputDocumentIfModified(final OutputDocument outputDocument) {
			if (attributesMap!=null) outputDocument.replace(element.getAttributes(),attributesMap);
		}
	}
}

