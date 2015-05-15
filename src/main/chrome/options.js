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

/* global chrome, Mousetrap, confirmSessionAndSignedIn, getServiceOrDefault */

var storage = chrome.storage.local;

var saveButton = document.getElementById('save');
var serviceInput = document.getElementById('service');

loadOptions();

Mousetrap.stopCallback = function() { return false; };

Mousetrap.bind("alt+s", function() {
	document.getElementById('save').click();
	return false;
	});

saveButton.addEventListener('click', saveOptions);
serviceInput.addEventListener('keyup', onSettingsChange);

function onSettingsChange() {
	storage.get(['service'], function(items) {
		if (items.service) {
			if(serviceInput.value !== items.service) {
				updateServiceMessageNeedSave(serviceInput.value);
				return;
			}
		} else {
			updateServiceMessageNeedSave(serviceInput.value);
			return;
		}
	});
}

function strStartsWith(str, prefix) {
    return str.indexOf(prefix) === 0;
}

function updateServiceMessageNeedSave(serviceValue) {
	if(!serviceValue || (!strStartsWith(serviceValue, "http://") && !strStartsWith(serviceValue, "https://"))) {
		updateMessage("<span class=\"errorMessage\">Sorry, the service must begin with \"http://\" or \"https://\".</span>");
	} else {
		updateMessageNotSave();
	}
}

function updateMessageNotSave() {
	updateMessage("Your changes have not yet been saved.");
}

function saveOptions() {
	var serviceValue = serviceInput.value;
	
	// Remove redundant trailing "/".
	if(serviceValue.substr(serviceValue.length - 1, serviceValue.length) === '/') {
		serviceInput.value = serviceValue = serviceValue.substr(0, serviceValue.length - 1);
	}

	storage.set({
		'service' : serviceValue
	}, function() {
		updateMessage('<span class=\"successMessage\">Your changes have been saved.</span>');
		confirmSessionAndSignedIn(getServiceOrDefault({"service": serviceValue}), true);
	});
}

function loadOptions() {
	storage.get('service', function(items) {
		if (items.service) {
			serviceInput.value = items.service;
		}
	});
}

function updateMessage(messageText) {
	var message = document.querySelector('.message');
	message.innerHTML = messageText;
}
