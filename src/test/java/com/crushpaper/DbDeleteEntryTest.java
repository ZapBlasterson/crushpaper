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

/** Tests deleting entries. */
public class DbDeleteEntryTest extends DbLogicTestBase {

	// delete with null node
	@Test
	public void test1() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			assertFalse(dbLogic.deleteEntry(user, null, null, false,
					null, errors));
			assertTrue(errors.compare(errorMessages.errorEntryIsNull()));
			finalAssertions(user, expectedAfter);

	}

	// delete with null user
	@Test
	public void test2() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("1");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertFalse(dbLogic.deleteEntry(null, toDeleteNode, null,
					false, null, errors));
			assertTrue(errors.compare(errorMessages.errorUserIsNull()));
			finalAssertions(user, expectedAfter);

	}

	// delete a root node with 0 children
	@Test
	public void test3() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"), });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("2"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("1");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode, null,
					false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a root node with 1 child and delete the child
	@Test
	public void test4() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), }), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {});

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("1");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"delete", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a root node with 2 children and delete the child
	@Test
	public void test5() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"),
						new TestEntry("3"), }), new TestEntry("4"), });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("4"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("1");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"delete", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a root node with 1 child and uproot the child
	@Test
	public void test6() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), }), });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("2"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("1");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"orphan", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a root node with 2 children and uproot the child
	@Test
	public void test7() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"),
						new TestEntry("3"), }), new TestEntry("4"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("4"), new TestEntry("2"), new TestEntry("3"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("1");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"orphan", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a root node with 1 child and merge the child to the parent - fail
	@Test
	public void test8() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"), }),
				new TestEntry("4"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("1");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertFalse(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertTrue(errors
					.compare(errorMessages
							.errorChildrenActionMayNotBeParentIfTheDeletedEntryHasNoParent()));
			finalAssertions(user, expectedAfter);

	}

	// delete a node with no children and a parent with 0 other children and and
	// merge the non existent children to the parent
	@Test
	public void test9() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"), }),
				new TestEntry("4"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("4"), new TestEntry("1"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("2");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with no children and a parent with 1 other child, the
	// deleted node is the first
	@Test
	public void test10() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"),
						new TestEntry("3"), }), new TestEntry("4"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("3"), }),
				new TestEntry("4"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("2");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with no children and a parent with 1 other child, the
	// deleted node is the second
	@Test
	public void test11() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"),
						new TestEntry("3"), }), new TestEntry("4"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"), }),
				new TestEntry("4"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("3");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with no children and a parent with 2 other children, the
	// deleted node is the middle
	@Test
	public void test12() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"),
						new TestEntry("3"), new TestEntry("4"), }),
				new TestEntry("5"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"),
						new TestEntry("4"), }), new TestEntry("5"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("3");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with 1 child and a parent with 0 other children and merge
	// into the parent
	@Test
	public void test13() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }) }),
				new TestEntry("4"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("3"), }),
				new TestEntry("4"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("2");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with 1 child and a parent with 1 other child, the deleted
	// node is the first, and merge into the parent
	@Test
	public void test14() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3"), }),
						new TestEntry("4") }), new TestEntry("5"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("3"),
						new TestEntry("4"), }), new TestEntry("5"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("2");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with 1 child and a parent with 1 other child, the deleted
	// node is the second, and merge into the parent
	@Test
	public void test15() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3",
								new TestEntry[] { new TestEntry("4"), }), }),
				new TestEntry("5"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"),
						new TestEntry("4"), }), new TestEntry("5"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("3");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with 1 child and a parent with 2 other children, the
	// deleted node is the middle, and merge into the parent
	@Test
	public void test16() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3",
								new TestEntry[] { new TestEntry("4"), }),
						new TestEntry("5") }), new TestEntry("6"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"),
						new TestEntry("4"), new TestEntry("5"), }),
				new TestEntry("6"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("3");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with 2 children and a parent with 0 other children and
	// merge into the parent
	@Test
	public void test17() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"),
								new TestEntry("4") }) }), new TestEntry("5"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("3"),
						new TestEntry("4") }), new TestEntry("5"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("2");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with 2 children and a parent with 1 other child, the
	// deleted node is the first, and merge into the parent
	@Test
	public void test18() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] {
								new TestEntry("3"), new TestEntry("4") }),
						new TestEntry("5") }), new TestEntry("6"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5") }),
				new TestEntry("6"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("2");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with 2 children and a parent with 1 other child, the
	// deleted node is the second, and merge into the parent
	@Test
	public void test19() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3", new TestEntry[] {
								new TestEntry("4"), new TestEntry("5") }), }),
				new TestEntry("6"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"),
						new TestEntry("4"), new TestEntry("5") }),
				new TestEntry("6"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("3");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with 2 children and a parent with 2 other children, the
	// deleted node is the middle, and merge into the parent
	@Test
	public void test20() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3", new TestEntry[] {
								new TestEntry("4"), new TestEntry("5") }),
						new TestEntry("6") }), new TestEntry("7"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"),
						new TestEntry("4"), new TestEntry("5"),
						new TestEntry("6") }), new TestEntry("7"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("3");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with 3 children and a parent with 0 other children and
	// merge into the parent
	@Test
	public void test21() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"),
								new TestEntry("4"), new TestEntry("5"), }) }),
				new TestEntry("6"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5"), }),
				new TestEntry("6"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("2");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with 3 children and a parent with 1 other child, the
	// deleted node is the first, and merge into the parent
	@Test
	public void test22() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] {
						new TestEntry("2", new TestEntry[] {
								new TestEntry("3"), new TestEntry("4"),
								new TestEntry("5"), }), new TestEntry("6") }),
				new TestEntry("7"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5"),
						new TestEntry("6"), }), new TestEntry("7"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("2");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with 3 children and a parent with 1 other child, the
	// deleted node is the second, and merge into the parent
	@Test
	public void test23() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] {
						new TestEntry("1", new TestEntry[] {
								new TestEntry("2"),
								new TestEntry("3", new TestEntry[] {
										new TestEntry("4"), new TestEntry("5"),
										new TestEntry("6"), }), }),
						new TestEntry("7"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"),
						new TestEntry("4"), new TestEntry("5"),
						new TestEntry("6"), }), new TestEntry("7"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("3");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// delete a node with 3 children and a parent with 2 other children, the
	// deleted node is the middle, and merge into the parent
	@Test
	public void test24() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3", new TestEntry[] {
								new TestEntry("4"), new TestEntry("5"),
								new TestEntry("6"), }), new TestEntry("7") }),
				new TestEntry("8"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"),
						new TestEntry("4"), new TestEntry("5"),
						new TestEntry("6"), new TestEntry("7"), }),
				new TestEntry("8"), });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("3");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertTrue(dbLogic.deleteEntry(user, toDeleteNode,
					"parent", false, null, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	// an invalid childrenAction value
	@Test
	public void test25() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toDeleteId = before.getIdForValue("1");
			final Entry toDeleteNode = dbLogic.getEntryById(toDeleteId);
			assertFalse(dbLogic.deleteEntry(user, toDeleteNode, "",
					false, null, errors));
			assertTrue(errors.compare(errorMessages
					.errorChildrenActionInvalid()));
			finalAssertions(user, expectedAfter);

	}
}
