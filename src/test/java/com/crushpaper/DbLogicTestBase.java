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

import javax.persistence.PersistenceException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * This is a utility class for JUnit tests. It would be nice if the static
 * members of this class could be nonstatic, but JUnit requires the BeforeClass
 * and AfterClass methods to be static.
 */
public class DbLogicTestBase {
	protected static final DbLogicErrorMessages errorMessages = new DbLogicErrorMessages();
	protected static DbLogicImpermanent dbLogic;
	protected static final Long createTime = 1L;

	/**
	 * JUnit runs this before any tests in the class are run. This class uses a
	 * sequential ID generator so that IDs are deterministic. This is can also
	 * be helpful for interactive debugging.
	 */
	@BeforeClass
	public static void beforeClass() {
		dbLogic = new DbLogicImpermanent();
		dbLogic.createTestDb();
		dbLogic.setIdGenerator(new SequentialIdGenerator());
	}

	@AfterClass
	public static void afterClass() {
		// dbWrapper.shutDown(); breaks the unit unit tests with LOCK_MODE=1
	}

	public DbLogicTestBase() {
	}

	/**
	 * This method removes everything from the database to prepare the next
	 * test. Unfortunately this method can't create an entirely new DB unless we
	 * use a different file system path. This is because the DB memory maps
	 * files and java unmaps files in finalizers for security reasons. The
	 * finalizers can not be guaranteed to be run before the next DB is created
	 * in the same place. If the finalizers have not been run, then the files
	 * have not been unmapped and are still on disk. On windows the files will
	 * be locked and can not be deleted. On the plus side this is faster then
	 * creating a new DB, because even the impermanent DB is on disk.
	 */
	@Before
	public void before() {
		try {
			dbLogic.rollback();
		} catch (final PersistenceException e) {
		}

		dbLogic.clearData();
		dbLogic.getIdGenerator().reset();
	}

	/**
	 * Nearly all tests should run this as part of their assertions. This
	 * method: 1) Asserts that the DB does not have errors. 2) Asserts that the
	 * contents of the DB matches `expectedAfter`. 3) Asserts that IDs and
	 * values are unique.
	 */
	public TestEntrySet finalAssertions(User user, TestEntrySet expectedAfter) {
		final TestEntrySet actualAfter = dbLogic.getEntryTestSet(user);
		assertFalse(dbLogic.hasErrors(new Errors()));
		assertTrue(actualAfter.areIdsValid());
		assertTrue(actualAfter.areValuesValid());
		assertTrue(expectedAfter.compare(actualAfter));
		return actualAfter;
	}
}