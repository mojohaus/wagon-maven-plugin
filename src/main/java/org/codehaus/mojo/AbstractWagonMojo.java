package org.codehaus.mojo;

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
 * @author Sherali Karimov
 */
public abstract class AbstractWagonMojo
    extends AbstractMojo
{

    /**
     * URL to upload to or download from or list.  Must point to a directory.
     * 
     * @parameter expression="${wagon.url}"
     * @required
     */
    protected String url;

    /**
     * ID of the server under the above URL. This is used when wagon needs extra
     * authentication information for instance.
     * 
     * @parameter expression="${wagon.serverId}" default-value="";
     */
    protected String serverId;

    /**
     * @component
     */
    protected WagonManager wagonManager;

    /**
     * The current user system settings for use in Maven.
     * 
     * @parameter expression="${settings}"
     * @readonly
     */
    protected Settings settings;

    /**
     * Internal Maven's project
     * @parameter expression="${project}"
     * @readonly
     * @since alpha 1
     */
    protected MavenProject project;

    /**
     * If true, performs a case sensitive wildcard matching. Case insensitive otherwise.
     * 
     * @parameter expression="${wagon.caseSensitive}" default-value="false"
     */
    protected boolean isCaseSensitive;

    public void execute()
        throws MojoExecutionException
    {
        final Repository repository = new Repository( serverId, url );

        try
        {
            final Wagon wagon = wagonManager.getWagon( repository );

            try
            {
                if ( this.getLog().isDebugEnabled() )
                {
                    Debug debug = new Debug();
                    wagon.addSessionListener( debug );
                    wagon.addTransferListener( debug );
                }

                ProxyInfo proxyInfo = getProxyInfo( settings );
                if ( proxyInfo != null )
                {
                    wagon.connect( repository, wagonManager.getAuthenticationInfo( repository.getId() ), proxyInfo );
                }
                else
                {
                    wagon.connect( repository, wagonManager.getAuthenticationInfo( repository.getId() ) );
                }

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
                    wagon.disconnect();
                }
                catch ( ConnectionException e )
                {
                    getLog().debug( "Error disconnecting wagon - ignored", e );
                }
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
    }

    /**
     * Convenience method to map a <code>Proxy</code> object from the user
     * system settings to a <code>ProxyInfo</code> object.
     * 
     * @return a proxyInfo object or null if no active proxy is define in the
     *         settings.xml
     */
    private static ProxyInfo getProxyInfo( Settings settings )
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
     * Perform the necessary action. To be implemented in the child mojo.
     * 
     * @param wagon
     * @throws MojoExecutionException
     * @throws WagonException
     */
    protected abstract void execute( Wagon wagon )
        throws MojoExecutionException, WagonException;

}