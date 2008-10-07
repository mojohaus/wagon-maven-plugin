package org.codehaus.mojo;

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
    public static void scan( Wagon wagon, String basePath, List collected, boolean recursive, boolean hasDirectoryIndicator,
                      Log logger )
        throws WagonException
    {
        logger.debug( "scanning " + basePath + " ..." );
        List files = wagon.getFileList( basePath );

        if ( files.isEmpty() )
        {
            logger.debug( "Found empty directory: " + basePath );
            return;
            //collected.add( basePath );
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

                if ( hasDirectoryIndicator )
                {
                    if ( file.endsWith( "/" ) )
                    {
                        collected.add( fileResource );
                        continue;
                    }
                }

                try
                {
                    //assume the entry is a directory 
                    if ( recursive )
                    {
                        scan( wagon, dirResource, collected, recursive, hasDirectoryIndicator, logger );
                    }
                    else
                    {
                        //just want to determine if it is a file or directory
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
