package org.codehaus.mojo.wagon;

import org.codehaus.mojo.wagon.shared.WagonDownload;
import org.codehaus.mojo.wagon.shared.WagonFileSet;

/**
 * Contains common configuration to scan for Wagon's files
 */
public abstract class AbstractWagonListMojo
    extends AbstractSingleWagonMojo

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
     * @parameter expression="${wagon.isCaseSensitive}" default-value="true"
     */
    protected boolean isCaseSensitive = true;

    /**
     * @component
     */
    protected WagonDownload wagonDownload;
    
    
    protected WagonFileSet getWagonFileSet()
    {
        return this.getWagonFileSet( fromDir, includes, excludes, isCaseSensitive );
    }

}
