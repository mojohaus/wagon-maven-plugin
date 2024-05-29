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

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.mojo.wagon.shared.ContinuationType;
import org.codehaus.mojo.wagon.shared.WagonFileSet;

/**
 * Transfers a set of files from a remote URL to a specified local directory.
 */
@Mojo( name = "download" , requiresProject = false)
public class DownloadMojo
    extends AbstractWagonListMojo
{

    /**
     * Local directory to download the remote resource ( tree ) to.
     */
    @Parameter( property = "wagon.toDir", defaultValue = "${project.build.directory}/wagon-plugin")
    private File toDir;


    /**
     * Configure the continuation type
     * When continuation type is ONLY_MISSING, download file from source Wagon that do not exist in toDir directory
     * When continuation type is NONE, download all files
     */
    @Parameter( property = "wagon.continuationType")
    private ContinuationType continuationType = ContinuationType.NONE;

    @Override
    protected void execute( Wagon wagon )
        throws WagonException
    {
        WagonFileSet fileSet = this.getWagonFileSet();
        fileSet.setDownloadDirectory( this.toDir );

        this.wagonDownload.download( wagon, fileSet, this.getLog(), continuationType );
    }

}
