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

/* global uiTextEn, Mousetrap */

var uiText = null;

var isFireFox = navigator.userAgent.toLowerCase().indexOf('firefox') > -1;
var isIe = window.navigator.userAgent.indexOf("MSIE ") > 0 || !!navigator.userAgent.match(/Trident.*rv\:11\./);

/** Returns the element corresponding to the browser event. */
function getEventEl(ev) {
	if (!ev) {
		ev = window.event;
	}

	var eventEl = null;
	if (ev.target) {
		eventEl = ev.target;
	} else if (ev.srcElement) {
		eventEl = ev.srcElement;
	}

	if (eventEl && eventEl.nodeType === 3) {
		eventEl = eventEl.parentNode;
	}

	return eventEl;
}

/** Returns true if the haystack string contains the needle string as a word, i.e. surrounded by spaces . */
function containsToken(needle, haystack, ignoreCase) {
	if (ignoreCase) {
		needle = needle.toLowerCase();
		haystack = haystack.toLowerCase();
	}

	if (haystack.indexOf(' ' + needle + ' ') !== -1) {
		return true;
	}

	if (haystack.substr(0, needle.length + 1) === needle + ' ') {
		return true;
	}

	if (haystack.substr(haystack.length - (needle.length + 1)) === ' ' + needle) {
		return true;
	}

	if (haystack === needle) {
		return true;
	}

	return false;
}

/** Returns true if the element has the specified tag and class. If class is null only the tag is checked. */
function isElementOfTagAndClass(el, tag, className) {
	return el.nodeName === tag && (!className || isElementOfClass(el, className));
}

/** Returns true if the element has the specified class. */
function isElementOfClass(el, className) {
	return el.className && containsToken(className, el.className, false);
}

/** Returns the closest ancestor of the element, potentially the element itself, that has the specified tag and class.
 *  If breakPointIdOrClass is true and begins with a "." and an element with a class of breakPointIdOrClass is found first then null is returned.
	If breakPointIdOrClass is true and does not begin with a "." and an element with id==breakPointIdOrClass is found first then null is returned.
	If no such element found then null is returned.
 */
function getElOrAncestor(el, nodeName, className, breakPointIdOrClass) {
	var breakPointId = null;
	var breakPointClass = null;
	if (breakPointIdOrClass) {
		if (breakPointIdOrClass.substring(0, 1) === ".") {
			breakPointClass = breakPointIdOrClass.substring(1);
		} else {
			breakPointId = breakPointIdOrClass;
		}
	}

	while (1) {
		if (!el) {
			return;
		}

		if (isElementOfTagAndClass(el, nodeName, className)) {
			return el;
		}

		if (breakPointId && el.id === breakPointId) {
			return;
		}

		if (breakPointClass && isElementOfClass(el, breakPointClass)) {
			return;
		}

		el = el.parentNode;
	}
}

/** Forwards the page to it's https equivalent if one is available. */
function forwardToHttpsIfAvailableOrShow(search, func) {
	if (("httpsPort" in window.sessionDict) && location.protocol !== "https:" && !window.sessionDict.isSignedIn) {
		var newPathname = location.pathname;
		if (newPathname === "/signedOut/") {
			newPathname = "/";
		}

		var toConcat = "";
		if (location.search !== search) {
			toConcat = search;
		}

		var portString = "";
		if(window.sessionDict.httpsPort !== "443") {
			portString = ":" + window.sessionDict.httpsPort;
		}
		
		location.assign("https://" + location.hostname + portString +
				concatUrl(newPathname + location.search, toConcat));
	} else {
		func();
	}
}

/** Starts the process of showing the signin dialog. */
function signIn() {
	forwardToHttpsIfAvailableOrShow("signIn=1", showSignInPopup);
}

/** Shows the sign in popup. */
function showSignInPopup() {
	var popup = createPopupForDialog(false, [ function() { Mousetrap.unbind("enter"); } ] );
	var title = uiText.popupTitleSignIn();
	var html = decoratePopupTitle(title);
	html += "<div class=\"account\"><input class=\"mousetrap\" autocorrect=\"off\" type=\"text\" id=\"username\" placeholder=\"" + uiText.sentenceYourUserName() + "\" maxlength=\"20\"><br>" +
	"<input class=\"mousetrap\" type=\"password\" id=\"password\" placeholder=\"" + uiText.sentenceYourPassword() + "\" maxlength=\"20\"><br>" +
	"<table class=\"responseAndSave\"><tr>" +
	"<td><div id=\"response\"></div></td>" +
	"<td><button id=\"submit\" class=\"specialbutton\" style=\"float:right;\" onclick=\"postSignIn(); return false;\">" + uiText.buttonSignIn() + "</button></td>" +
	"</tr></table></div>";

	addThenCenterPopup(popup, html);
	Mousetrap.bind("enter", postSignIn);
}

/** Shows the sign in not needed dialog. */
function alreadySignedIn() {
	var popup = createPopupForDialog(false, [ function() { Mousetrap.unbind("enter"); }, addGlobalShortCuts ] );
	var title = uiText.popupTitleAlreadySignedIn();
	var html = decoratePopupTitle(title);
	html += uiText.sentenceSignInNotNeeded();

	addThenCenterPopup(popup, html);
	Mousetrap.bind("enter", closePopup);
}

/** Starts the process of showing the create account dialog. */
function createAccount() {
	forwardToHttpsIfAvailableOrShow("createAccount=1", showCreateAccountPopup);
}

/** Shows the create account dialog. */
function showCreateAccountPopup() {
	var popup = createPopupForDialog(false);
	var title = uiText.popupTitleCreateAccount();
	var html = decoratePopupTitle(title);
	html += "<div class=\"account\"><input class=\"mousetrap\" autocorrect=\"off\" type=\"text\" id=\"username\" placeholder=\"" + uiText.sentenceChooseAUserName() + "\" maxlength=\"20\"><br>" +
	"<input class=\"mousetrap\" type=\"password\" id=\"password\" placeholder=\"" + uiText.sentenceChooseAPassword() + "\" maxlength=\"20\"><br>" +
	"<input class=\"mousetrap\" type=\"password\" id=\"password2\" placeholder=\"" + uiText.sentenceVerifyPassword() + "\" maxlength=\"20\"><br>" +
	"<input class=\"mousetrap\" type=\"email\" id=\"email\" placeholder=\"" + uiText.sentenceEmailOptional() + "\" maxlength=\"100\"><br>" +
	"<input type=\"checkbox\" name=\"mayContact\" id=\"mayContact\"><label for=\"mayContact\">" + uiText.sentenceMayBeContacted() + "</label><br>" +
	"<table class=\"responseAndSave\"><tr>" +
	"<td><div id=\"response\"></div></td>" +
	"<td><button id=\"submit\" class=\"specialbutton\" style=\"float:right;\" onclick=\"postCreateAccount(); return false;\">" + uiText.buttonCreateAccount() + "</button></td>" +
	"</tr></table></div>";


	addThenCenterPopup(popup, html);
}

/** Sets the error text of a popup window. */
function setResponseErrorMessage(errorText, responseId) {
	responseId = (typeof responseId === 'undefined') ? "response" : responseId;

	document.getElementById(responseId).innerHTML = "<span class=\"errorMessage\">" + errorText + "</span>";
}

/** Returns the set of errors as a single string. */
function getErrorText(xhr, whatDidNotHappen, whatProbablyDidNotHappen) {
	var response;
	if (xhr.responseText === "") {
		return uiText.errorEmptyResponseFromServer(whatProbablyDidNotHappen, xhr.status);
	} else {
		try {
			response = JSON.parse(xhr.responseText);
		} catch(e) {
			return uiText.errorJsonCouldNotBeParsed(whatProbablyDidNotHappen);
		}
	}

	var result = whatDidNotHappen;
	if ("errors" in response) {
		for (var i = 0; i < response.errors.length; ++i) {
			result += "<br>" + response.errors[i];
		}
	}

	return result;
}

/** Returns a string to append to an URL so that a request for the URL can not be get cached by the browser. */
function getAnUrlUniquer() {
	return "time=" + new Date().getTime();
}

/** Closes the sign in popup and changes the location to make it less confusing. */
function closeSignedInOrCreateAccountPopup() {
	setTimeout(function() {
		if (location.pathname === "/" && (location.search === "?signIn=1" || location.search === "?createAccount=1")) {
			location.assign("/");
		} else if (location.pathname === "/signedOut/") {
			location.assign("/");
		} else {
			location.reload();
		} }, 500);
}

/** Create an async HTTP request and set the response and timeout handler. */
function createAsyncRequest(method, url, func) {
	var xhr = new XMLHttpRequest();

	xhr.open(method, url, true);

	setResponseHandler(xhr, func);

	return xhr;
}

/** Set the response and timeout handler for an HTTP request. */
function setResponseHandler(xhr, func) {
	xhr.onreadystatechange = function() {
		if (xhr.readyState === 4) {
			func();
		}
	};

	xhr.timeout = 60 * 1000;
}

/** Create an async JSON HTTP request and set the response and timeout handler. */
function createJsonAsyncRequest(method, url, func) {
	var xhr = createAsyncRequest(method, url, func);
	xhr.setRequestHeader("Content-Type", "application/json; charset=utf-8");
	return xhr;
}

/** Sends the create account request. */
function postCreateAccount() {
	// Prevent double submit.
	document.getElementById("submit").disabled = true;
	document.getElementById("username").disabled = true;
	document.getElementById("password").disabled = true;
	document.getElementById("password2").disabled = true;
	document.getElementById("email").disabled = true;
	document.getElementById("mayContact").disabled = true;

	var xhr = createAsyncRequest("POST", "/createAccount?" + getAnUrlUniquer(), function() {
		aRequestIsInProgress(false);

		if (xhr.status === 200) {
			document.getElementById("response").innerHTML = "<span class=\"successMessage\">" + uiText.sentenceAccountCreated() + "</span>";
			closeSignedInOrCreateAccountPopup();
		} else {
			var errorText = getErrorText(xhr, uiText.errorAccountNotCreated(), uiText.errorProbablyAccountNotCreated());
			setResponseErrorMessage(errorText);
			document.getElementById("submit").disabled = false;
			document.getElementById("username").disabled = false;
			document.getElementById("password").disabled = false;
			document.getElementById("password2").disabled = false;
			document.getElementById("email").disabled = false;
			document.getElementById("mayContact").disabled = false;
		}
	});

	var message = {
			'username' : document.getElementById("username").value,
			'password' : document.getElementById("password").value,
			'password2' : document.getElementById("password2").value,
			'email' : document.getElementById("email").value,
			'mayContact' : isElementChecked("mayContact"),
			'csrft': getCsrft()
	};

	document.getElementById("response").innerHTML = uiText.sentenceCreatingAccount();
	xhr.send(JSON.stringify(message));
	aRequestIsInProgress(true);
}

/** Sends the sign in request. */
function postSignIn() {
	// Prevent double submit.
	document.getElementById("submit").disabled = true;
	document.getElementById("username").disabled = true;
	document.getElementById("password").disabled = true;

	var xhr = createAsyncRequest("POST", "/signIn?" + getAnUrlUniquer(), function() {
		aRequestIsInProgress(false);

		if (xhr.status === 200) {
			document.getElementById("response").innerHTML = "<span class=\"successMessage\">" + uiText.sentenceSignedIn() + "</span>";
			closeSignedInOrCreateAccountPopup();
		} else {
			var errorText = getErrorText(xhr, uiText.errorNotSignedIn(), uiText.errorProbablyNotSignedIn());
			setResponseErrorMessage(errorText);
			document.getElementById("submit").disabled = false;
			document.getElementById("username").disabled = false;
			document.getElementById("password").disabled = false;
		}
	});

	var message = {
			'username' : document.getElementById("username").value,
			'password' : document.getElementById("password").value,
			'csrft': getCsrft()
	};

	document.getElementById("response").innerHTML = uiText.sentenceSigningIn();
	xhr.send(JSON.stringify(message));
	aRequestIsInProgress(true);
	return false;
}

/** Sends the signout request. */
function signOut(noMatterWhat) {
	var xhr = createAsyncRequest("POST", "/signOut?" + getAnUrlUniquer(), function() {
		aRequestIsInProgress(false);
		if (xhr.status === 200) {
			location.assign("/signedOut/");
		} else {
			var errorText = uiText.errorProbablyNotSignedOut();
			var response;
			if (xhr.responseText !== "") {
				try {
					response = JSON.parse(xhr.responseText);
				} catch(e) {
					errorText = uiText.errorJsonCouldNotBeParsed(uiText.errorProbablyNotSignedOut());
				}
			}
			
			if (("needsPassword" in response) || ("needsUsername" in response)) {
				errorText = uiText.errorNotSignedOutDetails(response.needsPassword, response.needsUsername);
				errorText += "<button class=\"specialbutton\" style=\"margin-top:10px; margin-right:20px; float:none;\" onclick=\"setAccountInfo(); return false;\">" + uiText.buttonSetAccountInfo() + "</button>";
				errorText += "<button class=\"specialbutton\" style=\"margin-top:10px; float:none;\" onclick=\"signOut(true); return false;\">" + uiText.buttonForceSignOut() + "</button>";
			} else {
				errorText = getErrorText(xhr, uiText.errorNotSignedOut(), uiText.errorProbablyNotSignedOut());
			}
			
			var title = uiText.popupTitleSignOut();
			
			showPopupForError(title, errorText);
		}
	});

	var message = { 'csrft': getCsrft() };
	if(noMatterWhat) {
		message.noMatterWhat = true;
	}
	
	xhr.send(JSON.stringify(message));
	aRequestIsInProgress(true);
}

/** Close everything and allow the user to update their account. */
function setAccountInfo() {
	closePopup();
	closeAllPanes(true);
	makePane("/changeAccount/", "Account", null, "account");
} 

/** Returns the mouse offset of the current event. */
function getMouseOffset(target, ev) {
	var targetPos = getPosition(target);
	var mousePos = getMousePosition(ev);
	return {
		x : mousePos.x - targetPos.x - getScrollLeft(),
		y : mousePos.y - targetPos.y - getScrollTop()
	};
}

/** Returns the position of the element. */
function getPosition(el) {
	var rect = el.getBoundingClientRect();
	return { x: rect.left , y: rect.top };
}

/** Returns the position of the mouse for an event. */
function getMousePosition(ev) {
	if (ev.pageX || ev.pageY) {
		return {
			x : ev.pageX,
			y : ev.pageY
		};
	}

	return {
		x : ev.clientX + document.body.scrollLeft - document.body.clientLeft,
		y : ev.clientY + document.body.scrollTop - document.body.clientTop
	};
}

// Globals for popup dragging.
var draggedPopupMouseOffset = null;
var draggedPopup = null;

/** Handles mousemove for a popup. */
function popupOnMouseMove(ev) {
	if (draggedPopup) {
		var mousePos = getMousePosition(ev);
		draggedPopup.style.top = mousePos.y - draggedPopupMouseOffset.y + "px";
		draggedPopup.style.left = mousePos.x - draggedPopupMouseOffset.x + "px";
		return false;
	}
}

/** Handles mouseup for a popup. */
function popupOnMouseUp() {
	if (!draggedPopup) {
		return;
	}

	draggedPopup = null;
	document.onmousemove = null;
}

/** Handles mousedown for a popup. */
function popupOnMouseDown(ev) {
	// It is important that you can still select text.
	var eventEl = getEventEl(ev);

	if (!isElementOfClass(eventEl, "popupSection") && !isElementOfClass(eventEl, "popupSectionError")) {
		return true;
	}

	draggedPopup = window.popup;
	draggedPopupMouseOffset = getMouseOffset(draggedPopup, ev);
	document.onmousemove = popupOnMouseMove;
	return false;
}

var areCommandsAllowed = true;
var areCommandsAllowedCounter = 0;

/** Changes whether or not commands are allowed. */
function commandsAreNowAllowed(areThey) {
	areCommandsAllowedCounter += (areThey ? 1 : -1);
	areCommandsAllowed = areCommandsAllowedCounter === 0;
}

/** Returns true if a popup is up. */
function isPopupUp() {
	if (!("popup" in window)) {
		return false;
	}

	return true;
}

var previousDocumentCursor = null;
var inProgressRequests = 0;

/** Updates the document to reflect whether any requests are in progress. */
function aRequestIsInProgress(isIt) {
	var oldInProgressRequests = inProgressRequests;
	inProgressRequests += isIt ? 1 : -1;

	if (oldInProgressRequests === 0 && inProgressRequests === 1) {
		previousDocumentCursor = document.body.style.cursor;
		document.body.style.cursor = "progress";
	} else if (inProgressRequests === 0) {
		document.body.style.cursor = previousDocumentCursor;
	}
}

/** Updates the document to reflect that no requests are in progress. */
function noRequestsAreInProgress() {
	inProgressRequests = 0;
	if (document.body.style.cursor === "progress") {
		document.body.style.cursor = previousDocumentCursor;
	}
}

/** Handles clicking anywhere on the document except the popup to dismiss the popup.
 * It the user has changed an important field then clicking anywhere will not close the popup.
 * */
function clickToClosePopup(ev) {
	var eventEl = getEventEl(ev);
	if (eventEl.id && eventEl.id === "overlay") {
		var noteEl = document.getElementById('noteinput');
		var quotationEl = document.getElementById('quotationinput');
		var noteOriginalEl = document.getElementById('noteoriginal');
		var quotationOriginalEl = document.getElementById('quotationoriginal');
		if (noteEl && noteOriginalEl) {
			if (noteEl.value !== noteOriginalEl.value) {
				return;
			}
		} else if (noteEl) {
			if (noteEl.value) {
				return;
			}
		}

		if (quotationEl && quotationOriginalEl && quotationEl.value !== quotationOriginalEl.value) {
			return;
		}

		closePopup();
	}
}

/** Logic for closing an error popup. */
function onErrorPopupClose() {
	Mousetrap.unbind("enter");
}

/** Adds the event listener. */
function attachEventListener(element, name, func) {
	if (element.addEventListener) {
		element.addEventListener(name, func);
	} else if (element.attachEvent) {
		element.attachEvent('on' + name, func);
	}
}

/** Returns the type for what can be selected based on what is displayed on the page. */
function getDefaultEntryTypes() {
	var hasNotes = doAnyContainerPanesExist();
	var hasNotebook = document.getElementById("notebooks");
	if (hasNotes && hasNotebook) {
		return [ "note", "notebook" ];
	}

	if (hasNotebook) {
		return "notebook";
	}

	return "note";
}

/** Shows a popup with an error message. */
function showPopupForError(title, message, onCloseCallbacks) {
	if (isPopupUp()) {
		return;
	}

	removeGlobalShortCuts();

	Mousetrap.bind("enter", function() {
		closePopup();
		return false;
	});

	if (!onCloseCallbacks) {
		onCloseCallbacks = [];
	}

	onCloseCallbacks.unshift(onErrorPopupClose);
	onCloseCallbacks.push(addGlobalShortCuts);
	var popup = createPopup(true, onCloseCallbacks);

	var html = decoratePopupTitle(title, true) + message;
	addThenCenterPopup(popup, html);
}

/** Creates a popup window. */
function createPopup(forError, onCloseCallbacks) {
	commandsAreNowAllowed(false);

	Mousetrap.bind("esc", function() {
		if (closePopup()) {
			return;
		}
	});

	attachEventListener(document, "mousedown", clickToClosePopup);
	var popup = window.popup = document.createElement('DIV');

	if (!onCloseCallbacks) {
		onCloseCallbacks = [];
	}

	onCloseCallbacks.unshift(function() {
		Mousetrap.unbind("esc");
		document.removeEventListener("mousedown", clickToClosePopup);
		} );

	window.popupOnCloseCallbacks = onCloseCallbacks;
	popup.onmousedown = popupOnMouseDown;
	popup.onmouseup = popupOnMouseUp;

	popup.className = "popup";

	if (forError) {
		popup.className += " error";
	}

	document.getElementById("overlay").style.display = "block";

	return popup;
}

/** Creates a popup window. */
function createPopupForDialog(supportsSaving, onCloseCallbacks) {
	removeGlobalShortCuts();

	if (supportsSaving) {
		addPopupShortCuts();
	}

	if (!onCloseCallbacks) {
		onCloseCallbacks = [];
	}

	onCloseCallbacks.push(removePopupShortCuts, addGlobalShortCuts);

	return createPopup(false, onCloseCallbacks);
}

/** Closes any popup which is up. */
function closePopup() {
	if (!isPopupUp()) {
		return false;
	}

	if (window.popupOnCloseCallbacks) {
		for (var i = 0; i < window.popupOnCloseCallbacks.length; ++i) {
			window.popupOnCloseCallbacks[i]();
		}
	}

	document.getElementById("overlay").style.display = "none";

	var popup = window.popup;
	popup.style.display = 'none';
	popup.parentNode.removeChild(popup);
	delete window.popup;

	commandsAreNowAllowed(true);

	return true;
}

/** Returns the title of a popup window decorated with HTML. */
function decoratePopupTitle(text, isError) {
	var result = "<div class=\"popupSection" + (isError ? "Error" : "") + "\">" +
	"<div class=\"popupSectionContents popupSectionTitle\">" +
	"<span class=\"popuptitle\">" + text + "</span>";

	result += "<div style=\"float:right\" class=\"paneCloseIcon\" title=\"" +
	uiText.tooltipClosePopup(isError) +
	"\" onclick=\"closePopup(); return false;\"></div>" +
	"</div></div><div class=\"popupSection" + (isError ? "Error" : "") + "\"><div class=\"popupSectionContents\">";
	return result;
}

/** Add the popup to the document, sets its display to block, and centers it. */
function addThenCenterPopup(popup, html) {
	popup.innerHTML = html + "</div></div>";
	document.body.appendChild(popup);
	popup.style.display = "block";
	centerEl(popup);

	// Do not auto focus on mobile. It messes up the window.
	if (!isDisplaySmall()) {
		var note = document.getElementById('noteinput');
		if (note) {
			note.focus();
		}

		var username = document.getElementById('username');
		if (username) {
			username.focus();
		}
	}
}

/** Returns the window's inner height. */
function getWindowInnerHeight() {
	if (window.innerHeight) {
		return window.innerHeight;
	} else if (document.documentElement &&
			document.documentElement.clientHeight) {
		return document.documentElement.clientHeight;
	} else {
		return document.body.clientHeight;
	}
}

/** Returns the window's inner width. */
function getWindowInnerWidth() {
	if (window.innerWidth) {
		return window.innerWidth;
	} else if (document.documentElement&&
			document.documentElement.clientWidth) {
		return document.documentElement.clientWidth;
	} else {
		return document.body.clientWidth;
	}
}

/** Centers the element on the document.
This function does not account for scrollbar presence or width.  */
function centerEl(el) {
	var newElOffsetLeft = (getWindowInnerWidth() - el.offsetWidth) / 2;
	var newElOffsetTop = (getWindowInnerHeight() - el.offsetHeight) / 2;

	// Make sure you can always see the left and top of the element.
	if (newElOffsetLeft < 0) {
		newElOffsetLeft = 0;
	}

	if (newElOffsetTop < 0) {
		newElOffsetTop = 0;
	}

	el.style.top = (newElOffsetTop + getScrollTop()) + 'px';
	el.style.left = (newElOffsetLeft + getScrollLeft()) + 'px';
}

/** Returns the number of pixels the document is scrolled from the top. */
function getScrollTop() {
	return document.body.scrollTop || window.pageYOffset;
}

/** Returns the number of pixels the document is scrolled from the left. */
function getScrollLeft() {
	return document.body.scrollLeft || window.pageXOffset;
}

/** Resizes popups when the window resizes. */
function recenterPopupWindow() {
	if (!isPopupUp())
		return;

	centerEl(window.popup);
}

/** Takes any number of arrays or lists as arguments and copies them into one array.
Good for creating static lists from live lists, i.e. that change dynamically.
 */
function copyArray() {
	var result = [];
	for (var i = 0; i < arguments.length; ++i) {
		var source = arguments[i];
		for (var j = 0; j < source.length; ++j) {
			result[result.length] = source[j];
		}
	}

	return result;
}

/** Finds all elements with the className and updates the parents of those nodes with the output of the
func run on the element. */
function updateParentsFromChildren(className, func, root) {
	var els = copyArray(root.getElementsByClassName(className));
	for (var i = 0; i < els.length; ++i) {
		var el = els[i];
		var parent = el.parentNode;
		parent.innerHTML = func(el.innerHTML * 1);
	}
}

/** Updates all dates and time in the page. */
function updateAllDatesAndTimes(root) {
	updateParentsFromChildren("rawDateTime", uiText.formatDateTime, root);
}

/** Returns the value of the cookie with the specified name. */
function getCookie(name) {
	var value = "; " + document.cookie;
	var parts = value.split("; " + name + "=");
	if (parts.length === 2) {
		return parts.pop().split(";").shift();
	}

	return "";
}

/** Returns the value of the cross site scripting prevention token. */
function getCsrft() {
	return getCookie("JSESSIONID");
}

/** Returned the ie version or null.
 * Based on https://gist.github.com/padolsey/527683
 */
function getIeVersion() {
	var undef,
	v = 3,
	div = document.createElement('div'),
	all = div.getElementsByTagName('i');

	while (
			div.innerHTML = '<!--[if gt IE ' + (++v) + ']><i></i><![endif]-->',
			all[0]
	);

	return v > 4 ? v : undef;
}

/** Displays a warning if an unsupported version of IE is used. */
function displayIeLt8Message() {
	var ieVersion = getIeVersion();
	if (ieVersion && ieVersion < 8) {
		var siteRequirementsEl = document.getElementById("siteRequirements");
		siteRequirementsEl.innerHTML = uiText.errorIe7Warning();
		siteRequirementsEl.style.display = "block";
	}
}

/** Returns the containing pane. */
function getContainingPaneEl(el) {
	return getElOrAncestor(el, 'DIV', 'pane', 'allPanes');
}

/** Returns the containing A node. */
function getContainingLinkEl(el) {
	return getElOrAncestor(el, 'A', null, 'allPanes');
}

/** Returns the containing FORM node. */
function getContainingFormEl(el) {
	return getElOrAncestor(el, 'FORM', null, 'allPanes');
}

/** Returns the url hopefully as it was in the source. Only works for a relative link. */
function getPathPlusSearch(aEl) {
	return aEl.pathname + aEl.search;
}

/** Create a pane with the result of a link. */
function newPaneForLink(ev, title, paneId) {
	var eventEl = getEventEl(ev);
	var aEl = getContainingLinkEl(eventEl);
	makePane(getPathPlusSearch(aEl), title, null, paneId);
}

/** Replace a pane with the result of a link. */
function replacePaneForLink(ev, title, doNotChangeTitle) {
	var eventEl = getEventEl(ev);
	var paneEl = getContainingPaneEl(eventEl);
	var aEl = getContainingLinkEl(eventEl);
	makePane(getPathPlusSearch(aEl), title, paneEl, null, doNotChangeTitle);
}

/** Refresh the contents of the pane. */
function refreshPane(ev) {
	var eventEl = getEventEl(ev);
	var paneEl = getContainingPaneEl(eventEl);
	makePane(getUrlForPane(paneEl), null, paneEl);
}

/** Refresh the contents of the pane. */
function refreshPaneById(id, closeIfNotFound, unlessIsPaneEl) {
	var paneEl = document.getElementById(id);
	if (paneEl && paneEl !== unlessIsPaneEl) {
		makePane(getUrlForPane(paneEl), null, paneEl, null, false, null, closeIfNotFound);
	}
}

/** Refreshes the backups pane. */
function refreshBackupsPane() {
	refreshPaneById("backups");
}

/** Refreshes the search pane. */
function refreshSearchPane(unlessIsPaneEl) {
	refreshPaneById("search", false, unlessIsPaneEl);
}

/** Refreshes the quotations pane. */
function refreshQuotationsPane(unlessIsPaneEl) {
	refreshPaneById("quotations", false, unlessIsPaneEl);
}

/** Refreshes the sources pane. */
function refreshSourcesPane(unlessIsPaneEl) {
	refreshPaneById("sources", false, unlessIsPaneEl);
}

/** Refreshes all source panes. */
function refreshAllSourcePanes(unlessIsPaneEl) {
	var allPanes = getAllPanesEl();
	for (var i = 1; i < allPanes.childNodes.length; ++i) {
		var paneIndex = i - 1;
		var paneType = getPaneType(paneIndex);
		if (paneType === "source") {
			var paneEl = getPaneElByIndex(paneIndex);
			refreshPaneById(paneEl.id, true, unlessIsPaneEl);
		}
	}
}

/** Replace a pane with the result of a form submission. */
function replacePaneForForm(ev, title, functionToCallAfter) {
	var eventEl = getEventEl(ev);
	var paneEl = getContainingPaneEl(eventEl);
	var formEl = getContainingFormEl(eventEl);
	makePane(formEl, title, paneEl, null, false, functionToCallAfter);
}

/** Create a pane with the result of a form submission. */
function newPaneForForm(ev, title, paneId) {
	var eventEl = getEventEl(ev);
	var formEl = getContainingFormEl(eventEl);
	makePane(formEl, title, null, paneId);
}

/** Creates a new tab or window. */
function newTab(ev) {
	ev.preventDefault();
	ev.stopPropagation();

	var eventEl = getEventEl(ev);
	var aEl = getContainingLinkEl(eventEl);
	window.open(aEl.href, "_blank");
}

/** Concatenates a fragment of a query to an url. */
function concatUrl(url, queryFragment) {
	if (url.indexOf("?") === -1) {
		url += "?";
	} else {
		url += "&";
	}

	return url + queryFragment;
}

/** Concatenates a fragment of a query to an url by using an @ and double encoding it. */
function concatAndDoubleEncodeUrl(url, queryFragment) {
	if (url.indexOf("/", 1) === -1) {
		url += "/";
	}

	return url + (queryFragment !== "" ? "@" : "")  + encodeURIComponent(queryFragment);
}

/** Replaces the ? with and @ and double encodes the query. */
function doubleEncodeUrl(url) {
	var questionIndex = url.indexOf("?");
	if (questionIndex === -1) {
		return url;
	}

	var uri = url.substr(0, questionIndex);
	var query = url.substr(questionIndex + 1);
	return uri + (query !== "" ? "@" : "") + encodeURIComponent(query);
}

/** Appends an unique query id to an url. */
function addUrlUniquer(url) {
	return concatUrl(url, getAnUrlUniquer());
}

/** Removes the server and port from an URL. */
function makeUrlRelative(url) {
	return url.replace(/^https?:\/\/[^\/]+/i, "");
}

/** Returns the allPanes element. */
function getAllPanesEl() {
	return document.getElementById("allPanes");
}

/** Returns true if the display is for a small device. */
function isDisplaySmall() {
	return getWindowInnerWidth() < 500;
}

/** Closes the menu if the the display is for a small device. */
function closeMenuIfSmallDisplay() {
	if (isDisplaySmall()) {
		showOrHideMenu(true);
	}
}

/** Creates or replaces a pane. */
function makePane(target, title, paneEl, paneId, doNotChangeTitle, functionToCallAfter, closeIfNotFound) {
	closeMenuIfSmallDisplay();

	var formUrlEncoded = null;
	var xhr = new XMLHttpRequest();
	var updateUrl = null;
	if (typeof target === 'string') {
		updateUrl = doubleEncodeUrl(target);
		xhr.open("GET", addUrlUniquer(updateUrl), true);
	} else {
		formUrlEncoded = "";
		for (var i = 0; i < target.elements.length; ++i) {
			var element = target.elements[i];
			if (element.name && ((element.type !== "radio" && element.type !== "checkbox") || element.checked)) {
				formUrlEncoded += (formUrlEncoded.length ? "&" : "") + encodeURIComponent(element.name) + "=" + encodeURIComponent(element.value);
			}
		}

		var url = makeUrlRelative(target.action);
		if (target.method.toLowerCase() === "get") {
			updateUrl = url = concatAndDoubleEncodeUrl(url, formUrlEncoded);
			formUrlEncoded = null;
		}

		xhr.open(target.method, addUrlUniquer(url), true);

		if (target.method.toLowerCase() === "post") {
			xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		}
	}

	xhr.setRequestHeader("X-no-header", "true");
	setResponseHandler(xhr, function() {
		aRequestIsInProgress(false);

		if (xhr.status === 200) {
			if (paneId) {
				paneEl = document.getElementById(paneId);
			}

			if (!paneEl) {
				if (isDisplaySmall()) {
					closeAllPanes(true);
				}

				var cellEl = document.createElement("DIV");

				var paneContainerEl = document.createElement("DIV");
				paneContainerEl.className = "paneContainer";

				paneEl = document.createElement("DIV");
				paneEl.className = "pane";
				if (paneId) {
					paneEl.id = paneId;
				}

				paneContainerEl.appendChild(paneEl);

				var allPanesEl = getAllPanesEl();
				cellEl.appendChild(paneContainerEl);
				allPanesEl.appendChild(cellEl);
			} else {
				onPaneUnload(paneEl);
			}

			if (paneId && paneId !== "welcome") {
				closeWelcomePane();
			}

			paneEl.innerHTML = xhr.responseText;

			updatePaneUrlAndTitle(paneEl, updateUrl, doNotChangeTitle ? null : title);
			updateMetaDataForPane(paneEl, getIndexOfPaneEl(paneEl));

			if (paneEl.id) {
				startPaneReloadedAnim(paneEl.id);
			}

			onPaneLoad(paneEl);

			if (functionToCallAfter) {
				functionToCallAfter();
			}
		} else {
			if (xhr.status === 404) {
				if (closeIfNotFound) {
					closePaneByEl(paneEl);
					return;
				}
			}

			if (!title) {
				title = getTitleForPane(paneEl);
			}

			showPopupForError(title, uiText.errorCouldNotGetPage());
		}
	});

	if (formUrlEncoded) {
		xhr.send(formUrlEncoded);
	} else {
		xhr.send();
	}

	aRequestIsInProgress(true);
}

/** Starts the animation for reloading the pane. */
function startPaneReloadedAnim(paneId) {
	var color = "#FDFFAA";
	var duration = 1500;
	startBackgroundTransition(paneId, color, duration);
	var buttonsId = "buttons_" + paneId;
	if (document.getElementById(buttonsId)) {
		startBackgroundTransition(buttonsId, color, duration);
	}
}

/** Creates a popup with the result of a link submission. */
function showPopupWithPage(ev, title) {
	var eventEl = getEventEl(ev);
	var xhr = createAsyncRequest("GET", addUrlUniquer(eventEl.href), function() {
		aRequestIsInProgress(false);

		if (xhr.status === 200) {
			var popup = createPopupForDialog();
			var html = decoratePopupTitle(title);
			html += xhr.responseText;

			addThenCenterPopup(popup, html);
		} else {
			showPopupForError(title, uiText.errorCouldNotGetPage());
		}
	});

	xhr.setRequestHeader("X-no-header", "true");
	xhr.setRequestHeader("X-no-title", "true");
	xhr.send();
	aRequestIsInProgress(true);
}

/** Close the pane in which the event originated. */
function closePane(ev) {
	var eventEl = getEventEl(ev);
	var paneEl = getContainingPaneEl(eventEl);
	closePaneByEl(paneEl);

	createWelcomePaneIfNeeded();
}

