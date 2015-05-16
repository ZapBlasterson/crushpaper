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
These tests are manually performed before each release.
<br><br>
When the user interface stops changes rapidly the bulk of these tests will be automated with Pixel Perfect tests.

1. Chrome Extension
    1. The Options Window
        1. Verify the error message when the Service field is blank.
        1. Verify the error message when the Service field does not begin with http:// or https://.
        1. After entering information in an input field verify the status message that the data has not been saved.
        1. After clicking the save button verify the status message that the data has been saved.
        1. Close and reopen the options window and verify that the saved information has been persisted.
    1. The Browser Action
        1. Before quoting text in the document verify that clicking the browser action icon displays the popup window and that the title and URL are populated and that the note field has the focus.
        1. Quoting text in the document and verify that clicking the browser action icon displays the popup window and that the title, URL and quotation are populated and that the note field has the focus.
        1. Verify that a quotation can be saved by clicking the save button.
        1. Verify that a quotation can be saved by clicking the Ctrl-s button.
        1. Verify that a quotation can be saved with and without empty text for the note.
        1. Verify that a quotation can be not be saved with an empty title or URL.
        1. Update the options to use an incorrect port. Try to save a quotation and verify the error message.
        1. Update the options to remove the server setting. Click the browser action item. Verify the error message and that the options tab is opened.
    1. The Context Menu Action
        1. Right click a document without quoting text and verify that the quotation action is not present in the context menu.
        1. Right click quoted text in a document and verify that the quotation action is present in the context menu and has the right icon.
        1. Click the quotation action and verify that the quotation popup displays and contains the title, URL and selected text.
        1. Save the quotation.
        1. Verify the window can be closed by clicking the X and that the quotation is not saved.
        1. Verify the window can be closed by pressing Alt-F4 and that the quotation is not saved.
