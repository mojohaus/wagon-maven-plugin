package org.codehaus.mojo.wagon.shared;

import java.io.File;


/**
 * Wagon configuration to scan for a set of remote files.
 */
public class WagonFileSet
{
    /**
     * Path after the url, this is where the scan starts
     */
    
    private String directory = "";
    
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
     * User default exclude sets
     */
    private boolean  useDefaultExcludes = true;

    /**
     * Local path to download the remote resource ( tree ) to.
     */
    private File downloadDirectory;

    /**
     * Relative of a remote URL when it used to copy files between 2 URLs.
     */
    private String outputDirectory = "";
    
    //////////////////////////////////////////////////////////////////////////////////////
    
    public String getDirectory()
    {
        return directory;
    }

    public void setDirectory( String remotePath )
    {
        this.directory = remotePath;
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
    
    /**
     * Retrieves the included and excluded files from this file-set's directory.
     * Specifically, <code>"file-set: <I>[directory]</I> (included:
     * <I>[included files]</I>, excluded: <I>[excluded files]</I>)"</code>
     *
     * @return The included and excluded files from this file-set's directory.
     * Specifically, <code>"file-set: <I>[directory]</I> (included:
     * <I>[included files]</I>, excluded: <I>[excluded files]</I>)"</code>
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "file-set: " + getDirectory() + " (included: " + getIncludes() + ", excluded: " + getExcludes() + ")";
    }
    
    public boolean isUseDefaultExcludes()
    {
        return useDefaultExcludes;
    }

    public void setUseDefaultExcludes( boolean useDefaultExcludes )
    {
        this.useDefaultExcludes = useDefaultExcludes;
    }
    
    public String getOutputDirectory()
    {
        return outputDirectory;
    }

    public void setOutputDirectory( String outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }    
    
}
