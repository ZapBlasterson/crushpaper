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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * This class loads configuration from a properties file. It validates
 * individual properties as they are read. It is responsible for printing error
 * messages.
 */
public class Configuration {
	private Properties properties;

	/** Loads the file. Returns false if it could not be loaded. */
	public boolean load(File configFile) {
		FileInputStream fileInput;
		try {
			fileInput = new FileInputStream(configFile);
		} catch (FileNotFoundException e) {
			System.err.println("crushpaper: Sorry, the configuration file `"
					+ configFile.getAbsolutePath()
					+ "` could not be imported because it does not exist.");
			return false;
		}

		properties = new Properties();
		try {
			properties.load(fileInput);
		} catch (IOException e) {
			System.err.println("crushpaper: Sorry, the configuration file `"
					+ configFile.getAbsolutePath()
					+ "` could not be imported because `" + e.getMessage()
					+ "`.");
			return false;
		}

		return true;
	}

	/**
	 * Returns the name of the key that indicates if the server allows users to
	 * save notes even if they are not signed in.
	 */
	public String getAllowSaveIfNotLoggedInKey() {
		return "allowSaveIfNotSignedIn";
	}

	/**
	 * Returns true if the server allows users to save notes even if they are
	 * not signed in.
	 */
	public Boolean getAllowSaveIfNotSignedIn() {
		return getBooleanHelper(getAllowSaveIfNotLoggedInKey());
	}

	/**
	 * Returns the name of the key that indicates if the server consider
	 * connections over the loopback address to be admins.
	 */
	public String getLoopbackIsAdminKey() {
		return "loopbackIsAdmin";
	}

	/**
	 * Returns true if the server consider connections over the loopback address
	 * to be admins.
	 */
	public Boolean getLoopbackIsAdmin() {
		return getBooleanHelper(getLoopbackIsAdminKey());
	}

	/**
	 * Returns true if the server is the official crushpaper.com site.
	 */
	public Boolean getIsOfficialSite() {
		Boolean value = getBooleanHelper("isOfficialSite");
		if (value == null) {
			return false;
		}

		return value;
	}

	/**
	 * Returns the name of the key that indicates if the server allows users to
	 * save notes even if they are not logged in.
	 */
	public String getAllowSelfSignUpKey() {
		return "allowSelfSignUp";
	}

	/**
	 * Returns true if the server allows users to save notes even if they are
	 * not logged in.
	 */
	public Boolean getAllowSelfSignUp() {
		return getBooleanHelper(getAllowSelfSignUpKey());
	}

	/** Helper function. */
	private Boolean getBooleanHelper(String key) {
		String value = properties.getProperty(key, "false");
		if (value != null && !value.equals("true") && !value.equals("false")) {
			System.err.println("crushpaper: Sorry, the value `" + value
					+ "` for property `" + key + "` is not true or false.");
			System.exit(1);
		}

		return Boolean.valueOf(value);
	}

	/**
	 * Returns the name of the key that indicates if the server is in single
	 * user mode.
	 */
	public String getSingleUserKey() {
		return "singleUser";
	}

	/**
	 * Returns the username if the server is in single user mode or null if the
	 * server is not in single user mode.
	 */
	public String getSingleUserName() {
		return properties.getProperty(getSingleUserKey(), null);
	}

	/**
	 * Returns the name of the key for the HTTP port that the server should
	 * listen on.
	 */
	public String getHttpPortKey() {
		return "http.port";
	}

	/** Returns the HTTP port that the server should listen on. */
	public Integer getHttpPort() {
		return getPort(getHttpPortKey(), "8080");
	}

	/**
	 * Returns the name of the key for the HTTPS port that the server should
	 * listen on.
	 */
	public String getHttpsPortKey() {
		return "https.port";
	}

	/**
	 * Returns the name of the key for the HTTPS port that is being proxied to the server's HTTPS port.
	 */
	public String getHttpsProxiedPortKey() {
		return "https.proxiedPort";
	}
	
	/** Returns the HTTPS port that the server should listen on. */
	public Integer getHttpsPort() {
		return getPort(getHttpsPortKey(), null);
	}

