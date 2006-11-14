package org.codehaus.mojo.wagon;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class WagonMojoTest extends AbstractMojoTestCase {
    // ----------------------------------------------------------------------
    // Test Settings.
    //
    // Adjust these to match your environment when testing.
    // ----------------------------------------------------------------------

    private static String HOST = "dist.kink.y";
    private static String USERNAME = "tom";

    private static String PASSWORD = "tom";
    private WagonMojo mojo;

    protected AuthenticationInfo getAuthInfo() {
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();

        authenticationInfo.setUserName(USERNAME);
        authenticationInfo.setPassword(PASSWORD);

        return authenticationInfo;
    }

    protected String getTestRepositoryUrl()
            throws IOException {
        return "dav:http://" + HOST + "/test";
    }

    protected void setUp() throws Exception {
        super.setUp();

        mojo = new WagonMojo();
        
        setVariableValueToObject(mojo, "container", getContainer());

        // set up a repository
        setVariableValueToObject(mojo, "remote", getTestRepositoryUrl());

        // set up some tasks
        File localfile = File.createTempFile("temp", "txt");
        Task[] tasks = new Task[]  {
                new Task("put", localfile, localfile.getName()),
                new Task("get", localfile, localfile.getName())
        };
        setVariableValueToObject(mojo, "tasks", tasks);

    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void testMojo() throws java.lang.Exception {
        if (!isOnline()) {
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
    public static boolean isOnline() {
        try {
            Socket socket = new Socket(HOST, 80);

            socket.getOutputStream();

            socket.close();

            return true;
        }
        catch (IOException e) {
            return false;
        }
    }
}
