package org.codehaus.mojo.wagon;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Downloads files named in {@code includes} over plain HTTP, whose wagon cannot list a directory: the goal must not
 * scan the remote file system.
 */
public class WagonMojoHttpExactIT extends AbstractHttpServerIT {

    @Test
    public void testDownloadExactPaths() throws Exception {
        Path projDir = prepareProject("http-download-exact");

        executeErrorFree(projDir, "-Dserver.port=" + getServerPort(), "clean", "verify");

        Path downloads = projDir.resolve("target/it/http-download-exact");
        assertTrue(Files.exists(downloads.resolve("from-root/1.1/commons-dbutils-1.1-sources.txt")));
        assertTrue(Files.exists(downloads.resolve("from-root/1.2/commons-dbutils-1.2-sources.txt")));
        assertTrue(Files.exists(downloads.resolve("from-dir/commons-dbutils-1.1-sources.txt")));
        assertFalse(Files.exists(downloads.resolve("from-dir/common-dbutils-1.1.pom.asc")), "excludes still apply");

        String log = new String(Files.readAllBytes(projDir.resolve("build.log")), StandardCharsets.UTF_8);
        assertTrue(
                log.contains("Skipping") && log.contains("1.3/missing.txt, it does not exist"),
                "missing file is skipped");
        assertFalse(log.contains("Scanning remote file system"), "the remote file system is not scanned");
    }

    @Override
    protected Path getDirectoryToServe() {
        return projectSource("http-download").resolve("files");
    }
}
