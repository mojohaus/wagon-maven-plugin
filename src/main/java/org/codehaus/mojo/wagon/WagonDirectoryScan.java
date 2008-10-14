package org.codehaus.mojo.wagon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.SelectorUtils;
import org.codehaus.plexus.util.StringUtils;

public class WagonDirectoryScan
{
    /**
     * Patterns which should be excluded by default.
     * 
     * @see #addDefaultExcludes()
     */
    public static final String[] DEFAULTEXCLUDES = org.codehaus.plexus.util.DirectoryScanner.DEFAULTEXCLUDES;

    /**
     * The wagon
     */
    private Wagon wagon;

    /**
     * Relative to wagon url
     */
    private String directory;

    /** The patterns for the wagon files to be included. */
    private String[] includes;

    /** The patterns for the wagon files to be excluded. */
    private String[] excludes;

    /**
     * Whether or not the file system should be treated as a case sensitive one.
     */
    private boolean isCaseSensitive = true;

    /**
     * The files which matched at least one include and at least one exclude.
     */
    private List filesIncluded = new ArrayList();

    /**
     * Sets the list of include patterns to use. All '/' and '\' characters are replaced by
     * <code>File.separatorChar</code>, so the separator used need not match
     * <code>File.separatorChar</code>.
     * <p>
     * When a pattern ends with a '/' or '\', "**" is appended.
     * 
     * @param includes A list of include patterns. May be <code>null</code>, indicating that all
     *            files should be included. If a non-<code>null</code> list is given, all elements
     *            must be non-<code>null</code>.
     */
    public void setIncludes( String[] includes )
    {
        if ( includes == null )
        {
            this.includes = null;
        }
        else
        {
            this.includes = new String[includes.length];
            for ( int i = 0; i < includes.length; i++ )
            {
                String pattern = includes[i].trim();

                if ( pattern.endsWith( "/" ) )
                {
                    pattern += "**";
                }
                this.includes[i] = pattern;
            }
        }
    }

    /**
     * Sets the list of exclude patterns to use. All '/' and '\' characters are replaced by
     * <code>File.separatorChar</code>, so the separator used need not match
     * <code>File.separatorChar</code>.
     * <p>
     * When a pattern ends with a '/' or '\', "**" is appended.
     * 
     * @param excludes A list of exclude patterns. May be <code>null</code>, indicating that no
     *            files should be excluded. If a non-<code>null</code> list is given, all elements
     *            must be non-<code>null</code>.
     */
    public void setExcludes( String[] excludes )
    {
        if ( excludes == null )
        {
            this.excludes = null;
        }
        else
        {
            this.excludes = new String[excludes.length];
            for ( int i = 0; i < excludes.length; i++ )
            {
                String pattern = excludes[i].trim();

                if ( pattern.endsWith( "/" ) )
                {
                    pattern += "**";
                }
                this.excludes[i] = pattern;
            }
        }
    }

    /**
     * Tests whether or not a name matches against at least one include pattern.
     * 
     * @param name The name to match. Must not be <code>null</code>.
     * @return <code>true</code> when the name matches against at least one include pattern, or
     *         <code>false</code> otherwise.
     */
    private boolean isIncluded( String name )
    {
        for ( int i = 0; i < includes.length; i++ )
        {
            if ( matchPath( includes[i], name, isCaseSensitive ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests whether or not a name matches against at least one exclude pattern.
     * 
     * @param name The name to match. Must not be <code>null</code>.
     * @return <code>true</code> when the name matches against at least one exclude pattern, or
     *         <code>false</code> otherwise.
     */
    protected boolean isExcluded( String name )
    {
        for ( int i = 0; i < excludes.length; i++ )
        {
            if ( matchPath( excludes[i], name, isCaseSensitive ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests whether or not a given path matches a given pattern.
     * 
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The path to match, as a String. Must not be <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed case sensitively.
     * 
     * @return <code>true</code> if the pattern matches against the string, or <code>false</code>
     *         otherwise.
     */
    private static boolean matchPath( String pattern, String str, boolean isCaseSensitive )
    {
        return SelectorUtils.matchPath( pattern, str, isCaseSensitive );
    }

    public void scan()
        throws WagonException
    {
        if ( wagon == null )
        {
            throw new IllegalStateException( "No wagon set" );
        }

        if ( StringUtils.isBlank( directory ) )
        {
            directory = "";
        }

        if ( includes == null )
        {
            // No includes supplied, so set it to 'matches all'
            includes = new String[1];
            includes[0] = "**";
        }

        if ( excludes == null )
        {
            excludes = new String[0];
        }

        filesIncluded = new ArrayList();

        scandir( directory );

        Collections.sort( filesIncluded );

    }

    /**
     * Scans the given directory for files and directories. Found files and directories are placed
     * in their respective collections, based on the matching of includes, excludes, and the
     * selectors. When a directory is found, it is scanned recursively.
     * 
     * @param dir The directory to scan. Must not be <code>null</code>.
     * @param vpath The path relative to the base directory (needed to prevent problems with an
     *            absolute path when using dir). Must not be <code>null</code>.
     * @param fast Whether or not this call is part of a fast scan.
     * @throws IOException
     * 
     * @see #filesIncluded
     */
    private void scandir( String fromPath )
        throws WagonException
    {
        List files = wagon.getFileList( fromPath );

        for ( Iterator iterator = files.iterator(); iterator.hasNext(); )
        {
            String filePath = (String) iterator.next();

            if ( filePath.endsWith( "." ) ) //including ".."
            {
                continue;
            }

            if ( !StringUtils.isBlank( fromPath ) )
            {
                if ( fromPath.endsWith( "/" ) )
                {
                    filePath = fromPath + filePath;
                }
                else
                {
                    filePath = fromPath + "/" + filePath;
                }
            }

            
            if ( this.isDirectory( filePath ) )
            {
                //append an ending slash so that we can perform directory exclude
                if ( !filePath.endsWith( "/" ) )
                {
                    filePath += "/";
                }

                String relativePath = this.relativePath( filePath );

                if ( isIncluded( relativePath ) )
                {
                    if ( !isExcluded( relativePath ) )
                    {
                        scandir( filePath );
                    }
                }
            }
            else
            {
                String relativePath = this.relativePath( filePath );

                if ( isIncluded( relativePath ) )
                {
                    if ( !isExcluded( relativePath ) )
                    {
                        filesIncluded.add( relativePath );
                    }
                }
            }
        }
    }
    
    private String relativePath ( String filePath )
    {
        if ( this.isCaseSensitive )
        {
            if ( this.directory.equalsIgnoreCase( filePath ) )
            {
                return "";
            }
            
            return filePath.substring( this.directory.length() +1 );
        }
        else
        {
            if ( this.directory.equals( filePath ) )
            {
                return "";
            }
            
            return filePath.substring( this.directory.length() + 1 );
        }
    }

    private boolean isDirectory( String existedRemotePath )
        throws WagonException
    {
        if ( existedRemotePath.endsWith( "/" ) )
        {
            return true;
        }

        return wagon.resourceExists( existedRemotePath + "/" );
    }

    public List getFilesIncluded()
    {
        return filesIncluded;
    }

    public void setWagon( Wagon wagon )
    {
        this.wagon = wagon;
    }

    public void setCaseSensitive( boolean isCaseSensitive )
    {
        this.isCaseSensitive = isCaseSensitive;
    }

    public void setBasePath( String basePath )
    {
        this.directory = basePath;
    }

}
