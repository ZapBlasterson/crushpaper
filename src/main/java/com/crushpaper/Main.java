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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Contains the main method that parses command line arguments and starts the
 * HTTP server.
 */
public class Main {
	public static void main(String[] args) throws IOException {
		Options options = new Options();
		options.addOption("help", false, "print this message");
		options.addOption("properties", true,
				"file system path to the crushpaper properties file");

		// Parse the command line.
		CommandLineParser parser = new BasicParser();
		CommandLine commandLine = null;

		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			System.err
					.println("crushpaper: Sorry, could not parse command line because `"
							+ e.getMessage() + "`.");
			System.exit(1);
		}

		if (commandLine == null || commandLine.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("crushpaper", options);
			return;
		}

		// Get the properties path.
		String properties = null;
		if (commandLine.hasOption("properties")) {
			properties = commandLine.getOptionValue("properties");
		}

		if (properties == null || properties.isEmpty()) {
			System.err
					.println("crushpaper: Sorry, the `properties` command argument must be specified.");
			System.exit(1);
		}

		Configuration configuration = new Configuration();
		if (!configuration.load(new File(properties))) {
			System.exit(1);
		}

		// Get values.
		File databaseDirectory = configuration.getDatabaseDirectory();
		File keyStorePath = configuration.getKeyStoreFile();
		Integer httpPort = configuration.getHttpPort();
		Integer httpsPort = configuration.getHttpsPort();
		String keyStorePassword = configuration.getKeyStorePassword();
		String keyManagerPassword = configuration.getKeyManagerPassword();
		File temporaryDirectory = configuration.getTemporaryDirectory();
		String singleUserName = configuration.getSingleUserName();
		Boolean allowSelfSignUp = configuration.getAllowSelfSignUp();
		Boolean allowSaveIfNotSignedIn = configuration
				.getAllowSaveIfNotSignedIn();
		File logDirectory = configuration.getLogDirectory();
		Boolean loopbackIsAdmin = configuration.getLoopbackIsAdmin();
		File sessionStoreDirectory = configuration.getSessionStoreDirectory();
		Boolean isOfficialSite = configuration.getIsOfficialSite();
		File extraHeaderFile = configuration.getExtraHeaderFile();

		// Validate the values.
		if (httpPort != null && httpsPort != null && httpPort.equals(httpsPort)) {
			System.err.println("crushpaper: Sorry, `"
					+ configuration.getHttpPortKey() + "` and `"
					+ configuration.getHttpsPortKey()
					+ "` must not be set to the same value.");
			System.exit(1);
		}

		if ((httpsPort == null) != (keyStorePath == null)) {
			System.err.println("crushpaper: Sorry, `"
					+ configuration.getHttpsPortKey() + "` and `"
					+ configuration.getKeyStoreKey()
					+ "` must either both be set or not set.");
			System.exit(1);
		}

		if (databaseDirectory == null) {
			System.err.println("crushpaper: Sorry, `"
					+ configuration.getDatabaseDirectoryKey()
					+ "` must be set.");
			System.exit(1);
		}

		if (singleUserName != null
				&& !AccountAttributeValidator.isUserNameValid(singleUserName)) {
			System.err.println("crushpaper: Sorry, the username in `"
					+ configuration.getSingleUserKey() + "` is not valid.");
			return;
		}

		if (allowSelfSignUp == null || allowSaveIfNotSignedIn == null
				|| loopbackIsAdmin == null) {
			System.exit(1);
		}

		String extraHeader = null;
		if (extraHeaderFile != null) {
			extraHeader = readFile(extraHeaderFile);
			if (extraHeader == null) {
				System.err.println("crushpaper: Sorry, the file `"
						+ extraHeaderFile.getPath() + "` set in `"
						+ configuration.getExtraHeaderKey()
						+ "` could not be read.");
				System.exit(1);
			}
		}

		final DbLogic dbLogic = new DbLogic(databaseDirectory);
		dbLogic.createDb();
		final Servlet servlet = new Servlet(dbLogic, singleUserName,
				allowSelfSignUp, allowSaveIfNotSignedIn, loopbackIsAdmin,
				httpPort, httpsPort, keyStorePath, keyStorePassword,
				keyManagerPassword, temporaryDirectory, logDirectory,
				sessionStoreDirectory, isOfficialSite, extraHeader);
		servlet.run();
	}

	/** Returns the contents of the file as a string or null. */
	static String readFile(File file) {
		byte[] encoded;

		try {
			encoded = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			return null;
		}

		return new String(encoded, Charset.forName("UTF-8"));
	}
}
