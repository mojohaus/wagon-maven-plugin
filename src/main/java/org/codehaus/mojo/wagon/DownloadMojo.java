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
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Downloads file(s) . 
 * 
 * @author Sherali Karimov
 * @goal download
 */
public class DownloadMojo
    extends AbstractWagonMojo
{

    /**
     * Download all files recursively 
     * @parameter expression="${wagon.recursive}" default-value="true"
     */
    private boolean recursive;

    /**
     * Path after the url, can be a file or directory
     * @parameter expression="${wagon.remoteResource}" default-value=""
     */
    private String remoteResource;
    
    /**
     * Local path to download the remote resource ( tree ) to.
     * 
     * @parameter expression="${wagon.downloadDirectory}" default-value="${project.build.directory}/wagon-plugin"
     */
    private File downloadDirectory;

    protected void execute( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        this.wagonHelpers.download( wagon, remoteResource, recursive, downloadDirectory, this.getLog() );
    }

}
