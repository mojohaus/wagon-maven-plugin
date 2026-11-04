package org.codehaus.mojo.wagon;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The plain HTTP wagon cannot list a directory, so {@code list} and a scanning {@code download} need a protocol that
 * can: here WebDAV, through the wagon-webdav-jackrabbit build extension.
 */
public class WagonMojoWebDavIT extends AbstractHttpServerIT {

    @Test
    public void testListAndDownload() throws Exception {
        Path projDir = prepareProject("webdav-list");

        executeErrorFree(projDir, "-Dserver.port=" + getServerPort(), "clean", "verify");

        String log = new String(Files.readAllBytes(projDir.resolve("build.log")), StandardCharsets.UTF_8);
        assertTrue(log.contains("1.2/commons-dbutils-1.2-sources.txt"), "list shows files below subdirectories");

        Path downloaded = projDir.resolve("target/it/webdav-list/1.1");
        assertTrue(Files.exists(downloaded.resolve("commons-dbutils-1.1-sources.txt")));
        assertFalse(Files.exists(projDir.resolve("target/it/webdav-list/1.2")), "only the included files are fetched");
    }

    @Override
    protected Path getDirectoryToServe() {
        return projectSource("http-download").resolve("files");
    }
}
