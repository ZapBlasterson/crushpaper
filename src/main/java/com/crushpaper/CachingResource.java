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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.util.resource.Resource;

/** This class caches resources for the lifetime of the process. */
public class CachingResource extends Resource {
	private Resource resource;
	private byte[] cachedInput;
	private String directory;

	private static ConcurrentHashMap<String, CachingResource> cachedResources = new ConcurrentHashMap<String, CachingResource>();

	protected CachingResource(Resource resource, String directory) {
		this.resource = resource;
		this.directory = directory;
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
		cache();

		return cachedInput.length;
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

	private void cache() {
		// In this case the input was already cached.
		if (cachedInput != null) {
			return;
		}

		try {
			cachedInput = IOUtils.toByteArray(resource.getInputStream());
		} catch (IOException e) {
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		// The input was probably already cached by a call to length().
		cache();

		return new ByteArrayInputStream(cachedInput);
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
		String fullPath = directory + path;

		// If it was already cached return from the cache.
		CachingResource cached = cachedResources.get(fullPath);
		if (cached != null) {
			return cached;
		}

		// Get the real underlying resource.
		Resource underlyingResource = resource.addPath(path);
		if (underlyingResource == null) {
			return null;
		}

		// Cache the resource.
		cached = new CachingResource(underlyingResource, null);
		cached.cache();
		cachedResources.put(fullPath, cached);

		// Return it.
		return cached;
	}
}
