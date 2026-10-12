# Agent guide for wagon-maven-plugin

This file is for automated agents and human contributors working on this repository. It records
what the code and the POMs do not make obvious.

## Layout

- One module in `org.codehaus.mojo.wagon` with nine goals: `download`, `download-single`, `upload`,
  `upload-single`, `copy`, `list`, `exist`, `merge-maven-repos` and `sshexec`. The mojos share the
  `Abstract*Mojo` base classes; the work is done by the components in `shared`
  (`DefaultWagonDownload`, `DefaultWagonUpload`, `DefaultWagonCopy`, `DefaultMavenRepoMerger`),
  which are exposed through `WagonFactory`.
- The site is built from `src/site` (`apt` pages and the FAQ in `fml`).
- `src/it` is a set of example projects for manual runs, described in `src/it/readme.txt`. No
  build runs them.

## Build gates

- On a local build Spotless applies the formatting; it also reformats Markdown, so run
  `mvn spotless:apply` after editing the README or this file.
- The parent, `mojo-parent`, sets `maven.compiler.release` to 8, so main code must use the Java 8
  API.
- The plugin compiles against Maven 3.9.16 (`mavenVersion`) and declares a literal
  `<prerequisites><maven>3.9.0</maven>`; keep the two apart, so raising `mavenVersion` does not
  raise the prerequisite.

## Tests

- There are no unit tests. The tests are the `*IT` classes in `src/test/java`, JUnit 5 tests that
  maven-failsafe-plugin runs in `integration-test`, so `mvn verify` runs them.
- Each IT builds a project from `src/test/projects/<name>` with maven-executor. `AbstractMavenIT`
  copies the project to `target/it-projects/<test class>/<name>`, forks the Maven installation
  that runs the outer build (the `maven.home` property from the failsafe configuration), passes the
  plugin version as `-Dit-plugin.version`, writes the output to `build.log` in the copy, and fails
  on a non-zero exit code or any `[ERROR]` line. The `install` goal of maven-invoker-plugin
  installs the plugin into `target/local-repo` before that, and the forked builds use it as their
  local repository.
- The HTTP tests (`WagonMojoHttpIT`, `WagonMojoHttpSettingsIT`, `Wagon334MojoHttpIT`) extend
  `AbstractJettyIT`, which serves a `files` directory of the test project from an embedded Jetty
  on a free `localhost` port and passes it as `-Dserver.port`. The FTP test starts an embedded Apache
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

- `.github/workflows/maven.yml` calls the shared `maven-verify.yml@v5` workflow from
  `apache/maven-gh-actions-shared`. Branches in this repository build under the push event, so look
  at the push run for a pull request from a same-repository branch.
- Pull requests are squash- or rebase-merged; merge commits are disabled. Release Drafter builds
  the release notes from the titles and labels, so the title should describe the change for a
  user.

