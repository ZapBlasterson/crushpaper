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

/**
 * This is the full list of strings that can be returned by the HTTP server.
 * This serves to isolate all messages into one place where they can be reviewed
 * for consistency. This should also make it easier to internationalize the
 * application if desired. buttons, labels and links should not have periods.
 * sentences and errors should be full sentences with periods. fragments should
 * be portions of sentences and should generally not have periods.
 */
public class ServletText {
	public String buttonImport() {
		return "Import";
	}

	public String buttonSave() {
		return "Save";
	}

	public String errorEntryCouldNotBeFound() {
		return "Sorry, the specified note could not be found.";
	}

	public String errorNotebookCouldNotBeFound() {
		return "Sorry, the specified notebook could not be found.";
	}

	public String errorNotebooksCouldNotBeFound() {
		return "Sorry, your notebooks could not be found.";
	}

	public String errorMovedCouldNotBeFound() {
		return "Sorry, the specified note to move could not be found.";
	}

	public String errorNoSourceFound() {
		return "Sorry, no such source was found.";
	}

	public String errorParentCouldNotBeFound() {
		return "Sorry, the specified parent could not be found.";
	}

	public String errorChildCouldNotBeFound() {
		return "Sorry, the specified subnote could not be found.";
	}

	public String errorCouldNotParseQuery() {
		return "Sorry, your query could not be parsed.";
	}

	public String errorInternalDatabase() {
		return "Sorry, there was an internal database error.";
	}

	public String errorHasNoParent() {
		return "Sorry, this note has no parent.";
	}

	public String errorParentIdInvalidFormat() {
		return "Sorry, the specified parent ID was in an invalid format.";
	}

	public String errorMovedIdInvalidFormat() {
		return "Sorry, the ID of the note to move was in an invalid format.";
	}

	public String errorChildIdInvalidFormat() {
		return "Sorry, the specified subnote ID was in an invalid format.";
	}

	public String errorIdIsInvalidFormat() {
		return "Sorry, the specified ID was in an invalid format.";
	}

	public String errorIdsAreInvalidFormat() {
		return "Sorry, the specified IDs are in an invalid format.";
	}

	public String errorInvalidOperation() {
		return "Sorry, the operation that was specified was not understood.";
	}

	public String errorInvalidPlacementValue() {
		return "Sorry, the placement that was specified was not understood.";
	}

	public String errorInvalidTimestamp() {
		return "Sorry, the specified timestamp was in an invalid format.";
	}

	public String errorJson() {
		return "Sorry, the JSON could not be parsed.";
	}

	public String errorMayNotSeeSource() {
		return "Sorry, you are not entitled to view this source.";
	}

	public String errorMayNotSeeEntry() {
		return "Sorry, you are not entitled to view this note.";
	}

	public String errorMayNotSeeNotebook() {
		return "Sorry, you are not entitled to view this notebook.";
	}

	public String errorMayNotSeeNotebooks() {
		return "Sorry, you are not entitled to view these notebooks.";
	}

	public String errorMissingOperation() {
		return "Sorry, no operation was specified.";
	}

	public String errorNoFileUploaded() {
		return "Sorry, no file was uploaded so it cannot be imported.";
	}

	public String errorNoteNotCreated() {
		return "Sorry, your note could not be created.";
	}

	public String errorPageNotFound() {
		return "Sorry, the page you were looking for doesn't exist.";
	}

	public String errorImportFailed() {
		return "Sorry, your import failed.";
	}

	public String fragmentBlankTitle() {
		return "Blank title";
	}

	public String fragmentBlankNote() {
		return "Blank note";
	}

	public String fragmentBlankQuotation() {
		return "Blank quotation";
	}

	public String sentenceCmdForRestore() {
		return "To restore the backup first shutdown the server and then run these commands:";
	}

	public String fragmentShowingContentsOf() {
		return "Showing contents of";
	}

	public String labelApplicationName() {
		return "CrushPaper";
	}

	public String pageTitleQuotation() {
		return "Quotation";
	}

	public String pageTitleNotebook() {
		return "Notebook";
	}

	public String pageTitleAllNotes() {
		return "All Notes";
	}

	public String pageTitleOfflineBackupDb() {
		return "Offline Backup";
	}

