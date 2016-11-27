package org.codehaus.mojo.wagon;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.maven.artifact.manager.WagonConfigurationException;

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

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.observers.Debug;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.repository.RepositoryPermissions;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.mojo.wagon.shared.WagonFileSet;
import org.codehaus.mojo.wagon.shared.WagonUtils;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Provides base functionality for dealing with I/O using wagon.
 */
public abstract class AbstractWagonMojo
    extends AbstractMojo
    implements Contextualizable
{

    /**
     * @component
     */
    protected WagonManager wagonManager;

    /**
     * The current user system settings for use in Maven.
     *
     * @parameter default-value="${settings}"
     * @readonly
     */
    protected Settings settings;

    /**
     * Internal Maven's project
     *
     * @parameter default-value="${project}"
     * @readonly
     */
    protected MavenProject project;

    /**
     * When <code>true</code>, skip the execution.
     *
     * @parameter property="wagon.skip" default-value="false"
     */
    protected boolean skip = false;

    /////////////////////////////////////////////////////////////////////////

    private PlexusContainer container;

    /**
     * Convenient method to create a wagon
     *
     * @param id
     * @param url
     * @param wagonManager
     * @param settings
     * @param logger
     * @return
     * @throws MojoExecutionException
     */
    protected Wagon createWagon( String id, String url )
        throws MojoExecutionException
    {
        try
        {
            return createWagon( id, url, wagonManager, settings, this.getLog() );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to create a Wagon instance for " + url, e );
        }

    }

    protected WagonFileSet getWagonFileSet( String fromDir, String includes, String excludes, boolean caseSensitive,
                                            String toDir )
    {
        return WagonUtils.getWagonFileSet( fromDir, includes, excludes, caseSensitive, toDir );
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Convenient method to create a wagon
     *
     * @param id
     * @param url
     * @param wagonManager
     * @param settings
     * @param logger
     * @return
     * @throws MojoExecutionException
     */
    private Wagon createWagon( String id, String url, WagonManager wagonManager, Settings settings, Log logger )
        throws WagonException, UnsupportedProtocolException, WagonConfigurationException
    {

        final Repository repository = new Repository( id, url );
        repository.setPermissions( getPermissions( id, settings ) );

        Wagon wagon = wagonManager.getWagon( repository );

        configureWagon( wagon, id, settings, container, getLog() );

        if ( logger.isDebugEnabled() )
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

        return wagon;
    }

    private static RepositoryPermissions getPermissions( String id, Settings settings )
    {
        // May not have an id
        if ( StringUtils.isBlank( id ) )
        {
            return null;
        }

        // May not be a server matching that id
        Server server = settings.getServer( id );
        if ( server == null )
        {
            return null;
        }

        // Extract perms (if there are any)
        String filePerms = server.getFilePermissions();
        String dirPerms = server.getDirectoryPermissions();

        // Check to see if custom permissions were supplied
        if ( StringUtils.isBlank( filePerms ) && StringUtils.isBlank( dirPerms ) )
        {
            return null;
        }

        // There are custom permissions specified in settings.xml for this server
        RepositoryPermissions permissions = new RepositoryPermissions();
        permissions.setFileMode( filePerms );
        permissions.setDirectoryMode( dirPerms );
        return permissions;
    }

    /**
     * Convenience method to map a <code>Proxy</code> object from the user system settings to a <code>ProxyInfo</code>
     * object.
     *
     * @return a proxyInfo object or null if no active proxy is define in the settings.xml
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
     * Configure the Wagon with the information from serverConfigurationMap ( which comes from settings.xml )
     *
     * @param wagon
     * @param repositoryId
     * @param settings
     * @param container
     * @param log
     * @throws TransferFailedException
     * @todo Remove when {@link WagonManager#getWagon(Repository) is available}. It's available in Maven 2.0.5.
     */
    private static void configureWagon( Wagon wagon, String repositoryId, Settings settings, PlexusContainer container,
                                        Log log )
        throws TransferFailedException
    {
        log.debug( " configureWagon " );

        // MSITE-25: Make sure that the server settings are inserted
        for ( Server server : settings.getServers() )
        {
            String id = server.getId();

            log.debug( "configureWagon server " + id );

            if ( id != null && id.equals( repositoryId ) && ( server.getConfiguration() != null ) )
            {
                final PlexusConfiguration plexusConf =
                    new XmlPlexusConfiguration( (Xpp3Dom) server.getConfiguration() );

                ComponentConfigurator componentConfigurator = null;
                try
                {
                    componentConfigurator =
                        (ComponentConfigurator) container.lookup( ComponentConfigurator.ROLE, "basic" );
                    if ( isMaven3OrMore() )
                    {
                        componentConfigurator.configureComponent( wagon, plexusConf, container.getContainerRealm() );
                    }
                    else
                    {
                        configureWagonWithMaven2( componentConfigurator, wagon, plexusConf, container );
                    }
                }
                catch ( final ComponentLookupException e )
                {
                    throw new TransferFailedException( "While configuring wagon for \'" + repositoryId
                        + "\': Unable to lookup wagon configurator." + " Wagon configuration cannot be applied.", e );
                }
                catch ( ComponentConfigurationException e )
                {
                    throw new TransferFailedException( "While configuring wagon for \'" + repositoryId
                        + "\': Unable to apply wagon configuration.", e );
                }
                finally
                {
                    if ( componentConfigurator != null )
                    {
                        try
                        {
                            container.release( componentConfigurator );
                        }
                        catch ( ComponentLifecycleException e )
                        {
                            log.error( "Problem releasing configurator - ignoring: " + e.getMessage() );
                        }
                    }
                }
            }
        }
    }

    private static void configureWagonWithMaven2( ComponentConfigurator componentConfigurator, Wagon wagon,
                                                  PlexusConfiguration plexusConf, PlexusContainer container )
        throws ComponentConfigurationException
    {
        // in Maven 2.x :
        // * container.getContainerRealm() -> org.codehaus.classworlds.ClassRealm
        // * componentConfiguration 3rd param is org.codehaus.classworlds.ClassRealm
        // so use some reflection see MSITE-609
        try
        {
            Method methodContainerRealm = container.getClass().getMethod( "getContainerRealm" );
            ClassRealm realm = (ClassRealm) methodContainerRealm.invoke( container );

            Method methodConfigure = componentConfigurator.getClass().getMethod( "configureComponent", new Class[] {
                Object.class, PlexusConfiguration.class, ClassRealm.class } );

            methodConfigure.invoke( componentConfigurator, wagon, plexusConf, realm );
        }
        catch ( Exception e )
        {
            throw new ComponentConfigurationException( "Failed to configure wagon component for a Maven2 use "
                + e.getMessage(), e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    /**
     * Check the current Maven version to see if it's Maven 3.0 or newer.
     */
    private static boolean isMaven3OrMore()
    {
        return new ComparableVersion( getMavenVersion() ).compareTo( new ComparableVersion( "3.0" ) ) >= 0;
    }

    private static String getMavenVersion()
    {
        // This relies on the fact that MavenProject is the in core classloader
        // and that the core classloader is for the maven-core artifact
        // and that should have a pom.properties file
        // if this ever changes, we will have to revisit this code.
        final Properties properties = new Properties();
        final String corePomProperties = "META-INF/maven/org.apache.maven/maven-core/pom.properties";

        InputStream in = null;
        try
        {
            in = MavenProject.class.getClassLoader().getResourceAsStream( corePomProperties );
            properties.load( in );
            in.close();
            in = null;
        }
        catch ( IOException ioe )
        {
            return "";
        }
        finally
        {
            IOUtil.close( in );
        }

        return properties.getProperty( "version" ).trim();
    }
}