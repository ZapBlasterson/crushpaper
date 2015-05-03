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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * This class is uses JPA/Hibernate/H2 database for persistence. It has a cache
 * of uncommitted data to compensate for JPA/Hibernate.
 */
public class JpaDb implements DbInterface {
	private IdGenerator idGenerator;
	private File dbDirectory;

	public JpaDb(IdGenerator idGenerator, File dbDirectory) {
		this.idGenerator = idGenerator;
		this.dbDirectory = dbDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#indexByParentId(String,
	 * com.crushpaper.Entry)
	 */
	@Override
	public void indexByParentId(String parentId, Entry entry) {
		HashMap<String, HashSet<Entry>> parentIdToEntryCache = getCacheForEntryByParentId();
		HashSet<Entry> newSet = parentIdToEntryCache.get(parentId);
		if (newSet == null) {
			parentIdToEntryCache.put(parentId, newSet = new HashSet<Entry>());
		}

		newSet.add(entry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#unindexByParentId(String,
	 * com.crushpaper.Entry)
	 */
	@Override
	public void unindexByParentId(String parentId, Entry entry) {
		HashMap<String, HashSet<Entry>> parentIdToEntryCache = getCacheForEntryByParentId();
		HashSet<Entry> oldSet = parentIdToEntryCache.get(parentId);
		if (oldSet != null) {
			oldSet.remove(entry);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.crushpaper.DbInterface#setIdGenerator(com.crushpaper.IdGenerator)
	 */
	@Override
	public void indexEntryById(String id, Entry entry) {
		getCacheForEntryById().put(id, entry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.crushpaper.DbInterface#setIdGenerator(com.crushpaper.IdGenerator)
	 */
	@Override
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#getEntryById(java.lang.String)
	 */
	@Override
	public Entry getEntryById(String id) {
		if (id == null || id.isEmpty() || !idGenerator.isIdWellFormed(id)) {
			return null;
		}

		Entry entry = getCacheForEntryById().get(id);
		if (entry == null) {
			entry = (Entry) getFirstOrNull(getOrCreateEntityManager()
					.createNamedQuery("Entry.getById").setParameter("id", id));

			if (entry != null && !wasEntryDeletedInThisTransaction(entry)) {
				entry.index(this);
			}
		}

		return entry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#getAllEntries()
	 */
	@Override
	public List<?> getAllEntries() {
		List<?> dbResults = getOrCreateEntityManager().createNamedQuery(
				"Entry.getAll").getResultList();

		// If there is nothing in the cache just return what is in the DB.
		if (!areAnyEntriesInCache()) {
			for (Object object : dbResults) {
				Entry entry = (Entry) object;
				entry.index(this);
			}
			return dbResults;
		}

		// Index the db results.
		HashSet<Entry> realResults = new HashSet<Entry>();
		for (Object object : dbResults) {
			Entry entry = (Entry) object;
			realResults.add(entry);
			entry.index(this);
		}

		// Add any entries that have not been persisted yet.
		HashMap<String, Entry> entryByIdCache = getCacheForEntryById();
		realResults.addAll(entryByIdCache.values());

		// Remove anything that has been deleted.
		realResults.removeAll(getCacheForDeletedEntries());

		return new ArrayList<Object>(realResults);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#getAllParentlessEntries()
	 */
	@Override
	public List<?> getAllParentlessEntries() {
		List<?> dbResults = getOrCreateEntityManager().createNamedQuery(
				"Entry.getAllParentless").getResultList();

		// If there is nothing in the cache just return what is in the DB.
		if (!areAnyEntriesInCache()) {
			for (Object object : dbResults) {
				Entry entry = (Entry) object;
				entry.index(this);
			}
			return dbResults;
		}

		// Remove any entries that have been modified in the cache to have a
		// parent.
		HashSet<Entry> realResults = new HashSet<Entry>();
		for (Object object : dbResults) {
			Entry entry = (Entry) object;
			if (entry.getParentId() == null) {
				realResults.add(entry);
				entry.index(this);
			}
		}

		// Add any entries that have been modified in the cache to have no
		// parent.
		HashMap<String, HashSet<Entry>> entryByParentIdCache = getCacheForEntryByParentId();
		HashSet<Entry> cachedMatches = entryByParentIdCache.get(null);
		if (cachedMatches != null) {
			realResults.addAll(cachedMatches);
		}

		// Remove anything that has been deleted.
		realResults.removeAll(getCacheForDeletedEntries());

		return new ArrayList<Object>(realResults);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#getEntriesByUserId(java.lang.String)
	 */
	@Override
	public List<?> getEntriesByUserId(String userId) {
		if (userId == null) {
			return new ArrayList<Entry>();
		}

		List<?> dbResults = getOrCreateEntityManager()
				.createNamedQuery("Entry.getByUserId")
				.setParameter("userId", userId).getResultList();

		// If there is nothing in the cache just return what is in the db.
		if (!areAnyEntriesInCache()) {
			for (Object object : dbResults) {
				Entry entry = (Entry) object;
				entry.index(this);
			}

			return dbResults;
		}

		// Remove any entries that have been modified in the cache to have a
		// different userid.
		HashSet<Entry> realResults = new HashSet<Entry>();
		for (Object object : dbResults) {
			Entry entry = (Entry) object;
			if (userId.equals(entry.getUserId())) {
				realResults.add(entry);
				entry.index(this);
			}
		}

		// Add any entries that have been modified in the cache to have the
		// parent.
		HashMap<String, Entry> entryByIdCache = getCacheForEntryById();
		for (Entry entry : entryByIdCache.values()) {
			if (userId.equals(entry.getUserId())) {
				realResults.add(entry);
			}
		}

		// Remove anything that has been deleted.
		realResults.removeAll(getCacheForDeletedEntries());

		return new ArrayList<Object>(realResults);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#getAllUsers(int, int)
	 */
	@Override
	public List<?> getAllUsers(int startPosition, int maxResults) {
		return getOrCreateEntityManager().createNamedQuery("User.getAll")
				.setFirstResult(startPosition).setMaxResults(maxResults)
				.getResultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#getEntriesByParentId(java.lang.String)
	 */
	@Override
	public List<?> getEntriesByParentId(String parentId) {
		if (parentId == null || parentId.isEmpty()
				|| !idGenerator.isIdWellFormed(parentId)) {
			return new ArrayList<Entry>();
		}

		List<?> dbResults = getOrCreateEntityManager()
				.createNamedQuery("Entry.getByParentId")
				.setParameter("parentId", parentId).getResultList();

		// If there is nothing in the cache just return what is in the db.
		if (!areAnyEntriesInCache()) {
			for (Object object : dbResults) {
				Entry entry = (Entry) object;
				entry.index(this);
			}

			return dbResults;
		}

		// Remove any entries that have been modified in the cache to have a
		// different or null parent.
		HashSet<Entry> realResults = new HashSet<Entry>();
		for (Object object : dbResults) {
			Entry entry = (Entry) object;
			if (parentId.equals(entry.getParentId())) {
				realResults.add(entry);
				entry.index(this);
			}
		}

		// Add any entries that have been modified in the cache have the parent.
		HashMap<String, HashSet<Entry>> entryByParentIdCache = getCacheForEntryByParentId();
		HashSet<Entry> cachedMatches = entryByParentIdCache.get(parentId);
		if (cachedMatches != null) {
			realResults.addAll(cachedMatches);
		}

		// Remove anything that has been deleted.
		realResults.removeAll(getCacheForDeletedEntries());

		return new ArrayList<Object>(realResults);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.crushpaper.DbInterface#doesTableOfContentsHaveAnyNotebooks(java.lang
	 * .String)
	 */
	@Override
	public boolean doesTableOfContentsHaveAnyNotebooks(String tableOfContentsId) {
		List<?> dbResults = getOrCreateEntityManager()
				.createNamedQuery("Entry.getByParentId")
				.setParameter("parentId", tableOfContentsId).setMaxResults(1)
				.getResultList();
		return dbResults.size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.crushpaper.DbInterface#searchEntriesForUserHelper(java.lang.String,
	 * java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public List<?> searchEntriesForUserHelper(String userId, String field,
			String query, int startPosition, int maxResults) {
		if (userId == null || userId.isEmpty()
				|| !idGenerator.isIdWellFormed(userId)) {
			return new ArrayList<Entry>();
		}

		if (query == null) {
			return new ArrayList<Entry>();
		}

		if (field == null) {
			return new ArrayList<Entry>();
		}

		FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search
				.getFullTextEntityManager(getOrCreateEntityManager());

		QueryBuilder qb = fullTextEntityManager.getSearchFactory()
				.buildQueryBuilder().forEntity(Entry.class).get();

		org.apache.lucene.search.Query luceneQuery = qb
				.bool()
				.must(qb.keyword().onField(field).matching(query).createQuery())
				.must(new TermQuery(new Term("userId", userId))).createQuery();

		javax.persistence.Query jpaQuery = fullTextEntityManager
				.createFullTextQuery(luceneQuery, Entry.class)
				.setFirstResult(startPosition).setMaxResults(maxResults);

		return jpaQuery.getResultList();
	}

	/**
	 * Returns the first result of the query or null because
	 * Query.getSingleResult() would throw a NoResultException.
	 */
	private Object getFirstOrNull(Query query) {
		List<?> result = query.getResultList();
		if (result == null || result.isEmpty()) {
			return null;
		}

		return result.get(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.crushpaper.DbInterface#wasEntryDeletedInThisTransaction(com.crushpaper
	 * .Entry)
	 */
	@Override
	public boolean wasEntryDeletedInThisTransaction(Entry entry) {
		return getCacheForDeletedEntries().contains(entry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#getUserHelper(java.lang.String)
	 */
	@Override
	public User getUserHelper(String userName) {
		return (User) getFirstOrNull(getOrCreateEntityManager()
				.createNamedQuery("User.getByUserName").setParameter(
						"userName", userName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#persistEntry(com.crushpaper.Entry)
	 */
	@Override
	public void persistEntry(Entry entry) {
		getOrCreateEntityManager().persist(entry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#persistUser(com.crushpaper.User)
	 */
	@Override
	public void persistUser(User user) {
		getOrCreateEntityManager().persist(user);
		getCacheForUserById().put(user.getId(), user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#removeEntry(com.crushpaper.Entry)
	 */
	@Override
	public void removeEntry(Entry entry) {
		// Index it as removed.
		getCacheForDeletedEntries().add(entry);

		// Mark it for removal.
		getOrCreateEntityManager().remove(entry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#getUserById(java.lang.String)
	 */
	@Override
	public User getUserById(String id) {
		if (id == null) {
			return null;
		}

		User user = getCacheForUserById().get(id);
		if (user == null) {
			user = (User) getFirstOrNull(getOrCreateEntityManager()
					.createNamedQuery("User.getById").setParameter("id", id));

			if (user != null) {
				getCacheForUserById().put(id, user);
			}
		}

		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#getEntriesBySourceId(java.lang.String,
	 * int, int)
	 */
	@Override
	public List<?> getEntriesBySourceId(String sourceId, int startPosition,
			int maxResults) {
		if (sourceId == null || sourceId.isEmpty()
				|| !idGenerator.isIdWellFormed(sourceId)) {
			return new ArrayList<Entry>();
		}

		List<?> dbResults = getOrCreateEntityManager()
				.createNamedQuery("Entry.getBySourceId")
				.setParameter("sourceId", sourceId)
				.setFirstResult(startPosition).setMaxResults(maxResults)
				.getResultList();

		// If there is nothing in the cache just return what is in the db.
		if (!areAnyEntriesInCache()) {
			for (Object object : dbResults) {
				Entry entry = (Entry) object;
				entry.index(this);
			}

			return dbResults;
		}

		// Index and add to the results.
		HashSet<Entry> realResults = new HashSet<Entry>();
		for (Object object : dbResults) {
			Entry entry = (Entry) object;
			realResults.add(entry);
			entry.index(this);
		}

		// Remove anything that has been deleted.
		realResults.removeAll(getCacheForDeletedEntries());

		return new ArrayList<Object>(realResults);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#getEntryByUserIdAndUrl(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Entry getEntryByUserIdAndUrl(String userId, String url) {
		return (Entry) getFirstOrNull(getOrCreateEntityManager()
				.createNamedQuery("Entry.getByUserIdAndUrl")
				.setParameter("userId", userId).setParameter("sourceUrl", url));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.crushpaper.DbInterface#getEntriesByUserIdAndType(java.lang.String,
	 * java.lang.String, int, int)
	 */
	@Override
	public List<?> getEntriesByUserIdAndType(String userId, String type,
			int startPosition, int maxResults) {
		return getOrCreateEntityManager()
				.createNamedQuery("Entry.getByUserIdAndType")
				.setParameter("userId", userId).setParameter("type", type)
				.setFirstResult(startPosition).setMaxResults(maxResults)
				.getResultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#clearData()
	 */
	@Override
	public void clearData() {
		getOrCreateEntityManager().createNativeQuery("truncate table USR")
				.executeUpdate();
		getOrCreateEntityManager().createNativeQuery("truncate table ENTRY")
				.executeUpdate();
		final StringBuffer out = new StringBuffer();
		final StringBuffer err = new StringBuffer();
		String indexPath = new File(dbDirectory, "com.crushpaper.Entry")
				.getAbsolutePath();
		CommandLineUtil.removeDirectory(out, err, indexPath);
		commit();
	}

	private static final ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<EntityManager>();

	/** Creates a new entity manager that does not auto commit. */
	private EntityManager createEntityManager() {
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();
		entityManager.setFlushMode(FlushModeType.COMMIT);
		entityManagerThreadLocal.set(entityManager);
		return entityManager;
	}

	/**
	 * Returns an entity manager and creates it if needed. Also recreates it if
	 * it has been spontaneously closed which is a real thing that can happen.
	 * 
	 * @return
	 */
	private EntityManager getOrCreateEntityManager() {
		buildEntityManagerFactory();

		EntityManager entityManager = entityManagerThreadLocal.get();
		if (entityManager == null) {
			entityManager = createEntityManager();
		}

		if (!entityManager.isOpen()) {
			entityManager = createEntityManager();
		}

		EntityTransaction transaction = entityManager.getTransaction();
		if (!transaction.isActive())
			transaction.begin();

		return entityManager;
	}

	/** Returns the entity manager if one exists. It might not even be open. */
	private EntityManager getEntityManager() {
		return entityManagerThreadLocal.get();
	}

	/** Closes an entity manager if it is open. */
	private void closeEntityManager() {
		EntityManager entityManager = getEntityManager();
		if (entityManager != null && entityManager.isOpen()) {
			entityManager.close();
			entityManagerThreadLocal.set(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#commit()
	 */
	@Override
	public void commit() {
		clearCache();

		EntityManager entityManager = getEntityManager();
		if (entityManager != null && entityManager.isOpen()) {
			EntityTransaction transaction = entityManager.getTransaction();
			if (transaction.isActive()) {
				transaction.commit();
			}
		}

		closeEntityManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#rollback()
	 */
	@Override
	public void rollback() {
		clearCache();

		EntityManager entityManager = getEntityManager();
		if (entityManager != null && entityManager.isOpen()) {
			EntityTransaction transaction = entityManager.getTransaction();
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}

		closeEntityManager();
	}

	private static final ThreadLocal<HashMap<String, User>> idToUserCacheThreadLocal = new ThreadLocal<HashMap<String, User>>() {
		@Override
		protected HashMap<String, User> initialValue() {
			return new HashMap<String, User>();
		}
	};

	private static final ThreadLocal<HashSet<Entry>> deletedEntriesCacheThreadLocal = new ThreadLocal<HashSet<Entry>>() {
		@Override
		protected HashSet<Entry> initialValue() {
			return new HashSet<Entry>();
		}
	};

	private static final ThreadLocal<HashMap<String, Entry>> idToEntryCacheThreadLocal = new ThreadLocal<HashMap<String, Entry>>() {
		@Override
		protected HashMap<String, Entry> initialValue() {
			return new HashMap<String, Entry>();
		}
	};

	private static final ThreadLocal<HashMap<String, HashSet<Entry>>> parentIdToEntryCacheThreadLocal = new ThreadLocal<HashMap<String, HashSet<Entry>>>() {
		@Override
		protected HashMap<String, HashSet<Entry>> initialValue() {
			return new HashMap<String, HashSet<Entry>>();
		}
	};

	private boolean areAnyEntriesInCache() {
		return !idToEntryCacheThreadLocal.get().isEmpty()
				|| !deletedEntriesCacheThreadLocal.get().isEmpty();
	}

	private HashMap<String, Entry> getCacheForEntryById() {
		return idToEntryCacheThreadLocal.get();
	}

	private HashMap<String, HashSet<Entry>> getCacheForEntryByParentId() {
		return parentIdToEntryCacheThreadLocal.get();
	}

	private HashSet<Entry> getCacheForDeletedEntries() {
		return deletedEntriesCacheThreadLocal.get();
	}

	private HashMap<String, User> getCacheForUserById() {
		return idToUserCacheThreadLocal.get();
	}

	/**
	 * Clear the cache of records that have been created, modified or queried
	 * during the transaction.
	 */
	private void clearCache() {
		getCacheForEntryById().clear();
		getCacheForEntryByParentId().clear();
		getCacheForDeletedEntries().clear();
		getCacheForUserById().clear();
	}

	EntityManagerFactory entityManagerFactory;

	/**
	 * Create a JPA entity manager factory which is used to create entity
	 * managers which are used to query and commit to the DB.
	 */
	private synchronized void buildEntityManagerFactory() {
		if (entityManagerFactory != null) {
			return;
		}

		registerShutdownHook(this);

		Map<String, Object> configOverrides = new HashMap<String, Object>();
		String dbPath = dbDirectory.getAbsolutePath();
		configOverrides.put("hibernate.search.default.indexBase", dbPath);
		configOverrides.put("hibernate.connection.url", "jdbc:h2:" + dbPath
				+ File.separator + "db" +
				// Make sure that transactions are fully isolated.
				";LOCK_MODE=1" +
				// Make sure the database is not closed if all of the entity
				// managers are closed.
				";DB_CLOSE_DELAY=-1");

		// This string has to match what is in persistence.xml.
		entityManagerFactory = Persistence.createEntityManagerFactory(
				"manager", configOverrides);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#doCsvBackup(java.lang.String)
	 */
	@Override
	public int doCsvBackup(String destination) {
		Object numUserRows = getFirstOrNull(getOrCreateEntityManager()
				.createNativeQuery(
						"CALL CSVWRITE('" + destination
								+ "/usr.csv', 'SELECT * FROM USR')"));
		Object numEntryRows = getFirstOrNull(getOrCreateEntityManager()
				.createNativeQuery(
						"CALL CSVWRITE('" + destination
								+ "/entry.csv', 'SELECT * FROM ENTRY')"));
		if (numUserRows != null && numEntryRows != null) {
			return ((Integer) numUserRows).intValue()
					+ ((Integer) numEntryRows).intValue();
		}

		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#createDb()
	 */
	@Override
	public void createDb() {
		buildEntityManagerFactory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crushpaper.DbInterface#shutDown()
	 */
	@Override
	public void shutDown() {
		// Close caches and connection pools.
		if (entityManagerFactory != null & entityManagerFactory.isOpen()) {
			entityManagerFactory.close();
		}
	}

	private static boolean registeredShutdownHook = false;

	/**
	 * Helper method that requests that the DB is shutdown once and only once
	 * when the application is cleanly shutdown. This is not guaranteed to work.
	 */
	private static void registerShutdownHook(final DbInterface jpaDb) {
		if (registeredShutdownHook) {
			return;
		}

		registeredShutdownHook = true;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				jpaDb.shutDown();
			}
		});
	}
}
