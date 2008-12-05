package org.codehaus.mojo.wagon.shared;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 * @author Dan T. Tran
 * 
 * @plexus.component role="org.codehaus.mojo.wagon.shared.WagonUpload" role-hint="zip-scp"
 */

public class WagonUploadZipScp
    implements WagonUpload
{
    public void upload( Wagon wagon, FileSet fileset, Log logger )
        throws WagonException
    {
        logger.info( "Uploading " + fileset );

        FileSetManager fileSetManager = new FileSetManager( logger, logger.isDebugEnabled() );

        String[] files = fileSetManager.getIncludedFiles( fileset );
        
        //to be done....
        
        //create zip
        
        //upload the zip
        
        //unpack the zip at remote site
    }

}
