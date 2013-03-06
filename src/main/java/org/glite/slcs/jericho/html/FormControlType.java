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

import java.util.Collection;
import java.util.HashMap;

/**
 * Represents the <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#h-17.2.1">control type</a>
 * of a {@link FormControl}.
 * <p>
 * Use the {@link FormControl#getFormControlType()} method to determine the type of a form control.
 * <p>
 * The following table shows the relationship between the HTML 4.01 specification control type descriptions,
 * their associated {@link Element} names and attributes, and the <code>FormControlType</code> constants defined in this class:
 * <table class="bordered" style="margin: 15px" cellspacing="0">
 *  <tr>
 *   <th><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#h-17.2.1">Description</a>
 *	 <th>{@linkplain Element#getName() Element Name}
 *   <th>Distinguishing Attribute
 *   <th><code>FormControlType</code>
 *  <tr>
 *   <td rowspan="3"><a name="submit-button" target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#buttons">buttons</a> - <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#submit-button">submit button</a>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-BUTTON">BUTTON</a></code>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-BUTTON">type</a>="submit"</code>
 *   <td>{@link #BUTTON}
 *  <tr>
 *   <td rowspan="2"><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT">INPUT</a></code>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-INPUT">type</a>="submit"</code>
 *   <td>{@link #SUBMIT}
 *  <tr>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-INPUT">type</a>="<a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#input-control-types">image</a>"</code>
 *   <td>{@link #IMAGE}
 *  <tr>
 *   <td><a name="reset-button-control" target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#buttons">buttons</a> - <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#reset-button">reset button</a>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-BUTTON">BUTTON</a></code>,
 *       <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT">INPUT</a></code>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-BUTTON">type</a>="reset"</code>
 *   <td>-
 *  <tr>
 *   <td><a name="push-button-control" target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#buttons">buttons</a> - <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#push-button">push button</a>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-BUTTON">BUTTON</a></code>,
 *       <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT">INPUT</a></code>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-BUTTON">type</a>="button"</code>
 *   <td>-
 *  <tr>
 *   <td><a name="checkbox-control" target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#checkbox">checkboxes</a>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT">INPUT</a></code>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-INPUT">type</a>="checkbox"</code>
 *   <td>{@link #CHECKBOX}
 *  <tr>
 *   <td><a name="radio-button-control" target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#radio">radio buttons</a>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT">INPUT</a></code>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-INPUT">type</a>="radio"</code>
 *   <td>{@link #RADIO}
 *  <tr>
 *   <td rowspan="2"><a name="menu-control" target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#menu">menus</a>
 *   <td rowspan="2"><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-SELECT">SELECT</a></code>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-multiple">multiple</a></code>
 *   <td>{@link #SELECT_MULTIPLE}
 *  <tr>
 *   <td>absence of <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-multiple">multiple</a></code>
 *   <td>{@link #SELECT_SINGLE}
 *  <tr>
 *   <td rowspan="3"><a name="text-input-control" target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#text-input">text input</a>
 *   <td rowspan="2"><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT">INPUT</a></code>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-INPUT">type</a>="<a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#idx-text_input_control-1">text</a>"</code>
 *   <td>{@link #TEXT}
 *  <tr>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-INPUT">type</a>="<a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#idx-password_input_control">password</a>"</code>
 *   <td>{@link #PASSWORD}
 *  <tr>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-TEXTAREA">TEXTAREA</a></code>
 *   <td>-
 *   <td>{@link #TEXTAREA}
 *  <tr>
 *   <td><a name="file-select-control" target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#file-select">file select</a>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT">INPUT</a></code>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-INPUT">type</a>="file"</code>
 *   <td>{@link #FILE}
 *  <tr>
 *   <td><a name="hidden-control" target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#hidden-control">hidden controls</a>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT">INPUT</a></code>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-INPUT">type</a>="hidden"</code>
 *   <td>{@link #HIDDEN}
 *  <tr>
 *   <td><a name="object-control" target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#object-control">object controls</a>
 *   <td><code><a target="_blank" href="http://www.w3.org/TR/html401/struct/objects.html#edef-OBJECT">OBJECT</a></code>
 *   <td><code>-
 *   <td>-
 * </table>
 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#reset-button-control">Reset buttons</a> and
 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#push-button-control">push buttons</a>
 * have no associated <code>FormControlType</code> because they do not contribute to the
 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#form-data-set">form data set</a>
 * of a <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#submit-format">submitted</a> form,
 * and so have no relevance to the methods provided in the {@link FormControl} and associated classes.
 * If required they can be found and manipulated as normal {@linkplain Element elements}.
 * <p>
 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#object-control">Object controls</a>
 * have no associated <code>FormControlType</code> because any data they might contribute to the
 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#form-data-set">form data set</a>
 * is entirely dependent on the
 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/objects.html#adef-classid">class</a> of object,
 * the interpretation of which is is beyond the scope of this library.
 * <p>
 * This library does not consider the
 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-OPTION">OPTION</a></code>
 * elements found within
 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-SELECT">SELECT</a></code>
 * elements to be controls themselves, despite them being referred to as such in some
 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-value-OPTION">parts</a>
 * of the HTML 4.01 specification.
 * Hence the absence of an <code>OPTION</code> control type.
 *
 * @see FormControl
 * @see FormField
 */