	public String pageTitleOnlineBackupDb() {
		return "Online Backup";
	}

	public String pageTitleNewNotebook() {
		return "New Notebook";
	}

	public String pageTitleSource() {
		return "Source";
	}

	public String pageTitleSources() {
		return "Sources";
	}

	public String pageTitleExport() {
		return "Export";
	}

	public String pageTitleQuotations() {
		return "Quotations";
	}

	public String pageTitleImport() {
		return "Import";
	}

	public String pageTitleShowBackups() {
		return "Backups";
	}

	public String pageTitleNotebooks() {
		return "Notebooks";
	}

	public String pageTitleEditNote() {
		return "Edit Note";
	}

	public String pageTitleEditQuotation() {
		return "Edit Quotation";
	}

	public String pageTitleCheckDbForErrors() {
		return "Check Errors";
	}

	public String sentenceImported() {
		return "Your file has been successfully imported.";
	}

	public String sentenceNoSourcesExist() {
		return "No sources have been created yet.<br><br>A source is created when you create a quotation from a web page.<br><br>"
				+ sentenceChromeExtension();
	}

	public String sentenceNoQuotationsExist() {
		return "No quotations have been created yet.<br><br>"
				+ sentenceChromeExtension();
	}

	private String sentenceChromeExtension() {
		return "If you have the CrushPaper Chrome extension installed then create a quotation:<br><br>"
				+ "1. Go to another web site.<br>"
				+ "2. Select any text on that web site.<br>"
				+ "3. Right click the text.<br>"
				+ "4. Click \"Save selected text to CrushPaper\".<br>"
				+ "5. Come back to this page and click the refresh icon. "
				+ "<div title=\"Click to refresh this part of the page.\" class=\"refreshIcon\" onclick=\"refreshPane(event); return false;\"></div>";
	}

	public String sentenceNoQuotationsForThisSourceExist() {
		return "No quotations have been created for this source yet.";
	}

	public String sentenceNoNotesExist() {
		return "No notebooks have been created yet.<br><br>You can click on \"New Notebook\" on the top of the left menu to create one.";
	}

	public String sentenceNothingHere() {
		return "There is nothing here. This page assists in debugging.";
	}

	public String sentencePressHForHelp() {
		return "Press the \"h\" key for <u>h</u>elp.";
	}

	public String sentenceTheDatabaseHasNoErrors() {
		return "The database has no errors.";
	}

	public String sentenceTheDatabaseHasErrors() {
		return "The database has errors.";
	}

	public String sentenceThereWereNoMatches() {
		return "There were no matches.";
	}

	public String textNoBackupsHaveBeenCreated() {
		return "No backups have been created.";
	}

	public String labelApplicationNameTooltip() {
		return "Go to the CrushPaper welcome page.";
	}

	public String labelCrushPaperComTooltip() {
		return "Go to crushpaper.com.";
	}

	public String labelTwitterTooltip() {
		return "Go to CrushPaper's Twitter account.";
	}

	public String labelGithubTooltip() {
		return "Go to CrushPaper's GitHub repo.";
	}

	public String labelGoogleGroupTooltip() {
		return "Go to CrushPaper's Google Group.";
	}

	public String pageTitleCreateNoteTooltip() {
		return "Create a new notebook.";
	}

	public String pageTitleNotebooksTooltip() {
		return "View all your notebooks.";
	}

	public String pageTitleAllNotesTooltip() {
		return "View all your notes.";
	}

	public String pageTitleQuotationsTooltip() {
		return "View all your quotations.";
	}

	public String pageTitleSourcesTooltip() {
		return "View all your sources.";
	}

	public String pageTitleExportTooltip() {
		return "Export all your notebooks as a JSON file.";
	}

	public String pageTitleImportTooltip() {
		return "Import notebooks from a JSON file.";
	}

	public String pageTitleOfflineBackupDbTooltip() {
		return "Create an offline backup of the database on the server. Changes made during this process will corrupt the backup.";
	}

	public String pageTitleOnlineBackupDbTooltip() {
		return "Create an online backup of the database on the server. Changes made during this process will NOT corrupt the backup.";
	}

	public String pageTitleCheckDbForErrorsTooltip() {
		return "Check the database for integrity errors.";
	}

