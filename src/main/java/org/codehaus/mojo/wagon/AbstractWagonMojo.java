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
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.observers.Debug;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.mojo.wagon.shared.WagonFileSet;
import org.codehaus.plexus.util.StringUtils;

/**
 * Provides base functionality for dealing with I/O using wagon.
 * 
 */
public abstract class AbstractWagonMojo
    extends AbstractMojo
{

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
     * 
     * @parameter expression="${project}"
     * @readonly
     */
    protected MavenProject project;

    /**
     * When <code>true</code>, skip the execution.
     * 
     * @parameter expression="${wagon.skip}" default-value="false"
     */
    protected boolean skip = false;

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
    protected  Wagon createWagon( String id, String url )
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

                ProxyInfo proxyInfo = getProxyInfo();
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

    protected WagonFileSet getWagonFileSet( String fromDir, String includes, String excludes, boolean isCaseSensitive )
    {
        WagonFileSet fileSet = new WagonFileSet();
        fileSet.setDirectory( fromDir );
        
        if ( ! StringUtils.isBlank( includes ) )
        {
            fileSet.setIncludes( StringUtils.split( includes, "," ) );
        }
        
        if ( ! StringUtils.isBlank( excludes ) )
        {
            fileSet.setExcludes( StringUtils.split( excludes, "," ) );
        }
        
        fileSet.setCaseSensitive( isCaseSensitive );
        
        return fileSet;
        
    }    
    
    /**
     * Convenience method to map a <code>Proxy</code> object from the user system settings to a
     * <code>ProxyInfo</code> object.
     * 
     * @return a proxyInfo object or null if no active proxy is define in the settings.xml
     */
    private ProxyInfo getProxyInfo()
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