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

import java.io.File;
import java.util.Arrays;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 * Upload multiple sets of files.
 * 
 * @author Sherali Karimov
 * @author Dan T. Tran
 * @goal upload
 * @requiresProject true
 */
public class UploadMojo
    extends AbstractWagonMojo
{
    /**
     * Local directory to upload to wagon url
     * @parameter expression="${wagon.fromDir}" default-value="${project.basedir}"
     */
    private File fromDir;
    
    /**
     * Comma separate list of ocalDirectory's Ant excludes
     * @parameter expression="${wagon.excludes}" 
     * l
     */
    private String [] excludes;
    
    /**
     * Comma separate list of ocalDirectory's Ant includes
     * @parameter expression="${wagon.includes}" 
     * localDirectory's Ant includes
     */
    private String [] includes;
    
    /**
     * Follow local symbolic link if possible
     * @parameter expression="${wagon.followSymLink}" default-value="true" 
     */
    private boolean  followSymLink = false;

    /**
     * User default exclude sets
     * @parameter expression="${wagon.userDefaultExcludes}" default-value="true" 
     */
    private boolean  userDefaultExcludes = true;
    
    
    /**
     * @parameter expression="${wagon.todir}" default-value="";
     */
    private String toDir = "";
    
    
    /**
     * @component
     */
    protected WagonUpload wagonUpload;

    
    protected void execute( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        Fileset fileSet = new Fileset();
        
        fileSet.setDirectory( this.fromDir.getAbsolutePath() );
        
        if ( this.includes != null )
        {
            fileSet.setIncludes( Arrays.asList( this.includes ) );
        }

        if ( this.excludes != null )
        {
            fileSet.setIncludes( Arrays.asList( this.excludes ) );
        }
        
        fileSet.setFollowSymlinks( this.followSymLink );
        
        fileSet.setUseDefaultExcludes( this.userDefaultExcludes );
        
        fileSet.setOutputDirectory( toDir );

        this.wagonUpload.upload( wagon, fileSet, this.getLog() );
    }

}