	public String pageTitleShowBackupsTooltip() {
		return "Show all available backup copies.";
	}

	public String printLinkTooltip() {
		return "View a more printable version of the current page.";
	}

	public String clickToEditAlt() {
		return "Click to edit";
	}

	public String clickToEditTheQuotationTooltip() {
		return "Click to edit the quotation.";
	}

	public String clickToEditTheNoteTooltip() {
		return "Click to edit the note.";
	}

	public String clickToEditTheSourceTooltip() {
		return "Click to edit the source.";
	}

	public String showExternalSourceLinkTooltip() {
		return "View the external web page for the source.";
	}

	public String dragEntryAlt() {
		return "Drag";
	}

	public String dragEntryTooltip() {
		return "Drag this note into or next to another note by dragging this image.";
	}

	public String quotationInListTooltip() {
		return "This is the text of the quotation. Click to edit.";
	}

	public String noteInListTooltip(String type) {
		return "This is the text of the " + type
				+ (type.equals("notebook") ? " title" : "")
				+ ". Click to edit.";
	}

	public String modTimeInListTooltip(String type) {
		return "This is the time the " + type
				+ " was last modified. Click to edit.";
	}

	public String showDomainTooltip() {
		return "This is the domain of the source.";
	}

	public String errorPageNotAllowed() {
		return "Sorry, you are not allowed to view this page.";
	}

	public String notAllowedTitle() {
		return "Not Viewable";
	}

	public String pageTitleClearDbTooltip() {
		return "Clear the entire database.";
	}

	public String pageTitleClearDb() {
		return "Clear DB";
	}

	public String sentenceCleared() {
		return "The database was cleared of all data.";
	}

	public String welcome() {
		return "Welcome";
	}

	public String fragmentFrom() {
		return "From";
	}

	public String moreFromThisSourceTooltip() {
		return "Show more quotations from this source.";
	}

	public String buttonMoreQuotations() {
		return "More quotations from this source";
	}

	public String fragmentLastModified() {
		return "Last modified";
	}

	public String offlineBackupDbAreYouSure() {
		return "Backing up the database is time consuming.<br>There is almost never a reason to do an offline backup instead of an online backup.";
	}

	public String onlineBackupDbAreYouSure() {
		return "Backing up the database is time consuming.";
	}

	public String clearAreYouSure() {
		return "Clearing removes all data from the database, not just your own.";
	}

	public String checkDbForErrorsAreYouSure() {
		return "Checking the database for errors is time consuming.<br><br>It may lock the database for a long time.";
	}

	public String exportAreYouSure() {
		return "Exporting your notebooks is time consuming.<br><br>It may lock the database for a long time.";
	}

	public String sentenceOfflineBackupWasSuccessful() {
		return "The offline backup was successful.";
	}

	public String sentenceOfflineBackupWasNotSuccessful() {
		return "<span class=\"errorMessage\">The offline backup was not successful.</span>";
	}

	public String sentenceOnlineBackupWasSuccessful(int numRowsExtracted) {
		return "The online backup was successful.<br><br>" + numRowsExtracted
				+ " rows were backed up.";
	}

	public String sentenceOnlineBackupWasNotSuccessful() {
		return "<span class=\"errorMessage\">The online backup was not successful.</span>";
	}

	public String linkShowRestoreBackupCmdTooltip() {
		return "Click to show the command that will restore this backup.";
	}

	public String errorNoNameSpecifiedForRestoration() {
		return "The backup that you would like to restore was not specified.";
	}

	public String pageTitleRestoreBackupCommandDb() {
		return "Command to Restore a Backup";
	}

	public String sentenceToRestoreCommand() {
		return "Click one of the links below to view the command that will restore that backup.";
	}

	public String pageTitleClearedDb() {
		return "Entire Database Cleared";
	}

	public String pageTitleImported() {
		return "Notes Imported";
	}

	public String linkNext() {
		return "Next Page";
	}

	public String linkPrevious() {
		return "Previous Page";
	}

	public String sentenceNoMoreResults() {
		return "Sorry, there were no more results.";
	}

	public String fragmentCreated() {
		return "Created";
	}

	public String linkSignIn() {
		return "Sign In";
	}

