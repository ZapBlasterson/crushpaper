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

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Tests creating a JSON backup for a user from the DB. */
public class DbJsonBackupForUserTest extends DbLogicTestBase {

	@Test
	public void test1() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {});

		final String expectedJson = "{\n" + "\"entries\": [\n" + "]\n}\n";

		final Errors errors = new Errors();
		try {
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			final StringBuilder result = new StringBuilder();
			dbLogic.backupJsonForUser(user, result);
			assertEquals(expectedJson, result.toString());
		} catch (final IOException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test2() {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] { new TestEntry(
				"1") });

		final String expectedJson = "{\n" + "\"entries\": [\n" + "{\n"
				+ "\"note\": \"1\",\n" + "\"modTime\": 1,\n"
				+ "\"createTime\": 1,\n" + "\"id\": \"S3\",\n"
				+ "\"type\": \"root\"\n" + "}\n" + "]\n}\n";

		final Errors errors = new Errors();
		try {
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			final StringBuilder result = new StringBuilder();
			dbLogic.backupJsonForUser(user, result);
			assertEquals(expectedJson, result.toString());
		} catch (final IOException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test3() throws IOException {
		final TestEntrySet before = new TestEntrySet(new TestEntry[] {
				new TestEntry("1", 1), new TestEntry("2", 2) });

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode node1 = mapper.readTree("{\n"
				+ "\"note\": \"1\",\n" + "\"modTime\": 1,\n"
				+ "\"createTime\": 1,\n" + "\"id\": \"S3\",\n"
				+ "\"type\": \"root\"\n" + "}");
		
		final JsonNode node2 = mapper.readTree("{\n"
				+ "\"note\": \"2\",\n" + "\"modTime\": 2,\n"
				+ "\"createTime\": 2,\n" + "\"id\": \"S4\",\n"
				+ "\"type\": \"root\"\n" + "}\n");
		final Errors errors = new Errors();
		try {
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			final StringBuilder result = new StringBuilder();
			dbLogic.backupJsonForUser(user, result);
			final JsonNode resultNode = mapper.readTree(result.toString());
			assertTrue(resultNode.isObject());
			final JsonNode entriesNodes = resultNode.get("entries");
			assertTrue(entriesNodes.isArray());
			assertEquals(2, entriesNodes.size());
			boolean matched1 = false, matched2 = false;
			for(int i = 0; i < 2; ++i) {
				final JsonNode obj = entriesNodes.get(i);
				if(obj.equals(node1)) {
					matched1 = true;
				} else if(obj.equals(node2)) {
					matched2 = true;
				}
			}
				
			assertTrue(matched1);
			assertTrue(matched2);
		} catch (final IOException e) {
			assertTrue(false);
		}
	}

	@Test
	public void test4() throws IOException {
		final TestEntrySet before = new TestEntrySet(
				new TestEntry[] { new TestEntry("1", new TestEntry[] {
						new TestEntry("2", 2), new TestEntry("3", 3) }) });

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode node1 = mapper.readTree("{\n"
				+ "\"note\": \"3\",\n" + "\"modTime\": 3,\n"
				+ "\"createTime\": 3,\n" + "\"id\": \"S5\",\n"
				+ "\"type\": \"note\",\n"
				+ "\"parentId\": \"S3\"\n"
				+ "}");
		
		final JsonNode node2 = mapper.readTree("{\n"
				+ "\"note\": \"2\",\n" + "\"modTime\": 2,\n"
				+ "\"createTime\": 2,\n" + "\"id\": \"S4\",\n"
				+ "\"type\": \"note\",\n"
				+ "\"parentId\": \"S3\",\n"
				+ "\"nextSiblingId\": \"S5\"\n"
				+ "}");
		
		final JsonNode node3 = mapper.readTree("{\n"
				+ "\"note\": \"1\",\n" + "\"modTime\": 1,\n"
				+ "\"createTime\": 1,\n" + "\"id\": \"S3\",\n"
				+ "\"type\": \"root\"\n" + "}\n");

		final Errors errors = new Errors();
		try {
			final User user = dbLogic.getOrCreateUser("user");
			assertTrue(dbLogic.addEntries(before, user, createTime,
					errors));
			dbLogic.commit();
			final StringBuilder result = new StringBuilder();
			dbLogic.backupJsonForUser(user, result);
			final JsonNode resultNode = mapper.readTree(result.toString());
			assertTrue(resultNode.isObject());
			final JsonNode entriesNodes = resultNode.get("entries");
			assertTrue(entriesNodes.isArray());
			assertEquals(3, entriesNodes.size());
			boolean matched1 = false, matched2 = false, matched3 = false;
			for(int i = 0; i < 3; ++i) {
				final JsonNode obj = entriesNodes.get(i);
				if(obj.equals(node1)) {
					matched1 = true;
				} else if(obj.equals(node2)) {
					matched2 = true;
				} else if(obj.equals(node3)) {
					matched3 = true;
				}
			}
				
			assertTrue(matched1);
			assertTrue(matched2);
			assertTrue(matched3);
		} catch (final IOException e) {
			assertTrue(false);
		}
	}
}