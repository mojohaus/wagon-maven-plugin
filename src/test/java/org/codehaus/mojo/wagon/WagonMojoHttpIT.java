package org.codehaus.mojo.wagon;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WagonMojoHttpIT extends AbstractHttpServerIT {

    @Test
    public void testDownload() throws Exception {
        Path projDir = prepareProject("http-download");

        executeErrorFree(projDir, "-Dserver.port=" + getServerPort(), "clean", "verify");

        assertTrue(Files.exists(projDir.resolve("target/it/http-download/1.1/commons-dbutils-1.1-sources.txt")));
    }

    @Override
    protected Path getDirectoryToServe() {
        return projectSource("http-download").resolve("files");
    }
}
