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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Tests DbLogicImpermanent. */
public class DbLogicImpermanentTest extends DbLogicTestBase {
	Errors errors = new Errors();

	@Test
	public void test1() {
		final TestEntrySet entries = new TestEntrySet(
				new TestEntry[] {
						new TestEntry("1"),
						new TestEntry("2", new TestEntry[] { new TestEntry(
								"3"), }) });

		final User user = dbLogic.getOrCreateUser("user");
		assertFalse(dbLogic
				.addEntries(null, user, createTime, errors));
		assertFalse(dbLogic.addEntries(entries, null, createTime, errors));

		assertTrue(dbLogic
				.addEntries(entries, user, createTime, errors));
		assertTrue(entries.areIdsValid());
		assertEquals(null, dbLogic.getEntryTestSet(null));
		final TestEntrySet resultEntries = dbLogic.getEntryTestSet(user);
		assertTrue(resultEntries.areIdsValid());
		assertTrue(resultEntries.areValuesValid());
		assertFalse(dbLogic.hasErrors(new Errors()));
		assertTrue(entries.compare(resultEntries));
		assertFalse(errors.hasErrors());
	}

	@Test
	public void test2() {
		final TestEntrySet entries = new TestEntrySet(
				new TestEntry[] {
						new TestEntry(null),
						new TestEntry("2", new TestEntry[] { new TestEntry(
								"3"), }) });

		final User user = dbLogic.getOrCreateUser("user");
		assertFalse(dbLogic.addEntries(entries, user, createTime,
				errors));
		assertFalse(errors.hasErrors());
	}

	@Test
	public void test3() {
		final TestEntrySet entries = new TestEntrySet(
				new TestEntry[] {
						null,
						new TestEntry("2", new TestEntry[] { new TestEntry(
								"3"), }) });

		final User user = dbLogic.getOrCreateUser("user");
		assertFalse(dbLogic.addEntries(entries, user, createTime,
				errors));
		assertFalse(errors.hasErrors());
	}

	@Test
	public void test4() {
		final TestEntrySet entries = new TestEntrySet(
				new TestEntry[] {
						new TestEntry("1"),
						new TestEntry("2", new TestEntry[] { new TestEntry(
								null), }) });

		final User user = dbLogic.getOrCreateUser("user");
		assertFalse(dbLogic.addEntries(entries, user, createTime,
				errors));
		assertFalse(errors.hasErrors());
	}

	@Test
	public void test5() {
		final TestEntrySet entries = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"),
				new TestEntry("2", new TestEntry[] { null, }) });

		final User user = dbLogic.getOrCreateUser("user");
		assertFalse(dbLogic.addEntries(entries, user, createTime,
				errors));
		assertFalse(errors.hasErrors());
	}
}
