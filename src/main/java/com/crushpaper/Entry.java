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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.TokenizerDef;

import com.crushpaper.DbLogic.Constants;

/** Used to store notes, quotations and sources. */
@Entity
@AnalyzerDef(name = "exacttokenanalyzer", tokenizer = @TokenizerDef(factory = ExactTokenizerFactory.class))
@Indexed
@Table(indexes = { @Index(columnList = "parentId"),
		@Index(columnList = "userId"),
		@Index(columnList = "userId, sourceUrl"),
		@Index(columnList = "userId, type, modTime DESC"),
		@Index(columnList = "sourceId, modTime DESC"), })
@NamedQueries({
		@NamedQuery(name = "Entry.getById", query = "select e from Entry e where id = :id"),
		@NamedQuery(name = "Entry.getByParentId", query = "select e from Entry e where parentId = :parentId"),
		@NamedQuery(name = "Entry.getByUserIdAndUrl", query = "select e from Entry e where userId = :userId and sourceUrl = :sourceUrl"),
		@NamedQuery(name = "Entry.getByUserIdAndType", query = "select e from Entry e where userId = :userId and type = :type ORDER BY modTime DESC"),
		@NamedQuery(name = "Entry.getByUserId", query = "select e from Entry e where userId = :userId"),
		@NamedQuery(name = "Entry.getBySourceId", query = "select e from Entry e where sourceId = :sourceId ORDER BY modTime DESC"),
		@NamedQuery(name = "Entry.getAll", query = "select e from Entry e"),
		@NamedQuery(name = "Entry.getAllParentless", query = "select e from Entry e where parentId = NULL") })
public class Entry {
	/** The unique id for the entry. In production this is a UUID. */
	@Id
	@Column(nullable = false)
	private String id;

	/** This is the UUID of the user that owns the entry. */
	@Column(nullable = false)
	@Field()
	@Analyzer(definition = "exacttokenanalyzer")
	private String userId;

	/**
	 * This is the UUID of the entry that serves as the source of this note if
	 * the note is a quotation. Can be null.
	 */
	private String sourceId;

	/**
	 * This is the Unix time in milliseconds at which the entry was last
	 * modified.
	 */
	@Column(nullable = false)
	private long modTime;

	/** This is the Unix time in milliseconds at which the entry was created. */
	@Column(nullable = false)
	private long createTime;

	/**
	 * This is the UUID of the entry which is a parent of this entry. In
	 * production this is never null. Can be set on entries of very type.
	 */
	private String parentId;

	/** source, quotation, note or notebook. */
	@Column(nullable = false)
	private String type;

	/**
	 * The text of the note, i.e. what the user has written. Set on quotations,
	 * notes, and notebooks.
	 */
	@Field()
	@Column(length = 800 * 50)
	private String note;

	/**
	 * The text of the quotation, i.e. what the user has quoted from a source.
	 * Only set on quotations.
	 */
	@Field()
	@Column(length = 800 * 50)
	private String quotation;

	/**
	 * The text of the source title, i.e. the title of the source document from
	 * which a user has quoted. Only set on sources. The quotation text would be
	 * in another entry.
	 */
	@Field()
	@Column(length = 1024 * 2)
	private String sourceTitle;

	/** The text of the notebook title. */
	@Field()
	@Column(length = 1024)
	private String notebookTitle;

	/**
	 * UUID of the notebook that contains this entry. Only set on the root note
	 * which the user can't see.
	 */
	private String notebookId;

	/**
	 * UUID of the entry which the user can't see that is the root of the
	 * notebook.
	 */
	private String rootId;

	/** Can be set on every type except roots. */
	private String nextSiblingId;

	/** Can be set on every type except roots. */
	private String previousSiblingId;

	/** Can be set on any type. */
	private String firstChildId;

	/** Can be set on any type. */
	private String lastChildId;

	/** Can only be set on notebooks. */
	private boolean isPublic;

	/**
	 * The URL of the source document from which a user has quoted. Only set on
	 * sources. The quotation text would be in another entry.
	 */
	@Column(length = 1024 * 2)
	private String sourceUrl;

	/** A reference to the object which indexes uncommitted records. */
	@Transient
	private DbInterface db;

	Entry() {
	}

	public String getId(String defaultValue) {
		return id;
	}