/** Creates the welcome pane if needed. */
function createWelcomePaneIfNeeded() {
	var allPanesEl = getAllPanesEl();
	if (allPanesEl.childNodes.length < 2) {
		createWelcomePane();
	}
}

/** Closes the pane. */
function closePaneByEl(paneEl) {
	if (paneEl.id) {
		stopBackgroundTransitionById(paneEl.id);
	}

	onPaneUnload(paneEl);

	removePaneInfo(paneEl);

	var toRemove = paneEl.parentNode.parentNode;
	toRemove.parentNode.removeChild(toRemove);
}

/** Closes all open panes. */
function closeAllPanes(skipCreatingWelcomePane) {
	var paneEls = [];
	var allPanes = getAllPanesEl();
	for (var i = 1; i < allPanes.childNodes.length; ++i) {
		var paneContainer = allPanes.childNodes[i].childNodes[0];
		var paneEl = getPaneElFromPaneContainer(paneContainer);
		paneEls.push(paneEl);
	}

	for (i = 0; i < paneEls.length; ++i) {
		closePaneByEl(paneEls[i]);
	}

	if (!skipCreatingWelcomePane) {
		createWelcomePane();
	}
}

/** Closes the pane with the id "welcome". */
function closeWelcomePane() {
	var welcomePaneEl = document.getElementById("welcome");
	if (welcomePaneEl) {
		closePaneByEl(welcomePaneEl);
	}
}

/** Creates the pane with the id "welcome". */
function createWelcomePane() {
	makePane("/", uiText.pageTitleWelcome(), null, "welcome");
	history.pushState({}, "", "/");
	document.title = uiText.pageTitleWelcome();
}

var urls = [];
var titles = [];
var paneTypes = [];
var editablePanes = [];
var treePanes = [];
var paneTouchInfoTexts = [];

/** Returns the last loaded GET URL for a pane. */
function getUrlForPane(paneEl) {
	initializeInfoForPanes();
	var index = getIndexOfPaneEl(paneEl);
	return urls[index];
}

/** Returns the last set title for a pane. */
function getTitleForPane(paneEl) {
	initializeInfoForPanes();
	var index = getIndexOfPaneEl(paneEl);
	return titles[index];
}

/** Returns the panetype for a pane. */
function getPaneType(index) {
	initializeInfoForPanes();
	return paneTypes[index];
}

/** Returns the index of pane from the left excluding the menu pane. */
function getIndexOfPaneEl(paneEl) {
	// Ignore the menu pane..
	var index = -1;
	var paneContainerEl = paneEl.parentNode.parentNode;
	while ((paneContainerEl = paneContainerEl.previousSibling)) {
		++index;
	}

	return index;
}

/** Initializes the url and title arrays if needed. */
function initializeInfoForPanes(forceReinitialize) {
	if (urls.length === 0 || forceReinitialize) {
		var arr = splitUris(window.location.pathname);
		if (arr && arr.length > 1) {
			urls = arr;
		} else {
			urls[0] = window.location.pathname + window.location.search;
		}
	}

	if (titles.length === 0 || forceReinitialize) {
		titles = [];
		paneTypes = [];
		var allPanes = getAllPanesEl();
		for (var i = 1; i < allPanes.childNodes.length; ++i) {
			var paneContainer = allPanes.childNodes[i].childNodes[0];
			var paneEl = getPaneElFromPaneContainer(paneContainer);
			updateMetaDataForPane(paneEl, i - 1);
		}
	}
}

/** Update meta data for the pane such as title and paneType. */
function updateMetaDataForPane(paneEl, paneIndex) {
	var metaDataDictJson = getMetaDataDictFromPaneEl(paneEl);

	var metaDataDict;
	try {
		metaDataDict = JSON.parse(metaDataDictJson.innerHTML);
	} catch (e) {
		metaDataDict = {};
	}

	if ("title" in metaDataDict) {
		titles[paneIndex] = metaDataDict.title;
	} else if (titles.length <= paneIndex) {
		titles[paneIndex] = "";
	}

	if ("paneType" in metaDataDict) {
		paneTypes[paneIndex] = metaDataDict.paneType;
	} else {
		paneTypes[paneIndex] = "";
	}

	if ("notEditable" in metaDataDict) {
		editablePanes[paneIndex] = !metaDataDict.notEditable;
	} else {
		editablePanes[paneIndex] = true;
	}

	if ("tree" in metaDataDict) {
		treePanes[paneIndex] = metaDataDict.tree;
	} else {
		treePanes[paneIndex] = false;
	}

	if ("touchInfoText" in metaDataDict) {
		paneTouchInfoTexts[paneIndex] = metaDataDict.touchInfoText;
	} else {
		paneTouchInfoTexts[paneIndex] = "";
	}
}

/** Adds or updates a pane in the URL and title arrays. */
function updatePaneUrlAndTitle(paneEl, url, title) {
	initializeInfoForPanes();

	if (!url || url.indexOf("/", 1) === -1) {
		return null;
	}

	var index = getIndexOfPaneEl(paneEl);

	if (url !== urls[index]) {
		urls[index] = url;
		updateCurrentLocation();
	}

	if (title) {
		titles[index] = title;
		updateCurrentTitle();
	}
}

/** Update the title for a pane. */
function updatePaneTitle(index, title) {
	initializeInfoForPanes();

	if (title && title !== titles[index]) {
		titles[index] = title;
		updateCurrentTitle();
	}
}

/** Returns true if the pane is editable. */
function isPaneEditable(index) {
	initializeInfoForPanes();

	if (index < editablePanes.length) {
		return editablePanes[index];
	}

	return false;
}

/** Returns true if the pane contains a tree. */
function isPaneATree(index) {
	initializeInfoForPanes();

	if (index < treePanes.length) {
		return treePanes[index];
	}

	return false;
}

/** Returns the touch info text for the pane. */
function getPaneTouchInfoText(index) {
	initializeInfoForPanes();

	if (index < paneTouchInfoTexts.length) {
		return paneTouchInfoTexts[index];
	}

	return "";
}

/** Marks whether the pane is editable. */
function setPaneIsEditable(index, value) {
	editablePanes[index] = value;
}

/** Move the pane from the old index to the new index. */
function movePaneFromIndexToIndex(oldIndex, newIndex) {
	initializeInfoForPanes();

	var url = urls[oldIndex];
	var title = titles[oldIndex];
	var paneType = paneTypes[oldIndex];
	var editablePane = editablePanes[oldIndex];
	var treePane = treePanes[oldIndex];
	var paneTouchInfoText = paneTouchInfoTexts[oldIndex];
		
	urls.splice(oldIndex, 1);
	titles.splice(oldIndex, 1);
	paneTypes.splice(oldIndex, 1);
	editablePanes.splice(oldIndex, 1);
	treePanes.splice(oldIndex, 1);
	paneTouchInfoTexts.splice(oldIndex, 1);

	urls.splice(newIndex, 0, url);
	titles.splice(newIndex, 0, title);
	paneTypes.splice(newIndex, 0, paneType);
	editablePanes.splice(newIndex, 0, editablePane);
	treePanes.splice(newIndex, 0, treePane);
	paneTouchInfoTexts.splice(newIndex, 0, paneTouchInfoText);

	var allPanes = getAllPanesEl();
	var insertAfter = allPanes.childNodes[newIndex + 1];
	var removedPaneContainer = allPanes.removeChild(allPanes.childNodes[oldIndex + 1]);
	allPanes.insertBefore(removedPaneContainer, insertAfter);
		
	updateCurrentLocation();
	updateCurrentTitle();
}

/** Removes a pane from the URL and title arrays. */
function removePaneInfo(paneEl) {
	initializeInfoForPanes();

	var index = getIndexOfPaneEl(paneEl);

	urls.splice(index, 1);
	titles.splice(index, 1);
	paneTypes.splice(index, 1);
	editablePanes.splice(index, 1);
	treePanes.splice(index, 1);
	paneTouchInfoTexts.splice(index, 1);

	updateCurrentLocation();
	updateCurrentTitle();
}

/** Updates the document's location. */
function updateCurrentLocation() {
	var currentLocation = "";
	for (var i = 0; i < urls.length; ++i) {
		currentLocation += urls[i];
	}

	history.pushState({}, "", currentLocation);
}

/** Updates the document's title. */
function updateCurrentTitle() {
	var currentTitle = "";
	for (var i = 0; i < titles.length; ++i) {
		if (!titles[i]) {
			continue;
		}

		if (currentTitle !== "") {
			currentTitle += " | ";
		}

		currentTitle += titles[i];
	}

	if (!document.getElementById("welcome")) {
		currentTitle += " - CrushPaper";
	}

	document.title = currentTitle;
}

/** Splits the URI into individual URIs for panes. */
function splitUris(uri) {
	var uris = [];
	var start = 0;
	while (true) {
		if (uri[start] !== '/') {
			uris = null;
			break;
		}

		var secondSlash = uri.indexOf('/', start + 1);
		if (secondSlash === -1) {
			uris = null;
			break;
		}

		var isDone = false;
		var thirdSlash = uri.indexOf('/', secondSlash + 1);
		if (thirdSlash === -1) {
			thirdSlash = uri.length;
			isDone = true;
		}

		uris.push(uri.substring(start, thirdSlash));

		if (isDone) {
			break;
		}

		start = thirdSlash;
	}

	return uris;
}

// Globals for pane resizing.
var resizedPaneLastMousePos = null;
var resizedPaneSection = null;
var resizedPaneEw = false;
var resizedPaneNs = false;

/** Handles resizing a pane. */
function paneResizeOnMouseMove(ev) {
	if (resizedPaneSection) {
		var mousePos = getMousePosition(ev);
		
		if (resizedPaneEw) {
			var paneWidth = parseInt(resizedPaneSection.parentNode.offsetWidth);
			resizedPaneSection.parentNode.style.width = paneWidth - (resizedPaneLastMousePos.x - mousePos.x) + "px";
		}
		
		if (resizedPaneNs) {
			var paneHeight = parseInt(resizedPaneSection.offsetHeight);
			resizedPaneSection.style.height = paneHeight - (resizedPaneLastMousePos.y - mousePos.y) - 20 + "px";
		}
		
		resizedPaneLastMousePos = mousePos;
		return false;
	}
}

/** Handles mouseup for a pane. */
function paneResizeOnMouseUp() {
	if (!resizedPaneSection) {
		return;
	}

	finishResizingPane();
}

/** Helper function for finishing a pane resize. */
function finishResizingPane() {
	document.onmouseup = null;
	resizedPaneSection = null;
	document.onmousemove = null;
}

/** Handles mousedown for resizing a pane. */
function paneResizeOnMouseDown(ev) {
	var eventEl = getEventEl(ev);
	if (isElementOfClass(eventEl, "dragEwPane")) {
		resizedPaneEw = true;
		resizedPaneNs = false;
	} else if (isElementOfClass(eventEl, "dragDiagPane")) {
		resizedPaneEw = true;
		resizedPaneNs = true;
	} else if (isElementOfClass(eventEl, "dragNsPane")) {
		resizedPaneEw = false;
		resizedPaneNs = true;
	}
	
	resizedPaneSection = getElOrAncestor(eventEl, 'DIV', 'paneSection');
	document.onmouseup = paneResizeOnMouseUp;
	resizedPaneLastMousePos = getMousePosition(ev);
	document.onmousemove = paneResizeOnMouseMove;
	ev.preventDefault();
	ev.stopPropagation();
	return false;
}

// Globals for pane moving.
var movedPaneMouseOffset = null;
var movedPane = null;
var movedPaneClone = null;
var movedPaneMouseOffset = null;
var movedPaneDivider = null;
var dropPaneBeforeIndex = -1;

/** Handles moving a pane. */
function paneMoveOnMouseMove(ev) {
	if (movedPane) {
		var mousePos = getMousePosition(ev);
		movedPaneClone.style.top = (mousePos.y - movedPaneMouseOffset.y) + "px";
		movedPaneClone.style.left = (mousePos.x - movedPaneMouseOffset.x) + "px";
		
		var allPanes = getAllPanesEl();
		var previousPaneRightX = -100;
		var paneBelongsBeforeIndex = -1;
		var placementX = -1;
		for (var i = 1; i < allPanes.childNodes.length; ++i) {
			var paneContainer = allPanes.childNodes[i].childNodes[0];
			var paneEl = getPaneElFromPaneContainer(paneContainer);
			var panePosition = getPosition(paneEl);
			var paneWidth = parseInt(paneEl.offsetWidth);
			if (mousePos.x >= previousPaneRightX && mousePos.x <= panePosition.x) {
				paneBelongsBeforeIndex = i - 1;
				placementX = panePosition.x - 11;
				break;
			}
			
			previousPaneRightX = panePosition.x + paneWidth;
		}
		
		if (paneBelongsBeforeIndex === -1) {
			if (mousePos.x >= previousPaneRightX) {
				paneBelongsBeforeIndex = allPanes.childNodes.length - 1;
				placementX = previousPaneRightX + 3;
			}
		}
		
		dropPaneBeforeIndex = -1;
	    if (paneBelongsBeforeIndex === -1) {
	    	if(movedPaneDivider) {
	    		movedPaneDivider.style.display = "none";
	    	}
	    } else {
			if (!movedPaneDivider) {
				movedPaneDivider = document.createElement('DIV');
				movedPaneDivider.className = "movePaneDivider";
				document.body.appendChild(movedPaneDivider);
			}
			
			var paneIndex = getIndexOfPaneEl(movedPane);
			var color = "red";
			if (paneBelongsBeforeIndex !== paneIndex &&
					paneBelongsBeforeIndex !== paneIndex + 1) {
				color = "green";
				dropPaneBeforeIndex = paneBelongsBeforeIndex; 
			}

			movedPaneDivider.style["border-left"] = "dashed 3px " + color;
			movedPaneDivider.style.top = "0px";
			movedPaneDivider.style.left = placementX + "px";
			movedPaneDivider.style.height = getWindowInnerHeight() + "px";
			movedPaneDivider.style.display = "";
	    }
	    
		return false;
	}
}

/** Handles mouseup for a pane. */
function paneMoveOnMouseUp() {
	if (!movedPane) {
		return;
	}

	finishMovingPane();
}

/** Helper function for finishing a pane move. */
function finishMovingPane() {
	if (dropPaneBeforeIndex !== -1) {
		var paneIndex = getIndexOfPaneEl(movedPane);
		movePaneFromIndexToIndex(paneIndex, dropPaneBeforeIndex);
	}
	
	document.onmouseup = null;
	movedPane = null;
	if (movedPaneClone) {
		movedPaneClone.parentNode.removeChild(movedPaneClone);
		movedPaneClone = null;
	}
	document.onmousemove = null;
    if (movedPaneDivider) {
    	movedPaneDivider.style.display = "none";
    }
}

/** Handles mousedown for moving a pane. */
function paneMoveOnMouseDown(ev) {
	var eventEl = getEventEl(ev);
	if ((!isElementOfClass(eventEl, "headerpane") && !isElementOfClass(eventEl, "paneButtons")) ||
			(("id" in eventEl.parentNode) && eventEl.parentNode.id === "welcome")) {
		return true;
	}
	
	movedPane = getElOrAncestor(eventEl, 'DIV', 'pane');
	document.onmouseup = paneMoveOnMouseUp;
	document.onmousemove = paneMoveOnMouseMove;
	var position = getPosition(movedPane);
	
	movedPaneClone = document.createElement("DIV");
	movedPaneClone.innerHTML = "<br><br><br>" + uiText.dragPaneHint();
	movedPaneClone.style.opacity = 0.7;
	movedPaneClone.style.filter = "alpha(opacity=70)"; // For IE.
	movedPaneClone.style["z-index"] = "100";
	movedPaneClone.style.position = "absolute";
	movedPaneClone.className = "pane paneContainer dragPaneClone";
	document.body.appendChild(movedPaneClone);

	// This would work without the pixel adjustments here if padding was 0px.
	movedPaneClone.style.width = (movedPane.clientWidth) + "px";
	movedPaneClone.style.height = movedPane.clientHeight + "px";
	movedPaneClone.style.top = (getScrollTop() + position.y) + "px";
	movedPaneClone.style.left = (getScrollLeft() + position.x) + "px";
	movedPaneMouseOffset = getMouseOffset(movedPaneClone, ev);
	
	ev.preventDefault();
	ev.stopPropagation();
	return false;
}

/** Disables and element if it exists. */
function toggleEnableElIfExists(elId) {
	var el = document.getElementById(elId);
	if (el) {
		el.disabled = !el.disabled;
	}
}

/** Returns the DB ID from the id. */
function getDbIdFromElId(elId) {
	return elId.substr(6);
}

/** Returns the DB ID of the element. */
function getDbIdFromEl(el) {
	return getDbIdFromElId(el.id);
}

/** Returns the alone element for an entry corresponding to a DB ID. */
function getAloneElByDbId(dbId) {
	return document.getElementById('alone_' + dbId);
}

/** Returns the subtree element for an entry corresponding to the alone element. */
function getSubtreeElForAloneEl(aloneEl) {
	if (!aloneEl) {
		return null;
	}

	return aloneEl.parentNode;
}

/** Returns the subtree element for an entry corresponding to a DB ID. */
function getSubtreeElByDbId(dbId) {
	return getSubtreeElForAloneEl(getAloneElByDbId(dbId));
}

/** Returns the alone element for an entry that contains `el`. */
function getCorrespondingAloneEl(el) {
	var aloneEl = getElOrAncestor(el, 'DIV', 'alone', '.container');
	if (aloneEl && isElementOfClass(aloneEl, "fakealone")) {
		return null;
	}

	return aloneEl;
}

/** Returns true if the entry element is contained by another entry. */
function doesSubtreeElHaveParent(subtreeEl) {
	return subtreeEl.parentNode && isElementOfTagAndClass(subtreeEl.parentNode, 'DIV', 'justchildren');
}

/** Returns the parent entry for an entry. */
function getParentOfSubtreeEl(subtreeEl) {
	return subtreeEl.parentNode.parentNode;
}

/** Removes an entry from its parent. */
function removeSubtreeElFromParent(subtreeEl) {
	subtreeEl.parentNode.removeChild(subtreeEl);
}

/** Adds an entry as the last child of another entry. */
function addLastChildToSubtreeEl(parentSubtreeEl, newChildSubtreeEl) {
	parentSubtreeEl.childNodes[1].appendChild(newChildSubtreeEl);
}

/** Adds an entry as the first child of another entry. */
function addFirstChildToSubtreeEl(parentSubtreeEl, newChildSubtreeEl) {
	parentSubtreeEl.childNodes[1].insertBefore(newChildSubtreeEl,
			parentSubtreeEl.childNodes[1].firstChild);
}

/** Returns all of the siblings of the entry. */
function getSiblingsOfSubtreeEl(subtreeEl) {
	var siblingEls = [];

	for (var i = 0; i < subtreeEl.parentNode.childNodes.length; ++i) {
		var maybeSiblingEl = subtreeEl.parentNode.childNodes[i];
		if (maybeSiblingEl.nodeName === 'DIV' && maybeSiblingEl !== subtreeEl) {
			siblingEls.push(maybeSiblingEl);
		}
	}

	return siblingEls;
}

/** Returns the quotation element for the entry. */
function getQuotationElOfAloneEl(aloneEl) {
	return aloneEl.getElementsByClassName("quotation")[0];
}

/** Returns true if the entry is a quotation. */
function isAloneElAQuotation(aloneEl) {
	return aloneEl.getElementsByClassName("quotation").length !== 0;
}

/** Returns true if the entry is a quotation. */
function isEntryAQuotation(dbId) {
	if (!dbId) {
		return false;
	}

	var aloneEl = getAloneElByDbId(dbId);
	if (!aloneEl) {
		return false;
	}

	return isAloneElAQuotation(aloneEl);
}

/** Returns the note element for the entry. */
function getNoteElOfAloneEl(aloneEl) {
	return aloneEl.getElementsByClassName("note")[0];
}

/** Returns the modtime element for the entry. */
function getModTimeElOfAloneEl(aloneEl) {
	return aloneEl.getElementsByClassName("modTime")[0];
}

/** Returns the entrydaytime element for the entry. */
function getEntryDaytimeElOfAloneEl(aloneEl) {
	return aloneEl.getElementsByClassName("entryDaytime")[0];
}

/** Returns the alone element within the subtree element for an entry. */
function getAloneElFromSubtreeEl(subtreeEl) {
	return subtreeEl.childNodes[0];
}

/** Returns the ID for the subtree. */
function getDbIdFromSubtreeEl(subtreeEl) {
	return getDbIdFromEl(getAloneElFromSubtreeEl(subtreeEl));
}

/** Returns the array of children within the subtree element for an entry. */
function getChildrenOfSubtreeEl(subtreeEl) {
	return subtreeEl.childNodes[1].childNodes;
}

/** Returns the children element within the subtree element for an entry. */
function getChildrenElOfSubtreeEl(subtreeEl) {
	return subtreeEl.childNodes[1];
}

/** Returns true if the entry has children. */
function doesSubtreeElHaveChildrenDisplayed(subtreeEl) {
	return subtreeEl.childNodes.length > 0 &&
	subtreeEl.childNodes[1].childNodes.length > 0;
}

/** Inserts the entry before a sibling. */
function insertSubtreeBeforeSibling(insertedSubtreeEl, referenceSubtreeEl,
		parentOverride) {
	var parent = parentOverride ? parentOverride.childNodes[1]
	: referenceSubtreeEl.parentNode;
	parent.insertBefore(insertedSubtreeEl, referenceSubtreeEl);
}

/** Inserts the entry after a sibling. */
function insertSubtreeAfterSibling(insertedSubtreeEl, referenceSubtreeEl,
		parentOverride) {
	var parent = parentOverride ? parentOverride.childNodes[1]
	: referenceSubtreeEl.parentNode;
	parent.insertBefore(insertedSubtreeEl, referenceSubtreeEl.nextSibling);
}

/** Returns true if the entry has a previous sibling. */
function doesSubtreeElHavePrevious(subtreeEl) {
	return !!subtreeEl.previousSibling;
}

/** Returns true if the entry has a next sibling. */
function doesSubtreeElHaveNext(subtreeEl) {
	return !!subtreeEl.nextSibling;
}

/** Returns the entry's previous sibling. Don't call this if the entry has no previous sibling. */
function getPreviousOfSubtreeEl(subtreeEl) {
	return subtreeEl.previousSibling;
}

/** Returns the entry's next sibling. Don't call this if the entry has no next sibling. */
function getNextOfSubtreeEl(subtreeEl) {
	return subtreeEl.nextSibling;
}

/** Sets up the window mousing clicking and hovering. */
function setUpWindowForMousing() {
	if (!("allSelectedDbIds" in window)) {
		window.allSelectedDbIds = {};
	}

	if (!("allHoveredDbIds" in window)) {
		window.allHoveredDbIds = {};
	}
}

/** Returns the fake just children in the container. */
function getFakeJustChildrenFromContainer(container) {
	if (container.childNodes.length === 2) {
		return container.childNodes[1];
	}

	return null;
}

/** If the container has any entries then this function selects the root entry and returns its db id. */
function getAndSelectTheContainersRoot(onlyFromTree) {
	var container = getFirstContainer(onlyFromTree);
	if (container) {
		var fakeJustChildrenEl = getFakeJustChildrenFromContainer(container);
		if (fakeJustChildrenEl && fakeJustChildrenEl.childNodes.length) {
			var subtreeEl = fakeJustChildrenEl.childNodes[0];
			var aloneEl = getAloneElFromSubtreeEl(subtreeEl);
			var dbId = getDbIdFromEl(aloneEl);
			selectAloneEl(aloneEl);
			return dbId;
		}
	}

	return null;
}

var lastSelectedDbId = null;

/** Returns the DB ID of the last selected entry if any. */
function getSelectedDbId(doNotAutoselect, onlyFromTree) {
	setUpWindowForMousing();

	if (lastSelectedDbId) {
		return lastSelectedDbId;
	}

	for (var dbId in window.allSelectedDbIds) {
		return dbId;
	}

	if (doNotAutoselect) {
		return null;
	}

	return getAndSelectTheContainersRoot(onlyFromTree);
}

/** Returns the DB IDs of all selected entries. */
function getSelectedDbIds(doNotAutoselect, onlyFromTree) {
	setUpWindowForMousing();

	var results = [];
	for (var dbId in window.allSelectedDbIds) {
		results.push(dbId);
	}

	if (!doNotAutoselect && results.length === 0) {
		var rootDbId = getAndSelectTheContainersRoot(onlyFromTree);
		if (rootDbId) {
			results.push(rootDbId);
		}
	}

	return results;
}

/** Returns the number of selected entries. */
function getNumSelected(doNotAutoselect, onlyFromTree) {
	setUpWindowForMousing();
	var numSelected = Object.keys(window.allSelectedDbIds).length;
	if (numSelected !== 0) {
		return numSelected;
	}

	if (!doNotAutoselect) {
		var rootDbId = getAndSelectTheContainersRoot(onlyFromTree);
		if (rootDbId) {
			return 1;
		}
	}

	return 0;
}

/** Returns true if any of the selected DB IDs are from a list rather than a tree. */
function areAnySelectedFromAList() {
	setUpWindowForMousing();

	for (var dbId in window.allSelectedDbIds) {
		if (isListDbId(dbId)) {
			return true;
		}
	}

	return false;
}

/** Handle when the user hits the escape key when there is no popup. */
function escapeHandler() {
	unselectAllEntries();
	
	noteEscapeHandler();
	
	return false;
}

/** Unselects all entries, modifying the data structures and the DOM. */
function unselectAllEntries() {
	setUpWindowForMousing();

	lastSelectedDbId = null;

	for (var dbId in window.allSelectedDbIds) {
		delete window.allSelectedDbIds[dbId];

		updateSelectionDisplayForAloneEl(getAloneElByDbId(dbId));
	}

	return false;
}

/** Updates the DOM for an entry to indicate if it is selected or hovered. */
function updateSelectionDisplayForAloneEl(aloneEl) {
	var dbId = getDbIdFromEl(aloneEl);

	setUpWindowForMousing();

	var trueDbId = getTrueDbIdFromListDbId(dbId);

	var selected = false;
	if (dbId in window.allSelectedDbIds) {
		aloneEl.className = "selected-alone alone " + trueDbId;
		selected = true;
	} else if (dbId in window.allHoveredDbIds) {
		aloneEl.className = "hover-alone alone " + trueDbId;
	} else {
		aloneEl.className = "alone " + trueDbId;
	}
	
	var checkboxes = aloneEl.getElementsByClassName("aloneCheckbox");
	if (checkboxes.length > 0 && checkboxes[0].checked !== selected) {
		checkboxes[0].checked = selected;
	}
}

/** Unselects and unhovers the entry, modifying the data structures and but not the DOM. */
function removeAloneElFromSelections(aloneEl) {
	setUpWindowForMousing();

	var dbId = getDbIdFromEl(aloneEl);

	if (dbId in window.allSelectedDbIds) {
		delete window.allSelectedDbIds[dbId];
	}

	if (dbId in window.allHoveredDbIds) {
		delete window.allHoveredDbIds[dbId];
	}

	if (lastSelectedDbId === dbId) {
		lastSelectedDbId = null;
	}
}

/** Updates data structures to mark the entry as selected. */
function selectEntry(dbId) {
	setUpWindowForMousing();

	window.allSelectedDbIds[dbId] = true;

	lastSelectedDbId = dbId;
}

/** Updates data structures to mark the entry NOT selected. */
function unselectEntry(dbId) {
	setUpWindowForMousing();

	if (dbId in window.allSelectedDbIds) {
		delete window.allSelectedDbIds[dbId];
	}

	if (lastSelectedDbId === dbId) {
		lastSelectedDbId = null;
	}
}

/** Updates data structures to mark the entry as hovered. */
function hoverEntry(dbId) {
	setUpWindowForMousing();

	window.allHoveredDbIds[dbId] = true;
}

/** Returns true if the entry is hovered. */
function isEntryHovered(dbId) {
	setUpWindowForMousing();

	return (dbId in window.allHoveredDbIds);
}

/** Updates data structures to mark the entry as not hovered. */
function unhoverEntry(dbId) {
	setUpWindowForMousing();

	if (dbId in window.allHoveredDbIds) {
		delete window.allHoveredDbIds[dbId];
	}
}

/** Returns the hovered dbids. */
function getHoveredDbIds() {
	setUpWindowForMousing();

	var results = [];
	for (var dbId in window.allHoveredDbIds) {
		results.push(dbId);
	}

	return results;
}

/** Returns the index of the child that has the specified dbId or -1. */
function getIndexOfChildById(children, dbId) {
	for (var i = 0; i < children.length; ++i) {
		if (getDbIdFromSubtreeEl(children[i]) === dbId) {
			return i;
		}
	}

	return -1;
}

// Globals for entry drag and drop.
var aloneElBeingDragged = null;
var draggedEntryClone = null;
var draggedEntryButtonEl = null;
var draggedEntryMouseOffset = null;
var lastEntryDropTarget = null;
var lastEntryDropTargetAcceptabilityDesc = null;
var acceptabilityDescEl = null;
var entryDropSiblingDividerEl = null;
var entryDropBorderEl = null;
var justTheEntry = null;

/** Returns the true dbid, the parts after the ":". */
function getTrueDbIdFromListDbId(listDbId) {
	var colonPos = listDbId.indexOf(":");
	if (colonPos !== -1) {
		return listDbId.substr(colonPos + 1);
	}

	return listDbId;
}

/** Returns true if the dbid is from a list, i.e contains ":". */
function isListDbId(dbId) {
	return dbId.indexOf(":") !== -1;
}

var isCommandKeyPressed = false;

/** Handles touch events. */
function documentOnTouchEnd(ev) {
	unselectAllEntries();
	documentOnMouseOver(ev);
	documentOnMouseDown(ev, true);
}

/** Handles mousedown events for the container of entries and the whole page. */
function documentOnMouseDown(ev, fromTouch) {
	stopScrollTransition();

	if (!areCommandsAllowed) {
		return;
	}

	if (isCommandKeyPressed) {
		onContextMenu(ev);
		return;
	}

	var eventEl = getEventEl(ev);

	var clickedAloneEl = getCorrespondingAloneEl(eventEl);
	if (!clickedAloneEl) {
		unselectAllEntries();
		blurNote();
		return;
	}

	var noteEl = getElOrAncestor(eventEl, 'DIV', 'note', '.alone');
	if (!noteEl && !isElementOfClass(eventEl, "content") && eventEl.nodeName !== "TD") {
		blurNote();
	}

	var clickedDbId = getDbIdFromEl(clickedAloneEl);
	
	// Make sure the click handler for clickable things is called.
	if (isElementOfClass(eventEl, "justDrag")) {
		handleSelectionMouseDown(clickedAloneEl, clickedDbId, ev);
		return;
	}

	if (isElementOfClass(eventEl, "noDrag")) {
		return;
	}

	var paneEl = getContainingPaneEl(clickedAloneEl);
	var paneIndex = getIndexOfPaneEl(paneEl);
	if (!isPaneEditable(paneIndex)) {
		var paneType = getPaneType(paneIndex);
		var dbId = getTrueDbIdFromListDbId(getDbIdFromEl(clickedAloneEl));
		if (paneType === "notebooks") {
			makePane("/notebook/" + dbId, getEntryNoteText(dbId), null, dbId);
		} else if (paneType === "accounts") {
			makePane("/account/" + dbId, getEntryNoteText(dbId), null, "account");
		}

		unselectAllEntries();
		return;
	}

	if (ev.ctrlKey && ev.shiftKey) {
		return;
	}

	// Make sure links still work.
	if (eventEl.nodeName === "A") {
		return;
	}

	var shouldReturn = handleSelectionMouseDown(clickedAloneEl, clickedDbId, ev);
	
	var isNote = getElOrAncestor(eventEl, 'DIV', 'note', '.alone');
	
	if (!fromTouch) {
		var lastSelectedDbId = getSelectedDbId(true);
		if (lastSelectedDbId) {
			focusNoteAndSetCaret(getAloneElByDbId(lastSelectedDbId), !isNote && isFireFox);
		}
	}
	
	if (shouldReturn) {
		return;
	}

	var textIsSelectable = isNote ||
	getElOrAncestor(eventEl, 'DIV', 'quotation', '.alone');

	// Allow the user to select text.
	if (textIsSelectable) {
		return true;
	}
	
	if (fromTouch) {
		// Without this buttons on the hover menu may be pressed unintentionally from touch.
		ev.preventDefault();
		ev.stopPropagation();
	}
}

/** Handles clicking the alone el checkboxes. */
function checkboxOnClick(ev) {
	var eventEl = getEventEl(ev);
	var clickedAloneEl = getCorrespondingAloneEl(eventEl);
	if (!clickedAloneEl) {
		return;
	}
	
	if (eventEl.checked === true) {
		// Select the entry.
		if (ev.shiftKey) {
			aloneElOnShiftClick(clickedAloneEl, getDbIdFromEl(clickedAloneEl));
		} else {
			selectAloneEl(clickedAloneEl, false, false);
		}
	} else {
		// Unselect the entry.
		selectAloneEl(clickedAloneEl, false, true);
	}

	eventEl.blur();
}

/** Handles selections. Returns true if mousedown processing should be stopped. */
function handleSelectionMouseDown(clickedAloneEl, clickedDbId, ev) {
	var eventEl = getEventEl(ev);
	if (eventEl.nodeName === "INPUT") {
		return true;
	}
	
	if (ev.shiftKey) {
		aloneElOnShiftClick(clickedAloneEl, clickedDbId);
		return true;
	}

	var unselectOthers = !ev.ctrlKey && !ev.altKey;
	var toggleSelection = ev.ctrlKey || ev.altKey;
	selectAloneEl(clickedAloneEl, unselectOthers, toggleSelection);

	return false;
}

/** Handle shift click of alone els. */
function aloneElOnShiftClick(clickedAloneEl, clickedDbId) {
	var clickedSubtreeEl = getSubtreeElForAloneEl(clickedAloneEl);
	if (doesSubtreeElHaveParent(clickedSubtreeEl)) {
		var parentClickedSubtreeEl = getParentOfSubtreeEl(clickedSubtreeEl);

		var startFromTheFirstChildOfTheClickedParent = true;
		var lastSelectedDbId = getSelectedDbId(true);
		if (lastSelectedDbId) {
			var lastSelectedSubtreeEl = getSubtreeElByDbId(lastSelectedDbId);

			if (doesSubtreeElHaveParent(lastSelectedSubtreeEl)) {
				var parentLastSelectedSubtreeEl = getParentOfSubtreeEl(lastSelectedSubtreeEl);

				if (getDbIdFromSubtreeEl(parentLastSelectedSubtreeEl) === getDbIdFromSubtreeEl(parentClickedSubtreeEl)) {
					startFromTheFirstChildOfTheClickedParent = false;
				}
			}
		}

		var childrenOfClickedParent = copyArray(getChildrenOfSubtreeEl(parentClickedSubtreeEl));

		if (startFromTheFirstChildOfTheClickedParent) {
			lastSelectedDbId = getDbIdFromSubtreeEl(childrenOfClickedParent[0]);
		}

		var indexOfLastSelected = getIndexOfChildById(childrenOfClickedParent, lastSelectedDbId);
		var indexOfLastClicked = getIndexOfChildById(childrenOfClickedParent, clickedDbId);
		var minIndex = Math.min(indexOfLastSelected, indexOfLastClicked);
		var maxIndex = Math.max(indexOfLastSelected, indexOfLastClicked);
		for (var i = minIndex; i <= maxIndex; ++i) {
			selectAloneEl(getAloneElFromSubtreeEl(childrenOfClickedParent[i]));
		}

		// Make sure this is the last selected in case the user shift clicks again.
		selectAloneEl(clickedAloneEl);
	}
}

