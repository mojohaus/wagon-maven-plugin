package org.codehaus.mojo.wagon;

import java.io.File;

import io.takari.maven.testing.TestResources5;
import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenPluginTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.RegisterExtension;

@MavenVersions({"3.9.16"})
class WagonMojoSshIT {
    @RegisterExtension
    final TestResources5 resources = new TestResources5();

    @MavenPluginTest
    @Disabled
    void testSsh(MavenRuntimeBuilder builder) throws Exception {
        MavenRuntime maven =
                builder.withCliOptions("-B", "-e", "-s", "settings.xml").build();
        File projDir = resources.getBasedir("ssh-it");
        MavenExecution mavenExec = maven.forProject(projDir);

        MavenExecutionResult result = mavenExec.execute("clean", "verify");
        result.assertErrorFreeLog();
    }
}
