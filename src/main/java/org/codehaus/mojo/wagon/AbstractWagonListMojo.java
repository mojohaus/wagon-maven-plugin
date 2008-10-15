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
     * @parameter expression="${wagon.fromDir}" default-value=""
     */
    protected String fromDir = "";

    /**
     * Comma separated list of Ant's includes to scan for remote files     
     * @parameter expression="${wagon.includes}" default-value="*";
     */
    protected String includes;

    /**
     * Comma separated list of Ant's excludes to scan for remote files     
     * @parameter expression="${wagon.excludes}"
     * 
     */
    protected String excludes;

    /**
     * Whether to consider remote path case sensitivity during scan
     * @parameter expression="${wagon.isCaseSensitive}"
     */
    protected boolean isCaseSensitive = true;

    /**
     * @component
     */
    protected WagonDownload wagonDownload;
    
    
    protected WagonFileSet getWagonFileSet()
    {
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
