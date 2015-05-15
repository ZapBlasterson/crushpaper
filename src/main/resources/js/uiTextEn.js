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

/* global mightHaveTouch */

var uiTextEn = {};

/* Capitalizes the first letter of a string. */
uiTextEn.capitalizeFirstLetter = function(value) {
	return value.charAt(0).toUpperCase() + value.slice(1);
};

uiTextEn.getPlural = function(entryType) {
	return entryType + "s";
};

uiTextEn.maybeGetPlural = function(number, entryType) {
	if (number === 1) {
		return entryType;
	}
	
	return uiTextEn.getPlural(entryType);
};

uiTextEn.areOrIs = function(number) {
	if (number === 1) {
		return "is";
	}
	
	return "are";
};

uiTextEn.theirOrIts = function(number) {
	if (number === 1) {
		return "its";
	}
	
	return "their";
};

uiTextEn.oneOrOneOf = function(number, entryType) {
	if (number === 1) {
		return "The " + uiTextEn.getOneEntryTerm(entryType);
	} else {
		return "One of the " + number + " selected " + uiTextEn.getOneEntryTermPlural(entryType);
	}
};

uiTextEn.maybeGetPluralWithNumber = function(number, entryType) {
	if (number === 1) {
		return entryType;
	}
	
	return number + " " + uiTextEn.getPlural(entryType);
};

uiTextEn.termWithNumber = function(number, entryType) {
	return number + " " + uiTextEn.maybeGetPlural(number, entryType);
};

uiTextEn.getAllEntryTerms = function(entryType) {
	if (entryType.constructor === Array) {
		var result = "";

		for (var i = 0; i < entryType.length; ++i) {
			if (result.length) {
				result += " or ";
			}

			result += entryType[i];
		}

		return result;
	}

	return entryType;
};

uiTextEn.getOneEntryTerm = function(entryType, capitalizeFirst, pluralize) {
	var result;
	if (entryType.constructor === Array) {
		if (entryType.length > 0) {
			result = entryType[0];
		}
	} else {
		result = entryType;
	}

	if (pluralize) {
		result = uiTextEn.getPlural(result);
	}

	if (capitalizeFirst) {
		return uiTextEn.capitalizeFirstLetter(result);
	}

	return result;
};

uiTextEn.getOneEntryTermCapFirst = function(entryType) {
	return uiTextEn.getOneEntryTerm(entryType, true);
};

uiTextEn.getOneEntryTermCapFirstAndPlural = function(entryType) {
	return uiTextEn.getOneEntryTerm(entryType, true, true);
};

uiTextEn.getOneEntryTermPlural = function(entryType) {
	return uiTextEn.getOneEntryTerm(entryType, false, true);
};

uiTextEn.popupTitleHelp = function() {
	return "Keyboard and Mouse Help" + (!mightHaveTouch() ? " (press 'h' to see this at any time)" : "");
};

uiTextEn.sentenceSaving = function() {
	return "Saving...";
};

uiTextEn.popupTitleCreateNotebook = function() {
	return "Create a New Notebook";
};

uiTextEn.popupTitleNewNoteUnderneath = function(entryType) {
	return "Put a New " + uiTextEn.getOneEntryTermCapFirst(entryType) + " Underneath the Selected " + uiTextEn.getOneEntryTermCapFirst(entryType) + " and Above its Sub" + uiTextEn.getOneEntryTermPlural(entryType);
};

