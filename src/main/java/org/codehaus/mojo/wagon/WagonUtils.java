package org.codehaus.mojo.wagon;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Dan T. Tran
 * 
 * @plexus.component role="org.codehaus.mojo.wagon.WagonHelpers" role-hint="default"
 */

public class WagonUtils
    implements WagonHelpers
{
    public List getFileList( Wagon wagon, RemoteFileSet fileSet, Log logger )
        throws WagonException
    {
        WagonDirectoryScan dirScan = new WagonDirectoryScan();
        dirScan.setWagon( wagon );
        dirScan.setExcludes( fileSet.getExcludes() );
        dirScan.setIncludes( fileSet.getIncludes() );
        dirScan.setCaseSensitive( fileSet.isCaseSensitive() );
        dirScan.setBasePath( fileSet.getRemotePath() );

        dirScan.scan();

        return dirScan.getFilesIncluded();
    }

    public void download( Wagon wagon, RemoteFileSet remoteFileSet, Log logger )
        throws WagonException
    {
        List fileList = this.getFileList( wagon, remoteFileSet, logger );

        String url = wagon.getRepository().getUrl() + "/";

        for ( Iterator iterator = fileList.iterator(); iterator.hasNext(); )
        {
            String remoteFile = (String) iterator.next();

            File destination = new File( remoteFileSet.getDownloadDirectory() + "/" + remoteFile );

            if ( ! StringUtils.isBlank( remoteFileSet.getRemotePath() ) )
            {
                remoteFile = remoteFileSet.getRemotePath() + "/" + remoteFile;
            }
            
            logger.info( "Downloading " + url + remoteFile + " to " + destination + " ..." );
            wagon.get( remoteFile, destination );
        }
    }

    public void upload( Wagon wagon, FileSet fileset, Log logger )
        throws WagonException
    {
        logger.info( "Uploading " + fileset );

        FileSetManager fileSetManager = new FileSetManager( logger, logger.isDebugEnabled() );

        String[] files = fileSetManager.getIncludedFiles( fileset );

        String url = wagon.getRepository().getUrl() + "/";

        for ( int i = 0; i < files.length; ++i )
        {
            String relativeDestPath = StringUtils.replace( files[i], "\\", "/" );

            if ( !StringUtils.isBlank( fileset.getOutputDirectory() ) )
            {
                relativeDestPath = fileset.getOutputDirectory() + "/" + relativeDestPath;
            }

            File source = new File( fileset.getDirectory(), files[i] );

            logger.info( "Uploading " + source + " to " + url + relativeDestPath + " ..." );

            wagon.put( source, relativeDestPath );
        }

    }

}
