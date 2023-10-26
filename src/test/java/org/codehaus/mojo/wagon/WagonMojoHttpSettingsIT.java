package org.codehaus.mojo.wagon;

import java.io.File;

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
@MavenVersions( { "3.2.5" } )
public class WagonMojoHttpSettingsIT
{
    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime maven;

    public WagonMojoHttpSettingsIT( MavenRuntimeBuilder builder )
        throws Exception
    {
        this.maven = builder.withCliOptions( "-B", "-e", "-s", "settings.xml" ).build();
    }

    @Test
    public void testDownload()
        throws Exception
    {
        File projDir = resources.getBasedir( "http-download" );
        MavenExecution mavenExec = maven.forProject( projDir );

        MavenExecutionResult result = mavenExec.execute( "clean", "verify" );
        result.assertErrorFreeLog();
    }
}
