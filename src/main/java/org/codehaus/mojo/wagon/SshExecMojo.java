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
import org.apache.maven.wagon.Streams;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.providers.ssh.jsch.ScpWagon;

/**
 * Executes a list of commands against a given server.
 *
 * @goal sshexec
 * @requiresProject true
 */
public class SshExecMojo
    extends AbstractSingleWagonMojo
{

    /**
     * The commands that we will execute.
     *
     * @parameter
     * @required
     */
    private String[] commands;

    /**
     * Allow option not to fail the build on error
     *
     * @parameter default-value = "true"
     */
    private boolean failOnError = true;

    /**
     * Option to display remote command's outputs
     *
     * @parameter default-value = "false"
     */
    private boolean displayCommandOutputs = true;

    protected void execute( final Wagon wagon )
        throws MojoExecutionException
    {
        if ( commands != null )
        {
            for ( int i = 0; i < commands.length; i++ )
            {
                try
                {
                    Streams stream = ( (ScpWagon) wagon ).executeCommand( commands[i], true, false );
                    this.getLog().info( "sshexec: " + commands[i] + " ..." );
                    if ( displayCommandOutputs )
                    {
                        System.out.println( stream.getOut() );
                        System.out.println( stream.getErr() );
                    }
                }
                catch ( final WagonException e )
                {
                    if ( this.failOnError )
                    {
                        throw new MojoExecutionException( "Unable to execute remote command", e );
                    }
                    else
                    {
                        this.getLog().warn( e );
                    }
                }

            }
        }
    }
}
