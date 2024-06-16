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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * A copy of stage's plugin RepositoryCopier but use WagonUpload and WagonDownload instead.
 */
@Component(role = MavenRepoMerger.class, hint = "default")
public class DefaultMavenRepoMerger implements MavenRepoMerger {
    @Requirement
    private WagonDownload downloader;

    @Requirement
    private WagonUpload uploader;

    @Override
    public void merge(Wagon src, Wagon target, boolean optimize, Log logger) throws WagonException, IOException {

        // copy src to a local dir
        File downloadSrcDir = createTempDirectory("wagon-maven-plugin");

        WagonFileSet srcFileSet = new WagonFileSet();
        srcFileSet.setDownloadDirectory(downloadSrcDir);
        // ignore archiva/nexus .index at root dir
        String[] excludes = {".*/**", "archetype-catalog.xml*"};
        srcFileSet.setExcludes(excludes);

        try {
            downloader.download(src, srcFileSet, logger);

            // merge metadata
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(downloadSrcDir);
            String[] includes = {"**/" + MAVEN_METADATA};
            scanner.setIncludes(includes);
            scanner.scan();
            String[] files = scanner.getIncludedFiles();

            for (String file : files) {
                File srcMetadaFile = new File(downloadSrcDir, file + IN_PROCESS_MARKER);

                try {
                    target.get(file.replace('\\', '/'), srcMetadaFile);
                } catch (ResourceDoesNotExistException e) {
                    // We don't have an equivalent on the targetRepositoryUrl side because we have something
                    // new on the sourceRepositoryUrl side so just skip the metadata merging.
                    continue;
                }

                try {
                    mergeMetadata(srcMetadaFile, logger);
                } catch (XmlPullParserException e) {
                    throw new IOException("Metadata file is corrupt " + file + " Reason: " + e.getMessage());
                }
            }

            // upload to target
            FileSet tobeUploadedFileSet = new FileSet();
            tobeUploadedFileSet.setDirectory(downloadSrcDir.getAbsolutePath());

            this.uploader.upload(target, tobeUploadedFileSet, optimize);

        } finally {
            FileUtils.deleteDirectory(downloadSrcDir);
        }
    }

    private void mergeMetadata(File existingMetadata, Log logger) throws IOException, XmlPullParserException {
        File stagedMetadataFile = new File(existingMetadata.getParentFile(), MAVEN_METADATA);

        try (Reader existingMetadataReader = new FileReader(existingMetadata);
                FileWriter stagedMetadataWriter = new FileWriter(stagedMetadataFile);
                FileReader reader = new FileReader(stagedMetadataFile); ) {
            // Existing Metadata in target stage
            MetadataXpp3Reader xppReader = new MetadataXpp3Reader();
            Metadata existing = xppReader.read(existingMetadataReader);

            // Staged Metadata
            Metadata staged = xppReader.read(reader);

            // Merge and write back to staged metadata to replace the remote one
            existing.merge(staged);

            MetadataXpp3Writer metadataXpp3Writer = new MetadataXpp3Writer();
            metadataXpp3Writer.write(stagedMetadataWriter, existing);

            logger.info("Merging metadata file: " + stagedMetadataFile);

        } finally {
            existingMetadata.delete();
        }

        // Mark all metadata as in-process and regenerate the checksums as they will be different
        // after the merger

        try {
            File newMd5 = new File(stagedMetadataFile.getParentFile(), MAVEN_METADATA + ".md5");
            FileUtils.fileWrite(newMd5.getAbsolutePath(), checksum(stagedMetadataFile, MD5));

            File newSha1 = new File(stagedMetadataFile.getParentFile(), MAVEN_METADATA + ".sha1");
            FileUtils.fileWrite(newSha1.getAbsolutePath(), checksum(stagedMetadataFile, SHA1));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // We have the new merged copy so we're good

    }

    private String checksum(File file, String type) throws IOException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance(type);
        try (InputStream is = Files.newInputStream(file.toPath())) {
            byte[] buf = new byte[8192];
            int i;
            while ((i = is.read(buf)) > 0) {
                md5.update(buf, 0, i);
            }
        }
        return encode(md5.digest());
    }

    private String encode(byte[] binaryData) {
        if (binaryData.length != 16 && binaryData.length != 20) {
            int bitLength = binaryData.length * 8;
            throw new IllegalArgumentException("Unrecognised length for binary data: " + bitLength + " bits");
        }

        StringBuilder retValue = new StringBuilder();
        for (byte aBinaryData : binaryData) {
            String t = Integer.toHexString(aBinaryData & 0xff);
            if (t.length() == 1) {
                retValue.append("0").append(t);
            } else {
                retValue.append(t);
            }
        }

        return retValue.toString().trim();
    }

    public static File createTempDirectory(String prefix) throws IOException {
        File temp = File.createTempFile(prefix, Long.toString(System.nanoTime()));
        if (!temp.delete()) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }
        if (!temp.mkdir()) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        return temp;
    }
}
