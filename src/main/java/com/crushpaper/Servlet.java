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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.hibernate.search.exception.EmptyQueryException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;

import com.crushpaper.DbLogic.Constants;
import com.crushpaper.DbLogic.TreeRelType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class encapsulates all logic for handling HTTP requests. All logic for
 * enforcing the consistency of the database is in DbLogic.
 * */
public class Servlet extends HttpServlet {
	private static final long serialVersionUID = -1552137926361345398L;

	ServletText servletText = new ServletText();

	Servlet(DbLogic dbLogic, String singleUserName, Boolean allowSelfSignUp,
			Boolean allowSaveIfNotSignedIn, Boolean loopbackIsAdmin,
			Integer httpPort, Integer httpsPort, File keyStorePath,
			String keyStorePassword, String keyManagerPassword,
			File temporaryDirectory, File logDirectory,
			File sessionStoreDirectory, Boolean isOfficialSite,
			String extraHeader) {
		this.dbLogic = dbLogic;
		this.singleUserName = singleUserName != null ? singleUserName
				.toLowerCase() : null;
		this.allowSelfSignUp = allowSelfSignUp;
		this.allowSaveIfNotSignedIn = allowSaveIfNotSignedIn;
		this.loopbackIsAdmin = loopbackIsAdmin;
		this.versionNumber = getVersionNumber();
		this.httpPort = httpPort;
		this.httpsPort = httpsPort;
		this.keyStorePath = keyStorePath;
		this.keyStorePassword = keyStorePassword;
		this.keyManagerPassword = keyManagerPassword;
		this.temporaryDirectory = temporaryDirectory;
		this.logDirectory = logDirectory;
		this.sessionStoreDirectory = sessionStoreDirectory;
		this.isOfficialSite = isOfficialSite;
		this.extraHeader = extraHeader;
	}

	private final String singleUserName;
	private final DbLogic dbLogic;
	private final boolean allowSelfSignUp;
	private final boolean allowSaveIfNotSignedIn;
	private final boolean loopbackIsAdmin;
	private final String versionNumber;
	private final Integer httpPort;
	private final Integer httpsPort;
	private final File keyStorePath;
	private final String keyStorePassword;
	private final String keyManagerPassword;
	private final File temporaryDirectory;
	private final File logDirectory;
	private final File sessionStoreDirectory;
	private Resource helpDirectoryResource;
	private ExposedShutdownHashSessionManager sessionManager;
	private boolean isInJar;
	private final HashMap<String, String> helpMarkdownMap = new HashMap<String, String>();
	final String sessionUserIdAttribute = "uid";
	final private int defaultNoteDisplayDepth = 3;
	final private boolean isOfficialSite;
	final private String extraHeader;

	/** Returns the session ID for the session and creates it if needed. */
	private String getSessionId(RequestAndResponse requestAndResponse) {
		return requestAndResponse.request.getSession(true).getId();
	}

	/** Wraps the request and Response. */
	class RequestAndResponse {
		RequestAndResponse(HttpServletRequest request,
				HttpServletResponse response) {
			this.request = request;
			this.response = response;
		}

		/** Prints the value to the response. */
		public void print(String value) throws IOException {
			response.getWriter().print(value);
		}

		/** Prints the value followed by a line feed to the response. */
		public void println(String value) throws IOException {
			response.getWriter().println(value);
		}

		/** Gets the parameter from the request. */
		public String getParameter(String name) {
			parseParameters();
			final String[] values = parameters.get(name);
			if (values != null && values.length > 0) {
				return values[0];
			}
			return null;
		}

		/** Returns the map of parameters from the request. */
		public Map<String, String[]> getParameterMap() {
			parseParameters();
			return parameters;
		}

		/**
		 * Returns a "parameter" stored after the second slash in the URL.
		 * Returns null if not found.
		 */
		public String getURIParameter() {
			final String uri = overrideUri;
			final int slashPos = uri.indexOf("/", 1);
			if (slashPos == -1) {
				return null;
			}

			int atPos = uri.indexOf("@");
			atPos = atPos == -1 ? uri.length() : atPos;
			return uri.substring(slashPos + 1, atPos);
		}

		/** Returns the logical request URI without query parameters. */
		public String getRequestURI() {
			parseParameters();
			return requestUri;
		}

		public void setOverrideUri(String uri) {
			parametersAlreadyParsed = false;
			overrideUri = uri;
		}

		/**
		 * Parse query parameters which might be embedded in the URI double
		 * encoded after and @ sign.
		 */
		private void parseParameters() {
			if (parametersAlreadyParsed) {
				return;
			}

			parametersAlreadyParsed = true;

			final String uri = overrideUri;
			final int atPos = uri.indexOf("@");
			if (atPos == -1) {
				requestUri = overrideUri;
				parameters = request.getParameterMap();
				return;
			}

			requestUri = uri.substring(0, atPos);
			final String doubleEncodedContent = uri.substring(atPos + 1);

			final Charset utf8 = Charset.forName("UTF-8");
			final String singleEncodedContent = UrlEncoded.decodeString(
					doubleEncodedContent, 0, doubleEncodedContent.length(),
					utf8);

			final MultiMap<String> tempParameters = new MultiMap<String>();
			UrlEncoded.decodeTo(singleEncodedContent, tempParameters, utf8, 10);
			parameters = new HashMap<String, String[]>();
			final Iterator<Map.Entry<String, List<String>>> iter = tempParameters
					.entrySet().iterator();
			while (iter.hasNext()) {
				final Map.Entry<String, List<String>> e = iter.next();
				final String key = e.getKey();
				final List<String> valsList = e.getValue();
				final String[] valsArray = new String[valsList.size()];
				valsList.toArray(valsArray);
				parameters.put(key, valsArray);
			}
		}

		/** Sets the content type of the response to JSON utf8. */
		public void setResponseContentTypeJson() {
			response.setContentType("application/json;charset=utf-8");
		}

		private String overrideUri;
		private String requestUri;
		private boolean parametersAlreadyParsed;
		private Map<String, String[]> parameters;
		public HttpServletRequest request;
		public HttpServletResponse response;
		public boolean isLocalAdmin;
		public boolean skipFooter;
		public boolean skipHeader;
		public boolean justGetTitle;
		public boolean titleAlreadyFormed;
		public StringBuilder totalTitle;
		public boolean wasUserAlreadyStashed;
		public boolean userIsAdmin;
		public boolean userIsAccountClosed;
		public String userOptions;
		public boolean moreThanOneUri;
	}

	/** Get the userId corresponding to the session in the request. */
	public String getEffectiveUserId(RequestAndResponse requestAndResponse) {
		if (isInSingleUserMode()) {
			final User user = dbLogic.getUserByUserName(singleUserName);
			if (user != null) {
				return user.getId();
			}
			return null;
		}

		return (String) requestAndResponse.request.getSession().getAttribute(
				sessionUserIdAttribute);
	}

	/** Returns true if the user is a local admin. */
	private boolean isUserALocalAdmin(RequestAndResponse requestAndResponse) {
		if (loopbackIsAdmin) {
			final String remoteAddr = requestAndResponse.request
					.getRemoteAddr();
			if (remoteAddr != null
					&& (remoteAddr.equals("127.0.0.1") || remoteAddr
							.equals("0:0:0:0:0:0:0:1"))) {
				requestAndResponse.isLocalAdmin = true;
				return true;
			}
		}

		return false;
	}

	/**
	 * Stashes user information in the requestAndResponse to reduce the number
	 * of queries and transactions.
	 */
	private void stashRequestUser(RequestAndResponse requestAndResponse) {
		if (requestAndResponse.wasUserAlreadyStashed) {
			return;
		}

		requestAndResponse.wasUserAlreadyStashed = true;

		final User user = dbLogic
				.getUserById(getEffectiveUserId(requestAndResponse));
		if (user != null) {
			requestAndResponse.userIsAdmin = user.getIsAdmin();
			requestAndResponse.userIsAccountClosed = user.getIsAccountClosed();
			requestAndResponse.userOptions = user.getOptions();
		} else {
			requestAndResponse.userOptions = "{}";
		}
	}

	/** Returns true if the user is an admin. */
	private boolean isUserAnAdmin(RequestAndResponse requestAndResponse) {
		if (loopbackIsAdmin) {
			if (isUserALocalAdmin(requestAndResponse)) {
				return true;
			}
		}

		stashRequestUser(requestAndResponse);

		return requestAndResponse.userIsAdmin
				&& !requestAndResponse.userIsAccountClosed;
	}

	/** Returns true if the user account is closed. */
	private boolean isUsersAccountClosed(RequestAndResponse requestAndResponse) {
		stashRequestUser(requestAndResponse);

		return requestAndResponse.userIsAccountClosed;
	}

	/**
	 * Set request and response defaults. Can be overridden for specific
	 * requests.
	 */
	private void standardResponseStuff(RequestAndResponse requestAndResponse) {
		// Force session ID generation
		getSessionId(requestAndResponse);

		// This is the only way I could find to get Chrome to reload the page
		// when the user hits the back button.
		requestAndResponse.response.setHeader("Cache-control", "no-store");

		// Make this the default.
		requestAndResponse.response.setContentType("text/html;charset=utf-8");
	}

	/** Splits the URI into individual URIs for panes. */
	protected ArrayList<String> splitUris(String uri) {
		ArrayList<String> uris = new ArrayList<String>();
		int start = 0;
		while (true) {
			if (uri.charAt(start) != '/') {
				uris = null;
				break;
			}

			final int secondSlash = uri.indexOf('/', start + 1);
			if (secondSlash == -1) {
				uris = null;
				break;
			}

			boolean isDone = false;
			int thirdSlash = uri.indexOf('/', secondSlash + 1);
			if (thirdSlash == -1) {
				thirdSlash = uri.length();
				isDone = true;
			}

			uris.add(uri.substring(start, thirdSlash));

			if (isDone) {
				break;
			}

			start = thirdSlash;
		}

		return uris;
	}

