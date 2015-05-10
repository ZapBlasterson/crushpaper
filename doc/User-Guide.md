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
### Notes and Quotations

#### What are Notes and Quotations?

1. A note is a piece of text that is important to you.
1. You can organize your notes by putting them in a hierarchy. 
1. A quotation a piece of text that came from a source with an optional note.
1. A source is an external web page. 
1. Quotations are just like notes. They can be organized within the note hierarchy.

#### What can you do with Notes and Quotations?

1. They can be subnotes or parents of any other note.
1. CrushPaper tries to make it very easy to do this by enabling you to:
    1. Select one or more notes with Ctrl and Shift click.
    1. Move one or more notes by dragging them with the mouse.
    1. A note can be dragged by itself, or with its subnotes by holding down the Ctrl key.
    1. Move one or more notes using the Ctrl and arrow keys.
    1. Select notes using only the keyboard.
    1. Operate on notes using keyboard shortcuts or using a context menu that is displayed by right clicking or Command clicking a note.
    1. If you forget the keyboard shortcuts you can press the 'h' key to see a popup that explains them.
1. They can be deleted. When you delete a note you can choose to delete the note's subnotes as well, or make them subnotes of the deleted note's parent.
<!-- 1. You can choose whether just you or anyone can read a note. -->      
1. The text of the note or quotation can be changed.

#### Searching and Browsing

1. See more on search <a onclick="newPaneForLink(event, 'Search Help', 'help'); return false;" href="/help/Search-Help">here</a>.

#### Exporting Notes, Quotations and Sources

1. You can export all of your notes, quotations and sources in a JSON file.
1. You may do this if you want to move your information from one CrushPaper server to another.
1. You may also want to create backups of your information. 

#### Importing Notes, Quotations and Sources

1. You can import notes, quotations and sources by uploading a JSON file to the server. (JSON is a text format that is easy to manipulate.)
1. If you select "Reuse note IDs if possible" then the CrushPaper server will try to recreate your notes, quotations and sources with the same IDs as in the file. This is useful if you want to be able to retain the same URLs. If another item already has the ID then the CrushPaper server will assign a new ID.
1. If you select "The file is in MS Word List Format" then the CrushPaper server will treat the file as if it contains a single notebook in this format:
<pre>
*\tNote at level 1<br>
O\tNote at level 2<br>
?\tNote at level x<br>
</pre>
CrushPaper assumes that if the bullet character that appears at the beginning of the line has not been seen before then the line should be at a level below any other levels. 

### Account Information

#### Username
1. Usernames must be between 3 and 20 characters in length.
1. Usernames can only contain numbers and lowercase letters in ASCII.
1. If you create a notebook without creating an account then an account is manually created for you with a random username. You can change this username later.

#### Password
1. Passwords must be between 8 and 20 characters in length.
1. Passwords can contain any printable ASCII character.
1. A user can change their password at any time.
1. If a user's account was created automatically by creating a notebook then the account may not have a password.
1. If the account does not have a password one must be set before any account information can be changed.  

#### Email Address
1. Email addresses are optional.
1. Email addresses must be less than 101 characters in length.
1. Email addresses can contain most printable ASCII characters.
2. A user can change their email address at any time.

#### I may be contacted with information about this web site
1. If the user has clicked this checkbox then the owner of the site may email them with useful information about the site.

#### The account is closed
1. If the account has been closed the user may not sign in.
