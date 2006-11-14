package org.codehaus.mojo.wagon;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
* User: briantopping
* Date: Nov 13, 2006
* Time: 1:32:37 PM
* To change this template use File | Settings | File Templates.
*/
public class Task {
    private String command;
    private File localfile;
    private String remotepath;

    public Task() {
    }

    public Task(String command, File localfile, String remotepath) {
        this.command = command;
        this.localfile = localfile;
        this.remotepath = remotepath;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public File getLocalfile() {
        return localfile;
    }

    public void setLocalfile(File localfile) {
        this.localfile = localfile;
    }

    public String getRemotepath() {
        return remotepath;
    }

    public void setRemotepath(String remotepath) {
        this.remotepath = remotepath;
    }
}
