package org.codehaus.mojo.wagon;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

public interface WagonHelpers
{
    String ROLE = WagonHelpers.class.getName();

    List getFileList( Wagon wagon, String basePath, boolean recursive, Log logger )
        throws WagonException;

    void download( Wagon wagon, String basePath, boolean recursive, File downloadDirectory, Log logger )
        throws WagonException;

    void upload( Wagon wagon, FileSet fileset, Log logger )
        throws WagonException;

}