public final class FormControlType {
	private String formControlTypeId;
	private String elementName;
	private boolean hasPredefinedValue;
	private boolean submit;

	private static final HashMap ID_MAP=new HashMap(16,1.0F); // 12 types in total
	private static final HashMap INPUT_ELEMENT_TYPE_MAP=new HashMap(11,1.0F); // 8 input element types in total

	/**
	 * The form control type given to a <a href="#submit-button">submit button</a> control implemented using a
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-BUTTON">BUTTON</a></code> element.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;button type="submit" name="FieldName" value="PredefinedValue"&gt;Send&lt;/button&gt;</code>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <code>{@link #getElementName()} = {@link Tag#BUTTON}</code><br />
	 *    <code>{@link #hasPredefinedValue()} = true</code><br />
	 *    <code>{@link #isSubmit()} = true</code><br />
	 * </dl>
	 */
	public static final FormControlType BUTTON=new FormControlType("button",Tag.BUTTON,true,true).register();

	/**
	 * The form control type given to a <a href="#checkbox-control">checkbox</a> control.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;input type="checkbox" name="FieldName" value="PredefinedValue" /&gt;</code>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <code>{@link #getElementName()} = {@link Tag#INPUT}</code><br />
	 *    <code>{@link #hasPredefinedValue()} = true</code><br />
	 *    <code>{@link #isSubmit()} = false</code><br />
	 * </dl>
	 */
	public static final FormControlType CHECKBOX=new FormControlType("checkbox",Tag.INPUT,true,false).register();

	/**
	 * The form control type given to a <a href="#file-select-control">file select</a> control.
	 * <p>
	 * This library considers the <a href="FormControl.html#SubmissionValue">submission value</a> of this type of control
	 * to be consist of only the selected file name, regardless of whether the file content would normally be included in the
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#form-data-set">form data set</a>.
	 * <p>
	 * To determine manually whether the file content is included in the form data set, the
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-enctype">enctype</a></code>
	 * attribute of the control's associated <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-FORM">FORM</a>
	 * element can be examined.
	 * Although the exact behaviour is not defined in the HTML 4.01 specification, the convention is that the content
	 * is not included unless an <code>enctype</code> value of
	 * "<code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#didx-multipartform-data">multipart/form-data</a></code>"
	 * is specified.
	 * <p>
	 * For more information see the
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4">HTML 4.01 specification section 17.13.4 - Form content types</a>.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;input type="file" name="FieldName" value="DefaultFileName" /&gt;</code>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <code>{@link #getElementName()} = {@link Tag#INPUT}</code><br />
	 *    <code>{@link #hasPredefinedValue()} = false</code><br />
	 *    <code>{@link #isSubmit()} = false</code><br />
	 * </dl>
	 */
	public static final FormControlType FILE=new FormControlType("file",Tag.INPUT,false,false).register();

	/**
	 * The form control type given to a <a href="#hidden-control">hidden</a> control.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;input type="hidden" name="FieldName" value="DefaultValue" /&gt;</code>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <code>{@link #getElementName()} = {@link Tag#INPUT}</code><br />
	 *    <code>{@link #hasPredefinedValue()} = false</code><br />
	 *    <code>{@link #isSubmit()} = false</code><br />
	 * </dl>
	 * Note that {@link #hasPredefinedValue()} returns <code>false</code> for this control type
	 * because the value of hidden fields is usually set via server or client side scripting.
	 */
	public static final FormControlType HIDDEN=new FormControlType("hidden",Tag.INPUT,false,false).register();

