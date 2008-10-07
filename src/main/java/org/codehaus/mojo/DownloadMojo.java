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

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 * Downloads files that match specified pattern (resourceSrc) to the given destination. 
 * 
 * @author Sherali Karimov
 * @goal download
 */
public class DownloadMojo
    extends AbstractWagonMojo
{
    /**
     * Resource(s) to be downloaded. Can be a file or directory. Also supports wildcards.
     * 
     * @see PathParserUtil#toFiles(String)
     * @parameter expression="${wagon.resourceSrc}" default-value="."
     * 
     */
    protected String resourceSrc;
    
    /**
     * Local path to download the resource to.
     * 
     * For instance: 
     * <ul>
     * <li>src=dir1/dir2 dest=xyz will create xyz/dir2 </li>
     * <li>src=dir1/dir2/* dest=xyz will create xyz/ and put all the content of dir2 there </li>
     * <li>src=dir1/dir2 will create dir2 on the server with all the dir2 content</li>
     * </ul>
     * 
     * @parameter expression="${wagon.resourceDest}" default-value="${project.build.directory}/wagon-plugin"
     */
    protected String resourceDest;    
    
    protected void execute( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        final ResourceDescriptor descr = new ResourceDescriptor( resourceSrc, isCaseSensitive );
        
        List fileList = wagon.getFileList( descr.path );

        for ( Iterator iterator = fileList.iterator(); iterator.hasNext(); )
        {
            String fullPath = (String) iterator.next();
            String fileName = FilenameUtils.getName( fullPath );

            File destination = new File( resourceDest + "/" + fileName );

            if ( !iterator.hasNext() && descr.path.endsWith( fileName ) )
            {
                wagon.get( descr.path, destination ); // the source path points at a single file
            }
            else if ( descr.wildcard == null || descr.isMatch( fileName ) )
            {
                wagon.get( descr.path + "/" + fileName, destination );
            }
        }
    }

}
