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
 * This is the full list of error strings that can be returned by DbLogic. This
 * serves to isolate all errors into one place where they can be reviewed for
 * consistency. This should also make it easier to internationalize the
 * application if desired. errors should be full sentences with periods.
 */
public class DbLogicErrorMessages {

	public String errorDbAUserHadDanglingEntries(int size) {
		return "A user had " + size + " dangling entries.";
	}

	public String errorDatabase() {
		return "Sorry, there was an internal database error.";
	}

	public String errorParentIdWasNotFound(final String clientParentId) {
		return "Sorry, a note specified as a parent id "
				+ stringWithQuotes(clientParentId)
				+ "could not be found in the uploaded JSON.";
	}

	public String errorJson() {
		return "Sorry, the JSON could not be parsed.";
	}

	public String errorImportedIdWasNotFound(final String id) {
		return "Sorry, the imported ID corresponding to this real ID " + id
				+ " could not be found in the uploaded JSON.";
	}

	public String errorRealIdWasNotFound(final String id) {
		return "Sorry, the real ID corresponding to this imported ID " + id
				+ " could not be found in the uploaded JSON.";
	}

	public String errorInvalidTimestamp() {
		return "Sorry, the specified timestamp was in an invalid format.";
	}

	public String errorChildIsAnAncestorOfTheParent() {
		return "Sorry, the proposed child is an ancestor of the parent.";
	}

	public String errorChildIsNull() {
		return "Sorry, the proposed child was null.";
	}

	public String errorChildrenActionInvalid() {
		return "Sorry, the proposed childrenAction was not a valid value.";
	}

	public String errorChildrenActionMayNotBeParentIfTheDeletedEntryHasNoParent() {
		return "Sorry, the childrenAction may not be 'parent' if the deleted note has no parent.";
	}

	public String errorCreateTimeIsNull() {
		return "Sorry, the proposed createTime was null.";
	}

	public String errorDirectionIsNull() {
		return "Sorry, the proposed direction was null.";
	}

	public String errorSourceIsNull() {
		return "Sorry, the proposed source was null.";
	}

	public String errorEntryIsNull() {
		return "Sorry, the proposed note was null.";
	}

	public String errorIdIsInvalid() {
		return "Sorry, the proposed ID was in an invalid format.";
	}

	public String errorInsertingANewEntryAboveChildrenCanOnlyBeDoneWhenTheRelatedEntryIsAParent() {
		return "Sorry, inserting a new note above children can only be done when the related note is a parent.";
	}

	public String errorModTimeIsNull() {
		return "Sorry, the proposed modTime was null.";
	}

	public String errorMovedIsAnAncestorOfTheSibling() {
		return "Sorry, the proposed moved note was an ancestor of the target note.";
	}

	public String errorMovedIsNull() {
		return "Sorry, the proposed moved note was null.";
	}

	public String errorNextAndPreviousHaveDifferentParents() {
		return "Sorry, the proposed next and previous have different parents.";
	}

	public String errorNextHasNoParent() {
		return "Sorry, the proposed next has no parent.";
	}

	public String errorNextIsNull() {
		return "Sorry, the proposed next note was null.";
	}

	public String errorNotEntitledToModifyRelatedEntry() {
		return "Sorry, the user is not entitled to modify the related note.";
	}

	public String errorParentIsNull() {
		return "Sorry, the proposed parent was null.";
	}

	public String errorPlacementIsNotValid() {
		return "Sorry, the proposed placement was not valid.";
	}

	public String errorPlacementIsNull() {
		return "Sorry, the proposed placement was null.";
	}

	public String errorPreviousHasNoParent() {
		return "Sorry, the proposed previous note has no parent.";
	}

	public String errorPreviousIsNull() {
		return "Sorry, the proposed previous note was null.";
	}

	public String errorRelatedIdIsEmpty() {
		return "Sorry, the proposed related ID was empty.";
	}

	public String errorRelatedIdIsInInvalidFormat() {
		return "Sorry, the proposed related ID was in an invalid format.";
	}

	public String errorRelatedIdIsNull() {
		return "Sorry, the proposed related ID was null.";
	}

	public String errorSiblingHasNoParent() {
		return "Sorry, the proposed target note has no parent.";
	}

	public String errorSiblingIsNull() {
		return "Sorry, the proposed target note was null.";
	}

	public String errorTheDirectionIsInvalid() {
		return "Sorry, the direction to move was invalid.";
	}

	public String errorDbTheEntryAParentIdButNoParentRelationship(
			final String parentId, final String id) {
		return "The entry " + stringWithQuotes(id) + "has the parentId "
				+ parentId + " but no parent relationship.";
	}

	public String errorTheEntryCouldNotBeFound() {
		return "The node could not be found.";
	}

