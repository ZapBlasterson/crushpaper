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

@Entity
@Table(name = "USR", indexes = { @Index(columnList = "userName"),
		@Index(columnList = "modTime DESC") })
@NamedQueries({
		@NamedQuery(name = "User.getById", query = "select u from User u where id = :id"),
		@NamedQuery(name = "User.getByUserName", query = "select u from User u where userName = :userName"),
		@NamedQuery(name = "User.getAll", query = "select u from User u ORDER BY modTime DESC") })
public class User {
	/** The UUID for the user. */
	@Id
	private String id;

	/** The unix time in milliseconds at which the account was last updated. */
	@Column(nullable = false)
	private long modTime;

	/** The unix time in milliseconds at which the account was created. */
	@Column(nullable = false)
	private long createTime;

	/** The user's username. */
	@Column(nullable = false, unique = true)
	private String userName;

	/** The user's hashed password. */
	private String password;

	/** The user's email address. */
	private String email;

	/** Does the user have administrator privileges. */
	private boolean isAdmin;

	/** Has the user's account been closed. */
	private boolean isAccountClosed;

	/** Can the owner of this web site contact the user. */
	private boolean mayContact;

	/** Was the user's account originally created automatically. */
	private boolean wasCreatedAsAnon;

	/** Is the user still using an automatically generated user id. */
	private boolean isAnon;

	/** Was this account created by single user mode. */
	private boolean isSingleUser;

	/** The user's options. */
	private String options;

	/**
	 * UUID of the entry which the user can't see that is the root of the
	 * notebooks tree.
	 */
	private String tableOfContentsId;

	User() {
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

	public void setId(String value) {
		id = value;
	}

	public void setModTime(Long value) {
		modTime = value;
	}

	public void setCreateTime(Long value) {
		createTime = value;
	}

	public void setUserName(String value) {
		userName = value;
	}

	public void setPassword(String value) {
		password = value;
	}

	public void setEmail(String value) {
		email = value;
	}

	public void setIsAdmin(boolean value) {
		isAdmin = value;
	}

	public void setIsAccountClosed(boolean value) {
		isAccountClosed = value;
	}

	public void setMayContact(boolean value) {
		mayContact = value;
	}

	public void setIsSingleUser(boolean value) {
		isSingleUser = value;
	}

	public void setIsAnon(boolean value) {
		isAnon = value;
	}

	public void setWasCreatedAsAnon(boolean value) {
		wasCreatedAsAnon = value;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getPasswordOrBlank() {
		return password == null ? "" : password;
	}

	public String getEmail() {
		return email;
	}

	public String getEmailOrBlank() {
		return email == null ? "" : email;
	}

	public boolean getIsAdmin() {
		return isAdmin;
	}

	public boolean getIsAccountClosed() {
		return isAccountClosed;
	}

	public boolean getIsAnon() {
		return isAnon;
	}

	public boolean getWasCreatedAsAnon() {
		return wasCreatedAsAnon;
	}

	public boolean getMayContact() {
		return mayContact;
	}

	public boolean getIsSingleUser() {
		return isSingleUser;
	}

	public void setOptions(String value) {
		options = value;
	}

	public String getOptions() {
		return options == null ? "{}" : options;
	}

	public String getTableOfContentsId() {
		return tableOfContentsId;
	}

	public void setTableOfContentId(String value) {
		this.tableOfContentsId = value;
	}
}
