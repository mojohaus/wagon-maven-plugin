title: FAQ

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

# FAQ

1. [maven-stage-plugin:stage versus wagon-maven-plugin:merge-maven-repos](#stage-vs-merge-maven-repos)

## FAQ

<a id="stage-vs-merge-maven-repos"></a>

### maven-stage-plugin:stage versus wagon-maven-plugin:merge-maven-repos

Both are used to merge artifacts from one Maven repository to another with following differences;

* *merge-maven-repos* is more generic and therefore supports more protocols.
* *merge-maven-repos* supports both normal upload and "optimize" upload where
  files are compressed into an archive which are then transferred, uncompressed and
  removed remotely. Wagon scp, scpexe, and sftp are known to support remote execution
  and therefore suitable for this type of optimization.