	public String linkAccount() {
		return "Account";
	}

	public String linkCreateAccount() {
		return "Create Account";
	}

	public String tooltipSignIn() {
		return "Click here to sign into your account.";
	}

	public String tooltipCreateAccount() {
		return "Click here to create an account.";
	}

	public String tooltipEditAccount() {
		return "Click here to edit your account details.";
	}

	public String linkSignOut() {
		return "Sign Out";
	}

	public String tooltipSignOut() {
		return "Click here to sign out of your account.";
	}

	public String errorUsernameMustNotBeBlank() {
		return "Sorry, the username must not be blank.";
	}

	public String errorPasswordMustNotBeBlank() {
		return "Sorry, the password must not be blank.";
	}

	public String errorPasswordsMustMatch() {
		return "Sorry, the passwords must match.";
	}

	public String errorUserNameIsNotValid() {
		return "Sorry, the username is not valid. It must be between 3 and 20 letters long and only contain the characters a-z and 0-9.";
	}

	public String errorPasswordIsNotValid() {
		return "Sorry, the password is not valid. It must be between 8 and 20 letters long.";
	}

	public String errorUserNameIsAlreadyTaken() {
		return "Sorry, the username is already taken.";
	}

	public String errorCouldNotCreateAccount() {
		return "Sorry, the account could not be created.";
	}

	public String errorPasswordIsIncorrect() {
		return "Sorry, the password is incorrect.";
	}

	public String errorRequiresSignIn(boolean allowSaveIfNotSignedIn) {
		String result = "Sorry, you must be signed in for this.";

		if (allowSaveIfNotSignedIn)
			result += "<br><br>However, if you create a new notebook then an account will automatically be created for you.<br><br>"
					+ "You can change your username and password later.";

		return result;
	}

	public String pageTitleAccounts() {
		return "Accounts";
	}

	public String pageTitleAccountsTooltip() {
		return "View all accounts.";
	}

	public String clickToEditTheAccountTooltip() {
		return "Click to edit the account.";
	}

	public String errorNoAccountFound() {
		return "Sorry, the specified account could not be found.";
	}

	public String pageTitleAccount() {
		return "Account";
	}

	public String errorEmailIsNotValid() {
		return "Sorry, the email address is not valid. Remember, it is optional.";
	}

	public String sentenceNoAccountsExist() {
		return "No accounts have been created yet.";
	}

	public String tooltipSearch() {
		return "Go to 'Help / Search' for information on query syntax.";
	}

	public String errorQueryIsRequired() {
		return "Sorry, you must specify a search query.";
	}

	public String sentenceChooseAUserName() {
		return "Choose a username (at least 3 characters from a-z and 0-9)";
	}

	public String sentenceNewPassword() {
		return "Choose a new password (at least 8 characters)";
	}

	public String sentenceUserMayBeContacted() {
		return "The user may be contacted with information about this web site.";
	}

	public String sentenceIMayBeContacted() {
		return "I may be contacted with information about this web site.";
	}

	public String sentenceUserIsAnAdmin() {
		return "The user is an admin.";
	}

	public String sentenceIsAccountClosed() {
		return "The account is closed.";
	}

	public String sentenceVerifyNewPassword() {
		return "Verify the new password";
	}

	public String sentenceEmailOptional() {
		return "Email address (optional)";
	}

	public String errorAccountIsClosed() {
		return "Sorry, your account is closed.";
	}

	public String errorCurrentPasswordIsNotValid() {
		return "Sorry, the current password is not valid. It must be between 8 and 20 letters long.";
	}

	public String errorCurrentPasswordIsIncorrect() {
		return "Sorry, the current password is incorrect.";
	}

	public String pageTitleChangeAccount() {
		return "Change Account Details";
	}

	public String sentenceChangesWereSaved() {
		return "Your changes were saved.";
	}

	public String errorNoChangesToSave() {
		return "Sorry, there were no changes to save.";
	}

	public String errorChangesWereNotSaved() {
		return "Your changes were not saved because of this.";
	}

	public String errorSelfSignUpNotAllowed() {
		return "Sorry, creating your own account is not allowed on this server.";
	}

