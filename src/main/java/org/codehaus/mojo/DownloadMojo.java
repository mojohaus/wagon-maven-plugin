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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 * Downloads files that match specified pattern (resourceSrc) to the given destination. 
 * 
 * @author Sherali Karimov
 * @goal download
 */
public class DownloadMojo
    extends AbstractWagonMojo
{

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

    /**
     * Local path to download the remote resource ( tree ) to.
     * 
     * @parameter expression="${wagon.destinationDirectory}" default-value="${project.build.directory}/wagon-plugin"
     */
    protected File destinationDirectory;

    protected void execute( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        List fileList = new ArrayList();

        WagonUtils.scan( wagon, "", fileList, recursive, hasDirectoryIndicator, this.getLog() );

        for ( Iterator iterator = fileList.iterator(); iterator.hasNext(); )
        {
            String remotePath = (String) iterator.next();

            File destination = new File( destinationDirectory + "/" + remotePath );

            wagon.get( remotePath, destination ); // the source path points at a single file
        }
    }

}
