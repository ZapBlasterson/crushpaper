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

/** Tests the TestEntry class. */
public class TestEntryTest {
	@Test
	public void test() {
		final TestEntry oneEntry = new TestEntry("1");
		final TestEntry twoEntry = new TestEntry("2");
		final TestEntry threeEntry = new TestEntry("3", new TestEntry[] {
				new TestEntry("4"), new TestEntry("5"), });

		final TestEntry[] rootEntries = new TestEntry[] { oneEntry, twoEntry,
				threeEntry };
		final TestEntrySet entries = new TestEntrySet(rootEntries);

		assertTrue("Simple case of validity", entries.areValuesValid());
		assertTrue("Check caching", entries.areValuesValid());
		assertFalse(entries.areIdsValid());
		assertFalse(entries.areIdsValid());
		assertEquals(null, entries.getRootEntryByValue("0"));
		assertEquals(null, entries.getRootEntryByValue("4"));
		assertEquals(null, entries.getRootEntryByValue("5"));
		assertEquals(oneEntry, entries.getRootEntryByValue("1"));
		assertEquals(twoEntry, entries.getRootEntryByValue("2"));
		assertEquals(threeEntry, entries.getRootEntryByValue("3"));
		assertEquals(null, entries.getRootEntryByValue(null));
		assertTrue(entries.compare(entries));
		assertTrue(entries.getRootEntries() == rootEntries);
	}

