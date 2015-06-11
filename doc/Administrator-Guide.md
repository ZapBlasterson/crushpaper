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
This document covers administrator use of the CrushPaper web site.
<br><br>
For information on how to install, configure and run the CrushPaper server see this <a onclick="newPaneForLink(event, 'Help', 'help'); return false;" href="/doc/Build,-Install,-Configure-and-Run.md">document</a>.
<br><br>

### Pages
#### Notes, Quotations and Sources
1. Admins may see, modify or edit any user's notes, quotations and sources.  

#### Accounts
1. Only admins may browse the full list of accounts or search by username.
1. Admins may modify any user's account information without knowing that user's password.
1. Admins (or the user themselves) may prevent a user from signing in by marking their account as closed.
1. Only admins may mark another account as an admin.

#### Shutdown
1. Only admins may shutdown the server.
1. This method of shutting down the server reduces the risk of corrupting the database compared to killing the process.

#### Clear Database
1. This removes all data from the database.
1. If the server is configured for single user mode then that account is automatically recreated after the database is cleared.

#### Online Backup
1. This creates a copy of the database with an CSV extract for each database table.
1. Changes made while the backup is being done will NOT corrupt the backup.

#### Check DB Errors
1. This checks the database for internal consistency errors.
1. This functionality is mainly for CrushPaper developers to use.
1. This locks the database for a very long time. During this time any user operations will fail or be queued.
1. If any errors are reported this could have resulted from:
    1. A bug in the logic of the CrushPaper server.
    1. The server was shutdown in an ungraceful manner (`kill -9` is not graceful) and H2 was not able to recover its transactions.
    1. Manual modification of data in the H2 database through the H2 console interface.
    1. Corruption of the H2 database via the filesystem from another process.

#### Show Backups
1. This lists all of the backups that have been made.
1. Clicking on a backup will display the command for restoring the backup.
1. The server must be shutdown and the command must be run from the command line in order to restore a backup.

#### Offline Backup
1. This creates a copy of the database by copying the files via the command line.
1. Changes made while the backup is being done will corrupt the backup.
1. There is almost never a reason to do an offline backup instead of an online backup.

### Security
1. The CrushPaper server supports HTTPS and you are recommended to use it.
1. Passwords are stored SHA1 hashed in the database.  
1. All CrushPaper operations which modify the database or are long running are implemented as POST requests that use tokens to prevent Cross-site request forgery (CSRF).
1. The CrushPaper server is implemented entirely in Java which reduces the risks of security issues resulting from stack smashing and buffer overflows.
1. All input to the CrushPaper server is validated and anything stored in the database in length checked.
1. By policy a user's information is not stored in application or HTTP request log files. 

### H2 Database
1. By default the database is created with a blank username and password.
