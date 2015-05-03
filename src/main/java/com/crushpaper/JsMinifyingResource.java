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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

import org.eclipse.jetty.util.resource.Resource;

/**
 * This class minifies resources if their name ends with ".js" and do not
 * contain ".min.". It works by forwarding almost all calls to the resource
 * except for length.
 */
public class JsMinifyingResource extends Resource {
	private Resource resource;
	private String minified;

	protected JsMinifyingResource(Resource resource) {
		this.resource = resource;
	}

	@Override
	public boolean isContainedIn(Resource r) throws MalformedURLException {
		return resource.isContainedIn(r);
	}

	@Override
	public void close() {
		resource.close();
	}

	@Override
	public boolean exists() {
		return resource.exists();
	}

	@Override
	public boolean isDirectory() {
		return resource.isDirectory();
	}

	@Override
	public long lastModified() {
		return resource.lastModified();
	}

	@Override
	public long length() {
		// Need to minify here so that the right length is returned.
		if (!minify()) {
			return resource.length();
		}

		return minified.length();
	}

	@Override
	public URL getURL() {
		return resource.getURL();
	}

	@Override
	public File getFile() throws IOException {
		return resource.getFile();
	}

	@Override
	public String getName() {
		return resource.getName();
	}

	private boolean minify() {
		String name = resource.getName();

		// Only minify files matching this criteria.
		if (name.contains(".min.") || !name.endsWith(".js")) {
			return false;
		}

		// In this case the minified resource was already cached.
		if (minified != null) {
			return true;
		}

		JsMinifier minifier = new JsMinifier();
		try {
			minified = minifier.minify(resource.getInputStream()).toString();
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		// The input was probably already minified by a call to length().
		if (!minify()) {
			return resource.getInputStream();
		}

		return new ByteArrayInputStream(minified.getBytes(Charset
				.forName("UTF-8")));
	}

	/** Never returns a readable byte channel even if the resource supports it. */
	@Override
	public ReadableByteChannel getReadableByteChannel() throws IOException {
		return null;
	}

	@Override
	public boolean delete() throws SecurityException {
		return resource.delete();
	}

	@Override
	public boolean renameTo(Resource dest) throws SecurityException {
		return resource.renameTo(dest);
	}

	@Override
	public String[] list() {
		return resource.list();
	}

	@Override
	public Resource addPath(String path) throws IOException,
			MalformedURLException {
		Resource toReturn = resource.addPath(path);
		if (toReturn == null) {
			return null;
		}

		return new JsMinifyingResource(toReturn);
	}
}
