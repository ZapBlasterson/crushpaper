/*
Copyright 2015 CrushPaper.com.

This file is part of CrushPaper.

CrushPaper is free software: you can redistribute it and/or modify
it under the terms of version 3 of the GNU Affero General Public
License as published by the Free Software Foundation.

CrushPaper is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with CrushPaper.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.crushpaper;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Test;

/** Test for JsMinifier. */
public class JsMinifierTest {

	String minify(String toMinify) throws IOException {
		final JsMinifier minifier = new JsMinifier();
		return minifier.minify(
				new ByteArrayInputStream(toMinify.getBytes(Charset
						.forName("UTF-8")))).toString();
	}

	@Test
	public void test() throws IOException {
		assertEquals(minify(" "), "");
		assertEquals(minify("  "), "");
		assertEquals(minify("   "), "");

		assertEquals(minify("a"), "a");
		assertEquals(minify(" a"), "a");
		assertEquals(minify("  a"), "a");
		assertEquals(minify("   a"), "a");

		assertEquals(minify("a "), "a ");
		assertEquals(minify(" a "), "a ");
		assertEquals(minify("  a "), "a ");
		assertEquals(minify("   a "), "a ");

		assertEquals(minify("/**/"), "");
		assertEquals(minify("/* */"), "");
		assertEquals(minify("/*a*/"), "");
		assertEquals(minify("/* a */"), "");

		assertEquals(minify("/**//**/"), "");
		assertEquals(minify("/* *//* */"), "");
		assertEquals(minify("/*a*//*a*/"), "");
		assertEquals(minify("/* a *//* a */"), "");

		assertEquals(minify("/**/b/**/"), "b");
		assertEquals(minify("/* */b/* */"), "b");
		assertEquals(minify("/*a*/b/*a*/"), "b");
		assertEquals(minify("/* a */b/* a */"), "b");

		assertEquals(minify("/**/ b /**/"), "b ");
		assertEquals(minify("/* */ b /* */"), "b ");
		assertEquals(minify("/*a*/ b /*a*/"), "b ");
		assertEquals(minify("/* a */ b /* a */"), "b ");

		assertEquals(minify("\n"), "\n");
		assertEquals(minify(" \n "), "\n");
		assertEquals(minify("  \n  "), "\n");
		assertEquals(minify("  \n  \n"), "\n\n");

		assertEquals(minify("a\n"), "a\n");
		assertEquals(minify(" a\n "), "a\n");
		assertEquals(minify("  a\n  "), "a\n");
		assertEquals(minify("  a\n  \n"), "a\n\n");

		assertEquals(minify("a\nb"), "a\nb");
		assertEquals(minify(" a\n b"), "a\nb");
		assertEquals(minify("  a\n  b"), "a\nb");
		assertEquals(minify("  a\n  b\n"), "a\nb\n");

		assertEquals(minify("//"), "");
		assertEquals(minify("//\n"), "\n");

		assertEquals(minify("a//"), "a");
		assertEquals(minify("a//\n"), "a\n");
		assertEquals(minify("//b"), "");
		assertEquals(minify("//b\n"), "\n");
		assertEquals(minify("a//b"), "a");
		assertEquals(minify("a//b\n"), "a\n");
		assertEquals(minify("a//b\nc"), "a\nc");

		assertEquals(minify("a/**///b"), "a");
		assertEquals(minify("a/**/ //b"), "a ");
		assertEquals(minify("/**/ //b"), "");
		assertEquals(minify("/**/ a//b"), "a");
		assertEquals(minify("/**/ a //b"), "a ");
		assertEquals(minify("/**/\n"), "\n");
		assertEquals(minify("/**/\na"), "\na");

		assertEquals(minify("///**/\na"), "\na");

		assertEquals(minify("/*a\n*/\nb"), "\n\nb");
		assertEquals(minify("/*\n*/\n"), "\n\n");

		assertEquals(minify("/a"), "/a");
		assertEquals(minify("/"), "/");
		assertEquals(minify("/\n"), "/\n");

		assertEquals(minify("'/'\n"), "'/'\n");
		assertEquals(minify("'//'\n"), "'//'\n");
		assertEquals(minify("'/*'\n"), "'/*'\n");
		assertEquals(minify("'/**/'\n"), "'/**/'\n");
		assertEquals(minify("a'/'b\n"), "a'/'b\n");
		assertEquals(minify("a'//'b\n"), "a'//'b\n");
		assertEquals(minify("a'/*'b\n"), "a'/*'b\n");
		assertEquals(minify("a'/**/'b\n"), "a'/**/'b\n");
		assertEquals(minify("a'\\''b\n"), "a'\\''b\n");
		assertEquals(minify("a'c\\'d'b\n"), "a'c\\'d'b\n");

		assertEquals(minify("\"/\"\n"), "\"/\"\n");
		assertEquals(minify("\"//\"\n"), "\"//\"\n");
		assertEquals(minify("\"/*\"\n"), "\"/*\"\n");
		assertEquals(minify("\"/**/\"\n"), "\"/**/\"\n");
		assertEquals(minify("a\"/\"b\n"), "a\"/\"b\n");
		assertEquals(minify("a\"//\"b\n"), "a\"//\"b\n");
		assertEquals(minify("a\"/*\"b\n"), "a\"/*\"b\n");
		assertEquals(minify("a\"/**/\"b\n"), "a\"/**/\"b\n");
		assertEquals(minify("a\"\\\"\"b\n"), "a\"\\\"\"b\n");
		assertEquals(minify("a\"c\\\"d\"b\n"), "a\"c\\\"d\"b\n");

		assertEquals(minify("\"\"//"), "\"\"");
		assertEquals(minify("\"\\\"\"//"), "\"\\\"\"");
		assertEquals(minify("\"\"//"), "\"\"");
		assertEquals(minify("\"\\\"\"//"), "\"\\\"\"");
		
		assertEquals(minify("a//\n b"), "a\nb");
	}
}
