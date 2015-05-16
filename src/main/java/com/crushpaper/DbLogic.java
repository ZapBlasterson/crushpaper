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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class's responsibility is to make all changes to the database so that
 * the application's consistency rules are enforced. The database can have 3
 * kinds of objects: Users, Entries and Sources. There are two types of Entries:
 * Quotations and Notes. Quotations are Notes that are linked to Sources.
 * Sources are implemented as Entries.
 */
public class DbLogic {
	private final DbLogicErrorMessages errorMessages = new DbLogicErrorMessages();
	private IdGenerator idGenerator = new UuidlIdGenerator();
	private DbInterface db;
	private File dbDirectory;

	DbLogic(File dbDirectory) {
		this.dbDirectory = dbDirectory;
		db = new JpaDb(idGenerator, dbDirectory);
	}

	/**
	 * The application can get the ID generator so that it can check if an ID is
	 * in a valid format.
	 */
	public IdGenerator getIdGenerator() {
		return idGenerator;
	}

	/**
	 * The application can swap in an alternative ID generator for unit testing.
	 */
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
		db.setIdGenerator(idGenerator);
	}

	public File getDbDirectory() {
		return dbDirectory;
	}

	/**
	 * This enumerates the ways in which an entry can be related to another
	 * entry.
	 */
	public enum TreeRelType {
		Parent, Child, Previous, Next,
	};

	/**
	 * A list of constants that reduces the risk of typos.
	 */
	public static class Constants {
		public static String source = "source";
		public static String quotation = "quotation";
		public static String note = "note";
		public static String root = "root";
		public static String notebook = "notebook";
		public static String noteId = "noteId";
		public static String notebookId = "notebookId";
		public static String parentId = "parentId";
		public static String nextSiblingId = "nextSiblingId";
		public static String previousSiblingId = "previousSiblingId";
		public static String modTime = "modTime";
		public static String createTime = "createTime";
		public static String type = "type";
		public static String userName = "userName";
		public static String email = "email";
		public static String password = "password";
		public static String firstChildId = "firstChildId";
		public static String lastChildId = "lastChildId";
		public static String url = "url";
		public static String id = "id";
		public static String title = "title";
		public static String isAdmin = "isAdmin";
		public static String mayContact = "mayContact";
		public static String isAccountClosed = "isAccountClosed";
		public static String isSingleUser = "isSingleUser";
		public static String isPublic = "isPublic";
		public static String isAnon = "isAnon";
		public static String wasCreatedAsAnon = "wasCreatedAnon";
		public static String sourceId = "sourceId";
		public static String options = "options";
		public static String rootId = "rootId";
		public static String tableofcontents = "tableofcontents";
	}

	/**
	 * API method. A simplified interface for creating an notebook entry which
	 * is related to another entry.
	 */
	public Entry createEntryNoteBook(User user, String note, Long createTime,
			String relatedId, TreeRelType relationship,
			boolean insertAboveParentsChildren, boolean insertAsFirstChild,
			boolean isPublic, boolean isAdmin, boolean addSampleNote, Errors errors) {
		Entry notebook = createEntry(user, null, Constants.notebook, null, null, note,
				createTime, createTime, relatedId, relationship,
				insertAboveParentsChildren, insertAsFirstChild, isPublic,
				false, false, isAdmin, errors);
		
		if (notebook != null && addSampleNote) {
			createSimpleEntry(user, errorMessages.textOfFirstNote(), createTime,
					notebook.getRootId(), TreeRelType.Parent,
					false, false,
					false, isAdmin, Constants.note, errors, null);
		}
		
		return notebook;
	}

	/**
	 * API method. A simplified interface for creating an note entry which is
	 * related to another entry.
	 * @param type
	 *            only use this for unit testing.
	 */
	Entry createSimpleEntry(User user, String note, Long createTime,
			String relatedId, TreeRelType relationship,
			boolean insertAboveParentsChildren, boolean insertAsFirstChild,
			boolean isPublic, boolean isAdmin, String type, Errors errors, Entry source) {
		return createEntry(user, null, type != null ? type : Constants.note,
				null, null, note, createTime, createTime, relatedId,
				relationship, insertAboveParentsChildren, insertAsFirstChild,
				false, false, false, isAdmin, errors);
	}

	/**
	 * API method. A simplified interface for creating a note or quotation
	 * entry.
	 */
	public Entry createEntryQuotation(User user, Entry source,
			String quotation, String note, Long createTime, boolean isAdmin,
			Errors errors) {
		return createEntry(user, source, DbLogic.Constants.quotation, null,
				quotation, note, createTime, createTime, null, null, false,
				false, false, false, false, isAdmin, errors);
	}

	/**
	 * API method. A simplified interface for creating a note or quotation
	 * entry. This is for restoring from JSON.
	 */
	public Entry createRawEntry(User user, Entry source, String type,
			String id, String quotation, String note, Long modTime,
			Long createTime, boolean isPublic, boolean isAdmin, Errors errors) {
		return createEntry(user, source, type, id, quotation, note, modTime,
				createTime, null, null, false, false, isPublic, true, true,
				isAdmin, errors);
	}

	/**
	 * Helper method. Creates an entry. Operations supported by this method are:
	 * 1) Create a new notebook entry. 2) Create a new entry that is related to
	 * an existing entry. 3) Create a new entry that is a parent of an existing
	 * entry and a child of an existing entry.
	 */
	private Entry createEntry(User user, Entry source, String type, String id,
			String quotation, String note, Long modTime, Long createTime,
			String relatedId, TreeRelType relationshipType,
			boolean insertAboveParentsChildren, boolean insertAsFirstChild,
			boolean isPublic, boolean skipNeedRelationshipCheck,
			boolean rootWillBeCreatedLater, boolean isAdmin, Errors errors) {

		// Basic Validations.
		if (relationshipType != null) {
			if (relatedId == null) {
				Errors.add(errors, errorMessages.errorRelatedIdIsNull());
				return null;
			}

			if (relatedId.isEmpty()) {
				Errors.add(errors, errorMessages.errorRelatedIdIsEmpty());
				return null;
			}
		}

		if ((relatedId == null || relatedId.isEmpty())
				&& relationshipType != null) {
			Errors.add(errors, errorMessages.errorRelatedTypeIsNull());
			return null;
		}

		if (relationshipType != TreeRelType.Parent
				&& insertAboveParentsChildren) {
			Errors.add(
					errors,
					errorMessages
							.errorInsertingANewEntryAboveChildrenCanOnlyBeDoneWhenTheRelatedEntryIsAParent());
			return null;
		}

		if (type == null
				|| (!type.equals(Constants.quotation)
						&& !type.equals(Constants.note)
						&& !type.equals(Constants.notebook)
						&& !type.equals(Constants.root) && !type
							.equals(Constants.tableofcontents))) {
			Errors.add(errors,
					errorMessages.errorTheTypeOfTheEntryIsNotAllowed(null));
			return null;
		}

		if (createTime == null) {
			Errors.add(errors, errorMessages.errorCreateTimeIsNull());
			return null;
		}

		if (modTime == null) {
			Errors.add(errors, errorMessages.errorModTimeIsNull());
			return null;
		}

		if (user == null) {
			Errors.add(errors, errorMessages.errorUserIsNull());
			return null;
		}

		if (id != null && !idGenerator.isIdWellFormed(id)) {
			Errors.add(errors, errorMessages.errorIdIsInvalid());
			return null;
		}

		if (relatedId != null && !idGenerator.isIdWellFormed(relatedId)) {
			Errors.add(errors, errorMessages.errorRelatedIdIsInInvalidFormat());
			return null;
		}

		if (source != null && !canUserModifyEntry(user, source, isAdmin)) {
			Errors.add(errors,
					errorMessages.errorUserIsNotEntitledToModifyTheSource());
			return null;
		}

		if (createTime.longValue() > modTime.longValue()) {
			modTime = createTime;
		}

		if (relationshipType == null && relatedId == null) {
			// Notebooks default to being children of the table of contents.
			if (type.equals(Constants.notebook)) {
				relationshipType = TreeRelType.Parent;
				relatedId = user.getTableOfContentsId();
			} else if (type.equals(Constants.note)
					&& !skipNeedRelationshipCheck) {
				Errors.add(errors,
						errorMessages.errorCanNotCreateParentlessNote());
				return null;
			}
		}

		if ((type.equals(Constants.root) || type
				.equals(Constants.tableofcontents)) && relationshipType != null) {
			Errors.add(
					errors,
					errorMessages
							.errorRootsAndTableOfContentsCanNotBeCreatedWithARelationship(null));
			return null;
		}

		String parentId = null;
		Entry parent = null;
		Entry related = null;
		if ((relationshipType == TreeRelType.Child
				|| relationshipType == TreeRelType.Previous || relationshipType == TreeRelType.Next)
				&& relatedId != null) {
			related = getEntryById(relatedId);
			if (related == null) {
				Errors.add(errors, errorMessages
						.errorTheProposedRelatedEntryCouldNotBeFound());
				return null;
			}

			parentId = related.getParentId();
			parent = getEntryById(parentId);

			if ((relationshipType == TreeRelType.Previous || relationshipType == TreeRelType.Next)
					&& parentId == null) {
				Errors.add(errors,
						errorMessages.errorTheProposedSiblingEntryHasNoParent());
				return null;
			}
		} else if (relationshipType == TreeRelType.Parent && relatedId != null) {
			parentId = relatedId;
			related = parent = getEntryById(parentId);
			if (parent == null) {
				Errors.add(errors, errorMessages
						.errorTheProposedParentEntryCouldNotBeFound());
				return null;
			}
		}

		if (related != null && !canUserModifyEntry(user, related, isAdmin)) {
			Errors.add(errors,
					errorMessages.errorNotEntitledToModifyRelatedEntry());
			return null;
		}

		if (parent != null) {
			if (!verifyTypesForParentChildRelationship(type, parent.getType(),
					null, errors)) {
				return null;
			}
		}

		// Create it.
		final Entry entry = new Entry();
		entry.setDb(db);
		if (id == null) {
			id = idGenerator.getAnotherId();
		}

		entry.setId(id);

		entry.setType(type);

		if (note != null) {
			note = cleanUpText(note);
		}

		if (entry.isNotebook()) {
			entry.setNotebookTitle(note);
		} else {
			entry.setNote(note);
		}

		if (quotation != null) {
			quotation = cleanUpText(quotation);
			entry.setQuotation(quotation);
		}

		entry.setParentId(parentId);
		entry.setModTime(modTime);
		entry.setCreateTime(createTime);
		entry.setIsPublic(isPublic);
		entry.setUserId(user.getId());

		if (type.equals(Constants.quotation)) {
			if (source != null) {
				entry.setSourceId(source.getId());
			}
		}

		if (parent != null && insertAboveParentsChildren) {
			// This is the "put underneath" entry functionality where a new
			// entry is stuck in between the selected entry and all of
			// its children.

			for (Object objectChild : getEntriesByParentId(parent.getId())) {
				Entry child = (Entry) objectChild;
				if (!id.equals(child.getId())) {
					child.setParentId(id);
				}
			}

			// Set this entry's first and last to the parent's first
			// and last.
			entry.setFirstChildId(parent.getFirstChildId());
			entry.setLastChildId(parent.getLastChildId());

			// Set parent's first and last to this entry.
			parent.setFirstChildId(id);
			parent.setLastChildId(id);
		} else if (relationshipType == TreeRelType.Child) {
			// This is the "insert" entry functionality where a new
			// entry is inserted between a selected entry and its
			// parent.
			final String childIdToReparent = relatedId;

			final Entry child = getEntryById(childIdToReparent);
			if (child != null) {
				if (parent != null) {
					// If parent's last entry is this child then set the
					// parent's last entry to the new entry.
					if (childIdToReparent.equals(parent.getLastChildId())) {
						parent.setLastChildId(id);
					}

					// If parent's first entry is this child then set the
					// parent's last entry to this new entry.
					if (childIdToReparent.equals(parent.getFirstChildId())) {
						parent.setFirstChildId(id);
					}
				}

				// Set the next of the previous of the child (if
				// any) to this new entry.
				String childsPreviousId = null;
				if (child.hasPreviousSiblingId()) {
					childsPreviousId = child.getPreviousSiblingId();
					final Entry childsPrevious = getEntryById(childsPreviousId);
					if (childsPrevious != null) {
						childsPrevious.setNextSiblingId(id);
					}
				}

				// Set the previous of the next of the child (if
				// any) to this new entry.
				String childsNextId = null;
				if (child.hasNextSiblingId()) {
					childsNextId = child.getNextSiblingId();
					final Entry childsNext = getEntryById(childsNextId);
					if (childsNext != null) {
						childsNext.setPreviousSiblingId(id);
					}
				}

				// Set this entry's first and last to this child.
				entry.setFirstChildId(childIdToReparent);
				entry.setLastChildId(childIdToReparent);

				// Set this entry's previous to the child's old.
				// previous.
				if (childsPreviousId != null) {
					entry.setPreviousSiblingId(childsPreviousId);
				}

				// Set this entry's next to the child's old next.
				if (childsNextId != null) {
					entry.setNextSiblingId(childsNextId);
				}

				child.setParentId(id);
				child.setNextSiblingId(null);
				child.setPreviousSiblingId(null);
			}
		} else if ((relationshipType == TreeRelType.Previous || relationshipType == TreeRelType.Next)
				&& relatedId != null) {
			final Entry sibling = getEntryById(relatedId);
			if (sibling == null) {
				Errors.add(errors,
						errorMessages.errorTheSiblingEntryCouldNotBeFound());
				return null;
			}

			parentId = sibling.getParentId();

			parent = getEntryById(parentId);
			if (parent == null) {
				Errors.add(errors, errorMessages.errorSiblingParentNotFound());
				return null;
			}

			if (relationshipType == TreeRelType.Previous) {
				// In this case the related object will be entry's
				// previous.
				final String parentsLastChildId = parent.getLastChildId();
				if (parentsLastChildId != null
						&& parentsLastChildId.equals(relatedId)) {
					parent.setLastChildId(id);
				}

				final String siblingsNextId = sibling.getNextSiblingId();
				sibling.setNextSiblingId(id);
				entry.setPreviousSiblingId(relatedId);
				if (siblingsNextId != null) {
					entry.setNextSiblingId(siblingsNextId);
					final Entry siblingsNext = getEntryById(siblingsNextId);
					siblingsNext.setPreviousSiblingId(id);
				}
			} else {
				// In this case the related object will be entry's next.
				final String parentsFirstChildId = parent.getFirstChildId();
				if (parentsFirstChildId != null
						&& parentsFirstChildId.equals(relatedId)) {
					parent.setFirstChildId(id);
				}

				final String siblingsPreviousId = sibling
						.getPreviousSiblingId();
				sibling.setPreviousSiblingId(id);
				entry.setNextSiblingId(relatedId);
				if (siblingsPreviousId != null) {
					entry.setPreviousSiblingId(siblingsPreviousId);
					final Entry siblingsPrevious = getEntryById(siblingsPreviousId);
					siblingsPrevious.setNextSiblingId(id);
				}
			}
		} else if (parent != null) {
			// This is the simple case where a new child is added to
			// a parent.
			if (insertAsFirstChild) {
				if (!parent.hasLastChildId()) {
					parent.setLastChildId(id);
				}

				if (parent.hasFirstChildId()) {
					final String parentsFirstChildId = parent.getFirstChildId();
					final Entry parentsFirstChild = getEntryById(parentsFirstChildId);
					if (parentsFirstChildId != null) {
						parentsFirstChild.setPreviousSiblingId(id);
					}

					entry.setNextSiblingId(parentsFirstChildId);
				}

				parent.setFirstChildId(id);
			} else {
				if (!parent.hasFirstChildId()) {
					parent.setFirstChildId(id);
				}

				if (parent.hasLastChildId()) {
					final String parentsLastChildId = parent.getLastChildId();
					final Entry parentsLastChild = getEntryById(parentsLastChildId);
					if (parentsLastChild != null) {
						parentsLastChild.setNextSiblingId(id);
					}

					entry.setPreviousSiblingId(parentsLastChildId);
				}

				parent.setLastChildId(id);
			}
		}

		db.persistEntry(entry);

		if (!rootWillBeCreatedLater
				&& entry.getType("").equals(Constants.notebook)) {
			Entry root = createRoot(user, null, Constants.root, entry.getId(),
					createTime, createTime, errors);
			entry.setRootId(root.getId());
		}

		return entry;
	}

	/** Verify that types of the parent and child are valid for a relationship. */
	private boolean verifyTypesForParentChildRelationship(String childType,
			String parentType, String childId, Errors errors) {
		if (childType.equals(Constants.notebook)
				&& !(parentType.equals(Constants.tableofcontents) || parentType
						.equals(Constants.notebook))) {
			Errors.add(errors, errorMessages.errorInvalidRelatedType(childType,
					parentType, childId));
			return false;
		}

		if ((childType.equals(Constants.note)
				|| childType.equals(Constants.quotation) || childType
					.equals(Constants.source))
				&& !(parentType.equals(Constants.root)
						|| parentType.equals(Constants.note)
						|| parentType.equals(Constants.quotation) || parentType
							.equals(Constants.source))) {
			Errors.add(errors, errorMessages.errorInvalidRelatedType(childType,
					parentType, childId));
			return false;
		}

		return true;
	}

	/**
	 * API method. Checks if the user is entitled to see the entry.
	 */
	public boolean canUserSeeEntry(User user, Entry entry, boolean isAdmin) {
		if (isAdmin) {
			return true;
		}

		if (user == null) {
			return false;
		}

		return entry.getUserId() != null
				&& entry.getUserId().equals(user.getId());
	}

	/**
	 * API method. Checks if the user is entitled to modify the entry.
	 */
	public boolean canUserModifyEntry(User user, Entry entry, boolean isAdmin) {
		return canUserSeeEntry(user, entry, isAdmin);
	}

	/** API method. Delete an entry entry. */
	public boolean deleteEntry(User user, Entry entry, String childrenAction,
			boolean isAdmin, List<String> deletedEntryIds, Errors errors) {
		String originalChildrenAction = childrenAction;

		// Basic Validations.
		if (entry == null) {
			Errors.add(errors, errorMessages.errorEntryIsNull());
			return false;
		}

		if (user == null) {
			Errors.add(errors, errorMessages.errorUserIsNull());
			return false;
		}

		if (childrenAction != null && !childrenAction.equals("delete")
				&& !childrenAction.equals("orphan")
				&& !childrenAction.equals("parent")) {
			Errors.add(errors, errorMessages.errorChildrenActionInvalid());
			return false;
		}

		if (!canUserModifyEntry(user, entry, isAdmin)) {
			Errors.add(errors, errorMessages.errorUserIsNotEntitledToDelete());
			return false;
		}

		final Entry parent = getEntryById(entry.getParentId());

		if (parent == null && childrenAction != null
				&& childrenAction.equals("parent")) {
			if (entry.isSource() || entry.isQuotation()) {
				childrenAction = "delete";
			} else {
				Errors.add(
						errors,
						errorMessages
								.errorChildrenActionMayNotBeParentIfTheDeletedEntryHasNoParent());
				return false;
			}
		}

		boolean doNotUpdateSiblings = false;
		if (childrenAction != null && childrenAction.equals("parent")) {
			// if this entry has children
			// if this entry has a previous
			// set the previous's next to the first child
			// set the first child's previous to the entry's previous
			// else
			// set the parent's first entry to the first child
			// if this entry has a next
			// set the next's previous to the last child
			// set the last child's next to the entry's next
			// else
			// set the parent's last entry to the last child

			reparentChildren(entry, parent);

			doNotUpdateSiblings = true;
		}

		if (parent != null) {
			removeEntryFromParent(entry, parent, doNotUpdateSiblings);
		}

		// Deal with children.
		for (Object objectChild : getEntriesByParentId(entry.getId())) {
			Entry child = (Entry) objectChild;
			// Delete all the children.
			if (childrenAction == null || childrenAction.equals("delete")) {
				// Recursive delete.
				if (!deleteEntry(user, child, childrenAction, isAdmin,
						deletedEntryIds, errors)) {
					return false;
				}
			} else if (childrenAction.equals("orphan")) {
				// Make the children notebook entries.
				child.setParentId(null);
				child.setNextSiblingId(null);
				child.setPreviousSiblingId(null);
			} else if (childrenAction.equals("parent")) {
				// Make the children children of the parent of the entry
				// that is being deleted.
				child.setParentId(parent.getId());
			}
		}

		if(deletedEntryIds != null) {
			deletedEntryIds.add(entry.getId());
		}

		db.removeEntry(entry);

		// Delete the notes for the notebook.
		if (entry.isNotebook()) {
			Entry rootEntry = getEntryById(entry.getRootId());
			return deleteEntry(user, rootEntry, "delete", isAdmin,
					deletedEntryIds, errors);
		} else if (entry.isSource()) {
			for (Object objectQuotation : getEntriesBySourceId(entry.getId(),
					0, Integer.MAX_VALUE)) {
				Entry quotation = (Entry) objectQuotation;

				if (wasEntryDeletedInThisTransaction(quotation)) {
					continue;
				}

				if (!deleteEntry(user, quotation, originalChildrenAction,
						isAdmin, deletedEntryIds, errors)) {
					return false;
				}
			}
		}

		return true;
	}

	/** Move the children of `entry` to be chidlren of `parent`. */
	private void reparentChildren(Entry entry, final Entry parent) {
		// if this entry has children
		// if this entry has a previous
		// set the previous's next to the first child
		// set the first child's previous to the entry's previous
		// else
		// set the parent's first entry to the first child
		// if this entry has a next
		// set the next's previous to the last child
		// set the last child's next to the entry's next
		// else
		// set the parent's last entry to the last child
		final Entry firstChild = getEntryById(entry.getFirstChildId());
		if (firstChild != null) {
			final Entry previous = getEntryById(entry
					.getPreviousSiblingId());
			if (previous != null) {
				previous.setNextSiblingId(firstChild.getId());
				firstChild.setPreviousSiblingId(previous.getId());
			} else {
				parent.setFirstChildId(firstChild.getId());
			}
		} else {
			final Entry previous = getEntryById(entry
					.getPreviousSiblingId());
			if (previous != null) {
				previous.setNextSiblingId(entry.getNextSiblingId());
			}
		}

		final Entry lastChild = getEntryById(entry.getLastChildId());
		if (lastChild != null) {
			final Entry next = getEntryById(entry.getNextSiblingId());
			if (next != null) {
				next.setPreviousSiblingId(lastChild.getId());
				lastChild.setNextSiblingId(next.getId());
			} else {
				parent.setLastChildId(lastChild.getId());
			}
		} else {
			final Entry next = getEntryById(entry.getNextSiblingId());
			if (next != null) {
				next.setPreviousSiblingId(entry.getPreviousSiblingId());
			}
		}
	}

	/**
	 * API method. Make the entry it a notebook.
	 */
	public boolean makeNotebookEntry(User user, Entry entry, boolean isAdmin,
			Errors errors) {
		// Basic Validations.
		if (entry == null) {
			Errors.add(errors, errorMessages.errorEntryIsNull());
			return false;
		}

		if (user == null) {
			Errors.add(errors, errorMessages.errorUserIsNull());
			return false;
		}

		if (!canUserModifyEntry(user, entry, isAdmin)) {
			Errors.add(errors,
					errorMessages.errorUserIsNotEntitledToModifyThisEntry());
			return false;
		}

		final Entry parent = getEntryById(entry.getParentId());
		if (parent != null) {
			removeEntryFromParent(entry, parent, false);
		}

		return true;
	}

	/**
	 * API method. Unlink the entry from parents and children
	 */
	public boolean unlinkEntry(User user, Entry entry, boolean isAdmin,
			Errors errors) {
		// Basic Validations.
		if (entry == null) {
			Errors.add(errors, errorMessages.errorEntryIsNull());
			return false;
		}

		if (user == null) {
			Errors.add(errors, errorMessages.errorUserIsNull());
			return false;
		}

		if (!canUserModifyEntry(user, entry, isAdmin)) {
			Errors.add(errors,
					errorMessages.errorUserIsNotEntitledToModifyThisEntry());
			return false;
		}

		if(!entry.isQuotation() && !entry.isSource()) {
			Errors.add(errors,
					errorMessages.errorUserOnlyQuotationsAndSourcesMayBeUnlinked());
			return false;
		}
		
		final Entry parent = getEntryById(entry.getParentId());
		if(parent == null) {
			return true;
		}

		reparentChildren(entry, parent);
		removeEntryFromParent(entry, parent, true);
		entry.setFirstChildId(null);
		entry.setLastChildId(null);
		for (Object objectChild : getEntriesByParentId(entry.getId())) {
			Entry child = (Entry) objectChild;
			child.setParentId(parent.getId());
		}
		
		return true;
	}

	/**
	 * Helper method. Removes the entry from its parent. After this method call
	 * the entry is unattached go any parent or sibling. This method updates the
	 * entry, its parents, and its siblings.
	 */
	private void removeEntryFromParent(Entry entry, Entry parent,
			boolean doNotUpdateSiblings) {
		// If parent's last entry is this then set the parent's last entry to
		// this
		// entry's previous.
		// Else if parent's first entry is this then set the parent's last entry
		// to this entry's next.

		final String entryId = entry.getId();
		final String nextSiblingId = entry.getNextSiblingId();
		final String previousSiblingId = entry.getPreviousSiblingId();

		entry.setParentId(null);

		final String parentsFirstId = parent.getFirstChildId();
		if (parentsFirstId.equals(entryId)) {
			parent.setFirstChildId(nextSiblingId);
		}

		final String parentsLastId = parent.getLastChildId();
		if (parentsLastId.equals(entryId)) {
			parent.setLastChildId(previousSiblingId);
		}

		final Entry previousSibling = getEntryById(previousSiblingId);

		final Entry nextSibling = getEntryById(nextSiblingId);

		if (!doNotUpdateSiblings) {
			if (nextSibling != null) {
				nextSibling.setPreviousSiblingId(previousSiblingId);
			}

			if (previousSibling != null) {
				previousSibling.setNextSiblingId(nextSiblingId);
			}
		}

		entry.setNextSiblingId(null);
		entry.setPreviousSiblingId(null);
	}

	/**
	 * API method. Return an entry for the user, and create it if it doesn't
	 * exist.
	 */
	public User getOrCreateUser(String userName) {
		if (!AccountAttributeValidator.isUserNameValid(userName)) {
			return null;
		}

		User user = db.getUserHelper(userName);
		if (user != null) {
			return user;
		}

		return createUserHelper(userName);
	}

	/**
	 * API method. Return a new entry for the user or null if it already exists.
	 */
	public User createUser(String userName) {
		if (!AccountAttributeValidator.isUserNameValid(userName)) {
			return null;
		}

		User user = db.getUserHelper(userName);
		if (user != null) {
			return null;
		}

		return createUserHelper(userName);
	}

	/** Helper method. Creates a user entry */
	private User createUserHelper(String userName) {
		User user = new User();
		user.setUserName(userName);
		user.setId(idGenerator.getAnotherId());
		long time = System.currentTimeMillis();
		user.setModTime(time);
		user.setCreateTime(time);

		db.persistUser(user);

		Entry root = createRoot(user, null, Constants.tableofcontents, null,
				time, time, null);
		user.setTableOfContentId(root.getId());

		return user;
	}

	/**
	 * API method. Returns the user entry for the id if it exists. If the `id`
	 * is null or invalid then null is returned.
	 */
	public User getUserById(String id) {
		return db.getUserById(id);
	}

	/**
	 * API method. Returns the user entry for the userName if it exists. If the
	 * `id` is null or invalid then null is returned.
	 */
	public User getUserByUserName(String userName) {
		if (!AccountAttributeValidator.isUserNameValid(userName)) {
			return null;
		}

		return db.getUserHelper(userName);
	}

	/**
	 * API method. Returns the entry entry if it exists. If the `id` is null or
	 * blank then null is returned.
	 */
	public Entry getEntryById(String id) {
		return db.getEntryById(id);
	}

	/** Returns true if the entry was deleted in this transaction. */
	public boolean wasEntryDeletedInThisTransaction(Entry entry) {
		return db.wasEntryDeletedInThisTransaction(entry);
	}

	/**
	 * API method. Returns the entries for the user if their quotation field
	 * matches the query.
	 */
	public List<?> searchEntriesForUserByQuotation(String userId, String query,
			int startPosition, int maxResults) {
		return db.searchEntriesForUserHelper(userId, "quotation", query,
				startPosition, maxResults);
	}

	/**
	 * API method. Returns the entries for the user if their note field matches
	 * the query.
	 */
	public List<?> searchEntriesForUserByNote(String userId, String query,
			int startPosition, int maxResults) {
		return db.searchEntriesForUserHelper(userId, "note", query,
				startPosition, maxResults);
	}

	/**
	 * API method. Returns the entries for the user if their notebookTitle field
	 * matches the query.
	 */
	public List<?> searchEntriesForUserByNotebookTitle(String userId,
			String query, int startPosition, int maxResults) {
		return db.searchEntriesForUserHelper(userId, "notebookTitle", query,
				startPosition, maxResults);
	}

	/**
	 * API method. Returns the entries for the user if their sourceTitle field
	 * matches the query.
	 */
	public List<?> searchEntriesForUserBySourceTitle(String userId,
			String query, int startPosition, int maxResults) {
		return db.searchEntriesForUserHelper(userId, "sourceTitle", query,
				startPosition, maxResults);
	}

	/**
	 * API method. Returns true if the table of contents has any notebooks.
	 */
	public boolean doesTableOfContentsHaveAnyNotebooks(String tableOfContentsId) {
		return db.doesTableOfContentsHaveAnyNotebooks(tableOfContentsId);
	}

	/**
	 * API method. Returns the entries that are children of the parent. If the
	 * `parentId` is null or blank then null is returned.
	 */
	public List<?> getEntriesByParentId(String parentId) {
		return db.getEntriesByParentId(parentId);
	}

	/**
	 * API method. Returns all users.
	 */
	public List<?> getAllUsers(int startPosition, int maxResults) {
		return db.getAllUsers(startPosition, maxResults);
	}

	/**
	 * API method. Returns all entries.
	 */
	public List<?> getAllEntries() {
		return db.getAllEntries();
	}

	/**
	 * API method. Returns all parentless entries.
	 */
	public List<?> getAllParentlessEntries() {
		return db.getAllParentlessEntries();
	}

	/**
	 * API method. Returns all entries for the user.
	 */
	public List<?> getEntriesByUserId(String userId) {
		return db.getEntriesByUserId(userId);
	}

	/**
	 * API method. Returns all entries for the source.
	 */
	public List<?> getEntriesBySourceId(String sourceId, int startPosition,
			int maxResults) {
		return db.getEntriesBySourceId(sourceId, startPosition, maxResults);
	}

	/**
	 * API method. Returns the source for the user and url.
	 */
	public Entry getEntryByUserIdAndUrl(String userId, String url) {
		return db.getEntryByUserIdAndUrl(userId, url);
	}

	/**
	 * API method. Returns the entries for the user and type.
	 */
	public List<?> getEntriesByUserIdAndType(String userId, String type,
			int startPosition, int maxResults) {
		return db.getEntriesByUserIdAndType(userId, type, startPosition,
				maxResults);
	}

	/**
	 * API method that helps the application from creating more than one DB in
	 * the lifetime of the application. See other comments the reason why.
	 */
	static public boolean hasADbHasEverBeenCreatedInThisProcess() {
		return aDbHasEverBeenCreatedInThisProcess;
	}

	static private boolean aDbHasEverBeenCreatedInThisProcess = false;

	/** API method. Creates the DB and its indexes if needed. */
	public boolean createDb() {
		synchronized (this) {
			aDbHasEverBeenCreatedInThisProcess = true;
			db.createDb();
			return true;
		}
	}

	/**
	 * API method. Starts its own transaction. Returns true if the DB is
	 * internally consistent. If this returns true then either: 1) there is a
	 * bug in the application code, 2) there is a bug in the DB code, 3) the DB
	 * was tampered with by another application 4) the DB has been corrupted.
	 */
	public boolean hasErrors(Errors errors) {
		if (errors == null)
			return false;

		// Iterate over all entries.
		List<?> entries = getAllEntries();
		for (Object entryUncasted : entries) {
			Entry entry = (Entry) entryUncasted;

			// Make sure it has an id.
			final String id = entry.getId();
			if (id == null || id.isEmpty()) {
				errors.add(errorMessages.errorDbTheEntryHadNoId());
				continue;
			}

			final String userId = entry.getUserId();
			if (userId == null || userId.isEmpty()) {
				errors.add(errorMessages
						.errorDbTheEntryIsRelatedTheWrongNumberOfUsers(id, 0));
				continue;
			}

			// Make sure it is linked to a user.
			User user = getUserById(userId);
			if (user == null) {
				errors.add(errorMessages
						.errorDbTheEntryIsRelatedTheWrongNumberOfUsers(id, 0));
				continue;
			}

			Entry parent = getEntryById(entry.getParentId());

			// If it has a parent id that parent must be linked.
			if (entry.hasParentId() && parent == null) {
				errors.add(errorMessages
						.errorDbTheEntryAParentIdButNoParentRelationship(
								entry.getParentId(""), id));
			}

			// If it has a first child id then last child id must be set.
			if (entry.hasLastChildId() && !entry.hasFirstChildId()) {
				errors.add(errorMessages
						.errorDbTheEntryHasALastChildIdButNoFirstChildId(
								entry.getLastChildId(""), id));
			}

			// If it has a last child id then first child id must be set.
			if (entry.hasFirstChildId() && !entry.hasLastChildId()) {
				errors.add(errorMessages
						.errorDbTheEntryHasAFirstChildIdButNoLastChildId(
								entry.getFirstChildId(""), id));
			}

			// If the parent's lastChildId is this then this entry may have
			// no nextSiblingId.
			if (parent != null && parent.getLastChildId("").equals(id)
					&& entry.hasNextSiblingId()) {
				errors.add(errorMessages
						.errorDbTheEntryIsTheParentsLastChildButHasANextSibling(id));
			}

			// If the parent's firstChildId is this then this entry may have
			// no previousSiblingId.
			if (parent != null && parent.getFirstChildId("").equals(id)
					&& entry.hasPreviousSiblingId()) {
				errors.add(errorMessages
						.errorDbTheEntryIsTheParentsFirstChildButHasAPreviousSibling(id));
			}

			final Hashtable<String, Entry> children = new Hashtable<String, Entry>();
			Entry firstChild = null;
			for (Object objectChild : getEntriesByParentId(entry.getId())) {
				Entry child = (Entry) objectChild;
				children.put(child.getId(), child);
				if (!child.hasPreviousSiblingId()) {
					if (firstChild != null) {
						errors.add(errorMessages
								.errorDbTheEntryHasMoreThanOneChildWithoutAPreviousSiblingId(id));
					}

					firstChild = child;
				}
			}

			// Make sure first child is is consistent.
			if (entry.hasFirstChildId() && children.isEmpty()) {
				errors.add(errorMessages
						.errorDbTheEntryHasAFirstChildIdButNoChildren(id));
			}

			if (!entry.hasFirstChildId() && !children.isEmpty()) {
				errors.add(errorMessages
						.errorDbTheEntryHasAFirstChildIdButNoChildren(id));
			}

			if (!children.isEmpty()) {
				if (firstChild == null) {
					errors.add(errorMessages
							.errorDbTheEntryHasNoChildWithoutAPreviousSiblingId(id));
				} else {
					if (!firstChild.getId("").equals(entry.getFirstChildId(""))) {
						errors.add(errorMessages
								.errorDbTheEntryHasAChildWithoutAPreviousSiblingId(id));
					}
				}

				Entry child = firstChild;
				Entry lastChild = null;
				int i = 0;
				for (; i < children.size(); ++i) {
					if (child == null) {
						break;
					}

					if (!child.hasNextSiblingId()) {
						lastChild = child;
						break;
					}

					final String previousId = child.getId("");

					final String nextId = child.getNextSiblingId();
					child = children.get(nextId);
					if (child == null) {
						errors.add(errorMessages
								.errorDbTheEntryHasAChildWithASiblingThatIsNotRelated(
										id, nextId));
					} else if (!child.getPreviousSiblingId("").equals(
							previousId)) {
						errors.add(errorMessages
								.errorDbTheEntryHasAChildWhosePreviousDoesNotMatch(
										id, child.getPreviousSiblingId(""),
										previousId));
					}
				}

				if (i != children.size() - 1) {
					errors.add(errorMessages.errorDbTheEntryHasExtraChildren(
							id, children.size() - i));
				}

				if (lastChild == null) {
					errors.add(errorMessages
							.errorDbTheEntryHasNoChildrenWithoutANextSiblingId(id));
				} else {
					if (!lastChild.getId("").equals(entry.getLastChildId(""))) {
						errors.add(errorMessages
								.errorDbTheEntryHasAChildWithoutANextSiblingIdThatIsNotItsLastChild(
										id, lastChild.getId("")));
					}
				}
			}
		}

		return errors.hasErrors();
	}

	/** Helper method. Appends indentation to `result`. */
	private void appendIndentation(int level, StringBuilder result) {
		for (int i = 0; i < level; ++i)
			result.append("  ");
	}

	/**
	 * Helper method. Converts an entry to a JSON string.
	 */
	public void toJsonEntryHelper(Entry entry, StringBuilder result, int level,
			boolean notTheFirst, boolean skipFirstLinefeed) {
		if (notTheFirst) {
			result.append(",\n");
		}

		if (!skipFirstLinefeed)
			appendIndentation(level, result);

		result.append("{\n");

		++level;

		boolean addedAnyYet = false;
		addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
				entry.getId(), addedAnyYet, "id", level);
		addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
				entry.getNote(), addedAnyYet, "note", level);
		addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
				entry.getFirstChildId(), addedAnyYet, "firstChild", level);
		addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
				entry.getLastChildId(), addedAnyYet, "lastChild", level);
		addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
				entry.getPreviousSiblingId(), addedAnyYet, "previousSibling",
				level);
		addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
				entry.getNextSiblingId(), addedAnyYet, "nextSibling", level);

		ArrayList<Entry> childrenEntries = getChildrenInOrder(entry);

		if (!childrenEntries.isEmpty()) {
			result.append(",\n");
			appendIndentation(level, result);
			result.append("\"children\": [\n");

			boolean notTheFirstChild = false;
			for (Entry childEntry : childrenEntries) {
				toJsonEntryHelper(childEntry, result, level + 1,
						notTheFirstChild, false);
				notTheFirstChild = true;
			}

			result.append("\n");
			appendIndentation(level, result);
			result.append("]");
		}

		--level;
		result.append("\n");
		appendIndentation(level, result);
		result.append("}");
	}

	/**
	 * API method. Converts all of the entries in the database to a JSON string.
	 * It does not include sources or users. This is just for simple
	 * visualization of the database. It is helpful for interactive debugging.
	 * This is not for exporting as it contains a lot of redundant information.
	 * It is also not for unit testing. Performance is abysmal.
	 * */
	public String toJson() {
		StringBuilder result = new StringBuilder();

		result.append("{\n");
		boolean notTheFirst = false;

		List<?> entries = getAllParentlessEntries();
		for (Object entryUncasted : entries) {
			Entry entry = (Entry) entryUncasted;

			if (notTheFirst) {
				result.append(",\n");
			}

			appendIndentation(1, result);
			result.append(JsonBuilder.quote(entry.getId()));
			result.append(": ");

			toJsonEntryHelper(entry, result, 1, false, true);
			notTheFirst = true;
		}

		result.append("\n}");

		return result.toString();
	}

	/*
	 * API method. Creates a parent child relationship between two entries. The
	 * user's entitlements must be checked before calling this method.
	 */
	public boolean createParentChildRelationship(User user, Entry parent,
			Entry child, Entry previousSibling, boolean isAdmin, Errors errors) {
		if (child == null) {
			Errors.add(errors, errorMessages.errorChildIsNull());
			return false;
		}

		if (parent == null) {
			Errors.add(errors, errorMessages.errorParentIsNull());
			return false;
		}

		if (user == null) {
			Errors.add(errors, errorMessages.errorUserIsNull());
			return false;
		}

		if (previousSibling != null
				&& !previousSibling.getParentId().equals(parent.getId())) {
			Errors.add(errors, errorMessages
					.errorThePrevousSiblingDoesNotHaveTheRightParent());
			return false;
		}

		// The child already has a parent then remove it.
		if (child.getParentId() != null
				&& !makeNotebookEntry(user, child, isAdmin, errors)) {
			return false;
		}

		// If parent has no first child make this child the parent's first
		// child.
		if (!parent.hasFirstChildId()) {
			parent.setFirstChildId(child.getId());
		}

		if (previousSibling == null
				|| previousSibling.getId().equals(parent.getLastChildId())) {
			// If the parent has a last child, make the last child's next this
			// child, and this child's previous that other child.
			if (parent.getLastChildId() != null) {
				final Entry lastChild = getEntryById(parent.getLastChildId());
				lastChild.setNextSiblingId(child.getId());
				child.setPreviousSiblingId(parent.getLastChildId());
			}

			// Make the parent's last child this child.
			parent.setLastChildId(child.getId());
		} else {
			final Entry next = getEntryById(previousSibling.getNextSiblingId());
			child.setPreviousSiblingId(previousSibling.getId());
			child.setNextSiblingId(previousSibling.getNextSiblingId());
			previousSibling.setNextSiblingId(child.getId());
			if (next != null) {
				next.setPreviousSiblingId(child.getId());
			} else {
				// Make the parent's last child this child.
				parent.setLastChildId(child.getId());
			}
		}

		// Create the relationship.
		child.setParentId(parent.getId());

		return true;
	}

	/** Stores temporary information about a parent's children during user backup restoration. */
	static class ChildrenInfo {
		HashMap<String, String> restoredNextToRealPreviousIds = new HashMap<String, String>();
		String lastRealId = null;
		int count = 0;
	}

	/** Helper method. Returns the children of a restored entry in order. */
	private LinkedList<String> getRealChildIdsInOrderForUserRestore(
			ChildrenInfo childrenInfo,
			HashMap<String, String> realEntryIdToRestoredEntryId, Errors errors) {
		LinkedList<String> realChildIdsInOrder = new LinkedList<String>();
		String realCurrentId = childrenInfo.lastRealId;

		if (realCurrentId == null) {
			Errors.add(errors, errorMessages.errorNoLastWasFound());
			return null;
		}

		childrenInfo.restoredNextToRealPreviousIds.remove(null);
		realChildIdsInOrder.addFirst(realCurrentId);

		int i = 0, numChildren = childrenInfo.restoredNextToRealPreviousIds
				.entrySet().size();
		for (; i < numChildren; ++i) {
			String restoredCurrentId = realEntryIdToRestoredEntryId
					.get(realCurrentId);
			if (restoredCurrentId == null) {
				Errors.add(errors,
						errorMessages.errorRestoredIdWasNotFound(realCurrentId));
				return null;
			}

			realCurrentId = childrenInfo.restoredNextToRealPreviousIds
					.get(restoredCurrentId);
			if (realCurrentId == null) {
				Errors.add(errors,
						errorMessages.errorRealIdWasNotFound(restoredCurrentId));
				return null;
			}

			// Eliminate the potential for duplicates.
			childrenInfo.restoredNextToRealPreviousIds
					.remove(restoredCurrentId);

			realChildIdsInOrder.addFirst(realCurrentId);
		}

		return realChildIdsInOrder;
	}

	/**
	 * API method. This method starts its own transactions. Restores a file of notes
	 * and creates a notebook for those notes.
	 */
	public boolean restoreMsWordListFormatForUser(String userId,
			InputStreamReader streamReader, boolean isAdmin,
			Errors errors) {
		boolean result = reallyRestoreMsWordListFormatForUser(userId, streamReader, isAdmin, errors);
		if (!result) {
			rollback();
		} else {
			commit();
		}

		return result;
	}
	
	/** Helper method. Does all the real work for restoreMsWordListFormatForUser(). */
	public boolean reallyRestoreMsWordListFormatForUser(String userId,
			InputStreamReader streamReader, boolean isAdmin,
			Errors errors) {

		if (userId == null) {
			Errors.add(errors, errorMessages.errorsUserIdIsNull());
			return false;
		}

		if (streamReader == null) {
			Errors.add(errors, errorMessages.errorsTheInputStreamReaderIsNull());
			return false;
		}
		
		BufferedReader bf = new BufferedReader(streamReader);
		
		final User user = getUserById(userId);
		if (user == null) {
			return false;
		}

		boolean createdAnyChildren = false;
		try {
			final long now = System.currentTimeMillis();
			Entry notebook = createEntryNoteBook(user, "Restored Notebook", now,
					null, null, false, false, false, isAdmin, false, errors);
			if(notebook == null) {
				return false;
			}
			
			Entry root = getEntryById(notebook.getRootId());

			ArrayList<Entry> parents = new ArrayList<Entry>();
			HashMap<String, Integer> bulletToDepth = new HashMap<String, Integer>();
			String line = null;
			Integer previousDepth = 0;
			parents.add(root);
			while ((line = bf.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				
				String note = line;
				Integer depth = 1;
				if (line.length() > 1 && line.charAt(1) == '\t') {
					String bullet = line.substring(0, 1);
					note = line.substring(2);
					
					depth = bulletToDepth.get(bullet);
					if (depth == null) {
						depth = new Integer(bulletToDepth.size() + 1);
						bulletToDepth.put(bullet, depth);
					}
					
					for (int i = parents.size(); i > depth.intValue(); --i) {
						parents.remove(i - 1);
					}
					depth = new Integer(parents.size() + 1);
				} else {
					previousDepth = 0;
					while (parents.size() > 1) {
						parents.remove(parents.size() - 1);
					}
				}

				if (parents.isEmpty()) {
					return false;
				}
				
				Entry parent = parents.get(parents.size() - 1);
				
				Entry entry = createSimpleEntry(user, note, now,
								parent.getId(), TreeRelType.Parent,
								false, false,
								false, isAdmin, Constants.note, errors, null);
				if (entry == null) {
					return false;
				}
				
				if (previousDepth.intValue() != depth.intValue()) {
					parents.add(entry);
				} else {
					parents.set(parents.size() - 1, entry);
				}
				
				createdAnyChildren = true;
			}
		} catch (IOException e) {
			Errors.add(errors, errorMessages.errorProblemReadingInput());
		}
		
		return createdAnyChildren;
	}
	
	/**
	 * API method. This method starts its own transactions. Restores a JSON stream of a
	 * user's entries and sources.
	 */
	public boolean restoreJsonForUser(String userId,
			InputStreamReader streamReader, boolean reuseIds, boolean isAdmin,
			Errors errors) {
		boolean result = reallyRestoreJsonForUser(userId, streamReader,
				reuseIds, isAdmin, errors);
		if (!result) {
			rollback();
		} else {
			commit();
		}

		return result;
	}

	/** Helper method. Does all the real work for restoreJsonForUser(). */
	public boolean reallyRestoreJsonForUser(String userId,
			InputStreamReader streamReader, boolean reuseIds, boolean isAdmin,
			Errors errors) {

		if (userId == null) {
			Errors.add(errors, errorMessages.errorsUserIdIsNull());
			return false;
		}

		if (streamReader == null) {
			Errors.add(errors, errorMessages.errorsTheInputStreamReaderIsNull());
			return false;
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNodeHelper json = new JsonNodeHelper(
					mapper.readTree(streamReader));

			// Save the restored IDs for later.
			final HashMap<String, String> restoredEntryIdToRealEntryId = new HashMap<String, String>();
			final HashMap<String, String> realEntryIdToRestoredEntryId = new HashMap<String, String>();
			final HashMap<String, String> entryIdToParentRestoredEntryId = new HashMap<String, String>();
			final HashMap<String, String> entryIdToSourceRestoredEntryId = new HashMap<String, String>();
			final HashMap<String, String> entryIdToRootRestoredEntryId = new HashMap<String, String>();
			final HashMap<String, ChildrenInfo> restoredEntryIdToChildren = new HashMap<String, ChildrenInfo>();
			final HashSet<String> rootRestoredEntryIds = new HashSet<String>();

			JsonNodeHelper[] entries = json.getJsonArray("entries");
			if (entries != null) {
				for (JsonNodeHelper jsonEntry : entries) {
					final User user = getUserById(userId);
					if (user == null) {
						return false;
					}

					final String sourceId = jsonEntry.getString("sourceId");
					final String type = jsonEntry
							.getString(DbLogic.Constants.type);
					final String id = jsonEntry.getString(DbLogic.Constants.id);
					final String quotation = jsonEntry
							.getString(DbLogic.Constants.quotation);
					final String note = jsonEntry
							.getString(DbLogic.Constants.note);
					
					Long createTime = jsonEntry
							.getLong(DbLogic.Constants.createTime);
					if (createTime == null) {
						createTime = new Long(System.currentTimeMillis());
					}
					
					Long modTime = jsonEntry.getLong(DbLogic.Constants.modTime);
					if (modTime == null) {
						modTime = createTime;
					}
					
					final String nextSiblingId = jsonEntry
							.getString(DbLogic.Constants.nextSiblingId);
					final String parentId = jsonEntry
							.getString(DbLogic.Constants.parentId);
					final boolean isPublic = jsonEntry
							.getBoolean(DbLogic.Constants.isPublic);
					final String url = jsonEntry
							.getString(DbLogic.Constants.url);
					final String rootId = jsonEntry
							.getString(DbLogic.Constants.rootId);

					if (type == null) {
						Errors.add(errors, errorMessages
								.errorTheTypeOfTheEntryIsNotAllowed(id));
						return false;
					}

					if (type.equals(Constants.note)
							&& !EntryAttributeValidator.isNoteValid(note)) {
						Errors.add(errors, errorMessages.errorNoteIsInvalid(id));
						return false;
					}

					if (type.equals(Constants.notebook)
							&& !EntryAttributeValidator
									.isNotebookTitleValid(note)) {
						Errors.add(errors, errorMessages.errorNoteIsInvalid(id));
						return false;
					}

					if (!EntryAttributeValidator.isQuotationValid(quotation)) {
						Errors.add(errors,
								errorMessages.errorQuotationIsInvalid(id));
						return false;
					}

					if (!EntryAttributeValidator.isUrlValid(url)) {
						Errors.add(errors, errorMessages.errorUrlIsInvalid(id));
						return false;
					}

					if (type.equals(Constants.source)
							&& !EntryAttributeValidator
									.isSourceTitleValid(note)) {
						Errors.add(errors,
								errorMessages.errorTitleIsInvalid(id));
						return false;
					}

					if (!type.equals(Constants.quotation) && sourceId != null) {
						Errors.add(errors, errorMessages
								.errorOnlyQuotationsMayHaveASource(id));
						return false;
					}

					if (!type.equals(Constants.quotation) && quotation != null) {
						Errors.add(errors, errorMessages
								.errorOnlyQuotationsMayHaveAQuotation(id));
						return false;
					}

					if (!type.equals(Constants.source) && url != null) {
						Errors.add(errors,
								errorMessages.errorOnlySourcesMayHaveAUrl(id));
						return false;
					}

					if (!type.equals(Constants.notebook) && rootId != null) {
						Errors.add(errors, errorMessages
								.errorOnlyNotebooksMayHaveARoot(id));
						return false;
					}

					if (type.equals(Constants.notebook) && rootId == null) {
						Errors.add(errors,
								errorMessages.errorNotebooksMustHaveARootId(id));
						return false;
					}

					if (type.equals(Constants.tableofcontents)) {
						Errors.add(errors, errorMessages
								.errorTableOfContentsMayNotBeRestored(id));
						return false;
					}

					if ((type.equals(Constants.root) || type
							.equals(Constants.tableofcontents))
							&& parentId != null) {
						Errors.add(
								errors,
								errorMessages
										.errorRootsAndTableOfContentsCanNotBeCreatedWithARelationship(id));
						return false;
					}

					if (!(type.equals(Constants.root) || type
							.equals(Constants.notebook) || type
							.equals(Constants.source) || type
							.equals(Constants.quotation)) && parentId == null) {
						Errors.add(
								errors,
								errorMessages
										.errorOnlyRootsNotebooksSourcesAndQuotationsCanBeCreatedWithOutAParent(id));
						return false;
					}

					// Reuse IDs if possible.
					String newRealId = null;
					if (reuseIds && getEntryById(id) == null) {
						newRealId = id;
					}

					Entry entry = null;
					if (type.equals(Constants.source)) {
						// Create the entry.
						entry = updateOrCreateSource(user, newRealId, url,
								note, modTime, createTime, isAdmin, errors);
						if (entry == null) {
							return false;
						}
					} else {
						// Create the entry.
						entry = createRawEntry(user, null, type, newRealId,
								quotation, note, modTime, createTime, isPublic,
								isAdmin, errors);
						if (entry == null) {
							return false;
						}

						if (sourceId != null) {
							entryIdToSourceRestoredEntryId.put(entry.getId(),
									sourceId);
						}
					}

					if (type.equals(Constants.root)) {
						rootRestoredEntryIds.add(id);
					}

					// Save the restored entry IDs for later.
					if (id != null) {
						if (restoredEntryIdToRealEntryId.containsKey(id)) {
							Errors.add(errors,
									errorMessages.errorDuplicateId(id));
							return false;
						}

						restoredEntryIdToRealEntryId.put(id, entry.getId());
						realEntryIdToRestoredEntryId.put(entry.getId(), id);
					}

					if (parentId != null) {
						entryIdToParentRestoredEntryId.put(entry.getId(),
								parentId);

						ChildrenInfo parentsChildren = restoredEntryIdToChildren
								.get(parentId);
						if (parentsChildren == null) {
							parentsChildren = new ChildrenInfo();
							restoredEntryIdToChildren.put(parentId,
									parentsChildren);
						}

						parentsChildren.restoredNextToRealPreviousIds.put(
								nextSiblingId, entry.getId());
						++parentsChildren.count;

						if (nextSiblingId == null)
							parentsChildren.lastRealId = entry.getId();
					}

					if (rootId != null) {
						entryIdToRootRestoredEntryId.put(entry.getId(), rootId);
					}
				}
			}

			// Now create the parent relationships.
			for (final Map.Entry<String, String> entry : entryIdToParentRestoredEntryId
					.entrySet()) {
				final User user = getUserById(userId);
				if (user == null) {
					return false;
				}

				final String childId = entry.getKey();
				final String restoredParentId = entry.getValue();
				final String parentId = restoredEntryIdToRealEntryId
						.get(restoredParentId);
				if (parentId == null) {
					Errors.add(errors, errorMessages
							.errorParentIdWasNotFound(restoredParentId));
					return false;
				}

				final Entry child = getEntryById(childId);
				final Entry parent = getEntryById(parentId);
				if (!verifyTypesForParentChildRelationship(child.getType(),
						parent.getType(),
						realEntryIdToRestoredEntryId.get(childId), errors)) {
					return false;
				}

				if (!createParentChildRelationship(user, parent, child, null,
						isAdmin, errors)) {
					return false;
				}
			}

			// Now create the source relationships.
			for (final Map.Entry<String, String> mapEntry : entryIdToSourceRestoredEntryId
					.entrySet()) {
				final User user = getUserById(userId);
				if (user == null) {
					return false;
				}

				final String entryId = mapEntry.getKey();
				final String restoredSourceId = mapEntry.getValue();
				final String sourceId = restoredEntryIdToRealEntryId
						.get(restoredSourceId);
				if (sourceId == null) {
					Errors.add(errors, errorMessages
							.errorSourceIdWasNotFound(restoredSourceId));
					return false;
				}

				final Entry source = getEntryById(sourceId);
				if (!source.isSource()) {
					Errors.add(errors, errorMessages
							.errorSourceIdWasNotASource(restoredSourceId));
					return false;
				}

				final Entry entry = getEntryById(entryId);
				entry.setSourceId(sourceId);
			}

			// Now create the sibling relationships.
			for (final Map.Entry<String, ChildrenInfo> mapEntry : restoredEntryIdToChildren
					.entrySet()) {
				final ChildrenInfo childrenInfo = mapEntry.getValue();
				if (childrenInfo.count == childrenInfo.restoredNextToRealPreviousIds
						.size()) {
					LinkedList<String> sortedRealChildIds = getRealChildIdsInOrderForUserRestore(
							childrenInfo, realEntryIdToRestoredEntryId, errors);
					if (sortedRealChildIds == null) {
						return false;
					}

					for (String realChildId : sortedRealChildIds) {
						final User user = getUserById(userId);
						if (user == null) {
							return false;
						}

						final Entry entry = getEntryById(realChildId);
						if (!makeEntryLastChild(user, entry, errors)) {
							return false;
						}
					}
				}
			}

			// Now create the root relationships.
			for (final Map.Entry<String, String> mapEntry : entryIdToRootRestoredEntryId
					.entrySet()) {
				final User user = getUserById(userId);
				if (user == null) {
					return false;
				}

				final String notebookId = mapEntry.getKey();
				final String restoredRootId = mapEntry.getValue();
				final String rootId = restoredEntryIdToRealEntryId
						.get(restoredRootId);
				if (rootId == null) {
					Errors.add(errors, errorMessages
							.errorRootIdWasNotFound(restoredRootId));
					return false;
				}

				final Entry notebook = getEntryById(notebookId);
				final Entry root = getEntryById(rootId);
				if (!root.getType().equals(Constants.root)) {
					Errors.add(errors, errorMessages
							.errorRootIdWasNotARoot(restoredRootId));
					return false;
				}

				rootRestoredEntryIds.remove(restoredRootId);
				notebook.setRootId(rootId);
				root.setNotebookId(notebookId);
			}

			if (!rootRestoredEntryIds.isEmpty()) {
				Errors.add(errors, errorMessages.errorNotAllRootsHadNotebooks());
				return false;
			}
		} catch (final IOException e) {
			Errors.add(errors, errorMessages.errorJson());
			return false;
		}

		return true;
	}

	/**
	 * API method. Creates a backup in JSON for a user's entries and sources.
	 */
	public void backupJsonForUser(User user, StringBuilder result)
			throws IOException {
		result.append("{\n");
		result.append("\"entries\": [");

		// Allow the user to be null in case the user has not created
		// anything yet.
		if (user != null) {
			boolean first = true;
			List<?> entries = getEntriesByUserId(user.getId());
			for (Object entryUncasted : entries) {
				Entry entry = (Entry) entryUncasted;

				// Do not backup the table of contents since when this file is
				// restored
				// there will already be a TOC.
				if (entry.getType().equals(DbLogic.Constants.tableofcontents)) {
					continue;
				}

				if (!first) {
					result.append(",");
				} else {
					first = false;
				}

				result.append("\n{\n");
				boolean addedAnyYet = false;

				// Do not backup parent or next IDs for direct children of the
				// table of contents, since the TOC
				// is not backed up.
				String parentId = entry.getParentId();
				if (parentId != null
						&& !parentId.equals(user.getTableOfContentsId())) {
					addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
							entry.getParentId(), addedAnyYet,
							DbLogic.Constants.parentId);
					addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
							entry.getNextSiblingId(), addedAnyYet,
							DbLogic.Constants.nextSiblingId);
				}

				addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
						entry.getQuotation(), addedAnyYet,
						DbLogic.Constants.quotation);
				addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
						entry.getNoteOrTitle(), addedAnyYet, DbLogic.Constants.note);
				addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
						entry.getModTime(), addedAnyYet,
						DbLogic.Constants.modTime);
				addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
						entry.getIsPublic(), addedAnyYet,
						DbLogic.Constants.isPublic);
				addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
						entry.getCreateTime(), addedAnyYet,
						DbLogic.Constants.createTime);
				addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
						entry.getId(), addedAnyYet, DbLogic.Constants.id);
				addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
						entry.getType(), addedAnyYet, DbLogic.Constants.type);
				addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
						entry.getSourceId(), addedAnyYet,
						DbLogic.Constants.sourceId);
				addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
						entry.getSourceUrl(), addedAnyYet,
						DbLogic.Constants.url);
				addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
						entry.getRootId(), addedAnyYet,
						DbLogic.Constants.rootId);
				result.append("\n}");
			}
		}

		result.append("\n]\n");
		result.append("}\n");
	}

	/**
	 * Helper method. Makes the entry the last child of its parent. The user's
	 * entitlements must be checked before calling this method.
	 */
	private boolean makeEntryLastChild(User user, Entry entry, Errors errors) {
		if (entry == null) {
			Errors.add(errors, errorMessages.errorEntryIsNull());
			return false;
		}

		if (user == null) {
			Errors.add(errors, errorMessages.errorUserIsNull());
			return false;
		}

		if (!entry.hasParentId()) {
			Errors.add(errors, errorMessages.errorPreviousHasNoParent());
			return false;
		}

		Entry parent = getEntryById(entry.getParentId());

		if (parent.getLastChildId("").equals(entry.getId()))
			return true;

		snipOutEntry(entry, parent, false);

		final String parentsLastChildId = parent.getLastChildId();
		final Entry parentsLastChild = getEntryById(parentsLastChildId);
		insertEntryAfter(entry, parentsLastChild, false);

		return true;
	}

	/**
	 * Helper method. Removes an entry from its parent and siblings, but not
	 * from its children. The user's entitlements must be checked before calling
	 * this method.
	 */
	private void snipOutEntry(Entry entry, final Entry parent,
			boolean removeParentRelationship) {
		final String previousId = entry.getPreviousSiblingId();
		if (previousId != null) {
			final Entry previous = getEntryById(previousId);
			previous.setNextSiblingId(entry.getNextSiblingId());
		} else {
			parent.setFirstChildId(entry.getNextSiblingId());
		}

		final String nextId = entry.getNextSiblingId();
		if (nextId != null) {
			final Entry next = getEntryById(nextId);
			next.setPreviousSiblingId(previousId);
		} else {
			parent.setLastChildId(previousId);
		}
	}

	/**
	 * Helper method. Inserts an entry after a sibling. The user's entitlements
	 * must be checked before calling this method.
	 */
	private void insertEntryAfter(Entry entry, Entry previousSibling,
			boolean createParentRelationship) {
		final String previousSiblingParentId = previousSibling.getParentId();
		final Entry previousSiblingParent = getEntryById(previousSiblingParentId);

		if (createParentRelationship) {
			entry.setParentId(previousSiblingParentId);
		}

		entry.setPreviousSiblingId(previousSibling.getId());
		entry.setNextSiblingId(previousSibling.getNextSiblingId());

		if (previousSibling.getNextSiblingId() != null) {
			final Entry previousSiblingsNext = getEntryById(previousSibling
					.getNextSiblingId());
			previousSiblingsNext.setPreviousSiblingId(entry.getId());
		} else {
			previousSiblingParent.setLastChildId(entry.getId());
		}

		previousSibling.setNextSiblingId(entry.getId());
	}

	/**
	 * Helper method. Inserts an entry before a sibling. The user's entitlements
	 * must be checked before calling this method.
	 */
	private void insertEntryBefore(Entry entry, Entry nextSibling,
			boolean createParentRelationship) {
		final String nextSiblingParentId = nextSibling.getParentId();
		final Entry nextSiblingParent = getEntryById(nextSiblingParentId);

		if (createParentRelationship) {
			entry.setParentId(nextSiblingParentId);
		}

		entry.setPreviousSiblingId(nextSibling.getPreviousSiblingId());
		entry.setNextSiblingId(nextSibling.getId());

		if (nextSibling.getPreviousSiblingId() != null) {
			final Entry nextSiblingsPrevious = getEntryById(nextSibling
					.getPreviousSiblingId());
			nextSiblingsPrevious.setNextSiblingId(entry.getId());
		} else {
			nextSiblingParent.setFirstChildId(entry.getId());
		}

		nextSibling.setPreviousSiblingId(entry.getId());
	}

	/**
	 * API method. Returns true if `descendant` is a descendant of
	 * `potentialAncestor`.
	 */
	public boolean isEntryADescendantOfAncestor(Entry descendant,
			Entry potentialAncestor) {
		while (true) {
			if (descendant.getId().equals(potentialAncestor.getId())) {
				return true;
			}

			if (!descendant.hasParentId()) {
				break;
			}

			descendant = getEntryById(descendant.getParentId());
		}

		return false;
	}

	/**
	 * API method. Makes an entry a sibling of another.
	 */
	public boolean makeEntrySiblingOfAnother(User user, Entry sibling,
			Entry moved, boolean justTheEntry, String placement,
			boolean isAdmin, Errors errors) {
		// Basic Validations.
		if (user == null) {
			Errors.add(errors, errorMessages.errorUserIsNull());
			return false;
		}

		if (sibling == null) {
			Errors.add(errors, errorMessages.errorSiblingIsNull());
			return false;
		}

		if (moved == null) {
			Errors.add(errors, errorMessages.errorMovedIsNull());
			return false;
		}

		if (placement == null) {
			Errors.add(errors, errorMessages.errorPlacementIsNull());
			return false;
		}

		if (!placement.equals("previous") && !placement.equals("next")) {
			Errors.add(errors, errorMessages.errorPlacementIsNotValid());
			return false;
		}

		if (!canUserModifyEntry(user, sibling, isAdmin)) {
			Errors.add(errors, errorMessages
					.errorUserIsNotEntitledToModifyTheSiblingEntry());
			return false;
		}

		if (!canUserModifyEntry(user, moved, isAdmin)) {
			Errors.add(errors,
					errorMessages.errorUserIsNotEntitledToModifyTheMovedEntry());
			return false;
		}

		if (!sibling.hasParentId()) {
			Errors.add(errors, errorMessages.errorSiblingHasNoParent());
			return false;
		}

		if (!justTheEntry) {
			if (isEntryADescendantOfAncestor(sibling, moved)) {
				Errors.add(errors,
						errorMessages.errorMovedIsAnAncestorOfTheSibling());
				return false;
			}
		} else {
			// Get children in an order so they can be iterated in order,
			// even after they are turned into notebooks.
			final ArrayList<Entry> movedsChildrenInOrder = getChildrenInOrder(moved);

			// Save this for later.
			final Entry parentOfMoved = getEntryById(moved.getParentId());

			// Now make them notebooks.
			for (final Entry movedsChildTemp : movedsChildrenInOrder) {
				if (!makeNotebookEntry(user, movedsChildTemp, isAdmin, errors)) {
					return false;
				}
			}

			if (parentOfMoved != null) {
				// This works because moved hasn't been moved yet.
				Entry previous = moved;
				for (final Entry movedsChildTemp : movedsChildrenInOrder) {
					if (!createParentChildRelationship(user, parentOfMoved,
							movedsChildTemp, previous, isAdmin, errors)) {
						return false;
					}

					previous = movedsChildTemp;
				}
			}
		}

		final Entry parentOfSibling = getEntryById(sibling.getParentId());

		// Make sure they have the same parent.
		final boolean placementIsNext = placement.equals("next");
		boolean placedWhenReparented = false;
		if (!moved.hasParentId()
				|| !sibling.getParentId().equals(moved.getParentId())) {
			if (moved.getParentId() != null) {
				if (!makeNotebookEntry(user, moved, isAdmin, errors)) {
					return false;
				}
			}

			if (placementIsNext) {
				placedWhenReparented = true;
			}

			if (!createParentChildRelationship(user, parentOfSibling, moved,
					placedWhenReparented ? sibling : null, isAdmin, errors)) {
				return false;
			}
		}

		if (!placedWhenReparented) {
			snipOutEntry(moved, parentOfSibling, false);
			if (placementIsNext) {
				insertEntryAfter(moved, sibling, false);
			} else {
				insertEntryBefore(moved, sibling, false);
			}
		}

		return true;
	}

	/**
	 * API method. Makes an entry a child of another.
	 */
	public boolean makeEntryAChildOfAParent(User user, Entry parent,
			Entry child, boolean justTheEntry, boolean isAdmin, Errors errors) {
		// Basic Validations.
		if (user == null) {
			Errors.add(errors, errorMessages.errorUserIsNull());
			return false;
		}

		if (parent == null) {
			Errors.add(errors, errorMessages.errorParentIsNull());
			return false;
		}

		if (child == null) {
			Errors.add(errors, errorMessages.errorChildIsNull());
			return false;
		}

		if (!canUserModifyEntry(user, parent, isAdmin)) {
			Errors.add(errors, errorMessages
					.errorTheUserIsNotEntitledToModifyTheParentEntry());
			return false;
		}

		if (!canUserModifyEntry(user, child, isAdmin)) {
			Errors.add(errors, errorMessages
					.errorTheUserIsNotEntitledToModifyTheChildEntry());
			return false;
		}

		if (!justTheEntry) {
			if (isEntryADescendantOfAncestor(parent, child)) {
				Errors.add(errors,
						errorMessages.errorChildIsAnAncestorOfTheParent());
				return false;
			}
		} else {
			// Get children in an order so they can be iterated in order,
			// even after they are temporarily made into notebooks.
			final ArrayList<Entry> childsChildrenInOrder = getChildrenInOrder(child);

			// Save this for later.
			final Entry parentOfChild = getEntryById(child.getParentId());

			// Now make them into notebooks .
			for (final Entry childsChildTemp : childsChildrenInOrder) {
				if (!makeNotebookEntry(user, childsChildTemp, isAdmin, errors)) {
					return false;
				}
			}

			if (parentOfChild != null) {
				// This works because the child hasn't been moved yet.
				Entry previous = child;
				for (final Entry childsChildTemp : childsChildrenInOrder) {
					if (!createParentChildRelationship(user, parentOfChild,
							childsChildTemp, previous, isAdmin, errors)) {
						return false;
					}

					previous = childsChildTemp;
				}
			}
		}

		if (!createParentChildRelationship(user, parent, child, null, isAdmin,
				errors)) {
			return false;
		}

		return true;
	}

	/**
	 * API method. Moves an entry before, after, left or right.
	 */
	public boolean moveEntry(User user, Entry entry, String direction,
			boolean isAdmin, Errors errors) {
		// Basic Validations.
		if (direction == null) {
			Errors.add(errors, errorMessages.errorDirectionIsNull());
			return false;
		}

		if (entry == null) {
			Errors.add(errors, errorMessages.errorEntryIsNull());
			return false;
		}

		if (!canUserModifyEntry(user, entry, isAdmin)) {
			Errors.add(errors,
					errorMessages.errorUserIsNotEntitledToMoveTheEntry());
			return false;
		}

		final String parentId = entry.getParentId();
		String parentFirstChildId = null;
		String parentLastChildId = null;
		final Entry parent = getEntryById(parentId);
		if (parent == null) {
			Errors.add(errors,
					errorMessages.errorTheEntryHadNoParentSoItCouldNotBeMoved());
			return false;
		}

		parentFirstChildId = parent.getFirstChildId();
		parentLastChildId = parent.getLastChildId();

		if (direction.equals("before")) {
			// 1 previousPrevious
			// 2 previous
			// 3 entry move this up
			// 4 next

			// 1 next changed
			// 3 prev and next changes
			// 2 prev and next changes
			// 4 prev changed
			final String previousId = entry.getPreviousSiblingId();
			if (previousId == null) {
				Errors.add(errors,
						errorMessages.errorThereIsNoEntryToMoveBefore());
				return false;
			}

			final Entry previous = getEntryById(previousId);

			final String previousPreviousId = previous.getPreviousSiblingId();
			if (previousPreviousId != null) {
				final Entry previousPrevious = getEntryById(previousPreviousId);
				if (previousPrevious != null) {
					previousPrevious.setNextSiblingId(entry.getId());
				}
			}

			final String nextId = entry.getNextSiblingId();
			if (nextId != null) {
				final Entry next = getEntryById(nextId);
				if (next != null) {
					next.setPreviousSiblingId(previousId);
				}
			}

			entry.setPreviousSiblingId(previousPreviousId);

			entry.setNextSiblingId(previousId);

			previous.setPreviousSiblingId(entry.getId());

			previous.setNextSiblingId(nextId);

			if (parentFirstChildId != null && previousId != null
					&& parentFirstChildId.equals(previousId)) {
				parent.setFirstChildId(entry.getId());
			}

			if (parentLastChildId != null
					&& parentLastChildId.equals(entry.getId())) {
				parent.setLastChildId(previousId);
			}

		} else if (direction.equals("after")) {
			// 1 previous
			// 2 entry move this down
			// 3 next
			// 4 nextNext

			// 1 next changed
			// 3 prev and next changes
			// 2 prev and next changes
			// 4 prev changed
			final String nextId = entry.getNextSiblingId();
			if (nextId == null) {
				Errors.add(errors,
						errorMessages.errorThereIsNoEntryToMoveAfter());
				return false;
			}

			final Entry next = getEntryById(nextId);

			final String nextNextId = next.getNextSiblingId();
			if (nextNextId != null) {
				final Entry nextNext = getEntryById(nextNextId);
				if (nextNext != null) {
					nextNext.setPreviousSiblingId(entry.getId());
				}
			}

			final String previousId = entry.getPreviousSiblingId();
			if (previousId != null) {
				final Entry previous = getEntryById(previousId);
				if (previousId != null) {
					previous.setNextSiblingId(nextId);
				}
			}

			entry.setPreviousSiblingId(nextId);

			entry.setNextSiblingId(nextNextId);

			next.setPreviousSiblingId(previousId);

			next.setNextSiblingId(entry.getId());

			if (parentFirstChildId != null
					&& parentFirstChildId.equals(entry.getId())) {
				parent.setFirstChildId(nextId);
			}

			if (parentLastChildId != null && nextId != null
					&& parentLastChildId.equals(nextId)) {
				parent.setLastChildId(entry.getId());
			}
		} else if (direction.equals("left")) {
			// 1 parent
			// 2 entry move this left
			// 3 parent's next

			// 1
			// 2
			// 3

			final String parentParentId = parent.getParentId();
			if (parentParentId == null) {
				Errors.add(errors,
						errorMessages.errorThereIsNoParentOfTheParent());
				return false;
			}

			snipOutEntry(entry, parent, true);
			insertEntryAfter(entry, parent, true);

		} else if (direction.equals("right")) {
			// 1 previous
			// 2 child of previous
			// 3 entry move this right
			// 4 next

			// 1
			// 2
			// 3
			// 4

			final String previousId = entry.getPreviousSiblingId();
			if (previousId == null) {
				Errors.add(errors, errorMessages
						.errorThereIsNoPreviousEntryToUseAsAParent());
				return false;
			}

			snipOutEntry(entry, parent, true);

			final Entry previous = getEntryById(previousId);
			final String previousLastChildId = previous.getLastChildId();
			if (previousLastChildId != null) {
				final Entry previousLastChild = getEntryById(previousLastChildId);
				previousLastChild.setNextSiblingId(entry.getId());
			}

			entry.setPreviousSiblingId(previousLastChildId);
			entry.setNextSiblingId(null);
			previous.setLastChildId(entry.getId());
			if (!previous.hasFirstChildId()) {
				previous.setFirstChildId(entry.getId());
			}
			previous.setLastChildId(entry.getId());

			entry.setParentId(previous.getId());
		} else {
			Errors.add(errors, errorMessages.errorTheDirectionIsInvalid());
			return false;
		}

		return true;
	}

	/**
	 * API method. Deletes the database from disk. If it has been used in the
	 * lifetime of this process it can not be deleted due to design of windows
	 * and java. This is because the DB memory maps files and java unmaps files
	 * in finalizers for security reasons. The finalizers can not be guaranteed
	 * to be run before the next DB is created in the same place. If the
	 * finalizers have not been run, then the files have not been unmapped and
	 * are still on disk. On windows the files will be locked and can not be
	 * deleted. See: http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4724038
	 */
	public boolean deleteDb() {
		if (aDbHasEverBeenCreatedInThisProcess) {
			return false;
		}

		try {
			FileUtils.cleanDirectory(dbDirectory);
		} catch (final IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * API method. Returns an entry for the source indexed by the user and the
	 * url. This method creates it if it didn't already exist.
	 */
	public Entry updateOrCreateSource(User user, String id, String url,
			String title, Long modTime, Long createTime, boolean isAdmin,
			Errors errors) {
		if (user == null) {
			Errors.add(errors, errorMessages.errorUserIsNull());
			return null;
		}

		if (url == null) {
			Errors.add(errors, errorMessages.errorUrlIsNull());
			return null;
		}

		Entry source = getEntryByUserIdAndUrl(user.getId(), url);
		if (source == null) {
			// Basic Validations.

			if (url.isEmpty()) {
				Errors.add(errors, errorMessages.errorUrlIsEmpty());
				return null;
			}

			if (title == null) {
				Errors.add(errors, errorMessages.errorTitleIsNull());
				return null;
			}

			if (title.isEmpty()) {
				Errors.add(errors, errorMessages.errorTitleIsEmpty());
				return null;
			}

			if (modTime == null) {
				Errors.add(errors, errorMessages.errorModTimeIsNull());
				return null;
			}

			if (createTime == null) {
				Errors.add(errors, errorMessages.errorCreateTimeIsNull());
				return null;
			}

			if (id != null && !idGenerator.isIdWellFormed(id)) {
				Errors.add(errors, errorMessages.errorIdIsInvalid());
				return null;
			}

			if (createTime.longValue() > modTime.longValue()) {
				modTime = createTime;
			}

			if (url != null) {
				url = cleanUpText(url);
			}

			if (title != null) {
				title = cleanUpText(title);
			}

			// Create it.
			source = new Entry();
			source.setDb(db);
			if (id == null) {
				id = idGenerator.getAnotherId();
			}

			source.setId(id);
			source.setSourceUrl(url);
			source.setSourceTitle(title);
			source.setCreateTime(createTime);
			source.setType(Constants.source);
			source.setUserId(user.getId());

			db.persistEntry(source);
		} else if (!canUserModifyEntry(user, source, isAdmin)) {
			Errors.add(errors,
					errorMessages.errorUserIsNotEntitledToModifyTheSource());
			return null;
		}

		source.setModTime(modTime);

		return source;
	}

	/**
	 * API method. Returns a root entry.
	 */
	public Entry createRoot(User user, String id, String type,
			String notebookId, Long modTime, Long createTime, Errors errors) {
		if (user == null) {
			Errors.add(errors, errorMessages.errorUserIsNull());
			return null;
		}

		if (modTime == null) {
			Errors.add(errors, errorMessages.errorModTimeIsNull());
			return null;
		}

		if (createTime == null) {
			Errors.add(errors, errorMessages.errorCreateTimeIsNull());
			return null;
		}

		if (id != null && !idGenerator.isIdWellFormed(id)) {
			Errors.add(errors, errorMessages.errorIdIsInvalid());
			return null;
		}

		if (notebookId != null && !idGenerator.isIdWellFormed(notebookId)) {
			Errors.add(errors, errorMessages.errorNotebookIdIsInvalid());
			return null;
		}

		if (createTime.longValue() > modTime.longValue()) {
			modTime = createTime;
		}

		// Create it.
		Entry root = new Entry();
		root.setDb(db);
		if (id == null) {
			id = idGenerator.getAnotherId();
		}

		root.setId(id);
		root.setCreateTime(createTime);
		root.setModTime(modTime);
		root.setType(type);
		root.setUserId(user.getId());
		root.setNotebookId(notebookId);

		db.persistEntry(root);

		return root;
	}

	/**
	 * API method. Change values for the entry entry.
	 */
	public Entry editEntry(User user, String id, String note, String quotation,
			boolean isPublic, Long modTime, boolean isAdmin, Errors errors) {
		// Basic Validations.
		if (modTime == null) {
			Errors.add(errors, errorMessages.errorModTimeIsNull());
			return null;
		}

		if (user == null) {
			Errors.add(errors, errorMessages.errorUserIsNull());
			return null;
		}

		if (id != null && !idGenerator.isIdWellFormed(id)) {
			Errors.add(errors, errorMessages.errorIdIsInvalid());
			return null;
		}

		final Entry entry = getEntryById(id);
		if (entry == null) {
			Errors.add(errors, errorMessages.errorTheEntryCouldNotBeFound());
			return null;
		}

		if (!canUserModifyEntry(user, entry, isAdmin)) {
			Errors.add(errors,
					errorMessages.errorTheUserIsNotEntitledToModifyTheEntry());
			return null;
		}

		if (note != null) {
			note = cleanUpText(note);
		}

		if (entry.isNotebook()) {
			entry.setNotebookTitle(note);
		} else if (entry.isSource()) {
			entry.setSourceTitle(note);
		} else {
			entry.setNote(note);
		}

		entry.setModTime(modTime);
		entry.setIsPublic(isPublic);

		if (entry.getCreateTime() > modTime.longValue()) {
			modTime = entry.getCreateTime();
		}

		if (entry.isQuotation()) {
			if (quotation != null) {
				quotation = cleanUpText(quotation);
				entry.setQuotation(quotation);
			}
		}

		return entry;
	}

	/** Fixes up the string for storing in the database. */
	private String cleanUpText(String value) {
		return value.replace("\r", "").trim();
	}

	/** API method. Shutdown the database. */
	public void shutDown() {
		db.shutDown();
	}

	/**
	 * API method. Returns the children of a entry in the order specified by
	 * their next relationships.
	 */
	public ArrayList<Entry> getChildrenInOrder(Entry entry) {
		// Get all the children.
		final Hashtable<String, Entry> children = new Hashtable<String, Entry>();
		Entry firstChild = null;
		for (Object objectChild : getEntriesByParentId(entry.getId())) {
			Entry child = (Entry) objectChild;
			children.put(child.getId(), child);
			if (!child.hasPreviousSiblingId()) {
				firstChild = child;
			}
		}

		// Put them in an order.
		final ArrayList<Entry> childrenInOrder = new ArrayList<Entry>();
		Entry child = firstChild;
		for (int i = 0; i < children.size(); ++i) {
			if (child == null) {
				break;
			}

			childrenInOrder.add(child);

			if (!child.hasNextSiblingId()) {
				break;
			}

			final String nextId = child.getNextSiblingId();
			child = children.get(nextId);
		}

		return childrenInOrder;
	}

	/** API method. Removes all the contents from the DB. */
	public void clearData() {
		db.clearData();
	}

	/** Commits a transaction if it has been started. */
	public void commit() {
		db.commit();
	}

	/** Rolls back a transaction if it has been started. */
	public void rollback() {
		db.rollback();
	}

	/**
	 * Backs up the contents of the database with CVS extract of each table.
	 * Returns the number of rows extracted or -1 if there was an error.
	 */
	public int doCsvDbBackup(String destination) {
		return db.doCsvBackup(destination);
	}
}
