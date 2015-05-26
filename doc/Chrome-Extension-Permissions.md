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

### What Permissions Does The CrushPaper Chrome Extension Have and Why Does It Have Them?

The Chrome extension doesn't know in advance what domain it must be able to communicate with because you can run the CrushPaper server on your own computer.<br><br>

Since CrushPaper is open source you can verify in its source code that CrushPaper is only doing good things with these permissions.   

#### Cookies

The Chrome extension can read any cookies in your browser. This is only used to get your session id for the crushpaper server.
   
#### HTTP Requests

The Chrome extension can make HTTP requests to any server. This is only used to save quotations.

#### Context Menu

The Chrome extension adds an item to your browser's context menu so that you can easily save quotations. 

#### Tab URLs

The Chrome extension can query the URLS of your open tabs. This allows the extension to check if its settings window is already open before opening it. 

#### Tab Contents

The Chrome extension can query the content of your open tabs. This allows the extension to get the text for the quotations you want to save. 

#### Storage

The Chrome extension can store information on your device. This allows the extension to save the connection details of your CrushPaper server in case you don't want to use crushpaper.com. 
