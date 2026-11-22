title: Usage
date: 2015-09-01

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

Usage
=====

The Maven Wagon Plugin, as the name implies, allows you to use various functions of a Maven wagon.
It allows you to upload resources from your build to a remote location using wagon.
It allows you to download resources from a repository using wagon.
It allows to list the content of a repository using wagon.
Finally, by combining the upload and download capabilities, it can merge a Maven repository to
another in a generic way.

The configuration are similar to FileSet in maven-assembly-plugin, maven-scm-plugin, etc.

However, due to some differences in term of provider implementation, not all goals are
working as expected. In general, a good way to see a particular wagon implementation
would work with this plugin is to run *wagon:list* goal against its URL.

The following contains a list of known limitations:

* This plugin brings its own wagon providers for `file:`, `http(s):`, `scp:` and `sftp:` URLs
  (wagon-file, wagon-http and wagon-ssh), so it does not depend on the providers a Maven distribution ships.
  Other protocols, such as `dav:` or `ftp:`, still need the provider as a build extension.
* The bundled wagon-http cannot list remote directories, so *list* and a *download* that has to scan the remote
  directory do not work over `http(s):` with it. Since wagon-http 3.5.0 an HTTP server is no longer
  treated as a directory tree, because HTTP has no directory semantics
  ([WAGON-618](https://issues.apache.org/jira/browse/WAGON-618)). For these goals, prefer a protocol that can list:
  `dav:http(s)://` with the *wagon-webdav-jackrabbit* provider when the server speaks WebDAV, or `sftp:`,
  `scp:`, `ftp:` or `file:`. Only as a last resort, declare a wagon-http up to 3.4.3 (for example
  3.3.4) as a dependency of the plugin; it replaces the bundled one and still parses HTML directory listings.
  A wagon-http build extension is not used for these URLs, because the plugin's own provider takes precedence.
* *list* and *download* work over `dav:http(s)://` with the *wagon-webdav-jackrabbit* provider
  (`org.apache.maven.wagon:wagon-webdav-jackrabbit`), declared as a build extension or as a dependency of the
  plugin. They did not work with the older Slide based *wagon-webdav* provider.
* *list* and *download* goals are very slow since it needs to scan the remote file system.
  So use *includes* and *excludes* wisely to limit the search. For example: includes=\*\*/somefile
  scans the entire remote source tree.

The following contains a couple of usage examples.
Live examples are available in [Git](https://github.com/mojohaus/wagon-maven-plugin/tree/master/src/it)

Configuration example to upload
-------------------------------

```xml
<project>
  [...]
  <build>
    [...]
    <extensions>
      <extension>
        <groupId>org.apache.maven.wagon</groupId>
        <artifactId>wagon-ssh</artifactId>
        <version><!-- latest release --></version>
      </extension>
    </extensions>

    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>wagon-maven-plugin</artifactId>
        <executions>
          <execution>
            <id>upload-javadoc</id>
            <phase>deploy</phase>
            <goals>
              <goal>upload</goal>
            </goals>
            <configuration>
              <fromDir>local.dir</fromDir>
              <includes>*</includes>
              <excludes>pom.xml</excludes>
              <url>scp://your.remote.host/</url>
              <toDir>remote.dir</toDir>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
  [...]
</project>
```

Configuration example to download
---------------------------------

```xml
<project>
  [...]
  <build>
    <extensions>
      <extension>
        <groupId>org.apache.maven.wagon</groupId>
        <artifactId>wagon-webdav-jackrabbit</artifactId>
      </extension>
    </extensions>

    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>wagon-maven-plugin</artifactId>
        <executions>
          <execution>
            <id>download-test-data</id>
            <phase>pre-integration-test</phase>
            <goals>
              <goal>download</goal>
            </goals>
            <configuration>
              <serverId>atlassian-public</serverId>
              <url>dav:https://maven.atlassian.com/public</url>
              <fromDir>com/atlassian/jira/plugins/jira-plugin-test-resources/${project.version}</fromDir>
              <toDir>target/test-data</toDir>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
  [...]
</project>
```

Configuration example to download known files without listing the directory
---------------------------------------------------------------------------

When every entry of `includes` is an exact path relative to `fromDir`, with no `*` or `?` and no trailing slash,
the *download* goal fetches those files directly and does not scan the remote directory. This works over plain
`http(s):` URLs, where a directory cannot be listed. A file that does not exist is skipped with a warning.

```sh
mvn wagon:download -Dwagon.url=https://repo.maven.apache.org/maven2 \
   -Dwagon.fromDir=org/apache/maven/plugins/maven-clean-plugin/3.2.0 \
   -Dwagon.includes=maven-clean-plugin-3.2.0.pom,maven-clean-plugin-3.2.0.jar \
   -Dwagon.toDir=target/downloads
```

Configuration example to check that a resource exists
-----------------------------------------------------

The *exist* goal logs whether a resource exists below the URL. It reports the result and does not fail the build
when the resource is missing. It needs a project, so run it in a directory with a `pom.xml`.

```sh
mvn wagon:exist -Dwagon.url=https://repo.maven.apache.org/maven2 \
   -Dwagon.resource=org/apache/maven/plugins/maven-clean-plugin/3.2.0/maven-clean-plugin-3.2.0.pom
```

Configuration example to merge Maven repositories
-------------------------------------------------

```sh
mvn wagon:merge-maven-repos \
   -Dwagon.source=https://repo.example.com/staging-repo \
   -Dwagon.target=scp://localhost/$LOGNAME/maven-repo
```

Configuration example to copy files under one URL to another URL
----------------------------------------------------------------

```sh
mvn wagon:copy \
   -Dwagon.source=https://repo.example.com/staging-repo \
   -Dwagon.target=scp://localhost/home/$LOGNAME/maven-repo
```

Configuration example to execute commands
-----------------------------------------

The *sshexec* goal runs commands over the `scp:` or `sftp:` provider this plugin brings, so it needs no
build extension.

```xml
<project>
  [...]
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>wagon-maven-plugin</artifactId>
        <executions>
          <execution>
            <id>execute-test-commands</id>
            <phase>deploy</phase>
            <goals>
              <goal>sshexec</goal>
            </goals>
            <configuration>
              <serverId>atlassian-public</serverId>
              <url>scp://maven.atlassian.com/public</url>
              <commands>
                <command>/usr/share/tomcat/bin/tomcat stop</command>
                <command>rm -rf /usr/share/tomcat/webapps/ROOT</command>
                <command>unzip -o /usr/share/tomcat/webapps/myapp.zip -d /usr/share/tomcat/webapps/</command>
                <command>/usr/share/tomcat/bin/tomcat start</command>
              </commands>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
  [...]
</project>
```

