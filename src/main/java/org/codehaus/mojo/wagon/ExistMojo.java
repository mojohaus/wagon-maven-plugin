package org.codehaus.mojo.wagon;

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

import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.mojo.wagon.shared.WagonDownload;

/**
 * Check for the existing of remote resource.
 */
@Mojo( name = "exist" )
public class ExistMojo
    extends AbstractSingleWagonMojo
{
    /**
     * relative path to a remote resource.
     */
    @Parameter( property = "wagon.resource")
    private String resource = "";

    @Component
    protected WagonDownload wagonDownload;

    @Override
    protected void execute( Wagon wagon )
        throws WagonException
    {
        if ( this.wagonDownload.exists( wagon, resource ) )
        {
            this.getLog().info( resource + " exists. " );
        }
        else
        {
            this.getLog().info( resource + " does not exists. " );
        }
    }

}
