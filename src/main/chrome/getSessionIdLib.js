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

/* global chrome */

/** Calls a callback with the value of the first matching cookie.
 * If not found, the callback is called with null as the value. */
function getCookie(domain, name, callback) {
    chrome.cookies.getAll({"domain": domain, "name": name }, function(cookies) {
    	var foundAMatch = false;

    	for(var i = 0; i < cookies.length; ++i) {
    		var cookie = cookies[i];
			callback(cookie.value);
			foundAMatch = true;
			break;
    	}
    	
        if(!foundAMatch) {
        	callback(null);
        }
    });
}

/** Returns the URL portion of the domain. */
function getDomainFromUrl(url) {
	if(!url) {
		return null;
	}

	var regex = new RegExp("^https?://([^:/]+)", "i");
	var matches = regex.exec(url);

	if(!matches || matches.length < 2) {
		return matches;
	}
	
	return matches[1];
}

/** Will eventually call the callback with the session ID. */
function getServiceSessionCookie(service, callback) {
	var domain = getDomainFromUrl(service);
	if(!domain) {
		callback(null);
		return;
	}
	
	getCookie(domain, "JSESSIONID", callback);
}

/** Shows a tab. */
function showTab(url) {
	chrome.tabs.query({ url: url }, function(tabs) {
	    if (tabs.length) {
	        chrome.tabs.update(tabs[0].id, {active: true});
	    } else {
	        chrome.tabs.create({url: url});
	    }
	    
    	window.close();
	});
}

/** Shows the sign in page and closes the current popup. */
function showSignInTabAndClose(service) {
	document.getElementById("needsConfigurationMessage").innerHTML = "<span class=\"errorMessage\">Please sign into CrushPaper so that you can save quotations." +
		"<br>The sign in page will be automatically opened in 5 seconds.</span>";
	
	setTimeout(function() {
		showTab(service + "/?signIn=1");
	}, 5000);
}

/** Shows the sign in page and closes the current popup. */
function showSignedInAndClose() {
	document.getElementById("needsConfigurationMessage").innerHTML = "<span class=\"successMessage\">You are signed into CrushPaper." +
		"<br>This window will close in 5 seconds.";
	
	setTimeout(function() {
		window.close();
	}, 5000);
}

/** Returns a string to append to an URL so that a request for the URL can not be get cached by the browser. */
function getAnUrlUniquer() {
    return "time=" + new Date().getTime();
}

/** Confirm that the browser has a session and is signed in. */
function confirmSessionAndSignedIn(service, closeEvenIfSignedIn, completionCallback) {
	getServiceSessionCookie(service, function(sessionId) {
		if(!sessionId) {
			showSignInTabAndClose(service);
		} else {
			var xhr = new XMLHttpRequest();

			xhr.open("GET", service + "/isSignedIn/?sessionId=" + sessionId + "&" + getAnUrlUniquer(), true);
			xhr.onreadystatechange = function() {
				if (xhr.readyState === 4) {
					var signedIn = false;
					if (xhr.status === 200) {
						try {									
							var response = JSON.parse(xhr.responseText);
							if (("isSignedIn" in response) &&
									response.isSignedIn) {
								signedIn = true;
							}
						} catch(e) {
						}
					}

					if(!signedIn) {
						showSignInTabAndClose(service);
					} else if(closeEvenIfSignedIn) {
						showSignedInAndClose();
					}
					
					if(completionCallback) {
						completionCallback(sessionId);
					}
				}
			};

			xhr.send();
		}
	});
}

/** JSHint does not provide a method for annotating externally used function as used
 * so this function is a way of hiding those errors.
 */
function markFunctionsAsUsed() {
	if (true) { return; }

	// JSHint's dead code detection doesn't detect that the following code is dead:
	confirmSessionAndSignedIn();
}

markFunctionsAsUsed();