	/** Returns the HTTPS  port that is being proxied to the server's HTTPS port. */
	public Integer getHttpsProxiedPort() {
		return getPort(getHttpsProxiedPortKey(), null);
	}

	/** Returns the name of the key for the key store file used for SSL. */
	public String getKeyStoreKey() {
		return "https.keystore";
	}

	/** Returns the key store file used for SSL. */
	public File getKeyStoreFile() {
		return getFileThatExists(getKeyStoreKey(), null);
	}

	/** Returns the key store password for the key store file. */
	public String getKeyStorePassword() {
		return properties.getProperty("https.keystorePassword");
	}

	/** Returns the key manager password for the key store file. */
	public String getKeyManagerPassword() {
		return properties.getProperty("https.keymanagerPassword");
	}

	/**
	 * Returns the name of the key for the file containing extra HTML header
	 * data.
	 */
	public String getExtraHeaderKey() {
		return "http.extraHeader";
	}

	/** Returns the file containing extra HTML header data. */
	public File getExtraHeaderFile() {
		return getFileThatExists(getExtraHeaderKey(), null);
	}

	/**
	 * Returns the name of the key for the directory used to store the database.
	 */
	public String getDatabaseDirectoryKey() {
		return "database.directory";
	}

	/** Returns the directory used to store the database. */
	public File getDatabaseDirectory() {
		return getDirectory(getDatabaseDirectoryKey(), null);
	}

	/** Returns the directory in which sessions are persisted. */
	public File getSessionStoreDirectory() {
		return getDirectory("sessionStore.directory", null);
	}

	/**
	 * Returns the name of the key for the directory used to store the log
	 * files.
	 */
	public String getLogDirectoryKey() {
		return "logs.directory";
	}

	/** Returns the directory used to store the log files.. */
	public File getLogDirectory() {
		return getDirectory(getLogDirectoryKey(), null);
	}

	/** Returns the directory used to store temporary files. */
	public File getTemporaryDirectory() {
		return getDirectory("temporary.directory", null);
	}

	/** A helper method that validates that a property value is a port. */
	private Integer getPort(String key, String defaultValue) {
		String stringValue = properties.getProperty(key, defaultValue);
		if (stringValue == null) {
			return null;
		}

		if (!Pattern.compile("^[0-9]+$").matcher(stringValue).find()) {
			System.err.println("crushpaper: Sorry, the value `" + stringValue
					+ "` for property `" + key + "` is not a number.");
			System.exit(1);
		}

		int intValue = Integer.parseInt(stringValue);
		if (intValue < 1 || intValue > 256 * 256) {
			System.err.println("crushpaper: Sorry, the value `" + stringValue
					+ "` for property `" + key + "` is not a port number.");
			System.exit(1);
		}

		return intValue;
	}

	/**
	 * A helper method that validates that a property value is a path that
	 * exists.
	 */
	private File getPathThatExists(String key, String defaultValue) {
		String value = properties.getProperty(key, defaultValue);
		if (value == null)
			return null;

		File file = new File(value);
		if (!file.exists()) {
			System.err.println("crushpaper: Sorry, the path `" + value
					+ "` for property `" + key
					+ "` does not exist in the file system.");
			System.exit(1);
		}

		return file;
	}

	/**
	 * A helper method that validates that a property value is a directory that
	 * exists.
	 */
	private File getDirectory(String key, String defaultValue) {
		String value = properties.getProperty(key, defaultValue);
		if (value == null) {
			return null;
		}

		File file = new File(value);
		if (file.exists() && !file.isDirectory()) {
			System.err.println("crushpaper: Sorry, the path `"
					+ file.getAbsolutePath() + "` for property `" + key
					+ "` is not a directory.");
			System.exit(1);
		}

		return file;
	}

	/**
	 * A helper method that validates that a property value is a file that
	 * exists.
	 */
	private File getFileThatExists(String key, String defaultValue) {
		File file = getPathThatExists(key, defaultValue);
		if (file == null)
			return null;

		if (!file.isFile()) {
			System.err.println("crushpaper: Sorry, the path `"
					+ file.getAbsolutePath() + "` for property `" + key
					+ "` is not a file.");
			System.exit(1);
		}

		return file;
	}
}
