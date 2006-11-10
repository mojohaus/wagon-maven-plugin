package org.codehaus.mojo.wagon;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
   A mojo wrapper for Wagon.  Should autoconfigure in typical Wagon style.  But maybe that doesn't work yet.
   We really only have a couple of commands with Wagon: get and put.  Thus the configuration...
   <code>
       <configuration>
           <repository/>
           <tasks>
               <task>
                   <command>put</command>
                   <localfile>bleh</localfile>
                   <remotepath>bleh</remotepath>
               </task>
               <task>
                    <command>put</command>
                    <localfile>bleh</localfile>
                    <remotepath>bleh</remotepath>
               </task>
           <tasks>
       </configuration>
   </code>

   @goal touch
   @phase process-sources
 */
public class WagonMojo
        extends AbstractMojo {

    private PlexusContainer container;

    /**
     * @parameter
     */
    private boolean interactive = true;

    /**
     * @parameter
     */
    private Task[] tasks;

    /**
     * @parameter
     */
    private Repository repository;

    public Wagon getWagon(String protocol)
            throws UnsupportedProtocolException {
        Wagon wagon;

        try {
            wagon = (Wagon) container.lookup(Wagon.ROLE, protocol);
            wagon.setInteractive(interactive);
        }
        catch (ComponentLookupException e) {
            throw new UnsupportedProtocolException(
                    "Cannot find wagon which supports the requested protocol: " + protocol, e);
        }

        return wagon;
    }


    public void execute()
            throws MojoExecutionException {

        try {
            Wagon wagon = getWagon(repository.getProtocol());
            wagon.connect(repository);
            for (int i = 0; i < tasks.length; i++) {
                Task task = tasks[i];
                if (task.getCommand().equals("get")) {
                    wagon.get(task.getRemotepath(), task.getLocalfile());
                } else if (task.getCommand().equals("put")) {
                    wagon.put(task.getLocalfile(), task.getRemotepath());
                } else {
                    throw new MojoExecutionException("Command is unsupported: " + task.getCommand());
                }

            }
            wagon.disconnect();
        } catch (UnsupportedProtocolException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ConnectionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (AuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TransferFailedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ResourceDoesNotExistException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (AuthorizationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static class Task {
        private String command;
        private File localfile;
        private String remotepath;


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
}
