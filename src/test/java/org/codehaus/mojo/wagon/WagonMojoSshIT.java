package org.codehaus.mojo.wagon;

import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class WagonMojoSshIT extends AbstractMavenIT {

    @Test
    @Disabled
    public void testSsh() throws Exception {
        Path projDir = prepareProject("ssh-it");

        executeErrorFree(projDir, "-e", "-s", "settings.xml", "clean", "verify");
    }
}
