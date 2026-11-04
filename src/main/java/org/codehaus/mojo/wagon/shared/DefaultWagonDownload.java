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

import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.StringUtils;

@Named("default")
@Singleton
public class DefaultWagonDownload implements WagonDownload {

    @Override
    public List<String> getFileList(Wagon wagon, WagonFileSet fileSet, Log logger) throws WagonException {
        logger.info("Scanning remote file system: " + wagon.getRepository().getUrl() + " ...");

        WagonDirectoryScanner dirScan = new WagonDirectoryScanner();
        dirScan.setLogger(logger);
        dirScan.setWagon(wagon);
        dirScan.setExcludes(fileSet.getExcludes());
        dirScan.setIncludes(fileSet.getIncludes());
        dirScan.setCaseSensitive(fileSet.isCaseSensitive());
        dirScan.setDirectory(fileSet.getDirectory());
        if (fileSet.isUseDefaultExcludes()) {
            dirScan.addDefaultExcludes();
        }

        dirScan.scan();

        return dirScan.getFilesIncluded();
    }

    @Override
    public void download(Wagon wagon, WagonFileSet remoteFileSet, Log logger) throws WagonException {
        List<String> fileList = getExactFileList(remoteFileSet, logger);
        boolean exact = fileList != null;
        if (!exact) {
            fileList = this.getFileList(wagon, remoteFileSet, logger);
        }

        String url = wagon.getRepository().getUrl() + "/";

        if (fileList.isEmpty()) {
            logger.info("Nothing to download.");
            return;
        }

        for (String remoteFile : fileList) {
            File destination = new File(remoteFileSet.getDownloadDirectory() + "/" + remoteFile);
            destination.getParentFile().mkdirs();

            if (!StringUtils.isBlank(remoteFileSet.getDirectory())) {
                remoteFile = remoteFileSet.getDirectory() + "/" + remoteFile;
            }

            logger.info("Downloading " + url + remoteFile + " to " + destination + " ...");

            try {
                wagon.get(remoteFile, destination);
            } catch (ResourceDoesNotExistException e) {
                if (!exact) {
                    throw e;
                }
                // a scan reports only what exists, so an include that matches nothing was never an error either
                logger.warn("Skipping " + url + remoteFile + ", it does not exist.");
            }
        }
    }

    @Override
    public boolean exists(Wagon wagon, String resource) throws WagonException {
        return wagon.resourceExists(resource);
    }

    /**
     * Resolves the files of a file set without listing the remote directory, which is possible when every include is
     * an exact path. The excludes still apply to those paths, in the way the scanner applies them.
     *
     * @param fileSet the file set to resolve
     * @param logger the logger
     * @return the paths relative to the directory of the file set, or {@code null} when at least one include is a
     *         pattern (or there is no include) and the remote directory has to be scanned
     */
    static List<String> getExactFileList(WagonFileSet fileSet, Log logger) {
        String[] includes = fileSet.getIncludes();
        if (includes == null || includes.length == 0) {
            return null;
        }

        List<String> paths = new ArrayList<>();
        for (String include : includes) {
            String path = include.trim();
            if (!isExactPath(path)) {
                return null;
            }
            if (!paths.contains(path)) {
                paths.add(path);
            }
        }

        List<String> excludes = new ArrayList<>();
        if (fileSet.getExcludes() != null) {
            for (String exclude : fileSet.getExcludes()) {
                excludes.add(exclude.trim().endsWith("/") ? exclude.trim() + "**" : exclude.trim());
            }
        }
        if (fileSet.isUseDefaultExcludes()) {
            Collections.addAll(excludes, WagonDirectoryScanner.DEFAULTEXCLUDES);
        }

        List<String> files = new ArrayList<>();
        for (String path : paths) {
            if (isExcluded(path, excludes, fileSet.isCaseSensitive())) {
                logger.debug("Excluded: " + path);
            } else {
                files.add(path);
            }
        }
        Collections.sort(files);

        logger.info("Every include is an exact path, not scanning the remote file system.");
        return files;
    }

    private static boolean isExcluded(String path, List<String> excludes, boolean caseSensitive) {
        for (String exclude : excludes) {
            if (SelectorUtils.matchPath(exclude, path, caseSensitive)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether an include names one file below the directory of the file set instead of being a pattern: no
     * {@code *} or {@code ?}, no trailing slash (a directory), and no backslash, leading slash or {@code .} and
     * {@code ..} segments, which the scanner never reports and which could leave the download directory.
     */
    static boolean isExactPath(String include) {
        if (include.isEmpty()
                || include.indexOf('*') >= 0
                || include.indexOf('?') >= 0
                || include.indexOf('\\') >= 0
                || include.startsWith("/")
                || include.endsWith("/")) {
            return false;
        }
        for (String segment : include.split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                return false;
            }
        }
        return true;
    }
}
