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

/**
 * @plexus.component role="org.codehaus.mojo.wagon.shared.WagonUpload" role-hint="zip-scp"
 */

public class WagonUploadZipScp
    implements WagonUpload
{
    public void upload( Wagon wagon, FileSet fileset, Log logger )
        throws WagonException, IOException
    {
        logger.info( "Uploading " + fileset );

        File zipFile;
        zipFile = File.createTempFile( "wagon", ".zip" );

        try
        {
            FileSetManager fileSetManager = new FileSetManager( logger, logger.isDebugEnabled() );
            String[] files = fileSetManager.getIncludedFiles( fileset );
            createZip( files, zipFile, fileset.getDirectory() );

            String zipFileName = zipFile.getName();
            String targetRepoBaseDirectory = fileset.getOutputDirectory();

            wagon.put( zipFile, targetRepoBaseDirectory + "/" + zipFileName );

            logger.info( "Unpacking zip file on the target machine." );

            // We use the super quiet option here as all the noise seems to kill/stall the connection
            String command = "unzip -o -qq -d " + targetRepoBaseDirectory + " " + targetRepoBaseDirectory + "/"
                + zipFileName;

            ( (CommandExecutor) wagon ).executeCommand( command );

            logger.info( "Deleting zip file from the target repository." );

            command = "rm -f " + targetRepoBaseDirectory + "/" + zipFile.getName();

            ( (CommandExecutor) wagon ).executeCommand( command );
        }
        finally
        {
            zipFile.delete();
        }

    }

    public static void createZip( String[] files, File zipName, String basedir )
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
