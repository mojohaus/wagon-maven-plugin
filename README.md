# MojoHaus Wagon Maven Plugin

This is the [wagon-maven-plugin](http://www.mojohaus.org/wagon-maven-plugin/).

[![Build Status](https://travis-ci.org/mojohaus/wagon-maven-plugin.svg?branch=master)](https://travis-ci.org/mojohaus/wagon-maven-plugin)

## Releasing

* Make sure `ssh-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```