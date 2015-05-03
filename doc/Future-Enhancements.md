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

### Additional Browser Extensions
1. Create an IE quoting extension.
1. Create an Opera quoting extension.
1. Create a Safari quoting extension.
1. Create a Firefox quoting extension.

### User Visible Features
1. Make much more mobile friendly. 
1. Support undo and redo.
1. Support browsing of edit history.
1. Support placing images into notes and quotations.
1. Support dragging images into notes and quotations.
1. Support dragging text into notes and quotations.
1. Support dragging video into notes and quotations.
1. Support subscribing to the server for real-time notification of changes.
1. Support extraction of trees of notes for publication as a document.
1. Support styling notes with additional data and actions for chapters, questions, answers, feedback and todos.
1. Enable commenting and voting on notes.
1. Support notification to other users with embedded @ in note.
1. Support categorization with embedded @#tags in notes.
1. When quoting with a browser extension automatically convert DOM in quoted text to markdown.
1. Support creating quotations from PDFs.
1. Add the capability to favorite notes.
1. Support quoting on web sites that disable copy and paste.
1. Add the concept of domains of research.
1. Show quotations in the context of the quoted source.
1. Spell checking of the text of notes.
1. Track the number of views each quotation and note receives.
1. Enable the user to see the list of notes they viewed most recently.
1. Enable the user to specify if a note is publicly readable/editable.
1. Enable the user to see the most popular public notes.
1. Enable the user to see the most popular of their public notes.
1. Enable the user to search all public notes.
1. Enable the user to search all public notes from another person.
1. Enable the user to specify if a note is readable/editable by a group of people or individuals.
1. Enable the user to specify if a note is publicly editable or other users can create relationships to it.
1. Add the capability to annotate the author and creation times of sources.
1. Add the capability to annotate the author and creation times of quotations (for the case where a source is by multiple authors). 
1. Importing and exporting in more formats, perhaps: nested JSON (currently the JSON format is flat), XML, CSV and/or <a href="http://dev.opml.org/spec2.html">OPML</a>.
1. Searching within a subtree.
1. Support creating quotations and quotations through the web site rather than through a browser extension.
1. Support moving quotations to another source.
1. Support copying, cutting and pasting of entries.
1. Support icons and shortcuts for making text bold or italic.
1. Search underneath any notebook or note.
1. Add the capability to specify the parent of quotations through the chrome extension.
1. Add the capability to create sources rather than just through the Chrome extension.
1. Create a split siblings command that adds a new parent that is the next sibling of the parent.
1. Create a split siblings command that makes all next siblings children of the note
1. Create a command that moves a note to be be first or last child of its parent.
1. Support searching for a parent note of a new note in the create a new note popup.
1. Support moving notes to be children of a parent by searching for the parent.
1. Prevent creating quotations of the CrushPaper web site. These links and titles can be very confusing.
1. Support Ctrl+arrow moving of non contiguous notes.
1. Support SEO friendly URLs and HTML titles for notes.
1. Support cloning of public notebooks. If a visitor sees one they like they should be able to clone or fork it. 
1. When public notebooks are requested by web crawlers the full depth should be returned.
1. Web crawlers should be blocked from requesting individual notes rather than notebooks. 
1. Validate all information and relations in an import before making any database changes.
1. Support viewing and editing different sections of a notebook in different panes at the same time.

### Increased reliability
1. Persist failed requests locally in the web browser's storage for later retry.
1. Record all write operations in a transaction log and assign IDs.

### Additional Testing
1. Creation of HTTP interface tests.
1. Creation of pixel perfect browser tests.
1. Create a testing tool that can rerun the transaction log.

### Extensibility
1. Make all functionality accessible through the JSON HTTP API.

### Administration
1. Ability to delete an account and all related contented (currently accounts may only be closed to prevent further sign in).
1. User metrics: users that have the most entries, users that have made the most changes.
1. Current usage information: Users who are currently signed in. Users who have most recently created or modified data.

