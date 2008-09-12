package org.codehaus.mojo;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;


/**
 * Lists the content of the specified directory (resourceSrc) under a specified repository (url) according to the given wildcard (part of resourceSrc).
 * Wildcard can be turned on and off as required.
 * 
 * @author Sherali Karimov
 * @goal list
 */
public class ListMojo extends AbstractWagonMojo
{
    /**
     * If true, applies the provided wildcard to the list of files before printing simulating the download mojo's behavior. Otherwise prints the full list.
     * 
     * @parameter expression="${wagon.applyWildcard}" default-value="false"
     */
    protected boolean applyWildcard;
    
    protected void execute(Wagon wagon, ResourceDescriptor descr) throws MojoExecutionException, WagonException
    {
        List fileList = wagon.getFileList(descr.path);
        getLog().info("Listing: "+descr.path);
        for (Iterator iterator = fileList.iterator(); iterator.hasNext();)
        {
            String file = (String) iterator.next();
            if(!applyWildcard || descr.wildcard == null || descr.isMatch(file))
            {
                getLog().info("\t"+file);
            }
        }
    }
}
