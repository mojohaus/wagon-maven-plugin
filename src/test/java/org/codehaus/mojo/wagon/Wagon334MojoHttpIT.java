package org.codehaus.mojo.wagon;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Wagon334MojoHttpIT extends AbstractJettyIT {

    @Test
    public void testDownload() throws Exception {
        // issue: https://github.com/mojohaus/wagon-maven-plugin/issues/39
        Path projDir = prepareProject("http-download-02");

        executeErrorFree(projDir, "-Dserver.port=" + getServerPort(), "clean", "verify");

        Path downloadDir = projDir.resolve("target/it/http-download/");
        assertTrue(Files.exists(downloadDir.resolve("commons-dbutils-1.2-bin.tar.gz.asc")));
        assertTrue(Files.exists(downloadDir.resolve("commons-dbutils-1.2-bin.tar.gz.asc.md5")));
        assertTrue(Files.exists(downloadDir.resolve("commons-dbutils-1.2-bin.tar.gz.asc.sha1")));
    }

    @Override
    protected Path getDirectoryToServe() {
        return projectSource("http-download-02").resolve("files");
    }
}