var padDraggedEntryInterval = null;

/** Starts the drag of the alone el. */
function startAloneDrag(ev, justTheEntryArg) {
	justTheEntry = justTheEntryArg;
	var eventEl = getEventEl(ev);

	var clickedAloneEl = getCorrespondingAloneEl(eventEl);
	if (!clickedAloneEl) {
		return;
	}
	
	selectAloneEl(clickedAloneEl, !ev.ctrlKey && !ev.shiftKey);

	var draggedAloneEl = clickedAloneEl;
	var clickedDbId = getDbIdFromEl(clickedAloneEl);
	var clickedIsSelected = isDbIdSelected(clickedDbId);
	if (!clickedIsSelected) {
		var selectedDbId = getSelectedDbId(true);
		if (!selectedDbId) {
			return;
		}
		
		draggedAloneEl = getAloneElByDbId(selectedDbId);
	}
	
	commandsAreNowAllowed(false);

	// Support dragging.
	aloneElBeingDragged = draggedAloneEl;
	draggedEntryClone = draggedAloneEl.cloneNode(true);
	var buttonClass = justTheEntry ? "dragButton" : "dragChildrenButton";
	draggedEntryButtonEl = draggedEntryClone.getElementsByClassName(buttonClass)[0];
	var position = getPosition(clickedAloneEl);

	document.onmouseup = dragEntryOnMouseUp;
	document.onmousemove = dragEntryOnMouseMove;
	padDraggedEntryInterval = window.setInterval(scrollToPadDraggedEntry, 50);
	draggedEntryClone.style.opacity = 0.4;
	draggedEntryClone.style.filter = "alpha(opacity=40)"; // For IE.
	draggedEntryClone.style["z-index"] = "100";
	draggedEntryClone.style.position = "absolute";
	draggedEntryClone.style["box-shadow"] = "3px 3px 13px rgba(0,0,0,0.55)";
	draggedEntryClone.className = draggedEntryClone.className + " clone";
	document.body.appendChild(draggedEntryClone);

	// This would work without the pixel adjustments here if padding was 0px.
	draggedEntryClone.style.width = (draggedAloneEl.clientWidth - 4) + "px";
	draggedEntryClone.style.height = draggedAloneEl.clientHeight + "px";
	draggedEntryClone.style.top = (getScrollTop() + position.y - 1) + "px";
	draggedEntryClone.style.left = (getScrollLeft() + position.x - 1) + "px";
	draggedEntryMouseOffset = getMouseOffset(draggedEntryClone, ev);

	// Required for FireFox, Opera, Safari and IE because they have default logic for dragging images.
	// Also Firefox will think text that is dragged over should be selected.
	ev.preventDefault();
	ev.stopPropagation();

	Mousetrap.bind("esc", function() {
		// Do not pass the event since it has no mouse position.
		dragEntryOnMouseUp();
	});

	// So that the global for position is initialized.
	dragEntryOnMouseMove(ev);
	
	return true;
}

/** Converts the element to text for logging purposes. */
function elementToText(el, limit) {
	if (el === null) {
		return "null";
	}

	var text = "";

	var i = 0;
	while (el) {
		if (text !== "") {
			text += "; ";
		}

		text += el.nodeName;

		if (("id" in el) && el.id !== "") {
			text += " #" + el.id;
		}

		if (("className" in el) && el.className !== "") {
			text += " ." + el.className;
		}

		var offset = 0;
		var temp = el;
		while (temp.previousSibling) {
			temp = temp.previousSibling;
			++offset;
		}
		
		if (el.parentNode) {
			text += " (" + offset + "/" + el.parentNode.childNodes.length + ")";
		}
		
		++i;
		if (i === limit) {
			break;
		}
		
		el = el.parentNode;
	}

	return text;
}

var aloneHoverMenuEl = null;

/** Handles mouseover events for the container of entries. */
function documentOnMouseOver(ev) {
	if (!areCommandsAllowed) {
		return;
	}

	var eventEl = getEventEl(ev);

	var aloneEl = getCorrespondingAloneEl(eventEl);
	if (!aloneEl) {
		unhoverAllEntries();
		return;
	}

	var dbId = getDbIdFromEl(aloneEl);

	unhoverAllEntries(dbId);

	if (!isElementOfClass(aloneEl, "clone") && !isEntryHovered(dbId)) {
		hoverEntry(dbId);

		updateSelectionDisplayForAloneEl(aloneEl);

		var paneEl = getContainingPaneEl(aloneEl);
		var paneIndex = getIndexOfPaneEl(paneEl);
		var isEditable = isPaneEditable(paneIndex);
		if (isEditable) {
			var entryType = getEntryType(dbId);
			aloneHoverMenuEl = document.createElement('DIV');
			aloneHoverMenuEl.className = "hoverMenu";

			if (!isDisplaySmall()) {
				aloneHoverMenuEl.innerHTML += "<button class=\"noDrag dragChildrenButton\" onmousedown=\"startAloneDrag(event, false);\" title=\"" +
				uiText.tooltipDragWithChildren(entryType) + "\"><img class=\"noDrag\" src=\"/images/dragmult.png\"></button>";
				
				aloneHoverMenuEl.innerHTML += "<button class=\"noDrag dragButton\" onmousedown=\"startAloneDrag(event, true);\" title=\"" +
				uiText.tooltipDragWithoutChildren(entryType) + "\"><img class=\"noDrag\" src=\"/images/drag.png\"></button>";
			}
			
			var isTree = isPaneATree(paneIndex);
			if (isTree) {
				aloneHoverMenuEl.innerHTML += "<button class=\"noDrag\" onclick=\"onClickCreateSubnote(event);\" title=\"" +
				uiText.tooltipNewSubnote(entryType) + "\"><img class=\"noDrag\" src=\"/images/fatplus.png\"></button>";
			} else if (getEntryHasParent(dbId)) {
				aloneHoverMenuEl.innerHTML += "<button class=\"noDrag\" onclick=\"onClickOpen(event);\" title=\"" +
				uiText.tooltipOpen(entryType) + "\"><img class=\"noDrag\" src=\"/images/notebook.png\"></button>";
			}

			aloneHoverMenuEl.innerHTML += "<button class=\"noDrag\" onclick=\"editOnClick(event);\" title=\"" +
			uiText.tooltipEdit(entryType) + "\"><img class=\"noDrag\" src=\"/images/pencilwhite.png\"></button>" +
			"<button class=\"noDrag redButton\" onclick=\"deleteOnClick(event);\" title=\"" +
			uiText.tooltipDelete(entryType) + "\"><img class=\"noDrag\" src=\"/images/trashwhite.png\"></button>";

			aloneEl.appendChild(aloneHoverMenuEl);
		}
	}
}

/** Unhovers any selected entries and hides the hover menu. */
function unhoverAllEntries(exceptDbId) {
	var dbId;

	// There should only be one of these.
	var hoveredDbIds = getHoveredDbIds();
	for (var i = 0; i < hoveredDbIds.length; ++i) {
		dbId = hoveredDbIds[i];
		if (dbId === exceptDbId) {
			continue;
		}

		var aloneEl = getAloneElByDbId(dbId);

		unhoverEntry(dbId);

		updateSelectionDisplayForAloneEl(aloneEl);
	}

	if (aloneHoverMenuEl) {
		dbId = getDbIdFromEl(aloneHoverMenuEl.parentNode);
		if (dbId !== exceptDbId) {
			aloneHoverMenuEl.parentNode.removeChild(aloneHoverMenuEl);
			aloneHoverMenuEl = null;
		}
	}
}

/** Removes any hover menu that is displayed. */
function removeTheHoverMenu() {
	if (aloneHoverMenuEl) {
		aloneHoverMenuEl.parentNode.removeChild(aloneHoverMenuEl);
		aloneHoverMenuEl = null;
	}
}

/** Returns any selected text in the window. */
function getSelectedText() {
    var text = "";
    
    if (typeof window.getSelection !== "undefined") {
        text = window.getSelection().toString();
    } else if (typeof document.selection !== "undefined" && document.selection.type === "Text") {
        text = document.selection.createRange().text;
    }
    
    return text;
}

/** Adds a context menu/right click menu. */
function onContextMenu(ev) {
	var eventEl = getEventEl(ev);

	// Make sure links still work.
	if (eventEl.nodeName === "A") {
		return;
	}
	
	// Ensure that the browser's default context menu shows up for selected text.
	if (getSelectedText())  {
		return;
	}
	
	var aloneEl = getCorrespondingAloneEl(eventEl);
	if (aloneEl && areCommandsAllowed) {
		var paneEl = getContainingPaneEl(aloneEl);
		var paneIndex = getIndexOfPaneEl(paneEl);
		if (isPaneEditable(paneIndex)) {
			unselectAllEntries();
			selectAloneEl(aloneEl);
			showPopupForContextMenu();
			ev.stopPropagation();
			ev.preventDefault();

			return false;
		}
	}

	return true;
}

/** Shows the context menu if an entry is already selected. */
function handleKeyPressForContextMenu() {
	var title = uiText.popupTitleContextMenu();
	if (getNumSelected(false) < 1) {
		showPopupForError(title, uiText.errorAtLeastNoteMustBeSelected(getDefaultEntryTypes()));
		return false;
	}

	showPopupForContextMenu();

	return false;
}

/** An onpopstate handler that makes the back and forward buttons work even though the document is being rewritten by http calls. */
function handlePopStateGetPage() {
	var xhr = createAsyncRequest("GET", addUrlUniquer(window.location.pathname + window.location.search), function() {
		aRequestIsInProgress(false);

		var showError = true;
		if (xhr.status === 200) {
			var responseText = xhr.responseText;
			unloadAllPanes();

			var bodyHtml = responseText;
			if (bodyHtml.length) {
				document.body.innerHTML = bodyHtml;
				initializeInfoForPanes(true);
				onFinishPageReload();
				updateCurrentTitle();
				showError = false;
			}
		}

		if (showError) {
			var errorText = getErrorTextNotFound(xhr, uiText.errorInvalidResponse());
			showPopupForError(uiText.popupTitleRefreshPage(), errorText);
		}
	});

	xhr.setRequestHeader("X-for-refresh", "true");
	xhr.send();
	aRequestIsInProgress(true);
}

/** Makes the parent of the entry the main entry in the container. */
function makeParentEntryMainOfTheContainer(aloneEl) {
	var dbId = getDbIdFromEl(aloneEl);
	var entryType = getEntryType(dbId);
	var xhr = createAsyncRequest("GET", "/noteParentJson/?id=" + dbId +
			"&" + getAnUrlUniquer(), function() {
		aRequestIsInProgress(false);

		if (xhr.status === 200) {
			var response = validateResponseForMakingParentMainOfContainer(xhr.responseText,
					uiText.errorParentNotFound(entryType), uiText.popupTitleViewParentEntry(entryType));
			if (!response) {
				return;
			}

			var subtreeEl = getSubtreeElForAloneEl(aloneEl);
			var maybeContainer = subtreeEl.parentNode;
			maybeContainer.removeChild(subtreeEl);

			var actuallyContainer = getContainerForEl(maybeContainer);

			var childrenOfContainer = copyArray(actuallyContainer.childNodes);
			for (var i = 0; i < childrenOfContainer.length; ++i) {
				actuallyContainer.removeChild(childrenOfContainer[i]);
			}

			var newSubtreeEl = createEntryEl(response, actuallyContainer,
					"addToContainer", response.type, false);
			var childrenEl = getChildrenOfSubtreeEl(newSubtreeEl);
			if (response.skippedIndex >= childrenEl.length || response.skippedIndex === -1) {
				addLastChildToSubtreeEl(newSubtreeEl, subtreeEl);
			} else {
				insertSubtreeBeforeSibling(subtreeEl, childrenEl[response.skippedIndex]);
			}

			addToEntryInfoDict(response.entryInfoDict);
			fixPlusIcons(newSubtreeEl);
			makeNotesInTreeContentEditable(newSubtreeEl);
			
			selectAndScrollToAloneEl(getAloneElByDbId(response.id));
		} else {
			var errorText = getErrorTextNotFound(xhr, uiText.errorParentNotFound());
			showPopupForError(uiText.popupTitleViewParentEntry(entryType), errorText);
		}
	});

	xhr.send();
	aRequestIsInProgress(true);

	return false;
}

/** Makes the entry the main entry in the container. */
function makeEntryMainOfTheContainer() {
	if (!getFirstContainer(true)) {
		return false;
	}

	var title = uiText.popupTitleViewEntry(getDefaultEntryTypes());

	if (getNumSelected(false, true) > 1) {
		showPopupForError(title, uiText.errorOnlyOneNoteMayBeSelected(getDefaultEntryTypes()));
		return false;
	}

	if (areAnySelectedFromAList()) {
		showPopupForError(title, uiText.errorMaySelectFromList(getDefaultEntryTypes()));
		return false;
	}

	var selectedDbId = getSelectedDbId(false, true);
	if (!selectedDbId) {
		showPopupForError(title, uiText.errorANoteMustBeSelected(getDefaultEntryTypes()));
		return false;
	}

	var aloneEl = getAloneElByDbId(selectedDbId);

	var subtreeEl = getSubtreeElForAloneEl(aloneEl);
	var maybeContainer = subtreeEl.parentNode;
	maybeContainer.removeChild(subtreeEl);

	var actuallyContainer = getContainerForEl(maybeContainer);
	removeAllEntriesFromContainer(actuallyContainer);

	actuallyContainer.appendChild(subtreeEl);

	return false;
}

/** Removes all entries from the container. */
function removeAllEntriesFromContainer(container) {
	handleRemovalOfSubtreeAndChildren(container);
}

/** Returns the container for the element. */
function getContainerForEl(maybeContainer) {
	return getElOrAncestor(maybeContainer, 'DIV', 'container');
}

/** Formats all datetimes in the container. */
function formatDatetimesInContainer(container) {
	for (var i = 0; i < container.childNodes[1].childNodes.length; ++i) {
		var subtreeEl = container.childNodes[1].childNodes[i];
		formatDatetimesInSubtreeAndChildren(subtreeEl);
	}
}

/** Formats all datetimes in subtree recursively. */
function formatDatetimesInSubtreeAndChildren(subtreeEl) {
	var aloneEl = getAloneElFromSubtreeEl(subtreeEl);
	formatDateTimeIfNeeded(aloneEl);

	var children = getChildrenOfSubtreeEl(subtreeEl);
	for (var i = 0; i < children.length; ++i) {
		formatDatetimesInSubtreeAndChildren(children[i]);
	}
}

/** Formats the datetime in the aloneEl if needed. */
function formatDateTimeIfNeeded(aloneEl) {
	if (!isElementOfClass(aloneEl, "alone") || isElementOfClass(aloneEl, "fakealone")) {
		return;
	}

	var previousModTimeEl = getModTimeElOfAloneEl(aloneEl);
	if (previousModTimeEl && previousModTimeEl.childNodes.length > 1) {
		previousModTimeEl.innerHTML = uiText.formatDateTime(previousModTimeEl.childNodes[1].innerHTML * 1);
	}
}

function getChildNodeAtPath() {
	var el = arguments[0];
	for (var i = 1; i < arguments.length; ++i) {
		if (!el || !el.childNodes || el.childNodes.length === 0) {
			return null;
		}

		el = el.childNodes[arguments[i]];
	}

	return el;
}

/** Returns the TD that contains the triangle for the alone el. */
function getTriTdFromAloneEl(aloneEl) {
	return getChildNodeAtPath(aloneEl, 0, 0, 0, 0);
}

/** Returns the IMG that contains the + icon for the alone el. */
function getPlusFromAloneEl(aloneEl) {
	return getChildNodeAtPath(aloneEl, 0, 0, 0, 1, 0, 0, 0, 0, 0);
}

/** Returns the IMG that contains the - icon for the alone el. */
function getMinusFromAloneEl(aloneEl) {
	return getChildNodeAtPath(aloneEl, 0, 0, 0, 1, 0, 0, 1, 0, 0);
}

/** Sets the triangle, plus and minus icons correctly for the aloneEl. */
function setTheRightChildIconsForAloneEl(aloneEl) {
	if (isElementOfClass(aloneEl, "fakealone")) {
		return;
	}

	var dbId = getDbIdFromEl(aloneEl);
	if (isListDbId(dbId)) {
		return;
	}

	var subtreeEl = getSubtreeElForAloneEl(aloneEl);
	var hasChildren = getEntryHasChildren(dbId);
	var hasChildrenDisplayed = doesSubtreeElHaveChildrenDisplayed(subtreeEl);

	var triTdClassName = "triTd justDrag";

	var entryType = getEntryType(dbId);

	var minusVisible = false;
	var triTdTooltip = uiText.tooltipTriNoChildren(entryType);
	if (hasChildren) {
		if (hasChildrenDisplayed) {
			triTdClassName = "triTdDownTri justDrag";
			minusVisible = true;
			triTdTooltip = uiText.tooltipTriHideChildren(entryType);
		} else {
			triTdClassName = "triTdRightTri justDrag";
			triTdTooltip = uiText.tooltipTriShowChildren(entryType);
		}
	}

	var triTd = getTriTdFromAloneEl(aloneEl);
	triTd.className = triTdClassName;
	triTd.setAttribute("title", triTdTooltip);

	if (minusVisible) {
		showMinusIcon(aloneEl);
	} else {
		hideMinusIcon(aloneEl);
	}

	if (!hasChildren) {
		hidePlusIcon(aloneEl);
	} else if (!hasChildrenDisplayed) {
		showPlusIcon(aloneEl);
	} 
}

/** If the dictionary contains an error than show it in the popup and return null, otherwise return it. */
function showDictPopupForError(title, dict) {
	if (!dict) {
		return null;
	}

	if ("error" in dict) {
		showPopupForError(title, dict.error);
		return null;
	}

	return dict;
}

/** Validate that a response for getting the children contains id, entryInfoDict and childrenHtml. */
function validateResponseForGettingChildren(responseText, prefix, title) {
	return showDictPopupForError(title, validateResponseText(responseText, prefix, [ "id", "entryInfoDict", "childrenHtml"]));
}

/** Validate that a response from the server contains id, entryInfoDict subtreeHtml and skippedIndex. */
function validateResponseForMakingParentMainOfContainer(responseText, prefix, title) {
	return showDictPopupForError(title, validateResponseText(responseText, prefix, [ "id", "entryInfoDict", "subtreeHtml", "skippedIndex"]));
}

var editResponseFields = [ "id", "note", "noteHtml", "quotation", "quotationHtml", "modTime", "isPublic", "type" ];

/** Validate that a response from the server contains id, note, noteHtml, quotation, quotationHtml, modTime, isPublic and type. */
function validateEditResponse(responseText) {
	return showErrorInPopup(validateResponseText(responseText, uiText.errorProbablyNotSaved(), editResponseFields));
}

/** Validate that a response from the server contains id, note, noteHtml, quotation, quotationHtml, modTime, isPublic and type. */
function validateInlineEditResponse(responseText, title) {
	return showDictPopupForError(title, validateResponseText(responseText, uiText.errorProbablyNotSaved(), editResponseFields));
}

/** Validate that a response from the server contains id, note, quotation, subtreeHtml, isPublic, and type. */
function validateNoteOpResponse(responseText) {
	return showErrorInPopup(validateResponseText(responseText, uiText.errorProbablyNotSaved(), [ "id", "note", "quotation", "subtreeHtml", "isPublic", "type" ]));
}

/** Validate that a response from the server contains id, note, quotation, subtreeHtml, isPublic, and type. */
function validateDeleteResponse(responseText) {
	var prefix = uiText.errorProbablyNotSaved();
	var key = "deleted";
	var response = showErrorInPopup(validateResponseText(responseText, prefix, [ key ]));
	if (response && response.deleted && response.deleted.constructor !== Array) {
		showErrorInPopup({"error": uiText.errorJsonKeyIsNotArray(prefix, key)});
		return null;
	}

	return response;
}

/** Validate that a response from the server contains id, note, quotation, subtreeHtml, isPublic, type and userWasSignedIn. */
function validateNewNotebookResponse(responseText) {
	return showErrorInPopup(validateResponseText(responseText, uiText.errorProbablyNotSaved(), [ "id", "note", "quotation", "subtreeHtml", "isPublic", "type", "userWasSignedIn" ]));
}

/** Validate that a response from the server is an array. */
function validateArrayResponse(responseText) {
	return validateResponseTextIsArray(responseText, uiText.errorProbablyNotSaved());
}

/** Validate that a response from the server contains id, note, quotation, subtreeHtml, isPublic, and type. */
function validateMakeSiblingOrChildResponse(dict) {
	return validateResponseDict(dict, uiText.errorProbablyNotSaved(), [ "id", "note", "quotation", "subtreeHtml", "isPublic", "type" ]);
}

/** If the dictionary contains an error than show it in the popup and return null, otherwise return it. */
function showErrorInPopup(dict) {
	if (!dict) {
		return null;
	}

	if (typeof dict === 'object' && "error" in dict) {
		setResponseErrorMessage(dict.error);
		return null;
	}

	return dict;
}

/** Validates that a response text from the server is a JSON dict with the needed keys. */
function validateResponseText(responseText, prefix, neededKeys) {
	if (responseText === "") {
		return { "error": uiText.errorEmptyResponseFromServer(prefix, "none") };
	}

	var responseDict;
	try {
		responseDict = JSON.parse(responseText);
	} catch (e) {
		return { "error": uiText.errorJsonCouldNotBeParsed(prefix) };
	}

	return validateResponseDict(responseDict, prefix, neededKeys);
}

/** Validates that a response text from the server is an array. */
function validateResponseTextIsArray(responseText, prefix) {
	if (responseText === "") {
		return { "error": uiText.errorEmptyResponseFromServer(prefix, "none") };
	}

	var responseArray;
	try {
		responseArray = JSON.parse(responseText);
	} catch (e) {
		return { "error": uiText.errorJsonCouldNotBeParsed(prefix) };
	}

	if (responseArray.constructor !== Array) {
		return { "error": uiText.errorJsonIsNotArray(prefix) };
	}

	return responseArray;
}

/** Validates a response dictionary from within a responseText. */
function validateResponseDict(responseDict, prefix, neededKeys) {
	var returnValue = {};

	for (var i = 0; i < neededKeys.length; ++i) {
		var neededKey = neededKeys[i];

		if (!(neededKey in responseDict)) {
			return { "error": uiText.errorJsonMissingKey(prefix, neededKey) };
		}

		returnValue[neededKey] = responseDict[neededKey];
	}

	return returnValue;
}

/** Helper function for creating the elements for an entry. quotationIt defaults to true. */
function createEntryEl(entryData, subtreeEl, operation, entryType, quotationIt) {
	quotationIt = (typeof quotationIt === 'undefined') ? true : quotationIt;

	var tempElement = document.createElement('DIV');
	tempElement.innerHTML = entryData.subtreeHtml;
	var newSubtreeEl = tempElement.childNodes[0];
	newSubtreeEl.parentNode.removeChild(newSubtreeEl);

	if (operation === "insertBefore") {
		insertSubtreeBeforeSibling(newSubtreeEl, subtreeEl);
	} else if (operation === "insertAfter") {
		insertSubtreeBeforeSibling(newSubtreeEl, getNextOfSubtreeEl(subtreeEl),
				getParentOfSubtreeEl(subtreeEl));
	} else if (operation === "addChildLast") {
		addLastChildToSubtreeEl(subtreeEl, newSubtreeEl);
	} else if (operation === "addChildFirst") {
		addFirstChildToSubtreeEl(subtreeEl, newSubtreeEl);
	} else if (operation === "addToContainer") {
		subtreeEl.appendChild(newSubtreeEl);
	}

	if (quotationIt) {
		// Disable this for now.
		// var dbId = getDbIdFromSubtreeEl(newSubtreeEl);
		// This is a little longer than the scroll animation.
		// startBackgroundTransition(dbId, "#E29FE4", 4000);
	}

	formatDatetimesInSubtreeAndChildren(newSubtreeEl);
	showOrHideDatetimesInSubtreeAndChildren(newSubtreeEl);
	fixDivTitlesInSubtreeAndChildren(newSubtreeEl, entryType);
	makeNotesInTreeContentEditable(newSubtreeEl);
	return newSubtreeEl;
}

/** Fixs the div titles in the subtree recursively. */
function fixDivTitlesInSubtreeAndChildren(newSubtreeEl, entryType) {
	var paneEl = getContainingPaneEl(newSubtreeEl);
	var paneIndex = getIndexOfPaneEl(paneEl);
	var isOnlyViewable = !isPaneEditable(paneIndex);

	var i;
	var entryDaytimes = newSubtreeEl.getElementsByClassName("entryDaytime");
	if (entryDaytimes) {
		for (i = 0; i < entryDaytimes.length; ++i) {
			entryDaytimes[i].setAttribute("title", uiText.modTimeTooltip(entryType, !isOnlyViewable));
		}
	}

	var quotations = newSubtreeEl.getElementsByClassName("quotations");
	if (quotations) {
		for (i = 0; i < quotations.length; ++i) {
			quotations[i].setAttribute("title", uiText.quotationTooltip(entryType, !isOnlyViewable));
		}
	}

	var checkboxEls = newSubtreeEl.getElementsByClassName("aloneCheckbox");
	if (checkboxEls) {
		for (i = 0; i < checkboxEls.length; ++i) {
			checkboxEls[i].setAttribute("title", uiText.checkboxTooltip(entryType));
		}
	}
}

/** Fixes the plus icons for this subtree. Has to be done after the entryInfo is set. */
function fixPlusIcons(subtreeEl) {
	var children = getChildrenOfSubtreeEl(subtreeEl);
	var aloneEl = getAloneElFromSubtreeEl(subtreeEl);
	if (!children.length) {
		if (getEntryHasChildren(getDbIdFromEl(aloneEl))) {
			showPlusIcon(aloneEl);
			return true;
		} else {
			hidePlusIcon(aloneEl);
			return false;
		}
	}

	var anyHavePluses = false;
	for (var i = 0; i < children.length; ++i) {
		var child = children[i];
		anyHavePluses |= fixPlusIcons(child);
	}

	if (anyHavePluses) {
		showPlusIcon(aloneEl);
	} else {
		hidePlusIcon(aloneEl);
	}

	return anyHavePluses;
}

/** Returns the alone element for the entry which is the drop target. */
function getDropTarget(mousePos) {
	var containers = getAllContainers();
	for (var j = 0; j < containers.length; ++j) {
		var container = containers[j];
		var yDropPadding = 2;
		var yFirstOrLastDropPadding = 30;
		var topDropPadding = yDropPadding;
		var bottomDropPadding = yDropPadding;

		var xDropPadding = 20;
		var xFirstOrLastPadding = 30;
		var rightDropPadding = xFirstOrLastPadding;
		if (j !== containers.length - 1 && containers.length > 1) {
			rightDropPadding = xDropPadding;
		}

		var leftDropPadding = xFirstOrLastPadding;
		if (j !== 0 && containers.length > 1) {
			leftDropPadding = xDropPadding;
		}

		var els = container.getElementsByClassName("alone");
		for (var i = 0; i < els.length; ++i) {
			if (i === 0) {
				topDropPadding = yFirstOrLastDropPadding;
			}

			if (i === els.length - 1) {
				bottomDropPadding = yFirstOrLastDropPadding;
			}

			var currentTarget = els[i];

			var targetPos = getPosition(currentTarget);
			var targetWidth = parseInt(currentTarget.offsetWidth);
			var targetHeight = parseInt(currentTarget.offsetHeight);

			var currentTargetDbId = getDbIdFromEl(currentTarget);
			var currentTargetEntryType = getEntryType(currentTargetDbId);
			if ((currentTargetEntryType === "root" || currentTargetEntryType === "tableofcontents") &&
					getEntryHasChildren(currentTargetDbId)) {
				continue;
			}

			var targetLogicalX = getScrollLeft() + targetPos.x;
			var targetLogicalY = getScrollTop() + targetPos.y;

			if ((mousePos.x > targetLogicalX - leftDropPadding) && (mousePos.x < targetLogicalX + targetWidth + rightDropPadding) &&
					(mousePos.y > targetLogicalY - topDropPadding) &&
					(mousePos.y < (targetLogicalY + targetHeight + bottomDropPadding))) {
				var where = "bottom";
				var neighborDropZone = 7;
				if (targetHeight === 1) {
					where = "middle";
				} else if (mousePos.y < (targetLogicalY + neighborDropZone)) {
					where = "top";
				} else if (mousePos.y < (targetLogicalY + targetHeight - neighborDropZone)) {
					where = "middle";
				}

				return {
					"el": currentTarget,
					"where": where,
					"pos": targetPos
				};
			}
		}
	}

	return null;
}

var dragEntryMousePos = null;

/** Handles dragging of an entry. */
function dragEntryOnMouseMove(ev) {
    // In case the user ctrl clicked and unselected the last clicked entry.
	unhoverAllEntries();
	
	if (ev) {
		dragEntryMousePos = getMousePosition(ev);
	}

	if (draggedEntryClone) {
		draggedEntryClone.style.top = (dragEntryMousePos.y - draggedEntryMouseOffset.y) + "px";
		draggedEntryClone.style.left = (dragEntryMousePos.x - draggedEntryMouseOffset.x) + "px";
		dragEntryOnMouseMoveHelper(dragEntryMousePos, justTheEntry);

		if (acceptabilityDescEl) {
			acceptabilityDescEl.style.top = (dragEntryMousePos.y - 10) + "px";
			acceptabilityDescEl.style.left = (dragEntryMousePos.x + 8) + "px";
		}

		return false;
	}
}

/** Scrolls the window to pad the dragged entry. */
function scrollToPadDraggedEntry() {
	if (dragEntryMousePos) {
		var change = scrollToPadMouse(dragEntryMousePos);
		dragEntryMousePos.x += change.x;
		dragEntryMousePos.y += change.y;
		dragEntryOnMouseMove();
	}
}

/** Unselects the last item which was the drop target. */
function removeCueFromDropTarget() {
	if (lastEntryDropTarget) {
		if (lastEntryDropTarget.where === "middle" && !isElementOfClass(lastEntryDropTarget.el, "fakealone") && entryDropBorderEl) {
			entryDropBorderEl.style.display = "none";
		} else if (entryDropSiblingDividerEl) {
			entryDropSiblingDividerEl.style.display = "none";
		}
	}
}

/** Returns an array describing whether a drop target is acceptable for the dragged entry.
 * The first entry is can be "move" which means there is no target, "no-drop" which means the dragged entry can not be
dropped on the target or "pointer" which means the dragged entry can be
dropped on the target.
The second is a null or the reason why no-drop was returned.
 */
function acceptabilityOfDropTarget(dropTarget, justTheEntry) {
	var draggedEntryType = getEntryType(getDbIdFromEl(aloneElBeingDragged));
	var selectedDbIds = getSelectedDbIds(true);
	var numSelectedDbIds = selectedDbIds.length;
	var selectedDbId = null;
	var i;
	
	var atLeastOneHasChildren = false;
	for (i = 0; i < numSelectedDbIds; ++i) {
		selectedDbId = selectedDbIds[i];
		atLeastOneHasChildren = getEntryHasChildren(selectedDbId);
		if (atLeastOneHasChildren) {
			break;
		}
	}

	if (!dropTarget || dropTarget.el === aloneElBeingDragged) {
		return [ "move", uiText.dragHintWhatIsBeingDragged(draggedEntryType, numSelectedDbIds, justTheEntry, atLeastOneHasChildren) ];
	}

	var paneEl = getContainingPaneEl(dropTarget.el);
	var paneIndex = getIndexOfPaneEl(paneEl);

	if (!isPaneATree(paneIndex)) {
		return [ "no-drop", uiText.dragHintCanNotDropIntoList(draggedEntryType, numSelectedDbIds) ];
	}

	// A user can't drop onto something that is selected because it is also being dragged.
	var dropTargetId = getDbIdFromEl(dropTarget.el);

	if (isDbIdSelected(dropTargetId)) {
		return [ "no-drop", uiText.dragHintCanNotDropIntoSelected(draggedEntryType, numSelectedDbIds) ];
	}

	var subtreeElDropTarget = getSubtreeElForAloneEl(dropTarget.el);
	var targetEntryType = getEntryType(dropTargetId);

	for (i = 0; i < numSelectedDbIds; ++i) {
		selectedDbId = selectedDbIds[i];
		var error = acceptabilityOfDropTargetHelper(dropTarget.where, justTheEntry, subtreeElDropTarget,
				selectedDbId, targetEntryType, draggedEntryType, numSelectedDbIds);
		if (error) {
			return [ "no-drop", error ];
		}
	}

	// Do this here so that better error messages can be returned.
	if (!isPaneEditable(paneIndex)) {
		return [ "no-drop", uiText.dragHintCanNotDropIntoNonEditablePane(draggedEntryType, numSelectedDbIds) ];
	}

	return [ "pointer", uiText.dragHintCanDropHere(draggedEntryType, numSelectedDbIds, justTheEntry, atLeastOneHasChildren) ];
}

