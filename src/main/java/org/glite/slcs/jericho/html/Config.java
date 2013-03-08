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

import java.util.Map;

/**
 * Encapsulates global configuration properties which determine the behaviour of various functions.
 * <p>
 * All of the properties in this class are static, affecting all objects and threads.
 * Multiple concurrent configurations are not possible.
 * <p>
 * Properties that relate to <a target="_blank" href="http://www.w3.org/TR/html401/conform.html#didx-user_agent">user agent</a>
 * compatibility issues are stored in instances of the {@link Config.CompatibilityMode} class.
 * This allows all of the properties in the compatibility mode to be set as a block by setting the static
 * {@link #CurrentCompatibilityMode} property to a different instance.
 *
 * @see Config.CompatibilityMode
 */ 
public final class Config {
	private Config() {}

	/**
	 * Determines the string used to separate a single column's multiple values in the output of the {@link FormFields#getColumnValues(Map)} method.
	 * <p>
	 * The situation where a single column has multiple values only arises if {@link FormField#getUserValueCount()}<code>&gt;1</code>
	 * on the relevant form field, which usually indicates a poorly designed form.
	 * <p>
	 * The default value is "<code>,</code>" (a comma, not including the quotes).
	 * <p>
	 * Must not be <code>null</code>.
	 */
	public static String ColumnMultipleValueSeparator=",";

	/**
	 * Determines the string that represents the value <code>true</code> in the output of the {@link FormFields#getColumnValues(Map)} method.
	 * <p>
	 * The default value is "<code>true</code>" (without the quotes).
	 * <p>
	 * Must not be <code>null</code>.
	 */
	public static String ColumnValueTrue=Boolean.toString(true);

	/**
	 * Determines the string that represents the value <code>false</code> in the output of the {@link FormFields#getColumnValues(Map)} method.
	 * <p>
	 * The default value is <code>null</code>, which represents no output at all.
	 */
	public static String ColumnValueFalse=null;

	/**
	 * Determines the currently active {@linkplain Config.CompatibilityMode compatibility mode}.
	 * <p>
	 * The default setting is {@link Config.CompatibilityMode#IE} (MS Internet Explorer 6.0).
	 * <p>
	 * Must not be <code>null</code>.
	 */
	public static CompatibilityMode CurrentCompatibilityMode=CompatibilityMode.IE;

	/**
	 * Determines whether apostrophes are encoded when calling the {@link CharacterReference#encode(CharSequence)} method.
	 * <p>
	 * A value of <code>false</code> means {@linkplain CharacterEntityReference#_apos apostrophe}
	 * (U+0027) characters are not encoded.
	 * The only time apostrophes need to be encoded is within an attribute value delimited by
	 * single quotes (apostrophes), so in most cases ignoring apostrophes is perfectly safe and
	 * enhances the readability of the source document.
	 * <p>
	 * Note that apostrophes are always encoded as a {@linkplain NumericCharacterReference numeric character reference}, never as the
	 * character entity reference {@link CharacterEntityReference#_apos &amp;apos;}.
	 * <p>
	 * The default value is <code>false</code>.
	 */
	public static boolean IsApostropheEncoded=false;

	/**
	 * Used in Element.getChildElements.
	 * Will only make this public if someone makes a convincing argument why you would ever need to include server tags in an element hierarchy.
	 */ 
	static final boolean IncludeServerTagsInElementHierarchy=false;

	/**
	 * Represents a set of maximum unicode code points to be recognised for the three types of
	 * <a href="CharacterReference.html#Unterminated">unterminated</a> character reference in a given context.
	 * <p>
	 * The three types of character reference are:
	 * <ul>
	 *  <li>{@linkplain CharacterEntityReference Character entity reference}
	 *  <li><a href="NumericCharacterReference.html#DecimalCharacterReference">Decimal character reference</a>
	 *  <li><a href="NumericCharacterReference.html#HexadecimalCharacterReference">Hexadecimal character reference</a>
	 * </ul>
	 * <p>
	 * The two types of contexts used in this library are:
	 * <ul>
	 *  <li>Inside an attribute value
	 *  <li>Outside an attribute value
	 * </ul>
	 */ 
	static class UnterminatedCharacterReferenceSettings {
		// use volatile fields to make them thread safe
		public volatile int characterEntityReferenceMaxCodePoint;
		public volatile int decimalCharacterReferenceMaxCodePoint;
		public volatile int hexadecimalCharacterReferenceMaxCodePoint;

