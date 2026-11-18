package org.codehaus.mojo.wagon.shared;

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
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMavenRepoMergerTest {

    @Test
    void mergeMetadataReadsStagedFileBeforeOpeningWriter(@TempDir Path directory) throws Exception {
        Path existingMetadata = directory.resolve("maven-metadata.xml.rip");
        Path stagedMetadata = directory.resolve("maven-metadata.xml");

        Files.write(
                existingMetadata,
                ("<metadata><groupId>com.example</groupId><artifactId>demo</artifactId>"
                                + "<versioning><latest>1.0</latest><release>1.0</release>"
                                + "<versions><version>1.0</version></versions></versioning></metadata>")
                        .getBytes(StandardCharsets.UTF_8));
        Files.write(
                stagedMetadata,
                ("<metadata><groupId>com.example</groupId><artifactId>demo</artifactId>"
                                + "<versioning><latest>2.0</latest><release>2.0</release>"
                                + "<versions><version>2.0</version></versions></versioning></metadata>")
                        .getBytes(StandardCharsets.UTF_8));

        Method method = DefaultMavenRepoMerger.class.getDeclaredMethod("mergeMetadata", File.class, Log.class);
        method.setAccessible(true);
        method.invoke(new DefaultMavenRepoMerger(), existingMetadata.toFile(), new SystemStreamLog());

        String merged = new String(Files.readAllBytes(stagedMetadata), StandardCharsets.UTF_8);
        assertTrue(merged.contains("<version>1.0</version>"));
        assertTrue(merged.contains("<version>2.0</version>"));
        assertFalse(Files.exists(existingMetadata));
    }
}
