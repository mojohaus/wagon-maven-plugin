package org.codehaus.mojo.wagon.shared;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.FileUtils;

/**
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

    public void copy( Wagon src, WagonFileSet wagonFileSet, Wagon target, Log logger )
        throws WagonException, IOException
    {
        boolean removeDownloadDir = false;

        if ( wagonFileSet.getDownloadDirectory() == null )
        {
            String tempdir = System.getProperty( "java.io.tmpdir" );
            File downloadSrcDir = File.createTempFile( tempdir, "wagon" );
            downloadSrcDir.delete();
            wagonFileSet.setDownloadDirectory( downloadSrcDir );
            removeDownloadDir = true;
        }

        try
        {
            this.downloader.download( src, wagonFileSet, logger );

            FileSet localFileSet = new FileSet();
            localFileSet.setDirectory( wagonFileSet.getDownloadDirectory().getAbsolutePath() );

            this.uploader.upload( target, localFileSet, logger );
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
