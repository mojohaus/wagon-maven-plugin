package org.codehaus.mojo.wagon;

/**
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 * Downloads file(s) .
 * 
 * @author Sherali Karimov
 * @author Dan T. Tran
 * @goal download
 * 
 * @requiresProject false
 */
public class DownloadMojo
    extends AbstractWagonMojo
{
    /**
     * Local directory to download the remote resource ( tree ) to.
     * 
     * @parameter expression="${wagon.toDir}" default-value="${project.build.directory}/wagon-plugin"
     */
    private File toDir;

    /**
     * @parameter expression="${wagon.recursive}" default-value="false"
     */
    private boolean recursive = false;

    protected void execute( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        RemoteFileSet fileSet = new RemoteFileSet();
        fileSet.setDownloadDirectory( this.toDir );
        fileSet.setRecursive( this.recursive );
        
        this.wagonHelpers.download( wagon, fileSet, this.getLog() );
    }
    
}
