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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * A copy of stage's plugin RepositoryCopier but use WagonUpload and WagonDownload instead.
 */
@Named("default")
@Singleton
public class DefaultMavenRepoMerger implements MavenRepoMerger {
    @Inject
    private WagonDownload downloader;

    @Inject
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
            DefaultMavenRepoMerger.deleteDirectory(downloadSrcDir.toPath());
        }
    }

    private void mergeMetadata(File existingMetadata, Log logger) throws IOException, XmlPullParserException {
        File stagedMetadataFile = new File(existingMetadata.getParentFile(), MAVEN_METADATA);

        try {
            MetadataXpp3Reader xppReader = new MetadataXpp3Reader();
            Metadata existing;
            Metadata staged;

            // Read both files before opening the writer: constructing FileWriter truncates the
            // staged file, which would leave the second read with no data at all.
            try (Reader existingMetadataReader = new FileReader(existingMetadata);
                    Reader stagedMetadataReader = new FileReader(stagedMetadataFile)) {
                existing = xppReader.read(existingMetadataReader);
                staged = xppReader.read(stagedMetadataReader);
            }

            existing.merge(staged);

            try (Writer stagedMetadataWriter = new FileWriter(stagedMetadataFile)) {
                new MetadataXpp3Writer().write(stagedMetadataWriter, existing);
            }

            logger.info("Merging metadata file: " + stagedMetadataFile);
        } finally {
            existingMetadata.delete();
        }

        // Mark all metadata as in-process and regenerate the checksums as they will be different
        // after the merger

        try {
            File newMd5 = new File(stagedMetadataFile.getParentFile(), MAVEN_METADATA + ".md5");
            Files.write(newMd5.toPath(), checksum(stagedMetadataFile, MD5).getBytes(StandardCharsets.UTF_8));

            File newSha1 = new File(stagedMetadataFile.getParentFile(), MAVEN_METADATA + ".sha1");
            Files.write(newSha1.toPath(), checksum(stagedMetadataFile, SHA1).getBytes(StandardCharsets.UTF_8));
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

    static void deleteDirectory(final Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        final IOException[] failure = new IOException[1];
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    failure[0] = e;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                failure[0] = exc;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                if (exc != null) {
                    failure[0] = exc;
                }
                try {
                    Files.delete(dir);
                } catch (IOException e) {
                    failure[0] = e;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        if (failure[0] != null) {
            throw failure[0];
        }
    }
}
