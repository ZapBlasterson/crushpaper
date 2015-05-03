### Verification of Release Quality
1. Make sure all new source files have the CrushPaper copyright and AGPLv3 license statement.
1. Make sure the code has no TODOs or FIXMEs.
1. Make sure the code has no unnecessary System.*print or console.log statements. 
1. Check if any 3rd party dependencies should be upgraded.
1. Make sure all code is formatted and cleaned up with Eclipse. (JavaScript code should not be cleaned up because this conflicts with JSHint rules.
1. Review a diff between the last released version and the current proposed code and verify that it meets expectations.
1. Ensure all JUnit tests pass.
1. Obtain test coverage metrics.
1. Run all GUI and Chrome Extension manual tests from the browser with a Maven Shade Jar installation.
1. Visit a page of each type and verify there are no HTML errors with the Kingsquare HTML Validator Chrome extension.
1. Visit a page of each type and verify there are no JavaScript errors with the JavaScript Errors Notifier Chrome extension.

### Release Creation 
1. Choose the next version number.
1. Update the version number in pom.xml.
1. Commit the pom.xml.
1. Tag the source code with the version number.
1. Package and upload the JAR.

### Notification of Release
1. Publish the Chrome extension.
1. Update crushpaper.com for the new release with the version number, download link and release notes.
