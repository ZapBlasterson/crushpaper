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

import java.util.HashMap;

/**
 * Represents a set of entries in the db. Part of the unit testing scaffolding,
 * but this is not a mock or a fake. Don't put null values or null entries in
 * this set. The code won't crash, but comparisons wouldn't work intuitively.
 * This is OK, because TestEntries are only trying to model Entries from
 * DbLogic, which can not be null, and whose values can be empty, but not null.
 * Order does not matter for the top level entries, but it does matter for every
 * other entry.
 */
public class TestEntrySet {
	private final TestEntry[] entries;
	private final HashMap<String, TestEntry> entriesByValue = new HashMap<String, TestEntry>();
	private final HashMap<String, TestEntry> entriesById = new HashMap<String, TestEntry>();
	private boolean wasValidityOfValuesAlreadyChecked = false;
	private boolean areValuesValid = false;
	private boolean wasValidityOfIdsAlreadyChecked = false;
	private boolean areIdsValid = false;

	TestEntrySet(TestEntry[] entries) {
		this.entries = entries;
	}

	public TestEntry[] getRootEntries() {
		return entries;
	}

	public TestEntry getRootEntryByValue(String value) {
		if (entries != null) {
			for (final TestEntry entry : entries) {
				if (value == null) {
					if (entry != null && value == entry.getValue()) {
						return entry;
					}
				} else if (entry != null && value.equals(entry.getValue())) {
					return entry;
				}
			}
		}

		return null;
	}

	public boolean compare(TestEntrySet other) {
		if ((other.entries == null) != (entries == null)) {
			return false;
		}

		if (entries != null && other.entries.length != entries.length) {
			return false;
		}

		if (entries != null) {
			for (final TestEntry entry : entries) {
				final TestEntry fromOther = other
						.getRootEntryByValue(entry != null ? entry.getValue()
								: null);
				if ((entry == null) != (fromOther == null)) {
					return false;
				}

				if (entry != null && !entry.compare(fromOther)) {
					return false;
				}
			}
		}

		return true;
	}

	public boolean doIdsMatch(TestEntrySet other) {
		other.areIdsValid();
		areIdsValid();

		for (final HashMap.Entry<String, TestEntry> entry : entriesByValue
				.entrySet()) {
			final String otherId = other.getIdForValue(entry.getKey());

			// Only check the values that are present in both.
			if (otherId != null && !otherId.equals(entry.getValue().getId())) {
				return false;
			}
		}

		return true;
	}

	public String getIdForValue(String value) {
		final TestEntry testEntry = entriesByValue.get(value);
		if (testEntry == null) {
			return null;
		}

		return testEntry.getId();
	}

	public boolean areValuesValid() {
		if (wasValidityOfValuesAlreadyChecked) {
			return areValuesValid;
		}

		wasValidityOfValuesAlreadyChecked = true;
		return areValuesValid = areValuesValidHelper();
	}

	public boolean areValuesValidHelper() {
		if (entries == null) {
			return false;
		}

		for (final TestEntry entry : entries) {
			if (entry == null) {
				return false;
			}

			if (!areValuesValid(entry)) {
				return false;
			}
		}

		return true;
	}

	private boolean areValuesValid(TestEntry entry) {
		if (entry.getValue() == null) {
			return false;
		}

		if (entriesByValue.put(entry.getValue(), entry) != null) {
			return false;
		}

		if (entry.getChildren() != null) {
			for (final TestEntry child : entry.getChildren()) {
				if (child == null) {
					return false;
				}

				if (!areValuesValid(child)) {
					return false;
				}
			}
		}

		return true;
	}

	public boolean areIdsValid() {
		if (wasValidityOfIdsAlreadyChecked) {
			return areIdsValid;
		}

		wasValidityOfIdsAlreadyChecked = true;
		return areIdsValid = areIdsValidHelper();
	}

	public boolean areIdsValidHelper() {
		if (entries == null) {
			return false;
		}

		for (final TestEntry entry : entries) {
			if (entry == null) {
				return false;
			}

			if (!areIdsValid(entry)) {
				return false;
			}
		}

		return true;
	}

	private boolean areIdsValid(TestEntry entry) {
		if (entry.getId() == null
				|| entriesById.put(entry.getId(), entry) != null) {
			return false;
		}

		if (entry.getChildren() != null) {
			for (final TestEntry child : entry.getChildren()) {
				if (child == null) {
					return false;
				}

				if (!areIdsValid(child)) {
					return false;
				}
			}
		}

		return true;
	}
}
