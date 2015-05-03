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

/** Tests making an entry a child of another entry. */
public class DbMakeEntryAChildOfAParentTest extends
		DbLogicTestBase {
	// null sibling
	@Test
	public void test1() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("2");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertFalse(dbLogic.makeEntryAChildOfAParent(user, null,
					childNode, true, false, errors));
			assertTrue(errors.compare(errorMessages.errorParentIsNull()));
			finalAssertions(user, expectedAfter);

	}

	// null child node
	@Test
	public void test2() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("2");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertFalse(dbLogic.makeEntryAChildOfAParent(user,
					parentNode, null, true, false, errors));
			assertTrue(errors.compare(errorMessages.errorChildIsNull()));
			finalAssertions(user, expectedAfter);

	}

	// null user node
	@Test
	public void test3() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("2");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertFalse(dbLogic.makeEntryAChildOfAParent(null, parentNode,
					childNode, true, false, errors));
			assertTrue(errors.compare(errorMessages.errorUserIsNull()));
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a childless node into it's direct parent
	@Test
	public void test4() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), }) });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("2");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a childless node into it's grand parent
	@Test
	public void test5() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a childless node into it's grand parent and the parent has a
	// sibling
	@Test
	public void test6() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3"), }),
						new TestEntry("4") }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("4"),
						new TestEntry("3") }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a childless node into it's grand parent and the node has a next
	@Test
	public void test7() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3"), new TestEntry("4"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("4"), }),
						new TestEntry("3"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a childless node into it's grand parent and the node has a
	// previous
	@Test
	public void test8() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3"), new TestEntry("4"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3"), }),
						new TestEntry("4"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("4");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a childless node into it's grand parent and the node has a
	// previous and a next
	@Test
	public void test9() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3"), new TestEntry("4"),
						new TestEntry("5"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] {
								new TestEntry("3"), new TestEntry("5"), }),
						new TestEntry("4"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("4");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a childless node into it's direct parent
	@Test
	public void test10() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), }) });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("2");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a childless node into it's grand parent
	@Test
	public void test11() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a childless node into it's grand parent and the parent has a sibling
	@Test
	public void test12() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3"), }),
						new TestEntry("4") }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("4"),
						new TestEntry("3") }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a childless node into it's grand parent and the node has a next
	@Test
	public void test13() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3"), new TestEntry("4"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("4"), }),
						new TestEntry("3"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a childless node into it's grand parent and the node has a previous
	@Test
	public void test14() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3"), new TestEntry("4"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3"), }),
						new TestEntry("4"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("4");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a childless node into it's grand parent and the node has a previous
	// and a next
	@Test
	public void test15() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3"), new TestEntry("4"),
						new TestEntry("5"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] {
								new TestEntry("3"), new TestEntry("5"), }),
						new TestEntry("4"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("4");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a childless node into it's child - fail
	@Test
	public void test16() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), }) });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("2");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("1");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertFalse(dbLogic.makeEntryAChildOfAParent(user,
					parentNode, childNode, false, false, errors));
			assertTrue(errors.compare(errorMessages
					.errorChildIsAnAncestorOfTheParent()));
			finalAssertions(user, expectedAfter);

	}

	// drag a childless node into it's grandchild - fail
	@Test
	public void test17() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }) });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("3");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("1");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertFalse(dbLogic.makeEntryAChildOfAParent(user,
					parentNode, childNode, false, false, errors));
			assertTrue(errors.compare(errorMessages
					.errorChildIsAnAncestorOfTheParent()));
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a childless node into it's child
	@Test
	public void test18() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("1"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("2");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("1");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a childless node into it's grand child
	@Test
	public void test19() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3",
								new TestEntry[] { new TestEntry("1"), }), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("3");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("1");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a childless node into it's child and the child has a child
	@Test
	public void test20() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3"), new TestEntry("1"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("2");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("1");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a childless node into it's grand child and the grand child has
	// a child
	@Test
	public void test21() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry(
						"1",
						new TestEntry[] { new TestEntry(
								"2",
								new TestEntry[] { new TestEntry("3",
										new TestEntry[] { new TestEntry("4"), }), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3", new TestEntry[] {
								new TestEntry("4"), new TestEntry("1"), }), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("3");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("1");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a childless node into it's grand parent and the the parent has
	// a sibling
	@Test
	public void test22() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3"), }),
						new TestEntry("4"), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("4"),
						new TestEntry("3"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// // FUCK
	// ctrl drag a node that has a child into it's direct parent
	@Test
	public void test23() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("2");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a node that has a child into it's grand parent
	@Test
	public void test24() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry(
						"1",
						new TestEntry[] { new TestEntry(
								"2",
								new TestEntry[] { new TestEntry("3",
										new TestEntry[] { new TestEntry("4"), }), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("4"), }),
						new TestEntry("3"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a node that has a child into it's grand parent and the parent
	// has a sibling
	@Test
	public void test25() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] { new TestEntry("3",
								new TestEntry[] { new TestEntry("4"), }), }),
						new TestEntry("5") }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("4"), }),
						new TestEntry("5"), new TestEntry("3"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a node that has a child into it's grand parent and the parent
	// has a sibling
	@Test
	public void test25Alt() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry(
						"1",
						new TestEntry[] {
								new TestEntry(
										"2",
										new TestEntry[] {
												new TestEntry(
														"3",
														new TestEntry[] { new TestEntry(
																"4"), }),
												new TestEntry("5") }),
								new TestEntry("6") }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] {
								new TestEntry("4"), new TestEntry("5") }),
						new TestEntry("6"), new TestEntry("3"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a node that has a child into it's grand parent and the node has
	// a next
	@Test
	public void test26() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3",
								new TestEntry[] { new TestEntry("4"), }),
						new TestEntry("5"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] {
								new TestEntry("4"), new TestEntry("5"), }),
						new TestEntry("3"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a node that has a child into it's grand parent and the node has
	// a previous
	@Test
	public void test27() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry(
						"1",
						new TestEntry[] { new TestEntry(
								"2",
								new TestEntry[] {
										new TestEntry("3"),
										new TestEntry(
												"4",
												new TestEntry[] { new TestEntry(
														"5"), }), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] {
								new TestEntry("3"), new TestEntry("5"), }),
						new TestEntry("4"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("4");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a node that has a child into it's grand parent and the node has
	// a previous and a next
	@Test
	public void test28() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3"),
						new TestEntry("4",
								new TestEntry[] { new TestEntry("5"), }),
						new TestEntry("6"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] {
								new TestEntry("3"), new TestEntry("5"),
								new TestEntry("6"), }), new TestEntry("4"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("4");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a node that has a child into it's grand parent and the node has
	// a previous and a next
	@Test
	public void test28Alt() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3"),
						new TestEntry("4", new TestEntry[] {
								new TestEntry("5"), new TestEntry("6"), }),
						new TestEntry("7"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] {
								new TestEntry("3"), new TestEntry("5"),
								new TestEntry("6"), new TestEntry("7"), }),
						new TestEntry("4"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("4");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a node that has a child into it's grand parent and the node has
	// a previous and a next
	@Test
	public void test28Alt2() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry("1",
						new TestEntry[] { new TestEntry("2", new TestEntry[] {
								new TestEntry("3"),
								new TestEntry("4", new TestEntry[] {
										new TestEntry("5"), new TestEntry("6"),
										new TestEntry("7"), }),
								new TestEntry("8"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] {
								new TestEntry("3"), new TestEntry("5"),
								new TestEntry("6"), new TestEntry("7"),
								new TestEntry("8"), }), new TestEntry("4"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("4");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a node that has a child into it's direct parent
	@Test
	public void test29() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }) });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("2");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a node that has a child into it's grand parent
	@Test
	public void test30() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry(
						"1",
						new TestEntry[] { new TestEntry(
								"2",
								new TestEntry[] { new TestEntry("3",
										new TestEntry[] { new TestEntry("4"), }), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3",
								new TestEntry[] { new TestEntry("4"), }), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a node that has a child into it's grand parent and the parent has a
	// sibling
	@Test
	public void test31() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] { new TestEntry("3",
								new TestEntry[] { new TestEntry("4"), }), }),
						new TestEntry("5") }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("5"),
						new TestEntry("3",
								new TestEntry[] { new TestEntry("4"), }), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a node that has a child into it's grand parent and the node has a
	// next
	@Test
	public void test32() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3",
								new TestEntry[] { new TestEntry("4"), }),
						new TestEntry("5"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("5"), }),
						new TestEntry("3",
								new TestEntry[] { new TestEntry("4"), }), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("3");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a node that has a child into it's grand parent and the node has a
	// previous
	@Test
	public void test33() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry(
						"1",
						new TestEntry[] { new TestEntry(
								"2",
								new TestEntry[] {
										new TestEntry("3"),
										new TestEntry(
												"4",
												new TestEntry[] { new TestEntry(
														"5"), }), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3"), }),
						new TestEntry("4",
								new TestEntry[] { new TestEntry("5"), }), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("4");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a node that has a child into it's grand parent and the node has a
	// previous and a next
	@Test
	public void test34() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3"),
						new TestEntry("4",
								new TestEntry[] { new TestEntry("5"), }),
						new TestEntry("6"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] {
								new TestEntry("3"), new TestEntry("6"), }),
						new TestEntry("4",
								new TestEntry[] { new TestEntry("5"), }), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("1");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("4");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// drag a node that has a child into it's child - fail
	@Test
	public void test35() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }) });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("2");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("1");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertFalse(dbLogic.makeEntryAChildOfAParent(user,
					parentNode, childNode, false, false, errors));
			assertTrue(errors.compare(errorMessages
					.errorChildIsAnAncestorOfTheParent()));
			finalAssertions(user, expectedAfter);

	}

	// drag a node that has a child into it's grandchild - fail
	@Test
	public void test36() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry(
						"1",
						new TestEntry[] { new TestEntry(
								"2",
								new TestEntry[] { new TestEntry("3",
										new TestEntry[] { new TestEntry("4"), }), }), }) });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("3");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("1");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertFalse(dbLogic.makeEntryAChildOfAParent(user,
					parentNode, childNode, false, false, errors));
			assertTrue(errors.compare(errorMessages
					.errorChildIsAnAncestorOfTheParent()));
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a node that has a child into it's child
	@Test
	public void test37() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3"), new TestEntry("1"), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("2");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("1");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// ctrl drag a node that has a child into it's grand child
	@Test
	public void test38() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry(
						"1",
						new TestEntry[] { new TestEntry(
								"2",
								new TestEntry[] { new TestEntry("3",
										new TestEntry[] { new TestEntry("4"), }), }), }) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3", new TestEntry[] {
								new TestEntry("4"), new TestEntry("1"), }), }) });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String parentId = before.getIdForValue("3");
			final Entry parentNode = dbLogic.getEntryById(parentId);
			final String childId = before.getIdForValue("1");
			final Entry childNode = dbLogic.getEntryById(childId);
			assertTrue(dbLogic.makeEntryAChildOfAParent(user, parentNode,
					childNode, true, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}
}