	public String errorOnlyAnAdminCanChangeIsAdmin() {
		return "Sorry, only an admin can change whether a user is an admin.";
	}

	public String errorSingleUserMustStayAnAdmin() {
		return "Sorry, single users must be admins.";
	}

	public String sentenceAllowSaveIfNotSignedIn() {
		return "Creating this notebook will automatically create an account for you and sign you in.<br><br>You can change your username and password later.<br><br>";
	}

	public String sentenceYouHaveBeenSignedOut() {
		return "You have been signed out.";
	}

	public String sentenceYouHaveNotBeenSignedOut() {
		return "Sorry, it seems like you tried to sign out but it didn't work.";
	}

	public String pageTitleSignedOut() {
		return "Signed Out";
	}

	public String errorWrongCsrft() {
		return "Sorry, the wrong csrft was supplied.";
	}

	public String pageTitleWelcome() {
		return "Welcome to CrushPaper";
	}

	public String pageTitleWelcomeExtra() {
		return " where you can create notes and save quotations from sources";
	}

	public String sentenceReuseIds() {
		return "Reuse note IDs if possible.";
	}

	public String shutdownAreYouSure() {
		return "This kills the CrushPaper server process. <br><br>It will not handle HTTP requests until restarted from the command line.";
	}

	public String sentenceShuttingdown() {
		return "The CrushPaper server process is shutting down. <br><br>It will not handle HTTP requests until restarted from the command line.";
	}

	public String pageTitleShutdown() {
		return "Shutdown";
	}

	public String pageTitleShutdownTooltip() {
		return "This kills the process. It will not handle HTTP requests until restarted from the command line.";
	}

	public String pageTitleHelp() {
		return "Help";
	}

	public String pageTitleHelpTooltip() {
		return "View the list of help pages for this site.";
	}

	public String pageTitleUiHelp() {
		return "UI Help";
	}

	public String pageTitleUiHelpTooltip() {
		return "View the list keyboard shortcuts and mouse actions for this page.";
	}

	public String callToAction(boolean allowSaveIfNotSignedIn) {
		if (allowSaveIfNotSignedIn)
			return "<br><br><div style=\"text-align: center\"><div style=\"display: inline-block;\">"
					+ "<b>Try CrushPaper for free without creating an account!</b><br><br>"
					+ "<form action=\"/newNotebook/\" method=\"GET\">"
					+ "<button onclick=\"showPopupForCreateNotebook(); return false;\" id=\"save\" class=\"specialbutton\" style=\"float: none;\" title=\"Create a notebook where you can put notes and quotations\">"
					+ "Create a New Notebook" + "</button></form></div></div>";
		return "";
	}

	public String viewYourNotebooks() {
		return "<br><br><div style=\"text-align: center\"><div style=\"display: inline-block;\">"
				+ "<form action=\"/notebooks/\" method=\"GET\">"
				+ "<button onclick=\"newPaneForForm(event, '"
				+ pageTitleNotebooks()
				+ "', 'notebooks'); return false;\" id=\"save\" class=\"specialbutton\" style=\"float: none;\" title=\"View all of the notebooks you have created\">"
				+ "Go to your notebooks" + "</button></form></div></div>";
	}

	public String errorNoteIsInvalid() {
		return "Sorry, the note is too long.";
	}

	public String errorQuotationIsInvalid() {
		return "Sorry, the quotation is too long.";
	}

	public String errorUrlIsInvalid() {
		return "Sorry, the web address is too long.";
	}

	public String errorTitleIsInvalid() {
		return "Sorry, the title is too long.";
	}

	public String pageTitleUsersNotebooksTooltip() {
		return "View all the user's notebooks.";
	}

	public String pageTitleUsersAllNotesTooltip() {
		return "View all the user's notes.";
	}

	public String pageTitleUsersQuotationsTooltip() {
		return "View all the user's quotations.";
	}

	public String pageTitleUsersSourcesTooltip() {
		return "View all the user's sources.";
	}

	public String pageTitleUsersSearchTooltip() {
		return "Search as the user.";
	}

	public String sentenceSeeWhatTheUserSees() {
		return "See what the user sees:";
	}

	public String errorParentMustBeMovedBeforeChild() {
		return "Sorry, parents must be moved before subnotes are moved. This is a bug in the user interface.";
	}

