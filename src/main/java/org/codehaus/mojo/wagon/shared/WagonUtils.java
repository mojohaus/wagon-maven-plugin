package org.codehaus.mojo.wagon.shared;

import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.observers.Debug;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.util.StringUtils;

public class WagonUtils
{
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
    public static Wagon createWagon( String id, String url, WagonManager wagonManager, Settings settings, Log logger )
        throws WagonException, UnsupportedProtocolException, WagonConfigurationException
    {
        Wagon wagon = null;

        final Repository repository = new Repository( id, url );

        wagon = wagonManager.getWagon( repository );

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

    public static WagonFileSet getWagonFileSet( String fromDir, String includes, String excludes,
                                                boolean isCaseSensitive )
    {
        WagonFileSet fileSet = new WagonFileSet();
        fileSet.setDirectory( fromDir );

        if ( !StringUtils.isBlank( includes ) )
        {
            fileSet.setIncludes( StringUtils.split( includes, "," ) );
        }

        if ( !StringUtils.isBlank( excludes ) )
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

}
