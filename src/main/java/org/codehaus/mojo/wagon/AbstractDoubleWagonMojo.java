package org.codehaus.mojo.wagon;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */


/**
 * Provides base functionality for dealing with I/O using single wagon.
 * 
 */
public abstract class AbstractDoubleWagonMojo
    extends AbstractWagonMojo
{

    /**
     * The URL to the source repository.
     * 
     * @parameter expression="${source}"
     * @required
     */
    protected String source;

    /**
     * The URL to the target repository.
     * 
     * @parameter expression="${target}"
     * @required
     */
    protected String target;

    /**
     * The id of the source repository, required if you need the configuration from the user
     * settings.
     * 
     * @parameter expression="${sourceId}" default-value="source"
     */
    protected String sourceId;

    /**
     * The id of the target repository, required if you need the configuration from the user
     * settings.
     * 
     * @parameter expression="${targetId}" default-value="target"
     */
    protected String targetId;

}