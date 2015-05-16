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

import org.junit.Test;

/** Tests restoring MS Word Lists into the DB. */
public class DbRestoreMsWordListFormatForUserTest extends DbLogicTestBase {
	
	private InputStreamReader getJsonStreamReader(String json) {
		return new InputStreamReader(new ByteArrayInputStream(json.getBytes()));
	}

	@Test
	public void test1() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[0]);
		
		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("R", new TestEntry[] {
						new TestEntry("1")
				}, "Imported Notebook" ) } );
		
		final String json = "*\t1";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreMsWordListFormatForUser(user.getId(),
					getJsonStreamReader(json), false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);
	}
	
	@Test
	public void test2() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[0]);
		
		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("R", new TestEntry[] {
						new TestEntry("1"),
						new TestEntry("2")
				}, "Imported Notebook" ) } );
		
		final String json = "*\t1\n" +
				"*\t2\n";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreMsWordListFormatForUser(user.getId(),
					getJsonStreamReader(json), false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);
	}

	@Test
	public void test3() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[0]);
		
		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("R", new TestEntry[] {
						new TestEntry("1"),
						new TestEntry("2"),
						new TestEntry("3")
				}, "Imported Notebook" ) } );
		
		final String json = "*\t1\n" +
				"2\n" +
				"*\t3\n";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreMsWordListFormatForUser(user.getId(),
					getJsonStreamReader(json), false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);
	}
	
	@Test
	public void test4() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[0]);
		
		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("R", new TestEntry[] {
						new TestEntry("1", new TestEntry[] {
								new TestEntry("2") } ),
						new TestEntry("3"),
						new TestEntry("4", new TestEntry[] {
								new TestEntry("5") } )
				}, "Imported Notebook" ) } );
		
		final String json = "*\t1\n" +
				"o\t2\n" +
				"3\n" +
				"*\t4\n" +
				"o\t5\n";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreMsWordListFormatForUser(user.getId(),
					getJsonStreamReader(json), false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);
	}
	
	@Test
	public void test5() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[0]);
		
		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("R", new TestEntry[] {
						new TestEntry("1", new TestEntry[] {
								new TestEntry("2", new TestEntry[] {
										new TestEntry("3") }) } ),
						new TestEntry("4"),
						new TestEntry("5", new TestEntry[] {
								new TestEntry("6", new TestEntry[] {
										new TestEntry("7") }) } )
				}, "Imported Notebook" ) } );
		
		final String json = "*\t1\n" +
				"o\t2\n" +
				"?\t3\n" +
				"4\n" +
				"*\t5\n" +
				"o\t6\n" +
				"?\t7\n";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreMsWordListFormatForUser(user.getId(),
					getJsonStreamReader(json), false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);
	}
	
	@Test
	public void test6() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[0]);
		
		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("R", new TestEntry[] {
						new TestEntry("1", new TestEntry[] {
								new TestEntry("2", new TestEntry[] {
										new TestEntry("3") } ) } ),
						new TestEntry("4", new TestEntry[] {
								new TestEntry("5") }),
				}, "Imported Notebook" ) } );
		
		final String json = "*\t1\n" +
				"o\t2\n" +
				"?\t3\n" +
				"*\t4\n" +
				"?\t5\n";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreMsWordListFormatForUser(user.getId(),
					getJsonStreamReader(json), false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);
	}
	
	@Test
	public void test7() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[0]);
		
		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("R", new TestEntry[] {
						new TestEntry("1", new TestEntry[] {
								new TestEntry("2", new TestEntry[] {
										new TestEntry("3") } ) } ),
						new TestEntry("4", new TestEntry[] {
								new TestEntry("5") }),
				}, "Imported Notebook" ) } );
		
		final String json = 
				"*\t1\n" +
				"o\t2\n" +
				"?\t3\n" +
				"*\t4\n" +
				"?\t5\n";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreMsWordListFormatForUser(user.getId(),
					getJsonStreamReader(json), false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);
	}
	
	@Test
	public void test8() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[0]);
		
		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("R", new TestEntry[] {
						new TestEntry("1", new TestEntry[] {
								new TestEntry("2"),
								new TestEntry("3") } ),
				}, "Imported Notebook" ) } );
		
		final String json = 
				"*\t1\n" +
				"o\t2\n" +
				"o\t3\n";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreMsWordListFormatForUser(user.getId(),
					getJsonStreamReader(json), false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);
	}
	
	@Test
	public void test9() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[0]);
		
		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("R", new TestEntry[] {
						new TestEntry("1", new TestEntry[] {
								new TestEntry("2"),
								new TestEntry("3", new TestEntry[] {
										new TestEntry("4"),
										new TestEntry("5") } ) } ),
				}, "Imported Notebook" ) } );
		
		final String json = 
				"*\t1\n" +
				"o\t2\n" +
				"o\t3\n" +
				"x\t4\n" +
				"x\t5\n";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreMsWordListFormatForUser(user.getId(),
					getJsonStreamReader(json), false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);
	}
	
	@Test
	public void test10() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[0]);
		
		final TestEntrySet expectedAfter = new TestEntrySet(new TestEntry[] {
				new TestEntry("R", new TestEntry[] {
						new TestEntry("1"),
						new TestEntry("2", new TestEntry[] {
								new TestEntry("3"),
								new TestEntry("4", new TestEntry[] {
										new TestEntry("5"),
										new TestEntry("6") } ),
								new TestEntry("7", new TestEntry[] {
										new TestEntry("8") }) } ),
						new TestEntry("9", new TestEntry[] {
								new TestEntry("10") } ),
				}, "Imported Notebook" ) } );
		
		final String json = "*\t1\n" +
				"*\t2\n" +
				"o\t3\n" +
				"o\t4\n" +
				"x\t5\n" +
				"x\t6\n" +
				"o\t7\n" +
				"?\t8\n" +
				"*\t9\n" +
				"?\t10\n";

		final Errors errors = new Errors();
		
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			assertTrue(dbLogic.restoreMsWordListFormatForUser(user.getId(),
					getJsonStreamReader(json), false, errors));
			assertFalse(errors.hasErrors());
			finalAssertions(user, expectedAfter);
	}
	
	
}
