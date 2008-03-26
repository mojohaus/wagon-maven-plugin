package org.codehaus.mojo.wagon;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

/**
 * A mojo wrapper for Wagon.  Should autoconfigure in typical Wagon style.  But maybe that doesn't work yet.
 * We really only have a couple of commands with Wagon: get and put.  Thus the configuration...
 * <code>
 *     <configuration>
 *         <repository/>
 *         <tasks>
 *             <task>
 *                 <command>put</command>
 *                 <localfile>bleh</localfile>
 *                 <remotepath>bleh</remotepath>
 *             </task>
 *             <task>
 *                  <command>put</command>
 *                  <localfile>bleh</localfile>
 *                  <remotepath>bleh</remotepath>
 *             </task>
 *         <tasks>
 *     </configuration>
 * </code>
 *
 * @goal run
 * @phase deploy
 * @author Brian Topping <topping atCodehaus.org>
 */
public class WagonMojo
    extends AbstractMojo
    implements Contextualizable
{

    private PlexusContainer container;

    /**
     * @parameter
     */
    private boolean interactive = true;

    /**
     * @parameter
     */
    private Task[] tasks;

    /**
     * @parameter
     */
    private String remote;

    public Wagon getWagon( String protocol )
        throws UnsupportedProtocolException
    {
        Wagon wagon;

        try
        {
            wagon = (Wagon) container.lookup( Wagon.ROLE, protocol );
            wagon.setInteractive( interactive );
        }
        catch ( ComponentLookupException e )
        {
            throw new UnsupportedProtocolException(
                "Cannot find wagon which supports the requested protocol: " + protocol, e );
        }

        return wagon;
    }


    public void execute()
        throws MojoExecutionException
    {

        Repository repository = new Repository( "local", remote );
        Wagon wagon;
        try
        {
            wagon = getWagon( repository.getProtocol() );
        }
        catch ( UnsupportedProtocolException e )
        {
            throw new MojoExecutionException( "Could not load wagon", e );
        }

        try
        {
            wagon.connect( repository );
        }
        catch ( ConnectionException e )
        {
            throw new MojoExecutionException( "Couldn't connect to destination", e );
        }
        catch ( AuthenticationException e )
        {
            throw new MojoExecutionException( "Bad authentication", e );
        }

        try
        {
            doWagon( wagon );
        }
        catch ( MojoExecutionException e )
        {
            throw e;
        }
        finally
        {
            try
            {
                wagon.disconnect();
            }
            catch ( ConnectionException e )
            {
                // ignore
            }
        }
    }

    private void doWagon( Wagon wagon )
        throws MojoExecutionException
    {
        for ( int i = 0; i < tasks.length; i++ )
        {
            Task task = tasks[i];
            try
            {
                if ( task.getCommand().equals( "get" ) )
                {
                    wagon.get( task.getRemotepath(), task.getLocalfile() );
                }
                else if ( task.getCommand().equals( "put" ) )
                {
                    wagon.put( task.getLocalfile(), task.getRemotepath() );
                }
                else
                {
                    throw new MojoExecutionException( "Command is unsupported: " + task.getCommand() );
                }

            }
            catch ( TransferFailedException e )
            {
                if ( e.getMessage().split( ":" )[1].trim().charAt( 0 ) != '2' )
                {
                    throw new MojoExecutionException( "Transfer Failure", e );
                }
            }
            catch ( ResourceDoesNotExistException e )
            {
                throw new MojoExecutionException( "Resource does not exist", e );
            }
            catch ( AuthorizationException e )
            {
                throw new MojoExecutionException( "Bad authorization", e );
            }
        }
    }

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( "plexus" );
    }
}
