# MojoHaus Wagon Maven Plugin

This is the [wagon-maven-plugin](http://www.mojohaus.org/wagon-maven-plugin/).

[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/wagon-maven-plugin.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.codehaus.mojo%22%20a%3A%wagon-maven-plugin%22)
[![Apache License 2](https://img.shields.io/badge/wagon-Apache_v2-yellow.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Build Status](https://travis-ci.org/mojohaus/wagon-maven-plugin.svg?branch=master)](https://travis-ci.org/mojohaus/wagon-maven-plugin)

## Releasing

* Make sure `ssh-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```