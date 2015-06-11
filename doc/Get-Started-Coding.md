<!---
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
--->
1. Make sure you have JDK 1.8 installed.
1. Make sure you have Eclipse installed.
    1. I use Luna Service Release 1a (4.4.1) with Eclipse IDE for Java Developers
1. Start Eclipse.
1. Make sure you have "JavaScript Development Tools" installed. You can confirm in Help / Installation Details.
1. Make sure you have ["JSHint Eclipse Integration"](http://github.eclipsesource.com/jshint-eclipse/install.html) installed. You can confirm in Help / Installation Details.
1. Get the source from GitHub. Place the crushpaper directory where you want to work on it. This is probably in the Eclipse workspace directory. 
1. Click File / Import / Existing Projects Into Workspace.
    1. Select the crushpaper directory.
    1. Click Finish.
1. Right click on the crushpaper project in the Project Explorer and click Maven / Update Project.
1. Right click on the crushpaper project in the Project Explorer and click Run As / Run Configurations.
    1. Click the Arguments tab and in "Program Arguments" paste "-properties src/main/sampleconfig/example.properties" without the quotes.
    1. Click Apply.
    1. Click Close.
1. Right click on the crushpaper project in the Project Explorer and click Properties.
    1. In the tree on the left of the properties popup click JSHint / Configuration.
        1. Click the checkbox labeled "Enable project specific configuration".
        1. Click Apply.
    1. In the tree on the left of the properties popup click JSHint.
        1. Next to the the "Enable JSHint for these files and folders:" textarea there is a button labeled "Add". Click it.
        1. Don't change anything in the popup. Click the OK button.
        1. Next to the "But exclude these files and folders from validation:" textarea there is a button labeled "Add". Click it.
        1. Don't change anything in the popup. Click the OK button.
        1. Click the OK button

To build on the command line follow <a onclick="newPaneForLink(event, null, 'help'); return false;" href="/doc/Build,-Install,-Configure-and-Run.md">these directions</a>.