/** Helper for acceptabilityOfDropTarget(). */
function acceptabilityOfDropTargetHelper(where, justTheEntry, subtreeElDropTarget, draggedDbId,
		targetEntryType, draggedEntryType, numSelectedDbIds) {
	var targetTypeIsNotebook = targetEntryType === "notebook" || targetEntryType === "tableofcontents";
	var targetTypeIsNote = targetEntryType === "note" || targetEntryType === "root" || targetEntryType === "source";
	if (draggedEntryType === "notebook" && !targetTypeIsNotebook) {
		if (targetTypeIsNote) {
			return uiText.dragHintCanNotDropNotebookIntoNote();
		} else {
			return uiText.dragHintCanNotDropNotebookIntoThis();
		}
	}

	if ((draggedEntryType === "note"  || targetEntryType === "source") && !targetTypeIsNote) {
		if (targetTypeIsNotebook) {
			return uiText.dragHintCanNotDropNoteIntoNotebook(draggedEntryType);
		} else {
			return uiText.dragHintCanNotDropNoteIntoThis(draggedEntryType);
		}
	}

	// In case it is being dragged from a list.
	if (isListDbId(draggedDbId)) {
		var aloneDbId = getTrueDbIdFromListDbId(draggedDbId);
		var aloneEl = getAloneElByDbId(aloneDbId);
		if (aloneEl) {
			draggedDbId = aloneDbId;
		}
	}

	var subtreeElBeingDragged = getSubtreeElForAloneEl(getAloneElByDbId(draggedDbId));

	// In case it is being dragged into itself from a list.
	if (subtreeElDropTarget === subtreeElBeingDragged) {
		if (where === "middle") {
			return uiText.dragHintCanNotDropIntoSelf(draggedEntryType, numSelectedDbIds);
		} else {
			return uiText.dragHintCanNotDropNextToItself(draggedEntryType, numSelectedDbIds);
		}
	}

	// Prevent dragging the entry into its own parent.
	if (where === "middle" &&
			doesSubtreeElHaveParent(subtreeElBeingDragged) &&
			subtreeElDropTarget === getParentOfSubtreeEl(subtreeElBeingDragged)) {
		return uiText.dragHintCanNotDropIntoParent(draggedEntryType, numSelectedDbIds);
	}

	// Prevents circular references.
	if (!justTheEntry) {
		if (isEntryDescendentOfAncestor(subtreeElDropTarget, subtreeElBeingDragged)) {
			return uiText.dragHintCanNotDropIntoSub(draggedEntryType, numSelectedDbIds);
		}
	}

	// Prevents no op drags.
	if (doesSubtreeElHavePrevious(subtreeElBeingDragged) &&
			getPreviousOfSubtreeEl(subtreeElBeingDragged) === subtreeElDropTarget &&
			where === "bottom") {
		return uiText.dragHintCanNotDropNextToItself(draggedEntryType, numSelectedDbIds);
	}

	if (doesSubtreeElHaveNext(subtreeElBeingDragged) &&
			getNextOfSubtreeEl(subtreeElBeingDragged) === subtreeElDropTarget &&
			where === "top") {
		return uiText.dragHintCanNotDropNextToItself(draggedEntryType, numSelectedDbIds);
	}

	if ((where === "top" || where === "bottom")) {
		if (!doesSubtreeElHaveParent(subtreeElDropTarget)) {
			return uiText.dragHintCanNotDropNextToSomethingThatHasNoParent(targetEntryType);
		} else if (subtreeElBeingDragged === getParentOfSubtreeEl(subtreeElDropTarget)) {
			return uiText.dragHintCanNotDropNextToItsOwnChild(targetEntryType, numSelectedDbIds);
		}
	}

	return null;
}

/** Returns true if the entry is a descendent of the potential ancestor. */
function isEntryDescendentOfAncestor(potentialAncestorSubtreeEl, subtreeEl) {
	while (true) {
		if (potentialAncestorSubtreeEl === subtreeEl) {
			return true;
		}

		if (!doesSubtreeElHaveParent(potentialAncestorSubtreeEl)) {
			break;
		}

		potentialAncestorSubtreeEl = getParentOfSubtreeEl(potentialAncestorSubtreeEl);
	}

	return false;
}

/** Returns true if the drop targets are equal. */
function dropTargetsAreEqual(left, right) {
	if (!left && !right) {
		return true;
	}

	if (!left || !right) {
		return false;
	}

	return left.el === right.el && left.where === right.where;
}

/** Adds a dashed border to a drop target. */
function addDashedBorder(dropTarget, color) {
	if (dropTarget.where === "middle" && !isElementOfClass(dropTarget.el, "fakealone")) {
		if (!entryDropBorderEl) {
			entryDropBorderEl = document.createElement('DIV');
			entryDropBorderEl.className = "dropBorder";
			document.body.appendChild(entryDropBorderEl);
		}

		entryDropBorderEl.style.border = "dashed 3px " + color;
		entryDropBorderEl.style.width = dropTarget.el.offsetWidth + "px";
		entryDropBorderEl.style.height = dropTarget.el.offsetHeight + "px";
		entryDropBorderEl.style.top = getScrollTop() + dropTarget.pos.y - 3 + "px";
		entryDropBorderEl.style.left = getScrollLeft() + dropTarget.pos.x - 3 + "px";

		entryDropBorderEl.style.display = "block";
	} else {
		if (!entryDropSiblingDividerEl) {
			entryDropSiblingDividerEl = document.createElement('DIV');
			entryDropSiblingDividerEl.className = "dropDivider";
			document.body.appendChild(entryDropSiblingDividerEl);
		}

		entryDropSiblingDividerEl.style["border-top"] = "dashed 3px " + color;
		entryDropSiblingDividerEl.style.width = dropTarget.el.offsetWidth + "px";
		entryDropSiblingDividerEl.style.top = getScrollTop() + dropTarget.pos.y +
		(dropTarget.where === "bottom" ? dropTarget.el.offsetHeight
				: -3) + "px";
		entryDropSiblingDividerEl.style.left = getScrollLeft() + dropTarget.pos.x + "px";

		entryDropSiblingDividerEl.style.display = "";
	}
}

/** Updates the acceptability description for the drop target. */
function updateAcceptabilityDescription(acceptability, acceptabilityDesc) {
	if (acceptabilityDesc === lastEntryDropTargetAcceptabilityDesc) {
		return;
	}

	if (acceptabilityDescEl) {
		acceptabilityDescEl.parentNode.removeChild(acceptabilityDescEl);
		acceptabilityDescEl = null;
	}

	lastEntryDropTargetAcceptabilityDesc = acceptabilityDesc;
	if (!acceptabilityDesc) {
		return;
	}

	acceptabilityDescEl = document.createElement("DIV");
	acceptabilityDescEl.className = "dropHint dropHint" + acceptability;
	acceptabilityDescEl.innerHTML = acceptabilityDesc;
	document.body.appendChild(acceptabilityDescEl);
}

/** Handles mousemove for entry dragging. */
function dragEntryOnMouseMoveHelper(mousePos, justTheEntry) {
	var dropTarget = getDropTarget(mousePos);
	var acceptabilityInfo = acceptabilityOfDropTarget(dropTarget, justTheEntry);
	var acceptability = acceptabilityInfo[0];
	updateAcceptabilityDescription(acceptability, acceptabilityInfo[1]);

	draggedEntryButtonEl.style.cursor = acceptability;

	if (acceptability !== "pointer") {
		removeCueFromDropTarget();

		if (!dropTarget) {
			lastEntryDropTarget = null;
		} else {
			lastEntryDropTarget = dropTarget;

			if (aloneElBeingDragged !== dropTarget.el) {
				addDashedBorder(dropTarget, "red");
			}
		}

		return;
	}

	if (!dropTargetsAreEqual(lastEntryDropTarget, dropTarget)) {
		removeCueFromDropTarget();

		lastEntryDropTarget = dropTarget;
		addDashedBorder(dropTarget, "#0D0");
	}
}

/** Handles mouseup for entry dragging. */
function dragEntryOnMouseUp(ev) {
	if (!draggedEntryClone) {
		return;
	}

	clearInterval(padDraggedEntryInterval);
	padDraggedEntryInterval = null;

	removeCueFromDropTarget();
	document.body.removeChild(draggedEntryClone);

	if (ev) {
		// Check everything again one last time in case the user pressed or
		// unpressed control before mousing up.
		var mousePos = getMousePosition(ev);
		var dropTarget = getDropTarget(mousePos);
		var acceptabilityInfo = acceptabilityOfDropTarget(dropTarget, justTheEntry);
		var acceptability = acceptabilityInfo[0];
		if (acceptability === "pointer") {
			handleDrop(dropTarget, justTheEntry);
		} else {
			commandsAreNowAllowed(true);
		}
	} else {
		commandsAreNowAllowed(true);
	}
	
	aloneElBeingDragged = null;
	draggedEntryClone = null;
	draggedEntryButtonEl = null;
	document.onmousemove = null;
	document.onmouseup = null;
	updateAcceptabilityDescription(null, null);
}

/** Handles dropping on a drop target. */
function handleDrop(dropTarget, justTheEntry) {
	if (!dropTarget || !aloneElBeingDragged) {
		return;
	}

	var droppedDbIds = getSelectedDbIds(true);

	var draggedDbId = getDbIdFromEl(aloneElBeingDragged);
	if (!isDbIdSelected(draggedDbId)) {
		droppedDbIds.push(draggedDbId);
	}

	var entryType = getEntryType(draggedDbId);

	var title = uiText.popupTitleDragNodeToNewParent(entryType);
	if (dropTarget.where !== "middle") {
		title = uiText.popupTitleDragNodeToNewSibling(dropTarget.where, entryType);
	}

	var handleDropHelper = function() {
		var sortedDroppedDbIds = sortIdsByAscendingYPosition(droppedDbIds);
		if (dropTarget.where === "middle") {
			var targetId = getDbIdFromEl(dropTarget.el);
			ensureDirectChildrenAreVisible(targetId,
				function() {
				makeEntriesSiblingOrChild(dropTarget.el, sortedDroppedDbIds, justTheEntry, "/makeChildrenJson", uiText.popupTitleDragNodeToNewParent, null);
			},
			function() {
				commandsAreNowAllowed(true);
				showPopupForError(title, uiText.errorChildrenNeededToBeLoadedFirst(entryType));
			});
		} else {
			makeEntriesSiblingOrChild(dropTarget.el, sortedDroppedDbIds, justTheEntry, "/makeSiblingsJson",
					uiText.popupTitleDragNodeToNewSibling, dropTarget.where === "top" ? "previous" : "next");
		}
	};

	var handleError = function() {
		commandsAreNowAllowed(true);
		showPopupForError(title, uiText.errorChildrenNeededToBeLoadedFirst(entryType));
	};

	var needToLoadChildren = false;
	if (justTheEntry) {
		for (var i = 0; i < droppedDbIds.length; ++i) {
			var movedId = droppedDbIds[i];
			var movedAloneEl = getAloneElByDbId(movedId);
			var movedSubtreeEl = getSubtreeElForAloneEl(movedAloneEl);
			var movedHasChildren = getEntryHasChildren(movedId);
			var movedHasChildrenDisplayed = doesSubtreeElHaveChildrenDisplayed(movedSubtreeEl);

			if (movedHasChildren && !movedHasChildrenDisplayed) {
				needToLoadChildren = true;
				ensureDirectChildrenAreVisible(movedId, handleDropHelper, handleError);
			}
		}
	}

	if (!needToLoadChildren) {
		handleDropHelper();
	}
}

/** Returns a sorted an array of dbIds sorted by the ascending Y position the corresponding aloneEl. */
function sortIdsByAscendingYPosition(dbIds) {
	var idsAndY = [];
	for (var i = 0; i < dbIds.length; ++i) {
		var dbId = dbIds[i];
		var aloneEl = getAloneElByDbId(dbId);
		var y = getPosition(aloneEl).y;
		idsAndY.push([dbId, y]);
	}

	idsAndY.sort(function(a, b) { return a[1] - b[1]; });

	var sortedIds = [];
	for (i = 0; i < idsAndY.length; ++i) {
		sortedIds.push(idsAndY[i][0]);
	}

	return sortedIds;
}

/** Makes a set of entries a child or a sibling of another entry. */
function makeEntriesSiblingOrChild(targetAloneEl, movedDbIds, justTheEntry, uri, popupTitleFunc, placement) {
	var targetId = getDbIdFromEl(targetAloneEl);
	var entryType = getEntryType(targetId);

	var xhr = createJsonAsyncRequest("POST", uri + "?" + getAnUrlUniquer(), function() {
		aRequestIsInProgress(false);
		commandsAreNowAllowed(true);
		
		var errorTitle = popupTitleFunc(entryType, placement);

		if (xhr.status === 200) {
			var listResults = validateArrayResponse(xhr.responseText);
			if ("error" in listResults) {
				showPopupForError(errorTitle, listResults.error);
				return;
			}

			var indexFromList = 0;

			for (var j = 0; j < movedDbIds.length; ++j) {
				var movedId = movedDbIds[j];
				var isFromList = isListDbId(movedId);
				var movedAloneEl;
				if (isFromList) {
					unselectEntry(movedId);
					movedAloneEl = getAloneElByDbId(movedId);
					if (movedAloneEl) {
						updateSelectionDisplayForAloneEl(movedAloneEl);
					}

					var aloneDbId = getTrueDbIdFromListDbId(movedId);
					var aloneEl = getAloneElByDbId(aloneDbId);
					if (aloneEl) {
						movedId = aloneDbId;
						++indexFromList;
						isFromList = false;
					}
				}

				var targetSubtreeEl = getSubtreeElForAloneEl(targetAloneEl);
				if (isFromList) {
					if (indexFromList >= listResults.length) {
						showPopupForError(errorTitle, uiText.errorJsonArrayDoesNotHaveIndex(uiText.errorProbablyNotSaved(), indexFromList));
						return;
					}

					var entryDetails = validateMakeSiblingOrChildResponse(listResults[indexFromList]);
					if ("error" in entryDetails) {
						showPopupForError(errorTitle, entryDetails.error);
						return;
					}

					if (placement === "next") {
						createEntryEl(entryDetails, targetSubtreeEl, "insertAfter", entryDetails.type);
					} else if (placement === "previous") {
						createEntryEl(entryDetails, targetSubtreeEl, "insertBefore", entryDetails.type);
					} else {
						createEntryEl(entryDetails, targetSubtreeEl, "addChildLast", entryDetails.type);
					}

					setEntryInfo(entryDetails.id, entryDetails.note, entryDetails.quotation, entryDetails.isPublic, entryDetails.type);
					++indexFromList;
				} else {
					movedAloneEl = getAloneElByDbId(movedId);
					var movedSubtreeEl = getSubtreeElForAloneEl(movedAloneEl);

					// Save this for later so that hasChildren of this parent can be updated.
					var oldParentOfMoved = null;
					if (doesSubtreeElHaveParent(movedSubtreeEl)) {
						oldParentOfMoved = getParentOfSubtreeEl(movedSubtreeEl);
					}

					var movedHadChildren = getEntryHasChildren(movedId);

					if (!justTheEntry) {
						removeSubtreeElFromParent(movedSubtreeEl);
						if (placement === "next") {
							insertSubtreeAfterSibling(movedSubtreeEl,
									targetSubtreeEl);
						} else if (placement === "previous") {
							insertSubtreeBeforeSibling(movedSubtreeEl,
									targetSubtreeEl);
						} else {
							addLastChildToSubtreeEl(targetSubtreeEl, movedSubtreeEl);
						}
					} else {
						// Remove the children of the entry being moved.
						var childrenOfMoved = copyArray(getChildrenOfSubtreeEl(movedSubtreeEl));
						for (var i = 0; i < childrenOfMoved.length; ++i) {
							removeSubtreeElFromParent(childrenOfMoved[i]);
						}

						// Make those children children of the moved entry's parent.
						if (oldParentOfMoved) {
							var previous = movedSubtreeEl;
							for (i = 0; i < childrenOfMoved.length; ++i) {
								insertSubtreeAfterSibling(childrenOfMoved[i],
										previous, oldParentOfMoved);
								previous = childrenOfMoved[i];
							}
						}

						var container = getContainerForEl(movedSubtreeEl);

						// Remove the entry.
						removeSubtreeElFromParent(movedSubtreeEl);

						// Insert the entry.
						if (placement === "next") {
							insertSubtreeAfterSibling(movedSubtreeEl,
									targetSubtreeEl);
						} else if (placement === "previous") {
							insertSubtreeBeforeSibling(movedSubtreeEl,
									targetSubtreeEl);
						} else {
							addLastChildToSubtreeEl(targetSubtreeEl, movedSubtreeEl);
						}

						// If the moved entry was root of the container pick a new root.
						if (!oldParentOfMoved) {
							var subtreeToMakeRoot = targetSubtreeEl;
							while (doesSubtreeElHaveParent(subtreeToMakeRoot)) {
								subtreeToMakeRoot = getParentOfSubtreeEl(subtreeToMakeRoot);
							}

							container.appendChild(subtreeToMakeRoot);
						}

						setEntryHasNoChildren(movedId);

						if (movedHadChildren && oldParentOfMoved) {
							setEntryHasChildren(getDbIdFromSubtreeEl(oldParentOfMoved));
						}
					}

					// Update whether or not the old parent has children.
					if (oldParentOfMoved && !doesSubtreeElHaveChildrenDisplayed(oldParentOfMoved)) {
						setEntryHasNoChildren(getDbIdFromSubtreeEl(oldParentOfMoved));
					}
					
					if (oldParentOfMoved) {
						showOrHidePlusUpToRoot(oldParentOfMoved);
					}
				}

				// Update that the new parent has children.
				if (placement === null) {
					setEntryHasChildren(getDbIdFromSubtreeEl(targetSubtreeEl));
					showOrHidePlusUpToRoot(targetSubtreeEl);
				} else {
					showOrHidePlusUpToRoot(getParentOfSubtreeEl(targetSubtreeEl));
				}
				
				updateEntryDetailsHasParent(movedId);
			}
		} else {
			var errorText = getErrorTextNotFound(xhr, uiText.errorNotMoved(entryType));
			showPopupForError(errorTitle, errorText);
		}
	});

	var message = {
			'targetId' : targetId,
			'movedIds' : movedDbIds,
			'justTheEntry' : justTheEntry,
			'csrft': getCsrft()
	};

	if (placement) {
		message.placement = placement;
	}

	xhr.send(JSON.stringify(message));
	aRequestIsInProgress(true);
}

/** Handles clicking the open button. */
function onClickOpen(ev) {
	var eventEl = getEventEl(ev);

	var aloneEl = getCorrespondingAloneEl(eventEl);
	if (!aloneEl) {
		return;
	}

	var paneEl = getContainingPaneEl(eventEl);
	var paneIndex = getIndexOfPaneEl(paneEl);
	var paneType = getPaneType(paneIndex);
	var dbId = getTrueDbIdFromListDbId(getDbIdFromEl(aloneEl));
	if (paneType === "notebooks") {
		var notebookPaneEl = document.getElementById(dbId);
		if (!notebookPaneEl) {
			makePane("/notebook/" + dbId, null, null, dbId);
		} else {
			startPaneReloadedAnim(dbId);
		}

		return;
	}

	var entryType = getEntryType(getDbIdFromEl(aloneEl));
	var xhr = createJsonAsyncRequest("POST", "/getNotebookPathJson?" + getAnUrlUniquer(), function() {
		aRequestIsInProgress(false);

		var errorTitle = uiText.popupTitleShowNotebookFor(entryType);

		if (xhr.status === 200) {
			var listResults = validateArrayResponse(xhr.responseText);
			if ("error" in listResults) {
				showPopupForError(errorTitle, listResults.error);
				return;
			}

			if (listResults.length === 1) {
				showPopupForError(errorTitle, uiText.errorNoteIsNotPartOfANotebook(entryType));
				return;
			}

			var notebookId = listResults[listResults.length - 1];
			var continueShowPath = function(startAt) {
				var dbIdToRequest;
				var dbIdToShow;
				var nextAloneEl;
				for (var i = startAt; i >= 0; --i) {
					dbIdToShow = listResults[i];
					nextAloneEl = getAloneElByDbId(dbIdToShow);
					if (!nextAloneEl) {
						dbIdToRequest = listResults[i + 1];
						break;
					}
				}

				if (dbIdToRequest) {
					var recallContinueShowPath = function() {
						continueShowPath(i);
					};

					ensureDirectChildrenAreVisible(dbIdToRequest,
							function() {
						recallContinueShowPath();
					},
					function() {
						showPopupForError(errorTitle, uiText.errorChildrenNeededToBeLoadedFirst(entryType));
					});
				} else if (i === -1) {
					selectAloneEl(nextAloneEl, true);
					startFastScrollTransition(dbIdToShow);
				}
			};

			var startShowPath = function() {
				continueShowPath(listResults.length - 2);
			};

			var notebookPaneEl = document.getElementById(notebookId);
			if (!notebookPaneEl) {
				makePane("/notebook/" + notebookId, null, null, notebookId, null, startShowPath);
			} else {
				startShowPath();
			}
		} else {
			var errorText = getErrorTextNotFound(xhr, uiText.errorNoteNotFound(entryType));
			showPopupForError(errorTitle, errorText);
		}
	});

	var message = {
			'entryId' : dbId,
			'csrft': getCsrft()
	};

	xhr.send(JSON.stringify(message));
	aRequestIsInProgress(true);
}

/** Handles clicking the create subnote button. */
function onClickCreateSubnote(ev) {
	var eventEl = getEventEl(ev);

	var aloneEl = getCorrespondingAloneEl(eventEl);
	if (!aloneEl) {
		return;
	}

	selectAloneEl(aloneEl, true);

	showPopupForCreateChildEntry();
}

/** Handles clicking the edit button. */
function editOnClick(ev) {
	var eventEl = getEventEl(ev);

	var aloneEl = getCorrespondingAloneEl(eventEl);
	if (!aloneEl) {
		return;
	}

	selectAloneEl(aloneEl, true);

	showPopupForEditEntry();
}

/** Handles clicking the delete button. */
function deleteOnClick(ev) {
	var eventEl = getEventEl(ev);

	var aloneEl = getCorrespondingAloneEl(eventEl);
	if (!aloneEl) {
		return;
	}

	selectAloneEl(aloneEl, !ev.ctrlKey && !ev.shiftKey);

	showPopupForDeleteEntry();
}

/** Returns the set of errors as a single string. */
function getErrorTextNotFound(xhr, prefix) {
	var response;
	if (xhr.responseText === "") {
		return uiText.errorEmptyResponseFromServer(prefix, xhr.status);
	} else {
		try {
			response = JSON.parse(xhr.responseText);
		} catch(e) {
			return uiText.errorJsonCouldNotBeParsed(prefix);
		}
	}

	var result = "";
	if ("errors" in response) {
		for (var i = 0; i < response.errors.length; ++i) {
			result += " " + response.errors[i];
		}
	}

	return result;
}

/** Enables or disabled all input for the main popup. */
function toggleEnabledInputForPopup() {
	// Prevent the user from making changes that have no impact.
	// It would be cleaner if these were only disabled for appropriate popups.
	toggleEnableElIfExists("noteinput");
	toggleEnableElIfExists("quotationinput");
	toggleEnableElIfExists("childrenToParent");
	toggleEnableElIfExists("childrenToOrphan");
	toggleEnableElIfExists("childrenToDelete");
	toggleEnableElIfExists("insertAsFirstChild");
	toggleEnableElIfExists("isPublic");
	toggleEnableElIfExists("showTimestamps");
	toggleEnableElIfExists("saveOnEnter");

	// Prevent double submit.
	toggleEnableElIfExists("submit");
}

/** Continue the save() that was already started. */
function saveButAlreadyStarted() {
	save(true);
}

/** Handles the save button clicks of any popup. */
function save(alreadyStartedSave) {
	// Validations.
	var note = document.getElementById("noteinput");
	var quotation = document.getElementById("quotationinput");
	if (quotation && note && quotation.value.trim() === "" && note.value.trim() === "") {
		setResponseErrorMessage(uiText.errorBlankNoteAndQuotation());
		return;
	} else if (!quotation && note && note.value.trim() === "") {
		var type = document.getElementById("type");
		if (type && type.value === "notebook") {
			setResponseErrorMessage(uiText.errorBlankNotebookTitle());
		} else {
			setResponseErrorMessage(uiText.errorBlankNote());
		}
		return;
	}

	if (!alreadyStartedSave) {
		toggleEnabledInputForPopup();
	}

	var xhr = createJsonAsyncRequest("POST", "/noteOpJson?" + getAnUrlUniquer(), function() {
		aRequestIsInProgress(false);

		if (xhr.status === 200) {
			document.getElementById("response").innerHTML = "<span class=\"successMessage\">" + uiText.sentenceSuccessfullySaved() + "</span>";
			var response;
			var noteop = document.getElementById('noteop');
			var newSubtreeEl = null;
			var newAloneEl;
			var sibling;
			var parentId;
			var paneIndex;
			var paneEl;
			var userWasSignedIn;
			var notebooksPaneEl;
			var pageTitleDiv;
			var wasANotebookDeleted;
			var wasASourceDeleted;
			var i;

			if (noteop && noteop.value === 'newNotebook') {
				response = validateNewNotebookResponse(xhr.responseText);
				if (!response) {
					toggleEnabledInputForPopup();
					return;
				}

				userWasSignedIn = response.userWasSignedIn;

				makePane("/notebook/" + response.id, response.note, null, response.id);

				notebooksPaneEl = document.getElementById("notebooks");
				if (notebooksPaneEl) {
					var container = getContainerFromPaneEl(notebooksPaneEl);
					if (container) {
						createEntryEl(response, container, "addChildLast", response.type);
	
						setEntryInfo(response.id, response.note, response.quotation, response.isPublic, response.type);
						setEntryHasChildren(parentId);
					}
				}
			}

			if (noteop && noteop.value === 'editNotebook') {
				response = validateEditResponse(xhr.responseText);
				if (!response) {
					toggleEnabledInputForPopup();
					return;
				}

				paneIndex = parseInt(document.getElementById('paneIndex').value);
				paneEl = getPaneElByIndex(paneIndex);
				pageTitleDiv = getTitleDivFromPaneEl(paneEl);
				pageTitleDiv.innerHTML = response.noteHtml;
				updatePaneTitle(paneIndex, response.note);
				updateDisplayedEntryDetails(response);
			}

			if (noteop && noteop.value === 'editSource') {
				response = validateEditResponse(xhr.responseText);
				if (!response) {
					toggleEnabledInputForPopup();
					return;
				}

				paneIndex = parseInt(document.getElementById('paneIndex').value);
				paneEl = getPaneElByIndex(paneIndex);
				pageTitleDiv = getTitleDivFromPaneEl(paneEl);
				pageTitleDiv.innerHTML = response.noteHtml;
				updatePaneTitle(paneIndex, response.note);
				updateDisplayedEntryDetails(response);
			}

			if (noteop && noteop.value === 'deleteNotebook') {
				response = validateDeleteResponse(xhr.responseText);
				paneIndex = parseInt(document.getElementById('paneIndex').value);
				paneEl = getPaneElByIndex(paneIndex);
				closePaneByEl(paneEl);

				notebooksPaneEl = document.getElementById("notebooks");
				if (notebooksPaneEl) {
					removeEntriesFromTree("delete", document.getElementById('ids').value, true, false);
				}

				wasANotebookDeleted = true;
			}

			if (noteop && noteop.value === 'deleteSource') {
				response = validateDeleteResponse(xhr.responseText);
				paneIndex = parseInt(document.getElementById('paneIndex').value);
				paneEl = getPaneElByIndex(paneIndex);
				closePaneByEl(paneEl);

				wasASourceDeleted = true;
			}

			if (noteop && noteop.value === 'edit') {
				response = validateEditResponse(xhr.responseText);
				if (!response) {
					toggleEnabledInputForPopup();
					return;
				}

				updateDisplayedEntryDetails(response);

				setEntryInfo(response.id, response.note, response.quotation, response.isPublic, response.type, getEntryHasChildren(response.id));

				var paneForEntryEl = document.getElementById(response.id);
				if (paneForEntryEl) {
					pageTitleDiv = getTitleDivFromPaneEl(paneForEntryEl);
					pageTitleDiv.innerHTML = response.noteHtml;
					updatePaneTitle(getIndexOfPaneEl(paneForEntryEl), response.note);
				}
			}

			if (noteop && (noteop.value === 'delete' || noteop.value === 'makeNotebook')) {
				response = validateDeleteResponse(xhr.responseText);
				var ids = document.getElementById('ids').value;
				// Tree removal must be done first.
				var whatWasDeleted = removeEntriesFromTree(noteop.value, ids, isElementChecked('childrenToParent'), true);
				wasANotebookDeleted = whatWasDeleted[0];
				wasASourceDeleted = whatWasDeleted[1];
				removeEntriesFromLists(noteop.value, ids);
			}

			if (noteop && noteop.value === 'createChild') {
				response = validateNoteOpResponse(xhr.responseText);
				if (!response) {
					toggleEnabledInputForPopup();
					return;
				}

				var insertAs = "addChildLast";
				if (isElementChecked('insertAsFirstChild')) {
					insertAs = "addChildFirst";
				}

				parentId = document.getElementById('id').value;
				newSubtreeEl = createEntryEl(response, getSubtreeElByDbId(parentId), insertAs, response.type);

				setEntryInfo(response.id, response.note, response.quotation, response.isPublic, response.type);
				setEntryHasChildren(parentId);
				selectAloneEl(getAloneElFromSubtreeEl(newSubtreeEl), true);
			}

			if (noteop && noteop.value === 'insert') {
				response = validateNoteOpResponse(xhr.responseText);
				if (!response) {
					toggleEnabledInputForPopup();
					return;
				}

				var existingSubtreeEl = getSubtreeElByDbId(document
						.getElementById('id').value);
				newSubtreeEl = createEntryEl(response,
						existingSubtreeEl, "insertBefore", response.type);
				newAloneEl = getAloneElFromSubtreeEl(newSubtreeEl);

				removeSubtreeElFromParent(existingSubtreeEl);

				addLastChildToSubtreeEl(newSubtreeEl, existingSubtreeEl);

				setEntryInfo(response.id, response.note, response.quotation, response.isPublic, response.type);
				setEntryHasChildren(response.id);
				if (isShowingPlus(getAloneElFromSubtreeEl(existingSubtreeEl))) {
					showPlusIcon(newAloneEl);
				}

				selectAloneEl(newAloneEl, true);
			}

			if (noteop && noteop.value === 'putBefore') {
				response = validateNoteOpResponse(xhr.responseText);
				if (!response) {
					toggleEnabledInputForPopup();
					return;
				}

				sibling = getSubtreeElByDbId(document.getElementById('id').value);
				newSubtreeEl = createEntryEl(response, sibling, "insertBefore", response.type);
				setEntryInfo(response.id, response.note, response.quotation, response.isPublic, response.type);
				selectAloneEl(getAloneElFromSubtreeEl(newSubtreeEl), true);
			}

			if (noteop && noteop.value === 'putAfter') {
				response = validateNoteOpResponse(xhr.responseText);
				if (!response) {
					toggleEnabledInputForPopup();
					return;
				}

				sibling = getSubtreeElByDbId(document.getElementById('id').value);
				newSubtreeEl = createEntryEl(response, sibling, "insertAfter", response.type);
				setEntryInfo(response.id, response.note, response.quotation, response.isPublic, response.type);
				selectAloneEl(getAloneElFromSubtreeEl(newSubtreeEl), true);
			}

			if (noteop && noteop.value === 'putUnderneath') {
				response = validateNoteOpResponse(xhr.responseText);
				if (!response) {
					toggleEnabledInputForPopup();
					return;
				}

				parentId = document.getElementById('id').value;
				var parentSubtreeEl = getSubtreeElByDbId(parentId);
				newSubtreeEl = createEntryEl(
						response,
						parentSubtreeEl,
						"addChildLast", response.type);

				newAloneEl = getAloneElFromSubtreeEl(newSubtreeEl);

				var siblings = getSiblingsOfSubtreeEl(newSubtreeEl);

				for (i = 0; i < siblings.length; ++i) {
					var siblingSubtreeEl = siblings[i];
					removeSubtreeElFromParent(siblingSubtreeEl);
					addLastChildToSubtreeEl(newSubtreeEl, siblingSubtreeEl);
				}

				setEntryInfo(response.id, response.note, response.quotation, response.isPublic, response.type);
				if (isShowingPlus(getAloneElFromSubtreeEl(parentSubtreeEl))) {
					showPlusIcon(newAloneEl);
				}

				setEntryHasChildren(parentId);
				if (siblings.length) {
					setEntryHasChildren(response.id);
				}

				selectAloneEl(newAloneEl, true);
			}

			if (response.deleted) {
				for (i = 0; i < response.deleted.length; ++i) {
					removeEntriesFromTree("delete", response.deleted[i], true, false);
				}
			}

			refreshQuotationsPane();
			refreshSourcesPane();
			refreshAllSourcePanes();
			refreshSearchPane();

			createWelcomePaneIfNeeded();

			setTimeout(function() {
				if (userWasSignedIn) {
					var newUrl = "/notebook/" + response.id;
					if (!isDisplaySmall()) {
						newUrl += "/quotations/";
					}
					location.assign(newUrl);
				} else {
					closePopup();

					if (newSubtreeEl) {
						var dbId = getDbIdFromSubtreeEl(newSubtreeEl);
						// This is a little shorter than the background animation.
						startScrollTransition(dbId, 1200);
					}
				}
			}, 500);
		} else {
			var errorText = getErrorText(xhr, uiText.errorNotSaved(), uiText.errorProbablyNotSaved());
			setResponseErrorMessage(errorText);
			toggleEnabledInputForPopup();
		}
	});

	var message = { 'csrft': getCsrft() };
	copyInputToMessage(message, 'noteinput', "note");
	copyInputToMessage(message, 'quotationinput', "quotation");
	copyInputToMessage(message, 'id');
	copyInputToMessage(message, 'ids');
	copyInputToMessage(message, 'noteop');
	copyInputToMessage(message, 'type');
	setInMessageFromCheckBox(message, 'childrenToParent', 'childrenAction', 'parent');
	setInMessageFromCheckBox(message, 'childrenToOrphan', 'childrenAction', 'orphan');
	setInMessageFromCheckBox(message, 'childrenToDelete', 'childrenAction', 'delete');
	copyCheckBoxToMessageIfTrue(message, 'insertAsFirstChild');
	copyCheckBoxToMessageIfTrue(message, 'isPublic');
	copyCheckBoxToMessageIfTrue(message, 'unlinkOnly');
	
	document.getElementById("response").innerHTML = uiText.sentenceSaving();
	xhr.send(JSON.stringify(message));
	aRequestIsInProgress(true);
}

/** Updates the displayed details for an entry. */
function updateDisplayedEntryDetails(response) {
	var dbId = getTrueDbIdFromListDbId(response.id);
	var aloneEls = document.getElementsByClassName(dbId);
	for (var i = 0; i < aloneEls.length; ++i) {
		var aloneEl = aloneEls[i];

		var quotation = null;
		if (isAloneElAQuotation(aloneEl)) {
			quotation = getQuotationElOfAloneEl(aloneEl);
			quotation.innerHTML = response.quotationHtml;
		}

		var note = getNoteElOfAloneEl(aloneEl);
		if (quotation && !note && response.noteHtml) {
			note = document.createElement("DIV");
			note.className = "note mousetrap";
			quotation.parentNode.insertBefore(note, quotation.nextSibling.nextSibling);
		}
		
		if(note) {
			note.innerHTML = response.noteHtml;
			makeNotesInTreeContentEditable(note.parentNode);
		}
		
		var modTimeEl = getModTimeElOfAloneEl(aloneEl);
		if (modTimeEl) {
			modTimeEl.innerHTML = uiText.formatDateTime(response.modTime);
		}

		setEntryInfoIfPresent(getDbIdFromEl(aloneEl), response.note, response.quotation, response.isPublic, response.type, getEntryHasChildren(dbId));
	}
}

/** Updates all entry infos with the dbId to indicate that they have a parent. */
function updateEntryDetailsHasParent(dbId) {
	dbId = getTrueDbIdFromListDbId(dbId);
	var aloneEls = document.getElementsByClassName(dbId);
	for (var i = 0; i < aloneEls.length; ++i) {
		var aloneEl = aloneEls[i];

		setEntryHasParent(getDbIdFromEl(aloneEl));
	}
}