	public String errorDbTheEntryDoesNotHaveAParentAndIsNotIndexedAsARootNote(
			final String id) {
		return "The entry "
				+ stringWithQuotes(id)
				+ "is a note and does not have a parent and is not indexed as a root note.";
	}

	public String errorTheEntryHadNoParentSoItCouldNotBeMoved() {
		return "Sorry, the note had no parent so it could not be moved.";
	}

	public String errorDbTheEntryHasABlankFirstChildId(final String id) {
		return "The entry " + stringWithQuotes(id)
				+ "has a blank firstChildId.";
	}

	public String errorDbTheEntryHasABlankLastChildId(final String id) {
		return "The entry " + stringWithQuotes(id) + "has a blank lastChildId.";
	}

	public String errorDbTheEntryHasABlankNextSiblingId(final String id) {
		return "The entry " + stringWithQuotes(id)
				+ "has a blank nextSiblingId.";
	}

	public String errorDbTheEntryHasABlankParentId(final String id) {
		return "The entry " + stringWithQuotes(id) + "has a blank parentId.";
	}

	public String errorDbTheEntryHasABlankPreviousSiblingId(final String id) {
		return "The entry " + stringWithQuotes(id)
				+ "has a blank previousSiblingId.";
	}

	public String errorDbTheEntryHasAChildWhosePreviousDoesNotMatch(
			final String id, String previousSiblingId, final String previousId) {
		return "The entry " + stringWithQuotes(id)
				+ "has a child whose previous id " + previousSiblingId
				+ " does not match " + previousId + ".";
	}

	public String errorDbTheEntryHasAChildWithASiblingThatIsNotRelated(
			final String id, final String nextId) {
		return "The entry " + stringWithQuotes(id)
				+ "has a child with a sibling with id " + nextId
				+ " that is not related.";
	}

	public String errorDbTheEntryHasAChildWithoutANextSiblingIdThatIsNotItsLastChild(
			final String id, String lastChildId) {
		return "The entry " + stringWithQuotes(id) + "has a child "
				+ lastChildId
				+ " without a nextSiblingId that is not its lastChild.";
	}

	public String errorDbTheEntryHasAChildWithoutAPreviousSiblingId(
			final String id) {
		return "The entry "
				+ stringWithQuotes(id)
				+ "has a child without a previousSiblingId that is not its firstChild.";
	}

	public String errorDbTheEntryHasAFirstChildIdButNoChildren(final String id) {
		return "The entry " + stringWithQuotes(id)
				+ "does not have a firstChildId but has children.";
	}

	public String errorDbTheEntryHasAFirstChildIdButNoLastChildId(
			final String firstChildId, final String id) {
		return "The entry " + stringWithQuotes(id) + "has a first child id"
				+ firstChildId + " but no last child id.";
	}

	public String errorDbTheEntryHasALastChildIdButNoFirstChildId(
			final String lastChildId, final String id) {
		return "The entry " + stringWithQuotes(id) + "has a last child id"
				+ lastChildId + " but no first child id.";
	}

	public String errorDbTheEntryHasAParentAndIsAlsoIndexedAsARootNote(
			final String id) {
		return "The entry " + stringWithQuotes(id)
				+ "is a note, has a parent and is also indexed as a root note.";
	}

	public String errorDbTheEntryHasExtraChildren(final String id,
			int numExtraChildren) {
		return "The entry " + stringWithQuotes(id) + "has " + numExtraChildren
				+ " extra children.";
	}

	public String errorDbTheEntryHasMoreThanOneChildWithoutAPreviousSiblingId(
			final String id) {
		return "The entry " + stringWithQuotes(id)
				+ "has more than one child without a previousSiblingId.";
	}

	public String errorDbTheEntryHasNoChildrenWithoutANextSiblingId(
			final String id) {
		return "The entry " + stringWithQuotes(id)
				+ "has no child without a nextSiblingId.";
	}

	public String errorDbTheEntryHasNoChildWithoutAPreviousSiblingId(
			final String id) {
		return "The entry " + stringWithQuotes(id)
				+ "has no child without a previousSiblingId.";
	}

	public String errorDbTheEntryHasTheWrongParentId(String entryParentId,
			final String id, String parentNodeId) {
		return "The entry " + stringWithQuotes(id) + "has the wrong parentId "
				+ parentNodeId + " and " + entryParentId + ".";
	}

	public String errorDbTheEntryIsAQuotationButIsNotInTheQuotationsIndex(
			final String id) {
		return "The entry " + stringWithQuotes(id)
				+ "is a quotation but is not in the quotations index.";
	}

	public String errorDbTheEntryIsANoteButNotInTheNotesIndex(final String id) {
		return "The entry " + stringWithQuotes(id)
				+ "is a note but is not in the notes index.";
	}

