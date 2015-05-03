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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.ArrayUtils;

/** See the javadoc for `exec()` static member function of this class. */
public class ThreadedSynchronousStreamReader {

	/** A helper class that reads input into a string buffer in its own thread. */
	private static class ReaderThread extends Thread {
		InputStream inputStream;
		StringBuffer result;

		private ReaderThread(InputStream inputStream, StringBuffer result) {
			this.inputStream = inputStream;
			this.result = result;
		}

		@Override
		public void run() {
			try {
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				String line = null;
				while ((line = bufferedReader.readLine()) != null)
					result.append(line);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Synchronously forks a child process capturing its standard out and err
	 * into StringBuffers. Returns the child process's exit code or -1 if it is
	 * not available. It creates threads for each stream because that is
	 * required in Java to avoid the possibility of deadlock.
	 */
	public static int exec(StringBuffer output, StringBuffer error,
			String args[]) {
		try {
			String osName = System.getProperty("os.name");
			if (osName.equals("Windows 95")) {
				String[] prefix = new String[2];
				prefix[0] = "command.com";
				prefix[1] = "/C";
				args = ArrayUtils.addAll(prefix, args);
			} else if (osName.startsWith("Windows")) {
				String[] prefix = new String[2];
				prefix[0] = "cmd.exe";
				prefix[1] = "/C";
				args = ArrayUtils.addAll(prefix, args);
			}

			Runtime runtime = Runtime.getRuntime();
			Process childProcess = runtime.exec(args);
			ReaderThread errorReader = new ReaderThread(
					childProcess.getErrorStream(), output);
			ReaderThread outputReader = new ReaderThread(
					childProcess.getInputStream(), error);
			errorReader.start();
			outputReader.start();
			int exitValue = childProcess.waitFor();
			return exitValue;
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return -1;
	}
}