		public static UnterminatedCharacterReferenceSettings ACCEPT_ALL=new UnterminatedCharacterReferenceSettings(CompatibilityMode.CODE_POINTS_ALL,CompatibilityMode.CODE_POINTS_ALL,CompatibilityMode.CODE_POINTS_ALL);

		public UnterminatedCharacterReferenceSettings() {
			this(CompatibilityMode.CODE_POINTS_NONE,CompatibilityMode.CODE_POINTS_NONE,CompatibilityMode.CODE_POINTS_NONE);
		}

		public UnterminatedCharacterReferenceSettings(final int characterEntityReferenceMaxCodePoint, final int decimalCharacterReferenceMaxCodePoint, final int hexadecimalCharacterReferenceMaxCodePoint) {
			this.characterEntityReferenceMaxCodePoint=characterEntityReferenceMaxCodePoint;
			this.decimalCharacterReferenceMaxCodePoint=decimalCharacterReferenceMaxCodePoint;
			this.hexadecimalCharacterReferenceMaxCodePoint=hexadecimalCharacterReferenceMaxCodePoint;
		}

		public String toString() {
			return "\n    Character entity reference: "+getDescription(characterEntityReferenceMaxCodePoint)
						+"\n    Decimal character reference: "+getDescription(decimalCharacterReferenceMaxCodePoint)
						+"\n    Haxadecimal character reference: "+getDescription(hexadecimalCharacterReferenceMaxCodePoint);
		}

		private String getDescription(final int codePoint) {
			if (codePoint==CompatibilityMode.CODE_POINTS_NONE) return "None";
			if (codePoint==CompatibilityMode.CODE_POINTS_ALL) return "All";
			return "0x"+Integer.toString(codePoint,16);
		}
	}

	/**
	 * Represents a set of configuration parameters that relate to
	 * <a target="_blank" href="http://www.w3.org/TR/html401/conform.html#didx-user_agent">user agent</a> compatibility issues.
	 * <p>
	 * The predefined compatibility modes {@link #IE}, {@link #MOZILLA}, {@link #OPERA} and {@link #XHTML} provide an easy means of
	 * ensuring the library interprets the markup in a way consistent with some of the most commonly used browsers,
	 * at least in relation to the behaviour described by the properties in this class.
	 * <p>
	 * The properties of any <code>CompatibilityMode</code> object can be modified individually, including those in
	 * the predefined instances as well as newly constructed instances.
	 * Take note however that modifying the properties of the predefined instances has a global affect.
	 * <p>
	 * The currently active compatibility mode is stored in the static 
	 * {@link Config#CurrentCompatibilityMode} property.
	 * <p>
	 */
	public static final class CompatibilityMode {
		private String name;
		private volatile boolean formFieldNameCaseInsensitive;
		volatile UnterminatedCharacterReferenceSettings unterminatedCharacterReferenceSettingsInsideAttributeValue;
		volatile UnterminatedCharacterReferenceSettings unterminatedCharacterReferenceSettingsOutsideAttributeValue;

		/**
		 * Indicates the recognition of all unicode code points.
		 * <p>
		 * This value is used in properties which specify a maximum unicode code point to be recognised by the parser.
		 *
		 * @see #getUnterminatedCharacterEntityReferenceMaxCodePoint(boolean insideAttributeValue)
		 * @see #getUnterminatedDecimalCharacterReferenceMaxCodePoint(boolean insideAttributeValue)
		 * @see #getUnterminatedHexadecimalCharacterReferenceMaxCodePoint(boolean insideAttributeValue)
		 */
		public static final int CODE_POINTS_ALL=CharacterReference.MAX_CODE_POINT;

		/**
		 * Indicates the recognition of no unicode code points.
		 * <p>
		 * This value is used in properties which specify a maximum unicode code point to be recognised by the parser.
		 *
		 * @see #getUnterminatedCharacterEntityReferenceMaxCodePoint(boolean insideAttributeValue)
		 * @see #getUnterminatedDecimalCharacterReferenceMaxCodePoint(boolean insideAttributeValue)
		 * @see #getUnterminatedHexadecimalCharacterReferenceMaxCodePoint(boolean insideAttributeValue)
		 */
		public static final int CODE_POINTS_NONE=CharacterReference.INVALID_CODE_POINT;

