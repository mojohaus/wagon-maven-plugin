package org.codehaus.mojo.wagon;

import static java.util.Objects.requireNonNull;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( MavenJUnitTestRunner.class )
@MavenVersions( { "3.2.5" } )
public class WagonMojoHttpIT extends AbstractJettyIT
{
    @Rule
    public final TestResources resources = new TestResources();
    private final MavenRuntimeBuilder mavenBuilder;

    public MavenRuntime maven;

    public WagonMojoHttpIT( MavenRuntimeBuilder builder )
        throws Exception
    {
        this.mavenBuilder = builder.withCliOptions( "-B" );
    }

    @Before
    public void setPort() throws Exception {
        this.maven = this.mavenBuilder.withCliOptions( "-Dserver.port=" + getServerPort() ).build();
    }

    @Test
    public void testDownload()
        throws Exception
    {
        File projDir = resources.getBasedir( "http-download" );
        MavenExecution mavenExec = maven.forProject( projDir );

        MavenExecutionResult result = mavenExec.execute( "clean", "verify" );
        result.assertErrorFreeLog();

        Assert.assertTrue( new File( result.getBasedir(),
                        "target/it/http-download/1.1/commons-dbutils-1.1-sources.txt" ).exists() );
    }

    @Override
    protected Path getDirectoryToServe() throws IOException {
        return resources.getBasedir("http-download").toPath()
            .resolve("files");
    }
}
