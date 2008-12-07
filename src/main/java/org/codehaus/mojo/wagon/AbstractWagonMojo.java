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

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.Wagon;
import org.codehaus.mojo.wagon.shared.WagonFileSet;
import org.codehaus.mojo.wagon.shared.WagonUtils;

/**
 * Provides base functionality for dealing with I/O using wagon.
 * 
 */
public abstract class AbstractWagonMojo
    extends AbstractMojo
{

    /**
     * @component
     */
    protected WagonManager wagonManager;

    /**
     * The current user system settings for use in Maven.
     * 
     * @parameter expression="${settings}"
     * @readonly
     */
    protected Settings settings;

    /**
     * Internal Maven's project
     * 
     * @parameter expression="${project}"
     * @readonly
     */
    protected MavenProject project;

    /**
     * When <code>true</code>, skip the execution.
     * 
     * @parameter expression="${wagon.skip}" default-value="false"
     */
    protected boolean skip = false;

    /**
     * Convenient method to create a wagon
     * 
     * @param id
     * @param url
     * @param wagonManager
     * @param settings
     * @param logger
     * @return
     * @throws MojoExecutionException
     */
    protected Wagon createWagon( String id, String url )
        throws MojoExecutionException
    {
        try
        {
            return WagonUtils.createWagon( id, url, wagonManager, settings, this.getLog() );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to create a Wagon instance for " + url, e );
        }

    }

    protected WagonFileSet getWagonFileSet( String fromDir, String includes, String excludes, boolean caseSensitive, String toDir )
    {
        return WagonUtils.getWagonFileSet( fromDir, includes, excludes, caseSensitive, toDir );
    }

}