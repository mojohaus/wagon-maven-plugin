package org.codehaus.mojo.wagon;

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

import java.io.IOException;

import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.observers.Debug;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.mojo.wagon.shared.MavenRepoMerger;

/**
 * Merge artifacts from one Maven repository to another Maven repository.
 * 
 * @goal merge-maven-repo
 * @requiresProject false
 */
public abstract class MergeMavenRepoMojo
    extends AbstractMojo
{

    /**
     * The URL to the source repository.
     * 
     * @parameter expression="${wagon.src.url}"
     */
    private String source;

    /**
     * The URL to the target repository.
     * 
     * <p>
     * <strong>Note:</strong> currently only <code>scp:</code> URLs are allowed as a target URL.
     * </p>
     * 
     * @parameter expression="${wagon.targetUrl}"
     */
    private String target;

    /**
     * The id of the source repository, required if you need the configuration from the user
     * settings.
     * 
     * @parameter expression="${wagon.srcUrl}" default-value="source"
     */
    private String sourceId;

    /**
     * The id of the target repository, required if you need the configuration from the user
     * settings.
     * 
     * @parameter expression="${wagon.targetId}" default-value="target"
     */
    private String targetId;

    /**
     * @component
     */
    private WagonManager wagonManager;
    
    /**
     * @component
     */
    private MavenRepoMerger mavenRepoMerger;
    

    /**
     * The current user system settings for use in Maven.
     * 
     * @parameter expression="${settings}"
     * @readonly
     */
    private Settings settings;

    /**
     * Internal Maven's project
     * 
     * @parameter expression="${project}"
     * @readonly
     * @since alpha 1
     */
    protected MavenProject project;

    public void execute()
        throws MojoExecutionException
    {
        Wagon srcWagon = null;
        Wagon targetWagon = null;

        try
        {
            srcWagon = AbstractWagonMojo.createWagon( sourceId, source, wagonManager, settings, this.getLog() );
            targetWagon = AbstractWagonMojo.createWagon( targetId, target, wagonManager, settings, this.getLog() );
            mavenRepoMerger.copy( srcWagon, targetWagon, this.getLog() );
        }
        catch ( IOException iox )
        {
            throw new MojoExecutionException( "Error during performing repository merge: " + iox );
        }
        catch ( WagonException e )
        {
            throw new MojoExecutionException( "Error during performing repository merge: " + e );
        }
        finally
        {
            try
            {
                if ( srcWagon != null )
                {
                    srcWagon.disconnect();
                }
            }
            catch ( ConnectionException e )
            {
                getLog().debug( "Error disconnecting wagon - ignored", e );
            }

            try
            {
                if ( targetWagon != null )
                {
                    targetWagon.disconnect();
                }
            }
            catch ( ConnectionException e )
            {
                getLog().debug( "Error disconnecting wagon - ignored", e );
            }
        }

    }

    protected Wagon createWagon( String id, String url, WagonManager wagonManager )
        throws MojoExecutionException
    {
        Wagon wagon = null;

        final Repository repository = new Repository( id, url );

        try
        {
            wagon = wagonManager.getWagon( repository );

            try
            {
                if ( this.getLog().isDebugEnabled() )
                {
                    Debug debug = new Debug();
                    wagon.addSessionListener( debug );
                    wagon.addTransferListener( debug );
                }

                ProxyInfo proxyInfo = AbstractWagonMojo.getProxyInfo( settings );
                if ( proxyInfo != null )
                {
                    wagon.connect( repository, wagonManager.getAuthenticationInfo( repository.getId() ), proxyInfo );
                }
                else
                {
                    wagon.connect( repository, wagonManager.getAuthenticationInfo( repository.getId() ) );
                }
            }
            catch ( WagonException e )
            {
                throw new MojoExecutionException( "Error handling resource", e );
            }
        }
        catch ( UnsupportedProtocolException e )
        {
            throw new MojoExecutionException( "Unsupported protocol: '" + repository.getProtocol() + "'", e );
        }
        catch ( WagonConfigurationException e )
        {
            throw new MojoExecutionException( "Unable to configure Wagon: '" + repository.getProtocol() + "'", e );
        }

        return wagon;
    }

}