	public String errorTargetIdInvalidFormat() {
		return "Sorry, the specified target ID was in an invalid format.";
	}

	public String errorTargetNoteCouldNotBeFound() {
		return "Sorry, the target note could not be found.";
	}

	public String errorTargetParentCouldNotBeFound() {
		return "Sorry, the target parent could not be found.";
	}

	public String errorDuplicateEntry() {
		return "Sorry, there was a duplicate note in the list. This is a bug in the user interface.";
	}

	public String errorTargetAndObjectCanNotBeTheSame() {
		return "Sorry, the target and object cannot be the same. This is a bug in the user interface.";
	}

	public String errorLevelsIsInvalid() {
		return "Sorry, the levels parameter is invalid. This is a bug in the user interface.";
	}

	public String errorMayNotSeeList() {
		return "Sorry, you are not entitled to view this list.";
	}

	public String pageTitleAdvancedHelp() {
		return "Advanced Help";
	}

	public String introTextShowNotebook(boolean forTouch) {
		String result = "&bull; This is a notebook. It is a hierarchy of your notes.<br>&bull; ";
		
		if(forTouch) {
			result += "Press a note to edit it.";
		} else {
			result += "Mouse over, right click or press h to see all of the available commands.";
		}
		
		return result;
	}

	public String introTextShowSources(boolean forTouch) {
		return "&bull; This is the list of all the sources from which you have saved quotations.<br>"
				+ sentenceWhatIsAQuotation() + sentenceQuotationClickDesc(forTouch, false);
	}

	private String sentenceWhatIsAQuotation() {
		return "&bull; A quotation is a snippet of text from another web site that you selected and saved to CrushPaper.";
	}

	public String introTextShowNotebooks(boolean forTouch) {
		return "&bull; This is the hierarchy of all the notebooks you have created.<br>&bull; Each notebook contains a hierarchy of notes and quotations.<br>"
				+ sentenceWhatIsAQuotation() +
					"<br>&bull; " + clickOrTouchCapFirst(forTouch) + " a notebook to view or " + clickOrTouch(forTouch) + " the pencil icon above to edit this hierarchy.";
	}

	public String introTextSearchQuotations(boolean forTouch) {
		return "&bull; You have just searched the quotations you have saved.<br>"
				+ sentenceWhatIsAQuotation() + sentenceQuotationClickDesc(forTouch, false)
				+ sentenceNotebookIconClickDesc(forTouch);
	}

	public String introTextSearchNotes(boolean forTouch) {
		return "You have just searched all of your notes and your quotations that have notes.<br>"
				+ sentenceWhatIsAQuotation()
				+ "<br>" + clickOrTouchCapFirst(forTouch) + " a note to view or edit it."
				+ sentenceNotebookIconClickDesc(forTouch);
	}

	public String introTextSearchNotebooks(boolean forTouch) {
		return "&bull; You have just searched the titles of all your notebooks.<br>" + clickOrTouchCapFirst(forTouch) + " the book icon to view a notebook.";
	}

	public String introTextSearchSources(boolean forTouch) {
		return "&bull; You have just searched the titles and web addresses of all the sources from which you have saved quotations.<br>"
				+ sentenceWhatIsAQuotation() + sentenceQuotationClickDesc(forTouch, false);
	}

	private String clickOrTouch(boolean forTouch) {
		return forTouch ? "touch" : "click";
	}

	private String clickOrTouchCapFirst(boolean forTouch) {
		return forTouch ? "Touch" : "Click";
	}

	private String sentenceQuotationClickDesc(boolean forTouch, boolean forShowSource) {
		if(forShowSource) {
			return "";
		}
		
		String result = "<br>&bull; ";
		
		if (forTouch) {
			result += "Press ";
		} else {
			result += "Click the quotation to drag it into one of your notebooks, or click ";
		}
		
		result += "\"More quotations from this source\" to view them.";
		
		return result;
	}

	private String sentenceNotebookIconClickDesc(boolean forTouch) {
		return "<br>&bull; " + clickOrTouchCapFirst(forTouch) + " the book icon to view it in the context of its notebook if it is in one.";
	}

