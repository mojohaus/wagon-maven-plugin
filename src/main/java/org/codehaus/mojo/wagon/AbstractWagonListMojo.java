package org.codehaus.mojo.wagon;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.mojo.wagon.shared.WagonDownload;
import org.codehaus.mojo.wagon.shared.WagonFileSet;

/**
 * Contains common configuration to scan for Wagon's files
 * @author dtran
 *
 */
public abstract class AbstractWagonListMojo
    extends AbstractWagonMojo

{
    /**
     * Directory path relative to Wagon's URL
     * @parameter expression="${wagon.fromDir}" defaultValue="";
     */
    protected String fromDir = "";

    /**
     * Ant's inclusion list to scan for remote files
     * @parameter
     */
    protected String[] includes;

    /**
     * Ant's exclusion list to scan for remote files
     * @parameter
     */
    protected String[] excludes;

    /**
     * Whether to consider remote path case sensitivity during scan
     * @parameter default-value="true"
     */
    protected boolean isCaseSensitive = true;

    /**
     * @component
     */
    protected WagonDownload wagonDownload;
    
    
    protected WagonFileSet getWagonFileSet()
    {

        if ( includes == null && excludes == null )
        {
            includes = new String[1];
            includes[0] = "*"; //prevent user from recursively download lots of files
            //only download files at remote root dir
        }      
        
        WagonFileSet fileSet = new WagonFileSet();
        fileSet.setDirectory( fromDir );
        fileSet.setIncludes( this.includes );
        fileSet.setExcludes( this.excludes );
        fileSet.setCaseSensitive( this.isCaseSensitive );
        
        return fileSet;
        
    }

}
