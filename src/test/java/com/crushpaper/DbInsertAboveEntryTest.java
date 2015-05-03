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

/** Tests inserting an entry above another entry. */
public class DbInsertAboveEntryTest extends DbLogicTestBase {
	// create a node above another node with an invalid related id
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
									DbLogic.TreeRelType.Child, false, false,
									false, false, null, errors));
			assertTrue(errors.compare(errorMessages
					.errorRelatedIdIsInInvalidFormat()));
			finalAssertions(user, expectedAfter);

	}

	// create node inserting above a node that does not exist - fail
	@Test
	public void test2() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			assertEquals(null, dbLogic.createSimpleEntry(user, "Q",
					createTime, dbLogic.getIdGenerator().getAnotherId(),
					DbLogic.TreeRelType.Child, false, false, false, false,
					null, errors));
			assertTrue(errors.compare(errorMessages
					.errorTheProposedRelatedEntryCouldNotBeFound()));
			finalAssertions(user, expectedAfter);

	}

	// create node inserting above a node
	@Test
	public void test3() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("Q",
						new TestEntry[] { new TestEntry("1"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String childNodeId = before.getIdForValue("1");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, childNodeId, DbLogic.TreeRelType.Child,
					false, false, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// create node inserting above a node that has a child
	@Test
	public void test4() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("Q",
						new TestEntry[] { new TestEntry("1",
								new TestEntry[] { new TestEntry("2"), }), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String childNodeId = before.getIdForValue("1");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, childNodeId, DbLogic.TreeRelType.Child,
					false, false, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// create node inserting above a node - the node's parent has 1 child
	@Test
	public void test5() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1",
						new TestEntry[] { new TestEntry("Q",
								new TestEntry[] { new TestEntry("2"), }), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String childNodeId = before.getIdForValue("2");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, childNodeId, DbLogic.TreeRelType.Child,
					false, false, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// create node inserting above a node - the node's parent has 2 children and
	// the node is the first
	@Test
	public void test6() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1",
				new TestEntry[] { new TestEntry("2"), new TestEntry("3"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("Q",
								new TestEntry[] { new TestEntry("2"), }),
						new TestEntry("3"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String childNodeId = before.getIdForValue("2");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, childNodeId, DbLogic.TreeRelType.Child,
					false, false, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// create node inserting above a node - the node's parent has 2 children and
	// the node is the second
	@Test
	public void test7() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1",
				new TestEntry[] { new TestEntry("2"), new TestEntry("3"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("Q",
								new TestEntry[] { new TestEntry("3"), }),

				}) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String childNodeId = before.getIdForValue("3");
			assertNotNull(dbLogic.createSimpleEntry(user, "Q",
					createTime, childNodeId, DbLogic.TreeRelType.Child,
					false, false, false, false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}
}
