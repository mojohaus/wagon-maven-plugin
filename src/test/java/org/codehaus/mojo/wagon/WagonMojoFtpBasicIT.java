package org.codehaus.mojo.wagon;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WagonMojoFtpBasicIT extends AbstractMavenIT {

    private FtpServer ftpServer;

    @BeforeEach
    public void setup() throws Exception {
        ftpServer = createFtp();
        ftpServer.start();
    }

    @AfterEach
    public void teardown() {
        if (ftpServer != null && !ftpServer.isStopped()) {
            ftpServer.stop();
        }
    }

    @Test
    public void testFtpBasic() throws Exception {
        Path projDir = prepareProject("ftp-basic");

        executeErrorFree(projDir, "-e", "-s", "settings.xml", "clean", "verify");

        String downloaded = getClass().getSimpleName() + ".class";
        assertTrue(Files.exists(projDir.resolve("target/it/" + downloaded)));
        assertTrue(Files.exists(projDir.resolve("target/it/single-dir/" + downloaded)));
    }

    private FtpServer createFtp() throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory();

        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(true);

        ListenerFactory factory = new ListenerFactory();
        factory.setPort(8221);

        serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());
        serverFactory.addListener("default", factory.createListener());

        // the test project downloads this test's own class file from target/test-classes
        BaseUser user = new BaseUser();
        user.setName("anonymous");
        user.setHomeDirectory(new File("target").getAbsolutePath());
        serverFactory.getUserManager().save(user);

        return serverFactory.createServer();
    }
}
