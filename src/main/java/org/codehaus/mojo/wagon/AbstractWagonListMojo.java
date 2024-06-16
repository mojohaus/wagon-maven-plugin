package org.codehaus.mojo.wagon;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.mojo.wagon.shared.WagonDownload;
import org.codehaus.mojo.wagon.shared.WagonFileSet;

/**
 * Contains common configuration to scan for Wagon's files
 */
public abstract class AbstractWagonListMojo extends AbstractSingleWagonMojo {

    /**
     * Directory path relative to Wagon's URL
     */
    @Parameter(property = "wagon.fromDir")
    protected String fromDir = "";

    /**
     * Comma separated list of Ant's includes to scan for remote files
     */
    @Parameter(property = "wagon.includes", defaultValue = "*")
    protected String includes;

    /**
     * Comma separated list of Ant's excludes to scan for remote files.
     */
    @Parameter(property = "wagon.excludes")
    protected String excludes;

    /**
     * Whether to consider remote path case sensitivity during scan.
     */
    @Parameter(property = "wagon.caseSensitive", defaultValue = "true")
    protected boolean caseSensitive = true;

    @Component
    protected WagonDownload wagonDownload;

    protected WagonFileSet getWagonFileSet() {
        return this.getWagonFileSet(fromDir, includes, excludes, caseSensitive, "");
    }
}
