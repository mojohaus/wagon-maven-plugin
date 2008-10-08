package org.codehaus.mojo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.StringUtils;

public class WagonUtils
{
    public static List getFileList( Wagon wagon, String basePath, boolean recursive, Log logger )
        throws WagonException
    {
        ArrayList fileList = new ArrayList();

        if ( wagon.resourceExists( basePath ) )
        {
            if ( !wagon.resourceExists( basePath + "/" ) )
            {
                fileList.add( basePath );
                return fileList;
            }
        }

        scanRemoteRepo( wagon, basePath, fileList, recursive, logger );

        return fileList;

    }

    public static void scanRemoteRepo( Wagon wagon, String basePath, List collected, boolean recursive, Log logger )
        throws WagonException
    {
        logger.debug( "scanning " + basePath + " ..." );

        List files = wagon.getFileList( basePath );

        if ( files.isEmpty() )
        {
            logger.debug( "Found empty directory: " + basePath );
            return;
        }
        else
        {
            for ( Iterator iterator = files.iterator(); iterator.hasNext(); )
            {
                String file = (String) iterator.next();

                if ( file.endsWith( "." ) ) //including ".."
                {
                    continue;
                }

                String dirResource = null;

                //convert an entry to directory path and scan

                if ( StringUtils.isEmpty( basePath ) )
                {
                    dirResource = file;
                }
                else
                {
                    if ( basePath.endsWith( "/" ) )
                    {
                        dirResource = basePath + file;
                    }
                    else
                    {
                        dirResource = basePath + "/" + file;
                    }
                }

                if ( !dirResource.endsWith( "/" ) )
                {
                    dirResource += "/"; //force a directory scan
                }

                String fileResource = dirResource.substring( 0, dirResource.length() - 1 );

                try
                {
                    //assume the entry is a directory 
                    if ( recursive )
                    {
                        scanRemoteRepo( wagon, dirResource, collected, recursive, logger );
                    }
                    else
                    {
                        //just want to determine if it is a file or directory by checking for exception
                        wagon.getFileList( dirResource );
                    }
                }
                catch ( ResourceDoesNotExistException e )
                {
                    //directory scan fails so it must be a file
                    logger.debug( "Found file " + fileResource );
                    collected.add( fileResource );
                }

                catch ( TransferFailedException e )
                {
                    //until WAGON-245 is fixed
                    logger.debug( "Found file " + fileResource );
                    collected.add( fileResource );
                }

            }
        }
    }

}
