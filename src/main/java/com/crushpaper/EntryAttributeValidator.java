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

/** Contains helper methods for is validating entry fields. */
public class EntryAttributeValidator {
	/** Returns true if the source URL is valid. */
	public static boolean isUrlValid(String value) {
		return value == null || value.length() < 1024 * 2;
	}

	/** Returns true if the source title is valid. */
	public static boolean isSourceTitleValid(String value) {
		return value == null || value.length() < 1024 * 2;
	}

	/** Returns true if the notebook title is valid. */
	public static boolean isNotebookTitleValid(String value) {
		return value == null || value.length() < 1024;
	}

	/** Returns true if the note is valid. */
	public static boolean isNoteValid(String value) {
		return value == null || value.length() < 800 * 50;
	}

	/** Returns true if the quotation is valid. */
	public static boolean isQuotationValid(String value) {
		return value == null || value.length() < 800 * 50;
	}
}