	public String errorDbTheEntryIsNotInTheEntriesIndex(final String id) {
		return "The entry " + stringWithQuotes(id)
				+ "is not in the notes index.";
	}

	public String errorDbTheEntryHadNoId() {
		return "The entry had no id.";
	}

	public String errorDbTheEntryIsRelatedTheWrongNumberOfUsers(
			final String id, int foundUsers) {
		return "The entry " + stringWithQuotes(id) + "is related to "
				+ foundUsers + " users instead of 1.";
	}

	public String errorDbTheEntryIsRelatedToMoreThanOneParent(final String id,
			String parentNodeId, String newParentNodeId) {
		return "The entry " + stringWithQuotes(id)
				+ "is related to more than one parent " + parentNodeId
				+ " and " + newParentNodeId + ".";
	}

	public String errorDbTheEntryIsTheParentsFirstChildButHasAPreviousSibling(
			final String id) {
		return "The entry " + stringWithQuotes(id)
				+ "is its parent's first child but has a previous sibling.";
	}

	public String errorDbTheEntryIsTheParentsLastChildButHasANextSibling(
			final String id) {
		return "The entry " + stringWithQuotes(id)
				+ "is its parent's last child but has a next sibling.";
	}

	public String errorThePrevousSiblingDoesNotHaveTheRightParent() {
		return "Sorry, the previous target does not have the right parent.";
	}

	public String errorTheProposedParentEntryCouldNotBeFound() {
		return "Sorry, the proposed parent note could not be found.";
	}

	public String errorTheProposedRelatedEntryCouldNotBeFound() {
		return "Sorry, the proposed related note could not be found.";
	}

	public String errorTheProposedSiblingEntryHasNoParent() {
		return "Sorry, the proposed target note has no parent.";
	}

	public String errorThereIsNoEntryToMoveAfter() {
		return "Sorry, there is no note to move after.";
	}

	public String errorThereIsNoEntryToMoveBefore() {
		return "Sorry, there is no note to move before.";
	}

	public String errorThereIsNoParentOfTheParent() {
		return "Sorry, there is no parent of the parent.";
	}

	public String errorThereIsNoPreviousEntryToUseAsAParent() {
		return "Sorry, there is no previous note to use as a parent.";
	}

	public String errorTheSiblingEntryCouldNotBeFound() {
		return "Sorry, the proposed target note could not be found.";
	}

	public String errorTheTypeOfTheEntryIsNotAllowed(String id) {
		return "Sorry, the type of the note " + stringWithQuotes(id)
				+ "is not an allowed value.";
	}

	public String errorTheUserIsNotEntitledToModifyTheChildEntry() {
		return "Sorry, the user is not entitled to modify the child note.";
	}

	public String errorTheUserIsNotEntitledToModifyTheEntry() {
		return "Sorry, the user is not entitled to modify the note.";
	}

	public String errorTheUserIsNotEntitledToModifyTheParentEntry() {
		return "Sorry, the user is not entitled to modify the parent note.";
	}

	public String errorTitleIsEmpty() {
		return "Sorry, the title is empty.";
	}

	public String errorTitleIsNull() {
		return "Sorry, the title is null.";
	}

	public String errorUrlIsEmpty() {
		return "Sorry, the URL is empty.";
	}

	public String errorUrlIsNull() {
		return "Sorry, the URL is null.";
	}

	public String errorUserIsNotEntitledToDelete() {
		return "Sorry, the user is not entitled to delete this.";
	}

	public String errorUserIsNotEntitledToModifyTheSource() {
		return "Sorry, the user is not entitled to modify the source.";
	}

	public String errorUserIsNotEntitledToModifyTheMovedEntry() {
		return "Sorry, the user is not entitled to modify the moved note.";
	}

	public String errorUserIsNotEntitledToModifyTheSiblingEntry() {
		return "Sorry, the user is not entitled to modify the target note.";
	}

	public String errorUserIsNotEntitledToModifyThisEntry() {
		return "Sorry, the user is not entitled to uproot this.";
	}

	public String errorUserIsNotEntitledToMoveTheEntry() {
		return "Sorry, the user is not entitled to move the note.";
	}

	public String errorUserIsNull() {
		return "Sorry, the user is null.";
	}

	public String errorTheEntryHasAFirstChildIdButNoChildren(final String id) {
		return "The note " + stringWithQuotes(id)
				+ "has a firstChildId but no children.";
	}

	public String errorsUserIdIsNull() {
		return "Sorry, the user ID is null.";
	}

	public String errorsTheInputStreamReaderIsNull() {
		return "Sorry, the input stream reader is null.";
	}

	public String errorNoLastWasFound() {
		return "Sorry, no last note of the notes was found.";
	}

