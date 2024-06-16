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

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Upload a single file with option to change name
 */
@Mojo(name = "upload-single", requiresProject = false)
public class UploadSingleMojo extends AbstractSingleWagonMojo {
    /**
     * Path to a local file to be uploaded.
     */
    @Parameter(property = "wagon.fromFile", required = true)
    private File fromFile;

    /**
     * Relative path to the URL. When blank, default to fromFile's file name.
     */
    @Parameter(property = "wagon.toFile")
    private String toFile;

    @Override
    protected void execute(Wagon wagon) throws WagonException {
        if (this.skip) {
            this.getLog().info("Skip execution.");
            return;
        }

        if (StringUtils.isBlank(toFile)) {
            toFile = fromFile.getName();
        }

        this.getLog()
                .info("Uploading: " + fromFile + " " + wagon.getRepository().getUrl() + "/" + toFile);

        wagon.put(fromFile, toFile);
    }
}
