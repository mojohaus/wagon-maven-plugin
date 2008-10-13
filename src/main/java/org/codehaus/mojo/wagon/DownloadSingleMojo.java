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
 * Download a single file.
 * 
 * @author Dan T. Tran
 * @goal download-single
 * 
 * @requiresProject true
 */
public class DownloadSingleMojo
    extends AbstractWagonMojo
{
    /**
     * Relative path to the URL of the remote file
     * @parameter expression="${wagon.remoteFile}"
     * @required 
     */
    private String fromFile;
    
    /**
     * Directory to download the remoteFile to
     * @parameter expression="${wagon.toDir}" 
     */
    private File toDir;

    /**
     * File to download the remoteFile to.  Use this option to rename the file after download
     * @parameter expression="${wagon.toFile}" 
     */
    private File toFile;
    

    protected void execute( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        if ( toDir != null )
        {
            toFile = new File( toDir, new File( fromFile ).getName() );
        }
        
        if ( toFile == null )
        {
            throw new MojoExecutionException( "Either toDir or toFile is required" );
        }
        
        this.getLog().info( "Downloading: " + wagon.getRepository().getUrl() + "/" + fromFile + " to " + toFile );

        wagon.get( fromFile, toFile );

    }

}
