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

import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.observers.Debug;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;

/**
 * Provides base functionality for dealing with I/O using wagon.
 * 
 */
public abstract class AbstractWagonMojo
    extends AbstractMojo
{

    /**
     * URL to upload to or download from or list. Must exist and point to a directory.
     * 
     * @parameter expression="${wagon.url}"
     * @required
     */
    private String url;

    /**
     * ID of the server under the above URL. This is used when wagon needs extra authentication
     * information for instance.
     * 
     * @parameter expression="${wagon.serverId}" default-value="";
     */
    private String serverId = "";

    /**
     * @component
     */
    private WagonManager wagonManager;

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

    /**
     * When <code>true</code>, skip the execution.
     * 
     * @parameter expression="${wagon.skip}" default-value="false"
     */
    protected boolean skip = false;

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
            wagon = createWagon( serverId, url, wagonManager, settings, this.getLog() );
            execute( wagon );
        }
        catch ( WagonException e )
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
     * Convenience method to map a <code>Proxy</code> object from the user system settings to a
     * <code>ProxyInfo</code> object.
     * 
     * @return a proxyInfo object or null if no active proxy is define in the settings.xml
     */
    public static ProxyInfo getProxyInfo( Settings settings )
    {
        ProxyInfo proxyInfo = null;
        if ( settings != null && settings.getActiveProxy() != null )
        {
            Proxy settingsProxy = settings.getActiveProxy();

            proxyInfo = new ProxyInfo();
            proxyInfo.setHost( settingsProxy.getHost() );
            proxyInfo.setType( settingsProxy.getProtocol() );
            proxyInfo.setPort( settingsProxy.getPort() );
            proxyInfo.setNonProxyHosts( settingsProxy.getNonProxyHosts() );
            proxyInfo.setUserName( settingsProxy.getUsername() );
            proxyInfo.setPassword( settingsProxy.getPassword() );
        }

        return proxyInfo;
    }

    /**
     * Convenient method to create a wagon
     * @param id
     * @param url
     * @param wagonManager
     * @param settings
     * @param logger
     * @return
     * @throws MojoExecutionException
     */
    protected static Wagon createWagon( String id, String url, WagonManager wagonManager, Settings settings, Log logger )
        throws MojoExecutionException
    {
        Wagon wagon = null;

        final Repository repository = new Repository( id, url );

        try
        {
            wagon = wagonManager.getWagon( repository );

            try
            {
                if ( logger.isDebugEnabled() )
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

    /**
     * Perform the necessary action. To be implemented in the child mojo.
     * 
     * @param wagon
     * @throws MojoExecutionException
     * @throws WagonException
     */
    protected abstract void execute( Wagon wagon )
        throws MojoExecutionException, WagonException;

}