package org.codehaus.mojo.wagon.shared;

import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.util.StringUtils;

public class WagonUtils
{

    public static WagonFileSet getWagonFileSet( String fromDir, String includes, String excludes,
                                                boolean isCaseSensitive, String toDir )
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

        fileSet.setOutputDirectory( toDir );

        return fileSet;

    }

    /**
     * create Wagon for a pom distributionManagment
     *
     * @param wagonFactory
     * @param project
     * @param settings
     * @return
     * @throws MojoExecutionException
     */
    public static  Wagon createDistributionManagementWagon( WagonFactory wagonFactory, 
                                                            MavenProject project, Settings settings) 
                    throws MojoExecutionException
    {
        DistributionManagement dist = project.getDistributionManagement();
        if ( dist == null )
        {
            throw new MojoExecutionException( "no <distributionManagement> set for using -Dmaven.target=pom" );
        }
        boolean isSnapshot = project.getVersion().endsWith("-SNAPSHOT");
        DeploymentRepository repo = ( isSnapshot )? dist.getSnapshotRepository() : dist.getRepository();
        String repoId = repo.getId();
        String url = repo.getUrl();
        if ( url == null )
        {
            String repoTag = ( isSnapshot )? "snapshotRepository" : "repository";
            throw new MojoExecutionException( "no <distributionManagement><" + repoTag + "><url> set" );
        }
        Wagon targetWagon = createWagon( wagonFactory, repoId, url, settings );
        return targetWagon;
    }

    public static Wagon createWagon( WagonFactory wagonFactory, String id, String url, Settings settings)
                    throws MojoExecutionException
    {
        try
        {
            return wagonFactory.create( url, id, settings );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to create a Wagon instance for " + url, e );
        }
    }

    public static void disconnectWagon( Log log, Wagon wagon )
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
            log.debug( "Error disconnecting wagon - ignored", e );
        }
    }

}