/** Remove each entry from any lists it is in. */
function removeEntriesFromLists(noteop, idsValue) {
	var onlyUnlink = isElementChecked("onlyUnlink");
	
	var ids = idsValue.split(",");
	for (var j = 0; j < ids.length; ++j) {
		var id = ids[j];

		if (onlyUnlink && isListDbId(id) && isEntryAQuotation(id) || getEntryType(id) === "source") {
			setEntryHasNoParent(id);
			setEntryHasNoChildren(id);
			continue;
		}
		
		var dbId = getTrueDbIdFromListDbId(id);

		var aloneEls = copyArray(document.getElementsByClassName(dbId));
		for (var i = 0; i < aloneEls.length; ++i) {
			var aloneEl = aloneEls[i];
			var subtreeEl = getSubtreeElForAloneEl(aloneEl);
			handleRemovalOfSubtreeAndChildren(subtreeEl);
			removeSubtreeElFromParent(subtreeEl);
		}
	}
}

/** Finishing handling delete and make notebook operations for save(). */
function removeEntriesFromTree(noteop, idsValue, childrenToParent, removeCorrespondingPane) {
	var ids = idsValue.split(",");
	var wasANotebookDeleted;
	var wasASourceDeleted;

	for (var j = 0; j < ids.length; ++j) {
		var id = ids[j];
		var subtreeEl = getSubtreeElByDbId(id);
		if (!subtreeEl) {
			// It was already removed recursively.
			continue;
		}

		if (noteop === 'delete') {
			var entryType = getEntryType(id);
			if (entryType === "notebook") {
				wasANotebookDeleted = true;
			} else if (entryType === "source") {
				wasANotebookDeleted = true;
			}
		}

		// Get this now so that we later update whether the parent has children left.
		var parentEl = null;
		if (doesSubtreeElHaveParent(subtreeEl)) {
			parentEl = getParentOfSubtreeEl(subtreeEl);
		}

		removeAloneElFromSelections(getAloneElFromSubtreeEl(subtreeEl));

		var childrenWereMovedToParent = false;
		if (noteop === 'delete' && childrenToParent === true) {
			if (removeCorrespondingPane) {
				var paneEl = document.getElementById(id);
				if (paneEl) {
					closePaneByEl(paneEl);
				}
			}

			var children = copyArray(getChildrenOfSubtreeEl(subtreeEl));
			for (var i = 0; i < children.length; ++i) {
				var child = children[i];
				removeSubtreeElFromParent(child);
				insertSubtreeBeforeSibling(child, subtreeEl);
			}

			if (children.length) {
				childrenWereMovedToParent = true;
			}
		}

		handleRemovalOfSubtreeAndChildren(subtreeEl);
		removeSubtreeElFromParent(subtreeEl);

		if (parentEl) {
			if (!childrenWereMovedToParent && !doesSubtreeElHaveChildrenDisplayed(parentEl)) {
				setEntryHasNoChildren(getDbIdFromSubtreeEl(parentEl));
			}
			
			showOrHidePlusUpToRoot(parentEl);
		}
	}

	return [ wasANotebookDeleted, wasASourceDeleted ];
}

/** Unselects, unhovers and deletes entry text for all entries in subtree recursively. */
function handleRemovalOfSubtreeAndChildren(subtreeEl) {
	var dbId = getDbIdFromSubtreeEl(subtreeEl);
	unselectEntry(dbId);
	unhoverEntry(dbId);
	removeEntryInfo(dbId);
	stopBackgroundTransitionById(dbId);

	var children = getChildrenOfSubtreeEl(subtreeEl);
	for (var i = 0; i < children.length; ++i) {
		handleRemovalOfSubtreeAndChildren(children[i]);
	}
}

/** Unselects all entries in subtree recursively. */
function unselectSubtreeAndChildren(subtreeEl) {
	var dbId = getDbIdFromSubtreeEl(subtreeEl);
	unselectEntry(dbId);

	var children = getChildrenOfSubtreeEl(subtreeEl);
	for (var i = 0; i < children.length; ++i) {
		unselectSubtreeAndChildren(children[i]);
	}
}

/** Copies a form field into the message. */
function copyInputToMessage(message, fieldName, destinationFieldName) {
	if (document.getElementById(fieldName)) {
		message[destinationFieldName ? destinationFieldName : fieldName] = document.getElementById(fieldName).value;
	}
}

/** Copies a checkbox field into the message if it is true. */
function copyCheckBoxToMessageIfTrue(message, fieldName) {
	if (isElementChecked(fieldName)) {
		message[fieldName] = true;
	}
}

/** Sets a value in the message if the checkbox field is true. */
function setInMessageFromCheckBox(message, formFieldName, messageFieldName, value) {
	if (isElementChecked(formFieldName)) {
		message[messageFieldName] = value;
	}
}

/** Moves the selected entry before its previous. */
function moveEntriesBefore() {
	moveEntries("before");
	return false;
}

/** Moves the selected entry after its next. */
function moveEntriesAfter() {
	moveEntries("after");
	return false;
}

/** Moves the selected entry left to be a child of its parent. */
function moveEntriesLeft() {
	moveEntries("left");
	return false;
}

/** Moves the selected entry right to be a child of its previous. */
function moveEntriesRight() {
	moveEntries("right");
	return false;
}

/** Returns the parent if the the dbIds all have a parent and it is the same parent, otherwise returns null. */
function doAllDbIdsHaveTheSameParent(dbIds) {
	var parent = null;
	var lastParent = null;
	for (var i = 0; i < dbIds.length; ++i) {
		var dbId = dbIds[i];
		var subtreeEl = getSubtreeElByDbId(dbId);
		if (!doesSubtreeElHaveParent(subtreeEl)) {
			return null;
		}

		parent = getParentOfSubtreeEl(subtreeEl);
		if (!lastParent) {
			lastParent = parent;
		} else if (parent !== lastParent) {
			return null;
		}
	}

	return lastParent;
}

/** If the selected entries have the same parent return them in order, otherwise null. */
function getSelectedOrderedDbIds() {
	var selectedDbIds = getSelectedDbIds();
	var parentSubtreeEl = doAllDbIdsHaveTheSameParent(selectedDbIds);
	if (parentSubtreeEl === null) {
		return null;
	}

	var children = getChildrenOfSubtreeEl(parentSubtreeEl);
	var result = [];
	for (var i = 0; i < children.length; ++i) {
		var child = children[i];
		var childDbId = getDbIdFromSubtreeEl(child);
		var childIsSelected = isDbIdSelected(childDbId);

		if (childIsSelected) {
			result.push(childDbId);
		}
	}

	return result;
}

/** If the selected entries are contiguous return an array of the dbIds, otherwise null. */
function getSelectedOrderedContiguousDbIds() {
	var selectedDbIds = getSelectedDbIds();
	var parentSubtreeEl = doAllDbIdsHaveTheSameParent(selectedDbIds);
	if (parentSubtreeEl === null) {
		return null;
	}

	var children = getChildrenOfSubtreeEl(parentSubtreeEl);
	var firstChildSelected = -1;
	var lastChildSelected = -1;
	var result = [];
	for (var i = 0; i < children.length; ++i) {
		var child = children[i];
		var childDbId = getDbIdFromSubtreeEl(child);
		var childIsSelected = isDbIdSelected(childDbId);

		if (childIsSelected) {
			result.push(childDbId);

			if (firstChildSelected === -1) {
				firstChildSelected = i;
			} else if (lastChildSelected < i - 1) {
				return null;
			}
		}

		if (childIsSelected) {
			lastChildSelected = i;
		}
	}

	return result;
}

/** Moves an an entry before, after, left or right. */
function moveEntries(direction) {
	if (!getFirstContainer(true)) {
		return;
	}

	var orderedDbIds = null;
	var title = uiText.popupTitleMoveTheSelectedNote(direction, getDefaultEntryTypes());
	if (getNumSelected(false, true) < 1) {
		showPopupForError(title, uiText.errorAtLeastOneNoteMustBeSelected(getDefaultEntryTypes()));
		return;
	}

	if (areAnySelectedFromAList()) {
		showPopupForError(title, uiText.errorMaySelectFromList(getDefaultEntryTypes()));
		return false;
	}

	var selectedDbId = getSelectedDbId(false, true);
	var entryType = getEntryType(selectedDbId);
	title = uiText.popupTitleMoveTheSelectedNote(direction, entryType);

	if (getNumSelected(false, true) > 1) {
		if (direction === 'left') {
			orderedDbIds = getSelectedOrderedDbIds();
			if (orderedDbIds === null) {
				showPopupForError(title, uiText.errorSelectedMustHaveTheSameParent(entryType));
				return;
			}
			
			orderedDbIds.reverse();
		} else {
			orderedDbIds = getSelectedOrderedContiguousDbIds();
			if (orderedDbIds === null) {
				showPopupForError(title, uiText.errorSelectedMustBeContiguous(entryType));
				return;
			}

			if (direction === 'after') {
				orderedDbIds.reverse();
			}
		}
	} else {
		orderedDbIds = [getSelectedDbId(false, true)];
	}

	var firstMovedSubtreeEl = getSubtreeElByDbId(orderedDbIds[0]);

	if (direction === 'before' && !doesSubtreeElHavePrevious(firstMovedSubtreeEl)) {
		showPopupForError(title, uiText.errorFirstCanNotBeMovedBefore(entryType));
		return;
	} else if (direction === 'after' &&
			!doesSubtreeElHaveNext(firstMovedSubtreeEl)) {
		showPopupForError(title, uiText.errorLastCanNotBeMovedAfter(entryType));
		return;
	} else if (direction === 'left' &&
			(!doesSubtreeElHaveParent(firstMovedSubtreeEl) ||
					!doesSubtreeElHaveParent(getParentOfSubtreeEl(firstMovedSubtreeEl)))) {
		showPopupForError(title, uiText.errorEntryCanNotBeMovedLeftWithoutGrandParent(entryType));
		return;
	} else if (direction === 'right' &&
			!doesSubtreeElHavePrevious(firstMovedSubtreeEl)) {
		showPopupForError(title, uiText.errorEntryCanNotBeMovedRightWithoutPrevious(entryType));
		return;
	} else {
		var moveEntriesHelper = function() {
			var xhr = createJsonAsyncRequest("POST", "/moveNotesJson?" + getAnUrlUniquer(), function() {
				aRequestIsInProgress(false);

				if (xhr.status === 200) {
					for (var i = 0; i < orderedDbIds.length; ++i) {
						var dbId = orderedDbIds[i];
						var movedSubtreeEl = getSubtreeElByDbId(dbId);
						var before, parent;
						if (direction === 'before') {
							before = getPreviousOfSubtreeEl(movedSubtreeEl);
							removeSubtreeElFromParent(movedSubtreeEl);
							insertSubtreeBeforeSibling(movedSubtreeEl, before);
						} else if (direction === 'after') {
							var afterAfter = getNextOfSubtreeEl(getNextOfSubtreeEl(movedSubtreeEl));
							parent = getParentOfSubtreeEl(movedSubtreeEl);
							removeSubtreeElFromParent(movedSubtreeEl);
							insertSubtreeBeforeSibling(movedSubtreeEl,
									afterAfter, parent);
						} else if (direction === 'left') {
							parent = getParentOfSubtreeEl(movedSubtreeEl);
							removeSubtreeElFromParent(movedSubtreeEl);
							insertSubtreeAfterSibling(movedSubtreeEl, parent);
							if (!doesSubtreeElHaveChildrenDisplayed(parent)) {
								setEntryHasNoChildren(getDbIdFromSubtreeEl(parent));
							}

							if (isShowingPlus(getAloneElFromSubtreeEl(movedSubtreeEl))) {
								showPlusUpToRoot(getParentOfSubtreeEl(movedSubtreeEl));
							}

							showOrHidePlusUpToRoot(parent);							
						} else if (direction === 'right') {
							before = getPreviousOfSubtreeEl(movedSubtreeEl);
							removeSubtreeElFromParent(movedSubtreeEl);
							addLastChildToSubtreeEl(before, movedSubtreeEl);
							setEntryHasChildren(getDbIdFromSubtreeEl(before));
							parent = getParentOfSubtreeEl(movedSubtreeEl);
							if (isShowingPlus(getAloneElFromSubtreeEl(movedSubtreeEl))) {
								showPlusIcon(getAloneElFromSubtreeEl(parent));
							}
						}
					}

					// Make sure the user can see the moved entry.
					if (orderedDbIds.length) {
						if (direction === 'before' || direction === 'left' || direction === 'right') {
							startFastScrollTransition(orderedDbIds[0]);
						} else if (direction === 'after') {
							startFastScrollTransition(orderedDbIds[orderedDbIds.length - 1]);
						}
					}
				} else {
					var errorText = getErrorText(xhr, uiText.errorNotMoved(entryType), uiText.errorProbablyNotMoved(entryType));
					showPopupForError(title, errorText);
				}

				commandsAreNowAllowed(true);
			});

			var message = {
					'direction' : direction,
					'ids' : orderedDbIds,
					'csrft': getCsrft()
			};

			xhr.send(JSON.stringify(message));
			aRequestIsInProgress(true);
			commandsAreNowAllowed(false);
		};

		if (direction === 'right') {
			var dbId = orderedDbIds[0];
			var movedSubtreeEl = getSubtreeElByDbId(dbId);
			var before = getPreviousOfSubtreeEl(movedSubtreeEl);
			ensureDirectChildrenAreVisible(getDbIdFromSubtreeEl(before), moveEntriesHelper, function() {
				showPopupForError(title, uiText.errorChildrenNeededToBeLoadedFirst(entryType));
			});
		} else {
			moveEntriesHelper();
		}
	}
}

/** Returns the end of the save form. */
function endOfForm(funcName) {
	if (!funcName) {
		funcName = "save";
	}

	return "<table class=\"responseAndSave\"><tr>" +
	"<td><div id=\"response\"></div></td>" +
	"<td><button id=\"submit\" class=\"specialbutton\" onclick=\"" + funcName + "(); return false;\" title=\"" + uiText.tooltipButtonSave() + "\">" + uiText.buttonSave() + "</button></td>" +
	"</tr></table>";
}

/** Returns HTML for the isPublic form field. */
function getIsPublicInput(/*isPublic*/) {
	return "";
	// Disable for now this feature needs usability improvements.
	/*
	return "<input type=\"checkbox\" name=\"isPublic\" id=\"isPublic\"" + (isPublic ? " checked" : "") + "><label for=\"isPublic\">" + uiText.labelIsItPublic() + "</label><br>";
	 */
}

/** Shows the popup for editing an entry. */
function showPopupForEditEntry() {
	if (!areCommandsAllowed) {
		return false;
	}

	if (!getFirstContainer()) {
		return false;
	}

	var title = uiText.popupTitleEditTheSelectedNote(getDefaultEntryTypes());

	if (getNumSelected() < 1) {
		showPopupForError(title, uiText.errorANoteMustBeSelected(getDefaultEntryTypes()));
		return false;
	} else if (getNumSelected() > 1) {
		showPopupForError(title, uiText.errorOnlyOneNoteMayBeSelected(getDefaultEntryTypes()));
	} else {
		var selectedDbId = getSelectedDbId();
		var entryType = getEntryType(selectedDbId);
		title = uiText.popupTitleEditTheSelectedNote(entryType);
		startFastScrollTransition(selectedDbId, function() {
			var onCloseCallbacks = [removeTextEditingShortCuts];
			if (entryType === "notebook") {
				onCloseCallbacks.push(removeCreateNotebookPopupShortCuts);
			}
			
			var popup = createPopupForDialog(true, onCloseCallbacks);
			
			addTextEditingShortCuts(entryType);
			showPopupWithNoteFields(popup, title, entryType, "edit", selectedDbId, null, true);
		});
	}

	return false;
}

/** Finish the popup with common input fields and show it. */
function showPopupWithNoteFields(popup, title, entryType, noteop, dbId, extraHtml, getTextForDbId) {
	var html = decoratePopupTitle(title);

	html += ifNeededSentenceCtrlAndAltEnterToSubmit(entryType);

	if (entryType !== "notebook" && entryType !== "source") {
		html += "<div class=\"infotext\">" + uiText.sentenceSuggestShortNotes() + "</div>";
	}

	var needsLabels = false;
	var isQuotation = isEntryAQuotation(dbId);
	if (dbId && isQuotation && getTextForDbId) {
		needsLabels = true;
		html += "<div class=\"textLabel\">" + uiText.labelQuotation() + "</div>";
		html += "<div><textarea class=\"mousetrap\" id=\"quotationinput\" placeholder=\"" + uiText.textYourQuotation() + "\"></textarea></div>";
		html += "<input type=\"hidden\" id=\"quotationoriginal\">";
	}

	html += getNoteInputFieldForType(entryType, dbId, needsLabels) + "<br>";
	if (dbId) {
		html += "<input type=\"hidden\" value=\"" + dbId + "\" id=\"id\">";
	}

	if (extraHtml) {
		html += extraHtml;
	}

	html += "<input type=\"hidden\" value=\"" + noteop + "\" id=\"noteop\">";
	if (dbId) {
		html += "<input type=\"hidden\" id=\"noteoriginal\">";
	}

	html += endOfForm();

	addThenCenterPopup(popup, html);

	var note = document.getElementById('noteinput');
	if (getTextForDbId) {
		var noteText = getTextForDbId;
		if (getTextForDbId.constructor !== String) {
			noteText = getEntryNoteText(dbId);
		}

		note.value = noteText;

		if (dbId) {
			var noteOriginal = document.getElementById('noteoriginal');
			noteOriginal.value = noteText;
		}
	}

	if (isQuotation && getTextForDbId && getTextForDbId.constructor !== String) {
		var quotation = document.getElementById('quotationinput');
		if (getTextForDbId) {
			quotation.value = getEntryQuotationText(dbId);
			if (dbId) {
				var quotationOriginal = document.getElementById('quotationoriginal');
				quotationOriginal.value = getEntryQuotationText(dbId);
			}
		}
	}
}

/** Shows the popup for editing a notebook entry. */
function showPopupForEditNotebook(id, notebookTitle, paneIndex) {
	if (!areCommandsAllowed) {
		return false;
	}

	var popup = createPopupForDialog(true, [removeTextEditingShortCuts, removeCreateNotebookPopupShortCuts]);
	addTextEditingShortCuts("notebook");

	showPopupWithNoteFields(popup, uiText.popupTitleEditTheSelectedNotebook(), "notebook", "editNotebook", id,
			"<input type=\"hidden\" value=\"" + paneIndex + "\" id=\"paneIndex\">", notebookTitle);

	return false;
}

/** Shows the popup for editing a source entry. */
function showPopupForEditSource(id, sourceTitle, paneIndex) {
	if (!areCommandsAllowed) {
		return false;
	}

	var popup = createPopupForDialog(true, [removeTextEditingShortCuts]);
	addTextEditingShortCuts("source");

	showPopupWithNoteFields(popup, uiText.popupTitleEditTheSelectedSource(), "source", "editSource", id,
			"<input type=\"hidden\" value=\"" + paneIndex + "\" id=\"paneIndex\">", sourceTitle);

	return false;
}

/** Sets up the options dict for getting and setting. */
function resetOptionsDict() {
	var el = document.getElementById('optionsDictJson');
	if (!el) {
		window.optionsDict = {};
	}

	try {
		var dict = JSON.parse(el.innerHTML);
		if (dict.constructor !== Object) {
			window.optionsDict = {};
		}

		window.optionsDict = dict;
	} catch(e) {
		window.optionsDict = {};
	}
}

/** Sets up the session dict for getting and setting. */
function resetSessionDict() {
	var el = document.getElementById('sessionDictJson');
	if (!el) {
		window.sessionDict = {};
	}

	try {
		var dict = JSON.parse(el.innerHTML);
		if (dict.constructor !== Object) {
			window.sessionDict = {};
		}

		window.sessionDict = dict;
	} catch(e) {
		window.sessionDict = {};
	}
}

/** Sets up the entry info for getting and setting. */
function addEntryDictEl(dictEl, notIsTree) {
	if (!window.entryInfoDict) {
		window.entryInfoDict = {};
	}

	var dict;
	try {
		dict = JSON.parse(dictEl.innerHTML);
	} catch(e) {
		return null;
	}

	if (dict.constructor === Object) {
		addToEntryInfoDict(dict, notIsTree);
	}

	return dict;
}

/** Adds or replaces new info to the entry info dictionaries. */
function addToEntryInfoDict(additionalEntryInfos, notIsTree) {
	for (var dbId in additionalEntryInfos) {
		window.entryInfoDict[dbId] = additionalEntryInfos[dbId];
		if (!notIsTree) {
			updateHasChildrenIcons(dbId);
		}
	}
}

/** Gets the note text for an entry. */
function getEntryNoteText(dbId) {
	if (!window.entryInfoDict || !(dbId in window.entryInfoDict))
		return "";

	return window.entryInfoDict[dbId][0];
}

/** Gets the quotation text for an entry. */
function getEntryQuotationText(dbId) {
	if (!window.entryInfoDict || !(dbId in window.entryInfoDict))
		return "";

	return window.entryInfoDict[dbId][1];
}

/** Gets whether the entry is public. */
function getEntryIsPublic(dbId) {
	if (!window.entryInfoDict || !(dbId in window.entryInfoDict))
		return false;

	return window.entryInfoDict[dbId][2];
}

/** Gets whether the entry has children. */
function getEntryHasChildren(dbId) {
	if (!window.entryInfoDict || !(dbId in window.entryInfoDict))
		return false;

	return window.entryInfoDict[dbId][3];
}

/** Gets the entry's type. */
function getEntryType(dbId) {
	if (!window.entryInfoDict || !(dbId in window.entryInfoDict))
		return "";

	return window.entryInfoDict[dbId][4];
}

/** Gets whether the entry has a parent. */
function getEntryHasParent(dbId) {
	if (!window.entryInfoDict || !(dbId in window.entryInfoDict))
		return false;

	return window.entryInfoDict[dbId][5];
}

/** Sets that the entry has a parent. */
function setEntryHasParent(dbId) {
	if (!window.entryInfoDict || !(dbId in window.entryInfoDict))
		return;

	window.entryInfoDict[dbId][5] = true;
}

/** Sets that the entry has no parent. */
function setEntryHasNoParent(dbId) {
	if (!window.entryInfoDict || !(dbId in window.entryInfoDict))
		return;

	window.entryInfoDict[dbId][5] = false;
}

/** Sets the information for an entry. Defaults hasChildren to false. */
function setEntryInfo(dbId, noteText, quotationText, isPublic, type, hasChildren, hasParent) {
	hasChildren = (typeof hasChildren === 'undefined') ? false : hasChildren;
	hasParent = (typeof hasChildren === 'undefined') ? false : hasParent;

	window.entryInfoDict[dbId] = [ noteText, quotationText, isPublic, hasChildren, type, hasParent ];

	updateHasChildrenIcons(dbId);
}

/** Sets the information for an entry if the info already exists. Defaults hasChildren to false. */
function setEntryInfoIfPresent(dbId, noteText, quotationText, isPublic, type, hasChildren) {
	if (!window.entryInfoDict || !(dbId in window.entryInfoDict))
		return;

	setEntryInfo(dbId, noteText, quotationText, isPublic, type, hasChildren);
}

/** Removes information for an entry. */
function removeEntryInfo(dbId) {
	delete window.entryInfoDict[dbId];
}

/** Records that the entry has no children. */
function setEntryHasNoChildren(dbId) {
	if (!(dbId in window.entryInfoDict)) {
		return;
	}

	window.entryInfoDict[dbId][3] = false;

	updateHasChildrenIcons(dbId);

	showOrHidePlusUpToRoot(getSubtreeElByDbId(dbId));
}

/** Records that the entry has at least one child. */
function setEntryHasChildren(dbId) {
	if (!(dbId in window.entryInfoDict)) {
		return;
	}

	window.entryInfoDict[dbId][3] = true;

	updateHasChildrenIcons(dbId);
}

/** Updates the icons for the entry to reflect if the entry has children. */
function updateHasChildrenIcons(dbId) {
	var aloneEl = getAloneElByDbId(dbId);
	if (!aloneEl) {
		return;
	}

	setTheRightChildIconsForAloneEl(aloneEl);
}

/** Updates the icons for all entries to reflect if the entry has children. */
function updateAllHasChildrenIcons(entryInfoDict) {
	for (var dbId in entryInfoDict) {
		updateHasChildrenIcons(dbId);
	}
}

/** Returns an array of the object's keys. */
function getObjectKeys(object) {
    var keys = [];

    for (var key in object) {
        if (object.hasOwnProperty(key)) {
            keys.push(key);
        }
    }

    return keys;
}

/** Shows the popup for deleting an entry. */
function showPopupForDeleteEntry() {
	if (!areCommandsAllowed) {
		return false;
	}

	if (!getFirstContainer()) {
		return false;
	}

	var title = uiText.popupTitleDeleteTheSelectedNote(getDefaultEntryTypes());

	if (getNumSelected() < 1) {
		showPopupForError(title, uiText.errorAtLeastOneNoteMustBeSelected(getDefaultEntryTypes()));
	} else {
		var selectedDbIds = getSelectedDbIds();
		var entryType;
		var selectedDbId;
		for (var j = 0; j < selectedDbIds.length; ++j) {
			selectedDbId = selectedDbIds[j];
			var tmpEntryType = getEntryType(selectedDbId);

			if (tmpEntryType === "notebook") {
				entryType = tmpEntryType;
				break;
			}

			entryType = tmpEntryType;
		}

		title = uiText.popupTitleDeleteTheSelectedNote(entryType);

		if (getNumSelected() > 1) {
			title = uiText.popupTitleDeleteTheSelectedNotes(entryType);
			if (entryType === "notebook") {
				showPopupForError(title, uiText.errorOnlyOneNoteMayBeSelected(entryType));
				return false;
			}
		}

		var anyHasChildren = false;
		var anyHasParent = false;
		var notVisibleEntries = 0;
		var numHiddenChildren = 0;
		var numSourcesFromNotebook = 0;
		var numQuotationsFromNotebook = 0;
		var numSourcesFromList = 0;
		var numQuotationsFromList = 0;
		var uniqueDbIds = {};
		for (var i = 0; i < selectedDbIds.length; ++i) {
			selectedDbId = selectedDbIds[i];
			
			var uniqueDbId = getTrueDbIdFromListDbId(selectedDbId);
			
			if (uniqueDbId in uniqueDbIds) {
				if (!isListDbId(selectedDbId)) {
					uniqueDbIds[uniqueDbId] = selectedDbId;
				}
				
				continue;
			}
			
			uniqueDbIds[uniqueDbId] = selectedDbId;
			
			var subtreeEl = getSubtreeElByDbId(selectedDbId);

			var hasChildren = doesSubtreeElHaveChildrenDisplayed(subtreeEl);
			anyHasChildren |= hasChildren;

			anyHasParent |= doesSubtreeElHaveParent(subtreeEl);

			if (!isEntryVisible(selectedDbId)) {
				++notVisibleEntries;
			}

			if (getEntryHasChildren(selectedDbId)) {
				if (!hasChildren) {
					++numHiddenChildren;
				}

				anyHasChildren = true;
			}

			var isFromList = isListDbId(selectedDbId);
			if (getEntryHasParent(selectedDbId)) {
				if(isEntryAQuotation(selectedDbId)) {
					if (!isFromList) {
						++numQuotationsFromNotebook;
					} else {
						++numQuotationsFromList;
					}
				} else if(getEntryType(selectedDbId) === "source") {
					if (!isFromList) {
						++numSourcesFromNotebook;
					} else {
						++numSourcesFromList;
					}
				}
			}
		}

		var showPopupForDeleteEntryHelper = function() {
			var popup = createPopupForDialog(true, [removeDeleteOptionsShortCuts]);
			addDeleteOptionsShortCuts();

			// Delete the ids from top to bottom so that the order of events is the same on the server and the GUI.
			// But that only matters for tree deletes.
			var dbIds = getObjectKeys(uniqueDbIds);
			var treeDbIds = [];
			var listDbIds = [];
			for (var i = 0; i < dbIds.length; ++i) {
				var dbId = uniqueDbIds[dbIds[i]];
				if (isListDbId(dbId)) {
					listDbIds.push(dbId);
				} else {
					treeDbIds.push(dbId);
				}
			}
			
			var sortedUniqueDbIds = copyArray(sortIdsByAscendingYPosition(treeDbIds), listDbIds);
			var html = decoratePopupTitle(title);

			if (sortedUniqueDbIds.length > 1 && notVisibleEntries) {
				html += "<span class=\"errorMessage\">" + uiText.sentenceNoteToDeleteAreNotVisible(notVisibleEntries, sortedUniqueDbIds.length, entryType) + "</span>";
			} else if (numHiddenChildren) {
				html += "<span class=\"errorMessage\">" + uiText.sentenceNoteToDeleteHiddenChildren(numHiddenChildren, sortedUniqueDbIds.length, entryType) + "</span>";
			} else {
				html += uiText.sentenceNumNotesWillBeDeleted(sortedUniqueDbIds.length, entryType) + "<br>";
			}

			html += "<input type=\"hidden\" value=\"" +
			sortedUniqueDbIds.join() +
			"\" id=\"ids\"><input type=\"hidden\" " +
			"value=\"delete\" id=\"noteop\">";

			if (entryType === "notebook") {
				html += "<input type=\"hidden\" checked name=\"children\" value=\"parent\" id=\"childrenToParent\">";
			} else if (anyHasChildren) {
				html += uiText.sentenceWhatToDoWithChildren(entryType) + "<br>";
				if (anyHasParent) {
					html += "<input checked type=\"radio\" name=\"children\" value=\"parent\" id=\"childrenToParent\"><label for=\"childrenToParent\">" + uiText.labelMoveToParent(entryType) + "</label><br>";
				}

				/** Disable for now because probably no one will want this feature unless it is more polished.
	            html += "<input " + childrenToOrphanDisabled +
	            	" type=\"radio\" name=\"children\" value=\"orphan\" id=\"childrenToOrphan\"><label for=\"childrenToOrphan\">" + uiText.labelOrphan() + "</label><br>";
				 */

				html += "<input " + (!anyHasParent ? "checked " : "") + " type=\"radio\" name=\"children\" value=\"delete\" id=\"childrenToDelete\"><label for=\"childrenToDelete\">" + uiText.labelDelete() + "</label><br>";
			}
			
			if (numSourcesFromNotebook || numQuotationsFromNotebook) {
				html += "<input checked type=\"checkbox\" name=\"unlinkOnly\" id=\"unlinkOnly\"><label for=\"unlinkOnly\">" +
				uiText.labelOnlyUnlinkSourcesAndQuotations(numSourcesFromNotebook, numQuotationsFromNotebook) + "</label><br>";
			}
			
			if (numSourcesFromList || numQuotationsFromList) {
				html += uiText.sentenceHowSourceAndQuotationDeletesWork(numSourcesFromList, numQuotationsFromList) + "<br>";
			}
			
			html += endOfForm("prepForDeleteAndSave");

			addThenCenterPopup(popup, html);

			// Do not auto focus on mobile. It messes up the window.
			// Do not auto focus for notebooks since deleting has such a greater impact.
			if (entryType !== "notebook" && !isDisplaySmall()) {
				var submit = document.getElementById('submit');
				if (submit) {
					submit.focus();
				}
			}
		};

		if (selectedDbIds.length === 1) {
			startFastScrollTransition(selectedDbIds[0], showPopupForDeleteEntryHelper);
		} else {
			showPopupForDeleteEntryHelper();
		}
	}

	return false;
}

/** Shows the popup for deleting a notebook. */
function showPopupForDeleteNotebook(id, paneIndex) {
	if (!areCommandsAllowed) {
		return false;
	}

	var title = uiText.popupTitleDeleteTheSelectedNotebook();

	var popup = createPopupForDialog(true, [removeDeleteOptionsShortCuts]);
	addDeleteOptionsShortCuts();
	var html = decoratePopupTitle(title);

	html += "<input type=\"hidden\" value=\"" +
	id +
	"\" id=\"ids\">" +
	"<input type=\"hidden\" " +
	"value=\"" + paneIndex + "\" id=\"paneIndex\">" +
	"<input type=\"hidden\" " +
	"value=\"deleteNotebook\" id=\"noteop\">";

	html += endOfForm();

	addThenCenterPopup(popup, html);

	return false;
}

/** Shows the popup for deleting a source. */
function showPopupForDeleteSource(id, paneIndex) {
	if (!areCommandsAllowed) {
		return false;
	}

	var title = uiText.popupTitleDeleteTheSelectedSource();

	var popup = createPopupForDialog(true, [removeDeleteOptionsShortCuts]);
	addDeleteOptionsShortCuts();
	var html = decoratePopupTitle(title);

	html += "<input type=\"hidden\" value=\"" +
	id +
	"\" id=\"ids\">" +
	"<input type=\"hidden\" " +
	"value=\"" + paneIndex + "\" id=\"paneIndex\">" +
	"<input type=\"hidden\" " +
	"value=\"deleteSource\" id=\"noteop\">";

	html += endOfForm();

	addThenCenterPopup(popup, html);

	return false;
}

/** Shows the popup for exporting a notebook. */
function showPopupForExportNotebook(id) {
	if (!areCommandsAllowed) {
		return false;
	}

	var title = uiText.popupTitleExportTheNotebook();

	var popup = createPopupForDialog(true);
	var html = decoratePopupTitle(title);

	html += "<table><tr><td><form action=\"/doExport/" +
			"\" method=\"POST\">" +
			"<input checked onclick=\"exportFormatOnClick(event);\" type=\"radio\" name=\"format\" value=\"html\" id=\"formatHtml\"><label for=\"formatHtml\">" + uiText.labelFormatHtml() + "</label><br>" +
			"<div style=\"margin-left:20px;\">" +
			"<input checked type=\"radio\" name=\"htmlStructure\" value=\"paragraphs\" id=\"htmlStructureParagraphs\"><label for=\"htmlStructureParagraphs\">" + uiText.labelHtmlStructureParagraphs() + "</label><br>" +
			"<input type=\"radio\" name=\"htmlStructure\" value=\"nestedLists\" id=\"htmlStructureNestedLists\"><label for=\"htmlStructureNestedLists\">" + uiText.labelHtmlStructureNestedLists() + "</label><br>" +
			"</div>" +
			"<input onclick=\"exportFormatOnClick(event);\" type=\"radio\" name=\"format\" value=\"markdown\" id=\"formatMarkdown\"><label for=\"formatMarkdown\">" + uiText.labelFormatMarkdown() + "</label><br>" +
			"<input onclick=\"exportFormatOnClick(event);\" type=\"radio\" name=\"format\" value=\"rtf\" id=\"formatRtf\"><label for=\"formatRtf\">" + uiText.labelFormatRtf() + "</label><br>" +
			"<input checked type=\"checkbox\" name=\"includeReferencesSection\" id=\"includeReferencesSection\"><label for=\"includeReferencesSection\">" + uiText.labelIncludeReferencesSection() + "</label><br>" +
			"<input type=\"checkbox\" name=\"includeQuotations\" id=\"includeQuotations\"><label for=\"includeQuotations\">" + uiText.labelIncludeQuotations() + "</label><br>" +
			"<input type=\"hidden\" name=\"id\" value=\"" + id + "\">" +
			"<input type=\"hidden\" name=\"csrft\" value=\"" + getCsrft() + "\">" +
			"<button class=\"specialbutton withTopMargin\" title=\"" + uiText.tooltipButtonExport() + "\">" +
			uiText.buttonExport() + "</button></form></td></tr></table>";
	
	addThenCenterPopup(popup, html);

	return false;
}