	@Test
	public void test2() {
		final TestEntrySet entries = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"),
				new TestEntry("2"),
				new TestEntry("3", new TestEntry[] { new TestEntry("4"),
						new TestEntry("1"), }) });

		assertFalse("A duplicate value", entries.areValuesValid());
		assertTrue(entries.compare(entries));
	}

	@Test
	public void test3() {
		final TestEntrySet entries = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"),
				new TestEntry("2"),
				new TestEntry("3",
						new TestEntry[] { new TestEntry("4"), null, }) });

		assertFalse("A null entry", entries.areValuesValid());
		assertTrue(entries.compare(entries));
	}

	@Test
	public void test4() {
		final TestEntrySet entries = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"),
				new TestEntry("2"),
				new TestEntry("3", new TestEntry[] { new TestEntry("4"),
						new TestEntry(null) }) });

		assertFalse("A null value", entries.areValuesValid());
		assertTrue(entries.compare(entries));
	}

	@Test
	public void test5() {
		final TestEntry nullValueEntry = new TestEntry(null);
		final TestEntrySet entries = new TestEntrySet(
				new TestEntry[] { nullValueEntry });

		assertEquals(nullValueEntry, entries.getRootEntryByValue(null));
		assertFalse("A null value", entries.areValuesValid());
		assertTrue(entries.compare(entries));
	}

	@Test
	public void test6() {
		final TestEntrySet entries1 = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"),
				new TestEntry("2"),
				new TestEntry("3", new TestEntry[] { new TestEntry("4"),
						new TestEntry("5"), }) });

		final TestEntrySet entries2 = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"),
				new TestEntry("2"),
				new TestEntry("3", new TestEntry[] { new TestEntry("4"),
						new TestEntry("5"), }) });

		assertTrue(entries1.areValuesValid());
		assertTrue(entries2.areValuesValid());
		assertTrue(entries1.compare(entries2));
		assertTrue(entries2.compare(entries1));
	}

	@Test
	public void test7() {
		final TestEntrySet entries1 = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"),
				new TestEntry("3", new TestEntry[] { new TestEntry("5"), }) });

		final TestEntrySet entries2 = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"),
				new TestEntry("3", new TestEntry[] { new TestEntry("4") }) });

		assertTrue(entries1.areValuesValid());
		assertTrue(entries2.areValuesValid());
		assertTrue(entries1.compare(entries1));
		assertTrue(entries2.compare(entries2));
		assertFalse(entries1.compare(entries2));
		assertFalse(entries2.compare(entries1));
	}

	@Test
	public void test8() {
		final TestEntrySet entries1 = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"),
				new TestEntry("3", new TestEntry[] { new TestEntry("4") }) });

		final TestEntrySet entries2 = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"),
				new TestEntry("2"),
				new TestEntry("3", new TestEntry[] { new TestEntry("4"),
						new TestEntry("5") }) });

		assertTrue(entries1.areValuesValid());
		assertTrue(entries2.areValuesValid());
		assertTrue(entries1.compare(entries1));
		assertTrue(entries2.compare(entries2));
		assertFalse(entries1.compare(entries2));
		assertFalse(entries2.compare(entries1));
	}

	@Test
	public void test9() {
		final TestEntrySet entries1 = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"),
				new TestEntry("3", new TestEntry[] { null }) });

		final TestEntrySet entries2 = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"),
				new TestEntry("3", new TestEntry[] { null }) });

		assertFalse(entries1.areValuesValid());
		assertFalse(entries2.areValuesValid());
		assertTrue(entries1.compare(entries2));
		assertTrue(entries2.compare(entries1));
	}

	@Test
	public void test10() {
		final TestEntrySet entries1 = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"),
				new TestEntry("3", new TestEntry[] { new TestEntry(null) }) });

		final TestEntrySet entries2 = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2"),
				new TestEntry("3", new TestEntry[] { new TestEntry(null) }) });

		assertFalse(entries1.areValuesValid());
		assertFalse(entries2.areValuesValid());
		assertTrue(entries1.compare(entries2));
		assertTrue(entries2.compare(entries1));
	}

	@Test
	public void test11() {
		final TestEntrySet entries1 = new TestEntrySet(null);
		assertTrue(entries1.getRootEntries() == null);
		assertTrue(entries1.getRootEntryByValue("1") == null);

		final TestEntrySet entries2 = new TestEntrySet(
				new TestEntry[] { new TestEntry("1") });

		assertFalse(entries1.areValuesValid());
		assertTrue(entries2.areValuesValid());
		assertTrue(entries2.compare(entries2));
		assertTrue(entries1.compare(entries1));
		assertFalse(entries1.compare(entries2));
		assertFalse(entries2.compare(entries1));
	}

	@Test
	public void test12() {
		final TestEntrySet entries1 = new TestEntrySet(
				new TestEntry[] { new TestEntry("1") });

		final TestEntrySet entries2 = new TestEntrySet(
				new TestEntry[] { new TestEntry("2") });

		assertTrue(entries1.areValuesValid());
		assertTrue(entries2.areValuesValid());
		assertTrue(entries2.compare(entries2));
		assertTrue(entries1.compare(entries1));
		assertFalse(entries1.compare(entries2));
		assertFalse(entries2.compare(entries1));
	}

	@Test
	public void test13() {
		final TestEntrySet entries1 = new TestEntrySet(
				new TestEntry[] { new TestEntry("1") });

		final TestEntrySet entries2 = new TestEntrySet(new TestEntry[] {
				new TestEntry("1"), new TestEntry("2") });

		assertTrue(entries1.areValuesValid());
		assertTrue(entries2.areValuesValid());
		assertTrue(entries2.compare(entries2));
		assertTrue(entries1.compare(entries1));
		assertFalse(entries1.compare(entries2));
		assertFalse(entries2.compare(entries1));
	}

	@Test
	public void test14() {
		final TestEntrySet entries1 = new TestEntrySet(new TestEntry[] { null });

		final TestEntrySet entries2 = new TestEntrySet(
				new TestEntry[] { new TestEntry(null) });

		assertFalse(entries1.areValuesValid());
		assertFalse(entries2.areValuesValid());
		assertTrue(entries2.compare(entries2));
		assertTrue(entries1.compare(entries1));
		assertFalse(entries1.compare(entries2));
		assertFalse(entries2.compare(entries1));
	}

}