	/**
	 * The form control type given to a <a href="#submit-button">submit button</a> control implemented using an
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT">INPUT</a></code> element with attribute
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-INPUT">type</a>="<a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#input-control-types">image</a>"</code>.
	 * <p>
	 * See the description under the heading "image" in the
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#h-17.4.1">HTML 4.01 specification section 17.4.1 - Form control types created with INPUT</a>.
	 * <p>
	 * When a {@linkplain FormControl form control} of type <code>IMAGE</code> is present in the form used to
	 * {@linkplain FormFields#FormFields(Collection) construct} a {@link FormFields} instance, three separate
	 * {@link FormField} objects are created for the one control.
	 * One has the {@linkplain FormField#getName() name} specified in the
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-name-INPUT">name</a></code>
	 * attribute of the <code>INPUT</code> element, and the other two have this name with the suffixes
	 * "<code>.x</code>" and "<code>.y</code>" appended to them to represent the additional
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#idx-coordinates">click coordinates</a>
	 * submitted by this control when activated using a pointing device.
	 * <p>
	 * This type of control is also mentioned in the
	 * <a target="_blank" href="http://www.w3.org/TR/html401/struct/objects.html#h-13.6.2">HTML 4.01 specification section 13.6.2 - Server-side image maps</a>.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;input type="image" name="FieldName" src="ImageURL" value="PredefinedValue" /&gt;</code>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <code>{@link #getElementName()} = {@link Tag#INPUT}</code><br />
	 *    <code>{@link #hasPredefinedValue()} = true</code><br />
	 *    <code>{@link #isSubmit()} = true</code><br />
	 * </dl>
	 */
	public static final FormControlType IMAGE=new FormControlType("image",Tag.INPUT,true,true).register();

	/**
	 * The form control type given to a <a href="#text-input-control">text input</a> control implemented using an
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT">INPUT</a></code> element with attribute
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-INPUT">type</a>="<a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#idx-password_input_control">password</a>"</code>.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;input type="password" name="FieldName" value="DefaultValue" /&gt;</code>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <code>{@link #getElementName()} = {@link Tag#INPUT}</code><br />
	 *    <code>{@link #hasPredefinedValue()} = false</code><br />
	 *    <code>{@link #isSubmit()} = false</code><br />
	 * </dl>
	 */
	public static final FormControlType PASSWORD=new FormControlType("password",Tag.INPUT,false,false).register();

	/**
	 * The form control type given to a <a href="#radio-button-control">radio button</a> control.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;input type="radio" name="FieldName" value="PredefinedValue" /&gt;</code>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <code>{@link #getElementName()} = {@link Tag#INPUT}</code><br />
	 *    <code>{@link #hasPredefinedValue()} = true</code><br />
	 *    <code>{@link #isSubmit()} = false</code><br />
	 * </dl>
	 */
	public static final FormControlType RADIO=new FormControlType("radio",Tag.INPUT,true,false).register();

	/**
	 * The form control type given to a <a href="#menu-control">menu</a> control implemented using a
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-SELECT">SELECT</a></code> element containing
	 * the attribute "<code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-multiple">multiple</a></code>".
	 * <p>
	 * <code>SELECT</code> elements that do not contain the attribute "<code>multiple</code>" are represented by the
	 * {@link #SELECT_SINGLE} form control type.
	 * <p>
	 * This is the only control type that can have multiple
	 * <a href="FormControl.html#SubmissionValue">submission values</a> within the one control.
	 * Contrast this with {@link #CHECKBOX} controls, which require multiple separate controls with the same
	 * {@linkplain FormControl#getName() name} in order to contribute multiple submission values.
	 * <p>
	 * The individual {@link Tag#OPTION OPTION} elements contained within a {@linkplain FormControl form control} of this type can be
	 * obtained using the {@link FormControl#getOptionElementIterator()} method.
	 * <p>
	 * The most efficient way to test whether a form control type is either <code>SELECT_MULTIPLE</code> or <code>SELECT_SINGLE</code>
	 * is to test for {@link #getElementName()}<code>==</code>{@link Tag#SELECT}.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *   <dd>
	 *    <code>
	 *     &lt;select name="FieldName" multiple&gt;<br />
	 *     &nbsp; &lt;option value="PredefinedValue1" selected&gt;Display Text1&lt;/option&gt;<br />
	 *     &nbsp; &lt;option value="PredefinedValue2"&gt;Display Text2&lt;/option&gt;<br />
	 *     &lt;/select&gt;
	 *    </code>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <code>{@link #getElementName()} = {@link Tag#SELECT}</code><br />
	 *    <code>{@link #hasPredefinedValue()} = true</code><br />
	 *    <code>{@link #isSubmit()} = false</code><br />
	 * </dl>
	 */
	public static final FormControlType SELECT_MULTIPLE=new FormControlType("select_multiple",Tag.SELECT,true,false).register();

