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

/** Tests making an entry without its children a sibling of another entry. */
public class DbMakeJustEntryASiblingOfAnotherTest extends
		DbLogicTestBase {

	// null sibling
	@Test
	public void test1() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"), }),
				new TestEntry("3") });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertFalse(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, null, true, "previous", false, errors));
		assertTrue(errors.compare(errorMessages.errorMovedIsNull()));
		finalAssertions(user, expectedAfter);
	}

	// null moved
	@Test
	public void test2() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"), }),
				new TestEntry("3") });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertFalse(dbLogic.makeEntrySiblingOfAnother(user, null,
				movedNode, true, "previous", false, errors));
		assertTrue(errors.compare(errorMessages.errorSiblingIsNull()));
		finalAssertions(user, expectedAfter);
	}

	// null user
	@Test
	public void test3() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"), }),
				new TestEntry("3") });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
	
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertFalse(dbLogic.makeEntrySiblingOfAnother(null, siblingNode,
				movedNode, true, "previous", false, errors));
		assertTrue(errors.compare(errorMessages.errorUserIsNull()));
		finalAssertions(user, expectedAfter);
	}

	// null placement
	@Test
	public void test4() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"), }),
				new TestEntry("3") });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertFalse(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, null, false, errors));
		assertTrue(errors.compare(errorMessages.errorPlacementIsNull()));
		finalAssertions(user, expectedAfter);
	}

	// invalid placement
	@Test
	public void test5() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"), }),
				new TestEntry("3") });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertFalse(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "XXXX", false, errors));
		assertTrue(errors.compare(errorMessages.errorPlacementIsNotValid()));
		finalAssertions(user, expectedAfter);
	}

	// invalid placement
	@Test
	public void test6() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2") });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("1");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("2");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertFalse(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertTrue(errors.compare(errorMessages.errorSiblingHasNoParent()));
		finalAssertions(user, expectedAfter);
	}

	// simple case
	@Test
	public void test7() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"), }),
				new TestEntry("3") });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// simple case
	@Test
	public void test7Sub() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2"), }),
				new TestEntry("3") });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with only one sibling into the top of its previous
	// node
	@Test
	public void test8() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3") }),

				});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with only one sibling into the bottom of its previous
	// node
	@Test
	public void test9() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3") }),

				});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with only one sibling into the top of its next node
	@Test
	public void test10() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3") }),

				});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("3");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("2");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with only one sibling into the bottom of its next
	// node
	@Test
	public void test11() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3") }),

				});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("3");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("2");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with only one sibling into the top of its previous's
	// previous node
	@Test
	public void test12() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("4"), new TestEntry("2"),
						new TestEntry("3") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("4");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with only one sibling into the bottom of its
	// previous's previous node
	@Test
	public void test13() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("4"),
						new TestEntry("3") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("4");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with only one sibling into the top of its previous's
	// previous's previous node
	@Test
	public void test14() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("5"), new TestEntry("2"),
						new TestEntry("3"), new TestEntry("4") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("5");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with only one sibling into the bottom of its
	// previous's previous's previous node
	@Test
	public void test15() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("5"),
						new TestEntry("3"), new TestEntry("4"), }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("5");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with only one sibling into the top of its next's next
	// node
	@Test
	public void test16() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2"),
						new TestEntry("4"), }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("4");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("2");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with only one sibling into the bottom of its next's
	// next node
	@Test
	public void test17() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("4"),
						new TestEntry("2"), }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("4");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("2");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with only one sibling into the top of its next's
	// next's next node
	@Test
	public void test18() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5"), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("4"),
						new TestEntry("2"), new TestEntry("5"), }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("5");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("2");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with only one sibling into the bottom of its next's
	// next's next node
	@Test
	public void test19() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5"), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("4"),
						new TestEntry("5"), new TestEntry("2"), }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("5");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("2");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two siblings into the top of its previous node
	@Test
	public void test20() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2"),
						new TestEntry("4") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two siblings into the bottom of its previous
	// node
	@Test
	public void test21() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two siblings into the top of its next node
	@Test
	public void test22() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("3");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("2");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two siblings into the bottom of its next node
	@Test
	public void test23() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2"),
						new TestEntry("4") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("3");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("2");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two siblings into the top of its previous's
	// previous node
	@Test
	public void test24() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("4"), new TestEntry("2"),
						new TestEntry("3"), new TestEntry("5") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("4");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two siblings into the bottom of its previous's
	// previous node
	@Test
	public void test25() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("4"),
						new TestEntry("3"), new TestEntry("5") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("4");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two siblings into the top of its previous's
	// previous's previous node
	@Test
	public void test26() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5"),
						new TestEntry("6") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("5"), new TestEntry("2"),
						new TestEntry("3"), new TestEntry("4"),
						new TestEntry("6") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("5");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two siblings into the bottom of its previous's
	// previous's previous node
	@Test
	public void test27() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5"),
						new TestEntry("6") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("5"),
						new TestEntry("3"), new TestEntry("4"),
						new TestEntry("6") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("5");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two siblings into the top of its next's next
	// node
	@Test
	public void test28() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("4"),
						new TestEntry("3"), new TestEntry("5") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("5");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two siblings into the bottom of its next's next
	// node
	@Test
	public void test29() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5"), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("4"),
						new TestEntry("5"), new TestEntry("3"), }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("5");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two siblings into the top of its next's next's
	// next node
	@Test
	public void test30() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5"),
						new TestEntry("6"), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("4"),
						new TestEntry("5"), new TestEntry("3"),
						new TestEntry("6"), }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("6");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two siblings into the bottom of its next's
	// next's next node
	@Test
	public void test31() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2"), new TestEntry("3"),
						new TestEntry("4"), new TestEntry("5"),
						new TestEntry("6"), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("4"),
						new TestEntry("5"), new TestEntry("6"),
						new TestEntry("3"), }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("6");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node into the top of its parent
	@Test
	public void test32() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2"), }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node into the bottom of its parent
	@Test
	public void test33() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3"), }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node into the bottom of its grand parent
	@Test
	public void test34() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry(
						"1",
						new TestEntry[] { new TestEntry(
								"2",
								new TestEntry[] { new TestEntry("3",
										new TestEntry[] { new TestEntry("4"), }), }), }),

				});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3") }),
						new TestEntry("4"), }) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("4");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node into the top of its grand parent
	@Test
	public void test35() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry(
						"1",
						new TestEntry[] { new TestEntry(
								"2",
								new TestEntry[] { new TestEntry("3",
										new TestEntry[] { new TestEntry("4"), }), }), }),

				});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("4"),
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3") }),

				}) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("4");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node into the top of its child
	@Test
	public void test36() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3"), }) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("3");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("2");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node into the bottom of its child
	@Test
	public void test37() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2"), }) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("3");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("2");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node into the top of its grandchild
	@Test
	public void test38() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("1"), new TestEntry("3"), }) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("3");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("1");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node into the bottom of its grandchild
	@Test
	public void test39() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] { new TestEntry("2",
						new TestEntry[] { new TestEntry("3"), }), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("2", new TestEntry[] {
						new TestEntry("3"), new TestEntry("1"), }) });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("3");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("1");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with one child with only one sibling into the top of
	// its previous node
	@Test
	public void test40() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3",
								new TestEntry[] { new TestEntry("C"), }) }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2"),
						new TestEntry("C") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with one child with only one sibling into the bottom
	// of its previous node
	@Test
	public void test41() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3",
								new TestEntry[] { new TestEntry("C"), }) }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("3"),
						new TestEntry("C") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with one child with only one sibling into the top of
	// its next node
	@Test
	public void test42() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("C"), }),
						new TestEntry("3") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("C"), new TestEntry("2"),
						new TestEntry("3") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("3");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("2");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with one child with only one sibling into the bottom
	// of its next node
	@Test
	public void test43() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("C"), }),
						new TestEntry("3") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("C"), new TestEntry("3"),
						new TestEntry("2") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("3");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("2");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with one child with only one sibling into the top of
	// its previous's previous node
	@Test
	public void test44() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3"),
						new TestEntry("4",
								new TestEntry[] { new TestEntry("C"), }) }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("4"), new TestEntry("2"),
						new TestEntry("3"), new TestEntry("C") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("4");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with one child with only one sibling into the bottom
	// of its previous's previous node
	@Test
	public void test45() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3"),
						new TestEntry("4",
								new TestEntry[] { new TestEntry("C"), }) }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2"), new TestEntry("4"),
						new TestEntry("3"), new TestEntry("C") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("4");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with one child with two siblings into the top of its
	// previous node
	@Test
	public void test46() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3",
								new TestEntry[] { new TestEntry("C"), }),
						new TestEntry("4") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2"),
						new TestEntry("C"), new TestEntry("4") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two children with two siblings into the top of
	// its previous node
	@Test
	public void test47() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3", new TestEntry[] {
								new TestEntry("C1"), new TestEntry("C2") }),
						new TestEntry("4") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2"),
						new TestEntry("C1"), new TestEntry("C2"),
						new TestEntry("4") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with three children with two siblings into the top of
	// its previous node
	@Test
	public void test48() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2"),
						new TestEntry("3", new TestEntry[] {
								new TestEntry("C1"), new TestEntry("C2"),
								new TestEntry("C3") }), new TestEntry("4") }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"), new TestEntry("2"),
						new TestEntry("C1"), new TestEntry("C2"),
						new TestEntry("C3"), new TestEntry("4") }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with three children into the top of its parent
	@Test
	public void test49() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry("1",
						new TestEntry[] { new TestEntry("2", new TestEntry[] {
								new TestEntry("L"),
								new TestEntry("3", new TestEntry[] {
										new TestEntry("C1"),
										new TestEntry("C2"),
										new TestEntry("C3") }),
								new TestEntry("R"), }), }),

				});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("3"),
						new TestEntry("2", new TestEntry[] {
								new TestEntry("L"), new TestEntry("C1"),
								new TestEntry("C2"), new TestEntry("C3"),
								new TestEntry("R"), }),

				}), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with one child into the bottom of its parent
	@Test
	public void test50() {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry(
						"1",
						new TestEntry[] { new TestEntry(
								"2",
								new TestEntry[] { new TestEntry("3",
										new TestEntry[] { new TestEntry("4"), }), }), }),

				});

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("4"), }),
						new TestEntry("3"), }), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("2");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("3");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "next", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}

	// control drag a node with two children into the top of its grandchild
	@Test
	public void test51() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1", new TestEntry[] {
						new TestEntry("2",
								new TestEntry[] { new TestEntry("3"), }),
						new TestEntry("4"), }),

		});

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("2", new TestEntry[] { new TestEntry("1"),
						new TestEntry("3"), }), new TestEntry("4"), });

		final Errors errors = new Errors();
		
		final User user = dbLogic.getOrCreateUser("user");
		assertTrue(dbLogic.addEntries(before, user, createTime,
				errors));

		final String siblingId = before.getIdForValue("3");
		final Entry siblingNode = dbLogic.getEntryById(siblingId);
		final String movedId = before.getIdForValue("1");
		final Entry movedNode = dbLogic.getEntryById(movedId);
		assertTrue(dbLogic.makeEntrySiblingOfAnother(user,
				siblingNode, movedNode, true, "previous", false, errors));
		assertFalse(errors.hasErrors());
		finalAssertions(user, expectedAfter);
	}
}
