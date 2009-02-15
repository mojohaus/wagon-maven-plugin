package org.codehaus.mojo.wagon.shared;

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

import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 * 
 */
public interface WagonDownload
{
    String ROLE = WagonDownload.class.getName();

    /**
     * @param wagon - a Wagon instance
     * @param fileSet - Criteria to build the list
     * @param logger
     * @return a list of files at the remote host relative to RemoteFileSet's directory
     * @throws WagonException
     */
    List getFileList( Wagon wagon, WagonFileSet fileSet, Log logger )
        throws WagonException;

    /**
     * 
     * @param wagon - a Wagon instance
     * @param remoteFileSet - 
     * @param logger
     * @throws WagonException
     */
    public void download( Wagon wagon, WagonFileSet remoteFileSet, Log logger )
        throws WagonException;

    /**
     * 
     * @param wagon - a Wagon instance
     * @param resource 
     * @throws WagonException
     */
    public boolean exists( Wagon wagon, String resource )
        throws WagonException;
    
}
