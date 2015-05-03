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
function openPopUp(info, tabWithSelection) {
    chrome.tabs.create({
        url: chrome.extension.getURL('popup.html'),
        active: false
	    }, function(popupTab) {
	        // After the tab has been created, open a window and inject the tab.
	        chrome.windows.create({
	            tabId: popupTab.id,
	            type: 'popup',
	            focused: true,
	            height: 465,
	            width: 495
	        }, function() {
	        	// Do this in a callback so that the popup window is already listening for the message.
	        	
		        chrome.extension.sendMessage( { "quotation": info.selectionText,
		        	"url": tabWithSelection.url.toString(),
		        	"title": tabWithSelection.title.toString() } );
		        	
		        });
	    });
}

/* This seems to be a method that prevents the context menu
from being added twice and lets behavior change on refresh. */
var context = "selection";
var title = "Save selected text to CrushPaper";
var id = "CrushPaperQuotation";
chrome.contextMenus.create({id:id,
	"title": title, "contexts":[context],
    "onclick": openPopUp});
chrome.contextMenus.update(id, 
	{"title": title, "contexts":[context],
    "onclick": openPopUp});
