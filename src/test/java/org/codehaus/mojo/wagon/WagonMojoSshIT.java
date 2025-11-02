package org.codehaus.mojo.wagon;

import java.io.File;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import org.junit.Rule;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({"3.6.3"})
public class WagonMojoSshIT {
    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime maven;

    public WagonMojoSshIT(MavenRuntimeBuilder builder) throws Exception {
        this.maven = builder.withCliOptions("-B", "-e", "-s", "settings.xml").build();
    }

    @Test
    @Disabled
    void ssh() throws Exception {
        File projDir = resources.getBasedir("ssh-it");
        MavenExecution mavenExec = maven.forProject(projDir);

        MavenExecutionResult result = mavenExec.execute("clean", "verify");
        result.assertErrorFreeLog();
    }
}