	public String errorDbAUserHadDanglingQuotations(int size) {
		return "A user had " + size + " dangling quotations.";
	}

	public String errorDbAUserHadDanglingNotes(int size) {
		return "A user had " + size + " dangling notes.";
	}

	public String errorDbAUserHadDanglingRootNotes(int size) {
		return "A user had " + size + " dangling root notes.";
	}

	public String errorNoteIsInvalid(String id) {
		return "Sorry, the note " + stringWithQuotes(id) + "is too long.";
	}

	private String stringWithQuotes(String id) {
		if (id != null) {
			return "\"" + id + "\" ";
		}

		return "";
	}

	public String errorQuotationIsInvalid(String id) {
		return "Sorry, the quotation " + stringWithQuotes(id) + "is too long.";
	}

	public String errorUrlIsInvalid(String id) {
		return "Sorry, the URL " + stringWithQuotes(id) + "is too long.";
	}

	public String errorTitleIsInvalid(String id) {
		return "Sorry, the title " + stringWithQuotes(id) + "is too long.";
	}

	public String errorSourceIdWasNotFound(final String clientSourceId) {
		return "Sorry, a note specified as a source id "
				+ stringWithQuotes(clientSourceId)
				+ "could not be found in the uploaded JSON.";
	}

	public String errorSiblingParentNotFound() {
		return "Sorry, the proposed sibling node has a parent that could not be found.";
	}

	public String errorNotebookIdIsInvalid() {
		return "Sorry, the proposed ID was in an invalid format.";
	}

	public String errorCanNotCreateParentlessNote() {
		return "Sorry, you cannot create a non root note. This is a bug in the UI.";
	}

	public String errorInvalidRelatedType(String childType, String parentType,
			String childId) {
		return "Sorry, you cannot create a child entry "
				+ stringWithQuotes(childId)
				+ "with a parent relationship from a \"" + childType
				+ "\" to \"" + parentType + "\". This is a bug in the UI.";
	}

	public String errorRootIdWasNotFound(final String clientRootId) {
		return "Sorry, a note specified as a root id " + clientRootId
				+ " could not be found in the uploaded JSON.";
	}

	public String errorOnlyQuotationsMayHaveASource(String id) {
		return "Sorry, the entry " + stringWithQuotes(id)
				+ "is not a quotation so it may not have a source.";
	}

	public String errorOnlyNotebooksMayHaveARoot(String id) {
		return "Sorry, the entry " + stringWithQuotes(id)
				+ "is not a notebook so it may not have a root.";
	}

	public String errorSourceIdWasNotASource(String importedSourceId) {
		return "Sorry, the entry " + stringWithQuotes(importedSourceId)
				+ "was treated as a source but is not a source.";
	}

	public String errorRootIdWasNotARoot(String importedRootId) {
		return "Sorry, the entry " + stringWithQuotes(importedRootId)
				+ "was treated as a root but is not a root.";
	}

	public String errorRelatedTypeIsNull() {
		return "Sorry, the proposed relationship type was null.";
	}

	public String errorRootsAndTableOfContentsCanNotBeCreatedWithARelationship(
			String id) {
		return "Sorry, roots and table of contents cannot be created with a relationship. This is a bug in the UI.";
	}

	public String errorOnlyQuotationsMayHaveAQuotation(String id) {
		return "Sorry, the entry " + stringWithQuotes(id)
				+ "is not a quotation so it may not have a quotation.";
	}

	public String errorOnlySourcesMayHaveATitle(String id) {
		return "Sorry, the entry " + stringWithQuotes(id)
				+ "is not a source so it may not have a title.";
	}

	public String errorOnlySourcesMayHaveAUrl(String id) {
		return "Sorry, the entry " + stringWithQuotes(id)
				+ "is not a source so it may not have a url.";
	}

	public String errorOnlyRootsAndNotebooksCanBeCreatedWithOutAParent(String id) {
		return "Sorry, the entry " + stringWithQuotes(id)
				+ "is not a root or a notebook so it needs a parent.";
	}

	public String errorNotAllRootsHadNotebooks() {
		return "Sorry, not every root had a notebook.";
	}

	public String errorTableOfContentsMayNotBeImported(String id) {
		return "Sorry, a table of contents " + stringWithQuotes(id)
				+ " may not be imported.";
	}

	public String errorNotebooksMustHaveARootId(String id) {
		return "Sorry, a notebook " + stringWithQuotes(id)
				+ "must have a root ID.";
	}

	public String errorDuplicateId(String id) {
		return "Sorry, the id " + stringWithQuotes(id)
				+ "was used more than once.";
	}
	
	public String errorUserOnlyQuotationsAndSourcesMayBeUnlinked() {
		return "Sorry, only quotations and sources may be unlinked.";
	}
}
