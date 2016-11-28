package org.codehaus.mojo.wagon.shared;

import java.util.List;

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
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
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

    ////////////////////////////////////////////////////////////////////
    private PlexusContainer container;

    /**
     * Convenient method to create a wagon
     * @param url
     * @param serverId
     * @param settings
     * @return
     * @throws WagonException
     */
    public Wagon create( String url, String serverId, Settings settings )
        throws WagonException
    {

        final Repository repository = new Repository( serverId, url );
        repository.setPermissions( getPermissions( serverId, settings ) );

        Wagon wagon = getWagon( repository.getProtocol() );

        configureWagon( wagon, serverId, settings );

        if ( logger.isDebugEnabled() )
        {
            Debug debug = new Debug();
            wagon.addSessionListener( debug );
            wagon.addTransferListener( debug );
        }

        ProxyInfo proxyInfo = getProxyInfo( settings );

        AuthenticationInfo authInfo = getAuthenticationInfo( serverId, settings );
        if ( proxyInfo != null )
        {
            wagon.connect( repository, authInfo, proxyInfo );
        }
        else
        {
            wagon.connect( repository, authInfo );
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

        String hint = protocol.toLowerCase( java.util.Locale.ENGLISH );

        Wagon wagon;
        try
        {
            wagon = container.lookup( Wagon.class, hint );
        }
        catch ( ComponentLookupException e )
        {
            throw new UnsupportedProtocolException( "Cannot find wagon which supports the requested protocol: "
                + protocol, e );
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
     * @throws TransferFailedException
     */
    private Wagon configureWagon( Wagon wagon, String repositoryId, Settings settings )
        throws TransferFailedException
    {
        logger.debug( " configureWagon " );

        // MSITE-25: Make sure that the server settings are inserted
        for ( Server server : settings.getServers() )
        {
            String id = server.getId();

            logger.debug( "configureWagon server " + id );

            if ( id != null && id.equals( repositoryId ) && ( server.getConfiguration() != null ) )
            {
                final PlexusConfiguration plexusConf =
                    new XmlPlexusConfiguration( (Xpp3Dom) server.getConfiguration() );

                ComponentConfigurator componentConfigurator = null;
                try
                {
                    componentConfigurator =
                        (ComponentConfigurator) container.lookup( ComponentConfigurator.ROLE, "basic" );

                    componentConfigurator.configureComponent( wagon, plexusConf, container.getContainerRealm() );

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
                            logger.error( "Problem releasing configurator - ignoring: " + e.getMessage() );
                        }
                    }
                }
            }
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
