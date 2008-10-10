package org.codehaus.mojo.wagon;

import java.io.File;

import org.codehaus.plexus.util.StringUtils;

/**
 * Wagon configuration to download/upload single file with option to change destination name
 * 
 * @author Dan T. Tran
 */
public class FileItem
{
    private File localFile;

    private String remoteDirectory = "";

    private String remoteFileName = "";

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

    public String getRemoteFileName()
    {
        return remoteFileName;
    }

    public void setRemoteFileName( String destFileName )
    {
        this.remoteFileName = destFileName;
    }

    public String getRemoteFilePath()
    {
        if ( StringUtils.isBlank( this.remoteFileName ) )
        {
            this.remoteFileName = localFile.getName();
        }
        
        String remoteFilePath = this.remoteDirectory + "/" + this.remoteFileName;
        
        if ( StringUtils.isBlank( this.remoteDirectory ) )
        {
            remoteFilePath = this.remoteFileName;
        }
        
        return remoteFilePath;
    }

}