	public String introTextSearchUsers() {
		return "&bull; You have just searched the usernames of all accounts on this server.";
	}

	public String introTextShowQuotations(boolean forTouch) {
		return "&bull; This is the list of all the quotations you have saved.<br>"
				+ sentenceWhatIsAQuotation() + sentenceQuotationClickDesc(forTouch, false);
	}

	public String introTextShowSource(boolean forTouch) {
		return "&bull; This is a source from which you have saved quotations.<br>&bull; " + clickOrTouchCapFirst(forTouch) + " the title of the source to go that web site."
				+ sentenceQuotationClickDesc(forTouch, true);
	}

	public String introTextSearchAccounts() {
		return "&bull; You have just searched the usernames of all the accounts on this server.";
	}

	public String introTextShowAccounts(boolean forTouch) {
		return "&bull; This is the list of all the accounts on this server.<br>&bull; " + clickOrTouchCapFirst(forTouch) + " to view or edit.<br>";
	}

	public String introTextShowNotes() {
		return "&bull; This is the list of all the notes you have you have created that do not contain quotations.<br>"
				+ sentenceWhatIsAQuotation();
	}

	public String labelAnyoneCanReadThis() {
		return "Anyone can read this notebook";
	}

	public String labelYourNotebookTitle() {
		return "Your notebook title";
	}

	public String buttonChangePassword() {
		return "Change Password";
	}

	public String buttonChangeAccountDetails() {
		return "Change Account Details";
	}

	public String prefix(boolean currentIsEditedUser) {
		return (currentIsEditedUser ? "You " : "The user ");
	}

	public String prefixWithIsOrAre(boolean currentIsEditedUser) {
		return (currentIsEditedUser ? "You are " : "The user is ");
	}

	public String prefixPossesive(boolean currentIsEditedUser) {
		return (currentIsEditedUser ? "Your " : "The user's ");
	}

	public String sentenceIsAnAdmin(boolean currentIsEditedUser) {
		return prefixWithIsOrAre(currentIsEditedUser) + " an admin.";
	}

	public String sentenceAccountIsClosed(boolean currentIsEditedUser) {
		return prefixPossesive(currentIsEditedUser) + "account is closed.";
	}

	public String sentenceMayBeContacted(boolean currentIsEditedUser,
			boolean mayBeContacted) {
		return prefix(currentIsEditedUser) + "may "
				+ (mayBeContacted ? "" : "not ")
				+ "be contacted with information about this web site.";
	}

	public String sentenceEmailIs(boolean currentIsEditedUser, String email) {
		if (email == null || email.isEmpty())
			return prefixPossesive(currentIsEditedUser)
					+ "email address is not set.";
		return prefixPossesive(currentIsEditedUser) + "email address is "
				+ email + ".";
	}

	public String sentenceUsernameIs(boolean currentIsEditedUser,
			String username) {
		return prefixPossesive(currentIsEditedUser) + "user name is "
				+ username + ".";
	}

	public String pageTitleViewAccount() {
		return "Account Details";
	}

	public String pageTitleChangePassword() {
		return "Change Password";
	}

	public String sentenceCurrentPassword(boolean currentIsEditedUser) {
		return prefixPossesive(currentIsEditedUser) + "current password";
	}

	public String pageTitleCloseAccount() {
		return "Close Account";
	}

	public String sentenceSureYouWantToCloseAccount(
			boolean currentIsEditedUser, String username) {
		return "Are you sure you want to close "
				+ (currentIsEditedUser ? "your" : username + "'s")
				+ " account?";
	}

	public String buttonCloseAccount() {
		return "Close Account";
	}

	public String sentenceEnterNewAccountDetailsHere(
			boolean currentIsEditedUser, String username) {
		return "Enter " + (currentIsEditedUser ? "your" : username + "'s")
				+ " new account details here:";
	}

	public String sentenceEnterNewPasswordHereTwice(
			boolean currentIsEditedUser, String username) {
		return "Enter " + (currentIsEditedUser ? "your" : username + "'s")
				+ " new password here twice:";
	}

	public String sentenceEnterYourCurrentPasswordHere() {
		return "Enter your current password here:";
	}

	public String errorUsernameMayNotBeChanged() {
		return "Username may not be changed";
	}