/** Make sure the HTML structure radio buttons are only selectable when they should be. */
function exportFormatOnClick(ev) {
	var eventEl = getEventEl(ev);
	var disabled = eventEl.id !== "formatHtml";
	document.getElementById("htmlStructureParagraphs").disabled = disabled;
	document.getElementById("htmlStructureNestedLists").disabled = disabled;
}

/** Returns true if the element exists and is checked. */
function isElementChecked(id) {
	return document.getElementById(id) && document.getElementById(id).checked === true;
}

/** Prepare for deletion by loading children if needed. */
function prepForDeleteAndSave() {
	toggleEnabledInputForPopup();

	var idValue = document.getElementById('ids').value;
	var ids = idValue.split(",");

	var entryType = getEntryType(ids[0]);
	var handleError = function() {
		showPopupForError(uiText.popupTitleDeleteTheSelectedNote(entryType), uiText.errorChildrenNeededToBeLoadedFirst(entryType));
	};

	var waitingForChildren = false;
	if (isElementChecked('childrenToParent')) {
		for (var j = 0; j < ids.length; ++j) {
			var id = ids[j];
			var subtreeEl = getSubtreeElByDbId(id);

			if (!doesSubtreeElHaveParent(subtreeEl)) {
				continue;
			}

			waitingForChildren = true;
			ensureDirectChildrenAreVisible(id, saveButAlreadyStarted, handleError);
		}
	}

	if (!waitingForChildren) {
		save(true);
	}
}

/** Shows the popup for making an entry a notebook. */
function showPopupForMakeEntryNotebook() {
	if (!areCommandsAllowed) {
		return false;
	}

	if (!getFirstContainer(true)) {
		return false;
	}

	var title = uiText.popupTitleMakeNotebook();

	if (getNumSelected(false, true) < 1) {
		showPopupForError(title, uiText.errorAtLeastOneNoteMustBeSelected(getDefaultEntryTypes()));
	} else if (areAnySelectedFromAList()) {
		showPopupForError(title, uiText.errorMaySelectFromList(getDefaultEntryTypes()));
	} else {
		if (getNumSelected(false, true) > 1) {
			title = uiText.popupTitleMakeNotebooks();
		}

		var selectedDbIds = getSelectedDbIds(false, true);

		var notVisibleEntries = 0;
		for (var i = 0; i < selectedDbIds.length; ++i) {
			var selectedDbId = selectedDbIds[i];
			if (!isEntryVisible(selectedDbId)) {
				++notVisibleEntries;
			}
		}

		var showPopupForMakeEntryNotebookHelper = function() {
			var popup = createPopupForDialog(true);
			var sortedSelectedDbIds = sortIdsByAscendingYPosition(selectedDbIds);
			var html = decoratePopupTitle(title);

			if (selectedDbIds.length > 1 && notVisibleEntries) {
				html += "<span class=\"errorMessage\">" + uiText.sentenceNoteToMakeNotebooksAreNotVisible(notVisibleEntries, selectedDbIds.length) + "</span>";
			} else {
				html += uiText.sentenceNumNotesWillBeMadeNotebooks(selectedDbIds.length);
			}

			html += "<input type=\"hidden\" value=\"" +
			sortedSelectedDbIds.join() +
			"\" id=\"ids\"><input type=\"hidden\" value=\"makeNotebook\" id=\"noteop\">";

			html += endOfForm();

			addThenCenterPopup(popup, html);
		};

		if (selectedDbIds.length === 1) {
			startFastScrollTransition(selectedDbIds[0], showPopupForMakeEntryNotebookHelper);
		} else {
			showPopupForMakeEntryNotebookHelper();
		}
	}

	return false;
}

/** Returns info about enter and ctrl+enter if needed. */
function ifNeededSentenceCtrlAndAltEnterToSubmit(entryType) {
	if (entryType === "notebook" || entryType === "source") {
		return "";
	}

	if (getOptionSaveOnEnter()) {
		return uiText.sentenceCtrlAndAltEnterToSubmit(entryType) + "<br><br>";
	}

	return "";
}

/** Returns the note input field for the type. */
function getNoteInputFieldForType(entryType, id, needsLabels) {
	var result = "<input type=\"hidden\" value=\"" + entryType + "\" id=\"type\">";

	if (entryType === "notebook") {
		return result + "<input class=\"mousetrap\" type=\"text\" id=\"noteinput\" placeholder=\"" + uiText.labelYourNotebookTitle() + "\"><br>" +
		getIsPublicInput(getEntryIsPublic(id));
	}

	if (entryType === "source") {
		return result + "<input class=\"mousetrap\" type=\"text\" id=\"noteinput\" placeholder=\"" + uiText.labelTheSourcesTitle() + "\"><br>";
	}

	if (needsLabels) {
		result += "<div class=\"textLabel\">" + uiText.labelNote() + "</div>";
	}
	
	return result + "<textarea class=\"mousetrap\" id=\"noteinput\" placeholder=\"" + uiText.textYourNote() + "\"></textarea>";
}

/** Shows the popup for creating a child of an entry. */
function showPopupForCreateChildEntry() {
	if (!areCommandsAllowed) {
		return false;
	}

	if (!getFirstContainer(true)) {
		return false;
	}

	var title = uiText.popupTitleCreateChildNote(getDefaultEntryTypes());

	if (getNumSelected(false, true) < 1) {
		showPopupForError(title, uiText.errorANoteMustBeSelected(getDefaultEntryTypes()));
	} else if (getNumSelected(false, true) > 1) {
		showPopupForError(title, uiText.errorOnlyOneNoteMayBeSelected(getDefaultEntryTypes()));
	} else if (areAnySelectedFromAList()) {
		showPopupForError(title, uiText.errorMaySelectFromList(getDefaultEntryTypes()));
	} else {
		var selectedDbId = getSelectedDbId(false, true);
		var entryType = getEntryType(selectedDbId);
		title = uiText.popupTitleCreateChildNote(entryType);
		ensureDirectChildrenAreVisible(selectedDbId,
				startFastScrollTransition(selectedDbId, function() {
					var onCloseCallbacks = [removeCreateChildOptionsShortCuts, removeTextEditingShortCuts];
					if (entryType === "notebook") {
						onCloseCallbacks.push(removeCreateNotebookPopupShortCuts);
					}
					
					var popup = createPopupForDialog(true, onCloseCallbacks);
					addCreateChildOptionsShortCuts(entryType);

					var extraHtml = getInsertAsFirstChildHtml(entryType, selectedDbId);
					showPopupWithNoteFields(popup, title, entryType, "createChild", selectedDbId, extraHtml);
				}),
				function() {
			showPopupForError(title, uiText.errorChildrenNeededToBeLoadedFirst(entryType));
		});
	}

	return false;
}

/** Return the HTML for enabling the user to specify if the child should be inserted first. */
function getInsertAsFirstChildHtml(entryType, dbId) {
	if (getEntryHasChildren(dbId)) {
		return "<input type=\"checkbox\" name=\"insertAsFirstChild\" id=\"insertAsFirstChild\"><label for=\"insertAsFirstChild\">" + uiText.labelMakeFirstChild(entryType) + "</label><br><br>";
	}

	return "";
}

/** Shows the popup for inserting an entry as a parent of another entry. */
function showPopupForInsertEntry() {
	if (!areCommandsAllowed) {
		return false;
	}

	if (!getFirstContainer(true)) {
		return false;
	}

	var title = uiText.popupTitleInsertNote(getDefaultEntryTypes());

	if (getNumSelected(false, true) < 1) {
		showPopupForError(title, uiText.errorANoteMustBeSelected(getDefaultEntryTypes()));
	} else if (getNumSelected(false, true) > 1) {
		showPopupForError(title, uiText.errorOnlyOneNoteMayBeSelected(getDefaultEntryTypes()));
	} else if (areAnySelectedFromAList()) {
		showPopupForError(title, uiText.errorMaySelectFromList(getDefaultEntryTypes()));
	} else {
		var selectedDbId = getSelectedDbId(false, true);
		var entryType = getEntryType(selectedDbId);
		title = uiText.popupTitleInsertNote(entryType);
		startFastScrollTransition(selectedDbId, function() {
			var onCloseCallbacks = [removeTextEditingShortCuts];
			if (entryType === "notebook") {
				onCloseCallbacks.push(removeCreateNotebookPopupShortCuts);
			}
			var popup = createPopupForDialog(true, onCloseCallbacks);
			addTextEditingShortCuts(entryType);
			var subtreeEl = getSubtreeElByDbId(selectedDbId);
			var hasParent = doesSubtreeElHaveParent(subtreeEl);
			showPopupWithNoteFields(popup, title, entryType, "insert", selectedDbId,
					"<input type=\"hidden\" value=\"" + (hasParent ? "true" : "") + "\" id=\"hasParent\">");
		});
	}

	return false;
}

/** Shows the popup for putting a new entry after another entry. */
function showPopupForPutAfterEntry() {
	if (!areCommandsAllowed) {
		return false;
	}

	if (!getFirstContainer(true)) {
		return false;
	}

	var title = uiText.popupTitleNewNoteAfter(getDefaultEntryTypes());

	if (getNumSelected(false, true) < 1) {
		showPopupForError(title, uiText.errorANoteMustBeSelected(getDefaultEntryTypes()));
	} else if (getNumSelected(false, true) > 1) {
		showPopupForError(title, uiText.errorOnlyOneNoteMayBeSelected(getDefaultEntryTypes()));
	} else if (areAnySelectedFromAList()) {
		showPopupForError(title, uiText.errorMaySelectFromList(getDefaultEntryTypes()));
	} else {
		var selectedDbId = getSelectedDbId(false, true);
		var entryType = getEntryType(selectedDbId);
		title = uiText.popupTitleNewNoteAfter(entryType);
		if (!doesSubtreeElHaveParent(getSubtreeElByDbId(selectedDbId))) {
			showPopupForError(title, uiText.errorTheEntryNeedsAParent(entryType));
		} else {
			startFastScrollTransition(selectedDbId, function() {
				var onCloseCallbacks = [removeTextEditingShortCuts];
				if (entryType === "notebook") {
					onCloseCallbacks.push(removeCreateNotebookPopupShortCuts);
				}
				var popup = createPopupForDialog(true, onCloseCallbacks);
				addTextEditingShortCuts(entryType);

				showPopupWithNoteFields(popup, title, entryType, "putAfter", selectedDbId);
			});
		}
	}

	return false;
}

/** Shows the popup for putting a new entry before another entry. */
function showPopupForPutBeforeEntry() {
	if (!areCommandsAllowed) {
		return false;
	}

	if (!getFirstContainer(true)) {
		return false;
	}

	var title = uiText.popupTitleNewNoteBefore(getDefaultEntryTypes());

	if (getNumSelected(false, true) < 1) {
		showPopupForError(title, uiText.errorANoteMustBeSelected(getDefaultEntryTypes()));
	} else if (getNumSelected(false, true) > 1) {
		showPopupForError(title, uiText.errorOnlyOneNoteMayBeSelected(getDefaultEntryTypes()));
	} else if (areAnySelectedFromAList()) {
		showPopupForError(title, uiText.errorMaySelectFromList(getDefaultEntryTypes()));
	} else {
		var selectedDbId = getSelectedDbId(false, true);
		var entryType = getEntryType(selectedDbId);
		title = uiText.popupTitleNewNoteBefore(entryType);
		if (!doesSubtreeElHaveParent(getSubtreeElByDbId(selectedDbId))) {
			showPopupForError(title, uiText.errorTheEntryNeedsAParent(entryType));
		} else {
			startFastScrollTransition(selectedDbId, function() {
				var onCloseCallbacks = [removeTextEditingShortCuts];
				if (entryType === "notebook") {
					onCloseCallbacks.push(removeCreateNotebookPopupShortCuts);
				}
				var popup = createPopupForDialog(true, onCloseCallbacks);
				addTextEditingShortCuts(entryType);

				showPopupWithNoteFields(popup, title, entryType, "putBefore", selectedDbId);
			});
		}
	}

	return false;
}

/** Shows the popup for putting a new entry underneath another entry. */
function showPopupForPutUnderneathEntry() {
	if (!areCommandsAllowed) {
		return false;
	}

	if (!getFirstContainer(true)) {
		return false;
	}

	var title = uiText.popupTitleNewNoteUnderneath(getDefaultEntryTypes());

	if (getNumSelected(false, true) < 1) {
		showPopupForError(title, uiText.errorANoteMustBeSelected(getDefaultEntryTypes()));
	} else if (getNumSelected(false, true) > 1) {
		showPopupForError(title, uiText.errorOnlyOneNoteMayBeSelected(getDefaultEntryTypes()));
	} else if (areAnySelectedFromAList()) {
		showPopupForError(title, uiText.errorMaySelectFromList(getDefaultEntryTypes()));
	} else {
		var selectedDbId = getSelectedDbId(false, true);
		var entryType = getEntryType(selectedDbId);
		title = uiText.popupTitleNewNoteUnderneath(entryType);
		ensureDirectChildrenAreVisible(selectedDbId,
				startFastScrollTransition(selectedDbId, function() {
					var onCloseCallbacks = [removeTextEditingShortCuts];
					if (entryType === "notebook") {
						onCloseCallbacks.push(removeCreateNotebookPopupShortCuts);
					}
					var popup = createPopupForDialog(true, onCloseCallbacks);
					addTextEditingShortCuts(entryType);
					showPopupWithNoteFields(popup, title, entryType, "putUnderneath", selectedDbId);
				}),
				function() {
			showPopupForError(title, uiText.errorChildrenNeededToBeLoadedFirst(entryType));
		});
	}

	return false;
}


/** Shows the popup for putting a new notebook or forwards to https first. */
function showPopupForCreateNotebook() {
	if (!areCommandsAllowed) {
		return false;
	}

	forwardToHttpsIfAvailableOrShow("createNotebook=1", reallyShowPopupForCreateNotebook);
}

/** Adds shortcuts for create notebook. */
function addCreateNotebookPopupShortCuts() {
	Mousetrap.bind("enter", save);
}

/** Removes shortcuts for create notebook. */
function removeCreateNotebookPopupShortCuts() {
	Mousetrap.unbind("enter");
}

/** Shows the popup for putting a new notebook. */
function reallyShowPopupForCreateNotebook() {
	var popup = createPopupForDialog(true, [removeMakePublicOptionShortCuts, removeCreateNotebookPopupShortCuts]);
	addMakePublicOptionShortCuts();
	addCreateNotebookPopupShortCuts();

	showPopupWithNoteFields(popup, uiText.popupTitleCreateNotebook(), "notebook", "newNotebook");

	return false;
}

/** Returns the entry which is above the specified entry. */
function getAboveAloneEl(dbId) {
	var selectedEl = getSubtreeElByDbId(dbId);

	// First try up, then try left.
	if (doesSubtreeElHavePrevious(selectedEl)) {
		selectedEl = getPreviousOfSubtreeEl(selectedEl);
		while(true) {
			var children = getChildrenOfSubtreeEl(selectedEl);
			if(!children.length) {
				break;
			}
			selectedEl = children[children.length - 1];
		}
		
		return getAloneElFromSubtreeEl(selectedEl);
	} else if (doesSubtreeElHaveParent(selectedEl)) {
		return nullIfFakeAloneEl(getAloneElFromSubtreeEl(getParentOfSubtreeEl(selectedEl)));
	}
}

/** If the aloneEl is a fake return null. */
function nullIfFakeAloneEl(aloneEl) {
	if (isElementOfClass(aloneEl, "fakealone")) {
		return null;
	}

	return aloneEl;
}

/** Returns the entry which is below the specified entry. */
function getBelowAloneEl(dbId) {
	var selectedEl = getSubtreeElByDbId(dbId);
	// First try down, then try all parents' down.
	if (doesSubtreeElHaveNext(selectedEl)) {
		return getAloneElFromSubtreeEl(getNextOfSubtreeEl(selectedEl));
	}

	while (doesSubtreeElHaveParent(selectedEl)) {
		selectedEl = getParentOfSubtreeEl(selectedEl);
		if (isElementOfClass(selectedEl, "container")) {
			break;
		}

		if (doesSubtreeElHaveNext(selectedEl)) {
			return getAloneElFromSubtreeEl(getNextOfSubtreeEl(selectedEl));
		}
	}
}

/** Returns the entry which is righter than the specified entry. */
function getRighterAloneEl(dbId) {
	var selectedEl = getSubtreeElByDbId(dbId);
	// First try child, then down.
	if (doesSubtreeElHaveChildrenDisplayed(selectedEl)) {
		return getAloneElFromSubtreeEl(getChildrenOfSubtreeEl(selectedEl)[0]);
	}

	return getBelowAloneEl(dbId);
}

/** Returns true if the dbId is selected. */
function isDbIdSelected(dbId) {
	return (dbId in window.allSelectedDbIds);
}

/** Returns the topmost of the selected entries. */
function getFirstOfSelected() {
	setUpWindowForMousing();

	var first = null, firstY = Number.MAX_VALUE;
	for (var dbId in window.allSelectedDbIds) {
		var selectedEl = getAloneElByDbId(dbId);
		var y = selectedEl.getBoundingClientRect().bottom + getScrollTop();
		if (!first || y < firstY) {
			first = dbId;
			firstY = y;
		}
	}

	return first;
}

/** Returns the bottommost of the selected entries. */
function getLastOfSelected() {
	setUpWindowForMousing();

	var last = null, lastY = 0;
	for (var dbId in window.allSelectedDbIds) {
		var selectedEl = getAloneElByDbId(dbId);
		var y = selectedEl.getBoundingClientRect().bottom + getScrollTop();
		if (!last || y > lastY) {
			last = dbId;
			lastY = y;
		}
	}

	return last;
}

// Globals for note text editing.
var editedNoteDbId = null;
var oldNoteHtml = null;
var noteIsFocused = false;
var lastCaretPosition = null;
var lastDirection = null;

/** Immediately after the user presses the up or down button when a note has the focus check to see if the caret has been moved.
 * If the caret has not been moved then move the focus to the next or previous note. */
function justAfterUpOrDown() {
	if (noteIsFocused) {
		var aloneEl = getAloneElByDbId(editedNoteDbId);
		if (!aloneEl) {
			return;
		}

		var noteEl = getNoteElOfAloneEl(aloneEl); 
		var caretPosition = getCaretPosition(noteEl);
		if (lastCaretPosition === caretPosition) {
			var newSelection;
			if (lastDirection === "up") {
				newSelection = getAboveAloneEl(editedNoteDbId);
			} else if (lastDirection === "down") {
				newSelection = getRighterAloneEl(editedNoteDbId);
			}
	
			if (newSelection) {
				blurNote();
				
				selectAndScrollToAloneEl(newSelection, true);
			
				focusNoteAndSetCaret(newSelection);
			}
		}
	}
}

/** Moves the selection up or down. */
function moveSelection(direction) {
	if (noteIsFocused) {
		if (direction === "up" || direction === "down") {
			var isFirstOrLast = isCaretOnFirstOrLastLine();
			if (isFirstOrLast[0] || isFirstOrLast[1]) {
				var aloneEl = getAloneElByDbId(editedNoteDbId);
				if (!aloneEl) {
					return;
				}

				var noteEl = getNoteElOfAloneEl(aloneEl); 
				lastCaretPosition = getCaretPosition(noteEl);
				setTimeout(justAfterUpOrDown, 0);
				lastDirection = direction;
			}
		}
		
		return;
	}
	
	var newSelection;
	if (getNumSelected(true) < 1) {
		if (direction === "up") {
			newSelection = getEntryForUpWithNoSelection();
		} else if (direction === "down") {
			newSelection = getEntryForDownWithNoSelection();
		}
	} else if (getNumSelected(true) === 1) {
		if (direction === "up") {
			newSelection = getAboveAloneEl(getSelectedDbId(false, true));
		} else if (direction === "down") {
			newSelection = getRighterAloneEl(getSelectedDbId(false, true));
		}
	} else {
		if (direction === "up") {
			newSelection = getAboveAloneEl(getFirstOfSelected());
		} else if (direction === "down") {
			newSelection = getRighterAloneEl(getLastOfSelected());
		}
	}

	if (newSelection) {
		blurNote();
		
		selectAndScrollToAloneEl(newSelection, true);
	
		focusNoteAndSetCaret(newSelection);
	}
	
	return false;
}

/** Removes any selection from the page. */
function deselectAllSelections() {
    var selection = ('getSelection' in window) ? window.getSelection() :
        ('selection' in document) ? document.selection : null;
	  
    if ('removeAllRanges' in selection) {
        selection.removeAllRanges();
    } else if ('empty' in selection) {
        selection.empty();
    }
}

/** Focuses the note for the aloneEl and sets the caret. */
function focusNoteAndSetCaret(aloneEl, forceSetCaret) {
	// Because FireFox does not let you focus in an onfocus callback.
	setTimeout(function() {
		var noteEl = getNoteElOfAloneEl(aloneEl);
		if (!forceSetCaret && doesNoteHaveCaret(noteEl)) {
			return;
		}
		
		deselectAllSelections();
		
		if (noteEl) {
			noteEl.focus();
			setCaretPositionEnd(noteEl);
		}
	}, 0);
}
	
/** Selects the entry. */
function selectAloneEl(aloneEl, unselectOthers, toggleSelection) {
	if (unselectOthers) {
		unselectAllEntries();
	}

	var dbId = getDbIdFromEl(aloneEl);
	if(toggleSelection && isDbIdSelected(dbId)) {
		unselectEntry(dbId); 
	} else {
		selectEntry(dbId);
	}
	
	updateSelectionDisplayForAloneEl(aloneEl);
	stopBackgroundTransitionById(dbId);
}

/** Selects the aloneEl and scrolls to it. */
function selectAndScrollToAloneEl(aloneEl) {
	selectAloneEl(aloneEl, true);
	var dbId = getDbIdFromEl(aloneEl);
	startFastScrollTransition(dbId);
}

/** Selects the entry's parent instead. */
function selectTheEntrysParentInstead() {
	if (!getFirstContainer(true)) {
		return false;
	}

	var title = uiText.popupTitleViewParentEntry(getDefaultEntryTypes());
	if (getNumSelected(false, true) > 1) {
		showPopupForError(title, uiText.errorOnlyOneNoteMayBeSelected(getDefaultEntryTypes()));
		return false;
	}

	if (areAnySelectedFromAList()) {
		showPopupForError(title, uiText.errorMaySelectFromList(getDefaultEntryTypes()));
		return false;
	}

	var selectedDbId = getSelectedDbId(false, true);

	if (selectedDbId) {
		var selectedSubtreeEl = getSubtreeElByDbId(selectedDbId);
		if (selectedSubtreeEl && doesSubtreeElHaveParent(selectedSubtreeEl)) {
			var parentDbId = getDbIdFromSubtreeEl(getParentOfSubtreeEl(selectedSubtreeEl));

			selectAndScrollToAloneEl(getAloneElByDbId(parentDbId));
		} else {
			makeParentEntryMainOfTheContainer(getAloneElByDbId(selectedDbId));
		}
	}

	return false;
}

/** Returns the entry to display when nothing is selected and the user presses up. */
function getEntryForUpWithNoSelection() {
	var container = getFirstContainer();
	if (!container)
		return null;

	var aloneEls = container.getElementsByClassName("alone");
	
	// Skip the fake at the top.
	var aboveAloneEl;
	for (var i = 1; i < aloneEls.length; ++i) {
		var aloneEl = aloneEls[i];
		
		if (isEntryVisibleByEl(aloneEl)) {
			if (aboveAloneEl) {
				return aboveAloneEl;
			}
			
			return aloneEl;
		}
		
		aboveAloneEl = aloneEl;
	}

	return null;
}

/** Returns the entry to display when nothing is selected and the user presses down. */
function getEntryForDownWithNoSelection() {
	var container = getFirstContainer();
	if (!container)
		return null;

	var aloneEls = container.getElementsByClassName("alone");
	
	// Make sure the fake at the top is not selected.
	var belowAloneEl;
	for (var i = aloneEls.length - 1; i > 0; --i) {
		var aloneEl = aloneEls[i];
		if (isEntryVisibleByEl(aloneEl)) {
			if (belowAloneEl) {
				return belowAloneEl;
			}
			
			return aloneEl;
		}
		
		belowAloneEl = aloneEl;
	}

	return null;
}

/** Handles the shortcut that selects the insert as first child option for the create child popup. */
function selectCreateChildOptionInsertAsFirstChild() {
	var insertAsFirstChild = document.getElementById('insertAsFirstChild');
	insertAsFirstChild.checked = !insertAsFirstChild.checked;
	return false;
}

/** Handles the shortcut that selects the make public child option for the edit note popup. */
//Disable for now this feature needs usability improvements.
/*
function selectOptionMakePublic() {
	var isPublic = document.getElementById('isPublic');
	if (isPublic) {
		isPublic.checked = !isPublic.checked;
	}

    return false;
}
 */

/** Handles the shortcut that selects the move children to parent delete option. */
function selectDeleteOptionMoveToParent() {
	document.getElementById('childrenToParent').checked = true;
	return false;
}

/** Handles the shortcut that selects the orphan children delete option. */
function selectDeleteOptionOrphan() {
	document.getElementById('childrenToOrphan').checked = true;
	return false;
}

/** Handles the shortcut that selects the delete children delete option. */
function selectDeleteOptionDelete() {
	document.getElementById('childrenToDelete').checked = true;
	return false;
}

/** Removes shortcuts for making a notebook public. */
function removeMakePublicOptionShortCuts() {
	// Disable for now this feature needs usability improvements.
	/*
    Mousetrap.unbind("alt+a");
	 */
}

/** Removes text editing shortcuts. */
function removeTextEditingShortCuts() {
	removeMakePublicOptionShortCuts();

	if (getOptionSaveOnEnter()) {
		Mousetrap.unbind("ctrl+enter");
		Mousetrap.unbind("alt+enter");
		Mousetrap.unbind("enter");
	}
}

/** Inserts a line feed in the selected textarea. */
function insertEnterAtCursor(ev) {
	var textareaEl = getEventEl(ev);
	insertAtCursor(textareaEl, "\n");
}

/** Calls save and returns false so that when triggered from a keypress it does not propogate. */
function saveAndReturnFalse() {
	save();
	return false;
}

/** Adds shortcuts for making a notebook public. */
function addMakePublicOptionShortCuts() {
	// Disable for now this feature needs usability improvements.
	/*
    Mousetrap.bind("alt+a", selectOptionMakePublic);
	 */
}

/** Adds text editing shortcuts. */
function addTextEditingShortCuts(entryType) {
	if (entryType === "notebook") {
		addMakePublicOptionShortCuts();
		addCreateNotebookPopupShortCuts();
	}

	if (getOptionSaveOnEnter() && entryType !== "notebook") {
		Mousetrap.bind("ctrl+enter", insertEnterAtCursor);
		Mousetrap.bind("alt+enter", insertEnterAtCursor);
		Mousetrap.bind("enter", saveAndReturnFalse);
	}
}

/** Removes shortcuts to the create child popup. */
function removeCreateChildOptionsShortCuts() {
	Mousetrap.unbind("alt+f");
	removeTextEditingShortCuts();
}

/** Adds shortcuts to the create child popup. */
function addCreateChildOptionsShortCuts(entryType) {
	Mousetrap.bind("alt+f", selectCreateChildOptionInsertAsFirstChild);
	addTextEditingShortCuts(entryType);
}

/** Adds shortcuts from the delete entry popup. */
function addDeleteOptionsShortCuts() {
	Mousetrap.bind("p", selectDeleteOptionMoveToParent);

	Mousetrap.bind("r", selectDeleteOptionOrphan);

	Mousetrap.bind("d", selectDeleteOptionDelete);

	Mousetrap.bind("alt+p", selectDeleteOptionMoveToParent);

	Mousetrap.bind("alt+r", selectDeleteOptionOrphan);

	Mousetrap.bind("alt+d", selectDeleteOptionDelete);
}

/** Removes shortcuts from the delete entry popup. */
function removeDeleteOptionsShortCuts() {
	Mousetrap.unbind("p");
	Mousetrap.unbind("r");
	Mousetrap.unbind("d");
	Mousetrap.unbind("alt+p");
	Mousetrap.unbind("alt+r");
	Mousetrap.unbind("alt+d");
}

/** Returns a header for the help popup decorated with HTML. */
function decorateHelpHeader(text) {
	return "<span class=\"helpheader\">" + text + ":</span><br>";
}

/** Returns an HTML footer for a list of help items. */
function finishHelpItemsList() {
	return "<br>";
}

/** Returns HTML for a pretty key combo. */
function getPrettyKeys(keys) {
	keys = keys.replace(/\+{2}/g, '+plus');
	var combo = keys.split("+");

	var result = "";
	for (var i = 0; i < combo.length; ++i) {
		if (i !== 0) {
			result += " + ";
		}

		result += getPrettyKey(combo[i]);
	}

	return result;
}

/** Returns HTML for a pretty key. */
function getPrettyKey(key) {
	if (key === "Up") {
		key = "&uarr;";
	} else if (key === "Down") {
		key = "&darr;";
	} else if (key === "Left") {
		key = "&larr;";
	} else if (key === "Right") {
		key = "&rarr;";
	} else if (key === "Del") {
		key = "Delete";
	} else if (key === "plus") {
		key = "+";
	}

	return "<span class=\"key\">" + key + "</span>";
}

/** Returns help for shortcuts decorated with HTML. */
function decorateHelpShortcutDescription(keys, text) {
	return "<div class=\"keywrapperhelp\"><div><span class=\"keywrapperhelp\">" + getPrettyKeys(keys) + "</span> : </div><div>" + text + ".</div></div>";
}

/** Returns help for hints decorated with HTML. */
function decorateHelpHintDescription(text) {
	return "<span>&bull; " + text + ".</span><br>";
}

/** Shows the help popup. */
function showPopupForHelp() {
	if (!areCommandsAllowed) {
		return false;
	}

	var popup = createPopupForDialog(false);

	var html = decoratePopupTitle(uiText.popupTitleHelp());
	var commandMetaInfo = getCommandMetaInfo(getDefaultEntryTypes());
	for (var i = 0; i < commandMetaInfo.length; ++i) {
		var commandMetaInfoSection = commandMetaInfo[i];

		html += decorateHelpHeader(commandMetaInfoSection.header);

		var j;
		if ("commands" in commandMetaInfoSection) {
			for (j = 0; j < commandMetaInfoSection.commands.length; ++j) {
				var command = commandMetaInfoSection.commands[j];
				if ("description" in command) {
					html += decorateHelpShortcutDescription(getDisplayKeys(command), command.description);
				}
			}
		}

		if ("hints" in commandMetaInfoSection) {
			for (j = 0; j < commandMetaInfoSection.hints.length; ++j) {
				var hint = commandMetaInfoSection.hints[j];
				html += decorateHelpHintDescription(hint.text);
			}
		}

		if (i + 1 !== commandMetaInfo.length) {
			html += finishHelpItemsList();
		}
	}

	addThenCenterPopup(popup, html);

	return false;
}

/** Returns a context menu header decorated with HTML. */
function decorateContextMenuButtonHeader(text) {
	return "<span class=\"menubuttonheader\">" + text + ":</span><br>";
}

/** Returns an HTML footer for a list of context meny items. */
function finishContextMenuButtonsList() {
	return "<br>";
}

/** Returns a context menu button decorated with HTML. */
function decorateContextMenuButton(keys, text, functionName) {
	return "<button class=\"contextmenubutton\" onclick=\"closePopup(); " +
	functionName + "();\">" + text + "</button><span class=\"keywrappermenu\">" + getPrettyKeys(keys) + "</span><br>";
}

/** Returns metadata about each potential command.
 hints are not commands.
 Otherwise each item has a description, the keys for its shortcut, and function that implements the command.
 */
function getCommandMetaInfo(entryType) {
	return [
	        {
	        	"header" : uiText.helpBasicCommands(),
	        	"inContextMenu" : true,
	        	"worksOnLists": true,
	        	"commands" : [
	        	              {
	        	            	  "keys" : "Alt+c",
	        	            	  "description" : uiText.helpCreateSubnote(entryType),
	        	            	  "function" : showPopupForCreateChildEntry
	        	              },
	        	              {
	        	            	  "keys" : "F2",
	        	            	  "description" : uiText.helpEdit(entryType),
	        	            	  "function" : showPopupForEditEntry,
	        	            	  "worksOnLists": true,
	        	              },
	        	              {
	        	            	  "keys" : "Alt+Del",
	        	            	  "description" : uiText.helpDelete(entryType),
	        	            	  "function" : showPopupForDeleteEntry,
	        	            	  "worksOnLists": true
	        	              },
	        	              ]
	        },
	        {
	        	"header" : uiText.helpInlineEdit(),
	        	"inContextMenu" : false,
	        	"notGlobal" : true,
	        	"commands" : [
	        	              {
	        	            	  "keys" : "Alt+s",
	        	            	  "description" : uiText.helpSaveInlineEdit(),
	        	              },
	        	              {
	        	            	  "keys" : "Esc",
	        	            	  "description" : uiText.helpUndoInlineEdit(),
	        	              },
	        	              ]
	        },
	        {
	        	"header" : uiText.helpMouse(),
	        	"inContextMenu" : false,
	        	"hints" : uiText.helpMouseHints(entryType),
	        	"commands" : [
	        	              {
	        	            	  "keys" : "Alt+m",
	        	            	  "function" : handleKeyPressForContextMenu
	        	              }
	        	              ]
	        },
	        {
	        	"header" : uiText.helpAdvanced(),
	        	"inContextMenu" : true,
	        	"commands" : [
	        	              {
	        	            	  "keys" : "Alt+i",
	        	            	  "description" : uiText.helpInsert(entryType),
	        	            	  "function" : showPopupForInsertEntry
	        	              },
	        	              {
	        	            	  "keys" : "Alt+u",
	        	            	  "description" : uiText.helpUnderneath(entryType),
	        	            	  "function" : showPopupForPutUnderneathEntry
	        	              },
	        	              {
	        	            	  "keys" : "Alt+b",
	        	            	  "description" : uiText.helpBefore(entryType),
	        	            	  "function" : showPopupForPutBeforeEntry
	        	              },
	        	              {
	        	            	  "keys" : "Alt+a",
	        	            	  "description" : uiText.helpAfter(entryType),
	        	            	  "function" : showPopupForPutAfterEntry
	        	              } ]
	        },
	        {
	        	"header" : uiText.helpMove(entryType),
	        	"inContextMenu" : true,
	        	"commands" : [
	        	              {
	        	            	  "keys" : "Ctrl+Alt+Up",
	        	            	  "description" : uiText.helpMovePrevious(entryType),
	        	            	  "function" : moveEntriesBefore
	        	              },
	        	              {
	        	            	  "keys" : "Ctrl+Alt+Down",
	        	            	  "description" : uiText.helpMoveAfter(entryType),
	        	            	  "function" : moveEntriesAfter
	        	              },
	        	              {
	        	            	  "keys" : "Ctrl+Alt+Left",
	        	            	  "forceKeyUpOnIe": true,
	        	            	  "description" : uiText.helpMoveLeft(entryType),
	        	            	  "function" : moveEntriesLeft
	        	              },
	        	              {
	        	            	  "keys" : "Ctrl+Alt+Right",
	        	            	  "forceKeyUpOnIe": true,
	        	            	  "description" : uiText.helpMoveRight(entryType),
	        	            	  "function" : moveEntriesRight
	        	              },
	        	              /* Disable for now because probably no one will want this feature unless it is more polished
                        {
                            "keys" : "r",
                            "description" : uiText.helpMakeNotebook(),
                            "function" : showPopupForMakeEntryNotebook
                        }
	        	               */
	        	              ]
	        },
	        {
	        	"header" : uiText.helpShowOrHide(entryType),
	        	"inContextMenu" : true,
	        	"commands" : [
	        	              
	        	              {
	        	            	  "keys" : "Alt+Shift+=",
	        	            	  "displayKeys" : "Alt++",
	        	            	  "description" : uiText.helpShow(entryType),
	        	            	  "function" : showChildrenOfSelectedEntry
	        	              },
	        	              
	        	              {
	        	            	  "keys" : "Alt+-",
	        	            	  "forceKeyPressOnFireFox": true,
	        	            	  "description" : uiText.helpHide(entryType),
	        	            	  "function" : hideChildrenOfSelectedEntry
	        	              },
	        	              /* Comment these out until they work better.
                        {
                            "keys" : "p",
                            "description" : uiText.helpShowParent(entryType),
                            "function" : selectTheEntrysParentInstead
                        },
                        {
                            "keys" : "t",
                            "description" : uiText.helpMakeMain(entryType),
                            "function" : makeEntryMainOfTheContainer
                        }
	        	               */
	        	              ]
	        },
	        {
	        	"header" : uiText.helpSelect(entryType),
	        	"inContextMenu" : false,
	        	"commands" : [
	        	              {
	        	            	  "keys" : "Esc",
	        	            	  "description" : uiText.helpUnselectAllOrDismiss(entryType),
	        	            	  "function": escapeHandler
	        	              },
	        	              {
	        	            	  "keys" : "Up",
	        	            	  "description" : uiText.helpSelectAbove(entryType),
	        	            	  "function" : moveSelectionUp
	        	              },
	        	              {
	        	            	  "keys" : "Down",
	        	            	  "description" : uiText.helpSelectBelow(entryType),
	        	            	  "function" : moveSelectionDown
	        	              }
	        	              ],
	        } ];
}

