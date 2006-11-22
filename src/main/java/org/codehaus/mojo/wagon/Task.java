package org.codehaus.mojo.wagon;

import java.io.File;

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
public class Task
{
    private String command;

    private File localfile;

    private String remotepath;

    public Task()
    {
    }

    public Task( String command, File localfile, String remotepath )
    {
        this.command = command;
        this.localfile = localfile;
        this.remotepath = remotepath;
    }

    public String getCommand()
    {
        return command;
    }

    public void setCommand( String command )
    {
        this.command = command;
    }

    public File getLocalfile()
    {
        return localfile;
    }

    public void setLocalfile( File localfile )
    {
        this.localfile = localfile;
    }

    public String getRemotepath()
    {
        return remotepath;
    }

    public void setRemotepath( String remotepath )
    {
        this.remotepath = remotepath;
    }
}
