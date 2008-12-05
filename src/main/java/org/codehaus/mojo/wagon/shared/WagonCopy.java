package org.codehaus.mojo.wagon.shared;

import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 * 
 */
public interface WagonCopy
{
    String ROLE = WagonCopy.class.getName();

    /**
     * Transfer files between 2 Wagon URLs.  If download directory is not given in the fileset
     * a temporary one will be created.
     * 
     * @param fromWagon - source Wagon
     * @param fileset
     * @param toWagon - target Wagon
     * @param logger
     * @throws WagonException
     */
    void copy( Wagon fromWagon, WagonFileSet fileset, Wagon toWagon, Log logger )
        throws WagonException, IOException;

}
