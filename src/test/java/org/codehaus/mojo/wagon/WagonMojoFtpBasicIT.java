package org.codehaus.mojo.wagon;

import java.io.File;

import io.takari.maven.testing.TestResources5;
import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenPluginTest;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@MavenVersions({"3.9.16"})
class WagonMojoFtpBasicIT {
    @RegisterExtension
    final TestResources5 resources = new TestResources5();

    private FtpServer ftpServer;

    @BeforeEach
    void setup() throws Exception {
        ftpServer = createFtp();
        ftpServer.start();
    }

    @AfterEach
    void teardown() {
        if (ftpServer != null && !ftpServer.isStopped()) {
            ftpServer.stop();
        }
    }

    @MavenPluginTest
    void testFtpBasic(MavenRuntimeBuilder builder) throws Exception {
        MavenRuntime maven =
                builder.withCliOptions("-B", "-e", "-s", "settings.xml").build();
        File projDir = resources.getBasedir("ftp-basic");
        MavenExecution mavenExec = maven.forProject(projDir);
        MavenExecutionResult result = mavenExec.execute("clean", "verify");
        result.assertErrorFreeLog();
        assertTrue(new File(result.getBasedir(), "target/it/" + this.getClass().getSimpleName() + ".class").exists());
        assertTrue(new File(
                        result.getBasedir(),
                        "target/it/single-dir/" + this.getClass().getSimpleName() + ".class")
                .exists());
        assertTrue(new File(
                        result.getBasedir(),
                        "target/it/single-dir/" + this.getClass().getSimpleName() + ".class")
                .exists());
    }

    private FtpServer createFtp() throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(true);

        ListenerFactory factory = new ListenerFactory();
        factory.setPort(8221);

        serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());
        serverFactory.addListener("default", factory.createListener());

        BaseUser user = new BaseUser();
        user.setName("anonymous");
        user.setHomeDirectory(new File("target").getAbsolutePath());
        serverFactory.getUserManager().save(user);

        return serverFactory.createServer();
    }
}
