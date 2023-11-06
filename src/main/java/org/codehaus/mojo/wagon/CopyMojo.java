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

import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.mojo.wagon.shared.ContinuationType;
import org.codehaus.mojo.wagon.shared.WagonCopy;
import org.codehaus.mojo.wagon.shared.WagonFileSet;

/**
 * Copy artifacts from one Wagon repository to another Wagon repository.
 */
@Mojo( name = "copy" , requiresProject = false)
public class CopyMojo
    extends AbstractCopyMojo
{
    /**
     * Directory path relative to source's Wagon
     */
    @Parameter( property = "wagon.fromDir")
    private String fromDir = "";

    /**
     * Comma separated list of Ant's includes to scan for remote files
     */
    @Parameter( property = "wagon.includes", defaultValue = "*")
    private String includes;

    /**
     * Comma separated list of Ant's excludes to scan for remote files
     */
    @Parameter( property = "wagon.excludes")
    private String excludes;

    /**
     * Whether to consider remote path case sensitivity during scan.
     */
    @Parameter( property = "wagon.caseSensitive")
    private boolean caseSensitive = true;

    /**
     * Local directory to store downloaded artifacts. This directory is needed in order to use continuation type ONLY_MISSING during the download from source Wagin.
     * If not provided, the artifacts are downloaded to a temporary directory
     * If not provided, the continuation type ONLY_MISSING will only work during the upload to target
     */
    @Parameter( property = "wagon.downloadDirectory" )
    private File downloadDirectory;

    /**
     * Configure the continuation type.
     * When continuation type is ONLY_MISSING, download file from source Wagon that do not               exist in
     * downloadDirectory and copy files that do not exist in target Wagon
     * When continuation type is NONE, copy all files
     */
    @Parameter( property = "wagon.continuationType" )
    private ContinuationType continuationType = ContinuationType.NONE;

    /**
     * Remote path relative to target's url to copy files to.
     */
    @Parameter( property = "wagon.toDir" )
    private String toDir = "";

    @Component
    private WagonCopy wagonCopy;

    @Override
    protected void copy( Wagon srcWagon, Wagon targetWagon )
        throws IOException, WagonException
    {
        WagonFileSet fileSet = this.getWagonFileSet( fromDir, includes, excludes, caseSensitive, toDir );
        if ( downloadDirectory != null )
        {
            fileSet.setDownloadDirectory(downloadDirectory);
        }
        wagonCopy.copy(srcWagon, fileSet, targetWagon, optimize, this.getLog(), continuationType);
    }

}
