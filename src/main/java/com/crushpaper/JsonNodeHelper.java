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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A class that wraps the JsonNode so that less code is required for common
 * functions.
 */
public class JsonNodeHelper {
	JsonNodeHelper(JsonNode node) {
		this.node = node;
	}

	/**
	 * Returns the string value for the key, null if it does not exist, or
	 * throws an exception if the value is not a string.
	 * 
	 * @throws IOException
	 */
	public String getString(String key) throws IOException {
		JsonNode value = node.get(key);
		if (value == null) {
			return null;
		}

		if (!value.isTextual()) {
			throw new IOException();
		}

		return value.asText();
	}

	/**
	 * Returns the string array value for the key, null if it does not exist, or
	 * throws an exception if the value is not an array of strings.
	 * 
	 * @throws IOException
	 */
	public String[] getStringArray(String key) throws IOException {
		JsonNode value = node.get(key);
		if (value == null) {
			return null;
		}

		if (!value.isArray()) {
			throw new IOException();
		}

		int size = value.size();
		String[] values = new String[size];
		for (int i = 0; i < size; ++i) {
			JsonNode element = value.get(i);
			if (element == null) {
				throw new IOException();
			}

			if (!element.isTextual()) {
				throw new IOException();
			}

			values[i] = element.asText();
		}

		return values;
	}

	/**
	 * Returns the json array for the key, null if it does not exist, or throws
	 * an exception if the value is not an array of objects.
	 * 
	 * @throws IOException
	 */
	public JsonNodeHelper[] getJsonArray(String key) throws IOException {
		JsonNode value = node.get(key);
		if (value == null) {
			return null;
		}

		if (!value.isArray()) {
			throw new IOException();
		}

		int size = value.size();
		JsonNodeHelper[] values = new JsonNodeHelper[size];
		for (int i = 0; i < size; ++i) {
			JsonNode element = value.get(i);
			if (element == null) {
				throw new IOException();
			}

			if (!element.isObject()) {
				throw new IOException();
			}

			values[i] = new JsonNodeHelper(element);
		}

		return values;
	}

	/**
	 * Returns the boolean value for the key, false if it does not exist, or
	 * throws an exception if the value is not a boolean.
	 * 
	 * @throws IOException
	 */
	public boolean getBoolean(String key) throws IOException {
		JsonNode value = node.get(key);

		if (value == null) {
			return false;
		}

		if (!value.isBoolean()) {
			throw new IOException();
		}

		return value.asBoolean();
	}

	private JsonNode node;

	/**
	 * Returns the long value for the key, false if it does not exist, or throws
	 * an exception if the value is not a long.
	 * 
	 * @throws IOException
	 */
	public Long getLong(String key) throws IOException {
		JsonNode value = node.get(key);

		if (value == null) {
			return null;
		}

		if (!value.isNumber()) {
			throw new IOException();
		}

		return value.asLong();
	}
}
