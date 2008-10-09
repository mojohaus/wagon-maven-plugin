package org.codehaus.mojo.wagon;

import java.io.File;

import org.codehaus.plexus.util.StringUtils;

/*
 * Wagon configuration to download/upload single file with option to change destination name
 * 
 * @author Dan T. Tran
 */
public class FileItem
{
    private File localFile;

    private String remoteDirectory;

    private String destFileName;

    public File getLocalFile()
    {
        return localFile;
    }

    public void setLocalFile( File localFile )
    {
        this.localFile = localFile;
    }

    public String getRemoteDirectory()
    {
        return remoteDirectory;
    }

    public void setRemoteDirectory( String remoteDirectory )
    {
        this.remoteDirectory = remoteDirectory;
    }

    public String getDestFileName()
    {
        return destFileName;
    }

    public void setDestFile( String destFileName )
    {
        this.destFileName = destFileName;
    }

    public String getRemoteFilePath()
    {
        if ( StringUtils.isBlank( this.destFileName ) )
        {
            this.destFileName = localFile.getName();
        }
        
        String remoteFilePath = this.remoteDirectory + "/" + this.destFileName;
        
        if ( StringUtils.isBlank( this.remoteDirectory ) )
        {
            remoteFilePath = this.destFileName;
        }
        
        return remoteFilePath;
    }

}