		/**
		 * <a target="_blank" href="http://www.microsoft.com/windows/ie/">Microsoft Internet Explorer</a> compatibility mode.
		 * <p>
		 * <code>{@link #getName() Name} = IE</code><br />
		 * <code>{@link #isFormFieldNameCaseInsensitive() FormFieldNameCaseInsensitive} = true</code><br />
		 * <table cellspacing="0" cellpadding="0">
		 *  <tr><th>Recognition of unterminated character references:<th><th align="center">&nbsp; (inside attribute) &nbsp;<th align="center">&nbsp; (outside attribute) &nbsp;
		 *  <tr><td>{@link #getUnterminatedCharacterEntityReferenceMaxCodePoint(boolean) UnterminatedCharacterEntityReferenceMaxCodePoint}<td><code>&nbsp;=</code><td align="center">U+00FF<td align="center">U+00FF
		 *  <tr><td>{@link #getUnterminatedDecimalCharacterReferenceMaxCodePoint(boolean) UnterminatedDecimalCharacterReferenceMaxCodePoint}<td><code>&nbsp;=</code><td align="center">{@linkplain #CODE_POINTS_ALL All}<td align="center">{@linkplain #CODE_POINTS_ALL All}
		 *  <tr><td>{@link #getUnterminatedHexadecimalCharacterReferenceMaxCodePoint(boolean) UnterminatedHexadecimalCharacterReferenceMaxCodePoint}<td><code>&nbsp;=</code><td align="center">{@linkplain #CODE_POINTS_ALL All}<td align="center">{@linkplain #CODE_POINTS_NONE None}
		 * </table>		 
		 */
		public static final CompatibilityMode IE=new CompatibilityMode("IE",true,
			new UnterminatedCharacterReferenceSettings(0xFF, CODE_POINTS_ALL, CODE_POINTS_ALL), // inside attributes
			new UnterminatedCharacterReferenceSettings(0xFF, CODE_POINTS_ALL, CODE_POINTS_NONE) // outside attributes
		);

		/**
		 * <a target="_blank" href="http://www.mozilla.org/products/mozilla1.x/">Mozilla</a> / 
		 * <a target="_blank" href="http://www.mozilla.org/products/firefox/">Firefox</a> /
		 * <a target="_blank" href="http://browser.netscape.com/">Netscape</a> compatibility mode.
		 * <p>
		 * <code>{@link #getName() Name} = Mozilla</code><br />
		 * <code>{@link #isFormFieldNameCaseInsensitive() FormFieldNameCaseInsensitive} = false</code><br />
		 * <table cellspacing="0" cellpadding="0">
		 *  <tr><th>Recognition of unterminated character references:<th><th align="center">&nbsp; (inside attribute) &nbsp;<th align="center">&nbsp; (outside attribute) &nbsp;
		 *  <tr><td>{@link #getUnterminatedCharacterEntityReferenceMaxCodePoint(boolean) UnterminatedCharacterEntityReferenceMaxCodePoint}<td><code>&nbsp;=</code><td align="center">U+00FF<td align="center">{@linkplain #CODE_POINTS_ALL All}
		 *  <tr><td>{@link #getUnterminatedDecimalCharacterReferenceMaxCodePoint(boolean) UnterminatedDecimalCharacterReferenceMaxCodePoint}<td><code>&nbsp;=</code><td align="center">{@linkplain #CODE_POINTS_ALL All}<td align="center">{@linkplain #CODE_POINTS_ALL All}
		 *  <tr><td>{@link #getUnterminatedHexadecimalCharacterReferenceMaxCodePoint(boolean) UnterminatedHexadecimalCharacterReferenceMaxCodePoint}<td><code>&nbsp;=</code><td align="center">{@linkplain #CODE_POINTS_ALL All}<td align="center">{@linkplain #CODE_POINTS_ALL All}
		 * </table>		 
		 */
		public static final CompatibilityMode MOZILLA=new CompatibilityMode("Mozilla",false,
			new UnterminatedCharacterReferenceSettings(0xFF,            CODE_POINTS_ALL, CODE_POINTS_ALL), // inside attributes
			new UnterminatedCharacterReferenceSettings(CODE_POINTS_ALL, CODE_POINTS_ALL, CODE_POINTS_ALL) // outside attributes
		);

