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

import java.util.List;

/** This interface is for persisting and querying Users and Entries. */
public interface DbInterface {

	/** Index the entry by the id. */
	public void indexEntryById(String id, Entry entry);

	/** Indexes an entry by parent ID. */
	public void indexByParentId(String parentId, Entry entry);

	/** Indexes an entry by parent ID. */
	public void unindexByParentId(String parentId, Entry entry);

	/**
	 * The application can swap in an alternative ID generator for unit testing.
	 */
	public abstract void setIdGenerator(IdGenerator idGenerator);

	/**
	 * Returns the entry entry if it exists. If the `id` is null or blank then
	 * null is returned.
	 */
	public abstract Entry getEntryById(String id);

	/**
	 * Returns all entries.
	 */
	public abstract List<?> getAllEntries();

	/**
	 * Returns all parentless entries.
	 */
	public abstract List<?> getAllParentlessEntries();

	/**
	 * Returns all entries for the user.
	 */
	public abstract List<?> getEntriesByUserId(String userId);

	/**
	 * Returns all users.
	 */
	public abstract List<?> getAllUsers(int startPosition, int maxResults);

	/**
	 * Returns the entries that are children of the parent. If the `parentId` is
	 * null or blank then null is returned.
	 */
	public abstract List<?> getEntriesByParentId(String parentId);

	/**
	 * Returns true if the table of contents has any notebooks.
	 */
	public abstract boolean doesTableOfContentsHaveAnyNotebooks(
			String tableOfContentsId);

	/** Searches for entries for a user where the query matches the named field. */
	public abstract List<?> searchEntriesForUserHelper(String userId,
			String field, String query, int startPosition, int maxResults);

	/** Returns true if the entry was deleted in this transaction. */
	public abstract boolean wasEntryDeletedInThisTransaction(Entry entry);

	/**
	 * Only getUser() and getOrCreateUser() should call this.
	 */
	public abstract User getUserHelper(String userName);

	/** Marks the entry for persisting. */
	public abstract void persistEntry(Entry entry);

	/** Marks the user for persisting. */
	public abstract void persistUser(User user);

	/** Marks the entry for removal. */
	public abstract void removeEntry(Entry entry);

	/**
	 * Returns the user entry for the id if it exists. If the `id` is null or
	 * invalid then null is returned.
	 */
	public abstract User getUserById(String id);

	/**
	 * Returns all entries for the source.
	 */
	public abstract List<?> getEntriesBySourceId(String sourceId,
			int startPosition, int maxResults);

	/**
	 * Returns the source for the user and url.
	 */
	public abstract Entry getEntryByUserIdAndUrl(String userId, String url);

	/**
	 * Returns the entries for the user and type.
	 */
	public abstract List<?> getEntriesByUserIdAndType(String userId,
			String type, int startPosition, int maxResults);

	/** Removes all the contents from the DB. */
	public abstract void clearData();

	/** Commits a transaction if it has been started. */
	public abstract void commit();

	/** Rolls back a transaction if it has been started. */
	public abstract void rollback();

	/**
	 * Backs up the contents of the database with CVS extract of each table.
	 * Returns the number of rows extracted or -1 if there was an error.
	 */
	public abstract int doCsvBackup(String destination);

	/** Creates the database if needed. */
	public abstract void createDb();

	/** Shuts the database down. */
	public abstract void shutDown();

}