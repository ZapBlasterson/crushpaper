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

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/** Contains helper methods for validating fields in the account. */
public class AccountAttributeValidator {

	/** Returns true if the username is valid. */
	static boolean isUserNameValid(String value) {
		return value != null && value.length() >= 3 && value.length() <= 20
				&& Pattern.compile("^[a-z0-9]+$").matcher(value).find();
	}

	/** Returns true if the password is valid. */
	public static boolean isPasswordValid(String value) {
		return value != null && value.length() >= 8 && value.length() <= 20
				&& StringUtils.isAsciiPrintable(value);
	}

	/** Returns true if the email is valid. */
	public static boolean isEmailValid(String value) {
		return value != null
				&& value.length() <= 100
				&& Pattern
						.compile(
								"^[a-zA-Z0-9!#$%&'*+-/=?^_`{|}~.]+@[a-zA-Z0-9-.]+\\.[a-zA-Z0-9]+$")
						.matcher(value).find();
	}
}
