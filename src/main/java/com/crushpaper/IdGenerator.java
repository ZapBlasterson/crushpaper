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

/** An interface for creating unique IDs. */
public interface IdGenerator {
	/** Returns another ID. */
	public String getAnotherId();

	/** Resets the ID generator. Some ID generators do not need to be reset. */
	public void reset();

	/**
	 * Returns true if the id is well formed according to the method that the
	 * generator uses to generate IDs.
	 */
	public boolean isIdWellFormed(String id);
}
