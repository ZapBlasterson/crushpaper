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

import java.util.ArrayList;
import java.util.List;

/**
 * Objects of this class contain a list of errors, each described by a text
 * message.
 */
public class Errors {
	private List<String> errors;

	public Errors() {
	}

	/** Returns the list. It can be null. */
	public List<String> getTexts() {
		return errors;
	}

	/**
	 * Add the error to the list. Does not check for duplicates. Do not add a
	 * null or an empty message.
	 */
	public void add(String text) {
		if (errors == null) {
			errors = new ArrayList<String>();
		}
		errors.add(text);
	}

	/** Helper function that discards the text if `errors` is null. */
	static public void add(Errors errors, String text) {
		if (errors == null) {
			return;
		}
		errors.add(text);
	}

	/** Returns true if any errors have been added. */
	public boolean hasErrors() {
		return errors != null;
	}

	/**
	 * Returns true if the list contains exactly one error that matches the
	 * supplied text exactly.
	 */
	public boolean compare(String text) {
		return errors != null && errors.size() == 1
				&& errors.get(0).equals(text);
	}
}