/** Returns a function that can be used by the context menu for its buttons. */
function createContextMenuFunc(helper) {
	return function() {
		closePopup();
		helper();
		return false;
	};
}

/** Removes the shortcuts for the context menu. */
function removeContextMenuShortCuts() {
	var commandMetaInfo = getCommandMetaInfo(getDefaultEntryTypes());
	for (var i = 0; i < commandMetaInfo.length; ++i) {
		var commandMetaInfoSection = commandMetaInfo[i];
		if (commandMetaInfoSection.inContextMenu) {
			for (var j = 0; j < commandMetaInfoSection.commands.length; ++j) {
				var command = commandMetaInfoSection.commands[j];
				var actionAndForced = getKeyActionAndForced(command);
				Mousetrap.unbind(command.keys.toLowerCase(), actionAndForced[0], actionAndForced[1]);
			}
		}
	}
}

/** Shows the context menu popup. */
function showPopupForContextMenu() {
	if (!areCommandsAllowed) {
		return false;
	}

	var title = uiText.popupTitleContextMenu();

	var popup = createPopupForDialog(true, [removeContextMenuShortCuts]);
	var html = decoratePopupTitle(title);

	html += uiText.sentenceContextMenuHelp();

	var selectedDbId = getSelectedDbId(true);
	if (!selectedDbId) {
		return false;
	}

	var entryType = getEntryType(selectedDbId);
	var aloneEl = getAloneElByDbId(selectedDbId);
	var paneEl = getContainingPaneEl(aloneEl);
	var paneIndex = getIndexOfPaneEl(paneEl);
	var isATree = isPaneATree(paneIndex);

	var commandMetaInfo = getCommandMetaInfo(entryType);
	var haveAddedAny = false;
	for (var i = 0; i < commandMetaInfo.length; ++i) {
		var commandMetaInfoSection = commandMetaInfo[i];
		if (commandMetaInfoSection.inContextMenu && (isATree || commandMetaInfoSection.worksOnLists)) {
			if (haveAddedAny) {
				html += finishContextMenuButtonsList();
			}

			haveAddedAny = true;

			html += decorateContextMenuButtonHeader(commandMetaInfoSection.header);

			for (var j = 0; j < commandMetaInfoSection.commands.length; ++j) {
				var command = commandMetaInfoSection.commands[j];
				if (isATree || command.worksOnLists) {
					html += decorateContextMenuButton(getDisplayKeys(command), command.description,
							getFunctionName(command["function"]));
					var actionAndForced = getKeyActionAndForced(command);
					Mousetrap.bind(command.keys.toLowerCase(),
							createContextMenuFunc(command["function"]), actionAndForced[0], actionAndForced[1]);
				}
			}
		}
	}

	addThenCenterPopup(popup, html);

	return false;
}

/** Returns the display keys for the command. */
function getDisplayKeys(command) {
	if ("displayKeys" in command) {
		return command.displayKeys;
	}
	
	return command.keys;
}

