package org.codehaus.mojo.wagon.shared;

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
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;

@Component(role = WagonDownload.class, hint = "default")
public class DefaultWagonDownload
    implements WagonDownload
{

    @Override
    public List getFileList( Wagon wagon, WagonFileSet fileSet, Log logger )
        throws WagonException
    {
        logger.info( "Scanning remote file system: " + wagon.getRepository().getUrl() + " ..." );

        WagonDirectoryScanner dirScan = new WagonDirectoryScanner();
        dirScan.setLogger( logger );
        dirScan.setWagon( wagon );
        dirScan.setExcludes( fileSet.getExcludes() );
        dirScan.setIncludes( fileSet.getIncludes() );
        dirScan.setCaseSensitive( fileSet.isCaseSensitive() );
        dirScan.setDirectory( fileSet.getDirectory() );
        if ( fileSet.isUseDefaultExcludes() )
        {
            dirScan.addDefaultExcludes();
        }

        dirScan.scan();

        return dirScan.getFilesIncluded();
    }

    @Override
    public void download( Wagon wagon, WagonFileSet remoteFileSet, Log logger )
        throws WagonException
    {
        List fileList = this.getFileList( wagon, remoteFileSet, logger );

        String url = wagon.getRepository().getUrl() + "/";

        if ( fileList.size() == 0 )
        {
            logger.info( "Nothing to download." );
            return;
        }

        for ( Object aFileList : fileList )
        {
            String remoteFile = (String) aFileList;

            File destination = new File( remoteFileSet.getDownloadDirectory() + "/" + remoteFile );
            destination.getParentFile().mkdirs();

            if ( !StringUtils.isBlank( remoteFileSet.getDirectory() ) )
            {
                remoteFile = remoteFileSet.getDirectory() + "/" + remoteFile;
            }

            logger.info( "Downloading " + url + remoteFile + " to " + destination + " ..." );

            if(!destination.exists()) {
                wagon.get(remoteFile, destination);
            }
        }
    }

    @Override
    public boolean exists( Wagon wagon, String resource )
        throws WagonException
    {
        return wagon.resourceExists( resource );
    }

}
