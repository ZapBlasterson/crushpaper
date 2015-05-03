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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This minifier removes all comments preserving any embedded line feeds. The
 * intention is that the minified output preserves all line number to aid in
 * debugging.
 */
public class JsMinifier {

	/** This is the interface method of the class. */
	public StringBuilder minify(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));

		StringBuilder minified = new StringBuilder();

		Boolean startOfLine = true;
		while (true) {
			int cInt = bufferedReader.read();

			if (cInt == -1) {
				break;
			}

			char c = (char) cInt;
			if (c == '/') {
				int c1 = bufferedReader.read();
				if (c1 == '/') {
					skipUntilEndOfSingleLineComment(bufferedReader, minified);
					startOfLine = true;
				} else if (c1 == '*') {
					skipUntilEndOfMultiLineComment(bufferedReader, minified);
				} else {
					minified.append(c);
					if (c1 != -1) {
						minified.append((char) c1);
					}
				}

				continue;
			} else if (c == '\'' || c == '"') {
				minified.append(c);
				includeUntilAfter(bufferedReader, minified, c);
				startOfLine = false;
				continue;
			}

			if (startOfLine && (c == ' ' || c == '\t')) {
				continue;
			}

			startOfLine = (c == '\n');

			minified.append(c);
		}

		bufferedReader.close();

		return minified;
	}

	/**
	 * Appends every character up until the terminginated matching character
	 * `end`. Understands escapes with '\'.
	 */
	private void includeUntilAfter(BufferedReader bufferedReader,
			StringBuilder minified, char end) throws IOException {
		char cPrevious = 0;
		boolean previousSet = false;
		while (true) {
			int cInt = bufferedReader.read();

			if (cInt == -1) {
				return;
			}

			char c = (char) cInt;

			minified.append(c);

			if (c == end && (!previousSet || cPrevious != '\\')) {
				return;
			}

			previousSet = true;
			cPrevious = c;
		}
	}

	/**
	 * Skips all characters until the end of the single line comment and appends
	 * a line feed if one was found.
	 */
	private void skipUntilEndOfSingleLineComment(BufferedReader bufferedReader,
			StringBuilder minified) throws IOException {
		while (true) {
			int cInt = bufferedReader.read();

			if (cInt == -1) {
				return;
			}

			char c = (char) cInt;

			if (c == '\n') {
				minified.append('\n');
				return;
			}
		}
	}

	/** Skips all characters until the end of the multi line comment and. */
	private void skipUntilEndOfMultiLineComment(BufferedReader bufferedReader,
			StringBuilder minified) throws IOException {
		char cPrevious = 0;
		boolean previousSet = false;
		while (true) {
			int cInt = bufferedReader.read();

			if (cInt == -1) {
				return;
			}

			char c = (char) cInt;

			if (c == '/' && previousSet && cPrevious == '*') {
				return;
			}

			if (c == '\n') {
				minified.append('\n');
			}

			previousSet = true;
			cPrevious = c;
		}
	}
}
