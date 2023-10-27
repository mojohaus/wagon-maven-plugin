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

import java.io.IOException;

import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.mojo.wagon.shared.MavenRepoMerger;

/**
 * Merge artifacts from one Maven repository to another Maven repository.
 */
@Mojo( name = "merge-maven-repos" , requiresProject = false)
public class MergeMavenRepoMojo
    extends AbstractCopyMojo
{

    @Component
    private MavenRepoMerger mavenRepoMerger;

    @Override
    protected void copy( Wagon srcWagon, Wagon targetWagon )
        throws IOException, WagonException
    {
        mavenRepoMerger.merge( srcWagon, targetWagon, optimize, this.getLog() );
    }

}
