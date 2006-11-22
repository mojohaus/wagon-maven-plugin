package org.codehaus.mojo.wagon;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class WagonMojoTest
    extends AbstractMojoTestCase
{
    // ----------------------------------------------------------------------
    // Test Settings.
    //
    // Adjust these to match your environment when testing.
    // ----------------------------------------------------------------------

    private static String HOST = "dist.kink.y";

    private static String USERNAME = "tom";

    private static String PASSWORD = "tom";

    private WagonMojo mojo;

    protected AuthenticationInfo getAuthInfo()
    {
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();

        authenticationInfo.setUserName( USERNAME );
        authenticationInfo.setPassword( PASSWORD );

        return authenticationInfo;
    }

    protected String getTestRepositoryUrl()
        throws IOException
    {
        return "dav:http://" + HOST + "/test";
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        mojo = new WagonMojo();

        setVariableValueToObject( mojo, "container", getContainer() );

        // set up a repository
        setVariableValueToObject( mojo, "remote", getTestRepositoryUrl() );

        // set up some tasks
        File localfile = File.createTempFile( "temp", "txt" );
        Task[] tasks = new Task[]{new Task( "put", localfile, localfile.getName() ),
            new Task( "get", localfile, localfile.getName() )};
        setVariableValueToObject( mojo, "tasks", tasks );

    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void testMojo()
        throws java.lang.Exception
    {
        if ( !isOnline() )
        {
            return;
        }

        mojo.execute();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * Tests to see if the host that we're testing against actually is up.
     */
    public static boolean isOnline()
    {
        try
        {
            Socket socket = new Socket( HOST, 80 );

            socket.getOutputStream();

            socket.close();

            return true;
        }
        catch ( IOException e )
        {
            return false;
        }
    }
}
