package org.codehaus.mojo.wagon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({"3.6.3"})
public class WagonMojoHttpSettingsIT extends AbstractJettyIT {
    @Rule
    public final TestResources resources = new TestResources();

    public MavenRuntime maven;
    private final MavenRuntimeBuilder mavenBuilder;

    public WagonMojoHttpSettingsIT(MavenRuntimeBuilder builder) throws Exception {
        this.mavenBuilder = builder.withCliOptions("-B", "-e", "-s", "settings.xml");
    }

    @BeforeEach
    void setPort() throws Exception {
        this.maven = this.mavenBuilder
                .withCliOptions("-Dserver.port=" + getServerPort())
                .build();
    }

    @Test
    void download() throws Exception {
        File projDir = resources.getBasedir("http-download");
        MavenExecution mavenExec = maven.forProject(projDir);

        MavenExecutionResult result = mavenExec.execute("clean", "verify");
        result.assertErrorFreeLog();
    }

    @Override
    protected Path getDirectoryToServe() throws IOException {
        return resources.getBasedir("http-download").toPath().resolve("files");
    }
}
