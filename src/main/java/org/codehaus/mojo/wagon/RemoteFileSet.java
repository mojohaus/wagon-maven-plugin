package org.codehaus.mojo.wagon;

import java.io.File;


/**
 * Wagon configuration to download a set of remote files
 * 
 * @author Dan T. Tran
 */
public class RemoteFileSet
{
    /**
     * Download all files recursively 
     */
    private boolean recursive = true;

    /**
     * Path after the url, can be a file or directory
     */
    private String remotePath = "";
    
    /**
     * Local path to download the remote resource ( tree ) to.
     */
    private File downloadDirectory;

    public boolean isRecursive()
    {
        return recursive;
    }

    public void setRecursive( boolean recursive )
    {
        this.recursive = recursive;
    }

    public String getRemotePath()
    {
        return remotePath;
    }

    public void setRemotePath( String remotePath )
    {
        this.remotePath = remotePath;
    }

    public File getDownloadDirectory()
    {
        return downloadDirectory;
    }

    public void setDownloadDirectory( File downloadDirectory )
    {
        this.downloadDirectory = downloadDirectory;
    }
    
    
    
}
