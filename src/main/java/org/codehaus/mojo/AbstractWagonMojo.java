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
     * Resource(s) to be uploaded or downloaded or listed. Can be a file or directory. Also supports
     * wildcards.
     * 
     * @see PathParserUtil#toFiles(String)
     * @parameter expression="${io.resourceSrc}"
     * @required
     */
    protected String resourceSrc;

    /**
     * Path on the server to upload/download the resource to. If not specified - assumed
     * to be "target/wagon-plugin/".
     * 
     * For instance: 
     * <ul>
     * <li>src=dir1/dir2 dest=xyz will create xyz/dir2 </li>
     * <li>src=dir1/dir2/* dest=xyz will create xyz/ and put all the content of dir2 there </li>
     * <li>src=dir1/dir2 will create dir2 on the server with all the dir2 content</li>
     * 
     * @parameter expression="${wagon.resourceDest}" default-value="target/wagon-plugin/"
     */
    protected String resourceDest;

    /**
     * URL to upload to or download from or list.
     * 
     * @parameter expression="${wagon.url}"
     * @required
     */
    protected String url;

    /**
     * ID of the server under the above URL. This is used when wagon needs extra
     * authentication information for instance.
     * 
     * @parameter expression="${wagon.serverId}"
     * @required
     */
    protected String serverId;

    /**
     * If true, ignores invalid source resources during execution. Otherwise - fail the execution.
     * 
     * @parameter expression="${wagon.ignoreInvalidResource}" default-value="false"
     */
    protected boolean ignoreInvalidResource;

    /**
     * @component
     */
    protected WagonManager wagonManager;

    /**
     * The current user system settings for use in Maven.
     * 
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    protected Settings settings;

    /**
     * If true, performs a case sensitive wildcard matching. Case insensitive otherwise.
     * 
     * @parameter expression="${wagon.caseSensitive}" default-value="false"
     */
    private boolean isCaseSensitive;

    public void execute()
        throws MojoExecutionException
    {
        final ResourceDescriptor descr = new ResourceDescriptor( resourceSrc, isCaseSensitive );
        if ( url == null )
        {
            throw new MojoExecutionException( "The URL is missing." );
        }

        final Repository repository = new Repository( serverId, url );
        Debug debug = new Debug();

        try
        {
            final Wagon wagon = wagonManager.getWagon( repository );

            try
            {
                wagon.addSessionListener( debug );
                wagon.addTransferListener( debug );

                ProxyInfo proxyInfo = getProxyInfo( settings );
                if ( proxyInfo != null )
                {
                    wagon.connect( repository, wagonManager.getAuthenticationInfo( repository.getId() ), proxyInfo );
                }
                else
                {
                    wagon.connect( repository, wagonManager.getAuthenticationInfo( repository.getId() ) );
                }

                execute( wagon, descr );
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
     * Perform the necessary action. To be implemented in the child mojo.
     * 
     * @param wagon
     * @param descr
     * @throws MojoExecutionException
     * @throws WagonException
     */
    protected abstract void execute( Wagon wagon, ResourceDescriptor descr )
        throws MojoExecutionException, WagonException;

    /**
     * Convenience method to map a <code>Proxy</code> object from the user
     * system settings to a <code>ProxyInfo</code> object.
     * 
     * @return a proxyInfo object or null if no active proxy is define in the
     *         settings.xml
     */
    protected static ProxyInfo getProxyInfo( Settings settings )
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
}