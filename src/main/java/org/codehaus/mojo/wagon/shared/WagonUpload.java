package org.codehaus.mojo.wagon.shared;

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

import java.io.IOException;

import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 *
 */
public interface WagonUpload
{

    /**
     * Upload a set of files via FileSet interface to a remote repository via Wagon
     *
     * @param wagon - a Wagon instance
     * @param fileset file set to upload
     * @param optimize locally compressed and uncompress at the remote site if scp is use
     * @param continuationType continuation type. Incremental upload will only upload resources that doesn't exist
     * @throws WagonException if nay wagon exception
     * @throws IOException if any io exception
     */
    void upload( Wagon wagon, FileSet fileset, boolean optimize, ContinuationType continuationType )
            throws WagonException, IOException;

    /**
     * Upload a set of files via FileSet interface to a remote repository via Wagon
     *
     * @param wagon - a Wagon instance
     * @param fileset file set to upload
     * @param optimize locally compressed and uncompress at the remote site if scp is use
     * @throws WagonException if nay wagon exception
     * @throws IOException if any io exception
     */
    void upload( Wagon wagon, FileSet fileset, boolean optimize )
        throws WagonException, IOException;
}