		/**
		 * Opera compatibility mode.
		 * <p>
		 * <code>{@link #getName() Name} = Opera</code><br />
		 * <code>{@link #isFormFieldNameCaseInsensitive() FormFieldNameCaseInsensitive} = true</code><br />
		 * <table cellspacing="0" cellpadding="0">
		 *  <tr><th>Recognition of unterminated character references:<th><th align="center">&nbsp; (inside attribute) &nbsp;<th align="center">&nbsp; (outside attribute) &nbsp;
		 *  <tr><td>{@link #getUnterminatedCharacterEntityReferenceMaxCodePoint(boolean) UnterminatedCharacterEntityReferenceMaxCodePoint}<td><code>&nbsp;=</code><td align="center">U+003E<td align="center">{@linkplain #CODE_POINTS_ALL All}
		 *  <tr><td>{@link #getUnterminatedDecimalCharacterReferenceMaxCodePoint(boolean) UnterminatedDecimalCharacterReferenceMaxCodePoint}<td><code>&nbsp;=</code><td align="center">{@linkplain #CODE_POINTS_ALL All}<td align="center">{@linkplain #CODE_POINTS_ALL All}
		 *  <tr><td>{@link #getUnterminatedHexadecimalCharacterReferenceMaxCodePoint(boolean) UnterminatedHexadecimalCharacterReferenceMaxCodePoint}<td><code>&nbsp;=</code><td align="center">{@linkplain #CODE_POINTS_ALL All}<td align="center">{@linkplain #CODE_POINTS_ALL All}
		 * </table>		 
		 */
		public static final CompatibilityMode OPERA=new CompatibilityMode("Opera",true,
			new UnterminatedCharacterReferenceSettings(0x3E,            CODE_POINTS_ALL, CODE_POINTS_ALL), // inside attributes
			new UnterminatedCharacterReferenceSettings(CODE_POINTS_ALL, CODE_POINTS_ALL, CODE_POINTS_ALL) // outside attributes
		);

		/**
		 * <a target="_blank" href="http://www.w3.org/TR/xhtml1/#xhtml">XHTML</a> compatibility mode.
		 * <p>
		 * <code>{@link #getName() Name} = XHTML</code><br />
		 * <code>{@link #isFormFieldNameCaseInsensitive() FormFieldNameCaseInsensitive} = false</code><br />
		 * <table cellspacing="0" cellpadding="0">
		 *  <tr><th>Recognition of unterminated character references:<th><th align="center">&nbsp; (inside attribute) &nbsp;<th align="center">&nbsp; (outside attribute) &nbsp;
		 *  <tr><td>{@link #getUnterminatedCharacterEntityReferenceMaxCodePoint(boolean) UnterminatedCharacterEntityReferenceMaxCodePoint}<td><code>&nbsp;=</code><td align="center">{@linkplain #CODE_POINTS_NONE None}<td align="center">{@linkplain #CODE_POINTS_NONE None}
		 *  <tr><td>{@link #getUnterminatedDecimalCharacterReferenceMaxCodePoint(boolean) UnterminatedDecimalCharacterReferenceMaxCodePoint}<td><code>&nbsp;=</code><td align="center">{@linkplain #CODE_POINTS_NONE None}<td align="center">{@linkplain #CODE_POINTS_NONE None}
		 *  <tr><td>{@link #getUnterminatedHexadecimalCharacterReferenceMaxCodePoint(boolean) UnterminatedHexadecimalCharacterReferenceMaxCodePoint}<td><code>&nbsp;=</code><td align="center">{@linkplain #CODE_POINTS_NONE None}<td align="center">{@linkplain #CODE_POINTS_NONE None}
		 * </table>		 
		 */
		public static final CompatibilityMode XHTML=new CompatibilityMode("XHTML");

		/**
		 * Constructs a new <code>CompatibilityMode</code> with the given {@linkplain #getName() name}.
		 * <p>
		 * All properties in the new instance are initially assigned their default values, which are the same as the strict
		 * rules of the {@link #XHTML} compatibility mode.
		 *
		 * @param name  the {@linkplain #getName() name} of the new compatibility mode
		 */
		public CompatibilityMode(final String name) {
			this(name,false,new UnterminatedCharacterReferenceSettings(),new UnterminatedCharacterReferenceSettings());
		}

