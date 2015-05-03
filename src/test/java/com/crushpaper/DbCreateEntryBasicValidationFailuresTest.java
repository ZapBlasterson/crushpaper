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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Tests basic validations for creating entries. */
public class DbCreateEntryBasicValidationFailuresTest extends
		DbLogicTestBase {
	// create with a null parent id
	@Test
	public void test1() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			assertEquals(null, dbLogic.createSimpleEntry(user, "2",
					createTime, null, DbLogic.TreeRelType.Parent, false,
					false, false, false, null, errors));
			assertTrue(errors.compare(errorMessages.errorRelatedIdIsNull()));
			finalAssertions(user, expectedAfter);

	}

	// create with a empty parent id
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
					createTime, "", DbLogic.TreeRelType.Parent, false, false,
					false, false, null, errors));
			assertTrue(errors.compare(errorMessages.errorRelatedIdIsEmpty()));
			finalAssertions(user, expectedAfter);

	}

	// create with insertAboveParentsChildren=true without a parent
	@Test
	public void test3() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentNodeId = before.getIdForValue("1");
			assertEquals(null, dbLogic.createSimpleEntry(user, "2",
					createTime, parentNodeId, DbLogic.TreeRelType.Child,
					true, false, false, false, null, errors));
			assertTrue(errors
					.compare(errorMessages
							.errorInsertingANewEntryAboveChildrenCanOnlyBeDoneWhenTheRelatedEntryIsAParent()));
			finalAssertions(user, expectedAfter);

	}

	// create with null createTime
	@Test
	public void test4() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentNodeId = before.getIdForValue("1");
			assertEquals(null, dbLogic.createSimpleEntry(user, "2",
					null, parentNodeId, DbLogic.TreeRelType.Parent, false,
					false, false, false, null, errors));
			assertTrue(errors.compare(errorMessages.errorCreateTimeIsNull()));
			finalAssertions(user, expectedAfter);

	}

	// create with null user
	@Test
	public void test5() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentNodeId = before.getIdForValue("1");
			assertEquals(null, dbLogic.createSimpleEntry(null, "2",
					createTime, parentNodeId, DbLogic.TreeRelType.Parent,
					false, false, false, false, null, errors));
			assertTrue(errors.compare(errorMessages.errorUserIsNull()));
			finalAssertions(user, expectedAfter);

	}

	// create single
	@Test
	public void test6() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			assertEquals(null, dbLogic.createSimpleEntry(user, "2",
					createTime, null, null, false, false, false, false, null, errors));
			assertTrue(errors.compare(errorMessages.errorCanNotCreateParentlessNote()));
			finalAssertions(user, expectedAfter);
	}
}
