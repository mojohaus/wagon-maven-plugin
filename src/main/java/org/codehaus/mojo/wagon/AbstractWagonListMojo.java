package org.codehaus.mojo.wagon;

import org.codehaus.mojo.wagon.shared.WagonDownload;
import org.codehaus.mojo.wagon.shared.WagonFileSet;
import org.codehaus.plexus.util.StringUtils;

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
     * Comma separated list of Ant's includes to scan for remote files     
     * @parameter
     */
    protected String includes;

    /**
     * Comma separated list of Ant's excludes to scan for remote files     
     * @parameter
     */
    protected String excludes;

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
            //Prevent user from recursively download lots of files
            //only download files at remote root dir
            includes = "*"; 
        }      
        
        WagonFileSet fileSet = new WagonFileSet();
        fileSet.setDirectory( fromDir );
        
        if ( ! StringUtils.isBlank( includes ) )
        {
            fileSet.setIncludes( StringUtils.split( this.includes, "," ) );
        }
        
        if ( ! StringUtils.isBlank( excludes ) )
        {
            fileSet.setExcludes( StringUtils.split( this.excludes, "," ) );
        }
        
        fileSet.setCaseSensitive( this.isCaseSensitive );
        
        return fileSet;
        
    }

}
