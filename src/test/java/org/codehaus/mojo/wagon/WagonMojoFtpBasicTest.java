package org.codehaus.mojo.wagon;

import java.io.File;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.After;
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
@MavenVersions( { "3.2.5" } )
public class WagonMojoFtpBasicTest
{
    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime maven;
    public final FtpServer ftpServer;

    public WagonMojoFtpBasicTest( MavenRuntimeBuilder builder )
        throws Exception
    {
        this.ftpServer = createFtp();
        this.maven = builder.withCliOptions( "-B", "-e", "-s", "settings.xml" ).build();
    }

    @Before
    public void setup() throws Exception  {
        ftpServer.start();
    }

    @After
    public void teardown() {
        if (ftpServer != null && !ftpServer.isStopped()){
            ftpServer.stop();
        }
    }

    @Test
    public void testFtpBasic()
        throws Exception
    {
        File projDir = resources.getBasedir( "ftp-basic" );
        MavenExecution mavenExec = maven.forProject( projDir );
        MavenExecutionResult result = mavenExec.execute( "clean", "verify" );
        result.assertErrorFreeLog();
        Assert.assertTrue( new File( result.getBasedir(), "target/it/WagonMojoFtpBasicTest.class" ).exists() );
        Assert.assertTrue( new File( result.getBasedir(), "target/it/single-dir/WagonMojoFtpBasicTest.class" ).exists() );
        Assert.assertTrue( new File( result.getBasedir(), "target/it/single-dir/WagonMojoHttpTest.class" ).exists() );

    }

    private FtpServer createFtp()
        throws FtpException
    {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled( true );

        ListenerFactory factory = new ListenerFactory();
        factory.setPort( 8221 );

        serverFactory.setConnectionConfig( connectionConfigFactory.createConnectionConfig() );
        serverFactory.addListener( "default", factory.createListener() );

        BaseUser user = new BaseUser();
        user.setName( "anonymous" );
        user.setHomeDirectory( new File( "target" ).getAbsolutePath() );
        serverFactory.getUserManager().save( user );

        return serverFactory.createServer();
    }
}
