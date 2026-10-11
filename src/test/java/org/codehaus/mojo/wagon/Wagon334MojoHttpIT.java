package org.codehaus.mojo.wagon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import io.takari.maven.testing.TestResources5;
import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenPluginTest;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@MavenVersions({"3.9.16"})
class Wagon334MojoHttpIT extends AbstractJettyIT {
    @RegisterExtension
    final TestResources5 resources = new TestResources5();

    @MavenPluginTest
    void testDownload(MavenRuntimeBuilder builder) throws Exception {
        MavenRuntime maven = builder.withCliOptions("-B")
                .withCliOptions("-Dserver.port=" + getServerPort())
                .build();
        // issue: https://github.com/mojohaus/wagon-maven-plugin/issues/39
        File projDir = resources.getBasedir("http-download-02");
        MavenExecution mavenExec = maven.forProject(projDir);

        MavenExecutionResult result = mavenExec.execute("clean", "verify");
        result.assertErrorFreeLog();

        File downloadDir = new File(result.getBasedir(), "target/it/http-download/");
        assertTrue(new File(downloadDir, "commons-dbutils-1.2-bin.tar.gz.asc").exists());
        assertTrue(new File(downloadDir, "commons-dbutils-1.2-bin.tar.gz.asc.md5").exists());
        assertTrue(new File(downloadDir, "commons-dbutils-1.2-bin.tar.gz.asc.sha1").exists());
    }

    @Override
    protected Path getDirectoryToServe() throws IOException {
        return resources.getBasedir("http-download-02").toPath().resolve("files");
    }
}