	/**
	 * The form control type given to a <a href="#menu-control">menu</a> control implemented using a
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-SELECT">SELECT</a></code> element that does
	 * <b>not</b> contain the attribute "<code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-multiple">multiple</a></code>".
	 * <p>
	 * <code>SELECT</code> elements that do contain the attribute "<code>multiple</code>" are represented by the
	 * {@link #SELECT_MULTIPLE} form control type.
	 * <p>
	 * The individual {@link Tag#OPTION OPTION} elements contained within a {@linkplain FormControl form control} of this type can be
	 * obtained using the {@link FormControl#getOptionElementIterator()} method.
	 * <p>
	 * The most efficient way to test whether a form control type is either <code>SELECT_MULTIPLE</code> or <code>SELECT_SINGLE</code>
	 * is to test for {@link #getElementName()}<code>==</code>{@link Tag#SELECT}.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *   <dd>
	 *    <code>
	 *     &lt;select name="FieldName"&gt;<br />
	 *     &nbsp; &lt;option value="PredefinedValue1" selected&gt;Display Text1&lt;/option&gt;<br />
	 *     &nbsp; &lt;option value="PredefinedValue2"&gt;Display Text2&lt;/option&gt;<br />
	 *     &lt;/select&gt;
	 *    </code>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <code>{@link #getElementName()} = {@link Tag#SELECT}</code><br />
	 *    <code>{@link #hasPredefinedValue()} = true</code><br />
	 *    <code>{@link #isSubmit()} = false</code><br />
	 * </dl>
	 */
	public static final FormControlType SELECT_SINGLE=new FormControlType("select_single",Tag.SELECT,true,false).register();

	/**
	 * The form control type given to a <a href="#submit-button">submit button</a> control implemented using an
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT">INPUT</a></code> element with attribute
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-INPUT">type</a>="submit"</code>.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;input type="submit" name="FieldName" value="PredefinedValue" /&gt;</code>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <code>{@link #getElementName()} = {@link Tag#INPUT}</code><br />
	 *    <code>{@link #hasPredefinedValue()} = true</code><br />
	 *    <code>{@link #isSubmit()} = true</code><br />
	 * </dl>
	 */
	public static final FormControlType SUBMIT=new FormControlType("submit",Tag.INPUT,true,true).register();

	/**
	 * The form control type given to a <a href="#text-input-control">text input</a> control implemented using an
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-INPUT">INPUT</a></code> element with attribute
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#adef-type-INPUT">type</a>="text"</code>.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;input type="text" name="FieldName" value="DefaultValue" /&gt;</code>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <code>{@link #getElementName()} = {@link Tag#INPUT}</code><br />
	 *    <code>{@link #hasPredefinedValue()} = false</code><br />
	 *    <code>{@link #isSubmit()} = false</code><br />
	 * </dl>
	 */
	public static final FormControlType TEXT=new FormControlType("text",Tag.INPUT,false,false).register();

	/**
	 * The form control type given to a <a href="#text-input-control">text input</a> control implemented using a
	 * <code><a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#edef-TEXTAREA">TEXTAREA</a></code> element.
	 * <p>
	 * <dl>
	 *  <dt>Example:</dt>
	 *   <dd><code>&lt;textarea name="FieldName"&gt;Default Value&lt;/textarea&gt;</code>
	 *  <dt>Properties:</dt>
	 *   <dd>
	 *    <code>{@link #getElementName()} = {@link Tag#TEXTAREA}</code><br />
	 *    <code>{@link #hasPredefinedValue()} = false</code><br />
	 *    <code>{@link #isSubmit()} = false</code><br />
	 * </dl>
	 */
	public static final FormControlType TEXTAREA=new FormControlType("textarea",Tag.TEXTAREA,false,false).register();

