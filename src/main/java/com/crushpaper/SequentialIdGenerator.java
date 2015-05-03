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
 * Generates IDs that are unique within the lifetime of the object, until it is
 * `reset()`. This can be helpful during interactive debugging or for
 * deterministic testing.
 */
public class SequentialIdGenerator implements IdGenerator {
	int id = 0;
	UuidlIdGenerator UuidlIdGenerator = new UuidlIdGenerator();

	@Override
	public String getAnotherId() {
		return "S" + Integer.toString(getNextIntId());
	}

	/**
	 * This is in its own method to wrap the minimum required logic in a
	 * synchronized block.
	 */
	synchronized private int getNextIntId() {
		return ++id;
	}

	/** This actually resets the generator. */
	@Override
	synchronized public void reset() {
		id = 0;
	}

	@Override
	public boolean isIdWellFormed(String id) {
		return UuidlIdGenerator.isIdWellFormed(id);
	}
}
