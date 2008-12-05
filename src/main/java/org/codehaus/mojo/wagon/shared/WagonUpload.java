package org.codehaus.mojo.wagon.shared;

import java.io.IOException;

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
     * Upload a set of files via FileSet interface to a remote repository via Wagon 
     * @param wagon - a Wagon instance
     * @param fileset
     * @param logger
     * @param optimize locally compressed and uncompress at the remote site if scp is use
     * @throws WagonException
     */
    public void upload( Wagon wagon, FileSet fileset, boolean optimize, Log logger  )
        throws WagonException, IOException;
}
