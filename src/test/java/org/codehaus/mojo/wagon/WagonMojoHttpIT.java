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
class WagonMojoHttpIT extends AbstractJettyIT {
    @RegisterExtension
    final TestResources5 resources = new TestResources5();

    @MavenPluginTest
    void testDownload(MavenRuntimeBuilder builder) throws Exception {
        MavenRuntime maven = builder.withCliOptions("-B")
                .withCliOptions("-Dserver.port=" + getServerPort())
                .build();
        File projDir = resources.getBasedir("http-download");
        MavenExecution mavenExec = maven.forProject(projDir);

        MavenExecutionResult result = mavenExec.execute("clean", "verify");
        result.assertErrorFreeLog();

        assertTrue(
                new File(result.getBasedir(), "target/it/http-download/1.1/commons-dbutils-1.1-sources.txt").exists());
    }

    @Override
    protected Path getDirectoryToServe() throws IOException {
        return resources.getBasedir("http-download").toPath().resolve("files");
    }
}
