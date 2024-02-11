# MojoHaus Wagon Maven Plugin

This is the [wagon-maven-plugin](http://www.mojohaus.org/wagon-maven-plugin/).

[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/wagon-maven-plugin.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.codehaus.mojo%22%20a%3A%wagon-maven-plugin%22)
[![Apache License 2](https://img.shields.io/badge/wagon-Apache_v2-yellow.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![GitHub CI](https://github.com/mojohaus/wagon-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/mojohaus/wagon-maven-plugin/actions/workflows/maven.yml)


## Maintained versions

Wagen Maven Plugin requires Maven 3.6.3+ and JDK 8+

However, we maintain the latest Plugin version with the latest Maven.

We execute tests against different operating systems and JDKs
by [GitHub Actions](https://github.com/mojohaus/wagon-maven-plugin/actions/workflows/maven.yml?query=branch%3Amaster)

## Contributing

### Creating Issues

If you find a problem please first search current opened and closed issues and pull requests.
It can be that someone already has reported similar.

You can also check current [milestone](https://github.com/mojohaus/wagon-maven-plugin/milestones)
in order to see what will be in next release.

Only when you can not find similar issue please create a new one in the
[ticket system](https://github.com/mojohaus/wagon-maven-plugin/issues)
and describe what is going wrong or what you expect to happen.

If you have a full working example or a log file this is also helpful.

You should of course describe only a single issue in a single ticket and not
mixing up several things into a single issue.

Please always check your issue with the latest Plugin and tha latest Maven version.

### Creating a Pull Request

Before you start working on more complicated change, new feature
it is good practice to create an issue in
the [ticket system](https://github.com/mojohaus/wagon-maven-plugin/issues)
or send an emil to [development list](https://www.mojohaus.org/wagon-maven-plugin/mailing-lists.html)
and describe what the problem is or what kind of feature you would like to add.
Wait a few days for feedback from other contributors.
Afterwards you can create an appropriate pull request.

It is required if you want to get a pull request to be integrated into please
squash your commits into a single commit which references the optional issue
in the commit message which looks like this:

```
Fixed #Issue - change subject 

a description
```

Please take consider that change subject will be used in release notes
and will be present in git history so should be enough descriptive.

This makes it simpler to merge it and this will also close the
appropriate issue automatically in one go.
This make the life as maintainer a little bit easier.

A pull request has to fulfill only a single ticket and should never
create/add/fix several issues in one, cause otherwise the history is hard to
read and to understand and makes the maintenance of the issues and pull request
hard or to be honest impossible.

## Releasing

* Make sure `gpg-agent` is running.
* Execute `./mvnw -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
../mvnw site
../mvnw scm-publish:publish-scm
```
