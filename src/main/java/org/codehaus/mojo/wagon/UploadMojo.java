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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.StringUtils;

/*
 * Uploads file(s)
 * 
 * @author Sherali Karimov
 * @author Dan T. Tran
 * 
 * @goal upload
 * 
 * @requiresProject true
 */
public class UploadMojo
    extends AbstractWagonMojo
{

    /**
     * @parameter
     */
    private FileItem fileItem;

    /**
     * @parameter
     */
    private List fileItems = new ArrayList( 0 );

    /**
     * A single FileSet to upload.
     *
     * @parameter
     * @since 1.0-alpha-1
     */
    private Fileset fileset;

    /**
     * Multiple FileSet to upload
     * 
     * @parameter
     * @since 1.0-alpha-1
     */
    private List filesets = new ArrayList( 0 );

    protected void execute( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        this.uploadFileItems( wagon );
        this.uploadFileSets( wagon );
    }

    private void uploadFileSets( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        if ( fileset != null )
        {
            filesets.add( fileset );
        }

        for ( int i = 0; i < filesets.size(); ++i )
        {
            Fileset oneFileset = (Fileset) filesets.get( i );

            if ( StringUtils.isBlank( oneFileset.getDirectory() ) )
            {
                oneFileset.setDirectory( this.project.getBasedir().getAbsolutePath() );
            }

            this.wagonHelpers.upload( wagon, oneFileset, this.getLog() );
        }
    }

    private void uploadFileItems( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        if ( fileItem != null )
        {
            fileItems.add( fileItem );
        }

        for ( int i = 0; i < fileItems.size(); ++i )
        {
            FileItem fileItem = (FileItem) fileItems.get( i );

            this.wagonHelpers.upload( wagon, fileItem, this.getLog() );
        }
    }

}