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

### Supported Operating Systems
1. CrushPaper has been developed on Windows 7, Windows 8 and on Linux.
1. It is pure Java and so it should work on most operating systems. 

###How to Build CrushPaper
1. Install Java. The version of Java used during development is 1.8.0_40 64 bit.
1. Install Maven. The version of Maven used during development is 3.2.5.
1. In the root of the crushpaper project run `mvn package`. This should compile all classes, run all tests, and create the executable JAR.
1. To build without running the tests run `mvn package -Dmaven.test.skip=true`.
1. To build with Eclipse follow <a onclick="newPaneForLink(event, null, 'help'); return false;" href="/doc/Get-Started-Coding.md">these directions</a>. 
 
### How to Install the CrushPaper Server
1. Either download or build (see below) the CrushPaper Server executable Jar. It is named crushpaper-&lt;version.jar.
  1. You can download it from <a target="_blank" href="https://github.com/ZapBlasterson/crushpaper/releases">GitHub</a>.
1. It is recommended that you run the CrushPaper server as a dedicated OS userid but this is optional.
1. It is recommended that you use HTTPS but this is optional. You can generate an SSL certificate for free by following <a target="_blank" href=" http://wiki.eclipse.org/Jetty/Howto/Configure_SSL#Generating_Key_Pairs_and_Certificates">these directions</a>.
1. Choose the directory where you want to run CrushPaper. Call it `$CRUSHPAPER`.
1. If using Unix: `mkdir -p $CRUSHPAPER/logs $CRUSHPAPER/tmp $CRUSHPAPER/db $CRUSHPAPER/config $CRUSHPAPER/bin $CRUSHPAPER/backups $CRUSHPAPER/sessions`
1. If using Windows: `mkdir %CRUSHPAPER%\logs %CRUSHPAPER%\tmp %CRUSHPAPER%\db %CRUSHPAPER%\config %CRUSHPAPER%\bin %CRUSHPAPER%/backups %CRUSHPAPER%/sessions`
1. Copy the CrushPaper Server executable JAR (`crushpaper-version.jar`) into the `bin` directory that was just created.
1. Create `crushpaper.properties` in the `config` directory that was just created with:<br>
    `http.port = 8080`<br>
    `database.directory = db`<br>
    `temporary.directory = tmp`<br>
    `logs.directory = logs`<br>
    `sessionStore.directory = sessions`<br>
    `singleUser = yourid`<br>
    `loopbackIsAdmin=true`<br>	
1. If using Unix then create `$CRUSHPAPER/bin/start` with:<br>
    `cd $CRUSHPAPER`<br>
    `JAVA_HOME=<path/to/java>`<br>
    `PATH=$PATH:$JAVA_HOME/bin`<br>
    `nohup java -server \`<br>
        `-jar ./bin/crushpaper-version.jar \`<br>
        `-properties ./config/crushpaper.properties \`<br>
        `>> ./logs/crushpaper.log 2>&1 &`<br>
    Don't forget to make the script executable with: `chmod +x $CRUSHPAPER/bin/start`<br>
1. If using Windows then create `%CRUSHPAPER%\bin\start.bat` with:<br>
    `set CRUSHPAPER="<path/to/crushpaper>"`<br>
    `cd %CRUSHPAPER%`<br>
    `set JAVA_HOME="<path/to/java>"`<br>
    `set Path=%Path%:%JAVA_HOME%\bin`<br>
    `java ^`<br>
        `-jar .\bin\crushpaper-<version>.jar ^`<br>
        `-properties .\config\crushpaper.properties ^`<br>
        `1> .\logs\crushpaper.log 2>&1`<br>
1. If using Unix then start the CrushPaper server by running `$CRUSHPAPER/bin/start`.
1. If using Windows then start the CrushPaper server by running `%CRUSHPAPER%\bin\start.bat`.
1. Open your browser and visit <a target="_blank" href="http://localhost:8080">http://localhost:8080</a>.

### How to Run the CrushPaper Server
1. Ensure that your JAVA_HOME environment variable is set and your PATH environment variable includes the Java bin directory.
1. The CrushPaper server can be run from any working directory but remember that relative paths in command line arguments and property files are relative to this working directory.
1. Run it with this command: `$ nohup java -jar <path/to/crushpaper-version.jar> -properties <path/to/crushpaper.properties> 2>&1 > path/to/logfile &`
1. To run it from the build area use this command: `$ java -jar target/crushpaper-*.jar -properties src/main/sampleconfig/example.properties`

### How to Kill the CrushPaper Server
1. If you `kill -9` the server you risk corrupting the H2 database, although the H2 will usually be able recover the database at start up. 
1. You can shutdown the server with `kill` command.
1. With administrator privileges you may also shutdown the server from its web site.

### Command Line Arguments
1. **-properties &lt;path/to/crushpaper.properties&gt;** - This is the path to the CrushPaper server properties file. This argument is mandatory.
1. **-help** - This argument results in a very simple help message being printed.

### Configuration File Properties
#### Mandatory Properties
1. **database.directory = &lt;path/to/database/directory&gt;** - This is the path to the directory where the H2 database is stored.
1. **temporary.directory = &lt;path/to/temporary/directory&gt;** - This is the path to the directory where temporary files are stored. These temporary files are user backup files that are in the process of being restored.
1. **logs.directory = &lt;path/to/logs/directory&gt;** - This is the path to the directory where the server's HTTP request logs are stored.
 
#### Recommended Properties
1. **http.port = &lt;port&gt;** - This is the HTTP port that the server listens on. Either this property or `https.port` must be set. Both can be set.
1. **singleUser = &lt;username&gt;** - If this set to any value then anyone who can access the server is treated as if they signed into the account named &lt;username&gt;. This may be reasonable to use if CrushPaper is installed on a PC or laptop which is accessible only to trusted networks. Defaults to blank which disables this functionality.
1. **loopbackIsAdmin = &lt;true|false&gt;** - This treats any session as an admin user if the session is connected from the same host on which the CrushPaper server is running using the loopback interface, i.e `localhost:8080`, `loopback:8080`, `127.0.0.1:8080` or `0:0:0:0:0:0:0:1:8080`.
1. **sessionStore.directory = &lt;path/to/directory&gt;** - This is the directory used to store HTTP session information. If this is not set then HTTP sessions will not be persist across server restarts.

#### HTTPS Properties
1. **https.port = &lt;port&gt;** - This is the HTTPS port that the server listens on. Either this property or `http.port` must be set. Both can be set.
1. **https.keystore = &lt;path/to/file&gt;** - This is the keystore file used for HTTPS. It must be set if `https.port` is set.
1. **https.keystorePassword = &lt;password&gt;** - This is the password for the keystore file. It is optional because not all keystores have passwords.
1. **https.keymanagerPassword = &lt;password&gt;** - This is the keymanager password for the keystore file. Not all keystores require this.

#### Optional Properties
1. **allowSaveIfNotSignedIn = &lt;true|false&gt;** - If this is true creating a notebook automatically creates a new account for the session if the user is not signed in. Only set this to true for servers where you want to encourage people to try it out. Defaults to false.
1. **allowSelfSignUp = &lt;true|false&gt;** - If this is true anyone who can access the server can create an account. Defaults to false.
1. **https.proxiedPort = &lt;port&gt;** - Only set this if connections are forwarded from another port to `https.port`. This may only be set if `https.port` is set.

###H2 Database Documentation
1. The H2 Database documentation is <a target="_blank" href="http://www.h2database.com">here</a>.
