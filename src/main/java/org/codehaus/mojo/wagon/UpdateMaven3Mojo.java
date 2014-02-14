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

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Work around for WAGON-407 to copy commons-io, commons-lang, and jsoup to ${maven.home}/lib/ext directory
 *
 * @goal update-maven-3
 * @requiresProject false
 */
public class UpdateMaven3Mojo
    extends AbstractMojo
{
    /**
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @parameter default-value = "${project.remoteArtifactRepositories}"
     */
    private List<ArtifactRepository> remoteRepositories;

    /**
     * @parameter default-value= "${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * commons-io:commons-io version
     * @parameter property = "commonsIoVersion" default-value = "2.2";
     */
    private String commonsIoVersion = "2.2";

    /**
     * commons-lang:commons-lang version
     * @parameter property = "commonsLangVersion" default-value = "2.6";
     */
    private String commonsLangVersion = "2.6";

    /**
     * org.jsoup:jsoup version
     * @parameter property = "jsoupVersion" default-value = "1.7.2"
     */
    private String jsoupVersion = "1.7.2";

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        updateMavenLib( this.artifactFactory.createBuildArtifact( "commons-io", "commons-io", commonsIoVersion, "jar" ) );
        updateMavenLib( this.artifactFactory.createBuildArtifact( "commons-lang", "commons-lang", commonsLangVersion,
                                                                  "jar" ) );
        updateMavenLib( this.artifactFactory.createBuildArtifact( "org.jsoup", "jsoup", jsoupVersion, "jar" ) );
    }

    private void updateMavenLib( Artifact artifact )
        throws MojoExecutionException
    {
        try
        {
            File mavenLibDir = new File( System.getProperty( "maven.home" ), "lib/ext" );
            artifactResolver.resolve( artifact, remoteRepositories, localRepository );
            this.getLog().info( "Copy " + artifact.getFile() + " to " + mavenLibDir  );
            FileUtils.copyFileToDirectory( artifact.getFile(), mavenLibDir );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Unable to download artifact", e );
        }

    }
}
