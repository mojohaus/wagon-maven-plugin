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
import java.io.IOException;
import java.util.Arrays;

import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.apache.maven.wagon.CommandExecutor;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = WagonUpload.class, hint = "default")
public class DefaultWagonUpload
    implements WagonUpload
{

    private static final Logger LOG = LoggerFactory.getLogger(DefaultWagonUpload.class);

    @Requirement
    private ArchiverManager archiverManager;

    public void upload( Wagon wagon, FileSet fileset )
        throws WagonException
    {

        FileSetManager fileSetManager = new FileSetManager( LOG, LOG.isDebugEnabled() );

        String[] files = fileSetManager.getIncludedFiles( fileset );
        Arrays.sort(files);

        String url = wagon.getRepository().getUrl() + "/";

        if ( files.length == 0 )
        {
            LOG.info( "Nothing to upload." );
            return;
        }

        for ( String file : files )
        {
            String relativeDestPath = StringUtils.replace( file, "\\", "/" );

            if ( !StringUtils.isBlank( fileset.getOutputDirectory() ) )
            {
                relativeDestPath = fileset.getOutputDirectory() + "/" + relativeDestPath;
            }

            File source = new File( fileset.getDirectory(), file );

            LOG.info( "Uploading " + source + " to " + url + relativeDestPath + " ..." );

            wagon.put( source, relativeDestPath );
        }

    }

    @Override
    public void upload( Wagon wagon, FileSet fileset, boolean optimize )
        throws WagonException, IOException
    {
        if ( !optimize )
        {
            upload( wagon, fileset );
            return;
        }

        if ( !( wagon instanceof CommandExecutor ) )
        {
            throw new UnsupportedProtocolException( "Wagon " + wagon.getRepository().getProtocol()
                + " does not support optimize upload" );
        }

        LOG.info( "Uploading " + fileset );

        File zipFile;
        zipFile = File.createTempFile( "wagon", ".zip" );

        try
        {
            FileSetManager fileSetManager = new FileSetManager( LOG, LOG.isDebugEnabled() );
            String[] files = fileSetManager.getIncludedFiles( fileset );

            if ( files.length == 0 )
            {
                LOG.info( "Nothing to upload." );
                return;
            }

            LOG.info( "Creating " + zipFile + " ..." );
            createZip( files, zipFile, fileset.getDirectory() );

            String remoteFile = zipFile.getName();
            String remoteDir = fileset.getOutputDirectory();
            if ( !StringUtils.isBlank( remoteDir ) )
            {
                remoteFile = remoteDir + "/" + remoteFile;
            }

            LOG.info( "Uploading " + zipFile + " to " + wagon.getRepository().getUrl() + "/" + remoteFile + " ..." );
            wagon.put( zipFile, remoteFile );

            // We use the super quiet option here as all the noise seems to kill/stall the connection
            String command = "unzip -o -qq -d " + remoteDir + " " + remoteFile;
            if ( StringUtils.isBlank( remoteDir ) )
            {
                command = "unzip -o -qq " + remoteFile;
            }

            try
            {
                LOG.info( "Remote: " + command );
                ( (CommandExecutor) wagon ).executeCommand( command );
            }
            finally
            {
                command = "rm -f " + remoteFile;
                LOG.info( "Remote: " + command );

                ( (CommandExecutor) wagon ).executeCommand( command );
            }

        }
        finally
        {
            zipFile.delete();
        }

    }

    private void createZip( String[] files, File zipFile, String basedir )
        throws IOException
    {
        try
        {
            ZipArchiver archiver = (ZipArchiver) this.archiverManager.getArchiver( zipFile );
            archiver.setDestFile( zipFile );
            for ( String file : files )
            {
                archiver.addFile( new File( basedir, file ), file );
            }
            archiver.createArchive();
        }
        catch ( NoSuchArchiverException e )
        {
            // should never happen
        }
    }
}