	private FormControlType(final String formControlTypeId, final String elementName, final boolean hasPredefinedValue, final boolean submit) {
		this.formControlTypeId=formControlTypeId;
		this.elementName=elementName;
		this.hasPredefinedValue=hasPredefinedValue;
		this.submit=submit;
	}

	private FormControlType register() {
		ID_MAP.put(formControlTypeId,this);
		if (elementName==Tag.INPUT) INPUT_ELEMENT_TYPE_MAP.put(formControlTypeId,this);
		return this;
	}

	/**
	 * Returns the {@linkplain Element#getName() name} of the {@link Element} that constitues this form control type.
	 * @return the {@linkplain Element#getName() name} of the {@link Element} that constitues this form control type.
	 */
	public String getElementName() {
		return elementName;
	}

	/**
	 * Indicates whether any <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#current-value">value</a>
	 * submitted by this type of control is predefined in the HTML and typically not modified by the user or server/client scripts.
	 * <p>
	 * The word "typically" is used because the use of client side scripts can cause
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#h-17.2.1">control types</a>
	 * which normally have predefined values to be set by the user, which is a condition which is beyond
	 * the scope of this library to test for.
	 * <p>
	 * The predefined value is defined by the control's <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#initial-value">initial value</a>.
	 * <p>
	 * A return value of <code>true</code> signifies that a form control of this type is a
	 * <a href="FormControl.html#PredefinedValueControl">predefined value control</a>.
	 * <p>
	 * A return value of <code>false</code> signifies that a form control of this type is a
	 * <a href="FormControl.html#UserValueControl">user value control</a>.
	 * <p>
	 * Note that the {@link #HIDDEN} type returns <code>false</code> for this method because the value of hidden fields is usually set via server or client side scripting.
	 *
	 * @return <code>true</code> if any <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#current-value">value</a> submitted by this type of control is predefined in the HTML and typically not modified by the user or server/client scripts, otherwise <code>false</code>.
	 */
	public boolean hasPredefinedValue() {
		return hasPredefinedValue;
	}

	/**
	 * Indicates whether this control type causes the form to be <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#submit-format">submitted</a>.
	 * <p>
	 * Returns <code>true</code> only for the {@link #SUBMIT}, {@link #BUTTON}, and {@link #IMAGE} instances.
	 *
	 * @return <code>true</code> if this control type causes the form to be <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#submit-format">submitted</a>, otherwise <code>false</code>.
	 */
	public boolean isSubmit() {
		return submit;
	}

	/**
	 * Returns the {@linkplain Element#getName() name} of the {@link Element} that constitues this form control type.
	 * <p>
	 * This method has been deprecated as of version 2.0 and replaced with the exactly equivalent
	 * {@link #getElementName()} method for aesthetical reasons.
	 *
	 * @return the {@linkplain Element#getName() name} of the {@link Element} that constitues this form control type.
	 * @deprecated  Use {@link #getElementName() getElementName()} instead.
	 */
	public String getTagName() {
		return elementName;
	}

	/**
	 * Indicates whether any <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#current-value">value</a>
	 * submitted by this type of control is predefined in the HTML and typically not modified by the user or server/client scripts.
	 * <p>
	 * This method has been deprecated as of version 2.0 and replaced with the exactly equivalent
	 * {@link #hasPredefinedValue()} method for aesthetical reasons.
	 *
	 * @return <code>true</code> if any <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#current-value">value</a> submitted by this type of control is predefined in the HTML and typically not modified by the user or server/client scripts, otherwise <code>false</code>.
	 * @deprecated  Use {@link #hasPredefinedValue() hasPredefinedValue()} instead.
	 */
	public boolean isPredefinedValue() {
		return hasPredefinedValue;
	}

	/**
	 * Indicates whether more than one control of this type with the same <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#control-name">name</a> can be <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#successful-controls">successful</a>.
	 * <p>
	 * Returns <code>false</code> only for the {@link #RADIO}, {@link #SUBMIT}, {@link #BUTTON}, and {@link #IMAGE} instances.
	 * <p>
	 * Note that before version 1.4.1 this method also returned <code>false</code> for the {@link #SELECT_SINGLE} instance.
	 * This was a bug resulting from confusion as to whether each <code>OPTION</code> element in a
	 * <code>SELECT</code> element constituted a control (since it is possible for multiple options to be
	 * <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#successful-controls">successful</a>)
	 * or only the <code>SELECT</code> element as a whole.
	 * Now that the control is clearly defined as the entire <code>SELECT</code> element,
	 * it is clear that multiple {@link #SELECT_SINGLE} controls with the same name result in multiple values.
	 * <p>
	 * Because this may not be immediately intuitive, and the method is no longer used internally,
	 * this method has been deprecated as of version 2.0 to avoid any further confusion.
	 *
	 * @return <code>true</code> if more than one control of this type with the same <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#control-name">name</a> can be <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#successful-controls">successful</a>, otherwise <code>false</code>.
	 * @deprecated  Use the more useful {@link FormField#allowsMultipleValues()} method instead.
	 */
	public boolean allowsMultipleValues() {
		return !(this==RADIO || isSubmit());
	}

