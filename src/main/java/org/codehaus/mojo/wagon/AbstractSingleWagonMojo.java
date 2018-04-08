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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

import java.io.IOException;

/**
 * Provides base functionality for dealing with I/O using single wagon.
 */
public abstract class AbstractSingleWagonMojo
    extends AbstractWagonMojo
{

    /**
     * URL to upload to or download from or list. Must exist and point to a directory.
     */
    @Parameter( property = "wagon.url", required = true )
    private String url;

    /**
     * settings.xml's server id for the URL. This is used when wagon needs extra authentication information.
     */
    @Parameter( property = "wagon.serverId", defaultValue = "serverId")
    private String serverId;

    public void execute()
        throws MojoExecutionException
    {
        if ( this.skip )
        {
            this.getLog().info( "Skip execution." );
            return;
        }

        Wagon wagon = null;
        try
        {
            wagon = createWagon( serverId, url );
            execute( wagon );
        }
        catch ( WagonException e )
        {
            throw new MojoExecutionException( "Error handling resource", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error handling resource", e );
        }
        finally
        {
            try
            {
                if ( wagon != null )
                {
                    wagon.disconnect();
                }
            }
            catch ( ConnectionException e )
            {
                getLog().debug( "Error disconnecting wagon - ignored", e );
            }
        }
    }

    /**
     * Perform the necessary action. To be implemented in the child mojo.
     * 
     * @param wagon
     * @throws MojoExecutionException
     * @throws WagonException
     */
    protected abstract void execute( Wagon wagon )
        throws MojoExecutionException, WagonException, IOException;

}