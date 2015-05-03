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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Tests creating child entries. */
public class DbCreateChildEntryTest extends DbLogicTestBase {

	// create with invalid related id
	@Test
	public void test1() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			assertEquals(
					null,
					dbLogic
							.createSimpleEntry(
									user,
									"2",
									createTime,
									"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
									DbLogic.TreeRelType.Parent, false, false,
									false, false, null, errors));
			assertTrue(errors.compare(errorMessages
					.errorRelatedIdIsInInvalidFormat()));
			finalAssertions(user, expectedAfter);

	}

	// create first child of a node that does not exist - fail
	@Test
	public void test2() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			assertEquals(null, dbLogic.createSimpleEntry(user, "2",
					createTime, dbLogic.getIdGenerator().getAnotherId(),
					DbLogic.TreeRelType.Parent, false, false, false, false,
					null, errors));
			assertTrue(errors.compare(errorMessages
					.errorTheProposedParentEntryCouldNotBeFound()));
			finalAssertions(user, expectedAfter);

	}

	// create a child
	@Test
	public void test3() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1",
						new TestEntry[] { new TestEntry("Q"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentNodeId = before.getIdForValue("1");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, parentNodeId, DbLogic.TreeRelType.Parent,
					false, false, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// create a last child of a parent with no children
	@Test
	public void test4() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1",
						new TestEntry[] { new TestEntry("Q"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentNodeId = before.getIdForValue("1");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, parentNodeId, DbLogic.TreeRelType.Parent,
					false, false, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// create a first child of a parent with no children
	@Test
	public void test5() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1",
						new TestEntry[] { new TestEntry("Q"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentNodeId = before.getIdForValue("1");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, parentNodeId, DbLogic.TreeRelType.Parent,
					false, true, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// create a first child of a parent with 1 children
	@Test
	public void test6() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("Q"), new TestEntry("2"),

				}) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentNodeId = before.getIdForValue("1");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, parentNodeId, DbLogic.TreeRelType.Parent,
					false, true, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// create a first child of a parent with 2 children
	@Test
	public void test7() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1",
				new TestEntry[] { new TestEntry("2"), new TestEntry("3"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("Q"), new TestEntry("2"),
						new TestEntry("3"),

				}) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentNodeId = before.getIdForValue("1");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, parentNodeId, DbLogic.TreeRelType.Parent,
					false, true, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// create a first child of a parent with 3 children
	@Test
	public void test8() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("Q"), new TestEntry("2"),
						new TestEntry("3"), new TestEntry("4"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentNodeId = before.getIdForValue("1");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, parentNodeId, DbLogic.TreeRelType.Parent,
					false, true, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// create a last child of a parent with 1 children
	@Test
	public void test9() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("Q"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentNodeId = before.getIdForValue("1");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, parentNodeId, DbLogic.TreeRelType.Parent,
					false, false, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// create a last child of a parent with 2 children
	@Test
	public void test10() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1",
				new TestEntry[] { new TestEntry("2"), new TestEntry("3"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3"),
						new TestEntry("Q"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentNodeId = before.getIdForValue("1");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, parentNodeId, DbLogic.TreeRelType.Parent,
					false, false, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// create a last child of a parent with 3 children
	@Test
	public void test11() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("Q"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentNodeId = before.getIdForValue("1");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, parentNodeId, DbLogic.TreeRelType.Parent,
					false, false, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}
}
