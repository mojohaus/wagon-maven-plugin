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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

/**
 * Download a single file.
 */
@Mojo( name = "download-single", requiresProject = false)
public class DownloadSingleMojo
    extends AbstractSingleWagonMojo
{
    /**
     * Relative path to the base URL. When empty, assume base URL has the full path
     */
    @Parameter( property = "wagon.fromFile")
    private String fromFile;

    /**
     * Directory to download the remote file to.
     */
    @Parameter( property = "wagon.toDir")
    private File toDir;

    /**
     * File to download the remote file to. Use this option to rename the file after download. When toDir is present,
     * this argument is ignored.
     */
    @Parameter( property = "wagon.toFile")
    private File toFile;

    /**
     * Skip download if local file already exists.
     */
    @Parameter( property = "wagon.skipIfExists")
    private boolean skipIfExists;

    @Override
    protected void execute( Wagon wagon )
        throws MojoExecutionException, WagonException
    {

        if ( this.skip )
        {
            this.getLog().info( "Skip execution." );
            return;
        }

        if ( toDir != null )
        {
            toFile = new File( toDir, new File( fromFile ).getName() );
        }

        if ( toFile == null )
        {
            throw new MojoExecutionException( "Either toDir or toFile is required" );
        }

        if ( skipIfExists && toFile.exists() )
        {
            getLog().info("Skip execution - file " + toFile + " already exists." );
            return;
        }
        this.getLog().info( "Downloading: " + wagon.getRepository().getUrl() + "/" + fromFile + " to " + toFile );

        wagon.get( fromFile, toFile );

    }

    @Override
    public void execute()
        throws MojoExecutionException
    {
        if ( StringUtils.isBlank( this.fromFile ) ) {

            //recompute base url and fromFile
            String [] tokens = StringUtils.split( this.url, "/\\" );
            this.fromFile = tokens[tokens.length - 1];
            this.url = url.substring( 0, url.length() - this.fromFile.length() - 1 );
        }

        super.execute();
    }
}
