package org.codehaus.mojo;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Lists the content of the specified directory (resourceSrc) under a specified repository (url) according to the given wildcard (part of resourceSrc).
 * Wildcard can be turned on and off as required.
 * 
 * @author Sherali Karimov
 * @goal list
 */
public class ListMojo
    extends AbstractWagonMojo
{
    /**
     * Resource(s) to be listed. Can be a file or directory. Also supports
     * wildcards.
     * 
     * @see PathParserUtil#toFiles(String)
     * @parameter expression="${wagon.resourceSrc}" 
     */
    private String resourceSrc;

    /**
     * 
     * @parameter expression="${wagon.recursive}" default-value="true"
     */
    private boolean recursive;

    /**
     * The list return from the protocol has a ending slash to indicate a directory.
     * The value is automatically discoverred
     */
    private boolean hasDirectoryIndicator = false;

    protected void execute( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        List files = new ArrayList();

        if ( resourceSrc == null )
        {
            resourceSrc = "";
        }

        WagonUtils.scan( wagon, resourceSrc, files, this.recursive, this.hasDirectoryIndicator, this.getLog() );

        for ( Iterator iterator = files.iterator(); iterator.hasNext(); )
        {
            String file = (String) iterator.next();
            getLog().info( "\t" + file );
        }
    }
}
