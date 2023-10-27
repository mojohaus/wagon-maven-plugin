package org.codehaus.mojo.wagon;

import java.io.File;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

@RunWith( MavenJUnitTestRunner.class )
@MavenVersions( { "3.6.3" } )
public class Wagon334MojoHttpIT extends AbstractJettyIT
{
    @Rule
    public final TestResources resources = new TestResources();

    private final MavenRuntimeBuilder mavenBuilder;
    public MavenRuntime maven;

    public Wagon334MojoHttpIT(MavenRuntimeBuilder builder ) {
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
        // issue: https://github.com/mojohaus/wagon-maven-plugin/issues/39
        File projDir = resources.getBasedir( "http-download-02" );
        MavenExecution mavenExec = maven.forProject( projDir );

        MavenExecutionResult result = mavenExec.execute( "clean", "verify" );
        result.assertErrorFreeLog();

        File downloadDir = new File(result.getBasedir(), "target/it/http-download/");
        Assert.assertTrue( new File(downloadDir, "commons-dbutils-1.2-bin.tar.gz.asc" ).exists() );
        Assert.assertTrue( new File(downloadDir, "commons-dbutils-1.2-bin.tar.gz.asc.md5" ).exists() );
        Assert.assertTrue( new File(downloadDir, "commons-dbutils-1.2-bin.tar.gz.asc.sha1" ).exists() );
    }

    @Override
    protected Path getDirectoryToServe() throws IOException {
        return resources.getBasedir("http-download-02").toPath()
            .resolve("files");
    }
}
