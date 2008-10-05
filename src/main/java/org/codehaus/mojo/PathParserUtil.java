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
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.MojoExecutionException;

public class PathParserUtil
{
    /**
     * Takes a list of paths and converts them to a set of File objects. Path
     * can be either a relative or absolute path to a file or directory or a
     * reference to children of a directory using '*' or '?'. For instance:
     * <ul>
     * <li> dir1/dir2/file.txt </li>
     * <li> dir1/dir2/ </li>
     * <li> dir1/dir2 </li>
     * <li> dir1/dir2/*.txt </li>
     * <li> dir1/dir2/Us*Nam?.* </li>
     * </ul>
     * 
     * The wildcard '*' must not occur within the path but only at the end of
     * the path. I.e. the following paths are unsupported:
     * <ul>
     * <li> dir1/*\/dir2 </li>
     * <li> *\/dir2 </li>
     * </ul>
     * 
     * @param paths
     * @return
     * @throws MojoExecutionException
     */
    static Set toFiles(String[] paths, boolean isCaseSensitive) throws MojoExecutionException
    {
        Set resources = new HashSet();
        for (int i = 0; i < paths.length; i++)
        {
            Set nextSet = toFiles(paths[i], isCaseSensitive);
            if (nextSet != null)
                resources.addAll(nextSet);

        }
        return resources;
    }

    /**
     * Parses the given file path into one or more File objects. Path can be
     * either a relative or absolute path to a file or directory or a reference
     * to children of a directory using '*' or '?'. For instance:
     * <ul>
     * <li> dir1/dir2/file.txt </li>
     * <li> dir1/dir2/ </li>
     * <li> dir1/dir2 </li>
     * <li> dir1/dir2/*.txt </li>
     * <li> dir1/dir2/Us*Nam?.* </li>
     * </ul>
     * 
     * The wildcard '*' must not occur within the path but only at the end of
     * the path. I.e. the following paths are unsupported:
     * <ul>
     * <li> dir1/*\/dir2 </li>
     * <li> dir1/*\/dir2 </li>
     * <li> dir1/Us*Name?/* </li>
     * <li> *\/dir?/file.txt </li>
     * </ul>
     * 
     * @param pathStr
     * @return
     * @throws MojoExecutionException
     */
    static Set toFiles(final String pathStr, boolean isCaseSensitive) throws MojoExecutionException
    {
        if (pathStr.length() == 0)
            return null;

        ResourceDescriptor descr = new ResourceDescriptor(pathStr, isCaseSensitive);
        return toFiles(descr);
    }


    /**
     * Converts the given descriptor to a set of File objects that match the descriptor.
     * 
     * @param descr
     * @return
     */
    public static Set toFiles(ResourceDescriptor descr)
    {
        Set matchedFiles = new HashSet();

        File parent = new File(descr.path);
        if (parent.exists())
        {
            if (descr.wildcard != null)
            {
                if (parent.isDirectory())
                {
                    getMatchingChildren(parent, descr.wildcard, descr.isCaseSensitive, matchedFiles);
                }
            }
            else
            {
                matchedFiles.add(parent);
            }
        }

        return matchedFiles;
    }

    static void getMatchingChildren(File directory, String wildcard, boolean isCaseSensitive, Set matchingFileContainer)
    {
        FileFilter filter = new WildcardFileFilter(wildcard, isCaseSensitive ? IOCase.SENSITIVE : IOCase.INSENSITIVE);
        File children[] = directory.listFiles(filter);

        for (int i = 0; i < children.length; i++)
            matchingFileContainer.add(children[i]);
    }

    /**
     * Looks for the first occurrence of either '?' or '*' character and returns
     * its position. Otherwise returns -1;
     * 
     * @param path
     * @return position of the first occurrence of either '?' or '*' character
     *         or -1;
     */
    static int findFirstGlobCharPosition(final String path)
    {
        char[] array = path.toCharArray();
        for (int i = 0; i < array.length; i++)
        {
            if (array[i] == '?' || array[i] == '*')
                return i;
        }
        return -1;
    }
}