		private CompatibilityMode(final String name, final boolean formFieldNameCaseInsensitive, final UnterminatedCharacterReferenceSettings unterminatedCharacterReferenceSettingsInsideAttributeValue, final UnterminatedCharacterReferenceSettings unterminatedCharacterReferenceSettingsOutsideAttributeValue) {
			this.name=name;
			this.formFieldNameCaseInsensitive=formFieldNameCaseInsensitive;
			this.unterminatedCharacterReferenceSettingsInsideAttributeValue=unterminatedCharacterReferenceSettingsInsideAttributeValue;
			this.unterminatedCharacterReferenceSettingsOutsideAttributeValue=unterminatedCharacterReferenceSettingsOutsideAttributeValue;
		}

		/**
		 * Returns the name of this compatibility mode.
		 * @return the name of this compatibility mode.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Indicates whether {@linkplain FormField#getName() form field names} are treated as case insensitive.
		 * <p>
		 * Microsoft Internet Explorer treats field names as case insensitive,
		 * while Mozilla treats them as case sensitive.
		 * <p>
		 * The value of this property in the {@linkplain Config#CurrentCompatibilityMode current compatibility mode}
		 * affects all instances of the {@link FormFields} class.
		 * It should be set to the desired configuration before any instances of <code>FormFields</code> are created.
		 *
		 * @return <code>true</code> if {@linkplain FormField#getName() form field names} are treated as case insensitive, otherwise <code>false</code>.
		 * @see #setFormFieldNameCaseInsensitive(boolean)
		 */
		public boolean isFormFieldNameCaseInsensitive() {
			return formFieldNameCaseInsensitive;
		}

		/**
		 * Sets whether {@linkplain FormField#getName() form field names} are treated as case insensitive.
		 * <p>
		 * See {@link #isFormFieldNameCaseInsensitive()} for the documentation of this property.
		 *
		 * @param value  the new value of the property
		 */
		public void setFormFieldNameCaseInsensitive(final boolean value) {
			formFieldNameCaseInsensitive=value;
		}

		/**
		 * Returns the maximum unicode code point of an <a href="CharacterReference.html#Unterminated">unterminated</a>
		 * {@linkplain CharacterEntityReference character entity reference} which is to be recognised in the specified context.
		 * <p>
		 * For example, if <code>getUnterminatedCharacterEntityReferenceMaxCodePoint(true)</code> has the value <code>0xFF</code> (U+00FF)
		 * in the {@linkplain Config#CurrentCompatibilityMode current compatibility mode}, then:
		 * <ul>
		 *  <li>{@link CharacterReference#decode(CharSequence,boolean) CharacterReference.decode("&amp;gt",true)}
		 *   returns "<code>&gt;</code>".<br />
		 *   The string is recognised as the character entity reference {@link CharacterEntityReference#_gt &amp;gt;}
		 *   despite the fact that it is <a href="CharacterReference.html#Unterminated">unterminated</a>,
		 *   because its unicode code point U+003E is below the maximum of U+00FF set by this property.
		 *  <li>{@link CharacterReference#decode(CharSequence,boolean) CharacterReference.decode("&amp;euro",true)}
		 *   returns "<code>&amp;euro</code>".<br />
		 *   The string is not recognised as the character entity reference {@link CharacterEntityReference#_euro &amp;euro;}
		 *   because it is <a href="CharacterReference.html#Unterminated">unterminated</a>
		 *   and its unicode code point U+20AC is above the maximum of U+00FF set by this property.
		 * </ul>
		 * <p>
		 * See the documentation of the {@link Attribute#getValue()} method for further discussion.
		 * 
		 * @param insideAttributeValue  the context within an HTML document - <code>true</code> if inside an attribute value or <code>false</code> if outside an attribute value.
		 * @return the maximum unicode code point of an <a href="CharacterReference.html#Unterminated">unterminated</a> {@linkplain CharacterEntityReference character entity reference} which is to be recognised in the specified context.
		 * @see #setUnterminatedCharacterEntityReferenceMaxCodePoint(boolean insideAttributeValue, int maxCodePoint)
		 */
		public int getUnterminatedCharacterEntityReferenceMaxCodePoint(final boolean insideAttributeValue) {
			return getUnterminatedCharacterReferenceSettings(insideAttributeValue).characterEntityReferenceMaxCodePoint;
		}

