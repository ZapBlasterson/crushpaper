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

/** Tests moving an entry right. */
public class DbMoveEntryRightTest extends DbLogicTestBase {
	// move node right null node
	@Test
	public void test1() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));
		
		assertFalse(dbLogic.moveEntry(user, null, "right", false,
				errors));
		assertTrue(errors.compare(errorMessages.errorEntryIsNull()));
		finalAssertions(user, expectedAfter);
	}

	// move node right null direction
	@Test
	public void test2() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toMoveId = before.getIdForValue("1");
		final Entry toMoveNode = dbLogic.getEntryById(toMoveId);
		assertFalse(dbLogic.moveEntry(user, toMoveNode, null,
				false, errors));
		assertTrue(errors.compare(errorMessages.errorDirectionIsNull()));
		finalAssertions(user, expectedAfter);
	}

	// move a parentless node right
	@Test
	public void test3() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toMoveId = before.getIdForValue("1");
		final Entry toMoveNode = dbLogic.getEntryById(toMoveId);
		assertFalse(dbLogic.moveEntry(user, toMoveNode, "right",
				false, errors));
		assertTrue(errors.compare(errorMessages
				.errorTheEntryHadNoParentSoItCouldNotBeMoved()));
		finalAssertions(user, expectedAfter);
	}

	// move without a previous right
	@Test
	public void test4() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2") }) });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toMoveId = before.getIdForValue("2");
		final Entry toMoveNode = dbLogic.getEntryById(toMoveId);
		assertFalse(dbLogic.moveEntry(user, toMoveNode, "right",
				false, errors));
		assertTrue(errors.compare(errorMessages
				.errorThereIsNoPreviousEntryToUseAsAParent()));
		finalAssertions(user, expectedAfter);
	}

	// move a node right that has 1 previous sibling and no next siblings and
	// the previous sibling has 0 children
	@Test
	public void test5() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3") }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1",
						new TestEntry[] { new TestEntry("2",
								new TestEntry[] { new TestEntry("3") }) }) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toMoveId = before.getIdForValue("3");
		final Entry toMoveNode = dbLogic.getEntryById(toMoveId);
		assertTrue(dbLogic.moveEntry(user, toMoveNode, "right",
				false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// move a node right that has 1 previous sibling and 1 next sibling and the
	// previous sibling has 0 children
	@Test
	public void test6() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4") }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3") }),
						new TestEntry("4") }) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toMoveId = before.getIdForValue("3");
		final Entry toMoveNode = dbLogic.getEntryById(toMoveId);
		assertTrue(dbLogic.moveEntry(user, toMoveNode, "right",
				false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// move a node right that has 1 previous sibling and no next siblings and
	// the previous sibling has 1 child
	@Test
	public void test7() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3") }),
						new TestEntry("4"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1",
						new TestEntry[] { new TestEntry("2", new TestEntry[] {
								new TestEntry("3"), new TestEntry("4") }), }) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toMoveId = before.getIdForValue("4");
		final Entry toMoveNode = dbLogic.getEntryById(toMoveId);
		assertTrue(dbLogic.moveEntry(user, toMoveNode, "right",
				false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// move a node right that has 1 previous sibling and 1 next sibling and the
	// previous sibling has 1 child
	@Test
	public void test8() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3") }),
						new TestEntry("4"), new TestEntry("5") }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] {
								new TestEntry("3"), new TestEntry("4") }),
						new TestEntry("5") }) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toMoveId = before.getIdForValue("4");
		final Entry toMoveNode = dbLogic.getEntryById(toMoveId);
		assertTrue(dbLogic.moveEntry(user, toMoveNode, "right",
				false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}
}
