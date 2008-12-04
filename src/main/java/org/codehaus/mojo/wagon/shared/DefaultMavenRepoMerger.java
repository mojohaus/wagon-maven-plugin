package org.codehaus.mojo.wagon.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * A copy of stage's plugin RepositoryCopier but use WagonUpload and WagonDownload instead
 * 
 * @plexus.component role="org.codehaus.mojo.wagon.shared.MavenRepoCopier" role-hint="default"
 */

public class DefaultMavenRepoMerger
    implements MavenRepoMerger
{
    /**
     * @plexus.requirement role="org.codehaus.mojo.wagon.shared.WagonDownload"

     */
    private WagonDownload downloader;

    /**
     * @plexus.requirement role="org.codehaus.mojo.wagon.shared.WagonUpload"
     */
    private WagonUpload uploader;

    public void merge( Wagon src, Wagon target, Log logger )
        throws WagonException, IOException
    {

        String tempdir = System.getProperty( "java.io.tmpdir" );

        //copy src to a local dir
        File downloadSrcDir = File.createTempFile( tempdir, "wagon" );
        downloadSrcDir.delete();

        WagonFileSet srcFileSet = new WagonFileSet();
        srcFileSet.setDownloadDirectory( downloadSrcDir );
        String[] excludes = { "**/.index" };
        srcFileSet.setExcludes( excludes );

        try
        {
            downloader.download( src, srcFileSet, logger );

            //merge metadata
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( downloadSrcDir );
            String[] includes = { "**/" + MAVEN_METADATA };
            scanner.setIncludes( includes );
            scanner.scan();
            String[] files = scanner.getIncludedFiles();

            for ( int i = 0; i < files.length; ++i )
            {
                File srcMetadaFile = new File( downloadSrcDir, files[i] + IN_PROCESS_MARKER );

                try
                {
                    target.get( files[i], srcMetadaFile );
                }
                catch ( ResourceDoesNotExistException e )
                {
                    // We don't have an equivalent on the targetRepositoryUrl side because we have something
                    // new on the sourceRepositoryUrl side so just skip the metadata merging.
                    continue;
                }

                try
                {
                    mergeMetadata( srcMetadaFile );
                }
                catch ( XmlPullParserException e )
                {
                    throw new IOException( "Metadata file is corrupt " + files[i] + " Reason: " + e.getMessage() );
                }

            }

            //upload to target
            FileSet tobeUploadedFileSet = new FileSet();
            tobeUploadedFileSet.setDirectory( downloadSrcDir.getAbsolutePath() );

            this.uploader.upload( target, tobeUploadedFileSet, logger );

        }
        finally
        {
            FileUtils.deleteDirectory( downloadSrcDir );
        }

    }

    private void mergeMetadata( File existingMetadata )
        throws IOException, XmlPullParserException
    {

        Writer existingMetadataWriter = null;
        Reader existingMetadataReader = null;
        Reader stagedMetadataReader = null;
        File stagedMetadataFile = null;

        try
        {
            MetadataXpp3Reader xppReader = new MetadataXpp3Reader();
            MetadataXpp3Writer xppWriter = new MetadataXpp3Writer();

            // Existing Metadata in target stage
            existingMetadataReader = new FileReader( existingMetadata );
            Metadata existing = xppReader.read( existingMetadataReader );

            // Staged Metadata
            stagedMetadataFile = new File( existingMetadata.getParentFile(), MAVEN_METADATA );
            stagedMetadataReader = new FileReader( stagedMetadataFile );
            Metadata staged = xppReader.read( stagedMetadataReader );

            // Merge
            existing.merge( staged );
            existingMetadataWriter = new FileWriter( existingMetadata );
            xppWriter.write( existingMetadataWriter, existing );

            stagedMetadataFile.delete();
        }
        finally
        {
            IOUtil.close( existingMetadataWriter );
            IOUtil.close( stagedMetadataReader );
            IOUtil.close( existingMetadataReader );

            if ( stagedMetadataFile != null )
            {
                stagedMetadataFile.delete();
            }
        }

        // Mark all metadata as in-process and regenerate the checksums as they will be different
        // after the merger

        try
        {
            File newMd5 = new File( existingMetadata.getParentFile(), MAVEN_METADATA + ".md5" + IN_PROCESS_MARKER );
            FileUtils.fileWrite( newMd5.getAbsolutePath(), checksum( existingMetadata, MD5 ) );
            File oldMd5 = new File( existingMetadata.getParentFile(), MAVEN_METADATA + ".md5" );
            oldMd5.delete();

            File newSha1 = new File( existingMetadata.getParentFile(), MAVEN_METADATA + ".sha1" + IN_PROCESS_MARKER );
            FileUtils.fileWrite( newSha1.getAbsolutePath(), checksum( existingMetadata, SHA1 ) );
            File oldSha1 = new File( existingMetadata.getParentFile(), MAVEN_METADATA + ".sha1" );
            oldSha1.delete();
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( e );
        }

        // We have the new merged copy so we're good

    }

    private String checksum( File file, String type )
        throws IOException, NoSuchAlgorithmException
    {
        MessageDigest md5 = MessageDigest.getInstance( type );

        InputStream is = new FileInputStream( file );

        byte[] buf = new byte[8192];

        int i;

        while ( ( i = is.read( buf ) ) > 0 )
        {
            md5.update( buf, 0, i );
        }

        IOUtil.close( is );

        return encode( md5.digest() );
    }

    private String encode( byte[] binaryData )
    {
        if ( binaryData.length != 16 && binaryData.length != 20 )
        {
            int bitLength = binaryData.length * 8;
            throw new IllegalArgumentException( "Unrecognised length for binary data: " + bitLength + " bits" );
        }

        String retValue = "";

        for ( int i = 0; i < binaryData.length; i++ )
        {
            String t = Integer.toHexString( binaryData[i] & 0xff );

            if ( t.length() == 1 )
            {
                retValue += ( "0" + t );
            }
            else
            {
                retValue += t;
            }
        }

        return retValue.trim();
    }

}