		/**
		 * Sets the maximum unicode code point of an <a href="CharacterReference.html#Unterminated">unterminated</a>
		 * {@linkplain CharacterEntityReference character entity reference} which is to be recognised in the specified context.
		 * <p>
		 * See {@link #getUnterminatedCharacterEntityReferenceMaxCodePoint(boolean insideAttributeValue)} for the documentation of this property.
		 *
		 * @param insideAttributeValue  the context within an HTML document - <code>true</code> if inside an attribute value or <code>false</code> if outside an attribute value.
		 * @param maxCodePoint  the maximum unicode code point.
		 */
		public void setUnterminatedCharacterEntityReferenceMaxCodePoint(final boolean insideAttributeValue, final int maxCodePoint) {
			getUnterminatedCharacterReferenceSettings(insideAttributeValue).characterEntityReferenceMaxCodePoint=maxCodePoint;
		}

		/**
		 * Returns the maximum unicode code point of an <a href="CharacterReference.html#Unterminated">unterminated</a>
		 * <a href="NumericCharacterReference.html#DecimalCharacterReference">decimal character reference</a> which is to be recognised in the specified context.
		 * <p>
		 * For example, if <code>getUnterminatedDecimalCharacterReferenceMaxCodePoint(true)</code> had the hypothetical value <code>0xFF</code> (U+00FF)
		 * in the {@linkplain Config#CurrentCompatibilityMode current compatibility mode}, then:
		 * <ul>
		 *  <li>{@link CharacterReference#decode(CharSequence,boolean) CharacterReference.decode("&amp;#62",true)}
		 *   returns "<code>&gt;</code>".<br />
		 *   The string is recognised as the numeric character reference <code>&amp;#62;</code>
		 *   despite the fact that it is <a href="CharacterReference.html#Unterminated">unterminated</a>,
		 *   because its unicode code point U+003E is below the maximum of U+00FF set by this property.
		 *  <li>{@link CharacterReference#decode(CharSequence,boolean) CharacterReference.decode("&amp;#8364",true)}
		 *   returns "<code>&amp;#8364</code>".<br />
		 *   The string is not recognised as the numeric character reference <code>&amp;#8364;</code>
		 *   because it is <a href="CharacterReference.html#Unterminated">unterminated</a>
		 *   and its unicode code point U+20AC is above the maximum of U+00FF set by this property.
		 * </ul>
		 * 
		 * @param insideAttributeValue  the context within an HTML document - <code>true</code> if inside an attribute value or <code>false</code> if outside an attribute value.
		 * @return the maximum unicode code point of an <a href="CharacterReference.html#Unterminated">unterminated</a> <a href="NumericCharacterReference.html#DecimalCharacterReference">decimal character reference</a> which is to be recognised in the specified context.
		 * @see #setUnterminatedDecimalCharacterReferenceMaxCodePoint(boolean insideAttributeValue, int maxCodePoint)
		 */
		public int getUnterminatedDecimalCharacterReferenceMaxCodePoint(final boolean insideAttributeValue) {
			return getUnterminatedCharacterReferenceSettings(insideAttributeValue).decimalCharacterReferenceMaxCodePoint;
		}

		/**
		 * Sets the maximum unicode code point of an <a href="CharacterReference.html#Unterminated">unterminated</a>
		 * <a href="NumericCharacterReference.html#DecimalCharacterReference">decimal character reference</a> which is to be recognised in the specified context.
		 * <p>
		 * See {@link #getUnterminatedDecimalCharacterReferenceMaxCodePoint(boolean insideAttributeValue)} for the documentation of this property.
		 *
		 * @param insideAttributeValue  the context within an HTML document - <code>true</code> if inside an attribute value or <code>false</code> if outside an attribute value.
		 * @param maxCodePoint  the maximum unicode code point.
		 */
		public void setUnterminatedDecimalCharacterReferenceMaxCodePoint(final boolean insideAttributeValue, final int maxCodePoint) {
			getUnterminatedCharacterReferenceSettings(insideAttributeValue).decimalCharacterReferenceMaxCodePoint=maxCodePoint;
		}

