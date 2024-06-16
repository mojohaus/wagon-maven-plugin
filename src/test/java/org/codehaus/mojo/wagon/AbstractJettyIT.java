package org.codehaus.mojo.wagon;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractJettyIT {

    private Server server;
    private int port;

    @Before
    public void runJetty() throws Exception {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(0);
        connector.setHost("localhost");
        server.addConnector(connector);

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setResourceBase(getDirectoryToServe().toString());

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {resource_handler, new DefaultHandler()});
        server.setHandler(handlers);

        server.start();
        this.port = connector.getLocalPort();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    protected int getServerPort() {
        return this.port;
    }

    protected abstract Path getDirectoryToServe() throws IOException;
}
