package org.codehaus.mojo.wagon;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 * 
 * @author Dan T. Tran
 *
 */
public interface WagonHelpers
{
    String ROLE = WagonHelpers.class.getName();

    /**
     * 
     * @param wagon - a Wagon instance
     * @param remotePath - directory or file relative to the wagon's url
     * @param recursive -  list all
     * @param logger
     * @return a list of files at the remote host
     * @throws WagonException
     */
    List getFileList( Wagon wagon, String remotePath, boolean recursive, Log logger )
        throws WagonException;

    /**
     * 
     * @param wagon - a Wagon instance
     * @param remotePath - directory or file relative to the wagon's url
     * @param recursive - list all
     * @param downloadDirectory 
     * @param logger
     * @throws WagonException
     */
    void download( Wagon wagon, String remotePath, boolean recursive, File downloadDirectory, Log logger )
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

    /**
     * 
     * @param wagon - a Wagon instance
     * @param fileItem
     * @param logger
     * @throws WagonException
     */
    public void upload( Wagon wagon, FileItem fileItem, Log logger )
        throws WagonException;

}
