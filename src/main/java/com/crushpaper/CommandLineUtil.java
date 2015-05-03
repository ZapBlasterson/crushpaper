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

import org.apache.commons.lang3.StringUtils;

/** Command line related utility functions. */
public class CommandLineUtil {
	/** Copies one directory to another. */
	static public int copyDirectory(StringBuffer out, StringBuffer err,
			String source, String destination) {
		return ThreadedSynchronousStreamReader.exec(out, err,
				getCopyDirectoryArgs(source, destination));
	}

	/** Copies one directory to another. */
	static public int removeDirectory(StringBuffer out, StringBuffer err,
			String directory) {
		return ThreadedSynchronousStreamReader.exec(out, err,
				getRmDirArgs(directory));
	}

	/** Returns the array of command line arguments to copy a directory. */
	static public String[] getCopyDirectoryArgs(String source,
			String destination) {
		String osName = System.getProperty("os.name");
		if (!osName.startsWith("Windows")) {
			return new String[] { "cp", "-rp", source, destination };
		}

		// Reference
		// http://www.microsoft.com/resources/documentation/windows/xp/all/proddocs/en-us/xcopy.mspx?mfr=true
		return new String[] { "xcopy", source, destination, "/i", "/k", "/h",
				"/f", "/s", "/e", "/q" };
	}

	/** Returns the array of command line arguments to remove a directory. */
	static public String[] getRmDirArgs(String directory) {
		String osName = System.getProperty("os.name");
		if (!osName.startsWith("Windows")) {
			return new String[] { "rm", "-rf", directory };
		}

		return new String[] { "rmdir", directory, "/s", "/q" };
	}

	/**
	 * Returns the array of with double quotes around any argument with spaces
	 * in it so it can be run from the command line.
	 */
	static public String getArgsForCopyAndPaste(String[] args) {
		String[] result = new String[args.length];

		for (int i = 0; i < args.length; ++i) {
			String arg = args[i];
			result[i] = arg.contains(" ") ? "\"" + arg + "\"" : arg;
		}

		return StringUtils.join(result, " ");
	}
}
