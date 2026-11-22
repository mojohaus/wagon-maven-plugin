package org.codehaus.mojo.wagon;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WagonMojoHttpSettingsIT extends AbstractHttpServerIT {

    @Test
    public void testDownload() throws Exception {
        Path projDir = prepareProject("http-download");

        executeErrorFree(projDir, "-e", "-s", "settings.xml", "-Dserver.port=" + getServerPort(), "clean", "verify");

        assertTrue(Files.exists(projDir.resolve("target/it/http-download/1.1/commons-dbutils-1.1-sources.txt")));
        // usePreemptive in settings.xml: credentials go out with the first request, without waiting for a 401
        assertFalse(authorizationHeaders.isEmpty());
        assertEquals("Basic aXQtdXNlcjppdC1wYXNzd29yZA==", authorizationHeaders.get(0));
    }

    @Override
    protected Path getDirectoryToServe() {
        return projectSource("http-download").resolve("files");
    }
}
