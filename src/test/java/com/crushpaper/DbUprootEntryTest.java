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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Tests uprooting an entry. */
public class DbUprootEntryTest extends DbLogicTestBase {

	// uproot with null node
	@Test
	public void test1() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		assertFalse(dbLogic
				.makeNotebookEntry(user, null, false, errors));
		assertTrue(errors.compare(errorMessages.errorEntryIsNull()));
		finalAssertions(user, expectedAfter);
	}

	// uproot with null user
	@Test
	public void test2() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toUprootId = before.getIdForValue("1");
		final Entry toUprootNode = dbLogic.getEntryById(toUprootId);
		assertFalse(dbLogic.makeNotebookEntry(null, toUprootNode, false,
				errors));
		assertTrue(errors.compare(errorMessages.errorUserIsNull()));
		finalAssertions(user, expectedAfter);
	}

	// uproot a node with 0 children
	@Test
	public void test3() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"), });

		final Errors errors = new Errors();
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toUprootId = before.getIdForValue("2");
		final Entry toUprootNode = dbLogic.getEntryById(toUprootId);
		assertTrue(dbLogic.makeNotebookEntry(user, toUprootNode, false,
				errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// uproot a node with 1 child
	@Test
	public void test4() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"),
				new TestEntry("2", new TestEntry[] { new TestEntry("3"), }), });

		final Errors errors = new Errors();
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toUprootId = before.getIdForValue("2");
		final Entry toUprootNode = dbLogic.getEntryById(toUprootId);
		assertTrue(dbLogic.makeNotebookEntry(user, toUprootNode, false,
				errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// uproot a node with a parent that has 1 other child and the uprooted node
	// is the first
	@Test
	public void test5() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3"), }),
						new TestEntry("4") }) });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("4") }),
				new TestEntry("2", new TestEntry[] { new TestEntry("3"),

				}), });

		final Errors errors = new Errors();
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toUprootId = before.getIdForValue("2");
		final Entry toUprootNode = dbLogic.getEntryById(toUprootId);
		assertTrue(dbLogic.makeNotebookEntry(user, toUprootNode, false,
				errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// uproot a node with a parent that has 1 other child and the uprooted node
	// is the second
	@Test
	public void test6() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3",
								new TestEntry[] { new TestEntry("4"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"), }),
				new TestEntry("3", new TestEntry[] { new TestEntry("4"), }), });

		final Errors errors = new Errors();
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toUprootId = before.getIdForValue("3");
		final Entry toUprootNode = dbLogic.getEntryById(toUprootId);
		assertTrue(dbLogic.makeNotebookEntry(user, toUprootNode, false,
				errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// uproot a node with a parent that has 2 other children and the uprooted
	// node is the middle
	@Test
	public void test7() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3",
								new TestEntry[] { new TestEntry("4"), }),
						new TestEntry("5"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"),
						new TestEntry("5"), }),
				new TestEntry("3", new TestEntry[] { new TestEntry("4"), }), });

		final Errors errors = new Errors();
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toUprootId = before.getIdForValue("3");
		final Entry toUprootNode = dbLogic.getEntryById(toUprootId);
		assertTrue(dbLogic.makeNotebookEntry(user, toUprootNode, false,
				errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// uproot a root node
	@Test
	public void test8() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1",
						new TestEntry[] { new TestEntry("2"), }) });

		final Errors errors = new Errors();
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toUprootId = before.getIdForValue("1");
		final Entry toUprootNode = dbLogic.getEntryById(toUprootId);
		assertTrue(dbLogic.makeNotebookEntry(user, toUprootNode, false,
				errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}
}
