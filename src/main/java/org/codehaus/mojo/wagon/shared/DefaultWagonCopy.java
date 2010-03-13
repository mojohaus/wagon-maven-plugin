package org.codehaus.mojo.wagon.shared;

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

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Copy a set of file from a wagon repo to another wagon repo
 * 
 * @plexus.component role="org.codehaus.mojo.wagon.shared.WagonCopy" role-hint="default"
 */

public class DefaultWagonCopy
    implements WagonCopy
{
    /**
     * @plexus.requirement role="org.codehaus.mojo.wagon.shared.WagonDownload"
     */
    private WagonDownload downloader;

    /**
     * @plexus.requirement role="org.codehaus.mojo.wagon.shared.WagonUpload"
     */
    private WagonUpload uploader;

    public void copy( Wagon src, WagonFileSet wagonFileSet, Wagon target, boolean optimize, Log logger )
        throws WagonException, IOException
    {
        if ( wagonFileSet == null )
        {
            wagonFileSet = new WagonFileSet();
        }
        
        boolean removeDownloadDir = false;

        if ( wagonFileSet.getDownloadDirectory() == null )
        {
            File downloadSrcDir = File.createTempFile( "wagon", "wagon" );
            downloadSrcDir.delete();
            wagonFileSet.setDownloadDirectory( downloadSrcDir );
            removeDownloadDir = true;
        }

        try
        {
            this.downloader.download( src, wagonFileSet, logger );

            FileSet localFileSet = new FileSet();
            localFileSet.setDirectory( wagonFileSet.getDownloadDirectory().getAbsolutePath() );
            localFileSet.setOutputDirectory( wagonFileSet.getOutputDirectory() );
            
            this.uploader.upload( target, localFileSet, optimize, logger );
        }
        finally
        {
            if ( removeDownloadDir )
            {
                FileUtils.deleteDirectory( wagonFileSet.getDownloadDirectory() );
            }
        }

    }
}
