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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Uploads file(s)
 * 
 * @author Sherali Karimov
 * @goal upload
 */
public class UploadMojo
    extends AbstractWagonMojo
{

    /**
     * A single FileSet to upload.
     *
     * @parameter
     * @since 1.0-alpha-1
     */
    private Fileset fileset;
    
    private boolean verbose = false;

    protected void execute( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        this.processFileSet( wagon, this.fileset );
    }

    private void processFileSet( Wagon wagon, Fileset oneFileSet )
        throws WagonException
    {
        if ( StringUtils.isBlank( oneFileSet.getDirectory() ) )
        {
            oneFileSet.setDirectory( this.project.getBasedir().getAbsolutePath() );
        }

        getLog().info( "uploading " + oneFileSet );

        FileSetManager fileSetManager = new FileSetManager( getLog(), this.verbose );

        String[] files = fileSetManager.getIncludedFiles( oneFileSet );
        
        for ( int i = 0; i < files.length; ++i )
        {
            String relativeDestPath = StringUtils.replace( files[i], "\\", "/" );
            
            if ( !StringUtils.isBlank( oneFileSet.getOutputDirectory() ) )
            {
                relativeDestPath = oneFileSet.getOutputDirectory() + "/" + relativeDestPath;
            }

            File source = new File( oneFileSet.getDirectory(), files[i] );

            wagon.put( source, relativeDestPath );
        }

    }

}