package org.codehaus.mojo.wagon;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Uploads to and downloads from a {@code file:} URL in a project without any wagon extension, so the build only works
 * if the plugin brings the file provider itself.
 */
public class WagonMojoFileIT extends AbstractMavenIT {

    @Test
    public void testFileUploadAndDownload() throws Exception {
        Path projDir = prepareProject("file-upload");

        executeErrorFree(projDir, "-e", "clean", "verify");

        assertTrue(Files.exists(projDir.resolve("target/repo/single/pom.xml")));
        assertTrue(Files.exists(projDir.resolve("target/downloaded/pom.xml")));
    }
}
