package org.codehaus.mojo.wagon.shared;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.repository.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultWagonDownloadTest {

    @TempDir
    Path downloadDir;

    private final List<String> listed = new ArrayList<>();

    private final List<String> fetched = new ArrayList<>();

    /** A wagon that lists the given names for any directory and fetches all but {@code missing}. */
    private Wagon wagon(List<String> listing, String missing) {
        return (Wagon) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Wagon.class}, (p, m, a) -> {
            switch (m.getName()) {
                case "getRepository":
                    return new Repository("test", "http://localhost/repo");
                case "getFileList":
                    listed.add((String) a[0]);
                    return a[0].toString().isEmpty() ? listing : Collections.emptyList();
                case "get":
                    if (a[0].equals(missing)) {
                        throw new ResourceDoesNotExistException("missing " + a[0]);
                    }
                    fetched.add((String) a[0]);
                    return null;
                default:
                    throw new UnsupportedOperationException(m.getName());
            }
        });
    }

    private WagonFileSet fileSet(String directory, String[] includes, String[] excludes) {
        WagonFileSet fileSet = new WagonFileSet();
        fileSet.setDirectory(directory);
        fileSet.setIncludes(includes);
        fileSet.setExcludes(excludes);
        fileSet.setCaseSensitive(true);
        fileSet.setDownloadDirectory(downloadDir.toFile());
        return fileSet;
    }

    @Test
    void exactIncludesAreFetchedWithoutListing() throws Exception {
        WagonFileSet fileSet = fileSet("", new String[] {"b/two.txt", " one.txt "}, null);

        new DefaultWagonDownload().download(wagon(Collections.emptyList(), null), fileSet, new SystemStreamLog());

        assertEquals(Arrays.asList("b/two.txt", "one.txt"), fetched);
        assertTrue(listed.isEmpty(), "nothing is listed: " + listed);
        assertTrue(new File(downloadDir.toFile(), "b").isDirectory(), "directories are created for the files");
    }

    @Test
    void exactIncludesAreRelativeToTheDirectory() throws Exception {
        WagonFileSet fileSet = fileSet("sub/dir", new String[] {"one.txt"}, null);

        new DefaultWagonDownload().download(wagon(Collections.emptyList(), null), fileSet, new SystemStreamLog());

        assertEquals(Collections.singletonList("sub/dir/one.txt"), fetched);
        assertTrue(listed.isEmpty());
    }

    @Test
    void patternsStillScan() throws Exception {
        WagonFileSet fileSet = fileSet("", new String[] {"*.txt"}, null);

        new DefaultWagonDownload()
                .download(wagon(Arrays.asList("a.txt", "b.jar"), null), fileSet, new SystemStreamLog());

        assertEquals(Collections.singletonList("a.txt"), fetched);
        assertFalse(listed.isEmpty());
    }

    @Test
    void oneNonExactIncludeScansForAll() throws Exception {
        WagonFileSet fileSet = fileSet("", new String[] {"a.txt", "*.jar"}, null);

        new DefaultWagonDownload()
                .download(wagon(Arrays.asList("a.txt", "b.jar", "c.txt"), null), fileSet, new SystemStreamLog());

        assertEquals(Arrays.asList("a.txt", "b.jar"), fetched);
        assertFalse(listed.isEmpty());
    }

    @Test
    void noIncludesScans() {
        assertNull(DefaultWagonDownload.getExactFileList(fileSet("", null, null), new SystemStreamLog()));
    }

    @Test
    void excludesApplyToExactPaths() throws Exception {
        WagonFileSet fileSet =
                fileSet("", new String[] {"keep.txt", "drop.txt", "dir/drop.txt", "dir/keep.txt~"}, new String[] {
                    "**/drop.txt", "drop.txt"
                });

        new DefaultWagonDownload().download(wagon(Collections.emptyList(), null), fileSet, new SystemStreamLog());

        // dir/keep.txt~ is a default exclude
        assertEquals(Collections.singletonList("keep.txt"), fetched);
        assertTrue(listed.isEmpty());
    }

    @Test
    void defaultExcludesCanBeSwitchedOff() throws Exception {
        WagonFileSet fileSet = fileSet("", new String[] {"keep.txt~"}, null);
        fileSet.setUseDefaultExcludes(false);

        new DefaultWagonDownload().download(wagon(Collections.emptyList(), null), fileSet, new SystemStreamLog());

        assertEquals(Collections.singletonList("keep.txt~"), fetched);
    }

    @Test
    void missingExactFileIsSkipped() throws Exception {
        WagonFileSet fileSet = fileSet("", new String[] {"one.txt", "gone.txt", "two.txt"}, null);

        new DefaultWagonDownload().download(wagon(Collections.emptyList(), "gone.txt"), fileSet, new SystemStreamLog());

        assertEquals(Arrays.asList("one.txt", "two.txt"), fetched);
    }

    @Test
    void exactPaths() {
        for (String exact : new String[] {"a", "a.txt", "a/b/c-1.0.jar", "a b.txt", "a+b[1].txt"}) {
            assertTrue(DefaultWagonDownload.isExactPath(exact), exact);
        }
        for (String pattern : new String[] {
            "", "*", "**", "a/**", "*.txt", "a?.txt", "dir/", "/a.txt", "a//b", "./a", "a/./b", "../a", "a/..", "a\\b"
        }) {
            assertFalse(DefaultWagonDownload.isExactPath(pattern), pattern);
        }
    }
}
