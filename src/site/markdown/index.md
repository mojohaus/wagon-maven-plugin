title: Introduction
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

# Overview

Use this plugin to view and transfer resources between repositories using Maven Wagon.

## Goals Overview

The Wagon Maven Plugin has the following goals.

* [`wagon:upload-single`](./upload-single-mojo.html) uploads the specified file to a remote location.
* [`wagon:upload`](./upload-mojo.html) uploads the specified set of files to a remote location.
* [`wagon:download-single`](./download-single-mojo.html) downloads the specified file from a remote location.
* [`wagon:download`](./download-mojo.html) downloads the specified set of files from a remote location.
* [`wagon:list`](./list-mojo.html) lists the content of a specified location in a remote repository.
* [`wagon:copy`](./copy-mojo.html) copies a set of files under a Wagon repository to another.
* [`wagon:merge-maven-repos`](./merge-maven-repos-mojo.html) merges , including metadata, a Maven repository to another.
* [`wagon:sshexec`](./sshexec-mojo.html) Executes a set of commands at remote SSH host.

## Usage

Instructions on the basic usage of the Wagon Maven Plugin can be found [here](./usage.html).

## Examples

* Live examples are available in [Git](https://github.com/mojohaus/wagon-maven-plugin/tree/master/src/it)

