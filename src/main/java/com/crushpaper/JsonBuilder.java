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

import org.apache.commons.lang3.StringEscapeUtils;

/** A utility class that helps with creation of JSON data. */
public class JsonBuilder {
	/** Returns the value JSON encoded and quoted. */
	static String quote(String value) {
		if (value == null) {
			return "null";
		}

		return "\"" + StringEscapeUtils.escapeJson(value) + "\"";
	}

	/** Appends indentation to `result`. */
	static public void appendIndentation(int level, StringBuilder result) {
		for (int i = 0; i < level; ++i)
			result.append("  ");
	}

	/** Adds a key and value to a JSON StringBuilder. */
	static boolean addPropertyToJsonString(StringBuilder result, Object value,
			boolean addedAnyYet, String key, int indentationLevel) {
		appendIndentation(indentationLevel, result);
		return addPropertyToJsonString(result, value, addedAnyYet, key);
	}

	/** Adds a key and value to a JSON StringBuilder. */
	static boolean addPropertyToJsonString(StringBuilder result, Object value,
			boolean addedAnyYet, String key) {
		if (value == null) {
			return addedAnyYet;
		}

		if (value instanceof Boolean && !((Boolean) value)) {
			return addedAnyYet;
		}

		if (addedAnyYet) {
			result.append(",\n");
		}

		result.append("\"");
		result.append(key);
		result.append("\": ");

		if (value instanceof String) {
			result.append(quote((String) value));
		} else {
			result.append(value);
		}

		return true;
	}

}
