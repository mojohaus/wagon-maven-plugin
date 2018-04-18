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

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Provides base functionality for dealing with I/O using single wagon.
 */
public abstract class AbstractDoubleWagonMojo
    extends AbstractWagonMojo
{

    /**
     * The URL to the source repository.
     */
    @Parameter( property = "wagon.source", required = true)
    protected String source;

    /**
     * The URL to the target repository.
     */
    @Parameter( property = "wagon.target", required = true)
    protected String target;

    /**
     * settings.xml's server id of the source repository. This is used when wagon needs extra authentication
     * information.
     */
    @Parameter( property = "wagon.sourceId", defaultValue = "source")
    protected String sourceId;

    /**
     * settings.xml's server id of the target repository. This is used when wagon needs extra authentication
     * information.
     */
    @Parameter( property = "wagon.targetId", defaultValue = "target")
    protected String targetId;

    /**
     * Optimize the upload by locally compressed all files in one bundle, upload the bundle, and finally remote
     * uncompress the bundle. This only works with SCP's URL
     */
    @Parameter( property = "wagon.optimize", defaultValue = "false")
    protected boolean optimize = false;

}