	/** Route HTTP GET requests. */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			doGetHelper(request, response);
		} catch (final Exception e) {
			logger.log(Level.INFO, "Exception", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		dbLogic.rollback();
	}

	/** Does the real work of doGet(). */
	private void doGetHelper(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		logger.log(Level.INFO, "request: " + request.getRequestURI());

		final RequestAndResponse requestAndResponse = new RequestAndResponse(
				request, response);

		standardResponseStuff(requestAndResponse);

		final String fullUri = request.getRequestURI();
		requestAndResponse.setOverrideUri(fullUri);

		if (fullUri.equals("/")) {
			handleHtmlIndexPage(requestAndResponse);
			return;
		}

		if (fullUri.equals("/robots.txt")) {
			handleRobotsTxt(requestAndResponse);
			return;
		}

		final ArrayList<String> uris = splitUris(fullUri);
		if (uris == null) {
			returnHtml404(requestAndResponse);
			return;
		}

		requestAndResponse.moreThanOneUri = uris.size() > 1;

		// Build the title of a multi pane request.
		if (uris.size() > 1) {
			requestAndResponse.justGetTitle = true;

			for (int i = 0; i < uris.size(); ++i) {
				final String uri = uris.get(i);

				requestAndResponse.setOverrideUri(uri);

				routeSingleGetRequest(requestAndResponse, uri);
			}

			requestAndResponse.justGetTitle = false;
			requestAndResponse.titleAlreadyFormed = true;
		}

		for (int i = 0; i < uris.size(); ++i) {
			final String uri = uris.get(i);

			requestAndResponse.setOverrideUri(uri);

			if (uris.size() > 1) {
				if (i == 0) {
					requestAndResponse.skipFooter = true;
				} else if (i == uris.size() - 1) {
					requestAndResponse.skipHeader = true;
					requestAndResponse.skipFooter = false;
				} else {
					requestAndResponse.skipHeader = true;
				}
			}

			routeSingleGetRequest(requestAndResponse, uri);
		}
	}

	/** Handle requests for robots.txt. */
	private void handleRobotsTxt(RequestAndResponse requestAndResponse)
			throws IOException {
		requestAndResponse.response.setContentType("text/plain;");
		if (isOfficialSite) {
			requestAndResponse.print("User-agent: *\nDisallow:\n");
		} else {
			requestAndResponse.print("User-agent: *\nDisallow: /\n");
		}
	}

	/** Routes a single GET request. */
	private void routeSingleGetRequest(RequestAndResponse requestAndResponse,
			String uri) throws IOException, ServletException {
		if (uri.startsWith("/notebooks/")) {
			handleHtmlShowNotebooks(requestAndResponse);
		} else if (uri.startsWith("/quotations/")) {
			handleHtmlShowQuotations(requestAndResponse);
		} else if (uri.startsWith("/sources/")) {
			handleHtmlShowSources(requestAndResponse);
		} else if (uri.startsWith("/source/")) {
			handleHtmlShowSource(requestAndResponse);
		} else if (uri.startsWith("/notebook/")) {
			handleHtmlShowNotebook(requestAndResponse);
		} else if (uri.startsWith("/search/")) {
			handleHtmlSearch(requestAndResponse);
			// /////////
		} else if (uri.equals("/help/")) {
			handleHtmlBasicHelp(requestAndResponse);
		} else if (uri.equals("/advancedHelp/")) {
			handleHtmlAdvancedHelp(requestAndResponse);
		} else if (uri.startsWith("/help/")) {
			handleHtmlHelp(requestAndResponse);
		} else if (uri.equals("/export/")) {
			handleHtmlExportForm(requestAndResponse);
		} else if (uri.equals("/import/")) {
			handleHtmlImportForm(requestAndResponse);
			// ///////////
		} else if (uri.startsWith("/account/")) {
			handleHtmlShowAccount(requestAndResponse);
		} else if (uri.startsWith("/accounts/")) {
			handleHtmlShowAccounts(requestAndResponse);
		} else if (uri.equals("/shutdown/")) {
			handleHtmlShutdownForm(requestAndResponse);
		} else if (uri.equals("/clear/")) {
			handleHtmlClearForm(requestAndResponse);
		} else if (uri.equals("/onlineBackup/")) {
			handleHtmlOnlineBackupForm(requestAndResponse);
		} else if (uri.equals("/checkForErrors/")) {
			handleHtmlCheckForErrorsForm(requestAndResponse);
		} else if (uri.equals("/backups/")) {
			handleHtmlShowDBBackups(requestAndResponse);
		} else if (uri.equals("/offlineBackup/")) {
			handleHtmlOfflineBackupForm(requestAndResponse);
			// ///////////
		} else if (uri.equals("/noteJson/")) {
			handleJsonShowEntry(requestAndResponse);
		} else if (uri.equals("/noteParentJson/")) {
			handleJsonShowEntryParent(requestAndResponse);
		} else if (uri.equals("/noteChildrenJson/")) {
			handleJsonShowEntryChildren(requestAndResponse);

		} else if (uri.equals("/searchNotesJson/")) {
			handleJsonSearchNotes(requestAndResponse);
		} else if (uri.equals("/newNotebook/")) {
			handleHtmlNewNotebookForm(requestAndResponse);
		} else if (uri.equals("/nothing/")) {
			handleHtmlNothing(requestAndResponse);
		} else if (uri.equals("/couldNotCreateNote/")) {
			handleHtmlCouldNotCreateNote(requestAndResponse);
		} else if (uri.equals("/restoreBackupCommand/")) {
			handleHtmlShowRestoreBackupCommand(requestAndResponse);
		} else if (uri.equals("/signedOut/")) {
			handleHtmlShowSignedOut(requestAndResponse);
		} else if (uri.startsWith("/changePassword/")) {
			handleHtmlChangePassword(requestAndResponse);
		} else if (uri.startsWith("/changeAccount/")) {
			handleHtmlChangeAccount(requestAndResponse);
		} else if (uri.startsWith("/closeAccount/")) {
			handleHtmlCloseAccount(requestAndResponse);
		} else if (uri.equals("/isSignedIn/")) {
			handleJsonIsSignedIn(requestAndResponse);
		} else if (uri.equals("/importFrame/")) {
			handleHtmlImportFrame(requestAndResponse);
		} else {
			returnHtml404(requestAndResponse);
		}
	}

	/** Route HTTP POST requests. */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			doPostHelper(request, response);
		} catch (final Exception e) {
			logger.log(Level.INFO, "Exception", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		dbLogic.rollback();
	}

	/** Does the real work of doPost(). */
	private void doPostHelper(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		logger.log(Level.INFO, "request: " + request.getRequestURI());
		final RequestAndResponse requestAndResponse = new RequestAndResponse(
				request, response);

		standardResponseStuff(requestAndResponse);

		final String uri = request.getRequestURI();

		requestAndResponse.setOverrideUri(uri);

		if (uri.equals("/createQuotationJson")) {
			handleJsonCreateQuotation(requestAndResponse);
		} else if (uri.equals("/makeNotebook")) {
			handleHtmlMakeNotebook(requestAndResponse);
		} else if (uri.equals("/moveNotesJson")) {
			handleJsonMoveNotes(requestAndResponse);
		} else if (uri.equals("/noteOpJson")) {
			handleJsonNoteOp(requestAndResponse);
		} else if (uri.equals("/getNotebookPathJson")) {
			handleJsonGetNotebookPath(requestAndResponse);
		} else if (uri.equals("/makeChildrenJson")) {
			handleJsonMakeChildren(requestAndResponse);
		} else if (uri.equals("/makeSiblingsJson")) {
			handleJsonMakeSiblings(requestAndResponse);
		} else if (uri.equals("/signIn")) {
			handleJsonSignIn(requestAndResponse);
		} else if (uri.equals("/signOut")) {
			handleJsonSignOut(requestAndResponse);
		} else if (uri.equals("/createAccount")) {
			handleJsonCreateAccount(requestAndResponse);
		} else if (uri.startsWith("/doImport/")) {
			handleHtmlDoImport(requestAndResponse);
		} else if (uri.startsWith("/doOfflineBackup/")) {
			handleHtmlDoOfflineBackup(requestAndResponse);
		} else if (uri.startsWith("/doOnlineBackup/")) {
			handleHtmlDoOnlineBackup(requestAndResponse);
		} else if (uri.startsWith("/doClear/")) {
			handleHtmlDoClear(requestAndResponse);
		} else if (uri.startsWith("/doExport/")) {
			handleHtmlDoExport(requestAndResponse);
		} else if (uri.startsWith("/doShutdown/")) {
			handleHtmlDoShutdown(requestAndResponse);
		} else if (uri.startsWith("/doCheckForErrors/")) {
			handleHtmlDoCheckForErrors(requestAndResponse);
		} else if (uri.startsWith("/changePassword/")) {
			handleHtmlChangePassword(requestAndResponse);
		} else if (uri.startsWith("/changeAccount/")) {
			handleHtmlChangeAccount(requestAndResponse);
		} else if (uri.startsWith("/closeAccount/")) {
			handleHtmlCloseAccount(requestAndResponse);
		} else if (uri.equals("/saveOptions")) {
			handleJsonSaveOptions(requestAndResponse);
		} else {
			returnHtml404(requestAndResponse);
		}
	}

	/** Part of the JSON API. Creates a new quotation. */
	private void handleJsonCreateQuotation(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();
		String url;
		String title;
		String quotation;
		String note;
		String sessionId;

		try {
			final JsonNodeHelper json = getJsonNode(requestAndResponse);

			url = json.getString(DbLogic.Constants.url);

			title = json.getString(DbLogic.Constants.title);

			quotation = json.getString(DbLogic.Constants.quotation);

			note = json.getString(DbLogic.Constants.note);

			sessionId = json.getString("sessionId");

		} catch (final IOException e) {
			returnJson400(requestAndResponse, servletText.errorJson());
			return;
		}

		if (!EntryAttributeValidator.isNoteValid(note)) {
			returnJson400(requestAndResponse, servletText.errorNoteIsInvalid());
			return;
		}

		if (!EntryAttributeValidator.isQuotationValid(quotation)) {
			returnJson400(requestAndResponse,
					servletText.errorQuotationIsInvalid());
			return;
		}

		if (!EntryAttributeValidator.isUrlValid(url)) {
			returnJson400(requestAndResponse, servletText.errorUrlIsInvalid());
			return;
		}

		if (!EntryAttributeValidator.isSourceTitleValid(title)) {
			returnJson400(requestAndResponse, servletText.errorTitleIsInvalid());
			return;
		}

		final Errors errors = new Errors();
		try {
			final Long time = new Long(System.currentTimeMillis());

			String userId = null;
			if (sessionManager != null) {
				final HttpSession session = sessionManager
						.getSession(sessionId);
				if (session != null
						&& session.getAttribute(sessionUserIdAttribute) != null) {
					userId = (String) session
							.getAttribute(sessionUserIdAttribute);
				}
			}

			final User user = dbLogic.getUserById(userId);
			if (user == null) {
				returnJson400(requestAndResponse,
						servletText.errorNoAccountFound());
				return;
			}

			if (user.getIsAccountClosed()) {
				returnJson400(requestAndResponse,
						servletText.errorAccountIsClosed());
				return;
			}

			final Entry source = dbLogic.updateOrCreateSource(user, null, url,
					title, time, time, isUserAnAdmin(requestAndResponse),
					errors);
			if (source == null) {
				returnJson400(requestAndResponse, errors);
				return;
			}

			final Entry entry = dbLogic.createEntryQuotation(user, source,
					quotation, note, time, isUserAnAdmin(requestAndResponse),
					errors);
			if (entry == null) {
				returnJson400(requestAndResponse, errors);
				return;
			}

			dbLogic.commit();

			returnJson200(requestAndResponse);
		} catch (final PersistenceException e) {
			logger.log(Level.INFO, "Exception", e);
			returnJson500(requestAndResponse,
					servletText.errorInternalDatabase());
		}
	}

	/** Returns true if the CSRF token is wrong or null. */
	private boolean isTheCsrftWrong(RequestAndResponse requestAndResponse,
			String csrft) {
		if (csrft == null || csrft.isEmpty() || csrft.length() > 100) {
			return true;
		}

		return !csrft.equals(getCsrft(requestAndResponse));
	}

	/** Returns the CSRFT for the session. */
	private String getCsrft(RequestAndResponse requestAndResponse) {
		return getSessionId(requestAndResponse);
	}

	/** Part of the HTML API. Displays the list of basic help. */
	private void handleHtmlBasicHelp(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleHelp();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId("help");

		pageWrapper.addHeader();

		// Prevent the last link from floating off.
		requestAndResponse.print("<table><tr><td>");

		startHelpSection(requestAndResponse, "Start Here");
		addHelpLink(requestAndResponse, "What CrushPaper Is");
		addHelpLink(requestAndResponse, "Why I Created CrushPaper");
		addHelpLink(requestAndResponse, "Why CrushPaper Is Free");
		addHelpLink(requestAndResponse, "User Guide");
		addHelpLink(requestAndResponse, "Search Help");
		addHelpLink(requestAndResponse, "Privacy Policy");
		addHelpLink(requestAndResponse, "Future Enhancements");
		endHelpSection(requestAndResponse);

		requestAndResponse
				.print("<a onclick=\"newPaneForLink(event, '"
						+ servletText.pageTitleHelp()
						+ "', 'help'); return false;\" class=\"nextLink\" href=\"/advancedHelp/\">Help for administrators and code contributors.</a>");
		requestAndResponse.print("</td></tr></table>");

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Displays the list of advanced help. */
	private void handleHtmlAdvancedHelp(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleAdvancedHelp();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId("help");

		pageWrapper.addHeader();

		startHelpSection(requestAndResponse, "For Administrators");
		addHelpLink(requestAndResponse, "Administrator Guide");
		addHelpLink(requestAndResponse, "Build, Install, Configure and Run");
		addHelpLink(requestAndResponse, "System Overview");
		endHelpSection(requestAndResponse);

		startHelpSection(requestAndResponse, "For Contributors");
		addHelpLink(requestAndResponse, "Code Contribution Guidelines");
		addHelpLink(requestAndResponse, "Get Started Coding");
		addHelpLink(requestAndResponse, "Release Process");
		addHelpLink(requestAndResponse, "Testing Strategy");
		addHelpLink(requestAndResponse, "Licenses");
		endHelpSection(requestAndResponse);

		pageWrapper.addFooter();
	}

	/** Adds a section header for some help pages. */
	private void startHelpSection(RequestAndResponse requestAndResponse,
			String name) throws IOException {
		requestAndResponse
				.println("<div class=\"helpListSection\"><div class=\"helpListHeader\">"
						+ name + "</div>");
	}

	/** Adds a section footer for some help pages. */
	private void endHelpSection(RequestAndResponse requestAndResponse)
			throws IOException {
		requestAndResponse.println("</div>");
	}

	/** Adds a link to a help page. */
	private void addHelpLink(RequestAndResponse requestAndResponse, String name)
			throws IOException {
		requestAndResponse
				.println("<div class=\"helpListItem\">&bull; <a onclick=\"replacePaneForLink(event, uiText.pageTitleHelpPage()); return false;\" href=\"/help/"
						+ name.replace(" ", "-") + "\">" + name + "</a></div>");
	}

	/** Returns the markdown for a help item with the specified name. */
	private String getHelpMarkdown(String helpName) {
		// Basic validation.
		if (helpName == null || helpName.isEmpty() || helpName.length() > 50
				|| !StringUtils.isAsciiPrintable(helpName)) {
			return null;
		}

		if (isInJar) {
			if (helpMarkdownMap.containsKey(helpName)) {
				return helpMarkdownMap.get(helpName);
			}
		}

		final String helpMarkeddown = getHelpMarkdownHelper(helpName);
		if (isInJar) {
			helpMarkdownMap.put(helpName, helpMarkeddown);
		}

		return helpMarkeddown;
	}

	/** Helper for getHelpMarkdown(). */
	private String getHelpMarkdownHelper(String helpName) {
		String helpMarkdown = null;
		InputStream inputStream = null;
		try {
			final Resource fileResource = helpDirectoryResource.addPath("/"
					+ helpName + ".md");
			if (fileResource.exists()) {
				inputStream = fileResource.getInputStream();
				helpMarkdown = IOUtils.toString(inputStream, "UTF-8");
			}
		} catch (final Exception e) {
			return null;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (final IOException e) {
				}
			}
		}

		return getMarkdownHtml(helpMarkdown, false, true);
	}

	/** Part of the HTML API. Displays help. */
	private void handleHtmlHelp(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {

		final String helpName = requestAndResponse.getURIParameter();

		final String title = helpName.replace("-", " ");
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final String helpMarkdown = getHelpMarkdown(helpName);
		if (helpMarkdown == null) {
			returnHtml404(requestAndResponse);
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId("help");
		pageWrapper.addHeader();

		requestAndResponse.print(helpMarkdown);

		addCallToAction(requestAndResponse);

		pageWrapper.addFooter();
	}

	/**
	 * Part of the HTML API. Displays a form that enables a user to create a
	 * note.
	 */
	private void handleHtmlNewNotebookForm(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleNewNotebook();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false);
		pageWrapper.addHeader();

		final boolean userIsSignedIn = isUserSignedIn(requestAndResponse);
		if (!userIsSignedIn && !allowSaveIfNotSignedIn) {
			requestAndResponse.print(servletText
					.errorRequiresSignIn(allowSaveIfNotSignedIn));
		} else if (userIsSignedIn && isUsersAccountClosed(requestAndResponse)) {
			requestAndResponse.print(servletText.errorAccountIsClosed());
		} else {
			if (!userIsSignedIn) {
				requestAndResponse.print(servletText
						.sentenceAllowSaveIfNotSignedIn());
			}

			requestAndResponse
					.print("<script type=\"text/javascript\">\n"
							+ "function saveNote() {\n"
							+ "  if(document.getElementById(\"note\").value.trim() == \"\") {\n"
							+ "    setResponseErrorMessage(errorBlankNote(), \"createResponse\");\n"
							+ "  } else {\n"
							+ "    document.getElementById(\"putNote\").submit();\n"
							+ "  }\n" + "}\n" + "</script>");
			requestAndResponse
					.print("<form action=\"/makeNotebook\" id=\"putNote\" method=\"POST\">"
							+ "<table class=\"nopadding\"><tr><td colspan=\"2\">"
							+ "<input type=\"text\" id=\"note\" name=\"note\" placeholder=\""
							+ servletText.labelYourNotebookTitle()
							+ "\" autofocus>"
							+ "</td></tr>"
							+ "<tr><td>"
							+ "<input type=\"checkbox\" name=\"isPublic\" id=\"isPublic\"><label for=\"isPublic\">"
							+ servletText.labelAnyoneCanReadThis()
							+ "</label>"
							+ "<input type=\"hidden\" name=\"csrft\" value=\""
							+ getCsrft(requestAndResponse)
							+ "\">"
							+ "</td></tr>"
							+ "<tr><td><div id=\"createResponse\"></div></td>"
							+ "<td><button id=\"save\" class=\"specialbutton\" onclick=\"saveNote(); return false;\">"
							+ servletText.buttonSave()
							+ "</button></td></tr></table></form>");
		}

		pageWrapper.addFooter();
	}

	/**
	 * Part of the HTML API. Displays a page indicating that a note could not be
	 * created.
	 */
	private void handleHtmlCouldNotCreateNote(
			RequestAndResponse requestAndResponse) throws IOException,
			ServletException {
		final String title = servletText.pageTitleNewNotebook();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId("welcome");
		pageWrapper.addHeader();

		requestAndResponse.print(servletText.errorNoteNotCreated());

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Displays the index. */
	private void handleHtmlIndexPage(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				servletText.pageTitleWelcome(), false).setPaneId("welcome");
		pageWrapper.addHeader();

		final String welcomeText = getHelpMarkdown("What-CrushPaper-Is");
		if (welcomeText != null) {
			requestAndResponse.print(welcomeText);
		}

		addCallToAction(requestAndResponse);

		pageWrapper.addFooter();
	}

	/** Adds the call to action. */
	private void addCallToAction(RequestAndResponse requestAndResponse)
			throws IOException {
		if (!doesUserHaveAnyNotebooks(requestAndResponse)) {
			requestAndResponse.print(servletText
					.callToAction(allowSaveIfNotSignedIn));
		} else {
			requestAndResponse.print(servletText.viewYourNotebooks());
		}
	}

	/** Returns true if the user has any notebooks. */
	private boolean doesUserHaveAnyNotebooks(
			RequestAndResponse requestAndResponse) throws IOException {
		boolean hasNotebooks = false;
		try {
			final String userId = getEffectiveUserId(requestAndResponse);
			if (userId != null) {
				final User user = dbLogic.getUserById(userId);
				if (user != null) {
					hasNotebooks = dbLogic
							.doesTableOfContentsHaveAnyNotebooks(user
									.getTableOfContentsId());
				}
			}

			dbLogic.commit();
		} catch (final PersistenceException e) {
		}

		return hasNotebooks;
	}

	/**
	 * Returns true if the current user can see data for the requested user.
	 * Responsible for printing the error if false.
	 * 
	 * @throws IOException
	 */
	User canUserSeeUsersData(RequestAndResponse requestAndResponse,
			boolean printError) throws IOException {
		final String effectiveUserId = getEffectiveUserId(requestAndResponse);
		final String queryUserId = getURIParameterOrUserId(requestAndResponse);
		final User effectiveUser = dbLogic.getUserById(effectiveUserId);
		final User queryUser = dbLogic.getUserById(queryUserId);
		if (effectiveUser == null) {
			if (printError) {
				requestAndResponse.print(servletText
						.errorRequiresSignIn(allowSaveIfNotSignedIn));
			}
			return null;
		}

		if (effectiveUser.getIsAccountClosed()) {
			if (printError) {
				requestAndResponse.print(servletText.errorAccountIsClosed());
			}
			return null;
		}

		if (queryUser == null) {
			if (printError) {
				requestAndResponse.print(servletText.errorNoAccountFound());
			}
			return null;
		}

		if (queryUser.getUserName().equals(effectiveUser.getUserName())) {
			return queryUser;
		}

		if (!isUserAnAdmin(requestAndResponse)) {
			if (printError) {
				requestAndResponse.print(servletText.errorMayNotSeeList());
			}
			return null;
		}

		return queryUser;
	}

	/** Part of the HTML API. Shows a list of all the user's notebooks. */
	private void handleHtmlShowNotebooks(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String paneId = "notebooks";
		final String defaultTitle = servletText.pageTitleNotebooks();
		final String notFoundMessage = servletText
				.errorNotebooksCouldNotBeFound();
		final String mayNotSeeMessage = servletText.errorMayNotSeeNotebooks();
		final String introMessage = servletText.introTextShowNotebooks(false);
		final String touchIntroMessage = servletText.introTextShowNotebooks(true);
		final String tooltipNewChild = servletText.tooltipNewNotebook();
		final String buttonNewChild = servletText.buttonNewNotebook();
		final String titleIfCanSee = defaultTitle;
		boolean userCanSee = false;
		final User user = canUserSeeUsersData(requestAndResponse, false);

		Entry root = null;
		if (user != null) {
			root = dbLogic.getEntryById(user.getTableOfContentsId());
			userCanSee = true;
		}

		handleHtmlShowEntryTree(requestAndResponse, paneId, defaultTitle,
				notFoundMessage, mayNotSeeMessage, introMessage, touchIntroMessage,
				tooltipNewChild, buttonNewChild, titleIfCanSee, root,
				userCanSee, user, false, "showPopupForCreateNotebook",
				"notebooks", true);
	}

	/**
	 * Adds a title to the response if needed. Returns true if the caller should
	 * not build a page at this point.
	 */
	private boolean addTitle(RequestAndResponse requestAndResponse, String title) {
		if (!requestAndResponse.justGetTitle) {
			return false;
		}

		if (requestAndResponse.totalTitle == null) {
			requestAndResponse.totalTitle = new StringBuilder();
		} else {
			requestAndResponse.totalTitle.append(" | ");
		}

		requestAndResponse.totalTitle.append(title.replace("|", ""));

		return true;
	}

	Random random = new Random();

	/**
	 * Returns a pseudo-random number between min and max, inclusive. The
	 * difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min
	 *            Minimum value
	 * @param max
	 *            Maximum value. Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	synchronized private int randInt(int min, int max) {
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		return random.nextInt((max - min) + 1) + min;
	}

	/**
	 * Part of the HTML API. Creates a note and forwards to an url to display
	 * it.
	 */
	private void handleHtmlMakeNotebook(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String note = requestAndResponse.request
				.getParameter(DbLogic.Constants.note);

		final String isPublicString = requestAndResponse.request
				.getParameter("isPublic");
		final boolean isPublic = isPublicString != null
				&& isPublicString.equals("on");

		final String csrft = requestAndResponse.getParameter("csrft");
		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			requestAndResponse.response.sendRedirect("/couldNotCreateNote");
			return;
		}

		String noteId = null;
		final Errors errors = new Errors();
		try {
			final Long time = new Long(System.currentTimeMillis());

			User user = null;
			final String userId = getEffectiveUserId(requestAndResponse);
			if (userId == null && allowSaveIfNotSignedIn) {
				user = createAnonUser();
				if (user == null) {
					requestAndResponse.response
							.sendRedirect("/couldNotCreateNote");
					return;
				} else {
					mapSessionToUser(requestAndResponse, user.getId());
				}
			} else {
				user = dbLogic.getUserById(userId);

				if (user != null && user.getIsAccountClosed()) {
					user = null;
				}
			}

			if (user == null) {
				requestAndResponse.response.sendRedirect("/couldNotCreateNote");
				return;
			} else if (user.getIsAccountClosed()) {
				requestAndResponse.print(servletText.errorAccountIsClosed());
				return;
			}

			if (!EntryAttributeValidator.isNotebookTitleValid(note)) {
				returnJson400(requestAndResponse,
						servletText.errorNoteIsInvalid());
				return;
			}

			final Entry entry = dbLogic.createEntryNoteBook(user, note, time,
					null, null, false, false, isPublic,
					isUserAnAdmin(requestAndResponse), errors);
			if (entry == null) {
				requestAndResponse.response.sendRedirect("/couldNotCreateNote");
				return;
			}

			noteId = entry.getId();
			dbLogic.commit();
		} catch (final PersistenceException e) {
			requestAndResponse.response.sendRedirect("/couldNotCreateNote");
			return;
		}

		requestAndResponse.response.sendRedirect("/notebook/" + noteId);
	}

	/** Tries a few times to create an anonymous user. */
	private User createAnonUser() {
		for (int i = 0; i < 10; ++i) {
			final User user = dbLogic.createUser("anon"
					+ randInt(1, Integer.MAX_VALUE));

			if (user != null) {
				user.setWasCreatedAsAnon(true);
				user.setIsAnon(true);
				return user;
			}
		}

		return null;
	}

	/** Part of the JSON API. Moves a note up or down. */
	private void handleJsonMoveNotes(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();
		String[] ids;
		String direction;
		String csrft;

		try {
			final JsonNodeHelper json = getJsonNode(requestAndResponse);
			ids = json.getStringArray("ids");
			direction = json.getString("direction");
			csrft = json.getString("csrft");
		} catch (final IOException e) {
			returnJson400(requestAndResponse, servletText.errorJson());
			return;
		}

		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			returnJson400(requestAndResponse,
					servletText.errorRequiresSignIn(false));
			return;
		}

		try {
			final Errors errors = new Errors();

			final User user = dbLogic
					.getUserById(getEffectiveUserId(requestAndResponse));
			if (user == null) {
				returnJson400(requestAndResponse,
						servletText.errorRequiresSignIn(false));
				return;
			}

			if (user.getIsAccountClosed()) {
				returnJson400(requestAndResponse,
						servletText.errorAccountIsClosed());
			}

			final HashSet<String> movedIdsSet = new HashSet<String>();
			for (int i = 0; i < ids.length; ++i) {
				final String id = ids[i];

				if (!dbLogic.getIdGenerator().isIdWellFormed(id)) {
					returnJson400(requestAndResponse,
							servletText.errorIdIsInvalidFormat());
					return;
				}

				final Entry entry = dbLogic.getEntryById(id);

				if (entry == null) {
					returnJson400(requestAndResponse,
							servletText.errorEntryCouldNotBeFound());
					return;
				}

				if (movedIdsSet.contains(id)) {
					returnJson400(requestAndResponse,
							servletText.errorDuplicateEntry());
					return;
				}

				movedIdsSet.add(id);

				if (!dbLogic.moveEntry(user, entry, direction,
						isUserAnAdmin(requestAndResponse), errors)) {
					returnJson400(requestAndResponse, errors);
					return;
				}
			}

			dbLogic.commit();

			returnJson200(requestAndResponse);
		} catch (final PersistenceException e) {
			logger.log(Level.INFO, "Exception", e);
			returnJson500(requestAndResponse,
					servletText.errorInternalDatabase());
		}
	}

	/** Part of the JSON API. Makes entry a child of another one. */
	private void handleJsonMakeChildren(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();
		String targetId;
		String[] movedIds;
		boolean justTheEntry;
		String csrft;

		try {
			final JsonNodeHelper json = getJsonNode(requestAndResponse);
			targetId = json.getString("targetId");
			movedIds = json.getStringArray("movedIds");
			justTheEntry = json.getBoolean("justTheEntry");
			csrft = json.getString("csrft");
		} catch (final IOException e) {
			returnJson400(requestAndResponse, servletText.errorJson());
			return;
		}

		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			returnJson400(requestAndResponse,
					servletText.errorRequiresSignIn(false));
			return;
		}

		if (!dbLogic.getIdGenerator().isIdWellFormed(targetId)) {
			returnJson400(requestAndResponse,
					servletText.errorTargetIdInvalidFormat());
			return;
		}

		try {
			final Errors errors = new Errors();
			final User user = dbLogic
					.getUserById(getEffectiveUserId(requestAndResponse));
			if (user == null) {
				returnJson400(requestAndResponse,
						servletText.errorRequiresSignIn(false));
				return;
			}

			if (user.getIsAccountClosed()) {
				returnJson400(requestAndResponse,
						servletText.errorAccountIsClosed());
			}

			final Entry parent = dbLogic.getEntryById(targetId);
			if (parent == null) {
				returnJson400(requestAndResponse,
						servletText.errorTargetParentCouldNotBeFound());
				return;
			}

			final LinkedList<EntryAndIsFromList> entriesToMove = new LinkedList<EntryAndIsFromList>();
			final String errorMessage = validateEntriesParentsBeforeChildren(
					requestAndResponse, movedIds, entriesToMove);
			if (errorMessage != null) {
				returnJson400(requestAndResponse, errorMessage);
				return;
			}

			final StringBuilder result = new StringBuilder();
			result.append("[");
			boolean isFirst = true;
			for (final EntryAndIsFromList entryToMove : entriesToMove) {
				if (parent.getId().equals(entryToMove.entry.getId())) {
					returnJson400(requestAndResponse,
							servletText.errorTargetAndObjectCanNotBeTheSame());
					return;
				}

				if (!dbLogic.makeEntryAChildOfAParent(user, parent,
						entryToMove.entry, justTheEntry,
						isUserAnAdmin(requestAndResponse), errors)) {
					returnJson400(requestAndResponse, errors);
					return;
				}

				if (entryToMove.isFromList) {
					if (!isFirst) {
						result.append(",");
					}

					result.append("{");
					result.append("\"id\":\"" + entryToMove.entry.getId()
							+ "\",\n");
					addJsonForEntry(result, entryToMove.entry, false, false,
							false, true);
					result.append("}");

					isFirst = false;
				}
			}

			result.append("]");

			dbLogic.commit();

			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			logger.log(Level.INFO, "Exception", e);
			returnJson500(requestAndResponse,
					servletText.errorInternalDatabase());
		}
	}

	static class EntryAndIsFromList {
		EntryAndIsFromList(Entry entry, boolean isFromList) {
			this.entry = entry;
			this.isFromList = isFromList;
		}

		public final Entry entry;
		public final boolean isFromList;
	}

	/**
	 * Validates a list of entries, in that the IDs must be well formed, the
	 * entries must exist and the parents must be placed before the children.
	 * Put the validated Entries in the same order as the IDs in the
	 * validatedEntriesList parameter. Returns null if there was no error. God,
	 * how I long to code in a better language.
	 */
	private String validateEntriesParentsBeforeChildren(
			RequestAndResponse requestAndResponse, String[] idsToValidate,
			LinkedList<EntryAndIsFromList> validatedEntriesList)
			throws ServletException, IOException {
		// Iterate in reverse to validate that parents are moved before
		// children.
		final HashSet<String> validatedIdsSet = new HashSet<String>();
		for (int i = idsToValidate.length - 1; i >= 0; --i) {
			final String[] idToValidateArray = idsToValidate[i].split(":");
			final String idToValidate = idToValidateArray[idToValidateArray.length == 1 ? 0
					: 1];
			final boolean isFromList = idToValidateArray.length != 1;

			if (!dbLogic.getIdGenerator().isIdWellFormed(idToValidate)) {
				return servletText.errorIdIsInvalidFormat();
			}

			final Entry movedEntry = dbLogic.getEntryById(idToValidate);
			if (movedEntry == null) {
				return servletText.errorEntryCouldNotBeFound();
			}

			if (validatedIdsSet.contains(idToValidate)) {
				return servletText.errorDuplicateEntry();
			}

			// Validate that parents are moved before children.
			validatedIdsSet.add(idToValidate);

			final Entry oldParentOfMovedEntry = dbLogic.getEntryById(movedEntry
					.getParentId());

			if (oldParentOfMovedEntry != null
					&& validatedIdsSet.contains(oldParentOfMovedEntry.getId())) {
				servletText.errorParentMustBeMovedBeforeChild();
			}

			validatedEntriesList.addFirst(new EntryAndIsFromList(movedEntry,
					isFromList));
		}

		return null;
	}

	/** Part of the JSON API. Makes entry a sibling of another one. */
	private void handleJsonMakeSiblings(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();
		String[] movedIds;
		String targetId;
		String placement;
		boolean justTheEntry;
		String csrft;

		try {
			final JsonNodeHelper json = getJsonNode(requestAndResponse);
			movedIds = json.getStringArray("movedIds");
			targetId = json.getString("targetId");
			placement = json.getString("placement");
			justTheEntry = json.getBoolean("justTheEntry");
			csrft = json.getString("csrft");
		} catch (final IOException e) {
			returnJson400(requestAndResponse, servletText.errorJson());
			return;
		}

		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			returnJson400(requestAndResponse,
					servletText.errorRequiresSignIn(false));
			return;
		}

		if (placement == null
				|| !(placement.equals("next") || placement.equals("previous"))) {
			returnJson400(requestAndResponse,
					servletText.errorInvalidPlacementValue());
			return;
		}

		if (!dbLogic.getIdGenerator().isIdWellFormed(targetId)) {
			returnJson400(requestAndResponse,
					servletText.errorTargetIdInvalidFormat());
			return;
		}

		try {
			final Errors errors = new Errors();
			final User user = dbLogic
					.getUserById(getEffectiveUserId(requestAndResponse));
			if (user == null) {
				returnJson400(requestAndResponse,
						servletText.errorRequiresSignIn(false));
				return;
			}

			if (user.getIsAccountClosed()) {
				returnJson400(requestAndResponse,
						servletText.errorAccountIsClosed());
			}

			final Entry sibling = dbLogic.getEntryById(targetId);
			if (sibling == null) {
				returnJson400(requestAndResponse,
						servletText.errorTargetNoteCouldNotBeFound());
				return;
			}

			final LinkedList<EntryAndIsFromList> entriesToMove = new LinkedList<EntryAndIsFromList>();
			final String errorMessage = validateEntriesParentsBeforeChildren(
					requestAndResponse, movedIds, entriesToMove);
			if (errorMessage != null) {
				returnJson400(requestAndResponse, errorMessage);
				return;
			}

			final StringBuilder result = new StringBuilder();
			result.append("[");
			boolean isFirst = true;
			for (final EntryAndIsFromList entryToMove : entriesToMove) {
				if (sibling.getId().equals(entryToMove.entry.getId())) {
					returnJson400(requestAndResponse,
							servletText.errorTargetAndObjectCanNotBeTheSame());
					return;
				}

				if (!dbLogic.makeEntrySiblingOfAnother(user, sibling,
						entryToMove.entry, justTheEntry, placement,
						isUserAnAdmin(requestAndResponse), errors)) {
					returnJson400(requestAndResponse, errors);
					return;
				}

				if (entryToMove.isFromList) {
					if (!isFirst) {
						result.append(",");
					}

					result.append("{");
					result.append("\"id\":\"" + entryToMove.entry.getId()
							+ "\",\n");
					addJsonForEntry(result, entryToMove.entry, false, false,
							false, true);
					result.append("}");

					isFirst = false;
				}
			}

			result.append("]");

			dbLogic.commit();

			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			logger.log(Level.INFO, "Exception", e);
			returnJson500(requestAndResponse,
					servletText.errorInternalDatabase());
		}
	}

	/** Part of the JSON API. Gets the path to the notebook for the entry. */
	private void handleJsonGetNotebookPath(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();
		String entryId;
		String csrft;

		try {
			final JsonNodeHelper json = getJsonNode(requestAndResponse);
			entryId = json.getString("entryId");
			csrft = json.getString("csrft");
		} catch (final IOException e) {
			returnJson400(requestAndResponse, servletText.errorJson());
			return;
		}

		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			returnJson400(requestAndResponse,
					servletText.errorRequiresSignIn(false));
			return;
		}

		if (!dbLogic.getIdGenerator().isIdWellFormed(entryId)) {
			returnJson400(requestAndResponse,
					servletText.errorTargetIdInvalidFormat());
			return;
		}

		try {
			final User user = dbLogic
					.getUserById(getEffectiveUserId(requestAndResponse));
			if (user == null) {
				returnJson400(requestAndResponse,
						servletText.errorRequiresSignIn(false));
				return;
			}

			if (user.getIsAccountClosed()) {
				returnJson400(requestAndResponse,
						servletText.errorAccountIsClosed());
			}

			Entry entry = dbLogic.getEntryById(entryId);
			if (entry == null) {
				returnJson400(requestAndResponse,
						servletText.errorTargetNoteCouldNotBeFound());
				return;
			}

			final StringBuilder result = new StringBuilder();
			result.append("[");
			boolean isFirst = true;
			while (entry != null) {
				if (!isFirst) {
					result.append(",");
				}

				isFirst = false;

				result.append(JsonBuilder.quote(entry.getId()));

				entry = dbLogic.getEntryById(entry.getParentId());

				if (entry != null && entry.isRoot()) {
					entry = dbLogic.getEntryById(entry.getNotebookId());
					result.append(",");
					result.append(JsonBuilder.quote(entry.getId()));
					break;
				}
			}

			result.append("]");

			dbLogic.commit();

			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			logger.log(Level.INFO, "Exception", e);
			returnJson500(requestAndResponse,
					servletText.errorInternalDatabase());
		}
	}

	/** Part of the JSON API. Handles many note operations. */
	private void handleJsonNoteOp(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();
		String type = null;
		String note = null;
		String quotation = null;
		String id = null;
		String ids = null;
		String noteop = null;
		String childrenAction = null;
		boolean insertAsFirstChild = false;
		boolean isPublic = false;
		String csrft = null;

		try {
			final JsonNodeHelper json = getJsonNode(requestAndResponse);

			type = json.getString(DbLogic.Constants.type);

			note = json.getString(DbLogic.Constants.note);

			quotation = json.getString(DbLogic.Constants.quotation);

			id = json.getString(DbLogic.Constants.id);

			ids = json.getString("ids");

			noteop = json.getString("noteop");

			childrenAction = json.getString("childrenAction");

			insertAsFirstChild = json.getBoolean("insertAsFirstChild");

			isPublic = json.getBoolean("isPublic");

			csrft = json.getString("csrft");
		} catch (final IOException e) {
			returnJson400(requestAndResponse, servletText.errorJson());
			return;
		}

		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			returnJson400(requestAndResponse,
					servletText.errorRequiresSignIn(false));
			return;
		}

		if (!EntryAttributeValidator.isNoteValid(note)) {
			returnJson400(requestAndResponse, servletText.errorNoteIsInvalid());
			return;
		}

		if (type != null && type.equals(Constants.note)
				&& !EntryAttributeValidator.isNoteValid(note)) {
			returnJson400(requestAndResponse, servletText.errorNoteIsInvalid());
			return;
		}

		if (type != null && type.equals(Constants.notebook)
				&& !EntryAttributeValidator.isNotebookTitleValid(note)) {
			returnJson400(requestAndResponse, servletText.errorNoteIsInvalid());
			return;
		}

		if (!EntryAttributeValidator.isQuotationValid(quotation)) {
			returnJson400(requestAndResponse,
					servletText.errorQuotationIsInvalid());
			return;
		}

		try {
			final Long time = new Long(System.currentTimeMillis());
			final Errors errors = new Errors();
			final StringBuilder result = new StringBuilder();
			User user = dbLogic
					.getUserById(getEffectiveUserId(requestAndResponse));

			boolean userWasSignedIn = false;
			// Create the user if needed and possible.
			if (user == null) {
				if (allowSaveIfNotSignedIn && noteop != null
						&& noteop.equals("newNotebook")) {
					user = createAnonUser();

					if (user == null) {
						returnJson400(requestAndResponse,
								servletText.errorCouldNotCreateAccount());
						return;
					} else {
						mapSessionToUser(requestAndResponse, user.getId());
						userWasSignedIn = true;
					}
				} else {
					returnJson400(requestAndResponse,
							servletText.errorRequiresSignIn(false));
					return;
				}
			}

			if (user.getIsAccountClosed()) {
				returnJson400(requestAndResponse,
						servletText.errorAccountIsClosed());
				return;
			}

			result.append("{\"modTime\": " + time + "\n");
			Entry entry = null;
			boolean includeNote = false;
			boolean success = false;
			ArrayList<String> deletedEntryIds = null;
			if (noteop != null) {
				if (noteop.equals("edit") || noteop.equals("editNotebook")
						|| noteop.equals("editSource")) {
					if (id == null) {
						returnJson400(requestAndResponse,
								servletText.errorIdIsInvalidFormat());
						return;
					}

					final String[] idParts = id.split(":");
					final String trueId = idParts[idParts.length == 1 ? 0 : 1];

					if (!dbLogic.getIdGenerator().isIdWellFormed(trueId)) {
						returnJson400(requestAndResponse,
								servletText.errorIdIsInvalidFormat());
						return;
					}

					entry = dbLogic.editEntry(user, trueId, note, quotation,
							isPublic, time, isUserAnAdmin(requestAndResponse),
							errors);
					includeNote = true;
					success = entry != null;
				} else if (noteop.equals("delete")
						|| noteop.equals("deleteNotebook")
						|| noteop.equals("deleteSource")) {
					deletedEntryIds = new ArrayList<String>();

					if (ids == null) {
						returnJson400(requestAndResponse,
								servletText.errorIdsAreInvalidFormat());
						return;
					}

					final String[] idsArray = ids.split(",");

					// Do this here so that we don't need to delete the orphan
					// functionality or tests.
					// It has also been disabled in the UI because probably no
					// one will want this feature unless it is more polished.
					if (childrenAction != null
							&& childrenAction.equals("orphan")) {
						childrenAction = "parent";
					}

					if (idsArray.length > 1) {
						childrenAction = "delete";
					} else if (noteop.equals("deleteNotebook")
							|| noteop.equals("deleteSource")) {
						childrenAction = "parent";
					}

					success = true;

					final LinkedList<EntryAndIsFromList> entriesToDelete = new LinkedList<EntryAndIsFromList>();
					String errorMessage = validateEntriesParentsBeforeChildren(
							requestAndResponse, idsArray, entriesToDelete);

					for (final EntryAndIsFromList entryToDelete : entriesToDelete) {
						if (entryToDelete.entry.getType().equals(
								Constants.tableofcontents)
								|| entryToDelete.entry.getType().equals(
										Constants.root)) {
							errorMessage = servletText
									.errorEntryCanNotBeDeleted();
						}
					}

					if (errorMessage != null) {
						success = false;
						errors.add(errorMessage);
					} else {
						for (final EntryAndIsFromList entryToDelete : entriesToDelete) {
							// Check to make sure the entry has not already been
							// deleted.
							// So far I haven't been able to find a way to do
							// this that doesn't throw an exception.
							if (dbLogic
									.wasEntryDeletedInThisTransaction(entryToDelete.entry)) {
								continue;
							}

							success &= dbLogic.deleteEntry(user,
									entryToDelete.entry, childrenAction,
									isUserAnAdmin(requestAndResponse),
									deletedEntryIds, errors);
						}
					}
					/**
					 * Disable for now because probably no one will want this
					 * feature unless it is more polished } else if
					 * (noteop.equals("makeNotebook")) { String[] idsArray =
					 * ids.split(",");
					 * 
					 * success = true;
					 * 
					 * final LinkedList<Entry> entriesToMakeNotebooks = new
					 * LinkedList<Entry>(); final String errorMessage =
					 * validateEntriesParentsBeforeChildren( requestAndResponse,
					 * idsArray, entriesToMakeNotebooks); if (errorMessage !=
					 * null) { success = false; errors.add(errorMessage); } else
					 * { for (Entry entryToMakeNotebook :
					 * entriesToMakeNotebooks) { success &=
					 * dbLogic.makeNotebookEntry( user, entryToMakeNotebook,
					 * isUserAnAdmin(requestAndResponse), errors); } }
					 */
				} else if (noteop.equals("insert")) {
					if (id == null
							|| !dbLogic.getIdGenerator().isIdWellFormed(id)) {
						returnJson400(requestAndResponse,
								servletText.errorIdIsInvalidFormat());
						return;
					}

					entry = dbLogic.createSimpleEntry(user, note, time, id,
							TreeRelType.Child, false, false, isPublic,
							isUserAnAdmin(requestAndResponse), type, errors);
					includeNote = true;
					success = entry != null;
				} else if (noteop.equals("putUnderneath")) {
					if (id == null
							|| !dbLogic.getIdGenerator().isIdWellFormed(id)) {
						returnJson400(requestAndResponse,
								servletText.errorIdIsInvalidFormat());
						return;
					}

					entry = dbLogic.createSimpleEntry(user, note, time, id,
							TreeRelType.Parent, true, false, isPublic,
							isUserAnAdmin(requestAndResponse), type, errors);
					includeNote = true;
					success = entry != null;
				} else if (noteop.equals("createChild")) {
					if (id == null
							|| !dbLogic.getIdGenerator().isIdWellFormed(id)) {
						returnJson400(requestAndResponse,
								servletText.errorIdIsInvalidFormat());
						return;
					}

					entry = dbLogic.createSimpleEntry(user, note, time, id,
							TreeRelType.Parent, false, insertAsFirstChild,
							isPublic, isUserAnAdmin(requestAndResponse), type,
							errors);
					includeNote = true;
					success = entry != null;
				} else if (noteop.equals("newNotebook")) {
					entry = dbLogic.createEntryNoteBook(user, note, time, null,
							null, false, false, isPublic,
							isUserAnAdmin(requestAndResponse), errors);
					includeNote = true;
					success = entry != null;
				} else if (noteop.equals("putBefore")) {
					if (id == null
							|| !dbLogic.getIdGenerator().isIdWellFormed(id)) {
						returnJson400(requestAndResponse,
								servletText.errorIdIsInvalidFormat());
						return;
					}

					entry = dbLogic.createSimpleEntry(user, note, time, id,
							TreeRelType.Next, false, false, isPublic,
							isUserAnAdmin(requestAndResponse), type, errors);
					includeNote = true;
					success = entry != null;
				} else if (noteop.equals("putAfter")) {
					if (id == null
							|| !dbLogic.getIdGenerator().isIdWellFormed(id)) {
						returnJson400(requestAndResponse,
								servletText.errorIdIsInvalidFormat());
						return;
					}

					entry = dbLogic.createSimpleEntry(user, note, time, id,
							TreeRelType.Previous, false, isPublic,
							isUserAnAdmin(requestAndResponse), false, type,
							errors);
					includeNote = true;
					success = entry != null;
				} else {
					errors.add(servletText.errorInvalidOperation());
				}
			} else {
				errors.add(servletText.errorMissingOperation());
			}

			if (!success) {
				requestAndResponse.response
						.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				result.append(",\"success\":false");
				result.append(",");
				errorsToJson(errors, result);
			} else {
				result.append(",\"success\":true");
			}

			if (entry != null) {
				result.append(",\"id\":\"" + entry.getId() + "\"\n");

				if (includeNote) {
					result.append(",");

					addJsonForEntry(
							result,
							entry,
							noteop.equals("edit")
									|| noteop.equals("editNotebook")
									|| noteop.equals("editSource"),
							noteop.equals("newNotebook"), userWasSignedIn, true);
				}
			}

			if (deletedEntryIds != null) {
				result.append(", \"deleted\": [");
				boolean isFirst = true;
				for (final String deletedEntryId : deletedEntryIds) {
					if (!isFirst) {
						result.append(",");
					}
					isFirst = false;
					result.append("\"" + deletedEntryId + "\"");
				}
				result.append("]");
			}

			result.append("}\n");
			dbLogic.commit();

			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			logger.log(Level.INFO, "Exception", e);
			returnJson500(requestAndResponse,
					servletText.errorInternalDatabase());
		}
	}

	/** Adds JSON for the entry. */
	private void addJsonForEntry(final StringBuilder result, Entry entry,
			boolean includeJustTextFields, boolean includeUserWasSignIn,
			boolean userWasSignedIn, boolean forceQuotationToNote)
			throws IOException {
		result.append("\"note\":"
				+ JsonBuilder.quote(entry.getNoteOrNotebookTitle("")) + "\n");

		result.append(",\"quotation\":"
				+ JsonBuilder.quote(entry.getQuotation("")) + "\n");

		result.append(",\"isPublic\":" + entry.getIsPublic() + "\n");

		String typeToAdd = entry.getType();
		if (typeToAdd.equals(DbLogic.Constants.quotation)) {
			typeToAdd = DbLogic.Constants.note;
		}

		result.append(",\"type\":\"" + typeToAdd + "\"\n");

		if (includeUserWasSignIn) {
			result.append(",\"userWasSignedIn\":" + userWasSignedIn + "\n");
		}

		if (includeJustTextFields) {
			result.append(",\"noteHtml\":"
					+ JsonBuilder.quote(getNoteMarkdown(entry, false, entry.hasQuotation())) + "\n");
			result.append(",\"quotationHtml\":"
					+ JsonBuilder.quote(getQuotationMarkdown(entry, false))
					+ "\n");
		} else {
			final StringBuilder innerResult = new StringBuilder();
			addEntryHtmlToTreeSimple(entry, innerResult, null, 0);
			result.append(",\"subtreeHtml\":"
					+ JsonBuilder.quote(innerResult.toString()) + "\n");
		}
	}

	/** Part of the JSON API. Handles sign out requests. */
	private void handleJsonSignOut(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();
		String csrft;

		try {
			final JsonNodeHelper json = getJsonNode(requestAndResponse);
			csrft = json.getString("csrft");
		} catch (final IOException e) {
			returnJson400(requestAndResponse, servletText.errorJson());
			return;
		}

		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			returnJson400(requestAndResponse,
					servletText.errorRequiresSignIn(false));
			return;
		}

		unmapSessionToUser(requestAndResponse);
		returnJson200(requestAndResponse);
	}

	/** Part of the JSON API. Handles sign in requests. */
	private void handleJsonSignIn(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();
		String userName;
		String password;
		String csrft;

		try {
			final JsonNodeHelper json = getJsonNode(requestAndResponse);
			userName = json.getString("username");
			password = json.getString("password");
			csrft = json.getString("csrft");
		} catch (final IOException e) {
			returnJson400(requestAndResponse, servletText.errorJson());
			return;
		}

		if (userName != null) {
			userName = userName.toLowerCase();
		}

		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			returnJson400(requestAndResponse, servletText.errorWrongCsrft());
			return;
		}

		if (userName == null || userName.isEmpty()) {
			returnJson400(requestAndResponse,
					servletText.errorUsernameMustNotBeBlank());
			return;
		}

		if (password == null || password.isEmpty()) {
			returnJson400(requestAndResponse,
					servletText.errorPasswordMustNotBeBlank());
			return;
		}

		if (!AccountAttributeValidator.isUserNameValid(userName)) {
			returnJson400(requestAndResponse,
					servletText.errorUserNameIsNotValid());
			return;
		}

		if (!AccountAttributeValidator.isPasswordValid(password)) {
			returnJson400(requestAndResponse,
					servletText.errorPasswordIsNotValid());
			return;
		}

		try {
			final User user = dbLogic.getUserByUserName(userName);
			if (user == null) {
				returnJson400(requestAndResponse,
						servletText.errorNoAccountFound());
				return;
			}

			if (user.getIsAccountClosed()) {
				returnJson400(requestAndResponse,
						servletText.errorAccountIsClosed());
				return;
			}

			final String realPassword = user.getPassword();
			if (realPassword == null
					|| !realPassword.equals(DigestUtils.sha1Hex(password))) {
				returnJson400(requestAndResponse,
						servletText.errorPasswordIsIncorrect());
				return;
			}

			mapSessionToUser(requestAndResponse, user.getId());

			dbLogic.commit();

			returnJson200(requestAndResponse);
		} catch (final PersistenceException e) {
			logger.log(Level.INFO, "Exception", e);
			returnJson500(requestAndResponse,
					servletText.errorInternalDatabase());
		}
	}

	/** Maps the session to the user. */
	private void mapSessionToUser(RequestAndResponse requestAndResponse,
			String userId) {
		requestAndResponse.request.getSession().setAttribute(
				sessionUserIdAttribute, userId);
	}

	/** Unmaps the session from the user. */
	private void unmapSessionToUser(RequestAndResponse requestAndResponse) {
		requestAndResponse.request.getSession().removeAttribute(
				sessionUserIdAttribute);
	}

	/** Part of the JSON API. Handles sign in requests. */
	private void handleJsonCreateAccount(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();
		String userName, password, password2, email;
		boolean mayContact = false;
		String csrft;

		try {
			final JsonNodeHelper json = getJsonNode(requestAndResponse);
			userName = json.getString("username");
			password = json.getString("password");
			password2 = json.getString("password2");
			email = json.getString("email");
			mayContact = json.getBoolean("mayContact");
			csrft = json.getString("csrft");
		} catch (final IOException e) {
			returnJson400(requestAndResponse, servletText.errorJson());
			return;
		}

		if (userName != null) {
			userName = userName.toLowerCase();
		}

		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			returnJson400(requestAndResponse, servletText.errorWrongCsrft());
			return;
		}

		if (!allowSelfSignUp) {
			returnJson400(requestAndResponse,
					servletText.errorSelfSignUpNotAllowed());
			return;
		}

		if (userName == null || userName.isEmpty()) {
			returnJson400(requestAndResponse,
					servletText.errorUsernameMustNotBeBlank());
			return;
		}

		if (password == null || password.isEmpty()) {
			returnJson400(requestAndResponse,
					servletText.errorFirstPasswordMustBeSet(true, null));
			return;
		}

		if (password == null || password2.isEmpty()) {
			returnJson400(requestAndResponse,
					servletText.errorSecondPasswordMustBeSet(true, null));
			return;
		}

		if (!password2.equals(password)) {
			returnJson400(requestAndResponse,
					servletText.errorPasswordsMustMatch());
			return;
		}

		if (!AccountAttributeValidator.isUserNameValid(userName)) {
			returnJson400(requestAndResponse,
					servletText.errorUserNameIsNotValid());
			return;
		}

		if (!AccountAttributeValidator.isPasswordValid(password)) {
			returnJson400(requestAndResponse,
					servletText.errorPasswordIsNotValid());
			return;
		}

		if (email != null && email.isEmpty()) {
			email = null;
		}

		if (email != null && !AccountAttributeValidator.isEmailValid(email)) {
			returnJson400(requestAndResponse,
					servletText.errorEmailIsNotValid());
			return;
		}

		try {
			User user = dbLogic.getUserByUserName(userName);
			if (user != null) {
				returnJson400(requestAndResponse,
						servletText.errorUserNameIsAlreadyTaken());
				return;
			}

			user = dbLogic.createUser(userName);
			if (user == null) {
				returnJson400(requestAndResponse,
						servletText.errorCouldNotCreateAccount());
				return;
			}

			user.setPassword(DigestUtils.sha1Hex(password));
			user.setEmail(email);
			user.setMayContact(mayContact);

			mapSessionToUser(requestAndResponse, user.getId());

			dbLogic.commit();

			returnJson200(requestAndResponse);
		} catch (final PersistenceException e) {
			logger.log(Level.INFO, "Exception", e);
			returnJson500(requestAndResponse,
					servletText.errorInternalDatabase());
		}
	}

	/** Part of the JSON API. Handles save option requests. */
	private void handleJsonSaveOptions(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();
		boolean showTimestamps = false;
		boolean saveOnEnter = false;
		String csrft;

		try {
			final JsonNodeHelper json = getJsonNode(requestAndResponse);
			showTimestamps = json.getBoolean("showTimestamps");
			saveOnEnter = json.getBoolean("saveOnEnter");
			csrft = json.getString("csrft");
		} catch (final IOException e) {
			returnJson400(requestAndResponse, servletText.errorJson());
			return;
		}

		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			requestAndResponse.print(servletText.errorRequiresSignIn(false));
			return;
		}

		try {
			final User user = dbLogic
					.getUserById(getEffectiveUserId(requestAndResponse));

			if (user == null) {
				requestAndResponse
						.print(servletText.errorRequiresSignIn(false));
				return;
			}

			final String options = "{\"showTimestamps\":" + showTimestamps
					+ ",\"saveOnEnter\":" + saveOnEnter + "}";
			user.setOptions(options);
			dbLogic.commit();

			returnJson200(requestAndResponse);
		} catch (final PersistenceException e) {
			logger.log(Level.INFO, "Exception", e);
			returnJson500(requestAndResponse,
					servletText.errorInternalDatabase());
		}
	}

	/**
	 * Returns a JSON node for the request's data.
	 * 
	 * @throws IOException
	 */
	private JsonNodeHelper getJsonNode(RequestAndResponse requestAndResponse)
			throws IOException {
		final InputStream stream = requestAndResponse.request.getInputStream();
		final InputStreamReader streamReader = new InputStreamReader(stream,
				Charset.forName("UTF-8"));
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode node = mapper.readTree(streamReader);
		return new JsonNodeHelper(node);
	}

	/** Creates the user for single user mode. */
	private void createTheUserForSingleUserMode() {
		if (!isInSingleUserMode()) {
			return;
		}

		try {
			final User user = dbLogic.getOrCreateUser(singleUserName);
			if (user == null) {
				logger.log(Level.SEVERE, "Could not create single user user");
				return;
			}

			if (!user.getIsSingleUser()) {
				user.setIsSingleUser(true);
			}

			if (!user.getIsAdmin()) {
				user.setIsAdmin(true);
			}

			dbLogic.commit();
		} catch (final PersistenceException e) {
			logger.log(Level.SEVERE, "Could not create single user user", e);
		}
	}

	/** Returns the sanitized Markdown HTML for some text. */
	private String getMarkdownHtml(String text, boolean noLinks,
			boolean skipSanitizing) {
		if (text == null) {
			return "";
		}

		final String markdown = (noLinks ? pegDownNolinkProcessor
				: pegDownProcessor).get().markdownToHtml(text,
				linkRenderer.get());
		if (skipSanitizing) {
			return markdown;
		}

		final String cleaned = Jsoup.clean(markdown, whitelist.get());
		return cleaned;
	}

	private static final ThreadLocal<Whitelist> whitelist = new ThreadLocal<Whitelist>() {
		@Override
		protected Whitelist initialValue() {
			return Whitelist.relaxed()
					.addEnforcedAttribute("a", "rel", "nofollow")
					.addEnforcedAttribute("a", "target", "_blank");
		}
	};

	private static final ThreadLocal<PegDownNoLinkProcessor> pegDownNolinkProcessor = new ThreadLocal<PegDownNoLinkProcessor>() {
		@Override
		protected PegDownNoLinkProcessor initialValue() {
			return new PegDownNoLinkProcessor();
		}
	};

	private static final ThreadLocal<PegDownProcessor> pegDownProcessor = new ThreadLocal<PegDownProcessor>() {
		@Override
		protected PegDownProcessor initialValue() {
			return new PegDownProcessor();
		}
	};

	private static final ThreadLocal<LinkRenderer> linkRenderer = new ThreadLocal<LinkRenderer>() {
		@Override
		protected LinkRenderer initialValue() {
			return new LinkRenderer();
		}
	};

	String standardCss = "<link rel=\"shortcut icon\" href=\"/images/favicon.ico\">\n"
			+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/ui.css\">\n";

	/**
	 * Holds settings related to how the header and footer for a response should
	 * be created. Has a fluent interface to minimize boiler plate code.
	 */
	class PageWrapper {
		PageWrapper(RequestAndResponse requestAndResponse, String title,
				boolean needsAdmin) {
			this.requestAndResponse = requestAndResponse;
			this.title = title;
			this.needsAdmin = needsAdmin;
		}

		/**
		 * Adds text to introduce the page.
		 * 
		 * @throws IOException
		 */
		private void addPageIntroText(String clickText, String touchText) throws IOException {
			requestAndResponse.println("<div class=\"infotext\">" + clickText + "</div>");
			
			addMetaData(new KeyAndValue("touchInfoText", touchText));
		}
		
		/**
		 * Appends an HTML header to a response.
		 */
		public void addHeader() throws IOException {
			final boolean onlyContent = getNoHeader();

			final boolean isUserAnAdmin = isUserAnAdmin(requestAndResponse);

			addMetaData(new KeyAndValue("title", title));

			if (!onlyContent) {
				if (!requestAndResponse.skipHeader) {
					final boolean isForPageRefresh = getIsForPageRefresh();

					if (!isForPageRefresh) {
						requestAndResponse
								.print("<!doctype html>"
										+ "<html>"
										+ "<head>"
										// So that android renders text with the
										// correct font sizes.
										+ "<meta name=\"HandheldFriendly\" content=\"true\"/>"
										+ "<meta name=\"viewport\" content=\"width=device-width\" />"
										+ "<title>");
	
	
						requestAndResponse
								.print(requestAndResponse.titleAlreadyFormed ? requestAndResponse.totalTitle
										.toString() : title.replace("|", ""));
	
						if (paneId == null || !paneId.equals("welcome")) {
							requestAndResponse.print(" - "
									+ servletText.labelApplicationName());
						} else {
							requestAndResponse.print(servletText
									.pageTitleWelcomeExtra());
						}
					
						requestAndResponse
								.print("</title>"
										+ standardCss
										+ (extraHeader != null ? extraHeader
												: "")
										+ "</head>\n"
										+ "<body><div id=\"siteRequirements\">"
										+ servletText.errorJavaScriptNeeded()
										+ "</div><script type=\"text/javascript\">\n"
										+ "document.getElementById(\"siteRequirements\").style.display=\"none\";\n"
										+ "var asyncScripts = [\n"
										+ "  '/js/mousetrap.min.js',\n"
										+ "  '/js/uiTextEn.js',\n"
										+ "  '/js/ui.js'\n"
										+ "];\n"
										+ "function loadScript(src, callback) {\n"
										+ "	 var script = document.createElement('script');\n"
										+ "	 script.type = 'text/javascript';\n"
										+ "	 script.src = src;\n"
										+ "	 script.onload = callback;\n"
										+ "	 script.onreadystatechange = function() {\n"
										+ "	   if (this.readyState == 'complete') {\n"
										+ "		 callback();\n"
										+ "	   }\n"
										+ "	 }\n"
										+ "  document.head.appendChild(script);\n"
										+ "}\n"
										+ "var srcsLoaded = 0;\n"
										+ "function maybeCallFinish() {\n"
										+ "  if(++srcsLoaded === asyncScripts.length + 1) {"
										+ "    onFinishFullPageLoad();\n"
										+ "  }\n"
										+ "}\n"
										+ "for(var scriptIndex = 0; scriptIndex < asyncScripts.length; ++scriptIndex) {\n"
										+ "  var script = document.createElement('script');\n"
										+ "  loadScript(asyncScripts[scriptIndex], maybeCallFinish);\n"
										+ "}"
										+ "</script>\n");
					}

					requestAndResponse.print("<div id=\"allPanes\">");

					// Add the portion of the left hand menu that is for all
					// users.
					requestAndResponse
							.print("<div><div class=\"paneContainer\"><div class=\"pane\" id=\"menu\">"
									+ "<div class=\"paneSection\"><span><a id=\"appName\" title=\""
									+ servletText.labelApplicationNameTooltip()
									+ "\" href=\"/\">"
									+ servletText.labelApplicationName()
									+ "</a></span><a onclick=\"showOrHideMenu();\" id=\"showMenu\" title=\""
									+ servletText.tooltipMenu()
									+ "\">"
									+ servletText.linkMenu() + "</a></div>");

					// Add account, sign in, create account links as needed.
					if (!isUserSignedIn(requestAndResponse)) {
						requestAndResponse
								.print("<div class=\"paneSection\">\n");
						requestAndResponse
								.print("<a onclick=\"closeMenuIfSmallDisplay(); signIn(); return false;\" title=\""
										+ servletText.tooltipSignIn()
										+ "\">"
										+ servletText.linkSignIn() + "</a>");

						if (allowSelfSignUp) {
							requestAndResponse
									.print("<a onclick=\"closeMenuIfSmallDisplay(); createAccount(); return false;\" title=\""
											+ servletText
													.tooltipCreateAccount()
											+ "\">"
											+ servletText.linkCreateAccount()
											+ "</a>");
						}

						requestAndResponse.print("</div>\n");
					}

					boolean startedCommandPaneSection = false;
					if (isUserSignedIn(requestAndResponse)
							|| allowSaveIfNotSignedIn) {
						requestAndResponse
							.print("<div class=\"paneSection\">\n");
						startedCommandPaneSection = true;

						requestAndResponse
								.print("<a onclick=\"closeMenuIfSmallDisplay(); showPopupForCreateNotebook(); return false;\" title=\""
										+ servletText
												.pageTitleCreateNoteTooltip()
										+ "\" href=\"/newNotebook/\">"
										+ servletText.pageTitleNewNotebook()
										+ "</a>\n");
					}
					
					if (isUserSignedIn(requestAndResponse)) {
						if (!startedCommandPaneSection) {
							startedCommandPaneSection = true;

							requestAndResponse
								.print("<div class=\"paneSection\">\n");
						}

						requestAndResponse
								.print("<a onclick=\"newPaneForLink(event, '"
										+ servletText.pageTitleNotebooks()
										+ "', 'notebooks'); return false;\" title=\""
										+ servletText
												.pageTitleNotebooksTooltip()
										+ "\" href=\"/notebooks/\">"
										+ servletText.pageTitleNotebooks()
										+ "</a>\n");

						requestAndResponse
								.print("<a onclick=\"newPaneForLink(event, '"
										+ servletText.pageTitleQuotations()
										+ "', 'quotations'); return false;\" title=\""
										+ servletText
												.pageTitleQuotationsTooltip()
										+ "\" href=\"/quotations/\">"
										+ servletText.pageTitleQuotations()
										+ "</a>\n");

						requestAndResponse
								.print("<a onclick=\"newPaneForLink(event, '"
										+ servletText.pageTitleSources()
										+ "', 'sources'); return false;\" title=\""
										+ servletText
												.pageTitleSourcesTooltip()
										+ "\" href=\"/sources/\">"
										+ servletText.pageTitleSources()
										+ "</a>\n");

						requestAndResponse
								.print("<a onclick=\"newPaneForLink(event, '"
										+ servletText.pageTitleSearch()
										+ "', 'search'); return false;\" title=\""
										+ servletText
												.pageTitleSearchTooltip()
										+ "\" href=\"/search/\">"
										+ servletText.pageTitleSearch()
										+ "</a>\n");

						requestAndResponse
								.print("<a onclick=\"closeMenuIfSmallDisplay(); closeAllPanes(); return false;\" title=\""
										+ servletText
												.pageTitleCloseAllTooltip()
										+ "\">"
										+ servletText.pageTitleCloseAll()
										+ "</a>\n");
					}
					
					if (startedCommandPaneSection) {
						requestAndResponse.print("</div>\n");
					}

					requestAndResponse
							.print("<div class=\"paneSection\">\n");

					requestAndResponse
							.print("<a onclick=\"newPaneForLink(event, '"
									+ servletText.pageTitleHelp()
									+ "', 'help'); return false;\" title=\""
									+ servletText.pageTitleHelpTooltip()
									+ "\" href=\"/help/\">"
									+ servletText.pageTitleHelp()
									+ "</a>\n");

					requestAndResponse
							.print("<a onclick=\"closeMenuIfSmallDisplay(); showPopupForHelp(event); return false;\" title=\""
									+ servletText.pageTitleUiHelpTooltip()
									+ "\" href=\"/help/\">"
									+ servletText.pageTitleUiHelp()
									+ "</a>\n");

					if (isUserSignedIn(requestAndResponse)) {
						requestAndResponse
								.print("<a onclick=\"closeMenuIfSmallDisplay(); showPopupForOptions(); return false;\" title=\""
										+ servletText.tooltipOptions()
										+ "\">"
										+ servletText.linkOptions()
										+ "</a>");
					}

					requestAndResponse.print("</div>\n");

					if (isUserSignedIn(requestAndResponse)) {
						requestAndResponse
								.print("<div class=\"paneSection\">\n");
						if (!isInSingleUserMode()) {
							requestAndResponse
									.print("<a onclick=\"signOut(); return false;\" title=\""
											+ servletText.tooltipSignOut()
											+ "\">"
											+ servletText.linkSignOut()
											+ "</a>");

							requestAndResponse
									.print("<a onclick=\"newPaneForLink(event, '"
											+ servletText.linkAccount()
											+ "', 'account'); return false;\" href=\"/account/\" title=\""
											+ servletText.tooltipEditAccount()
											+ "\">"
											+ servletText.linkAccount()
											+ "</a>");
						}

						requestAndResponse
								.print("<a onclick=\"newPaneForLink(event, '"
										+ servletText.pageTitleExport()
										+ "', 'export'); return false;\" title=\""
										+ servletText.pageTitleImportTooltip()
										+ "\" href=\"/export/\">"
										+ servletText.pageTitleExport()
										+ "</a>\n");

						requestAndResponse
								.print("<a onclick=\"newPaneForLink(event, '"
										+ servletText.pageTitleImport()
										+ "', 'import'); return false;\" title=\""
										+ servletText.pageTitleImportTooltip()
										+ "\" href=\"/import/\">"
										+ servletText.pageTitleImport()
										+ "</a>\n");

						requestAndResponse.print("</div>\n");
					}

					// Add the portion of the left hand menu that is for admin
					// users.
					if (isUserAnAdmin) {
						requestAndResponse
								.print("<div class=\"paneSection\">\n");

						requestAndResponse
								.print("<a onclick=\"newPaneForLink(event, '"
										+ servletText.pageTitleAccounts()
										+ "', 'accounts'); return false;\" title=\""
										+ servletText
												.pageTitleAccountsTooltip()
										+ "\" href=\"/accounts/\">"
										+ servletText.pageTitleAccounts()
										+ "</a>\n");

						requestAndResponse
								.print("<a onclick=\"newPaneForLink(event, '"
										+ servletText.pageTitleShutdown()
										+ "', 'shutdown'); return false;\" title=\""
										+ servletText
												.pageTitleShutdownTooltip()
										+ "\" href=\"/shutdown/\">"
										+ servletText.pageTitleShutdown()
										+ "</a>\n");

						requestAndResponse
								.print("<a onclick=\"newPaneForLink(event, '"
										+ servletText.pageTitleClearDb()
										+ "', 'clear'); return false;\" title=\""
										+ servletText.pageTitleClearDbTooltip()
										+ "\" href=\"/clear/\">"
										+ servletText.pageTitleClearDb()
										+ "</a>\n");

						requestAndResponse
								.print("<a onclick=\"newPaneForLink(event, '"
										+ servletText.pageTitleOnlineBackupDb()
										+ "', 'onlineBackup'); return false;\" title=\""
										+ servletText
												.pageTitleOnlineBackupDbTooltip()
										+ "\" href=\"/onlineBackup/\">"
										+ servletText.pageTitleOnlineBackupDb()
										+ "</a>\n");

						requestAndResponse
								.print("<a onclick=\"newPaneForLink(event, '"
										+ servletText
												.pageTitleCheckDbForErrors()
										+ "', 'checkForErrors'); return false;\" title=\""
										+ servletText
												.pageTitleCheckDbForErrorsTooltip()
										+ "\" href=\"/checkForErrors/\">"
										+ servletText
												.pageTitleCheckDbForErrors()
										+ "</a>\n");

						requestAndResponse
								.print("<a onclick=\"newPaneForLink(event, '"
										+ servletText.pageTitleShowBackups()
										+ "', 'backups'); return false;\" title=\""
										+ servletText
												.pageTitleShowBackupsTooltip()
										+ "\" href=\"/backups/\">"
										+ servletText.pageTitleShowBackups()
										+ "</a>\n");

						requestAndResponse
								.print("<a onclick=\"newPaneForLink(event, '"
										+ servletText
												.pageTitleOfflineBackupDb()
										+ "', 'offlineBackup'); return false;\" title=\""
										+ servletText
												.pageTitleOfflineBackupDbTooltip()
										+ "\" href=\"/offlineBackup/\">"
										+ servletText
												.pageTitleOfflineBackupDb()
										+ "</a>\n");
						requestAndResponse.print("</div>\n");
					}

					requestAndResponse.print("<div class=\"paneSection\">\n");

					if (!isOfficialSite) {
						requestAndResponse
								.print("<a class=\"externalsite\" title=\""
										+ servletText
												.labelCrushPaperComTooltip()
										+ "\" href=\"http://www.crushpaper.com\">crushpaper.com</a>");

						if (versionNumber != null) {
							requestAndResponse
									.print("<span class=\"versionNumber\">v"
											+ versionNumber + "</span>");
						}
					}

					requestAndResponse
							.print("<a class=\"externalsite\" title=\""
									+ servletText.labelTwitterTooltip()
									+ "\" href=\"https://twitter.com/ZapBlasterson\">Twitter</a>");

					requestAndResponse
							.print("<a class=\"externalsite\" title=\""
									+ servletText.labelGithubTooltip()
									+ "\" href=\"https://github.com/ZapBlasterson/crushpaper\">GitHub</a>");

					requestAndResponse
							.print("<a class=\"externalsite\" title=\""
									+ servletText.labelGoogleGroupTooltip()
									+ "\" href=\"https://groups.google.com/d/forum/crushpaper\">Google Group</a>");

					requestAndResponse.print("</div>");
					requestAndResponse.print("</div></div></div>");
				}

				requestAndResponse
						.print("<div><div class=\"paneContainer\"><div "
								+ (paneId != null ? "id=\"" + paneId + "\" "
										: "") + " class=\"pane contentPane\">");
			}

			final boolean isWelcomePane = paneId != null
					&& paneId.equals("welcome");

			if (!getNoTitle()) {
				requestAndResponse
						.print("<div class=\"headerpane paneSection\">"
								+ "<div class=\""
								+ (isWelcomePane ? "welcomePaneTitle " : "")
								+ " paneTitle\">"
								+ (!isUserAnAdmin && needsAdmin ? servletText
										.notAllowedTitle() : title) + "</div>");

				if (!isWelcomePane) {
					requestAndResponse
							.print("<div class=\"paneButtonsBg\"><div "
									+ (paneId != null ? "id=\"buttons_"
											+ paneId + "\" " : "")
									+ " class=\"paneButtons\">");
					if (includeEdit) {
						requestAndResponse
								.print("<div class=\"editIcon\" onmouseover=\"panePencilOnMouseOver(event); return false;\" onmouseout=\"panePencilOnMouseOut(event); return false;\" onclick=\"panePencilOnClick(event); return false;\"></div>");
					}

					if (includeDelete) {
						requestAndResponse
								.print("<div class=\"deleteIcon\" onclick=\"paneTrashOnClick(event); return false;\"></div>");
					}

					requestAndResponse
							.print("<div title=\""
									+ servletText.tooltipRefreshPane()
									+ "\" class=\"refreshIcon\" onclick=\"refreshPane(event); return false;\"></div>");

					requestAndResponse
							.print("<div title=\""
									+ servletText.tooltipClosePane()
									+ "\" class=\"paneCloseIcon\" onclick=\"closePane(event); return false;\"></div>");

					requestAndResponse.print("</div></div>");
				}

				requestAndResponse.print("</div><div class=\"paneSection\">\n");
			}
		}

		/** Returns true if the request is for a refresh. */
		private boolean getIsForPageRefresh() {
			final String value = requestAndResponse.request
					.getHeader("X-for-refresh");
			return Boolean.valueOf(value);
		}

		/** Returns true if the request does not want the header. */
		private boolean getNoHeader() {
			final String value = requestAndResponse.request
					.getHeader("X-no-header");
			return Boolean.valueOf(value);
		}

		/** Returns true if the request does not want the title. */
		private boolean getNoTitle() {
			final String value = requestAndResponse.request
					.getHeader("X-no-title");
			return Boolean.valueOf(value);
		}

		/** Appends an HTML footer to a response. */
		public void addFooter() throws IOException {
			requestAndResponse
					.print("\n<script type=\"application/json\" class=\"metaDataDictJson\">\n{\n");
			final StringBuilder result = new StringBuilder();
			boolean addedAnyYet = false;
			for (final KeyAndValue keyAndValue : metaData) {
				addedAnyYet = JsonBuilder.addPropertyToJsonString(result,
						keyAndValue.value, addedAnyYet, keyAndValue.key);
			}

			requestAndResponse.print(result.toString());
			requestAndResponse.print("\n}\n</script>\n");

			requestAndResponse
					.print("<div class=\"dragPane\" onmousedown=\"paneOnMouseDown(event);\" ></div></div></div></div></div>");

			final boolean onlyContent = getNoHeader();
			if (!onlyContent) {
				if (!requestAndResponse.skipFooter) {
					requestAndResponse
							.print("</div><div id=\"top\"></div><div id=\"bottom\"></div>");

					requestAndResponse.print("<div id=\"overlay\"></div>\n");

					// For user options.
					stashRequestUser(requestAndResponse);

					requestAndResponse
							.print("\n<script type=\"application/json\" id=\"optionsDictJson\">\n");
					requestAndResponse.print(requestAndResponse.userOptions);
					requestAndResponse.print("\n</script>\n");

					requestAndResponse
							.print("\n<script type=\"application/json\" id=\"sessionDictJson\">{\n");
					if (httpsPort != null) {
						requestAndResponse.print("\"httpsPort\":"
								+ httpsPort.intValue() + ",");
					}

					requestAndResponse.print("\"isSignedIn\":"
							+ isUserSignedIn(requestAndResponse));

					requestAndResponse.print("\n}</script>\n");

					final boolean isForPageRefresh = getIsForPageRefresh();
					if (!isForPageRefresh) {
						requestAndResponse.print("<script type=\"text/javascript\">\n"
								+ "maybeCallFinish();\n"
								+ "</script>\n");
						requestAndResponse.print("</body>" + "</html>");
					}
				}
			}
		}

		public PageWrapper setTitle(String title) {
			this.title = title;
			return this;
		}

		public PageWrapper setPaneId(String paneId) {
			this.paneId = paneId;
			return this;
		}

		public PageWrapper setIncludeEdit() {
			this.includeEdit = true;
			return this;
		}

		public PageWrapper setIncludeDelete() {
			this.includeDelete = true;
			return this;
		}

		/** Adds meta data to the page that will be served to the client. */
		public void addMetaData(KeyAndValue keyAndValue) {
			metaData.add(keyAndValue);
		}

		private final RequestAndResponse requestAndResponse;
		private String title;
		private String paneId;
		private final boolean needsAdmin;
		private boolean includeEdit;
		private boolean includeDelete;
		private final ArrayList<KeyAndValue> metaData = new ArrayList<KeyAndValue>();
	}

	static class KeyAndValue {
		public KeyAndValue(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		final String key;
		final Object value;
	}

	/** Returns true if the user is signed in. */
	private boolean isUserSignedIn(RequestAndResponse requestAndResponse) {
		return isInSingleUserMode()
				|| requestAndResponse.request.getSession().getAttribute(
						sessionUserIdAttribute) != null;
	}

	/** Returns true if the server is in single user mode. */
	private boolean isInSingleUserMode() {
		return singleUserName != null;
	}

	/** Returns an HTML 404. */
	private void returnHtml404(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		final String title = "Error 404";
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId("404");
		pageWrapper.addHeader();
		requestAndResponse.print(servletText.errorPageNotFound());
		pageWrapper.addFooter();
	}

	/** Returns a JSON 200. */
	private void returnJson200(RequestAndResponse requestAndResponse)
			throws ServletException, IOException {
		requestAndResponse.setResponseContentTypeJson();
		requestAndResponse.print("{\"success\":true}");
	}

	/** Returns a JSON 400. */
	private void returnJson400(RequestAndResponse requestAndResponse,
			String text) throws ServletException, IOException {
		requestAndResponse.setResponseContentTypeJson();
		requestAndResponse.response
				.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		requestAndResponse.print("{\"errors\":[" + JsonBuilder.quote(text)
				+ "] }");
	}

	/** Returns a JSON 500. */
	private void returnJson500(RequestAndResponse requestAndResponse,
			String text) throws ServletException, IOException {
		requestAndResponse.setResponseContentTypeJson();
		requestAndResponse.response
				.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		requestAndResponse.print("{\"errors\":[" + JsonBuilder.quote(text)
				+ "] }");
	}

	/** Returns a JSON 400. */
	private void returnJson400(RequestAndResponse requestAndResponse,
			Errors errors) throws ServletException, IOException {
		requestAndResponse.setResponseContentTypeJson();
		requestAndResponse.response
				.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		requestAndResponse.print("{");
		errorsToJson(errors, requestAndResponse.response.getWriter());
		requestAndResponse.print("}");
	}

	/** Helper method. Converts `errors` to JSON. */
	private void errorsToJson(Errors errors, PrintWriter writer) {
		final StringBuilder result = new StringBuilder();
		errorsToJson(errors, result);
		writer.print(result.toString());
	}

	/** Helper method. Converts `errors` to JSON. */
	private void errorsToJson(Errors errors, StringBuilder result) {
		result.append("\"errors\":[");
		if (errors != null && errors.hasErrors()) {
			boolean first = true;
			for (final String text : errors.getTexts()) {
				if (!first) {
					result.append(",");
				}

				first = false;

				result.append(JsonBuilder.quote(text));
			}
		}

		result.append("]");
	}

	/** Helper method. Converts `errors` to HTML. */
	private void errorsToHTML(Errors errors, PrintWriter writer) {
		if (errors != null && errors.hasErrors()) {
			writer.print("<ol>");

			for (final String text : errors.getTexts()) {
				writer.print("<li>");
				writer.print(StringEscapeUtils.escapeHtml4(text));
				writer.print("</li>");
			}

			writer.print("</ol>");
		}
	}

	/** Part of the HTML API. Shows the user's sources. */
	private void handleHtmlShowSources(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleSources();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final String paneId = "sources";
		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId(paneId);
		pageWrapper.addHeader();
		pageWrapper.addMetaData(new KeyAndValue("paneType", paneId));

		pageWrapper.addPageIntroText(servletText.introTextShowSources(false),
				servletText.introTextShowSources(true));

		try {
			final StringBuilder result = new StringBuilder();

			User queryUser = null;
			if (null != (queryUser = canUserSeeUsersData(requestAndResponse,
					true))) {
				final ResultsPaginator paginator = new ResultsPaginator(
						requestAndResponse,
						servletText.sentenceNoSourcesExist(), result,
						servletText);

				startItemList(result, paneId);

				final ArrayList<EntryInfo> entryInfoList = new ArrayList<EntryInfo>();

				final List<?> sources = dbLogic
						.getEntriesByUserIdAndType(queryUser.getId(),
								DbLogic.Constants.source,
								paginator.getStartPosition(),
								paginator.getMaxResults());
				for (final Object sourceUncasted : sources) {
					final Entry entry = (Entry) sourceUncasted;

					final int resultNumber = paginator.next();
					if (resultNumber == -1) {
						continue;
					} else if (resultNumber == 0) {
						break;
					}

					addSourceHtml(entry, result, SourceEmbedContext.InSources,
							null, resultNumber, paneId);

					addEntryToInfoList(entry, entryInfoList);
				}

				finishItemList(result);

				result.append("\n<script type=\"application/json\" class=\"entryInfoDictJson\">\n");
				addJsonForEntryInfos(result, entryInfoList, paneId);
				result.append("\n</script>\n");

				paginator.done();
			}

			dbLogic.commit();

			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			requestAndResponse.print(servletText.errorInternalDatabase());
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Shows search results and displays form. */
	private void handleHtmlSearch(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleSearch();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		String query = requestAndResponse.getParameter("q");
		final boolean queryWasNull = query == null;
		if (query == null) {
			query = "";
		}

		String dataSet = requestAndResponse.getParameter("s");
		if (dataSet == null
				|| (!dataSet.equals("quotations") && !dataSet.equals("sources")
						&& !dataSet.equals("accounts") && !dataSet
							.equals("notebooks"))) {
			dataSet = "notes";
		}

		if (!isUserAnAdmin(requestAndResponse) && dataSet.equals("accounts")) {
			dataSet = "notes";
		}

		query = query.trim();

		final String paneId = "search";
		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId(paneId);
		pageWrapper.addHeader();

		requestAndResponse
				.print("<form action=\""
						+ StringEscapeUtils.escapeHtml4(requestAndResponse
								.getRequestURI())
						+ "\" method=\"GET\"><table class=\"nopadding\"><tr><td>"
						+ "<input class=\"searchbox\" title=\""
						+ servletText.tooltipSearch()
						+ "\" placeholder=\""
						+ servletText.placeholderSearch()
						+ "\" type=\"text\" name=\"q\" value=\""
						+ StringEscapeUtils.escapeHtml4(query)
						+ "\" autofocus></td></tr><tr><td>"
						+ "<span class=\"searchRadio\"><input type=\"radio\" name=\"s\" value=\"notes\" id=\"searchNotes\""
						+ isInputChecked(dataSet, "notes")
						+ "><label for=\"searchNotes\">"
						+ servletText.labelSearchNotes()
						+ "</label></span> "
						+ "<span class=\"searchRadio\"><input type=\"radio\" name=\"s\" value=\"quotations\" id=\"searchQuotations\""
						+ isInputChecked(dataSet, "quotations")
						+ "><label for=\"searchQuotations\">"
						+ servletText.labelSearchQuotations()
						+ "</label></span> "
						+ "<span class=\"searchRadio\"><input type=\"radio\" name=\"s\" value=\"sources\" id=\"searchSources\""
						+ isInputChecked(dataSet, "sources")
						+ "><label for=\"searchSources\">"
						+ servletText.labelSearchSources()
						+ "</label></span> "
						+ "<span class=\"searchRadio\"><input type=\"radio\" name=\"s\" value=\"notebooks\" id=\"searchNotebooks\""
						+ isInputChecked(dataSet, "notebooks")
						+ "><label for=\"searchNotebooks\">"
						+ servletText.labelSearchNotebooks()
						+ "</label></span>"
						+ (!isUserAnAdmin(requestAndResponse) ? ""
								: "<input type=\"radio\" name=\"s\" value=\"accounts\" id=\"searchAccounts\""
										+ isInputChecked(dataSet, "accounts")
										+ "><label for=\"searchAccounts\">"
										+ servletText.labelSearchAccounts()
										+ "</label>")
						+ "</td></tr>"
						+ "<tr><td><button onclick=\"replacePaneForForm(event, '"
						+ servletText.buttonSearch()
						+ "'); return false;\" class=\"specialbutton\" style=\"margin:10px 0px 10px 0px\">"
						+ servletText.buttonSearch()
						+ "</button></td></tr></table></form>");

		if (!queryWasNull && query.isEmpty()) {
			requestAndResponse.print(servletText.errorQueryIsRequired());
		} else if (dataSet == null || dataSet.isEmpty()) {
			requestAndResponse
					.print(servletText.errorSearchDataSetIsRequired());
		} else if (!query.isEmpty()) {
			pageWrapper.addMetaData(new KeyAndValue("paneType", dataSet));

			if (dataSet.equals("notes")) {
				handleHtmlSearchNotes(pageWrapper, requestAndResponse, query, paneId);
			} else if (dataSet.equals("quotations")) {
				handleHtmlSearchQuotations(pageWrapper, requestAndResponse, query, paneId);
			} else if (dataSet.equals("sources")) {
				handleHtmlSearchSources(pageWrapper, requestAndResponse, query, paneId);
			} else if (dataSet.equals("notebooks")) {
				handleHtmlSearchNotebooks(pageWrapper, requestAndResponse, query, paneId);
			} else if (dataSet.equals("accounts")) {
				pageWrapper.addMetaData(new KeyAndValue("notEditable", true));
				handleHtmlSearchAccounts(pageWrapper, requestAndResponse, query, paneId);
			} else {
				requestAndResponse.print(servletText
						.errorSearchDataSetIsRequired());
			}
		}

		pageWrapper.addFooter();
	}

	/**
	 * Returns a string which indicates if the input was checked in a submitted
	 * form.
	 */
	private String isInputChecked(String dataSet, String value) {
		if (dataSet != null && dataSet.equals(value)) {
			return " checked";
		}
		return "";
	}

	/** Helper method. Shows search results within quotations. */
	private void handleHtmlSearchQuotations(PageWrapper pageWrapper,
			RequestAndResponse requestAndResponse, String query, String paneId)
			throws IOException, ServletException {
		pageWrapper.addPageIntroText(
				servletText.introTextSearchQuotations(false),
				servletText.introTextSearchQuotations(true));

		try {
			final StringBuilder result = new StringBuilder();

			User queryUser = null;
			if (null != (queryUser = canUserSeeUsersData(requestAndResponse,
					true))) {
				final ResultsPaginator paginator = new ResultsPaginator(
						requestAndResponse,
						servletText.sentenceThereWereNoMatches(), result,
						servletText);

				try {
					final List<?> results = dbLogic
							.searchEntriesForUserByQuotation(queryUser.getId(),
									query, paginator.getStartPosition(),
									paginator.getMaxResults());
	
					entryListToHtmlAndJson(paneId, result, paginator, results);
				} catch (EmptyQueryException e) {
					requestAndResponse.print(servletText.errorNeedLongerQuery());
				}
			}

			dbLogic.commit();

			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			requestAndResponse.print(servletText.errorInternalDatabase());
		}
	}

	/** Converts the entry list to HTML and JSON. */
	private void entryListToHtmlAndJson(String paneId,
			final StringBuilder result, ResultsPaginator paginator,
			List<?> results) throws IOException {
		entryListToHtmlAndJson(paneId, result, paginator, results,
				SourceEmbedContext.InQuotations);
	}

	/** Converts the entry list to HTML and JSON. */
	private void entryListToHtmlAndJson(String paneId,
			final StringBuilder result, ResultsPaginator paginator,
			List<?> results, SourceEmbedContext embedContext)
			throws IOException {
		startItemList(result, paneId);

		final ArrayList<EntryInfo> entryInfoList = new ArrayList<EntryInfo>();
		for (final Object entryUncasted : results) {
			final Entry entry = (Entry) entryUncasted;
			final int resultNumber = paginator.next();
			if (resultNumber == -1) {
				continue;
			} else if (resultNumber == 0) {
				break;
			}

			addEntryHtmlToList(entry, result, resultNumber, paneId,
					embedContext);

			addEntryToInfoList(entry, entryInfoList);
		}

		finishItemList(result);

		result.append("\n<script type=\"application/json\" class=\"entryInfoDictJson\">\n");
		addJsonForEntryInfos(result, entryInfoList, paneId);
		result.append("\n</script>\n");

		paginator.done();
	}

	/** Helper method. Shows search results within notes. */
	private void handleHtmlSearchNotes(PageWrapper pageWrapper, RequestAndResponse requestAndResponse,
			String query, String paneId) throws IOException, ServletException {
		pageWrapper.addPageIntroText(servletText.introTextSearchNotes(false),
				servletText.introTextSearchNotes(true));

		try {
			final StringBuilder result = new StringBuilder();

			User queryUser = null;
			if (null != (queryUser = canUserSeeUsersData(requestAndResponse,
					true))) {
				final ResultsPaginator paginator = new ResultsPaginator(
						requestAndResponse,
						servletText.sentenceThereWereNoMatches(), result,
						servletText);

				try {
					final List<?> results = dbLogic.searchEntriesForUserByNote(
							queryUser.getId(), query, paginator.getStartPosition(),
							paginator.getMaxResults());
	
					entryListToHtmlAndJson(paneId, result, paginator, results);
				} catch (EmptyQueryException e) {
					requestAndResponse.print(servletText.errorNeedLongerQuery());
				}
			}

			dbLogic.commit();

			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			requestAndResponse.print(servletText.errorInternalDatabase());
		}
	}

	/** Helper method. Shows search results within notebooks. */
	private void handleHtmlSearchNotebooks(PageWrapper pageWrapper,
			RequestAndResponse requestAndResponse, String query, String paneId)
			throws IOException, ServletException {
		pageWrapper.addPageIntroText(
				servletText.introTextSearchNotebooks(false),
				servletText.introTextSearchNotebooks(true));

		try {
			final StringBuilder result = new StringBuilder();

			User queryUser = null;
			if (null != (queryUser = canUserSeeUsersData(requestAndResponse,
					true))) {
				final ResultsPaginator paginator = new ResultsPaginator(
						requestAndResponse,
						servletText.sentenceThereWereNoMatches(), result,
						servletText);

				try {
					final List<?> results = dbLogic
							.searchEntriesForUserByNotebookTitle(queryUser.getId(),
									query, paginator.getStartPosition(),
									paginator.getMaxResults());
	
					entryListToHtmlAndJson(paneId, result, paginator, results);
				} catch (EmptyQueryException e) {
					requestAndResponse.print(servletText.errorNeedLongerQuery());
				}
			}

			dbLogic.commit();

			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			requestAndResponse.print(servletText.errorInternalDatabase());
		}
	}

	/** Part of the JSON API. Returns the matches as JSON. */
	private void handleJsonSearchNotes(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();

		String query = requestAndResponse.getParameter("q");
		if (query == null) {
			query = "";
		}

		query = query.trim();

		if (!isUserSignedIn(requestAndResponse)) {
			returnJson400(requestAndResponse,
					servletText.errorRequiresSignIn(allowSaveIfNotSignedIn));
		} else if (isUsersAccountClosed(requestAndResponse)) {
			returnJson400(requestAndResponse,
					servletText.errorAccountIsClosed());
		} else {
			try {
				final StringBuilder result = new StringBuilder();
				final User user = dbLogic
						.getUserById(getEffectiveUserId(requestAndResponse));
				if (user != null) {
					final ResultsPaginator paginator = new ResultsPaginator(
							requestAndResponse, null, result, null);
					result.append("{ \"results\": [");

					if (user != null) {
						boolean first = true;
						final List<?> results = dbLogic
								.searchEntriesForUserByNote(user.getId(),
										query, paginator.getStartPosition(),
										paginator.getMaxResults());

						for (final Object entryUncasted : results) {
							final Entry entry = (Entry) entryUncasted;

							final int resultNumber = paginator.next();
							if (resultNumber == -1) {
								continue;
							} else if (resultNumber == 0) {
								break;
							}

							if (!first) {
								result.append(",");
								first = false;
							}

							result.append("\n");
							result.append("{ \"id\":\"");
							result.append(entry.getId());
							result.append("\", \"note\":\"");
							result.append(StringEscapeUtils.escapeJson(entry
									.getNoteOrNotebookTitle("")));
							result.append("\", \"quotation\":\"");
							result.append(StringEscapeUtils.escapeJson(entry
									.getQuotation("")));
							result.append("\"}");
						}
					}

					result.append("\n],\n\"more\": "
							+ (paginator.hasMore() ? "true" : "false") + " }\n");
				}

				dbLogic.commit();

				requestAndResponse.print(result.toString());
			} catch (EmptyQueryException e) {
				returnJson400(requestAndResponse,
						servletText.errorNeedLongerQuery());
			} catch (final PersistenceException e) {
				logger.log(Level.INFO, "Exception", e);
				returnJson500(requestAndResponse,
						servletText.errorInternalDatabase());
			}
		}
	}

	/** Returns the entry's note markdown. */
	private String getNoteMarkdown(Entry entry, boolean noLinks, boolean noPlaceholder) {
		final String value = entry.getNoteOrNotebookTitle();
		if (value == null || value.isEmpty()) {
			if(noPlaceholder) {
				return "";
			}
			
			String placeholder = servletText.fragmentBlankNote();
			if (entry.isSource() || entry.isNotebook()) {
				placeholder = servletText.fragmentBlankTitle();
			}

			return "<span class=\"placeholder\">" + placeholder + "</span>";
		}

		return getMarkdownHtml(value, noLinks, false);
	}

	/** Returns the entry's quotation markdown. */
	private String getQuotationMarkdown(Entry entry, boolean noLinks) {
		final String value = entry.getQuotation();
		if (value == null || value.isEmpty()) {
			return "<span class=\"placeholder\">"
					+ servletText.fragmentBlankQuotation() + "</span>";
		}

		return getMarkdownHtml(value, noLinks, false);
	}

	/** Returns the destination directory for a new backup. */
	private File getBackupDestination() {
		final SimpleDateFormat dayFormat = new SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss");

		final String daytime = dayFormat.format(new Date());

		return new File(new File(dbLogic.getDbDirectory().getParentFile(),
				"backups"), daytime);
	}

	/** Part of the HTML API. Performs an offline backup of the database. */
	private void handleHtmlDoOfflineBackup(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String csrft = requestAndResponse.getParameter("csrft");

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				servletText.pageTitleOfflineBackupDb(), true)
				.setPaneId("offlineBackup");
		pageWrapper.addHeader();
		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			requestAndResponse.print(servletText.errorRequiresSignIn(false));
		} else if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			final String source = dbLogic.getDbDirectory().getAbsolutePath();
			final String destination = getBackupDestination().getAbsolutePath();

			// This would be simpler but it throws exceptions on some files:
			// FileUtils.copyRecursively();

			final StringBuffer out = new StringBuffer();
			final StringBuffer err = new StringBuffer();
			final int result = CommandLineUtil.copyDirectory(out, err, source,
					destination);

			if (result == 0) {
				requestAndResponse.print(servletText
						.sentenceOfflineBackupWasSuccessful());
			} else {
				requestAndResponse.print(servletText
						.sentenceOfflineBackupWasNotSuccessful());
			}
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Performs an online backup of the database. */
	private void handleHtmlDoOnlineBackup(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String csrft = requestAndResponse.getParameter("csrft");
		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				servletText.pageTitleOnlineBackupDb(), true)
				.setPaneId("onlineBackup");
		pageWrapper.addHeader();
		if (isTheCsrftWrong(requestAndResponse, csrft)) {

			requestAndResponse.print(servletText.errorRequiresSignIn(false));
		} else if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			final String destination = getBackupDestination().getPath();

			final int numRowsExtracted = dbLogic.doCsvBackup(destination);
			if (numRowsExtracted != -1) {
				requestAndResponse.print(servletText
						.sentenceOnlineBackupWasSuccessful(numRowsExtracted));
			} else {
				requestAndResponse.print(servletText
						.sentenceOnlineBackupWasNotSuccessful());
			}
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Show the offline backup form. */
	private void handleHtmlOfflineBackupForm(
			RequestAndResponse requestAndResponse) throws IOException,
			ServletException {
		final String title = servletText.pageTitleOfflineBackupDb();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, true).setPaneId("offlineBackup");

		pageWrapper.addHeader();

		if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			requestAndResponse.print("<table class=\"nopadding\"><tr><td>");
			requestAndResponse.print(servletText
					.pageTitleOfflineBackupDbTooltip());
			requestAndResponse.print("<br><br>");
			requestAndResponse.print(servletText.offlineBackupDbAreYouSure());
			requestAndResponse.print("</td></tr><tr><td>");

			requestAndResponse
					.print("<form action=\"/doOfflineBackup/"
							+ "\" method=\"POST\">"
							+ "<input type=\"hidden\" name=\"csrft\" value=\""
							+ getCsrft(requestAndResponse)
							+ "\">"
							+ "<button onclick=\"replacePaneForForm(event, '"
							+ servletText.pageTitleOfflineBackupDb()
							+ "', refreshBackupsPane); return false;\" class=\"specialbutton withTopMargin\">"
							+ servletText.pageTitleOfflineBackupDb()
							+ "</button></form>");
			requestAndResponse.print("</td></tr></table>");
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Show the online backup form. */
	private void handleHtmlOnlineBackupForm(
			RequestAndResponse requestAndResponse) throws IOException,
			ServletException {
		final String title = servletText.pageTitleOnlineBackupDb();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, true).setPaneId("onlineBackup");

		pageWrapper.addHeader();

		if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			requestAndResponse.print("<table class=\"nopadding\"><tr><td>");
			requestAndResponse.print(servletText
					.pageTitleOnlineBackupDbTooltip());
			requestAndResponse.print("<br><br>");
			requestAndResponse.print(servletText.onlineBackupDbAreYouSure());
			requestAndResponse.print("</td></tr><tr><td>");

			requestAndResponse
					.print("<form action=\"/doOnlineBackup/"
							+ "\" method=\"POST\">"
							+ "<input type=\"hidden\" name=\"csrft\" value=\""
							+ getCsrft(requestAndResponse)
							+ "\">"
							+ "<button onclick=\"replacePaneForForm(event, '"
							+ servletText.pageTitleOnlineBackupDb()
							+ "', refreshBackupsPane); return false;\" class=\"specialbutton withTopMargin\">"
							+ servletText.pageTitleOnlineBackupDb()
							+ "</button></form>");
			requestAndResponse.print("</td></tr></table>");
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Show the clear form. */
	private void handleHtmlClearForm(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleClearDb();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, true).setPaneId("clear");

		pageWrapper.addHeader();

		if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			requestAndResponse.print("<table class=\"nopadding\"><tr><td>");
			requestAndResponse.print(servletText.pageTitleClearDbTooltip());
			requestAndResponse.print("<br><br>");
			requestAndResponse.print(servletText.clearAreYouSure());
			requestAndResponse.print("</td></tr><tr><td>");

			requestAndResponse
					.print("<form action=\"/doClear/"
							+ "\" method=\"POST\">"
							+ "<input type=\"hidden\" name=\"csrft\" value=\""
							+ getCsrft(requestAndResponse)
							+ "\">"
							+ "<button onclick=\"replacePaneForForm(event, '"
							+ servletText.pageTitleClearDb()
							+ "'); return false;\" class=\"specialbutton withTopMargin\">"
							+ servletText.pageTitleClearDb()
							+ "</button></form>");
			requestAndResponse.print("</td></tr></table>");
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Clears the database. */
	private void handleHtmlDoClear(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				servletText.pageTitleClearDb(), true).setPaneId("clear");
		pageWrapper.addHeader();
		final String csrft = requestAndResponse.getParameter("csrft");
		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			requestAndResponse.print(servletText.errorRequiresSignIn(false));
		} else if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			dbLogic.clearData();
			if (sessionManager != null) {
				try {
					sessionManager.reallyShutdownSessions();
				} catch (final Exception e) {
				}
			}

			createTheUserForSingleUserMode();

			requestAndResponse.print(servletText.sentenceCleared());
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Shuts the process down. */
	private void handleHtmlDoShutdown(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				servletText.pageTitleShutdown(), true).setPaneId("shutdown");
		pageWrapper.addHeader();

		final String csrft = requestAndResponse.getParameter("csrft");
		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			requestAndResponse.print(servletText.errorRequiresSignIn(false));
		} else if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			requestAndResponse.print(servletText.sentenceShuttingdown());
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
					}

					System.exit(0);
				}
			}.start();
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Show the shutdown form. */
	private void handleHtmlShutdownForm(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleShutdown();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, true).setPaneId("shutdown");

		pageWrapper.addHeader();

		if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			requestAndResponse.print("<table class=\"nopadding\"><tr><td>");
			requestAndResponse.print(servletText.shutdownAreYouSure());
			requestAndResponse.print("</td></tr><tr><td>");
			requestAndResponse
					.print("<form action=\"/doShutdown/"
							+ "\" method=\"POST\">"
							+ "<input type=\"hidden\" name=\"csrft\" value=\""
							+ getCsrft(requestAndResponse)
							+ "\">"
							+ "<button onclick=\"replacePaneForForm(event, '"
							+ servletText.pageTitleShutdown()
							+ "'); return false;\" class=\"specialbutton withTopMargin\">"
							+ servletText.pageTitleShutdown()
							+ "</button></form>");
			requestAndResponse.print("</td></tr></table>");
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Restores a backup. */
	private void handleHtmlShowRestoreBackupCommand(
			RequestAndResponse requestAndResponse) throws IOException,
			ServletException {
		final String title = servletText.pageTitleRestoreBackupCommandDb();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, true);
		pageWrapper.addHeader();

		if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			final String name = requestAndResponse.getParameter("name");
			if (name == null) {
				requestAndResponse.print(servletText
						.errorNoNameSpecifiedForRestoration());
			} else {
				final File source = new File(new File(dbLogic.getDbDirectory()
						.getParent(), "backups"), name);

				boolean isOnlineBackup = false;

				// Determine if the backup directory is the result of an online
				// or offline backup.
				final File[] listOfFiles = source.listFiles();
				if (listOfFiles != null) {
					for (int i = 0; i < listOfFiles.length; i++) {
						if (listOfFiles[i].getName().endsWith(".csv")) {
							isOnlineBackup = true;
							break;
						}
					}
				}

				String cmd = null;
				if (isOnlineBackup) {
					cmd = "DELETE * FROM USR;\n";
					cmd += "DELETE * FROM ENTRY;\n";
					cmd += "INSERT INTO USR SELECT * FROM CSVREAD('"
							+ (source.getAbsolutePath() + File.separator).replace("\\",
									"\\\\") + "usr.csv');\n";
					cmd += "INSERT INTO ENTRY SELECT * FROM CSVREAD('"
							+ (source.getAbsolutePath() + File.separator).replace("\\",
									"\\\\") + "entry.csv');";
				} else {
					final String destination = dbLogic.getDbDirectory()
							.getAbsolutePath();
					cmd = CommandLineUtil
							.getArgsForCopyAndPaste(CommandLineUtil
									.getRmDirArgs(destination))
							+ " && "
							+ CommandLineUtil
									.getArgsForCopyAndPaste(CommandLineUtil
											.getCopyDirectoryArgs(
													source.getAbsolutePath(),
													destination));
				}

				String htmlCmd = StringEscapeUtils.escapeHtml4(cmd);
				htmlCmd = htmlCmd.replace("\n", "<br><br>");
				requestAndResponse.print(servletText.sentenceCmdForRestore()
						+ "<br><br>" + htmlCmd);
			}
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Show the check for errors form. */
	private void handleHtmlCheckForErrorsForm(
			RequestAndResponse requestAndResponse) throws IOException,
			ServletException {
		final String title = servletText.pageTitleCheckDbForErrors();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, true).setPaneId("checkForErrors");

		pageWrapper.addHeader();

		if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			requestAndResponse.print("<table class=\"nopadding\"><tr><td>");
			requestAndResponse.print(servletText.checkDbForErrorsAreYouSure());
			requestAndResponse.print("</td></tr><tr><td>");

			requestAndResponse
					.print("<form action=\"/doCheckForErrors/"
							+ "\" method=\"POST\">"
							+ "<input type=\"hidden\" name=\"csrft\" value=\""
							+ getCsrft(requestAndResponse)
							+ "\">"
							+ "<button onclick=\"replacePaneForForm(event, '"
							+ servletText.pageTitleCheckDbForErrors()
							+ "'); return false;\" class=\"specialbutton withTopMargin\">"
							+ servletText.pageTitleCheckDbForErrors()
							+ "</button></form>");
			requestAndResponse.print("</td></tr></table>");
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Checks the database for errors. */
	private void handleHtmlDoCheckForErrors(
			RequestAndResponse requestAndResponse) throws IOException,
			ServletException {
		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				servletText.pageTitleCheckDbForErrors(), true)
				.setPaneId("checkForErrors");
		pageWrapper.addHeader();

		final String csrft = requestAndResponse.getParameter("csrft");
		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			requestAndResponse.print(servletText.errorRequiresSignIn(false));
		} else if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			final Errors errors = new Errors();
			final boolean hasErrors = dbLogic.hasErrors(errors);
			requestAndResponse.print(hasErrors ? servletText
					.sentenceTheDatabaseHasErrors() : servletText
					.sentenceTheDatabaseHasNoErrors());
			errorsToHTML(errors, requestAndResponse.response.getWriter());
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Show the list of backups. */
	private void handleHtmlShowDBBackups(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleShowBackups();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, true).setPaneId("backups");
		pageWrapper.addHeader();

		if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			final File backupsDirectory = new File(dbLogic.getDbDirectory()
					.getParent(), "backups");
			requestAndResponse.print(servletText.fragmentShowingContentsOf()
					+ " " + backupsDirectory.getAbsolutePath() + "<br><br>");

			boolean anyBackups = false;

			final StringBuilder result = new StringBuilder();
			result.append("<ol>");
			try {
				for (final File file : backupsDirectory.listFiles()) {
					anyBackups = true;
					result.append("<li>");

					result.append(" <a onclick=\"showPopupWithPage(event, '"
							+ servletText.pageTitleRestoreBackupCommandDb()
							+ "'); return false;\" class=\"cursorIsPointer\" title=\""
							+ servletText.linkShowRestoreBackupCmdTooltip()
							+ "\" href=\"/restoreBackupCommand/?name="
							+ file.getName() + "\">");

					result.append(file.getName());

					result.append("</a></li>");
				}
			} catch (final Exception e) {
			}

			result.append("</ol>");

			if (!anyBackups) {
				requestAndResponse.print(servletText
						.textNoBackupsHaveBeenCreated());
			} else {
				requestAndResponse.print(servletText.sentenceToRestoreCommand()
						+ "<br>");
				requestAndResponse.print(result.toString());
			}
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Show nothing. */
	private void handleHtmlNothing(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.print(servletText.sentenceNothingHere());
	}

	/** Part of the HTML API. Show the import form. */
	private void handleHtmlImportForm(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleImport();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId("import");
		pageWrapper.addHeader();

		if (!isUserSignedIn(requestAndResponse)) {
			requestAndResponse.print(servletText
					.errorRequiresSignIn(allowSaveIfNotSignedIn));
		} else if (isUsersAccountClosed(requestAndResponse)) {
			requestAndResponse.print(servletText.errorAccountIsClosed());
		} else {
			pageWrapper.addPageIntroText(servletText.introTextImport(), null);

			requestAndResponse
					.print("<iframe src=\"/importFrame/\" allowtransparency=\"true\"></iframe>");
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Show the import frame. */
	private void handleHtmlImportFrame(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		addIFrameHeader(requestAndResponse);

		if (!isUserSignedIn(requestAndResponse)) {
			requestAndResponse.print(servletText
					.errorRequiresSignIn(allowSaveIfNotSignedIn));
		} else if (isUsersAccountClosed(requestAndResponse)) {
			requestAndResponse.print(servletText.errorAccountIsClosed());
		} else {
			requestAndResponse
					.print("<form action=\"/doImport/\" method=\"POST\" enctype=\"multipart/form-data\">"
							+ "<input type=\"hidden\" name=\"csrft\" value=\""
							+ getCsrft(requestAndResponse)
							+ "\">"
							+ "<table class=\"nopadding\"><tr><td>"
							+ "<input type=\"file\" name=\"file\"/>"
							+ "</td></tr><tr><td>"
							+ "<input type=\"checkbox\" name=\"reuseIds\" id=\"reuseIds\" checked><label for=\"reuseIds\">"
							+ servletText.sentenceReuseIds()
							+ "</label><br>"
							+ "</td></tr><tr><td>"
							+ "<button class=\"specialbutton withTopMargin\">"
							+ servletText.buttonImport()
							+ "</button>"
							+ "</td></tr></table>" + "</form>");
		}

		addIFrameFooter(requestAndResponse);
	}

	/**
	 * Adds a success message to the response.
	 * 
	 * @throws IOException
	 */
	private void addSuccessMessage(RequestAndResponse requestAndResponse,
			String message) throws IOException {
		requestAndResponse.print("<span class=\"successMessage\">" + message
				+ "</span>");
	}

	/**
	 * Adds a success message to the response.
	 * 
	 * @throws IOException
	 */
	private void addErrorMessage(RequestAndResponse requestAndResponse,
			String message) throws IOException {
		requestAndResponse.print("<span class=\"errorMessage\">" + message
				+ "</span>");
	}

	/** Part of the HTML API. Show the account page. */
	private void handleHtmlShowAccount(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleViewAccount();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId("account");

		pageWrapper.addHeader();

		if (isUserALocalAdminOrNotClosed(requestAndResponse)) {
			try {
				final String userId = getURIParameterOrUserId(requestAndResponse);

				final User currentUser = dbLogic
						.getUserById(getEffectiveUserId(requestAndResponse));

				final User editedUser = dbLogic.getUserById(userId);

				if (editedUser == null) {
					requestAndResponse.print(servletText.errorNoAccountFound());
				} else {
					final boolean currentIsEditedUser = isCurrentUserTheEditedUser(
							currentUser, editedUser);
					final boolean isUserAdmin = isUserAnAdmin(requestAndResponse);
					if (!isUserAdmin && !currentIsEditedUser) {
						requestAndResponse.print(servletText
								.errorPageNotAllowed());
					} else {
						final boolean canChangeIsAdmin = isUserAdmin;

						requestAndResponse.print(servletText
								.sentenceUsernameIs(currentIsEditedUser,
										StringEscapeUtils
												.escapeHtml4(editedUser
														.getUserName()))
								+ "<br>");

						requestAndResponse.print(servletText.sentenceEmailIs(
								currentIsEditedUser, StringEscapeUtils
										.escapeHtml4(editedUser
												.getEmailOrBlank()))
								+ "<br>");

						requestAndResponse.print(servletText
								.sentenceMayBeContacted(currentIsEditedUser,
										editedUser.getMayContact())
								+ "<br>");

						if (canChangeIsAdmin) {
							if (editedUser.getIsAccountClosed()) {
								requestAndResponse
										.print(servletText
												.sentenceAccountIsClosed(currentIsEditedUser)
												+ "<br>");
							}

							if (editedUser.getIsAdmin()) {
								requestAndResponse.print(servletText
										.sentenceIsAnAdmin(currentIsEditedUser)
										+ "<br>");
							}
						}

						requestAndResponse
								.print("<table class=\"accountButtons\"><tr><td>");

						requestAndResponse
								.print("<form action=\"/changeAccount/"
										+ (currentIsEditedUser ? "" : userId)
										+ "\" method=\"GET\">"
										+ "<button onclick=\"replacePaneForForm(event, '"
										+ servletText
												.buttonChangeAccountDetails()
										+ "'); return false;\" class=\"specialbutton\">"
										+ servletText
												.buttonChangeAccountDetails()
										+ "</button></form><br>");

						requestAndResponse.print("</td><td>");

						requestAndResponse
								.print("<form action=\"/changePassword/"
										+ (currentIsEditedUser ? "" : userId)
										+ "\" method=\"GET\">"
										+ "<button onclick=\"replacePaneForForm(event, '"
										+ servletText.buttonChangePassword()
										+ "'); return false;\" class=\"specialbutton\">"
										+ servletText.buttonChangePassword()
										+ "</button></form><br>");

						if (!editedUser.getIsAccountClosed()) {
							requestAndResponse.print("</td><td>");
							requestAndResponse
									.print("<form action=\"/closeAccount/"
											+ (currentIsEditedUser ? ""
													: userId)
											+ "\" method=\"GET\">"
											+ "<button onclick=\"replacePaneForForm(event, '"
											+ servletText.buttonCloseAccount()
											+ "'); return false;\" class=\"specialbutton\">"
											+ servletText.buttonCloseAccount()
											+ "</button></form><br>");
						}

						requestAndResponse.print("</td></tr></table>");

						if (!currentIsEditedUser) {
							requestAndResponse.print("<hr class=\"title\"/>"
									+ servletText.sentenceSeeWhatTheUserSees()
									+ "<ul>");

							addUserLink(requestAndResponse,
									servletText.pageTitleNotebooks(),
									servletText
											.pageTitleUsersNotebooksTooltip(),
									"/notebooks", "notebooks", editedUser);
							addUserLink(requestAndResponse,
									servletText.pageTitleQuotations(),
									servletText
											.pageTitleUsersQuotationsTooltip(),
									"/quotations", "quotations", editedUser);
							addUserLink(requestAndResponse,
									servletText.pageTitleSources(),
									servletText.pageTitleUsersSourcesTooltip(),
									"/sources", "sources", editedUser);
							addUserLink(requestAndResponse,
									servletText.pageTitleSearch(),
									servletText.pageTitleUsersSearchTooltip(),
									"/search", "search", editedUser);

							requestAndResponse.print("</ul>");
						}
					}
				}

				dbLogic.commit();
			} catch (final PersistenceException e) {
				requestAndResponse.print(servletText.errorInternalDatabase());
			}
		}

		pageWrapper.addFooter();
	}

	/** Returns true if the current user is the edited user. */
	private boolean isCurrentUserTheEditedUser(final User currentUser,
			final User editedUser) {
		return currentUser != null && editedUser != null
				&& editedUser.getUserName().equals(currentUser.getUserName());
	}

	/** Part of the HTML API. Show the change account form. */
	private void handleHtmlChangeAccount(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleChangeAccount();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId("account");
		pageWrapper.addHeader();

		if (isUserALocalAdminOrNotClosed(requestAndResponse)) {
			try {
				final String userId = getURIParameterOrUserId(requestAndResponse);

				final User currentUser = dbLogic
						.getUserById(getEffectiveUserId(requestAndResponse));

				final User editedUser = dbLogic.getUserById(userId);
				if (editedUser == null) {
					requestAndResponse.print(servletText.errorNoAccountFound());
				} else {
					final boolean currentIsEditedUser = isCurrentUserTheEditedUser(
							currentUser, editedUser);
					final boolean isUserAdmin = isUserAnAdmin(requestAndResponse);
					if (!isUserAdmin && !currentIsEditedUser) {
						requestAndResponse.print(servletText
								.errorPageNotAllowed());
					} else {
						final boolean canChangeIsAdmin = isUserAdmin;
						final String submitted = requestAndResponse.request
								.getParameter("save");
						boolean needsForm = true;
						if (submitted != null) {
							boolean needsChange = false;
							boolean hasErrors = false;

							if (isTheCsrftWrong(requestAndResponse,
									requestAndResponse.request
											.getParameter("csrft"))) {
								requestAndResponse.print(servletText
										.errorRequiresSignIn(false));
								needsForm = false;
							}

							// Validate new username.
							String changedUserName = null;
							String newUserName = requestAndResponse.request
									.getParameter("username");
							if (newUserName != null) {
								newUserName = newUserName.toLowerCase();
							}

							final String oldUserName = editedUser.getUserName();
							if (newUserName != null && !newUserName.isEmpty()
									&& !newUserName.equals(oldUserName)) {
								if (!editedUser.getIsAnon()) {
									addErrorMessage(
											requestAndResponse,
											servletText
													.errorUsernameMayNotBeChanged());
									hasErrors = true;
								} else if (!AccountAttributeValidator
										.isUserNameValid(newUserName)) {
									addErrorMessage(requestAndResponse,
											servletText
													.errorUserNameIsNotValid());
									hasErrors = true;
								} else if (dbLogic
										.getUserByUserName(newUserName) != null) {
									addErrorMessage(
											requestAndResponse,
											servletText
													.errorUserNameIsAlreadyTaken());
									hasErrors = true;
								} else {
									changedUserName = newUserName;
									needsChange = true;
								}
							}

							// Validate email.
							String changedEmail = null;
							String email = requestAndResponse.request
									.getParameter("email");
							if (email != null && email.isEmpty()) {
								email = null;
							}

							boolean emailIsChanged = false;
							if (email == null && editedUser.getEmail() != null) {
								changedEmail = email;
								needsChange = true;
								emailIsChanged = true;
							} else if (email != null
									&& !AccountAttributeValidator
											.isEmailValid(email)) {
								addErrorMessage(requestAndResponse,
										servletText.errorEmailIsNotValid());
								hasErrors = true;
							} else if (email != null) {
								changedEmail = email;
								needsChange = true;
								emailIsChanged = true;
							}

							// Validate mayContact.
							final String mayContactString = requestAndResponse.request
									.getParameter("mayContact");
							final boolean mayContact = mayContactString != null
									&& mayContactString.equals("on");
							if (mayContact != editedUser.getMayContact()) {
								needsChange = true;
							}

							// Validate isAccountClosed.
							final String isAccountClosedString = requestAndResponse.request
									.getParameter("isAccountClosed");
							final boolean isAccountClosed = isAccountClosedString != null
									&& isAccountClosedString.equals("on");
							if (isAccountClosed != editedUser
									.getIsAccountClosed()) {
								needsChange = true;
							}

							// Validate isAdmin.
							final String isAdminString = requestAndResponse.request
									.getParameter("isAdmin");
							final boolean isAdmin = isAdminString != null
									&& isAdminString.equals("on");
							if (isAdmin != editedUser.getIsAdmin()) {
								if (!isAdmin && editedUser.getIsSingleUser()) {
									hasErrors = true;
									addErrorMessage(
											requestAndResponse,
											servletText
													.errorSingleUserMustStayAnAdmin());
								}

								if (!canChangeIsAdmin) {
									hasErrors = true;
									addErrorMessage(
											requestAndResponse,
											servletText
													.errorOnlyAnAdminCanChangeIsAdmin());
								}

								needsChange = true;
							}

							// Make the changes.
							if (needsChange && !hasErrors) {
								final Long time = new Long(
										System.currentTimeMillis());
								editedUser.setModTime(time);

								if (changedUserName != null) {
									editedUser.setUserName(changedUserName);
									editedUser.setIsAnon(false);
								}

								if (emailIsChanged) {
									editedUser.setEmail(changedEmail);
								}

								editedUser.setMayContact(mayContact);

								if (canChangeIsAdmin) {
									editedUser.setIsAdmin(isAdmin);
									editedUser
											.setIsAccountClosed(isAccountClosed);
								}

								addSuccessMessage(requestAndResponse,
										servletText.sentenceChangesWereSaved());
								needsForm = false;
							} else if (hasErrors) {
								addErrorMessage(requestAndResponse,
										servletText.errorChangesWereNotSaved());
							} else {
								addErrorMessage(requestAndResponse,
										servletText.errorNoChangesToSave());
							}
						}

						if (needsForm) {
							requestAndResponse
									.print("<div class=\"infoheader\">"
											+ servletText
													.sentenceEnterNewAccountDetailsHere(
															currentIsEditedUser,
															editedUser
																	.getUserName())
											+ "</div>");

							requestAndResponse
									.print("<form action=\"/changeAccount/"
											+ (currentIsEditedUser ? ""
													: userId)
											+ "\" method=\"POST\"><div class=\"account\">"
											+ "<input type=\"hidden\" name=\"csrft\" value=\""
											+ getCsrft(requestAndResponse)
											+ "\">");

							if (editedUser.getIsAnon()) {
								requestAndResponse
										.print("<div class=\"infoheader\">"
												+ servletText
														.sentencePleaseChangeNameFromGenerated(
																currentIsEditedUser,
																editedUser
																		.getUserName())
												+ "</div>");

								requestAndResponse
										.print("<input autocorrect=\"off\" type=\"text\" id=\"username\" name=\"username\" placeholder=\""
												+ servletText
														.sentenceChooseAUserName()
												+ "\" maxlength=\"20\"><br>");
							}

							requestAndResponse
									.print("<input type=\"email\" id=\"email\" name=\"email\" placeholder=\""
											+ servletText
													.sentenceEmailOptional()
											+ "\" maxlength=\"100\" value=\""
											+ StringEscapeUtils
													.escapeHtml4(editedUser
															.getEmailOrBlank())
											+ "\"><br>");

							requestAndResponse
									.print("<input type=\"checkbox\" name=\"mayContact\" id=\"mayContact\""
											+ (editedUser.getMayContact() ? " checked"
													: "")
											+ "><label for=\"mayContact\">"
											+ (currentIsEditedUser ? servletText
													.sentenceIMayBeContacted()
													: servletText
															.sentenceUserMayBeContacted())
											+ "</label><br>");

							if (canChangeIsAdmin) {
								requestAndResponse
										.print("<input type=\"checkbox\" name=\"isAccountClosed\" id=\"isAccountClosed\""
												+ (editedUser
														.getIsAccountClosed() ? " checked"
														: "")
												+ "><label for=\"isAccountClosed\">"
												+ servletText
														.sentenceIsAccountClosed()
												+ "</label><br>");

								requestAndResponse
										.print("<input type=\"checkbox\" name=\"isAdmin\" id=\"isAdmin\""
												+ (editedUser.getIsAdmin() ? " checked"
														: "")
												+ "><label for=\"isAdmin\">"
												+ servletText
														.sentenceUserIsAnAdmin()
												+ "</label><br>");
							}

							requestAndResponse
									.print("<table class=\"responseAndSave\"><tr>"
											+ "<td><div id=\"response\"></div></td>"
											+ "<td><button onclick=\"replacePaneForForm(event, '"
											+ servletText
													.buttonChangeAccountDetails()
											+ "'); return false;\" id=\"save\" name=\"save\" class=\"specialbutton\" style=\"float:right; margin-top:10px;\">"
											+ servletText
													.buttonChangeAccountDetails()
											+ "</button></td>"
											+ "</tr></table></div>" + "</form>");
						}
					}
				}

				dbLogic.commit();
			} catch (final PersistenceException e) {
				requestAndResponse.print(servletText.errorInternalDatabase());
			}
		}

		pageWrapper.addFooter();
	}

	/**
	 * Returns true if the user is either local admin or a signed in user whose
	 * account is not closed.
	 */
	boolean isUserALocalAdminOrNotClosed(RequestAndResponse requestAndResponse)
			throws IOException {
		if (isUserALocalAdmin(requestAndResponse)) {
			return true;
		}

		if (!isUserSignedIn(requestAndResponse)) {
			requestAndResponse.print(servletText.errorRequiresSignIn(false));
			return false;
		} else if (isUsersAccountClosed(requestAndResponse)) {
			requestAndResponse.print(servletText.errorAccountIsClosed());
			return false;
		}

		return true;
	}

	/** Part of the HTML API. Show the close account form. */
	private void handleHtmlCloseAccount(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleCloseAccount();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId("account");
		pageWrapper.addHeader();

		if (isUserALocalAdminOrNotClosed(requestAndResponse)) {
			try {
				final String userId = getURIParameterOrUserId(requestAndResponse);

				final User currentUser = dbLogic
						.getUserById(getEffectiveUserId(requestAndResponse));

				final User editedUser = dbLogic.getUserById(userId);
				if (editedUser == null) {
					requestAndResponse.print(servletText.errorNoAccountFound());
				} else {
					final boolean currentIsEditedUser = isCurrentUserTheEditedUser(
							currentUser, editedUser);
					final boolean isUserAdmin = isUserAnAdmin(requestAndResponse);
					if (!isUserAdmin && !currentIsEditedUser) {
						requestAndResponse.print(servletText
								.errorPageNotAllowed());
					} else {
						boolean needsForm = true;
						boolean needsCurrentPassword = isCurrentPasswordNeeded(
								editedUser, currentIsEditedUser, isUserAdmin);
						final String submitted = requestAndResponse.request
								.getParameter("save");
						if (submitted != null) {
							boolean needsChange = false;
							boolean hasErrors = false;

							if (isTheCsrftWrong(requestAndResponse,
									requestAndResponse.request
											.getParameter("csrft"))) {
								requestAndResponse.print(servletText
										.errorRequiresSignIn(false));
								needsForm = false;
							} else {
								// Validate old password.
								if (needsCurrentPassword) {
									final String realPassword = editedUser
											.getPassword();
									final String currentPassword = requestAndResponse.request
											.getParameter("currentpassword");
									if (currentPassword == null
											|| currentPassword.isEmpty()) {
										addErrorMessage(
												requestAndResponse,
												servletText
														.errorPasswordMustNotBeBlank());
										hasErrors = true;
									} else if (!AccountAttributeValidator
											.isPasswordValid(currentPassword)) {
										addErrorMessage(
												requestAndResponse,
												servletText
														.errorCurrentPasswordIsIncorrect());
										hasErrors = true;
									} else if (realPassword == null
											|| !realPassword.equals(DigestUtils
													.sha1Hex(currentPassword))) {
										addErrorMessage(
												requestAndResponse,
												servletText
														.errorCurrentPasswordIsIncorrect());
										hasErrors = true;
									}
								}

								needsChange = !editedUser.getIsAccountClosed();

								// Make the changes.
								if (needsChange && !hasErrors) {
									final Long time = new Long(
											System.currentTimeMillis());
									editedUser.setModTime(time);

									editedUser.setIsAccountClosed(true);

									addSuccessMessage(requestAndResponse,
											servletText
													.sentenceChangesWereSaved());
									needsForm = false;
								} else if (hasErrors) {
									addErrorMessage(requestAndResponse,
											servletText
													.errorChangesWereNotSaved());
								} else {
									addErrorMessage(requestAndResponse,
											servletText.errorNoChangesToSave());
									needsForm = false;
								}
							}
						}

						if (needsForm) {
							// Recompute this in case the values have changed.
							needsCurrentPassword = isCurrentPasswordNeeded(
									editedUser, currentIsEditedUser,
									isUserAdmin);

							requestAndResponse
									.print("<form action=\"/closeAccount/"
											+ (currentIsEditedUser ? ""
													: userId)
											+ "\" method=\"POST\"><div class=\"account\">"
											+ "<input type=\"hidden\" name=\"csrft\" value=\""
											+ getCsrft(requestAndResponse)
											+ "\">"
											+ "<div class=\"infoheader\">"
											+ servletText
													.sentenceSureYouWantToCloseAccount(
															currentIsEditedUser,
															StringEscapeUtils
																	.escapeHtml4(editedUser
																			.getUserName()))
											+ "</div>");

							if (needsCurrentPassword) {
								requestAndResponse
										.print("<input type=\"password\" id=\"currentpassword\" name=\"currentpassword\" placeholder=\""
												+ servletText
														.sentenceCurrentPassword(currentIsEditedUser)
												+ "\" maxlength=\"20\"><br>");
							}

							requestAndResponse
									.print("<table class=\"responseAndSave\"><tr>"
											+ "<td><div id=\"response\"></div></td>"
											+ "<td><button onclick=\"replacePaneForForm(event, '"
											+ servletText.buttonCloseAccount()
											+ "'); return false;\" id=\"save\" name=\"save\" class=\"specialbutton\" style=\"float:right; margin-top:10px;\">"
											+ servletText.buttonCloseAccount()
											+ "</button></td>"
											+ "</tr></table></div>" + "</form>");
						}
					}
				}

				dbLogic.commit();
			} catch (final PersistenceException e) {
				requestAndResponse.print(servletText.errorInternalDatabase());
			}
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Change the password. */
	private void handleHtmlChangePassword(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleChangePassword();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId("account");
		pageWrapper.addHeader();

		if (isUserALocalAdminOrNotClosed(requestAndResponse)) {
			try {
				final String userId = getURIParameterOrUserId(requestAndResponse);

				final User currentUser = dbLogic
						.getUserById(getEffectiveUserId(requestAndResponse));

				final User editedUser = dbLogic.getUserById(userId);
				if (editedUser == null) {
					requestAndResponse.print(servletText.errorNoAccountFound());
				} else {
					final boolean currentIsEditedUser = isCurrentUserTheEditedUser(
							currentUser, editedUser);
					final boolean isUserAdmin = isUserAnAdmin(requestAndResponse);
					if (!isUserAdmin && !currentIsEditedUser) {
						requestAndResponse.print(servletText
								.errorPageNotAllowed());
					} else {
						boolean needsCurrentPassword = isCurrentPasswordNeeded(
								editedUser, currentIsEditedUser, isUserAdmin);
						boolean showForm = true;
						final String submitted = requestAndResponse.request
								.getParameter("save");
						if (submitted != null) {
							boolean needsChange = false;
							boolean hasErrors = false;

							if (isTheCsrftWrong(requestAndResponse,
									requestAndResponse.request
											.getParameter("csrft"))) {
								requestAndResponse.print(servletText
										.errorRequiresSignIn(false));
								showForm = false;
							}

							// Validate new passwords.
							String changedPassword = null;
							final String newPassword = requestAndResponse.request
									.getParameter("newpassword");
							final String newPassword2 = requestAndResponse.request
									.getParameter("newpassword2");
							if (newPassword == null || newPassword.isEmpty()) {
								addErrorMessage(requestAndResponse,
										servletText
												.errorFirstPasswordMustBeSet(
														currentIsEditedUser,
														editedUser
																.getUserName()));
								hasErrors = true;
							}

							if (newPassword2 == null || newPassword2.isEmpty()) {
								addErrorMessage(requestAndResponse,
										servletText
												.errorSecondPasswordMustBeSet(
														currentIsEditedUser,
														editedUser
																.getUserName()));
								hasErrors = true;
							}

							if (!hasErrors && newPassword != null
									&& newPassword2 != null
									&& !newPassword2.equals(newPassword)) {
								addErrorMessage(requestAndResponse,
										servletText.errorPasswordsMustMatch());
								hasErrors = true;
							}

							if (!hasErrors
									&& !AccountAttributeValidator
											.isPasswordValid(newPassword)) {
								addErrorMessage(requestAndResponse,
										servletText.errorPasswordIsNotValid());
								hasErrors = true;
							}

							if (!hasErrors
									&& editedUser.getPasswordOrBlank().equals(
											DigestUtils.sha1Hex(newPassword))) {
								addErrorMessage(
										requestAndResponse,
										servletText
												.errorNewPasswordIsTheSameAsTheCurrent());
								hasErrors = true;
							}

							if (!hasErrors) {
								changedPassword = newPassword;
								needsChange = true;
							}

							// Validate old password.
							if (needsCurrentPassword) {
								final String realPassword = editedUser
										.getPassword();
								final String currentPassword = requestAndResponse.request
										.getParameter("currentpassword");
								if (!AccountAttributeValidator
										.isPasswordValid(currentPassword)) {
									addErrorMessage(
											requestAndResponse,
											servletText
													.errorCurrentPasswordIsNotValid());
									hasErrors = true;
								} else if (realPassword == null
										|| !realPassword.equals(DigestUtils
												.sha1Hex(currentPassword))) {
									addErrorMessage(
											requestAndResponse,
											servletText
													.errorCurrentPasswordIsIncorrect());
									hasErrors = true;
								}
							}

							// Make the changes.
							if (needsChange && !hasErrors) {
								final Long time = new Long(
										System.currentTimeMillis());
								editedUser.setModTime(time);

								if (changedPassword != null) {
									editedUser.setPassword(DigestUtils
											.sha1Hex(changedPassword));
								}

								addSuccessMessage(requestAndResponse,
										servletText.sentenceChangesWereSaved());
								showForm = false;
							} else if (hasErrors) {
								addErrorMessage(requestAndResponse,
										servletText.errorChangesWereNotSaved());
							} else {
								addErrorMessage(requestAndResponse,
										servletText.errorNoChangesToSave());
								showForm = false;
							}
						}

						if (showForm) {
							// Recompute this in case the values have changed.
							needsCurrentPassword = isCurrentPasswordNeeded(
									editedUser, currentIsEditedUser,
									isUserAdmin);

							requestAndResponse
									.print("<form action=\"/changePassword/"
											+ (currentIsEditedUser ? ""
													: userId)
											+ "\" method=\"POST\"><div class=\"account\">"
											+ "<input type=\"hidden\" name=\"csrft\" value=\""
											+ getCsrft(requestAndResponse)
											+ "\">");

							if (needsCurrentPassword) {
								requestAndResponse
										.print("<div class=\"infoheader\">"
												+ servletText
														.sentenceEnterYourCurrentPasswordHere()
												+ "</div>");

								requestAndResponse
										.print("<input type=\"password\" id=\"currentpassword\" name=\"currentpassword\" placeholder=\""
												+ servletText
														.sentenceCurrentPassword(currentIsEditedUser)
												+ "\" maxlength=\"20\"><br>");
							}

							requestAndResponse
									.print("<div class=\"infoheader\">"
											+ servletText
													.sentenceEnterNewPasswordHereTwice(
															currentIsEditedUser,
															editedUser
																	.getUserName())
											+ "</div>");

							requestAndResponse
									.print("<input type=\"password\" id=\"newpassword\" name=\"newpassword\" placeholder=\""
											+ servletText.sentenceNewPassword()
											+ "\" maxlength=\"20\"><br>"
											+ "<input type=\"password\" id=\"newpassword2\" name=\"newpassword2\" placeholder=\""
											+ servletText
													.sentenceVerifyNewPassword()
											+ "\" maxlength=\"20\"><br>");

							requestAndResponse
									.print("<table class=\"responseAndSave\"><tr>"
											+ "<td><div id=\"response\"></div></td>"
											+ "<td><button onclick=\"replacePaneForForm(event, '"
											+ servletText
													.buttonChangePassword()
											+ "'); return false;\" id=\"save\" name=\"save\" class=\"specialbutton\" style=\"float:right; margin-top:10px;\">"
											+ servletText
													.buttonChangePassword()
											+ "</button></td>"
											+ "</tr></table></div>" + "</form>");
						}
					}
				}

				dbLogic.commit();
			} catch (final PersistenceException e) {
				requestAndResponse.print(servletText.errorInternalDatabase());
			}
		}

		pageWrapper.addFooter();
	}

	/**
	 * Return the parameter from the URL or if there is none then the ID of the
	 * current user.
	 */
	private String getURIParameterOrUserId(RequestAndResponse requestAndResponse) {
		final String userId = requestAndResponse.getURIParameter();
		if (userId != null && !userId.isEmpty()) {
			return userId;
		}

		return getEffectiveUserId(requestAndResponse);
	}

	/**
	 * Adds the HTML for a link with the userid appended.
	 * 
	 * @throws IOException
	 */
	private void addUserLink(RequestAndResponse requestAndResponse,
			String title, String tooltip, String url, String paneId, User user)
			throws IOException {
		requestAndResponse.println("<li><a onclick=\"newPaneForLink(event, '"
				+ title + "', '" + paneId + "'); return false;\" title=\""
				+ tooltip + "\" href=\"" + url + "/" + user.getId() + "\">"
				+ title + "</a></li>");
	}

	/**
	 * Returns true if the old password is needed to authenticate the account
	 * changes.
	 */
	private boolean isCurrentPasswordNeeded(final User editedUser,
			boolean currentIsEditedUser, boolean isUserAdmin) {
		return (!isUserAdmin || currentIsEditedUser)
				&& !editedUser.getIsSingleUser()
				&& editedUser.getPassword() != null;
	}

	/** Part of the HTML API. Handle an import. */
	private void handleHtmlDoImport(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final Errors errors = new Errors();
		final Part part = requestAndResponse.request.getPart("file");

		final String reuseIdsString = requestAndResponse.request
				.getParameter("reuseIds");
		final boolean reuseIds = reuseIdsString != null
				&& reuseIdsString.equals("on");

		final String csrft = requestAndResponse.getParameter("csrft");

		addIFrameHeader(requestAndResponse);

		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			requestAndResponse.print(servletText.errorRequiresSignIn(false));
		} else if (!isUserSignedIn(requestAndResponse)) {
			requestAndResponse.print(servletText.errorRequiresSignIn(false));
		} else if (isUsersAccountClosed(requestAndResponse)) {
			requestAndResponse.print(servletText.errorAccountIsClosed());
		} else if (part == null) {
			requestAndResponse.print(servletText.errorNoFileUploaded());
		} else {
			final InputStream stream = part.getInputStream();
			final InputStreamReader streamReader = new InputStreamReader(
					stream, Charset.forName("UTF-8"));

			final boolean result = dbLogic.importJsonForUser(
					getEffectiveUserId(requestAndResponse), streamReader,
					reuseIds, isUserAnAdmin(requestAndResponse), errors);
			if (!result) {
				requestAndResponse.print(servletText.errorImportFailed()
						+ "<br>");

				for (final String text : errors.getTexts()) {
					requestAndResponse.print(text);
					requestAndResponse.print("<br>");
				}
			} else {
				requestAndResponse.print(servletText.sentenceImported());
			}

			// This is so that if this gets reloaded a page can actually be
			// loaded.
			requestAndResponse.print("<script type=\"text/javascript\">\n"
					+ "history.replaceState(null, null, '/importFrame/');\n"
					+ "</script>");

		}

		addIFrameFooter(requestAndResponse);
	}

	/* Prints an HTML header for an iframe page. */
	private void addIFrameHeader(RequestAndResponse requestAndResponse)
			throws IOException {
		requestAndResponse.print("<!doctype html>" + "<html>" + "<head>"
				+ standardCss + "</head>\n" + "<style type=\"text/css\">"
				+ "html, body { background:none transparent }" + "</style>"
				+ "<body>");
	}

	/* Prints an HTML footer for an iframe page. */
	private void addIFrameFooter(RequestAndResponse requestAndResponse)
			throws IOException {
		requestAndResponse.print("</body><html>");
	}

	/** Part of the HTML API. Handle an export. */
	private void handleHtmlDoExport(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String pageTitle = servletText.pageTitleExport();
		final String csrft = requestAndResponse.getParameter("csrft");
		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				pageTitle, false).setPaneId("export");

		if (isTheCsrftWrong(requestAndResponse, csrft)) {
			pageWrapper.addHeader();
			requestAndResponse.print(servletText.errorRequiresSignIn(false));
			pageWrapper.addFooter();
		} else if (!isUserSignedIn(requestAndResponse)) {
			pageWrapper.addHeader();
			requestAndResponse.print(servletText
					.errorRequiresSignIn(allowSaveIfNotSignedIn));
			pageWrapper.addFooter();
		} else if (isUsersAccountClosed(requestAndResponse)) {
			pageWrapper.addHeader();
			requestAndResponse.print(servletText.errorAccountIsClosed());
			pageWrapper.addFooter();
		} else {
			requestAndResponse.setResponseContentTypeJson();

			final StringBuilder result = new StringBuilder();
			try {
				final String userId = getEffectiveUserId(requestAndResponse);
				final User user = dbLogic.getUserById(userId);
				if (user != null) {
					requestAndResponse.response.setHeader(
							"Content-Disposition",
							"attachment; filename=crushpaper-export-"
									+ user.getUserName()
									+ "-"
									+ formatDateTimeForFileName(System
											.currentTimeMillis()) + ".json");

					dbLogic.exportJsonForUser(user, result);
				}

				dbLogic.commit();
			} catch (final PersistenceException e) {
			}

			requestAndResponse.print(result.toString());
		}
	}

	/** Part of the HTML API. Show the export form. */
	private void handleHtmlExportForm(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleExport();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId("export");

		pageWrapper.addHeader();

		if (!isUserSignedIn(requestAndResponse)) {
			requestAndResponse.print(servletText
					.errorRequiresSignIn(allowSaveIfNotSignedIn));
		} else if (isUsersAccountClosed(requestAndResponse)) {
			requestAndResponse.print(servletText.errorAccountIsClosed());
		} else {
			requestAndResponse.print("<table class=\"nopadding\"><tr><td>");
			requestAndResponse.print(servletText.exportAreYouSure());
			requestAndResponse.print("</td></tr><tr><td>");

			requestAndResponse.print("<form action=\"/doExport/"
					+ "\" method=\"POST\">"
					+ "<input type=\"hidden\" name=\"csrft\" value=\""
					+ getCsrft(requestAndResponse) + "\">"
					+ "<button class=\"specialbutton withTopMargin\">"
					+ servletText.pageTitleExport() + "</button></form>");
			requestAndResponse.print("</td></tr></table>");
		}

		pageWrapper.addFooter();
	}

	/** Helper method. Handle searching sources. */
	private void handleHtmlSearchSources(PageWrapper pageWrapper, RequestAndResponse requestAndResponse,
			String query, String paneId) throws IOException, ServletException {
		pageWrapper.addPageIntroText(
				servletText.introTextSearchSources(false), servletText.introTextSearchSources(true));

		try {
			final StringBuilder result = new StringBuilder();

			User queryUser = null;
			if (null != (queryUser = canUserSeeUsersData(requestAndResponse,
					true))) {
				final ArrayList<EntryInfo> entryInfoList = new ArrayList<EntryInfo>();
				if (query.startsWith("http://") || query.startsWith("https://")) {
					final Entry entry = dbLogic.getEntryByUserIdAndUrl(
							queryUser.getId(), query);
					if (entry == null) {
						servletText.sentenceThereWereNoMatches();
					} else {
						startItemList(result, paneId);

						addSourceHtml(entry, result,
								SourceEmbedContext.InSources, null, 1, paneId);

						addEntryToInfoList(entry, entryInfoList);

						finishItemList(result);
					}
				} else {
					final ResultsPaginator paginator = new ResultsPaginator(
							requestAndResponse,
							servletText.sentenceThereWereNoMatches(), result,
							servletText);
					try {
						final List<?> results = dbLogic
								.searchEntriesForUserBySourceTitle(
										queryUser.getId(), query,
										paginator.getStartPosition(),
										paginator.getMaxResults());
	
						startItemList(result, paneId);
	
						for (final Object entryUncasted : results) {
							final Entry entry = (Entry) entryUncasted;
	
							final int resultNumber = paginator.next();
							if (resultNumber == -1) {
								continue;
							} else if (resultNumber == 0) {
								break;
							}
	
							addSourceHtml(entry, result,
									SourceEmbedContext.InSources, null,
									resultNumber, paneId);
	
							addEntryToInfoList(entry, entryInfoList);
						}
	
						finishItemList(result);
	
						paginator.done();
					} catch (EmptyQueryException e) {
						requestAndResponse.print(servletText.errorNeedLongerQuery());
					}
				}

				result.append("\n<script type=\"application/json\" class=\"entryInfoDictJson\">\n");
				addJsonForEntryInfos(result, entryInfoList, paneId);
				result.append("\n</script>\n");
			}

			dbLogic.commit();
			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			requestAndResponse.print(servletText.errorInternalDatabase());
		}
	}

	/** Part of the HTML API. Shows search results within users. */
	private void handleHtmlSearchAccounts(PageWrapper pageWrapper,
			RequestAndResponse requestAndResponse, String query, String paneId)
			throws IOException, ServletException {
		pageWrapper.addPageIntroText(servletText.introTextSearchUsers(), null);

		if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			try {
				final StringBuilder result = new StringBuilder();
				final User user = dbLogic
						.getUserByUserName(query.toLowerCase());
				if (user == null) {
					result.append(servletText.sentenceThereWereNoMatches());
				} else {
					startItemList(result, paneId);
					addUserHtml(user, result, 1, paneId);
					finishItemList(result);
				}

				dbLogic.commit();

				requestAndResponse.print(result.toString());
			} catch (final PersistenceException e) {
				requestAndResponse.print(servletText.errorInternalDatabase());
			}
		}
	}

	/** Part of the HTML API. Handle showing quotations. */
	private void handleHtmlShowQuotations(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleQuotations();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final String paneId = "quotations";
		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId(paneId);
		pageWrapper.addHeader();
		pageWrapper.addMetaData(new KeyAndValue("paneType", paneId));

		pageWrapper.addPageIntroText(
				servletText.introTextShowQuotations(false), servletText.introTextShowQuotations(true));

		try {
			final StringBuilder result = new StringBuilder();

			User queryUser = null;
			if (null != (queryUser = canUserSeeUsersData(requestAndResponse,
					true))) {
				final ResultsPaginator paginator = new ResultsPaginator(
						requestAndResponse,
						servletText.sentenceNoQuotationsExist(), result,
						servletText);

				final List<?> results = dbLogic
						.getEntriesByUserIdAndType(queryUser.getId(),
								DbLogic.Constants.quotation,
								paginator.getStartPosition(),
								paginator.getMaxResults());

				entryListToHtmlAndJson(paneId, result, paginator, results);
			}

			dbLogic.commit();

			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			requestAndResponse.print(servletText.errorInternalDatabase());
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Handle showing users. */
	private void handleHtmlShowAccounts(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleAccounts();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final String paneId = "accounts";
		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, true).setPaneId(paneId);
		pageWrapper.addHeader();
		pageWrapper.addMetaData(new KeyAndValue("notEditable", true));
		pageWrapper.addMetaData(new KeyAndValue("paneType", "accounts"));

		if (!isUserAnAdmin(requestAndResponse)) {
			requestAndResponse.print(servletText.errorPageNotAllowed());
		} else {
			pageWrapper.addPageIntroText(
					servletText.introTextShowAccounts(false), servletText.introTextShowAccounts(true));

			try {
				final StringBuilder result = new StringBuilder();
				final ResultsPaginator paginator = new ResultsPaginator(
						requestAndResponse,
						servletText.sentenceNoAccountsExist(), result,
						servletText);

				startItemList(result, paneId);

				final List<?> users = dbLogic
						.getAllUsers(paginator.getStartPosition(),
								paginator.getMaxResults());
				for (final Object userUncasted : users) {
					final User user = (User) userUncasted;

					final int resultNumber = paginator.next();
					if (resultNumber == -1) {
						continue;
					} else if (resultNumber == 0) {
						break;
					}

					addUserHtml(user, result, resultNumber, paneId);
				}

				finishItemList(result);

				paginator.done();

				dbLogic.commit();
				requestAndResponse.print(result.toString());
			} catch (final PersistenceException e) {
				requestAndResponse.print(servletText.errorInternalDatabase());
			}
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Show a source. */
	private void handleHtmlShowSource(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		String title = servletText.pageTitleSource();

		final String paneId = "source";
		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId(paneId);

		final String id = requestAndResponse.getURIParameter();

		boolean headerAdded = false;
		// Do not check if the session is signed in or the account is closed in
		// case the page is public.
		try {
			final StringBuilder result = new StringBuilder();
			final Entry source = dbLogic.getEntryById(id);
			final User user = dbLogic
					.getUserById(getEffectiveUserId(requestAndResponse));

			if (source != null) {
				pageWrapper.setPaneId(source.getId());
			}

			if (source == null) {
				if (addTitle(requestAndResponse, title)) {
					dbLogic.commit();
					return;
				}

				if (!requestAndResponse.moreThanOneUri) {
					requestAndResponse.response
							.setStatus(HttpServletResponse.SC_NOT_FOUND);
				}

				pageWrapper.addHeader();
				headerAdded = true;

				result.append(servletText.errorNoSourceFound());
			} else if (!dbLogic.canUserSeeEntry(user, source,
					isUserAnAdmin(requestAndResponse))) {
				if (addTitle(requestAndResponse, title)) {
					dbLogic.commit();
					return;
				}

				pageWrapper.addHeader();
				headerAdded = true;

				if (user == null) {
					result.append(servletText.errorRequiresSignIn(false));
				} else {
					result.append(servletText.errorMayNotSeeSource());
				}
			} else {
				title = source.getSourceTitle();
				if (addTitle(requestAndResponse, title)) {
					dbLogic.commit();
					return;
				}

				pageWrapper.setIncludeEdit();
				pageWrapper.setIncludeDelete();
				pageWrapper.setTitle(title);
				pageWrapper.addMetaData(new KeyAndValue("paneType", "source"));
				pageWrapper.addHeader();
				headerAdded = true;

				pageWrapper.addPageIntroText(
						servletText.introTextShowSource(false), servletText.introTextShowSource(true));

				addSourceHtml(source, result, SourceEmbedContext.InSource,
						null, -1, paneId);

				final ResultsPaginator paginator = new ResultsPaginator(
						requestAndResponse,
						servletText.sentenceNoQuotationsForThisSourceExist(),
						result, servletText);

				final List<?> results = dbLogic.getEntriesBySourceId(
						source.getId(), paginator.getStartPosition(),
						paginator.getMaxResults());
				entryListToHtmlAndJson(paneId, result, paginator, results,
						SourceEmbedContext.InSourceQuotations);
			}

			dbLogic.commit();
			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			if (!headerAdded) {
				pageWrapper.addHeader();
			}

			requestAndResponse.print(servletText.errorInternalDatabase());
		}

		pageWrapper.addFooter();
	}

	/** Part of the HTML API. Show that the session has been signed out. */
	private void handleHtmlShowSignedOut(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		final String title = servletText.pageTitleSignedOut();
		if (addTitle(requestAndResponse, title)) {
			return;
		}

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				title, false).setPaneId("welcome");
		pageWrapper.addHeader();

		if (!isUserSignedIn(requestAndResponse)) {
			requestAndResponse
					.print(servletText.sentenceYouHaveBeenSignedOut());
		} else {
			requestAndResponse.print(servletText
					.sentenceYouHaveNotBeenSignedOut());
		}

		pageWrapper.addFooter();
	}

	/** Formats a timestamp into a datetime for file names. */
	String formatDateTimeForFileName(long unixTime) {
		final Date date = new Date(unixTime);

		final SimpleDateFormat format = filenameDateAndTimeFormat.get();

		return format.format(date);
	}

	/** Formats a timestamp into a date and time of day with the default format. */
	String formatDateAndTime(Long unixTime) {
		final Date date = new Date(unixTime);

		final SimpleDateFormat format = defaultDateAndTimeFormat.get();

		return format.format(date);
	}

	private static final ThreadLocal<SimpleDateFormat> filenameDateAndTimeFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			final SimpleDateFormat dayFormat = new SimpleDateFormat(
					"yyyy-MM-dd-HH-mm-ss");
			return dayFormat;
		}
	};

	private static final ThreadLocal<SimpleDateFormat> defaultDateAndTimeFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			final SimpleDateFormat dayFormat = new SimpleDateFormat(
					"E, MMM d, yyyy @ h:mm a");
			return dayFormat;
		}
	};

	/** Called to start an item list. */
	private void startItemList(StringBuilder result, String rootId) {
		result.append("<div class=\"container\""
				+ "><div class=\"alone fakealone\" id=\"alone_ef_" + rootId
				+ "\"></div><div class=\"justchildren fakejustchildren\">");
	}

	/** Called to start adding an item to an item list. */
	private void startItemListItem(StringBuilder result, String rootId,
			String itemId) {
		result.append("<div class=\"subtree\">");

		result.append("<div class=\"alone " + itemId + "\" id=\"alone_"
				+ rootId + ":" + itemId + "\">");
	}

	/** Called to finish adding an item to an item list. */
	private void finishItemListItem(StringBuilder result) {
		result.append("</div><div class=\"justchildren\"></div></div>");
	}

	/** Called to finish an item list. */
	private void finishItemList(StringBuilder result) {
		result.append("</div></div>");
	}

	/** Helper method. Adds the HTML for an entry to a list. */
	private void addEntryHtmlToList(Entry entry, StringBuilder result,
			int resultNumber, String rootId,
			SourceEmbedContext embedContext) throws IOException {
		startItemListItem(result, rootId, entry.getId());

		result.append("<table class=\"magic nopadding\"><tr><td class=\"resultNumber\">");

		result.append(getItemMetaDataJsonHtml(entry.getType(), entry.getId()))
				.toString();

		result.append(resultNumber + ".</td><td class=\"listItem\">");

		if (entry.hasQuotation()) {
			result.append("<div class=\"quotation\" title=\""
					+ servletText.quotationInListTooltip() + "\">");
			result.append(getQuotationMarkdown(entry, true));
			result.append("</div><br>");
		}

		result.append("<div class=\"note\" title=\""
				+ servletText.noteInListTooltip(entry.getType()) + "\">");
		result.append(getNoteMarkdown(entry, true, entry.hasQuotation()));
		result.append("</div>");

		final StringBuilder atString = new StringBuilder();
		atString.append("<div class=\"listModTime\" title=\""
				+ servletText.modTimeInListTooltip(entry.getType()) + "\">"
				+ servletText.fragmentLastModified() + " <span>");
		atString.append(formatDateAndTime(entry.getModTime())
				+ "<span class=\"rawDateTime\">" + entry.getModTime()
				+ "</span></span></div>");

		boolean sourceIncluded = false;
		if (embedContext != SourceEmbedContext.InSourceQuotations) {
			final Entry source = dbLogic.getEntryById(entry.getSourceId());
			if (source != null) {
				addSourceHtml(source, result, embedContext,
						atString.toString(), -1, null);
				sourceIncluded = true;
			}
		}

		if (!sourceIncluded) {
			result.append("<div class=\"listItemFooter\">"
					+ atString.toString() + "</div>");
		}

		result.append("</td></tr></table>");

		finishItemListItem(result);
	}

	/** Helper method. Adds the HTML for a user to a list. */
	private void addUserHtml(User user, StringBuilder result, int resultNumber,
			String rootId) throws IOException {
		if (resultNumber != -1) {
			startItemListItem(result, rootId, user.getId());

			result.append("<table class=\"magic nopadding\"><tr><td class=\"resultNumber\">");

			result.append(getItemMetaDataJsonHtml("user", user.getId()))
					.toString();

			result.append(resultNumber + ".</td><td class=\"listItem\">");
		}

		result.append("Username: " + user.getUserName());
		if (user.getEmail() != null) {
			result.append(", email: " + user.getEmail());
		}

		if (user.getIsAdmin()) {
			result.append(", is an admin");
		}

		if (user.getMayContact()) {
			result.append(", may be contacted");
		}

		result.append("<br>");

		if (resultNumber != -1) {
			result.append("<div class=\"listItemFooter\">");
			result.append("<div title=\""
					+ servletText.modTimeInListTooltip("account") + "\">"
					+ servletText.fragmentLastModified() + " <span>");
			result.append(formatDateAndTime(user.getModTime())
					+ "<span class=\"rawDateTime\">" + user.getModTime()
					+ "</span></span>");
			result.append("</div></div>");
			result.append("</td></tr></table>");
			finishItemListItem(result);
		} else {
			result.append(servletText.fragmentLastModified() + " <span>");
			result.append(formatDateAndTime(user.getModTime())
					+ "<span class=\"rawDateTime\">" + user.getModTime()
					+ "</span></span>");
			result.append(". " + servletText.fragmentCreated() + " <span>");
			result.append(formatDateAndTime(user.getCreateTime())
					+ "<span class=\"rawDateTime\">" + user.getModTime()
					+ "</span></span>");
		}
	}

	/**
	 * A simple class that contains the straight text for an entry to make it
	 * editable rather than just displayable as html.
	 */
	class EntryInfo {
		public EntryInfo(String id, String note, String quotation,
				boolean isPublic, boolean hasChildren, boolean hasParent,
				String type) {
			this.id = id;
			this.note = note;
			this.quotation = quotation;
			this.isPublic = isPublic;
			this.hasChildren = hasChildren;
			this.hasParent = hasParent;
			this.type = type;
		}

		String id;
		String note;
		String quotation;
		boolean isPublic;
		boolean hasChildren;
		boolean hasParent;
		String type;
	}

	/**
	 * Helper method. A wrapper function for addEntryHtmlToTree that defaults
	 * onlyChildren to false and idOfEntryToSkip to null.
	 */
	private void addEntryHtmlToTreeSimple(Entry entry, StringBuilder result,
			List<EntryInfo> entryInfoList, int levelsOfChildrenToInclude)
			throws IOException {
		addEntryHtmlToTree(entry, result, entryInfoList,
				levelsOfChildrenToInclude, false, null, true);
	}

	/**
	 * Helper method. Adds the HTML for an entry to a tree.
	 */
	private int addEntryHtmlToTree(Entry entry, StringBuilder result,
			List<EntryInfo> entryInfoList, int levelsOfChildrenToInclude,
			boolean onlyChildren, String idOfEntryToSkip,
			boolean includeRootInEntryInfoList) throws IOException {
		if (!onlyChildren) {
			result.append("<div class=\"subtree\">");

			result.append("<div class=\"alone " + entry.getId()
					+ "\" id=\"alone_" + entry.getId() + "\">");

			result.append("<table class=\"nopadding alonetd\"><tr><td onmousedown=\"triangleOnMouseDown(event); return false;\" class=\"triTd justDrag\">"
					+ "<div></div></td><td>"
					+ "<table class=\"nopadding\"><tr><td class=\"nowords\"><img onmouseover=\"plusOnMouseOver(event);\" onmouseout=\"plusOnMouseOut(event);\" alt=\"plus\" title=\""
					+ servletText.plusTooltip()
					+ "\" class=\"justDrag\" onmousedown=\"plusOnMouseDown(event); return false;\" src=\"/images/plus.png\"></td></tr>"
					+ "<tr><td class=\"nowords\"><img onmouseover=\"minusOnMouseOver(event);\" onmouseout=\"minusOnMouseOut(event);\" alt=\"minus\" title=\""
					+ servletText.minusTooltip()
					+ "\" class=\"justDrag\" onmousedown=\"minusOnMouseDown(event); return false;\" src=\"/images/minus.png\"></td></tr></table>"
					+ "</td><td class=\"content\">");

			if (entry.hasQuotation()) {
				result.append("<div class=\"quotation\">");
				result.append(getQuotationMarkdown(entry, false));
				result.append("</div><br>");
			}

			result.append("<div class=\"note\">");
			result.append(getNoteMarkdown(entry, false, entry.hasQuotation()));
			result.append("</div>");

			result.append("<span class=\"entryDaytime\">"
					+ servletText.fragmentLastModified() + " "
					+ "<span class=\"modTime\">"
					+ formatDateAndTime(entry.getModTime())
					+ "<span class=\"rawDateTime\">" + entry.getModTime()
					+ "</span></span></span>");

			final Entry source = dbLogic.getEntryById(entry.getSourceId());
			if (source != null) {
				addSourceHtml(source, result, SourceEmbedContext.InQuotation,
						null, -1, null);
			}

			// Good for debugging.
			if (false) {
				result.append("<br>id: " + entry.getId());
				result.append("<br>parent: " + entry.getParentId(""));
				result.append("<br>first child: " + entry.getFirstChildId(""));
				result.append("<br>last child: " + entry.getLastChildId(""));
				result.append("<br>previous sibling: "
						+ entry.getPreviousSiblingId(""));
				result.append("<br>next sibling: " + entry.getNextSiblingId(""));
			}

			result.append("</td></tr></table></div>");
			result.append("<div class=\"justchildren\">");
		}

		if (includeRootInEntryInfoList) {
			addEntryToInfoList(entry, entryInfoList);
		}

		int indexOfEntryToSkip = -1;
		if (levelsOfChildrenToInclude > 0) {
			--levelsOfChildrenToInclude;

			final Hashtable<String, Entry> children = new Hashtable<String, Entry>();
			Entry first = null;
			for (final Object childObject : dbLogic.getEntriesByParentId(entry
					.getId())) {
				final Entry child = (Entry) childObject;
				children.put(child.getId(), child);
				if (!child.hasPreviousSiblingId()) {
					first = child;
				}
			}

			if (first != null) {
				// This is the code path if there is no DB corruption.
				Entry child = first;
				for (int i = 0; i < children.size(); ++i) {
					if (child == null) {
						break;
					}

					if (idOfEntryToSkip != null
							&& idOfEntryToSkip.equals(child.getId())) {
						indexOfEntryToSkip = i;
					} else {
						addEntryHtmlToTree(child, result, entryInfoList,
								levelsOfChildrenToInclude, false, null, true);
					}

					if (!child.hasNextSiblingId()) {
						break;
					}

					final String nextId = child.getNextSiblingId();
					child = children.get(nextId);
				}
			} else {
				// This is an error code path. It should only happen if there is
				// DB corruption.
				final Iterator<Map.Entry<String, Entry>> iterator = children
						.entrySet().iterator();
				int i = 0;
				while (iterator.hasNext()) {
					final Map.Entry<String, Entry> mapEntry = iterator.next();
					final Entry child = mapEntry.getValue();

					if (idOfEntryToSkip != null
							&& idOfEntryToSkip.equals(child.getId())) {
						indexOfEntryToSkip = i;
					} else {
						addEntryHtmlToTree(child, result, entryInfoList,
								levelsOfChildrenToInclude, false, null, true);
					}

					++i;
				}
			}
		}

		if (!onlyChildren) {
			result.append("</div>");
			result.append("</div>");
		}

		return indexOfEntryToSkip;
	}

	/** Adds the entry to the entry list. */
	private void addEntryToInfoList(Entry entry, List<EntryInfo> entryInfoList) {
		if (entryInfoList != null) {
			String typeToAdd = entry.getType();
			if (typeToAdd.equals(DbLogic.Constants.quotation)) {
				typeToAdd = DbLogic.Constants.note;
			}

			entryInfoList.add(new EntryInfo(entry.getId(), entry
					.getNoteOrNotebookTitle(""), entry.getQuotation(""), entry
					.getIsPublic(), entry.hasFirstChildId(), entry
					.hasParentId(), typeToAdd));
		}
	}

	enum SourceEmbedContext {
		InSources, InSource, InQuotations, InQuotation, InSourceQuotations
	};

	/**
	 * Returns JSON HTML suitable for injecting into a list describes the item.
	 * 
	 * @throws IOException
	 */
	private StringBuilder getItemMetaDataJsonHtml(String type, String id)
			throws IOException {
		final StringBuilder result = new StringBuilder();

		result.append("\n<script type=\"application/json\" class=\"itemMetaDataDictJson\">\n{\n");
		final boolean addedAnyYet = false;
		JsonBuilder.addPropertyToJsonString(result, type, addedAnyYet, "type");
		JsonBuilder.addPropertyToJsonString(result, id, addedAnyYet, "id");
		result.append("\n}\n</script>\n");

		return result;
	}

	/** Helper method. Adds the HTML for a source to a list. */
	private void addSourceHtml(Entry source, StringBuilder result,
			SourceEmbedContext embedContext, String prefix, int resultNumber,
			String rootId) throws IOException {
		final String url = source.getSourceUrl();

		final StringBuilder header = new StringBuilder();
		final StringBuilder footer = new StringBuilder();
		if (embedContext == SourceEmbedContext.InQuotation
				|| embedContext == SourceEmbedContext.InQuotations
				|| embedContext == SourceEmbedContext.InSourceQuotations) {
			header.append("<div class=\"listItemFooter\">");
			footer.append("</div>");
		} else if (embedContext == SourceEmbedContext.InSources) {
			startItemListItem(result, rootId, source.getId());
			header.append("<table class=\"magic nopadding\"><tr><td class=\"resultNumber\">");
			header.append(getItemMetaDataJsonHtml(source.getType(),
					source.getId()).toString());
			header.append(resultNumber + ".</td><td class=\"listItem\">");
			footer.append("</td></tr></table>");
			finishItemListItem(footer);
		} else {
			header.append("<div>");
			footer.append("</div>");
		}

		result.append(header);
		if (prefix != null) {
			result.append(prefix);
		}

		if (embedContext == SourceEmbedContext.InQuotation
				|| embedContext == SourceEmbedContext.InQuotation) {
			result.append(servletText.fragmentFrom());
			result.append(" ");
		}

		String domain = null;
		try {
			if (url != null) {
				final URI uri = new URI(url);
				domain = uri.getHost();
			}
		} catch (final URISyntaxException e) {
		}

		result.append("<div class=\"sourceTitle\">");
		
		if (domain != null && embedContext != SourceEmbedContext.InSources) {
			result.append("<a onclick=\"newTab(event); return false;\" target=\"_blank\" title=\""
					+ servletText.showExternalSourceLinkTooltip()
					+ "\" href=\"");
			result.append(StringEscapeUtils.escapeHtml4(url));
			result.append("\">");
		}

		String title = source.getSourceTitle();
		if (title == null || title.isEmpty()) {
			title = servletText.fragmentBlankTitle();
		}

		if (embedContext == SourceEmbedContext.InSource) {
			title = servletText.fragmentVisitExternalSource();
		}

		result.append(StringEscapeUtils.escapeHtml4(title));

		if (domain != null && embedContext != SourceEmbedContext.InSources) {
			result.append("</a>");
		}

		result.append("</div>");
		
		result.append(" ");

		if (domain != null) {
			result.append("<span class=\"domain\">");
			if (embedContext != SourceEmbedContext.InSource) {
				result.append("(");
			}

			result.append(domain);

			if (embedContext != SourceEmbedContext.InSource) {
				result.append(")");
			}

			result.append("</span>");
		}

		if (embedContext == SourceEmbedContext.InQuotation
				|| embedContext == SourceEmbedContext.InQuotations
				|| embedContext == SourceEmbedContext.InSources) {
			result.append(" <a onclick=\"newPaneForLink(event, 'Source', '"
					+ source.getId()
					+ "'); return false;\" class=\"sourceMore\" title=\""
					+ servletText.moreFromThisSourceTooltip()
					+ "\" href=\"/source/");
			result.append(source.getId());
			result.append("\">");
			result.append(servletText.buttonMoreQuotations());
			result.append("</a>");
		}

		if (embedContext == SourceEmbedContext.InSource
				|| embedContext == SourceEmbedContext.InSources) {
			result.append("<br><div class=\"listItemFooter\">");
			result.append(servletText.fragmentLastModified() + " <span>");
			result.append(formatDateAndTime(source.getModTime())
					+ "<span class=\"rawDateTime\">" + source.getModTime()
					+ "</span></span></div>");
		}

		result.append(footer);
	}

	/**
	 * Part of the JSON API. Returns the JSON describing the parent of an entry.
	 */
	private void handleJsonShowEntryParent(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();
		final String id = requestAndResponse.request
				.getParameter(DbLogic.Constants.id);

		if (!dbLogic.getIdGenerator().isIdWellFormed(id)) {
			returnJson400(requestAndResponse,
					servletText.errorIdIsInvalidFormat());
			return;
		}

		try {
			final User user = dbLogic
					.getUserById(getEffectiveUserId(requestAndResponse));
			final Entry entry = dbLogic.getEntryById(id);

			if (entry == null) {
				returnJson400(requestAndResponse,
						servletText.errorEntryCouldNotBeFound());
				return;
			}

			final StringBuilder result = new StringBuilder();

			// Do not check if the session is signed in or the account is closed
			// in case the page is public.
			if (!dbLogic.canUserSeeEntry(user, entry,
					isUserAnAdmin(requestAndResponse))) {
				if (user == null) {
					returnJson400(requestAndResponse,
							servletText.errorRequiresSignIn(false));
				} else {
					returnJson400(requestAndResponse,
							servletText.errorMayNotSeeEntry());
				}
			} else {
				final String parentId = entry.getParentId();
				if (parentId == null) {
					returnJson400(requestAndResponse,
							servletText.errorHasNoParent());
				} else {
					final Entry parentEntry = dbLogic.getEntryById(parentId);
					if (parentEntry == null) {
						returnJson400(requestAndResponse,
								servletText.errorParentCouldNotBeFound());
					} else if (!dbLogic.canUserSeeEntry(user, parentEntry,
							isUserAnAdmin(requestAndResponse))) {
						returnJson400(requestAndResponse,
								servletText.errorMayNotSeeEntry());
					} else {
						final ArrayList<EntryInfo> entryInfoList = new ArrayList<EntryInfo>();
						final StringBuilder innerResult = new StringBuilder();
						final int skippedIndex = addEntryHtmlToTree(
								parentEntry, innerResult, entryInfoList,
								defaultNoteDisplayDepth, false, id, true);
						result.append("{ \"subtreeHtml\": "
								+ JsonBuilder.quote(innerResult.toString()));
						result.append(",\n\"id\": \"" + parentEntry.getId()
								+ "\"");
						result.append(",\n\"skippedIndex\": " + skippedIndex);
						result.append(",\n\"entryInfoDict\":");
						addJsonForEntryInfos(result, entryInfoList, null);
						result.append("}");
					}
				}
			}

			dbLogic.commit();

			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			returnJson500(requestAndResponse,
					servletText.errorInternalDatabase());
		}
	}

	/**
	 * Part of the JSON API. Returns JSON indicating if the session exists and
	 * is signed in.
	 */
	private void handleJsonIsSignedIn(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();

		final String sessionId = requestAndResponse.request
				.getParameter("sessionId");

		boolean signedIn = false;

		if (sessionManager != null) {
			final HttpSession session = sessionManager.getSession(sessionId);
			if (session != null
					&& session.getAttribute(sessionUserIdAttribute) != null) {
				signedIn = true;
			}
		}

		requestAndResponse.println("{ \"isSignedIn\": " + signedIn + " }");
	}

	/** Part of the JSON API. Returns the JSON describing an entry. */
	private void handleJsonShowEntry(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		requestAndResponse.setResponseContentTypeJson();

		final String id = requestAndResponse.request
				.getParameter(DbLogic.Constants.id);

		if (!dbLogic.getIdGenerator().isIdWellFormed(id)) {
			returnJson400(requestAndResponse,
					servletText.errorIdIsInvalidFormat());
			return;
		}

		try {
			final User user = dbLogic
					.getUserById(getEffectiveUserId(requestAndResponse));

			final Entry entry = dbLogic.getEntryById(id);

			if (entry == null) {
				returnJson400(requestAndResponse,
						servletText.errorEntryCouldNotBeFound());
				return;
			}

			final StringBuilder result = new StringBuilder();

			// Do not check if the session is signed in or the account is closed
			// in case the page is public.
			if (!dbLogic.canUserSeeEntry(user, entry,
					isUserAnAdmin(requestAndResponse))) {
				if (user == null) {
					returnJson400(requestAndResponse,
							servletText.errorRequiresSignIn(false));
				} else {
					returnJson400(requestAndResponse,
							servletText.errorMayNotSeeEntry());
				}
			} else {
				final String parentId = entry.getParentId();
				if (parentId == null) {
					returnJson400(requestAndResponse,
							servletText.errorHasNoParent());
				} else {
					final Entry parentEntry = dbLogic.getEntryById(parentId);
					if (parentEntry == null) {
						returnJson400(requestAndResponse,
								servletText.errorParentCouldNotBeFound());
					} else if (!dbLogic.canUserSeeEntry(user, parentEntry,
							isUserAnAdmin(requestAndResponse))) {
						returnJson400(requestAndResponse,
								servletText.errorMayNotSeeEntry());
					} else {
						final ArrayList<EntryInfo> entryInfoList = new ArrayList<EntryInfo>();
						final StringBuilder innerResult = new StringBuilder();
						addEntryHtmlToTreeSimple(entry, innerResult,
								entryInfoList, defaultNoteDisplayDepth);
						result.append("{ \"subtreeHtml\": "
								+ JsonBuilder.quote(innerResult.toString())
								+ "\n");
						result.append(", \"id\": "
								+ JsonBuilder.quote(entry.getId()) + "\n");
						result.append(", \"entryInfoDict\": ");
						addJsonForEntryInfos(result, entryInfoList, null);
						result.append(" }");
					}
				}
			}

			dbLogic.commit();

			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			returnJson500(requestAndResponse,
					servletText.errorInternalDatabase());
		}
	}

	/**
	 * Part of the JSON API. Returns the JSON describing the children of an
	 * entry.
	 */
	private void handleJsonShowEntryChildren(
			RequestAndResponse requestAndResponse) throws IOException,
			ServletException {
		requestAndResponse.setResponseContentTypeJson();

		final String id = requestAndResponse.request
				.getParameter(DbLogic.Constants.id);
		final String levels = requestAndResponse.getParameter("levels");

		if (!dbLogic.getIdGenerator().isIdWellFormed(id)) {
			returnJson400(requestAndResponse,
					servletText.errorIdIsInvalidFormat());
			return;
		}

		if (levels == null
				|| levels.length() > 3
				|| (!Pattern.compile("^\\d+$").matcher(levels).find() && levels
						.equals("max"))) {
			returnJson400(requestAndResponse,
					servletText.errorLevelsIsInvalid());
			return;
		}

		int numLevels = Integer.MAX_VALUE;
		if (!levels.equals("max")) {
			numLevels = Integer.parseInt(levels);
		}

		try {
			final User user = dbLogic
					.getUserById(getEffectiveUserId(requestAndResponse));

			final Entry entry = dbLogic.getEntryById(id);

			if (entry == null) {
				returnJson400(requestAndResponse,
						servletText.errorEntryCouldNotBeFound());
				return;
			}

			final StringBuilder result = new StringBuilder();

			// Do not check if the session is signed in or the account is closed
			// in case the page is public.
			if (!dbLogic.canUserSeeEntry(user, entry,
					isUserAnAdmin(requestAndResponse))) {
				if (user == null) {
					returnJson400(requestAndResponse,
							servletText.errorRequiresSignIn(false));
				} else {
					returnJson400(requestAndResponse,
							servletText.errorMayNotSeeEntry());
				}
			} else {
				final ArrayList<EntryInfo> entryInfoList = new ArrayList<EntryInfo>();
				final StringBuilder innerResult = new StringBuilder();

				addEntryHtmlToTree(entry, innerResult, entryInfoList,
						numLevels, true, null, false);
				result.append("{ \"childrenHtml\": "
						+ JsonBuilder.quote(innerResult.toString()) + "\n");
				result.append(", \"id\": " + JsonBuilder.quote(entry.getId())
						+ "\n");
				result.append(", \"entryInfoDict\": ");
				addJsonForEntryInfos(result, entryInfoList, null);
				result.append(" }");
			}

			dbLogic.commit();

			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			returnJson500(requestAndResponse,
					servletText.errorInternalDatabase());
		}
	}

	/** Part of the HTML API. Returns the HTML for a notebook. */
	private void handleHtmlShowNotebook(RequestAndResponse requestAndResponse)
			throws IOException, ServletException {
		String paneId = "notebook";
		final String defaultTitle = servletText.pageTitleNotebook();
		final String notFoundMessage = servletText
				.errorNotebookCouldNotBeFound();
		final String mayNotSeeMessage = servletText.errorMayNotSeeNotebook();
		final String introMessage = servletText.introTextShowNotebook(false);
		final String touchIntroMessage = servletText.introTextShowNotebook(true);
		final String tooltipNewChild = servletText.tooltipNewNote();
		final String buttonNewChild = servletText.buttonNewNote();
		String titleIfCanSee = defaultTitle;

		final String id = requestAndResponse.getURIParameter();

		Entry entry = dbLogic.getEntryById(id);
		if (entry != null
				&& !entry.getType("").equals(DbLogic.Constants.notebook)) {
			entry = null;
		}

		final User user = dbLogic
				.getUserById(getEffectiveUserId(requestAndResponse));
		boolean userCanSee = false;
		Entry root = null;
		if (entry != null) {
			titleIfCanSee = entry.getNoteOrNotebookTitle("");
			paneId = entry.getId();
			root = dbLogic.getEntryById(entry.getRootId());
			userCanSee = dbLogic.canUserSeeEntry(user, entry,
					isUserAnAdmin(requestAndResponse));
		}

		handleHtmlShowEntryTree(requestAndResponse, paneId, defaultTitle,
				notFoundMessage, mayNotSeeMessage, introMessage, touchIntroMessage,
				tooltipNewChild, buttonNewChild, titleIfCanSee, root,
				userCanSee, user, true, "newSubNote", "notebook", false);
	}

	/** Show a tree of notes. */
	private void handleHtmlShowEntryTree(RequestAndResponse requestAndResponse,
			String paneId, String defaultTitle, String notFoundMessage,
			String mayNotSeeMessage, String introMessage, String touchIntroMessage,
			String tooltipNewChild, String buttonNewChild,
			String titleIfCanSee, Entry root, boolean userCanSee, User user,
			boolean showDelete, String buttonFunction, String paneType,
			Boolean notEditable) throws IOException, ServletException {

		final PageWrapper pageWrapper = new PageWrapper(requestAndResponse,
				defaultTitle, false).setPaneId(paneId);

		pageWrapper.addMetaData(new KeyAndValue("paneType", paneType));
		pageWrapper.addMetaData(new KeyAndValue("notEditable", notEditable));
		pageWrapper.addMetaData(new KeyAndValue("tree", true));

		boolean headerAdded = false;
		try {
			final StringBuilder result = new StringBuilder();

			if (root == null) {
				if (addTitle(requestAndResponse, defaultTitle)) {
					dbLogic.commit();
					return;
				}

				pageWrapper.addHeader();
				headerAdded = true;

				if (!requestAndResponse.moreThanOneUri) {
					requestAndResponse.response
							.setStatus(HttpServletResponse.SC_NOT_FOUND);
				}

				if (user == null) {
					result.append(servletText.errorRequiresSignIn(false));
				} else {
					result.append(notFoundMessage);
				}
			} else if (!userCanSee) {
				if (addTitle(requestAndResponse, defaultTitle)) {
					dbLogic.commit();
					return;
				}

				pageWrapper.addHeader();
				headerAdded = true;

				if (user == null) {
					result.append(servletText.errorRequiresSignIn(false));
				} else {
					result.append(mayNotSeeMessage);
				}
			} else {
				if (addTitle(requestAndResponse, titleIfCanSee)) {
					dbLogic.commit();
					return;
				}

				pageWrapper.setTitle(titleIfCanSee);
				pageWrapper.setIncludeEdit();
				if (showDelete) {
					pageWrapper.setIncludeDelete();
				}
				pageWrapper.addHeader();

				pageWrapper.addPageIntroText(introMessage, touchIntroMessage);

				headerAdded = true;

				result.append("<div class=\"container\""
						+ "><div class=\"alone fakealone\" id=\"alone_"
						+ root.getId()
						+ "\"></div><div class=\"justchildren fakejustchildren\">");

				final ArrayList<EntryInfo> entryInfoList = new ArrayList<EntryInfo>();
				addEntryHtmlToTree(root, result, entryInfoList,
						defaultNoteDisplayDepth, true, null, true);

				result.append("</div></div>");

				result.append("<div class=\"centered\"><button title=\""
						+ tooltipNewChild
						+ "\" class=\"centered specialbutton\" onclick=\""
						+ buttonFunction + "(event); return false;\">"
						+ buttonNewChild + "</button></div>");

				result.append("\n<script type=\"application/json\" class=\"entryInfoDictJson\">\n");
				addJsonForEntryInfos(result, entryInfoList, null);
				result.append("\n</script>\n");
			}

			dbLogic.commit();

			requestAndResponse.print(result.toString());
		} catch (final PersistenceException e) {
			if (!headerAdded) {
				pageWrapper.addHeader();
			}

			requestAndResponse.print(servletText.errorInternalDatabase());
		}

		pageWrapper.addFooter();
	}

	/** Adds the JSON for note and quotation text. */
	private void addJsonForEntryInfos(StringBuilder result,
			ArrayList<EntryInfo> entryInfoList, String paneId) {
		result.append("{\n");

		boolean addedOneAlready = false;
		for (final EntryInfo entryInfo : entryInfoList) {
			if (addedOneAlready) {
				result.append(",\n");
			}
			addedOneAlready = true;

			result.append(JsonBuilder
					.quote((paneId != null ? paneId + ":" : "") + entryInfo.id));
			result.append(":");
			result.append("[" + JsonBuilder.quote(entryInfo.note) + ", "
					+ JsonBuilder.quote(entryInfo.quotation) + ", "
					+ entryInfo.isPublic + ", " + entryInfo.hasChildren
					+ ", \"" + entryInfo.type + "\", " + entryInfo.hasParent
					+ "]");
		}

		result.append("\n}");
	}

	private static Logger logger = Logger.getLogger(Servlet.class.getName());

	/**
	 * Runs the server.
	 * 
	 * @throws IOException
	 */
	public boolean run() throws IOException {
		createTheUserForSingleUserMode();
		
		int numThreads = 8;
		if (httpsPort != null) {
			numThreads += 3;
		}

		final int idleTimeout = 60000;
		final BlockingQueue<Runnable> queue = new BlockingArrayQueue<>(10000);
		final Server server = new Server(new QueuedThreadPool(numThreads,
				numThreads, idleTimeout, queue));

		// HTTP Configuration
		final HttpConfiguration http_config = new HttpConfiguration();
		http_config.setSecureScheme("https");
		if (httpsPort != null) {
			http_config.setSecurePort(httpsPort);
		}

		http_config.setOutputBufferSize(32768);

		// HTTP connector
		final ServerConnector http = new ServerConnector(server,
				new HttpConnectionFactory(http_config));
		http.setPort(httpPort);
		http.setIdleTimeout(30000);

		final ArrayList<Connector> connectors = new ArrayList<Connector>();
		connectors.add(http);

		if (keyStorePath != null) {
			// SSL requires a certificate so we configure a factory for SSL
			// contents
			// with information pointing to what keystore the SSL connection
			// needs
			// to know about.
			final SslContextFactory sslContextFactory = new SslContextFactory();
			sslContextFactory.setKeyStorePath(keyStorePath.getAbsolutePath());
			if (keyStorePassword != null) {
				sslContextFactory.setKeyStorePassword(keyStorePassword);
			}
			
			if (keyManagerPassword != null) {
				sslContextFactory.setKeyManagerPassword(keyManagerPassword);
			}

			// HTTPS configuration.
			final HttpConfiguration https_config = new HttpConfiguration(
					http_config);
			https_config.addCustomizer(new SecureRequestCustomizer());

			// HTTPS connector.
			final ServerConnector https = new ServerConnector(server,
					new SslConnectionFactory(sslContextFactory,
							HttpVersion.HTTP_1_1.asString()),
					new HttpConnectionFactory(https_config));
			https.setPort(httpsPort);
			https.setIdleTimeout(500000);

			connectors.add(https);
		}

		server.setConnectors(connectors.toArray(new Connector[connectors.size()]));

		final ContextHandlerCollection contexts = createContexts(
				temporaryDirectory, sessionStoreDirectory);

		// Set a handler
		if (logDirectory == null) {
			server.setHandler(contexts);
		} else {
			logDirectory.mkdirs();

			// Configure HTTP request logging.
			final HandlerCollection handlers = new HandlerCollection();

			final RequestLogHandler requestLogHandler = new RequestLogHandler();
			handlers.setHandlers(new Handler[] { contexts, requestLogHandler });
			server.setHandler(handlers);

			final NCSARequestLog requestLog = new NCSARequestLog(
					new File(logDirectory, "jetty-yyyy_mm_dd.request.log")
							.getAbsolutePath());
			requestLog.setRetainDays(90);
			requestLog.setAppend(true);
			requestLog.setExtended(false);
			requestLog.setLogTimeZone("GMT");
			requestLog.setLogLatency(true);
			requestLogHandler.setRequestLog(requestLog);
		}

		// Start the server
		try {
			server.start();
			server.join();
		} catch (final Throwable t) {
			logger.log(Level.SEVERE, t.getStackTrace().toString());
		}

		return true;
	}

	/** Returns a resource for the directory. */
	Resource getDirectoryResource(String directory, boolean isInJar,
			File installRootDirectory) {
		if (isInJar) {
			try {
				return JarResource.newJarResource(Resource
						.newResource(Servlet.class.getClassLoader()
								.getResource(directory)));
			} catch (final IOException e) {
			}
			return null;
		} else {
			return Resource.newResource(new File(installRootDirectory,
					directory));
		}
	}

	/** Creates a context handler for the directory. */
	private ContextHandler createContextHandler(String directory,
			boolean isInJar, File installRootDirectory, int expiresInSeconds) {
		final ContextHandler contextHandler = new ContextHandler();
		final ResourceHandler resourceHandler = new ExpiresResourceHandler(expiresInSeconds);
		final String directoryWithSlash = "/" + directory;
		contextHandler.setContextPath(directoryWithSlash);

		Resource directoryResource = getDirectoryResource(directory, isInJar,
				installRootDirectory);
		directoryResource = new JsMinifyingResource(directoryResource);

		if (isInJar) {
			directoryResource = new CachingResource(directoryResource,
					directoryWithSlash);
		}

		resourceHandler.setBaseResource(directoryResource);

		if (!isInJar) {
			// This makes development easier because Eclipse can copy files
			// to the target directory on each save on Windows.
			resourceHandler.setMinMemoryMappedContentLength(0);
		}

		contextHandler.setHandler(resourceHandler);

		return contextHandler;
	}

	/**
	 * Creates the context that handle HTTP requests.
	 * 
	 * @throws IOException
	 */
	public ContextHandlerCollection createContexts(File temporaryDirectory,
			File sessionStoreDirectory) throws IOException {
		// Enable HTTP session tracking with cookies.
		final ExposedShutdownServletContextHandler htmlJsonContext = new ExposedShutdownServletContextHandler(
				ServletContextHandler.SESSIONS);
		if (sessionStoreDirectory != null) {
			sessionManager = ((ExposedShutdownHashSessionManager) htmlJsonContext
					.getSessionHandler().getSessionManager());
			sessionManager.setStoreDirectory(sessionStoreDirectory);
			sessionManager.setSavePeriod(30);
		}

		htmlJsonContext.setContextPath("/");

		// Enable uploading files.
		final ServletHolder holder = new ServletHolder(this);
		holder.getRegistration().setMultipartConfig(
				new MultipartConfigElement(temporaryDirectory.getPath()));
		htmlJsonContext.addServlet(holder, "/");

		// Get the location static files will be served from.
		final URL location = Servlet.class.getProtectionDomain()
				.getCodeSource().getLocation();
		final File installRootDirectory = new File(location.getFile());

		isInJar = getClassResourceName().startsWith("jar:");
		
		int dynamicContexExpiresInSeconds = 2 * 60;
		final ContextHandler cssContextHandler = createContextHandler("css",
				isInJar, installRootDirectory, dynamicContexExpiresInSeconds);
		final ContextHandler jsContextHandler = createContextHandler("js",
				isInJar, installRootDirectory, dynamicContexExpiresInSeconds);
		final ContextHandler imagesContextHandler = createContextHandler(
				"images", isInJar, installRootDirectory, 3600);

		helpDirectoryResource = getDirectoryResource("doc", isInJar,
				installRootDirectory);

		// Create a ContextHandlerCollection and set the context handlers to it.
		final ContextHandlerCollection contextHandlers = new ContextHandlerCollection();
		contextHandlers.setHandlers(new Handler[] { imagesContextHandler,
				jsContextHandler, cssContextHandler, htmlJsonContext });

		return contextHandlers;
	}

	/**
	 * Returns the name of the resource this class is embedded in which might be
	 * a JAR.
	 */
	private String getClassResourceName() {
		return this.getClass().getResource("Servlet.class").toString();
	}

	/** Returns the version number embedded in the JAR name or null. */
	private String getVersionNumber() {
		final String maybeJarName = getClassResourceName();
		final Pattern pattern = Pattern
				.compile("^.*\\-(\\d+\\.\\d+\\.\\d+[^.]*)\\.jar.*$");
		final Matcher matcher = pattern.matcher(maybeJarName);
		if (matcher.matches()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}
}