uiTextEn.popupTitleNewNoteBefore = function(entryType) {
	return "Put a New " + uiTextEn.getOneEntryTermCapFirst(entryType) + " Before the Selected " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.errorOnlyOneNoteMayBeSelected = function(entryType) {
	return "Sorry, only one " + uiTextEn.getAllEntryTerms(entryType) + " may be selected.";
};

uiTextEn.errorMaySelectFromList = function(entryType) {
	var term = uiTextEn.getAllEntryTerms(entryType);
	return "Sorry, please select " + uiTextEn.aOrAn(term) + " " + term + " from a notebook for this action, not from a list.";
};

uiTextEn.errorAtLeastOneNoteMustBeSelected = function(entryType) {
	return "Sorry, at least one " + uiTextEn.getAllEntryTerms(entryType) + " must be selected.";
};

uiTextEn.errorBlankNoteAndQuotation = function() {
	return "Sorry, the note and quotation must not both be blank.";
};

uiTextEn.textYourNote = function() {
	return "Your note";
};

uiTextEn.textYourQuotation = function() {
	return "The quotation";
};

uiTextEn.popupTitleNewNoteAfter = function(entryType) {
	return "Put a New " + uiTextEn.getOneEntryTermCapFirst(entryType) + " After the Selected " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.popupTitleCreateChildNote = function(entryType) {
	return "Create a Sub" + uiTextEn.getOneEntryTerm(entryType) + " of the Selected " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.popupTitleInsertNote = function(entryType) {
	return "Insert a " + uiTextEn.getOneEntryTermCapFirst(entryType) + " as a Parent of the Selected " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.popupTitleMakeNotebook = function() {
	return "Make the Selected Note a Notebook";
};

uiTextEn.popupTitleMakeNotebooks = function() {
	return "Make the Selected Notes Notebooks";
};

uiTextEn.labelMoveToParent = function(entryType) {
	return "Make them sub" + uiTextEn.getOneEntryTermPlural(entryType) + " of the " + uiTextEn.getOneEntryTerm(entryType) + "'s <u>p</u>arent";
};

uiTextEn.labelIsItPublic = function() {
	return "<u>A</u>nyone can read this notebook";
};

uiTextEn.labelMakeFirstChild = function(entryType) {
	return "Make it the <u>f</u>irst sub" + uiTextEn.getOneEntryTerm(entryType);
};

uiTextEn.labelOrphan = function() {
	return "Turn them into notebooks";
};

uiTextEn.labelDelete = function() {
	return "<u>D</u>elete them";
};

uiTextEn.popupTitleDeleteTheSelectedNote = function(entryType) {
	return "Delete the Selected " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.popupTitleDeleteTheSelectedNotebook = function() {
	return "Delete the Selected Notebook";
};

uiTextEn.popupTitleDeleteTheSelectedSource = function() {
	return "Delete the Selected Source";
};

uiTextEn.popupTitleDeleteTheSelectedNotes = function(entryType) {
	return "Delete the Selected " + uiTextEn.getOneEntryTermCapFirstAndPlural(entryType);
};

uiTextEn.sentenceWhatToDoWithChildren = function(entryType) {
	return "What would you like to do with sub" + uiTextEn.getOneEntryTermPlural(entryType) + " of the " + uiTextEn.getOneEntryTerm(entryType) + " being deleted?";
};

uiTextEn.popupTitleEditTheSelectedNote = function(entryType) {
	return "Edit the Selected " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.popupTitleEditTheSelectedNotebook = function() {
	return "Edit the Selected Notebook";
};

uiTextEn.popupTitleEditTheSelectedSource = function() {
	return "Edit the Selected Source";
};

uiTextEn.buttonSave = function() {
	return "<u>S</u>ave";
};

uiTextEn.tooltipButtonSave = function() {
	return "Save this change. (Shortcut is Alt+s).";
};

uiTextEn.errorFirstCanNotBeMovedBefore = function(entryType) {
	return "Sorry, there is nothing to move the first " + uiTextEn.getOneEntryTerm(entryType) + " in a list before.";
};

uiTextEn.errorLastCanNotBeMovedAfter = function(entryType) {
	return "Sorry, there is nothing to move the last " + uiTextEn.getOneEntryTerm(entryType) + " in a list after.";
};

uiTextEn.errorEntryCanNotBeMovedLeftWithoutGrandParent = function(entryType) {
	return "Sorry, a " + uiTextEn.getOneEntryTerm(entryType) + " cannot be moved left unless it has a grandparent.";
};

uiTextEn.errorEntryCanNotBeMovedRightWithoutPrevious = function(entryType) {
	return "Sorry, a " + uiTextEn.getOneEntryTerm(entryType) + " cannot be moved right unless it has a previous " + uiTextEn.getOneEntryTerm(entryType) + ".";
};

uiTextEn.popupTitleMoveTheSelectedNote = function(direction, entryType) {
	var suffix = (direction === "before" || direction === "after") ? " Another" : "";
	return "Move the Selected " + uiTextEn.getOneEntryTermCapFirst(entryType) + " " + uiTextEn.capitalizeFirstLetter(direction) + suffix;
};

uiTextEn.errorNotSaved = function() {
	return "Sorry, this was not saved.";
};

uiTextEn.errorProbablyNotSaved = function() {
	return "Sorry, this was probably not saved.";
};

uiTextEn.errorSelectedMustHaveTheSameParent = function(entryType) {
	return "Sorry, selected " + uiTextEn.getOneEntryTermPlural(entryType) + " must have the same parent.";
};

uiTextEn.errorSelectedMustBeContiguous = function(entryType) {
	return "Sorry, selected " + uiTextEn.getOneEntryTermPlural(entryType) + " must be contiguous.";
};

uiTextEn.errorJsonMissingKey = function(prefix, key) {
	return prefix + " The server returned JSON missing the key \"" + key + "\".";
};

uiTextEn.errorJsonKeyIsNotArray = function(prefix, key) {
	return prefix + " The server returned JSON where the key \"" + key + "\" was not an array.";
};

uiTextEn.sentenceSuccessfullySaved = function() {
	return "Successfully saved.";
};

uiTextEn.popupTitleDragNodeToNewParent = function(entryType) {
	return "Drag the " + uiTextEn.getOneEntryTermCapFirstAndPlural(entryType) + " to be a Subnote of Another " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.popupTitleShowNotebookFor = function(entryType) {
	return "Show Notebook For " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.errorNoteNotFound = function(entryType) {
	return "Sorry, the " + uiTextEn.getOneEntryTerm(entryType) + " was not found.";
};

uiTextEn.errorNoteIsNotPartOfANotebook = function(entryType) {
	return "Sorry, the " + uiTextEn.getOneEntryTerm(entryType) + " is not part of a notebook.";
};

uiTextEn.errorParentNotFound = function(entryType) {
	return "Sorry, the " + uiTextEn.getOneEntryTerm(entryType) + "'s parent was not found.";
};

uiTextEn.popupTitleDragNodeToNewSibling = function(entryType, which) {
	return "Drag the " + uiTextEn.getOneEntryTermCapFirst(entryType) + " to be the " + uiTextEn.capitalizeFirstLetter(which) + " of Another " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.errorNotMoved = function(entryType) {
	return "Sorry, the " + uiTextEn.getOneEntryTerm(entryType) + " was not moved.";
};

uiTextEn.errorProbablyNotMoved = function(entryType) {
	return "Sorry, the " + uiTextEn.getOneEntryTerm(entryType) + " was probably not moved.";
};

uiTextEn.errorANoteMustBeSelected = function(entryType) {
	return "Sorry, a " + uiTextEn.getOneEntryTerm(entryType) + " must be selected.";
};

uiTextEn.popupTitleContextMenu = function() {
	return "Context Menu";
};

uiTextEn.popupTitleViewEntry = function(entryType) {
	return "View the Selected " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.popupTitleViewParentEntry = function(entryType) {
	return "View the Parent of the Selected " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.popupTitleViewChildrenEntry = function(entryType) {
	return "View the Sub" + uiTextEn.getOneEntryTermPlural(entryType) + " of the Selected " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.popupTitleHideChildrenEntry = function(entryType) {
	return "Hide the Sub" + uiTextEn.getOneEntryTermPlural(entryType) + " of the Selected " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.errorTheEntryNeedsAParent = function(entryType) {
	return "Sorry, the " + uiTextEn.getOneEntryTerm(entryType) + " needs a parent to do this.";
};

uiTextEn.sentenceNoteToDeleteAreNotVisible = function(notVisibleEntries, numToDelete, entryType) {
	return "Warning: " + notVisibleEntries + " of the " + numToDelete + " " + uiTextEn.getOneEntryTermPlural(entryType) +
	" to delete " + uiTextEn.areOrIs(notVisibleEntries) + " not visible where this window is scrolled.";
};

uiTextEn.sentenceNoteToMakeNotebooksAreNotVisible = function(notVisibleEntries, numToMakeNotebooks) {
	return "Warning: " + notVisibleEntries + " of the " + numToMakeNotebooks + " notes to make notebooks " + uiTextEn.areOrIs(notVisibleEntries) + " not visible where this window is scrolled.";
};

uiTextEn.sentenceNoteToDeleteHiddenChildren = function(numHiddenChildren, numToDelete, entryType) {
	if (numToDelete === 1) {
		return "Warning: The " + uiTextEn.getOneEntryTerm(entryType) + " to delete has a sub" + uiTextEn.getOneEntryTerm(entryType) + " that " + uiTextEn.areOrIs(numHiddenChildren) + " hidden.";
	}

	return "Warning: " + numHiddenChildren + " of the " + numToDelete + " " + uiTextEn.getOneEntryTermPlural(entryType) + " to delete have sub" + uiTextEn.getOneEntryTermPlural(entryType) + " that " + uiTextEn.areOrIs(numHiddenChildren) + " hidden.";
};

uiTextEn.sentenceNumNotesWillBeDeleted = function(numSelectedDbIds, entryType) {
	var singular = uiTextEn.getOneEntryTerm(entryType);
	var plural = uiTextEn.getOneEntryTermPlural(entryType);
	return numSelectedDbIds + " " + (numSelectedDbIds > 1 ? plural : singular) + " will be deleted.";
};

uiTextEn.sentenceNumNotesWillBeMadeNotebooks = function(numSelectedDbIds) {
	return numSelectedDbIds + " " + (numSelectedDbIds > 1 ? "notes" : "note") + " will be turned into a notebook.";
};

uiTextEn.errorThereAreNoMoreChildrenToHide = function(entryType) {
	return "Sorry, there are no more sub" + uiTextEn.getOneEntryTermPlural(entryType) + " to hide.";
};

uiTextEn.errorThereAreNoMoreChildrenToShow = function(entryType) {
	return "Sorry, there are no more sub" + uiTextEn.getOneEntryTermPlural(entryType) + " to show.";
};

uiTextEn.errorChildrenNeededToBeLoadedFirst = function(entryType) {
	return "Sorry, the sub" + uiTextEn.getOneEntryTermPlural(entryType) + " need to be loaded to do this but they couldn't be.";
};

uiTextEn.sentenceCtrlAndAltEnterToSubmit = function(entryType) {
	return "Press Enter to save this " + uiTextEn.getOneEntryTerm(entryType) + ". Press Ctrl+Enter or Alt+Enter to add a new line.";
};

uiTextEn.sentenceContextMenuHelp = function() {
	return "Each button's shortcut is shown to its right.<br>To view even more commands press the 'h' key.<br><br>";
};

uiTextEn.popupTitleOptions = function() {
	return "Options";
};

uiTextEn.labelShowTimestamps = function() {
	return "Show note timestamps when viewing a notebook.";
};

uiTextEn.labelSaveOnEnter = function() {
	return "Press the Enter key to save a note.";
};

uiTextEn.tooltipTriNoChildren = function(entryType) {
	return "If this " + uiTextEn.getOneEntryTerm(entryType) + " had sub" + uiTextEn.getOneEntryTermPlural(entryType) + " you could click this triangle to show or hide them.";
};

uiTextEn.tooltipTriShowChildren = function(entryType) {
	return "Click to show the direct sub" + uiTextEn.getOneEntryTermPlural(entryType) + " of this " + uiTextEn.getOneEntryTerm(entryType) + ".";
};

uiTextEn.tooltipTriHideChildren = function(entryType) {
	return "Click to hide the direct sub" + uiTextEn.getOneEntryTermPlural(entryType) + " of this " + uiTextEn.getOneEntryTerm(entryType) + " .";
};

uiTextEn.tooltipNewSubnote = function(entryType) {
	return "Click to create a sub" + uiTextEn.getOneEntryTerm(entryType) + " of this " + uiTextEn.getOneEntryTerm(entryType) + ". Shortcut key is c.";
};

uiTextEn.tooltipEdit = function(entryType) {
	return "Click to edit this " + uiTextEn.getOneEntryTerm(entryType) + ". Shortcut key is F2.";
};

uiTextEn.tooltipDelete = function(entryType) {
	return "Click to delete this " + uiTextEn.getOneEntryTerm(entryType) + " and any other selected " + uiTextEn.getOneEntryTermPlural(entryType) + ". Shortcut key is Delete";
};

uiTextEn.errorCouldNotGetPage = function() {
	return "Sorry, the page could not be retrieved.";
};

uiTextEn.popupTitleSignIn = function() {
	return "Sign In";
};

uiTextEn.popupTitleCreateAccount = function() {
	return "Create a New Account";
};

uiTextEn.buttonSignIn = function() {
	return "Sign In";
};

uiTextEn.buttonCreateAccount = function() {
	return "Create Account";
};

uiTextEn.sentenceChooseAUserName = function() {
	return "Choose a username (at least 3 characters from a-z and 0-9)";
};

uiTextEn.sentenceChooseAPassword = function() {
	return "Choose a password (at least 8 characters)";
};

uiTextEn.sentenceYourUserName = function() {
	return "Your username";
};

uiTextEn.sentenceYourPassword = function() {
	return "Your password";
};

uiTextEn.sentenceVerifyPassword = function() {
	return "Verify the password";
};

uiTextEn.sentenceEmailOptional = function() {
	return "Email address (optional)";
};

uiTextEn.sentenceMayBeContacted = function() {
	return "I may be contacted with information about this web site.";
};

uiTextEn.sentenceSigningIn = function() {
	return "Signing in...";
};

uiTextEn.sentenceCreatingAccount = function() {
	return "Creating account...";
};

uiTextEn.errorJsonCouldNotBeParsed = function(prefix) {
	return prefix + " The server returned JSON that could not be parsed.";
};

uiTextEn.errorJsonIsNotArray = function(prefix) {
	return prefix + " The server returned JSON that is not an array.";
};

uiTextEn.errorJsonArrayDoesNotHaveIndex = function(prefix, index) {
	return prefix + " The server returned a JSON array smaller than " + index + ".";
};

uiTextEn.errorEmptyResponseFromServer = function(prefix, statusCode) {
	if (statusCode === 0) {
		return prefix + " The request timed out.";
	}

	return prefix + " The server sent an empty response" + (statusCode !== "none" ? " with status code " + statusCode : "") + ".";
};

uiTextEn.sentenceAccountCreated = function() {
	return "Your account was created.";
};

uiTextEn.errorProbablyAccountNotCreated = function() {
	return "Sorry, your account was probably not created.";
};

uiTextEn.errorAccountNotCreated = function() {
	return "Sorry, your account was not created.";
};

uiTextEn.sentenceSignedIn = function() {
	return "You are now signed in.";
};

uiTextEn.errorProbablyNotSignedIn = function() {
	return "Sorry, you were probably not signed in.";
};

uiTextEn.errorNotSignedIn = function() {
	return "Sorry, you were not signed in.";
};

uiTextEn.popupTitleSignOut = function() {
	return "Sign Out";
};

uiTextEn.errorNotSignedOut = function() {
	return "Sorry, you were not signed out.";
};

uiTextEn.errorProbablyNotSignedOut = function() {
	return "Sorry, you were probably not signed out.";
};

uiTextEn.errorBlankNote = function() {
	return "Sorry, the note must not be blank.";
};

uiTextEn.errorBlankNotebookTitle = function() {
	return "Sorry, the notebook title must not be blank.";
};

uiTextEn.errorIe7Warning = function() {
	return "Sorry, this site does not work reliably in Internet Explorer 7 and below due to bugs in IE7.";
};

uiTextEn.titleRestoreBackupFor = function(filename) {
	return "Restore backup for " + filename;
};

uiTextEn.pageTitleHelpPage = function() {
	return "Help Page";
};

uiTextEn.pageTitleWelcome = function() {
	return "Welcome to CrushPaper";
};

uiTextEn.popupTitleAlreadySignedIn = function() {
	return "Already Signed In";
};

uiTextEn.sentenceSignInNotNeeded = function() {
	return "You are signed into CrushPaper but the Chrome extension thinks that you are not.<br><br>" +
	"Perhaps you configured CrushPaper with a different domain name, port or IP address than this web site.";
};

uiTextEn.errorInvalidResponse = function() {
	return "Sorry, the response was invalid.";
};

uiTextEn.popupTitleRefreshPage = function() {
	return "Refresh Page";
};

uiTextEn.labelYourNotebookTitle = function() {
	return "Your notebook title";
};

uiTextEn.labelTheSourcesTitle = function() {
	return "The source's title";
};

uiTextEn.tooltipClosePopup = function(isError) {
	return "Click to close this popup. (Shortcut is Escape" + (isError ? " or Enter" : "") + ").";
};

/** Returns the number a string that is at least two digits long prefixed with zero if necessary. */
uiTextEn.makeNumberAtLeastTwoDigits = function(number) {
	return (number < 10 ? "0" : "") + number;
};

/** Returns the string "AM" or "PM" as appropriate for the hours. */
uiTextEn.getAmOrPm = function(hours) {
	return hours >= 12 ? "PM" : "AM";
};

/** Returns the name of the month corresponding to its number. */
uiTextEn.getMonthName = function(number) {
	if (number === 0) {
		return "Jan";
	} else if (number === 1) {
		return "Feb";
	} else if (number === 2) {
		return "Mar";
	} else if (number === 3) {
		return "Apr";
	} else if (number === 4) {
		return "May";
	} else if (number === 5) {
		return "Jun";
	} else if (number === 6) {
		return "Jul";
	} else if (number === 7) {
		return "Aug";
	} else if (number === 8) {
		return "Sep";
	} else if (number === 9) {
		return "Oct";
	} else if (number === 10) {
		return "Nov";
	} else if (number === 11) {
		return "Dec";
	}
};

/** Returns the name of the day corresponding to its number. */
uiTextEn.getDayName = function(number) {
	if (number === 0) {
		return "Sun";
	} else if (number === 1) {
		return "Mon";
	} else if (number === 2) {
		return "Tue";
	} else if (number === 3) {
		return "Wed";
	} else if (number === 4) {
		return "Thu";
	} else if (number === 5) {
		return "Fri";
	} else if (number === 6) {
		return "Sat";
	}
};

/** Returns the hour as a number from 1-12. */
uiTextEn.hoursString = function(hours) {
	var result = hours % 12;
	if (result === 0) {
		result = 12;
	}

	return result;
};

/** Returns the milliseconds since the unix epoch as a more readable string. */
uiTextEn.formatDateTime = function(milliseconds) {
	var date = new Date(milliseconds);
	return uiTextEn.formatDateHelper(date) + " @ " + uiTextEn.formatTimeHelper(date);
};

/** Returns the milliseconds since the unix epoch as a more readable string. */
uiTextEn.formatDateHelper = function(date) {
	return uiTextEn.getDayName(date.getDay()) + ", " + uiTextEn.getMonthName(date.getMonth()) +
	" " + date.getDate() + ", " + date.getFullYear();
};

/** Returns the milliseconds since the Unix epoch as a more readable string. */
uiTextEn.formatTimeHelper = function(date) {
	return uiTextEn.hoursString(date.getHours()) + ":" +
	uiTextEn.makeNumberAtLeastTwoDigits(date.getMinutes()) + " " +
	uiTextEn.getAmOrPm(date.getHours());
};

uiTextEn.helpBasicCommands = function() {
	return "Basic Commands";
};

uiTextEn.helpCreateSubnote = function(entryType) {
	return "<u>C</u>reate a sub" + uiTextEn.getOneEntryTerm(entryType) + " of the selected " + uiTextEn.getOneEntryTerm(entryType);
};

uiTextEn.helpEdit = function(entryType) {
	return "Edit the selected " + uiTextEn.getOneEntryTerm(entryType);
};

uiTextEn.helpDelete = function(entryType) {
	return "Delete the selected " + uiTextEn.getOneEntryTerm(entryType);
};

uiTextEn.helpMouse = function() {
	return "Using the Mouse";
};


uiTextEn.helpMouseHints = function(entryType) {
	var hints = [
	             "Right click or Command click a " + uiTextEn.getOneEntryTerm(entryType) + " or press <u>m</u> to display a context menu",
	             "Drag a " + uiTextEn.getOneEntryTerm(entryType) + " to the left or right of another " + uiTextEn.getOneEntryTerm(entryType) + " to make it the next or previous of the other " + uiTextEn.getOneEntryTerm(entryType),
	             "Drag a " + uiTextEn.getOneEntryTerm(entryType) + " into another " + uiTextEn.getOneEntryTerm(entryType) + " to make it a sub" + uiTextEn.getOneEntryTerm(entryType) + " of the other " + uiTextEn.getOneEntryTerm(entryType),
	             "Hold down Ctrl or Alt to drag a " + uiTextEn.getOneEntryTerm(entryType) + "'s sub" + uiTextEn.getOneEntryTermPlural(entryType) + " with it",
	             "Click the + or - icons to show or hide a " + uiTextEn.getOneEntryTerm(entryType) + "'s sub" + uiTextEn.getOneEntryTermPlural(entryType),
	             "Click a " + uiTextEn.getOneEntryTerm(entryType) + " to select it or Ctrl or Alt click to unselect it",
	             "Ctrl or Alt click other " + uiTextEn.getOneEntryTermPlural(entryType) + " to add them to the selection",
	             "Shift click to add " + uiTextEn.getOneEntryTermPlural(entryType) + " between the clicked " + uiTextEn.getOneEntryTerm(entryType) + " and the last selected " + uiTextEn.getOneEntryTerm(entryType) + " to the selection",
	             "Press Escape when dragging to cancel the drag"
	             ];

	var results = [];
	for (var i = 0; i < hints.length; ++i) {
		results.push({ "text" : hints[i] });
	}

	return results;
};


uiTextEn.helpInlineEdit = function() {
	return "Inline Editing Commands";
};

uiTextEn.helpUndoInlineEdit = function() {
	return "Undo unsaved changes you have made to the note";
};

uiTextEn.helpSaveInlineEdit = function() {
	return "Save changes you have made to the note";
};


uiTextEn.helpAdvanced = function() {
	return "Advanced Commands";
};

uiTextEn.helpInsert = function(entryType) {
	return "<u>I</u>nsert a new " + uiTextEn.getOneEntryTerm(entryType) + " between the selected " + uiTextEn.getOneEntryTerm(entryType) + " and its parent";
};

uiTextEn.helpUnderneath = function(entryType) {
	return "Put a new " + uiTextEn.getOneEntryTerm(entryType) + " <u>u</u>nderneath the selected " + uiTextEn.getOneEntryTerm(entryType) + " and above its sub" + uiTextEn.getOneEntryTermPlural(entryType);
};

uiTextEn.helpBefore = function(entryType) {
	return "Put a new " + uiTextEn.getOneEntryTerm(entryType) + " <u>b</u>efore the selected " + uiTextEn.getOneEntryTerm(entryType);
};

uiTextEn.helpAfter = function(entryType) {
	return "Put a new " + uiTextEn.getOneEntryTerm(entryType) + " <u>a</u>fter the selected " + uiTextEn.getOneEntryTerm(entryType);
};


uiTextEn.helpMove = function(entryType) {
	return "How to Move a " + uiTextEn.getOneEntryTermCapFirst(entryType);
};

uiTextEn.helpMovePrevious = function(entryType) {
	return "Move the currently selected " + uiTextEn.getOneEntryTermPlural(entryType) + " before the previous " + uiTextEn.getOneEntryTerm(entryType);
};

uiTextEn.helpMoveAfter = function(entryType) {
	return "Move the currently selected " + uiTextEn.getOneEntryTermPlural(entryType) + " after the next " + uiTextEn.getOneEntryTerm(entryType);
};

uiTextEn.helpMoveLeft = function(entryType) {
	return "Move the currently selected " + uiTextEn.getOneEntryTermPlural(entryType) + " next to their parent";
};

uiTextEn.helpMoveRight = function(entryType) {
	return "Make the currently selected " + uiTextEn.getOneEntryTermPlural(entryType) + " sub" + uiTextEn.getOneEntryTermPlural(entryType) + " of the previous " + uiTextEn.getOneEntryTerm(entryType);
};

uiTextEn.helpMakeNotebook = function() {
	return "Make the selected note a notebook";
};


uiTextEn.helpShowOrHide = function(entryType) {
	return "How to Show or Hide " + uiTextEn.getOneEntryTermCapFirstAndPlural(entryType);
};

uiTextEn.helpShow = function(entryType) {
	return "Show the sub" + uiTextEn.getOneEntryTermPlural(entryType) + " of the selected " + uiTextEn.getOneEntryTerm(entryType);
};

uiTextEn.helpHide = function(entryType) {
	return "Hide the sub" + uiTextEn.getOneEntryTermPlural(entryType) + " of the selected " + uiTextEn.getOneEntryTerm(entryType);
};

uiTextEn.helpShowParent = function(entryType) {
	return "Show and select the currently selected " + uiTextEn.getOneEntryTerm(entryType) + "'s <u>p</u>arent";
};

uiTextEn.helpMakeMain = function(entryType) {
	return "Make <u>t</u>he selected " + uiTextEn.getOneEntryTerm(entryType) + " the main " + uiTextEn.getOneEntryTerm(entryType) + " of this window";
};


uiTextEn.helpSelect = function(entryType) {
	return "How to Select " + uiTextEn.getOneEntryTermCapFirstAndPlural(entryType);
};

uiTextEn.helpSelectAll = function(entryType) {
	return "<u>S</u>elect all " + uiTextEn.getOneEntryTermPlural(entryType) + " on the page";
};

uiTextEn.helpUnselectAll = function(entryType) {
	return "Unselect all currently selected " + uiTextEn.getOneEntryTermPlural(entryType);
};

uiTextEn.helpUnselectAllOrDismiss = function(entryType) {
	return "Unselect all currently selected " + uiTextEn.getOneEntryTermPlural(entryType) + " or dismiss a popup";
};

uiTextEn.helpSelectAbove = function(entryType) {
	return "Select the " + uiTextEn.getOneEntryTerm(entryType) + " above the currently selected " + uiTextEn.getOneEntryTerm(entryType) + ", or the last " + uiTextEn.getOneEntryTerm(entryType) + " on the page";
};

uiTextEn.helpSelectBelow = function(entryType) {
	return "Select the " + uiTextEn.getOneEntryTerm(entryType) + " below the currently selected " + uiTextEn.getOneEntryTerm(entryType) + ", or the first " + uiTextEn.getOneEntryTerm(entryType) + " on the page";
};

uiTextEn.helpSelectLeft = function(entryType) {
	return "Select the parent of the currently selected " + uiTextEn.getOneEntryTerm(entryType);
};

uiTextEn.helpSelectRight = function(entryType) {
	return "Select the first sub" + uiTextEn.getOneEntryTerm(entryType) + " of the currently selected " + uiTextEn.getOneEntryTerm(entryType);
};


uiTextEn.helpHelp = function() {
	return "Help";
};

uiTextEn.helpHelpHelp = function() {
	return "Show this help dialog";
};


uiTextEn.dragHintWhatIsBeingDragged = function(entryType, number, justTheEntry, hasChildren) {
	return "You are dragging " + uiTextEn.termWithNumber(number, entryType) +
		(hasChildren ? " " + (justTheEntry ? "without" : "with") + " " + uiTextEn.theirOrIts(number) + " sub" + uiTextEn.getPlural(entryType) : "") + ".";
};

uiTextEn.dragHintCanDropHere = function(entryType, number, justTheEntry, hasChildren) {
	return "You can drop the " + uiTextEn.maybeGetPluralWithNumber(number, entryType) + 
		(hasChildren ? " " + (justTheEntry ? "without" : "with") + " " + uiTextEn.theirOrIts(number) + " sub" + uiTextEn.getPlural(entryType) : "") + " here.";
};

uiTextEn.dragHintCanNotDropIntoSelected = function(entryType, number) {
	return "The " + uiTextEn.maybeGetPluralWithNumber(number, entryType) + " cannot be dragged into a " + uiTextEn.getOneEntryTerm(entryType) + " that is also being dragged.";
};

uiTextEn.dragHintCanNotDropNotebookIntoNote = function() {
	return "The notebook cannot be dragged into a note. It can only be dragged within the notebook pane.";
};

uiTextEn.dragHintCanNotDropNoteIntoNotebook = function(entryType) {
	return "The " + uiTextEn.getOneEntryTerm(entryType) + " cannot be dragged into the notebook pane. It can only be dragged into other notes.";
};

uiTextEn.dragHintCanNotDropNotebookIntoThis = function() {
	return "The notebook cannot be dragged into a list. It can only be dragged within the notebook pane.";
};

uiTextEn.dragHintCanNotDropNoteIntoThis = function(entryType) {
	return "The " + uiTextEn.getOneEntryTerm(entryType) + " cannot be dragged into a list. It can only be dragged into other notes.";
};

uiTextEn.dragHintCanNotDropIntoParent = function(entryType, number) {
	return uiTextEn.oneOrOneOf(number, entryType) + " cannot be dragged into its parent.";
};

uiTextEn.dragHintCanNotDropIntoSelf = function(entryType, number) {
	return uiTextEn.oneOrOneOf(number, entryType) + " cannot be dragged into itself.";
};

uiTextEn.dragHintCanNotDropIntoNonEditablePane = function(entryType, number) {
	return "The " + uiTextEn.maybeGetPluralWithNumber(number, entryType) + " cannot be dragged into an uneditable pane. Click the pencil icon first.";
};

uiTextEn.dragHintCanNotDropIntoList = function(entryType, number) {
	return "The " + uiTextEn.maybeGetPluralWithNumber(number, entryType) + " cannot be dragged into a list. It can only be dragged into a notebook.";
};

uiTextEn.dragHintCanNotDropIntoSub = function(entryType, number) {
	return uiTextEn.oneOrOneOf(number, entryType) + " cannot be dragged into its sub" + uiTextEn.getOneEntryTerm(entryType) + " while Ctrl is pressed.";
};

uiTextEn.dragHintCanNotDropNextToItself = function(entryType, number) {
	return uiTextEn.oneOrOneOf(number, entryType) + " cannot be dragged next to itself.";
};

uiTextEn.dragHintCanNotDropNextToSomethingThatHasNoParent = function(entryType, number) {
	return "The " + uiTextEn.maybeGetPluralWithNumber(number, entryType) + " cannot be dragged next to a " + uiTextEn.getOneEntryTerm(entryType) + " that has no parent.";
};

uiTextEn.dragHintCanNotDropNextToItsOwnChild = function(entryType, number) {
	return uiTextEn.oneOrOneOf(number, entryType) + " cannot be dragged next to its own sub" + uiTextEn.getOneEntryTerm(entryType) + ".";
};

uiTextEn.tooltipOpen = function(entryType) {
	return "Click to view this " + uiTextEn.getOneEntryTerm(entryType) + ".";
};

uiTextEn.tooltipDrag = function(entryType) {
	return "Hold down to drag this " + uiTextEn.getOneEntryTerm(entryType) + ". Press Escape to stop dragging.";
};

uiTextEn.editOrViewTooltip = function(isEditable) {
	if (!isEditable) {
		return " Click to view in another pane.";
	}

	return " Right click, Command click or press h to see what you can do with it.";
};

uiTextEn.aOrAn = function(entryType) {
	return entryType.charAt(0) === 'a' ? "an" : "a";
};

uiTextEn.aloneElTooltip = function(entryType, isEditable) {
	var type = uiTextEn.getOneEntryTerm(entryType);
	return "This is " + uiTextEn.aOrAn(type) + " " + type + "." + uiTextEn.editOrViewTooltip(isEditable);
};

uiTextEn.checkboxTooltip = function(entryType) {
	var type = uiTextEn.getOneEntryTerm(entryType);
	return "If this checkbox is checked, this " + type + " will be dragged or deleted if you click a blue button to the right.";
};

uiTextEn.noteTooltip = function(entryType, isEditable) {
	var type = uiTextEn.getOneEntryTerm(entryType);
	return "This is the text of the " + type + (type === "notebook" ? " title" : "") + "." + uiTextEn.editOrViewTooltip(isEditable);
};

uiTextEn.quotationTooltip = function(entryType, isEditable) {
	return "This is the text of the quotation" + uiTextEn.editOrViewTooltip(isEditable);
};

uiTextEn.modTimeTooltip = function(entryType, isEditable) {
	return "This is the time the " + uiTextEn.getOneEntryTerm(entryType) + " was last modified." + uiTextEn.editOrViewTooltip(isEditable);
};

uiTextEn.sentenceSuggestShortNotes = function() {
	return "I suggest that you write short notes.<br>Make each list item a separate subnote by clicking the big white plus button.";
};

uiTextEn.tooltipPaneEdit = function(entryType) {
	if (entryType === "notebooks") {
		return "Click to enable or disable editing within this hierarchy of notebooks.";
	}

	return "Click to edit the title of this " + entryType + ".";
};

uiTextEn.tooltipPaneDelete = function(entryType) {
	return "Click to delete this " + entryType + ".";
};

uiTextEn.sentenceHowSourceAndQuotationDeletesWork = function(numSourcesFromList, numQuotationsFromList) {
	var result = "Please note that deleting ";
	
	if (numSourcesFromList) {
		result += "sources";
	}
	
	if (numSourcesFromList && numQuotationsFromList) {
		result += " and ";
	}

	if (numQuotationsFromList) {
		result += "quotations";
	}
	
	result += " also removes them from any notebook they are in.";
		
	return result;
};

uiTextEn.labelOnlyUnlinkSourcesAndQuotations = function(numSourcesFromNotebook, numQuotationsFromNotebook) {
	var result = "Do not delete ";
	
	if (numSourcesFromNotebook) {
		result += numSourcesFromNotebook + " " + uiTextEn.maybeGetPlural(numSourcesFromNotebook, "source");
	}
	
	if (numSourcesFromNotebook && numQuotationsFromNotebook) {
		result += " and ";
	}

	if (numQuotationsFromNotebook) {
		result += numQuotationsFromNotebook + " " + uiTextEn.maybeGetPlural(numQuotationsFromNotebook, "quotation");
	}
	
	result += " that " + uiTextEn.areOrIs(numSourcesFromNotebook + numQuotationsFromNotebook) + " part of a notebook. Only remove it from the notebook.";
		
	return result;
};

uiTextEn.errorNoteMustNotBeEmpty = function() {
	return "Sorry, the note must not be blank.";
};