	public long getModTime() {
		return modTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public String getId() {
		return id;
	}

	public boolean hasId() {
		return id != null;
	}

	/**
	 * Also indexes the entry by this ID and by a null parent ID. Do not call
	 * this more than once. Treat the ID as if it is immutable.
	 */
	public void setId(String value) {
		db.indexEntryById(value, this);
		id = value;

		db.indexByParentId(parentId, this);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String value) {
		userId = value;
	}

	public void setModTime(Long value) {
		modTime = value;
	}

	public void setCreateTime(Long value) {
		createTime = value;
	}

	public boolean isNote() {
		return getType("").equals(Constants.note);
	}

	public boolean isQuotation() {
		return getType("").equals(Constants.quotation);
	}

	public boolean isRoot() {
		return getType("").equals(Constants.root);
	}

	public boolean isNotebook() {
		return getType("").equals(Constants.notebook);
	}

	public boolean isSource() {
		return getType("").equals(Constants.source);
	}

	/** Also unindexes by the old parent ID and reindexed by the new ID. */
	public void setParentId(String value) {
		db.unindexByParentId(parentId, this);

		parentId = value;

		db.indexByParentId(parentId, this);
	}

	public void setNextSiblingId(String value) {
		nextSiblingId = value;
	}

	public void setPreviousSiblingId(String value) {
		previousSiblingId = value;
	}

	public void setQuotation(String value) {
		quotation = value;
	}

	public void setType(String value) {
		type = value;
	}

	public void setNote(String value) {
		note = value;
	}

	public void setFirstChildId(String value) {
		firstChildId = value;
	}

	public void setLastChildId(String value) {
		lastChildId = value;
	}

	public String getParentId(String defaultValue) {
		return parentId == null ? defaultValue : parentId;
	}

	public String getNextSiblingId(String defaultValue) {
		return nextSiblingId == null ? defaultValue : nextSiblingId;
	}

	public String getPreviousSiblingId(String defaultValue) {
		return previousSiblingId == null ? defaultValue : previousSiblingId;
	}

	public String getQuotation(String defaultValue) {
		return quotation == null ? defaultValue : quotation;
	}

	public String getType(String defaultValue) {
		return type == null ? defaultValue : type;
	}

	public String getNote(String defaultValue) {
		return note == null ? defaultValue : note;
	}

	public String getFirstChildId(String defaultValue) {
		return firstChildId == null ? defaultValue : firstChildId;
	}

	public String getLastChildId(String defaultValue) {
		return lastChildId == null ? defaultValue : lastChildId;
	}

	public String getParentId() {
		return parentId;
	}

	public String getNextSiblingId() {
		return nextSiblingId;
	}

	public String getPreviousSiblingId() {
		return previousSiblingId;
	}

	public String getQuotation() {
		return quotation;
	}

	public String getType() {
		return type;
	}

	public String getNote() {
		return note;
	}

	public String getFirstChildId() {
		return firstChildId;
	}

	public String getLastChildId() {
		return lastChildId;
	}

	public boolean hasParentId() {
		return parentId != null;
	}

	public boolean hasNextSiblingId() {
		return nextSiblingId != null;
	}

	public boolean hasPreviousSiblingId() {
		return previousSiblingId != null;
	}

	public boolean hasQuotation() {
		return quotation != null;
	}

	public boolean hasType() {
		return type != null;
	}

	public boolean hasNote() {
		return note != null;
	}

	public boolean hasFirstChildId() {
		return firstChildId != null;
	}

	public boolean hasLastChildId() {
		return lastChildId != null;
	}

	public void setIsPublic(boolean value) {
		isPublic = value;
	}

	public boolean getIsPublic() {
		return isPublic;
	}

	public void setSourceTitle(String value) {
		sourceTitle = value;
	}

	public void setSourceUrl(String value) {
		sourceUrl = value;
	}

	public String getSourceTitle() {
		return sourceTitle;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceId(String value) {
		sourceId = value;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setNotebookTitle(String value) {
		notebookTitle = value;
	}

	public String getNotebookTitle() {
		return notebookTitle;
	}

	public void setDb(DbInterface db) {
		this.db = db;
	}

	/** This is called when an entry is retrieved from the database. */
	public void index(DbInterface db) {
		this.db = db;
		setId(id);
	}

	public String getNotebookId() {
		return notebookId;
	}

	public void setNotebookId(String value) {
		this.notebookId = value;
	}

	public String getRootId() {
		return rootId;
	}

	public void setRootId(String value) {
		this.rootId = value;
	}

	public String getNoteOrTitle() {
		if (note != null)
			return note;

		if (notebookTitle != null)
			return notebookTitle;

		return sourceTitle;
	}

	public String getNoteOrTitle(String defaultValue) {
		if (note != null)
			return note;

		if (notebookTitle != null)
			return notebookTitle;

		if (sourceTitle != null)
			return sourceTitle;

		return defaultValue;
	}
}
