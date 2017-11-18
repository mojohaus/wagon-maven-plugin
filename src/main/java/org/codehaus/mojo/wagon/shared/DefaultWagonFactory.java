package org.codehaus.mojo.wagon.shared;

import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.observers.Debug;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.repository.RepositoryPermissions;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.BasicComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @plexus.component role="org.codehaus.mojo.wagon.shared.WagonFactory" role-hint="default"
 */
public class DefaultWagonFactory
    implements WagonFactory, Contextualizable
{

    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    /**
     * @plexus.requirement role="org.apache.maven.settings.crypto.SettingsDecrypter"
     */
    private SettingsDecrypter settingsDecrypter;

    /**
     * @plexus.requirement role="org.codehaus.plexus.component.configurator.ComponentConfigurator" hint="basic"
     */
    private ComponentConfigurator componentConfigurator;

    /**
     * @plexus.requirement role="org.codehaus.plexus.component.configurator.ComponentConfigurator" hint="map-oriented"
     */
    private ComponentConfigurator mapComponentConfigurator;

    ////////////////////////////////////////////////////////////////////
    private PlexusContainer container;

    /**
     * Convenient method to create a wagon
     *
     * @param url
     * @param serverId
     * @param settings
     * @return
     * @throws WagonException
     */
    public Wagon create( String url, String serverId, Settings settings )
        throws WagonException
    {
        final Repository repository = new Repository( serverId, url == null ? "" : url );
        repository.setPermissions( getPermissions( serverId, settings ) );
        Wagon wagon;
        if ( url == null )
        {
            wagon = createAndConfigureWagon( serverId, settings, repository );
        }
        else
        {
            wagon = getWagon( repository.getProtocol() );

            configureWagon( wagon, serverId, settings );
        }



        if ( logger.isDebugEnabled() )
        {
            Debug debug = new Debug();
            wagon.addSessionListener( debug );
            wagon.addTransferListener( debug );
        }

        AuthenticationInfo authInfo = getAuthenticationInfo( serverId, settings );
        ProxyInfo proxyInfo = getProxyInfo( settings );
        wagon.connect( repository, authInfo, proxyInfo );

        return wagon;
    }

    /**
     * Configure the Wagon with the information from serverConfigurationMap ( which comes from settings.xml )
     *
     * @param repositoryId
     * @param settings
     * @throws TransferFailedException
     */
    private Wagon createAndConfigureWagon( String repositoryId, Settings settings, Repository repository )
            throws WagonException
    {
        Wagon wagon = null;
        for ( Server server : settings.getServers() )
        {
            String id = server.getId();

            if ( id != null && id.equals( repositoryId ) )
            {
                Xpp3Dom configuration = (Xpp3Dom) server.getConfiguration();
                String url = configuration == null ? null : configuration.getAttribute( "url" );
                if ( StringUtils.isBlank( url ) )
                {
                    throw new NullPointerException( "url cannot be null" );
                }
                repository.setUrl( url );

                wagon = getWagon( repository.getProtocol() );
                configureWagon( wagon, repositoryId, server );

                break;

            }
        }

        return wagon;
    }

    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    private Wagon getWagon( String protocol )
        throws UnsupportedProtocolException
    {
        if ( protocol == null )
        {
            throw new UnsupportedProtocolException( "Unspecified protocol" );
        }

        try
        {
            return container.lookup( Wagon.class, protocol.toLowerCase( java.util.Locale.ENGLISH ) );
        }
        catch ( ComponentLookupException e )
        {
            throw new UnsupportedProtocolException( "Cannot find wagon which supports the requested protocol: "
                + protocol, e );
        }
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
     * @throws TransferFailedException
     */
    private Wagon configureWagon( Wagon wagon, String repositoryId, Settings settings )
        throws TransferFailedException
    {
        for ( Server server : settings.getServers() )
        {
            String id = server.getId();

            if ( id != null && id.equals( repositoryId ) && ( server.getConfiguration() != null ) )
            {

                configureWagon( wagon, repositoryId, server);
                break;

            }
        }

        return wagon;
    }

    private Wagon configureWagon( Wagon wagon, String repositoryId, Server server)
            throws TransferFailedException
    {
        final PlexusConfiguration plexusConf =
                new XmlPlexusConfiguration( (Xpp3Dom) server.getConfiguration() );
        try
        {
            if ( componentConfigurator == null || !( componentConfigurator instanceof BasicComponentConfigurator ) ) {
                componentConfigurator = new BasicComponentConfigurator();
            }
            componentConfigurator.configureComponent( wagon, plexusConf,
                    (ClassRealm) this.getClass().getClassLoader() );
        }
        catch ( ComponentConfigurationException e )
        {
            throw new TransferFailedException( "While configuring wagon for \'" + repositoryId
                    + "\': Unable to apply wagon configuration.", e );
        }
        return wagon;
    }

    public AuthenticationInfo getAuthenticationInfo( String id, Settings settings )
    {
        List<Server> servers = settings.getServers();

        if ( servers != null )
        {
            for ( Server server : servers )
            {
                if ( id.equalsIgnoreCase( server.getId() ) )
                {
                    SettingsDecryptionResult result =
                        settingsDecrypter.decrypt( new DefaultSettingsDecryptionRequest( server ) );
                    server = result.getServer();

                    AuthenticationInfo authInfo = new AuthenticationInfo();
                    authInfo.setUserName( server.getUsername() );
                    authInfo.setPassword( server.getPassword() );
                    authInfo.setPrivateKey( server.getPrivateKey() );
                    authInfo.setPassphrase( server.getPassphrase() );

                    return authInfo;
                }
            }

        }

        // empty one to prevent NPE
        return new AuthenticationInfo();
    }

    /**
     * {@inheritDoc}
     */
    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

}
