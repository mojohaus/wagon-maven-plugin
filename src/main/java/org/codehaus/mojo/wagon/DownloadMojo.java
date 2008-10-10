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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 * Downloads file(s) .
 * 
 * @author Sherali Karimov
 * 
 * @author Dan T. Tran
 * 
 * @goal download
 * 
 * @requiresProject false
 */
public class DownloadMojo
    extends AbstractWagonMojo
{
    /**
     * Local path to download the remote resource ( tree ) to.
     * 
     * @parameter expression="${wagon.downloadDirectory}" default-value="${project.build.directory}/wagon-plugin"
     */
    private File downloadDirectory;

    /**
     * RemoteFileSet configuration, if not set, a default one will be created.
     * @parameter
     */
    private RemoteFileSet remoteFileSet;

    /**
     * RemoteFileSet configuration, if not set, a default one will be created.
     * @parameter
     */
    private List remoteFileSets = new ArrayList( 0 );

    /**
     * @parameter
     */
    private FileItem remoteFileItem;

    /**
     * RemoteFileSet configuration, if not set, a default one will be created.
     * @parameter
     */
    private List remoteFileItems = new ArrayList( 0 );

    protected void execute( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        
        if ( remoteFileItem != null )
        {
            this.remoteFileItems.add( this.remoteFileItem );
        }

        if ( remoteFileSet != null )
        {
            this.remoteFileSets.add( this.remoteFileSet );
        }

        if ( remoteFileSets.isEmpty() && remoteFileItems.isEmpty() )
        {
            remoteFileSets.add( new RemoteFileSet() );
        }
        
        this.downloadRemoteFileItems( wagon );

        this.downloadRemoteFileSets( wagon );
    }

    private void downloadRemoteFileSets( Wagon wagon )
        throws MojoExecutionException, WagonException
    {

        for ( int i = 0; i < remoteFileSets.size(); ++i )
        {
            RemoteFileSet fileSet = (RemoteFileSet) remoteFileSets.get( i );
            
            if ( fileSet.getDownloadDirectory() == null )
            {
                fileSet.setDownloadDirectory( this.downloadDirectory );
            }

            this.wagonHelpers.download( wagon, fileSet, this.getLog() );
        }
    }

    private void downloadRemoteFileItems( Wagon wagon )
        throws MojoExecutionException, WagonException
    {

        for ( int i = 0; i < remoteFileItems.size(); ++i )
        {
            FileItem item = (FileItem) remoteFileItems.get( i );

            this.wagonHelpers.download( wagon, item, this.getLog() );
        }

    }
}
