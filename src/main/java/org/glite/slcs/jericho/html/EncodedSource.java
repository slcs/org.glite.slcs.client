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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Based on information in:
 * http://www.w3.org/TR/REC-xml/#sec-guessing-no-ext-info
 * http://www.w3.org/TR/html401/charset.html#h-5.2
 */
final class EncodedSource {
	public final Reader Reader;
	public final String Encoding;
	public final String EncodingSpecificationInfo;
	public final HttpURLConnection HttpURLConnection;
	
	private static final int PREVIEW_BUFFER_SIZE=2048;
	private static final int PREVIEW_MAX_BYTES=PREVIEW_BUFFER_SIZE*4; // Cater for each character in the preview buffer requiring an average of 4 bytes, which is twice what would reasonably be expected but ensures the reset() call on the BufferedInputStream doesn't fail.

	private static final String UTF_32="UTF-32"; // not supported in Java, will throw an exception.
	private static final String UTF_16="UTF-16";
	private static final String UTF_16BE="UTF-16BE";
	private static final String UTF_16LE="UTF-16LE";
	private static final String UTF_8="UTF-8";
	private static final String EBCDIC="Cp037";
	private static final String ISO_8859_1="ISO-8859-1";
	
	EncodedSource(final InputStream inputStream, final String encoding, final String encodingSpecificationInfo, final HttpURLConnection httpURLConnection) throws UnsupportedEncodingException {
		if (encoding==null)
			Reader=new InputStreamReader(inputStream); // Reader will be empty so the encoding is arbitrary.
		else
			Reader=new InputStreamReader(inputStream,encoding);
		Encoding=encoding;
		EncodingSpecificationInfo=encodingSpecificationInfo;
		HttpURLConnection=httpURLConnection;
	}

	public static EncodedSource construct(final URL url) throws IOException {
		final URLConnection urlConnection=url.openConnection();
		final HttpURLConnection httpURLConnection=(urlConnection instanceof HttpURLConnection) ? (HttpURLConnection)urlConnection : null;
		// urlConnection.setRequestProperty("Accept-Charset","UTF-8, ISO-8859-1;q=0"); // used for debugging
		final InputStream inputStream=urlConnection.getInputStream();
		final String contentType=urlConnection.getContentType();
		if (contentType!=null) {
			final String charset=Source.getCharsetParameterFromHttpHeaderValue(contentType);
			if (charset!=null) return new EncodedSource(inputStream,charset,"HTTP header Content-Type: "+contentType,httpURLConnection);
		}
		return construct(inputStream,httpURLConnection);
	}
	
	public static EncodedSource construct(final InputStream inputStream, final HttpURLConnection httpURLConnection) throws IOException {
		final BufferedInputStream in=(inputStream instanceof BufferedInputStream) ? (BufferedInputStream)inputStream : new BufferedInputStream(inputStream);
		in.mark(PREVIEW_MAX_BYTES);
		final String preliminaryEncoding=getPreliminaryEncoding(in);
		if (preliminaryEncoding==null) return new EncodedSource(in,null,"empty input stream",httpURLConnection);
		in.reset();
		final Source previewSource=getPreviewSource(in,preliminaryEncoding);
		in.reset();
		if (previewSource.getEncoding()!=null) return new EncodedSource(in,previewSource.encoding,previewSource.encodingSpecificationInfo,httpURLConnection);
		// No explicit encoding specified in document
		// If the document is not XML and is being loaded using HTTP, use the default specified by HTTP which is ISO-8859-1.
		// For the encoding to be ISO-8859-1, the preliminary encoding must be UTF-8.
		if (httpURLConnection!=null && preliminaryEncoding==UTF_8 && !previewSource.isXML())
			return new EncodedSource(in,ISO_8859_1,"HTTP default 8-bit encoding for non-XML document",httpURLConnection);
		// Just use the preliminary encoding (UTF-8 or UTF-16), which must be the case for an XML document without an XML declaration.
		return new EncodedSource(in,preliminaryEncoding,"XML default matching first four bytes of input stream",httpURLConnection);
	}

	private static String getPreliminaryEncoding(BufferedInputStream bufferedInputStream) throws IOException {
		final int b1=bufferedInputStream.read();
		if (b1==-1) return null;
		final int b2=bufferedInputStream.read();
		final int b3=bufferedInputStream.read();
		final int b4=bufferedInputStream.read();
		if ((b1&0xFE)==0xFE && b2==(b1^1)) { // first two bytes are FEFF or FFFE
			return (b3==0) ? UTF_32 : UTF_16;
		} else if (b1==0) {
			if (b2==0 || b4==0) return UTF_32;
			return UTF_16BE;
		} else if (b2==0) {
			return (b3==0) ? UTF_32 : UTF_16LE;
		} else if (b1==0x4C && b2==0x6F && b3==0xA7 && b4==0x94) return EBCDIC; // This only recognises "<?xm", not sure how straight HMTL documents in EBCDIC can be detected easily.
		return UTF_8;
	}

	private static Source getPreviewSource(BufferedInputStream bufferedInputStream, String preliminaryEncoding) throws IOException {
		final BufferedReader preliminaryReader=new BufferedReader(new InputStreamReader(bufferedInputStream,preliminaryEncoding),PREVIEW_BUFFER_SIZE);
		StringBuffer sb=new StringBuffer(PREVIEW_BUFFER_SIZE);
		for (int i=0; i<PREVIEW_BUFFER_SIZE; i++) {
			final int ch=preliminaryReader.read();
			if (ch==-1) break;
			sb.append((char)ch);
		}
		return new Source(sb);
	}
}