		/**
		 * Returns the maximum unicode code point of an <a href="CharacterReference.html#Unterminated">unterminated</a>
		 * <a href="NumericCharacterReference.html#HexadecimalCharacterReference">hexadecimal character reference</a> which is to be recognised in the specified context.
		 * <p>
		 * For example, if <code>getUnterminatedHexadecimalCharacterReferenceMaxCodePoint(true)</code> had the hypothetical value <code>0xFF</code> (U+00FF)
		 * in the {@linkplain Config#CurrentCompatibilityMode current compatibility mode}, then:
		 * <ul>
		 *  <li>{@link CharacterReference#decode(CharSequence,boolean) CharacterReference.decode("&amp;#x3e",true)}
		 *   returns "<code>&gt;</code>".<br />
		 *   The string is recognised as the numeric character reference <code>&amp;#x3e;</code>
		 *   despite the fact that it is <a href="CharacterReference.html#Unterminated">unterminated</a>,
		 *   because its unicode code point U+003E is below the maximum of U+00FF set by this property.
		 *  <li>{@link CharacterReference#decode(CharSequence,boolean) CharacterReference.decode("&amp;#x20ac",true)}
		 *   returns "<code>&amp;#x20ac</code>".<br />
		 *   The string is not recognised as the numeric character reference <code>&amp;#20ac;</code>
		 *   because it is <a href="CharacterReference.html#Unterminated">unterminated</a>
		 *   and its unicode code point U+20AC is above the maximum of U+00FF set by this property.
		 * </ul>
		 * 
		 * @param insideAttributeValue  the context within an HTML document - <code>true</code> if inside an attribute value or <code>false</code> if outside an attribute value.
		 * @return the maximum unicode code point of an <a href="CharacterReference.html#Unterminated">unterminated</a> <a href="NumericCharacterReference.html#HexadecimalCharacterReference">hexadecimal character reference</a> which is to be recognised in the specified context.
		 * @see #setUnterminatedHexadecimalCharacterReferenceMaxCodePoint(boolean insideAttributeValue, int maxCodePoint)
		 */
		public int getUnterminatedHexadecimalCharacterReferenceMaxCodePoint(final boolean insideAttributeValue) {
			return getUnterminatedCharacterReferenceSettings(insideAttributeValue).hexadecimalCharacterReferenceMaxCodePoint;
		}

		/**
		 * Sets the maximum unicode code point of an <a href="CharacterReference.html#Unterminated">unterminated</a>
		 * <a href="NumericCharacterReference.html#HexadecimalCharacterReference">headecimal character reference</a> which is to be recognised in the specified context.
		 * <p>
		 * See {@link #getUnterminatedHexadecimalCharacterReferenceMaxCodePoint(boolean insideAttributeValue)} for the documentation of this property.
		 *
		 * @param insideAttributeValue  the context within an HTML document - <code>true</code> if inside an attribute value or <code>false</code> if outside an attribute value.
		 * @param maxCodePoint  the maximum unicode code point.
		 */
		public void setUnterminatedHexadecimalCharacterReferenceMaxCodePoint(final boolean insideAttributeValue, final int maxCodePoint) {
			getUnterminatedCharacterReferenceSettings(insideAttributeValue).hexadecimalCharacterReferenceMaxCodePoint=maxCodePoint;
		}

		/**
		 * Returns a string representation of this object useful for debugging purposes.
		 * @return a string representation of this object useful for debugging purposes.
		 */
		public String getDebugInfo() {
			return "Form field name case insensitive: "+formFieldNameCaseInsensitive
				+"\nMaximum codepoints in unterminated character references:"
				+"\n  Inside attribute values:"
				+unterminatedCharacterReferenceSettingsInsideAttributeValue
				+"\n  Outside attribute values:"
				+unterminatedCharacterReferenceSettingsOutsideAttributeValue;
		}
	
		/**
		 * Returns the {@linkplain #getName() name} of this compatibility mode.
		 * @return the {@linkplain #getName() name} of this compatibility mode.
		 */
		public String toString() {
			return getName();
		}

		UnterminatedCharacterReferenceSettings getUnterminatedCharacterReferenceSettings(final boolean insideAttributeValue) {
			return insideAttributeValue ? unterminatedCharacterReferenceSettingsInsideAttributeValue : unterminatedCharacterReferenceSettingsOutsideAttributeValue;
		}
	}
}
