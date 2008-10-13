package org.codehaus.mojo.wagon;

import java.io.File;


/**
 * Wagon configuration to list/download a set of remote files
 * 
 * @author Dan T. Tran
 */
public class RemoteFileSet
{
    /**
     * Ant's excludes path expression
     */
    private String [] excludes;
    
    /**
     * Ant's includes path expression
     */
    private String [] includes;
    
    /**
     * 
     */
    private boolean caseSensitive;
    

    /**
     * Path after the url, can be a file or directory
     */
    private String remotePath = "";
    
    /**
     * Local path to download the remote resource ( tree ) to.
     */
    private File downloadDirectory;

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
    
    
    public String[] getExcludes()
    {
        return excludes;
    }

    public void setExcludes( String[] excludes )
    {
        this.excludes = excludes;
    }

    public String[] getIncludes()
    {
        return includes;
    }

    public void setIncludes( String[] includes )
    {
        this.includes = includes;
    }

    public boolean isCaseSensitive()
    {
        return caseSensitive;
    }

    public void setCaseSensitive( boolean caseSensitive )
    {
        this.caseSensitive = caseSensitive;
    }
    
}
