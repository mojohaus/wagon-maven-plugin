package org.codehaus.mojo.wagon.shared;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Dan T. Tran
 * 
 * @plexus.component role="org.codehaus.mojo.wagon.shared.WagonDownload" role-hint="default"
 */

public class WagonDownloadGeneric
    implements WagonDownload
{
    
    public List getFileList( Wagon wagon, WagonFileSet fileSet, Log logger )
        throws WagonException
    {
        WagonDirectoryScan dirScan = new WagonDirectoryScan();
        dirScan.setWagon( wagon );
        dirScan.setExcludes( fileSet.getExcludes() );
        dirScan.setIncludes( fileSet.getIncludes() );
        dirScan.setCaseSensitive( fileSet.isCaseSensitive() );
        dirScan.setDirectory( fileSet.getDirectory() );

        dirScan.scan();

        return dirScan.getFilesIncluded();
    }

    public void download( Wagon wagon, WagonFileSet remoteFileSet, Log logger )
        throws WagonException
    {
        List fileList = this.getFileList( wagon, remoteFileSet, logger );

        String url = wagon.getRepository().getUrl() + "/";

        for ( Iterator iterator = fileList.iterator(); iterator.hasNext(); )
        {
            String remoteFile = (String) iterator.next();

            File destination = new File( remoteFileSet.getDownloadDirectory() + "/" + remoteFile );

            if ( ! StringUtils.isBlank( remoteFileSet.getDirectory() ) )
            {
                remoteFile = remoteFileSet.getDirectory() + "/" + remoteFile;
            }
            
            logger.info( "Downloading " + url + remoteFile + " to " + destination + " ..." );
            
            wagon.get( remoteFile, destination );
        }
    }

}
