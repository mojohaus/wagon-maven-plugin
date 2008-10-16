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
 * @author Dan T. Tran
 * 
 * @plexus.component role="org.codehaus.mojo.wagon.shared.MavenRepoCopier" role-hint="default"
 */

public class MavenRepoCopierGeneric
    implements MavenRepoCopier
{
    private WagonDownload downloader;

    private WagonUpload  uploader;
    
    private MetadataXpp3Reader reader = new MetadataXpp3Reader();

    private MetadataXpp3Writer writer = new MetadataXpp3Writer();

    public void copy( Wagon src, Wagon target, Log logger )
        throws WagonException, IOException
    {

        String tempdir = System.getProperty( "java.io.tmpdir" );

        //copy src to a local dir
        File downloadDir = File.createTempFile( tempdir, "wagon" );
        downloadDir.delete();

        File downloadSrcDir = new File( downloadDir, "src" );
        downloadSrcDir.mkdirs();

        WagonFileSet srcFileSet = new WagonFileSet();
        srcFileSet.setDownloadDirectory( downloadSrcDir );
        downloader.download( src, srcFileSet, logger );

        //copy target's metadata to local dir
        File downloadTargetDir = new File( downloadDir, "target" );
        downloadTargetDir.mkdirs();

        
        //merge metada

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( downloadSrcDir );
        String[] includes = { "**/metadata.xml" };
        scanner.setIncludes( includes );
        scanner.scan();
        String[] files = scanner.getIncludedFiles();

        for ( int i = 0; i < files.length; ++i )
        {
            File emf = new File( downloadSrcDir, files[i] + ".rip" );
            try
            {
                target.get( files[i], emf );
            }
            catch ( ResourceDoesNotExistException e )
            {
                // We don't have an equivalent on the targetRepositoryUrl side because we have something
                // new on the sourceRepositoryUrl side so just skip the metadata merging.

                continue;
            }

            try
            {
                mergeMetadata( emf );
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

    private void mergeMetadata( File existingMetadata )
        throws IOException, XmlPullParserException
    {
        // Existing Metadata in target stage

        Reader existingMetadataReader = new FileReader( existingMetadata );

        Metadata existing = reader.read( existingMetadataReader );

        // Staged Metadata

        File stagedMetadataFile = new File( existingMetadata.getParentFile(), MAVEN_METADATA );

        Reader stagedMetadataReader = new FileReader( stagedMetadataFile );

        Metadata staged = reader.read( stagedMetadataReader );

        // Merge

        existing.merge( staged );

        Writer writer = new FileWriter( existingMetadata );

        this.writer.write( writer, existing );

        IOUtil.close( writer );

        IOUtil.close( stagedMetadataReader );

        IOUtil.close( existingMetadataReader );

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

        stagedMetadataFile.delete();
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

    protected String encode( byte[] binaryData )
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
