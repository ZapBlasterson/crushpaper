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

import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import com.crushpaper.Servlet.RequestAndResponse;

/**
 * Contains the logic for deciding which list items should be included in a
 * page.
 */
public class ResultsPaginator {
	ResultsPaginator(RequestAndResponse requestAndResponse,
			String noMatchesText, StringBuilder result, ServletText servletText) {
		this.requestAndResponse = requestAndResponse;
		this.noMatchesText = noMatchesText;
		this.result = result;
		this.servletText = servletText;

		start = getStartForPagination(requestAndResponse);
		end = start + getPageSizeForPagination();
		resultNumber = start;
	}

	/**
	 * Returns the next action the iterator should take. 0 means break.
	 * Otherwise it is the 1 based index of the result.
	 */
	int next() {
		++resultNumber;
		if (resultNumber > end) {
			return 0;
		}

		anyMatches = true;

		return resultNumber;
	}

	/**
	 * The iterator should call this when it has finished iterating to print out
	 * the right message.
	 */
	public void done() {
		if (resultNumber == (end + 1)) {
			getPreviousAndNextLinksForPagination(start != 0, true,
					requestAndResponse, result);
		} else if (start == 0 && !anyMatches) {
			result.append(noMatchesText);
		} else if (start != 0 && !anyMatches) {
			result.append(servletText.sentenceNoMoreResults());
		} else if (start != 0) {
			getPreviousAndNextLinksForPagination(true, false,
					requestAndResponse, result);
		}
	}

	/** Returns true if there would be more results. */
	public boolean hasMore() {
		return resultNumber == (end + 1);
	}

	/**
	 * Returns the name of the URL parameter that contains the start of the page
	 * for pagination.
	 */
	private String startParameterNameForPagination() {
		return "start";
	}

	/** Returns the page size for the pagination of the results. */
	private int getPageSizeForPagination() {
		return 20;
	}

	/** Returns the starting point for the pagination of the results. */
	private int getStartForPagination(RequestAndResponse requestAndResponse) {
		String start = requestAndResponse
				.getParameter(startParameterNameForPagination());
		if (start == null) {
			return 0;
		}

		try {
			return Integer.parseInt(start);
		} catch (Exception e) {
			return 0;
		}
	}

	/** Returns the HTML for the next link in pagination. */
	private void getPreviousAndNextLinksForPagination(
			boolean includePreviousLink, boolean includeNextLink,
			RequestAndResponse requestAndResponse, StringBuilder result) {
		StringBuilder baseUrl = new StringBuilder();
		baseUrl.append(StringEscapeUtils.escapeHtml4(requestAndResponse
				.getRequestURI()));

		int start = getStartForPagination(requestAndResponse)
				+ getPageSizeForPagination();

		boolean addedQuestionMark = false;
		java.util.Map<java.lang.String, java.lang.String[]> parameters = requestAndResponse
				.getParameterMap();
		for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
			String key = entry.getKey();

			if (key == null || key.equals(startParameterNameForPagination())
					|| key.equals("time"))
				continue;

			for (String value : entry.getValue()) {
				if (!addedQuestionMark) {
					baseUrl.append("?");
					addedQuestionMark = true;
				} else {
					baseUrl.append("&");
				}

				baseUrl.append(StringEscapeUtils.escapeHtml4(key));
				baseUrl.append("=");
				baseUrl.append(StringEscapeUtils.escapeHtml4(value));
			}
		}

		if (!addedQuestionMark) {
			baseUrl.append("?");
		} else {
			baseUrl.append("&");
		}

		baseUrl.append(startParameterNameForPagination());
		baseUrl.append("=");

		result.append("<table width=\"100%\"><tr><td>");

		if (includePreviousLink) {
			result.append("<a onclick=\"replacePaneForLink(event, '"
					+ servletText.pageTitleGetPreviousPage()
					+ "', true); return false;\" class=\"previousLink\" href=\"");

			result.append(baseUrl.toString());
			result.append(start - getPageSizeForPagination() * 2);

			result.append("\">");
			result.append(servletText.linkPrevious());
			result.append("</a>");
		}

		if (includeNextLink) {
			result.append("<a onclick=\"replacePaneForLink(event, '"
					+ servletText.pageTitleGetNextPage()
					+ "', true); return false;\" class=\"nextLink\" href=\"");

			result.append(baseUrl.toString());
			result.append(start);

			result.append("\">");
			result.append(servletText.linkNext());
			result.append("</a>");
		}

		result.append("</td></tr></table>");
	}

	/**
	 * Returns the starting position of the query. This is a multiple of the
	 * page size.
	 */
	public int getStartPosition() {
		return start;
	}

	/**
	 * Returns the number of results that should be returned by a query for this
	 * page. This is the page size + 1 so that it can be determined if a next
	 * link should be included.
	 */
	public int getMaxResults() {
		return getPageSizeForPagination() + 1;
	}

	RequestAndResponse requestAndResponse;
	StringBuilder result;
	private String noMatchesText;
	ServletText servletText;

	private int resultNumber, start, end;
	private boolean anyMatches;
}
