package org.codehaus.mojo.wagon.shared;

import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 * 
 * @author Dan T. Tran
 */
public interface WagonCopy
{
    String ROLE = WagonCopy.class.getName();

    /**
     * Transfer files between wagon URL.  If download directory is not given in the fileset
     * a temporary one will be created.
     * 
     * @param fromWagon - source Wagon
     * @param fileset
     * @param toWagon - target Wagon
     * @param logger
     * @throws WagonException
     */
    void copy( Wagon fromWagon, WagonFileSet fileset, Wagon toWargon, Log logger )
        throws WagonException, IOException;

}
