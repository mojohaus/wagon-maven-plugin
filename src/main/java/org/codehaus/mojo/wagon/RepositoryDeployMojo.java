package org.codehaus.mojo.wagon;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.wagon.Wagon;
import org.codehaus.mojo.wagon.shared.MavenRepoMerger;
import org.codehaus.mojo.wagon.shared.WagonUtils;

/**
 * Deploy artifacts from a local repository to pom distributionManagement repository.
 * <p>
 * This can be used typically after
 * <PRE>
 * mvn deploy -DaltDeploymentRepository=local::default::file:./target/staging-deploy
 * </PRE>
 */
@Mojo( name = "repository-deploy" , requiresProject = true)
public class RepositoryDeployMojo
    extends AbstractWagonMojo
{

    /**
     * The directory of the source repository.
     */
    @Parameter( property = "source", required = true)
    protected String source;

    /**
     * Optimize the upload by locally compressed all files in one bundle, upload the bundle, and finally remote
     * uncompress the bundle. This only works with SCP's URL
     */
    @Parameter( property = "wagon.optimize", defaultValue = "false")
    protected boolean optimize = false;

    @Component
    private MavenRepoMerger mavenRepoMerger;

    @Override
    public void execute()
        throws MojoExecutionException
    {
        if ( this.skip )
        {
            this.getLog().info( "Skip execution." );
            return;
        }

        Wagon srcWagon = null;
        Wagon targetWagon = null;
        try
        {
            srcWagon = createWagon( "default", "file:" + source );
            targetWagon = WagonUtils.createDistributionManagementWagon( wagonFactory, project, settings );

            mavenRepoMerger.merge( srcWagon, targetWagon, optimize, this.getLog() );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error during performing repository deploy", e );
        }
        finally
        {
            WagonUtils.disconnectWagon( getLog(), srcWagon );
            WagonUtils.disconnectWagon( getLog(), targetWagon );
        }
    }

}
