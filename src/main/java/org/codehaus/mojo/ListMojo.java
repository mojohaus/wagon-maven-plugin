package org.codehaus.mojo;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Lists the content of the specified directory (resourceSrc) under a specified repository (url) according to the given wildcard (part of resourceSrc).
 * Wildcard can be turned on and off as required.
 * 
 * @author Sherali Karimov
 * @goal list
 */
public class ListMojo
    extends AbstractWagonMojo
{
    /**
     * If true, applies the provided wildcard to the list of files before printing simulating the download mojo's behavior. Otherwise prints the full list.
     * 
     * @parameter expression="${wagon.applyWildcard}" default-value="false"
     */
    private boolean applyWildcard;

    /**
     * 
     * @parameter expression="${wagon.recursive}" default-value="true"
     */
    private boolean recursive;

    /**
     * Resource(s) to be listed. Can be a file or directory. Also supports
     * wildcards.
     * 
     * @see PathParserUtil#toFiles(String)
     * @parameter expression="${wagon.resourceSrc}" 
     */
    private String resourceSrc;
    
    /**
     * The list return from the protocol has a ending slash to indicate a directory.
     * The value is automatically discoverred
     */
    private boolean hasDirectoryIndicator = false;

    protected void execute( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        List files = new ArrayList();

        if ( resourceSrc == null )
        {
            resourceSrc = "";
        }

        scan( wagon, resourceSrc, files );

        //final ResourceDescriptor descr = new ResourceDescriptor( resourceSrc, isCaseSensitive );
        //List fileList = wagon.getFileList( descr.path );
        //getLog().info( "Listing: " + descr.path );
        for ( Iterator iterator = files.iterator(); iterator.hasNext(); )
        {
            String file = (String) iterator.next();
            //if ( !applyWildcard || descr.wildcard == null || descr.isMatch( file ) )
            {
                getLog().info( "\t" + file );
            }
        }
    }

    private void scan( Wagon wagon, String basePath, List collected )
        throws WagonException
    {
        this.getLog().debug( "scanning " + basePath + " ..." );
        List files = wagon.getFileList( basePath );

        if ( files.isEmpty() )
        {
            this.getLog().debug( "Found empty directory: " + basePath );
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

                if ( ! dirResource.endsWith( "/" ))
                {
                    dirResource += "/"; //force a directory scan
                }
                
                String fileResource = dirResource.substring( 0, dirResource.length() - 1  );

                if ( this.hasDirectoryIndicator  )
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
                    if ( this.recursive )
                    {
                        scan( wagon, dirResource, collected );
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
                    this.getLog().debug( "Found file " + fileResource );
                    collected.add( fileResource ); 
                }
                catch ( TransferFailedException e )
                {
                    //until WAGON-245 is fixed
                    this.getLog().debug( "Found file " + fileResource );
                    collected.add( fileResource );
                }
            }
        }
    }
    
    

}
