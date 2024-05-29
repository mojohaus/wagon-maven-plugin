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

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 *
 */
public interface WagonCopy
{

    /**
     * Transfer files between 2 Wagon URLs. If download directory is not given in the fileset a temporary one will be
     * created.
     *
     * @param fromWagon - source Wagon
     * @param fileset - file set to copy
     * @param toWagon - target Wagon
     * @param optimize - locally compressed and remotely uncompress for scp only
     * @param logger - logger used
     * @param continuationType - continuation type.
     *        When continuation type is ONLY_MISSING, download file from source Wagon that do not
     *        exist in downloadDirectory and copy files that do not exist in target Wagon
     *        When continuation type is NONE, copy all files
     * @throws WagonException if any wagon error
     * @throws IOException if any io error
     */
    void copy( Wagon fromWagon, WagonFileSet fileset, Wagon toWagon, boolean optimize, Log logger, ContinuationType continuationType )
            throws WagonException, IOException;
}
