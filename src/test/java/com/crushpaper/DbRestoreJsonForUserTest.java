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

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import org.junit.Ignore;
import org.junit.Test;

/** Tests restoring JSON for a user into the DB. */
public class DbRestoreJsonForUserTest extends DbLogicTestBase {
	
	private InputStreamReader getJsonStreamReader(String json) {
		return new InputStreamReader(new ByteArrayInputStream(json.getBytes()));
	}

	@Ignore
	@Test
	public void test1() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"), });

		final String json = "{ \"entries\": [ { \"type\": \"note\", \"note\": \"2\" } ] }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	@Ignore
	@Test
	public void test2() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"), new TestEntry("3"), });

		final String json = "{ \"entries\": [ { \"type\":\"note\", \"note\":\"2\" }, "
				+ "{ \"type\":\"note\", \"note\":\"3\" } ] }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	@Ignore
	@Test
	public void test3() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"),
				new TestEntry("2", new TestEntry[] { new TestEntry("3"), }) });

		final String json = "{ \"entries\": [ { \"id\":\"A\", \"type\":\"note\", \"note\":\"2\" }, "
				+ "{ \"parentId\":\"A\", \"type\":\"note\", \"note\":\"3\" } ] }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	@Ignore
	@Test
	public void test4() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"),
				new TestEntry("2", new TestEntry[] { new TestEntry("3"), }) });

		final String json = "{ \"entries\": [ { \"parentId\":\"A\", \"type\":\"note\", \"note\":\"3\" }, "
				+ "{ \"id\":\"A\", \"type\":\"note\", \"note\":\"2\" } ] }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	@Ignore
	@Test
	public void test5() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"),
				new TestEntry("2", new TestEntry[] { new TestEntry("3"),
						new TestEntry("4"), }) });

		final String json = "{ \"entries\": [ { \"id\":\"A\", \"type\":\"note\", \"note\":\"2\" }, "
				+ "{ \"parentId\":\"A\", \"id\":\"B\", \"type\":\"note\", \"note\":\"3\", \"nextSiblingId\":\"C\" }, "
				+ "{ \"parentId\":\"A\", \"id\":\"C\", \"type\":\"note\", \"note\":\"4\" } ] }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	@Ignore
	@Test
	public void test6() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"),
				new TestEntry("2", new TestEntry[] { new TestEntry("3"),
						new TestEntry("4"), }) });

		final String json = "{ \"entries\": [ { \"id\":\"A\", \"type\":\"note\", \"note\":\"2\" }, "
				+ "{ \"parentId\":\"A\", \"id\":\"C\", \"type\":\"note\", \"note\":\"4\" }, "
				+ "{ \"parentId\":\"A\", \"id\":\"B\", \"type\":\"note\", \"note\":\"3\", \"nextSiblingId\":\"C\" } ] }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	@Test
	public void test7() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final String json = "{ }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	@Test
	public void test8() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final String json = "{ \"entries\": [] }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);

	}

	@Test
	public void test9() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final String json = "";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertFalse(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertTrue(errors.compare(errorMessages.errorJson()));
			finalAssertions(user, expectedAfter);

	}

	@Test
	public void test10() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });
		
		final TestEntrySet expectedAfter = before;

		final String json = "{ }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertFalse(dbLogic.restoreJsonForUser(null,
					getJsonStreamReader(json), false, false, errors));
			assertTrue(errors.compare(errorMessages
					.errorsUserIdIsNull()));
			finalAssertions(user, expectedAfter);

	}

	@Test
	public void test11() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertFalse(dbLogic.restoreJsonForUser(user.getId(), null, false, false,
					errors));
			assertTrue(errors.compare(errorMessages
					.errorsTheInputStreamReaderIsNull()));
			finalAssertions(user, expectedAfter);

	}

	@Test
	public void test12() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final String json = "{ \"entries\": [ { \"type\": \"note\", \"note\": \"2\", \"createTime\":\"A\" } ] }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertFalse(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertTrue(errors.compare(errorMessages.errorJson()));
			finalAssertions(user, expectedAfter);

	}

	@Test
	public void test13() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final String json = "{ \"entries\": [ { \"type\": \"note\", \"note\": \"2\", \"modTime\":\"A\" } ] }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertFalse(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertTrue(errors.compare(errorMessages.errorJson()));
			finalAssertions(user, expectedAfter);

	}

	@Ignore
	@Test
	public void test14() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter = before;

		final String json = "{ \"entries\": [ { \"id\":\"A\", \"type\":\"note\", \"note\":\"2\" }, "
				+ "{ \"parentId\":\"A\", \"id\":\"C\", \"type\":\"note\", \"note\":\"4\" }, "
				+ "{ \"parentId\":\"A\", \"id\":\"B\", \"type\":\"note\", \"note\":\"3\", \"nextSiblingId\":\"D\" } ] }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertFalse(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertTrue(errors
					.compare(errorMessages.errorRealIdWasNotFound("C")));
			finalAssertions(user, expectedAfter);

	}

	@Ignore
	@Test
	public void test15() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter =  before;

		final String json = "{ \"entries\": [ { \"id\":\"A\", \"type\":\"note\", \"note\":\"2\" }, "
				+ "{ \"parentId\":\"A\", \"id\":\"B\", \"type\":\"note\", \"note\":\"3\", \"nextSiblingId\":\"C\" }, "
				+ "{ \"parentId\":\"A\", \"type\":\"note\", \"note\":\"4\" } ] }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertFalse(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertTrue(errors.compare(errorMessages
					.errorRestoredIdWasNotFound("S6")));
			finalAssertions(user, expectedAfter);

	}

	@Ignore
	@Test
	public void test16() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1"), });

		final TestEntrySet expectedAfter =  before;

		final String json = "{ \"entries\": [ { \"type\":\"note\", \"note\":\"2\" }, "
				+ "{ \"parentId\":\"A\", \"id\":\"B\", \"type\":\"note\", \"note\":\"3\", \"nextSiblingId\":\"C\" }, "
				+ "{ \"parentId\":\"A\", \"id\":\"C\", \"type\":\"note\", \"note\":\"4\" } ] }";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertFalse(dbLogic.restoreJsonForUser(user.getId(),
					getJsonStreamReader(json), false, false, errors));
			assertTrue(errors.compare(errorMessages
					.errorParentIdWasNotFound("A")));
			finalAssertions(user, expectedAfter);

	}
}
