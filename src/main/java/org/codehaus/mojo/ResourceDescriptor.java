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

import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Represents a path to one or more resources. More than once resource can be described by a wildcard in which case the path MUST be a directory.
 * Wildcard can be treated in a case sensitive/insensitive way as desired.
 */
public class ResourceDescriptor
{
    final String path;
    final String wildcard;
    final boolean isCaseSensitive;

    /**
     * Parses the given file path into one or more File objects. Path can be
     * either a relative or absolute path to a file or directory or a
     * reference to children of a directory using '*' or '?'. For instance:
     * <ul>
     * <li> dir1/dir2/file.txt </li>
     * <li> dir1/dir2/ </li>
     * <li> dir1/dir2 </li>
     * <li> dir1/dir2/*.txt </li>
     * <li> dir1/dir2/Us*Nam?.* </li>
     * </ul>
     * 
     * The wildcard '*' must not occur within the path but only at the end
     * of the path. I.e. the following paths are unsupported:
     * <ul>
     * <li> dir1/*\/dir2 </li>
     * <li> dir1/*\/dir2 </li>
     * <li> dir1/Us*Name?/* </li>
     * <li> *\/dir?/file.txt </li>
     * </ul>
     * 
     * @throws MojoExecutionException
     */
    public ResourceDescriptor(String path, boolean isCaseSensitive) throws MojoExecutionException
    {
        this.isCaseSensitive = isCaseSensitive;
        int pos = PathParserUtil.findFirstGlobCharPosition(path);

        if (pos != -1)
        {
            int dirEndPos = path.replace('\\', '/').lastIndexOf('/');
            if (dirEndPos >= pos)
                throw new MojoExecutionException("Invalid path - '" + path
                        + "'. Wildcards must not contain a path separator '/' or '\\'.");
            else if (dirEndPos == -1)
            {
                // children of the current dir are being matched
                this.path = "";
                this.wildcard = path;
            }
            else
            {
                this.path = path.substring(0, dirEndPos + 1);
                this.wildcard = path.substring(dirEndPos + 1);
            }
        }
        else
        {
            this.path = path;
            this.wildcard = null;
        }
    }

    /**
     * Convenience method. Delegates to {@link PathParserUtil#toFiles(org.apache.maven.plugins.PathParserUtil.ResourceDescriptor)} 
     * @return
     */
    public Set toLocalFiles()
    {
        return PathParserUtil.toFiles(this);
    }

    public boolean isMatch(String fileName)
    {
        return FilenameUtils.wildcardMatch(fileName, wildcard, isCaseSensitive ? IOCase.SENSITIVE : IOCase.INSENSITIVE);
    }
}