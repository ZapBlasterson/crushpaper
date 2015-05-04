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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This uses an "impermanent" implementation of the db. It is not really that
 * impermanent because it is on disk in a temp directory.
 */
public class DbLogicImpermanent extends DbLogic {

	DbLogicImpermanent() {
		super(new File("target/tmpdb" + File.separator + "db"));
	}

	/** Creates the DB. */
	public void createTestDb() {
		createDb();
	}

	/** Converts a real entry to a test entry, including its children. */
	private TestEntry convertToTestEntry(Entry entry) {
		final TestEntry testEntry = new TestEntry(entry.getNoteOrTitle(),
				entry.getId());

		final ArrayList<Entry> childrenEntries = getChildrenInOrder(entry);

		final ArrayList<TestEntry> childrenTestEntries = new ArrayList<TestEntry>();
		for (final Entry childEntry : childrenEntries) {
			childrenTestEntries.add(convertToTestEntry(childEntry));
		}

		if (!childrenTestEntries.isEmpty()) {
			testEntry.setChildren(childrenTestEntries
					.toArray(new TestEntry[childrenTestEntries.size()]));
		}

		return testEntry;
	}

	/** Converts all real entries for a user to test entries. */
	public TestEntrySet getEntryTestSet(User user) {
		if (user == null) {
			return null;
		}

		final ArrayList<TestEntry> testEntries = new ArrayList<TestEntry>();
		final List<?> entries = getAllParentlessEntries();
		for (final Object entryObject : entries) {
			final Entry entry = (Entry)entryObject;
			if(entry.getType().equals(DbLogic.Constants.tableofcontents)) {
				continue;
			}
			
			testEntries.add(convertToTestEntry(entry));
		}

		return new TestEntrySet(testEntries.toArray(new TestEntry[testEntries
				.size()]));
	}

	/** Adds a set of test entries to the DB. */
	public boolean addEntries(TestEntrySet testEntrySet, User user,
			long createTime, Errors errors) {
		if (user == null) {
			return false;
		}

		if (testEntrySet == null) {
			return false;
		}

		if (!testEntrySet.areValuesValid()) {
			return false;
		}

		for (final TestEntry testEntry : testEntrySet.getRootEntries()) {
			if (!addEntryHelper(testEntry, user, createTime, null, testEntrySet, errors)) {
				return false;
			}
		}

		return !hasErrors(errors);
	}

	/** Adds a test entry and its children to the DB. */
	private boolean addEntryHelper(TestEntry testEntry, User user,
			long createTime, String parentId, TestEntrySet testEntrySet, Errors errors) {
		if (testEntry == null) {
			return false;
		}

		if (testEntry.getValue() == null) {
			return false;
		}

		long entryCreateTime = createTime;
		if(testEntry.getCreateTime() != -1) {
			entryCreateTime = testEntry.getCreateTime();
		}

		Entry entry = null;
		if (testEntry.getIsSource()) {
			entry = updateOrCreateSource(user, null, testEntry.getValue(), testEntry.getValue(), entryCreateTime, entryCreateTime, false, errors);
		} else {
			Entry source = null;
			String type = parentId == null ? DbLogic.Constants.root : null;
			if (testEntry.getSourceValue() != null) {
				type = DbLogic.Constants.quotation;
				source = getEntryById(testEntrySet.getRootEntryByValue(testEntry.getSourceValue()).getId());
			}
		
			entry = createSimpleEntry(user,
					testEntry.getValue(), entryCreateTime, parentId,
					parentId == null ? null : TreeRelType.Parent, false, false,
					false, false, type, errors, source);
		}
		
		if (entry == null) {
			return false;
		}

		if (entry.getId() == null) {
			return false;
		}

		testEntry.setId(entry.getId());

		if (testEntry.getChildren() != null) {
			for (final TestEntry child : testEntry.getChildren()) {
				if (child == null) {
					return false;
				}

				if (!addEntryHelper(child, user, createTime,
						entry.getId(), testEntrySet, errors)) {
					return false;
				}
			}
		}

		return true;
	}
}
