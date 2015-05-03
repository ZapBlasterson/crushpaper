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

1. All code, Java and JavaScript, must be cleaned up, indented and formatted using Eclipse defaults.
    1. This is not because it is the best style, but because it is easy for contributers to be consistent.
    1. JavaScript must not have any JSHint warnings using the CrushPaper JSHint configuration. 
1. All code requires heavy commenting.
    1. Every class and method must be commented with Javadoc.
    1. Each complex code section of code must be commented.
    1. Comments must focus on what is being done, why it is being done, why it is being done the way that it is, and in what circumstances it should be done.
1. All changes require significant Unit Test coverage, with both positive and negative tests.
    1. Currently UI and HTTP interface code is exempt from this because it changes frequently.
1. If any manual tests are required the Testing Strategy documentation must be updated to include them.
1. If the change is visible or meaningful to users the documentation must be updated.
1. Copyright of all code additions to the code base must be assigned to CrushPaper.com.
    1. <a target="_blank" href="https://www.gnu.org/licenses/why-assign.html">The GNU FSF also encourages this.</a>
	1. Of course, anyone is free to maintain their own fork of the code with changes for which they do not assign the copyright to CrushPaper.
	1. One reason why contributors are asked to assign the copyright of their contribution is that copyright can be enforced only by the copyright holder. 
	1. Another is so that potentially in the future CrushPaper.com can make money from the software similar to how companies such as Neo4j dual license their software under both the GPL and an enterprise license. Hopefully it would benefit all CrushPaper users if CrushPaper contributors are able to make money from CrushPaper. 
	1. Contributors are responsible for ensuring that their employer or school has disclaimed copyright to the contributor's contribution.

 