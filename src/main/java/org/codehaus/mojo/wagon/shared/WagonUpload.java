package org.codehaus.mojo.wagon.shared;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 * 
 */
public interface WagonUpload
{
    String ROLE = WagonUpload.class.getName();

    /**
     * 
     * @param wagon - a Wagon instance
     * @param fileset
     * @param logger
     * @throws WagonException
     */
    void upload( Wagon wagon, FileSet fileset, Log logger )
        throws WagonException;

}