/** Return the name of a function. */
function getFunctionName(func) {
	if (func.name)
		return func.name;

	// For IE.
	return (func.toString().match(/function (.+?)\(/)||[,''])[1];
}

/** Moves the selection up. */
function moveSelectionUp() {
	return moveSelection("up");
}

/** Moves the selection down. */
function moveSelectionDown() {
	return moveSelection("down");
}

/** Returns an array with the key action and whether it should be forced. */
function getKeyActionAndForced(command) {
	if (command.forceKeyPressOnFireFox && isFireFox) {
		return [ "keypress", true ];
	}
	
	if (command.forceKeyUpOnIe && isIe) {
		return [ "keyup", true ];
	}
	
	var undef;
	return [ undef, false ];
}			

/** Adds global shortcuts. */
function addGlobalShortCuts() {
	var commandMetaInfo = getCommandMetaInfo(getDefaultEntryTypes());
	for (var i = 0; i < commandMetaInfo.length; ++i) {
		var commandMetaInfoSection = commandMetaInfo[i];
		if (("commands" in commandMetaInfoSection) && !("notGlobal" in commandMetaInfoSection)) {
			for (var j = 0; j < commandMetaInfoSection.commands.length; ++j) {
				var command = commandMetaInfoSection.commands[j];
				var actionAndForced = getKeyActionAndForced(command);
				Mousetrap.bind(command.keys.toLowerCase(),
						command["function"], actionAndForced[0], actionAndForced[1]);
			}
		}
	}
}

/** Adds shortcuts for all popups. */
function addPopupShortCuts() {
	Mousetrap.bind("alt+s", save);
}

/** Removes shortcuts for all popups. */
function removePopupShortCuts() {
	Mousetrap.unbind("alt+s");
}

/** Removes global shortcuts. */
function removeGlobalShortCuts() {
	var commandMetaInfo = getCommandMetaInfo(getDefaultEntryTypes());
	for (var i = 0; i < commandMetaInfo.length; ++i) {
		var commandMetaInfoSection = commandMetaInfo[i];

		if (("commands" in commandMetaInfoSection) && !("notGlobal" in commandMetaInfoSection)) {
			for (var j = 0; j < commandMetaInfoSection.commands.length; ++j) {
				var command = commandMetaInfoSection.commands[j];
				var actionAndForced = getKeyActionAndForced(command);
				Mousetrap.unbind(command.keys.toLowerCase(), actionAndForced[0], actionAndForced[1]);
			}
		}
	}
}

/** Converts the hex color to an 3 array of ints. */
function hexToRgb(hex) {
	var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
	return result ? [
	                 parseInt(result[1], 16),
	                 parseInt(result[2], 16),
	                 parseInt(result[3], 16) ]
	: null;
}

/** Overrides the background for the id to the specified rgba. */
function setOverrideBackgroundById(id, rgb, a) {
	var el = document.getElementById(id);
	if (!el) {
		return;
	}

	el.style["background-color"] = "rgba(" + rgb[0] + ", " + rgb[1] + ", "+ rgb[2] + ", " + a + ")";
}

/** Clears the overrides for the background for the id. */
function clearOverrideBackgroundById(id) {
	var el = document.getElementById(id);
	if (!el) {
		return;
	}

	el.style["background-color"] = "";
}

var backgroundTransitions = {};

function stopBackgroundTransitionById(id) {
	if (id in backgroundTransitions) {
		clearInterval(backgroundTransitions[id]);
		delete backgroundTransitions[id];
		clearOverrideBackgroundById(id);
	}
}

var defaultTransitionUpdateInterval = 50;

/** Creates a transition animation for the element and color. updateIntervalMs has a default. */
function startBackgroundTransition(id, color, durationMs, updateIntervalMs) {
	updateIntervalMs = (typeof updateIntervalMs === 'undefined') ? defaultTransitionUpdateInterval : updateIntervalMs;

	if (id in backgroundTransitions) {
		clearInterval(backgroundTransitions[id]);
	}

	var startMs = new Date().getTime();

	var startColorRgb = hexToRgb(color);

	setOverrideBackgroundById(id, startColorRgb, 100);

	backgroundTransitions[id] = window.setInterval(function() {
		var endMs = startMs + durationMs;
		var currentMs = new Date().getTime();
		if (currentMs >= endMs) {
			stopBackgroundTransitionById(id);
			return;
		}

		var elapsedMs = currentMs - startMs;
		var percentThrough = Math.floor((elapsedMs / durationMs) * 100);
		setOverrideBackgroundById(id, startColorRgb, (100 - percentThrough)/100);
	}, updateIntervalMs);
}

var scrollTransition = null;
var scrollTransitionHasCallback = false;

/** Stops the scroll transition if it has no callback. */
function stopScrollTransition() {
	if (!scrollTransitionHasCallback) {
		forceStopScrollTransition();
	}
}

/** Stops the scroll transition no matter what. */
function forceStopScrollTransition() {
	clearInterval(scrollTransition);
	scrollTransition = null;
	scrollTransitionHasCallback = false;
}

/** Returns true if the entry is at least partially visible. */
function isEntryVisible(dbId) {
	var aloneEl = getAloneElByDbId(dbId);
	if (!aloneEl) {
		return false;
	}

	return isEntryVisibleByEl(aloneEl);
}

/** Returns true if the entry is at least partially visible. */
function isEntryVisibleByEl(aloneEl) {
	var targetPos = getPosition(aloneEl);
	var targetHeight = parseInt(aloneEl.offsetHeight);

	var scrollTop = getScrollTop();
	var windowHeight = getWindowInnerHeight();
	var scrollBottom = scrollTop + windowHeight;
	var targetLogicalYStart = scrollTop + targetPos.y;
	var targetLogicalYEnd = targetLogicalYStart + targetHeight;

	return (targetLogicalYStart > scrollTop && targetLogicalYStart < scrollBottom) ||
	(targetLogicalYEnd > scrollTop && targetLogicalYEnd < scrollBottom) ||
	(targetLogicalYStart < scrollTop && targetLogicalYEnd > scrollBottom);
}

/** Returns a better scrolltop if there is one or null. */
function getBetterScrolltopForImprovedVisibility(targetPos, targetHeight) {
	var scrollTop = getScrollTop();
	var windowHeight = getWindowInnerHeight();
	var targetLogicalYStart = scrollTop + targetPos.y;
	var targetLogicalYEnd = targetLogicalYStart + targetHeight;
	var padding = 20;

	// Need to scroll down to see the top of the element.
	if (targetLogicalYStart > scrollTop + windowHeight) {
		return (targetLogicalYEnd + padding) - windowHeight;
	}

	// Need to scroll up to see the top of the element.
	if (targetLogicalYStart < scrollTop) {
		return targetLogicalYStart - padding;
	}

	// Need to scroll down to see the bottom of the element.
	if (targetHeight <= windowHeight) {
		if (targetLogicalYEnd > scrollTop + windowHeight) {
			return (targetLogicalYEnd + padding) - windowHeight;
		}
	}

	return null;
}

/** Creates a transition scroll for the dbId. defaulting the duration. */
function startFastScrollTransition(dbId, callback) {
	startScrollTransition(dbId, 500, undefined, callback);
}

/** Scrolls the window to pad the mouse. */
function scrollToPadMouse(mousePos) {
	var newScrollLeft = getScrollLeft();
    var scrollAmount = 20;
	if (mousePos.x - newScrollLeft <= 0) {
		newScrollLeft += mousePos.x - newScrollLeft - scrollAmount;
	} else if (getWindowInnerWidth() - (mousePos.x - newScrollLeft) <= 40) {
		newScrollLeft += scrollAmount;
	}

	var newScrollTop = getScrollTop();
	if (mousePos.y - newScrollTop <= 0) {
		newScrollTop += mousePos.y - newScrollTop - scrollAmount;
	} else if (getWindowInnerHeight() - (mousePos.y - newScrollTop) <= 40) {
		newScrollTop += scrollAmount;
	}

	if (newScrollLeft < 0) {
		newScrollLeft = 0;
	}

	if (newScrollTop < 0) {
		newScrollTop = 0;
	}

	var change = { "x": newScrollLeft - getScrollLeft(), "y": newScrollTop - getScrollTop() };
	if (change.x || change.y) {
		window.scroll(newScrollLeft, newScrollTop);
	}
	
	return change;
}

/** Creates a transition scroll for the dbId.  updateIntervalMs has a default. */
function startScrollTransition(dbId, durationMs, updateIntervalMs, callback) {
	updateIntervalMs = (updateIntervalMs === null || typeof updateIntervalMs === 'undefined') ? defaultTransitionUpdateInterval : updateIntervalMs;

	forceStopScrollTransition();

	var startMs = new Date().getTime();

	var onInterval = function() {
		var aloneEl = getAloneElByDbId(dbId);
		if (!aloneEl) {
			forceStopScrollTransition();
			if (callback) {
				callback();
			}
			return false;
		}

		var targetPos = getPosition(aloneEl);
		var targetHeight = parseInt(aloneEl.offsetHeight);
		var betterScrolltop = getBetterScrolltopForImprovedVisibility(targetPos, targetHeight);
		if (betterScrolltop === null) {
			forceStopScrollTransition();
			if (callback) {
				callback();
			}
			return false;
		}

		var currentMs = new Date().getTime();
		var elapsedMs = currentMs - startMs;
		var percentThrough = (elapsedMs / durationMs);

		var scrollTop = getScrollTop();
		var distance = Math.max(scrollTop, betterScrolltop) - Math.min(scrollTop, betterScrolltop);
		var traveled = Math.ceil(distance * percentThrough);
		var newScrollTop = 0;
		if (betterScrolltop < scrollTop) {
			newScrollTop = scrollTop - traveled;
		} else {
			newScrollTop = scrollTop + traveled;
		}

		window.scroll(getScrollLeft(), newScrollTop);

		return true;
	};

	if (!onInterval())
		return;

	if (callback) {
		scrollTransitionHasCallback = true;
	}

	scrollTransition = window.setInterval(onInterval, updateIntervalMs);
}

/** Returns the maximum levels of children shown. */
function getMaximumLevelsOfChildrenShown(subtreeEl) {
	var maxChildLevels = 0;
	var children = getChildrenOfSubtreeEl(subtreeEl);
	for (var i = 0; i < children.length; ++i) {
		var childLevels = getMaximumLevelsOfChildrenShown(children[i]);
		if (childLevels > maxChildLevels) {
			maxChildLevels = childLevels;
		}
	}

	return maxChildLevels + (children.length ? 1 : 0);
}

/** Returns the minimum levels of children shown.
 * Answers the question: how many levels should be retrieved on the next 'plus'. */
function getNextLevelToShow(subtreeEl) {
	var children = getChildrenOfSubtreeEl(subtreeEl);
	if (!children.length && getEntryHasChildren(getDbIdFromSubtreeEl(subtreeEl))) {
		return 1;
	}

	var i;
	var child;
	for (i = 0; i < children.length; ++i) {
		child = children[i];
		var childsChildren = getChildrenOfSubtreeEl(child);
		if (!childsChildren.length && getEntryHasChildren(getDbIdFromSubtreeEl(child))) {
			return 2;
		}
	}

	var minChildLevels = 0;
	for (i = 0; i < children.length; ++i) {
		child = children[i];
		var minLevels = getNextLevelToShow(child);
		if (minLevels !== 0 && (minLevels < minChildLevels || minChildLevels === 0)) {
			minChildLevels = minLevels;
		}
	}

	return minChildLevels ? minChildLevels + 1 : 0;
}

/** Shows or hides the plus icon as appropriate from the specified subtree up to the root of the container. */
function showOrHidePlusUpToRoot(subtreeEl) {
	var nextLevelToShow = 0;

	var isFirst = true;
	while (true) {
		if (nextLevelToShow === 0) {
			nextLevelToShow = getNextLevelToShow(subtreeEl);
		}

		var aloneEl = getAloneElFromSubtreeEl(subtreeEl);
		if (isElementOfClass(aloneEl, "fakealone")) {
			break;
		}

		if (nextLevelToShow !== 0) {
			if (!isFirst && isShowingPlus(aloneEl)) {
				break;
			}
			
			showPlusIcon(aloneEl);
		} else {
			if (!isFirst && !isShowingPlus(aloneEl)) {
				break;
			}
			
			hidePlusIcon(aloneEl);
		}

		if (!doesSubtreeElHaveParent(subtreeEl)) {
			break;
		}

		subtreeEl = getParentOfSubtreeEl(subtreeEl);
		isFirst = false;
	}
}

/** Shows plus icons up to the root of the container. */
function showPlusUpToRoot(subtreeEl) {
	while (true) {
		var aloneEl = getAloneElFromSubtreeEl(subtreeEl);
		if (isElementOfClass(aloneEl, "fakealone")) {
			break;
		}

		if (isShowingPlus(aloneEl)) {
			break;
		}
		
		showPlusIcon(aloneEl);

		if (!doesSubtreeElHaveParent(subtreeEl)) {
			break;
		}

		subtreeEl = getParentOfSubtreeEl(subtreeEl);
	}
}

/** Recursively expands to the level requests. Returns true if any requests were made. */
function expandToLevel(subtreeEl, levelsToShow, currentLevel) {
	var children = getChildrenOfSubtreeEl(subtreeEl);
	if (!children.length && getEntryHasChildren(getDbIdFromSubtreeEl(subtreeEl))) {
		requestChildren(subtreeEl, levelsToShow - currentLevel);
		return true;
	}

	var requestedAny = false;
	if (levelsToShow > currentLevel + 1) {
		for (var i = 0; i < children.length; ++i) {
			var child = children[i];
			if (getEntryHasChildren(getDbIdFromSubtreeEl(child))) {
				requestedAny |= expandToLevel(child, levelsToShow, currentLevel + 1);
			}
		}
	}

	return requestedAny;
}

var afterRequestChildrenSuccessCallback = null;
var afterRequestChildrenFailureCallback = null;

/** Ensures that direct children are visible and then calls the callback.
 * You can not call this in a loop unless the caller verified that every request has children that must be loaded.
 * The callback can be called too early. */
function ensureDirectChildrenAreVisible(dbId, successCallback, failureCallback) {
	var subtreeEl = getSubtreeElByDbId(dbId);
	afterRequestChildrenSuccessCallback = successCallback;
	afterRequestChildrenFailureCallback = failureCallback;
	if (!expandToLevel(subtreeEl, 1, 0) && successCallback) {
		successCallback();
	}
}

var numRequestsInFlight = 0;

/** Requests the children for the subtree. */
function requestChildren(subtreeEl, levels) {
	++numRequestsInFlight;

	var dbId = getDbIdFromSubtreeEl(subtreeEl);
	var entryType = getEntryType(dbId);
	var xhr = createAsyncRequest("GET", "/noteChildrenJson/?id=" + dbId + "&levels=" + levels +
			"&" + getAnUrlUniquer(), function() {
		var success = false;
		if (xhr.status === 200) {
			var response = validateResponseForGettingChildren(xhr.responseText, uiText.errorNoteNotFound(entryType), uiText.popupTitleViewChildrenEntry(entryType));
			if (response) {
				var childrenEl = getChildrenElOfSubtreeEl(subtreeEl);

				var tempElement = document.createElement('DIV');
				tempElement.innerHTML = response.childrenHtml;
				var childNodes = copyArray(tempElement.childNodes);
				for (var i = 0; i < childNodes.length; ++i) {
					var newSubtreeEl = childNodes[i];
					newSubtreeEl.parentNode.removeChild(newSubtreeEl);

					childrenEl.appendChild(newSubtreeEl);
					formatDatetimesInSubtreeAndChildren(newSubtreeEl);
					showOrHideDatetimesInSubtreeAndChildren(newSubtreeEl);
					fixDivTitlesInSubtreeAndChildren(newSubtreeEl, getEntryType(dbId));
					makeNotesInTreeContentEditable(newSubtreeEl);
				}

				addToEntryInfoDict(response.entryInfoDict);
				setTheRightChildIconsForAloneEl(getAloneElFromSubtreeEl(subtreeEl));
				fixPlusIcons(subtreeEl);
				showOrHidePlusUpToRoot(getParentOfSubtreeEl(subtreeEl));	
				success = true;
			}
		} else {
			var errorText = getErrorTextNotFound(xhr, uiText.errorNoteNotFound(entryType));
			showPopupForError(uiText.popupTitleViewChildrenEntry(entryType), errorText);
		}

		--numRequestsInFlight;
		if (!numRequestsInFlight) {
			aRequestIsInProgress(false);

			var copyOfSuccessCallback = afterRequestChildrenSuccessCallback;
			afterRequestChildrenSuccessCallback = null;
			var copyOfFailureCallback = afterRequestChildrenFailureCallback;
			afterRequestChildrenFailureCallback = null;
			
			if (success && copyOfSuccessCallback) {
				copyOfSuccessCallback();
			}
			
			if (!success && copyOfFailureCallback) {
				copyOfFailureCallback();
			}
		}
	});

	xhr.send();
}

/** Shows the children of the selected note. */
function showChildrenOfSelectedEntry() {
	if (!getFirstContainer(true)) {
		return false;
	}

	var title = uiText.popupTitleViewChildrenEntry(getDefaultEntryTypes());
	if (getNumSelected(false, true) > 1) {
		showPopupForError(title, uiText.errorOnlyOneNoteMayBeSelected(getDefaultEntryTypes()));
		return false;
	}

	if (areAnySelectedFromAList()) {
		showPopupForError(title, uiText.errorMaySelectFromList(getDefaultEntryTypes()));
		return false;
	}

	showChildrenOfEntry(getSelectedDbId(false, true));
	return false;
}

/** Shows the children of the note. */
function showChildrenOfEntry(dbId) {
	var title;
	if (!dbId) {
		title = uiText.popupTitleViewChildrenEntry(getDefaultEntryTypes());
		showPopupForError(title, uiText.errorANoteMustBeSelected(getDefaultEntryTypes()));
		return;
	}

	var entryType = getEntryType(dbId);
	title = uiText.popupTitleViewChildrenEntry(entryType);
	var subtreeEl = getSubtreeElByDbId(dbId);

	var levelsToShow = getNextLevelToShow(subtreeEl);
	if (levelsToShow === 0) {
		showPopupForError(title, uiText.errorThereAreNoMoreChildrenToShow(entryType));
		return;
	}

	afterRequestChildrenSuccessCallback = function () {
		setTheRightChildIconsForAloneEl(getAloneElFromSubtreeEl(subtreeEl));
	};
	
	afterRequestChildrenFailureCallback = null;
	
	expandToLevel(subtreeEl, levelsToShow, 0);

	aRequestIsInProgress(true);
}

/** Shows the plus icon. */
function showPlusIcon(aloneEl) {
	var plusIcon = getPlusFromAloneEl(aloneEl);
	if (plusIcon) {
		plusIcon.style.visibility = "visible";
	}
}

/** Returns true if the plus icon for the alone el is being shown. */
function isShowingPlus(aloneEl) {
	var plusIcon = getPlusFromAloneEl(aloneEl);
	return plusIcon && plusIcon.style.visibility === "visible";
}

/** Hides the plus icon. */
function hidePlusIcon(aloneEl) {
	var plusIcon = getPlusFromAloneEl(aloneEl);
	if (plusIcon) {
		plusIcon.style.visibility = "hidden";
	}
}

/** Shows the minus icon. */
function showMinusIcon(aloneEl) {
	var minusIcon = getMinusFromAloneEl(aloneEl);
	if (minusIcon) {
		minusIcon.style.visibility = "visible";
	}
}

/** Hides the minus icon. */
function hideMinusIcon(aloneEl) {
	var minusIcon = getMinusFromAloneEl(aloneEl);
	if (minusIcon) {
		minusIcon.style.visibility = "hidden";
	}
}

/** Recursively hides children if they are deeper than the new maximum level to show and updates the parents plus icon. */
function hideChildrenAfterMaximumLevel(subtreeEl, newMaximumLevelsShown, currentLevel) {
	hideChildrenAfterMaximumLevelHelper(subtreeEl, newMaximumLevelsShown, currentLevel);

	while (true) {
		var aloneEl = getAloneElFromSubtreeEl(subtreeEl);
		if (isElementOfClass(aloneEl, "alone") && !isElementOfClass(aloneEl, "fakealone")) {
			if (getEntryHasChildren(getDbIdFromEl(aloneEl))) {
				showPlusIcon(aloneEl);
			}
		}

		if (!doesSubtreeElHaveParent(subtreeEl))
			break;

		subtreeEl = getParentOfSubtreeEl(subtreeEl);
	}
}

/** Recursively hides children if they are deeper than the new maximum level to show. */
function hideChildrenAfterMaximumLevelHelper(subtreeEl, newMaximumLevelsShown, currentLevel) {
	var children = copyArray(getChildrenOfSubtreeEl(subtreeEl));
	for (var i = 0; i < children.length; ++i) {
		var child = children[i];
		if (newMaximumLevelsShown === currentLevel) {
			handleRemovalOfSubtreeAndChildren(child);
			removeSubtreeElFromParent(child);
		} else {
			hideChildrenAfterMaximumLevel(child, newMaximumLevelsShown, currentLevel + 1);
		}
	}

	var aloneEl = getAloneElFromSubtreeEl(subtreeEl);
	setTheRightChildIconsForAloneEl(aloneEl);
	if (getEntryHasChildren(getDbIdFromEl(aloneEl))) {
		showPlusIcon(aloneEl);
	}
}

/** Hides the children of the selected note. */
function hideChildrenOfSelectedEntry() {
	if (!getFirstContainer(true)) {
		return false;
	}

	var title = uiText.popupTitleHideChildrenEntry(getDefaultEntryTypes());

	if (getNumSelected(false, true) > 1) {
		showPopupForError(title, uiText.errorOnlyOneNoteMayBeSelected(getDefaultEntryTypes()));
		return false;
	}

	if (areAnySelectedFromAList()) {
		showPopupForError(title, uiText.errorMaySelectFromList(getDefaultEntryTypes()));
		return false;
	}

	hideChildrenOfEntry(getSelectedDbId(false, true));
	return false;
}

/** Hides the children of the selected note. */
function hideChildrenOfEntry(dbId, showNone) {
	var title;
	if (!dbId) {
		title = uiText.popupTitleHideChildrenEntry(getDefaultEntryTypes());
		showPopupForError(title, uiText.errorANoteMustBeSelected(getDefaultEntryTypes()));
		return false;
	}

	var entryType = getEntryType(dbId);
	title = uiText.popupTitleHideChildrenEntry(entryType);
	var subtreeEl = getSubtreeElByDbId(dbId);

	var maximumLevelsShown = getMaximumLevelsOfChildrenShown(subtreeEl);

	if (maximumLevelsShown > 0) {
		var newMaximumLevelsShown = (typeof showNone === 'undefined') ? maximumLevelsShown - 1 : 0;
		hideChildrenAfterMaximumLevel(subtreeEl, newMaximumLevelsShown, 0);
	} else {
		showPopupForError(title, uiText.errorThereAreNoMoreChildrenToHide(entryType));
	}

	return false;
}

/** Handles plus icon clicks. */
function plusOnMouseDown(ev) {
	if (!areCommandsAllowed) {
		return;
	}

	var eventEl = getEventEl(ev);
	var clickedAloneEl = getCorrespondingAloneEl(eventEl);
	if (!clickedAloneEl) {
		return;
	}

	showChildrenOfEntry(getDbIdFromEl(clickedAloneEl));
}

/** Handles minus icon clicks. */
function minusOnMouseDown(ev) {
	if (!areCommandsAllowed) {
		return;
	}

	var eventEl = getEventEl(ev);
	var clickedAloneEl = getCorrespondingAloneEl(eventEl);
	if (!clickedAloneEl) {
		return;
	}

	hideChildrenOfEntry(getDbIdFromEl(clickedAloneEl));
}

/** Inserts a string at the cursor position of a textarea or at the end if there is no position, without losting the position.
 * From http://stackoverflow.com/questions/11076975/insert-text-into-textarea-at-cursor-position-javascript */
function insertAtCursor(myField, myValue) {
	//IE support
	if (document.selection) {
		myField.focus();
		var sel = document.selection.createRange();
		sel.text = myValue;
	}
	//MOZILLA and others
	else if (myField.selectionStart || myField.selectionStart === '0') {
		var startPos = myField.selectionStart;
		var endPos = myField.selectionEnd;
		myField.value = myField.value.substring(0, startPos) +
		myValue + myField.value.substring(endPos, myField.value.length);
		myField.selectionStart = startPos + myValue.length;
		myField.selectionEnd = startPos + myValue.length;
	} else {
		myField.value += myValue;
	}
}

/** Shows the help popup. */
function showPopupForOptions() {
	if (!areCommandsAllowed) {
		return false;
	}

	var html = decoratePopupTitle(uiText.popupTitleOptions());

	html += "<input type=\"checkbox\" name=\"showTimestamps\" id=\"showTimestamps\"" + (getOptionShowEntryTimestamps() ? " checked" : "") + "><label for=\"showTimestamps\">" + uiText.labelShowTimestamps() + "</label><br>";
	html += "<input type=\"checkbox\" name=\"saveOnEnter\" id=\"saveOnEnter\"" + (getOptionSaveOnEnter() ? " checked" : "") + "><label for=\"saveOnEnter\">" + uiText.labelSaveOnEnter() + "</label><br>";
	html += endOfForm("saveOptions");
	var popup = createPopupForDialog(false);

	addThenCenterPopup(popup, html);

	return false;
}

/** Gets the save on enter options value. */
function getOptionSaveOnEnter() {
	if (!("saveOnEnter" in window.optionsDict)) {
		return false;
	}

	return window.optionsDict.saveOnEnter;
}

/** Sets the save on enter options value. */
function setOptionSaveOnEnter(value) {
	window.optionsDict.saveOnEnter = value;
}

/** Gets the show timestamps options value. */
function getOptionShowEntryTimestamps() {
	if (!("showTimestamps" in window.optionsDict)) {
		return false;
	}

	return window.optionsDict.showTimestamps;
}

/** Sets the show timestamps options value. */
function setOptionShowEntryTimestamps(value) {
	window.optionsDict.showTimestamps = value;
}

/** Show or hide any timestamps. */
function showOrHideEntryTimestamps(container) {
	var display = getOptionShowEntryTimestamps() ? "block" : "none";
	var entryDayTimes = container.getElementsByClassName("entryDaytime");
	for (var i = 0; i < entryDayTimes.length; ++i) {
		entryDayTimes[i].style.display = display;
	}
}

/** Show or hide a timestamp. */
function showOrHideEntryTimestamp(el) {
	var display = getOptionShowEntryTimestamps() ? "block" : "none";
	el.style.display = display;
}

/** Shows or hides all datetimes in subtree recursively. */
function showOrHideDatetimesInSubtreeAndChildren(subtreeEl) {
	var aloneEl = getAloneElFromSubtreeEl(subtreeEl);
	showOrHideDatetimesIfNeeded(aloneEl);

	var children = getChildrenOfSubtreeEl(subtreeEl);
	for (var i = 0; i < children.length; ++i) {
		showOrHideDatetimesInSubtreeAndChildren(children[i]);
	}
}

/** Formats the datetime in the aloneEl if needed. */
function showOrHideDatetimesIfNeeded(aloneEl) {
	if (!isElementOfClass(aloneEl, "alone") || isElementOfClass(aloneEl, "fakealone")) {
		return;
	}

	var entryDaytimeEl = getEntryDaytimeElOfAloneEl(aloneEl);
	if (entryDaytimeEl && entryDaytimeEl) {
		showOrHideEntryTimestamp(entryDaytimeEl);
	}
}

/** Handles the save button clicks of the options popup. */
function saveOptions() {
	toggleEnabledInputForPopup();

	var xhr = createJsonAsyncRequest("POST", "/saveOptions?" + getAnUrlUniquer(), function() {
		aRequestIsInProgress(false);

		if (xhr.status === 200) {
			document.getElementById("response").innerHTML = "<span class=\"successMessage\">" + uiText.sentenceSuccessfullySaved() + "</span>";

			setOptionSaveOnEnter(isElementChecked('saveOnEnter'));

			var oldOptionShowEntryTimestamps = getOptionShowEntryTimestamps();
			setOptionShowEntryTimestamps(isElementChecked('showTimestamps'));
			var newOptionShowEntryTimestamps = getOptionShowEntryTimestamps();

			if (oldOptionShowEntryTimestamps !== newOptionShowEntryTimestamps) {
				var allPanes = getAllPanesEl();
				for (var i = 1; i < allPanes.childNodes.length; ++i) {
					var paneContainer = allPanes.childNodes[i].childNodes[0];
					var paneEl = getPaneElFromPaneContainer(paneContainer);
					var container = getContainerFromPaneEl(paneEl);
					if (container) {
						showOrHideEntryTimestamps(container);
					}
				}
			}

			setTimeout(function() {
				closePopup();
			}, 500);
		} else {
			var errorText = getErrorText(xhr, uiText.errorNotSaved(), uiText.errorProbablyNotSaved());
			setResponseErrorMessage(errorText);
			toggleEnabledInputForPopup();
		}
	});

	var message = { 'csrft': getCsrft() };
	copyCheckBoxToMessageIfTrue(message, 'showTimestamps');
	copyCheckBoxToMessageIfTrue(message, 'saveOnEnter');

	document.getElementById("response").innerHTML = uiText.sentenceSaving();
	xhr.send(JSON.stringify(message));
	aRequestIsInProgress(true);
}

/** Handles click for the triangle icon in alone els. */
function triangleOnMouseDown(ev) {
	if (!areCommandsAllowed) {
		return;
	}

	var eventEl = getEventEl(ev);
	var clickedAloneEl = getCorrespondingAloneEl(eventEl);
	if (!clickedAloneEl) {
		return;
	}

	var clickedDbId = getDbIdFromEl(clickedAloneEl);
	if (getEntryHasChildren(clickedDbId)) {
		if (doesSubtreeElHaveChildrenDisplayed(getSubtreeElForAloneEl(clickedAloneEl))) {
			hideChildrenOfEntry(clickedDbId, true);
		} else {
			showChildrenOfEntry(clickedDbId);
		}
	}
}

/** Handles mouseover for the plus icon. */
function plusOnMouseOver(ev) {
	var eventEl = getEventEl(ev);
	eventEl.src = "/images/plusborder.png";
}

/** Handles mouseout for the plus icon. */
function plusOnMouseOut(ev) {
	var eventEl = getEventEl(ev);
	eventEl.src = "/images/plus.png";
}

/** Handles mouseover for the minus icon. */
function minusOnMouseOver(ev) {
	var eventEl = getEventEl(ev);
	eventEl.src = "/images/minusborder.png";
}

/** Handles mouseout for the minus icon. */
function minusOnMouseOut(ev) {
	var eventEl = getEventEl(ev);
	eventEl.src = "/images/minus.png";
}

/** Fixes all plus icons. */
function fixAllPlusIcons(container) {
	for (var i = 0; i < container.childNodes[1].childNodes.length; ++i) {
		var subtreeEl = container.childNodes[1].childNodes[i];
		fixPlusIcons(subtreeEl);
	}
}

/** Handles mouseover for the pencil icon. */
function panePencilOnMouseOver(ev) {
	var eventEl = getEventEl(ev);
	var paneEl = getContainingPaneEl(eventEl);
	var paneIndex = getIndexOfPaneEl(paneEl);
	var paneType = getPaneType(paneIndex);
	if (isPaneEditable(paneIndex) && paneType === "notebooks") {
		eventEl.style["background-image"] = "url(/images/pencilgreendark.png)";
	} else {
		eventEl.style["background-image"] = "url(/images/pencildark.png)";
	}
}

/** Handles mouseout for the pencil icon. */
function panePencilOnMouseOut(ev) {
	var eventEl = getEventEl(ev);
	var paneEl = getContainingPaneEl(eventEl);
	var paneIndex = getIndexOfPaneEl(paneEl);
	var paneType = getPaneType(paneIndex);
	if (isPaneEditable(paneIndex) && paneType === "notebooks") {
		eventEl.style["background-image"] = "url(/images/pencilgreen.png)";
	} else {
		eventEl.style["background-image"] = "url(/images/pencil.png)";
	}
}

function getRightTriPngUrl() { return "/images/righttri.png"; }
function getTri90PngUrl() { return "/images/tri90.png"; }
function getMinusPngUrl() { return "/images/minus.png"; }
function getPlusPngUrl() { return "/images/plus.png"; }
function getRightTriBorderPngUrl() { return "/images/righttriborder.png"; }
function getTri90BorderPngUrl() { return "/images/tri90border.png"; }
function getMinusBorderPngUrl() { return "/images/minusborder.png"; }
function getPlusBorderPngUrl() { return "/images/plusborder.png"; }
function getFatPlusPngUrl() { return "/images/fatplus.png"; }
function getPencilPngUrl() { return "/images/pencil.png"; }
function getTrashPngUrl() { return "/images/trash.png"; }
function getPencilDarkPngUrl() { return "/images/pencildark.png"; }
function getTrashDarkPngUrl() { return "/images/trashdark.png"; }
function getPencilWhitePngUrl() { return "/images/pencilwhite.png"; }
function getTrashWhitePngUrl() { return "/images/trashwhite.png"; }
function getPencilGreenPngUrl() { return "/images/pencilgreen.png"; }
function getPencilGreenDarkPngUrl() { return "/images/pencilgreendark.png"; }
function getNotebookPngUrl() { return "/images/notebook.png"; }
function getXPngUrl() { return "/images/x.png"; }
function getXDarkPngUrl() { return "/images/x.png"; }
function getRefreshPngUrl() { return "/images/refresh.png"; }
function getDragPngUrl() { return "/images/drag.png"; }
function getDragMultPngUrl() { return "/images/dragmult.png"; }
function getExportPngUrl() { return "/images/export.png"; }
function getExportDarkPngUrl() { return "/images/exportdark.png"; }

new Image().src = getTri90PngUrl();
new Image().src = getRightTriPngUrl();
new Image().src = getPlusPngUrl();
new Image().src = getMinusPngUrl();
new Image().src = getTri90BorderPngUrl();
new Image().src = getRightTriBorderPngUrl();
new Image().src = getPlusBorderPngUrl();
new Image().src = getMinusBorderPngUrl();
new Image().src = getFatPlusPngUrl();
new Image().src = getPencilPngUrl();
new Image().src = getTrashPngUrl();
new Image().src = getPencilDarkPngUrl();
new Image().src = getTrashDarkPngUrl();
new Image().src = getPencilWhitePngUrl();
new Image().src = getTrashWhitePngUrl();
new Image().src = getPencilGreenPngUrl();
new Image().src = getPencilGreenDarkPngUrl();
new Image().src = getNotebookPngUrl();
new Image().src = getXPngUrl();
new Image().src = getXDarkPngUrl();
new Image().src = getRefreshPngUrl();
new Image().src = getDragPngUrl();
new Image().src = getDragMultPngUrl();
new Image().src = getExportPngUrl();
new Image().src = getExportDarkPngUrl();

/** Called after a page is finished loading. */
function onFinishFullPageLoad() {
	uiText = uiTextEn;
	displayIeLt8Message();
	makeShortcutsWorkInIe();
	window.onpopstate = handlePopStateGetPage;
	window.oncontextmenu = onContextMenu;
	// So that clicking anywhere that is not an entry unselects all entries.
	document.onmousedown = documentOnMouseDown;
	document.onmouseover = documentOnMouseOver;
	document.ontouchend = documentOnTouchEnd;
	window.onresize = recenterPopupWindow;

	addGlobalShortCuts();
	onFinishPageReload();
	registerCommandKeyCallbacks();

	if (window.location.search === "?signIn=1") {
		if (window.sessionDict.isSignedIn) {
			alreadySignedIn();
		} else {
			signIn();
		}
	} else if (window.location.search === "?createAccount=1") {
		if (window.sessionDict.isSignedIn) {
			alreadySignedIn();
		} else {
			createAccount();
		}
	} else if (window.location.search === "?createNotebook=1") {
		reallyShowPopupForCreateNotebook();
	}
}

/** Registers keyup and keydown for the apple command key. */
function registerCommandKeyCallbacks() {
	Mousetrap.bind('command', function() {
		isCommandKeyPressed = true;
		return true;
	}, 'keydown');

	Mousetrap.bind('command', function() {
		isCommandKeyPressed = false;
		return true;
	}, 'keyup');
}

/** Does all the things needed to fix up the formatting for a complete tree of entries. */
function fixContainer(container, entryInfoDictEl, paneIndex) {
	var isTree = isPaneATree(paneIndex);
	var entryInfoDict = addEntryDictEl(entryInfoDictEl, !isTree);
	formatDatetimesInContainer(container);

	if (isTree) {
		updateAllHasChildrenIcons(entryInfoDict);
		showOrHideEntryTimestamps(container);
		fixAllPlusIcons(container);
	}

	var paneType = getPaneType(paneIndex);
	if (paneType) {
		var entryType = null;
		if (paneType === "notebooks") {
			entryType = "notebook";
		} else if (paneType === "notebook" || paneType === "notes" ||
				paneType === "quotations" || paneType === "source") {
			entryType = "note";
		} else if (paneType === "sources") {
			entryType = "source";
		} else if (paneType === "accounts") {
			entryType = "account";
		}

		if (entryType) {	
			fixDivTitlesInSubtreeAndChildren(container, entryType);
		}
		
		makeNotesInTreeContentEditable(container, paneType);
	}
}

/** Returns the element for the pane within a pane container. */
function getPaneElFromPaneContainer(el) {
	return el.childNodes[0];
}

/** Returns the title div within the pane element. */
function getTitleDivFromPaneEl(paneEl) {
	return paneEl.childNodes[0].childNodes[0];
}

/** Returns the edit icon within the pane element. */
function getEditIconFromPaneEl(paneEl) {
	var editIcons = paneEl.childNodes[0].getElementsByClassName("editIcon");
	if (!editIcons || !editIcons.length) {
		return null;
	}

	return editIcons[0];
}

/** Returns the delete icon within the pane element. */
function getDeleteIconFromPaneEl(paneEl) {
	var deleteIcons = paneEl.childNodes[0].getElementsByClassName("deleteIcon");
	if (!deleteIcons || !deleteIcons.length) {
		return null;
	}

	return deleteIcons[0];
}

/** Returns the entry container within the pane element if it exists. */
function getContainerFromPaneEl(paneEl) {
	for (var i = 0; i < paneEl.childNodes[1].childNodes.length; ++i) {
		var child = paneEl.childNodes[1].childNodes[i];
		if (isElementOfClass(child, "container")) {
			return child;
		}
	}

	return null;
}

/** Returns the info text within the pane element if it exists. */
function getInfoTextFromPaneEl(paneEl) {
	for (var i = 0; i < paneEl.childNodes[1].childNodes.length; ++i) {
		var child = paneEl.childNodes[1].childNodes[i];
		if (isElementOfClass(child, "infotext")) {
			return child;
		}
	}

	return null;
}

/** Returns the entry info within the pane element if it exists. */
function getEntryInfoDictFromPaneEl(paneEl) {
	for (var i = 0; i < paneEl.childNodes[1].childNodes.length; ++i) {
		var child = paneEl.childNodes[1].childNodes[i];
		if (isElementOfClass(child, "entryInfoDictJson")) {
			return child;
		}
	}

	return null;
}

/** Returns the entry info within the pane element if it exists. */
function getMetaDataDictFromPaneEl(paneEl) {
	if (paneEl.childNodes.length < 2) {
		return null;
	}

	for (var i = 0; i < paneEl.childNodes[1].childNodes.length; ++i) {
		var child = paneEl.childNodes[1].childNodes[i];
		if (isElementOfClass(child, "metaDataDictJson")) {
			return child;
		}
	}

	return null;
}

/** Fixes all panes in the page so that their views are correct. */
function loadAllPanes() {
	var allPanes = getAllPanesEl();
	for (var i = 1; i < allPanes.childNodes.length; ++i) {
		var paneContainer = allPanes.childNodes[i].childNodes[0];
		var paneEl = getPaneElFromPaneContainer(paneContainer);
		onPaneLoad(paneEl);
	}
}

/** Returns all of containers in the page. */
function getAllContainers() {
	var allPanes = getAllPanesEl();
	var containers = [];
	for (var i = 1; i < allPanes.childNodes.length; ++i) {
		var paneContainer = allPanes.childNodes[i].childNodes[0];
		var paneEl = getPaneElFromPaneContainer(paneContainer);
		var container = getContainerFromPaneEl(paneEl);
		if (container) {
			containers.push(container);
		}
	}

	return containers;
}

/** Returns true if any pane has a container. */
function doAnyContainerPanesExist() {
	var allPanes = getAllPanesEl();
	for (var i = 1; i < allPanes.childNodes.length; ++i) {
		var paneContainer = allPanes.childNodes[i].childNodes[0];
		var paneEl = getPaneElFromPaneContainer(paneContainer);
		if (getContainerFromPaneEl(paneEl)) {
			return true;
		}
	}

	return false;
}

/** Returns the el for the pane corresponding to the index.
 * 0 is the first real pane, not the index. */
function getPaneElByIndex(index) {
	var allPanes = getAllPanesEl();
	var paneContainer = allPanes.childNodes[index + 1].childNodes[0];
	var paneEl = getPaneElFromPaneContainer(paneContainer);
	return paneEl;
}

/** Unloads any containers in the panes. */
function unloadAllPanes() {
	var allPanes = getAllPanesEl();
	for (var i = 1; i < allPanes.childNodes.length; ++i) {
		var paneContainer = allPanes.childNodes[i].childNodes[0];
		var paneEl = getPaneElFromPaneContainer(paneContainer);
		onPaneUnload(paneEl);
	}
}

/** Called after a page is finished reloading. */
function onFinishPageReload() {
	noRequestsAreInProgress();
	resetOptionsDict();
	resetSessionDict();
	updateAllDatesAndTimes(document);
	loadAllPanes();
	
	// Because when FireFox refreshes it tries to keep checkboxes checked.
	var checkboxes = document.getElementsByClassName("aloneCheckbox");
    for (var i = 0; i < checkboxes.length; ++i) {
		checkboxes[i].checked = false;
	}
}

/** This is an unreliable indicator of whether the device has a touch screen.
 * Don't use it for anything very important.
 */
function mightHaveTouch() {
	return 'ontouchstart' in window;
}

/** Called when a pane loads. */
function onPaneLoad(paneEl) {
	fixPaneHeader(paneEl);

	var paneIndex = getIndexOfPaneEl(paneEl);

	var container = getContainerFromPaneEl(paneEl);
	if (container) {
		fixContainer(container, getEntryInfoDictFromPaneEl(paneEl), paneIndex);
	}

	if (mightHaveTouch()) {
		var touchInfoText = getPaneTouchInfoText(paneIndex);
		if (touchInfoText) {
			var infoText = getInfoTextFromPaneEl(paneEl);
			if (infoText) {
				infoText.innerHTML = touchInfoText;
			}
		}
	}

	updateAllDatesAndTimes(paneEl);
}

/** Fixes the pane when it is loaded to have the right tooltips. */
function fixPaneHeader(paneEl) {
	var paneIndex = getIndexOfPaneEl(paneEl);
	var paneType = getPaneType(paneIndex);

	var editIcon = getEditIconFromPaneEl(paneEl);
	if (editIcon) {
		editIcon.setAttribute("title", uiText.tooltipPaneEdit(paneType));
	}

	var deleteIcon = getDeleteIconFromPaneEl(paneEl);
	if (deleteIcon) {
		deleteIcon.setAttribute("title", uiText.tooltipPaneDelete(paneType));
	}
}

/** Called when a pane is unloaded. */
function onPaneUnload(paneEl) {
	var container = getContainerFromPaneEl(paneEl);
	if (container) {
		removeAllEntriesFromContainer(container);
	}
}

/** Returns the first container on the page. */
function getFirstContainer(mustBeTree) {
	var allPanes = getAllPanesEl();
	for (var i = 1; i < allPanes.childNodes.length; ++i) {
		var paneIndex = i - 1;
		if (!isPaneEditable(paneIndex)) {
			continue;
		}

		var paneContainerEl = allPanes.childNodes[i].childNodes[0];
		var paneEl = getPaneElFromPaneContainer(paneContainerEl);
		if (mustBeTree && !isPaneATree(paneIndex)) {
			continue;
		}

		var container = getContainerFromPaneEl(paneEl);
		if (container) {
			return container;
		}
	}

	return null;
}

/** Shows the popup for creating a child of the root note. */
function newSubNote(ev) {
	if (!areCommandsAllowed) {
		return false;
	}

	var eventEl = getEventEl(ev);
	var paneEl = getContainingPaneEl(eventEl);
	var containerEl = getContainerFromPaneEl(paneEl);
	var aloneEl = getAloneElFromSubtreeEl(containerEl);
	var rootId = getDbIdFromEl(aloneEl);

	var entryType = "note";

	var title = uiText.popupTitleCreateChildNote(entryType);

	var popup = createPopupForDialog(true, [removeCreateChildOptionsShortCuts]);
	addCreateChildOptionsShortCuts(entryType);
	var extraHtml = getInsertAsFirstChildHtml(entryType, rootId);
	showPopupWithNoteFields(popup, title, entryType, "createChild", rootId, extraHtml);

	return false;
}

/** Handle clicking the pencil icon on a pane. */
function panePencilOnClick(ev) {
	var eventEl = getEventEl(ev);
	var paneEl = getContainingPaneEl(eventEl);
	var title = getTitleForPane(paneEl);
	var paneIndex = getIndexOfPaneEl(paneEl);
	var paneType = getPaneType(paneIndex);

	if (paneType === "notebook") {
		showPopupForEditNotebook(paneEl.id, title, paneIndex);
	} else if (paneType === "source") {
		showPopupForEditSource(paneEl.id, title, paneIndex);
	} else if (paneType === "notebooks") {
		var containerEl = getContainerFromPaneEl(paneEl);
		if (isPaneEditable(paneIndex)) {
			eventEl.style["background-image"] = "url(/images/pencil.png)";
			setPaneIsEditable(paneIndex, false);
			var fakeJustChildrenEl = getFakeJustChildrenFromContainer(containerEl);
			var subtreeEl = fakeJustChildrenEl.childNodes[0];
			unselectSubtreeAndChildren(subtreeEl);
		} else {
			eventEl.style["background-image"] = "url(/images/pencilgreen.png)";
			setPaneIsEditable(paneIndex, true);
		}

		fixDivTitlesInSubtreeAndChildren(containerEl, "notebook");
	}
}

/** Handle clicking the delete icon on a notebook. */
function paneTrashOnClick(ev) {
	var eventEl = getEventEl(ev);
	var paneEl = getContainingPaneEl(eventEl);
	var paneIndex = getIndexOfPaneEl(paneEl);
	var paneType = getPaneType(paneIndex);
	if (paneType === "notebook") {
		showPopupForDeleteNotebook(paneEl.id, paneIndex);
	} else if (paneType === "source") {
		showPopupForDeleteSource(paneEl.id, paneIndex);
	}
}

/** Handle clicking the export icon on a notebook. */
function paneExportOnClick(ev) {
	var eventEl = getEventEl(ev);
	var paneEl = getContainingPaneEl(eventEl);
	showPopupForExportNotebook(paneEl.id);
}

/** Shows or hides the menu. */
function showOrHideMenu(onlyHide) {
	var menu = document.getElementById("menu");
	var children = menu.childNodes;

	var top = children[0];
	if (onlyHide && top.style["border-bottom"] === "") {
		return;
	}

	if (top.style["border-bottom"] !== "") {
		top.style["border-bottom"] = "";
	} else {
		top.style["border-bottom"] = "1px solid #DDD";
	}

	for (var i = 1; i < children.length; ++i) {
		var child = children[i];
		if (!child.style) {
			continue;
		}

		if (child.style.display === "block") {
			child.style.display = "";
		} else {
			child.style.display = "block";
		}
	}
}

/** Makes every note in the element content editable if the pane is right and so is the platform. */
function makeNotesInTreeContentEditable(el, paneType) {
	if(!paneType) {
		var paneEl = getContainingPaneEl(el);
		var paneIndex = getIndexOfPaneEl(paneEl);
		paneType = getPaneType(paneIndex);
	}
	
	if (paneType === "notebooks" || mightHaveTouch() || !window.getSelection || !document.createRange) {
		return;
	}

	var noteEls = el.getElementsByClassName("note");
	for (var i = 0; i < noteEls.length; ++i) {
		var noteEl = noteEls[i];
		noteEl.setAttribute("contentEditable", true);
		attachEventListener(noteEl, "focus", noteOnFocus);
		attachEventListener(noteEl, "blur", noteOnBlur);
		attachEventListener(noteEl, "input", noteOnInput);
	}
}

/** Undo the unsaved edit to a note text. */
function undoNoteEdit(dbId, htmlToReplaceWith) {
	if (htmlToReplaceWith === null) {
		return;
	}
	
	var aloneEl = getAloneElByDbId(dbId);
	if (!aloneEl) {
		return;
	}
	
	var noteEl = getNoteElOfAloneEl(aloneEl); 
	noteEl.innerHTML = htmlToReplaceWith;
}

/** Clear the note text editing globals. */
function clearNoteFromEditing() {
	oldNoteHtml = null;
	editedNoteDbId = null;
}

/** Handle the user focusing on note text either by
 * clicking on it or dragging into it.
 */
function noteOnFocus(ev) {
	noteIsFocused = true;
	addInlineNoteEditShortCuts();
	var eventEl = getEventEl(ev);
	var aloneEl = getCorrespondingAloneEl(eventEl);
	prepareNoteForInlineEdit(aloneEl);
	removeTheHoverMenu();
}

/** Handle loss of focus on the note text either by clicking out of it
 *  or programmatically.
 */
function noteOnBlur() {
	noteIsFocused = false;
	if (!editedNoteDbId) {
		return;
	}
	
	// This is a hack because when a popup is shown the note is blurred after the popup is shown.
	if (!isPopupUp()) {
		removeInlineNoteEditShortCuts();
	}
	
	saveNoteTextAfterInlineEdit();
}

/** Handle input to the note.
 * Can by by keypress, drag, cut or paste.
 * Usually noteOnFocus() is called before this.
 * If input is dragged or pasted into the note noteOnFocus() it is not called first,
 * but in that case the note does not have the focus.
 * And sometimes noteOnFocus() is not called even though the note has the focus.
 * It seems like a browser bug.
 */
function noteOnInput() {
	var aloneEl = getAloneElByDbId(editedNoteDbId);
	if (!aloneEl) {
		return;
	}

	prepareNoteForInlineEdit(aloneEl);

	var noteEl = getNoteElOfAloneEl(aloneEl); 
	// In case HTML was dragged into the note replace it.
	if (noteEl.childNodes.length !== 1 || (noteEl.childNodes.length === 1 && noteEl.childNodes[0].nodeName === "#text")) {
		var caretPosition;
		if (noteIsFocused) {
			caretPosition = getCaretPosition(noteEl);
		}
		
		var newNoteHtml = getHtmlOfEl(noteEl);
		noteEl.innerHTML = newNoteHtml;
		
		if (noteIsFocused) {
			setCaretPosition(noteEl, caretPosition);
		}
	}
	
	if (!noteIsFocused) {
		saveNoteTextAfterInlineEdit();
	}
}

/** Prepare a note text for editing by stashing a copy of it. */
function prepareNoteForInlineEdit(aloneEl) {
	var dbId = getDbIdFromEl(aloneEl);
	if (dbId === editedNoteDbId) {
		return;
	}
	
	var noteEl = getNoteElOfAloneEl(aloneEl); 
	oldNoteHtml = getHtmlOfEl(noteEl);
	editedNoteDbId = dbId;
}

/** Encode characters into HTML. */
function htmlEncode(value) {
	return value.
		replace(/&/g, "&amp;").
    	replace(/</g, "&lt;").
    	replace(/>/g, "&gt;").
    	replace(/\n/g, "<br>").
    	replace(/ /g, "&nbsp;");
}

/** Returns the preish HTML text of a simple element tree. */
function getHtmlOfEl(el) {
	var result = "";
	for (var i = 0; i < el.childNodes.length; i++) {
        var child = el.childNodes[i];
        if (child.nodeName === "#text") {
        	result += htmlEncode(child.nodeValue);
        } else if (child.nodeName === "BR") {
        	result += "<br>";
        } else {
        	result += getHtmlOfEl(child);
        	if (child.nodeName === "P") {
        		result += "<br>";
        	}
        }
	}
	
	return result;
}

/** Returns the preish text of a simple element tree. */
function getTextOfEl(el) {
	var result = "";
	for (var i = 0; i < el.childNodes.length; i++) {
        var child = el.childNodes[i];
        if (child.nodeName === "#text") {
        	result += child.nodeValue.replace(/&nbsp;/g, " ").replace(/\u00A0/g, " ");
        } else if (child.nodeName === "BR") {
        	result += "\n";
        } else {
        	result += getTextOfEl(child);
        	if (child.nodeName === "P") {
        		result += "\n";
        	}
        }
	}
	
	return result;
}

/** Handle pressing Escape while inline editing a note. */
function noteEscapeHandler() {
	if (!noteIsFocused) {
		return;
	}
		
	undoNoteEdit(editedNoteDbId, oldNoteHtml);
	var aloneEl = getAloneElByDbId(editedNoteDbId);
	if (!aloneEl) {
		return;
	}
	
	var noteEl = getNoteElOfAloneEl(aloneEl); 
	noteEl.blur();
	
	// Because otherwise FireFox will propogate this event to a handler that has not even been registered yet. 
	return false;
}

/** Handle blurring a note while inline editing it. */
function blurNote() {
	if (!noteIsFocused) {
		return;
	}
		
	var aloneEl = getAloneElByDbId(editedNoteDbId);
	if (!aloneEl) {
		return;
	}
	
	var noteEl = getNoteElOfAloneEl(aloneEl); 
	noteEl.blur();
	
	// Because otherwise FireFox will propogate this event to a handler that has not even been registered yet. 
	return false;
}
	
/** Adds shortcuts for inline note text editing. */
function addInlineNoteEditShortCuts() {
	Mousetrap.bind("alt+s", blurNote);
}

/** Removes shortcuts for inline note text editing. */
function removeInlineNoteEditShortCuts() {
	Mousetrap.unbind("alt+s");
}

/** Sets the caret position within an el tree. */
function setCaretPosition(el, position) {
	var length = 0;
	var previousLength = 0;
	for (var i = 0; i < el.childNodes.length; ++i) {
		var child = el.childNodes[i];
		previousLength = length;
	    if (child.nodeName === "#text") {
	    	length += child.nodeValue.length + 1;
	    } else if (child.nodeName === "BR") {
	    	length += 1;
	    }

	    if(length > position) {
	    	position = position - previousLength;
	    	setCaretPositionHelper(child, position);
	    	return;
	    }
	}
}

/** Sets the caret position to the end of an el tree. */
function setCaretPositionEnd(el) {
	if (el.childNodes.length === 0) {
    	var textNode = document.createTextNode("");
   		el.appendChild(textNode);
	}
	
	var child = el.childNodes[el.childNodes.length - 1];
	var length = 1;
    if (child.nodeName === "#text") {
    	length = child.nodeValue.length;
    }

   	setCaretPositionHelper(child, length);
}

/** Sets the caret position within an el. */
function setCaretPositionHelper(el, position) {
    if (window.getSelection && document.createRange) {
        var sel = window.getSelection();
        sel.removeAllRanges();
        var range = document.createRange();
        if(isFireFox && el.nodeName === "BR") {
        	var textNode = document.createTextNode("");
       		el.parentNode.insertBefore(textNode, el);
        	el = textNode;
        }
        
	    range.setStart(el, position);
	    range.setEnd(el, position);
        sel.addRange(range);
    }
}

/** Gets the caret position within an el tree. */
function getCaretPosition(el) {
	var caretPos = 0;
	var range;
	if (window.getSelection) {
		var selection = window.getSelection();
		if (selection.rangeCount) {
			range = selection.getRangeAt(0);
			if (range.commonAncestorContainer.nodeName === "#text") {
				caretPos = range.endOffset + getCaretPositionHelper(el, range.startContainer);
			} else {
				var startNode = range.startContainer;
				if (range.startContainer.childNodes.length !== 0) {
					startNode = range.startContainer.childNodes[range.endOffset];
				}
				
				caretPos = getCaretPositionHelper(el, startNode);
			}
		}
	}
	
	return caretPos;
}

/** Returns true if the note el has a caret. */
function doesNoteHaveCaret(noteEl) {
	if (window.getSelection) {
		var selection = window.getSelection();
		if (selection.rangeCount) {
			var range = selection.getRangeAt(0);
			if (range) {
				var noteElWithFocus = getElOrAncestor(range.startContainer, 'DIV', 'note', '.alone');
				return noteElWithFocus === noteEl;
			}
		}
	}
	
	return false;
}

/** Gets whether the caret position within an el tree is on the first or last line. */
function isCaretOnFirstOrLastLine() {
	var result = [ false, false ];
	var range;
	if (window.getSelection) {
		var selection = window.getSelection();
		if (selection.rangeCount) {
			range = selection.getRangeAt(0);
			if (range.commonAncestorContainer.nodeName === "#text") {
				result = isCaretOnFirstOrLastLineHelper(range.startContainer);
			} else {
				var startNode = range.startContainer;
				if (range.startContainer.childNodes.length !== 0) {
					startNode = range.startContainer.childNodes[range.endOffset];
				}
				
				result = isCaretOnFirstOrLastLineHelper(startNode);
			}
		}
	}
	
	return result;
}

/** Gets the caret position within an el. */
function getCaretPositionHelper(stopAt, el) {
	if (stopAt === el || !el) {
		return 0;
	}
	
	var result = 0;
	for (var i = 0; i < el.parentNode.childNodes.length; i++) {
        var child = el.parentNode.childNodes[i];
        if (child === el) {
        	break;
        }
        
       	result += getCaretPositionsInEl(child);
	}

	return result + getCaretPositionHelper(stopAt, el.parentNode);
}

/** Gets whether the caret position is on the first and last line. */
function isCaretOnFirstOrLastLineHelper(el) {
     return [ !el.previousSibling, !el.nextSibling ];
}

/** Gets the logical length of an el in terms of cursor positions. */
function getCaretPositionsInEl(el) {
    if (el.nodeName === "#text") {
    	return el.nodeValue.length + 1;
    } else if (el.nodeName === "BR") {
        return 1;
    }

	var result = 0;
	for (var i = 0; i < el.childNodes.length; i++) {
        var child = el.childNodes[i];
       	result += getCaretPositionsInEl(child);
	}

	return result;
}

var notesBeingSaved = {};

/** Save a note text after inline editing. */
function saveNoteTextAfterInlineEdit() {
	if (editedNoteDbId in notesBeingSaved) {
		return;
	}
	
	var aloneEl = getAloneElByDbId(editedNoteDbId);
	if (!aloneEl) {
		return;
	}
	
	var noteEl = getNoteElOfAloneEl(aloneEl);
	var dbId = editedNoteDbId;
	var newNoteHtml = getHtmlOfEl(noteEl);
	var newNoteText = getTextOfEl(noteEl);
	
	var doNotSave = false;
	if (oldNoteHtml === newNoteHtml) {
		doNotSave = true;
	}
	
	var entryType = getEntryType(dbId);
	var title = uiText.popupTitleEditTheSelectedNote(entryType);
	if (!doNotSave) {
		if (newNoteText.trim() === "") {
			showPopupForError(title, uiText.errorNoteMustNotBeEmpty());
			doNotSave = true;
		}
	}
	
	if (doNotSave) {
		undoNoteEdit(dbId, oldNoteHtml);
		clearNoteFromEditing();
		return;
	}
	
	var copyOfOldNoteHtml = oldNoteHtml;
	var xhr = createJsonAsyncRequest("POST", "/noteOpJson?" + getAnUrlUniquer(), function() {
		aRequestIsInProgress(false);
		commandsAreNowAllowed(true);
		delete notesBeingSaved[dbId];
		var aloneEl = getAloneElByDbId(dbId);
		var noteEl;
		if (aloneEl) {
			noteEl = getNoteElOfAloneEl(aloneEl); 
			noteEl.setAttribute("contentEditable", true);
			noteEl.style.cursor = "";
		}
		
		if (xhr.status === 200) {
			var response = validateInlineEditResponse(xhr.responseText, title);
			if (!response) {
				undoNoteEdit(dbId, copyOfOldNoteHtml);
				return;
			}
			
			updateDisplayedEntryDetails(response);

			setEntryInfo(response.id, response.note, response.quotation, response.isPublic, response.type, getEntryHasChildren(response.id));

			var paneForEntryEl = document.getElementById(response.id);
			if (paneForEntryEl) {
				var pageTitleDiv = getTitleDivFromPaneEl(paneForEntryEl);
				pageTitleDiv.innerHTML = response.noteHtml;
				updatePaneTitle(getIndexOfPaneEl(paneForEntryEl), response.note);
			}

			refreshSearchPane();
			
			if (isIe && noteEl && doesNoteHaveCaret(noteEl)) {
				deselectAllSelections();
			}
		} else {
			undoNoteEdit(dbId, copyOfOldNoteHtml);
			var errorText = getErrorText(xhr, uiText.errorNotSaved(), uiText.errorProbablyNotSaved());
			showPopupForError(title, errorText);
		}
	});

	noteEl.setAttribute("contentEditable", false);
	noteEl.style.cursor = "progress";
	clearNoteFromEditing();
	notesBeingSaved[editedNoteDbId] = true;
	var message = { 'csrft': getCsrft(),
			'noteop': 'editNoteText',
			'id': dbId,
			'note': newNoteText };
	xhr.send(JSON.stringify(message));
	aRequestIsInProgress(true);
	commandsAreNowAllowed(false);
}

/** A hacky way of making sure that IE shortcuts work even if there are browser defaults. */
function makeShortcutsWorkInIe() {
	if (!isIe) {
		return;
	}

	var buttons = ["s", "c", "a"];
	for (var i = 0; i < buttons.length; ++i) {
		var button = document.createElement("BUTTON");
		button.className = "hiddenButton";
		button.setAttribute("accesskey", buttons[i]);
		button.setAttribute("tabindex", "-1");
		document.body.appendChild(button);
	}
}

/** Allow dragging from the Chrome extension to a note. */
function onDragOverAloneEl(ev) {
    // Because ev.dataTransfer.getData() does not work correctly in Chrome in this callback.
	var data = ev.dataTransfer;
	if (!data.types || !data.types.length || data.types[0] !== "quotationid") {
		return;
	}

	ev.preventDefault();
}

/** Handle dropping from the Chrome extension into a note. */
function onDropAloneEl(ev) {
	var eventEl = getEventEl(ev);

	var aloneEl = getCorrespondingAloneEl(eventEl);
	if (!aloneEl) {
		return;
	}

	var quotationId = ev.dataTransfer.getData("quotationid");
	if (!quotationId) {
		return;
	}

	commandsAreNowAllowed(false);
	makeEntriesSiblingOrChild(aloneEl, [ ":" + quotationId ], true, "/makeChildrenJson", uiText.popupTitleDragNodeToNewParent, null);
	ev.preventDefault();
}

/** Refreshes any information possible related to a source
 * which was just updated with a new quotation.
 * This is called from the Chrome extension.
 */
function refreshSource(sourceDbId) {
	refreshQuotationsPane();
	refreshSourcesPane();
	refreshSearchPane();
	refreshPaneById(sourceDbId);
}

/** JSHint does not provide a method for annotating externally used function as used
 * so this function is a way of hiding those errors.
 */
function markFunctionsAsUsed() {
	if (true) { return; }

	// JSHint's dead code detection doesn't detect that the following code is dead:
	paneTrashOnClick();
	panePencilOnClick();
	newSubNote();
	onFinishFullPageLoad();
	minusOnMouseOut();
	minusOnMouseOver();
	plusOnMouseOut();
	plusOnMouseOver();
	triangleOnMouseDown();
	saveOptions();
	showPopupForOptions();
	minusOnMouseDown();
	plusOnMouseDown();
	showPopupForCreateNotebook();
	showPopupForMakeEntryNotebook();
	prepForDeleteAndSave();
	deleteOnClick();
	editOnClick();
	onClickCreateSubnote();
	paneResizeOnMouseDown();
	closeAllPanes();
	closePane();
	showPopupWithPage();
	newTab();
	replacePaneForForm();
	refreshPane();
	replacePaneForLink();
	newPaneForLink();
	signOut();
	postCreateAccount();
	createAccount();
	alreadySignedIn();
	signIn();
	elementToText();
	newPaneForForm();
	selectTheEntrysParentInstead();
	makeEntryMainOfTheContainer();
	refreshBackupsPane();
	panePencilOnMouseOver();
	panePencilOnMouseOut();
	onClickOpen();
	showOrHideMenu();
	startAloneDrag();
	checkboxOnClick();
	paneMoveOnMouseDown();
	setAccountInfo();
	showPopupForHelp();
	paneExportOnClick();
	exportFormatOnClick();
	onDragOverAloneEl();
	onDropAloneEl();
	refreshSource();
}

markFunctionsAsUsed();
