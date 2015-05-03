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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

/** A resource handler that sets the expires header to now + `expiresInSeconds`. */
public class ExpiresResourceHandler extends ResourceHandler {
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy kk:mm:ss z", Locale.US);
	int seconds;
	
	ExpiresResourceHandler(int expiresInSeconds) {
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		this.seconds = expiresInSeconds;
	}
	
    @Override
    protected void doResponseHeaders(HttpServletResponse response, Resource resource, String mimeType) {
        super.doResponseHeaders(response, resource, mimeType);
        Date expiresAt = new Date();
        expiresAt.setTime(new Date().getTime() + (seconds * 1000));
        response.setHeader("expires", dateFormat.format(expiresAt));
    }
}
