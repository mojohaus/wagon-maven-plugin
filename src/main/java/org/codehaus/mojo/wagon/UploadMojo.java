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
import java.io.IOException;
import java.util.Arrays;

import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.mojo.wagon.shared.WagonUpload;
import org.codehaus.plexus.util.StringUtils;

/**
 * Upload multiple sets of files.
 * 
 * @goal upload
 * @requiresProject true
 */
public class UploadMojo
    extends AbstractSingleWagonMojo
{
    /**
     * Local directory to upload to wagon's "url/toDir"
     * @parameter expression="${wagon.fromDir}" default-value="${project.basedir}"
     */
    private File fromDir;
    
    /**
     * Comma separate list of Ant's excludes to scan for local files
     * @parameter expression="${wagon.excludes}" 
     * 
     */
    private String excludes;
    
    /**
     * Comma separate list of Ant's includes to scan for local files
     * @parameter expression="${wagon.includes}" 
     * localDirectory's Ant includes
     */
    private String  includes;
    
    /**
     * Follow local symbolic link if possible
     * @parameter expression="${wagon.followSymLink}" default-value="false" 
     */
    private boolean  followSymLink = false;

    /**
     * Use default exclude sets
     * @parameter expression="${wagon.useDefaultExcludes}" default-value="true" 
     */
    private boolean  useDefaultExcludes = true;
    
    /**
     * Remote path relative to Wagon's url to upload local files to.
     * 
     * @parameter expression="${wagon.toDir}" default-value="";
     */
    private String toDir = "";
    
    /**
     * Optimize the upload by locally compressed all files in one bundle, 
     * upload the bundle, and finally remote uncompress the bundle.
     * 
     * @parameter expression="${wagon.optimize}" default-value="false";
     */
    
    private boolean optimize = false;
    
    /**
     * @component
     */
    protected WagonUpload wagonUpload;

    
    protected void execute( Wagon wagon )
        throws WagonException, IOException
    {
        FileSet fileSet = new FileSet();
        
        fileSet.setDirectory( this.fromDir.getAbsolutePath() );
        
        if ( ! StringUtils.isBlank( includes ) )
        {
            fileSet.setIncludes( Arrays.asList( StringUtils.split( this.includes, "," ) ) );
        }
        
        if ( ! StringUtils.isBlank( excludes ) )
        {
            fileSet.setExcludes( Arrays.asList( StringUtils.split( this.excludes, "," ) ) );
        }
        
        fileSet.setFollowSymlinks( this.followSymLink );
        
        fileSet.setUseDefaultExcludes( this.useDefaultExcludes );
        
        fileSet.setOutputDirectory( toDir );

        this.wagonUpload.upload( wagon, fileSet, optimize, this.getLog() );
    }

}