# MojoHaus Wagon Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/wagon-maven-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.codehaus.mojo/wagon-maven-plugin)
[![Apache License 2](https://img.shields.io/badge/wagon-Apache_v2-yellow.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![GitHub CI](https://github.com/mojohaus/wagon-maven-plugin/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/mojohaus/wagon-maven-plugin/actions/workflows/maven.yml?query=branch%3Amaster)

The [Wagon Maven Plugin](https://www.mojohaus.org/wagon-maven-plugin/) uploads, downloads, lists,
copies and merges files over any Maven Wagon transport, and runs commands on a remote SSH host. It
needs Maven 3.9.0 or later and JDK 8 or later. It brings its own `file:`, `http(s):`, `scp:` and
`sftp:` providers, and runs from the command line without a project for most goals:

```shell
mvn wagon:list -Dwagon.url=file:///var/repo -Dwagon.fromDir=releases
```

The [goals](https://www.mojohaus.org/wagon-maven-plugin/plugin-info.html), the known limitations of
each protocol and examples are on the [project site](https://www.mojohaus.org/wagon-maven-plugin/).

## Contributing

Report problems in the [issue tracker](https://github.com/mojohaus/wagon-maven-plugin/issues) with
the plugin, Maven and JDK versions and a project that reproduces them. For a larger change, open an
issue or write to the [mailing list](https://www.mojohaus.org/wagon-maven-plugin/mailing-lists.html)
first. [AGENTS.md](AGENTS.md) describes the build, the tests and the pull request conventions; it
applies to human contributors as well.

## Releasing

* Make sure `gpg-agent` is running.
* Execute `./mvnw -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
../mvnw site
../mvnw scm-publish:publish-scm
```