	public String sentencePleaseChangeNameFromGenerated(
			boolean currentIsEditedUser, String username) {
		return "Please change "
				+ (currentIsEditedUser ? "your" : username + "'s")
				+ " user name:";
	}

	public String errorFirstPasswordMustBeSet(boolean currentIsEditedUser,
			String username) {
		return "The first password field must contain "
				+ (currentIsEditedUser ? "your" : username + "'s")
				+ " new password";
	}

	public String errorSecondPasswordMustBeSet(boolean currentIsEditedUser,
			String username) {
		return "The second password field must also contain "
				+ (currentIsEditedUser ? "your" : username + "'s")
				+ " new password";
	}

	public String errorNewPasswordIsTheSameAsTheCurrent() {
		return "The new password is the same as the current password.";
	}

	public String plusTooltip() {
		return "Click to show one more level.";
	}

	public String minusTooltip() {
		return "Click to show one less level.";
	}

	public String introTextImport() {
		return "You can import notes, quotations and sources by uploading a JSON file to the server using this page.<br><br>"
				+ "If you select \"Reuse note IDs if possible\" then the CrushPaper server will try recreate your notes, quotations and sources with the same IDs as in the file. This is useful if you want to be able to retain the same URLs. If another item already has the ID then the CrushPaper server will assign a new ID.<br><br>"
				+ "If you select \"The file is in MS Word List Format\" then the CrushPaper server will treat the file as if it contains a single notebook in this format:" +
				"<pre>" +
				"*\tNote at level 1<br>" +
				"O\tNote at level 2<br>" +
				"?\tNote at level x<br>" +
				"</pre>" +
				"CrushPaper assumes that if the bullet character that appears at the beginning of the line has not been seen before then the line should be at a level below any other levels.";
	}

	public String tooltipOptions() {
		return "Change the options for this site.";
	}

	public String linkOptions() {
		return "Options";
	}

	public String pageTitleSearchTooltip() {
		return "Search your notes, quotations and sources.";
	}

	public String pageTitleSearch() {
		return "Search";
	}

	public String placeholderSearch() {
		return "";
	}

	public String buttonSearch() {
		return "Search";
	}

	public String labelSearchSources() {
		return "Sources";
	}

	public String labelSearchQuotations() {
		return "Quotations";
	}

	public String labelSearchNotes() {
		return "Notes";
	}

	public String errorSearchDataSetIsRequired() {
		return "Sorry, you must specify a data set to search.";
	}

	public String errorJavaScriptNeeded() {
		return "Sorry, JavaScript is required for most features of this site.";
	}

	public String pageTitleGetNextPage() {
		return "Get Next Page";
	}

	public String pageTitleGetPreviousPage() {
		return "Get Previous Page";
	}

	public String errorInvalidUrl() {
		return "Sorry, the web address was invalid.";
	}

	public String labelSearchAccounts() {
		return "Accounts";
	}

	public String tooltipRefreshPane() {
		return "Click to refresh this part of the page.";
	}

	public String tooltipClosePane() {
		return "Click to close this part of the page.";
	}

	public String pageTitleCloseAllTooltip() {
		return "Close all open subpages.";
	}

	public String pageTitleCloseAll() {
		return "Close All";
	}

	public String buttonNewNote() {
		return "New Note";
	}

	public String buttonNewNotebook() {
		return "New Notebook";
	}

	public String tooltipNewNote() {
		return "Click to add a new note to this notebook.";
	}

	public String tooltipNewNotebook() {
		return "Click to create a new notebook.";
	}

	public String pageTitleNote() {
		return "Note";
	}

	public String errorEntryCanNotBeDeleted() {
		return "Sorry, this the entry cannot be deleted this way. This is a bug in the user interface.";
	}

	public String fragmentVisitExternalSource() {
		return "View the external web page at";
	}

	public String labelSearchNotebooks() {
		return "Notebooks";
	}

	public String linkMenu() {
		return "Menu";
	}

	public String tooltipMenu() {
		return "Click to show or hide the menu.";
	}

	public String errorNeedLongerQuery() {
		return "Sorry, please try a longer query.";
	}

	public String sentenceMsWordListFormat() {
		return "The file is in MS Word List Format.";
	}
}
