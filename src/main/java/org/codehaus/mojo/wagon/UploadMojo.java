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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.mojo.wagon.shared.ContinuationType;
import org.codehaus.mojo.wagon.shared.WagonUpload;
import org.codehaus.plexus.util.StringUtils;

/**
 * Upload multiple sets of files.
 */
@Mojo( name = "upload" )
public class UploadMojo
    extends AbstractSingleWagonMojo
{
    /**
     * Local directory to upload to wagon's "url/toDir".
     */
    @Parameter( property = "wagon.fromDir", defaultValue = "${project.basedir}")
    private File fromDir;

    /**
     * Comma separate list of Ant's excludes to scan for local files.
     */
    @Parameter( property = "wagon.excludes")
    private String excludes;

    /**
     * Comma separate list of Ant's includes to scan for local files.
     */
    @Parameter( property = "wagon.includes")
    private String includes;

    /**
     * Follow local symbolic link if possible.
     */
    @Parameter( property = "wagon.followSymLink", defaultValue = "false")
    private boolean followSymLink = false;

    /**
     * Use default exclude sets.
     */
    @Parameter( property = "wagon.useDefaultExcludes", defaultValue = "true")
    private boolean useDefaultExcludes = true;

    /**
     * Remote path relative to Wagon's url to upload local files to.
     */
    @Parameter( property = "wagon.toDir")
    private String toDir = "";

    /**
     * Optimize the upload by locally compressed all files in one bundle, upload the bundle, and finally remote
     * uncompress the bundle.
     */
    @Parameter( property = "wagon.optimize", defaultValue = "false")
    private boolean optimize = false;

    @Component
    protected WagonUpload wagonUpload;

    /**
     * Configure the continuation type
     * When continuation type is ONLY_MISSING, upload files that do not exist in target Wagon
     * When continuation type is NONE, upload all files
     */
    @Parameter( property = "wagon.continuationType" )
    private ContinuationType continuationType = ContinuationType.NONE;

    @Override
    protected void execute( Wagon wagon )
        throws WagonException, IOException
    {
        FileSet fileSet = new FileSet();

        fileSet.setDirectory( this.fromDir.getAbsolutePath() );

        if ( !StringUtils.isBlank( includes ) )
        {
            fileSet.setIncludes( Arrays.asList( StringUtils.split( this.includes, "," ) ) );
        }

        if ( !StringUtils.isBlank( excludes ) )
        {
            fileSet.setExcludes( Arrays.asList( StringUtils.split( this.excludes, "," ) ) );
        }

        fileSet.setFollowSymlinks( this.followSymLink );

        fileSet.setUseDefaultExcludes( this.useDefaultExcludes );

        fileSet.setOutputDirectory( toDir );

        this.wagonUpload.upload( wagon, fileSet, optimize, continuationType );
    }

}
