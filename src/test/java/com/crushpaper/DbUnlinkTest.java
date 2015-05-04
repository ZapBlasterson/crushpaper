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

/** Tests unlinking entries. */
public class DbUnlinkTest extends DbLogicTestBase {

	// unlink with null node
	@Test
	public void test1() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			assertFalse(dbLogic.unlinkEntry(user, null, false,
					errors));
			assertTrue(errors.compare(errorMessages.errorEntryIsNull()));
			finalAssertions(user, expectedAfter);

	}

	// unlink with null user
	@Test
	public void test2() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toUnlinkId = before.getIdForValue("1");
			final Entry toUnlinkNode = dbLogic.getEntryById(toUnlinkId);
			assertFalse(dbLogic.unlinkEntry(null, toUnlinkNode, 
					false, errors));
			assertTrue(errors.compare(errorMessages.errorUserIsNull()));
			finalAssertions(user, expectedAfter);

	}

	// unlink a source with no children or parent
	@Test
	public void test2a() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("S", true) });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toUnlinkId = before.getIdForValue("S");
			final Entry toUnlinkNode = dbLogic.getEntryById(toUnlinkId);
			assertTrue(dbLogic.unlinkEntry(user, toUnlinkNode, 
					false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}
	
	// unlink a quotation with no children or parent
	@Test
	public void test2b() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("S", true), TestEntry.newQuotation("2", "S") });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toUnlinkId = before.getIdForValue("2");
			final Entry toUnlinkNode = dbLogic.getEntryById(toUnlinkId);
			assertTrue(dbLogic.unlinkEntry(user, toUnlinkNode, 
					false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}
	
	// unlink a root
	@Test
	public void test2c() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1") });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toUnlinkId = before.getIdForValue("1");
			final Entry toUnlinkNode = dbLogic.getEntryById(toUnlinkId);
			assertFalse(dbLogic.unlinkEntry(user, toUnlinkNode, 
					false, errors));
			assertTrue(errors.compare(errorMessages.errorUserOnlyQuotationsAndSourcesMayBeUnlinked()));
			finalAssertions(user, expectedAfter);

	}
	
	// unlink a note
	@Test
	public void test2d() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", new TestEntry[] { new TestEntry("2") } ) });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toUnlinkId = before.getIdForValue("2");
			final Entry toUnlinkNode = dbLogic.getEntryById(toUnlinkId);
			assertFalse(dbLogic.unlinkEntry(user, toUnlinkNode, 
					false, errors));
			assertTrue(errors.compare(errorMessages.errorUserOnlyQuotationsAndSourcesMayBeUnlinked()));
			finalAssertions(user, expectedAfter);

	}
	
	// unlink a quotation node with parent and 0 children
	@Test
	public void test3() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("S", true), new TestEntry("1", new TestEntry[] { TestEntry.newQuotation("2", "S") } ) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("S"), new TestEntry("1"), new TestEntry("2") });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toUnlinkId = before.getIdForValue("2");
			final Entry toUnlinkNode = dbLogic.getEntryById(toUnlinkId);
			assertTrue(dbLogic.unlinkEntry(user, toUnlinkNode, 
					false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}
	
	// unlink a quotation node with parent and 1 child
	@Test
	public void test4() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("S", true), new TestEntry("1", new TestEntry[] { TestEntry.newQuotation("2", "S",
						new TestEntry[] { new TestEntry("3") } ) } ) });

		final TestEntrySet expectedAfter = new TestEntrySet(
				new TestEntry[] { new TestEntry("S"), new TestEntry("1", new TestEntry[] { new TestEntry("3") } ), new TestEntry("2") });

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));

			final String toUnlinkId = before.getIdForValue("2");
			final Entry toUnlinkNode = dbLogic.getEntryById(toUnlinkId);
			assertTrue(dbLogic.unlinkEntry(user, toUnlinkNode, 
					false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}
}
