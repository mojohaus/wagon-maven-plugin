package org.codehaus.mojo.wagon;

import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 * 
 * @author Dan T. Tran
 */
public interface WagonHelpers
{
    String ROLE = WagonHelpers.class.getName();

    /**
     * @param wagon - a Wagon instance
     * @param fileSet - Criteria to build the list
     * @param logger
     * @return a list of files at the remote host relative to RemoteFileSet's directory
     * @throws WagonException
     */
    List getFileList( Wagon wagon, WagonFileSet fileSet, Log logger )
        throws WagonException;

    /**
     * 
     * @param wagon - a Wagon instance
     * @param remoteFileSet - 
     * @param logger
     * @throws WagonException
     */
    public void download( Wagon wagon, WagonFileSet remoteFileSet, Log logger )
        throws WagonException;

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
