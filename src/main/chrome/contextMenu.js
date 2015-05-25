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

function openPopUp(info, tabWithSelection) {
    chrome.tabs.create({
        url: chrome.extension.getURL('popup.html?fromContextMenu'),
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
	        	// PDFs have URLs in a different place than HTML.
	        	var url = tabWithSelection.url.toString();
	        	var title = tabWithSelection.title.toString();
	        	if ("srcUrl" in info) {
	        		url = info.srcUrl.toString();
	        		// PDFs seem to have their names URL encoded. And use the URL instead.
	        		title = decodeURIComponent(url);
	        	}

	        	// Do this in a callback so that the popup window is already listening for the message.
		        chrome.extension.sendMessage( { "quotation": info.selectionText,
		        	"url": url,
		        	"title": title } );
		        });
	    });
}

/* This seems to be a method that prevents the context menu
from being added twice and lets behavior change on refresh. */
var contexts = [ "selection", "page", "frame", "link", "editable", "image", "video" ];
var title = "Save selected text to CrushPaper";
var id = "CrushPaperQuotation";
chrome.contextMenus.create({id:id,
	"title": title, "contexts":contexts,
    "onclick": openPopUp});
chrome.contextMenus.update(id, 
	{"title": title, "contexts":contexts,
    "onclick": openPopUp});
