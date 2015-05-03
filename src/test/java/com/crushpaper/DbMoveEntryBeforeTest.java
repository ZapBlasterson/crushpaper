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

/** Tests moving an entry before another entry. */
public class DbMoveEntryBeforeTest extends DbLogicTestBase {
	// move node before null node
	@Test
	public void test1() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		assertFalse(dbLogic.moveEntry(user, null, "before",
				false, errors));
		assertTrue(errors.compare(errorMessages.errorEntryIsNull()));
		finalAssertions(user, expectedAfter);
	}

	// move node before null direction
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

	// move a parentless node before
	@Test
	public void test3() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toMoveId = before.getIdForValue("2");
		final Entry toMoveNode = dbLogic.getEntryById(toMoveId);
		assertFalse(dbLogic.moveEntry(user, toMoveNode, "before",
				false, errors));
		assertTrue(errors.compare(errorMessages
				.errorTheEntryHadNoParentSoItCouldNotBeMoved()));
		finalAssertions(user, expectedAfter);
	}

	// move node before invalid direction
	@Test
	public void test4() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1",
				new TestEntry[] { new TestEntry("2"), new TestEntry("3"), }) });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toMoveId = before.getIdForValue("3");
		final Entry toMoveNode = dbLogic.getEntryById(toMoveId);
		assertFalse(dbLogic.moveEntry(user, toMoveNode, "XXXXXX",
				false, errors));
		assertTrue(errors.compare(errorMessages
				.errorTheDirectionIsInvalid()));
		finalAssertions(user, expectedAfter);
	}

	// move a node before that has 0 previous and 0 next - fail
	@Test
	public void test5() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), }) });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toMoveId = before.getIdForValue("2");
		final Entry toMoveNode = dbLogic.getEntryById(toMoveId);
		assertFalse(dbLogic.moveEntry(user, toMoveNode, "before",
				false, errors));
		assertTrue(errors.compare(errorMessages
				.errorThereIsNoEntryToMoveBefore()));
		finalAssertions(user, expectedAfter);
	}

	// move a node before that has 0 previous and 1 next - fail
	@Test
	public void test6() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1",
				new TestEntry[] { new TestEntry("2"), new TestEntry("3"), }) });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toMoveId = before.getIdForValue("2");
		final Entry toMoveNode = dbLogic.getEntryById(toMoveId);
		assertFalse(dbLogic.moveEntry(user, toMoveNode, "before",
				false, errors));
		assertTrue(errors.compare(errorMessages
				.errorThereIsNoEntryToMoveBefore()));
		finalAssertions(user, expectedAfter);
	}

	// move a node before that has 1 previous and 0 nexts
	@Test
	public void test7() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1",
				new TestEntry[] { new TestEntry("2"), new TestEntry("3"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2"), }) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toUprootId = before.getIdForValue("3");
		final Entry toUprootNode = dbLogic.getEntryById(toUprootId);
		assertTrue(dbLogic.moveEntry(user, toUprootNode,
				"before", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// move a node before that has 2 previous and 0 nexts
	@Test
	public void test8() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("4"),
						new TestEntry("3"), }) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toUprootId = before.getIdForValue("4");
		final Entry toUprootNode = dbLogic.getEntryById(toUprootId);
		assertTrue(dbLogic.moveEntry(user, toUprootNode,
				"before", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// move a node before that has 1 previous and 1 next
	@Test
	public void test9() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2"),
						new TestEntry("4"), }) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toUprootId = before.getIdForValue("3");
		final Entry toUprootNode = dbLogic.getEntryById(toUprootId);
		assertTrue(dbLogic.moveEntry(user, toUprootNode,
				"before", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// move a node before that has 2 previous and 1 next
	@Test
	public void test10() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("4"),
						new TestEntry("3"), new TestEntry("5"), }) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String toUprootId = before.getIdForValue("4");
		final Entry toUprootNode = dbLogic.getEntryById(toUprootId);
		assertTrue(dbLogic.moveEntry(user, toUprootNode,
				"before", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}
}
