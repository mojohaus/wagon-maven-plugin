package org.codehaus.mojo.wagon;

import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

public interface WagonHelpers
{
    String ROLE = WagonHelpers.class.getName();
    
    List getFileList( Wagon wagon, String basePath, boolean recursive, Log logger )
        throws WagonException;
}
