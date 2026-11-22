# Agent guide for wagon-maven-plugin

This file is for automated agents and human contributors working on this repository. It records
what the code and the POMs do not make obvious.

## Layout

- One module in `org.codehaus.mojo.wagon` with nine goals: `download`, `download-single`, `upload`,
  `upload-single`, `copy`, `list`, `exist`, `merge-maven-repos` and `sshexec`. The mojos share the
  `Abstract*Mojo` base classes; the work is done by the components in `shared`
  (`DefaultWagonDownload`, `DefaultWagonUpload`, `DefaultWagonCopy`, `DefaultMavenRepoMerger`),
  which are exposed through `WagonFactory`.
- The site is built from `src/site/markdown`; the goal pages come from the Javadoc of the mojos.
- `src/it` is a set of example projects for manual runs, described in `src/it/readme.txt`. No
  build runs them.

## Build gates

- On a local build Spotless applies the formatting; it also reformats Markdown, so run
  `mvn spotless:apply` after editing the README or this file.
- The parent, `mojo-parent`, sets `maven.compiler.release` to 8, so main code must use the Java 8
  API.
- The plugin compiles against the Maven 3.9.x line (`mavenVersion`) and declares a literal
  `<prerequisites><maven>`; keep the two apart, so raising `mavenVersion` does not raise the
  prerequisite.

## Tests

- There are two JUnit 5 unit test classes in `src/test/java`, `DefaultMavenRepoMergerTest` and
  `DefaultWagonDownloadTest`, which maven-surefire-plugin runs in `test`. The rest are the `*IT`
  classes, JUnit 5 tests that maven-failsafe-plugin runs in `integration-test`, so `mvn verify`
  runs them all.
- Each IT builds a project from `src/test/projects/<name>` with maven-executor. `AbstractMavenIT`
  copies the project to `target/it-projects/<test class>/<name>`, forks the Maven installation
  that runs the outer build (the `maven.home` property from the failsafe configuration), passes the
  plugin version as `-Dit-plugin.version`, writes the output to `build.log` in the copy, and fails
  on a non-zero exit code or any `[ERROR]` line. The `install` goal of maven-invoker-plugin
  installs the plugin into `target/local-repo` before that, and the forked builds use it as their
  local repository.
- The HTTP and WebDAV tests (`WagonMojoHttpIT`, `WagonMojoHttpSettingsIT`, `Wagon334MojoHttpIT`,
  `WagonMojoHttpExactIT`, `WagonMojoWebDavIT`) extend `AbstractHttpServerIT`, which serves a directory
  of the test project with the JDK's built-in HTTP server on a free `localhost` port; the tests pass
  the port as `-Dserver.port`. The FTP test starts an embedded Apache
  FtpServer, which `ftp-basic` reaches at `ftp://anonymous@localhost:8221`. `WagonMojoSshIT` is `@Disabled`.
- Run a subset:

  ```shell
  mvn -B verify -Dit.test=WagonMojoHttpIT
  ```

## Dependencies

- Deliberate pins in `.github/dependabot.yml`: the Maven artifacts stay below 3.10.0 (the plugin
  baseline is the Maven 3.9.x line), the Maven wrapper stays on the Maven 3 line, `plexus-xml`
  stays below 4.0.0 (the Maven 4 line), and SLF4J stays below 2.0.0 (Maven 3.9 exports
  slf4j-api 1.7.36; the POM pins `dependency.slf4j.version` to 1.7.36).

## Continuous integration and pull requests

- `.github/workflows/maven.yml` calls the shared `maven-verify.yml` workflow in `mojohaus/.github`.
  Branches in this repository build under the push event, so look at the push run for a pull
  request from a same-repository branch.
- Pull requests are squash- or rebase-merged; merge commits are disabled. Release Drafter builds
  the release notes from the titles and labels, so the title should describe the change for a
  user.

## Documentation updates

A change that users or contributors can notice ships with the documentation it affects, in the
same pull request. A refactor with no visible effect needs none.

- The goal pages are generated from the Javadoc of the mojos: describe each `@Parameter` with its
  default, its `property` name and, for a new one, `@since`. Keep the class Javadoc of a mojo
  accurate, because it is the goal's description in `plugin-info.html`.
- `src/site/markdown/index.md` lists every goal and links the goal pages; add a new goal there.
- `src/site/markdown/usage.md` holds the known limitations of the wagon providers and one example
  per goal. Add an example for a new goal or parameter that needs more than its description.
- `src/site/markdown/faq.md` answers recurring questions. Keep the `src/site/site.xml` menu in
  sync when a page is added, and never rename a published page or change a heading: both are URLs
  that readers link to.
- Write site pages as plain Markdown, and show the plugin without a version, or with
  `<version><!-- latest release --></version>` where a version is needed, and never a snapshot
  version. A page that needs Velocity (`#set`, `#parse`, `$h2`) would be named `.md.vm`.
- `README.md` states what the plugin does and its Maven and JDK floor; update it when those
  change. Update this file when a change adds a convention, build gate or fixture.