	/**
	 * Returns a string which identifies this form control type.
	 * <p>
	 * This is the same as the control type's static field name in lower case, which is one of<br />
	 * <code>button</code>, <code>checkbox</code>, <code>file</code>, <code>hidden</code>, <code>image</code>,
	 * <code>password</code>, <code>radio</code>, <code>select_multiple</code>, <code>select_single</code>,
	 * <code>submit</code>, <code>text</code>, or <code>textarea</code>.
	 * <p>
	 * This method has been deprecated as of version 2.0 as it has no practical use.
	 *
	 * @return a string which identifies this form control type.
	 * @deprecated  Use {@link #toString() toString()} instead.
	 */
	public String getFormControlTypeId() {
		return formControlTypeId;
	}

	/**
	 * Returns the {@link FormControlType} with the specified {@linkplain #getFormControlTypeId() ID}.
	 * <p>
	 * This method has been deprecated as of version 2.0 as it has no practical use.
	 * <p>
	 * @param formControlTypeId  the ID of a form control type.
	 * @return the {@link FormControlType} with the specified ID, or <code>null</code> if no such control exists.
	 * @see #getFormControlTypeId()
	 * @deprecated  no replacement
	 */
	public static FormControlType get(final String formControlTypeId) {
		return (FormControlType)ID_MAP.get(formControlTypeId);
	}

	/**
	 * Returns an array containing the additional field names submitted if a control of this type with the specified <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#control-name">name</a> is <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#successful-controls">successful</a>.
	 * <p>
	 * Returns <code>null</code> for all control types except {@link #IMAGE}.
	 * It relates to the extra <code><i>name</i>.x</code> and <code><i>name</i>.y</code> data submitted when a pointing device is used to activate an IMAGE control.
	 * <p>
 	 * This method has been deprecated as of version 2.0 as it is no longer used internally and
	 * has no practical use as a public method.
	 *
	 * @param name  the <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#control-name">name</a> of a form control.
	 * @return an array containing the additional field names submitted if a control of this type with the specified <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#control-name">name</a> is <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#successful-controls">successful</a>, or <code>null</code> if none.
	 * @deprecated  no replacement
	 */
	public String[] getAdditionalSubmitNames(final String name) {
		if (this!=IMAGE) return null;
		final String[] names=new String[2];
		names[0]=name+".x";
		names[1]=name+".y";
		return names;
	}

	/**
	 * Indicates whether an HTML tag with the specified name is potentially a form <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#form-controls">control</a>.
	 * <p>
	 * Returns <code>true</code> if the specified tag name is one of
	 * "input", "textarea", "button" or "select" (ignoring case).
	 * <p>
 	 * This method has been deprecated as of version 2.0 as it is no longer used internally and
	 * has no practical use as a public method.
	 *
	 * @param tagName  the name of an HTML tag.
	 * @return <code>true</code> if an HTML tag with the specified name is potentially a form <a target="_blank" href="http://www.w3.org/TR/html401/interact/forms.html#form-controls">control</a>, otherwise <code>false</code>.
	 * @deprecated  no replacement
	 */
	public static boolean isPotentialControl(final String tagName) {
		return tagName.equalsIgnoreCase(Tag.INPUT) || tagName.equalsIgnoreCase(Tag.TEXTAREA) || tagName.equalsIgnoreCase(Tag.BUTTON) || tagName.equalsIgnoreCase(Tag.SELECT);
	}

	/**
	 * Returns a string representation of this object useful for debugging purposes.
	 * @return a string representation of this object useful for debugging purposes.
	 */
	public String toString() {
		return formControlTypeId;
	}

	static FormControlType getFromInputElementType(final String typeAttributeValue) {
		return (FormControlType)INPUT_ELEMENT_TYPE_MAP.get(typeAttributeValue);
	}
}
