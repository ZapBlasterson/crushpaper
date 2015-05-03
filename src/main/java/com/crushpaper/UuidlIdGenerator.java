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

import java.nio.ByteBuffer;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

/**
 * Returns a UUID encoded as a base64 string so that it uses less bytes. IDs are
 * case sensitive. These are used as element IDs in HTML. That means IE7 is not
 * supported.
 */
public class UuidlIdGenerator implements IdGenerator {

	@Override
	public String getAnotherId() {
		final UUID uuid = UUID.randomUUID();
		final ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return Base64.encodeBase64URLSafeString(bb.array());
	}

	/** Does not do anything. */
	@Override
	public void reset() {
	}

	@Override
	public boolean isIdWellFormed(String id) {
		return id.length() <= 30 && id.matches("^[a-zA-Z0-9_-]+$");
	}
}
