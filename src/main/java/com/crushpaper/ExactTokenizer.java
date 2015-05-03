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

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;

/** Returns a single token of all of the input for exact matches. */
public final class ExactTokenizer extends Tokenizer {
	public ExactTokenizer(AttributeFactory factory, Reader input) {
		super(factory, input);
	}

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();

		char[] term = new char[100];
		int length = input.read(term);
		if (length == -1) {
			return false;
		}

		termAtt.append(CharBuffer.wrap(term, 0, length));

		return true;
	}
}
