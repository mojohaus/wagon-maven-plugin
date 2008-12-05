package org.codehaus.mojo.wagon.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.apache.maven.wagon.CommandExecutor;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

/**
 * @plexus.component role="org.codehaus.mojo.wagon.shared.WagonUpload" role-hint="default"
 */

public class DefaultWagonUpload
    implements WagonUpload
{
    public void upload( Wagon wagon, FileSet fileset, Log logger )
        throws WagonException
    {

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

    public void upload( Wagon wagon, FileSet fileset, boolean optimize, Log logger )
        throws WagonException, IOException
    {
        if ( ! optimize)
        {
            upload( wagon, fileset, logger );
            return ;
        }
        
        
        logger.info( "Uploading " + fileset );

        File zipFile;
        zipFile = File.createTempFile( "wagon", ".zip" );

        try
        {
            FileSetManager fileSetManager = new FileSetManager( logger, logger.isDebugEnabled() );
            String[] files = fileSetManager.getIncludedFiles( fileset );
            logger.info( "Creating " + zipFile + " ..." );
            createZip( files, zipFile, fileset.getDirectory() );

            String remoteFileName = zipFile.getName();
            
            String remoteFile = zipFile.getName();
            String remoteDir = fileset.getOutputDirectory();
            if ( !StringUtils.isBlank( remoteDir ) )
            {
                remoteFile = remoteDir + "/" + remoteFile;
            }            

            logger.info( "Uploading " + zipFile + " to " + wagon.getRepository().getUrl() + "/" + remoteFile + " ..." );
            wagon.put( zipFile, remoteFile );
            
            String targetRepoBaseDirectory = wagon.getRepository().getBasedir();  
            
            // We use the super quiet option here as all the noise seems to kill/stall the connection
            String command = "unzip -o -qq -d " + targetRepoBaseDirectory + " " + targetRepoBaseDirectory + "/" + remoteFileName;

            logger.info( "Remote: " + command );
            ( (CommandExecutor) wagon ).executeCommand( command );

            command = "rm -f " + targetRepoBaseDirectory + "/" + remoteFileName ;
            logger.info( "Remote: " + command );

            ( (CommandExecutor) wagon ).executeCommand( command );
        }
        finally
        {
            zipFile.delete();
        }

    }

    private static void createZip( String[] files, File zipName, String basedir )
        throws IOException
    {
        ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( zipName ) );

        try
        {
            for ( int i = 0; i < files.length; i++ )
            {
                String file = (String) files[i];

                file = file.replace( '\\', '/' );

                writeZipEntry( zos, new File( basedir, file ), file );
            }
        }
        finally
        {
            IOUtil.close( zos );
        }
    }

    private static void writeZipEntry( ZipOutputStream jar, File source, String entryName )
        throws IOException
    {
        byte[] buffer = new byte[1024];

        int bytesRead;

        FileInputStream is = new FileInputStream( source );

        try
        {
            ZipEntry entry = new ZipEntry( entryName );

            jar.putNextEntry( entry );

            while ( ( bytesRead = is.read( buffer ) ) != -1 )
            {
                jar.write( buffer, 0, bytesRead );
            }
        }
        finally
        {
            is.close();
        }
    }

}
