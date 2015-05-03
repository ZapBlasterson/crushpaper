<!---
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
--->
The components of the system are:

### The Chrome Extension
1. This extension has the capability to save quotations by sending them over HTTP or HTTPS to the CrushPaper server.
1. It does not have read access to any information stored on the server.
1. Quotations can be created by:
    1. right clicking selected text within a web page and choosing the CrushPaper Quote Source action, or by
    1. selected text within a web page and clicking the CrushPaper Quote Source browser action on the right of the URL bar.
1. This extension is configured with the URL of the CrushPaper Server.
    
### The CrushPaper GUI
1. This is a rich JavaScript GUI served by the CrushPaper HTTP server.
1. Anything that can be done to the CrushPaper server can be done through this GUI.

### The CrushPaper Server
1. This is a Java application. It is packaged as a single executable JAR file with all of its dependencies to make it easy to install, configure and run.
1. It is a relatively simple application that uses Jetty as its embedded HTTP server and is configured with a simple properties file.

### The CrushPaper Database
1. The database technology is <a href="http://http://www.h2database.com">the H2 database</a>.
1. CrushPaper can be run with the H2 database external to or embedded within the CrushPaper server.
1. This enables CrushPaper to leverage H2's built-in clustering and replication functionality.

 