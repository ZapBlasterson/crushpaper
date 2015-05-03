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

/**
 * Represents an entry in the DB. Part of the unit testing scaffolding, but this
 * is not a mock or a fake.
 */
class TestEntry {
	private final String value;
	private TestEntry[] children = null;
	private String id = null;
	private long createTime = -1L;

	public TestEntry(String value) {
		this.value = value;
	}

	public TestEntry(String value, long createTime) {
		this.value = value;
		this.createTime = createTime;
	}

	public TestEntry(String value, String id) {
		this.value = value;
		this.id = id;
	}

	public TestEntry(String value, TestEntry[] children) {
		this.value = value;
		this.children = children;
	}

	public void setChildren(TestEntry[] children) {
		this.children = children;
	}

	/** equals() is not implemented so that hashCode() does not need to be. */
	public boolean compare(TestEntry other) {
		if (other == null) {
			return false;
		}

		if ((value == null) != (other.value == null)) {
			return false;
		}

		if (value != null && !other.value.equals(value)) {
			return false;
		}

		if ((children == null) != (other.children == null)) {
			return false;
		}

		if (children != null) {
			if (children.length != other.children.length) {
				return false;
			}

			for (int i = 0; i < children.length; ++i) {
				if ((children[i] == null) != (other.children[i] == null)) {
					return false;
				}

				if (children[i] != null
						&& !children[i].compare(other.children[i])) {
					return false;
				}
			}
		}

		return true;
	}

	public String getValue() {
		return value;
	}

	public String getId() {
		return id;
	}

	public TestEntry[] getChildren() {
		return children;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getCreateTime() {
		return createTime;
	}
}