1. Website - Test in Chrome, Firefox, Internet Explorer and Opera 
    1. Accounts
         1. Single User Mode
             1. Configure the server for single user mode and restart it.
             1. Verify that the site does not have sign or or create account links.
             1. Verify that a note can be saved.
             1. During automation verify that an HTTP sign up request is not honored.
             1. During automation verify that an HTTP create account request is not honored.
         1. Multi User Mode
             1. Self Sign Up Mode
                 1. Configure the server for self sign up mode and restart it.
                 1. Try to create an account with an invalid user name and verify it does not succeed.
                 1. Try to create an account with an invalid password and verify it does not succeed.
                 1. Try to create an account with passwords that do not match and verify it does not succeed.
                 1. Create an account.
                 1. Verify that a note can be saved.
                 1. Sign out and verify signed out
                 1. Sign in.
                 1. Verify that a note can be saved.
                 1. Sign out and verify signed out.
                 1. Sign in.
                 1. Cancel the account and verify automatically signed out.
                 1. Try to sign in and verify failure.
             1. Auto account creation
                 1. Configure the server for auto account creation and restart it.
                 1. Create a note.
                 1. Verify signed in.
                 1. Go to the accounts page.
                 1. Try to change a field without setting a password and verify it fails.
                 1. Set a new password and change your user name.
                 1. Sign out
                 2. Sign in
             1. No Self Sign Up Mode
                 1. Configure the server for no self sign up mode and restart it.
                 1. Verify that the sign up link is not present.
                 1. During automation verify that an HTTP create account request is not honored.
                 1. Sign in.
                 1. Verify that a note can be saved.
                 1. Sign out and verify signed out.
    1. Pages
        1. Note
            1. Popup Windows
                1. Open one. Verify the popup is recentered.
                1. Scroll the document and open a new popup and verify it is centered within the view.
                1. Leave the popup open and resize the window to be smaller and bigger. Verify the popup is recentered dynamically.
                1. Resize the window to be smaller than the popup. Close and reopen the popup and verify it can all be seen. 
                1. Press Escape. Verify the popup was dismissed.
                1. Open a popup. Verify it can be dragged anywhere by any point where there is no text.
                1. Verify text can be quoted.
                1. Hover over the X. Verify it changes color and the pointer changes.
                1. Click dismissed. Verify the popup was dismissed.
            1. Note Selections
                1. Left or right click a note and verify it is selected.
                1. Ctrl click to add a note to the selection.
                1. Alt click to add a note to the selection.
                1. Ctrl click to remove a note from the selection.
                1. Alt click to remove a note from the selection.
                1. Shift click to add several notes to the selection.
                1. Select all by pressing 's'.
                1. Unselect all by pressing Escape.
                1. Unselect a note with Ctrl+space.
                1. Press up arrow to move selection left.
                1. Press right arrow to move selection right
                1. Press up arrow move selection up.
                1. Press down arrow move selection down.
                1. Press f to select first.
                1. Press l select last.
            1. Context Menu
                1. Right click a note. Verify the note is selected and the context menu appears.
                1. Click each button and verify the context menu disappears and the action takes place.
                1. Press the shortcut for each action and verify the context menu disappears and the action takes place.
            1. Error Popups
                1. Display an error popup and verify that it is dismissed by clicking outside the window.
                1. Display an error popup and verify that it is dismissed by pressing Enter.
            1. Drag and Drop a Note
                1. Verify that a note can not be dragged into its parent.
                1. Verify that a note can not be dragged into its child or grand children without Ctrl.
                1. Verify that a note can be dragged into its child or grand children with Ctrl.
                1. Verify that Ctrl drag drags only the note.
                1. Verify that Ctrl drag drags the note and its children.
                1. Verify lifting the mouse when not over a valid drop target does not result in a change.
            1. Commands
                1. Move a Note Up
                    1. Verify Ctrl up arrow moves a selected note up.
                    1. Verify Ctrl up arrow can not move a note without a previous sibling up.
                    1. Verify Ctrl up arrow can move contiguous notes up.
                    1. Verify Ctrl up arrow results in an error if multiple non contiguous notes are selected.
                1. Move a Note Down
                    1. Verify Ctrl down arrow moves a selected note down.
                    1. Verify Ctrl down arrow can not move a note without a next sibling down.
                    1. Verify Ctrl down arrow can move contiguous notes down.
                    1. Verify Ctrl down arrow results in an error if multiple non contiguous notes are selected.
                1. Move a Note Left
                    1. Verify Ctrl left arrow moves a selected note left.
                    1. Verify Ctrl left arrow can not move a note without a grandparent left.
                    1. Verify Ctrl left arrow can move contiguous notes left.
                    1. Verify Ctrl left arrow results in an error if multiple notes with different parents are selected.
                1. Move a Note Right
                    1. Verify Ctrl right arrow moves a selected note right.
                    1. Verify Ctrl right arrow can not move a note without a parent right.
                    1. Verify Ctrl right arrow can move contiguous notes right.
                    1. Verify Ctrl right arrow results in an error if multiple non contiguous notes are selected.
                1. Edit a Note
                    1. Verify the note text and time stamp change.
                    1. Verify is public changes.
                    1. Edit a higlight and make sure you can edit the quotation.
                1. Delete a Note
                    1. Verify a single note with no parent or child can be deleted and it no longer appears in the root note view.
                    1. Verify a note with children can be deleted with uproot and its children now appear in the root view.
                    1. Verify a note with children can be deleted with delete children and its children are deleted too.
                    1. Verify a note with children and a parent can be deleted with reparent children and its children are now children of the deleted notes parent.
                1. Uproot a Note
                    1. Verify the note now appears in the root view and is no longer a child of its parent.
                1. Create a New Child Note
                    1. Verify the new note is created and appears in the note view.
                    1. Leave the note empty and verify the error message.
                    1. Stop the server and verify the error message.
                1. Insert a New Note Above Another Note
                    1. Verify the new note is created and appears in the note view.
                1. Create a New Note After Another Note
                    1. Verify the new note is created and appears in the note view.
                1. Create a New Note Before Another Note
                    1. Verify the new note is created and appears in the note view.
                1. Create a New Note Underneath Another Note
                    1. Verify the new note is created and appears in the note view.
                1. Create a New Root Note
                    1. Verify the new note is created and appears in the root note view.
                1. Make a Note's Parent the Main Note
                    1. Press 'p' and verify it works.
                    1. Press the back button and verify it works.
                1. Make a Note the Main Note
                    1. Press 't' and verify it works.
                    1. Press the back button and verify it works.
                    1. Press the forward button and verify it works.
                1. Help
                    1. Show the help dialog and make sure it looks correct.
        1. Create Note
            1. Verify the new note is created and appears in the root note view.
            1. Leave the note empty and verify the error message.
            1. Stop the server and verify the error message.
        1. Root Notes
            1. Verify only and all root notes (except quotations) are displayed.
        1. All Notes
            1. Verify all notes (except quotations) are displayed.
        1. Quotations
            1. Verify all quotations are displayed.
        1. Documents
            1. Verify all documents are displayed.
	    1. Backup
	        1. Verify a verification popup is displayed.
	        1. Verify clicking cancel dismisses the popup and nothing happens.
	        1. Verify clicking OK results in a file download with a pretty filename.
	    1. Restore
	        1. Verify the error message if no file is specified. 
	        1. Verify restoration of a file works with preserve IDs checked.
	        1. Verify restoration of a file works with preserve IDs not checked.
        1. Documentation
            1. Verify the list of documentation is displayed.
            1. Click on one and verify it looks marked down.
		1. Admin Privileges
		     1. Single User Mode
		         1. Verify the user does not have admin privileges.
		     1. Not Single user mode and Loopback connections are admins
		         1. Verify a user connected over the loopback has admin privileges.
		         1. Verify a user not connected over the loopback does not have admin privileges.
		     1. Not Single User Mode and Not Loopback connections are admins
		         1. Verify that is not an admin and is connected over the loopback does not has admin privileges.
		         1. Verify that is not an admin and is not connected over the loopback does not has admin privileges.
		         1. Verify that is an admin and has admin privileges.
	    1. Accounts
	        1. Verify all accounts are listed.
	        1. Verify that clicking on an account takes you to the account page.
	        1. Verify that admins can change any attribute of the user's account.
	    1. Shutdown
	        1. Verify a verification popup is displayed.
	        1. Verify clicking cancel dismisses the popup and nothing happens.
	        1. Verify clicking OK results in the process being shutdown. 
	    1. Clear DB
	        1. Verify a verification popup is displayed.
	        1. Verify clicking cancel dismisses the popup and nothing happens.
	        1. Verify clicking OK results in the db being cleared. 
	    1. Online Backup
	        1. Verify a verification popup is displayed.
	        1. Verify clicking cancel dismisses the popup and nothing happens.
	        1. Verify clicking OK results in the db being backed up. 
	    1. Offline Backup
	        1. Verify a verification popup is displayed.
	        1. Verify clicking cancel dismisses the popup and nothing happens.
	        1. Verify clicking OK results in the db being backed up. 
	    1. Check DB Errors
	        1. Verify a verification popup is displayed.
	        1. Verify clicking cancel dismisses the popup and nothing happens.
	        1. Verify clicking OK results in verification. 
	    1. Show Backups
	        1. Verify a list of backups is shown. 
	        1. Click on a backup and verify the command to restore the backup is shown.
	        