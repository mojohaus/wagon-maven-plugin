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

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;



/**
 * Uploads the given resources (files and/or directories) using a suitable wagon provider.
 * 
 * @author Sherali Karimov
 * @goal upload
 */
public class UploadMojo extends AbstractWagonMojo
{
    protected void execute(Wagon wagon, ResourceDescriptor descr) throws MojoExecutionException, WagonException
    {
        final Set resources = descr.toLocalFiles();
        if (resources.isEmpty())
        {
            final String message = "Resource " + resourceSrc + " does not match an existing file or directory.";
            if (ignoreInvalidResource)
            {
                getLog().info(message);
                return;
            }
            else
            {
                throw new InvalidResourceException(message);
            }
        }

        if (resources == null || resources.isEmpty())
        {
            throw new MojoExecutionException("The resources to upload are not specified.");
        }

        for (Iterator iterator = resources.iterator(); iterator.hasNext();)
        {
            File resource = (File) iterator.next();
            if (resource.isDirectory() && !wagon.supportsDirectoryCopy())
            {
                if(this.ignoreInvalidResource)
                    iterator.remove();
                else
                    throw new MojoExecutionException("Wagon protocol '" + wagon.getRepository().getProtocol() + "' doesn't support directory copying. " + resource + " will fail the operation.");
            }
        }

        for (Iterator iterator = resources.iterator(); iterator.hasNext();)
        {
            File resource = (File) iterator.next();
            if (resource.isDirectory())
                wagon.putDirectory(resource, resourceDest + '/' + resource.getName());
            else
                wagon.put(resource, resourceDest + '/' + resource.getName());
        }
    }
}