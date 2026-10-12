package org.codehaus.mojo.wagon;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class WagonMojoHttpSettingsIT extends AbstractJettyIT {

    @Test
    public void testDownload() throws Exception {
        Path projDir = prepareProject("http-download");

        executeErrorFree(projDir, "-e", "-s", "settings.xml", "-Dserver.port=" + getServerPort(), "clean", "verify");
    }

    @Override
    protected Path getDirectoryToServe() {
        return projectSource("http-download").resolve("files");
    }
}
